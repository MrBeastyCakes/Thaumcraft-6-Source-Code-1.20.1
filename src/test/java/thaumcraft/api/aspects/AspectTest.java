package thaumcraft.api.aspects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pins the BETA26 aspect table (src/main/java_old/api/aspects/Aspect.java): the 6 primals,
 * the 31 compounds, their component pairs, and the registry lookups built from them.
 */
class AspectTest {

    private static final List<String> BETA26_PRIMALS = List.of(
            "aer", "terra", "ignis", "aqua", "ordo", "perditio");

    private static final List<String> BETA26_ASPECTS = List.of(
            "aer", "terra", "ignis", "aqua", "ordo", "perditio",
            "vacuos", "lux", "motus", "gelum", "vitreus", "metallum", "victus", "mortuus",
            "potentia", "permutatio", "praecantatio", "auram", "alkimia", "vitium", "tenebrae",
            "alienis", "volatus", "herba", "instrumentum", "fabrico", "machina", "vinculum",
            "spiritus", "cognitio", "sensus", "aversio", "praemunio", "desiderium", "exanimis",
            "bestia", "humanus");

    @BeforeAll
    static void loadAspects() {
        // Touching a constant runs the static initializer that registers every built-in aspect.
        assertNotNull(Aspect.AIR);
    }

    @Test
    void registersExactlyTheBeta26AspectsInOrder() {
        assertEquals(BETA26_ASPECTS, List.copyOf(Aspect.aspects.keySet()));
    }

    @Test
    void primalsAreTheSixComponentlessAspects() {
        assertEquals(BETA26_PRIMALS, Aspect.getPrimalAspects().stream().map(Aspect::getTag).toList());
        for (Aspect primal : Aspect.getPrimalAspects()) {
            assertTrue(primal.isPrimal());
            assertNull(primal.getComponents());
        }
    }

    @Test
    void everyCompoundHasTwoRegisteredComponentsAndIsInTheMixList() {
        assertEquals(BETA26_ASPECTS.size() - BETA26_PRIMALS.size(), Aspect.getCompoundAspects().size());
        for (Aspect compound : Aspect.getCompoundAspects()) {
            Aspect[] parts = compound.getComponents();
            assertEquals(2, parts.length, compound.getTag());
            assertSame(parts[0], Aspect.getAspect(parts[0].getTag()), compound.getTag());
            assertSame(parts[1], Aspect.getAspect(parts[1].getTag()), compound.getTag());
            int key = (parts[0].getTag() + parts[1].getTag()).hashCode();
            assertSame(compound, Aspect.mixList.get(key), compound.getTag());
        }
    }

    @Test
    void componentPairsMatchBeta26() {
        assertComponents(Aspect.VOID, Aspect.AIR, Aspect.ENTROPY);
        assertComponents(Aspect.MAGIC, Aspect.ENERGY, Aspect.AIR);
        assertComponents(Aspect.FLUX, Aspect.ENTROPY, Aspect.MAGIC);
        assertComponents(Aspect.ELDRITCH, Aspect.VOID, Aspect.DARKNESS);
        assertComponents(Aspect.CRAFT, Aspect.EXCHANGE, Aspect.TOOL);
        assertComponents(Aspect.SOUL, Aspect.LIFE, Aspect.DEATH);
        assertComponents(Aspect.MAN, Aspect.SOUL, Aspect.LIFE);
    }

    @Test
    void lookupAndPresentationFields() {
        assertSame(Aspect.FIRE, Aspect.getAspect("ignis"));
        assertNull(Aspect.getAspect("tempestas"));
        assertEquals(0xff5a01, Aspect.FIRE.getColor());
        assertEquals("c", Aspect.FIRE.getChatcolor());
        assertEquals(771, Aspect.ENTROPY.getBlend());
        assertEquals(771, Aspect.VOID.getBlend());
        assertEquals(1, Aspect.LIGHT.getBlend());
        assertEquals("Praecantatio", Aspect.MAGIC.getName());
        assertEquals("thaumcraft:textures/aspects/ignis.png", Aspect.FIRE.getImage().toString());
        assertFalse(Aspect.MAGIC.isPrimal());
    }

    @Test
    void duplicateTagIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Aspect("ignis", 0, "c", 1));
    }

    private static void assertComponents(Aspect compound, Aspect first, Aspect second) {
        assertSame(first, compound.getComponents()[0], compound.getTag());
        assertSame(second, compound.getComponents()[1], compound.getTag());
    }
}
