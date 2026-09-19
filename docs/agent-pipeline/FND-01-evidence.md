# FND-01 Test Foundation Evidence

Date: 2026-09-19  
Revision under test: branch `codex/fnd-01-test-foundation`, base `7a6e1d7`  
Scope: test infrastructure and tests, plus a narrow dedicated-server classloading repair that marks two existing client-only entrypoint helpers with `@OnlyIn(Dist.CLIENT)`. No gameplay behavior changed.

## Automated evidence

| Check | Result | Evidence |
|---|---|---|
| Baseline `compileJava test` | Exit 0; `test` was `NO-SOURCE` | `build/fnd-01-baseline.log` |
| JUnit red step | Exit 1 because Jupiter symbols were unavailable before the dependency was added | `build/fnd-01-junit-red.log` |
| Focused AspectList suite | Exit 0; 4 tests, 0 failures, 0 errors, 0 skipped | `build/fnd-01-junit-green.log`; `build/test-results/test/TEST-thaumcraft.api.aspects.AspectListTest.xml` |
| Intentional failing assertion | Exit 1; 1 discovered test failed; the assertion was restored immediately afterward | `build/fnd-01-intentional-failure.log` |
| Pre-boundary-repair `compileJava` | Exit 0 | `build/fnd-01-compileJava-final.log` |
| Pre-boundary-repair `test` | Exit 0; 4 tests, 0 failures, 0 errors, 0 skipped | `build/fnd-01-test-final.log` |
| Pre-boundary-repair `build` | Exit 0 | `build/fnd-01-build-final.log` |
| Final combined `compileJava test build` | Exit 0 | `build/fnd-01-final-gradle.log` |
| GameTest compilation and fixture handling | Exit 0; original SNBT copied into isolated working directory | `build/fnd-01-gametest-final.log` |
| GameTest production-JAR isolation | `build/libs/thaumcraft-6.2.0.jar` contains no `gametest`, `FoundationGameTests`, or `fnd01` entries | Local `tar -tf` inspection after `build` |
| GameTest launch classpath | Main and `gameTest` class/resource outputs are both supplied as the `thaumcraft` mod | `build/fnd-01-gametest-first.log`, lines 89-90 |
| Guarded startup regression | Exit 1 before the client boundary repair because startup produced no nonzero passing count | `build/fnd-01-gametest-guarded-blocker.log` |
| Final `runGameTestServer` | Exit 0; 2 discovered, 2 required passed, 0 failed | `build/fnd-01-gametest-final.log`; complete captured process output at `build/gametest/runGameTestServer.log` |
| Pipeline/map/whitespace gates | Map check exit 0; validator exit 0; `git diff --check` exit 0 | Final local verification on 2026-09-19 |

The JUnit cases exercise real `AspectList` behavior: per-aspect resource sufficiency, copy independence, known-aspect NBT roundtrip, and replacement of prior state while ignoring unknown or missing aspect keys. The synthetic GameTest fixture is an original one-block empty SNBT structure copied only into the isolated `build/gametest-run/` working directory for execution.

`runGameTestServer` now rejects startup failure and zero-test runs even when Forge's Java process returns zero. It requires the server output marker `All <nonzero> required tests passed :)`; otherwise Gradle fails and points to the complete captured log.

## Startup regression and repair

The first launch reached no GameTest. During reflection of the common mod entrypoint constructor, Forge's dedicated-server dist cleaner rejected `net.minecraft.client.multiplayer.ClientLevel`. The common `thaumcraft.Thaumcraft` class exposed existing client-only helpers without dist annotations. Marking `getClientWorld()` and `isShiftKeyDown()` with `@OnlyIn(Dist.CLIENT)` preserves their client implementation and lets Forge remove them from the dedicated-server class surface.

The guarded red run exited 1 with **0 discovered, 0 passed, 0 failed**. After the boundary repair and native SNBT fixture correction, the final run reported **2 discovered, 2 passed, 0 failed**. The tests prove registered arcane-stone placement and a pedestal inventory save/load into a distinct registered-factory block entity, including slot count, maximum count, item identity, item count, tag preservation, and old-instance independence.

