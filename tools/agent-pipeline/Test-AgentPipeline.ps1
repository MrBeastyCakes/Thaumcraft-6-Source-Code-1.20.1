# Local structural validator for the agent pipeline. It performs no writes and
# needs no Gradle or game instance.
#
# Checks:
#   1. every required pipeline artifact exists;
#   2. the filesystem map is current (Update-FileSystemMap.ps1 -Check);
#   3. canonical work-item IDs are collected from the workboard with no duplicates;
#   4. every canonical workboard row has nonempty state, outcome, primary-area,
#      acceptance-evidence, and handoff columns;
#   5. every state uses an accepted form;
#   6. every internal BLOCKED: <ID> target exists;
#   7. every work-item ID referenced in deferred-issues.md is canonical, and
#      every DEFERRED:-state item is registered there;
#   8. every ACTIVE, VERIFYING, or DONE row names an existing handoff file;
#   9. there are no dependency cycles among canonical IDs;
#  10. no READY item exists while the budget hold says implementation is paused.
#
# Exit code is 0 on success and 1 on any structural failure.

[CmdletBinding()]
param(
    [string]$RepositoryRoot
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrEmpty($RepositoryRoot)) {
    # Windows PowerShell 5.1 cannot use $PSScriptRoot in a parameter default:
    # it is empty during parameter binding. Resolve the repository root here.
    $RepositoryRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}

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

function Visit-Dependency([string]$Node, [hashtable]$Edges, [System.Collections.Generic.HashSet[string]]$Visiting, [System.Collections.Generic.HashSet[string]]$Visited) {
    if ($Visited.Contains($Node)) { return }
    if ($Visiting.Contains($Node)) { throw "Dependency cycle detected at $Node" }
    [void]$Visiting.Add($Node)
    if ($Edges.ContainsKey($Node)) {
        Visit-Dependency -Node $Edges[$Node] -Edges $Edges -Visiting $Visiting -Visited $Visited
    }
    [void]$Visiting.Remove($Node)
    [void]$Visited.Add($Node)
}

$repository = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$pipelineRoot = Join-Path $repository 'docs/agent-pipeline'
$workboardPath = Join-Path $pipelineRoot 'parity-workboard.md'
$deferredIssuesPath = Join-Path $pipelineRoot 'deferred-issues.md'
$budgetHoldPath = Join-Path $pipelineRoot 'budget-hold.md'
$mapUpdaterPath = Join-Path $repository 'tools/agent-pipeline/Update-FileSystemMap.ps1'

