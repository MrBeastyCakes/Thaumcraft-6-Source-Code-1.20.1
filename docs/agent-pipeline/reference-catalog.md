# BETA26 Reference Catalog

This catalog stores the observation metadata behind every BETA26 parity claim: which environment was observed, where the artifact lives, and how the capture was made. It lets an agent confirm how a claim was produced without the original game files being present in this repository.

**Entries record metadata only, and they NEVER store original binaries, assets, decompiled files, or recordings in the repository.** An entry is a pointer to a capture that stays on the capturing machine; it is never a copy of the captured content.

**Capture status: the first BETA26 gameplay capture is recorded — `REF-0005` (2026-09-18).** An inventory pass on 2026-09-18 recorded the reference artifacts that exist on the developer machine (entries `REF-0001` .. `REF-0004`, all marked as inventory records rather than captures). `REF-0005` is the first real capture: a live session in the prepared clean profile, recorded with a documented environment deviation (a client-side controller mod was present during the observed launches and was removed afterwards — see the entry). Itemized rank-1 confirmation of specific claims still requires the per-item passes described in the work-item scenario notes, and the deviation is noted for clean re-verification of load-bearing items.

**Entry status.** Every entry states in its identifier line whether it is a *capture* (a reproducible observation of running BETA26), an *inventory record* (artifact metadata only, no observation), or an *inspection record* (a static, read-only examination of a reference artifact, for example a bytecode check of the shipped jar). Only captures can support rank-1 claims.

## Required fields

Every entry uses exactly these fields, in this order:

```text
Reference identifier: <stable catalog ID, for example REF-0001>
Minecraft version: <exact observed version string>
Forge version: <exact observed loader version string>
Thaumcraft version: <exact observed BETA26 build string>
Artifact location: <filesystem path of the artifact on the capturing machine, outside this repository>
SHA-256: <64-character lowercase hex digest of the observed artifact file>
Launcher or profile: <launcher, instance, or profile name used for the capture>
World seed: <seed of the world used, or n/a for a capture that uses no world>
Player setup: <game mode, inventory, research, and permission state needed to reproduce the observation>
Capture scenario: <exact steps or command sequence performed to observe the behavior>
Recorded by: <capture operator or capturing agent identity>
Date recorded: <ISO 8601 date of the capture>
```

## How to record an entry

1. Capture the behavior in an unmodified BETA26 environment and rank it against the evidence hierarchy in `parity-evidence-index.md`.
2. Compute the artifact digest on the capturing machine: `sha256sum <artifact>` in git-bash, or `certutil -hashfile <artifact> SHA256` in cmd. Multi-file artifacts such as source trees are hashed with a documented convention instead — see `Tree-hash convention (REF-0001)`; a tree digest is not the digest of a single file and must say so.
3. Add a new entry under `## Recorded entries` using the next free `REF` identifier, with every field from `## Required fields` in the same order.
4. Keep the artifact, the world directory, any decompiled source, and any recording outside the repository; the entry stores only the path and the digest.
5. Reference the identifier from the work item's scenario notes at `docs/agent-pipeline/<work-item>-evidence.md` and from the `Parity reference` field of the item's handoff.

## Recorded entries

Entries are added in `REF` order. Fields are listed in the required order. `REF-0001` .. `REF-0004` are inventory records; `REF-0006` is an inspection record; `REF-0005` is the first capture.

### REF-0001 — decompiled TC6 source tree

