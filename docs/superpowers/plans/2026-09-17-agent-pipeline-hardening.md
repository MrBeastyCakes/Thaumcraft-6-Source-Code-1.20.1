# Agent Pipeline Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the low-cost coordination and verification layer that lets agents safely resume TC6 parity work after the budget hold.

**Architecture:** The documentation layer becomes the durable source of coordination state: claims identify current ownership, per-item handoffs preserve evidence, and a feature matrix makes parity coverage measurable. Two PowerShell tools validate the deterministic filesystem map and the pipeline graph without Gradle or Minecraft.

**Tech Stack:** Markdown, Windows PowerShell 5.1 (also PowerShell 7 compatible), Git, existing Forge repository metadata.

**Spec:** `docs/agent-pipeline/pipeline-hardening-spec.md`

## Global Constraints

- Target Minecraft 1.20.1 Forge 47.3.0 on Java 17.
- Thaumcraft 6.1.BETA26 is the intended-behavior baseline.
- Do not start Minecraft, run Gradle, accept the EULA, download dependencies, alter gameplay code, or copy proprietary TC6 binaries/assets.
- Preserve the budget hold until the project owner explicitly clears it.
- The tools must be local, deterministic, read-only in check mode, and usable from PowerShell.
- Keep all changes in the existing repository and use small focused commits.

---

### Task 1: Add claim and handoff control records

**Files:**
- Create: `docs/agent-pipeline/active-claims.md`
- Create: `docs/agent-pipeline/handoffs/README.md`
- Create: `docs/agent-pipeline/handoffs/FND-03.md`
- Modify: `AGENTS.md`
- Modify: `docs/agent-pipeline/README.md`
- Modify: `docs/agent-pipeline/parity-workboard.md`

**Interfaces:**
- Consumes: canonical IDs and states in `parity-workboard.md`.
- Produces: one active-claim table with columns `Work item`, `Lane`, `Owner`, `Worktree or branch`, `Intended paths`, `Claimed`, `Last update`, `Dependency status`, and `Release condition`.
- Produces: one handoff file per terminal work item at `docs/agent-pipeline/handoffs/<ID>.md`.

- [x] **Step 1: Write the claim ledger with the budget-held empty state.**

  Use this exact table header and an explicit statement that no work may be claimed while the budget hold remains active:

  ```markdown
  | Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
  |---|---|---|---|---|---|---|---|---|
  | None | — | — | — | — | — | — | Budget hold | Project owner clears the hold |
  ```

- [x] **Step 2: Add the handoff archive contract and the FND-03 record.**

  The archive contract requires this exact field sequence: `Work item`, `Lane`, `Worktree or branch`, `Commit`, `Scope`, `Parity reference`, `Source evidence`, `Automated evidence`, `Runtime evidence`, `Files changed`, `Dependencies or follow-ups`, and `Coordinator disposition`. The `FND-03` record names commit `a908d65`, links `parity-evidence-index.md`, records no runtime/game test because the item is documentation-only, and sets disposition to `DONE`.

- [x] **Step 3: Connect the records to agent startup and state transitions.**

  Update `AGENTS.md` so a worker reads `active-claims.md` before claiming, the coordinator records a claim before editing, and `ACTIVE`, `VERIFYING`, and `DONE` workboard rows link a handoff path. Add a `PLY` lane for player equipment, effects, Warp, and Warp Ward. Update the pipeline README table and workflow to link the two new artifacts. Add a `Handoff` column to the workboard and populate FND-03 with `handoffs/FND-03.md`; leave non-terminal rows as `—`. Require `AGENTS.md`'s read-before-editing list to reference all pipeline artifacts, including `active-claims.md` and the `handoffs/` archive once created, so a worker is pointed at the current coordination state before editing. By the end of Tasks 1-3 the pipeline README tables must link all of `active-claims.md`, `handoffs/README.md`, `tc6-feature-matrix.md`, `system-entrypoints.md`, `reference-catalog.md`, `validation-matrix.md`, `budget-hold.md`, the existing pipeline files, and both tools (`Update-FileSystemMap.ps1` and `Test-AgentPipeline.ps1`).

