package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.items.resources.ItemCrystalEssence;
import thaumcraft.common.items.resources.ItemVisCrystal;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileCentrifuge;
import thaumcraft.common.tiles.essentia.TileJar;
import thaumcraft.common.tiles.essentia.TileJarVoid;
import thaumcraft.common.tiles.essentia.TileSmelter;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.init.ModBlockEntities;
import thaumcraft.init.ModBlocks;
import thaumcraft.init.ModItems;

/**
 * FND-04 container-capacity witnesses.
 *
 * Authority: the BETA26 tree (src/main/java_old). Jar CAPACITY 250 with a short-typed
 * "Amount" tag; void jar destroys overflow at the same clamp; alembic maxAmount 128 with an
 * unconditional doesContainerAccept; tube buffer MAXAMOUNT 10 accepting single units only;
 * smelter maxVis 256; phial-style items take their capacity at construction (phial 10,
 * crystal essence 1, label 1).
 *
 * Tests whose names end in "Control" pin port behavior that is documented as differing from
 * BETA26 in docs/agent-pipeline/fnd-04-container-capacities-evidence.md; they are not parity
 * claims.
 */
@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class ContainerCapacityGameTests {
    private static final String TEMPLATE = "fnd01_empty";
    private static final BlockPos TEST_POS = new BlockPos(0, 0, 0);

    private ContainerCapacityGameTests() {
    }

    // ==================== Jar (BETA26 TileJarFillable CAPACITY 250) ====================

    @GameTest(template = TEMPLATE)
    public static void jarFillsToCapacityAndReturnsOverflowLeftover(GameTestHelper helper) {
        TileJar jar = placeJar(helper);

        helper.assertTrue(jar.addToContainer(Aspect.AIR, 200) == 0,
                "200 units must fit an empty 250-capacity jar, leftover must be 0");
        helper.assertTrue(jar.getAmount() == 200,
                "jar must store the accepted amount, got " + jar.getAmount());
        helper.assertTrue(jar.getAspect() == Aspect.AIR,
                "accepted aspect must be recorded");

        helper.assertTrue(jar.addToContainer(Aspect.AIR, 100) == 50,
                "second add must return the 50-unit overflow, BETA26 clamp is min(am, 250 - amount)");
        helper.assertTrue(jar.getAmount() == 250,
                "jar must clamp at 250, got " + jar.getAmount());

        helper.assertTrue(jar.addToContainer(Aspect.AIR, 1) == 1,
                "a full jar must reject further units and return them");
        helper.assertTrue(jar.getAmount() == 250,
                "a full jar must stay at 250");
        helper.assertTrue(jar.getSuctionAmount(Direction.UP) == 0,
                "a full jar must expose zero suction");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void jarDrainToEmptyClearsAspectAndAmount(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        jar.addToContainer(Aspect.WATER, 5);

        helper.assertTrue(!jar.takeFromContainer(Aspect.FIRE, 1),
                "draining a different aspect must fail");
        helper.assertTrue(jar.takeFromContainer(Aspect.WATER, 5),
                "draining the stored amount must succeed");
        helper.assertTrue(jar.getAmount() == 0,
                "a drained jar must read amount 0, got " + jar.getAmount());
        helper.assertTrue(jar.getAspect() == null,
                "a drained jar must clear its aspect");
        helper.assertTrue(jar.getAspects().size() == 0,
                "a drained jar must report an empty aspect list");
        helper.assertTrue(!jar.takeFromContainer(Aspect.WATER, 1),
                "an empty jar must refuse further drains");
        helper.assertTrue(!jar.doesContainerContainAmount(Aspect.WATER, 1),
                "an empty jar must fail doesContainerContainAmount");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void jarFilterGatesAcceptanceAndSuction(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        jar.setAspectFilter(Aspect.AIR);

        helper.assertTrue(jar.doesContainerAccept(Aspect.AIR),
                "a filtered jar must accept the filter aspect");
        helper.assertTrue(!jar.doesContainerAccept(Aspect.FIRE),
                "a filtered jar must reject other aspects on the acceptance query");
        helper.assertTrue(jar.getSuctionAmount(Direction.UP) == 64,
                "a filtered jar pulls with suction 64");
        helper.assertTrue(jar.getMinimumSuction() == 64,
                "a filtered jar has minimum suction 64");

        // BETA26 gates the suction/query surface through the filter, but a direct add still
        // stores into an empty jar; the filter is applied by the pull paths, not by addToContainer.
        helper.assertTrue(jar.addToContainer(Aspect.FIRE, 3) == 0,
                "a direct add of another aspect must still store into an empty filtered jar");
        helper.assertTrue(jar.getAmount() == 3 && jar.getAspect() == Aspect.FIRE,
                "the stored aspect must be the added one, got " + jar.getAspect());

        jar.setAspectFilter(null);
        helper.assertTrue(jar.getSuctionAmount(Direction.UP) == 32,
                "an unfiltered jar pulls with suction 32");
        helper.assertTrue(jar.getMinimumSuction() == 32,
                "an unfiltered jar has minimum suction 32");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void jarAmountPersistsAsShortAcrossSaveLoad(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        jar.addToContainer(Aspect.FIRE, 250);

        CompoundTag saved = jar.saveWithFullMetadata();
        helper.assertTrue(saved.getTagType("Amount") == Tag.TAG_SHORT,
                "jar Amount must serialize as a short tag, got tag type " + saved.getTagType("Amount"));
        helper.assertTrue(saved.getShort("Amount") == 250,
                "the serialized short must hold the full 250 units");

        TileJar restored = (TileJar) ModBlockEntities.JAR.get()
                .create(helper.absolutePos(TEST_POS), ModBlocks.JAR_NORMAL.get().defaultBlockState());
        helper.assertTrue(restored != null, "the registered jar factory must create a block entity");
        restored.load(saved);

        helper.assertTrue(restored != jar, "the restored jar must be a distinct instance");
        helper.assertTrue(restored.getAspect() == Aspect.FIRE, "the restored jar must keep its aspect");
        helper.assertTrue(restored.getAmount() == 250,
                "the restored jar must keep the exact amount, got " + restored.getAmount());

        helper.succeed();
    }

    // ==================== Void jar (BETA26 TileJarFillableVoid, cap 250, overflow destroyed) ====================

    @GameTest(template = TEMPLATE)
    public static void voidJarDestroysOverflowAndKeepsCapacityClamp(GameTestHelper helper) {
        TileJarVoid jar = placeVoidJar(helper);

        helper.assertTrue(jar.addToContainer(Aspect.AIR, 300) == 0,
                "a void jar must destroy the overflow and report zero leftover");
        helper.assertTrue(jar.getAmount() == 250,
                "a void jar must clamp at 250, got " + jar.getAmount());
        helper.assertTrue(jar.addToContainer(Aspect.AIR, 5) == 0,
                "a void jar must keep voiding overflow after the clamp");
        helper.assertTrue(jar.getAmount() == 250,
                "a void jar must stay at 250");
        helper.assertTrue(jar.addToContainer(Aspect.FIRE, 4) == 4,
                "a void jar holding another aspect must refuse the mismatch and return it");
        helper.assertTrue(jar.getAmount() == 250,
                "a refused add must not change the stored amount");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void voidJarAcceptsAnyAspectWhenEmptyDespiteFilter(GameTestHelper helper) {
        TileJarVoid jar = placeVoidJar(helper);
        jar.setAspectFilter(Aspect.AIR);

        helper.assertTrue(!jar.doesContainerAccept(Aspect.FIRE),
                "a filtered void jar must still report the filter on the acceptance query");

        int leftover = jar.addToContainer(Aspect.FIRE, 3);
        helper.assertTrue(leftover == 0,
                "BETA26 void jars void any aspect accepted into an empty jar, got leftover " + leftover);
        helper.assertTrue(jar.getAspect() == Aspect.FIRE,
                "the stored aspect must be the added one, got " + jar.getAspect());
        helper.assertTrue(jar.getAmount() == 3,
                "the stored amount must be 3, got " + jar.getAmount());

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void voidJarAddEssentiaReportsConsumedAmountOnly(GameTestHelper helper) {
        TileJarVoid jar = placeVoidJar(helper);
        jar.setAspectFilter(Aspect.AIR);
        jar.addToContainer(Aspect.AIR, 250);

        helper.assertTrue(jar.addEssentia(Aspect.FIRE, 5, Direction.UP) == 0,
                "BETA26 addEssentia returns amount - leftover; a refused aspect must report 0 consumed");
        helper.assertTrue(jar.getAmount() == 250,
                "a refused transport add must not change the stored amount");

        helper.assertTrue(jar.addEssentia(Aspect.AIR, 5, Direction.UP) == 5,
                "a void jar must report the units it accepted and voided for the stored aspect");
        helper.assertTrue(jar.getAmount() == 250,
                "a void jar must stay clamped at 250");

        helper.assertTrue(jar.addEssentia(Aspect.FIRE, 5, Direction.UP) == 0,
                "a full void jar holding another aspect must not report consuming any units");

        helper.succeed();
    }

    // ==================== Alembic (BETA26 TileAlembic maxAmount 128) ====================

    @GameTest(template = TEMPLATE)
    public static void alembicClampsAt128AndReturnsLeftover(GameTestHelper helper) {
        TileAlembic alembic = placeAlembic(helper);

        helper.assertTrue(alembic.addToContainer(Aspect.WATER, 100) == 0,
                "100 units must fit an empty 128-capacity alembic");
        helper.assertTrue(alembic.addToContainer(Aspect.WATER, 50) == 22,
                "the alembic must return the 22-unit overflow, BETA26 clamp is min(am, maxAmount - amount)");
        helper.assertTrue(alembic.getAmount() == 128,
                "the alembic must clamp at 128, got " + alembic.getAmount());
        helper.assertTrue(alembic.addToContainer(Aspect.WATER, 1) == 1,
                "a full alembic must reject further units");
        helper.assertTrue(alembic.doesContainerContainAmount(Aspect.WATER, 128),
                "128 stored units must satisfy doesContainerContainAmount");
        helper.assertTrue(!alembic.doesContainerContainAmount(Aspect.WATER, 129),
                "doesContainerContainAmount must fail above the stored amount");
        helper.assertTrue(alembic.containerContains(Aspect.WATER) == 128,
                "containerContains must report the stored amount");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void alembicDrainToEmptyClearsState(GameTestHelper helper) {
        TileAlembic alembic = placeAlembic(helper);
        alembic.addToContainer(Aspect.WATER, 4);

        helper.assertTrue(!alembic.takeFromContainer(Aspect.AIR, 1),
                "the alembic must refuse a mismatched take");
        helper.assertTrue(alembic.takeFromContainer(Aspect.WATER, 4),
                "the alembic must serve a matching take");
        helper.assertTrue(alembic.getAmount() == 0 && alembic.getAspect() == null,
                "an emptied alembic must clear aspect and amount");
        helper.assertTrue(!alembic.takeFromContainer(Aspect.WATER, 1),
                "an empty alembic must refuse further takes");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void alembicDoesContainerAcceptIgnoresFilter(GameTestHelper helper) {
        TileAlembic alembic = placeAlembic(helper);
        alembic.setAspectFilter(Aspect.AIR);

        helper.assertTrue(alembic.doesContainerAccept(Aspect.FIRE),
                "BETA26 TileAlembic.doesContainerAccept is unconditional even with a filter set");
        helper.assertTrue(alembic.addToContainer(Aspect.FIRE, 1) == 1,
                "the filter still gates storage through addToContainer");
        helper.assertTrue(alembic.getAmount() == 0,
                "a refused add must not store anything");

        helper.succeed();
    }

    // ==================== Centrifuge (BETA26 has no capacity constant; one-unit output slot) ====================

    @GameTest(template = TEMPLATE)
    public static void centrifugeBufferClampCharacterizationControl(GameTestHelper helper) {
        // Control, not parity: BETA26 TileCentrifuge holds exactly one unit in a single output
        // slot and has no capacity constant. The port keeps a 10-unit input buffer by design;
        // this witness pins that buffer's clamp/leftover arithmetic so it cannot drift silently.
        TileCentrifuge centrifuge = placeCentrifuge(helper);

        helper.assertTrue(centrifuge.addToContainer(Aspect.MOTION, 3) == 0,
                "an empty centrifuge buffer must accept 3 units of compound essentia");
        helper.assertTrue(centrifuge.getAmount() == 3,
                "the centrifuge buffer must hold the accepted amount, got " + centrifuge.getAmount());
        helper.assertTrue(centrifuge.addToContainer(Aspect.MOTION, 12) == 5,
                "the centrifuge must clamp at its 10-unit buffer and return the 5-unit overflow");
        helper.assertTrue(centrifuge.getAmount() == 10,
                "the centrifuge buffer must clamp at 10, got " + centrifuge.getAmount());
        helper.assertTrue(centrifuge.addToContainer(Aspect.AIR, 2) == 2,
                "primal aspects must be refused by the centrifuge container");
        helper.assertTrue(centrifuge.containerContains(Aspect.MOTION) == 10,
                "containerContains must report the buffered amount");
        helper.assertTrue(!centrifuge.takeFromContainer(Aspect.MOTION, 1),
                "the centrifuge container must not expose direct takes");

        helper.succeed();
    }

    // ==================== Tube buffer (BETA26 TileTubeBuffer MAXAMOUNT 10, single units) ====================

    @GameTest(template = TEMPLATE)
    public static void bufferCapsAtTenSingleUnits(GameTestHelper helper) {
        TileTubeBuffer buffer = placeBuffer(helper);

        for (int i = 0; i < 9; i++) {
            helper.assertTrue(buffer.addToContainer(Aspect.AIR, 1) == 0,
                    "single unit " + (i + 1) + " must fit the 10-unit buffer");
        }
        helper.assertTrue(buffer.addToContainer(Aspect.FIRE, 1) == 0,
                "the buffer must accept a different aspect into its remaining space");
        helper.assertTrue(buffer.containerContains(Aspect.FIRE) == 1,
                "the buffer must track mixed aspects");
        helper.assertTrue(buffer.addToContainer(Aspect.AIR, 1) == 1,
                "a full buffer must return the refused unit");
        helper.assertTrue(buffer.addToContainer(Aspect.AIR, 3) == 3,
                "BETA26 buffers only accept single-unit additions and must return larger batches untouched");
        helper.assertTrue(buffer.takeEssentia(Aspect.FIRE, 5, Direction.UP) == 1,
                "takeEssentia must clamp its request to the stored amount");
        helper.assertTrue(buffer.containerContains(Aspect.FIRE) == 0,
                "the taken aspect must be removed");
        helper.assertTrue(buffer.takeFromContainer(Aspect.AIR, 9),
                "the buffer must serve the remaining take");
        helper.assertTrue(!buffer.takeFromContainer(Aspect.AIR, 1),
                "an empty buffer must refuse further takes");
        helper.assertTrue(buffer.takeEssentia(Aspect.AIR, 1, Direction.UP) == 0,
                "nothing can be taken from an empty buffer");

        helper.succeed();
    }

    // ==================== Smelter (BETA26 TileSmelter maxVis 256) ====================

    @GameTest(template = TEMPLATE)
    public static void smelterBufferLimitAndVisScaling(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.SMELTER.get());
        BlockEntity placed = helper.getBlockEntity(TEST_POS);
        helper.assertTrue(placed instanceof TileSmelter, "the registered smelter must create a TileSmelter");
        TileSmelter smelter = (TileSmelter) placed;

        smelter.aspects.add(Aspect.AIR, 250);
        smelter.aspects.add(Aspect.FIRE, 6);
        smelter.vis = smelter.aspects.visSize();
        helper.assertTrue(smelter.vis == 256,
                "the smelter buffer must track a 256-unit vis size, got " + smelter.vis);
        helper.assertTrue(smelter.getVisScaled(256) == 256,
                "the GUI scale must divide by the 256-unit capacity, got " + smelter.getVisScaled(256));

        helper.assertTrue(smelter.takeFromContainer(Aspect.AIR, 250),
                "the smelter must serve a take up to the stored amount");
        helper.assertTrue(smelter.vis == 6,
                "a take must refresh the vis size, got " + smelter.vis);
        helper.assertTrue(!smelter.takeFromContainer(Aspect.AIR, 1),
                "the smelter must refuse a take above the stored amount");
        helper.assertTrue(smelter.takeFromContainer(Aspect.FIRE, 6),
                "the smelter must serve the remaining take");
        helper.assertTrue(smelter.vis == 0,
                "an emptied smelter buffer must report vis 0");

        helper.succeed();
    }

    // ==================== Phial-style items ====================

    @GameTest(template = TEMPLATE)
    public static void phialFillsExactlyTenFromJar(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        jar.addToContainer(Aspect.AIR, 10);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PHIAL_EMPTY.get()));

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult result = held.getItem().useOn(useOn(helper, player, TEST_POS));
        helper.assertTrue(result.consumesAction(),
                "filling a phial from a jar with at least 10 units must consume the interaction, got " + result);

        helper.assertTrue(jar.getAmount() == 0,
                "the jar must lose exactly the 10-unit phial capacity, got " + jar.getAmount());
        helper.assertTrue(jar.getAspect() == null, "the emptied jar must clear its aspect");

        ItemStack filled = findPhial(player, true);
        helper.assertTrue(filled != null, "the player must receive a filled phial");
        AspectList aspects = ((ItemPhial) filled.getItem()).getAspects(filled);
        helper.assertTrue(aspects != null && aspects.getAmount(Aspect.AIR) == 10,
                "the phial must hold exactly its 10-unit capacity, got " + aspects);

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void phialFillRejectedBelowCapacity(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        jar.addToContainer(Aspect.AIR, 9);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PHIAL_EMPTY.get()));

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult result = held.getItem().useOn(useOn(helper, player, TEST_POS));

        helper.assertTrue(!result.consumesAction(),
                "a jar below the 10-unit capacity must not fill a phial, got " + result);
        helper.assertTrue(jar.getAmount() == 9 && jar.getAspect() == Aspect.AIR,
                "the jar must keep its 9 units, got " + jar.getAmount());
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.PHIAL_EMPTY.get()),
                "the held phial must stay empty");
        helper.assertTrue(findPhial(player, true) == null,
                "no filled phial may be produced below capacity");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void phialEmptiesIntoJarOnlyWhenCapacityFits(GameTestHelper helper) {
        TileJar jar = placeJar(helper);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemPhial.makeFilledPhial(Aspect.FIRE));

        jar.addToContainer(Aspect.FIRE, 245);
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult rejected = held.getItem().useOn(useOn(helper, player, TEST_POS));
        helper.assertTrue(!rejected.consumesAction(),
                "a jar with only 5 units of space must refuse the 10-unit phial, got " + rejected);
        helper.assertTrue(jar.getAmount() == 245,
                "a refused phial must not change the jar, got " + jar.getAmount());
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.PHIAL_FILLED.get()),
                "the refused phial must stay filled in hand");

        jar.takeFromContainer(Aspect.FIRE, 5);
        InteractionResult accepted = held.getItem().useOn(useOn(helper, player, TEST_POS));
        helper.assertTrue(accepted.consumesAction(),
                "a jar with 10 units of space must accept the 10-unit phial, got " + accepted);
        helper.assertTrue(jar.getAmount() == 250,
                "the jar must fill to capacity, got " + jar.getAmount());
        helper.assertTrue(findPhial(player, false) != null,
                "the spent phial must become an empty phial");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void phialNbtRoundTripsAndCapacityNumbersMatchBeta26(GameTestHelper helper) {
        ItemPhial emptyPhial = (ItemPhial) ModItems.PHIAL_EMPTY.get();
        ItemPhial filledPhial = (ItemPhial) ModItems.PHIAL_FILLED.get();
        helper.assertTrue(emptyPhial.getCapacity() == 10,
                "BETA26 phials are constructed with capacity 10, got " + emptyPhial.getCapacity());
        helper.assertTrue(filledPhial.getCapacity() == 10,
                "BETA26 phials are constructed with capacity 10, got " + filledPhial.getCapacity());

        ItemStack original = ItemPhial.makeFilledPhial(Aspect.ORDER);
        helper.assertTrue(filledPhial.getAmount(original) == 10,
                "makeFilledPhial must fill to the 10-unit capacity, got " + filledPhial.getAmount(original));

        CompoundTag serialized = original.save(new CompoundTag());
        ItemStack restored = ItemStack.of(serialized);
        helper.assertTrue(restored.is(ModItems.PHIAL_FILLED.get()),
                "the serialized phial must keep its item identity");
        helper.assertTrue(((ItemPhial) restored.getItem()).getAmount(restored) == 10,
                "the restored phial must keep the exact amount, got " + ((ItemPhial) restored.getItem()).getAmount(restored));
        helper.assertTrue(Aspect.ORDER.equals(((ItemPhial) restored.getItem()).getAspect(restored)),
                "the restored phial must keep its aspect");

        ItemStack crystal = ItemCrystalEssence.createStack(ModItems.CRYSTAL_ESSENCE.get(), Aspect.FIRE, 1);
        AspectList crystalAspects = ((ItemCrystalEssence) crystal.getItem()).getAspects(crystal);
        helper.assertTrue(crystalAspects != null && crystalAspects.getAmount(Aspect.FIRE) == 1,
                "BETA26 crystal essence stores 1 unit, got " + crystalAspects);

        ItemVisCrystal visCrystal = (ItemVisCrystal) ModItems.VIS_CRYSTAL_AIR.get();
        AspectList visAspects = visCrystal.getAspects(new ItemStack(visCrystal));
        helper.assertTrue(visAspects != null && visAspects.getAmount(Aspect.AIR) == 1,
                "the vis crystal must store 1 unit, got " + visAspects);

        helper.succeed();
    }

    // ==================== Fixtures ====================

    private static TileJar placeJar(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.JAR_NORMAL.get());
        return requireTile(helper, TileJar.class, "jar");
    }

    private static TileJarVoid placeVoidJar(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.JAR_VOID.get());
        return requireTile(helper, TileJarVoid.class, "void jar");
    }

    private static TileAlembic placeAlembic(GameTestHelper helper) {
        // Deferred issue ALC-02: BlockAlembic.newBlockEntity is still a null stub, so the
        // registered alembic tile is attached to the placed block directly. Only the
        // container surface is witnessed here; alembic processing stays with the ALC lane.
        helper.setBlock(TEST_POS, ModBlocks.ALEMBIC.get());
        TileAlembic alembic = new TileAlembic(helper.absolutePos(TEST_POS), ModBlocks.ALEMBIC.get().defaultBlockState());
        helper.getLevel().setBlockEntity(alembic);
        return requireTile(helper, TileAlembic.class, "alembic");
    }

    private static TileCentrifuge placeCentrifuge(GameTestHelper helper) {
        helper.setBlock(TEST_POS, ModBlocks.CENTRIFUGE.get());
        return requireTile(helper, TileCentrifuge.class, "centrifuge");
    }

    private static TileTubeBuffer placeBuffer(GameTestHelper helper) {
        // Deferred issue ALC-02: BlockTube.newBlockEntity is still a null stub, so the
        // registered buffer tile is attached to the placed block directly. Only the
        // container surface is witnessed here; tube routing stays with the ALC lane.
        helper.setBlock(TEST_POS, ModBlocks.TUBE_BUFFER.get());
        TileTubeBuffer buffer = new TileTubeBuffer(helper.absolutePos(TEST_POS), ModBlocks.TUBE_BUFFER.get().defaultBlockState());
        helper.getLevel().setBlockEntity(buffer);
        return requireTile(helper, TileTubeBuffer.class, "buffer tube");
    }

    private static <T extends BlockEntity> T requireTile(GameTestHelper helper, Class<T> type, String label) {
        BlockEntity placed = helper.getBlockEntity(TEST_POS);
        if (!type.isInstance(placed)) {
            throw new IllegalStateException("The registered " + label + " block must create a " + type.getSimpleName()
                    + ", got " + placed);
        }
        return type.cast(placed);
    }

    private static UseOnContext useOn(GameTestHelper helper, Player player, BlockPos relativePos) {
        BlockPos absolute = helper.absolutePos(relativePos);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    private static ItemStack findPhial(Player player, boolean filled) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemPhial phial && phial.isFilled() == filled) {
                return stack;
            }
        }
        return null;
    }
}
