# FND-04 Aspect Culling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Owner authorized continuous builder/critic execution; independent approval must precede any commit.

**Goal:** Restore the BETA26 seven-type limit in computed item aspects, retaining the correct types by weighted amount without mutating source data.

**Architecture:** Add the missing AspectHelper cullTags overloads as pure list operations, then invoke the default at the end of getBonusTags before getObjectTags applies its existing per-type amount cap. Preserve insertion order, amounts of retained entries, and the reviewed raw/computed API split.

**Tech Stack:** Minecraft 1.20.1, Forge 47.3.0, Java 17; JUnit 5 and the existing isolated GameTest source set.

**Spec:** AGENTS.md; FND-04 workboard acceptance; fnd-04-evidence.md items 7, 13, 16; released BETA26 AspectHelper.cullTags and crafting manager call order. Source-derived behavior has already been inspected; shipped-artifact corroboration is required in this task. Full attribution/progression remains the overarching requirement.

## Global Constraints

- Work in D:/dev/thaumcraft-shobie-review on codex/fnd-04-aspect-lookup, base 7e577bc. Preserve a83af2c and 30a58dc repairs and all existing tests.
- Private development only. No ordinary runServer, EULA acceptance, publication, remote Git changes, or copying original binaries/assets/source/bytecode into the repository.
- FND-04 remains ACTIVE and FND-01 VERIFYING; no dependent work is released by this slice.
- No new dependencies, recipe/registry/entity/bonus-formula changes, Gradle redesign, or weakened existing tests.
- Fresh builder, separate fresh critic, no worker subagents or commits. Coordinator owns claims, handoff, workboard, deferred register and final commit.
- Rank-2 release inspection is not rank-1 gameplay evidence. Record runtime parity as unverified where not observed.

## Review Focus

1. Culling before adding bonuses can leave more than seven types; test a bonus that becomes the eighth and should be discarded.
2. Culling after amount cap can drop a different type; test unequal amounts above 500 with a specific expected survivor.
3. Raw amount sorting ignores primal and compound weights; direct tests must distinguish each depth and both compound parents.
4. Equal weighted values must remove the earliest remaining insertion, without sorting/reordering survivors; test exact order and source isolation.
5. The original fixed minimum sentinel can fail to select a type at large amounts and a negative cap cannot terminate; use finite deterministic behavior and cover both explicitly.

## Task 1: Restore weighted aspect-type culling and integrate computed lookup

**Files:**
- Modify src/main/java/thaumcraft/api/aspects/AspectHelper.java (two public overloads and one small weighting helper if useful).
- Modify src/main/java/thaumcraft/common/lib/crafting/ThaumcraftCraftingManager.java (getBonusTags return only).
- Create src/test/java/thaumcraft/api/aspects/AspectCullingTest.java (pure list behavior).
- Modify src/gametest/java/thaumcraft/gametest/AspectLookupGameTests.java (production-path regressions; reuse per-stack NBT container fixtures).
- Create docs/agent-pipeline/fnd-04-culling-evidence.md; update reference-catalog.md and relevant fnd-04-evidence.md paragraphs. Filesystem map only through generator if required.

**Interfaces:** Add public static AspectList cullTags(AspectList source), delegating to cullTags(source, 7), and public static AspectList cullTags(AspectList source, int cap). Null source retains ordinary NullPointerException behavior; negative cap throws IllegalArgumentException rather than looping. For a non-null list and nonnegative cap, return a fresh list containing non-null keys. Retain zero/negative amounts in direct helper copying (container positive filtering is unchanged). Repeatedly remove the lowest weighted entry until size <= cap. Stable ties remove the first encountered remaining entry. Retained entry amounts and iteration order stay unchanged. Zero cap returns an independent empty list. No alias to the source's map.

**Weight contract:** Start with the amount as float. A primal multiplies by 0.9f. A compound considers its two parents in stored order: each non-primal parent multiplies by 1.1f, immediately followed by multiplication by 1.05f for each of that parent's two non-primal parents in stored order. Do not recurse deeper or use integer/double rounding. This describes selection weights only; never alter stored amounts. Use a minimum-selection strategy that always chooses a real entry even when weights exceed 32767; do not reproduce the original nontermination bug.

- [x] Independently hash the REF-0002 released jar and inspect cullTags plus its manager call with read-only javap. Confirm default seven, float multipliers/order, strict less-than tie behavior, fresh copy, and cull-before-amount-cap ordering. Record paraphrased next reference entry (currently REF-0012), explicitly rank-2 and distinguishing the original sentinel hazard from the intended port correction. No original code/bytecode retained.
- [x] Before production edits, add production-path GameTests using existing public/shared APIs, so RED compiles without the new helper. Use NBT_CONTAINER with per-stack contents; do not change global registrations or internalMethods.

