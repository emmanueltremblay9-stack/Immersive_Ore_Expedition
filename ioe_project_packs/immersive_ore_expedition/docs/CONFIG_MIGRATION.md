# Immersive Ore Expedition Config Migration

The consolidated NeoForge module registers one common config file:

`immersive_ore_expedition-common.toml`

The six previous module config files are not read automatically by the consolidated module:

- `ioe_core-common.toml`
- `ioe_expedition_worldgen-common.toml`
- `ioe_crystal_growth-common.toml`
- `ioe_nether_geodes-common.toml`
- `ioe_ieip_prospecting-common.toml`
- `ioe_retrogen_admin-common.toml`

Config options were moved under these top-level sections:

- `resourcePolicy`
- `worldgen`
- `crystalGrowth`
- `netherGeodes`
- `ieipProspecting`
- `retrogen`

No previous config option was intentionally dropped. Existing server configs need a manual one-time copy into the new file if non-default values were used.
