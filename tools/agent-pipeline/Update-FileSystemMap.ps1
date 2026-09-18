[CmdletBinding()]
param(
    [string]$RepositoryRoot,
    [switch]$Check
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrEmpty($RepositoryRoot)) {
    # On Windows PowerShell 5.1 an advanced script's parameter defaults are
    # evaluated before $PSScriptRoot exists, so resolve the default here.
    $RepositoryRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}

$repository = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$sourceRoot = Join-Path $repository 'src/main/java/thaumcraft'
$legacySourceRoot = Join-Path $repository 'src/main/java_old'
$resourceRoot = Join-Path $repository 'src/main/resources'
$templateRoot = Join-Path $repository 'src/main/templates'
$generatedResourceRoot = Join-Path $repository 'src/generated/resources'
$outputPath = Join-Path $repository 'docs/agent-pipeline/filesystem-map.md'

foreach ($requiredPath in @($sourceRoot, $resourceRoot)) {
    if (-not (Test-Path -LiteralPath $requiredPath -PathType Container)) {
        throw "Required source path does not exist: $requiredPath"
    }
}

function Get-FileCount([string]$path) {
    @(Get-ChildItem -LiteralPath $path -File -Recurse -ErrorAction Stop).Count
}

function Get-ImmediateDirectoryRows([string]$path) {
    Get-ChildItem -LiteralPath $path -Directory |
        Sort-Object Name |
        ForEach-Object {
            "| ``$($_.Name)/`` | $(Get-FileCount $_.FullName) |"
        }
}

