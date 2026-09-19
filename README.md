# Thaumcraft 6 - 1.20.1 Port

This repository contains the ongoing effort to port **Thaumcraft 6** from Minecraft 1.12.2 (Forge) to Minecraft 1.20.1 (Forge).

## 📌 Project Status

**This repository is an ongoing port, not a release-ready build.** A normal survival progression loop is not yet verified end to end, and 30 of the 32 tracked work items are blocked pending repairs.

Parity status is governed by [`docs/agent-pipeline/parity-workboard.md`](docs/agent-pipeline/parity-workboard.md). As of 2026-09-19 the workboard tracks **32 work items**: **1 done** (`FND-03`, the parity evidence index), **1 ready and unclaimed** (`FND-01`, the test and GameTest harness), and **30 blocked** pending repairs.

Top confirmed playability blockers ([`docs/agent-pipeline/deferred-issues.md`](docs/agent-pipeline/deferred-issues.md)): scanning and research knowledge sync are not server-authoritative; the Arcane Workbench bypasses its crystal and Vis checks; smelters always receive an empty aspect list; Alembic and tube block-entity factories return `null`; the Infusion Matrix has no block entity or ticker; and the Focal Manipulator does not open while focus graphs never reach their delivery effects.

Earlier completion percentages in this README were not evidence-backed: they reflected file and recipe counts from the porting effort, not verified runtime behavior. Treat any claim below that is not backed by workboard evidence as a lead, not proof.

---

## 📜 Historical Development Notes (Unverified)

The notes below are historical development notes from the porting effort. They are not parity evidence; the [parity workboard](docs/agent-pipeline/parity-workboard.md) is authoritative.

At the time of these notes the codebase compiled, and upstream reported the following:
- Compiles without errors
- Loads in Minecraft 1.20.1 with Forge 47.3.0
- Upstream reported joining a world in a development environment
- Aura system has background threads for all dimensions (upstream-reported)
- Research system loads 64 entries across 7 categories (upstream-reported)
- Blocks and items are registered (counts are historical)
- JEI integration is present (upstream-reported; recipe visibility was not re-verified)
- Recipe-to-research linking was reported as validated during development (upstream-reported)

### Known Issues (Non-Fatal)
- Some research entries fail to load due to uppercase ResourceLocation names (1.20+ requires lowercase)
- Recipe book categories for custom recipes show warnings
- Some block models use placeholder textures (tubes, some devices)

---

## 🚧 Development Status

*Historical development notes from the porting effort: the counts and percentages below are not parity evidence and were never runtime-validated; the [parity workboard](docs/agent-pipeline/parity-workboard.md) is authoritative.*

### Progress by Category

| Category | Ported | Original | Historical estimate | Status |
|----------|--------|----------|---------------------|--------|
| **Java Files** | 702 | 901 | 78% | 🔄 In Progress |
| **Blocks** | 175 | 91+ | 100%+ | 📜 Upstream-reported |
| **Items** | 179 | 90+ | 100%+ | 📜 Upstream-reported |
| **Block Entities** | 50 | 31 | 100%+ | 📜 Upstream-reported |
| **Entities** | 46 | 35+ | 100%+ | 📜 Upstream-reported |
| **Mob Effects** | 9 | 9 | 100% | 📜 Upstream-reported |
| **Menus/GUIs** | 19 | 17+2 | 100% | 📜 Upstream-reported (consolidated) |
| **Entity Renderers** | 35 | ~40 | 100% | 📜 Upstream-reported |
| **Block Entity Renderers** | 23 | ~25 | 92% | 🔄 In Progress |
| **JEI Integration** | 3 | 3 | 100% | 📜 Upstream-reported |

### Recipe Progress

| Recipe Type | Created | In JEI (reported) | Original | Historical estimate |
|-------------|---------|-------------------|----------|---------------------|
| **Arcane Workbench** | 79 | ✅ 79 | 81 | 98% |
| **Crucible** | 55 | ✅ 55 | 52 | 100%+ |
| **Infusion** | 62 | ✅ 62 | 60 | 100%+ |
| **Vanilla Crafting** | 64 | ✅ 64 | 64 | 100% |
| **Smelting** | 8 | ✅ 8 | 8 | 100% |
| **Total** | **268** | **268** | **265** | **100%+** |

