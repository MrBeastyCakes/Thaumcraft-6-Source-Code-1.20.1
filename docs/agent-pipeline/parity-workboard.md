# Thaumcraft 6.1.BETA26 Parity Workboard

## Release Contract

- **Target:** Minecraft 1.20.1 Forge 47.3.0, Java 17.
- **Fidelity baseline:** Thaumcraft 6.1.BETA26 intended content, progression, balance, and behavior.
- **Quality bar:** a fresh survival world reaches all major systems without commands; systems survive save/reload and dedicated-server multiplayer; no known crash, dupe, data-loss, or progression deadlock remains.
- **Distribution:** private play only unless written permission allows a public modified build.

## State Key

`READY` means an agent may claim the item. `BLOCKED` names a prerequisite. `DEFERRED` means the item is intentionally paused by a project constraint. `ACTIVE`, `VERIFYING`, and `DONE` are coordinator-controlled states backed by a handoff and evidence.

## Current Development State

The project owner lifted the implementation hold on 2026-09-19. `FND-01` remains `VERIFYING`; all five manual runtime cases remain pending. The independently reviewed `ae9a183` harness checkpoint is a narrow implementation-start gate for `FND-04`, which is `ACTIVE` under a separate coordinator-recorded claim. Every other non-complete item retains its existing dependency. See [foundation-start-gate-2026-09-19.md](foundation-start-gate-2026-09-19.md) and `deferred-issues.md`. FND-04 has six independently reviewed slices committed (latest `a96faf6`, entity attribution); remote-client, multiplayer, and consumer-level acceptance remain open.

## Dependency Spine

```mermaid
flowchart TD
  FND0[Foundation / test harness] --> RSR0[Research authority]
  FND0 -. ae9a183 reviewed start gate .-> FND04[FND-04 Aspect attribution and lookup]
  FND04 --> RSR0
  FND04 --> ALC0[Essentia production and storage]
  RSR0 --> RSR1[Recipe gates and early progression]
  RSR1 --> ALC00[ALC-00 Crucible alchemy]
  RSR1 --> ALC0
  ALC0 --> ALC1[Transport, infusion, thaumatorium]
  RSR1 --> CAS0[Focus construction and casting]
  ALC1 --> AUT0[Golems and automation]
  FND0 --> AUT04[AUT-04 Artifice and utility devices]
  RSR1 --> WLD0[World systems and endgame]
  FND0 --> WLD05[WLD-05 Creatures and combat]
  RSR1 --> PLY01[PLY-01 Player systems and Warp]
  PLY01 --> WLD0
  ALC1 --> CLI0[Client fidelity]
  CAS0 --> CLI0
  AUT0 --> REL0[Full acceptance sweep]
  WLD0 --> REL0
  CLI0 --> REL0
```

## Foundation — `FND`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| FND-01 | VERIFYING | Establish unit-test and GameTest conventions, fixtures, and a fresh-world/reload/server smoke suite. | `src/test/`, Gradle run configs | `test` contains tests; focused GameTests run; documented local command sequence passes. | handoffs/FND-01.md |
| FND-02 | BLOCKED: FND-01 | Audit every registered block entity, menu, renderer, capability, and packet against factory/ticker/side ownership. | `init/`, `common/blocks`, `common/tiles`, `common/lib` | Inventory names each missing or mismatched integration point; regressions have tests. | — |
| FND-03 | DONE | Created the BETA26 parity evidence index for systems under repair. | `docs/agent-pipeline/parity-evidence-index.md` | Reference hierarchy, evidence template, and initial evidence queue are documented. | handoffs/FND-03.md |
| FND-04 | ACTIVE | Establish authoritative aspect attribution, containers, and lookup shared by scanning and Essentia. | `api/aspects`, `common/lib`, `common/tiles/essentia` | Item aspects, container capacities, and shared lookup resolve consistently for scanning and Essentia handling with unit coverage. | handoffs/FND-04.md |

## Research and Early Progression — `RSR`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| RSR-01 | BLOCKED: FND-04 | Make scanning, stages, prerequisites, and knowledge persistence server-authoritative. | `api/research`, `common/lib/research`, capabilities, packets | Fresh player completes scan-gated entries; invalid client progress is rejected; reload preserves state. | — |
| RSR-02 | BLOCKED: RSR-01 | Enforce Arcane Workbench crystals, Vis, research gates, and atomic output consumption. | workbench menu/tile/recipes | Insufficient resources never craft; valid craft consumes exact resources across client/server. | — |
| RSR-03 | BLOCKED: RSR-01 | Validate the Thaumonomicon, research table/theorycrafting, and research-driven recipe discovery. | research menus/screens/data | Complete early-to-advanced progression works without commands. | — |

## Alchemy and Advanced Crafting — `ALC`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| ALC-00 | BLOCKED: RSR-02 | Restore Crucible recipes, input validation, aspect costs, and output economy. | `common/blocks/crafting`, `common/tiles/crafting`, recipes | Valid Crucible recipes consume exact inputs and aspects and produce expected outputs; invalid inputs are rejected without consumption. | — |
| ALC-01 | BLOCKED: FND-04 | Restore authoritative item aspects and smelter inputs. | aspect registry, smelter tiles | Valid items smelt into correct Essentia; invalid inputs do not consume items. | — |
| ALC-02 | BLOCKED: ALC-01 | Wire alembics, jars, tube variants, filters, and reload-safe Essentia transfer. | essentia blocks/tiles/capabilities | A survival-built line transports the expected aspect without loss/duplication after reload. | — |
| ALC-03 | BLOCKED: ALC-02 | Restore Infusion Matrix block entity, activation, pedestals, consumption, instability, and stabilizers. | crafting blocks/tiles, recipes | A normal infusion completes; instability and stabilizers match BETA26 intent. | — |
| ALC-04 | BLOCKED: ALC-03 | Restore Thaumatorium lookup, selection, and crafting. | Thaumatorium tile/menu/recipes | A researched recipe processes end-to-end using its required Essentia. | — |

