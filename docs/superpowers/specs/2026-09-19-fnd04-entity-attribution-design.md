# FND-04 entity-aspect attribution design

## Goal and authority

Replace the live port's flat entity-tag map with the intended TC6 BETA26 entity attribution contract, re-implemented for Minecraft 1.20.1 / Forge 47.3.0 / Java 17.

The behavioral authority is the BETA26 reference, read directly and paraphrased here, never copied: `src/main/java_old/api/aspects/AspectHelper.java:76-108` (`getEntityAspects`) plus `src/main/java_old/api/ThaumcraftApi.java:343-378` (`EntityTags`, `EntityTagsNBT`, `registerEntityTag`). BETA26 iterates one ordered list of registrations, overrides a running match instead of stopping at the first hit, compares typed NBT values, and returns a fresh name-hash list for players.

FND-04 remains ACTIVE. This slice covers entity attribution lookup only: no scan consumers, no vis-drop wiring, no object-tag, resolver, bonus, progression or multiplayer work, and no full-parity claim. No original code or asset is retained, and this changes nothing about the release/EULA prohibition.

## Defect measured before the repair

- `AspectHelper` held `Map<String, AspectList> entityTags` and handed the stored instance out of `getEntityAspects`; there was no registration order, no NBT-variant path, and no player case.
- `ThaumcraftApi.registerEntityTag` appended every call to `CommonInternals.scanEntities`, which nothing in the live tree read, and additionally flat-registered only registrations whose `nbt` varargs were empty. Filtered (variant) registrations were therefore inert, and unfiltered ones were written twice to two different places.
- Consequence: the powered-creeper variant sat commented out in `ConfigAspects`, every player resolved to `null`, and callers received store-owned lists.

## Single store

One package-private `EntityAspectStore` (`thaumcraft.api.aspects`) owns an append-only, registration-ordered `List<Registration>`; `AspectHelper` owns the single instance (`ENTITY_TAGS`). Both entry points feed it:

- `AspectHelper.registerEntityTag(ResourceLocation, AspectList)` appends an unfiltered registration for `id.toString()`.
- `ThaumcraftApi.registerEntityTag(String, AspectList, EntityTagsNBT...)` delegates to the new `AspectHelper.registerEntityTag(String, AspectList, EntityTagsNBT...)`, which appends the same way.

`CommonInternals.scanEntities` is deleted: it published a mutable public list that no consumer read and that could not uphold ownership or typed-filter discipline. `ThaumcraftApi.EntityTags` remains a public API type for addon source compatibility, but no store is exposed. Nothing else writes entity registrations.

Registration semantics:

- Entry = `(entityName, owned AspectList copy, normalized filters or "no filters")`.
- Filters are normalized to NBT tags at registration (a snapshot), so later mutation of the caller's `EntityTagsNBT` objects or the varargs array cannot change matching.
- A null name or a null aspect list is silently dropped at registration; a null filter element or a filter whose value has no NBT equivalent is stored as a never-matching entry. Nothing throws at registration.
- Entries are never replaced, reordered or removed; `getEntityTagCount()` reports the number of stored registrations (repeated ids and variants included), and `clearTags()` empties the store along with the object-tag store.

## Lookup algorithm

Iterate the store in registration order; keep the last match:

1. Skip entries whose `entityName` does not equal the queried registry id string exactly.
2. An entry with no filters always matches; `tags = entry.aspects`, and the walk continues.
3. An entry with filters matches only when every filter is present in the entity's serialized data with the identical NBT tag type *and* value (`Tag.equals`, which is type-strict in 1.20.1: `ByteTag.equals` requires another `ByteTag`). A missing key, a type mismatch, or a null/unsupported filter value fails that filter.
4. After the walk, return an owned copy of the last match, or `null` when nothing matched.

The id is the 1.20.1 registry id string (`BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()`, e.g. `minecraft:zombie`); a null registry key returns `null`.

