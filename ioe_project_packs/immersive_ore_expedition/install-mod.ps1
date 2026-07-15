[CmdletBinding()]
param(
    [string]$ModsDir = "C:\Users\Emmanuel Tremblay\AppData\Roaming\PrismLauncher\instances\1.21.1 TesT LaB\minecraft\mods",
    [string]$BuildDir = "build",
    [string]$SourceJar,
    [switch]$SkipBuild,
    [Alias("DryRun")]
    [switch]$PlanOnly
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.IO.Compression.FileSystem

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ExpectedLabModsDir = "C:\Users\Emmanuel Tremblay\AppData\Roaming\PrismLauncher\instances\1.21.1 TesT LaB\minecraft\mods"
$GradlePropertiesPath = Join-Path $ProjectRoot "gradle.properties"

function Resolve-ProjectPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$BasePath
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $BasePath $Path))
}

function Test-PathWithin {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$ParentPath
    )

    $normalizedPath = [System.IO.Path]::GetFullPath($Path).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    )
    $normalizedParent = [System.IO.Path]::GetFullPath($ParentPath).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    )
    return $normalizedPath.Equals($normalizedParent, [System.StringComparison]::OrdinalIgnoreCase) -or
        $normalizedPath.StartsWith(
            $normalizedParent + [System.IO.Path]::DirectorySeparatorChar,
            [System.StringComparison]::OrdinalIgnoreCase
        )
}

