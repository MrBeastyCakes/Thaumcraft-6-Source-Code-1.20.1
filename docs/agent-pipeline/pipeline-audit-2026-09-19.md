# Pipeline audit — 2026-09-19

Audited commit: `2f29892`. This is a coordinator audit record, not independent approval. Existing implementation was preserved; completed pipeline tasks were not repeated.

## Verified results

- Windows PowerShell 5.1 invocation of `tools/agent-pipeline/Test-AgentPipeline.ps1` exited 0. The filesystem map was current. Workboard counts were READY=0, ACTIVE=0, VERIFYING=0, DONE=1, DEFERRED=1, BLOCKED=30.
- Windows PowerShell 5.1 invocation of `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1` exited 0 with 8/8 passing: CONTROL, READY-HOLD, STALE-MAP, DUPLICATE-ID, MISSING-DEP, CYCLE, MISSING-ARTIFACT, and MISSING-HANDOFF. Each negative case requires its expected error as well as exit 1; passing means the intended invalid fixture was rejected.
- `git diff --check` was silent. Before this audit record, the worktree was clean.
- `git diff --name-only 12d2ad0..HEAD -- src build.gradle gradle.properties settings.gradle` returned no changes. This establishes source/build scope preservation over that range, not gameplay correctness.

## Open findings for the builder/critic loop

1. **PowerShell 7 fixture launcher compatibility.** The plan advertises PowerShell 7 compatibility, but the fixture runner constructs its child executable using `Join-Path $PSHOME 'powershell.exe'` (line 48). This session runs PowerShell 7.6.5, where that path does not exist; the corresponding `Test-Path` returned False. The Windows PowerShell invocation works. Select the executable for the actual host edition and test both supported hosts. The full suite was not rerun under PowerShell 7 because the missing child path is already established.

2. **GameTest source placement conflicts with the existing run configuration.** `validation-matrix.md` directs server and persistence GameTests to `src/test/java/thaumcraft/`, but `build.gradle:45` loads only `sourceSets.main` into its mod runs, including `gameTestServer`. Tests placed only in the documented test source set would not be included by that configuration. Correct the guide to distinguish JUnit sources from a GameTest source set that is actually loaded; FND-01 must prove test discovery when gameplay/build work resumes. Do not run Gradle or change its configuration under the current hold.

3. **Release acceptance has a circular prerequisite in prose.** `parity-workboard.md` blocks REL-03 packaging on REL-02 acceptance, while `validation-matrix.md` requires the REL-03 package as REL-02's clean-state input. Define an explicitly provisional candidate for acceptance, followed by final packaging after acceptance, or another acyclic sequence that preserves both gates. The structural validator checks workboard edges, so its passing result does not detect this cross-document dependency.

## Review status and next action

The independent reviewer task `/root/pipeline_final_critic` failed before delivering a verdict because the account usage limit was reached. There is no independent PASS for this audit. Do not repeatedly dispatch the same blocked reviewer without a change in availability.

When reviewer capacity is available, use a builder for the three bounded findings, then a fresh critic to verify the changes and all pipeline specification acceptance criteria. Preserve prior evidence and historical review notes. Commit repairs only after approval, as requested by the current execution goal. This audit record remains uncommitted pending that review.

Gameplay implementation remains paused. Neither the existing structural checks nor this audit proves TC6 gameplay parity.

## Builder repair evidence — 2026-09-19

These repairs address the three bounded findings above; they do not replace the original failure history or constitute independent approval.

