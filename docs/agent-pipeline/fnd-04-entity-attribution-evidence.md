# FND-04 entity-aspect attribution evidence

## Scope and reference

This slice reproduces the BETA26 entity-aspect lookup contract in the live port: one registration store, registration-order iteration with last-match-wins including NBT-variant entries, typed NBT value equality, the player name-hash rule, and owned copies on every lookup. It also restores the powered-creeper variant and audits the entity list against the BETA26 config.

The authority is the in-repository BETA26 source tree, read directly and paraphrased only: `src/main/java_old/api/aspects/AspectHelper.java:76-108` (`getEntityAspects`) and `src/main/java_old/api/ThaumcraftApi.java:343-378` (`EntityTags`, `EntityTagsNBT`, `registerEntityTag`). No original code or asset was copied or retained.

FND-04 remains ACTIVE. Out of scope here: scan consumers, vis drops, research/progression wiring, object-tag and resolver changes, and any multiplayer or client-runtime claim.

## Reproduction (RED, before any production edit)

Two new test files were added against the pre-repair public surface only.

`./gradlew.bat test --rerun-tasks` -> **39 tests completed, 2 failed** (35 pre-existing tests green). Log `build/gametest/fnd04-entity-red-unit.log`, failing JUnit XML copied to `build/gametest/fnd04-entity-red-unit-EntityAspectLookupTest.xml`:

