package thaumcraft.api.aspects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class AspectRegistrationStore {
    private final Map<String, Assignment> direct = new HashMap<>();
    private final List<TagRule> tagRules = new ArrayList<>();
    private volatile Map<String, Assignment> resolvedTags = Map.of();
    private long sequence;

    void registerDirect(String id, AspectList aspects) {
        if (id == null || aspects == null) {
            return;
        }
        direct.put(id, new Assignment(++sequence, aspects));
    }

    void registerTag(String tagId, AspectList aspects, Function<String, List<String>> resolver) {
        if (tagId == null || aspects == null || resolver == null) {
            return;
        }
        tagRules.add(new TagRule(++sequence, tagId, aspects.copy()));
        refreshTags(resolver);
    }

    void refreshTags(Function<String, List<String>> resolver) {
        if (resolver == null) {
            return;
        }
        Map<String, Assignment> replacement = new HashMap<>();
        for (TagRule rule : tagRules) {
            List<String> members = resolver.apply(rule.tagId());
            if (members == null) {
                continue;
            }
            for (String member : members) {
                if (member == null) {
                    continue;
                }
                Assignment current = replacement.get(member);
                if (current == null || rule.sequence() > current.sequence()) {
                    replacement.put(member, new Assignment(rule.sequence(), rule.aspects().copy()));
                }
            }
        }
        resolvedTags = replacement;
    }

    AspectList get(String id) {
        Assignment directAssignment = direct.get(id);
        Assignment tagAssignment = resolvedTags.get(id);
        if (directAssignment == null) {
            return tagAssignment == null ? null : tagAssignment.aspects();
        }
        if (tagAssignment == null || directAssignment.sequence() > tagAssignment.sequence()) {
            return directAssignment.aspects();
        }
        return tagAssignment.aspects();
    }

    int size() {
        Set<String> ids = new HashSet<>(direct.keySet());
        ids.addAll(resolvedTags.keySet());
        return ids.size();
    }

    void clear() {
        direct.clear();
        tagRules.clear();
        resolvedTags = Map.of();
        sequence = 0;
    }

    private record Assignment(long sequence, AspectList aspects) {
    }

    private record TagRule(long sequence, String tagId, AspectList aspects) {
    }
}
