# BETA26 Parity Evidence Index

This index prevents agents from mistaking the current port's behavior for Thaumcraft 6 parity. Every workboard item must record the reference behavior it restores and the evidence that proves the repair.

## Evidence Hierarchy

1. A reproducible observation in an unmodified Thaumcraft 6.1.BETA26 environment.
2. Original BETA26 assets, research data, release notes, and author documentation.
3. The original API/source only when its provenance and behavior match BETA26.
4. The current 1.20.1 port, only as an implementation starting point or regression reference.
5. Community documentation, only when higher-ranked evidence is unavailable and the claim is marked for later confirmation.

The original author's [BETA26 release thread](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1292130-thaumcraft-6-1-beta26-no-longer-being-developed) is the initial public reference for release content, behavior changes, and distribution terms.

## Required Record for Every Repair

```text
Work item: <workboard ID>
Reference setup: <world/player/items/blocks/research needed in BETA26>
Expected BETA26 behavior: <observable action, state transition, cost, output, timing>
Reference evidence: <recording, save, release-note line, original data/source location>
Port implementation: <files/classes/resources changed>
Automated evidence: <unit test/GameTest command and result>
Runtime evidence: <fresh-world, reload, and multiplayer scenario>
Known intentional adaptation: <only a 1.20.1 platform requirement; otherwise none>
```

## Initial Evidence Queue

| Work item | Behavior to capture before or during implementation | Existing port evidence |
|---|---|---|
| FND-01 | Fresh-world, reload, dedicated-server, and remote-client test procedures | `test` currently reports no source coverage. |
| FND-04 | Shared aspect attribution (item, block, and entity sources), aspect container capacities, and lookup consumed by scanning and Essentia | `api/aspects` defines the aspect classes and container interfaces; parity of attribution and lookup is not yet verified. Rank-3 reference behavior captured in [fnd-04-evidence.md](fnd-04-evidence.md); rank-1 confirmation pending. |
| RSR-01 | Scan-gated research completion, staged knowledge, prerequisites, and page progression | `ScanningManager`, `PlayerKnowledge`, and research packets contain simplified/stubbed progression paths. Rank-3 reference behavior captured in [rsr-01-evidence.md](rsr-01-evidence.md); rank-1 confirmation pending. First live capture `REF-0005` ([reference-catalog.md](reference-catalog.md)) covers fresh-start, dust-trigger, scan/re-scan, container-scan, and knowledge-feedback items (owner-reported; controller-mod deviation noted; per-item itemization pending). |
| RSR-02 | Arcane Workbench crystal/Vis/research checks and atomic crafting consumption | Workbench menu/result slot currently have resource-validation gaps. Rank-3 reference behavior captured in [rsr-02-evidence.md](rsr-02-evidence.md); rank-1 confirmation pending. First live capture `REF-0005` ([reference-catalog.md](reference-catalog.md)) records the session; the workbench block was not itemized in the owner's report. |
| RSR-03 | Thaumonomicon, research table/theorycrafting, and research-driven recipe discovery | The port's Thaumonomicon, research table/theorycrafting, and research-driven recipe discovery "need an end-to-end audit" ("Progression may stop after the first research gates."; "The workboard has no runtime parity evidence yet."); workboard acceptance "Complete early-to-advanced progression works without commands." Rank-3 reference behavior captured in [rsr-03-evidence.md](rsr-03-evidence.md); rank-1 confirmation pending. |
| ALC-00 | Crucible recipe matching, input validation, aspect costs, and output economy | Recipe, block, and tile classes exist (`CrucibleRecipe`, `BlockCrucible`, `TileCrucible`); end-to-end behavior is unverified. Rank-3 reference behavior captured in [alc-00-evidence.md](alc-00-evidence.md); rank-1 confirmation pending. |
| ALC-01 / ALC-02 | Smelter aspect extraction, alembic output, jars, tube transfer, and filters | Smelter aspect lookup and multiple block-entity factories are incomplete. Rank-3 reference behavior captured in [alc-01-evidence.md](alc-01-evidence.md); rank-1 confirmation pending. Rank-3 reference behavior for transport captured in [alc-02-evidence.md](alc-02-evidence.md); rank-1 confirmation pending. |
| ALC-03 | Matrix activation, pedestal checks, Essentia consumption, instability, and stabilizer behavior | Matrix/stabilizer integration has missing block-entity/tick paths. Rank-3 reference behavior captured in [alc-03-evidence.md](alc-03-evidence.md); rank-1 confirmation pending. |
| ALC-04 | Thaumatorium apparatus, recipe selection from the crucible-recipe catalog, heat and redstone gating, Essentia pulling, catalyst consumption, and output handling | Thaumatorium recipe lookup returns `null`. Rank-3 reference behavior captured in [alc-04-evidence.md](alc-04-evidence.md); rank-1 confirmation pending. |
| CAS-01 / CAS-02 | Focus-node construction, persistence, delivery mediums, effects, costs, and cooldowns | `FocusEngine` and projectile execution paths do not complete graph execution. Rank-3 reference behavior captured in [cas-01-evidence.md](cas-01-evidence.md); rank-1 confirmation pending. Rank-3 reference behavior for graph execution captured in [cas-02-evidence.md](cas-02-evidence.md); rank-1 confirmation pending. The caster-side pass is captured in [cas-03-evidence.md](cas-03-evidence.md); rank-1 confirmation pending. |
| AUT-01 / AUT-02 | Seal configuration, persistence, ownership, task creation, and golem work loops | Bell GUI and persistent seal storage are incomplete. Rank-3 reference behavior for seals captured in [aut-01-evidence.md](aut-01-evidence.md); rank-1 confirmation pending. Rank-3 reference behavior for golem tasks and upgrades captured in [aut-02-evidence.md](aut-02-evidence.md); rank-1 confirmation pending. |
| AUT-03 | Pattern Crafter, Arcane Bore, and related automation device behavior and economy | Pattern Crafter, Arcane Bore, and related automation need behavior and economy validation. Rank-3 reference behavior for the Pattern Crafter and Arcane Bore captured in [aut-03-evidence.md](aut-03-evidence.md); rank-1 confirmation pending. |
| AUT-04 | Standalone artifice and utility device behavior (for example Arcane Ear, Condenser, and Levitator) | Device blocks and tiles exist under `common/blocks/devices` and `common/tiles/devices`; behavior is unverified. |
| WLD-02 / WLD-03 | Magical tree growth, aura/Flux changes, rift lifecycle, taint growth, and cleansing | Sapling configured features return `null`; rift/stabilization behavior needs verification. |
| WLD-05 | Creature spawning, AI, combat, drops, constructs, and non-focus projectiles | Monster, construct, and projectile entity classes exist under `common/entities`; BETA26 behavior is unvalidated. |
| PLY-01 | Player equipment, Curios state, effects, Warp, and Warp Ward | Warp capability, events, and Warp Ward potion exist (`PlayerWarp`, `WarpEvents`, `PotionWarpWard`); behavior is unverified. |

## Evidence Storage Convention

Store small reproducible fixtures under `src/test/resources/thaumcraft/` and automated tests under `src/test/java/thaumcraft/`. Keep human-readable scenario notes next to their work item in this directory, named `<work-item>-evidence.md`. Do not store original-game binaries, decompiled assets, or large recordings in this repository.
