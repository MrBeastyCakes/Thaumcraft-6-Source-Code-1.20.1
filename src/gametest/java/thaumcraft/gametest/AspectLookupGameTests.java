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
        AspectList registered = AspectHelper.getObjectAspects(crystal);
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
        AspectList registered = AspectHelper.getObjectAspects(new ItemStack(TestItems.OPT_OUT_CONTAINER));
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

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class TestItems {
        static Item NBT_CONTAINER;
        static Item NULL_CONTAINER;
        static Item EMPTY_CONTAINER;
        static Item OPT_OUT_CONTAINER;
        static Item SHARED_LIST_CONTAINER;

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
                helper.register("fnd04_nbt_container", NBT_CONTAINER);
                helper.register("fnd04_null_container", NULL_CONTAINER);
                helper.register("fnd04_empty_container", EMPTY_CONTAINER);
                helper.register("fnd04_opt_out_container", OPT_OUT_CONTAINER);
                helper.register("fnd04_shared_list_container", SHARED_LIST_CONTAINER);

                ThaumcraftApi.registerObjectTag(new ItemStack(NBT_CONTAINER),
                        new AspectList().add(Aspect.ORDER, 11));
                ThaumcraftApi.registerObjectTag(new ItemStack(OPT_OUT_CONTAINER),
                        new AspectList().add(Aspect.EARTH, 7));
                ThaumcraftApi.registerObjectTag(new ItemStack(SHARED_LIST_CONTAINER),
                        new AspectList().add(Aspect.CRYSTAL, 9));
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
