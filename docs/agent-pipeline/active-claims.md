# Active Claims

This ledger is the sole current-owner record for work items. It shows who is touching what right now; completed work is preserved in the handoff archive under `handoffs/`.

## Current State

The project owner lifted the implementation hold on 2026-09-19. `FND-01` is `READY`, but it is not currently claimed. The empty state row below records that there is no active owner or implementation worktree yet.

## Claim Rules

- A worker reads this file before claiming any item and claims only a `READY` item after the coordinator records the claim here.
- The coordinator records a claim here before any editing starts. A claim records the work item, lane, owner, worktree or branch, intended paths, the claim timestamp, the last-update timestamp, the dependency status, and the release condition.
- The coordinator refreshes `Last update` on every claim change and removes the claim when the item's handoff is archived under `handoffs/`.

## Active Claims

| Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
|---|---|---|---|---|---|---|---|---|
| None | — | — | — | — | — | — | `FND-01` ready and unclaimed | Coordinator records a claim before implementation |