- [x] **Step 4: Validate the record format.**

  Run:

  ```bash
  grep -rnE "Work item \| Lane \| Owner|Budget hold|a908d65|handoffs/FND-03\.md" AGENTS.md docs/agent-pipeline
  git diff --check
  ```

  Expected: all four markers appear and Git reports no whitespace errors.

- [x] **Step 5: Commit the control records.**

  ```powershell
  git add AGENTS.md docs/agent-pipeline/active-claims.md docs/agent-pipeline/handoffs docs/agent-pipeline/README.md docs/agent-pipeline/parity-workboard.md
  git commit -m "docs: add parity work claims and handoffs"
  ```

### Task 2: Add parity inventory and entrypoint maps

**Files:**
- Create: `docs/agent-pipeline/tc6-feature-matrix.md`
- Create: `docs/agent-pipeline/system-entrypoints.md`
- Modify: `docs/agent-pipeline/README.md`
- Modify: `docs/agent-pipeline/parity-workboard.md`
- Modify: `docs/agent-pipeline/parity-evidence-index.md`
- Modify: `docs/agent-pipeline/deferred-issues.md`

**Interfaces:**
- Consumes: `parity-workboard.md`, `parity-evidence-index.md`, `filesystem-map.md`, and the current/legacy source roots.
- Produces: feature rows with `Feature group`, `BETA26 reference`, `Current port entry points`, `Owning work item`, `Cross-lane rationale`, `Evidence state`, and `Release gate`.
- Produces: entrypoint rows with `Work item`, `Current port roots`, `Legacy reference roots`, `Registration or data roots`, and `Expected test type`.

- [x] **Step 1: Add the five missing workboard owners before creating the matrix.**

  Add `FND-04` after FND-03: `Establish authoritative aspect attribution, containers, and lookup shared by scanning and Essentia`, blocked by FND-01. Change RSR-01 and ALC-01 to be blocked by FND-04. Add `ALC-00` before ALC-01: `Restore Crucible recipes, input validation, aspect costs, and output economy`, blocked by RSR-02. Add `AUT-04`: `Restore standalone artifice and utility devices`, blocked by FND-02. Add `WLD-05`: `Validate creature spawning, AI, combat, drops, constructs, and non-focus projectiles`, blocked by FND-02. Add `PLY-01`: `Restore player equipment, Curios state, effects, Warp, and Warp Ward`, blocked by RSR-03. Change WLD-04 to be blocked by PLY-01. Add matching deferred-register rows, dependency-spine edges, and evidence-index queue rows that name the BETA26 behavior to capture for each new item. Fold the noncanonical `ALC-03a` deferred row into the `ALC-03` row in `deferred-issues.md` and remove the `ALC-03a` ID entirely. Add deferred-register rows for `FND-02`, `AUT-02`, and `CAS-03` so those canonical IDs are represented, and ensure every work-item ID referenced in `deferred-issues.md` is a canonical ID. Update the workboard dependency-spine mermaid and the pipeline README dependency-spine graph to include the five new items (`FND-04`, `ALC-00`, `AUT-04`, `WLD-05`, `PLY-01`) and to remove any noncanonical or phantom node (the README currently contains a noncanonical `ECO` node).

