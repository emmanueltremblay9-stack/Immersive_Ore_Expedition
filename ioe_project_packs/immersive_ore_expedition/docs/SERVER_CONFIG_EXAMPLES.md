# IOE Server Config Examples

The consolidated module uses `immersive_ore_expedition-common.toml`. These are
separate controls, not a global no-mutation switch. Values below describe source
defaults; record the effective config and datapacks of the world being qualified.
Do not overwrite an existing config with this document.

## Current activation scopes

| Path | Current default / control | Meaning |
| --- | --- | --- |
| Natural Overworld expedition sites | `worldgen.global.naturalExpeditionSiteGenerationEnabled = true` | `ExpeditionSiteFeature.place` can place eligible sites in newly generated terrain, subject to site/component controls and placement checks. Production biome wrappers include `#c:is_overworld`. |
| Legacy placement proof hook | `worldgen.runtimePlacementEnabled = false` | Disables only the old proof/planning placement path; it does not disable natural expedition sites. |
| Legacy proof bridge / diagnostics | `worldgen.runtimeProofFeatureEnabled`, `worldgen.runtimeProofFeatureDiagnostics`, `worldgen.runtimePlacementDiagnostics` default `false` | Historical proof controls, not global worldgen switches. |
| New-chunk resource guard | `IoeNewChunkOreGuard` listeners are registered independently | May replace/remove unauthorized ores or growth blocks in admitted newly generated chunks, independently of the natural-site and proof gates. No global disable switch is asserted here. |
| Nether depth32 production routing | `NetherWorldActivation.PRODUCTION_ENABLED = false` in code | Nether-specific routing remains closed. The existing development GameTest prototype is a separate restricted path, not a server-config toggle. This does not stop every IOE operation in the Nether, including the new-chunk guard. |

The owner's 14 October instruction to activate IOE in new worlds and merge PR63
after tests is distinct from Nether qualification/activation. It does not grant
permission to merge PR64 or install another runtime during this documentation cycle.

### Natural-site control

The current source default is:

```toml
worldgen.global.naturalExpeditionSiteGenerationEnabled = true
```

Setting this existing key to `false` prevents the natural-site feature from
placing sites. It does **not** disable biome modifiers, independent new-chunk ore
sanitization, existing block behavior, or every IOE mutation. In particular, do not
use sites disabled / proof disabled as a promise that ordinary ore generation is
restored or that exploring new chunks cannot change resources.

The guard admits new chunks through `ChunkEvent.Load.isNewChunk`, checks its
bounded admission lifecycle, and uses `getChunkNow` rather than requesting missing
chunks. Existing chunks are not newly admitted just because they reload; a still
valid first-load admission can finish its existing work. Placement authorization
and Nether provenance protect matching resources. These rules are not permission
for retrogen or repair of old chunks.

## Province resource rules

```toml
worldgen.provinces.runtimeIntegrationEnabled = false
worldgen.provinces.resourcePolicyRules = []
```

These province planning controls are separate from natural-site activation and
the new-chunk guard. If configuring reviewed rules, their syntax is
`province|resource|allow`, `province|resource|deny`, or `province|resource|exclude`.
Exact resource IDs and `namespace:*` selectors are supported; strict exclusions
win. Neither these defaults nor empty rules establish globally mutation-free IOE.

## Historical proof examples

The v18–v23 proof controls remain available but are not the current Overworld
production path. Their former empty smoke biome tag and declaration-only feature
statements applied to that proof bridge, not to current production biome bindings.
See [historical smoke procedures](SMOKE_VALIDATION.md#historical-controlled-smoke-record-v18-v35).
Do not enable proof controls to activate normal expedition sites, and do not treat
all proof flags being false as a world-safety qualification.

## Qualification before use

Follow [SMOKE_VALIDATION](SMOKE_VALIDATION.md) and record the source commit, exact
JAR SHA256, mandatory dependencies, effective config/datapacks, and each activation
scope above. An old world's newly explored chunks are still new chunks; old-chunk
protection is not protection against changes in future terrain.

Use only an explicitly authorized qualification environment. This document neither
creates a new profile/server nor authorizes installation or an activation change.
A missing required dependency blocks launch qualification. No manual visual/client
success follows from CI, JAR inspection, or a reported download alone.

## Source references

- [Config defaults](../src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java)
- [Natural feature gate](../src/main/java/com/oblixorprime/ioe/worldgen/ExpeditionSiteFeature.java)
- [Guard registration](../src/main/java/com/oblixorprime/ioe/worldgen/IoeExpeditionWorldgenMod.java)
- [New-chunk guard](../src/main/java/com/oblixorprime/ioe/worldgen/IoeNewChunkOreGuard.java)
- [Nether routing](../src/main/java/com/oblixorprime/ioe/nethergeodes/NetherWorldActivation.java)