Required integration cases:
1. Six primals at 10 each, then VOID10 and LIGHT10, in that order: public and shared results must have exactly seven types, AIR absent and the other seven retained at 10. Source stack contents remain eight entries, including AIR10. Assert exact survivor order (EARTH,FIRE,WATER,ORDER,ENTROPY,VOID,LIGHT), not merely size.
2. Six primals at 4 each followed by VOID4, on a stack enchanted with Unbreaking I: computed MAGIC3 is the eighth type and has lower weight than the primals. Final public/shared result has exactly seven original types at 4 and no MAGIC. This proves culling happens after bonuses; existing enchantment tests remain intact.
3. Ordered contents AIR600, EARTH550, FIRE1000, WATER1000, ORDER1000, ENTROPY1000, VOID1000, LIGHT1000: remove EARTH before the 500 amount cap, retain AIR500 and all other six at 500; source contents retain original amounts. Capping first would instead remove AIR on a tie and must fail.

Example RED assertion on the first fixture:
```java
AspectList result = AspectHelper.getObjectAspects(stack);
helper.assertTrue(result.size() == 7, "Computed lookup must retain exactly seven aspect types");
helper.assertTrue(result.getAmount(Aspect.AIR) == 0, "First tied primal must be culled");
```
- [x] Run runGameTestServer once against old production; preserve the real failing log at build/gametest/fnd04-culling-red-runGameTestServer.log. Check failures are behavioral, not discovery/compile problems. Record only reached assertions.
- [x] Add direct JUnit coverage for the new helper. Test default seven and explicit caps, no-op copy independence, exact survivor order for tied primals, cap zero, negative-cap rejection, skipped null keys, preserved zero/negative amounts when not removed, source nonmutation, and selection at weights above the original sentinel.

Use these discriminating direct comparisons (cap 1, entries in stated order):
- AIR10 then VOID10 -> VOID10 (primal discount).
- MAGIC10 then LIGHT10 -> MAGIC10 (one compound parent).
- DARKNESS10 then MAGIC10 -> DARKNESS10 (both compound parents).
- AURA10 then MAGIC10 -> AURA10 (non-primal grandparent; omitting its weight creates a tie that removes AURA).
- MIND10 then DARKNESS10 -> MIND10 (two non-primal grandparents in the right parent: weight 12.1275 versus 12.1; missing either reverses the result).
- AIR50000, WATER60000, VOID40000 with cap2 -> AIR50000, WATER60000; do not let original sentinel nontermination enter implementation.

Keep assertions explicit, not derived from a copied algorithm. Existing Aspect singleton graph is used; do not create addon aspects or mutate global aspect definitions. If a specified graph differs from the release, report it instead of changing the expectation silently.

- [x] Implement independently from the paraphrased contract. A small float weighting helper can iterate the fixed parent depth; no general recursive framework. Copy entries into a fresh AspectList, select/remove until cap, and retain insertion order. Reject negative caps before looping. Integrate only the final getBonusTags return:
```java
return AspectHelper.cullTags(result);
```
- [x] Run focused direct JUnit tests and the GameTests to GREEN. Then run compileJava test build (one combined final run), production JAR fixture exclusion, map generator/check, pipeline validator and git diff --check. Repeat GameTests only if source changed after GREEN. Preserve logs under build/.
- [x] Write concise tracked evidence with actual RED/GREEN results, final commands/counts, reference basis and limits; full report to task-1-report.md in the SDD workspace. Distinguish normal BETA26 behavior from deliberate safety corrections for negative caps/high weights. No rank-1 gameplay or full FND-04 claim. Return for separate independent review, with no commit.

## Interface preflight and rulings

The existing AspectList uses LinkedHashMap insertion order and supports a fresh copy. getBonusTags currently returns an uncapped type list; getObjectTags then applies capAspects(...,500). The public helper and snapshot constructor already consume this same manager path. No new persistence/network boundary is introduced.

Ruling: reject negative caps and choose a minimum independently of the original 32767 sentinel. This preserves intended valid-input culling while avoiding nontermination, consistent with the owner's requirement to fix deadlocks. Cost if wrong: an addon relying on undocumented invalid-input behavior sees an exception instead of a hang; normal nonnegative caps retain reference behavior.

Self-review: one coherent missing normalization stage; all five boundary risks have explicit tests. Broader attribution, bonuses, entities, recipe generation, capacities, scanning/smelter agreement, survival, persistence and multiplayer remain separate open requirements. Owner continuous execution overrides repeated plan confirmation; owner approval-before-commit governs the review sequence.
Review outcome: independent specification PASS and quality PASS on 2026-09-19; see fnd-04-culling-evidence.md for execution results and limits. No full FND-04 completion claim.
