# Agent Pipeline Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the low-cost coordination and verification layer that lets agents safely resume TC6 parity work after the budget hold.

**Architecture:** The documentation layer becomes the durable source of coordination state: claims identify current ownership, per-item handoffs preserve evidence, and a feature matrix makes parity coverage measurable. Two PowerShell tools validate the deterministic filesystem map and the pipeline graph without Gradle or Minecraft.

**Tech Stack:** Markdown, PowerShell 7, Git, existing Forge repository metadata.

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

- [ ] **Step 1: Write the claim ledger with the budget-held empty state.**

  Use this exact table header and an explicit statement that no work may be claimed while the budget hold remains active:

  ```markdown
  | Work item | Lane | Owner | Worktree or branch | Intended paths | Claimed | Last update | Dependency status | Release condition |
  |---|---|---|---|---|---|---|---|---|
  | None | — | — | — | — | — | — | Budget hold | Project owner clears the hold |
  ```

- [ ] **Step 2: Add the handoff archive contract and the FND-03 record.**

  The archive contract requires this exact field sequence: `Work item`, `Lane`, `Worktree or branch`, `Commit`, `Scope`, `Parity reference`, `Source evidence`, `Automated evidence`, `Runtime evidence`, `Files changed`, `Dependencies or follow-ups`, and `Coordinator disposition`. The `FND-03` record names commit `a908d65`, links `parity-evidence-index.md`, records no runtime/game test because the item is documentation-only, and sets disposition to `DONE`.

- [ ] **Step 3: Connect the records to agent startup and state transitions.**

  Update `AGENTS.md` so a worker reads `active-claims.md` before claiming, the coordinator records a claim before editing, and `ACTIVE`, `VERIFYING`, and `DONE` workboard rows link a handoff path. Add a `PLY` lane for player equipment, effects, Warp, and Warp Ward. Update the pipeline README table and workflow to link the two new artifacts. Add a `Handoff` column to the workboard and populate FND-03 with `handoffs/FND-03.md`; leave non-terminal rows as `—`.

- [ ] **Step 4: Validate the record format.**

  Run:

  ```powershell
  rg -n "Work item \| Lane \| Owner|Budget hold|a908d65|handoffs/FND-03\.md" AGENTS.md docs/agent-pipeline
  git diff --check
  ```

  Expected: all four markers appear and Git reports no whitespace errors.

- [ ] **Step 5: Commit the control records.**

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
- Produces: feature rows with `Feature group`, `BETA26 reference`, `Current port entry points`, `Owning work item`, `Evidence state`, and `Release gate`.
- Produces: entrypoint rows with `Work item`, `Current port roots`, `Legacy reference roots`, `Registration or data roots`, and `Expected test type`.

- [ ] **Step 1: Add the five missing workboard owners before creating the matrix.**

  Add `FND-04` after FND-03: `Establish authoritative aspect attribution, containers, and lookup shared by scanning and Essentia`, blocked by FND-01. Change RSR-01 and ALC-01 to be blocked by FND-04. Add `ALC-00` before ALC-01: `Restore Crucible recipes, input validation, aspect costs, and output economy`, blocked by RSR-02. Add `AUT-04`: `Restore standalone artifice and utility devices`, blocked by FND-02. Add `WLD-05`: `Validate creature spawning, AI, combat, drops, constructs, and non-focus projectiles`, blocked by FND-02. Add `PLY-01`: `Restore player equipment, Curios state, effects, Warp, and Warp Ward`, blocked by RSR-03. Change WLD-04 to be blocked by PLY-01. Add matching deferred-register rows, dependency-spine edges, and evidence-index queue rows that name the BETA26 behavior to capture for each new item.

- [ ] **Step 2: Create the feature matrix with these exact initial groups.**

  Create one row for each of: `Foundations`, `Aspects and research`, `Arcane crafting`, `Crucible alchemy`, `Essentia production and transport`, `Infusion`, `Casting and foci`, `Golems and automation`, `Artifice and utility devices`, `Aura, Flux, rifts, and taint`, `Magical world generation and flora`, `Creatures and combat`, `Player systems and Warp`, `Eldritch and endgame`, `Client presentation`, and `Compatibility and release`. Map them respectively to `FND-01/FND-02`, `FND-04/RSR-01/RSR-03`, `RSR-02`, `ALC-00`, `ALC-01/ALC-02/ALC-04`, `ALC-03`, `CAS-01/CAS-03`, `AUT-01/AUT-03`, `AUT-04`, `WLD-01/WLD-03`, `WLD-02`, `WLD-05`, `PLY-01`, `WLD-04`, `CLI-01/CLI-03`, and `REL-01/REL-04`.

