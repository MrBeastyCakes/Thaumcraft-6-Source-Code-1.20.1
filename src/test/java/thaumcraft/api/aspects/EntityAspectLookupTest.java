package thaumcraft.api.aspects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import thaumcraft.api.ThaumcraftApi;

/**
 * FND-04 entity attribution: witnesses that are observable through the public
 * registration and lookup surface without a running game.
 *
 * The entity-instance clauses (players, NBT-variant matching, typed equality)
 * are witnessed behaviorally by thaumcraft.gametest.EntityAspectGameTests and
 * by EntityAspectStoreTest once the pure store seams exist.
 */
class EntityAspectLookupTest {

    @Test
    void storeAndLookupExchangeOwnedCopies() {
        ResourceLocation id = new ResourceLocation("fnd04_entity", "owned");
        AspectList input = new AspectList().add(Aspect.EARTH, 2);
        AspectHelper.registerEntityTag(id, input);
        input.add(Aspect.WATER, 50);

        AspectList registered = AspectHelper.getEntityAspects(id);
        assertNotNull(registered, "Registered entity id must resolve");
        assertEquals(0, registered.getAmount(Aspect.WATER), "Store must own a copy of the registered input");
        assertEquals(2, registered.getAmount(Aspect.EARTH));

        registered.add(Aspect.FIRE, 90);
        AspectList second = AspectHelper.getEntityAspects(id);
        assertNotNull(second, "Registered entity id must keep resolving");
        assertEquals(0, second.getAmount(Aspect.FIRE), "Lookup results must be owned copies");
        assertEquals(2, second.getAmount(Aspect.EARTH));
    }

    @Test
    void everyRegistrationPathFeedsTheStoreTheLookupReads() {
        int before = AspectHelper.getEntityTagCount();
        AspectHelper.registerEntityTag(new ResourceLocation("fnd04_entity", "plain"),
                new AspectList().add(Aspect.AIR, 1));
        ThaumcraftApi.registerEntityTag("fnd04_entity:variant",
                new AspectList().add(Aspect.FIRE, 2),
                new ThaumcraftApi.EntityTagsNBT("powered", (byte) 1));

        assertEquals(before + 2, AspectHelper.getEntityTagCount(),
                "Both registration entry points must feed the one store the lookup reads");
    }

    @Test
    void instanceFreeLookupSkipsVariantEntriesAndHonoursOrder() {
        AspectHelper.registerEntityTag(new ResourceLocation("fnd04_entity", "base"),
                new AspectList().add(Aspect.AIR, 1));
        ThaumcraftApi.registerEntityTag("fnd04_entity:base", new AspectList().add(Aspect.WATER, 4));

        AspectList resolved = AspectHelper.getEntityAspects(new ResourceLocation("fnd04_entity", "base"));
        assertNotNull(resolved);
        assertEquals(4, resolved.getAmount(Aspect.WATER), "Last unfiltered registration wins");
        assertEquals(0, resolved.getAmount(Aspect.AIR));

        ThaumcraftApi.registerEntityTag("fnd04_entity:variant_only",
                new AspectList().add(Aspect.FIRE, 2),
                new ThaumcraftApi.EntityTagsNBT("powered", (byte) 1));
        assertNull(AspectHelper.getEntityAspects(new ResourceLocation("fnd04_entity", "variant_only")),
                "NBT-variant entries cannot resolve without an entity instance");
    }

    @Test
    void unregisteredEntityIdsStayAbsent() {
        assertNull(AspectHelper.getEntityAspects(new ResourceLocation("fnd04_entity", "absent")));
    }
}
