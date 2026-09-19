# FND-04 live item-aspect observations — 2026-09-19

Reference: REF-0014. These are narrow original BETA26 tooltip observations, not full FND-04 acceptance.

## Setup and procedure

The coordinator launched the existing CurseForge TC6 Reference profile at about 08:00 local time. The client showed Minecraft 1.12.2, Forge 14.23.5.2859, and six active mods including Forge components. The mods directory contained only Thaumcraft 6.1.BETA26 and Baubles 1.5.2; their hashes were freshly checked below. CurseForge's cached controller-mod row remains stale; no controller jar was present or modified.

The coordinator reopened the separate creative capture world New Worlda from REF-0011 (seed -7348667044462872440), without opening the owner's other worlds. The world list again showed Creative Mode and version 1.12.2. Before launch, the display-only thaumcraft_graphics.cfg setting showTags was temporarily changed from false to true. Creative inventory tooltips then exposed calculated aspects without requiring held Shift input.

The coordinator opened inventory, hovered iron ore, searched `ir` and hovered the iron ingot, cleared the search and hovered oak wood, then searched `di` and hovered the diamond. Each result was captured with the game's F2 screenshot action. No items were acquired, commands issued, research granted, blocks changed or multiplayer session opened.

## Observed values and retained artifacts

All paths below are relative to C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/. The original screenshots and jars remain outside the repository.

| Item | Calculated tooltip result | Screenshot | SHA-256 |
| --- | --- | --- | --- |
| Iron Ore | METAL15, EARTH5; two icons | screenshots/2026-09-19_08.01.11.png | 343e5aad5c308f54040c84b73c28caa155713c20bcddff3e69e8fca0e1706871 |
| Iron Ingot | METAL15 only | screenshots/2026-09-19_08.01.50.png | 8661ecb3cfc04e5b24773b874bf2cfa3d8e9770e36c1bdece75991b7fe4a6bfe |
| Oak Wood (1.12.2 log item) | PLANT20 only | screenshots/2026-09-19_08.02.11.png | 6e5f5b0453dfb00895d90c584ac0908099ba03faeade9dad5b8164d3f602aad6 |
| Diamond | CRYSTAL15, DESIRE15; two icons | screenshots/2026-09-19_08.02.31.png | 1b5415abd38231c1d0efa4e332f0e4d55336f1b8011f3306fa64c56e65b3dfcf |

Reference jar hashes:

- mods/Thaumcraft-1.12.2-6.1.BETA26.jar: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f
- mods/Baubles-1.12-1.5.2.jar: b32010b2f2778aa1188585e7ead91ad46d4cb2c715f9c778a61848ba7fe51f8d

The session's rotating latest.log corroborates Forge initialization at 08:00:24, world join at 08:00:58, the four screenshot timestamps, world shutdown at 08:02:48 and client stop at 08:02:55. It is supporting session evidence, not a permanent capture identifier.

## Cleanup and limits

The world was saved and exited through the game menus. Reference process 15840 was verified absent, then showTags was restored to false. No mod jars, other worlds, EULA setting or release state changed.

The iron and diamond display values directly corroborate the selected startup-regression expectations; the ore and wood values provide additional attribution reference points. The screenshots do not show a registration mechanism, a port tooltip, survival acquisition, scanning knowledge, smelter output, resource costs, reload persistence or multiplayer agreement. Original 1.12.2 category expansion and the port's reloadable 1.20.1 tags have different lifecycles; separate implementation evidence is required for that adaptation.