### JEI Integration (Historical Report)

| Feature | Reported at the time |
|---------|----------------------|
| Arcane Workbench Category | Shows vis cost, crystal requirements |
| Crucible Category | Shows aspect requirements |
| Infusion Category | Shows instability, aspects, research |
| Recipe Catalysts | Click workbench/crucible/matrix to see recipes |
| Research Requirements | Displayed on recipe types |

### System Status (Historical Report)

| System | Reported status | Notes |
|--------|-----------------|-------|
| **Build System** | 📜 Upstream-reported | Gradle 8.8, Java 17+, Forge 47.3.0 |
| **Registration** | 📜 Upstream-reported | DeferredRegister classes present |
| **Aspect System** | 📜 Upstream-reported | 51 aspects, AspectList, containers; shared attribution/lookup is not established (`FND-04`) |
| **Aura System** | 📜 Upstream-reported | Chunk-based vis/flux, background thread; persistence unvalidated (`WLD-01`) |
| **Research API** | 📜 Upstream-reported | Categories, stages, scanning; scanning is not server-authoritative (`RSR-01`) |
| **Crafting Systems** | 📜 Upstream-reported | Arcane, Crucible, Infusion, Thaumatorium; confirmed defects in `RSR-02`, `ALC-00`, `ALC-03`, `ALC-04` |
| **Essentia System** | 📜 Upstream-reported | Tubes, jars, transport, centrifuge; alembic/tube factories return `null` (`ALC-02`), smelter aspect input broken (`ALC-01`) |
| **Infusion Altar** | 📜 Upstream-reported | Matrix, pedestals, stabilizers; the Infusion Matrix has no block entity or ticker (`ALC-03`) |
| **Golem System** | 📜 Upstream-reported | Entity, seals, AI; seal storage and configuration UI missing (`AUT-01`), tasks unvalidated (`AUT-02`) |
| **Focus/Casting** | 📜 Upstream-reported | Caster, foci, effects; the Focal Manipulator UI does not open (`CAS-01`) and focus execution is broken (`CAS-02`) |
| **Curios Integration** | 📜 Upstream-reported | Replaces Baubles API |
| **JEI Integration** | 📜 Upstream-reported | 3 custom categories; supported combinations tracked in `REL-01` |
| **Multiblock System** | 📜 Upstream-reported | 9 dust triggers, salis mundus reported functional |
| **World Generation** | 🔄 Partial | Biomes, ores ported; structures partial (`WLD-02`, `WLD-04`) |
| **Particles** | 🔄 Partial | Core particles; some effects pending |
| **Networking** | 📜 Upstream-reported | PacketHandler with SimpleChannel |
| **Research-Recipe Link** | 📜 Upstream-reported | 196 recipes had research keys at the time (historical) |

---

## 📦 Historical Implementation Inventory (Unverified)

### Blocks (191 registered)
- **Crafting**: Arcane Workbench, Crucible, Infusion Matrix, Research Table, Thaumatorium, Focal Manipulator, Pattern Crafter
- **Essentia**: Jars (normal/void/brain), Tubes (6 types), Alembic, Smelters (basic/thaumium/void), Centrifuge
- **Devices**: Lamps (arcane/growth/fertility), Mirrors, Pedestals, Bellows, Hungry Chest, Levitator, Condenser
- **World**: Ores (amber/cinnabar/quartz + deepslate), Crystals, Plants, Trees (Greatwood/Silverwood)
- **Taint**: Taint Fibre, Soil, Rock, Crust, Log, Feature, Geyser, Flux Goo
- **Decorative**: Candles (16), Nitor (16), Banners (17), Paving Stones, Metal Blocks