```text
Reference identifier: REF-0001 (inventory record, not a capture; role: decompiled source reference, candidate rank-3 evidence under parity-evidence-index.md)
Minecraft version: 1.12.2 (declared in the tree: @Mod acceptedMinecraftVersions = "[1.12.2]")
Forge version: 1.12.2-14.23.5.2860 (Gradle dependency in build.gradle); the @Mod annotation requires forge@[14.23.5.2768,)
Thaumcraft version: 6.1.BETA26 declared (@Mod version = "6.1.BETA26" in Thaumcraft.java, Thaumcraft.VERSION = "6.1.BETA26", CommonProxy.java sets "6.1.BETA26"); the tree's changelog.txt is byte-identical to the changelog inside the released BETA26 jar (REF-0002) whose newest entry is "6.1.BETA26 hotfix1"
Artifact location: D:/dev/hytale mods/magic/research/_legal/tc6-decompiled (git clone of https://github.com/TheDarkTower314/Thaumcraft-6-Source-Code.git, branch master, single commit 954022b dated 2022-12-28, working tree clean; the SHA-256 field below is a tree hash, not a single-file digest — see "Tree-hash convention (REF-0001)")
SHA-256: 61bf52ee4409f7ba5dba9d14dae19602de61f4cb9f5b10a0bbc954c8aa7568f6
Launcher or profile: not applicable (source tree, no launcher and no game run)
World seed: not applicable
Player setup: not applicable
Capture scenario: no capture performed — metadata inventory only: version-evidence grep for BETA26 across code/config/doc files (hits listed on the version line), file count 2,447 (excluding .git), total 15,905,687 bytes, and the tree hash defined below; the resource payload was compared against the BETA26 jar (1,537 tree resource files versus 1,537 jar entries, 1,390 byte-identical, 145 differ only by line endings, 2 differ in content: mcmod.info and pack.mcmeta; see "Resource comparison convention (REF-0001 vs REF-0002)"). Java sources were not compared against the jar's compiled classes, so source-level identity with the released jar at hotfix level is not verified.
Recorded by: inventory agent (read-only reference-evidence pass; no game run, no capture)
Date recorded: 2026-09-18
```

### REF-0002 — Thaumcraft 6.1.BETA26 release jar

```text
Reference identifier: REF-0002 (inventory record, not a capture; role: original BETA26 release artifact, candidate rank-2 evidence under parity-evidence-index.md)
Minecraft version: 1.12.2 (mcmod.info inside the jar: "mcversion": "1.12.2")
Forge version: version unverified for the artifact itself (mcmod.info declares only a Baubles dependency); both instances that hold the jar run Forge 14.23.5.2859
Thaumcraft version: 6.1.BETA26 (mcmod.info: "version": "1.12.2-6.1.BETA26", "modid": "thaumcraft", "authorList": ["Azanor"]); the bundled changelog's newest entry is "6.1.BETA26 hotfix1"
Artifact location: C:/Users/t8rto/curseforge/minecraft/Instances/Thaumcraft Reimagined/mods/Thaumcraft-1.12.2-6.1.BETA26.jar and C:/Users/t8rto/AppData/Roaming/ModrinthApp/profiles/Thaumcraft 6 Pack/mods/Thaumcraft-1.12.2-6.1.BETA26.jar (byte-identical copies, 11,360,786 bytes each, same digest)
SHA-256: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f
Launcher or profile: CurseForge instance "Thaumcraft Reimagined" (Minecraft 1.12.2, Forge 14.23.5.2859, 52 jars in mods/, installed 2026-09-17, never launched: playedCount 0) and Modrinth profile "Thaumcraft 6 Pack" (Minecraft 1.12.2, Forge 14.23.5.2859, 71 mods loaded on 2026-09-17)
World seed: unknown (one world exists at ModrinthApp/profiles/Thaumcraft 6 Pack/saves/New World; no seed was recorded)
Player setup: not applicable (no capture performed)
Capture scenario: no capture performed — metadata inventory only: sha256sum over the file plus metadata read from inside the archive (mcmod.info, META-INF/MANIFEST.MF, assets/thaumcraft/lang/en_us.lang)
Recorded by: inventory agent (read-only reference-evidence pass; no game run, no capture)
Date recorded: 2026-09-18
```

### REF-0003 — en_us.lang text extract (partial)

