Implementation is paused while the budget hold is active.

# Budget Hold Policy

This policy records who controls the budget, what may still happen while the hold is active, what is forbidden, and the order work resumes in.

## Project owner authority

The project owner is the release authority for this port. Only the project owner may clear the budget hold, authorize implementation spending, accept the Minecraft EULA, or approve any distribution. No agent or coordinator may clear the hold on its own.

## Allowed activities while the hold is active

- Source reading: inspecting the port source, `src/main/java_old/`, and the documents in this repository.
- Parity evidence cataloging: recording BETA26 observation metadata in `reference-catalog.md` and maintaining the evidence queue in `parity-evidence-index.md`.
- Documentation: writing and maintaining the pipeline records, plans, handoffs, and notes under `docs/`.
- Local non-game pipeline validation: `git diff --check`, `Update-FileSystemMap.ps1 -Check`, and `Test-AgentPipeline.ps1`, which run without Gradle or a game instance; only the `Documentation-only` tier of `validation-matrix.md` is executable during the hold.
- BETA26 reference-evidence pass: discovery and cataloging of reference material, setup of the unmodified reference environment, and observation runs of that environment to capture behavior evidence (owner-authorized 2026-09-18; see Owner instructions).

## Prohibited activities while the hold is active

- Gameplay changes: any edit under `src/main/java/`, `src/main/resources/`, or gameplay-affecting configuration.
- Gradle and game runs: build, test, GameTest, runClient, runServer, or any other task that starts the port's development runtime or exercises the port. (The owner-authorized reference-evidence pass is exempt for the unmodified BETA26 reference environment only.)
- EULA acceptance: any action that accepts the Minecraft EULA.
- Distribution: publishing, packaging for others, or sharing builds or test artifacts publicly.
- Original asset copying: importing BETA26 binaries, assets, decompiled files, or recordings into the repository.

## Reactivation condition

The hold is lifted only by an explicit project-owner instruction. Until that instruction is given, the hold stays in force, every blocked item stays blocked, and no work item may be claimed or marked `READY`.

## Owner instructions

- **2026-09-18, reference-evidence pass authorized.** Implementation remains paused. The owner authorized the BETA26 reference-evidence pass only: reference-material discovery and cataloging, setup of an unmodified Minecraft 1.12.2 + Forge + Thaumcraft 6.1.BETA26 reference environment, and observation runs of that environment to capture behavior evidence for identified workboard items. EULA acceptance remains the owner's action alone. The port's own Gradle and game runs remain prohibited until the hold is cleared.

## Spending order after reactivation

1. `FND-01`
2. `FND-04`
3. `RSR-01/RSR-02`
4. `ALC-00/ALC-03`
5. `CAS-01/CAS-02`
6. The remaining workboard order.

(Within each tier, items are worked in workboard dependency order.)

The tiers follow the workboard dependency spine and the deferred register's resume guidance.
