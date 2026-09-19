package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thaumcraft.init.ModBlockEntities;
import thaumcraft.init.ModBlocks;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class FoundationGameTests {
    private static final String TEMPLATE = "fnd01_empty";
    private static final BlockPos TEST_POS = new BlockPos(0, 0, 0);

    private FoundationGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void registeredArcaneStoneCanBePlaced(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.ARCANE_STONE.get());

        helper.assertBlockPresent(ModBlocks.ARCANE_STONE.get(), TEST_POS);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pedestalInventoryRoundTripsIntoDistinctBlockEntity(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.PEDESTAL_ARCANE.get());
        BlockEntity placed = helper.getBlockEntity(TEST_POS);
        helper.assertTrue(placed instanceof TilePedestal,
                "Registered arcane pedestal must create TilePedestal");
        TilePedestal original = (TilePedestal) placed;

        helper.assertTrue(original.getContainerSize() == 1,
                "Pedestal inventory must expose exactly one slot");
        helper.assertTrue(original.getMaxStackSize() == 1,
                "Pedestal inventory must limit stacks to one item");

        ItemStack source = new ItemStack(Items.DIAMOND, 8);
        source.getOrCreateTag().putString("fnd01_marker", "roundtrip");
        original.setItem(0, source);
        helper.assertTrue(original.getItem(0).getCount() == 1,
                "Pedestal must clamp an inserted stack to one item");

        CompoundTag saved = original.saveWithFullMetadata();
        BlockState state = ModBlocks.PEDESTAL_ARCANE.get().defaultBlockState();
        TilePedestal restored = ModBlockEntities.PEDESTAL.get().create(TEST_POS, state);
        helper.assertTrue(restored != null, "Registered pedestal factory must create a block entity");
        restored.load(saved);

        original.clearContent();
        helper.assertTrue(restored != original, "Roundtrip target must be a distinct block entity");
        helper.assertTrue(original.isEmpty(), "Clearing the old instance must leave it empty");
        helper.assertTrue(restored.getItem(0).is(Items.DIAMOND),
                "Restored pedestal must retain item identity");
        helper.assertTrue(restored.getItem(0).getCount() == 1,
                "Restored pedestal must retain its one-item count");
        helper.assertTrue("roundtrip".equals(restored.getItem(0).getTag().getString("fnd01_marker")),
                "Restored pedestal must retain item NBT");
        helper.succeed();
    }
}
