package thaumcraft.api.aspects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import org.junit.jupiter.api.Test;
import thaumcraft.api.ThaumcraftApi;

/**
 * FND-04 entity attribution: the pure store and player seams.
 *
 * The public-API RED witnesses live in EntityAspectLookupTest; the entity-instance clauses are
 * witnessed by thaumcraft.gametest.EntityAspectGameTests against real spawned entities. These
 * focused tests pin the same clauses directly on the pure seams, which need no running game.
 */
class EntityAspectStoreTest {

    @Test
    void typedFilterEqualityPinsTheNbtTagType() {
        CompoundTag byteOne = new CompoundTag();
        byteOne.putByte("powered", (byte) 1);
        CompoundTag intOne = new CompoundTag();
        intOne.putInt("powered", 1);
        CompoundTag stringOne = new CompoundTag();
        stringOne.putString("powered", "1");
        CompoundTag shortOne = new CompoundTag();
        shortOne.putShort("powered", (short) 1);

        assertTrue(EntityAspectStore.matches(byteOne, "powered", (byte) 1), "byte 1 filter matches NBT byte 1");
        assertTrue(EntityAspectStore.matches(byteOne, "powered", true), "a boolean filter denotes the byte tag Minecraft persists");
        assertFalse(EntityAspectStore.matches(intOne, "powered", (byte) 1), "byte 1 filter must not match an int-1 tag");
        assertFalse(EntityAspectStore.matches(byteOne, "powered", 1), "int-1 filter must not match a byte-1 tag");
        assertTrue(EntityAspectStore.matches(intOne, "powered", 1), "int 1 filter matches NBT int 1");
        assertFalse(EntityAspectStore.matches(stringOne, "powered", 1), "int filter must not match a string tag");
        assertTrue(EntityAspectStore.matches(shortOne, "powered", (short) 1), "short 1 filter matches NBT short 1");
        assertFalse(EntityAspectStore.matches(shortOne, "powered", (byte) 1), "byte filter must not match a short tag");
        assertTrue(EntityAspectStore.matches(stringOne, "powered", "1"), "string filter matches the identical string tag");
        assertTrue(EntityAspectStore.matches(intOne, "powered", IntTag.valueOf(1)), "tag-valued filters are used as-is");
        assertFalse(EntityAspectStore.matches(byteOne, "absent", (byte) 1), "absent NBT key never matches");
        assertFalse(EntityAspectStore.matches(byteOne, "powered", new Object()), "values without an NBT equivalent never match");
        assertFalse(EntityAspectStore.matches(byteOne, "powered", null), "null filter values never match");
        assertFalse(EntityAspectStore.matches(null, "powered", (byte) 1), "missing instance data never matches");
    }

    @Test
    void lookupUsesRegistrationOrderAndLastMatchWins() {
        EntityAspectStore store = new EntityAspectStore();
        store.register("test:mob", new AspectList().add(Aspect.FIRE, 10), null);
        store.register("test:mob", new AspectList().add(Aspect.WATER, 6),
                filters(new ThaumcraftApi.EntityTagsNBT("powered", (byte) 1)));
        store.register("test:mob", new AspectList().add(Aspect.ORDER, 4),
                filters(new ThaumcraftApi.EntityTagsNBT("powered", (byte) 0)));
        store.register("test:mob", new AspectList().add(Aspect.ENTROPY, 8),
                filters(new ThaumcraftApi.EntityTagsNBT("powered", (byte) 1),
                        new ThaumcraftApi.EntityTagsNBT("poisoned", true)));

        CompoundTag powered = new CompoundTag();
        powered.putByte("powered", (byte) 1);
        CompoundTag unpowered = new CompoundTag();
        unpowered.putByte("powered", (byte) 0);
        CompoundTag poweredAndPoisoned = new CompoundTag();
        poweredAndPoisoned.putByte("powered", (byte) 1);
        poweredAndPoisoned.putByte("poisoned", (byte) 1);
        CompoundTag poweredWithInt = new CompoundTag();
        poweredWithInt.putInt("powered", 1);

        assertEquals(6, store.lookup("test:mob", powered).getAmount(Aspect.WATER), "the last matching filtered entry wins");
        assertEquals(4, store.lookup("test:mob", unpowered).getAmount(Aspect.ORDER), "the byte-0 variant wins for an unpowered entity");
        assertEquals(10, store.lookup("test:mob", poweredWithInt).getAmount(Aspect.FIRE), "an int tag falls through the byte filters to the base entry");
        assertEquals(8, store.lookup("test:mob", poweredAndPoisoned).getAmount(Aspect.ENTROPY), "both filters matching lets the entry win");
        assertEquals(10, store.lookup("test:mob", null).getAmount(Aspect.FIRE), "instance-free lookup skips filtered entries");
        assertNull(store.lookup("test:absent", powered), "no matching registration means null");
        assertNull(store.lookup(null, powered), "null entity name means null");
        assertEquals(4, store.size(), "size counts every stored registration");
    }

