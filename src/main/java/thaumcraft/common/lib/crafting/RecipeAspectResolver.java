package thaumcraft.common.lib.crafting;

import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.*;

/** One top-level query owns its source snapshot, output index, cumulative history and memo. */
final class RecipeAspectResolver {
    private final Map<Item, List<Candidate>> byOutput = new IdentityHashMap<>();
    private final Set<CompoundTag> history = new HashSet<>();
    private final Map<CompoundTag, AspectList> memo = new HashMap<>();

    private final RecipeAspectSource source = RecipeAspectSource.current();
    private boolean indexed;

    private void indexRecipes() {
        if (indexed) return;
        indexed = true;
        RegistryAccess registries = source == null ? RegistryAccess.EMPTY : source.registries();
        Map<ResourceLocation, Object> recipes = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        recipes.putAll(ThaumcraftApi.getCraftingRecipes());
        if (source != null) for (Recipe<?> recipe : source.recipes().getRecipes()) recipes.put(recipe.getId(), recipe);
        for (Object recipe : recipes.values()) {
            try {
                Candidate candidate = adapt(recipe, registries);
                if (candidate != null && !candidate.output.isEmpty() && candidate.output.getCount() > 0)
                    byOutput.computeIfAbsent(candidate.output.getItem(), unused -> new ArrayList<>()).add(candidate);
            } catch (RuntimeException malformedRecipe) {
                // Dynamic/invalid third-party outputs cannot be attributed without a crafting context.
            }
        }
    }

    AspectList generate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new AspectList();
        // Explicit registrations take precedence at the direct generation boundary too.
        AspectList registered = AspectHelper.getRegisteredObjectAspects(stack);
        if (registered != null) return RecipeAspectMath.cap(registered);
        ItemStack normalized = stack.copy();
        normalized.setCount(1);
        if (normalized.isDamageableItem()) normalized.setDamageValue(0);
        CompoundTag key = normalized.save(new CompoundTag());
        AspectList completed = memo.get(key);
        if (completed != null) return completed.copy();
        if (!history.add(key) || history.size() >= 100) return new AspectList();
        indexRecipes();
        AspectList result = resolve(stack.getItem());
        result = RecipeAspectMath.cap(result);
        memo.put(key, result.copy());
        return result;
    }

    private AspectList resolve(Item item) {
        List<Candidate> candidates = byOutput.getOrDefault(item, List.of());
        for (int category = 0; category < 2; category++) {
            for (Candidate candidate : candidates) {
                if (candidate.category == category) {
                    AspectList result = evaluate(candidate);
                    if (result != null) return result; // A valid empty higher-priority recipe still wins.
                }
            }
        }
        AspectList best = new AspectList();
        int smallest = Integer.MAX_VALUE;
        for (Candidate candidate : candidates) {
            if (candidate.category != 2) continue;
            AspectList result = evaluate(candidate);
            if (result == null) continue;
            int total = result.visSize();
            if (total > 0 && total < smallest) { best = result; smallest = total; }
        }
        return best;
    }

    private AspectList evaluate(Candidate candidate) {
        try {
            int count = candidate.output.getCount();
            AspectList ingredients = new AspectList();
            Grid grid = new Grid();
            if (candidate.category == 2 && candidate.ingredients.size() > 9) return null;
            int slot = 0;
            for (Ingredient ingredient : candidate.ingredients) {
                if (ingredient == null) return null;
                ItemStack[] choices = ingredient.getItems();
                if (candidate.category == 0 && choices.length == 0) return null;
                if (choices.length > 0) {
                    ItemStack first = choices[0].copy();
                    first.setCount(1);
                    ingredients.add(ThaumcraftCraftingManager.getObjectTags(first, this));
                    if (slot < 9) grid.setItem(slot, first.copy());
                }
                slot++;
            }
            if (candidate.category == 0) return RecipeAspectMath.essentia(ingredients, candidate.required, count);
            if (candidate.recipe instanceof IArcaneRecipe arcane) {
                for (ItemStack remainder : arcane.getRemainingItems(grid))
                    if (!remainder.isEmpty()) ingredients = RecipeAspectMath.subtract(ingredients,
                            ThaumcraftCraftingManager.getObjectTags(remainder.copy(), this));
            } else if (candidate.recipe instanceof CraftingRecipe crafting) {
                for (ItemStack remainder : crafting.getRemainingItems(grid))
                    if (!remainder.isEmpty()) ingredients = RecipeAspectMath.subtract(ingredients,
                            ThaumcraftCraftingManager.getObjectTags(remainder.copy(), this));
            }
            AspectList result = RecipeAspectMath.normalize(ingredients, count);
            if (candidate.category == 1) return RecipeAspectMath.essentia(result, candidate.required, count);
            return candidate.recipe instanceof IArcaneRecipe arcane
                    ? RecipeAspectMath.arcane(result, arcane.getVis(), count) : result;
        } catch (RuntimeException malformedRecipe) {
            return null;
        }
    }

    private static Candidate adapt(Object recipe, RegistryAccess registries) {
        if (recipe instanceof CrucibleRecipeType crucible)
            return new Candidate(0, crucible.getResultItem(registries), List.of(crucible.getCatalyst()), crucible.getAspects(), recipe);
        if (recipe instanceof CrucibleRecipe crucible)
            return new Candidate(0, crucible.getRecipeOutput(), List.of(crucible.getCatalyst()), crucible.getAspects(), recipe);
        if (recipe instanceof InfusionRecipeType infusion)
            return new Candidate(1, infusion.getResultItem(registries), infusion.getIngredients(), infusion.getAspects(), recipe);
        if (recipe instanceof InfusionRecipe infusion && infusion.getRecipeOutput() instanceof ItemStack output) {
            List<Ingredient> inputs = new ArrayList<>();
            inputs.add(infusion.getRecipeInput()); inputs.addAll(infusion.getComponents());
            return new Candidate(1, output, inputs, infusion.getAspects(), recipe);
        }
        if (recipe instanceof Recipe<?> crafting && (recipe instanceof CraftingRecipe || recipe instanceof IArcaneRecipe))
            return new Candidate(2, crafting.getResultItem(registries), crafting.getIngredients(), new AspectList(), recipe);
        return null;
    }

    private record Candidate(int category, ItemStack output, List<Ingredient> ingredients,
                             AspectList required, Object recipe) {}

    /** Private copied first alternatives, including empty slots, for real remainder callbacks. */
    private static final class Grid extends SimpleContainer implements CraftingContainer, IArcaneWorkbench {
        Grid() { super(9); }
        @Override public int getWidth() { return 3; }
        @Override public int getHeight() { return 3; }
        @Override public List<ItemStack> getItems() {
            List<ItemStack> result = new ArrayList<>();
            for (int i = 0; i < 9; i++) result.add(getItem(i));
            return result;
        }
        @Override public void fillStackedContents(StackedContents contents) {
            for (int i = 0; i < 9; i++) contents.accountStack(getItem(i));
        }
    }
}
