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

Province runtime integration remains opt-in through `worldgen.provinces.runtimeIntegrationEnabled`, which defaults to `false`. With the default, existing ore-load planning behavior and validation flow stay unchanged. The active validation path is the consolidated module at `ioe_project_packs/immersive_ore_expedition` with mod id `immersive_ore_expedition`; local validation is disabled by default, GitHub Actions is the validation source of truth, and legacy split modules are not part of the active validation path.

Province bindings are config-driven through `worldgen.provinces.defaultProvince` and `worldgen.provinces.biomeProvinceBindings`. Binding entries use `biome_selector=province_id`, for example `minecraft:plains=temperate_iron` or `minecraft:*=overworld_default`. They only resolve province policy context for the opt-in runtime integration layer; they do not add ores, gems, blocks, items, entities, or generated content.

Province System v4 adds `worldgen.provinces.resourcePolicyRules`, defaulting to an empty list. Entries use `province_id|resource_id|decision`, with decisions `allow`, `deny`, or `exclude`, for example `default|minecraft:iron_ore|allow`, `temperate_iron|minecraft:coal_ore|deny`, or `immersive_ore_expedition:default|minecraft:diamond_ore|exclude`. Unqualified province ids resolve to `immersive_ore_expedition`; resource ids must be explicit `namespace:path` ids. Malformed rules are ignored, and duplicate/conflicting matches use the first valid rule. Empty rules leave current behavior unchanged, strict resource exclusions still win, and validation remains deferred to GitHub Actions.
