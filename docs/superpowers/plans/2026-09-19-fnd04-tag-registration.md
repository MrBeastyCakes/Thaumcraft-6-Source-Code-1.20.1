# FND-04 Item-Tag Registration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Continuous execution is authorized; independent approval precedes any commit.

**Goal:** Make ordinary item-tag aspect registrations effective after startup and faithful across tag reloads, preserving declaration order and direct registrations.

**Architecture:** A small ordered registration store separates direct entries from retained tag declarations and atomically replaced resolved tag entries. AspectHelper adapts Forge membership; a Forge tag-update subscriber refreshes it. Existing raw/computed lookup contracts remain intact.

**Tech Stack:** Minecraft 1.20.1, Forge 47.3.0, Java 17; JUnit 5 and isolated GameTests.

**Spec:** docs/superpowers/specs/2026-09-19-fnd04-tag-registration-design.md; AGENTS.md; FND-04 workboard and evidence items 12-16. Owner's full intended parity requirement remains binding.

## Global Constraints

- Repository D:/dev/thaumcraft-shobie-review, branch codex/fnd-04-aspect-lookup, base 2a6554d. Preserve reviewed container, public-routing and culling fixes and all existing tests.
- Private development only; no EULA acceptance, ordinary runServer, publication, remote Git changes, new dependencies, or original source/assets/bytecode copied into the repository.
- FND-04 remains ACTIVE and FND-01 VERIFYING; no dependent work is released by this slice.
- Repair ordinary String tag registration only. Complex-registration fidelity, recipe generation, NBT identity, bonus formulas, entity attribution and seed-value corrections remain separate. Keep existing complex-item controls passing.
- Fresh builder and separate fresh critic; neither spawns subagents or commits. Coordinator owns claims, workboard, handoff, deferred register and final commit.
- Rank-2 original artifact inspection and modern synthetic tests do not establish rank-1 gameplay or complete parity.

## Review Focus

1. Startup tags are unbound when declarations arrive: test real bound iron/diamond tags with expected assignments and a direct control.
2. Direct-versus-tag and overlapping-tag ordering can reverse on rebuild: unit tests cover both directions and reload preserves sequence numbers.
3. Reload removal can leave stale entries or erase direct assignments: a real reload round trip must reveal the older direct fallback and remove a newly added unregistered member again.
4. Shared AspectLists can leak changes between rules/members: snapshot inputs and copy per resolved member, while preserving direct registration's existing semantics.
5. Integrated client packets can duplicate static rebuilds: test event-side selection without replacing production registries and exercise actual server tag events in the reload test.

## Task 1: Restore deferred ordinary tag registration with reload-safe resolution

**Files:**
- Create src/main/java/thaumcraft/api/aspects/AspectRegistrationStore.java (package-private domain store).
- Modify src/main/java/thaumcraft/api/aspects/AspectHelper.java (delegate item registry to store; Forge tag-membership adapter; preserve entity/culling paths).
- Modify src/main/java/thaumcraft/api/ThaumcraftApi.java (ordinary String registerObjectTag overload only).
- Create src/main/java/thaumcraft/common/lib/events/AspectTagEvents.java (Forge TagsUpdatedEvent subscriber).
- Create src/test/java/thaumcraft/api/aspects/AspectRegistrationStoreTest.java (isolated store instances; no global registry resets).
- Create src/gametest/java/thaumcraft/gametest/AspectTagGameTests.java and test-only item tag JSON under src/gametest/resources/data/thaumcraft/tags/items/fnd04_reload.json. Additional test-only event/lifecycle helper may stay nested in that class.
- Create docs/agent-pipeline/fnd-04-tag-registration-evidence.md; update relevant fnd-04-evidence.md and reference-catalog.md; filesystem-map.md through generator only.

**Interfaces:** Package-private store methods registerDirect(String id, AspectList), registerTag(String tagId, AspectList, Function<String,List<String>> resolver), refreshTags(Function<String,List<String>> resolver), get(String id), size(), clear(). Equivalent small signatures may be adjusted with explanation if needed for correctness; no generic framework. Direct entries retain their existing list reference. Tag rules snapshot the input and resolved members receive distinct copies. Retain unbound/empty rules. Each registration has an increasing sequence; refresh never changes sequence. get chooses the newest direct or resolved tag assignment. size is union cardinality. clear removes all rules/entries and resets its own sequence. A completed replacement resolved map is published as one assignment.

AspectHelper keeps public getObjectAspects computed, getRegisteredObjectAspects raw, existing item/ResourceLocation registration signatures, object-tag count and clearTags semantics. Introduce registerObjectTagForItemTag(ResourceLocation, AspectList) and refreshTagRegistrations() for the adapter/event. Invalid/null tag input or null tag-list remains a no-op; do not change ItemStack null-list normalization. The ordinary API String overload validates/parses and delegates instead of dropping unbound tags. Do not alter ConfigAspects values or move/replay its full init/event.