- [ ] **Step 3: Create the entrypoint map from verifiable roots.**

  Include rows for all 32 canonical work items. Use current root paths such as `src/main/java/thaumcraft/api/research`, `src/main/java/thaumcraft/common/tiles`, `src/main/java/thaumcraft/common/golems`, `src/main/java/thaumcraft/common/world`, `src/main/java/thaumcraft/client`, `src/main/resources/data/thaumcraft`, and `src/main/resources/assets/thaumcraft`. For legacy paths whose exact location is not yet established, use the literal value `needs discovery from src/main/java_old/`; do not fabricate class paths.

- [ ] **Step 4: Link the inventory artifacts from the pipeline README.**

  Add concise table rows explaining that the feature matrix measures parity coverage and the entrypoint map shortens safe agent handoffs.

- [ ] **Step 5: Validate ownership coverage.**

  Run:

  ```powershell
  rg -n "^\| (Foundations|Aspects and research|Arcane crafting|Crucible alchemy|Essentia production and transport|Infusion|Casting and foci|Golems and automation|Artifice and utility devices|Aura, Flux, rifts, and taint|Magical world generation and flora|Creatures and combat|Player systems and Warp|Eldritch and endgame|Client presentation|Compatibility and release) \|" docs/agent-pipeline/tc6-feature-matrix.md
  rg -n "^\| (FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-[0-9]{2} \|" docs/agent-pipeline/system-entrypoints.md
  git diff --check
  ```

  Expected: 16 feature-group rows, 32 entrypoint rows, and no whitespace errors.

- [ ] **Step 6: Commit the inventory maps.**

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

- [ ] **Step 1: Create the reference catalog template.**

  Include the fields `Reference identifier`, `Minecraft version`, `Forge version`, `Thaumcraft version`, `Artifact location`, `SHA-256`, `Launcher or profile`, `World seed`, `Player setup`, `Capture scenario`, `Recorded by`, and `Date recorded`. State that entries record metadata only and never store original binaries, assets, decompiled files, or recordings in the repository.

- [ ] **Step 2: Create the validation matrix.**

  Define these rows: `Documentation-only`, `Pure deterministic logic`, `Server gameplay`, `Client presentation`, `Persistence`, `Multiplayer`, and `Release acceptance`. For each row name the required command or scenario, evidence location, clean-state requirement, and pass condition. Documentation-only uses `git diff --check` and the pipeline validator; server gameplay names GameTest plus fresh-world/reload; client presentation names a client smoke scenario; multiplayer names dedicated server plus remote client; release acceptance names all validation tiers.

- [ ] **Step 3: Create the budget hold policy.**

  Begin the policy with the exact sentence `Implementation is paused while the budget hold is active.` State that the project owner is the release authority; allowed activities are source reading, parity evidence cataloging, documentation, and local non-game pipeline validation; prohibited activities are gameplay changes, Gradle/game runs, EULA acceptance, distribution, and original asset copying. The reactivation condition is an explicit project-owner instruction. List spending order as `FND-01`, `FND-04`, `RSR-01/RSR-02`, `ALC-00/ALC-03`, `CAS-01/CAS-02`, then remaining workboard order.

- [ ] **Step 4: Link the new policy and validation artifacts.**

  Add README table rows for the catalog, validation matrix, and budget hold. Replace the deferred-issues opening budget sentence with a link to `budget-hold.md`.

- [ ] **Step 5: Validate the controls.**

  Run:

  ```powershell
  rg -n "SHA-256|Documentation-only|Project owner|FND-01" docs/agent-pipeline/reference-catalog.md docs/agent-pipeline/validation-matrix.md docs/agent-pipeline/budget-hold.md
  git diff --check
  ```

  Expected: each policy marker occurs in its named artifact and Git reports no whitespace errors.

- [ ] **Step 6: Commit the policy controls.**

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

- [ ] **Step 1: Write a failing temporary-fixture check.**

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

