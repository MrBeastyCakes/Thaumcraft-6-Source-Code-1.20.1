# FND-04 Public Aspect Lookup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Owner already authorized continuous builder/critic execution; commit only after independent approval.

**Goal:** Restore the public item-aspect API path to the shared computed lookup so item scanning and AspectList snapshots receive container contents and existing bonuses/caps.

**Architecture:** AspectHelper currently conflates raw registration reads with public computed reads. Retain its current registry read under getRegisteredObjectAspects(ItemStack), route getObjectAspects(ItemStack) through ThaumcraftApi.internalMethods, and move manager/registration-internal callers to the raw read. This avoids recursion and preserves current registration behavior while restoring the BETA26 API routing contract.

**Tech Stack:** Minecraft 1.20.1, Forge 47.3.0, Java 17; existing Gradle, JUnit, separate gameTest source set.

**Spec:** AGENTS.md; docs/agent-pipeline/parity-workboard.md FND-04; docs/agent-pipeline/fnd-04-evidence.md items 11, 16, 24; released BETA26 API routing corroboration below. Full intended parity remains required beyond this slice.

## Global Constraints

- Private development; no EULA acceptance, ordinary runServer, publication, remote Git changes, or copying original source/assets/bytecode into the repository.
- Base 2e30905 on codex/fnd-04-aspect-lookup; retain reviewed first slice 30a58dc and all prior behavior tests.
- FND-04 remains ACTIVE; FND-01 remains VERIFYING; no dependent item released by this slice.
- Preserve existing recipe-generation and registration behavior as controls, not as claims of parity. Recipe-generated ENTROPY1 is a known stub, and complex registration remains separate follow-up scope.
- Fresh builder and fresh independent critic; no builder/reviewer subagents or commits. Coordinator owns claims, handoff, workboard and commit.

## Review Focus

1. A public helper redirect can recurse via the crafting manager. A normal registered-item lookup must terminate with expected values.
2. An unknown item can wrongly appear registered once computation returns fallback tags. Assert exists remains false after computed reads.
3. Register-complex callers can bake generated/property bonuses into seed tags. Pin current raw-registration behavior for registered and previously unregistered fixture items.
4. AspectList snapshots can alias registered or container-owned data. Mutate snapshot and confirm subsequent public/shared lookup and seed remain unchanged.
5. Empty authoritative containers can remain scannable from their base tags. Exercise actual ScanGeneric.checkThing with a registered empty container versus a filled stack, alongside null/empty stack controls.

## Task 1: Separate raw registration reads and restore public API routing

**Files:**
- Modify src/main/java/thaumcraft/api/aspects/AspectHelper.java (raw/computed split and accurate Javadoc)
- Modify src/main/java/thaumcraft/common/lib/crafting/ThaumcraftCraftingManager.java (direct base-read call only)
- Modify src/main/java/thaumcraft/api/ThaumcraftApi.java (exists and registerComplexObjectTag raw-read calls only)
- Modify src/gametest/java/thaumcraft/gametest/AspectLookupGameTests.java (preserve old seed checks through explicit raw getter; new production-facing behavioral tests/fixtures)
- Create docs/agent-pipeline/fnd-04-public-lookup-evidence.md
- Update docs/agent-pipeline/reference-catalog.md and the relevant subset of fnd-04-evidence.md; filesystem-map.md only through generator if it changes.

**Interfaces:** Public AspectHelper.getObjectAspects(ItemStack) returns computed aspects through the established IInternalMethodHandler. New AspectHelper.getRegisteredObjectAspects(ItemStack) contains the exact previous raw-map read, including existing null/empty behavior. ThaumcraftCraftingManager.getObjectTags calls only the raw getter for its initial base lookup. ThaumcraftApi.exists and both registerComplexObjectTag raw reads use the raw getter, preserving existing behavior until separately reviewed. Entity lookups unchanged.

- [x] Inspect actual callsites, commonSetup handler initialization, and release references before editing. Root observed released AspectHelper.getObjectAspects bytecode offsets 0-9 delegates to ThaumcraftApi.internalMethods; released AspectList(ItemStack) offsets 15-19 calls that helper then snapshots entries. Independently corroborate helper, constructor and InternalMethodHandler routing using REF-0002 released jar and hash. Record paraphrased results as next unused REF entry (currently REF-0010), rank-2 inspection only. No original code/bytecode retained.
- [x] Add regression tests BEFORE production changes and execute the existing runGameTestServer. New RED cases must compile against existing API; do not introduce calls to the not-yet-added raw accessor until implementing the split. Keep old tests unchanged for RED. Use existing real VIS_CRYSTAL_AIR and isolated GameTest fixtures, no production registry replacement or global internalMethods replacement.

