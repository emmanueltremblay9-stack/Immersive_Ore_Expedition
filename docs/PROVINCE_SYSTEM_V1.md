# Province System v1

Province System v1 is the first safe foundation layer for IOE province-driven resource discovery after source-level consolidation.

## Scope

- Active mod id: `immersive_ore_expedition`
- Active module: `ioe_project_packs/immersive_ore_expedition`
- Config file: `immersive_ore_expedition-common.toml`
- No new ores, gems, blocks, items, entities, mixins, access transformers, or embedded dependency jars.
- No destructive worldgen hooks are introduced in v1.
- AE2, GeOre, Immersive Engineering, and Immersive Petroleum remain optional integrations.

## Namespace Policy

New province ids use the consolidated namespace:

```text
immersive_ore_expedition:<province_path>
```

Unqualified province ids are parsed into `immersive_ore_expedition`.

The old split module ids are documented legacy namespaces only:

```text
ioe_core
ioe_expedition_worldgen
ioe_crystal_growth
ioe_nether_geodes
ioe_ieip_prospecting
ioe_retrogen_admin
```

Legacy namespace parsing requires explicit opt-in in code and defaults to disabled in config.

## Config Sections

Province System v1 extends the unified common config without creating split config files.

Resource policy keys:

```text
resourcePolicy.allowedCategories
resourcePolicy.deniedCategories
resourcePolicy.excludedResources
resourcePolicy.debugDiagnostics
```

Province matching keys:

```text
worldgen.provinces.namespace
worldgen.provinces.allowLegacyNamespaces
worldgen.provinces.allowBiomes
worldgen.provinces.denyBiomes
worldgen.provinces.excludeBiomes
worldgen.provinces.debugDiagnostics
```

The existing logical sections remain:

```text
resourcePolicy
worldgen
crystalGrowth
netherGeodes
ieipProspecting
retrogen
```

## Strict Exclusions

The strict exclusion list is preserved:

- Apatite
- Tin
- Forestry Copper
- Platinum
- Osmium
- Tungsten
- Black Quartz
- Uraninite
- Monazite

Strict exclusions override resource category allow lists.

## Runtime Behavior

v1 only provides pure Java policy and diagnostic primitives:

- Province id parsing and namespace checks.
- Province biome allow, deny, and exclude matching.
- Province resource category allow, deny, and exclude policy.
- Diagnostic summaries for province/biome and resource decisions.

Existing worldgen and retrogen behavior is not expanded by this layer.

## CI Validation

Validation is expected to run in GitHub Actions. Local Gradle, local builds, local tests, PrismLauncher, Minecraft, and local smoke tests are disabled by default for this project workflow.

## Follow-Up Work

- Bind province rules to real configured province data after the policy format is finalized.
- Add non-destructive data loading for pack-defined province rules.
- Add runtime diagnostics behind explicit config toggles.
- Design later worldgen hooks separately and validate them in CI before any local runtime smoke approval.
