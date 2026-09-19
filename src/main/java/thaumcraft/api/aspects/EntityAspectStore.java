package thaumcraft.api.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import thaumcraft.api.ThaumcraftApi;

/**
 * The one ordered store behind entity aspect attribution.
 *
 * Semantics (BETA26 Phase-2 entity contract, re-implemented for 1.20.1):
 * - Registrations are kept in registration order and are never replaced or reordered.
 * - A lookup walks every registration whose entity name equals the queried registry id string.
 * - A registration without NBT filters always matches; a filtered registration matches only when
 *   every filter is present in the entity's serialized data with the identical NBT tag type and value.
 * - The walk does not stop at the first match: the last matching registration wins.
 * - No matching registration means no aspects (null).
 * - Both the registered input and every lookup result are owned copies; the store never hands out
 *   or retains a caller-owned list.
 *
 * Filters are normalized to NBT tags at registration, so later mutation of the caller's filter
 * objects cannot change matching. Values that carry no NBT type equivalent never match.
 */
final class EntityAspectStore {
    private final List<Registration> registrations = new ArrayList<>();

    void register(String entityName, AspectList aspects, ThaumcraftApi.EntityTagsNBT[] filters) {
        if (entityName == null || aspects == null) {
            return;
        }
        registrations.add(new Registration(entityName, aspects.copy(), copyFilters(filters)));
    }

    /**
     * Resolves the registrations for an entity name against instance data.
     *
     * @param entityData serialized entity data without the registry id, or null when no instance is
     *                   available; NBT-filtered registrations cannot match without instance data
     * @return an owned copy of the last matching registration's aspects, or null when none matches
     */
    AspectList lookup(String entityName, CompoundTag entityData) {
        return match(entityName, entityData == null ? null : () -> entityData);
    }

    /** Resolves registrations against lazily serialized entity data (serialized at most once). */
    AspectList lookupInstance(String entityName, Supplier<CompoundTag> entityData) {
        return match(entityName, entityData);
    }

    int size() {
        return registrations.size();
    }

    void clear() {
        registrations.clear();
    }

    private AspectList match(String entityName, Supplier<CompoundTag> entityData) {
        if (entityName == null) {
            return null;
        }
        AspectList matched = null;
        boolean resolved = false;
        CompoundTag data = null;
        for (Registration registration : registrations) {
            if (!registration.entityName().equals(entityName)) {
                continue;
            }
            List<Filter> filters = registration.filters();
            if (filters == null) {
                matched = registration.aspects();
                continue;
            }
            if (!resolved) {
                resolved = true;
                data = entityData == null ? null : entityData.get();
            }
            if (data != null && matchesAll(data, filters)) {
                matched = registration.aspects();
            }
        }
        return matched == null ? null : matched.copy();
    }

    private static List<Filter> copyFilters(ThaumcraftApi.EntityTagsNBT[] filters) {
        if (filters == null || filters.length == 0) {
            return null;
        }
        List<Filter> copy = new ArrayList<>(filters.length);
        for (ThaumcraftApi.EntityTagsNBT filter : filters) {
            if (filter == null) {
                return neverMatches();
            }
            copy.add(new Filter(filter.name, expectedTag(filter.value)));
        }
        return List.copyOf(copy);
    }

    private static List<Filter> neverMatches() {
        List<Filter> filters = new ArrayList<>(1);
        filters.add(new Filter(null, null));
        return List.copyOf(filters);
    }

    private static boolean matchesAll(CompoundTag data, List<Filter> filters) {
        for (Filter filter : filters) {
            if (!matches(data, filter)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(CompoundTag data, Filter filter) {
        if (filter.name() == null || filter.expected() == null) {
            return false;
        }
        Tag actual = data.get(filter.name());
        return actual != null && actual.equals(filter.expected());
    }

    /**
     * Typed NBT filter comparison. A filter value matches only when the entity carries the same
     * NBT tag type (for example a byte filter never matches an int tag, and an int filter never
     * matches a byte tag).
     */
    static boolean matches(CompoundTag data, String name, Object value) {
        return data != null && name != null && matches(data, new Filter(name, expectedTag(value)));
    }

    /**
     * Maps a registered filter value onto the NBT tag it denotes. Java booleans denote the byte
     * tags Minecraft persists for booleans; tag instances are used as-is; types without an NBT
     * equivalent never match.
     */
    static Tag expectedTag(Object value) {
        if (value instanceof Tag tag) {
            return tag.copy();
        }
        if (value instanceof Byte byteValue) {
            return ByteTag.valueOf(byteValue);
        }
        if (value instanceof Short shortValue) {
            return ShortTag.valueOf(shortValue);
        }
        if (value instanceof Integer intValue) {
            return IntTag.valueOf(intValue);
        }
        if (value instanceof Long longValue) {
            return LongTag.valueOf(longValue);
        }
        if (value instanceof Float floatValue) {
            return FloatTag.valueOf(floatValue);
        }
        if (value instanceof Double doubleValue) {
            return DoubleTag.valueOf(doubleValue);
        }
        if (value instanceof Boolean booleanValue) {
            return ByteTag.valueOf(booleanValue);
        }
        if (value instanceof String stringValue) {
            return StringTag.valueOf(stringValue);
        }
        return null;
    }

    private record Registration(String entityName, AspectList aspects, List<Filter> filters) {
    }

    private record Filter(String name, Tag expected) {
    }
}
