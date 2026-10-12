package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared GameTest helpers. Every Thaumcraft GameTest uses the {@link #PLATFORM} template: a 5x4x5
 * volume with a stone floor at y=0, so test blocks go at y=1 and above.
 */
public final class GameTestSupport {

    /** Template id, resolved as {@code thaumcraft:platform} (src/test/resources/data/thaumcraft/structures). */
    public static final String PLATFORM = "platform";

    private GameTestSupport() {
    }

    /** Fails the test with {@code message} unless {@code condition} holds. */
    public static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    /**
     * Simulates a save/reload of the block entity at a test-relative position: writes it with full
     * metadata, rebuilds a fresh instance from that tag through the same factory a chunk load uses,
     * and fails the test if the rebuilt instance does not write back an identical tag.
     *
     * @return the rebuilt (detached) block entity, for behavior-specific assertions
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T reloadBlockEntity(GameTestHelper helper, BlockPos relativePos) {
        BlockEntity original = helper.getBlockEntity(relativePos);
        if (original == null) {
            helper.fail("No block entity to reload at " + relativePos);
        }
        CompoundTag saved = original.saveWithFullMetadata();
        BlockEntity reloaded = BlockEntity.loadStatic(original.getBlockPos(), original.getBlockState(), saved);
        if (reloaded == null) {
            helper.fail("Block entity could not be rebuilt from its saved tag: " + saved + " at " + relativePos);
        }
        if (reloaded.getType() != original.getType()) {
            helper.fail("Reloaded block entity changed type to " + reloaded.getType() + " at " + relativePos);
        }
        CompoundTag resaved = reloaded.saveWithFullMetadata();
        if (!saved.equals(resaved)) {
            helper.fail("Block entity tag drifted across reload: " + saved + " -> " + resaved + " at " + relativePos);
        }
        return (T) reloaded;
    }
}
