# Immersive Ore Expedition workspace layout helper
# Run from the folder where you extracted the All Modules Bundle.
# This script does not download dependencies; it only initializes folder layout hints.

$ErrorActionPreference = "Stop"
$Root = Join-Path (Get-Location) "ImmersiveOreExpeditionWorkspace"
New-Item -ItemType Directory -Force -Path $Root | Out-Null

$Modules = @(
  "01_ioe_core",
  "02_ioe_expedition_worldgen",
  "03_ioe_crystal_growth",
  "04_ioe_nether_geodes",
  "05_ioe_ieip_prospecting",
  "06_ioe_retrogen_admin"
)

foreach ($m in $Modules) {
  $src = Join-Path (Get-Location) $m
  $dst = Join-Path $Root $m
  if (Test-Path $src) {
    Copy-Item $src $dst -Recurse -Force
    Write-Host "Copied $m -> $dst"
  } else {
    Write-Warning "Missing folder: $src"
  }
}

Write-Host "Next: open each module folder as an independent Codex/Cursor project."
Write-Host "Start with 01_ioe_core, then 02_ioe_expedition_worldgen."
