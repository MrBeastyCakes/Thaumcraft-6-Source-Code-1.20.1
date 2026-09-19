package thaumcraft.gametest;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class EntityAspectGameTests {
    private static final String TEMPLATE = "fnd01_empty";
    private static final BlockPos SPAWN_POS = new BlockPos(0, 0, 0);

    private EntityAspectGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void zombieUsesRegisteredBaseEntry(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN_POS);

        expect(helper, AspectHelper.getEntityAspects(zombie), "zombie base registration",
                new Aspect[]{Aspect.UNDEAD, Aspect.MAN, Aspect.EARTH}, new int[]{20, 10, 5});

        zombie.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void poweredCreeperVariantOverridesBaseEntry(GameTestHelper helper) {
        Creeper creeper = helper.spawn(EntityType.CREEPER, SPAWN_POS);

        expect(helper, AspectHelper.getEntityAspects(creeper), "unpowered creeper keeps the base entry",
                new Aspect[]{Aspect.PLANT, Aspect.FIRE}, new int[]{15, 15});

        CompoundTag powered = new CompoundTag();
        powered.putBoolean("powered", true);
        creeper.readAdditionalSaveData(powered);
        helper.assertTrue(creeper.isPowered(), "Fixture creeper must be powered");

        expect(helper, AspectHelper.getEntityAspects(creeper), "powered creeper wins over the earlier base entry",
                new Aspect[]{Aspect.PLANT, Aspect.FIRE, Aspect.ENERGY}, new int[]{15, 15, 15});

        creeper.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lastMatchingVariantWinsWithTypedNbtEquality(GameTestHelper helper) {
        String entityId = "minecraft:falling_block";
        FallingBlockEntity block = helper.spawn(EntityType.FALLING_BLOCK, SPAWN_POS);
        block.time = 1;
        helper.assertTrue(block.dropItem, "Fixture must persist the default byte-1 DropItem flag");

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.FIRE, 10));
        expect(helper, AspectHelper.getEntityAspects(block), "single unfiltered entry",
                new Aspect[]{Aspect.FIRE}, new int[]{10});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.WATER, 6),
                new ThaumcraftApi.EntityTagsNBT("DropItem", true));
        expect(helper, AspectHelper.getEntityAspects(block), "boolean filter matches the byte-1 tag and wins",
                new Aspect[]{Aspect.WATER}, new int[]{6});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.ORDER, 4),
                new ThaumcraftApi.EntityTagsNBT("DropItem", (byte) 0));
        expect(helper, AspectHelper.getEntityAspects(block), "typed mismatch falls through to the earlier match",
                new Aspect[]{Aspect.WATER}, new int[]{6});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.DEATH, 9),
                new ThaumcraftApi.EntityTagsNBT("DropItem", 1));
        expect(helper, AspectHelper.getEntityAspects(block), "int-1 filter must not match the byte-1 tag",
                new Aspect[]{Aspect.WATER}, new int[]{6});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.ENTROPY, 8),
                new ThaumcraftApi.EntityTagsNBT("fnd04_absent_key", (byte) 1));
        expect(helper, AspectHelper.getEntityAspects(block), "absent NBT key never matches",
                new Aspect[]{Aspect.WATER}, new int[]{6});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.LIFE, 3),
                new ThaumcraftApi.EntityTagsNBT("Time", 1));
        expect(helper, AspectHelper.getEntityAspects(block), "int-1 filter matches the int-1 tag and wins",
                new Aspect[]{Aspect.LIFE}, new int[]{3});

        ThaumcraftApi.registerEntityTag(entityId, new AspectList().add(Aspect.AURA, 7),
                new ThaumcraftApi.EntityTagsNBT("Time", (byte) 1));
        expect(helper, AspectHelper.getEntityAspects(block), "byte-1 filter must not match the int-1 tag",
                new Aspect[]{Aspect.LIFE}, new int[]{3});

        block.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void unregisteredEntityHasNoAspects(GameTestHelper helper) {
        LivingEntity stand = helper.spawn(EntityType.ARMOR_STAND, SPAWN_POS);

        helper.assertTrue(AspectHelper.getEntityAspects(stand) == null,
                "Unregistered entity type must have no aspects");

        stand.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void entityLookupReturnsOwnedCopies(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN_POS);
        Aspect[] base = {Aspect.UNDEAD, Aspect.MAN, Aspect.EARTH};
        int[] amounts = {20, 10, 5};

        AspectList first = AspectHelper.getEntityAspects(zombie);
        expect(helper, first, "zombie base registration", base, amounts);

        first.add(Aspect.FIRE, 90);
        expect(helper, AspectHelper.getEntityAspects(zombie), "caller mutation must not leak into later lookups", base, amounts);

        Zombie other = helper.spawn(EntityType.ZOMBIE, SPAWN_POS);
        expect(helper, AspectHelper.getEntityAspects(other), "second entity keeps the stored value", base, amounts);

        other.discard();
        zombie.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void playersUseDeterministicNameHashPicks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        AspectList expected = expectedPlayerAspects(player);
        Aspect[] aspects = expected.getAspects();
        int[] amounts = amountsOf(expected);

        AspectList first = AspectHelper.getEntityAspects(player);
        helper.assertTrue(first != null, "Player lookup must return a list");
        expect(helper, first, "name-hash picks", aspects, amounts);

        int drawn = 0;
        for (Aspect aspect : first.getAspects()) {
            helper.assertTrue(Aspect.aspects.containsValue(aspect), "Pick must come from the registered pool: " + aspect);
            drawn += first.getAmount(aspect);
        }
        helper.assertTrue(drawn == 49, "MAN 4 plus exactly three draws of 15, got " + first);
        int man = first.getAmount(Aspect.MAN);
        helper.assertTrue(man >= 4 && (man - 4) % 15 == 0, "MAN keeps its base 4 and aggregates any MAN draw, got " + first);

        expect(helper, AspectHelper.getEntityAspects(player), "repeat lookup stays deterministic", aspects, amounts);

        Set<Aspect> picked = new LinkedHashSet<>(java.util.Arrays.asList(aspects));
        Aspect outsider = null;
        for (Aspect aspect : Aspect.aspects.values()) {
            if (!picked.contains(aspect)) {
                outsider = aspect;
                break;
            }
        }
        helper.assertTrue(outsider != null, "Aspect pool must expose an unused aspect");
        first.add(outsider, 90);
        expect(helper, AspectHelper.getEntityAspects(player), "player results are owned copies", aspects, amounts);

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void taintSeedDuplicatesFollowBeta26LastMatchWins(GameTestHelper helper) {
        expect(helper, AspectHelper.getEntityAspects(new ResourceLocation("thaumcraft", "taint_seed")),
                "taint seed projection uses the later BETA26 duplicate",
                new Aspect[]{Aspect.PLANT, Aspect.BEAST, Aspect.FLUX}, new int[]{20, 20, 20});
        expect(helper, AspectHelper.getEntityAspects(new ResourceLocation("thaumcraft", "taint_seed_prime")),
                "taint seed prime projection uses the later BETA26 duplicate",
                new Aspect[]{Aspect.PLANT, Aspect.BEAST, Aspect.FLUX}, new int[]{30, 30, 30});

        var seed = helper.spawn(thaumcraft.init.ModEntities.TAINT_SEED.get(), SPAWN_POS);
        expect(helper, AspectHelper.getEntityAspects(seed), "taint seed entity uses the later BETA26 duplicate",
                new Aspect[]{Aspect.PLANT, Aspect.BEAST, Aspect.FLUX}, new int[]{20, 20, 20});
        seed.discard();

        var prime = helper.spawn(thaumcraft.init.ModEntities.TAINT_SEED_PRIME.get(), SPAWN_POS);
        expect(helper, AspectHelper.getEntityAspects(prime), "taint seed prime entity uses the later BETA26 duplicate",
                new Aspect[]{Aspect.PLANT, Aspect.BEAST, Aspect.FLUX}, new int[]{30, 30, 30});
        prime.discard();

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wispUsesPerTypeVariantWithDocumentedFallback(GameTestHelper helper) {
        var wisp = helper.spawn(thaumcraft.init.ModEntities.WISP.get(), SPAWN_POS);

        wisp.setWispType("aer");
        expect(helper, AspectHelper.getEntityAspects(wisp), "typed wisp takes the per-aspect variant",
                new Aspect[]{Aspect.AIR, Aspect.AURA, Aspect.FLIGHT}, new int[]{5, 5, 5});

        wisp.setWispType("ignis");
        expect(helper, AspectHelper.getEntityAspects(wisp), "variant follows the wisp's own tag",
                new Aspect[]{Aspect.FIRE, Aspect.AURA, Aspect.FLIGHT}, new int[]{5, 5, 5});

        wisp.setWispType("");
        expect(helper, AspectHelper.getEntityAspects(wisp), "typeless wisp falls back to the documented base entry",
                new Aspect[]{Aspect.AURA, Aspect.FLIGHT}, new int[]{10, 5});

        wisp.discard();
        helper.succeed();
    }

    private static AspectList expectedPlayerAspects(Player player) {
        Aspect[] pool = Aspect.aspects.values().toArray(new Aspect[0]);
        Random random = new Random(player.getName().getString().hashCode());
        AspectList expected = new AspectList().add(Aspect.MAN, 4);
        for (int i = 0; i < 3; i++) {
            expected.add(pool[random.nextInt(pool.length)], 15);
        }
        return expected;
    }

    private static int[] amountsOf(AspectList list) {
        Aspect[] aspects = list.getAspects();
        int[] amounts = new int[aspects.length];
        for (int i = 0; i < aspects.length; i++) {
            amounts[i] = list.getAmount(aspects[i]);
        }
        return amounts;
    }

    private static void expect(GameTestHelper helper, AspectList actual, String label, Aspect[] aspects, int[] amounts) {
        helper.assertTrue(actual != null, label + ": expected a list, got null");
        helper.assertTrue(actual.size() == aspects.length, label + ": expected " + aspects.length + " entries, got " + actual);
        for (int i = 0; i < aspects.length; i++) {
            helper.assertTrue(actual.getAmount(aspects[i]) == amounts[i],
                    label + ": expected " + aspects[i].getTag() + " " + amounts[i] + ", got " + actual);
        }
    }
}