## Casting — `CAS`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| CAS-01 | BLOCKED: FND-01 | Open the Focal Manipulator and preserve every focus-node setting. | manipulator block/menu/screen, focus NBT | A configured focus survives inventory movement and world reload. | — |
| CAS-02 | BLOCKED: CAS-01 | Execute focus graphs server-side for Touch, Bolt, projectile, cloud, mine, plan, modifiers, and effects. | `api/casters`, focus items, projectile entities, packets | Each delivery medium applies its configured effect exactly once with correct costs. | — |
| CAS-03 | BLOCKED: CAS-02 | Validate caster inventory, focus pouch, Vis costs, cooldowns, rendering, and multiplayer effects. | caster items, GUI, client FX | A player can craft/use/share foci without lost data or client/server divergence. | — |

## Automation — `AUT`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| AUT-01 | BLOCKED: FND-02 | Persist seals per level and restore the seal/logistics configuration UI. | golems, seals, bell, menus, packets | Seals survive reload and are fully configurable through the bell. | — |
| AUT-02 | BLOCKED: AUT-01 | Validate golem tasks, upgrades, ownership, inventories, and multiplayer behavior. | golem AI/tasks/entities | A configured golem completes representative BETA26 tasks after reload. | — |
| AUT-03 | BLOCKED: ALC-03 | Restore Pattern Crafter, Arcane Bore, and related automation devices. | automation blocks/tiles/entities | Each device consumes inputs and produces expected outputs without dupes. | — |
| AUT-04 | BLOCKED: FND-02 | Restore standalone artifice and utility devices. | `common/blocks/devices`, `common/tiles/devices`, `common/entities/construct` | Representative devices activate and produce expected outputs on a server without dupes, surviving reload. | — |

## World Systems — `WLD`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| WLD-01 | BLOCKED: FND-01 | Validate aura generation, Vis/Flux persistence, and dimension lifecycle. | `common/world/aura` | Fresh and reloaded worlds maintain stable aura values. | — |
| WLD-02 | BLOCKED: WLD-01 | Restore renewable Greatwood/Silverwood growth and worldgen parity. | saplings, features, biome modifiers | Saplings and generated features match intended behavior. | — |
| WLD-03 | BLOCKED: WLD-01 | Restore flux rifts, taint, stabilization, cleansing, and late-game consequences. | rift/taint entities, world blocks, devices | Rift lifecycle and taint behavior work in server and reload scenarios. | — |
| WLD-04 | BLOCKED: PLY-01 | Validate Eldritch progression, structures, bosses, and endgame unlocks. | structures, entities, research | A survival player reaches and completes the BETA26 endgame path. | — |
| WLD-05 | BLOCKED: FND-02 | Validate creature spawning, AI, combat, drops, constructs, and non-focus projectiles. | `common/entities/monster`, `common/entities/construct`, `common/entities/projectile` | Representative creatures, constructs, and projectiles spawn, fight, and drop correctly in survival and after reload. | — |

## Player Systems — `PLY`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| PLY-01 | BLOCKED: RSR-03 | Restore player equipment, Curios state, effects, Warp, and Warp Ward. | `common/items/curios`, `common/items/baubles`, `common/lib/capabilities`, `common/lib/events` | Equipment, Curios, effects, and Warp/Warp Ward persist across inventory changes, reload, and multiplayer. | — |

## Client Fidelity — `CLI`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| CLI-01 | BLOCKED: RSR-03 | Complete research, crafting, and device screens with correct server synchronization. | client screens/widgets/renderers | All core menus open and work without client-only state. | — |
| CLI-02 | BLOCKED: CAS-02 | Restore caster, beam, particle, and focus visuals. | client FX/particles/renderers | Effects are visible to nearby clients and do not crash dedicated servers. | — |
| CLI-03 | BLOCKED: WLD-04 | Replace placeholder/missing models, textures, sounds, and entity renders. | assets, models, renderers, `TEXTURETODO.md` | No core BETA26 content uses placeholder or missing assets. | — |

## Release Acceptance — `REL`

| ID | State | Outcome | Primary areas | Acceptance evidence | Handoff |
|---|---|---|---|---|---|
| REL-01 | BLOCKED: all gameplay lanes | Verify JEI/Curios contracts and configuration behavior. | compat, config, data | Supported combinations and optional behavior are documented and tested. | — |
| REL-02 | BLOCKED: all gameplay lanes | Build a provisional private-play candidate from the exact revision under test, then run long-play, dedicated-server, remote-client, reload, performance, dupe, and crash sweeps against it. | full repository | The candidate identity and all reported defects have reproduction/verification evidence; no open parity blockers remain. The candidate is acceptance input, not the final package. | — |
| REL-03 | BLOCKED: REL-02 | After REL-02 accepts the candidate revision, reproduce it as the final private-play package and write the install guide. | build/docs | The final JAR reproduces the accepted revision; exact versions and clean install verification are recorded. | — |
| REL-04 | BLOCKED: written permission | Prepare public distribution materials only after rights are confirmed. | metadata/docs | Permission, attribution, license, and release channel are verified. | — |
