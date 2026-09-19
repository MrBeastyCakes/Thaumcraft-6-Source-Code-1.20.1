# FND-04 entity-aspect attribution implementation plan

> For agentic workers: REQUIRED SUB-SKILL superpowers:subagent-driven-development. Owner requires a fresh builder and a separate critic, review before commit, and continuous execution.

**Goal:** Reproduce the BETA26 entity-aspect lookup contract (single store, ordered last-match-wins with typed NBT filters, player rule, owned copies) and restore the powered-creeper variant.
**Architecture:** One package-private ordered `EntityAspectStore` in `thaumcraft.api.aspects`, owned by `AspectHelper`; `ThaumcraftApi.registerEntityTag` delegates to it; the dead `CommonInternals.scanEntities` list is removed. Entity data for filters is `saveWithoutId`, produced lazily at most once per lookup.
**Tech Stack:** Minecraft 1.20.1, Forge 47.3.0, Java 17, existing JUnit 5 and Forge GameTests.
**Spec:** docs/superpowers/specs/2026-09-19-fnd04-entity-attribution-design.md.

## Global constraints

- Repository D:/dev/thaumcraft-shobie-review; branch codex/fnd-04-aspect-lookup; base f0a55f2. Preserve every reviewed behavior: container lookup, public route, culling tie order, tag reload, recipe attribution and direct-boundary precedence.
- FND04 remains ACTIVE, FND01 VERIFYING; no dependency release or full parity claim. No EULA, ordinary server, distribution, remote Git changes, dependency changes, or retention of original code/assets.
- Source authority is the BETA26 `AspectHelper.getEntityAspects` / `ThaumcraftApi.EntityTags` pair; trace intent and re-implement in modern terms, never copy.
- No production edit before behavioral RED. Builder/critic never commit and never spawn agents. Root owns coordination docs and the final commit.
- Scope: entity attribution lookup only. No scan consumers (RSR lanes), vis drops, or object-tag/resolver changes.

## Review focus

- Single registration store: every registration path must feed the store the lookup reads; no writes that nothing consumes; no dead second store.
- Lookup fidelity: registration-order iteration, last-match-wins including NBT-filtered entries, typed NBT value equality (byte 1 does not equal int 1), unfiltered entries always match, no match returns null.
- Player special case: MAN 4 plus three deterministic name-hash picks at 15 from the registered aspect pool; fresh list per call.
- Ownership: lookups return owned copies; mutating a result never touches the store or later lookups; inputs never mutated.
- Config audit: semantic diff against BETA26 `ConfigAspects`, restore the powered-creeper variant and other cleanly-mappable variant registrations, report the rest.
- Evidence: RED-first runs captured before the fix; GREEN after; every pre-existing test stays green (35 unit / 6 classes; 38 GameTests).

## Task 1: RED first (tests and fixtures only)

**Files:**
- Create `src/gametest/java/thaumcraft/gametest/EntityAspectGameTests.java` — six GameTests using only the pre-repair public API: zombie base entry; powered creeper variant; ordered typed-variant walk on a real `minecraft:falling_block` fixture (test-owned registrations: byte-1 `DropItem`, byte-0 `DropItem`, Integer-1 `DropItem`, absent key, Integer-1 `Time`, byte-1 `Time`); unregistered type; owned copies; player name-hash rule via `helper.makeMockPlayer()`.
- Create `src/test/java/thaumcraft/api/aspects/EntityAspectLookupTest.java` — public-API witnesses: registration/result owned copies, both entry points feeding one store, instance-free projection, absent id.
- Do not touch production code.

- [x] Capture `./gradlew.bat test --rerun-tasks` -> 39 tests completed, 2 failed (owned copies; single-store count), 35 pre-existing green. Log `build/gametest/fnd04-entity-red-unit.log`; failing XML `build/gametest/fnd04-entity-red-unit-EntityAspectLookupTest.xml`.
- [x] Capture `./gradlew.bat runGameTestServer` -> 44 discovered, 4 failed (player null; variant filter ignored; caller mutation leaked; powered creeper base value), 38 pre-existing green. Log `build/gametest/fnd04-entity-red-runGameTestServer.log`.

## Task 2: Implement the contract

**Files:**
- Create `src/main/java/thaumcraft/api/aspects/EntityAspectStore.java` — ordered store, normalized filter snapshot, typed `Tag` equality, last-match-wins, owned copies, package-private pure seams (`matches`, `expectedTag`).
- Modify `src/main/java/thaumcraft/api/aspects/AspectHelper.java` — replace the flat map with the store; add the String entry point; player branch and pure `playerAspects(String)`; instance-free projection; `clearTags`/`getEntityTagCount` read the store.
- Modify `src/main/java/thaumcraft/api/ThaumcraftApi.java` — `registerEntityTag` delegates to the store path; document the typed filter contract.
- Modify `src/main/java/thaumcraft/api/internal/CommonInternals.java` — delete the dead `scanEntities` list and its imports.

- [x] Store, lookup, player rule, overload semantics implemented as specified in the design doc.
- [x] `compileJava test` green with the unchanged RED tests.

## Task 3: Config audit and restoration

**Files:**
- Modify `src/main/java/thaumcraft/common/config/ConfigAspects.java`.

- [x] Restore the `minecraft:creeper` `powered` variant with a byte-typed filter (BETA26's Integer filter cannot match a byte tag under typed comparison).
- [x] Restore the three `thaumcraft:pech` `PechType` variants (byte-typed, unchanged key name).
- [x] Restore the commented `minecraft:item_frame` and `minecraft:painting` base entries.
- [x] Produce the 1.12 -> 1.20.1 semantic diff (81 BETA26 registrations vs 105 port registrations at build time) and report the guardian-elder, wisp-type and taint-seed duplicate findings. Post-review coordinator rulings then restored the taint-seed duplicate pairs and the per-WispType variants (documented typeless fallback), taking the port to 108 registration sites, the wisp loop expanding to one variant per aspect at runtime; the guardian-elder case stands as an equivalent type split.

## Task 4: Focused pure-seam unit witnesses

**Files:**
- Create `src/test/java/thaumcraft/api/aspects/EntityAspectStoreTest.java`.

- [x] Typed filter equality matrix (byte/int/short/string/boolean/Tag/absent/unsupported), ordered last-match-wins with fall-through, owned copies, `clear`, and the player rule against an independent recomputation for four names including MAN aggregation. These pin seams that do not exist before the repair; the same clauses are RED-witnessed at the GameTest level in Task 1.

## Task 5: Docs, map and gates

- [x] Design: `docs/superpowers/specs/2026-09-19-fnd04-entity-attribution-design.md`.
- [x] Plan: this file.
- [x] Evidence: `docs/agent-pipeline/fnd-04-entity-attribution-evidence.md`.
- [x] Regenerate `docs/agent-pipeline/filesystem-map.md` via `tools/agent-pipeline/Update-FileSystemMap.ps1`.
- [x] `./gradlew.bat compileJava test --rerun-tasks` -> 45 tests / 8 classes, 0 failures. Log `build/gametest/fnd04-entity-green-unit.log`.
- [x] `./gradlew.bat runGameTestServer` -> the builder's run at 44 passed; after the post-review ruling repairs the final state is all 46 required tests passed. Logs `build/gametest/fnd04-entity-green-runGameTestServer.log`, `fnd04-entity-taint-green-runGameTestServer.log`, `fnd04-entity-wisp-green-runGameTestServer.log`.
- [x] `git diff --check` clean; CRLF on every touched file; no new warnings beyond the known deprecation note.
- [x] Leave the working tree for critic review; no commit, no staged changes.