### Items (175 registered)
- **Tools**: Thaumium, Void, Elemental sets (5 tools each), Primal Crusher, Crimson Blade
- **Armor**: Thaumium, Void, Fortress, Robes, Void Robes, Cultist sets
- **Curios**: Goggles, Vis Amulet, Cloud Ring, Curiosity Band, Charms (Verdant/Voidseer/Undying)
- **Caster**: Basic Gauntlet, Foci (3 tiers), Focus Pouch
- **Resources**: Ingots, Nuggets, Plates, Clusters, Crystals, Phials, Mechanisms

### Entities (46 registered)
- **Bosses**: Eldritch Warden, Eldritch Golem, Cultist Leader, Giant Taintacle
- **Monsters**: Wisps, Pech, Mind Spider, Eldritch creatures, Cultists, Taint creatures
- **Constructs**: Thaumcraft Golem, Turrets (2 types), Arcane Bore
- **Projectiles**: Focus projectiles, Alumentum, Bottle Taint, Grapple
- **Special**: Flux Rift, Cultist Portal, Following Item

### Recipes (268 created; JEI visibility reported at the time)
- **Arcane** (79): Mechanisms, Thaumometer, Goggles, Tubes, Smelters, Devices, Armor, Tools
- **Crucible** (55): Metal transmutation, Vis crystals, Seals, Hedge alchemy
- **Infusion** (62): Foci, Mirrors, Lamps, Tools, Armor, Curios, Clusters, Charms
- **Crafting** (64): Basic recipes, storage blocks, decorative items
- **Smelting** (8): Ore processing

---

## 🏗 Project Structure

```
src/main/java/thaumcraft/
├── Thaumcraft.java              # Main mod class
├── init/                        # Registration
│   ├── ModBlocks.java           # 191 blocks
│   ├── ModItems.java            # 175 items
│   ├── ModBlockEntities.java    # 50 block entities
│   ├── ModEntities.java         # 46 entities
│   ├── ModEffects.java          # 9 effects
│   ├── ModRecipeTypes.java      # Recipe types
│   └── ModMenuTypes.java        # Menu types
├── api/                         # Public API
│   ├── aspects/                 # Aspect, AspectList
│   ├── research/                # Research system
│   ├── crafting/                # Recipe interfaces
│   └── aura/                    # Aura helpers
├── common/                      # Server-side
│   ├── blocks/                  # Block implementations
│   ├── tiles/                   # Block entities
│   ├── items/                   # Item implementations
│   ├── entities/                # Entity implementations
│   ├── golems/                  # Golem system
│   ├── world/                   # World gen, aura
│   ├── lib/crafting/            # Recipe implementations
│   └── menu/                    # Container menus
└── client/                      # Client-side
    ├── gui/screens/             # GUI screens
    ├── renderers/               # Renderers
    └── models/                  # Entity models

src/main/java_old/               # Original 1.12.2 (REFERENCE ONLY)

src/main/resources/
├── data/thaumcraft/
│   ├── recipes/                 # 268 JSON recipes
│   │   ├── arcane_workbench/    # 79 arcane recipes
│   │   ├── crafting/            # 64 vanilla recipes
│   │   ├── crucible/            # 55 crucible recipes
│   │   ├── infusion/            # 62 infusion recipes
│   │   └── smelting/            # 8 smelting recipes
│   ├── worldgen/                # Biomes, features
│   └── tags/                    # Block/item tags
└── assets/thaumcraft/
    ├── textures/                # All textures
    ├── models/                  # Block/item models
    ├── blockstates/             # Block states
    ├── research/                # 7 research category JSONs
    └── lang/                    # 9 languages
```

---

## 🔧 Key Changes from 1.12.2

| 1.12.2 | 1.20.1 |
|--------|--------|
| `RegistryEvent.Register<T>` | `DeferredRegister<T>` |
| `TileEntity` | `BlockEntity` |
| `readFromNBT`/`writeToNBT` | `load`/`saveAdditional` |
| `ITickable` | `BlockEntityTicker` |
| `EntityEntry` | `EntityType<T>` |
| `EntityAIBase` | `Goal` |
| `SharedMonsterAttributes` | `AttributeSupplier` |
| Baubles API | Curios API |
| Hardcoded recipes | JSON data-driven |
| `GuiContainer` | `AbstractContainerScreen` |