- [x] **Step 2: Create the feature matrix with these exact initial groups.**

  Create one row for each of these exact feature groups, with these exact owning work items:

  ```markdown
  | Feature group | Owning work item | Cross-lane rationale |
  |---|---|---|
  | Foundations | FND-01, FND-02, FND-03 | FND-01 (harness), FND-02 (integration audit), and FND-03 (evidence index) are three separate foundation deliverables with distinct acceptance evidence. |
  | Aspects and research | FND-04, RSR-01, RSR-03 | FND-04 builds the shared aspect registry that RSR-01 scanning and RSR-03 Thaumonomicon progression both consume, so ownership spans the FND and RSR lanes. |
  | Arcane crafting | RSR-02 | — |
  | Crucible alchemy | ALC-00 | — |
  | Essentia production and transport | ALC-01, ALC-02, ALC-04 | ALC-01 (aspects and smelter), ALC-02 (transport), and ALC-04 (Thaumatorium) are separate workboard items in one Essentia production chain. |
  | Infusion | ALC-03 | — |
  | Casting and foci | CAS-01, CAS-02, CAS-03 | Focus construction (CAS-01), graph execution (CAS-02), and caster inventory/validation (CAS-03) are serial workboard items covering one casting feature. |
  | Golems and automation | AUT-01, AUT-02, AUT-03 | Seals (AUT-01), golem tasks (AUT-02), and automation devices (AUT-03) are separate workboard items with distinct acceptance evidence. |
  | Artifice and utility devices | AUT-04 | — |
  | Aura, Flux, rifts, and taint | WLD-01, WLD-03 | WLD-01 owns aura/Vis/Flux persistence and WLD-03 owns rifts and taint, which read and write that same aura state. |
  | Magical world generation and flora | WLD-02 | — |
  | Creatures and combat | WLD-05 | — |
  | Player systems and Warp | PLY-01 | — |
  | Eldritch/endgame | WLD-04 | — |
  | Client presentation | CLI-01, CLI-02, CLI-03 | Screens (CLI-01), caster visuals (CLI-02), and the asset sweep (CLI-03) are independent client surfaces owned by separate workboard items. |
  | Compatibility and release | REL-01, REL-02, REL-03, REL-04 | Compatibility (REL-01), acceptance sweeps (REL-02), packaging (REL-03), and public-distribution gating (REL-04) are separate release workboard items with distinct gates. |
  ```

  Every group whose `Owning work item` cell lists more than one ID carries its explicit cross-lane ownership rationale in the `Cross-lane rationale` column; single-owner groups use `—`. All 32 canonical work-item IDs must appear in this table.

- [x] **Step 3: Create the entrypoint map from verifiable roots.**

  Include rows for all 32 canonical work items. Use current root paths such as `src/main/java/thaumcraft/api/research`, `src/main/java/thaumcraft/common/tiles`, `src/main/java/thaumcraft/common/golems`, `src/main/java/thaumcraft/common/world`, `src/main/java/thaumcraft/client`, `src/main/resources/data/thaumcraft`, and `src/main/resources/assets/thaumcraft`. For legacy paths whose exact location is not yet established, use the literal value `needs discovery from src/main/java_old/`; do not fabricate class paths.

- [x] **Step 4: Link the inventory artifacts from the pipeline README.**

  Add concise table rows explaining that the feature matrix measures parity coverage and the entrypoint map shortens safe agent handoffs.

- [x] **Step 5: Validate ownership coverage.**

  Run:

  ```bash
  grep -nE "^\| (Foundations|Aspects and research|Arcane crafting|Crucible alchemy|Essentia production and transport|Infusion|Casting and foci|Golems and automation|Artifice and utility devices|Aura, Flux, rifts, and taint|Magical world generation and flora|Creatures and combat|Player systems and Warp|Eldritch/endgame|Client presentation|Compatibility and release) \|" docs/agent-pipeline/tc6-feature-matrix.md
  grep -nE "^\| (FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-[0-9]{2} \|" docs/agent-pipeline/system-entrypoints.md
  python -c 'import re; from pathlib import Path; text = Path("docs/agent-pipeline/tc6-feature-matrix.md").read_text(encoding="utf-8"); ids = sorted(set(re.findall(r"(?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2}", text))); print("distinct canonical IDs in the feature matrix:", len(ids)); print(" ".join(ids))'
  git diff --check
  ```

  Expected: 16 feature-group rows, 32 entrypoint rows, a distinct-ID count of 32 from the feature matrix (every canonical work-item ID represented), and no whitespace errors.