try {
    $requiredArtifacts = @(
        'AGENTS.md',
        'docs/agent-pipeline/README.md',
        'docs/agent-pipeline/active-claims.md',
        'docs/agent-pipeline/filesystem-map.md',
        'docs/agent-pipeline/parity-workboard.md',
        'docs/agent-pipeline/parity-evidence-index.md',
        'docs/agent-pipeline/deferred-issues.md',
        'docs/agent-pipeline/tc6-feature-matrix.md',
        'docs/agent-pipeline/system-entrypoints.md',
        'docs/agent-pipeline/reference-catalog.md',
        'docs/agent-pipeline/validation-matrix.md',
        'docs/agent-pipeline/budget-hold.md',
        'docs/agent-pipeline/handoffs/README.md',
        'tools/agent-pipeline/Update-FileSystemMap.ps1'
    )
    foreach ($relativePath in $requiredArtifacts) {
        Require-Path (Join-Path $repository $relativePath)
    }
    if (-not [string]::IsNullOrEmpty($PSCommandPath)) {
        Require-Path $PSCommandPath
    }

    & $mapUpdaterPath -RepositoryRoot $repository -Check

    $workboardLines = @(Get-Content -LiteralPath $workboardPath -Encoding UTF8)
    $ids = @(Get-WorkItemIds $workboardLines)
    if ($ids.Count -eq 0) {
        throw "No canonical work-item IDs found in $workboardPath"
    }

    $duplicate = $ids | Group-Object | Where-Object { $_.Count -gt 1 } | Select-Object -First 1
    if ($null -ne $duplicate) {
        throw "Duplicate work-item ID: $($duplicate.Name)"
    }

    $idSet = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($id in $ids) { [void]$idSet.Add($id) }

    $stateById = @{}
    $handoffById = @{}
    foreach ($line in $workboardLines) {
        if ($line -match '^\|\s*((?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2})\s*\|') {
            $rowId = $Matches[1]
            $cells = @($line -split '\|')
            if ($cells.Count -lt 8) {
                throw "Work item row is missing a required column value: $rowId"
            }
            foreach ($cellIndex in 1..6) {
                if ([string]::IsNullOrWhiteSpace($cells[$cellIndex])) {
                    throw "Work item row is missing a required column value: $rowId"
                }
            }
            $stateById[$rowId] = $cells[2].Trim()
            $handoffById[$rowId] = $cells[6].Trim()
        }
    }

    $simpleStates = @('READY', 'ACTIVE', 'VERIFYING', 'DONE')
    foreach ($id in $ids) {
        $state = $stateById[$id]
        if ($simpleStates -ccontains $state) { continue }
        if ($state -match '^DEFERRED:\s*\S+') { continue }
        if ($state -match '^BLOCKED:\s*all gameplay lanes$') { continue }
        if ($state -match '^BLOCKED:\s*written permission$') { continue }
        if ($state -match '^BLOCKED:\s*[A-Z]{3}-\d{2}$') { continue }
        throw "Invalid work-item state: $id has '$state'"
    }

    foreach ($id in $ids) {
        $state = $stateById[$id]
        if ($state -match '^BLOCKED:\s*(.+)$') {
            $target = $Matches[1].Trim()
            if ($target -eq 'all gameplay lanes' -or $target -eq 'written permission') { continue }
            if (-not $idSet.Contains($target)) {
                throw "Missing internal dependency: $target"
            }
        }
    }

    $deferredText = Get-Content -LiteralPath $deferredIssuesPath -Raw -Encoding UTF8
    $referencedIds = [regex]::Matches($deferredText, '(?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d+[a-z]?')
    foreach ($referencedId in $referencedIds) {
        if (-not $idSet.Contains($referencedId.Value)) {
            throw "Noncanonical work-item ID referenced in deferred-issues.md: $($referencedId.Value)"
        }
    }
    foreach ($id in $ids) {
        if ($stateById[$id] -match '^DEFERRED:') {
            if ($deferredText -notmatch "\b$id\b") {
                throw "Deferred work item missing from deferred-issues.md: $id"
            }
        }
    }

    $terminalStates = @('ACTIVE', 'VERIFYING', 'DONE')
    foreach ($id in $ids) {
        if ($terminalStates -ccontains $stateById[$id]) {
            $handoffPath = Join-Path $pipelineRoot $handoffById[$id]
            if (-not (Test-Path -LiteralPath $handoffPath -PathType Leaf)) {
                throw "Missing handoff file: $handoffPath"
            }
        }
    }

    $edges = Get-InternalDependencies $workboardLines
    $visiting = New-Object 'System.Collections.Generic.HashSet[string]'
    $visited = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($id in $ids) {
        Visit-Dependency -Node $id -Edges $edges -Visiting $visiting -Visited $visited
    }

    $budgetHoldText = Get-Content -LiteralPath $budgetHoldPath -Raw -Encoding UTF8
    if ($budgetHoldText -match 'Implementation is paused') {
        foreach ($line in $workboardLines) {
            if ($line -match '^\|\s*((?:FND|RSR|ALC|CAS|AUT|WLD|PLY|CLI|REL)-\d{2})\s*\|\s*READY\s*\|') {
                throw "READY item exists during budget hold: $($Matches[1])"
            }
        }
    }
} catch {
    Write-Host "Agent pipeline validation failed: $($_.Exception.Message)"
    exit 1
}

$counts = @{}
foreach ($id in $ids) {
    $state = $stateById[$id]
    $family = $state
    if ($state -match '^DEFERRED:') { $family = 'DEFERRED' }
    elseif ($state -match '^BLOCKED:') { $family = 'BLOCKED' }
    if (-not $counts.ContainsKey($family)) { $counts[$family] = 0 }
    $counts[$family] = $counts[$family] + 1
}
$countParts = @()
foreach ($family in @('READY', 'ACTIVE', 'VERIFYING', 'DONE', 'DEFERRED', 'BLOCKED')) {
    $value = if ($counts.ContainsKey($family)) { $counts[$family] } else { 0 }
    $countParts += "$family=$value"
}
Write-Host "Work item states: $($countParts -join ', ')"
Write-Host 'External gates (informational): BLOCKED: all gameplay lanes; BLOCKED: written permission'
exit 0
