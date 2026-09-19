package thaumcraft.api.aspects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectListTest {

    @Test
    void containsRejectsAnyIndividuallyUnderfundedAspect() {
        AspectList available = new AspectList()
                .add(Aspect.AIR, 5)
                .add(Aspect.FIRE, 1);
        AspectList required = new AspectList()
                .add(Aspect.AIR, 4)
                .add(Aspect.FIRE, 2);

        assertFalse(available.contains(required));
        assertTrue(available.contains(new AspectList().add(Aspect.AIR, 5).add(Aspect.FIRE, 1)));
    }

    @Test
    void copyCanBeMutatedWithoutChangingTheOriginal() {
        AspectList original = new AspectList().add(Aspect.EARTH, 7);

        AspectList copy = original.copy();
        copy.remove(Aspect.EARTH, 3).add(Aspect.WATER, 2);

        assertEquals(7, original.getAmount(Aspect.EARTH));
        assertEquals(0, original.getAmount(Aspect.WATER));
        assertEquals(4, copy.getAmount(Aspect.EARTH));
        assertEquals(2, copy.getAmount(Aspect.WATER));
    }

    @Test
    void nbtRoundTripPreservesKnownAspectAmounts() {
        AspectList original = new AspectList()
                .add(Aspect.ORDER, 3)
                .add(Aspect.ENTROPY, 9);
        CompoundTag tag = new CompoundTag();

        original.writeToNBT(tag);
        AspectList restored = new AspectList();
        restored.readFromNBT(tag);

        assertEquals(2, restored.size());
        assertEquals(3, restored.getAmount(Aspect.ORDER));
        assertEquals(9, restored.getAmount(Aspect.ENTROPY));
    }

    @Test
    void nbtReadIgnoresUnknownAndMissingAspectKeys() {
        CompoundTag known = aspectEntry(Aspect.MAGIC.getTag(), 6);
        CompoundTag unknown = aspectEntry("fnd01_unknown_aspect", 99);
        CompoundTag missingKey = new CompoundTag();
        missingKey.putInt("amount", 42);
        ListTag entries = new ListTag();
        entries.add(known);
        entries.add(unknown);
        entries.add(missingKey);
        CompoundTag tag = new CompoundTag();
        tag.put("Aspects", entries);

        AspectList restored = new AspectList().add(Aspect.AIR, 100);
        restored.readFromNBT(tag);

        assertEquals(1, restored.size());
        assertEquals(6, restored.getAmount(Aspect.MAGIC));
        assertEquals(0, restored.getAmount(Aspect.AIR));
    }

    private static CompoundTag aspectEntry(String key, int amount) {
        CompoundTag entry = new CompoundTag();
        entry.putString("key", key);
        entry.putInt("amount", amount);
        return entry;
    }
}