- [x] **Step 6: Commit the inventory maps.**

  ```powershell
  git add docs/agent-pipeline/tc6-feature-matrix.md docs/agent-pipeline/system-entrypoints.md docs/agent-pipeline/parity-workboard.md docs/agent-pipeline/parity-evidence-index.md docs/agent-pipeline/deferred-issues.md docs/agent-pipeline/README.md
  git commit -m "docs: map TC6 parity coverage and entry points"
  ```

### Task 3: Define evidence, validation, and budget controls

**Files:**
- Create: `docs/agent-pipeline/reference-catalog.md`
- Create: `docs/agent-pipeline/validation-matrix.md`
- Create: `docs/agent-pipeline/budget-hold.md`
- Modify: `docs/agent-pipeline/README.md`
- Modify: `docs/agent-pipeline/deferred-issues.md`

**Interfaces:**
- Consumes: the evidence hierarchy, deferred issue register, and AGENTS validation contract.
- Produces: a reference metadata record, validation tiers, and explicit budget reactivation rules.

- [x] **Step 1: Create the reference catalog template.**

  Include the fields `Reference identifier`, `Minecraft version`, `Forge version`, `Thaumcraft version`, `Artifact location`, `SHA-256`, `Launcher or profile`, `World seed`, `Player setup`, `Capture scenario`, `Recorded by`, and `Date recorded`. State that entries record metadata only and never store original binaries, assets, decompiled files, or recordings in the repository.

- [x] **Step 2: Create the validation matrix.**

  Define these rows: `Documentation-only`, `Pure deterministic logic`, `Server gameplay`, `Client presentation`, `Persistence`, `Multiplayer`, and `Release acceptance`. For each row name the required command or scenario, evidence location, clean-state requirement, and pass condition. Documentation-only uses `git diff --check` and the pipeline validator; server gameplay names GameTest plus fresh-world/reload; client presentation names a client smoke scenario; multiplayer names dedicated server plus remote client; release acceptance names all validation tiers.

- [x] **Step 3: Create the budget hold policy.**

  Begin the policy with the exact sentence `Implementation is paused while the budget hold is active.` State that the project owner is the release authority; allowed activities are source reading, parity evidence cataloging, documentation, and local non-game pipeline validation; prohibited activities are gameplay changes, Gradle/game runs, EULA acceptance, distribution, and original asset copying. The reactivation condition is an explicit project-owner instruction. List spending order as `FND-01`, `FND-04`, `RSR-01/RSR-02`, `ALC-00/ALC-03`, `CAS-01/CAS-02`, then remaining workboard order. (Within each tier, items are worked in workboard dependency order.)

- [x] **Step 4: Link the new policy and validation artifacts.**

  Add README table rows for the catalog, validation matrix, and budget hold. Replace the deferred-issues opening budget sentence with a link to `budget-hold.md`.

- [x] **Step 5: Validate the controls.**

  Run:

  ```bash
  grep -nE "SHA-256|Documentation-only|Project owner|FND-01" docs/agent-pipeline/reference-catalog.md docs/agent-pipeline/validation-matrix.md docs/agent-pipeline/budget-hold.md
  git diff --check
  ```

  Expected: each policy marker occurs in its named artifact and Git reports no whitespace errors.

- [x] **Step 6: Commit the policy controls.**

  ```powershell
  git add docs/agent-pipeline/reference-catalog.md docs/agent-pipeline/validation-matrix.md docs/agent-pipeline/budget-hold.md docs/agent-pipeline/README.md docs/agent-pipeline/deferred-issues.md
  git commit -m "docs: define parity evidence and budget controls"
  ```

### Task 4: Make the filesystem map deterministic and checkable

**Files:**
- Modify: `tools/agent-pipeline/Update-FileSystemMap.ps1`
- Modify: `docs/agent-pipeline/filesystem-map.md`
- Test: temporary filesystem fixture outside the repository

**Interfaces:**
- Consumes: `-RepositoryRoot <string>` and optional `-Check <switch>`.
- Produces: exact expected Markdown content or a nonzero error when `-Check` finds a stale/missing map.

