# Deferred Issue Register

> Development status: the project owner lifted the implementation hold on 2026-09-19; see [budget-hold.md](budget-hold.md). `FND-01` remains `VERIFYING` with all five manual runtime cases pending. The reviewed `ae9a183` harness checkpoint satisfies only the FND-04 implementation-start gate, so FND-04 is the sole `ACTIVE` implementation item. This register preserves confirmed issues so implementation can start with the highest-impact repair rather than repeat the audit.

The target remains Minecraft 1.20.1 Forge 47.3.0 with Thaumcraft 6.1.BETA26 as the behavior reference. Entries describe the current port, not intentional changes to TC6.

## Playability Blockers

These prevent a normal progression loop or make a core system unusable. Repair them only after their recorded workboard start gate clears; the reviewed FND-01 harness checkpoint clears that gate only for FND-04.

| ID | Deferred issue | Player impact | Confirmed evidence | Workboard dependency |
|---|---|---|---|---|
| FND-01 | Automated harness implemented; manual world lifecycle and multiplayer acceptance pending. | Four unit tests and two server GameTests cover the initial foundation; full world persistence and client synchronization remain unverified. | `FND-01-evidence.md` records 4/4 JUnit and 2/2 GameTests; fresh-world/reload/chunk/multiplayer scenarios NOT RUN. | First item; `VERIFYING`, independent review passed; manual acceptance pending. |
| FND-04 | Shared aspect attribution, aspect containers, and lookup are not established for scanning or Essentia processing. | Scans and Essentia handling can disagree on item aspects, so every dependent repair rests on unverified data. | Container base-tag masking reproduced and repaired in the first slice; independent specification and quality review passed. See `fnd-04-container-lookup-evidence.md`. Public lookup routing is repaired and independently approved; REF-0011 confirms only the original Aer crystal AIR1 display. Missing seven-type culling is repaired and independently approved. Other attribution and end-to-end consumer evidence remain open. | Reviewed `ae9a183` FND-01 automated start gate satisfied; `ACTIVE`, third culling slice. |
| RSR-01 | Scanning, research stages, and knowledge sync are incomplete and accept client-led progress. | Research can deadlock or be bypassed; save/reload behavior is not trustworthy. | `ScanningManager.java:202-210`, `PlayerKnowledge.java:53-66`, `PacketSyncProgressToServer.java:104-107`. | FND-04 |
| RSR-02 | Arcane Workbench crystal checks are bypassed and crafting is not fully atomic. | Players can craft without the intended resources. | `ArcaneWorkbenchMenu.java:155-173`, `ArcaneWorkbenchResultSlot.java:163-178`. | RSR-01 |
| ALC-00 | Crucible recipes, input validation, aspect costs, and output economy are unverified. | Early alchemy cannot be trusted to consume or produce the BETA26 results. | Not yet reproduced end-to-end; retain as a verification item. | RSR-02 |
| ALC-01 | Smelters always receive an empty aspect list. | Items cannot be processed into Essentia. | `TileSmelter.java:199-208,260-266`. | FND-04 |
| ALC-02 | Alembic and tube block-entity factories return `null`. | Essentia transport and storage do not run. | `BlockAlembic.java:87-96`, `BlockTube.java:165-170`. | ALC-01 |
| ALC-03 | The Infusion Matrix has no block entity or ticker, and infusion stabilizers do not instantiate or participate in the matrix contract. | Infusion cannot activate or complete, and high-instability infusion behavior is incomplete. | `BlockInfusionMatrix.java:52-61`, `BlockStabilizer.java:54-63`, `TileStabilizer.java:92-113`. | ALC-02 |
| CAS-01 | The Focal Manipulator does not open its configuration UI. | Players cannot build configured foci through the intended path. | `BlockFocalManipulator.java:83-97`. | FND-01 |
| CAS-02 | Focus graph execution does not reach delivery effects. | Touch, bolt, and projectile foci fail to perform their spell effects. | `FocusEngine.java:261-313`, `FocusMediumTouch.java:174-179`, `FocusMediumBolt.java:50-81`, `EntityFocusProjectile.java:177-187`. | CAS-01 |

## Important System Repairs

These follow the playability blockers and are required for full BETA26 parity, but they are not the first spend of a limited budget.