function Get-FileSystemMapContent {
    $javaRows = Get-ImmediateDirectoryRows $sourceRoot
    $commonRows = Get-ImmediateDirectoryRows (Join-Path $sourceRoot 'common')
    $assetPath = Join-Path $resourceRoot 'assets/thaumcraft'
    $dataPath = Join-Path $resourceRoot 'data/thaumcraft'
    $assetCount = Get-FileCount $assetPath
    $dataCount = Get-FileCount $dataPath
    $assetRows = Get-ImmediateDirectoryRows $assetPath
    $dataRows = Get-ImmediateDirectoryRows $dataPath
    $legacyCount = if (Test-Path -LiteralPath $legacySourceRoot -PathType Container) { Get-FileCount $legacySourceRoot } else { 0 }
    $templateCount = if (Test-Path -LiteralPath $templateRoot -PathType Container) { Get-FileCount $templateRoot } else { 0 }
    $generatedResourceCount = if (Test-Path -LiteralPath $generatedResourceRoot -PathType Container) { Get-FileCount $generatedResourceRoot } else { 0 }
    $testJavaPath = Join-Path $repository 'src/test/java'
    $testResourcePath = Join-Path $repository 'src/test/resources'
    $testCount = 0
    if (Test-Path -LiteralPath $testJavaPath) { $testCount += Get-FileCount $testJavaPath }
    if (Test-Path -LiteralPath $testResourcePath) { $testCount += Get-FileCount $testResourcePath }
    # Windows PowerShell 5.1 reads BOM-less script files as ANSI, so a literal
    # em dash here would corrupt the rendered map; build it from its code point.
    $emdash = [char]0x2014

    @"
# Filesystem Map

> Generated from the current source layout by ``tools/agent-pipeline/Update-FileSystemMap.ps1``.

Repository root: ``$(Split-Path -Leaf $repository)`` $emdash Minecraft 1.20.1 Forge port.

## Root and Build Surface

| Path | Role |
|---|---|
| ``build.gradle``, ``settings.gradle``, ``gradle.properties`` | ForgeGradle build, mappings, versions, and dependencies |
| ``gradlew``, ``gradlew.bat``, ``gradle/`` | Gradle wrapper |
| ``README.md`` | upstream project claims and setup notes; not parity evidence |
| ``TODO.md`` | upstream implementation leads; audit against source before acting |
| ``TEXTURETODO.md`` | visual asset backlog |
| ``AGENTS.md`` | parity operating contract |
| ``docs/agent-pipeline/`` | agent workflow, map, and workboard |
| ``tools/agent-pipeline/`` | pipeline maintenance tooling |
| ``src/main/java/`` | compiled 1.20.1 port source |
| ``src/main/java_old/`` | $legacyCount legacy reference files; do not compile or bulk-copy them |
| ``src/main/resources/`` | hand-authored assets, data, metadata, and language content |
| ``src/main/templates/`` | $templateCount build-time metadata templates |
| ``src/generated/resources/`` | $generatedResourceCount generated-resource files |

## Java Source $emdash ``src/main/java/thaumcraft``

| Package | Recursive file count |
|---|---:|
$($javaRows -join [Environment]::NewLine)

``Thaumcraft.java`` is the mod bootstrap. ``api/`` holds cross-system contracts; ``common/`` holds gameplay authority; ``client/`` is presentation-only; ``compat/`` holds integration adapters; ``init/`` owns registrations.

## Gameplay Implementation Seams $emdash ``src/main/java/thaumcraft/common``

| Package | Recursive file count |
|---|---:|
$($commonRows -join [Environment]::NewLine)

## Static Content $emdash ``src/main/resources``

| Path | Recursive file count |
|---|---:|
| ``assets/thaumcraft/`` | $assetCount |
| ``data/thaumcraft/`` | $dataCount |

### Asset Domains $emdash ``assets/thaumcraft``

| Directory | Recursive file count |
|---|---:|
$($assetRows -join [Environment]::NewLine)

### Data Domains $emdash ``data/thaumcraft``

| Directory | Recursive file count |
|---|---:|
$($dataRows -join [Environment]::NewLine)

## Test, Run, and Generated Surfaces

| Path | State / use |
|---|---|
| ``src/test/java``, ``src/test/resources`` | $testCount test-source/resource files |
| ``run/`` | disposable local Forge dev instance, configuration, logs, and test state |
| ``build/`` | disposable Gradle output, generated metadata, remapping intermediates, and JARs |
| ``.gradle/`` | disposable local Gradle cache/state |
| ``src/generated/resources/`` | generated-resource input path declared by the build |

``.gitignore`` excludes ``build/``, ``.gradle/``, ``run/``, IDE output, and Eclipse metadata. Treat those directories as generated/disposable and never use them as source-of-truth implementation files.

## Gradle Entry Points

| Command | Use |
|---|---|
| ``.\\gradlew.bat compileJava`` | Java compile gate |
| ``.\\gradlew.bat test`` | deterministic test suite |
| ``.\\gradlew.bat runGameTestServer`` | Minecraft GameTest validation |
| ``.\\gradlew.bat runServer`` | dedicated-server smoke test |
| ``.\\gradlew.bat runClient`` | client smoke and visual validation |
| ``.\\gradlew.bat runData`` | data-generation validation |
| ``.\\gradlew.bat build`` | package/reobfuscation gate |
"@
}

$content = Get-FileSystemMapContent
$content = $content.TrimEnd() + [Environment]::NewLine

function Normalize-MapText([string]$value) {
    $text = $value.TrimStart([char]0xFEFF)
    (($text -replace "`r`n", "`n") -replace "`r", "`n").TrimEnd("`n")
}

if ($Check) {
    if (-not (Test-Path -LiteralPath $outputPath -PathType Leaf)) {
        throw "Filesystem map is missing: $outputPath"
    }
    # -Encoding UTF8 is required on Windows PowerShell 5.1, whose default
    # Get-Content encoding is ANSI and would mojibake the UTF-8 map bytes.
    $actual = Get-Content -LiteralPath $outputPath -Raw -Encoding UTF8
    if ((Normalize-MapText $actual) -ne (Normalize-MapText $content)) {
        throw "Filesystem map is stale. Run tools/agent-pipeline/Update-FileSystemMap.ps1."
    }
    Write-Host "Filesystem map is current: $outputPath"
    return
}

[System.IO.File]::WriteAllText($outputPath, $content, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Updated $outputPath"
