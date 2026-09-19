# FND-04 weighted aspect-type culling evidence

This note covers one normalization stage: weighted reduction of computed item aspects to seven types. FND-04 remains `ACTIVE`; attribution tables, recipe generation, scanning and smelter agreement, persistence, multiplayer, and rank-1 gameplay remain outside this slice.

## Reference basis

`REF-0012` records an independent read-only `javap -p -c` inspection of the released BETA26 jar from `REF-0002`. Its SHA-256 was rechecked as `9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f`. The shipped helper defaults to seven and copies non-null entries before repeatedly removing the lowest float-weighted type. Primals multiply by `0.9f`; each non-primal parent multiplies by `1.1f`, immediately followed by `1.05f` for each of that parent's non-primal parents, in stored order. A strict lower comparison preserves first-entry tie removal. The manager invokes culling after bonus merging, and amount capping occurs afterwards.

This is rank-2 artifact inspection, not rank-1 gameplay. No original source, bytecode, assets, or binary was retained in the repository.

## Port correction

Before this slice, computed lookup returned every aspect type. The restored public overloads return a fresh insertion-ordered list, preserve amounts including zero and negative values in direct helper copies, skip null keys, and apply the released fixed-depth float weighting. The manager now culls the fully bonus-merged list before `getObjectTags` caps each retained amount to 500.

Two deliberate safety corrections differ from the released implementation outside normal valid inputs. Negative caps throw `IllegalArgumentException` rather than looping. Minimum selection always starts from a real entry rather than the released `32767.0f` sentinel, so weights above that value cannot cause nontermination. Normal nonnegative-cap selection semantics remain aligned with the inspected release.

## TDD and automated evidence

- Production-path RED: `runGameTestServer` discovered 17 tests; the 14 existing controls passed and exactly three new tests failed at their first intended behavioral assertions. The uncapped port retained eight types, retained the post-merge MAGIC bonus as an eighth type, and kept both AIR and EARTH after capping all eight amounts instead of producing the expected seven-type survivor order. Log: `build/gametest/fnd04-culling-red-runGameTestServer.log`.
- Direct-helper RED: focused test compilation failed only because the two new `cullTags` overloads did not yet exist.
- Focused GREEN: `AspectCullingTest` passed 12 tests covering the default and explicit caps, exact tied survivor order, all discriminating parent/grandparent comparisons, source independence, null-key filtering, zero and negative amount preservation, cap zero, negative-cap rejection, null-source behavior, and weights above the released sentinel.
- Production-path GREEN: all 17 required GameTests passed. The three new tests exercise public and shared lookup, per-stack NBT container contents, bonus-before-cull ordering, cull-before-amount-cap ordering, exact survivor order and amounts, and source NBT nonmutation. Log: `build/gametest/fnd04-culling-green-runGameTestServer.log`.

## Limits

The checks establish deterministic helper and computed-lookup behavior in the port. They do not promote the slice to rank-1 gameplay evidence or complete FND-04. Container precedence and public routing remain supported separately by `REF-0009` and `REF-0010`; broader aspect attribution and downstream agreement still require their own evidence.

## Independent approval — 2026-09-19

Fresh critic `/root/fnd04_culling_critic` returned specification PASS and quality PASS, with no actionable findings. The critic inspected the actual patch and original release artifact, independently corroborating copy semantics, float weighting, strict ties, and cull-before-amount-cap ordering. The sentinel and negative-cap corrections are explicitly approved as nontermination fixes, not claims of literal reference behavior.

Independent execution: `gradlew.bat compileJava test build --rerun-tasks` exited 0 with 16/16 JUnit tests, no failures/errors/skips; `gradlew.bat runGameTestServer` exited 0 with all 17 required tests passing. Logs: `build/fnd04-culling-critic-compile-test-build.log` and `build/gametest/fnd04-culling-critic-runGameTestServer.log`. The production JAR contained 3,535 entries and no test/fixture entries. Map freshness, pipeline validation and whitespace checks passed. EULA remains false. The critic inspected the preserved RED log but did not recreate its historical pre-fix run.

Approval covers this focused repair only. FND-04 remains ACTIVE; FND-01 remains VERIFYING. Live end-to-end attribution, scanning/smelter agreement, persistence and multiplayer remain required.
