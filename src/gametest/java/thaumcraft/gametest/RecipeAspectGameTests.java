package thaumcraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import thaumcraft.api.aspects.*;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class RecipeAspectGameTests {
    @GameTest(template = "fnd01_empty")
    public static void loadedRecipeDerivesEarthThree(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(new ResourceLocation("thaumcraft", "fnd04_recipe_basic")).orElseThrow();
        var output = recipe.getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(output.is(AspectLookupGameTests.TestItems.RECIPE_OUTPUT) && output.getCount() == 1, "Loaded recipe exact output/count");
        helper.assertTrue(recipe.getIngredients().size() == 1 && recipe.getIngredients().get(0).getItems()[0].is(AspectLookupGameTests.TestItems.RECIPE_SEED), "Loaded recipe exact ingredient");
        var result = AspectHelper.getObjectAspects(output);
        helper.assertTrue(result.size() == 1 && result.getAmount(Aspect.EARTH) == 3, "Loaded recipe must derive exactly EARTH 3, got " + result);
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void noRecipeIsEmpty(GameTestHelper helper) {
        ItemStack output = new ItemStack(AspectLookupGameTests.TestItems.RECIPE_NONE);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(helper.getLevel().registryAccess()).is(output.getItem())), "No recipe fixture must have no loaded recipe");
        var result = AspectHelper.getObjectAspects(output);
        helper.assertTrue(result.size() == 0, "No recipe must derive exactly empty, got " + result);
        helper.succeed();
    }

    private static ItemStack stack(String name) {
        var item = AspectLookupGameTests.TestItems.RECIPE_ITEMS.get(name);
        if (item == null) throw new IllegalArgumentException(name);
        return new ItemStack(item);
    }

    private static net.minecraft.world.item.crafting.Recipe<?> loaded(GameTestHelper helper, String name) {
        return helper.getLevel().getRecipeManager().byKey(new ResourceLocation("thaumcraft", "fnd04_recipe_" + name)).orElseThrow();
    }

    private static void exact(GameTestHelper helper, ItemStack stack, AspectList expected) {
        AspectList actual = AspectHelper.getObjectAspects(stack);
        helper.assertTrue(actual.size() == expected.size(), "Expected " + expected + " got " + actual + " for " + stack);
        for (Aspect aspect : expected.getAspects()) helper.assertTrue(actual.getAmount(aspect) == expected.getAmount(aspect), "Expected " + expected + " got " + actual);
    }

    private static void derived(GameTestHelper helper, String name, AspectList expected) {
        ItemStack output = stack(name);
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(output) == null, "Derived fixture must be unseeded: " + name);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipes().stream().anyMatch(r -> r.getResultItem(helper.getLevel().registryAccess()).is(output.getItem())), "Loaded output recipe required: " + name);
        exact(helper, output, expected);
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(output) == null, "Derivation must not register output: " + name);
    }

    @GameTest(template = "fnd01_empty")
    public static void shapedDivisionAndStrictRounding(GameTestHelper helper) {
        helper.assertTrue(loaded(helper,"shaped") instanceof net.minecraft.world.item.crafting.ShapedRecipe, "Actual shaped recipe required");
        derived(helper,"shaped",new AspectList().add(Aspect.EARTH,6));
        derived(helper,"divided",new AspectList().add(Aspect.EARTH,1));
        derived(helper,"promoted",new AspectList().add(Aspect.EARTH,1));
        derived(helper,"zero",new AspectList());
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void firstAlternativeIsNotMinimum(GameTestHelper helper) {
        var options = loaded(helper,"alternative").getIngredients().get(0).getItems();
        helper.assertTrue(options.length == 2 && options[0].is(AspectLookupGameTests.TestItems.RECIPE_SEED), "First alternative is EARTH4, second EARTH1 (which would truncate to zero)");
        derived(helper,"alternative",new AspectList().add(Aspect.EARTH,3));
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void actualRemainderAndRefusedOverdraw(GameTestHelper helper) {
        var grid = new net.minecraft.world.inventory.TransientCraftingContainer(new net.minecraft.world.inventory.AbstractContainerMenu(null,0) {
            @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(net.minecraft.world.entity.player.Player p) { return true; }
        },3,3);
        grid.setItem(0,stack("metal8"));
        var recipe = (net.minecraft.world.item.crafting.CraftingRecipe)loaded(helper,"remainder");
        helper.assertTrue(recipe.getRemainingItems(grid).get(0).is(stack("metal3").getItem()), "Actual recipe returns METAL3 container");
        derived(helper,"remainder",new AspectList().add(Aspect.METAL,1));
        derived(helper,"short_remainder",new AspectList().add(Aspect.METAL,1));
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void arcaneVisAndCrystals(GameTestHelper helper) {
        var recipe = (thaumcraft.api.crafting.IArcaneRecipe) loaded(helper,"arcane");
        helper.assertTrue(recipe.getVis()==30 && recipe.getCrystals().getAmount(Aspect.AIR)==50, "Loaded Vis30 and unused AIR50 crystal requirements");
        derived(helper,"arcane",new AspectList().add(Aspect.EARTH,1).add(Aspect.MAGIC,2));
        derived(helper,"odd_arcane",new AspectList().add(Aspect.MAGIC,1));
        derived(helper,"arcane_remainder",new AspectList().add(Aspect.METAL,3).add(Aspect.MAGIC,1));
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void crucibleAndInfusionFormulasAndPriority(GameTestHelper helper) {
        helper.assertTrue(loaded(helper,"crucible") instanceof thaumcraft.common.lib.crafting.CrucibleRecipeType, "Loaded modern crucible");
        helper.assertTrue(loaded(helper,"infusion_infusion") instanceof thaumcraft.common.lib.crafting.InfusionRecipeType, "Loaded modern infusion");
        derived(helper,"crucible",new AspectList().add(Aspect.MAGIC,4));
        derived(helper,"infusion",new AspectList().add(Aspect.MAGIC,5));
        loaded(helper,"priority_crafting"); loaded(helper,"priority_infusion");
        derived(helper,"priority",new AspectList().add(Aspect.MAGIC,4));
        loaded(helper,"empty_priority_crafting");
        derived(helper,"empty_priority",new AspectList());
        loaded(helper,"infusion_priority_crafting");
        derived(helper,"infusion_priority",new AspectList().add(Aspect.MAGIC,5));
        loaded(helper,"empty_infusion_crafting");
        derived(helper,"empty_infusion",new AspectList());
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void smallestPositiveAndFirstTie(GameTestHelper helper) {
        loaded(helper,"minimum_a"); loaded(helper,"minimum_b"); loaded(helper,"minimum_c");
        derived(helper,"minimum",new AspectList().add(Aspect.EARTH,3));
        loaded(helper,"tie_a"); loaded(helper,"tie_b");
        derived(helper,"tie",new AspectList().add(Aspect.EARTH,3));
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void chainSiblingsAndAsymmetricCycle(GameTestHelper helper) {
        derived(helper,"middle",new AspectList().add(Aspect.EARTH,2));
        derived(helper,"siblings",new AspectList().add(Aspect.EARTH,3));
        derived(helper,"cycle_a",new AspectList().add(Aspect.EARTH,3));
        derived(helper,"cycle_b",new AspectList().add(Aspect.EARTH,2).add(Aspect.WATER,6));
        derived(helper,"cycle_siblings",new AspectList().add(Aspect.EARTH,2).add(Aspect.WATER,4));
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void countsCopiesExplicitAndLateSeeds(GameTestHelper helper) {
        ItemStack output = stack("siblings"); output.setCount(17);
        var tag = output.getOrCreateTag(); tag.putString("fixture","kept");
        exact(helper,output,new AspectList().add(Aspect.EARTH,3));
        AspectHelper.getObjectAspects(output).add(Aspect.FIRE,90);
        exact(helper,output,new AspectList().add(Aspect.EARTH,3));
        helper.assertTrue(output.getCount()==17 && "kept".equals(output.getTag().getString("fixture")), "Caller stack count and NBT unchanged");
        loaded(helper,"registered"); exact(helper,stack("registered"),new AspectList().add(Aspect.FIRE,11));
        derived(helper,"late_output",new AspectList().add(Aspect.EARTH,3));
        try {
            thaumcraft.api.ThaumcraftApi.registerObjectTag(stack("late_seed"),new AspectList().add(Aspect.WATER,8));
            derived(helper,"late_output",new AspectList().add(Aspect.WATER,6));
        } finally {
            thaumcraft.api.ThaumcraftApi.registerObjectTag(stack("late_seed"),new AspectList().add(Aspect.EARTH,4));
        }
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void legacyCatalogAndDepthCutoff(GameTestHelper helper) {
        var catalog = thaumcraft.api.ThaumcraftApi.getCraftingRecipes();
        java.util.List<ResourceLocation> owned = new java.util.ArrayList<>();
        try {
            for (int i=0;i<101;i++) {
                ResourceLocation id = new ResourceLocation("thaumcraft","fnd04_recipe_depth_"+i);
                helper.assertTrue(!catalog.containsKey(id), "Test owns new catalog ID"); owned.add(id);
                var input = i==100 ? new ItemStack(AspectLookupGameTests.TestItems.RECIPE_SEED) : stack("depth_"+(i+1));
                thaumcraft.api.ThaumcraftApi.addCrucibleRecipe(id,new thaumcraft.api.crafting.CrucibleRecipe("",stack("depth_"+i),input,new AspectList()));
            }
            helper.assertTrue(catalog.size() >= 101, "Depth recipe setup exists");
            exact(helper,stack("depth_0"),new AspectList());
            exact(helper,stack("depth_1"),new AspectList());
            exact(helper,stack("depth_2"),new AspectList().add(Aspect.EARTH,4));
            ResourceLocation duplicate = new ResourceLocation("thaumcraft","fnd04_recipe_basic");
            helper.assertTrue(!catalog.containsKey(duplicate), "Duplicate catalog ID test owned"); owned.add(duplicate);
            thaumcraft.api.ThaumcraftApi.addCrucibleRecipe(duplicate,new thaumcraft.api.crafting.CrucibleRecipe("",new ItemStack(AspectLookupGameTests.TestItems.RECIPE_OUTPUT),stack("water"),new AspectList()));
            exact(helper,new ItemStack(AspectLookupGameTests.TestItems.RECIPE_OUTPUT),new AspectList().add(Aspect.EARTH,3));
            ResourceLocation infusion = new ResourceLocation("thaumcraft","fnd04_recipe_legacy_infusion"); owned.add(infusion);
            thaumcraft.api.ThaumcraftApi.addInfusionCraftingRecipe(infusion,new thaumcraft.api.crafting.InfusionRecipe("",new ItemStack(stack("legacy_infusion").getItem(),2),0,new AspectList().add(Aspect.MAGIC,16),stack("magic3"),stack("magic5")));
            exact(helper,stack("legacy_infusion"),new AspectList().add(Aspect.MAGIC,5));
            ResourceLocation arcane = new ResourceLocation("thaumcraft","fnd04_recipe_legacy_arcane"); owned.add(arcane);
            var inputs = net.minecraft.core.NonNullList.<net.minecraft.world.item.crafting.Ingredient>create();
            inputs.add(net.minecraft.world.item.crafting.Ingredient.of(AspectLookupGameTests.TestItems.RECIPE_SEED));
            thaumcraft.api.ThaumcraftApi.addArcaneCraftingRecipe(arcane,new thaumcraft.common.lib.crafting.ShapelessArcaneRecipe(arcane,"",inputs,new ItemStack(stack("legacy_arcane").getItem(),2),30,new AspectList(),""));
            exact(helper,stack("legacy_arcane"),new AspectList().add(Aspect.EARTH,1).add(Aspect.MAGIC,2));
        } finally { owned.forEach(catalog::remove); }
        exact(helper,stack("depth_3"),new AspectList());
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty", timeoutTicks = 200)
    public static void vanillaPlanksAndRepeatedQueries(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(new ResourceLocation("minecraft","oak_planks")).orElseThrow();
        var output = recipe.getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(output.is(net.minecraft.world.item.Items.OAK_PLANKS) && output.getCount()==4, "Vanilla oak planks recipe produces four");
        var first = recipe.getIngredients().get(0).getItems()[0];
        var raw = AspectHelper.getRegisteredObjectAspects(first);
        helper.assertTrue(raw != null && raw.size()==1 && raw.getAmount(Aspect.PLANT)==20, "Current first oak-log ingredient has exactly PLANT20");
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(output)==null, "Oak planks are derived");
        exact(helper,output,new AspectList().add(Aspect.PLANT,3));
        long start = System.nanoTime();
        for (int i=0;i<100;i++) exact(helper,stack("siblings"),new AspectList().add(Aspect.EARTH,3));
        com.mojang.logging.LogUtils.getLogger().info("FND-04 100 repeated sibling queries took {} ms",(System.nanoTime()-start)/1_000_000.0);
        start = System.nanoTime();
        for (int i=0;i<1000;i++) exact(helper,new ItemStack(AspectLookupGameTests.TestItems.RECIPE_SEED),new AspectList().add(Aspect.EARTH,4));
        com.mojang.logging.LogUtils.getLogger().info("FND-04 1000 direct seed queries took {} ms",(System.nanoTime()-start)/1_000_000.0);

        helper.succeed();
    }


    @GameTest(template = "fnd01_empty")
    public static void recursiveIdentityRetainsNbtButNormalizesCountAndDamage(GameTestHelper helper) {
        var catalog = thaumcraft.api.ThaumcraftApi.getCraftingRecipes();
        java.util.List<ResourceLocation> owned = new java.util.ArrayList<>();
        try {
            for (String name : java.util.List.of("identity_nbt","identity_count","identity_damage")) {
                ItemStack recursive = stack(name);
                if (name.equals("identity_nbt")) recursive.getOrCreateTag().putString("identity","different");
                if (name.equals("identity_count")) recursive.setCount(30);
                if (name.equals("identity_damage")) recursive.setDamageValue(30);
                var inputs = net.minecraft.core.NonNullList.<net.minecraft.world.item.crafting.Ingredient>create();
                inputs.add(net.minecraftforge.common.crafting.StrictNBTIngredient.of(recursive));
                inputs.add(net.minecraft.world.item.crafting.Ingredient.of(AspectLookupGameTests.TestItems.RECIPE_SEED));
                ResourceLocation id = new ResourceLocation("thaumcraft","fnd04_recipe_"+name);
                helper.assertTrue(!catalog.containsKey(id), "Owned identity catalog ID"); owned.add(id);
                var recipe = new thaumcraft.common.lib.crafting.ShapelessArcaneRecipe(id,"",inputs,stack(name),0,new AspectList(),"");
                thaumcraft.api.ThaumcraftApi.addArcaneCraftingRecipe(id,recipe);
                helper.assertTrue(recipe.getIngredients().get(0).getItems()[0].getCount()==recursive.getCount(), "Identity fixture stack retained by actual ingredient");
                exact(helper,stack(name),new AspectList().add(Aspect.EARTH,name.equals("identity_nbt") ? 5 : 3));
                helper.assertTrue(recursive.getCount()==(name.equals("identity_count") ? 30 : 1), "Recipe ingredient not mutated");
                if (name.equals("identity_damage")) helper.assertTrue(recursive.getDamageValue()==30,"Damage normalization only affects copied history key");
            }
        } finally { owned.forEach(catalog::remove); }
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void missingCatalystFakeCatalogAndCap(GameTestHelper helper) {
        var catalog = thaumcraft.api.ThaumcraftApi.getCraftingRecipes();
        var fake = thaumcraft.api.ThaumcraftApi.getCraftingRecipesFake();
        ResourceLocation malformed = new ResourceLocation("thaumcraft","fnd04_recipe_malformed");
        ResourceLocation fallback = new ResourceLocation("thaumcraft","fnd04_recipe_malformed_fallback");
        ResourceLocation capped = new ResourceLocation("thaumcraft","fnd04_recipe_cap");
        helper.assertTrue(!catalog.containsKey(malformed) && !catalog.containsKey(fallback) && !catalog.containsKey(capped) && !fake.containsKey(malformed),"Owned catalog IDs");
        try {
            fake.put(malformed,new thaumcraft.api.crafting.CrucibleRecipe("",stack("legacy"),stack("water"),new AspectList().add(Aspect.FIRE,9)));
            exact(helper,stack("legacy"),new AspectList());
            thaumcraft.api.ThaumcraftApi.addCrucibleRecipe(malformed,new thaumcraft.api.crafting.CrucibleRecipe("",stack("legacy"),net.minecraft.world.item.crafting.Ingredient.EMPTY,new AspectList().add(Aspect.MAGIC,9)));
            var inputs = net.minecraft.core.NonNullList.<net.minecraft.world.item.crafting.Ingredient>create();
            inputs.add(net.minecraft.world.item.crafting.Ingredient.of(AspectLookupGameTests.TestItems.RECIPE_SEED));
            thaumcraft.api.ThaumcraftApi.addArcaneCraftingRecipe(fallback,new thaumcraft.common.lib.crafting.ShapelessArcaneRecipe(fallback,"",inputs,stack("legacy"),0,new AspectList(),""));
            exact(helper,stack("legacy"),new AspectList().add(Aspect.EARTH,3));
            thaumcraft.api.ThaumcraftApi.addCrucibleRecipe(capped,new thaumcraft.api.crafting.CrucibleRecipe("",stack("cap"),stack("water"),new AspectList().add(Aspect.MAGIC,1_000_000)));
            exact(helper,stack("cap"),new AspectList().add(Aspect.WATER,8).add(Aspect.MAGIC,500));
            AspectHelper.getObjectAspects(stack("cap")).add(Aspect.MAGIC,400);
            exact(helper,stack("cap"),new AspectList().add(Aspect.WATER,8).add(Aspect.MAGIC,500));
        } finally { catalog.remove(malformed); catalog.remove(fallback); catalog.remove(capped); fake.remove(malformed); }
        helper.succeed();
    }


    @GameTest(template = "fnd01_empty")
    public static void historyBudgetIsCumulativeAcrossCandidates(GameTestHelper helper) {
        var catalog = thaumcraft.api.ThaumcraftApi.getCraftingRecipes();
        java.util.List<ResourceLocation> owned = new java.util.ArrayList<>();
        try {
            for (int i=0;i<99;i++) {
                ResourceLocation id = new ResourceLocation("thaumcraft",String.format(java.util.Locale.ROOT,"fnd04_recipe_breadth_%03d",i));
                helper.assertTrue(!catalog.containsKey(id),"Owned breadth catalog ID"); owned.add(id);
                ItemStack input = i==98 ? new ItemStack(AspectLookupGameTests.TestItems.RECIPE_OUTPUT) : stack("depth_"+i);
                helper.assertTrue(AspectHelper.getRegisteredObjectAspects(input)==null,"Breadth input unseeded");
                var inputs = net.minecraft.core.NonNullList.<net.minecraft.world.item.crafting.Ingredient>create();
                inputs.add(net.minecraft.world.item.crafting.Ingredient.of(input));
                thaumcraft.api.ThaumcraftApi.addArcaneCraftingRecipe(id,new thaumcraft.common.lib.crafting.ShapelessArcaneRecipe(id,"",inputs,stack("breadth"),0,new AspectList(),""));
            }
            // Root + 98 completed empty inputs exhausts the first 99 entries. The valid
            // EARTH3 input is the 100th, and cannot be traversed in this same query.
            exact(helper,new ItemStack(AspectLookupGameTests.TestItems.RECIPE_OUTPUT),new AspectList().add(Aspect.EARTH,3));
            exact(helper,stack("breadth"),new AspectList());
        } finally { owned.forEach(catalog::remove); }
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void positiveAdditionKeepsOrderForCullingTie(GameTestHelper helper) {
        var crucible = (thaumcraft.common.lib.crafting.CrucibleRecipeType) loaded(helper,"cull_order");
        helper.assertTrue(crucible.getAspects().getAmount(Aspect.AIR)==1 && crucible.getAspects().getAmount(Aspect.LIFE)==100,"Cull fixture requires AIR 1 and LIFE 100");
        helper.assertTrue(crucible.getCatalyst().getItems().length==1 && crucible.getCatalyst().getItems()[0].is(stack("cull_catalyst").getItem()),"Cull fixture catalyst is the seven-type seed");
        ItemStack output = stack("cull_output");
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(output)==null,"Cull output must be unseeded");
        AspectList result = AspectHelper.getObjectAspects(output);
        helper.assertTrue(result.size()==7,"Seven survivors after the seven-type cull, got " + result);
        helper.assertTrue(result.getAmount(Aspect.WATER)==2,"WATER must survive as the first tied-lowest entry, got " + result);
        helper.assertTrue(!result.contains(Aspect.AIR),"AIR must be culled at its insertion slot, got " + result);
        helper.assertTrue(result.getAmount(Aspect.FIRE)==3 && result.getAmount(Aspect.EARTH)==3 && result.getAmount(Aspect.ORDER)==3 && result.getAmount(Aspect.ENTROPY)==3 && result.getAmount(Aspect.MAGIC)==3 && result.getAmount(Aspect.LIFE)==10,"Exact survivor amounts, got " + result);
        helper.succeed();
    }

    @GameTest(template = "fnd01_empty")
    public static void directGenerationHonorsRegistrationAndOwnsCopies(GameTestHelper helper) {
        loaded(helper,"registered");
        ItemStack seeded = stack("registered");
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(seeded).getAmount(Aspect.FIRE)==11,"Seeded fixture registration");
        AspectList direct = thaumcraft.common.lib.crafting.ThaumcraftCraftingManager.generateTags(seeded);
        helper.assertTrue(direct.size()==1 && direct.getAmount(Aspect.FIRE)==11,"Direct generation must honor the registered base, got " + direct);
        direct.add(Aspect.ENTROPY,40);
        AspectList again = thaumcraft.common.lib.crafting.ThaumcraftCraftingManager.generateTags(stack("registered"));
        helper.assertTrue(again.size()==1 && again.getAmount(Aspect.FIRE)==11,"Caller mutation must not leak into later results, got " + again);
        AspectList store = AspectHelper.getRegisteredObjectAspects(stack("registered"));
        helper.assertTrue(store.size()==1 && store.getAmount(Aspect.FIRE)==11,"Registration store must remain intact");
        ItemStack derivedOutput = new ItemStack(AspectLookupGameTests.TestItems.RECIPE_OUTPUT);
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(derivedOutput)==null,"Unseeded derivation control");
        AspectList derived = thaumcraft.common.lib.crafting.ThaumcraftCraftingManager.generateTags(derivedOutput);
        helper.assertTrue(derived.size()==1 && derived.getAmount(Aspect.EARTH)==3,"Unseeded direct generation still derives from recipes, got " + derived);
        helper.assertTrue(AspectHelper.getRegisteredObjectAspects(derivedOutput)==null,"Direct generation must not persist registrations");
        helper.succeed();
    }

    static void assertReloadRecipe(GameTestHelper helper, boolean replacement) {
        var recipe = loaded(helper,"reload_output");
        var output = recipe.getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(output.is(stack("reload_output").getItem()) && output.getCount()==1, "Reload recipe exact output");
        helper.assertTrue(recipe.getIngredients().size()==1 && recipe.getIngredients().get(0).getItems().length==1 && recipe.getIngredients().get(0).getItems()[0].is(replacement ? stack("water").getItem() : AspectLookupGameTests.TestItems.RECIPE_SEED), "Live RecipeManager reload ingredient");
        exact(helper,output,new AspectList().add(replacement ? Aspect.WATER : Aspect.EARTH,replacement ? 6 : 3));
        com.mojang.logging.LogUtils.getLogger().info("FND-04 live recipe reload verified: {}",replacement ? "WATER6" : "EARTH3");
    }
}
