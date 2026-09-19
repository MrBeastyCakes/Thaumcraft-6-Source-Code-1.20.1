# FND-04 recipe-derived attribution evidence

## Scope and reference

This slice replaces the unconditional ENTROPY1 generator with query-time recipe attribution through the existing public AspectHelper -> InternalMethodHandler -> ThaumcraftCraftingManager route. FND-04 remains ACTIVE; this does not complete seed/NBT source identity, bonus, complex registration, consumer, survival or multiplayer parity.

Arithmetic and control flow follow [REF-0015](fnd-04-recipe-reference-evidence.md) and the [approved modern design](../superpowers/specs/2026-09-19-fnd04-recipe-attribution-design.md). No original implementation or assets were copied. The coordinator also recorded [REF-0016](fnd-04-live-derived-items-2026-09-19.md), showing original oak planks PLANT3 and stick PLANT1. The tested modern oak-planks value agrees with that narrow displayed-value observation; no original mechanism, survival or consumer claim follows.

## Implementation

- RecipeAspectSource captures the current server manager/registries, otherwise calls the client provider anew. ClientAspectRecipeSource reads the current connection inside the callback; disconnect and replacement cannot retain a previous connection. Only the existing client event installs it. Common source/resolver classes import no client classes.
- Each public query owns one resolver. It lazily builds an item-output index only when derivation is required, merging supported real API-catalog recipes with loaded recipes. Loaded IDs win duplicates; full IDs sort once per query. Explicit/contained lookups avoid the index entirely. There is no generated registration and no persistent result/index cache.
- Crucible, infusion, and ordinary/arcane crafting retain reference priority, first ingredient alternatives, refused remainder overdraw, strict rounding, essentia/Vis additions, positive minimum and first ties. A private copied 3x3 inventory implements both modern container interfaces and is passed to actual remaining-items callbacks. Empty higher-priority results suppress crafting fallback. A missing catalyst alternative is invalid; an existing unseeded catalyst computing empty remains valid.
- A copied, count-normalized serialized stack is the full value history/memo key. Damage is normalized only for damageable items; other NBT is retained. Completed base lists are copied into and out of per-query memo before history rechecking. Direct cycle/depth cutoffs are not memoized. The history is cumulative across candidates; the 100th distinct key is not traversed. Recursive ingredient/remainder lookup keeps the same resolver and existing bonus/container/culling/cap behavior.

Deliberate adaptations and costs: deterministic full-ID ordering replaces unspecified legacy HashMap order; loaded datapacks win duplicate catalog IDs; per-query memo replaces persistent generated registrations; malformed/dynamic/unsupported recipes are skipped safely. First alternatives and cumulative history are retained. Repeated fresh derivation costs more than a persistent generated cache and may differ from old stale-cache order artifacts.

## Behavioral RED

Before production edits, two new public GameTests loaded a real JSON EARTH4 -> output1 recipe and an unseeded no-recipe item. The recipe test first asserted the actual RecipeManager ID, first ingredient and exact output/count. Both failed with ENTROPY1 instead of EARTH3/empty. All original21 controls passed (23 total, exactly2 failures).

Logs retained under ignored build output:

- `build/gametest/fnd04-recipes-red-runGameTestServer.log`
- `build/gametest/fnd04-recipes-red-gradle.log`

The first extended integration run exposed test-setup errors: an incorrect arcane serializer ID and a leftover old fallback-size assertion. Both were corrected in the fixtures/assertion, with no production workaround; intermediate logs are `fnd04-recipes-integration-*`. The next run passed33 GameTests and6 focused JUnit tests (`fnd04-recipes-second-*`).

## Final automated evidence

Java17: Eclipse Adoptium17.0.18.8-hotspot. Branch `codex/fnd-04-aspect-lookup`, starting checkpoint `dc17c32`.

`./gradlew.bat compileJava test build runGameTestServer` passed in45s. Full JUnit suite:34/34, including all original28 and five math cases plus one provider-lifecycle case. GameTests:36/36, including all original21 with only the authorized ENTROPY placeholder expectation replaced by empty. Complex-registration controls remain intact.

- `build/gametest/fnd04-recipes-green-gradle.log`
- `build/gametest/fnd04-recipes-green-runGameTestServer.log`
- `build/test-results/test/TEST-*.xml`

Coverage in RecipeAspectGameTests:

