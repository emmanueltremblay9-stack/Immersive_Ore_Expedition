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
- No live placement is enabled by these examples.

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

## Diagnostics

Keep diagnostics disabled unless actively investigating a server issue:

```toml
resourcePolicy.debugDiagnostics = false
worldgen.provinces.debugDiagnostics = false
```

If diagnostics are enabled, capture a short log window, then turn them back off to avoid noisy routine logs.

## Current Limitation

The v7-v17 roadmap work is primarily scaffold, planning, policy, persistence, and release validation. These config examples do not enable live placement of anchors, ore-load chambers, IE/IP clues, crystal sites, AE2 geodes, Nether geodes, Ancient Debris hearts, or retrogen resources.