```text
Reference identifier: REF-0003 (inventory record, not a capture; role: extracted game-data text, incomplete — do not cite until re-fetched in full)
Minecraft version: version unverified
Forge version: not applicable
Thaumcraft version: version unverified
Artifact location: D:/dev/hytale mods/magic/research/_legal/tc6-lang-and-config/en_us.lang.txt (100,389 bytes; the file ends with the fetch tool's truncation marker, and the complete en_us.lang inside the BETA26 jar is 210,632 bytes with digest 3b78dfdbe0c035ff89bba3cb7ecbb5116800f34f10bf459f17eeae4de439c086)
SHA-256: fb149b3652c4b61052e8e9a9d301961a9c479f92a8498b597ff5f4a6f0881722
Launcher or profile: not applicable
World seed: not applicable
Player setup: not applicable
Capture scenario: no capture performed — digest computed with sha256sum; the truncation was identified by comparing the extract against assets/thaumcraft/lang/en_us.lang inside the BETA26 jar (REF-0002). The recorded digest covers the whole file, including the fetch-tool envelope (source-URL header and untrusted-content banner) that precedes the extract text.
Recorded by: inventory agent (read-only reference-evidence pass)
Date recorded: 2026-09-18
```

### REF-0004 — ConfigRecipes.java text extract (partial)

```text
Reference identifier: REF-0004 (inventory record, not a capture; role: extracted source text, incomplete — do not cite until re-fetched in full)
Minecraft version: version unverified
Forge version: not applicable
Thaumcraft version: version unverified
Artifact location: D:/dev/hytale mods/magic/research/_legal/tc6-lang-and-config/ConfigRecipes.java.txt (100,315 bytes; the file ends with the fetch tool's truncation marker, so it is not a complete copy of ConfigRecipes.java)
SHA-256: db622b2697f034c984db5fbd4df092943d667176ac3cc5e0f242a76c19dc51ec
Launcher or profile: not applicable
World seed: not applicable
Player setup: not applicable
Capture scenario: no capture performed — digest computed with sha256sum; truncation identified from the trailing fetch-tool marker. The recorded digest covers the whole file, including the fetch-tool envelope (source-URL header and untrusted-content banner) that precedes the extract text.
Recorded by: inventory agent (read-only reference-evidence pass)
Date recorded: 2026-09-18
```

### REF-0005 — first BETA26 gameplay session (capture)

```text
Reference identifier: REF-0005 (capture — first live BETA26 gameplay observation; role: rank-1 candidate with a documented environment deviation)
Minecraft version: 1.12.2 (session log: "Forge Mod Loader version 14.23.5.2859 for Minecraft 1.12.2 loading"; world level.dat Version Name "1.12.2")
Forge version: 14.23.5.2859 (same session log line)
Thaumcraft version: 6.1.BETA26 (the observed mods/ artifact's digest equals REF-0002's: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f)
Artifact location: C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/ (mods/: Thaumcraft-1.12.2-6.1.BETA26.jar and Baubles-1.12-1.5.2.jar; the observed worlds live under saves/)
SHA-256: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f
Launcher or profile: CurseForge instance "TC6 Reference" (GUID 70e70c34-b9a7-4d2b-aa7b-704226093535), singleplayer
World seed: creative session world "New World-" seed 8625173870111429838 (spawn 232 64 236; created and played 2026-09-18, ~32 minutes of world tick time / 38,228 ticks); a discarded survival world "New World" seed -4878572646635015781 (single 33-second visit)
Player setup: creative (level.dat GameType 1); fresh player in the session world; no research grants are reported for the items below
Capture scenario: three launches on 2026-09-18 local time (14:32:51-14:33:13 clean and menu-only; 14:37:43-15:18:44; 15:22:41-15:25:22). Observed items (owner-reported; cross-checked against the instance's own session logs, level.dat, and world file times): six inactive Thaumonomicon entries at first view before scanning; a zombie-related entry attributed to scanning; Salis Mundus transmuted a bookshelf into a Thaumonomicon and a crafting table into an Arcane Workbench on the fresh creative player; one block scan with the expected feedback and no change on re-scanning the same object; an item placed inside a chest was scanned via the chest; the knowledge-gain HUD feedback behaved as expected. The observed world's player research data (level.dat) contains the pseudo-keys `!gotthaumonomicon`, `!gotdream`, `!minecraft:bookshelf0`, `!minecraft:crafting_table0`, `!minecraft:chest0`, and `!BrainyZombie` — consistent with the fresh-state trigger availability and with the zombie-related entry. A workbench-block pass (gates, take-order, persistence) was not itemized in the session report. ENVIRONMENT DEVIATION: a client-side controller mod (Controllable 0.11.2) was present in mods/ during both observed launches (its config files are timestamped during those launches; the jar was removed at 15:18:53 after the second launch, re-added at 15:22:21, and finally removed at 15:26:17). FML flagged the jar at load in the second launch ("non-mod file ... in your mods directory", log 14:37:52), and the mod crashed the client JVM at the end of that launch: `hs_err_pid22664.log` (instance root, 15:18:44) records EXCEPTION_ACCESS_VIOLATION in native code (msvcrt.dll frame) on the daemon thread "Controller Input". The crash occurred during client shutdown after the world session had already ended (disconnect 15:18:05), so the recorded observations were unaffected. At jar level the mod's mixin config targets only `net.minecraft.client.*` classes and its access transformer only vanilla client fields; no Thaumcraft-targeted transformations exist. Load-bearing claims from this capture should be re-verified in a session without this deviation before promotion.
Recorded by: observations by the project owner (TheBeardedTate); compiled by the port assistant from the owner's report, the instance's session logs (logs/, including gzip rotations), level.dat parsing, and file timestamps
Date recorded: 2026-09-18
```