function Get-InferredJarVersion {
    param(
        [Parameter(Mandatory = $true)][string]$JarPath,
        [Parameter(Mandatory = $true)][string]$ModId
    )

    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($JarPath)
    $versionMatch = [regex]::Match(
        $baseName,
        '^' + [regex]::Escape($ModId) + '[-_](.+)$',
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
    if ($versionMatch.Success) {
        return $versionMatch.Groups[1].Value
    }
    return $null
}

function Compare-VersionNumbers {
    param(
        [Parameter(Mandatory = $true)][string]$Left,
        [Parameter(Mandatory = $true)][string]$Right
    )

    $leftMatch = [regex]::Match($Left, '\d+(?:\.\d+)*')
    $rightMatch = [regex]::Match($Right, '\d+(?:\.\d+)*')
    if (-not $leftMatch.Success -or -not $rightMatch.Success) {
        return $null
    }
    $leftParts = @($leftMatch.Value.Split('.') | ForEach-Object { [int]$_ })
    $rightParts = @($rightMatch.Value.Split('.') | ForEach-Object { [int]$_ })
    $maxParts = [Math]::Max($leftParts.Count, $rightParts.Count)
    for ($index = 0; $index -lt $maxParts; $index++) {
        $leftPart = if ($index -lt $leftParts.Count) { $leftParts[$index] } else { 0 }
        $rightPart = if ($index -lt $rightParts.Count) { $rightParts[$index] } else { 0 }
        if ($leftPart -lt $rightPart) {
            return -1
        }
        if ($leftPart -gt $rightPart) {
            return 1
        }
    }
    return 0
}

function Test-VersionRange {
    param(
        [AllowNull()][string]$Version,
        [AllowNull()][string]$VersionRange
    )

    if ([string]::IsNullOrWhiteSpace($Version) -or [string]::IsNullOrWhiteSpace($VersionRange)) {
        return $false
    }
    $exactMatch = [regex]::Match($VersionRange, '^\[([^,\]]+)\]$')
    if ($exactMatch.Success) {
        return $Version -eq $exactMatch.Groups[1].Value
    }
    $rangeMatch = [regex]::Match($VersionRange, '^([\[\(])([^,]*),([^\]\)]*)([\]\)])$')
    if (-not $rangeMatch.Success) {
        return $false
    }

    $lowerBound = $rangeMatch.Groups[2].Value.Trim()
    $upperBound = $rangeMatch.Groups[3].Value.Trim()
    if ($lowerBound.Length -gt 0 -and $lowerBound -ne '0') {
        $lowerComparison = Compare-VersionNumbers -Left $Version -Right $lowerBound
        if ($null -eq $lowerComparison -or $lowerComparison -lt 0 -or
                ($lowerComparison -eq 0 -and $rangeMatch.Groups[1].Value -eq '(')) {
            return $false
        }
    }
    if ($upperBound.Length -gt 0) {
        $upperComparison = Compare-VersionNumbers -Left $Version -Right $upperBound
        if ($null -eq $upperComparison -or $upperComparison -gt 0 -or
                ($upperComparison -eq 0 -and $rangeMatch.Groups[4].Value -eq ')')) {
            return $false
        }
    }
    return $true
}

function Read-GradleProperties {
    param([Parameter(Mandatory = $true)][string]$Path)

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
    param([Parameter(Mandatory = $true)][string]$JarPath)

    $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $entry = $zip.GetEntry("META-INF/neoforge.mods.toml")
        if ($null -eq $entry) {
            return $null
        }
        $reader = [System.IO.StreamReader]::new($entry.Open())
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
    param([AllowNull()][string]$Metadata)

    if ([string]::IsNullOrWhiteSpace($Metadata)) {
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

function Get-MetadataField {
    param(
        [AllowNull()][string]$Block,
        [Parameter(Mandatory = $true)][string]$Field
    )

    if ([string]::IsNullOrWhiteSpace($Block)) {
        return $null
    }
    $fieldPattern = '(?m)^\s*' + [regex]::Escape($Field) + '\s*=\s*"([^"]+)"'
    $fieldMatch = [regex]::Match($Block, $fieldPattern)
    if (-not $fieldMatch.Success) {
        return $null
    }
    return $fieldMatch.Groups[1].Value
}

function Get-JarIdentity {
    param([Parameter(Mandatory = $true)][string]$JarPath)

    try {
        $metadata = Get-JarMetadataText -JarPath $JarPath
        $primaryBlock = Get-PrimaryModBlock -Metadata $metadata
        $modId = Get-MetadataField -Block $primaryBlock -Field "modId"
        $version = Get-MetadataField -Block $primaryBlock -Field "version"
        $versionSource = "metadata"
        if (-not [string]::IsNullOrWhiteSpace($modId) -and
                ([string]::IsNullOrWhiteSpace($version) -or $version.Contains('${'))) {
            $inferredVersion = Get-InferredJarVersion -JarPath $JarPath -ModId $modId
            if (-not [string]::IsNullOrWhiteSpace($inferredVersion)) {
                $version = $inferredVersion
                $versionSource = "filename"
            }
        }
        return [pscustomobject]@{
            ModId = $modId
            Version = $version
            VersionSource = $versionSource
            Metadata = $metadata
        }
    } catch {
        return [pscustomobject]@{
            ModId = $null
            Version = $null
            VersionSource = $null
            Metadata = $null
        }
    }
}

function Test-JarEntry {
    param(
        [Parameter(Mandatory = $true)][string]$JarPath,
        [Parameter(Mandatory = $true)][string]$EntryName
    )

    $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        return $null -ne $zip.GetEntry($EntryName)
    } finally {
        $zip.Dispose()
    }
}

function Get-DeclaredDependencies {
    param(
        [Parameter(Mandatory = $true)][string]$Metadata,
        [Parameter(Mandatory = $true)][string]$OwnerModId
    )

    $escapedOwner = [regex]::Escape($OwnerModId)
    $dependencyPattern = '(?ms)^\s*\[\[dependencies\.' + $escapedOwner + '\]\]\s*(.*?)(?=^\s*\[\[|\z)'
    $dependencyMatches = [regex]::Matches($Metadata, $dependencyPattern)
    $dependencies = @()
    foreach ($dependencyMatch in $dependencyMatches) {
        $block = $dependencyMatch.Groups[1].Value
        $dependencyId = Get-MetadataField -Block $block -Field "modId"
        if ([string]::IsNullOrWhiteSpace($dependencyId)) {
            continue
        }
        $dependencies += [pscustomobject]@{
            ModId = $dependencyId
            Type = Get-MetadataField -Block $block -Field "type"
            VersionRange = Get-MetadataField -Block $block -Field "versionRange"
        }
    }
    return @($dependencies)
}

function Get-ModJarMatches {
    param(
        [Parameter(Mandatory = $true)][System.IO.FileInfo[]]$Jars,
        [Parameter(Mandatory = $true)][string]$ModId
    )

    $matches = @()
    foreach ($jar in $Jars) {
        $identity = Get-JarIdentity -JarPath $jar.FullName
        if ($identity.ModId -eq $ModId) {
            $matches += [pscustomobject]@{
                File = $jar
                Version = $identity.Version
                VersionSource = $identity.VersionSource
            }
        }
    }
    return @($matches)
}

function Get-DependencyReport {
    param(
        [Parameter(Mandatory = $true)][object[]]$Dependencies,
        [Parameter(Mandatory = $true)][System.IO.FileInfo[]]$InstalledJars,
        [Parameter(Mandatory = $true)][hashtable]$PlatformVersions
    )

    $platformIds = @("minecraft", "neoforge")
    $report = @()
    foreach ($dependency in $Dependencies) {
        if ($platformIds -contains $dependency.ModId) {
            $platformVersion = $PlatformVersions[$dependency.ModId]
            $report += [ordered]@{
                ModId = $dependency.ModId
                Type = $dependency.Type
                VersionRange = $dependency.VersionRange
                PlatformDependency = $true
                ResolvedVersion = $platformVersion
                VersionSource = "gradle.properties"
                Count = $null
                ExactlyOne = $null
                VersionCompatible = Test-VersionRange -Version $platformVersion -VersionRange $dependency.VersionRange
                Jars = @()
            }
            continue
        }

        $matches = @(Get-ModJarMatches -Jars $InstalledJars -ModId $dependency.ModId)
        $jarEntries = @()
        foreach ($match in $matches) {
            $jarEntries += [ordered]@{
                Path = $match.File.FullName
                Version = $match.Version
                VersionSource = $match.VersionSource
                Size = $match.File.Length
                Sha256 = (Get-FileHash -LiteralPath $match.File.FullName -Algorithm SHA256).Hash
            }
        }
        $report += [ordered]@{
            ModId = $dependency.ModId
            Type = $dependency.Type
            VersionRange = $dependency.VersionRange
            PlatformDependency = $false
            Count = $matches.Count
            ExactlyOne = ($matches.Count -eq 1)
            VersionCompatible = $(if ($matches.Count -eq 1) {
                    Test-VersionRange -Version $matches[0].Version -VersionRange $dependency.VersionRange
                } else {
                    $false
                })
            Jars = $jarEntries
        }
    }
    return @($report)
}

if (-not (Test-Path -LiteralPath $GradlePropertiesPath -PathType Leaf)) {
    throw "Missing gradle.properties: $GradlePropertiesPath"
}
if (-not $SkipBuild) {
    throw "Local builds are disabled by this repository policy. Use a CI-built artifact with -SkipBuild -SourceJar <path>."
}
if ([string]::IsNullOrWhiteSpace($SourceJar)) {
    throw "An explicit CI-built -SourceJar is required."
}

$ResolvedBuildDir = Resolve-ProjectPath -Path $BuildDir -BasePath $ProjectRoot
$ResolvedSourceJar = Resolve-ProjectPath -Path $SourceJar -BasePath $ProjectRoot
if (-not (Test-PathWithin -Path $ResolvedBuildDir -ParentPath $ProjectRoot)) {
    throw "BuildDir must remain inside the project root: $ResolvedBuildDir"
}
if (-not (Test-Path -LiteralPath $ResolvedSourceJar -PathType Leaf)) {
    throw "Source JAR does not exist: $ResolvedSourceJar"
}
if ([System.IO.Path]::GetExtension($ResolvedSourceJar) -ne ".jar") {
    throw "Source artifact must be a JAR: $ResolvedSourceJar"
}
if (-not (Test-Path -LiteralPath $ModsDir -PathType Container)) {
    throw "Mods directory does not exist: $ModsDir"
}

$ResolvedModsDir = (Resolve-Path -LiteralPath $ModsDir).Path.TrimEnd([System.IO.Path]::DirectorySeparatorChar)
$ResolvedExpectedLabModsDir = (Resolve-Path -LiteralPath $ExpectedLabModsDir).Path.TrimEnd([System.IO.Path]::DirectorySeparatorChar)
if (-not [string]::Equals($ResolvedModsDir, $ResolvedExpectedLabModsDir, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing non-LAB mods directory: $ResolvedModsDir"
}

$properties = Read-GradleProperties -Path $GradlePropertiesPath
$ModId = $properties["mod_id"]
$ModName = $properties["mod_name"]
$ModVersion = $properties["mod_version"]
if ([string]::IsNullOrWhiteSpace($ModId) -or [string]::IsNullOrWhiteSpace($ModVersion)) {
    throw "gradle.properties must define mod_id and mod_version."
}

$sourceIdentity = Get-JarIdentity -JarPath $ResolvedSourceJar
if ($sourceIdentity.ModId -ne $ModId) {
    throw "Source JAR mod id mismatch: expected $ModId, found $($sourceIdentity.ModId)."
}
if ($sourceIdentity.Version -ne $ModVersion) {
    throw "Source JAR version mismatch: expected $ModVersion, found $($sourceIdentity.Version)."
}

$requiredFeatureEntries = @(
    "META-INF/neoforge.mods.toml",
    "com/oblixorprime/ioe/ImmersiveOreExpeditionMod.class",
    "com/oblixorprime/ioe/worldgen/IoeNewChunkOreGuard.class",
    "com/oblixorprime/ioe/expeditionlocator/ExpeditionLocatorReindexer.class",
    "data/immersive_ore_expedition/neoforge/biome_modifier/remove_known_ore_features.json",
    "data/immersive_ore_expedition/tags/worldgen/biome/resource_suppression_biomes.json"
)
$missingFeatureEntries = @($requiredFeatureEntries | Where-Object {
        -not (Test-JarEntry -JarPath $ResolvedSourceJar -EntryName $_)
    })
if ($missingFeatureEntries.Count -gt 0) {
    throw "Source JAR is missing required runtime entries: $($missingFeatureEntries -join ', ')"
}

$installedJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File)
$oldJarMatches = @(Get-ModJarMatches -Jars $installedJars -ModId $ModId)
$declaredDependencies = @(Get-DeclaredDependencies -Metadata $sourceIdentity.Metadata -OwnerModId $ModId)
$platformVersions = @{
    minecraft = $properties["minecraft_version"]
    neoforge = $properties["neo_version"]
}
$dependencyReport = @(Get-DependencyReport `
        -Dependencies $declaredDependencies `
        -InstalledJars $installedJars `
        -PlatformVersions $platformVersions)
$requiredDependencies = @($dependencyReport | Where-Object {
        $_.Type -eq "required"
    })
$duplicateOptionalDependencies = @($dependencyReport | Where-Object {
        $_.Type -eq "optional" -and -not $_.PlatformDependency -and $_.Count -gt 1
    })
$dependencyStackReady = (@($requiredDependencies | Where-Object {
            -not $_.VersionCompatible -or (-not $_.PlatformDependency -and -not $_.ExactlyOne)
        }).Count -eq 0) -and
        ($duplicateOptionalDependencies.Count -eq 0)

$sourceInfo = Get-Item -LiteralPath $ResolvedSourceJar
$sourceHash = (Get-FileHash -LiteralPath $ResolvedSourceJar -Algorithm SHA256).Hash
$targetJarPath = Join-Path $ResolvedModsDir $sourceInfo.Name
$reportPath = Join-Path $ResolvedBuildDir $(if ($PlanOnly) { "install-plan.json" } else { "install-report.json" })
$deletedOldJars = @()
$restoredOldJars = @()
$installedInfo = $null
$installedHash = $null
$hashesMatch = $null
$remainingMatches = @()
$mutationStarted = $false
$rollbackPerformed = $false
$rollbackSucceeded = $null
$installationError = $null
$installSucceeded = $false
$temporaryJarPath = $null
$backupDirectory = $null
$backupRecords = @()

New-Item -ItemType Directory -Path $ResolvedBuildDir -Force | Out-Null
if (-not $PlanOnly) {
    try {
        if (-not $dependencyStackReady) {
            throw "Required dependency stack is incomplete, incompatible, or ambiguous."
        }

        $targetAlreadyExists = Test-Path -LiteralPath $targetJarPath -PathType Leaf
        $targetIsKnownOldJar = @($oldJarMatches | Where-Object {
                [string]::Equals(
                    $_.File.FullName,
                    $targetJarPath,
                    [System.StringComparison]::OrdinalIgnoreCase
                )
            }).Count -eq 1
        if ($targetAlreadyExists -and -not $targetIsKnownOldJar) {
            throw "Target path is occupied by a JAR that was not identified as this mod: $targetJarPath"
        }

        $backupDirectory = Join-Path $ResolvedBuildDir ("install-backup-{0}" -f [guid]::NewGuid().ToString("N"))
        New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
        foreach ($oldJar in $oldJarMatches) {
            $backupPath = Join-Path $backupDirectory $oldJar.File.Name
            Copy-Item -LiteralPath $oldJar.File.FullName -Destination $backupPath
            $originalHash = (Get-FileHash -LiteralPath $oldJar.File.FullName -Algorithm SHA256).Hash
            $backupHash = (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash
            if ($originalHash -ne $backupHash) {
                throw "Backup hash mismatch for $($oldJar.File.FullName)."
            }
            $backupRecords += [pscustomobject]@{
                OriginalPath = $oldJar.File.FullName
                BackupPath = $backupPath
                Sha256 = $originalHash
            }
        }

        $temporaryJarPath = Join-Path $ResolvedModsDir (".{0}-install-{1}.tmp" -f $ModId, [guid]::NewGuid().ToString("N"))
        $mutationStarted = $true
        Copy-Item -LiteralPath $ResolvedSourceJar -Destination $temporaryJarPath
        $temporaryHash = (Get-FileHash -LiteralPath $temporaryJarPath -Algorithm SHA256).Hash
        if ($temporaryHash -ne $sourceHash) {
            throw "Staged JAR hash mismatch before replacement."
        }

        foreach ($oldJar in $oldJarMatches) {
            Remove-Item -LiteralPath $oldJar.File.FullName -Force
            $deletedOldJars += $oldJar.File.FullName
        }
        Move-Item -LiteralPath $temporaryJarPath -Destination $targetJarPath

        $installedIdentity = Get-JarIdentity -JarPath $targetJarPath
        if ($installedIdentity.ModId -ne $ModId -or $installedIdentity.Version -ne $ModVersion) {
            throw "Installed JAR metadata verification failed."
        }
        $missingInstalledFeatureEntries = @($requiredFeatureEntries | Where-Object {
                -not (Test-JarEntry -JarPath $targetJarPath -EntryName $_)
            })
        if ($missingInstalledFeatureEntries.Count -gt 0) {
            throw "Installed JAR is missing runtime entries: $($missingInstalledFeatureEntries -join ', ')"
        }
        $installedInfo = Get-Item -LiteralPath $targetJarPath
        $installedHash = (Get-FileHash -LiteralPath $targetJarPath -Algorithm SHA256).Hash
        $hashesMatch = $sourceHash -eq $installedHash
        if (-not $hashesMatch) {
            throw "Installed JAR hash does not match the CI artifact."
        }
        $remainingJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File)
        $remainingMatches = @(Get-ModJarMatches -Jars $remainingJars -ModId $ModId)
        if ($remainingMatches.Count -ne 1) {
            throw "Expected exactly one installed JAR for $ModId, found $($remainingMatches.Count)."
        }
        $installSucceeded = $true
    } catch {
        $installationError = $_.Exception.Message
        if ($mutationStarted) {
            $rollbackPerformed = $true
            try {
                if (Test-Path -LiteralPath $targetJarPath -PathType Leaf) {
                    $rollbackTargetIdentity = Get-JarIdentity -JarPath $targetJarPath
                    $rollbackTargetHash = (Get-FileHash -LiteralPath $targetJarPath -Algorithm SHA256).Hash
                    if ($rollbackTargetIdentity.ModId -ne $ModId -or $rollbackTargetHash -ne $sourceHash) {
                        throw "Rollback refused because the target JAR changed after staging: $targetJarPath"
                    }
                    Remove-Item -LiteralPath $targetJarPath -Force
                }
                foreach ($backupRecord in $backupRecords) {
                    if (Test-Path -LiteralPath $backupRecord.OriginalPath -PathType Leaf) {
                        $currentOriginalHash = (Get-FileHash -LiteralPath $backupRecord.OriginalPath -Algorithm SHA256).Hash
                        if ($currentOriginalHash -ne $backupRecord.Sha256) {
                            throw "Rollback refused to overwrite a concurrently changed JAR: $($backupRecord.OriginalPath)"
                        }
                    } else {
                        Copy-Item -LiteralPath $backupRecord.BackupPath -Destination $backupRecord.OriginalPath
                    }
                    $restoredHash = (Get-FileHash -LiteralPath $backupRecord.OriginalPath -Algorithm SHA256).Hash
                    if ($restoredHash -ne $backupRecord.Sha256) {
                        throw "Rollback hash mismatch for $($backupRecord.OriginalPath)."
                    }
                    $restoredOldJars += $backupRecord.OriginalPath
                }
                $rollbackSucceeded = $true
            } catch {
                $rollbackSucceeded = $false
                $installationError = "$installationError Rollback failed: $($_.Exception.Message)"
            }
        }
    } finally {
        if ($null -ne $temporaryJarPath -and (Test-Path -LiteralPath $temporaryJarPath)) {
            Remove-Item -LiteralPath $temporaryJarPath -Force
        }
        if ($null -ne $backupDirectory -and (Test-Path -LiteralPath $backupDirectory) -and
                ($installSucceeded -or -not $mutationStarted -or $rollbackSucceeded)) {
            Remove-Item -LiteralPath $backupDirectory -Recurse -Force
            $backupDirectory = $null
        }
    }

    $remainingJars = @(Get-ChildItem -LiteralPath $ResolvedModsDir -Filter "*.jar" -File)
    $remainingMatches = @(Get-ModJarMatches -Jars $remainingJars -ModId $ModId)
    if ($installSucceeded -and (Test-Path -LiteralPath $targetJarPath -PathType Leaf)) {
        $installedInfo = Get-Item -LiteralPath $targetJarPath
        $installedHash = (Get-FileHash -LiteralPath $targetJarPath -Algorithm SHA256).Hash
        $hashesMatch = $sourceHash -eq $installedHash
    }
}

$readyToInstall = ($missingFeatureEntries.Count -eq 0) -and $dependencyStackReady
$safeToLaunchMinecraft = $installSucceeded -and $hashesMatch -and
        ($remainingMatches.Count -eq 1) -and $dependencyStackReady
$commandExitStatus = $(if ($PlanOnly) {
        if ($readyToInstall) { 0 } else { 1 }
    } elseif ($safeToLaunchMinecraft) {
        0
    } else {
        1
    })
$installationResult = $(if ($PlanOnly) {
        "NOT_PERFORMED: plan only"
    } elseif ($installSucceeded) {
        "SUCCESS"
    } elseif ($rollbackPerformed -and $rollbackSucceeded) {
        "FAILED: previous installation restored"
    } else {
        "FAILED"
    })
$remainingRisks = $(if ($PlanOnly) {
        @("JAR not installed", "Minecraft not launched", "fresh world not scanned")
    } elseif ($installSucceeded) {
        @("Minecraft runtime not launched", "fresh world not scanned")
    } else {
        @("Installation failed; inspect Error, rollback fields, and any retained backup directory")
    })
$report = [ordered]@{
    Mode = $(if ($PlanOnly) { "PlanOnly" } else { "Install" })
    ProjectRoot = $ProjectRoot
    BuildDir = $ResolvedBuildDir
    ModsDir = $ResolvedModsDir
    ModId = $ModId
    ModName = $ModName
    ModLoader = "NeoForge/javafml"
    MinecraftVersion = $properties["minecraft_version"]
    NeoForgeVersion = $properties["neo_version"]
    PreviousVersions = @($oldJarMatches | ForEach-Object { $_.Version })
    InstalledVersion = $(if ($installSucceeded) { $ModVersion } else { $null })
    PlannedVersion = $ModVersion
    SourceJar = $ResolvedSourceJar
    InstalledJar = $(if ($installSucceeded) { $targetJarPath } else { $null })
    PlannedInstalledJar = $targetJarPath
    SourceFileSize = $sourceInfo.Length
    InstalledFileSize = $(if ($null -eq $installedInfo) { $null } else { $installedInfo.Length })
    SourceSha256 = $sourceHash
    InstalledSha256 = $installedHash
    HashesMatch = $hashesMatch
    RequiredFeatureEntries = $requiredFeatureEntries
    MissingFeatureEntries = $missingFeatureEntries
    FeatureArtifactsPresent = ($missingFeatureEntries.Count -eq 0)
    PlannedOldJars = @($oldJarMatches | ForEach-Object { $_.File.FullName })
    DeletedOldJars = $deletedOldJars
    RestoredOldJars = $restoredOldJars
    DeclaredDependencies = $dependencyReport
    DependencyStackReady = $dependencyStackReady
    RemainingJarsForMod = @($remainingMatches | ForEach-Object { $_.File.FullName })
    RemainingJarsForModCount = $(if ($PlanOnly) { $null } else { $remainingMatches.Count })
    OnlyInstalledJarRemains = $(if ($PlanOnly) { $null } else { $remainingMatches.Count -eq 1 })
    ReadyToInstall = $readyToInstall
    PrismMutationPerformed = $mutationStarted
    RollbackPerformed = $rollbackPerformed
    RollbackSucceeded = $rollbackSucceeded
    RetainedBackupDirectory = $backupDirectory
    ExactCommandLine = [Environment]::CommandLine
    CommandExitStatus = $commandExitStatus
    BuildResult = "NOT_PERFORMED: CI artifact supplied with -SkipBuild"
    InstallationResult = $installationResult
    Error = $installationError
    RemainingRisks = $remainingRisks
    SafeToLaunchMinecraft = $safeToLaunchMinecraft
}

$report | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $reportPath -Encoding UTF8
Get-Content -LiteralPath $reportPath

if (-not $report.ReadyToInstall) {
    throw "Install preflight failed. See $reportPath"
}
if ($null -ne $installationError) {
    throw "Install failed: $installationError See $reportPath"
}
if (-not $PlanOnly -and -not $report.SafeToLaunchMinecraft) {
    throw "Install verification failed. See $reportPath"
}