- [x] **Step 1: Write a failing temporary-fixture check.**

  Run this in PowerShell before adding `-Check`; it must fail because the existing script does not accept the switch:

  ```powershell
  $fixture = Join-Path ([System.IO.Path]::GetTempPath()) "thaumcraft-map-fixture"
  Remove-Item -LiteralPath $fixture -Recurse -Force -ErrorAction SilentlyContinue
  New-Item -ItemType Directory -Force "$fixture/src/main/java/thaumcraft/common", "$fixture/src/main/resources/assets/thaumcraft", "$fixture/src/main/resources/data/thaumcraft", "$fixture/docs/agent-pipeline" | Out-Null
  Set-Content -LiteralPath "$fixture/src/main/java/thaumcraft/Thaumcraft.java" -Value "class Thaumcraft {}"
  $acceptedCheck = $true
  try { & .\tools\agent-pipeline\Update-FileSystemMap.ps1 -RepositoryRoot $fixture -Check } catch { $acceptedCheck = $false }
  if ($acceptedCheck) { throw "The pre-change updater unexpectedly accepted -Check." }
  ```

- [x] **Step 2: Add deterministic renderer and `-Check`.**

  Change the parameter block to:

  ```powershell
  [CmdletBinding()]
  param(
      [string]$RepositoryRoot = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),
      [switch]$Check
  )
  ```

  Remove the generated timestamp and `git rev-parse` variables. Move the existing here-string construction into `Get-FileSystemMapContent`, which returns the expected map body. Render a fixed header that says `Generated from the current source layout by tools/agent-pipeline/Update-FileSystemMap.ps1.` The script must run on Windows PowerShell 5.1 (the only PowerShell installed on this host) while remaining PowerShell 7 compatible, so use no PowerShell 7-only syntax, and invoke it from git-bash or another non-PowerShell shell as `powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Update-FileSystemMap.ps1 [-RepositoryRoot <root>] [-Check]`. Normalize the rendered content with:

  ```powershell
  $content = $content.TrimEnd() + [Environment]::NewLine
  ```

  Define this normalizer before the write/check branch; it strips a leading byte-order mark when one is present, in addition to normalizing CRLF/CR line endings:

  ```powershell
  function Normalize-MapText([string]$value) {
      $text = $value.TrimStart([char]0xFEFF)
      (($text -replace "`r`n", "`n") -replace "`r", "`n").TrimEnd("`n")
  }
  ```

  Replace the final write so the map is written as UTF-8 without a byte-order mark (`Set-Content -Encoding utf8` emits a BOM on Windows PowerShell 5.1):

  ```powershell
  if ($Check) {
      if (-not (Test-Path -LiteralPath $outputPath -PathType Leaf)) {
          throw "Filesystem map is missing: $outputPath"
      }
      $actual = Get-Content -LiteralPath $outputPath -Raw
      if ((Normalize-MapText $actual) -ne (Normalize-MapText $content)) {
          throw "Filesystem map is stale. Run tools/agent-pipeline/Update-FileSystemMap.ps1."
      }
      Write-Host "Filesystem map is current: $outputPath"
      return
  }

  [System.IO.File]::WriteAllText($outputPath, $content, (New-Object System.Text.UTF8Encoding($false)))
  Write-Host "Updated $outputPath"
  ```

  Change the test-surface wording from `$testCount tracked test-source/resource files` to `$testCount test-source/resource files`.

