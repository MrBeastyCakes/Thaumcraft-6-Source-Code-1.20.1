# FND-04 container-capacities evidence (seventh slice)

Date 2026-09-19. Branch `codex/fnd-04-aspect-lookup`, base `d3be879`. Author: builder subagent; independent critic review pending (this slice is not committed here).

## Scope and reference

This slice censuses every aspect container in the port against its BETA26 counterpart and repairs the mismatches that fall in the five owned categories: capacity numbers, clamp/leftover arithmetic, acceptance gating, empty clearing, and amount persistence. Authority is the in-repository BETA26 tree (`src/main/java_old`) read directly and paraphrased only; no original code or asset was copied or retained. Comparisons for the owned categories use `TileJarFillable`, `TileJarFillableVoid`, `TileAlembic`, `TileCentrifuge`, `TileTubeBuffer`, `TileSmelter` and `ItemTCEssentiaContainer`; the remaining implementers were compared against their own BETA26 counterparts and are reported.

FND-04 stays ACTIVE. Device flow (smelter cadence, tube routing/suction, centrifuge processing), GUI/rendering, and the ALC-lane block-entity factories are out of scope.

## Census audit table

| Container | BETA26 authority (file:line + value/semantics) | Port state (file:line + value) | Verdict | Witness |
| --- | --- | --- | --- | --- |
| `TileJar` (jar) | `TileJarFillable.java:14` CAPACITY 250; `:35`/`:48` amount read/written as short `Amount`; `:69-82` clamp `min(am, 250 - amount)` with leftover returned; `:84-96` take guard `amount >= am && tag == aspect` and clears aspect/amount at zero; `:102-104` containsAmount; `:122-124` accept filter-or-null; `:155-164` suction 64 filtered / 32 plain, 0 at cap | `TileJar.java:22` CAPACITY 250; `:51`/`:61` short `Amount`; `:127-138` same clamp; `:141-152` same take/clear; `:160` same containsAmount; `:180` same accept; `:217-220` same suction | match | `jarFillsToCapacityAndReturnsOverflowLeftover`, `jarDrainToEmptyClearsAspectAndAmount`, `jarFilterGatesAcceptanceAndSuction`, `jarAmountPersistsAsShortAcrossSaveLoad` |
| `TileJarVoid` (void jar) | `TileJarFillableVoid.java:16-37` same 250 clamp, overflow **destroyed** (leftover 0), **no filter gate**; `:39-50` suction 48/32; transport adds inherit `TileJarFillable.java:182-184` (`amount - leftover`) | `TileJarVoid.java:56-88` destroyed overflow and clamp; filter gate removed at `:59-60` (was `:60-62`); `:91-96` now reports `amount - addToContainer(...)` | **differ** (acceptance, transport accounting) -> fixed F1/F2; suction 96/64 vs 48/32 reported only | `voidJarAcceptsAnyAspectWhenEmptyDespiteFilter` (RED), `voidJarAddEssentiaReportsConsumedAmountOnly` (RED), `voidJarDestroysOverflowAndKeepsCapacityClamp` |
| `TileAlembic` | `TileAlembic.java:30` maxAmount 128; `:76-89` filter gate then `min(am, maxAmount - amount)`; `:91-108` take guard plus empty normalisation; `:116-118` containsAmount; `:126-128` `doesContainerAccept` returns true unconditionally; `:58`/`:70` short `amount` | `TileAlembic.java:24` MAX_AMOUNT 128; `:80-92` same clamp and filter gate; `:95-110` same take/clear; `:118` same containsAmount; `:133-136` now unconditional | **differ** (acceptance query) -> fixed F3 | `alembicDoesContainerAcceptIgnoresFilter` (RED), `alembicClampsAt128AndReturnsLeftover`, `alembicDrainToEmptyClearsState` |
| `TileCentrifuge` | `TileCentrifuge.java` has **no capacity constant**; `:61-69` accepts exactly one unit and decrements the offer; `:88-90` containsAmount true only for `amt == 1`; `:103-105` containerContains 1; single output slot | `TileCentrifuge.java:32` MAX_AMOUNT 10 buffered input; `:237-251` clamp to 10 with leftover; `:265-267` any amount; `:280-282` reports the buffer | **differ** (port buffered design; BETA26 single-slot) - reported, not changed (centrifuge processing lane) | `centrifugeBufferClampCharacterizationControl` (labelled control, not a parity claim) |
| `TileTubeBuffer` | `TileTubeBuffer.java:28` MAXAMOUNT 10; `:79-90` accepts only single units while `visSize() < 10`; `:92-101` take guard; `:109-111` containsAmount; `:172-204` takeEssentia caps the request at the stored amount | `TileTubeBuffer.java:25` MAX_AMOUNT 10; `:137-146` same single-unit gate; `:149-156` same take; `:164-166` same containsAmount; `:226-260` same cap | match | `bufferCapsAtTenSingleUnits` |
| `TileTubeFilter` | `TileTubeFilter.java:37-79` an inert display-only container surface (no storage; zero aspects; the filter value is shown through the tube-filter semantics) | `TileTubeFilter.java:56-99` the same inert surface | match (reported, tube lane; no owned-category difference) | none |
| `TileSmelter` (buffer) | `TileSmelter.java:48` maxVis 256; `:181-186` gate `itemVis > maxVis - vis`; `:266` vis recomputed after smelting; `:304-312` take guard refreshing vis | `TileSmelter.java:49` maxVis 256; `:212` same gate; `:239`/`:250` add plus vis refresh; `:268-276` same take | match | `smelterBufferLimitAndVisScaling` |
| `TileCrucible` | `TileCrucible.java:46` maxTags 500 with gates at `:111`/`:130`; `:312` add 0; `:316`/`:320` take false; `:324`/`:328`/`:332` queries answer false/false/0; `:336` accept true | `TileCrucible.java:42` MAX_ASPECTS 500 with gate at `:107`; `:296` add 0; `:301` take false; `:311`/`:316`/`:326` queries answer live values; `:331` accept true | capacity **match**; query surface **differ** - reported, not changed (no port consumer reads those queries on a crucible) | none (reported) |
| `TileThaumatorium` | `TileThaumatorium.java:262-270` add mirrors the selected recipe's need; `:296`/`:301` containsAmount/containerContains; `:306` accept true; `:376-381` get/setAspects | `TileThaumatorium.java:328-340` clamps a per-aspect store to MAX_ESSENTIA 64; `:365`/`:375` same queries; `:380` accept only while under 64 | **differ** (acceptance shape, per-aspect store vs recipe need) - reported, not changed | none (reported) |
| `TileThaumatoriumTop` | `TileThaumatoriumTop.java:34-72` delegates add/take/contains to the parent; `:145`/`:153` get/setAspects | `TileThaumatoriumTop.java:99-135` delegates everything to the parent | match in shape; inherits the parent's reported acceptance difference | none |
| `TileInfusionMatrix` | `TileInfusionMatrix.java:948-978` inert container (add 0, take false, containsAmount false, containerContains 0, accept true) | `TileInfusionMatrix.java:886-916` identical inert shape | match | none |
| `TilePotionSprayer` | `TilePotionSprayer.java:239-277` clamps adds to the active recipe need, no takes | `TilePotionSprayer.java:347-384` identical clamp | match | none |
| `TileRechargePedestal` | `TileRechargePedestal.java:74-118` charge projection via `IRechargable`, other queries inert | `TileRechargePedestal.java:105-150` same projection from the server stack (no client-projection branch) | match in semantics | none |
| `TileMirrorEssentia` | `TileMirrorEssentia.java:182-256` single-unit pass-through to the linked mirror, offers above 1 refused, `getAspects` null | `TileMirrorEssentia.java:239-338` same single-unit pass-through | match | none |
| `TileEssentiaReservoir` | no BETA26 counterpart in this tree | `TileEssentiaReservoir.java:30` CAPACITY 500, jar-like clamp and filter | **port-only** - reported, never changed | none |
| `TileEssentiaInput` / `TileEssentiaOutput` | transport only (`TileEssentiaInput.java:12`, `TileEssentiaOutput.java:12` implement `IEssentiaTransport`) | transport only (`TileEssentiaInput.java:23`, `TileEssentiaOutput.java:23`) | match (not containers in either tree) | none |
| `TileJarBrain` | no BETA26 counterpart | port class extends `TileThaumcraft` only, no container interface | **port-only**, non-container | none |
| `ItemPhial` | `ItemPhial.java:32` capacity 10; fill from alembic `:87` and jar `:107` require at least 10; empty-into-jar `:131` guarded by the jar's remaining capacity, adds nothing otherwise | `ItemPhial.java:35` PHIAL_CAPACITY 10; `:119` fill gate; `:156` jar remaining-capacity guard; `:160-169` atomic refusal with rollback | **differ** (transfer atomicity) -> fixed F4 | `phialEmptiesIntoJarOnlyWhenCapacityFits` (RED), `phialFillsExactlyTenFromJar`, `phialFillRejectedBelowCapacity` |
| `ItemCrystalEssence` | `ItemCrystalEssence.java:16` base 1, raw NBT aspect storage, random aspect on tick/creation | `ItemCrystalEssence.java:34` baseAmount 1, same NBT storage, random aspect on tick/craft | match | `phialNbtRoundTripsAndCapacityNumbersMatchBeta26` |
| `ItemVisCrystal` | BETA26 ships one `crystal_essence` item (base 1); the port splits primal crystals into per-aspect items | `ItemVisCrystal.java:46-51` one unit per item type | match in capacity (structural port-only split) | `phialNbtRoundTripsAndCapacityNumbersMatchBeta26` |
| `ItemLabel` | `ItemLabel.java:23` an essentia container item with capacity 1 (blank/filled variants) | `ItemLabel.java:20` a plain `Item` that is not an `IEssentiaContainerItem`; stores its chosen aspect under its own NBT key | **differ** (interface and storage shape) - reported, not changed (label interaction lane) | none |
| BETA26 `BlockJarItem` | `essentia/BlockJarItem.java:33` an `IEssentiaContainerItem` jar item (capacity 250) | no port counterpart; `common/blocks/essentia/BlockJar.java:97` keeps a drop-contents TODO | reported - jar-item lane; no port type to compare | none |
| `ItemEssentiaContainer` base | `ItemGenericEssentiaContainer.java:30` base capacity, `:42-56` raw NBT aspect list with no clamp | `ItemEssentiaContainer.java:15-32` capacity constructor plus the same raw NBT handling | match | `phialNbtRoundTripsAndCapacityNumbersMatchBeta26` |
| Registration capacities | phial 10 (`ItemPhial.java:32`), crystal essence 1 (`ItemCrystalEssence.java:16`), label 1 (`ItemLabel.java:23`) | phial 10 (`ModItems.PHIAL_EMPTY`/`PHIAL_FILLED` via `ItemPhial`), crystal essence 1, vis crystal 1; there is no container label item | match for the containers that exist | `phialNbtRoundTripsAndCapacityNumbersMatchBeta26` |