- `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1` now selects `powershell.exe` for the `Desktop` host edition and `pwsh.exe` for the `Core` host edition, and verifies the selected child executable exists before creating fixtures. Under PowerShell 7.6.5, the former path `C:\Users\t8rto\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\powershell.exe` was absent, while the corrected `pwsh.exe` path in that directory existed. The full fixture suite passed 8/8 under Windows PowerShell 5.1.26100.9444 and 8/8 under PowerShell 7.6.5; both runs exercised the control plus all seven negative failure classes.
- `validation-matrix.md` now distinguishes JUnit sources and fixtures under `src/test/` from GameTests. It records that the current Forge runs load only `sourceSets.main`, places current GameTests in the loaded main mod tree, and permits a future dedicated GameTest source set only after FND-01 wires it into `gameTestServer` and proves discovery. The server gameplay and persistence rows use the same convention.
- `parity-workboard.md` and `validation-matrix.md` now define an acyclic release sequence: REL-02 builds and tests a provisional candidate from the exact revision under acceptance; after that revision is accepted, REL-03 reproduces it as the final private-play package and performs final clean-install verification.
- `Update-FileSystemMap.ps1 -Check` and `Test-AgentPipeline.ps1` each exited 0 under both Windows PowerShell 5.1 and PowerShell 7. The map was current, and each validator run reported READY=0, ACTIVE=0, VERIFYING=0, DONE=1, DEFERRED=1, BLOCKED=30. A focused search found no remaining guidance that places GameTests under `src/test` and no release-acceptance requirement that consumes an REL-03 package before REL-02 acceptance.
- No Gradle task, game process, download, EULA action, source change, or distribution action was performed. The repair remains uncommitted pending the required independent critic verdict.

Independent verdict: **PENDING**.

## Independent whole-scope critic verdict — 2026-09-19

**PASS for pipeline acceptance**, reviewing `2f29892` plus the three uncommitted repairs above. This verdict supersedes the earlier PENDING status without erasing its history. No blocking findings remain in the pipeline scope. Gameplay implementation, FND-01 test discovery, runtime parity, and release acceptance remain pending under the budget hold. This approval permits the coordinator to commit the reviewed repairs and audit record; the critic did not commit or edit implementation.

### Specification acceptance evidence matrix

| Acceptance requirement | Independent evidence | Result |
|---|---|---|
| Active claims, handoffs, feature matrix, entrypoints, reference, validation, and budget records cross-reference from README and AGENTS | Read both documents: README artifact table names all seven control surfaces and both tools; AGENTS read-before-editing list names all seven, with the handoff README explicitly included. All named artifacts exist. | PASS |
| FND-03 archived handoff and named handoffs for terminal states | Workboard's sole DONE row names `handoffs/FND-03.md`; that file records `a908d65` and the required ordered fields. Archive rules require named records for ACTIVE, VERIFYING, and DONE. Validator checks all three states; missing-handoff fixture independently rejected with exit 1 in both hosts. | PASS |
| Every required BETA26 group has an owner or ownership rationale | Read all 16 group rows and their rationales. A separate count/set comparison found exactly 16 groups and all 32 canonical workboard IDs represented; every multiple-owner row has a substantive rationale. Names match the specified groups (display capitalization only). | PASS |
| Deterministic map and successful Check | Both host invocations of `Update-FileSystemMap.ps1 -Check` succeeded. Two fresh in-memory calls to its actual renderer returned identical content and matched the checked-in map under the plan's explicit BOM/newline normalization. Map SHA-256 stayed `7B859E6C6D7F2578B14744218552F7540474FEF97395C3B9C11D848F3DCABACF` through all four repository map/validator invocations. | PASS |
| Current budget-held validator success and specified isolated negative fixtures | Independently ran the full fixture runner under Windows PowerShell 5.1 and PowerShell 7.6.5: both exited 0 with 8/8 cases passing (CONTROL, READY-HOLD, STALE-MAP, DUPLICATE-ID, MISSING-DEP, CYCLE, MISSING-ARTIFACT, MISSING-HANDOFF). Reviewed runner code: each negative case asserts exit 1 plus its intended error, regenerates the copied fixture map before injection, and removes the fixture. Both repository validator invocations exited 0. | PASS |
| Whitespace and no prohibited source/game/build activity | `git diff --check` exited 0 (only Git line-ending conversion warnings). `git diff --name-only 12d2ad0 -- src build.gradle gradle.properties settings.gradle` returned no changes. Current modified implementation scope is two pipeline documents and one pipeline fixture script, plus this audit note. The critic ran no Gradle, game, download, EULA, or gameplay-edit action. | PASS |