- [ ] **Step 2: Add deterministic renderer and `-Check`.**

  Change the parameter block to:

  ```powershell
  [CmdletBinding()]
  param(
      [string]$RepositoryRoot = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),
      [switch]$Check
  )
  ```

  Remove the generated timestamp and `git rev-parse` variables. Move the existing here-string construction into `Get-FileSystemMapContent`, which returns the expected map body. Render a fixed header that says `Generated from the current source layout by tools/agent-pipeline/Update-FileSystemMap.ps1.` Normalize the rendered content with:

  ```powershell
  $content = $content.TrimEnd() + [Environment]::NewLine
  ```

  Define this normalizer before the write/check branch:

  ```powershell
  function Normalize-MapText([string]$value) {
      (($value -replace "`r`n", "`n") -replace "`r", "`n").TrimEnd("`n")
  }
  ```

  Replace the final write with:

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

  Set-Content -LiteralPath $outputPath -Value $content -Encoding utf8 -NoNewline
  Write-Host "Updated $outputPath"
  ```

  Change the test-surface wording from `$testCount tracked test-source/resource files` to `$testCount test-source/resource files`.

- [ ] **Step 3: Regenerate and prove the check behavior.**

  Run:

  ```powershell
  .\tools\agent-pipeline\Update-FileSystemMap.ps1
  .\tools\agent-pipeline\Update-FileSystemMap.ps1 -Check
  & .\tools\agent-pipeline\Update-FileSystemMap.ps1 -RepositoryRoot $fixture
  & .\tools\agent-pipeline\Update-FileSystemMap.ps1 -RepositoryRoot $fixture -Check
  ```

  Then add a temporary file under the fixture's `src/main/java/thaumcraft/common/`, invoke `-Check` against the fixture, and confirm it fails with `Filesystem map is stale`. Remove the fixture in the same PowerShell session.

- [ ] **Step 4: Commit the deterministic map.**

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
- Produces: exit success after valid checks; throws on missing artifact, stale map, duplicate work-item ID, missing internal dependency, dependency cycle, or `READY` state during a budget hold.

- [ ] **Step 1: Write the failing invalid-state fixture.**

  Create `tools/agent-pipeline/test/Invoke-PipelineValidatorFixture.ps1`. It copies `AGENTS.md`, `docs/agent-pipeline`, `tools/agent-pipeline`, `src/main/java/thaumcraft`, and `src/main/resources` into a temporary fixture; changes only the FND-01 workboard cell from `DEFERRED: budget hold` to `READY`; invokes `Test-AgentPipeline.ps1 -RepositoryRoot <fixture>`; and throws unless the captured error contains `READY item exists during budget hold`. Before `Test-AgentPipeline.ps1` exists, the fixture script must fail because the validator path is absent.

- [ ] **Step 2: Implement the validator.**

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

  Require the pipeline files named by the specification, invoke `Update-FileSystemMap.ps1 -Check`, collect IDs, compare `($ids | Select-Object -Unique).Count` to `$ids.Count`, and verify every canonical row has a nonempty state, outcome, primary-area, acceptance-evidence, and handoff column. Accept only the state forms `READY`, `ACTIVE`, `VERIFYING`, `DONE`, `DEFERRED: <reason>`, `BLOCKED: <canonical ID>`, `BLOCKED: all gameplay lanes`, and `BLOCKED: written permission`. Verify every internal dependency target appears in `$ids`, every deferred canonical ID occurs in `deferred-issues.md`, and every `ACTIVE`, `VERIFYING`, or `DONE` row names an existing handoff file. Use a DFS with `visiting` and `visited` hash sets to throw `Dependency cycle detected at <ID>` on a back-edge. Detect the active budget hold with the exact phrase `Implementation is paused` in `budget-hold.md`; while it exists, throw `READY item exists during budget hold: <ID>` for any workboard row matching `^\|\s*(?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2}\s*\|\s*READY\s*\|`. Print state counts and the informational external-gate line before returning success.

- [ ] **Step 3: Prove success and each failure class.**

  Run the validator against the repository. In isolated fixtures, introduce one fault at a time: delete `active-claims.md`; alter the map; duplicate FND-01; change `BLOCKED: FND-01` to `BLOCKED: XYZ-99`; set `FND-01` to `BLOCKED: RSR-02` and `RSR-02` to `BLOCKED: FND-01`; remove the FND-03 handoff; then set FND-01 to `READY`. Require a nonzero exit for every fixture and retain no fixture afterward.

- [ ] **Step 4: Link and commit the validator.**

  Add `Test-AgentPipeline.ps1` to the README artifact table and list the command `.\tools\agent-pipeline\Test-AgentPipeline.ps1` under local pipeline validation. Then run:

  ```powershell
  .\tools\agent-pipeline\Test-AgentPipeline.ps1
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
  .\tools\agent-pipeline\Update-FileSystemMap.ps1 -Check
  .\tools\agent-pipeline\Test-AgentPipeline.ps1
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
