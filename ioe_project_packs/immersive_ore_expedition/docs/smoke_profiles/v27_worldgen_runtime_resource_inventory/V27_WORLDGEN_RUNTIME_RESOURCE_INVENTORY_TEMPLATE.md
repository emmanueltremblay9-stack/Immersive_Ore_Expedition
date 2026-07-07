# V27 Worldgen Runtime Resource Inventory

## Inventory Identity

- PR/task:
- Author:
- Inventory date/time:
- Branch/commit reviewed:
- Repository state:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Scope And Non-Authorization

| Check | Required | Observed | Notes |
| --- | --- | --- | --- |
| runtime still disabled in this PR | yes | | |
| no active resources changed in this PR | yes | | |
| read-only inventory only | yes | | |
| no smoke evidence invented | yes | | |
| v27 does not authorize a runtime PR | yes | | |
| v27 activates no runtime worldgen | yes | | |

## Prerequisite Chain

| Input | Required before future runtime use | Present | Notes |
| --- | --- | --- | --- |
| V23 smoke result from an actual run | yes | | `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md` |
| V24 maintainer decision over that result | yes | | `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md` |
| V25 readiness packet records `READY_FOR_RUNTIME_SLICE` | yes | | `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md` |
| V26 packet records valid runtime PR authorization state | yes | | `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md` |
| V27 inventory rechecked after latest rebase | yes | | This template is not authorization by itself. |

## Allowed Future Touch Classifications

Use only these values in inventory tables:

- `READ_ONLY_OBSERVED`
- `CANDIDATE_FUTURE_TOUCH`
- `DO_NOT_TOUCH_THIS_SLICE`
- `UNKNOWN_NEEDS_RECHECK`

## Observed Active Resource Surfaces

Paths are relative to `ioe_project_packs/immersive_ore_expedition`.

| Path | Observed role | Current state | Future touch classification | Notes |
| --- | --- | --- | --- | --- |
| `src/main/resources/pack.mcmeta` | Shipped resource pack metadata | `pack_format` observed as `34` | `DO_NOT_TOUCH_THIS_SLICE` | Recheck before any runtime PR. |
| `src/main/resources/META-INF/neoforge.mods.toml` | NeoForge mod metadata template | Gradle-expanded metadata placeholders observed | `DO_NOT_TOUCH_THIS_SLICE` | Not a worldgen activation surface for this slice. |
| `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | IOE smoke biome tag | `values` observed as `[]` | `CANDIDATE_FUTURE_TOUCH` | Must remain empty in this PR. |
| `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | NeoForge biome modifier smoke bridge | Observed target is only `#immersive_ore_expedition:worldgen_smoke_test_biomes` | `CANDIDATE_FUTURE_TOUCH` | Must not broaden biome binding in this PR. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | Configured feature declaration | Observed type is `immersive_ore_expedition:tiny_vertical_mine_entrance` | `CANDIDATE_FUTURE_TOUCH` | Declaration-only in default shipped resources. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | Placed feature declaration | Observed placement list is `[]` | `CANDIDATE_FUTURE_TOUCH` | Declaration-only unless invoked through a biome path. |
|  |  |  | `UNKNOWN_NEEDS_RECHECK` | Add only observed active files. |

## Config Gates Observed

| Path | Observed role | Current state | Future touch classification | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Runtime placement gate | `worldgen.runtimePlacementEnabled` default `false`; `worldgen.runtimePlacementDiagnostics` default `false` | `CANDIDATE_FUTURE_TOUCH` | Future changes need a disable path and fresh validation. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Runtime proof feature gate | `worldgen.runtimeProofFeatureEnabled` default `false`; `worldgen.runtimeProofFeatureDiagnostics` default `false` | `CANDIDATE_FUTURE_TOUCH` | Future smoke must enable this deliberately. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Province runtime integration gate | `worldgen.provinces.runtimeIntegrationEnabled` default `false` | `CANDIDATE_FUTURE_TOUCH` | Province runtime integration remains opt-in. |
| `src/main/java/com/oblixorprime/ioe/config/ImmersiveOreExpeditionConfig.java` | Province resource policy defaults | `worldgen.provinces.resourcePolicyRules` default `[]` | `READ_ONLY_OBSERVED` | Empty policy rules remain conservative by default. |

## Datapack/Worldgen Resources Observed

| Resource family | Observed paths | Current state | Notes |
| --- | --- | --- | --- |
| Pack metadata | `src/main/resources/pack.mcmeta` | `pack_format` observed as `34` | Recheck if Minecraft target changes. |
| Smoke biome tag | `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | Empty shipped tag | No real biome binding by default. |
| Biome modifier | `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | Targets only the IOE smoke tag | No broad biome tag observed. |
| Configured feature | `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | Declaration points at the IOE proof feature type | Does not prove invocation by itself. |
| Placed feature | `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | Empty placement list | Does not prove invocation by itself. |

## Future Separate Runtime PR Constraints

- Exact runtime objective:
- Exact active resources expected to change:
- Exact Java source files expected to change:
- Files explicitly out of scope:
- Default-off gate or disable path:
- Rollback path:
- Validation that must be regenerated:
- Manual smoke evidence that must be regenerated:
- Confirmation that pre-change v23 evidence will not be reused as proof:

## Validation Required Before Using This Inventory

- Recheck active `src/main/resources` file list.
- Recheck the shipped smoke tag remains empty before any deliberate runtime diff.
- Recheck the active biome modifier targets only the IOE smoke tag before any deliberate runtime diff.
- Recheck configured and placed feature declarations before any deliberate runtime diff.
- Recheck config gate defaults before any deliberate runtime diff.
- Run static diff/scope checks after any future runtime PR edits.
- Run JSON/content checks after any future runtime PR resource edits.
- Use GitHub Actions as automated validation source of truth.
- Capture fresh manual client/server/world smoke evidence after any runtime/resource change.

## Maintainer Disposition

Select one only after review:

- `INVENTORY_ONLY_ACCEPTED`
- `INVENTORY_NEEDS_CORRECTION`
- `RECHECK_AFTER_REBASE`
- `DO_NOT_USE_FOR_RUNTIME_PR`

## Disposition Basis

- Accepted observations:
- Corrections required:
- Rebase/recheck requirement:
- Reason this must not be used for a runtime PR:

## Boundaries

- `INVENTORY_ONLY_ACCEPTED` activates runtime worldgen: no
- `INVENTORY_ONLY_ACCEPTED` authorizes active resource changes: no
- Active `src/main/resources` may be changed by this v27 inventory: no
- Future implementation must be a separate PR: yes
- Future runtime/resource change needs fresh validation: yes