Load-bearing regression example:
```java
ItemStack crystal = new ItemStack(ModItems.VIS_CRYSTAL_AIR.get());
AspectList publicResult = AspectHelper.getObjectAspects(crystal);
helper.assertTrue(publicResult.getAmount(Aspect.AIR) == 1, "Public lookup must use contained AIR 1");
helper.assertTrue(publicResult.getAmount(Aspect.CRYSTAL) == 0, "Base CRYSTAL must not leak through public lookup");
```
Snapshot regression example:
```java
AspectList snapshot = new AspectList(new ItemStack(ModItems.VIS_CRYSTAL_AIR.get()));
helper.assertTrue(snapshot.getAmount(Aspect.AIR) == 1, "Stack constructor must snapshot computed AIR 1");
snapshot.add(Aspect.AIR, 99);
helper.assertTrue(AspectHelper.getObjectAspects(new ItemStack(ModItems.VIS_CRYSTAL_AIR.get())).getAmount(Aspect.AIR) == 1,
        "Snapshot edits must not mutate future lookups");
```

Required additional tests: existing NBT_CONTAINER base ORDER11 but empty queried contents must make new ScanGeneric().checkThing(null, emptyStack) false; filled contents make it true (ItemStack path does not dereference player). Compare public/shared values for the enchanted two-stack fixture including MAGIC3 and cap/positive-entry behavior from existing fixtures. Public null and ItemStack.EMPTY yield empty computed lists with initialized handler. Preserve exists true for registered crystal and false for a dedicated unregistered fixture after public computation. Pin registerComplexObjectTag's existing no-generated-bonus behavior using test-only items uniquely owned by those tests, for both absent and present raw registrations; do not enshrine it as BETA26-complete. Avoid order dependence or shared fixture mutation. Use meaningful assertion messages and exact expected results rather than only comparing two possibly wrong paths.

- [x] Retain real RED log under build/gametest/fnd04-public-red-runGameTestServer.log; record actual first failures, not assertions skipped after them. Then implement the minimal routing split:
```java
public static AspectList getObjectAspects(ItemStack stack) {
    return ThaumcraftApi.internalMethods.getObjectAspects(stack);
}
```
Move previous getter body verbatim to public static AspectList getRegisteredObjectAspects(ItemStack stack) with raw-contract Javadoc. Change manager and three registration-internal callers to getRegisteredObjectAspects. Keep registration writes, entity behavior, generateTags, container precedence, bonuses and cap logic otherwise unchanged. Update existing GameTest seed-read assertions to the new explicit raw getter so they still verify base AIR5/CRYSTAL5 rather than weakening them.
- [x] Run GameTests to GREEN, then combined compileJava test build, final runGameTestServer once if source changed since GREEN, production-JAR isolation (no gametest package, test classes or fnd04_ entries), map updater/check, pipeline validator and git diff --check. Preserve full logs in build/. Reuse established harness; no Gradle redesign or ordinary server/EULA.
- [x] Write tracked concise reference/RED/GREEN/command/count/limits evidence plus detailed report in plan workspace task-1-report.md. Full FND-04, rank-1 reference gameplay, research progression, smelter integration, registry identity/recipe generation/complex registration fidelity, persistence and multiplayer remain pending. Root handles coordination and approval records; do not claim scanning progression complete from the eligibility check.
- [x] Return to coordinator for separate independent review before any commit.

## Interface preflight

| Boundary | Produced/consumed contract | Check |
| --- | --- | --- |
| Public helper / internal handler / manager | Computed lookup delegates once; manager uses raw read | No recursion; real crystal and null/empty tests |
| API registration / raw helper | Existing raw registration semantics preserved | exists and two complex-registration controls |
| ScanGeneric / helper | Existing item eligibility consumes corrected aspects | Empty registered container negative, filled positive |
| AspectList / helper | Snapshot of computed aspects | Exact values and subsequent lookup nonmutation |
| Reference / documentation | Released routing is inspection evidence | Rank-1, whole-item and consumer progression gaps stay open |

Self-review: one cohesive FND-04 repair; all five identified boundary risks have explicit tests. No entity/recipe/progression repair is silently bundled. Continuous execution is already authorized by the owner; no additional approval needed to begin this reversible implementation slice.

Review outcome: fresh independent critic passed specification and quality on 2026-09-19. See fnd-04-public-lookup-evidence.md for verification and limits. Full FND-04 remains active.