- [x] Inspect actual startup calls and pinned Forge tag API first. Add two independent real-item startup GameTests plus an explicit direct control BEFORE production edits. First prove forge:ingots/iron and forge:gems/diamond are bound and contain the corresponding vanilla items; then assert raw iron METAL15 and diamond CRYSTAL15/DESIRE15 (no extra types), and identical computed values with no ENTROPY fallback. Control REDSTONE_ORE remains raw/computed EARTH5/ENERGY15. Example:
```java
ItemStack iron = new ItemStack(Items.IRON_INGOT);
AspectList raw = AspectHelper.getRegisteredObjectAspects(iron);
helper.assertTrue(raw != null && raw.size() == 1 && raw.getAmount(Aspect.METAL) == 15,
        "Bound iron tag must supply raw METAL 15 after startup");
```
- [x] Run runGameTestServer and preserve build/gametest/fnd04-tags-red-runGameTestServer.log. Reproduction must be behavioral with bound tag membership proven, not a missing tag/compile/discovery failure. If the hypothesis is disproved, report and stop before production edits rather than inventing a fix. Send root a concise RED update, then continue if confirmed.
- [x] Independently hash REF-0002 jar and inspect original AspectEventProxy ordinary category expansion and selected ConfigAspects seeds (ingotIron METAL15; gemDiamond CRYSTAL15/DESIRE15) via read-only javap. Record next free REF (currently REF-0013) with paraphrased scope and limits. Inspect pinned Forge47.3.0 TagsUpdatedEvent source/API. No original code or bytecode retained.
- [x] Add isolated-store JUnit tests before implementation. Required cases: an empty resolver at registration followed by membership after refresh; tag registered after binding applies immediately; direct-before-tag and tag-before-direct; two overlapping tag rules with reversed member enumeration; refresh does not change precedence; member removal restores older direct or absence and addition receives the rule; empty/unknown tags remain pending; input-list mutation does not alter a rule; member-list mutation does not change another member or the stored declaration (refresh restores declaration); union counts; clear followed by refresh cannot resurrect rules. Use explicit values AIR1/EARTH2/WATER4 and isolated instances, not implementation-derived expected values.

Example ordering assertion:
```java
store.registerDirect("test:a", new AspectList().add(Aspect.AIR, 1));
store.registerTag("test:group", new AspectList().add(Aspect.EARTH, 2), resolver);
assertEquals(2, store.get("test:a").getAmount(Aspect.EARTH));
// Change resolver membership to remove a, then refresh.
assertEquals(1, store.get("test:a").getAmount(Aspect.AIR));
```

- [x] Implement the small store and adapter from the contract. Use retained tag declarations and registration order; do not patch iron/diamond explicitly or simply replay all ConfigAspects seeds. The Forge event handler refreshes only when event.shouldUpdateStaticData() is true. No new packet, recipe cache or fallback aspect. Unit-test side selection through a narrow pure decision boundary or a test-local observer where needed; do not expose broad production test seams. Read real Forge memberships; no global registry replacement.
- [x] Add a real asynchronous reload GameTest using unique fixture items A/B/C and tag thaumcraft:fnd04_reload, default members A/C. Register A direct AIR1, then tag EARTH2, then C direct WATER4 during normal fixture registration; initial A=EARTH2, B absent, C=WATER4. Keep fixture registration in actual API order. After an actual server datapack reload replacing members with B/C, assert A=AIR1, B=EARTH2, C=WATER4. Unload the temporary pack and reload back: A=EARTH2, B absent, C=WATER4. Verify raw presence and computed values where registered, exact types/counts, plus iron METAL15 remains correct throughout. Do not call refreshTagRegistrations directly in this test.

Use a uniquely named temporary datapack only under the GameTest world's datapacks directory, with pack_format15 and replace:true in its test tag. Rescan the pack repository before selecting it. Chain reloadResources futures without blocking/joining on the server thread; assertions/failure delivery run on the server/GameTest thread. Use an appropriate bounded timeout (e.g. 1200 ticks). Restore the originally selected pack IDs, wait for restoration, then remove only test-owned files. Cleanup must occur on success/failure when possible and must not delete unrelated packs or touch run/ saves. Record any cleanup limitation. A test-only built-in optional pack is acceptable if simpler and equivalent; explain the choice.

- [x] Run focused unit tests and GameTests to GREEN. The actual reload test must prove the event path and changed membership; manually refreshing a helper is insufficient. Record exact stage observations. Preserve all previous 17 GameTests and16 JUnit controls.
- [x] Final combined compileJava test build; final GameTests only if source changed since GREEN; production JAR excludes all gametest classes, fixtures, fnd04_ tags and test pack data. Run map generator/check, pipeline validator and git diff --check. Keep logs under build/. No ordinary server or EULA action.
- [x] Write concise tracked evidence and full task-1-report.md in the SDD workspace: actual RED first failures, GREEN test counts/reload stages, selected BETA26 reference, modern lifecycle adaptation, commands/logs, file scope and remaining gaps. Return for independent review without committing. Do not claim full attribution, complex tag registration, live rank-1 parity or multiplayer acceptance.

## Preflight and rulings

Existing commonSetup calls ConfigAspects.init; ordinary API String registration expands only currently bound tags. No production TagsUpdatedEvent listener was found. Iron/diamond seeds use tags, while redstone ore also has an explicit item seed. The raw helper remains registry-ID based; NBT identity stays open. Existing complex item API reads the raw helper and writes explicit entries, so its prior tests must remain intact. Forge's event guard avoids duplicate integrated-client processing while allowing remote-client tag synchronization.

Ruling: preserve declaration order when adapting category expansion to reloadable tags. Cost if wrong: a later tag can override an earlier direct assignment where an addon expected direct specificity to win; this follows actual registration order rather than inventing a new priority.

Self-review: startup and real reload tests cover the observed failure and lifecycle; isolated tests cover ordering/ownership/clear/count semantics. No other gameplay subsystem is silently changed. Owner's continuous execution supersedes repeated skill approval gates; review-before-commit remains mandatory.

Completion checkpoint: builder implementation plus one coverage fix round passed independent specification and quality review. Tests28JUnit/21GameTests; exact live membership asserted at all3reload stages. Approved scope adjustment: A/B/C fixtures share existing AspectLookupGameTests.TestItems after reported listener conflict; three lifecycle guard tests live in src/test/java/thaumcraft/common/lib/events/AspectTagEventsTest.java. FullFND04 remainsACTIVE.
