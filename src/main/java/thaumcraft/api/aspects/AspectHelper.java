package thaumcraft.api.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
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
    private static final AspectRegistrationStore OBJECT_TAGS = new AspectRegistrationStore();
    
    /**
     * The single ordered store behind entity aspect attribution. Every registration entry point
     * feeds this store and the entity lookup reads only from it.
     */
    private static final EntityAspectStore ENTITY_TAGS = new EntityAspectStore();

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
        
        return OBJECT_TAGS.get(key);
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
            OBJECT_TAGS.registerDirect(itemId.toString(), aspects);
        }
    }
    
    /**
     * Register aspects for an item by ResourceLocation
     * @param itemId the item's resource location
     * @param aspects the aspects to associate
     */
    public static void registerObjectTag(ResourceLocation itemId, AspectList aspects) {
        if (itemId != null && aspects != null) {
            OBJECT_TAGS.registerDirect(itemId.toString(), aspects);
        }
    }

    /**
     * Register aspects for every current and future member of an item tag.
     */
    public static void registerObjectTagForItemTag(ResourceLocation tagId, AspectList aspects) {
        if (tagId != null && aspects != null) {
            OBJECT_TAGS.registerTag(tagId.toString(), aspects, AspectHelper::resolveItemTag);
        }
    }

    /** Refresh retained item-tag registrations after Forge rebinds tags. */
    public static void refreshTagRegistrations() {
        OBJECT_TAGS.refreshTags(AspectHelper::resolveItemTag);
    }

    private static List<String> resolveItemTag(String tagId) {
        ResourceLocation location = ResourceLocation.tryParse(tagId);
        if (location == null || ForgeRegistries.ITEMS.tags() == null) {
            return List.of();
        }
        var tag = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(location));
        if (!tag.isBound()) {
            return List.of();
        }
        List<String> members = new ArrayList<>();
        for (Item item : tag) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId != null) {
                members.add(itemId.toString());
            }
        }
        return members;
    }
    
    /**
     * Register aspects for an entity type.
     *
     * <p>This is the unfiltered shorthand of {@link #registerEntityTag(String, AspectList, ThaumcraftApi.EntityTagsNBT...)}
     * and feeds the same store. Registrations are kept in order and the last matching entry wins.
     *
     * @param entityId the entity type's resource location
     * @param aspects the aspects to associate
     */
    public static void registerEntityTag(ResourceLocation entityId, AspectList aspects) {
        if (entityId != null && aspects != null) {
            ENTITY_TAGS.register(entityId.toString(), aspects, null);
        }
    }

    /**
     * Register aspects for an entity type with optional NBT-variant filters.
     *
     * <p>Public so that {@code ThaumcraftApi.registerEntityTag} can feed the one store the lookup
     * reads. A registration without filters always matches; a filtered registration matches only
     * when every filter is present in the entity's serialized data with the identical NBT type and
     * value (see {@link ThaumcraftApi.EntityTagsNBT}).
     *
     * @param entityName the entity's registry name (e.g., "minecraft:zombie")
     * @param aspects the aspects to associate
     * @param nbt optional NBT filters to differentiate mob variants
     */
    public static void registerEntityTag(String entityName, AspectList aspects, ThaumcraftApi.EntityTagsNBT... nbt) {
        if (entityName != null && aspects != null) {
            ENTITY_TAGS.register(entityName, aspects, nbt);
        }
    }

    /**
     * Get the aspects associated with an entity type without instance data.
     *
     * <p>NBT-variant registrations cannot be evaluated without an entity, so this instance-free
     * projection returns the last registration for the id that has no filters, or null when only
     * variant registrations exist. The result is an owned copy.
     *
     * @param entityId the entity type's resource location
     * @return the aspects for this entity type, or null if none
     */
    public static AspectList getEntityAspects(ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return ENTITY_TAGS.lookup(entityId.toString(), (CompoundTag) null);
    }

    /**
     * Get the aspects associated with an entity instance.
     *
     * <p>Players always receive a fresh list of MAN 4 plus three deterministic name-hash draws of
     * 15 from the registered aspect pool. Every other entity resolves through the registration
     * store in registration order; the last registration whose NBT filters all match the entity's
     * serialized data (without the registry id) wins. The result is an owned copy, and mutating it
     * never affects the store or later lookups.
     *
     * @param entity the entity to query
     * @return the aspects for this entity, or null if none
     */
    public static AspectList getEntityAspects(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Player player) {
            return playerAspects(player.getName().getString());
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) {
            return null;
        }
        return ENTITY_TAGS.lookupInstance(entityId.toString(), () -> entity.saveWithoutId(new CompoundTag()));
    }

    /**
     * The BETA26 player rule: MAN 4 plus three aspects drawn at 15 each from the registered aspect
     * pool, selected by {@code new Random(name.hashCode())} so the picks are stable per name.
     * Pure and package-private so the rule is unit-testable without an entity.
     */
    static AspectList playerAspects(String name) {
        AspectList tags = new AspectList();
        tags.add(Aspect.MAN, 4);
        Aspect[] pool = Aspect.aspects.values().toArray(new Aspect[0]);
        if (name == null || pool.length == 0) {
            return tags;
        }
        Random random = new Random(name.hashCode());
        tags.add(pool[random.nextInt(pool.length)], 15);
        tags.add(pool[random.nextInt(pool.length)], 15);
        tags.add(pool[random.nextInt(pool.length)], 15);
        return tags;
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
        OBJECT_TAGS.clear();
        ENTITY_TAGS.clear();
    }
    
    /**
     * Get the number of registered object tags
     */
    public static int getObjectTagCount() {
        return OBJECT_TAGS.size();
    }
    
    /**
     * Get the number of stored entity registrations, including NBT-variant entries and repeated
     * registrations for the same entity id.
     */
    public static int getEntityTagCount() {
        return ENTITY_TAGS.size();
    }
}
