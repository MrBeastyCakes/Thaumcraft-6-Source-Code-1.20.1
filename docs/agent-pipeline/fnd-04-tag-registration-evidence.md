# FND-04 ordinary item-tag registration evidence

This slice repairs only ordinary String item-tag registrations. It retains declarations made before Forge binds tags, resolves them after tag updates, preserves API call order across direct and tag registrations, and removes stale memberships on reload. Complex registration, recipe generation, NBT identity, entity attribution, and multiplayer acceptance remain outside this slice.

## Reproduction

At base `2a6554d`, three pre-production GameTests proved the lifecycle defect behaviorally. `forge:ingots/iron` was bound and contained `minecraft:iron_ingot`; `forge:gems/diamond` was bound and contained `minecraft:diamond`. The raw seeded lists were nevertheless absent, so iron failed its METAL 15 assertion and diamond failed its CRYSTAL 15 / DESIRE 15 assertion. The explicit redstone-ore control passed raw and computed EARTH 5 / ENERGY 15, as did all prior 17 GameTests. The preserved log is `build/gametest/fnd04-tags-red-runGameTestServer.log` (20 discovered, exactly two failures).

## Repair and lifecycle coverage

`AspectRegistrationStore` retains ordered direct assignments and copied tag declarations. A refresh constructs and publishes a complete replacement tag map; each member receives its own list copy, removed members reveal an older direct assignment or absence, and refresh does not alter precedence. `AspectHelper` adapts Forge item-tag membership, the ordinary `ThaumcraftApi.registerObjectTag(String, AspectList)` overload retains valid declarations, and `AspectTagEvents` refreshes only when `TagsUpdatedEvent.shouldUpdateStaticData()` permits it.

Nine isolated-store tests cover pending rules, immediate resolution, both precedence directions, reversed overlapping enumeration, stable refresh order, removal/addition/fallback, unknown tags, input/member ownership, union counts, direct-list retention, and clear-without-resurrection. Three lifecycle-guard tests cover server data load, remote client packets, and integrated-client packet suppression.

The asynchronous GameTest registers fixture A direct AIR 1, then tag EARTH 2, then C direct WATER 4. It reads the live Forge tag and proves exactly two members at every stage: built-in A/C, replacement B/C, and restored A/C. A test-owned pack with `pack_format` 15 and `replace:true` changes membership through `MinecraftServer.reloadResources`; the Forge event path produces A=AIR 1, B=EARTH 2, C=WATER 4. Restoring the original selected pack IDs through a second real reload produces A=EARTH 2, B absent, C=WATER 4. Iron remains METAL 15 at every stage. The test never calls the refresh helper directly. The concrete cleanup target uses a generated UUID name; the deletion predicate accepts only a normalized direct child of the GameTest world's datapacks directory whose name has the test-owned prefix, reports deletion failures, and restores selection before failure delivery on the server executor.

The builder observed attempted separate fixture subscribers fail before test discovery, with registry freeze reporting the existing fixture IDs as ownerless overrides. Those attempt logs were overwritten by the required final GREEN run, so this is a builder observation rather than retained or independently reproducible proof. The approved narrow workaround adds A/B/C to the existing `AspectLookupGameTests.TestItems` registration block, preserving the intended API registration order; no general Forge restriction is claimed.

## Reference and limits

`REF-0013` records read-only `javap -p -c` inspection of the original BETA26 jar after re-verifying its SHA-256. The released ordinary String registration expands the named ore category, copies and count-normalizes each matching stack, copies the aspect declaration per member, and delegates to item registration. `ConfigAspects` seeds `ingotIron` with METAL 15 and `gemDiamond` with CRYSTAL 15 / DESIRE 15. The modern retained-rule/reload event is an adaptation for Forge 47.3.0 and is not a claim that 1.12.2 had a reload lifecycle.

## Validation

- Focused JUnit: 12 tests passed (nine store and three lifecycle guard).
- Negative sensitivity: removing C from the built-in fixture tag made the new live membership check fail at the initial stage; preserved in `build/gametest/fnd04-tags-membership-negative-runGameTestServer.log`.
- `runGameTestServer`: all 21 required tests passed after restoring C; `build/gametest/fnd04-tags-round1-runGameTestServer.log` records both replacement and restoration stages.
- No ordinary server, EULA action, original code retention or network publication occurred. The builder and critic made no commits; the coordinator commits only after approval.

## Independent review checkpoint

A separate fresh critic inspected the implementation and original screenshots, then independently ran `compileJava test build --rerun-tasks --console=plain`: 28 JUnit tests passed, with no failures, errors or skips. The production JAR contained 3,539 entries and no GameTest classes, fixture identifiers, test tags or temporary-pack content. Map, pipeline and whitespace checks passed; `run/eula.txt` remained false.

The first review correctly failed the reload coverage: C's newer direct registration could hide its absence from a tag. The builder added exact live membership checks at all three stages and demonstrated a missing-C negative failure. The critic inspected that negative log and independently reran the corrected GameTests: 21/21 passed, both reload stages were recorded, and no temporary datapack remained. Final specification and quality verdicts: PASS, limited to this slice.

Independent logs: `build/fnd04-tags-critic-compile-test-build.log` and `build/gametest/fnd04-tags-round1-critic-runGameTestServer.log`. The local review report is `.superpowers/sdd/2026-09-19-fnd04-tag-registration/critic-report.md`; this tracked note preserves its result if scratch files are unavailable.

[REF-0014 original tooltips](fnd-04-live-items-2026-09-19.md) independently corroborate iron METAL15 and diamond CRYSTAL15/DESIRE15. Their hashes and visible values were checked by the critic. This adds narrow display evidence, not original reload, survival or multiplayer acceptance. FND-04 remains ACTIVE.

Reviewed implementation commit: 3cd1486 (base 2a6554d).
