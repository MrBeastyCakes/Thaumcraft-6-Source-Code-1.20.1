# Filesystem Map

> Generated from the current source layout by `tools/agent-pipeline/Update-FileSystemMap.ps1`.

Repository root: `thaumcraft-shobie-review` — Minecraft 1.20.1 Forge port.

## Root and Build Surface

| Path | Role |
|---|---|
| `build.gradle`, `settings.gradle`, `gradle.properties` | ForgeGradle build, mappings, versions, and dependencies |
| `gradlew`, `gradlew.bat`, `gradle/` | Gradle wrapper |
| `README.md` | upstream project claims and setup notes; not parity evidence |
| `TODO.md` | upstream implementation leads; audit against source before acting |
| `TEXTURETODO.md` | visual asset backlog |
| `AGENTS.md` | parity operating contract |
| `docs/agent-pipeline/` | agent workflow, map, and workboard |
| `tools/agent-pipeline/` | pipeline maintenance tooling |
| `src/main/java/` | compiled 1.20.1 port source |
| `src/main/java_old/` | 902 legacy reference files; do not compile or bulk-copy them |
| `src/main/resources/` | hand-authored assets, data, metadata, and language content |
| `src/main/templates/` | 1 build-time metadata templates |
| `src/generated/resources/` | 0 generated-resource files |

## Java Source — `src/main/java/thaumcraft`

| Package | Recursive file count |
|---|---:|
| `api/` | 94 |
| `client/` | 137 |
| `common/` | 493 |
| `compat/` | 4 |
| `datagen/` | 1 |
| `init/` | 14 |

`Thaumcraft.java` is the mod bootstrap. `api/` holds cross-system contracts; `common/` holds gameplay authority; `client/` is presentation-only; `compat/` holds integration adapters; `init/` owns registrations.

## Gameplay Implementation Seams — `src/main/java/thaumcraft/common`

| Package | Recursive file count |
|---|---:|
| `blocks/` | 74 |
| `config/` | 4 |
| `entities/` | 65 |
| `golems/` | 29 |
| `items/` | 88 |
| `lib/` | 133 |
| `menu/` | 28 |
| `tiles/` | 54 |
| `world/` | 18 |

## Static Content — `src/main/resources`

| Path | Recursive file count |
|---|---:|
| `assets/thaumcraft/` | 2035 |
| `data/thaumcraft/` | 370 |

### Asset Domains — `assets/thaumcraft`

| Directory | Recursive file count |
|---|---:|
| `blockstates/` | 283 |
| `lang/` | 11 |
| `loot_tables/` | 2 |
| `models/` | 689 |
| `research/` | 9 |
| `shader/` | 5 |
| `sounds/` | 111 |
| `textures/` | 924 |

### Data Domains — `data/thaumcraft`

| Directory | Recursive file count |
|---|---:|
| `damage_type/` | 4 |
| `forge/` | 13 |
| `loot_tables/` | 13 |
| `recipes/` | 270 |
| `tags/` | 29 |
| `worldgen/` | 41 |

## Test, Run, and Generated Surfaces

| Path | State / use |
|---|---|
| `src/test/java`, `src/test/resources` | 0 test-source/resource files |
| `run/` | disposable local Forge dev instance, configuration, logs, and test state |
| `build/` | disposable Gradle output, generated metadata, remapping intermediates, and JARs |
| `.gradle/` | disposable local Gradle cache/state |
| `src/generated/resources/` | generated-resource input path declared by the build |

`.gitignore` excludes `build/`, `.gradle/`, `run/`, IDE output, and Eclipse metadata. Treat those directories as generated/disposable and never use them as source-of-truth implementation files.

## Gradle Entry Points

| Command | Use |
|---|---|
| `.\\gradlew.bat compileJava` | Java compile gate |
| `.\\gradlew.bat test` | deterministic test suite |
| `.\\gradlew.bat runGameTestServer` | Minecraft GameTest validation |
| `.\\gradlew.bat runServer` | dedicated-server smoke test |
| `.\\gradlew.bat runClient` | client smoke and visual validation |
| `.\\gradlew.bat runData` | data-generation validation |
| `.\\gradlew.bat build` | package/reobfuscation gate |
