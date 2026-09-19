# FND-04 public aspect lookup slice evidence

This note covers the public item-aspect routing boundary and the preservation of the port's current raw registration semantics. FND-04 remains `ACTIVE`; full attribution fidelity, recipe generation, progression, smelter integration, registry identity, persistence, multiplayer agreement, and rank-1 gameplay confirmation remain pending.

## Reference basis

- `REF-0010` records a read-only inspection of the released BETA26 jar from `REF-0002`, whose SHA-256 was independently rechecked as `9425f8643581b27ff8845b087c8bc6fc10425a32942f1a3f0e265ce6b38f7b5f`.
- The shipped public `AspectHelper.getObjectAspects` delegates through the configured internal handler, the handler delegates item lookup to `ThaumcraftCraftingManager.getObjectTags`, and `AspectList(ItemStack)` snapshots the public helper result into its own entries.
- This is rank-2 release-artifact inspection. It is not rank-1 observed gameplay, and no original source, bytecode, assets, or binaries were copied into the repository.

## Port defect and correction

Before this slice, the public helper returned only the direct registry-map value. Public consumers therefore bypassed container contents, generated fallback, bonuses, filtering, and caps even though the shared crafting lookup already computed them.

The corrected public helper delegates once to the initialized `IInternalMethodHandler`. A new `getRegisteredObjectAspects` method contains the previous raw map read, including its null/empty/unregistered behavior. The crafting manager uses that raw method for its initial base lookup, which prevents recursion. `ThaumcraftApi.exists` and both raw reads inside `registerComplexObjectTag` also use the raw method, preserving the port's current registration behavior for later parity work rather than persisting recipe fallback or property/enchantment bonuses.

Entity lookup, registration writes, generated-tag logic, container precedence, bonuses, and caps were otherwise unchanged.

## TDD and automated evidence

- RED against old production code: `./gradlew.bat runGameTestServer` exited 1 after launching 14 tests. Six required tests failed at their first relevant assertions: public/shared exact values reported no contained FIRE; public null lookup returned null; the crystal public lookup returned registered AIR 5 instead of contained AIR 1; the complex-registration fixture did not expose computed MAGIC 3 through the old public helper; the empty registered container remained scan-eligible; and the stack constructor captured registered AIR 5 instead of computed AIR 1. The other eight tests passed. Full generated log: `build/gametest/fnd04-public-red-runGameTestServer.log`.
- First GREEN after the routing split: the same command exited 0 with all 14 required GameTests passing. Full generated log: `build/gametest/fnd04-public-green-runGameTestServer.log`.
- A coordinator pre-review follow-up strengthened two boundaries: the dedicated unregistered fixture now proves exact generated ENTROPY 1 before confirming `exists` remains false after public computation, and snapshot mutation now rechecks both the shared AIR 1/CRYSTAL 0 result and the untouched raw AIR 5/CRYSTAL 5 seed. The covering GameTest server again passed all 14 required tests; log: `build/gametest/fnd04-public-post-review-runGameTestServer.log`.
- The tests use the real internal handler installed by common setup. No handler replacement, registry replacement, or order-dependent shared test mutation is used.
- Covered contracts: real crystal public values; independent constructor snapshot; null and empty public inputs; public/shared exact values on an enchanted two-count container including MAGIC 3; positive filtering and cap 500; empty versus filled `ScanGeneric` eligibility; raw `exists` behavior; and absent/present complex registration controls that exclude generated fallback and property bonuses from stored raw registrations.

## Limits

The recipe generator remains a stub and serves only as a regression control here. These tests do not establish recipe-derived BETA26 aspects, complex-registration parity, registry identity parity, or scanning progression. They establish the public routing boundary and preserve the current raw registration semantics until those areas receive separate reference evidence and implementation work.

## Live reference follow-up

[REF-0011](fnd-04-live-crystal-2026-09-19.md) records the original Aer crystal tooltip displaying AIR1 only. It corroborates this one numeric result, while API routing remains rank-2 artifact inspection. No matched live port tooltip or full scanning/smelter progression claim is made.

## Independent approval — 2026-09-19

Fresh critic `/root/fnd04_public_critic` returned specification PASS and task-quality PASS with no actionable findings after inspecting the actual patch and reference artifacts. The coordinator first strengthened two missing assertions: raw non-registration after a computed lookup, and shared/raw non-mutation after snapshot changes. The builder's covering rerun passed 14/14 GameTests.

The critic independently executed `gradlew.bat compileJava test build --rerun-tasks` (exit 0; JUnit 4 tests, 0 failures/errors/skips) and `gradlew.bat runGameTestServer` (exit 0; 14 launched, all 14 required tests passed). Logs: `build/fnd04-public-critic-compile-test-build.log` and `build/gametest/fnd04-public-critic-runGameTestServer.log`. The production JAR had 3,535 entries with no GameTest or fixture entries. Filesystem-map check, pipeline validation, and whitespace check passed. `run/eula.txt` remains false.

The critic also freshly verified the released jar's routing, screenshot/jar hashes, loaded setup, and REF-0011 screenshot. Approval permits this focused commit only. FND-04 remains ACTIVE and FND-01 VERIFYING; the largest remaining gap is original-game attribution values and end-to-end scanning/smelter agreement with persistence and multiplayer.