### REF-0006 — bytecode spot-check of the golem rank arithmetic (inspection, not a capture)

```text
Reference identifier: REF-0006 (inspection record, not a capture; role: rank-2 bytecode corroboration of a REF-0001 tree site)
Minecraft version: 1.12.2
Forge version: 14.23.5.2859
Thaumcraft version: 6.1.BETA26 (jar digest equals REF-0002's: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f)
Artifact location: C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/mods/Thaumcraft-1.12.2-6.1.BETA26.jar (read-only)
SHA-256: 9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f
Launcher or profile: not applicable (static inspection; no game launch)
World seed: not applicable
Player setup: not applicable
Capture scenario: bytecode inspection with javap -p -c (read-only; scratch in a temp directory) of EntityThaumcraftGolem.updateEntityAttributes(), EntityThaumcraftGolem.func_70658_aO() (the armour accessor), and GuiGolemBuilder.gatherInfo(): each fragile-penalty site compiles as iload / i2d / ldc2_w 0.75d / dmul / d2i — a double multiply by three-quarters truncated to int — while REF-0001's decompiled text renders the same sites as a cast of the fraction itself, which would zero the value (tree sites: EntityThaumcraftGolem.java:173, :249; GuiGolemBuilder.java:248, :256). Finding: the tree text is a decompiler artifact at those sites and the shipped arithmetic multiplies by three-quarters. Recorded because a rank-3 tree site was overruled by the rank-2 jar for one load-bearing figure (aut-02-evidence.md finding 10).
Recorded by: subagent builder and independent critic (read-only reference-evidence pass); the jar digest was re-verified by both before and after the inspection
Date recorded: 2026-09-18
```

## Tree-hash convention (REF-0001)

A source tree has no single file digest, so `REF-0001` uses a tree hash with the convention below. Any future re-verification of `REF-0001` must use the same convention; the digest is over the sorted hash lines of every file, not over any single file.

```text
cd "D:/dev/hytale mods/magic/research/_legal/tc6-decompiled"
find . -type f -not -path './.git/*' -print0 | LC_ALL=C sort -z | xargs -0 sha256sum > "$LOCALAPPDATA/Temp/tc6hash/tree_sha256sum_lines.txt"
sha256sum "$LOCALAPPDATA/Temp/tc6hash/tree_sha256sum_lines.txt"
```

