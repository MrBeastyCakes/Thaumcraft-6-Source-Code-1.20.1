# Active Claims

This ledger is the sole current-owner record for work items. It shows who is touching what right now; completed work is preserved in the handoff archive under `handoffs/`.

## Budget Hold State

The project owner cleared the budget hold on 2026-10-12 (see [budget-hold.md](budget-hold.md)). Items are claimed in the spending order recorded there, starting with `FND-01`.

## Claim Rules

- A worker reads this file before claiming any item and claims only a `READY` item.
- The coordinator records a claim here before any editing starts. A claim records the work item, lane, owner, worktree or branch, intended paths, the claim timestamp, the last-update timestamp, the dependency status, and the release condition.
- The coordinator refreshes `Last update` on every claim change and removes the claim when the item's handoff is archived under `handoffs/`.

## Active Claims

| Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
|---|---|---|---|---|---|---|---|---|
| FND-01 | FND | Claude Code session (coordinator and worker) | `ccr-83a6df83-lck5ok` | `build.gradle`, `src/test/java/thaumcraft/`, `src/test/resources/data/thaumcraft/structures/`, FND-01 pipeline records | 2026-10-12 | 2026-10-12 | None (first item after the hold) | Gradle `test` and `runGameTestServer` pass on a machine that can reach the Forge maven, recorded in `handoffs/FND-01.md` |
