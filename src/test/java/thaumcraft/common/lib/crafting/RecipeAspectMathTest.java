package thaumcraft.common.lib.crafting;

import org.junit.jupiter.api.Test;
import thaumcraft.api.aspects.*;
import static org.junit.jupiter.api.Assertions.*;

class RecipeAspectMathTest {
    private AspectList earth(int n) { return new AspectList().add(Aspect.EARTH, n); }
    @Test void strictRoundingAndDivision() {
        assertEquals(3, RecipeAspectMath.normalize(earth(4), 1).getAmount(Aspect.EARTH));
        assertEquals(0, RecipeAspectMath.normalize(earth(1), 1).size());
        assertEquals(0, RecipeAspectMath.normalize(earth(2), 2).size());
        assertEquals(1, RecipeAspectMath.normalize(earth(5), 4).getAmount(Aspect.EARTH));
    }
    @Test void remainderOverdrawIsRefused() {
        AspectList remainder = new AspectList().add(Aspect.METAL, 3);
        assertEquals(1, RecipeAspectMath.normalize(RecipeAspectMath.subtract(new AspectList().add(Aspect.METAL,8), remainder),2).getAmount(Aspect.METAL));
        assertEquals(1, RecipeAspectMath.normalize(RecipeAspectMath.subtract(new AspectList().add(Aspect.METAL,2), remainder),1).getAmount(Aspect.METAL));
    }
    @Test void categoryAdditionsUseSquareRootsAndIntegerVisDivision() {
        assertEquals(4, RecipeAspectMath.essentia(new AspectList().add(Aspect.MAGIC,3),new AspectList().add(Aspect.MAGIC,9),3).getAmount(Aspect.MAGIC));
        assertEquals(0, RecipeAspectMath.essentia(new AspectList(),new AspectList().add(Aspect.FIRE,8),3).size());
        assertEquals(5, RecipeAspectMath.essentia(RecipeAspectMath.normalize(new AspectList().add(Aspect.MAGIC,8),2),new AspectList().add(Aspect.MAGIC,16),2).getAmount(Aspect.MAGIC));
        assertEquals(2, RecipeAspectMath.arcane(new AspectList(),30,2).getAmount(Aspect.MAGIC));
        assertEquals(1, RecipeAspectMath.arcane(new AspectList(),3,1).getAmount(Aspect.MAGIC));
    }
    @Test void invalidCountsAreEmptyAndLargeValuesCapped() {
        for(int count : new int[]{0,-1,Integer.MIN_VALUE}) {
            assertEquals(0, RecipeAspectMath.normalize(earth(4),count).size());
            assertEquals(0, RecipeAspectMath.essentia(earth(4),earth(9),count).size());
            assertEquals(0, RecipeAspectMath.arcane(earth(4),30,count).size());
        }
        assertEquals(500, RecipeAspectMath.cap(earth(700)).getAmount(Aspect.EARTH));
        assertEquals(500, RecipeAspectMath.cap(RecipeAspectMath.normalize(earth(Integer.MAX_VALUE),1)).getAmount(Aspect.EARTH));
    }
    @Test void operationsOwnTheirCopies() {
        AspectList source = earth(8), required = earth(9);
        RecipeAspectMath.subtract(source,required).add(Aspect.FIRE,3);
        RecipeAspectMath.normalize(source,2).add(Aspect.FIRE,3);
        RecipeAspectMath.essentia(source,required,3).add(Aspect.FIRE,3);
        RecipeAspectMath.arcane(source,30,2).add(Aspect.FIRE,3);
        RecipeAspectMath.cap(source).add(Aspect.FIRE,3);
        assertEquals(8, source.getAmount(Aspect.EARTH)); assertEquals(1, source.size());
        assertEquals(9, required.getAmount(Aspect.EARTH)); assertEquals(1, required.size());
    }
    @Test void positiveAdditionsKeepInsertionOrderForCullingTies() {
        AspectList source = new AspectList().add(Aspect.AIR,1).add(Aspect.WATER,2).add(Aspect.MAGIC,3)
                .add(Aspect.FIRE,3).add(Aspect.EARTH,3).add(Aspect.ORDER,3).add(Aspect.ENTROPY,3);
        AspectList required = new AspectList().add(Aspect.AIR,1).add(Aspect.LIFE,100);
        AspectList essentia = RecipeAspectMath.essentia(source,required,1);
        assertArrayEquals(new Aspect[]{Aspect.AIR,Aspect.WATER,Aspect.MAGIC,Aspect.FIRE,Aspect.EARTH,Aspect.ORDER,Aspect.ENTROPY,Aspect.LIFE},essentia.getAspects());
        assertEquals(2,essentia.getAmount(Aspect.AIR)); assertEquals(10,essentia.getAmount(Aspect.LIFE));
        AspectList arcane = RecipeAspectMath.arcane(source,30,1);
        assertEquals(Aspect.MAGIC,arcane.getAspects()[2]); assertEquals(7,arcane.getAmount(Aspect.MAGIC));
        essentia.add(Aspect.ENTROPY,40); arcane.add(Aspect.ENTROPY,40);
        assertEquals(3,source.getAmount(Aspect.MAGIC)); assertEquals(1,source.getAmount(Aspect.AIR));
        assertEquals(7,source.size());
    }
}
