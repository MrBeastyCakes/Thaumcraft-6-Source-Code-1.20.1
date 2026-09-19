# FND-04 live derived-item tooltip observations — 2026-09-19

Reference: REF-0016. Narrow rank-1 original display evidence for two recipe-derived items, not full attribution or gameplay acceptance.

## Setup and procedure

Coordinator launched the existing CurseForge TC6 Reference profile, Minecraft1.12.2/Forge14.23.5.2859/Thaumcraft6.1.BETA26, with Baubles1.5.2 only. The mods directory had exactly those two jars; hashes matched REF0002 and the Baubles baseline. The menu showed six loaded/active mods including Forge components. CurseForge's stale Controllable row was not acted on and no controller jar existed.

Reopened coordinator-created creative world New Worlda, seed -7348667044462872440 from REF0011. Other owner worlds were not opened. Before launch, display-only config/thaumcraft_graphics.cfg showTags was temporarily false->true. Opened creative inventory, hovered Oak Wood Planks in Building Blocks, then searched `sti` and hovered Stick. F2 saved each screenshot. No item was acquired, crafted or consumed; no commands, research grants, block changes or multiplayer session.

## Results and artifacts

Paths are relative to C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/. Original artifacts remain outside the repository.

| Item | Visible result | Screenshot | SHA-256 |
| --- | --- | --- | --- |
| Oak Wood Planks | PLANT3 only | screenshots/2026-09-19_08.33.03.png | 089c72125665470e621d740ffa9d6b6aa393a0e69a46f72e0be0a779b5ed9438 |
| Stick | PLANT1 only | screenshots/2026-09-19_08.33.37.png | bd79292517b4f157d5e32b4c9920a7ab1d6695af6b7b451903ae058b3fbaddd5 |

Jar hashes freshly checked before launch:

- mods/Thaumcraft-1.12.2-6.1.BETA26.jar: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f
- mods/Baubles-1.12-1.5.2.jar: b32010b2f2778aa1188585e7ead91ad46d4cb2c715f9c778a61848ba7fe51f8d

Rotating latest.log corroborated Forge launch08:31:30, six mods successfully loaded08:31:47, worldjoin08:32:22, both screenshot timestamps, serverstop08:33:57 and clientstop08:34:09. This log is supporting session evidence, not a permanent artifact ID.

## Interpretation, cleanup and limits

These displayed values agree with the inspected75% recipe formula: a PLANT20 log producing four planks gives trunc(20*.75/4)=3; two PLANT3 planks producing four sticks give trunc(6*.75/4)=1. That arithmetic is an interpretation supported by REF0015 and the earlier log tooltip, not a screenshot of the internal mechanism or proof of every competing recipe path.

Saved/exited world through menus and quit game. Immediate process check saw18952 still closing; a later check confirmed it absent before showTags was restoredfalse. No outstanding client/config cleanup. No mod jars, other worlds, EULA or release state changed.

No port tooltip, survival acquisition/crafting, scan knowledge, Essentia output, costs, save/chunk reload or multiplayer acceptance is claimed. Modern recipe differences and full recipe-category coverage require separate tests. The independent critic should inspect both screenshots and verify hashes before promoting this capture as reviewed evidence.
