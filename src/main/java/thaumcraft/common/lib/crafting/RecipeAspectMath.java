package thaumcraft.common.lib.crafting;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/** BETA26 arithmetic, with copied ownership and invalid-count guards. */
final class RecipeAspectMath {
    private RecipeAspectMath() {}

    static AspectList subtract(AspectList source, AspectList remainder) {
        AspectList result = source.copy();
        for (Aspect aspect : remainder.getAspects()) {
            int amount = remainder.getAmount(aspect);
            if (amount > 0) result.reduce(aspect, amount);
        }
        return result;
    }

    static AspectList normalize(AspectList source, int count) {
        AspectList result = new AspectList();
        if (count <= 0) return result;
        for (Aspect aspect : source.getAspects()) {
            float value = source.getAmount(aspect) * .75f / count;
            if (value > .75f && value < 1) value = 1;
            if (value >= 1) result.add(aspect, (int) value);
        }
        return result;
    }

    static AspectList essentia(AspectList source, AspectList required, int count) {
        if (count <= 0) return new AspectList();
        AspectList result = source.copy();
        for (Aspect aspect : required.getAspects()) {
            int amount = required.getAmount(aspect);
            if (amount > 0) add(result, aspect, (int)(Math.sqrt(amount) / count));
        }
        return result;
    }

    static AspectList arcane(AspectList source, int vis, int count) {
        if (count <= 0) return new AspectList();
        AspectList result = source.copy();
        if (vis > 0) add(result, Aspect.MAGIC, (int)(Math.sqrt(1 + vis / 2) / count));
        return result;
    }

    private static void add(AspectList result, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return;
        }
        // In-place update keeps the existing LinkedHashMap slot; culling tie order depends on it.
        long total = (long) result.getAmount(aspect) + amount;
        result.aspects.put(aspect, (int)Math.min(Integer.MAX_VALUE, total));
    }

    static AspectList cap(AspectList source) {
        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            int amount = source.getAmount(aspect);
            if (amount > 0) result.add(aspect, Math.min(ThaumcraftCraftingManager.ASPECT_CAP, amount));
        }
        return result;
    }
}
