# FND-01 Client Smoke — 2026-09-19

Revision: 082966f, containing automated checkpoint ae9a183. Operator: /root via the installed Computer Use skill and native Minecraft UI. No source changes, cheats, inventory commands, or EULA acceptance.

This is supplemental client startup and base-world lifecycle evidence. It does not satisfy the pedestal, survival progression, chunk-lifecycle, or multiplayer acceptance cases in FND-01-evidence.md.

## Reproduction

A Gradle init script at `build/fnd01-client-smoke.init.gradle` sets only `minecraft.runs.client.workingDirectory` to `build/fnd01-client-smoke`. The profile was created empty before launch; it uses the repository's unmodified client run configuration and Java 17. Ordinary `run/` files and existing worlds were untouched.

```groovy
gradle.beforeProject { project ->
    project.afterEvaluate {
        project.minecraft.runs.client.workingDirectory project.file('build/fnd01-client-smoke')
    }
}
```

```powershell
.\gradlew.bat -I build/fnd01-client-smoke.init.gradle runClient --console=plain
```

In the UI, continue first-run setup, choose Singleplayer, create `FND01 Survival Smoke 2026-09-19` with Survival / Normal / Allow Cheats OFF and default world generation. After spawn, pause, Save and Quit to Title, and Quit Game. Relaunch the same command and re-enter the same saved world. All artifacts remain under ignored `build/`.

## Initial run

- Clean-profile title screen and rendered survival spawn directly observed through the native UI.
- Initial log: `build/fnd01-client-smoke-launch.log`.
- At 06:57:08 local, player `Dev` joined at `(540.5, 73.0, -123.5)`.
- At 06:57:37, Minecraft reported all dimensions saved. `level.dat`, playerdata, and region data exist in the named save directory.
- At 06:57:49, client logged `Stopping!`; Gradle process session 75404 exited 0 (`BUILD SUCCESSFUL`).
- World inventory was empty; no Thaumcraft block or research progression was exercised.

## Relaunch

The separately launched client reopened the same named save. At 06:59:00 local, `Dev` joined at `(540.5, 73.0, -123.5)`, matching the initial logged position. The rendered survival view, empty inventory, and full health/food were observed again. Output: `build/fnd01-client-smoke-reload.log`. Minecraft saved all dimensions again, logged `Stopping!` at 06:59:48, and the second Gradle process (session 14992) exited 0 with `BUILD SUCCESSFUL`. Both clients are closed.

## Remaining gates

FND-01 remains VERIFYING. The actual pedestal survival/restart/chunk-lifecycle cases and dedicated two-client/reconnect cases are unexecuted. The request for EULA acceptance/local dedicated-server authorization remains unanswered; `run/eula.txt` is still false. Independent critic /root/client_smoke_critic returned PASS for this supplemental smoke on 2026-09-19. The critic verified the two distinct logs, matching world/player coordinates, normal saves and exits, updated save files, no ERROR/FATAL entries, and unchanged eula=false. UI-only observations (mode/cheats settings, rendering, empty inventory and health/food) are attributed to the root operator, not independently reconstructed from logs. This approval does not mark FND-01 DONE.
