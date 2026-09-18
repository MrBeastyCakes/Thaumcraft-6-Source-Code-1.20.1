# Fixture runner for every failure class of Test-AgentPipeline.ps1, plus a
# no-fault control case.
#
# Each case copies AGENTS.md, docs/agent-pipeline, tools/agent-pipeline,
# src/main/java/thaumcraft, and src/main/resources into its own fresh unique
# temporary root, regenerates that root's own filesystem map (so the map check
# cannot mask the intended fault), and then injects exactly ONE fault, asserting
# that the injection actually changed the targeted file. The case then runs the
# fixture's validator in a child process so the validator's exit code is
# observable, and fails unless the exit code and the reported message match the
# expected failure class. Every fixture root is removed in a finally-style block
# whether the case passed or failed.
#
# Cases and expected validator outcomes:
#   CONTROL          no fault                           -> exit 0, state-counts line
#   READY-HOLD       FND-01 state -> READY              -> 'READY item exists during budget hold'
#   STALE-MAP        one byte appended to the map       -> 'Filesystem map is stale'
#   DUPLICATE-ID     a second FND-01 row appended       -> 'Duplicate work-item ID'
#   MISSING-DEP      FND-02 -> BLOCKED: XYZ-99          -> 'Missing internal dependency'
#   CYCLE            FND-01 and RSR-02 block each other -> 'Dependency cycle detected at'
#   MISSING-ARTIFACT active-claims.md deleted           -> 'Required pipeline artifact is missing'
#   MISSING-HANDOFF  handoffs/FND-03.md deleted         -> 'Missing handoff file'
#
# The runner prints one PASS/FAIL line per case and a final summary; it exits 0
# only when every case passes, and exits 1 otherwise.

[CmdletBinding()]
param(
    [string]$RepositoryRoot
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrEmpty($RepositoryRoot)) {
    # Windows PowerShell 5.1 cannot use $PSScriptRoot in a parameter default:
    # it is empty during parameter binding. Resolve the repository root here.
    $RepositoryRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
}

$copiedItems = @(
    'AGENTS.md',
    'docs/agent-pipeline',
    'tools/agent-pipeline',
    'src/main/java/thaumcraft',
    'src/main/resources'
)

$childPowerShell = Join-Path $PSHOME 'powershell.exe'

