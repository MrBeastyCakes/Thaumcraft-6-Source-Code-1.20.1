package thaumcraft.api.aspects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class AspectRegistrationStoreTest {
    private static final String A = "test:a";
    private static final String B = "test:b";
    private static final String C = "test:c";

    @Test
    void pendingTagResolvesAfterRefresh() {
        MutableResolver resolver = new MutableResolver();
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        assertNull(store.get(A));

        resolver.put("test:group", A);
        store.refreshTags(resolver);

        assertAmount(store.get(A), Aspect.EARTH, 2);
    }

    @Test
    void tagRegisteredAfterBindingAppliesImmediately() {
        MutableResolver resolver = new MutableResolver().put("test:group", A);
        AspectRegistrationStore store = new AspectRegistrationStore();

        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);

        assertAmount(store.get(A), Aspect.EARTH, 2);
    }

    @Test
    void newestRegistrationWinsInBothDirections() {
        MutableResolver resolver = new MutableResolver().put("test:group", A);
        AspectRegistrationStore directThenTag = new AspectRegistrationStore();
        directThenTag.registerDirect(A, aspects(Aspect.AIR, 1));
        directThenTag.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        assertAmount(directThenTag.get(A), Aspect.EARTH, 2);

        AspectRegistrationStore tagThenDirect = new AspectRegistrationStore();
        tagThenDirect.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        tagThenDirect.registerDirect(A, aspects(Aspect.AIR, 1));
        assertAmount(tagThenDirect.get(A), Aspect.AIR, 1);
    }

    @Test
    void overlappingTagsUseRegistrationOrderNotMemberOrder() {
        MutableResolver resolver = new MutableResolver()
                .put("test:first", A, B)
                .put("test:second", B, A);
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerTag("test:first", aspects(Aspect.AIR, 1), resolver);
        store.registerTag("test:second", aspects(Aspect.WATER, 4), resolver);

        assertAmount(store.get(A), Aspect.WATER, 4);
        assertAmount(store.get(B), Aspect.WATER, 4);

        resolver.put("test:first", B, A).put("test:second", A, B);
        store.refreshTags(resolver);
        assertAmount(store.get(A), Aspect.WATER, 4);
        assertAmount(store.get(B), Aspect.WATER, 4);
    }

    @Test
    void removalRevealsDirectAndAdditionReceivesRule() {
        MutableResolver resolver = new MutableResolver().put("test:group", A, C);
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerDirect(A, aspects(Aspect.AIR, 1));
        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        assertAmount(store.get(A), Aspect.EARTH, 2);
        assertAmount(store.get(C), Aspect.EARTH, 2);

        resolver.put("test:group", B, C);
        store.refreshTags(resolver);

        assertAmount(store.get(A), Aspect.AIR, 1);
        assertAmount(store.get(B), Aspect.EARTH, 2);
        assertAmount(store.get(C), Aspect.EARTH, 2);
    }

    @Test
    void removalWithoutFallbackBecomesAbsentAndUnknownTagRemainsPending() {
        MutableResolver resolver = new MutableResolver().put("test:group", A);
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerTag("test:unknown", aspects(Aspect.WATER, 4), resolver);
        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        resolver.put("test:group");
        store.refreshTags(resolver);
        assertNull(store.get(A));

        resolver.put("test:unknown", B);
        store.refreshTags(resolver);
        assertAmount(store.get(B), Aspect.WATER, 4);
    }

    @Test
    void tagDeclarationAndEachResolvedMemberOwnCopies() {
        MutableResolver resolver = new MutableResolver().put("test:group", A, B);
        AspectRegistrationStore store = new AspectRegistrationStore();
        AspectList input = aspects(Aspect.EARTH, 2);
        store.registerTag("test:group", input, resolver);
        input.add(Aspect.WATER, 4);
        store.get(A).add(Aspect.AIR, 1);

        assertEquals(0, store.get(B).getAmount(Aspect.AIR));
        assertEquals(0, store.get(B).getAmount(Aspect.WATER));

        store.refreshTags(resolver);
        assertEquals(0, store.get(A).getAmount(Aspect.AIR));
        assertEquals(0, store.get(A).getAmount(Aspect.WATER));
        assertAmount(store.get(A), Aspect.EARTH, 2);
    }

    @Test
    void directRegistrationRetainsListAndUnionSizeCountsUniqueIds() {
        MutableResolver resolver = new MutableResolver().put("test:group", A, B);
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerDirect(A, aspects(Aspect.AIR, 1));
        AspectList direct = aspects(Aspect.WATER, 4);
        store.registerDirect(C, direct);
        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);

        assertSame(direct, store.get(C));
        assertEquals(3, store.size());
    }

    @Test
    void clearPreventsRefreshFromResurrectingRulesAndResetsSequence() {
        MutableResolver resolver = new MutableResolver().put("test:group", A);
        AspectRegistrationStore store = new AspectRegistrationStore();
        store.registerTag("test:group", aspects(Aspect.EARTH, 2), resolver);
        store.clear();
        store.refreshTags(resolver);
        assertNull(store.get(A));
        assertEquals(0, store.size());

        store.registerDirect(A, aspects(Aspect.AIR, 1));
        store.registerTag("test:group", aspects(Aspect.WATER, 4), resolver);
        assertAmount(store.get(A), Aspect.WATER, 4);
    }

    private static AspectList aspects(Aspect aspect, int amount) {
        return new AspectList().add(aspect, amount);
    }

    private static void assertAmount(AspectList list, Aspect aspect, int amount) {
        assertEquals(amount, list.getAmount(aspect));
    }

    private static final class MutableResolver implements Function<String, List<String>> {
        private final Map<String, List<String>> members = new HashMap<>();

        MutableResolver put(String tag, String... ids) {
            members.put(tag, new ArrayList<>(List.of(ids)));
            return this;
        }

        @Override
        public List<String> apply(String tag) {
            return members.getOrDefault(tag, List.of());
        }
    }
}
