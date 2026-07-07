# IOE Server Config Examples

The consolidated module writes one common config file:

`immersive_ore_expedition-common.toml`

Defaults are intentionally conservative. Runtime worldgen placement remains default-off/no-op in the current scaffold and planning releases.

## Default Safety

- `worldgen.provinces.runtimeIntegrationEnabled` defaults to `false`.
- `worldgen.provinces.resourcePolicyRules` defaults to `[]`.
- Diagnostics remain opt-in and non-noisy by default.
- Strict resource exclusions still win over configured allow rules.
- Old split IOE namespaces remain legacy opt-in references only.
- Runtime placement proof remains disabled by default.
- Runtime proof feature registration bridge remains disabled by default.
- Configured/placed feature declarations are attached only to an empty IOE smoke biome tag by default.

## Province Resource Rules

Rules use this shape:

```toml
worldgen.provinces.resourcePolicyRules = [
  "immersive_ore_expedition:default|minecraft:iron_ore|allow",
  "alpine|minecraft:*|deny",
  "volcanic|immersive_ore_expedition:*|exclude"
]
```

The middle field is a resource selector. Exact resource ids and `namespace:*` selectors are supported. Exact selectors win over namespace wildcard selectors, and strict exclusions win over every configured rule.

Keep resource namespaces explicit. Do not rely on old split IOE namespaces unless a legacy migration intentionally enables them.

## Runtime Integration Gate

Province resource rules affect runtime ore-load planning only when province runtime integration is enabled:

```toml
worldgen.provinces.runtimeIntegrationEnabled = true
```

Leave this disabled for default-safe server releases unless the server owner has reviewed the province rules and understands the current scaffold/planning-only limitations.

## Runtime Placement Proof Gate

v18 adds a first explicit runtime placement proof gate. Keep it disabled for normal server configs:

```toml
worldgen.runtimePlacementEnabled = false
worldgen.runtimePlacementDiagnostics = false
```

Only enable it in a controlled smoke profile when collecting evidence for the v18 proof path:

```toml
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

The proof path still validates the anchor, loaded block resource, resource policy, strict exclusions, writable generation region, and target air block before placement. It does not enable the full surface clue, mineshaft, or ore-load gameplay loop, and it does not register configured features, placed features, or biome modifiers.

## Runtime Registration Smoke Bridge

v19 registers one custom feature type for controlled future smoke profiles but keeps bridge execution disabled by default:

```toml
worldgen.runtimeProofFeatureEnabled = false
worldgen.runtimeProofFeatureDiagnostics = false
```

Only enable it together with the v18 placement gate in a controlled smoke profile:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

The bridge does not add configured features, placed features, biome modifiers, datapack JSON, ores, gems, blocks, items, or entities. With default config it produces no world mutation, and live gameplay proof still requires manual client/server/world smoke evidence.

## Configured/Placed Feature Declaration Bridge

v20 adds one configured feature declaration and one placed feature declaration for the v19 proof feature id, `immersive_ore_expedition:tiny_vertical_mine_entrance`. The placed feature uses an empty placement modifier list and is not attached to any biome.

No extra config is required for the declarations, and no default changes are made. With normal server config, the declarations are addressable data only: no biome modifier is added, no biome binding is added, and no world mutation occurs.

## Biome Modifier Smoke-Tag Bridge

v21 adds a default-off biome modifier smoke bridge for `immersive_ore_expedition:tiny_vertical_mine_entrance`. The biome modifier targets only the IOE-owned tag `#immersive_ore_expedition:worldgen_smoke_test_biomes`, and the shipped tag is empty by default:

```json
{
  "replace": false,
  "values": []
}
```

With default resources and config, no real biome receives the placed feature and no world mutation occurs. A future external datapack may append one explicit biome to the smoke tag for manual smoke only. That external smoke profile must still enable both gates:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

Do not enable broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld` for this smoke path. Do not claim live placement or gameplay proof unless manual client/server/world smoke evidence was captured.

## V22 Controlled Smoke Profile Template

v22 adds a docs-only controlled smoke profile at `docs/smoke_profiles/v22_worldgen_smoke_profile/`. The profile is not active unless a tester manually installs its external datapack into a disposable test world and applies the smoke config template.

The sample datapack appends exactly one explicit vanilla biome id, `minecraft:plains`, to `immersive_ore_expedition:worldgen_smoke_test_biomes`. It does not use broad biome tags, modded biome ids, or multiple biome ids.

The docs-only smoke config template uses:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
worldgen.provinces.runtimeIntegrationEnabled = false
worldgen.provinces.resourcePolicyRules = []
```

Default server config remains unchanged with all runtime smoke gates disabled. Manual client/server/world smoke evidence is required before claiming live placement.

## V23 Controlled Smoke Runbook

v23 adds a docs-only controlled smoke runbook at `docs/smoke_profiles/v23_worldgen_smoke_runbook/`. The runbook uses the v22 external datapack and config template, records client/server execution status, and separates skipped, attempted, and observed placement outcomes.

The runbook does not change server defaults and does not make the v22 profile active. Keep normal server configs default-safe unless a disposable smoke profile is intentionally configured and evidenced.

## Diagnostics

Keep diagnostics disabled unless actively investigating a server issue:

```toml
resourcePolicy.debugDiagnostics = false
worldgen.provinces.debugDiagnostics = false
```

If diagnostics are enabled, capture a short log window, then turn them back off to avoid noisy routine logs.

## Current Limitation

The v7-v23 roadmap work is primarily scaffold, planning, policy, persistence, release validation, a default-off runtime placement proof gate, a default-off registration smoke bridge, declaration-only configured/placed feature resources, a biome modifier bridge that targets an empty smoke tag by default, a docs-only controlled external smoke profile package, and a docs-only controlled smoke runbook/result template. These config examples do not enable live placement of anchors, ore-load chambers, IE/IP clues, crystal sites, AE2 geodes, Nether geodes, Ancient Debris hearts, or retrogen resources.

v18 adds a default-off runtime placement proof path, v19 adds a default-off registration smoke bridge, v20 makes that bridge addressable as configured/placed feature data, v21 adds a default-off biome modifier smoke-tag bridge with zero real biome bindings by default, v22 documents a controlled external smoke profile without running it, and v23 documents the controlled smoke runbook/result template without running it. Do not claim live gameplay proof or smoke success unless manual client/server/world smoke evidence was captured.