`run/eula.txt` remained `eula=false`. It was neither edited nor accepted. No ordinary dedicated server was started.

## Manual runtime acceptance

The full feature acceptance scenarios below are **NOT RUN**. Supplemental clean-client startup, fresh empty survival spawn, save, full client quit/relaunch, and same-world re-entry passed on 2026-09-19; see [client smoke evidence](FND-01-client-smoke-2026-09-19.md). That narrower smoke did not obtain a pedestal or test its persistent state and does not satisfy the cases below. The project owner must perform any EULA-gated dedicated-server setup.

1. **Fresh survival world — NOT RUN.** Create a fresh survival world without commands or creative inventory; obtain and place arcane stone and an arcane pedestal through the intended progression; insert one tagged/renamed item and confirm the pedestal holds exactly one.
2. **Full quit and reload — NOT RUN.** Exit to title, close the client, relaunch, re-enter the same world, and confirm the exact pedestal item identity, count, and tag remain.
3. **Chunk unload and reload — NOT RUN.** Move far enough to unload the pedestal chunk, return, and confirm the same saved inventory without duplication or loss.
4. **Dedicated server plus clients — NOT RUN.** On an owner-authorized fresh server, connect two clean clients, repeat placement and pedestal insertion, and confirm both clients see the server-authoritative one-item state.
5. **Reconnect — NOT RUN.** Disconnect one client, change or retrieve the pedestal item with the other, reconnect, and confirm the returning client receives the current state without stale or duplicated data.

The passing pedestal GameTest proves only an in-memory save/load roundtrip into a distinct block entity through the registered factory. It does not prove world restart, chunk lifecycle, survival reachability, or multiplayer synchronization.

## Reproducible command sequence

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
.\gradlew.bat compileGameTestJava processGameTestResources
.\gradlew.bat runGameTestServer
.\gradlew.bat build
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\agent-pipeline\Update-FileSystemMap.ps1 -Check
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\agent-pipeline\Test-AgentPipeline.ps1
git diff --check
```

Expected current state: every automated command above passes. Manual world, restart, chunk lifecycle, and multiplayer acceptance remain separate unexecuted requirements.

## Independent review and checkpoint disposition

`/root/fnd01_critic` returned **spec PASS and quality PASS for the automated checkpoint** on 2026-09-19. The reviewer independently rebuilt the production JAR, forced fresh JUnit execution (4/4), verified test-code exclusion from the JAR, and ran both negative and positive GameTests. An intentionally false arcane-stone assertion made 1 of 2 tests fail and Gradle exit 1; the test source was then restored byte-for-byte (SHA-256 `b6044a91eda66af691a3e7291690701f97d5d3ebd0eb0b652b215b66c521476e`). The restored run passed 2/2 and exited 0. Logs: `build/fnd-01-critic-build.log`, `build/fnd-01-critic-unit.log`, `build/fnd-01-critic-gametest-negative.log`, and `build/fnd-01-critic-gametest-positive.log`.

The coordinator also reran the pipeline fixture suite after FND-01 became VERIFYING: 8/8 cases passed. FND-01 remains **VERIFYING**; at the time of this checkpoint, it did not unblock dependent items or establish full parity.

**2026-09-19 supersession note:** the later, evidence-backed scheduling decision in [foundation-start-gate-2026-09-19.md](foundation-start-gate-2026-09-19.md) uses this reviewed automated checkpoint to make **only FND-04** ready to start under the existing owner hold lift. That narrow start gate does not complete FND-01, change or pass any of the five manual cases above, establish parity, or release any other FND-01 dependency.

Deferred, nonblocking runner hardening: `build.gradle` closes the captured log stream in `doLast`, which is skipped if JavaExec throws. The independently tested negative-then-positive sequence works, but future runner hardening should guarantee cleanup without closing `System.out`. This is tracked under FND-01 and does not replace any pending manual acceptance.
