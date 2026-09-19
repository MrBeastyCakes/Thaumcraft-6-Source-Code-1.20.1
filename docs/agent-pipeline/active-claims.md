# Active Claims

This ledger is the sole current-owner record for work items. It shows who is touching what right now; completed work is preserved in the handoff archive under `handoffs/`.

## Current State

The project owner lifted the implementation hold on 2026-09-19. `FND-01` is verifying only its remaining manual runtime acceptance under the claim below. The automated checkpoint passed independent review. FND-04 is `ACTIVE` under its separate claim and reviewed start gate; the coordinator owns status and handoff updates.

## Claim Rules

- A worker reads this file before claiming any item and claims only a `READY` item after the coordinator records the claim here.
- The coordinator records a claim here before any editing starts. A claim records the work item, lane, owner, worktree or branch, intended paths, the claim timestamp, the last-update timestamp, the dependency status, and the release condition.
- The coordinator refreshes `Last update` on every claim change and removes the claim when the item's handoff is archived under `handoffs/`.

## Active Claims

| Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
|---|---|---|---|---|---|---|---|---|
| FND-01 | FND | /root (coordinator; manual acceptance pending) | codex/fnd-04-aspect-lookup | FND-01 manual runtime evidence and directly related coordination documents only | 2026-09-19 | 2026-09-19 | Automated checkpoint reviewed; five manual cases remain | All five manual runtime cases recorded as passing; then archive handoff and release claim |
| FND-04 | FND | /root (coordinator; next FND-04 investigation) | codex/fnd-04-aspect-lookup | AspectHelper.java; ThaumcraftCraftingManager.java; AspectCullingTest.java; AspectLookupGameTests.java; fnd-04 culling/reference evidence; reference-catalog.md; filesystem-map.md | 2026-09-19 | 2026-09-19 | Reviewed harness gate 5acd3c2; first slice 30a58dc approved; public-lookup slice a83af2c independently approved; culling slice 18db4e8 independently approved; next investigation: tag-registration lifecycle | Independent slice review; full item remains open until all acceptance evidence |