| ID | Deferred issue | Player impact | Confirmed evidence | Workboard dependency |
|---|---|---|---|---|
| FND-02 | Registered block entities, menus, renderers, capabilities, and packets have not been audited against factory/ticker/side ownership. | Integration gaps can leave blocks silently inert or crash on one side, blocking later repairs. | Not yet performed; retain as a verification item. | FND-01 |
| RSR-03 | Thaumonomicon, research table/theorycrafting, and research-driven recipe discovery need an end-to-end audit. | Progression may stop after the first research gates. | The workboard has no runtime parity evidence yet. | RSR-01 |
| ALC-04 | Thaumatorium recipe lookup returns `null`. | Automated alchemical crafting cannot select or run recipes. | `TileThaumatorium.java:224-225,289-302`. | ALC-03 |
| CAS-03 | Caster inventory, focus pouch, Vis costs, cooldowns, and multiplayer effect rendering lack a validation pass. | Crafted or shared foci can lose data or diverge between client and server. | Not yet reproduced end-to-end; retain as a verification item. | CAS-02 |
| AUT-01 | Golem seals lack durable per-level storage and a configuration UI. | Golem automation resets after reload or cannot be configured. | `ItemGolemBell.java:68-77,99-121`, `SealHandler.java:45,311-319`. | FND-02 |
| AUT-02 | Golem tasks, upgrades, ownership, inventories, and multiplayer behavior are unvalidated. | Configured golems may not complete representative BETA26 work after reload. | Not yet reproduced end-to-end; retain as a verification item. | AUT-01 |
| AUT-03 | Pattern Crafter, Arcane Bore, and related automation need behavior and economy validation. | Late-game automation is incomplete or unsafe to use. | Not yet reproduced end-to-end; retain as a verification item. | AUT-01 / ALC-03 |
| AUT-04 | Standalone artifice and utility devices are unvalidated. | Utility devices may be incomplete or unsafe to use. | Not yet reproduced end-to-end; retain as a verification item. | FND-02 |
| WLD-01 | Aura, Vis, and Flux persistence have not passed fresh-world or reload validation. | Core magical resources may drift, reset, or disagree across multiplayer. | No automated coverage exists yet. | FND-01 |
| WLD-02 | Greatwood and Silverwood sapling features resolve to `null`. | Magical trees cannot grow through normal play. | `BlockSaplingTC.java:37-40,58-61`. | WLD-01 |
| WLD-03 | Rift, taint, stabilization, cleansing, and late-game world behavior need reproduction and parity testing. | World hazards and their counterplay may be incomplete. | Current evidence is incomplete; confirm against BETA26 before changing behavior. | WLD-01 |
| WLD-04 | Eldritch structures, bosses, and endgame unlocks lack a completed progression verification pass. | The endgame may be unreachable. | No complete survival-path evidence exists yet. | PLY-01 |
| WLD-05 | Creature spawning, AI, combat, drops, constructs, and non-focus projectiles are unvalidated. | Combat encounters, construct behavior, and drops may diverge from BETA26. | Not yet reproduced end-to-end; retain as a verification item. | FND-02 |
| PLY-01 | Player equipment, Curios state, effects, Warp, and Warp Ward are unvalidated. | Player power, status effects, and Warp consequences may be wrong; the endgame gate depends on this lane. | Not yet reproduced end-to-end; retain as a verification item. | RSR-03 |

## Fidelity and Release Work

These should wait until the gameplay spine works. They matter for a finished private build, but do not justify spending on them while core progression is broken.

| ID | Deferred issue | Player impact | Workboard dependency |
|---|---|---|---|
| CLI-01 | Core research, crafting, and device screens need synchronization and interaction verification. | Menus may not open or may show stale state. | RSR-03 |
| CLI-02 | Caster, beam, particle, and focus visuals need dedicated-server-safe restoration. | Magic lacks expected feedback or can cause client/server crashes. | CAS-02 |
| CLI-03 | Placeholder or missing models, textures, sounds, and renders require an asset sweep. | Presentation is incomplete even if systems function. | WLD-04 |
| REL-01 | JEI, Curios, and configuration combinations need explicit compatibility coverage. | Optional integrations may break installs or recipes. | All gameplay lanes |
| REL-02 | Long-play, dedicated-server, remote-client, reload, performance, dupe, and crash sweeps are absent. | A build cannot be called stable yet. | All gameplay lanes |
| REL-03 | Reproducible private-play package and install guide are not prepared. | Private testers lack a supported install path. | REL-02 |
| REL-04 | Public distribution cannot proceed without written rights-holder permission. | Sharing a modified build remains out of scope. | Written permission |

## Implementation Order

Start with the ready `FND-04`, then repair the minimum survival spine in this order as dependencies clear. FND-01 remains `VERIFYING` until its five manual cases pass:

1. `RSR-01` and `RSR-02` — authoritative research and paid crafting.
2. `ALC-01`, `ALC-02`, and `ALC-03` — Essentia production, transport, and infusion.
3. `CAS-01` and `CAS-02` — focus construction and casting.
4. The remaining automation, world, client, and release items in the workboard order.

Before changing an issue marked as needing verification, capture the relevant BETA26 behavior under the evidence rules in `parity-evidence-index.md`.
