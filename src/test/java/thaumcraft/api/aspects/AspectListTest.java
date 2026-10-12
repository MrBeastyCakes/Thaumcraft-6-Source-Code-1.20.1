package thaumcraft.api.aspects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

/**
 * Pins the BETA26 AspectList arithmetic and NBT layout (src/main/java_old/api/aspects/AspectList.java)
 * that every aspect container and research cost relies on.
 */
class AspectListTest {

    @Test
    void addAccumulatesAndMergeKeepsTheHigherAmount() {
        AspectList list = new AspectList().add(Aspect.FIRE, 4).add(Aspect.FIRE, 3);
        assertEquals(7, list.getAmount(Aspect.FIRE));

        list.merge(Aspect.FIRE, 5);
        assertEquals(7, list.getAmount(Aspect.FIRE));
        list.merge(Aspect.FIRE, 9);
        assertEquals(9, list.getAmount(Aspect.FIRE));
        list.merge(Aspect.WATER, 2);
        assertEquals(2, list.getAmount(Aspect.WATER));
        assertEquals(0, list.getAmount(Aspect.AIR));
    }

    @Test
    void reduceIsAllOrNothingAndKeepsAnEmptyEntry() {
        AspectList list = new AspectList().add(Aspect.ORDER, 5);
        assertFalse(list.reduce(Aspect.ORDER, 6));
        assertEquals(5, list.getAmount(Aspect.ORDER));

        assertTrue(list.reduce(Aspect.ORDER, 5));
        assertEquals(0, list.getAmount(Aspect.ORDER));
        assertEquals(1, list.size());
        assertFalse(list.contains(Aspect.ORDER));
    }

    @Test
    void removeDropsTheEntryAtOrBelowZero() {
        AspectList list = new AspectList().add(Aspect.EARTH, 5).add(Aspect.AIR, 2);
        list.remove(Aspect.EARTH, 3);
        assertEquals(2, list.getAmount(Aspect.EARTH));
        list.remove(Aspect.EARTH, 10);
        assertEquals(0, list.getAmount(Aspect.EARTH));
        assertEquals(1, list.size());
        list.remove(Aspect.AIR);
        assertEquals(0, list.size());
    }

    @Test
    void listOperationsAndContainment() {
        AspectList cost = new AspectList().add(Aspect.FIRE, 2).add(Aspect.MAGIC, 3);
        AspectList pool = new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 3).add(Aspect.AIR, 1);
        assertTrue(pool.contains(cost));
        assertFalse(cost.contains(pool));
        assertEquals(9, pool.visSize());

        pool.remove(cost);
        assertEquals(3, pool.getAmount(Aspect.FIRE));
        assertEquals(0, pool.getAmount(Aspect.MAGIC));
        assertEquals(2, pool.size());

        pool.add(cost);
        assertEquals(5, pool.getAmount(Aspect.FIRE));
        assertEquals(3, pool.getAmount(Aspect.MAGIC));

        AspectList merged = new AspectList().add(Aspect.FIRE, 4).merge(cost);
        assertEquals(4, merged.getAmount(Aspect.FIRE));
        assertEquals(3, merged.getAmount(Aspect.MAGIC));
    }

    @Test
    void copyIsIndependent() {
        AspectList original = new AspectList().add(Aspect.LIFE, 4);
        AspectList copy = original.copy();
        copy.add(Aspect.LIFE, 1).add(Aspect.DEATH, 1);
        assertEquals(4, original.getAmount(Aspect.LIFE));
        assertEquals(1, original.size());
        assertEquals(5, copy.getAmount(Aspect.LIFE));
    }

    @Test
    void sortingByNameAndAmount() {
        AspectList list = new AspectList().add(Aspect.WATER, 1).add(Aspect.AIR, 3).add(Aspect.FIRE, 2);
        assertArrayEquals(new Aspect[] {Aspect.WATER, Aspect.AIR, Aspect.FIRE}, list.getAspects());
        assertArrayEquals(new Aspect[] {Aspect.AIR, Aspect.WATER, Aspect.FIRE}, list.getAspectsSortedByName());
        assertArrayEquals(new Aspect[] {Aspect.AIR, Aspect.FIRE, Aspect.WATER}, list.getAspectsSortedByAmount());
    }

    @Test
    void nbtRoundTripUsesTheBeta26Layout() {
        AspectList list = new AspectList().add(Aspect.FLUX, 7).add(Aspect.CRYSTAL, 2);
        CompoundTag tag = new CompoundTag();
        list.writeToNBT(tag);

        ListTag entries = tag.getList("Aspects", Tag.TAG_COMPOUND);
        assertEquals(2, entries.size());
        assertEquals("vitium", entries.getCompound(0).getString("key"));
        assertEquals(7, entries.getCompound(0).getInt("amount"));

        AspectList read = new AspectList().add(Aspect.AIR, 1);
        read.readFromNBT(tag);
        assertArrayEquals(list.getAspects(), read.getAspects());
        assertEquals(7, read.getAmount(Aspect.FLUX));
        assertEquals(2, read.getAmount(Aspect.CRYSTAL));
        assertEquals(0, read.getAmount(Aspect.AIR));
    }

    @Test
    void labelledNbtRoundTripSkipsUnknownAspects() {
        CompoundTag tag = new CompoundTag();
        new AspectList().add(Aspect.MIND, 3).writeToNBT(tag, "Cost");

        CompoundTag unknown = new CompoundTag();
        unknown.putString("key", "tempestas");
        unknown.putInt("amount", 5);
        tag.getList("Cost", Tag.TAG_COMPOUND).add(unknown);

        AspectList read = new AspectList();
        read.readFromNBT(tag, "Cost");
        assertEquals(1, read.size());
        assertEquals(3, read.getAmount(Aspect.MIND));
    }
}