| Behavior | Non-vacuous check / expected result |
| --- | --- |
| Loaded shapeless and shaped | Recipe ID/input/output validated; EARTH3 and EARTH6 |
| Division and rounding | EARTH4/count2 ->1; EARTH5/count4 ->1; EARTH1 ->empty |
| First alternative | First EARTH4 retained over second EARTH1 that would truncate to zero |
| Actual remainders | Real crafting callback returns METAL3; both METAL8/count2 and short METAL2 -> METAL1 |
| Arcane | Loaded Vis30, unused AIR50 crystals; EARTH1+MAGIC2; odd Vis3 ->MAGIC1; real remainder ->METAL3+MAGIC1 |
| Crucible/infusion | MAGIC3 catalyst +required9/count3 ->MAGIC4, FIRE8 disappears; center3+component5+required16/count2 ->MAGIC5 |
| Priority/minimum/ties | Crucible before infusion before crafting; both empty higher categories block fallback; smallest positive and first ID tie |
| Recursion/memo | Chain, repeated siblings, asymmetric cycle with exit; combined A/B query pins earlier completed B value |
| Cumulative100 bound | 99 distinct derived nodes succeed, 100 fail;98 empty candidate inputs consume budget before a valid final input |
| Identity/ownership | Recursive NBT distinction gives EARTH5; count/damage normalization gives EARTH3; caller/ingredient stacks and lists remain unchanged |
| Direct/late seeds | Registered FIRE11 output wins; seed replacement changes derived EARTH3 to WATER6 in next query |
| Legacy catalog | Real crucible chain, infusion, arcane; loaded duplicate ID wins; owned entries removed in finally |
| Invalid/fake/cap | Fake catalog ignored; missing catalyst skips to valid fallback; large essentia capped500 and copy isolation |
| Vanilla control | Actual oak-planks output4 and first input seed PLANT20 checked, output raw-unregistered; exact PLANT3 |

RecipeAspectMathTest independently pins all REF0015 numerical examples, strict .75 edge, refused overdraw, integer odd/even Vis,500cap, invalid output counts and copy isolation. RecipeAspectSourceTest exercises no connection ->first manager ->replacement manager ->no connection through the actual provider seam; it does not launch a client or replace global Minecraft registries.

The existing single real reload test now checks live RecipeManager input/output and public derived aspects at EARTH3 ->WATER6 ->EARTH3 stages. Replacement JSON lives in its already-owned UUID datapack. Original tag membership A/C ->B/C ->A/C, direct precedence and iron METAL15 assertions remain. All owned packs were removed; no second asynchronous reload was introduced.

## Cost, packaging and limits

Final run measured100 repeated sibling queries at187.926ms and1000 direct seed queries at4.0068ms on this machine, including test assertion overhead. These are diagnostics, not time assertions or performance acceptance. Source review confirms one source capture/index sort per derived query, no recursive registry scans, item-indexed candidates, completed-value memo reuse and at most99 traversed distinct generated identities. Wide candidate scans still cost work; no cross-query cache is claimed.

Production `build/libs/thaumcraft-6.2.0.jar` was inspected by ZIP path and byte-content scan:3545 entries, no test classes/IDs/recipes/structures/packs. `run/eula.txt` remains `eula=false`. No ordinary dedicated server, original client, remote client, dependency change, commit or release was performed by this builder.

Dedicated GameTest startup proves the common implementation loads without client runtime. The actual client event hook and connection access are source-checked and the common provider lifecycle has a unit test; real remote-client reload/reconnect and multiplayer remain open.

## Repair record and independent review

Round-1 independent review returned SPEC FAIL / QUALITY FAIL scoped to two defects. R1: the arithmetic helper's addition did remove-then-add, re-inserting an existing key at the tail of the LinkedHashMap-backed AspectList and flipping the seven-type culling tie-break (a tied-lowest aspect that should be culled first survived instead). R2: the direct generation boundary (RecipeAspectResolver.generate, reached by public ThaumcraftCraftingManager.generateTags) never consulted explicit registrations, so a seeded item could return empty or a competing recipe-derived amount through that entry. Both are repaired: addition updates the existing key in place with the long-clamp retained, and the direct generator returns a capped, owned copy of an explicit registration before any recipe indexing (no bonuses, no persistent registration, no aliasing).

RED-first regressions were added and observed failing on the pre-fix code: the unit test positiveAdditionsKeepInsertionOrderForCullingTies (order after essentia and arcane, amounts, copy isolation), plus GameTests positiveAdditionKeepsOrderForCullingTie (real crucible fixture fnd04_recipe_cull_order over a seven-type seeded catalyst; asserts the cull survivor is WATER 2 with AIR culled and exact survivor amounts) and directGenerationHonorsRegistrationAndOwnsCopies (FIRE 11 seed with a competing recipe, caller-mutation isolation, store intact, unseeded EARTH 3 control, no persisted registration). Pre-fix evidence: build/gametest/fnd04-repair-red-runGameTestServer.log (38 tests running, exactly the two new tests failing with the defect signatures) and build/gametest/fnd04-repair-red-unit-RecipeAspectMathTest.xml. Post-fix evidence: 35 unit tests across 6 classes and 38/38 GameTests, with build/gametest/fnd04-repair-green-runGameTestServer.log recording 'All 38 required tests passed'.

A fresh independent critic re-ran both gates and a 100k-trial equivalence probe over the repaired addition (identical value maps for every reachable input; the only behavioral delta is the intended insertion-slot preservation) and returned SPEC PASS / QUALITY PASS, scoped to the repair batch. Committed as `c6c7b1b`. Existing unrelated mod/Gradle warnings are present in logs; the final run has no failed tests or recipe parsing errors. Independent critic approval (repair batch) passed and the slice is committed as `c6c7b1b`; remote-client/multiplayer and consumer-level runtime acceptance remain open.