## Repairs (RED -> GREEN)

RED run 2 (`build/gametest/fnd04-cap-red-2-runGameTestServer.log`, 63 discovered, 4 failed) holds one failing witness per repair:

| Fix | Witness | RED message | Production change |
| --- | --- | --- | --- |
| F1 void jar filter gate | `voidJarAcceptsAnyAspectWhenEmptyDespiteFilter` | `BETA26 void jars void any aspect accepted into an empty jar, got leftover 3` | `TileJarVoid.addToContainer` no longer refuses a non-filter aspect; the empty/same-aspect branch and the 250 clamp are untouched |
| F2 void jar transport accounting | `voidJarAddEssentiaReportsConsumedAmountOnly` | `BETA26 addEssentia returns amount - leftover; a refused aspect must report 0 consumed` | `TileJarVoid.addEssentia` reports `amount - addToContainer(...)` |
| F3 alembic acceptance query | `alembicDoesContainerAcceptIgnoresFilter` | `BETA26 TileAlembic.doesContainerAccept is unconditional even with a filter set` | `TileAlembic.doesContainerAccept` returns true; the storage filter gate in `addToContainer` is unchanged |
| F4 phial transfer atomicity (found by this slice's RED) | `phialEmptiesIntoJarOnlyWhenCapacityFits` | `a refused phial must not change the jar, got 250` | `ItemPhial.useOn` refuses when a jar's remaining capacity is smaller than the phial and restores any partial absorption for other sources, so a refusal never mutates the target or consumes the phial. Deliberate, documented deviation: the port tests the phial's actual amount, while BETA26 always adds its fixed base (10) and tests `250 - base`, so a partially filled phial would duplicate the remainder under BETA26 - the port's variant is the non-duplicating one |

## Already-correct behavior (GREEN controls)

Controls, not repairs: jar clamp/leftover/drain/clear/filter/suction/persistence, void-jar destroyed overflow and clamp, alembic 128 clamp and drain clearing, tube-buffer single-unit 10-cap and take cap, smelter 256 buffer and take guard, phial fill gate and NBT roundtrip plus registration capacities. The single labelled non-parity control is `centrifugeBufferClampCharacterizationControl`, which pins the port's 10-unit buffer arithmetic and explicitly records the BETA26 single-slot fact.

## Run records

| Run | Command | Result | Log |
| --- | --- | --- | --- |
| Baseline (pre-change) | `./gradlew.bat runGameTestServer` | 46 GameTests discovered, all 46 required tests passed | `build/gametest/fnd04-cap-baseline-runGameTestServer.log` |
| RED 1 (witnesses added, fixtures initial, no production edit) | `./gradlew.bat runGameTestServer` | 63 discovered, 7 failed (3 behavioural - the F1, F2 and F4 witnesses - plus 4 fixture failures caused by the ALC-02 null block-entity factories: three alembic tests and the buffer test; F3's behavioural failure surfaced in round 2) | `build/gametest/fnd04-cap-red-1-fixtures-runGameTestServer.log` |
| RED 2 (fixture attachment adapted, still no production edit) | `./gradlew.bat runGameTestServer` | 63 discovered, 4 failed, exactly F1-F4 | `build/gametest/fnd04-cap-red-2-runGameTestServer.log` |
| GREEN (after the four repairs) | `./gradlew.bat runGameTestServer` | 63 discovered, all 63 required tests passed | `build/gametest/fnd04-cap-green-runGameTestServer.log` |
| Unit (after the repairs) | `./gradlew.bat test --rerun-tasks` | 45 tests / 8 classes, 0 failures | `build/gametest/fnd04-cap-green-unit.log` |
| Final unit gate (finished tree) | `./gradlew.bat compileJava test --rerun-tasks` | BUILD SUCCESSFUL; 45 tests / 8 classes, 0 failures | `build/gametest/fnd04-cap-green-final-compile-test.log` |
| Final GameTest gate (finished tree) | `./gradlew.bat runGameTestServer` | 63 discovered, all 63 required tests passed | `build/gametest/fnd04-cap-green-final-runGameTestServer.log` |

## Pipeline checks

- `Update-FileSystemMap.ps1` regenerated the filesystem map and `-Check` reports it current; the map content is unchanged because only file contents changed (the new GameTest lives outside the mapped roots and no mapped inventory moved).
- `Test-AgentPipeline.ps1` reports READY=0, ACTIVE=1, VERIFYING=1, DONE=1, DEFERRED=0, BLOCKED=29, with gameplay lanes gated externally as before.
- `git diff --check` exits 0; the only messages are Git's line-ending normalisation notes for files already modified before this slice. Every touched file is CRLF on disk.
- No new compiler warnings: the final compile log carries only the repository's known Gradle deprecation note.

## Port-only and reported findings

- `TileEssentiaReservoir` (CAPACITY 500) and `TileJarBrain` have no BETA26 counterpart; reported only.
- The port splits primal vis crystals into per-aspect items; capacity stays 1 and the BETA26 single-item shape is not reproduced here.
- `ItemLabel` is not an `IEssentiaContainerItem` in the port; its aspect storage uses its own NBT key. Reported for the label-interaction lane.
- `BlockAlembic.newBlockEntity` and `BlockTube.newBlockEntity` still return null (deferred issue ALC-02), so the alembic and tube-buffer witnesses attach the registered tile to the placed block directly. Tickers and flow remain with the ALC lane.
- Reported-only differences in this slice's own family: void-jar suction constants and the centrifuge buffered design (both deferred to the transport/processing lanes), plus the crucible and thaumatorium query/acceptance shapes.
- `TileTubeFilter` implements `IAspectContainer` in both trees with an inert display-only surface; no owned-category difference. Reported for the tube lane.
- BETA26 ships an `IEssentiaContainerItem` jar item (`BlockJarItem`, capacity 250); the port has no jar item and `BlockJar` keeps drop-contents TODOs. Reported for the jar-item lane.

## Limits

Evidence is automated (JUnit 5 unit tests plus Forge GameTests) on a dedicated GameTest server; there is no rank-1 gameplay observation and no client or multiplayer claim. Success here does not promote the rest of FND-04 or any ALC lane. The smelter's `canSmelt` capacity gate is not behaviourally reachable while ALC-01 keeps `getItemAspects` empty; the buffer witnesses therefore cover the container surface (take guard, vis tracking, GUI scaling). The F4 rollback branch for non-jar sources (`ItemPhial.java:160-169`) is witnessed by inspection only; no witness exercises it.

## Files

- Production: `TileJarVoid.java`, `TileAlembic.java`, `src/main/java/thaumcraft/common/items/consumables/ItemPhial.java` (plus the `TileJar` import there).
- Tests: `src/gametest/java/thaumcraft/gametest/ContainerCapacityGameTests.java` (17 GameTests).
- Docs: this file, `docs/superpowers/specs/2026-09-19-fnd04-container-capacities-design.md`, `docs/superpowers/plans/2026-09-19-fnd04-container-capacities.md`, regenerated `docs/agent-pipeline/filesystem-map.md`.
