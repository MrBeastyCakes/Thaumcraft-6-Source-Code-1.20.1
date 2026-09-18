# Handoff Archive

This directory is the durable archive of work-item handoffs. Each record captures what was done, what proves it, and how the coordinator disposed of it; the workboard's `Handoff` column links the record for every `ACTIVE`, `VERIFYING`, or `DONE` row.

## Archive Rules

- One record per work item, named by its canonical workboard ID: `<ID>.md` (for example `FND-03.md`).
- A handoff is required for every workboard row in an `ACTIVE`, `VERIFYING`, or `DONE` state, and the row's `Handoff` column names the path relative to `docs/agent-pipeline/` (for example `handoffs/FND-03.md`).
- Write the record from the worker's completion report before the coordinator sets a terminal state.
- Once archived, a record is immutable. Never rewrite or delete an archived record; superseding information is recorded as a new revision referenced from the workboard.
- Records store evidence descriptions and paths only. Never store original-game binaries, decompiled source, extracted assets, or recordings in this repository.

## Required Field Sequence

Every record uses these fields, in this exact order:

```text
Work item: <canonical workboard ID>
Lane: <FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL>
Worktree or branch: <git worktree or branch the work was performed in>
Commit: <short SHA of the work commit>
Scope: <what was created or changed and why>
Parity reference: <BETA26 behavior checked and the evidence document that records it>
Source evidence: <files or documents that justify the behavior claim>
Automated evidence: <command and observed result, or none plus the reason>
Runtime evidence: <game or server scenario and observed result, or none plus the reason>
Files changed: <exact paths changed by the work item>
Dependencies or follow-ups: <none, or explicit canonical IDs>
Coordinator disposition: <coordinator decision, such as DONE>
```

Documentation-only items state `none` and the reason for both `Automated evidence` and `Runtime evidence`; implementation items name the exact command or scenario and its observed outcome. `FND-03.md` is the archived example of this format.
