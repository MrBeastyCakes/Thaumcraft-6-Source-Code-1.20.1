# Active Claims

This ledger is the sole current-owner record for work items. It shows who is touching what right now; completed work is preserved in the handoff archive under `handoffs/`.

## Budget Hold State

Implementation is paused while the budget hold is active. **No work may be claimed while the budget hold remains active.** The empty state row below records the hold: there is no owner, no worktree, and no releasable path until the project owner clears the hold.

## Claim Rules

- A worker reads this file before claiming any item and claims only a `READY` item, and only after the coordinator clears the budget hold.
- The coordinator records a claim here before any editing starts. A claim records the work item, lane, owner, worktree or branch, intended paths, the claim timestamp, the last-update timestamp, the dependency status, and the release condition.
- The coordinator refreshes `Last update` on every claim change and removes the claim when the item's handoff is archived under `handoffs/`.

## Active Claims

| Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
|---|---|---|---|---|---|---|---|---|
| None | — | — | — | — | — | — | Budget hold | Project owner clears the hold |