- [x] **Step 3: Regenerate and prove determinism and check behavior.**

  Run (in a PowerShell session, or as `powershell.exe -NoProfile -ExecutionPolicy Bypass -File <script> <args>` from git-bash):

  ```powershell
  .\tools\agent-pipeline\Update-FileSystemMap.ps1
  .\tools\agent-pipeline\Update-FileSystemMap.ps1 -Check
  & .\tools\agent-pipeline\Update-FileSystemMap.ps1 -RepositoryRoot $fixture
  & .\tools\agent-pipeline\Update-FileSystemMap.ps1 -RepositoryRoot $fixture -Check
  ```

  Then prove determinism and that `-Check` never writes, in the same session:

  ```powershell
  $map = "docs/agent-pipeline/filesystem-map.md"
  .\tools\agent-pipeline\Update-FileSystemMap.ps1
  $hashOne = (Get-FileHash -Algorithm SHA256 -LiteralPath $map).Hash
  .\tools\agent-pipeline\Update-FileSystemMap.ps1
  $hashTwo = (Get-FileHash -Algorithm SHA256 -LiteralPath $map).Hash
  if ($hashOne -ne $hashTwo) { throw "Map rendering is not deterministic." }
  .\tools\agent-pipeline\Update-FileSystemMap.ps1 -Check
  $hashAfterCheck = (Get-FileHash -Algorithm SHA256 -LiteralPath $map).Hash
  if ($hashOne -ne $hashAfterCheck) { throw "-Check modified the map file." }
  ```

  Then add a temporary file under the fixture's `src/main/java/thaumcraft/common/`, invoke `-Check` against the fixture, and confirm it fails with `Filesystem map is stale`. Remove the fixture in the same PowerShell session.

- [x] **Step 4: Commit the deterministic map.**

  ```powershell
  git add tools/agent-pipeline/Update-FileSystemMap.ps1 docs/agent-pipeline/filesystem-map.md
  git commit -m "build: make filesystem map checkable"
  ```

### Task 5: Add and exercise the structural pipeline validator

**Files:**
- Create: `tools/agent-pipeline/Test-AgentPipeline.ps1`
- Modify: `docs/agent-pipeline/README.md`
- Test: `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1` and temporary copied pipeline fixtures outside the repository

**Interfaces:**
- Consumes: `-RepositoryRoot <string>`.
- Produces: exit success after valid checks; throws on missing artifact, stale map, duplicate work-item ID, missing internal dependency, dependency cycle, missing handoff file, or `READY` state during a budget hold.

- [x] **Step 1: Write the failing invalid-state fixture.**

  Create `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1`. It copies `AGENTS.md`, `docs/agent-pipeline`, `tools/agent-pipeline`, `src/main/java/thaumcraft`, and `src/main/resources` into a temporary fixture. After copying files into the fixture, the fixture script MUST first run `Update-FileSystemMap.ps1 -RepositoryRoot $fixture` (without `-Check`) so the fixture renders its own deterministic map, and only then change the FND-01 workboard cell from `DEFERRED: budget hold` to `READY`; without that regeneration the fixture map is stale for unrelated reasons (root leaf name and missing directories) and the expected `READY item exists during budget hold` error would never be reached. It then invokes `Test-AgentPipeline.ps1 -RepositoryRoot $fixture` as a child process and captures the validator's exit code and message, and it fails unless the exit code is nonzero and the captured message contains the expected text for that fault. Before `Test-AgentPipeline.ps1` exists, the fixture script must fail because the validator path is absent.

