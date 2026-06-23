# Optional: initialize each module as its own git repository.
# Review before running.

$Modules = @(
  "01_ioe_core",
  "02_ioe_expedition_worldgen",
  "03_ioe_crystal_growth",
  "04_ioe_nether_geodes",
  "05_ioe_ieip_prospecting",
  "06_ioe_retrogen_admin"
)

foreach ($m in $Modules) {
  if (Test-Path $m) {
    Push-Location $m
    if (-not (Test-Path ".git")) {
      git init
      git add .
      git commit -m "Initial Immersive Ore Expedition module skeleton"
    }
    Pop-Location
  }
}