**Serialization.** Entity data is `entity.saveWithoutId(new CompoundTag())` — the full persistent entity state without the registry id (BETA26's `writeToNBT` plus name comparison against a separate `EntityList` id). The tag is produced lazily, at most once per lookup, and only when a filtered entry for that id has already been reached, so unfiltered-only lookups never serialize.

**Typed filter values.** The port maps a filter's Java value onto the tag it denotes: `Tag` (used as a copy), `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Boolean` (byte 1/0 — Minecraft persists booleans as bytes) and `String`. Anything else never matches. This is a deliberate, documented adaptation of BETA26's `Object value`: BETA26's comparison was already typed, and its own javadoc example passed an NBT tag object. BETA26's shipped `1` (Integer) filter for the creeper's byte-serialized `powered` flag therefore could not match under its own comparison; the port expresses the byte type explicitly so the intended variant actually fires (see the config audit).

## Player rule

`Player` (any subclass, including `ServerPlayer`) short-circuits before any registry or store access: a fresh list each call with `MAN 4` plus three draws of 15 from `Aspect.aspects.values()`, selected by `new Random(name.hashCode())` so the picks are identical on every call for a given name. A MAN draw aggregates onto the base (`MAN 4 + 15k`), exactly as BETA26's `AspectList.add` did. A null name or an empty aspect pool returns the `MAN 4` base rather than throwing. `playerAspects(String)` is package-private and pure precisely so the rule is unit-testable without an entity.

## Overload semantics under the single store

- `AspectHelper.registerEntityTag(ResourceLocation, AspectList)`: unfiltered registration; identical to the String entry point with no filters.
- `AspectHelper.getEntityAspects(ResourceLocation)`: instance-free projection. With no entity there is nothing to compare NBT against, so NBT-variant entries cannot match and are skipped; the result is the last unfiltered registration for that id, or `null` when only variant entries exist. Both overloads read the same store the entity path reads, so they can never disagree about unfiltered registrations.

This was chosen over "return the newest entry regardless of filters" because fabricating a variant's aspects without the entity's data would invent attribution the entity may not have, and over "keep a second id-keyed map" because that reintroduces the dead store this slice removes.

## Ownership and purity

- Registration copies the input list; lookups return fresh copies. Mutating a returned list never reaches the store or a later lookup, and mutating an input after registration never changes the store.
- Registered entity stacks/NBT are never mutated: serialization writes into a scratch `CompoundTag`.
- The store hands out no internal list and exposes no mutation API beyond `clear()`.

## Config audit (BETA26 ConfigAspects -> live port)

A parsing audit of both files (81 BETA26 registrations vs 108 port registration sites after this slice; the wisp loop's one site expands to one filtered variant per registered aspect at runtime) mapped every BETA26 entity name to its 1.20.1 registry id. Findings:

- Restored: the `Creeper` `powered` variant (byte-typed filter; last match wins over the earlier base entry), the three `Thaumcraft.Pech` `PechType` 0/1/2 variants (byte-typed, key name unchanged; the port's unfiltered `thaumcraft:pech` base entry is port-only — BETA26 has the three filtered variants alone — so a crafted pech with an absent or unknown `PechType` byte resolves the base in the port where BETA26 returns null; unreachable in normal play because `EntityPech` always persists a byte `PechType`), and the previously commented `minecraft:item_frame` / `minecraft:painting` base entries.
- Cannot map cleanly, reported not restored: the `Guardian` `Elder=true` variant (1.20.1 has a distinct `minecraft:elder_guardian` entity type, already registered with the BETA26 elder aspects, and no `Elder` NBT key, so the runtime effect exists without an NBT variant).
- Restored under the coordinator's ruling: the `Thaumcraft.Wisp` per-aspect variants (`tag 5 + AURA 5 + FLIGHT 5` each) keyed on the port's persisted `WispType`, one registration per registered aspect, placed after the unfiltered entry so the matching variant wins under last-match-wins. The unfiltered `AURA 10 FLIGHT 5` entry remains as the port's documented fallback for a typeless wisp; BETA26 resolves null for that crafted-NBT-only state, and the divergence is deliberate and witnessed.
- Restored under the coordinator's ruling: BETA26 registers `Thaumcraft.TaintSeed` and `Thaumcraft.TaintSeedPrime` twice; the port now carries both pairs in the same relative order (`ConfigAspects:97-101`), so last-match-wins yields the later `PLANT20 BEAST20 FLUX20` / `PLANT30 BEAST30 FLUX30` entries exactly as BETA26. Pinned by the `taintSeedDuplicatesFollowBeta26LastMatchWins` witness (RED `build/gametest/fnd04-entity-taint-red-runGameTestServer.log` -> GREEN `build/gametest/fnd04-entity-taint-green-runGameTestServer.log`).
- Every other BETA26 registration has an exact port counterpart (aspects and filters), and the port's extra entities are 1.20.1-only additions (camel, sniffer, warden, allay, piglin family, drowned, phantom, bee, cat, and so on) that have no BETA26 counterpart by definition.

## Validation

RED-first, captured before any production edit:

- Unit: `EntityAspectLookupTest` witnessed owned-copy exchange and single-store counting through the pre-repair public API (2 of 4 failing; the other 2 controls passed).
- GameTest: `EntityAspectGameTests` witnessed player attribution (`null` before), the powered-creeper variant (base value before), last-match-wins with typed NBT equality (first unfiltered entry before) and result isolation (aliased list before) against real spawned entities; 4 of the 6 new tests failed while all 38 prior tests passed.

GREEN coverage is listed in `docs/agent-pipeline/fnd-04-entity-attribution-evidence.md`. Pure seams added with the repair pin typed filter equality (byte 1 matches byte 1, never int 1; int 1 matches int 1, never byte 1; boolean denotes the byte tag; absent keys and unsupported values never match), ordered last-match-wins with fall-through, owned copies, and the player rule against an independent recomputation for several names.

## Limits

- No scan consumer, vis-drop, research or progression wiring is included; the existing consumers (`ScanGeneric`, `EntityEvents`, `ToolEvents`) keep calling the same public lookup.
- `getEntityTagCount()` now counts stored registrations rather than distinct ids; it is an internal diagnostic with no callers in the tree.
- No client/multiplayer acceptance: the headless GameTest server proves the common implementation path only.
- The audit's items were disposed by the coordinator's rulings: the wisp per-WispType variants and the taint-seed duplicate pairs were restored (see above), and the guardian-elder case stands as an equivalent 1.20.1 type split.