- Result: `61bf52ee4409f7ba5dba9d14dae19602de61f4cb9f5b10a0bbc954c8aa7568f6` over 2,447 lines.
- Coverage: every file under the tree root except `.git/` (2,447 files, 15,905,687 bytes); each line has the form `<sha256> *./relative/path`, sorted in byte order by path.
- Cross-check: the same line set was rebuilt independently in Python (walk the tree, skip `.git`, sort relative paths in byte order, format identical lines); the rebuilt text equals the recorded file line-for-line, so the digest is reproducible.

## Resource comparison convention (REF-0001 vs REF-0002)

The resource-payload comparison pairs jar entries with tree files under the mapping jar root to `src/main/resources/` (for example jar `assets/thaumcraft/lang/en_us.lang` to tree `src/main/resources/assets/thaumcraft/lang/en_us.lang`). The pair count covers the 1,537 files present on both sides under that mapping; excluded are the jar's 3 `META-INF/` entries (build metadata) and 8 build/scaffolding files. Results: 1,390 byte-identical, 145 differing only by line endings (tree CRLF, jar LF), 2 differing in content (`mcmod.info`, `pack.mcmeta`).

## Environment feasibility (2026-09-18)

**Rank-1 status: first capture recorded — `REF-0005` (2026-09-18); itemized confirmation pending.** A clean BETA26 profile exists, is registered in the CurseForge launcher, and has been played (see the entry for the documented controller-mod deviation). What exists and what is missing:

**Present on this machine**

- Minecraft 1.12.2 + Forge 14.23.5.2859 installed twice: `C:/Users/t8rto/curseforge/minecraft/Install/versions/forge-14.23.5.2859` (CurseForge) and `C:/Users/t8rto/AppData/Roaming/ModrinthApp/meta/versions/1.12.2-14.23.5.2859` (Modrinth App, with matching `1.12.2-14.23.5.2859.json`).
- A working 1.12.2 Forge launch: the Modrinth profile "Thaumcraft 6 Pack" ran on 2026-09-17 (`logs/latest.log`: "Forge Mod Loader version 14.23.5.2859 for Minecraft 1.12.2 loading", "has successfully loaded 71 mods", world `saves/New World` created and saved; no crash reports).
- Java 8 runtime: `C:/Users/t8rto/AppData/Roaming/ModrinthApp/meta/java_versions/zulu8.96.0.205-ca-jre8.0.504-win_x64` (the runtime used by that launch).
- The BETA26 jar (REF-0002) and its required dependency `Baubles-1.12-1.5.2.jar` (108,450 bytes, sha256 `b32010b2f2778aa1188585e7ead91ad46d4cb2c715f9c778a61848ba7fe51f8d`, identical in both instances) are already on disk.
- A prepared clean reference profile: CurseForge instance `TC6 Reference` (GUID `70e70c34-b9a7-4d2b-aa7b-704226093535`, path `C:/Users/t8rto/curseforge/minecraft/Instances/TC6 Reference/`) containing exactly `Thaumcraft-1.12.2-6.1.BETA26.jar` and `Baubles-1.12-1.5.2.jar` (both digests recorded in the reference inventory above; see also `REF-0002`); the launcher registered it on 2026-09-18 (its log shows the instance scan and `Loaded 6 modpacks`). Launched and played on 2026-09-18 (see `REF-0005`); the briefly used controller mod was removed the same day, so `mods/` again contains exactly these two jars.
- A signed-in Microsoft account in the Modrinth App profile data (`TheBeardedTate`); no credentials are recorded here.
- Third-party thaum-named jars live in the same two `mods/` folders (ThaumicAugmentation 2.1.11 and 2.1.14, ThaumicInventoryScanning 2.0.10, ThaumicJEI 1.6.0-27 and 1.7.0, thaumicperiphery 0.3.1, thaumicwands 1.2.7, ThaumcraftFix 1.1.4, enchantingwiththaumcraft 1.4). They are addons, not BETA26 reference artifacts, and their presence is a further reason both existing instances count as modded environments.

