package thaumcraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.research.ScanGeneric;
import thaumcraft.init.ModItems;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class AspectLookupGameTests {
    private static final String TEMPLATE = "fnd01_empty";
    private static final String MOD_ID = "thaumcraft";

    private AspectLookupGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void containedAspectsOverrideRegisteredCrystalTags(GameTestHelper helper) {
        ItemStack crystal = new ItemStack(ModItems.VIS_CRYSTAL_AIR.get());
        AspectList registered = AspectHelper.getRegisteredObjectAspects(crystal);
        helper.assertTrue(registered != null, "Air crystal must have registered base tags");
        helper.assertTrue(registered.getAmount(Aspect.AIR) == 5,
                "Air crystal base registration must retain AIR 5");
        helper.assertTrue(registered.getAmount(Aspect.CRYSTAL) == 5,
                "Air crystal base registration must retain CRYSTAL 5");

        AspectList result = ThaumcraftCraftingManager.getObjectTags(crystal);

        helper.assertTrue(result.getAmount(Aspect.AIR) == 1,
                "Contained AIR 1 must override the registered AIR 5");
        helper.assertTrue(result.getAmount(Aspect.CRYSTAL) == 0,
                "Registered CRYSTAL must not supplement contained aspects");
        helper.assertTrue(registered.getAmount(Aspect.AIR) == 5,
                "Lookup must not mutate the registered AIR amount");
        helper.assertTrue(registered.getAmount(Aspect.CRYSTAL) == 5,
                "Lookup must not mutate the registered CRYSTAL amount");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void filledStacksOfOneContainerItemRemainIndependent(GameTestHelper helper) {
        ItemStack air = new ItemStack(TestItems.NBT_CONTAINER);
        ItemStack fire = new ItemStack(TestItems.NBT_CONTAINER);
        ((IEssentiaContainerItem) air.getItem()).setAspects(air, new AspectList().add(Aspect.AIR, 2));
        ((IEssentiaContainerItem) fire.getItem()).setAspects(fire, new AspectList().add(Aspect.FIRE, 3));
        fire.enchant(Enchantments.UNBREAKING, 1);
        CompoundTag airBefore = air.getTag().copy();
        CompoundTag fireBefore = fire.getTag().copy();

        AspectList airResult = ThaumcraftCraftingManager.getObjectTags(air);
        AspectList fireResult = ThaumcraftCraftingManager.getObjectTags(fire);

        helper.assertTrue(airResult.getAmount(Aspect.AIR) == 2,
                "First stack must resolve its own AIR contents");
        helper.assertTrue(airResult.getAmount(Aspect.FIRE) == 0,
                "First stack must not inherit the second stack contents");
        helper.assertTrue(fireResult.getAmount(Aspect.FIRE) == 3,
                "Second stack must resolve its own FIRE contents");
        helper.assertTrue(fireResult.getAmount(Aspect.AIR) == 0,
                "Second stack must not inherit the first stack contents");
        helper.assertTrue(fireResult.getAmount(Aspect.MAGIC) == 3,
                "Existing enchantment bonuses must remain additive");
        helper.assertTrue(airBefore.equals(air.getTag()), "Lookup must not mutate first-stack NBT");
        helper.assertTrue(fireBefore.equals(fire.getTag()), "Lookup must not mutate second-stack NBT");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nullAndEmptyContentsDoNotFallBackToGeneratedTags(GameTestHelper helper) {
        ItemStack nullContents = new ItemStack(TestItems.NULL_CONTAINER);
        ItemStack emptyContents = new ItemStack(TestItems.EMPTY_CONTAINER);

        AspectList nullResult = ThaumcraftCraftingManager.getObjectTags(nullContents);
        AspectList emptyResult = ThaumcraftCraftingManager.getObjectTags(emptyContents);

        helper.assertTrue(nullResult.size() == 0,
                "Null authoritative contents must not receive generated fallback tags");
        helper.assertTrue(emptyResult.size() == 0,
                "Empty authoritative contents must not receive generated fallback tags");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void optOutContainerRetainsRegisteredBaseTags(GameTestHelper helper) {
        AspectList registered = AspectHelper.getRegisteredObjectAspects(
                new ItemStack(TestItems.OPT_OUT_CONTAINER));
        helper.assertTrue(registered != null, "Opt-out fixture must have registered base tags");

        AspectList result = ThaumcraftCraftingManager.getObjectTags(
                new ItemStack(TestItems.OPT_OUT_CONTAINER));

        helper.assertTrue(result.getAmount(Aspect.EARTH) == 7,
                "Opt-out container must retain its registered EARTH base tag");
        helper.assertTrue(result.getAmount(Aspect.WATER) == 0,
                "Opt-out container contents must remain excluded");
        helper.assertTrue(registered.getAmount(Aspect.EARTH) == 7,
                "Opt-out lookup must not mutate registered base tags");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lookupFiltersAndCapsWithoutMutatingContainerList(GameTestHelper helper) {
        AspectList shared = SharedListContainerItem.SHARED;
        AspectList result = ThaumcraftCraftingManager.getObjectTags(
                new ItemStack(TestItems.SHARED_LIST_CONTAINER));

        helper.assertTrue(result.size() == 1, "Only positive contained entries may remain");
        helper.assertTrue(result.getAmount(Aspect.AIR) == 500,
                "Positive contained entries must retain the existing cap of 500");
        helper.assertTrue(result.getAmount(Aspect.WATER) == 0,
                "Zero contained entries must be excluded");
        helper.assertTrue(result.getAmount(Aspect.FIRE) == 0,
                "Negative contained entries must be excluded");
        helper.assertTrue(shared.size() == 3,
                "Normalization must not remove entries from a container-owned list");
        helper.assertTrue(shared.getAmount(Aspect.AIR) == 600,
                "Capping must not change a container-owned amount");
        helper.assertTrue(shared.getAmount(Aspect.WATER) == 0,
                "Zero entry must remain untouched in the container-owned list");
        helper.assertTrue(shared.getAmount(Aspect.FIRE) == -2,
                "Negative entry must remain untouched in the container-owned list");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void publicLookupUsesComputedCrystalContents(GameTestHelper helper) {
        ItemStack crystal = new ItemStack(ModItems.VIS_CRYSTAL_AIR.get());

        AspectList result = AspectHelper.getObjectAspects(crystal);

        helper.assertTrue(result.getAmount(Aspect.AIR) == 1,
                "Public lookup must use contained AIR 1");
        helper.assertTrue(result.getAmount(Aspect.CRYSTAL) == 0,
                "Base CRYSTAL must not leak through public lookup");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void stackConstructorSnapshotsComputedAspects(GameTestHelper helper) {
        AspectList snapshot = new AspectList(new ItemStack(ModItems.VIS_CRYSTAL_AIR.get()));

        helper.assertTrue(snapshot.getAmount(Aspect.AIR) == 1,
                "Stack constructor must snapshot computed AIR 1");
        helper.assertTrue(snapshot.getAmount(Aspect.CRYSTAL) == 0,
                "Stack constructor must exclude the registered CRYSTAL base");
        snapshot.add(Aspect.AIR, 99);
        helper.assertTrue(AspectHelper.getObjectAspects(new ItemStack(ModItems.VIS_CRYSTAL_AIR.get()))
                        .getAmount(Aspect.AIR) == 1,
                "Snapshot edits must not mutate future lookups");
        AspectList sharedAfterMutation = ThaumcraftCraftingManager.getObjectTags(
                new ItemStack(ModItems.VIS_CRYSTAL_AIR.get()));
        helper.assertTrue(sharedAfterMutation.getAmount(Aspect.AIR) == 1,
                "Snapshot edits must not mutate subsequent shared lookup AIR 1");
        helper.assertTrue(sharedAfterMutation.getAmount(Aspect.CRYSTAL) == 0,
                "Snapshot edits must not restore base CRYSTAL in shared lookup");
        AspectList rawAfterMutation = AspectHelper.getRegisteredObjectAspects(
                new ItemStack(ModItems.VIS_CRYSTAL_AIR.get()));
        helper.assertTrue(rawAfterMutation.getAmount(Aspect.AIR) == 5,
                "Snapshot edits must not mutate the registered AIR 5 seed");
        helper.assertTrue(rawAfterMutation.getAmount(Aspect.CRYSTAL) == 5,
                "Snapshot edits must not mutate the registered CRYSTAL 5 seed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void genericScanUsesComputedContainerContents(GameTestHelper helper) {
        ItemStack empty = new ItemStack(TestItems.NBT_CONTAINER);
        ItemStack filled = new ItemStack(TestItems.NBT_CONTAINER);
        ((IEssentiaContainerItem) filled.getItem()).setAspects(
                filled, new AspectList().add(Aspect.AIR, 2));
        ScanGeneric scanner = new ScanGeneric();

        helper.assertTrue(!scanner.checkThing(null, empty),
                "Registered container with empty queried contents must not be scannable");
        helper.assertTrue(scanner.checkThing(null, filled),
                "Registered container with filled queried contents must be scannable");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void publicAndSharedLookupsReturnExactComputedValues(GameTestHelper helper) {
        ItemStack enchanted = new ItemStack(TestItems.NBT_CONTAINER, 2);
        ((IEssentiaContainerItem) enchanted.getItem()).setAspects(
                enchanted, new AspectList().add(Aspect.FIRE, 3));
        enchanted.enchant(Enchantments.UNBREAKING, 1);

        AspectList publicResult = AspectHelper.getObjectAspects(enchanted);
        AspectList sharedResult = ThaumcraftCraftingManager.getObjectTags(enchanted);
        AspectList cappedPublic = AspectHelper.getObjectAspects(
                new ItemStack(TestItems.SHARED_LIST_CONTAINER));

        helper.assertTrue(publicResult.getAmount(Aspect.FIRE) == 3,
                "Public lookup must retain contained FIRE 3");
        helper.assertTrue(publicResult.getAmount(Aspect.MAGIC) == 3,
                "Public lookup must add the enchantment MAGIC 3 bonus");
        helper.assertTrue(publicResult.getAmount(Aspect.ORDER) == 0,
                "Public lookup must not retain the container base ORDER 11");
        helper.assertTrue(sharedResult.getAmount(Aspect.FIRE) == 3,
                "Shared lookup must retain contained FIRE 3");
        helper.assertTrue(sharedResult.getAmount(Aspect.MAGIC) == 3,
                "Shared lookup must add the enchantment MAGIC 3 bonus");
        helper.assertTrue(sharedResult.getAmount(Aspect.ORDER) == 0,
                "Shared lookup must not retain the container base ORDER 11");
        helper.assertTrue(cappedPublic.size() == 1,
                "Public lookup must exclude zero and negative contained entries");
        helper.assertTrue(cappedPublic.getAmount(Aspect.AIR) == 500,
                "Public lookup must apply the existing positive-entry cap");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void publicLookupReturnsEmptyListsForMissingStacks(GameTestHelper helper) {
        AspectList nullResult = AspectHelper.getObjectAspects(null);
        AspectList emptyResult = AspectHelper.getObjectAspects(ItemStack.EMPTY);

        helper.assertTrue(nullResult != null && nullResult.size() == 0,
                "Public null lookup must return an empty computed list");
        helper.assertTrue(emptyResult != null && emptyResult.size() == 0,
                "Public empty-stack lookup must return an empty computed list");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void existsStillReflectsRawRegistrations(GameTestHelper helper) {
        ItemStack unregistered = new ItemStack(TestItems.UNREGISTERED);
        helper.assertTrue(ThaumcraftApi.exists(new ItemStack(ModItems.VIS_CRYSTAL_AIR.get())),
                "Registered crystal must exist in the raw aspect registry");
        helper.assertTrue(!ThaumcraftApi.exists(unregistered),
                "Dedicated fixture must begin unregistered");
        AspectList computed = AspectHelper.getObjectAspects(unregistered);
        helper.assertTrue(computed.size() == 1,
                "Unregistered fixture must receive exactly one generated fallback aspect");
        helper.assertTrue(computed.getAmount(Aspect.ENTROPY) == 1,
                "Unregistered fixture must compute generated ENTROPY 1");
        helper.assertTrue(!ThaumcraftApi.exists(unregistered),
                "Public computation must not make an unregistered fixture exist");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void complexRegistrationDoesNotPersistComputedBonuses(GameTestHelper helper) {
        ItemStack absent = new ItemStack(TestItems.COMPLEX_ABSENT);
        absent.enchant(Enchantments.UNBREAKING, 1);
        ItemStack present = new ItemStack(TestItems.COMPLEX_PRESENT);
        present.enchant(Enchantments.UNBREAKING, 1);

        ThaumcraftApi.registerComplexObjectTag(absent, new AspectList().add(Aspect.AIR, 2));
        ThaumcraftApi.registerComplexObjectTag(present, new AspectList().add(Aspect.WATER, 2));

        AspectList absentRaw = AspectHelper.getRegisteredObjectAspects(absent);
        AspectList presentRaw = AspectHelper.getRegisteredObjectAspects(present);
        helper.assertTrue(absentRaw.getAmount(Aspect.AIR) == 2,
                "Absent complex registration must retain explicit AIR 2");
        helper.assertTrue(absentRaw.getAmount(Aspect.ENTROPY) == 0,
                "Absent complex registration must not persist generated fallback aspects");
        helper.assertTrue(absentRaw.getAmount(Aspect.MAGIC) == 0,
                "Absent complex registration must not persist enchantment bonuses");
        helper.assertTrue(presentRaw.getAmount(Aspect.EARTH) == 4,
                "Present complex registration must retain raw EARTH 4");
        helper.assertTrue(presentRaw.getAmount(Aspect.WATER) == 2,
                "Present complex registration must merge explicit WATER 2");
        helper.assertTrue(presentRaw.getAmount(Aspect.MAGIC) == 0,
                "Present complex registration must not persist enchantment bonuses");
        helper.assertTrue(AspectHelper.getObjectAspects(absent).getAmount(Aspect.MAGIC) == 3,
                "Absent fixture lookup must still compute MAGIC 3");
        helper.assertTrue(AspectHelper.getObjectAspects(present).getAmount(Aspect.MAGIC) == 3,
                "Present fixture lookup must still compute MAGIC 3");
        helper.succeed();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class TestItems {
        static Item NBT_CONTAINER;
        static Item NULL_CONTAINER;
        static Item EMPTY_CONTAINER;
        static Item OPT_OUT_CONTAINER;
        static Item SHARED_LIST_CONTAINER;
        static Item UNREGISTERED;
        static Item COMPLEX_ABSENT;
        static Item COMPLEX_PRESENT;

        private TestItems() {
        }

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                NBT_CONTAINER = new NbtContainerItem(false);
                NULL_CONTAINER = new NullContainerItem();
                EMPTY_CONTAINER = new EmptyContainerItem();
                OPT_OUT_CONTAINER = new OptOutContainerItem();
                SHARED_LIST_CONTAINER = new SharedListContainerItem();
                UNREGISTERED = new Item(new Item.Properties());
                COMPLEX_ABSENT = new Item(new Item.Properties());
                COMPLEX_PRESENT = new Item(new Item.Properties());
                helper.register("fnd04_nbt_container", NBT_CONTAINER);
                helper.register("fnd04_null_container", NULL_CONTAINER);
                helper.register("fnd04_empty_container", EMPTY_CONTAINER);
                helper.register("fnd04_opt_out_container", OPT_OUT_CONTAINER);
                helper.register("fnd04_shared_list_container", SHARED_LIST_CONTAINER);
                helper.register("fnd04_unregistered", UNREGISTERED);
                helper.register("fnd04_complex_absent", COMPLEX_ABSENT);
                helper.register("fnd04_complex_present", COMPLEX_PRESENT);

                ThaumcraftApi.registerObjectTag(new ItemStack(NBT_CONTAINER),
                        new AspectList().add(Aspect.ORDER, 11));
                ThaumcraftApi.registerObjectTag(new ItemStack(OPT_OUT_CONTAINER),
                        new AspectList().add(Aspect.EARTH, 7));
                ThaumcraftApi.registerObjectTag(new ItemStack(SHARED_LIST_CONTAINER),
                        new AspectList().add(Aspect.CRYSTAL, 9));
                ThaumcraftApi.registerObjectTag(new ItemStack(COMPLEX_PRESENT),
                        new AspectList().add(Aspect.EARTH, 4));
            });
        }
    }

    private static class NbtContainerItem extends Item implements IEssentiaContainerItem {
        private NbtContainerItem(boolean unused) {
            super(new Item.Properties());
        }

        @Override
        public AspectList getAspects(ItemStack stack) {
            if (!stack.hasTag()) {
                return null;
            }
            AspectList result = new AspectList();
            result.readFromNBT(stack.getTag());
            return result.size() == 0 ? null : result;
        }

        @Override
        public void setAspects(ItemStack stack, AspectList aspects) {
            aspects.writeToNBT(stack.getOrCreateTag());
        }

        @Override
        public boolean ignoreContainedAspects() {
            return false;
        }
    }

    private static final class NullContainerItem extends NbtContainerItem {
        private NullContainerItem() {
            super(false);
        }

        @Override
        public AspectList getAspects(ItemStack stack) {
            return null;
        }
    }

    private static final class EmptyContainerItem extends NbtContainerItem {
        private EmptyContainerItem() {
            super(false);
        }

        @Override
        public AspectList getAspects(ItemStack stack) {
            return new AspectList();
        }
    }

    private static final class OptOutContainerItem extends NbtContainerItem {
        private OptOutContainerItem() {
            super(false);
        }

        @Override
        public AspectList getAspects(ItemStack stack) {
            return new AspectList().add(Aspect.WATER, 13);
        }

        @Override
        public boolean ignoreContainedAspects() {
            return true;
        }
    }

    private static final class SharedListContainerItem extends NbtContainerItem {
        private static final AspectList SHARED = new AspectList()
                .add(Aspect.AIR, 600)
                .add(Aspect.WATER, 0)
                .add(Aspect.FIRE, -2);

        private SharedListContainerItem() {
            super(false);
        }

        @Override
        public AspectList getAspects(ItemStack stack) {
            return SHARED;
        }
    }
}