---

## 🛠 Building & Running

### Prerequisites
- JDK 17 or higher
- Gradle (wrapper included)

### Commands
```bash
# Compile (verify no errors)
./gradlew compileJava

# Build JAR
./gradlew build

# Run Client (requires Java 17)
export JAVA_HOME=/path/to/java17
./gradlew runClient

# Run Server  
./gradlew runServer

# IDE Setup
./gradlew genIntellijRuns   # IntelliJ IDEA
./gradlew genEclipseRuns    # Eclipse
```

### Java Version Note
The project requires **Java 17** to run. If you have multiple Java versions installed, set `JAVA_HOME` before running:
```bash
# Example on Linux
export JAVA_HOME=/home/user/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
./gradlew runClient
```

### Output
`build/libs/thaumcraft-1.20.1-6.0.0.jar`

---

## 📋 Historical Porting Checklist (Unverified)

The checkboxes below were marked during the porting effort and are historical development notes, not parity evidence. Current remaining work is tracked in the [parity workboard](docs/agent-pipeline/parity-workboard.md), where 30 items are blocked and `FND-01` is ready and unclaimed.

### High Priority - reported done during development
- [x] ~~Create all arcane recipes~~ (79/79)
- [x] ~~Create vanilla crafting recipes~~ (64/64)
- [x] ~~Create smelting recipes~~ (8/8)
- [x] ~~Fix runClient blocker~~ (mods.toml format, BlockTCDevice constructor)
- [x] ~~Fix duplicate capability registration~~
- [x] ~~JEI Integration~~ (3 custom categories; upstream-reported)
- [x] ~~Research-Recipe key alignment~~ (196 recipes had research keys at the time)
- [x] ~~Fix research JSON files~~ (added legacy item mappings)
- [x] ~~Fix item stack parsing in research system~~ (legacy format support)
- [x] ~~GUIs~~ (19 screens - 2 old turret GUIs consolidated into 1)
- [x] ~~Entity Renderers~~ (35 renderers - advanced turret renderer added)

### Medium Priority - reported done during development
- [x] ~~Implement golem seal-based AI switching~~ (reported functional at the time)
- [x] ~~Particle effects~~ (FXDispatcher present with 30+ custom particles)
- [x] ~~Casting visual effects~~ (FXBeamWand, FXBeamBore, FXArc, FXBolt present)
- [x] ~~Structure generation~~ (EldritchObeliskFeature, RuinedTowerFeature added)
- [x] ~~Golem press crafting~~ (TileGolemBuilder present)

### Lower Priority - reported done during development
- [x] ~~Parchment mappings~~ (configured, requires Java 17-21)
- [x] ~~Re-enable Curios runtime dependency~~ (CuriosCompat wrapper)
- [x] ~~Port multiblock detection system~~ (ConfigMultiblocks.java)

### Still Open
- [ ] Add missing block models (tubes, some devices)
- [ ] Comprehensive testing
- [ ] Performance optimization

---

## 📝 Contributing

1. Check [TODO.md](TODO.md) for detailed task breakdown
2. Reference original code in `src/main/java_old/`
3. Follow existing patterns in new source
4. Test with `./gradlew compileJava`

### Quick Start Areas
- Add missing recipes in `src/main/resources/data/thaumcraft/recipes/`
- Port GUIs from `java_old/client/gui/` to `client/gui/screens/`
- Complete entity renderers in `client/renderers/entity/`

---

## 📄 License

Community port of Thaumcraft. Original mod by Azanor.

---

*Last updated: 2026-09-19 | Status: active development; `FND-01` is ready and unclaimed. See the [parity workboard](docs/agent-pipeline/parity-workboard.md) for authoritative status; earlier completion percentages and "playable" claims were not evidence-backed.*