    @Test
    void storeAndLookupOwnTheirCopies() {
        EntityAspectStore store = new EntityAspectStore();
        AspectList input = new AspectList().add(Aspect.EARTH, 2);
        store.register("test:mob", input, null);
        input.add(Aspect.WATER, 50);
        CompoundTag data = new CompoundTag();

        AspectList first = store.lookup("test:mob", data);
        assertEquals(0, first.getAmount(Aspect.WATER), "the store owns a copy of the registered input");
        assertEquals(2, first.getAmount(Aspect.EARTH));

        first.add(Aspect.FIRE, 90);
        AspectList second = store.lookup("test:mob", data);
        assertNotSame(first, second, "every lookup returns a fresh list");
        assertEquals(0, second.getAmount(Aspect.FIRE), "mutating a result cannot reach the store");
        assertEquals(2, second.getAmount(Aspect.EARTH));
        assertEquals(1, store.size());
    }

    @Test
    void repeatedUnfilteredRegistrationsKeepCallOrder() {
        EntityAspectStore store = new EntityAspectStore();
        store.register("test:mob", new AspectList().add(Aspect.FIRE, 10), null);
        store.register("test:mob", new AspectList().add(Aspect.WATER, 6), null);

        assertEquals(6, store.lookup("test:mob", new CompoundTag()).getAmount(Aspect.WATER), "the later unfiltered entry wins");
        assertEquals(0, store.lookup("test:mob", new CompoundTag()).getAmount(Aspect.FIRE));
    }

    @Test
    void clearEmptiesTheStore() {
        EntityAspectStore store = new EntityAspectStore();
        store.register("test:mob", new AspectList().add(Aspect.FIRE, 10), null);
        store.clear();

        assertNull(store.lookup("test:mob", new CompoundTag()));
        assertEquals(0, store.size());
    }

    @Test
    void playerAspectsFollowTheBeta26Rule() {
        for (String name : java.util.List.of("fnd04_player", "Notch", "a", "fnd04_draw_probe")) {
            AspectList actual = AspectHelper.playerAspects(name);
            AspectList expected = independentlyExpected(name);

            assertEquals(expected.size(), actual.size(), "entry count for " + name);
            for (Aspect aspect : expected.getAspects()) {
                assertEquals(expected.getAmount(aspect), actual.getAmount(aspect), "draw for " + aspect.getTag() + " of " + name);
            }
            int total = 0;
            for (Aspect aspect : actual.getAspects()) {
                assertTrue(Aspect.aspects.containsKey(aspect.getTag()), "picks come from the registered aspect pool");
                assertTrue(actual.getAmount(aspect) > 0, "every stored entry is positive");
                total += actual.getAmount(aspect);
            }
            assertEquals(49, total, "MAN 4 plus three draws of 15 for " + name);
            assertEquals(4, actual.getAmount(Aspect.MAN) - 15 * manDraws(name),
                    "the MAN base stays 4 and MAN draws aggregate onto it for " + name);
        }

        AspectList repeated = AspectHelper.playerAspects("fnd04_player");
        AspectList first = AspectHelper.playerAspects("fnd04_player");
        assertEquals(first.size(), repeated.size(), "the rule is deterministic per name");
        for (Aspect aspect : first.getAspects()) {
            assertEquals(first.getAmount(aspect), repeated.getAmount(aspect));
        }

        first.add(Aspect.FIRE, 99);
        AspectList fresh = AspectHelper.playerAspects("fnd04_player");
        AspectList expectedFresh = independentlyExpected("fnd04_player");
        assertNotSame(first, fresh, "every call returns a new instance");
        assertEquals(expectedFresh.size(), fresh.size(), "mutating a previous result cannot change a later call");
        for (Aspect aspect : expectedFresh.getAspects()) {
            assertEquals(expectedFresh.getAmount(aspect), fresh.getAmount(aspect), "leaked draw for " + aspect.getTag());
        }
    }

    private static int manDraws(String name) {
        Aspect[] pool = Aspect.aspects.values().toArray(new Aspect[0]);
        Random random = new Random(name.hashCode());
        int draws = 0;
        for (int draw = 0; draw < 3; draw++) {
            if (pool[random.nextInt(pool.length)] == Aspect.MAN) {
                draws++;
            }
        }
        return draws;
    }

    private static AspectList independentlyExpected(String name) {
        Aspect[] pool = Aspect.aspects.values().toArray(new Aspect[0]);
        Random random = new Random(name.hashCode());
        AspectList expected = new AspectList().add(Aspect.MAN, 4);
        for (int draw = 0; draw < 3; draw++) {
            expected.add(pool[random.nextInt(pool.length)], 15);
        }
        return expected;
    }

    private static ThaumcraftApi.EntityTagsNBT[] filters(ThaumcraftApi.EntityTagsNBT... filters) {
        return filters;
    }
}
