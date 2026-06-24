[CmdletBinding()]
param(
    [string]$ModsDir = "C:\Users\Emmanuel Tremblay\AppData\Roaming\PrismLauncher\instances\1.21.1 TesT LaB\minecraft\mods",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.IO.Compression.FileSystem

$BuildRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildDir = Join-Path $BuildRoot "build"
$GradlePropertiesPath = Join-Path $BuildRoot "gradle.properties"
$ReportPath = Join-Path $BuildDir "install-report.json"

function Read-GradleProperties {
    param([string]$Path)

    $properties = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith("#")) {
            continue
        }
        $parts = $trimmed -split "=", 2
        if ($parts.Count -eq 2) {
            $properties[$parts[0].Trim()] = $parts[1].Trim()
        }
    }
    return $properties
}

function Get-JarMetadataText {
    param([string]$JarPath)

    $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $entry = $zip.GetEntry("META-INF/neoforge.mods.toml")
        if ($null -eq $entry) {
            return $null
        }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try {
            return $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    } finally {
        $zip.Dispose()
    }
}

function Get-PrimaryModBlock {
    param([string]$Metadata)

    if ($null -eq $Metadata) {
        return $null
    }
    $modsIndex = $Metadata.IndexOf("[[mods]]", [System.StringComparison]::Ordinal)
    if ($modsIndex -lt 0) {
        return $null
    }
    $block = $Metadata.Substring($modsIndex)
    $dependenciesIndex = $block.IndexOf("[[dependencies.", [System.StringComparison]::Ordinal)
    if ($dependenciesIndex -ge 0) {
        $block = $block.Substring(0, $dependenciesIndex)
    }
    return $block
}

function Test-JarModId {
    param(
        [string]$JarPath,
        [string]$ModId
    )

    $metadata = Get-JarMetadataText -JarPath $JarPath
    $primaryModBlock = Get-PrimaryModBlock -Metadata $metadata
    return $primaryModBlock -ne $null -and $primaryModBlock.Contains("modId=`"$ModId`"")
}

function Get-JarModVersion {
    param(
        [string]$JarPath,
        [string]$ModId
    )

    $metadata = Get-JarMetadataText -JarPath $JarPath
    $primaryModBlock = Get-PrimaryModBlock -Metadata $metadata
    if ($primaryModBlock -eq $null -or -not $primaryModBlock.Contains("modId=`"$ModId`"")) {
        return $null
    }
    $match = [regex]::Match($primaryModBlock, '(?m)^\s*version\s*=\s*"([^"]+)"')
    if ($match.Success) {
        return $match.Groups[1].Value
    }
    return $null
}

function Select-RuntimeJar {
    param(
        [string]$BuildRoot,
        [string]$ModId,
        [string]$ModVersion
    )

    $libsDir = Join-Path $BuildRoot "build\libs"
    if (-not (Test-Path -LiteralPath $libsDir)) {
        throw "Missing build libs directory: $libsDir"
    }

    $candidates = Get-ChildItem -LiteralPath $libsDir -Filter "*.jar" -File |
            Where-Object { $_.Name -notmatch '(?i)(sources|javadoc|dev|plain|test|tests|api)' }
    $matches = @($candidates | Where-Object {
                (Test-JarModId -JarPath $_.FullName -ModId $ModId) -and
                ((Get-JarModVersion -JarPath $_.FullName -ModId $ModId) -eq $ModVersion)
            })

    if ($matches.Count -ne 1) {
        throw "Expected exactly one runtime jar for $ModId $ModVersion, found $($matches.Count)."
    }
    return $matches[0]
}

if (-not (Test-Path -LiteralPath $GradlePropertiesPath)) {
    throw "Missing gradle.properties at $GradlePropertiesPath"
}

$properties = Read-GradleProperties -Path $GradlePropertiesPath
$ModId = $properties["mod_id"]
$ModName = $properties["mod_name"]
$ModVersion = $properties["mod_version"]

if ([string]::IsNullOrWhiteSpace($ModId) -or [string]::IsNullOrWhiteSpace($ModVersion)) {
    throw "gradle.properties must define mod_id and mod_version"
}

if (-not $SkipBuild) {
    & (Join-Path $BuildRoot "gradlew.bat") clean build --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
}

if (-not (Test-Path -LiteralPath $ModsDir)) {
    throw "Mods directory does not exist: $ModsDir"
}

$ResolvedModsDir = (Resolve-Path -LiteralPath $ModsDir).Path
$RuntimeJar = Select-RuntimeJar -BuildRoot $BuildRoot -ModId $ModId -ModVersion $ModVersion

$oldJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File | Where-Object {
            Test-JarModId -JarPath $_.FullName -ModId $ModId
        })
$previousVersion = $null
if ($oldJars.Count -gt 0) {
    $previousVersion = Get-JarModVersion -JarPath $oldJars[0].FullName -ModId $ModId
}

Write-Host "Matched old jars for $ModId in ${ResolvedModsDir}:"
foreach ($jar in $oldJars) {
    Write-Host " - $($jar.FullName)"
}
Write-Host "Deleting only jars whose embedded neoforge.mods.toml declares modId=`"$ModId`"."

foreach ($jar in $oldJars) {
    Remove-Item -LiteralPath $jar.FullName -Force
}

$TargetJarPath = Join-Path $ResolvedModsDir $RuntimeJar.Name
Copy-Item -LiteralPath $RuntimeJar.FullName -Destination $TargetJarPath -Force

$sourceInfo = Get-Item -LiteralPath $RuntimeJar.FullName
$targetInfo = Get-Item -LiteralPath $TargetJarPath
$sourceHash = (Get-FileHash -LiteralPath $RuntimeJar.FullName -Algorithm SHA256).Hash
$targetHash = (Get-FileHash -LiteralPath $TargetJarPath -Algorithm SHA256).Hash
$remainingJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File | Where-Object {
            Test-JarModId -JarPath $_.FullName -ModId $ModId
        })
$coreJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File | Where-Object {
            Test-JarModId -JarPath $_.FullName -ModId "ioe_core"
        })

New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
$report = [ordered]@{
    ModId = $ModId
    ModName = $ModName
    PreviousVersion = $previousVersion
    InstalledVersion = $ModVersion
    SourceJar = $RuntimeJar.FullName
    InstalledJar = $TargetJarPath
    SourceFileSize = $sourceInfo.Length
    InstalledFileSize = $targetInfo.Length
    SourceSha256 = $sourceHash
    InstalledSha256 = $targetHash
    HashesMatch = ($sourceHash -eq $targetHash)
    DeletedOldJars = @($oldJars | ForEach-Object { $_.FullName })
    RemainingJarsForMod = @($remainingJars | ForEach-Object { $_.FullName })
    RemainingJarsForModCount = $remainingJars.Count
    OnlyInstalledJarRemains = ($remainingJars.Count -eq 1)
    RequiredDependencyJars = @(
        [ordered]@{
            ModId = "ioe_core"
            Present = ($coreJars.Count -ge 1)
            Count = $coreJars.Count
            Jars = @($coreJars | ForEach-Object { $_.FullName })
        }
    )
}

$report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $ReportPath -Encoding UTF8
Get-Content -LiteralPath $ReportPath

if (-not $report.HashesMatch -or -not $report.OnlyInstalledJarRemains -or -not $report.RequiredDependencyJars[0].Present) {
    throw "Install verification failed. See $ReportPath"
}