function Invoke-FixtureCase {
    param(
        [string]$Name,
        [int]$ExpectedExitCode,
        [string]$ExpectedMessage,
        [scriptblock]$InjectFault
    )

    $fixture = Join-Path ([System.IO.Path]::GetTempPath()) "thaumcraft-pipeline-fixture-$Name-$([guid]::NewGuid().ToString('N'))"
    try {
        New-Item -ItemType Directory -Force -Path $fixture | Out-Null
        foreach ($relativePath in $copiedItems) {
            $source = Join-Path $RepositoryRoot $relativePath
            if (-not (Test-Path -LiteralPath $source)) {
                throw "Fixture source is missing: $source"
            }
            $target = Join-Path $fixture $relativePath
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
            Copy-Item -LiteralPath $source -Destination $target -Recurse -Force
        }

        $mapUpdater = Join-Path $fixture 'tools/agent-pipeline/Update-FileSystemMap.ps1'
        & $mapUpdater -RepositoryRoot $fixture 6>$null

        if ($null -ne $InjectFault) {
            & $InjectFault $fixture
        }

        $validatorPath = Join-Path $fixture 'tools/agent-pipeline/Test-AgentPipeline.ps1'

        # A missing -File target writes to stderr; capture it without terminating.
        $previousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            $validatorOutput = & $childPowerShell -NoProfile -ExecutionPolicy Bypass -File $validatorPath -RepositoryRoot $fixture 2>&1 | Out-String
            $validatorExitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $previousErrorActionPreference
        }

        if ($validatorExitCode -ne $ExpectedExitCode) {
            return "expected exit code $ExpectedExitCode but the validator exited with $validatorExitCode. Validator output: $($validatorOutput.Trim())"
        }
        if (-not [string]::IsNullOrEmpty($ExpectedMessage) -and $validatorOutput -notmatch $ExpectedMessage) {
            return "expected the validator to report '$ExpectedMessage'. Validator output: $($validatorOutput.Trim())"
        }
        return ''
    } catch {
        return "case execution failed: $($_.Exception.Message)"
    } finally {
        if (Test-Path -LiteralPath $fixture) {
            Remove-Item -LiteralPath $fixture -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

$cases = @(
    @{
        Name = 'CONTROL'
        ExpectedExitCode = 0
        ExpectedMessage = 'Work item states: READY=\d+, ACTIVE=\d+, VERIFYING=\d+, DONE=\d+, DEFERRED=\d+, BLOCKED=\d+'
        InjectFault = $null
    },
    @{
        Name = 'READY-HOLD'
        ExpectedExitCode = 1
        ExpectedMessage = 'READY item exists during budget hold'
        InjectFault = {
            param($fixture)
            $workboardPath = Join-Path $fixture 'docs/agent-pipeline/parity-workboard.md'
            $workboard = Get-Content -LiteralPath $workboardPath -Raw -Encoding UTF8
            $updated = $workboard -replace '(?m)^\| FND-01 \| DEFERRED: budget hold \|', '| FND-01 | READY |'
            if ($updated -eq $workboard) {
                throw 'Fault injection did not change the FND-01 workboard row.'
            }
            [System.IO.File]::WriteAllText($workboardPath, $updated, (New-Object System.Text.UTF8Encoding($false)))
        }
    },
    @{
        Name = 'STALE-MAP'
        ExpectedExitCode = 1
        ExpectedMessage = 'Filesystem map is stale'
        InjectFault = {
            param($fixture)
            $mapPath = Join-Path $fixture 'docs/agent-pipeline/filesystem-map.md'
            $before = (Get-Item -LiteralPath $mapPath).Length
            [System.IO.File]::AppendAllText($mapPath, 'x')
            $after = (Get-Item -LiteralPath $mapPath).Length
            if ($after -ne ($before + 1)) {
                throw 'Fault injection did not append exactly one byte to the fixture filesystem map.'
            }
        }
    },
    @{
        Name = 'DUPLICATE-ID'
        ExpectedExitCode = 1
        ExpectedMessage = 'Duplicate work-item ID'
        InjectFault = {
            param($fixture)
            $workboardPath = Join-Path $fixture 'docs/agent-pipeline/parity-workboard.md'
            $workboard = Get-Content -LiteralPath $workboardPath -Raw -Encoding UTF8
            $match = [regex]::Match($workboard, '(?m)^\|\s*FND-01\s*\|.*$')
            if (-not $match.Success) {
                throw 'Fault injection could not locate the FND-01 workboard row.'
            }
            $updated = $workboard.TrimEnd() + [Environment]::NewLine + $match.Value + [Environment]::NewLine
            if ($updated -eq $workboard) {
                throw 'Fault injection did not change the workboard file.'
            }
            [System.IO.File]::WriteAllText($workboardPath, $updated, (New-Object System.Text.UTF8Encoding($false)))
        }
    },
    @{
        Name = 'MISSING-DEP'
        ExpectedExitCode = 1
        ExpectedMessage = 'Missing internal dependency'
        InjectFault = {
            param($fixture)
            $workboardPath = Join-Path $fixture 'docs/agent-pipeline/parity-workboard.md'
            $workboard = Get-Content -LiteralPath $workboardPath -Raw -Encoding UTF8
            $updated = $workboard -replace '(?m)^\| FND-02 \| BLOCKED: FND-01 \|', '| FND-02 | BLOCKED: XYZ-99 |'
            if ($updated -eq $workboard) {
                throw 'Fault injection did not change the FND-02 workboard row.'
            }
            [System.IO.File]::WriteAllText($workboardPath, $updated, (New-Object System.Text.UTF8Encoding($false)))
        }
    },
    @{
        Name = 'CYCLE'
        ExpectedExitCode = 1
        ExpectedMessage = 'Dependency cycle detected at'
        InjectFault = {
            param($fixture)
            $workboardPath = Join-Path $fixture 'docs/agent-pipeline/parity-workboard.md'
            $workboard = Get-Content -LiteralPath $workboardPath -Raw -Encoding UTF8
            $first = $workboard -replace '(?m)^\| FND-01 \| DEFERRED: budget hold \|', '| FND-01 | BLOCKED: RSR-02 |'
            if ($first -eq $workboard) {
                throw 'Fault injection did not change the FND-01 workboard row.'
            }
            $second = $first -replace '(?m)^\| RSR-02 \| BLOCKED: RSR-01 \|', '| RSR-02 | BLOCKED: FND-01 |'
            if ($second -eq $first) {
                throw 'Fault injection did not change the RSR-02 workboard row.'
            }
            [System.IO.File]::WriteAllText($workboardPath, $second, (New-Object System.Text.UTF8Encoding($false)))
        }
    },
    @{
        Name = 'MISSING-ARTIFACT'
        ExpectedExitCode = 1
        ExpectedMessage = 'Required pipeline artifact is missing'
        InjectFault = {
            param($fixture)
            $claimsPath = Join-Path $fixture 'docs/agent-pipeline/active-claims.md'
            if (-not (Test-Path -LiteralPath $claimsPath -PathType Leaf)) {
                throw 'Fault injection could not find active-claims.md.'
            }
            Remove-Item -LiteralPath $claimsPath -Force
            if (Test-Path -LiteralPath $claimsPath) {
                throw 'Fault injection did not delete active-claims.md.'
            }
        }
    },
    @{
        Name = 'MISSING-HANDOFF'
        ExpectedExitCode = 1
        ExpectedMessage = 'Missing handoff file'
        InjectFault = {
            param($fixture)
            $handoffPath = Join-Path $fixture 'docs/agent-pipeline/handoffs/FND-03.md'
            if (-not (Test-Path -LiteralPath $handoffPath -PathType Leaf)) {
                throw 'Fault injection could not find handoffs/FND-03.md.'
            }
            Remove-Item -LiteralPath $handoffPath -Force
            if (Test-Path -LiteralPath $handoffPath) {
                throw 'Fault injection did not delete handoffs/FND-03.md.'
            }
        }
    }
)

$passedCount = 0
$failedCases = @()
foreach ($case in $cases) {
    $failure = Invoke-FixtureCase -Name $case.Name -ExpectedExitCode $case.ExpectedExitCode -ExpectedMessage $case.ExpectedMessage -InjectFault $case.InjectFault
    if ([string]::IsNullOrEmpty($failure)) {
        $passedCount++
        Write-Host "PASS: $($case.Name)"
    } else {
        Write-Host "FAIL: $($case.Name) - $failure"
        $failedCases += $case.Name
    }
}

Write-Host "Fixture cases passed: $passedCount/$($cases.Count)"
if ($failedCases.Count -gt 0) {
    Write-Host "Failed cases: $($failedCases -join ', ')"
    exit 1
}
exit 0
