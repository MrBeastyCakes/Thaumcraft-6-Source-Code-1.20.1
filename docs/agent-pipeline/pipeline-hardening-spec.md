# Agent Pipeline Hardening Specification

## Purpose

Make the existing parity pipeline safe to run with multiple agents after the budget hold ends. The result is a set of tracked coordination records, a measurable parity inventory, and local checks that prove pipeline structure without starting Minecraft or modifying gameplay code.

## Scope

This work adds the following control surfaces:

| Artifact | Responsibility |
|---|---|
| `docs/agent-pipeline/active-claims.md` | The sole current-owner record for active work items, scopes, worktrees, and claim timestamps. |
| `docs/agent-pipeline/handoffs/README.md` | Archive rules and an exact per-item handoff format. |
| `docs/agent-pipeline/handoffs/FND-03.md` | Historical handoff for the evidence-index item already marked done. |
| `docs/agent-pipeline/tc6-feature-matrix.md` | A feature-group inventory that maps all BETA26 systems to a workboard owner and evidence state. |
| `docs/agent-pipeline/system-entrypoints.md` | Current-port, legacy-reference, data, and test entry points for each workboard lane. |
| `docs/agent-pipeline/reference-catalog.md` | Reproducible BETA26 observation metadata without copying proprietary binaries or assets. |
| `docs/agent-pipeline/validation-matrix.md` | Required validation tiers, evidence locations, and clean-state rules. |
| `docs/agent-pipeline/budget-hold.md` | Budget authority, allowed activities, reactivation condition, and spending order. |
| `tools/agent-pipeline/Update-FileSystemMap.ps1` | A deterministic map renderer with a non-mutating `-Check` mode. |
| `tools/agent-pipeline/Test-AgentPipeline.ps1` | A local structural validator for pipeline files, map freshness, IDs, dependency references, cycles, and budget-hold status. |

## Operating Rules

- The target remains Minecraft 1.20.1 Forge 47.3.0 on Java 17, with Thaumcraft 6.1.BETA26 as the intended-behavior reference.
- The pipeline must never authorize public distribution, EULA acceptance, original-asset copying, game launch, or gameplay changes.
- A worker may claim only a `READY` item after the coordinator clears the budget hold. A claim records the work item, lane, owner, worktree or branch, intended paths, timestamp, last update, dependency status, and release condition.
- An `ACTIVE`, `VERIFYING`, or `DONE` workboard item must link to its handoff record. `FND-03` may use its existing evidence index as its behavior evidence, but its archive record must identify commit `a908d65`.
- The workboard gains these currently unowned parity items: `FND-04` for aspect attribution and lookup, `ALC-00` for Crucible alchemy, `AUT-04` for standalone artifice/utility devices, `WLD-05` for creature/entity behavior, and `PLY-01` for player equipment, effects, Warp, and Warp Ward. Add the `PLY` lane to `AGENTS.md` and all validator ID patterns.
- The feature matrix uses these exact top-level groups: foundations; aspects and research; arcane crafting; Crucible alchemy; Essentia production and transport; infusion; casting and foci; golems and automation; artifice and utility devices; aura, Flux, rifts, and taint; magical world generation and flora; creatures and combat; player systems and Warp; Eldritch/endgame; client presentation; compatibility and release.
- The entrypoint map maps each workboard item to current source roots, relevant `src/main/java_old/` roots when available, registration/data roots, and its expected test type. It may say `needs discovery` only where the current source cannot support a stronger claim.
- The reference catalog stores paths, SHA-256 values, runtime version, loader/mod versions, and capture commands or scenario names. It stores no original-game binary, decompiled source, extracted asset, or recording.

## Map and Validator Contract

`Update-FileSystemMap.ps1` keeps its existing `-RepositoryRoot` parameter and gains a `[switch]$Check` parameter.

- Rendering is deterministic: the checked-in map contains no generated timestamp or pre-commit SHA.
- Without `-Check`, the script writes the expected UTF-8 map to `docs/agent-pipeline/filesystem-map.md`.
- With `-Check`, the script writes nothing. It exits successfully only when the map exists and exactly matches the expected content; otherwise it throws a clear stale-or-missing-map error.
- The map must describe on-disk test file counts as `files`, never as `tracked` files unless the script explicitly uses Git tracking data.

`Test-AgentPipeline.ps1` keeps a `-RepositoryRoot` parameter and performs no writes. It must:

1. invoke the map updater in `-Check` mode;
2. verify every required pipeline artifact exists;
3. collect canonical work-item IDs from the workboard and reject duplicates, accepting the lanes `FND`, `RSR`, `ALC`, `CAS`, `AUT`, `WLD`, `PLY`, `CLI`, and `REL`;
4. resolve all internal `BLOCKED: <ID>` references and reject absent IDs;
5. reject dependency cycles among canonical IDs;
6. reject a `READY` item while the budget-hold document says implementation is paused;
7. require an existing named handoff file for every `ACTIVE`, `VERIFYING`, or `DONE` row;
8. report counts by state and list the external gates `all gameplay lanes` and `written permission` as informational output; and
9. exit nonzero on a structural failure.

## Acceptance Criteria

- The active-claim, handoff, feature-matrix, entrypoint, reference, validation, and budget documents cross-link from the pipeline README and AGENTS guide.
- `FND-03` has an archived handoff and every future terminal workboard state has a named handoff path.
- Each top-level BETA26 feature group has exactly one owning workboard item or an explicit rationale for cross-lane ownership.
- The map update is deterministic and `-Check` succeeds in a clean repository.
- The pipeline validator succeeds in the current budget-held state and fails in isolated fixtures with a stale map, duplicate ID, missing internal dependency, cycle, or prohibited `READY` state.
- `git diff --check` succeeds. No Gradle task, Minecraft instance, EULA interaction, or source/gameplay change is part of this work.

## Out of Scope

Repairing a gameplay feature; capturing BETA26 behavior; downloading dependencies; running Minecraft; changing Gradle; accepting the EULA; moving to NeoForge; and preparing a public release are outside this specification.
