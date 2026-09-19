# FND-04 live Aer crystal observation — 2026-09-19

Reference: REF-0011. This is a narrow live BETA26 observation, not full FND-04 acceptance.

## Setup and procedure

The coordinator launched the existing CurseForge `TC6 Reference` profile at 07:25 local time on 2026-09-19. The loaded client displayed Minecraft 1.12.2, Forge 14.23.5.2859, and six active mods including Forge components. The profile's mods directory held only Baubles 1.5.2 and the unchanged Thaumcraft 6.1.BETA26 release jar. Their SHA-256 values are recorded below. No gameplay addon or controller mod was loaded; CurseForge's cached Controllable row remained visible, but a disable attempt failed and the file was already absent. The actual filesystem and game loader are the evidence of the loaded setup.

A separate creative world, `New Worlda`, was created without opening the owner's existing worlds. Its saved metadata records seed `-7348667044462872440`, GameType 1, allowCommands 1, and version 1.12.2. The automatically selected default world name gained an `a` during a keyboard-input check; the attempted descriptive name did not enter through the legacy client's text-input path. No chat commands or research grants were used.

In the creative inventory, the coordinator searched `vis` with individual key input and hovered the Aer Vis Crystal. First, the ordinary tooltip displayed `Aer x1`. Then the built-in Thaumcraft Graphics setting `showTags` was temporarily changed from false to true through its configuration UI, enabling the calculated aspect icons without a held Shift key. The same crystal was hovered again and captured with the game's F2 screenshot action.

## Observed result and artifacts

The second screenshot shows the Aer Vis Crystal, its `Aer x1` contents text, and a calculated aspect row containing only the yellow AIR icon with amount 1. No CRYSTAL icon or additional calculated aspect appears. This matches the exact AIR1/CRYSTAL0 result asserted for the port's real air-crystal lookup. The observation is the result, not proof of the internal routing; REF-0009/REF-0010 supply separate artifact-inspection evidence for the original call path.

All artifacts remain outside the repository under `C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/`:

| Artifact | SHA-256 / purpose |
| --- | --- |
| `screenshots/2026-09-19_07.28.54.png` | `ab6025d3d87d0078db884e240aa818d00fb5f4034aa7ed60e178fdc41b132818` — ordinary contents tooltip only |
| `screenshots/2026-09-19_07.31.23.png` | `ad7bb9362d057021c2fbc486d1212efa7af8fb865839a86c592b2d9afd50617a` — calculated aspect row visible |
| `mods/Thaumcraft-1.12.2-6.1.BETA26.jar` | `9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f` |
| `mods/Baubles-1.12-1.5.2.jar` | `b32010b2f2778aa1188585e7ead91ad46d4cb2c715f9c778a61848ba7fe51f8d` |
| `saves/New Worlda/level.dat` | World metadata read after clean save; no save copied into repository |
| `logs/latest.log` | Session began at 07:25; world join 07:27:21; save/unload 07:31:58; client stopping 07:32:07. This rotating log is corroboration, not a permanent capture identifier. |

The original client logged missing scan-screen/scanner texture resources during startup and a short world-generation lag warning. Neither prevented the observed crystal tooltip; this note does not claim an error-free reference session.

## Cleanup and limits

The world was saved, the client exited through Quit Game, and its process was verified absent. The temporary `showTags` setting was restored to false in the graphics config after shutdown. Original worlds and mod jars were preserved. No EULA action or multiplayer launch occurred.

This confirms only the creative-inventory display of this Aer crystal in BETA26. It does not establish survival acquisition, filled/empty phial behavior, general container capacities, scanning knowledge/progression, smelter output, reload persistence of item state, or multiplayer agreement. The corresponding port result is currently proven by GameTests, not a matched live port tooltip observation. Full FND-04 and FND-01 manual acceptance remain open.