- `EntityAspectLookupTest > storeAndLookupExchangeOwnedCopies()` — `Store must own a copy of the registered input ==> expected: <0> but was: <50>` (the flat map aliased the caller's list).
- `EntityAspectLookupTest > everyRegistrationPathFeedsTheStoreTheLookupReads()` — `expected: <4> but was: <3>` (a filtered registration never reached the store the lookup reads).

`./gradlew.bat runGameTestServer` -> **44 GameTests discovered, 4 failed** (all 38 pre-existing tests green) — the builder's first RED, before the entity witnesses existed. Log `build/gametest/fnd04-entity-red-runGameTestServer.log`: (the post-review ruling repairs add their own RED logs: `fnd04-entity-taint-red-runGameTestServer.log`, `fnd04-entity-wisp-red-runGameTestServer.log`)

- `playersusedeterministicnamehashpicks` — `Player lookup must return a list` (players resolved to null).
- `poweredcreepervariantoverridesbaseentry` — `powered creeper wins over the earlier base entry: expected 3 entries, got AspectList[herba=15, ignis=15]` (the variant did not exist).
- `lastmatchingvariantwinswithtypednbtequality` — `boolean filter matches the byte-1 tag and wins: expected aqua 6, got AspectList[ignis=10]` (filters were ignored; the first unfiltered entry won).
- `entitylookupreturnsownedcopies` — `caller mutation must not leak into later lookups: expected 3 entries, got AspectList[exanimis=20, humanus=10, terra=5, ignis=90]` (the store handed out its own list).

## Implementation

- `EntityAspectStore` (new, package-private) is the single ordered store: append-only registrations, each holding an owned `AspectList` copy and a snapshot of its filters normalized to NBT tags. The walk keeps the *last* match (unfiltered entries always match; filtered entries match only when every filter is present with the identical tag type and value), and returns `null` when nothing matched. Results are fresh copies.
- `AspectHelper` owns the one store. `registerEntityTag(ResourceLocation, AspectList)` appends an unfiltered entry; the new `registerEntityTag(String, AspectList, EntityTagsNBT...)` is the entry point `ThaumcraftApi.registerEntityTag` delegates to, so both paths feed the store the lookup reads. The player branch returns a fresh `MAN 4` plus three `new Random(name.hashCode())` draws of 15 from `Aspect.aspects.values()` before any store access.
- The `ResourceLocation` lookup overload is the documented instance-free projection: with no entity, NBT-variant entries cannot be evaluated and are skipped, so it returns the last unfiltered registration (or `null`). It reads the same store as the entity path.
- `CommonInternals.scanEntities` is deleted — nothing read it and it exposed a mutable list that could not uphold ownership or typed-filter discipline. `ThaumcraftApi.EntityTags` remains a public API type for addon source compatibility.
- Entity data for filters is `entity.saveWithoutId(new CompoundTag())`: full persistent state, no registry id, serialized lazily at most once per lookup and only once a filtered entry for that id is reached.

Deliberate, documented adaptations: typed filter values are expressed as Java `Byte/Short/Integer/Long/Float/Double/Boolean/String` or a `Tag` (BETA26's `Object value` was also compared typed; its own javadoc passed a tag object); boolean filters denote the byte tag Minecraft persists, which is what makes the shipped creeper/pech registrations work under a typed comparison.

## Behavioral coverage (GREEN)

`build/gametest/fnd04-entity-green-unit.log` — `BUILD SUCCESSFUL`, **45 tests, 8 classes, 0 failures** (35 pre-existing + 10 new).

| Suite | Non-vacuous checks |
| --- | --- |
| `EntityAspectLookupTest` (public API) | store owns the registered input and returns owned results; both entry points increase the store count; the instance-free projection honours order and skips variant-only ids; an absent id stays absent |
| `EntityAspectStoreTest` (pure seams) | typed matrix: byte 1 matches byte 1 and never int 1 or short 1; int 1 matches int 1 and never byte 1 or a string tag; boolean matches the byte tag; a `Tag` value is used as-is; absent keys, null values, unknown value types and null instance data never match; ordered last-match-wins with fall-through over mixed filtered/unfiltered entries; multi-filter entries need every filter; owned copies; `clear`; player rule equals an independent `new Random(name.hashCode())` recomputation for four names including MAN aggregation, and a later call is unaffected by mutating an earlier result |

`build/gametest/fnd04-entity-green-runGameTestServer.log` (the builder's run at 44) plus post-review `fnd04-entity-taint-green-runGameTestServer.log` and `fnd04-entity-wisp-green-runGameTestServer.log` — final state **`All 46 required tests passed :)`** (38 pre-existing + 8 new).

| Behavior | Non-vacuous check / expected result |
| --- | --- |
| Production base entry | real spawned `minecraft:zombie` resolves the live `ConfigAspects` registration: exactly UNDEAD20 MAN10 EARTH5 |
| Powered variant, last match wins | real spawned `minecraft:creeper`: PLANT15 FIRE15 unpowered; after the `powered` byte is set through `readAdditionalSaveData`, exactly PLANT15 FIRE15 ENERGY15 — the later filtered entry overrides the earlier unfiltered one |
| Registration order, typed equality, fall-through | real `minecraft:falling_block` with test-owned registrations in order: unfiltered FIRE10; boolean `DropItem` filter wins (WATER6); byte-0 `DropItem` fails and falls through (WATER6); Integer-1 `DropItem` fails on the byte-1 tag (WATER6); absent key fails (WATER6); Integer-1 `Time` matches the int-1 tag (LIFE3); byte-1 `Time` fails on the int-1 tag (LIFE3) |
| Unregistered entity | spawned `minecraft:armor_stand` resolves to `null`, not an empty list |
| Owned copies | mutating a zombie's returned list leaves the store and a second zombie's lookup at exactly UNDEAD20 MAN10 EARTH5 |
| Player rule | mock player result equals an independent name-hash recomputation, totals 49 (MAN 4 + three draws of 15), keeps `MAN 4 + 15k`, is identical on a repeat call, and is unaffected by mutating an earlier result |

## Config audit (BETA26 -> port)

A parser-based audit of both `ConfigAspects` files (BETA26 81 registrations, port 99 before this slice and 108 registration sites after; the wisp loop site expands to one filtered variant per registered aspect at runtime) mapped every BETA26 entity name onto its 1.20.1 registry id and compared aspects plus filters. Every BETA26 registration now has a matching port id (zero missing entities); the differences are the following.

| BETA26 entry | Port status |
| --- | --- |
| `Creeper` + `powered=1` (PLANT15 FIRE15 ENERGY15) | **Restored** with a byte-typed filter; the same aspects as BETA26 and now reachable, because BETA26's Integer filter could never match the byte tag its typed comparison required |
| `Thaumcraft.Pech` `PechType` 0/1/2 (+DESIRE5 / +AVERSION5 / +MAGIC5) | **Restored** byte-typed under the unchanged `PechType` key, after the unfiltered base entry so the variants win. The unfiltered `thaumcraft:pech` base entry itself is port-only (BETA26 registers the three filtered variants alone), so a crafted pech with an absent or unknown `PechType` byte resolves the base in the port where BETA26 returns null — unreachable in normal play because `EntityPech` always persists a byte `PechType` |
| `ItemFrame` (SENSES5 CRAFT5), `Painting` (SENSES10 CRAFT5) | **Restored**; both id and aspects map 1:1 and the lines sat commented out |
| `Guardian` + `Elder=true` (BEAST10 ELDRITCH15 WATER15) | **Not restored, cannot map cleanly**: 1.20.1 has a distinct `minecraft:elder_guardian` entity type with no `Elder` NBT key; the port already registers that type with the same aspects, so the runtime effect exists without an NBT variant |
| `Thaumcraft.Wisp` + `Type=<aspect tag>` (one registration per aspect) | **Restored under the coordinator's ruling**: per-aspect variants keyed on the port's persisted `WispType` (`tag 5 + AURA 5 + FLIGHT 5`), placed after the unfiltered entry so the matching variant wins; the unfiltered `AURA 10 FLIGHT 5` entry stays as the documented fallback for a typeless wisp (BETA26 resolves null there — deliberate, witnessed divergence). Witness `wispUsesPerTypeVariantWithDocumentedFallback`; RED/GREEN logs `fnd04-entity-wisp-{red,green}-runGameTestServer.log` |
| `Thaumcraft.TaintSeed` / `TaintSeedPrime` registered twice in BETA26 | **Restored under the coordinator's ruling**: the port now carries both pairs in the same relative order (`ConfigAspects:97-101`), so last-match-wins yields the later PLANT20 BEAST20 FLUX20 / PLANT30 BEAST30 FLUX30 entries exactly as BETA26. Witness `taintSeedDuplicatesFollowBeta26LastMatchWins`; RED/GREEN logs `fnd04-entity-taint-{red,green}-runGameTestServer.log` |
| `Stray` registered twice with identical values | No-op duplicate |
| Port-only entities (camel, sniffer, warden, allay, frog, tadpole, goat, axolotl, glow squid, piglin family, hoglin, zoglin, strider, drowned, phantom, turtle, dolphin, panda, fox, bee, cat, ravager, pillager, wandering trader, trader llama) | 1.20.1 additions with no BETA26 counterpart by definition |

## Store consistency and ownership checks

- `grep -rn scanEntities src/main/java docs/` returns only `src/main/java_old/` reference hits: the live tree has no second entity store and no writer that nothing consumes.
- The only live writers are `AspectHelper.registerEntityTag(ResourceLocation, ...)` and `AspectHelper.registerEntityTag(String, ...)`; the only live readers are the two `getEntityAspects` overloads, consumed unchanged by `ScanGeneric`, `EntityEvents` and `ToolEvents`.
- Unit and GameTest witnesses prove inputs are never mutated, results are never aliased, and later lookups are unaffected by caller mutation of earlier results.
- `git diff --check` is clean; every touched file is CRLF; the compile output carries only the pre-existing deprecation notes (identical note set in the RED and GREEN unit logs).

## Builder notes and limits

- The first GREEN GameTest attempt failed one test: the player witness asserted `MAN == 4` while a MAN draw legitimately aggregates onto the base (result `humanus=19`). The production player rule was correct; the test expectation was wrong and was corrected in the test only (assertion now pins total 49 and `MAN 4 + 15k`). That intermediate log was overwritten by the required final GREEN run, so this is a builder observation rather than retained proof.
- The pure-seam unit witnesses (`EntityAspectStoreTest`) pin seams that do not exist before the repair; the same clauses were RED-witnessed behaviorally at the GameTest level, and the public-API unit witnesses were RED before the repair.
- No scan consumer, vis-drop, research, progression, client or multiplayer behavior is claimed or changed. `getEntityTagCount()` now counts stored registrations instead of distinct ids; it is an internal diagnostic with no callers. The audit's items were disposed by the coordinator's rulings: the wisp per-WispType variants and the taint-seed duplicate pairs were restored (rows above), and the guardian-elder case stands as an equivalent 1.20.1 type split.
- No commit, no staged changes, no dependency change, no ordinary server, no EULA action, no original code or asset retained by this builder.
