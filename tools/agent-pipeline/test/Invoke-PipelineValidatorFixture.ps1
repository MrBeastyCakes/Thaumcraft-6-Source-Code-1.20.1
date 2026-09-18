# Fixture for the READY-during-budget-hold failure class of Test-AgentPipeline.ps1.
#
# The fixture copies AGENTS.md, docs/agent-pipeline, tools/agent-pipeline,
# src/main/java/thaumcraft, and src/main/resources into a fresh temporary root,
# regenerates that root's own filesystem map, and changes ONLY the FND-01
# workboard cell from 'DEFERRED: budget hold' to 'READY'. It then runs the
# validator against the temporary root in a child process so the validator's
# exit code is observable, and it fails unless the validator exits nonzero and
# reports the budget-hold failure. Before Test-AgentPipeline.ps1 exists the
# fixture fails because the validator path is absent.

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

$fixture = Join-Path ([System.IO.Path]::GetTempPath()) 'thaumcraft-pipeline-validator-fixture'

try {
    if (Test-Path -LiteralPath $fixture) {
        Remove-Item -LiteralPath $fixture -Recurse -Force
    }

    $copiedItems = @(
        'AGENTS.md',
        'docs/agent-pipeline',
        'tools/agent-pipeline',
        'src/main/java/thaumcraft',
        'src/main/resources'
    )
    foreach ($relativePath in $copiedItems) {
        $source = Join-Path $RepositoryRoot $relativePath
        if (-not (Test-Path -LiteralPath $source)) {
            throw "Fixture source is missing: $source"
        }
        $target = Join-Path $fixture $relativePath
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
        Copy-Item -LiteralPath $source -Destination $target -Recurse -Force
    }
    Write-Host "Fixture built at: $fixture"

    # The fixture must render its own map before fault injection; the copied map
    # would otherwise be stale for unrelated reasons (fixture root leaf name and
    # the directories the fixture does not copy).
    $mapUpdater = Join-Path $fixture 'tools/agent-pipeline/Update-FileSystemMap.ps1'
    & $mapUpdater -RepositoryRoot $fixture
    Write-Host 'Regenerated the fixture filesystem map.'

    $workboardPath = Join-Path $fixture 'docs/agent-pipeline/parity-workboard.md'
    $workboard = Get-Content -LiteralPath $workboardPath -Raw -Encoding UTF8
    $updated = $workboard -replace '(?m)^\| FND-01 \| DEFERRED: budget hold \|', '| FND-01 | READY |'
    if ($updated -eq $workboard) {
        throw 'Fault injection did not change the FND-01 workboard row.'
    }
    [System.IO.File]::WriteAllText($workboardPath, $updated, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host "Injected fault: FND-01 state 'DEFERRED: budget hold' changed to 'READY'."

    $validatorPath = Join-Path $fixture 'tools/agent-pipeline/Test-AgentPipeline.ps1'
    $childPowerShell = Join-Path $PSHOME 'powershell.exe'
    Write-Host "Running validator as a child process: $validatorPath"

    # A missing -File target writes to stderr; capture it without terminating.
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $validatorOutput = & $childPowerShell -NoProfile -ExecutionPolicy Bypass -File $validatorPath -RepositoryRoot $fixture 2>&1 | Out-String
        $validatorExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    if ($null -eq $validatorExitCode -or $validatorExitCode -eq 0) {
        throw "Fixture failure: the validator exited with code $validatorExitCode; a nonzero exit was expected. Output: $validatorOutput"
    }
    if ($validatorOutput -notmatch 'READY item exists during budget hold') {
        throw "Fixture failure: expected the validator to report 'READY item exists during budget hold'. Exit code: $validatorExitCode. Output: $validatorOutput"
    }

    Write-Host 'PASS: validator failed as expected while the budget hold is active.'
    Write-Host "Validator exit code: $validatorExitCode"
    Write-Host "Validator output: $($validatorOutput.Trim())"
} finally {
    if (Test-Path -LiteralPath $fixture) {
        Remove-Item -LiteralPath $fixture -Recurse -Force -ErrorAction SilentlyContinue
    }
}
