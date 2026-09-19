package thaumcraft.api.aspects;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AspectCullingTest {

    @Test
    void defaultCapRemovesFirstTiedPrimalAndPreservesSurvivorOrder() {
        AspectList source = new AspectList()
                .add(Aspect.AIR, 10)
                .add(Aspect.EARTH, 10)
                .add(Aspect.FIRE, 10)
                .add(Aspect.WATER, 10)
                .add(Aspect.ORDER, 10)
                .add(Aspect.ENTROPY, 10)
                .add(Aspect.VOID, 10)
                .add(Aspect.LIGHT, 10);

        AspectList result = AspectHelper.cullTags(source);

        assertArrayEquals(new Aspect[]{
                        Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER,
                        Aspect.ENTROPY, Aspect.VOID, Aspect.LIGHT
                }, result.getAspects());
        assertEquals(10, result.getAmount(Aspect.EARTH));
        assertEquals(0, result.getAmount(Aspect.AIR));
        assertEquals(8, source.size());
        assertEquals(10, source.getAmount(Aspect.AIR));
    }

    @Test
    void explicitCapUsesPrimalDiscount() {
        assertSingleSurvivor(
                new AspectList().add(Aspect.AIR, 10).add(Aspect.VOID, 10),
                Aspect.VOID, 10);
    }

    @Test
    void explicitCapWeightsOneCompoundParent() {
        assertSingleSurvivor(
                new AspectList().add(Aspect.MAGIC, 10).add(Aspect.LIGHT, 10),
                Aspect.MAGIC, 10);
    }

    @Test
    void explicitCapWeightsBothCompoundParents() {
        assertSingleSurvivor(
                new AspectList().add(Aspect.DARKNESS, 10).add(Aspect.MAGIC, 10),
                Aspect.DARKNESS, 10);
    }

    @Test
    void explicitCapWeightsNonPrimalGrandparent() {
        assertSingleSurvivor(
                new AspectList().add(Aspect.AURA, 10).add(Aspect.MAGIC, 10),
                Aspect.AURA, 10);
    }

    @Test
    void explicitCapWeightsBothGrandparentsOfRightParentInStoredOrder() {
        assertSingleSurvivor(
                new AspectList().add(Aspect.MIND, 10).add(Aspect.DARKNESS, 10),
                Aspect.MIND, 10);
    }

    @Test
    void explicitCapSelectsRealMinimumAboveOriginalSentinel() {
        AspectList source = new AspectList()
                .add(Aspect.AIR, 50_000)
                .add(Aspect.WATER, 60_000)
                .add(Aspect.VOID, 40_000);

        AspectList result = AspectHelper.cullTags(source, 2);

        assertArrayEquals(new Aspect[]{Aspect.AIR, Aspect.WATER}, result.getAspects());
        assertEquals(50_000, result.getAmount(Aspect.AIR));
        assertEquals(60_000, result.getAmount(Aspect.WATER));
    }

    @Test
    void noOpCullReturnsIndependentCopyAndSkipsNullKeys() {
        AspectList source = new AspectList().add(Aspect.AIR, 3).add(Aspect.FIRE, 2);
        source.aspects.put(null, 99);

        AspectList result = AspectHelper.cullTags(source, 7);
        result.add(Aspect.AIR, 7).remove(Aspect.FIRE);

        assertNotSame(source, result);
        assertArrayEquals(new Aspect[]{Aspect.AIR}, result.getAspects());
        assertArrayEquals(new Aspect[]{Aspect.AIR, Aspect.FIRE, null}, source.getAspects());
        assertEquals(3, source.getAmount(Aspect.AIR));
        assertEquals(2, source.getAmount(Aspect.FIRE));
    }

    @Test
    void noOpCullPreservesZeroAndNegativeAmounts() {
        AspectList source = new AspectList().add(Aspect.AIR, 0).add(Aspect.FIRE, -4);

        AspectList result = AspectHelper.cullTags(source, 2);

        assertArrayEquals(new Aspect[]{Aspect.AIR, Aspect.FIRE}, result.getAspects());
        assertEquals(0, result.getAmount(Aspect.AIR));
        assertEquals(-4, result.getAmount(Aspect.FIRE));
    }

    @Test
    void zeroCapReturnsIndependentEmptyList() {
        AspectList source = new AspectList().add(Aspect.AIR, 3);

        AspectList result = AspectHelper.cullTags(source, 0);

        assertNotSame(source, result);
        assertEquals(0, result.size());
        assertEquals(1, source.size());
    }

    @Test
    void negativeCapIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> AspectHelper.cullTags(new AspectList(), -1));
    }

    @Test
    void nullSourceRetainsOrdinaryNullPointerException() {
        assertThrows(NullPointerException.class, () -> AspectHelper.cullTags(null));
        assertThrows(NullPointerException.class, () -> AspectHelper.cullTags(null, 7));
    }

    private static void assertSingleSurvivor(AspectList source, Aspect expected, int amount) {
        List<Aspect> originalOrder = List.of(source.getAspects());

        AspectList result = AspectHelper.cullTags(source, 1);

        assertArrayEquals(new Aspect[]{expected}, result.getAspects());
        assertEquals(amount, result.getAmount(expected));
        assertArrayEquals(originalOrder.toArray(Aspect[]::new), source.getAspects());
    }
}