- [x] **Step 2: Implement the validator.**

  Define these functions in `Test-AgentPipeline.ps1`:

  ```powershell
  function Require-Path([string]$Path) {
      if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
          throw "Required pipeline artifact is missing: $Path"
      }
  }

  function Get-WorkItemIds([string[]]$Lines) {
      @($Lines |
          ForEach-Object {
              if ($_ -match '^\|\s*((?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2})\s*\|') { $Matches[1] }
          })
  }

  function Get-InternalDependencies([string[]]$Lines) {
      $edges = @{}
      foreach ($line in $Lines) {
          if ($line -match '^\|\s*((?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2})\s*\|\s*BLOCKED:\s*((?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2})\b') {
              $edges[$Matches[1]] = $Matches[2]
          }
      }
      return $edges
  }
  ```

  Require the pipeline files named by the specification, invoke `Update-FileSystemMap.ps1 -Check` against `-RepositoryRoot $RepositoryRoot`, collect IDs, compare `($ids | Select-Object -Unique).Count` to `$ids.Count`, and verify every canonical row has a nonempty state, outcome, primary-area, acceptance-evidence, and handoff column. Accept only the state forms `READY`, `ACTIVE`, `VERIFYING`, `DONE`, `DEFERRED: <reason>`, `BLOCKED: <canonical ID>`, `BLOCKED: all gameplay lanes`, and `BLOCKED: written permission`. Throw `Duplicate work-item ID: <ID>` for a repeated ID. Verify that every `BLOCKED: <ID>` target exists among the canonical IDs (`Missing internal dependency: <ID>`); that every work-item ID referenced in `deferred-issues.md` is a canonical ID; that every canonical ID in a `DEFERRED:` state occurs in `deferred-issues.md`; and that every `ACTIVE`, `VERIFYING`, or `DONE` row names an existing handoff file (`Missing handoff file: <path>`). Use a DFS with `visiting` and `visited` hash sets to throw `Dependency cycle detected at <ID>` on a back-edge. Detect the active budget hold with the exact phrase `Implementation is paused` in `budget-hold.md`; while it exists, throw `READY item exists during budget hold: <ID>` for any workboard row matching `^\|\s*(?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2}\s*\|\s*READY\s*\|`.

  Wrap the whole validator body in `try`/`catch`: on any thrown structural failure the `catch` prints the error message and runs `exit 1` so the process exits nonzero; on success the script prints the state counts and the informational external-gate line before returning success. Invoke it as `powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Test-AgentPipeline.ps1 [-RepositoryRoot <root>]` so the exit code is observable without closing the operator's session; it must run on Windows PowerShell 5.1 (the only PowerShell installed on this host) while remaining PowerShell 7 compatible.

  ```powershell
  try {
      # ... the structural checks above; each failure throws ...
  } catch {
      Write-Host "Agent pipeline validation failed: $($_.Exception.Message)"
      exit 1
  }
  Write-Host "Work item states: <counts by state>"
  Write-Host "External gates (informational): BLOCKED: all gameplay lanes; BLOCKED: written permission"
  exit 0
  ```

- [x] **Step 3: Prove success and each failure class.**

  Run the validator against the repository as a child process so its exit code is observable and a structural failure cannot close the session:

  ```powershell
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Test-AgentPipeline.ps1
  $LASTEXITCODE
  ```

  Then build temporary fixtures with `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1`. Every fault fixture must first copy the pipeline, then regenerate the fixture's own map (`Update-FileSystemMap.ps1 -RepositoryRoot $fixture`, without `-Check`), and only then introduce exactly ONE fault: delete `active-claims.md`; alter the map; duplicate FND-01; change `BLOCKED: FND-01` to `BLOCKED: XYZ-99`; set `FND-01` to `BLOCKED: RSR-02` and `RSR-02` to `BLOCKED: FND-01`; remove the FND-03 handoff; then set FND-01 to `READY`. The fixture runner must capture the validator's exit code and message and fail unless the exit code is nonzero and the message contains the expected text for that fault: `Required pipeline artifact is missing`, `Filesystem map is stale`, `Duplicate work-item ID`, `Missing internal dependency`, `Dependency cycle detected at`, `Missing handoff file`, and `READY item exists during budget hold`. Require a nonzero exit for every fixture and retain no fixture afterward.

- [x] **Step 4: Link and commit the validator.**

  Add `Test-AgentPipeline.ps1` to the README artifact table and list the command `powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Test-AgentPipeline.ps1` under local pipeline validation. Then run:

  ```powershell
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Test-AgentPipeline.ps1
  git diff --check
  git add tools/agent-pipeline/Test-AgentPipeline.ps1 tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1 docs/agent-pipeline/README.md
  git commit -m "build: validate the parity agent pipeline"
  ```

### Task 6: Final review of the hardened pipeline

**Files:**
- Modify: `docs/agent-pipeline/README.md` only if an artifact link or command is missing.

