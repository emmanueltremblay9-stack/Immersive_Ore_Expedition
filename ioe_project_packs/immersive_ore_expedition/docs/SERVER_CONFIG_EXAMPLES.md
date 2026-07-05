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

## Diagnostics

Keep diagnostics disabled unless actively investigating a server issue:

```toml
resourcePolicy.debugDiagnostics = false
worldgen.provinces.debugDiagnostics = false
```

If diagnostics are enabled, capture a short log window, then turn them back off to avoid noisy routine logs.

## Current Limitation

The v7-v19 roadmap work is primarily scaffold, planning, policy, persistence, release validation, a default-off runtime placement proof gate, and a default-off registration smoke bridge. These config examples do not enable live placement of anchors, ore-load chambers, IE/IP clues, crystal sites, AE2 geodes, Nether geodes, Ancient Debris hearts, or retrogen resources.

v18 adds a default-off runtime placement proof path and v19 adds a default-off registration smoke bridge for controlled smoke evidence. Do not claim live gameplay proof or smoke success unless manual client/server/world smoke evidence was captured.
