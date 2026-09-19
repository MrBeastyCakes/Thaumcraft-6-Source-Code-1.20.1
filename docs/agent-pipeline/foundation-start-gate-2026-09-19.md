# Narrow FND-04 Foundation Start Gate — 2026-09-19

## Decision

Under the project owner's existing implementation hold lift, the coordinator has determined that the independently reviewed automated foundation checkpoint `ae9a183` is sufficient to make FND-04 `READY` for scheduling and a coordinator-recorded claim. This document records that readiness decision; it does not grant implementation authority. The decision applies to that one work item using the established deterministic harness. It is not completion of FND-01, a parity finding, or a general release of FND-01 dependents.

The checkpoint recorded 4/4 passing JUnit tests, verified that an intentional JUnit failure exits nonzero, verified positive and negative GameTest behavior, and finished with 2/2 passing GameTests. The final GameTests cover registered arcane-stone placement and a pedestal inventory NBT roundtrip through a distinct registered block-entity factory. See [FND-01-evidence.md](FND-01-evidence.md) and [handoffs/FND-01.md](handoffs/FND-01.md). The reviewed client restart evidence in [FND-01-client-smoke-2026-09-19.md](FND-01-client-smoke-2026-09-19.md) remains supplemental.

## Why the start gate is narrower than FND-01 completion

FND-01 manual case 1 requires a fresh survival player, without commands or creative inventory, to obtain and place arcane stone and an arcane pedestal through intended progression. The current pedestal recipe is `thaumcraft:arcane_workbench_shaped`, requires research `INFUSION`, costs 10 Vis, consumes arcane stone and arcane-stone slabs, and produces `pedestal_arcane` (`src/main/resources/data/thaumcraft/recipes/arcane_workbench/pedestal_arcane.json:2-20`).

That research condition is a live gate in the current lookup path: `ThaumcraftCraftingManager.findMatchingArcaneRecipe` reads the recipe research key and rejects it when `ThaumcraftCapabilities.isResearchKnown` is false (`src/main/java/thaumcraft/common/lib/crafting/ThaumcraftCraftingManager.java:42,59-64`). The nearby menu TODO does not remove that lookup gate.

The research data makes the intended dependency deeper. `BASEINFUSION` depends on `UNLOCKINFUSION`; `INFUSION` depends on `BASEINFUSION`; its first stage requires both observation and theory knowledge plus a filled Aer phial; its second stage requires crafting the infusion matrix; and its third stage exposes the arcane pedestal recipe (`src/main/resources/assets/thaumcraft/research/infusion.json:4-38`). The workboard currently routes the needed repair path through FND-04, then RSR-01, then RSR-02 and RSR-03. Requiring the survival-pedestal case to pass before any of that implementation could start would make FND-01 depend operationally on its own blocked descendants.

The correction separates two questions:

- **May implementation start?** Yes, for FND-04 only, because `ae9a183` established and independently validated the unit/GameTest harness that FND-04 needs for its deterministic acceptance evidence.
- **Has the runtime foundation passed?** No. FND-01 remains `VERIFYING`, and the following existing cases remain required and unchanged:
  1. Fresh survival: obtain and place arcane stone and a pedestal through intended progression, insert one tagged or renamed item, and confirm the one-item limit.
  2. Full quit and reload: relaunch and confirm exact item identity, count, and tag.
  3. Chunk unload and reload: confirm the same inventory without loss or duplication.
  4. Dedicated server plus two clean clients: confirm the server-authoritative one-item state.
  5. Reconnect: change or retrieve the item with one client and confirm the returning client receives current state without stale or duplicated data.

FND-04 may not become `ACTIVE` until the coordinator records a claim in `active-claims.md`. It may not become `DONE`, and no parity claim may be made, without its own applicable acceptance evidence. Its completion may release only the dependencies already recorded from FND-04; this decision does not make FND-02, CAS-01, WLD-01, or any other FND-01 edge ready.

This decision does not assert that every later semantic or runtime dependency cycle has been identified or resolved. FND-04 still requires its applicable scanning-versus-smelting, container, and shared-lookup runtime evidence in addition to unit coverage before any corresponding completion or parity claim. If a later work item exposes another scheduling conflict, the coordinator must review that conflict separately against concrete source, runtime, and dependency evidence; this FND-04 start gate does not imply readiness for it.

## Continuing external gates

Nothing in this decision authorizes Minecraft EULA acceptance, ordinary dedicated-server setup, public distribution, or copying original BETA26 assets. EULA acceptance remains an explicit project-owner action, FND-01 multiplayer evidence remains pending, and public distribution remains blocked pending written rights-holder permission.

The supporting dependency analysis is recorded in `.superpowers/sdd/FND-01-plan/dependency-audit.md`; that audit is evidence for this correction, not an authority to broaden it.

## Independent review

On 2026-09-19, /root/foundation_gate_critic returned PASS after inspecting source prerequisites, original acceptance, authority limits, and the full documentation diff. The independent validator and whitespace check passed. The critic confirmed the dated historical supersession and future-dependency caveat; this approves only FND-04 scheduling, not completion or release.

