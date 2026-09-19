# FND-04 container-capacities implementation plan

> For agentic workers: REQUIRED SUB-SKILL superpowers:subagent-driven-development. Owner requires a fresh builder and a separate critic, review before commit, and continuous execution.

**Goal:** Census every aspect container against its BETA26 counterpart (capacity, clamp/leftover, acceptance, empty clearing, amount persistence), repair every mismatch RED-first, and give already-correct behavior labelled GREEN controls.
**Architecture:** No new production types. Four narrow repairs in `TileJarVoid`, `TileAlembic` and `ItemPhial`; one new GameTest class of behaviour witnesses; audit, design and evidence documents.
**Tech Stack:** Minecraft 1.20.1, Forge 47.3.0, Java 17, existing JUnit 5 and Forge GameTests.
**Spec:** docs/superpowers/specs/2026-09-19-fnd04-container-capacities-design.md.

## Global constraints

- Repository D:/dev/thaumcraft-shobie-review; branch codex/fnd-04-aspect-lookup; base d3be879. Preserve every reviewed behavior: object-tag store, resolver, entity store, config restorations, culling, tags, container lookups.
- FND04 remains ACTIVE; no dependency release or full parity claim. No EULA, ordinary server, distribution, remote Git changes, dependency changes, or retention of original code/assets.
- Authority: `src/main/java_old/common/tiles/essentia/{TileJarFillable,TileJarFillableVoid,TileAlembic,TileCentrifuge,TileTubeBuffer,TileSmelter}.java` and `src/main/java_old/common/items/ItemTCEssentiaContainer.java`; read directly, re-implement observable behavior, never copy.
- No production edit before behavioral RED for every fix. Already-correct behavior gets GREEN control witnesses only, labelled honestly.
- Scope: capacities, clamp/leftover arithmetic, acceptance gating, empty clearing, amount persistence. Not device flow (ALC lanes), GUI, rendering or other stores.
- Builder never commits and never spawns agents; root owns coordination documents and the final commit.

## Review focus

- Census completeness: every `IAspectContainer`/`IAspectSource` tile and every `IEssentiaContainerItem` item is a row, including port-only containers and the smelter buffer that implements neither interface.
- Honest labels: fixes carry RED then GREEN; matches carry controls labelled as controls; reported differences state why they were not changed.
- Clamp fidelity: jar and void-jar arithmetic against BETA26 (250, overflow leftover for the jar, destroyed overflow for the void jar), alembic 128, tube buffer 10, smelter 256.
- Atomic transfers: a refused phial transfer must not mutate the target or consume the phial.
- Evidence: RED logs before each production edit, GREEN after, the full audit table with file:line references, and the gate counts (45 unit tests / 8 classes; 46 pre-existing GameTests).

## Task 1: Census and audit table

**Files:**
- No production edits. Findings recorded in the evidence document.

- [x] Enumerate port implementers by search: `TileJar`, `TileJarVoid`, `TileAlembic`, `TileCentrifuge`, `TileTubeBuffer`, `TileSmelter`, `TileEssentiaReservoir`, `TileCrucible`, `TileThaumatorium`, `TileThaumatoriumTop`, `TileInfusionMatrix`, `TilePotionSprayer`, `TileRechargePedestal`, `TileMirrorEssentia`, `ItemPhial`, `ItemEssentiaContainer`, `ItemCrystalEssence`, `ItemVisCrystal`, `ItemLabel`, with `TileEssentiaInput`/`TileEssentiaOutput`/`TileJarBrain` recorded as non-containers.
- [x] Compare capacity, clamp/leftover, acceptance, empty clearing and persistence against each BETA26 counterpart with file:line references; record verdict match / differ / port-only.

## Task 2: RED first (witnesses only)

**Files:**
- Create `src/gametest/java/thaumcraft/gametest/ContainerCapacityGameTests.java` - seventeen GameTests covering jar, void jar, alembic, centrifuge control, tube buffer, smelter and the phial-style items.
- Do not touch production code.

- [x] First RED run (fixtures before adaptation): 63 discovered, 7 failed - three behavioural failures (the F1, F2 and F4 witnesses) plus four fixture failures on the ALC-02 factory stubs (three alembic tests and the buffer test); F3's behavioural failure surfaced in the second round. Log `build/gametest/fnd04-cap-red-1-fixtures-runGameTestServer.log`.
- [x] Fixture adaptation only (attach the registered alembic/buffer tile to the placed block instead of relying on the deferred block factories): 63 discovered, 4 failed, one per planned fix. Log `build/gametest/fnd04-cap-red-2-runGameTestServer.log`.

## Task 3: Implement the repairs

**Files:**
- Modify `src/main/java/thaumcraft/common/tiles/essentia/TileJarVoid.java` - drop the `addToContainer` filter gate (F1); account transport adds as `amount - leftover` (F2).
- Modify `src/main/java/thaumcraft/common/tiles/essentia/TileAlembic.java` - unconditional acceptance query (F3).
- Modify `src/main/java/thaumcraft/common/items/consumables/ItemPhial.java` - jar remaining-capacity guard plus rollback of partial absorption for other sources (F4).

- [x] F1 and F2 implemented against the RED messages `BETA26 void jars void any aspect accepted into an empty jar, got leftover 3` and `BETA26 addEssentia returns amount - leftover; a refused aspect must report 0 consumed`.
- [x] F3 implemented against the RED message `BETA26 TileAlembic.doesContainerAccept is unconditional even with a filter set`.
- [x] F4 implemented against the RED message `a refused phial must not change the jar, got 250`.

## Task 4: Docs, map and gates

- [x] Design: `docs/superpowers/specs/2026-09-19-fnd04-container-capacities-design.md`.
- [x] Plan: this file.
- [x] Evidence with the audit table: `docs/agent-pipeline/fnd-04-container-capacities-evidence.md`.
- [x] Regenerate `docs/agent-pipeline/filesystem-map.md` via `tools/agent-pipeline/Update-FileSystemMap.ps1`.
- [x] `./gradlew.bat runGameTestServer` -> 63 discovered, all 63 required tests passed. Log `build/gametest/fnd04-cap-green-runGameTestServer.log`.
- [x] `./gradlew.bat test --rerun-tasks` -> 45 tests / 8 classes, 0 failures. Log `build/gametest/fnd04-cap-green-unit.log`.
- [x] Final full gates on the finished tree, CRLF on every touched file, `git diff --check` clean.
- [x] Leave the working tree for critic review; no commit, no staged changes.
