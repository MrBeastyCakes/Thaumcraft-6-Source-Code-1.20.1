package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.IPlayerWarp.EnumWarpType;
import thaumcraft.common.lib.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.tiles.essentia.TileJar;
import thaumcraft.init.ModBlockEntities;
import thaumcraft.init.ModBlocks;

import static thaumcraft.gametest.GameTestSupport.require;

/**
 * FND-01 smoke suite: proves the GameTest harness, the shared template, the block entity reload
 * helper, and player capability persistence all work in a fresh GameTest world on a dedicated server.
 * Run with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class HarnessSmokeTests {

    @GameTest(template = GameTestSupport.PLATFORM)
    public static void registriesContainThaumcraftContent(GameTestHelper helper) {
        require(helper, countOwn(ForgeRegistries.BLOCKS) > 0, "No thaumcraft blocks registered");
        require(helper, countOwn(ForgeRegistries.ITEMS) > 0, "No thaumcraft items registered");
        require(helper, countOwn(ForgeRegistries.BLOCK_ENTITY_TYPES) > 0, "No thaumcraft block entity types registered");
        require(helper, countOwn(ForgeRegistries.ENTITY_TYPES) > 0, "No thaumcraft entity types registered");
        require(helper, ModBlockEntities.JAR.get().isValid(ModBlocks.JAR_NORMAL.get().defaultBlockState()),
                "Jar block entity type does not accept the normal jar block");
        helper.succeed();
    }

    @GameTest(template = GameTestSupport.PLATFORM)
    public static void jarEssentiaSurvivesReload(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.JAR_NORMAL.get().defaultBlockState());
        TileJar jar = helper.getBlockEntity(pos);
        require(helper, jar != null, "Normal jar placed without a TileJar");

        int leftover = jar.addToContainer(Aspect.FIRE, 40);
        require(helper, leftover == 0, "Empty jar refused essentia, leftover " + leftover);

        TileJar reloaded = GameTestSupport.reloadBlockEntity(helper, pos);
        require(helper, reloaded.containerContains(Aspect.FIRE) == 40,
                "Reloaded jar holds " + reloaded.containerContains(Aspect.FIRE) + " ignis, expected 40");
        require(helper, reloaded.containerContains(Aspect.WATER) == 0, "Reloaded jar gained aqua");
        helper.succeed();
    }

    @GameTest(template = GameTestSupport.PLATFORM)
    public static void playerCapabilitiesSurviveSaveAndLoad(GameTestHelper helper) {
        Player before = helper.makeMockPlayer();
        IPlayerKnowledge knowledge = before.getCapability(ThaumcraftCapabilities.KNOWLEDGE).orElse(null);
        IPlayerWarp warp = before.getCapability(ThaumcraftCapabilities.WARP).orElse(null);
        require(helper, knowledge != null, "Knowledge capability not attached to player");
        require(helper, warp != null, "Warp capability not attached to player");

        knowledge.addResearch("FIRSTSTEPS");
        warp.set(EnumWarpType.PERMANENT, 3);
        warp.set(EnumWarpType.NORMAL, 5);
        warp.set(EnumWarpType.TEMPORARY, 7);

        CompoundTag saved = before.saveWithoutId(new CompoundTag());
        Player after = helper.makeMockPlayer();
        after.load(saved);

        IPlayerKnowledge loadedKnowledge = after.getCapability(ThaumcraftCapabilities.KNOWLEDGE).orElse(null);
        IPlayerWarp loadedWarp = after.getCapability(ThaumcraftCapabilities.WARP).orElse(null);
        require(helper, loadedKnowledge != null && loadedKnowledge.isResearchKnown("FIRSTSTEPS"),
                "Research was lost across player save/load");
        require(helper, loadedWarp != null
                        && loadedWarp.get(EnumWarpType.PERMANENT) == 3
                        && loadedWarp.get(EnumWarpType.NORMAL) == 5
                        && loadedWarp.get(EnumWarpType.TEMPORARY) == 7,
                "Warp was lost or changed across player save/load");
        helper.succeed();
    }

    private static int countOwn(IForgeRegistry<?> registry) {
        int count = 0;
        for (var key : registry.getKeys()) {
            if (Thaumcraft.MODID.equals(key.getNamespace())) {
                count++;
            }
        }
        return count;
    }
}