### Operating and tool contract coverage

| Requirement | Evidence and judgment |
|---|---|
| Target, authority, claim gating, and required claim fields | AGENTS/workboard retain Minecraft 1.20.1, Forge 47.3.0, Java 17, and BETA26. Active-claims ledger has all nine required columns and no owner; claiming requires READY after hold clearance. Budget document preserves owner-only implementation/EULA/distribution authority. Its separately dated owner-authorized reference-observation exception does not release port implementation. PASS. |
| Added work items and PLY lane | Canonical workboard includes FND-04, ALC-00, AUT-04, WLD-05, PLY-01; AGENTS lane table and handoff format include PLY; every validator canonical-ID pattern includes PLY. PASS. |
| Per-item source, legacy, registration/data, and test entrypoints | All 32 workboard items have entrypoint rows. Checked every backtick-delimited source/data/build/document path in the entrypoint map against disk: none absent. Gameplay rows identify legacy roots; discovery placeholders are limited to cross-cutting harness, evidence, and release items without a corresponding gameplay subsystem. PASS. |
| Reference metadata without copied artifacts | Catalog stores external artifact paths, SHA-256 values, exact runtime/loader/mod context, and scenarios or inspection commands; explicitly distinguishes inventory, inspection, and capture records. No repair adds a binary, asset, decompiled source, or recording. PASS. |
| Map write/check contract | Read implementation: retained RepositoryRoot, added Check; UTF-8 without BOM write occurs only after the Check branch returns; missing/stale branches throw clear errors; no generated timestamp or commit SHA; on-disk test counts say files. Normalization follows the approved plan. Fresh renderer and hash evidence above establish determinism/nonmutation. PASS. |
| Validator requirements 1–3 | Calls updater with Check; checks required files; collects canonical IDs including PLY and rejects duplicates. Successful repository run plus missing-artifact/duplicate fixtures demonstrate these paths. PASS. |
| Validator requirements 4–6 | Resolves internal BLOCKED targets, checks cycles with visiting/visited sets, and rejects READY under the literal active hold. Missing-dependency, cycle, and READY-HOLD fixtures exercise each failure independently. PASS. |
| Validator requirements 7–9 | Checks named handoff existence for ACTIVE/VERIFYING/DONE; fresh runs report READY=0, ACTIVE=0, VERIFYING=0, DONE=1, DEFERRED=1, BLOCKED=30 plus informational `all gameplay lanes` and `written permission`; negative cases produce exit 1. PASS. |

### Repair verdicts

- **PowerShell launcher: PASS.** Edition-sensitive executable selection is the only script behavior repair. Both full independent fixture runs passed, including the formerly broken Core host.
- **GameTest guidance: PASS.** Inspected `build.gradle` configureEach/mods block: it loads `sourceSets.main`, inherited by gameTestServer. Revised validation guidance uses the loaded main tree, separates JUnit placement, and requires FND-01 to prove discovery before any alternate source set is relied upon. This is accurate documentation, not evidence that discovery already works.
- **Release dependency: PASS.** Workboard and validation matrix both make REL-02 produce and test the provisional candidate; REL-03 depends on REL-02 and reproduces the accepted revision before final clean-install verification. No reverse REL-03 prerequisite remains in the acceptance tier.

Scope note: the current broad `55975de..HEAD` history also includes a later root README status correction (`2dcdf13`), beyond the original plan's pipeline-directory list. It is documentation, not a prohibited source/build change; the earlier historical scope statement is not being reused as evidence for the expanded range.