**Missing or blocking for rank-1**

- No unmodified BETA26 environment among the original instances. Both remain modpacks and are not valid references: CurseForge "Thaumcraft Reimagined" (52 jars, never launched) and Modrinth "Thaumcraft 6 Pack" (71 mods loaded, including ThaumcraftFix 1.12.2-1.1.4, Thaumic Augmentation 2.1.14, TC4 Research Port, Quark, OptiFine) — observing behavior in either would violate the rank-1 requirement of an unmodified BETA26 environment; the clean `TC6 Reference` profile above replaces them for captures.
- First sessions recorded: `TC6 Reference` was launched three times on 2026-09-18 and two worlds were created (launch times, worlds, and seeds recorded in `REF-0005`); itemized per-item scenario passes remain for follow-up sessions.
- No EULA record: no `eula.txt` exists under `.minecraft`, the CurseForge root, or the Modrinth App roots. Client gameplay does not create one; a dedicated-server capture requires the owner to accept the EULA (the file must then contain `eula=true`).
- Capture artifacts: `REF-0005` records the first session's launch times, worlds, and seeds; no screenshots or recordings were captured for it. Neither original instance has been played (`playedCount` 0), and the Modrinth profile's one older world still has no recorded seed.
- The vanilla launcher `C:/Users/t8rto/AppData/Roaming/.minecraft` has no 1.12.2 version (its `versions/` holds only `26.2` and `26.3-snapshot-7`) and no `mods/` directory.
- Not evidence: `D:/dev/thaumcraft-shobie-review/build/libs/thaumcraft-6.2.0.jar` is this port's own build output (rank 4 at best), not a reference artifact.

**Search coverage (bounded, read-only).** Thaumcraft-named jars were searched for and found only in the two instances above. Checked with depth limits and no hits: `D:/` top level, `D:/Mods`, `D:/Vortex Mods`, `D:/Games`, `D:/Launcher`, `D:/minecraft earth clone`, `D:/modded fallout`, `D:/Apace`, `D:/d`, `D:/models`, `D:/omarchy`, `D:/omarchy v2`, `D:/steam`, `D:/dev`; `C:/Users/t8rto/AppData/Roaming/.minecraft` (no `mods/`); `C:/Users/t8rto/Downloads` and `C:/Users/t8rto/Documents`; and the CurseForge mod-download cache. Launcher presence: only CurseForge and Modrinth App are installed — PrismLauncher, MultiMC, gdlauncher, Technic, and ATLauncher directories do not exist under AppData Roaming or Local.

**Remaining for itemized rank-1 confirmation:** follow-up sessions that run the work-item scenario checklists (for example `C1`-`C11` of [rsr-01-evidence.md](rsr-01-evidence.md) and `S1`-`S9` of [rsr-02-evidence.md](rsr-02-evidence.md)), each entered here as a new `REF` entry; EULA acceptance by the owner is required only if a dedicated-server capture is used.

## Illustrative example (not a real capture)

The block below shows the shape of one entry. It is an example only, not a real capture, and must not be cited as evidence.

```text
EXAMPLE ONLY — illustrative entry shape, not a real capture.

Reference identifier: REF-EXAMPLE-0001
Minecraft version: 1.12.2
Forge version: 14.23.5.2860
Thaumcraft version: 6.1.BETA26
Artifact location: D:/game-instances/TC6-Reference/mods/Thaumcraft-1.12.2-6.1.BETA26.jar
SHA-256: 0a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f9
Launcher or profile: CurseForge instance "TC6 Reference" (Minecraft 1.12.2, Forge 14.23.5.2860)
World seed: 1234567890
Player setup: creative mode, full Thaumonomicon unlocked, no Warp, no other mods
Capture scenario: create a fresh world, scan one aura node, and record the displayed aspect values before quitting
Recorded by: example author (not a real capture)
Date recorded: 2026-01-01
```
