# FND-04 container lookup slice evidence

This note covers only the contents-first branch of the shared item-aspect lookup. FND-04 remains `ACTIVE`; item/entity attribution, capacity behavior, scanning and smelter consumers, multiplayer agreement, and rank-1 gameplay confirmation remain outside this slice.

## Reference basis

- `REF-0001` documents the corresponding BETA26 tree branch in `ThaumcraftCraftingManager.getBonusTags`: a non-opted-out `IEssentiaContainerItem` replaces the base list with its contents before ordinary property and enchantment bonuses.
- `REF-0009` independently corroborates that branch in the shipped `Thaumcraft-1.12.2-6.1.BETA26.jar` (SHA-256 `9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f`). Read-only bytecode inspection found the interface and opt-out checks, removal of the incoming base contribution, retrieval of contained aspects, and exclusion of nonpositive contained entries. It also confirmed that the original generic item-container implementation does not opt out.
- This is rank-2 release-artifact inspection, not rank-1 observed gameplay. No reference source, bytecode, or proprietary asset was copied into the port.

## Port defect and correction

Before this slice, `getObjectTags` consulted a container only when no registered base tags existed, while `getBonusTags` always copied the selected base or generated tags. A registered item such as `VIS_CRYSTAL_AIR` therefore resolved as registered AIR 5 plus CRYSTAL 5 even though the queried item reports contained AIR 1.

The corrected `getBonusTags` gives a non-opted-out container's queried-stack contents authority over the registered or generated input. It copies positive entries into a fresh list, then applies the existing item/enchantment bonuses and the existing per-aspect cap of 500. Null and empty contents start from an empty list. The opt-out branch continues to copy base/fallback tags. The copy boundary prevents normalization or capping from mutating registered lists, item NBT, or a list owned and reused by the container.

No item opt-out flags, seed values, recipe generation, scanning, transport, or smelter code changed.

## TDD and automated evidence

The GameTests exercise `ThaumcraftCraftingManager.getObjectTags`, the production-facing shared lookup.

- RED, old production code: `./gradlew.bat runGameTestServer` exited 1 after launching 7 tests and reporting 4 required failures. The real `VIS_CRYSTAL_AIR` case returned its registered AIR 5 rather than contained AIR 1. Fixture cases also exposed base masking on the first of two differently filled stacks, generated fallback pollution for a null-returning container, and the missing positive-entry normalization/cap path. GameTest stops a method at its first failed assertion, so this RED does not claim that its later assertions ran. The opt-out control and both existing foundation tests passed. Full log: `build/gametest/fnd04-red-runGameTestServer.log` (generated evidence, not tracked).
- GREEN after the narrow fix: the same command reported all 7 required tests passed.
- A review follow-up replaced the second null-like setup with a distinct fixture that returns a non-null empty `AspectList`; the final GameTest run again passed all 7 required tests.
- Covered contracts: real seeded base versus contents precedence and seed-list immutability; two stacks of one item remaining independent; distinct null and non-null empty contents; opt-out base retention; nonpositive exclusion; cap 500; enchantment bonus retention; item-NBT immutability; and no mutation of a container-owned returned list.

Final verification on the completed slice:

- `./gradlew.bat compileJava test build`: exit 0; build successful; 4 JUnit tests with 0 failures, errors, or skips.
- `./gradlew.bat runGameTestServer`: exit 0; all 7 required GameTests passed, including the distinct non-null empty-list case.
- Production JAR inspection: `thaumcraft-6.2.0.jar` contained 3,535 entries and zero test-only `thaumcraft/gametest`, `AspectLookupGameTests`, or `fnd04_` entries.
- `Update-FileSystemMap.ps1 -Check`, `Test-AgentPipeline.ps1`, and `git diff --check`: each exited 0; the pipeline validator reported `READY=0`, `ACTIVE=1`, `VERIFYING=1`, `DONE=1`, `DEFERRED=0`, `BLOCKED=29`.
- Full generated logs remain under `build/`; the detailed builder report is `.superpowers/sdd/FND-04-container-plan/task-1-report.md`.

## Independent review

On 2026-09-19, `/root/fnd04_container_critic` returned specification PASS and code-quality PASS, approving only this focused slice before commit. The critic independently rehashed the BETA26 reference jar, checked the relevant released methods, ran `./gradlew.bat compileJava test build --rerun-tasks` (4/4 JUnit) and `./gradlew.bat runGameTestServer` (7/7 GameTests), verified all 3,535 production JAR entries excluded test fixtures, and passed map, pipeline, and whitespace checks. `run/eula.txt` remained false. No code findings remained.

Independent logs: `build/fnd04-independent-compile-test-build.log` and `build/gametest/fnd04-independent-runGameTestServer.log`. The original RED evidence limitation above was explicitly reviewed and retained. Full FND-04 acceptance remains open.
## Limits

This slice establishes deterministic port behavior and rank-2 reference corroboration for one lookup branch. It does not establish player-visible BETA26 results, the correctness of unrelated aspect seeds, recipe-derived attribution, entity attribution, scanning/smelter integration, persistence, multiplayer behavior, or full FND-04 acceptance. Those remain pending under the active work item.

## Live reference follow-up — 2026-09-19

REF-0011 now confirms the original Aer crystal calculated tooltip shows AIR1 only. See [live observation](fnd-04-live-crystal-2026-09-19.md). This narrow display result corroborates the real-crystal assertion; other containers, capacities, survival acquisition, scanning progression, smelter output, persistence and multiplayer remain unverified. The earlier automated evidence is unchanged.