**Interfaces:**
- Consumes: all control artifacts and local validation output.
- Produces: a clean, self-consistent, budget-held pipeline ready for a later FND-01 claim.

- [ ] **Step 1: Run the complete local pipeline review.**

  ```powershell
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Update-FileSystemMap.ps1 -Check
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./tools/agent-pipeline/Test-AgentPipeline.ps1
  git diff --check
  git status --short
  ```

  Expected: both tools succeed, Git reports no whitespace errors, and only the intended review change is present before the final commit.

- [ ] **Step 2: Verify scope boundaries.**

  Run:

  ```powershell
  git diff --name-only 55975de..HEAD
  ```

  Expected: the pipeline hardening sequence changed only `AGENTS.md`, `docs/agent-pipeline/`, `docs/superpowers/plans/`, and `tools/agent-pipeline/`; no `src/main/java/`, `src/main/resources/`, Gradle, or EULA file changed.

- [ ] **Step 3: Commit a README correction only when it exists.**

  ```powershell
  $readmeChanges = git status --short docs/agent-pipeline/README.md
  if ($readmeChanges) {
      git add docs/agent-pipeline/README.md
      git commit -m "docs: finalize parity pipeline controls"
  }
  ```

---

## Revision Notes (2026-09-18)

- Corrected the Task 2 Step 2 feature-matrix mapping to the complete 32-canonical-ID group table and added the required `Cross-lane rationale` column (also added to the matrix `Produces` line), because the spec requires every top-level feature group to have exactly one owning work item or an explicit cross-lane ownership rationale.
- Replaced the Task 2 Step 5 group pattern with the corrected exact group names, including `Eldritch/endgame`, and added a distinct-canonical-ID count check (expected 32) so the matrix provably covers all workboard owners.
- Extended Task 2 Step 1 to fold `ALC-03a` into `ALC-03`, add deferred-register rows for `FND-02`, `AUT-02`, and `CAS-03`, keep every deferred-issues ID canonical, and update both dependency-spine graphs (adding `FND-04`, `ALC-00`, `AUT-04`, `WLD-05`, `PLY-01`; removing the noncanonical `ECO` node) so the register and graphs match the canonical ID set.
- Extended Task 1 Step 3 so the `AGENTS.md` read-before-editing list names the pipeline artifacts and the pipeline README tables link every control artifact plus both tools by the end of Tasks 1-3, satisfying the cross-linking acceptance criterion.
- Added the workboard-dependency-order parenthetical to the Task 3 Step 3 spending order so tier ordering is unambiguous.
- Made the Task 4 map renderer 5.1-compatible and BOM-free (`[System.IO.File]::WriteAllText` with `UTF8Encoding($false)`), required the normalizer to strip a leading BOM, and required repeat-run, `-Check`-pass, and `-Check`-non-mutation hash evidence so determinism is proven, not assumed.
- Required every Task 5 fixture to regenerate its own map before injecting exactly one fault, and to assert the validator's exit code plus the expected message, because a stale fixture map would otherwise mask the intended fault.
- Required the Task 5 validator to print its error and `exit 1` on structural failure via `try`/`catch`, to print state counts plus the informational external gates on success, and replaced the ambiguous deferred-ID sentence with exact canonical-ID, deferred-row, and handoff-file checks (the missing-handoff failure class is now also named in the Task 5 `Produces` line).
- Replaced the `rg` invocations in Task 1 Step 4, Task 2 Step 5, and Task 3 Step 5 with `grep -nE`/python equivalents because this host has no ripgrep.
- Required the Task 4 and Task 5 PowerShell tools to run on Windows PowerShell 5.1, the only PowerShell installed here, and to be callable via `powershell.exe -NoProfile -ExecutionPolicy Bypass -File` while staying PowerShell 7 compatible; the Task 5 Step 4 and Task 6 Step 1 invocations now use that form so tool exit codes are observable.

Requirements unchanged: docs/agent-pipeline/pipeline-hardening-spec.md is untouched.
