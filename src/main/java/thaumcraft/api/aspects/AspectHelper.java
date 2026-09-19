package thaumcraft.api.aspects;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.ThaumcraftApi;

/**
 * AspectHelper - Utility class for working with aspects.
 * Handles aspect lookups for items, blocks, and entities.
 * 
 * @author Azanor
 * Ported to 1.20.1
 */
public class AspectHelper {
    
    /**
     * Registry of aspects for items/blocks
     * Key is the ResourceLocation string (e.g., "minecraft:stone")
     */
    private static Map<String, AspectList> objectTags = new HashMap<>();
    
    /**
     * Registry of aspects for entities
     * Key is the entity type ResourceLocation string
     */
    private static Map<String, AspectList> entityTags = new HashMap<>();

    /**
     * Returns a fresh aspect list containing at most seven weighted aspect types.
     */
    public static AspectList cullTags(AspectList source) {
        return cullTags(source, 7);
    }

    /**
     * Returns a fresh aspect list containing at most {@code cap} weighted aspect types.
     * When weights tie, the first remaining entry in insertion order is removed.
     */
    public static AspectList cullTags(AspectList source, int cap) {
        if (cap < 0) {
            throw new IllegalArgumentException("Aspect cap must be nonnegative");
        }

        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            if (aspect != null) {
                result.add(aspect, source.getAmount(aspect));
            }
        }

        while (result.size() > cap) {
            Aspect lowest = null;
            float lowestWeight = 0.0f;
            for (Aspect aspect : result.getAspects()) {
                float weight = cullingWeight(aspect, result.getAmount(aspect));
                if (lowest == null || weight < lowestWeight) {
                    lowest = aspect;
                    lowestWeight = weight;
                }
            }
            result.remove(lowest);
        }
        return result;
    }

    private static float cullingWeight(Aspect aspect, int amount) {
        float weight = amount;
        if (aspect.isPrimal()) {
            return weight * 0.9f;
        }

        for (Aspect parent : aspect.getComponents()) {
            if (!parent.isPrimal()) {
                weight *= 1.1f;
                for (Aspect grandparent : parent.getComponents()) {
                    if (!grandparent.isPrimal()) {
                        weight *= 1.05f;
                    }
                }
            }
        }
        return weight;
    }
    
    /**
     * Gets the computed aspects for an item stack through Thaumcraft's internal handler.
     * This includes contained aspects, generated aspects, bonuses, and normalization applied
     * by the shared crafting lookup.
     *
     * @param stack the item to query
     * @return the computed aspects for this stack, never null after common setup
     */
    public static AspectList getObjectAspects(ItemStack stack) {
        return ThaumcraftApi.internalMethods.getObjectAspects(stack);
    }

    /**
     * Gets only the aspect list directly registered for an item's registry id.
     * No contained aspects, generated fallback, item-property bonuses, or caps are applied.
     * The returned value is the registry-owned list and may be {@code null} for null, empty,
     * unregistered, or otherwise unresolved stacks.
     *
     * @param stack the item to query
     * @return the directly registered aspects, or null if none
     */
    public static AspectList getRegisteredObjectAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }
        
        // Check for specific item with NBT/damage
        // For now, just use the base item
        String key = itemId.toString();
        
        return objectTags.get(key);
    }
    
    /**
     * Register aspects for an item
     * @param stack the item to register
     * @param aspects the aspects to associate
     */
    public static void registerObjectTag(ItemStack stack, AspectList aspects) {
        if (stack == null || stack.isEmpty() || aspects == null) {
            return;
        }
        
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null) {
            objectTags.put(itemId.toString(), aspects);
        }
    }
    
    /**
     * Register aspects for an item by ResourceLocation
     * @param itemId the item's resource location
     * @param aspects the aspects to associate
     */
    public static void registerObjectTag(ResourceLocation itemId, AspectList aspects) {
        if (itemId != null && aspects != null) {
            objectTags.put(itemId.toString(), aspects);
        }
    }
    
    /**
     * Register aspects for an entity type
     * @param entityId the entity type's resource location
     * @param aspects the aspects to associate
     */
    public static void registerEntityTag(ResourceLocation entityId, AspectList aspects) {
        if (entityId != null && aspects != null) {
            entityTags.put(entityId.toString(), aspects);
        }
    }
    
    /**
     * Get the aspects associated with an entity type
     * @param entityId the entity type resource location
     * @return the aspects for this entity, or null if none
     */
    public static AspectList getEntityAspects(ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return entityTags.get(entityId.toString());
    }
    
    /**
     * Get the aspects associated with an entity instance
     * @param entity the entity to query
     * @return the aspects for this entity, or null if none
     */
    public static AspectList getEntityAspects(Entity entity) {
        if (entity == null) {
            return null;
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return getEntityAspects(entityId);
    }
    
    /**
     * Combine two primal aspects to get a compound aspect
     * @param aspect1 first aspect
     * @param aspect2 second aspect
     * @return the resulting compound aspect, or null if combination doesn't exist
     */
    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        if (aspect1 == null || aspect2 == null) {
            return null;
        }
        
        // Check both orderings
        int hash1 = (aspect1.getTag() + aspect2.getTag()).hashCode();
        int hash2 = (aspect2.getTag() + aspect1.getTag()).hashCode();
        
        Aspect result = Aspect.mixList.get(hash1);
        if (result == null) {
            result = Aspect.mixList.get(hash2);
        }
        
        return result;
    }
    
    /**
     * Check if two aspects can be combined
     * @param aspect1 first aspect
     * @param aspect2 second aspect
     * @return true if these aspects can form a compound
     */
    public static boolean canCombine(Aspect aspect1, Aspect aspect2) {
        return getCombinationResult(aspect1, aspect2) != null;
    }
    
    /**
     * Clear all registered aspect tags
     * Used for reloading
     */
    public static void clearTags() {
        objectTags.clear();
        entityTags.clear();
    }
    
    /**
     * Get the number of registered object tags
     */
    public static int getObjectTagCount() {
        return objectTags.size();
    }
    
    /**
     * Get the number of registered entity tags
     */
    public static int getEntityTagCount() {
        return entityTags.size();
    }
}
