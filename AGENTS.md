# Thaumcraft 6 Parity Agent Guide

## Mission

Finish this Minecraft **1.20.1 Forge 47.3.0** port for private play. The fidelity baseline is **Thaumcraft 6.1.BETA26**:

- Preserve its intended content, progression, balance, and player-facing behavior.
- Fix crashes, dupes, deadlocks, data loss, and other defects that prevent a stable playthrough.
- Do not add custom gameplay features or silently simplify an original system to make a task easier.
- Treat public distribution as out of scope until written permission from the original rights holder is obtained.

The README's claimed completion percentage and `TODO.md` are leads, not proof of parity. Runtime behavior and the parity workboard are the current sources of truth.

## Read This Before Editing

1. `docs/agent-pipeline/README.md` — pipeline rules and handoff format.
2. `docs/agent-pipeline/filesystem-map.md` — repository map and ownership boundaries.
3. `docs/agent-pipeline/parity-workboard.md` — ordered work lanes and acceptance gates.
4. `docs/agent-pipeline/parity-evidence-index.md` — how to prove the relevant TC6 BETA26 behavior.
5. The relevant TC6 BETA26 behavior and the implementation you intend to change.

## Agent Pipeline

Work is split into these non-overlapping lanes:

| Lane | Owns |
|---|---|
| `FND` | build, registrations, capabilities, networking contracts, tests, CI-style validation |
| `RSR` | aspects, scanning, research, knowledge, recipe gates |
| `ALC` | crucible, smelting, essentia, jars, tubes, infusion, thaumatorium |
| `CAS` | casters, foci, focal manipulator, casting effects, caster persistence |
| `AUT` | golems, seals, arcane bore, pattern crafter, automation devices |
| `WLD` | aura, flux, taint, biomes, features, structures, renewable magical flora |
| `CLI` | menus, screens, renderers, models, textures, particles, sounds, localization |
| `REL` | compatibility, packaging, configuration, performance, multiplayer/release acceptance |

The coordinator owns `parity-workboard.md`. A worker claims one ready work item in its task handoff, changes only that item's stated files plus necessary tests, and reports evidence back to the coordinator. Do not edit another lane's work merely because it is nearby; request a dependency handoff instead.

## Work Rules

- Keep each change atomic and scoped to one work item.
- Prefer an isolated Git worktree or branch for code changes. Never erase or reset existing user work.
- Inspect callers, serialization, networking, and client/server behavior before changing a gameplay system.
- Keep game authority on the server. Clients may render or request actions but must not decide research, crafting, inventory, damage, Vis, Essentia, or world-state outcomes.
- Preserve data across save/reload and chunk unload/reload. Version or migrate persistent data when a format changes.
- Use existing registration, capability, packet, recipe, and data-generation patterns unless the task explicitly repairs those patterns.
- Treat `src/main/java_old/` as a BETA26-era behavior reference, not compilable port source. Trace intent from it, then make an explicit 1.20.1 Forge implementation; never bulk-copy legacy code into `src/main/java/`.
- A TODO comment is not a fix. Replacing a stub with a no-op, free resource, or client-only action does not satisfy parity.
- Do not accept the Minecraft EULA, publish artifacts, change remote Git state, or distribute original assets without explicit user authorization.

## Validation Contract

Run the narrowest relevant checks while developing, then the required release gate for the item:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
.\gradlew.bat runGameTestServer
.\gradlew.bat runServer
.\gradlew.bat runClient
.\gradlew.bat runData
.\gradlew.bat build
```

`test` currently has no source coverage, so every new parity subsystem must add deterministic tests or GameTests before it can be marked done. For gameplay items, verify at minimum:

1. A fresh survival world can reach and use the feature without commands.
2. The feature survives save/reload and, where applicable, chunk unload/reload.
3. A dedicated server and a connected client agree on the outcome.
4. The feature consumes and produces the same meaningful resources as TC6 BETA26.

## Handoff Format

Report work in this format:

```text
Work item: <ID and title>
Lane: <FND|RSR|ALC|CAS|AUT|WLD|CLI|REL>
Scope: <files and behavior changed>
Evidence: <commands/tests/runtime scenario and outcome>
Parity reference: <TC6 BETA26 behavior checked>
Dependencies or follow-ups: <none or explicit IDs>
```

Never claim an item complete without evidence for its workboard acceptance criteria.
