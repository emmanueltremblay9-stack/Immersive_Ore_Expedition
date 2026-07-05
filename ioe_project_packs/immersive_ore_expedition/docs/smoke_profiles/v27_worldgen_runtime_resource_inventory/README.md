# V27 Worldgen Runtime Resource Inventory

This directory is a docs-only active runtime resource inventory snapshot prepared after the v26 runtime slice implementation packet. It records read-only observations from the active consolidated module so a later, separate runtime PR can recheck the exact shipped resource and config surfaces before making any active change.

v27 does not run smoke, accept evidence, make anything `READY_FOR_RUNTIME_SLICE`, select `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR`, activate runtime worldgen, change active `src/main/resources`, change config defaults, or modify legacy split-module source trees.

## Inputs

- V23 runbook: `../v23_worldgen_smoke_runbook/README.md`
- V23 result template: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 evidence gate: `../v24_worldgen_smoke_evidence_gate/README.md`
- V24 decision template: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- V25 readiness packet: `../v25_worldgen_runtime_promotion_readiness/README.md`
- V25 readiness template: `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`
- V26 implementation packet: `../v26_worldgen_runtime_slice_packet/README.md`
- V26 implementation template: `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md`
- V27 inventory template: `V27_WORLDGEN_RUNTIME_RESOURCE_INVENTORY_TEMPLATE.md`

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- The inventory is read-only documentation only.
- No smoke evidence is invented or accepted by this inventory.
- A future runtime PR must be separate and must regenerate validation after any active resource or source change.

## Observed Active Resource Surfaces

Paths are relative to `ioe_project_packs/immersive_ore_expedition`.

| Path | Observed role | Current state | Future touch classification | Notes |
| --- | --- | --- | --- | --- |
| `src/main/resources/pack.mcmeta` | Shipped resource pack metadata | `pack_format` is `34` | `DO_NOT_TOUCH_THIS_SLICE` | Read-only observation for Minecraft 1.21.1 resource metadata. |
| `src/main/resources/META-INF/neoforge.mods.toml` | NeoForge mod metadata template | Uses Gradle-expanded `${mod_id}`, `${mod_version}`, and dependency placeholders | `DO_NOT_TOUCH_THIS_SLICE` | Runtime jar metadata surface, not a worldgen activation surface. |
| `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | IOE smoke biome tag | `replace` is `false`; `values` is `[]` | `CANDIDATE_FUTURE_TOUCH` | The shipped tag binds zero real biomes by default. |
| `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | NeoForge biome modifier smoke bridge | `neoforge:add_features` targets only `#immersive_ore_expedition:worldgen_smoke_test_biomes` | `CANDIDATE_FUTURE_TOUCH` | It references `immersive_ore_expedition:tiny_vertical_mine_entrance` at `surface_structures`. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | Configured feature declaration | Type is `immersive_ore_expedition:tiny_vertical_mine_entrance`; config is `{}` | `CANDIDATE_FUTURE_TOUCH` | Declaration-only unless a valid placed feature is invoked through a biome path. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | Placed feature declaration | Feature is `immersive_ore_expedition:tiny_vertical_mine_entrance`; placement list is `[]` | `CANDIDATE_FUTURE_TOUCH` | Declaration-only and attached only through the empty smoke tag bridge by default. |

## Config Gates Observed

| Path | Observed role | Current state | Future touch classification | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Runtime placement gate defaults | `worldgen.runtimePlacementEnabled` default `false`; `worldgen.runtimePlacementDiagnostics` default `false` | `CANDIDATE_FUTURE_TOUCH` | Future changes must preserve an explicit disable path unless separately approved. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Runtime proof feature gate defaults | `worldgen.runtimeProofFeatureEnabled` default `false`; `worldgen.runtimeProofFeatureDiagnostics` default `false` | `CANDIDATE_FUTURE_TOUCH` | Future smoke must enable the proof bridge deliberately. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Province runtime integration gate | `worldgen.provinces.runtimeIntegrationEnabled` default `false` | `CANDIDATE_FUTURE_TOUCH` | Province runtime integration remains opt-in. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Province resource policy defaults | `worldgen.provinces.resourcePolicyRules` default `[]` | `READ_ONLY_OBSERVED` | Empty policy rules remain conservative by default. |

## Datapack And Worldgen Resources Observed

- Active shipped datapack/resource metadata: `src/main/resources/pack.mcmeta`.
- Active shipped smoke tag: `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json`.
- Active shipped biome modifier: `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json`.
- Active configured feature declaration: `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json`.
- Active placed feature declaration: `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json`.

## Future Separate Runtime PR Constraints

- Recheck this inventory after rebasing onto the then-current stack head.
- Name every active resource or source file before editing it.
- Keep runtime worldgen default-off unless the approved future scope explicitly changes that policy.
- Avoid broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld`.
- Do not reuse pre-change v23 smoke evidence as proof for changed runtime behavior.
- Regenerate static validation, CI evidence, and manual smoke evidence after any runtime/resource change.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this inventory.
- Do not modify legacy split-module source trees.
- Do not claim live placement from this inventory.
- Do not select a maintainer disposition without actual review.
