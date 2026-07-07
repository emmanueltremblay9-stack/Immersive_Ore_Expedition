# V28 Worldgen Runtime PR Preflight Manifest

## Manifest Identity

- PR/task:
- Author:
- Preflight date/time:
- Branch/commit reviewed:
- Future runtime PR branch, if known:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Scope And Non-Authorization

| Check | Required | Observed | Notes |
| --- | --- | --- | --- |
| runtime still disabled in this PR | yes | | |
| no active resources changed in this PR | yes | | |
| preflight manifest only | yes | | |
| no smoke evidence invented | yes | | |
| v28 does not authorize a runtime PR | yes | | |
| v28 modifies no active resources | yes | | |
| v28 activates no runtime worldgen | yes | | |

## Prerequisite Chain

| Input | Required before future runtime PR | Evidence path | Present | Notes |
| --- | --- | --- | --- | --- |
| V23 smoke result from an actual run | yes | `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md` | | |
| V24 maintainer decision over that result | yes | `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md` | | |
| V25 readiness packet | yes | `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md` | | |
| V26 runtime slice packet | yes | `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md` | | |
| V27 active runtime resource inventory | yes | `../v27_worldgen_runtime_resource_inventory/V27_WORLDGEN_RUNTIME_RESOURCE_INVENTORY_TEMPLATE.md` | | |
| V28 manifest rechecked after latest rebase | yes | this file | | |

## Required Prerequisite Dispositions

Fill these values from actual reviewed evidence. Do not preselect or infer them.

| Prerequisite | Required expected value | Observed value | Evidence file | Notes |
| --- | --- | --- | --- | --- |
| V23 smoke result | completed result from an actual client or server run | | | |
| V24 maintainer decision | decision compatible with runtime planning | | | |
| V25 maintainer disposition | `READY_FOR_RUNTIME_SLICE` | | | |
| V26 authorization state | `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` | | | |
| V27 maintainer disposition | `INVENTORY_ONLY_ACCEPTED` | | | |

## Allowed Manifest Statuses

Use only these values in the candidate future runtime diff manifest:

- `UNSPECIFIED`
- `EXPECTED_RUNTIME_CHANGE`
- `EXPECTED_TEST_OR_VALIDATION_CHANGE`
- `EXPECTED_DOC_UPDATE`
- `MUST_NOT_CHANGE`
- `NEEDS_RECHECK`

## Candidate Future Runtime Diff Manifest

Paths are relative to `ioe_project_packs/immersive_ore_expedition`.

| Path | Expected change type | Reason | Source prerequisite | Rollback expectation | Validation owner | Status |
| --- | --- | --- | --- | --- | --- | --- |
| `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | `UNSPECIFIED` | Possible future controlled biome binding surface | V27 inventory plus V26 packet | Restore shipped empty tag or remove intentional binding | future PR owner | `UNSPECIFIED` |
| `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | `UNSPECIFIED` | Possible future biome modifier invocation surface | V27 inventory plus V26 packet | Restore IOE smoke-tag-only target | future PR owner | `UNSPECIFIED` |
| `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | `UNSPECIFIED` | Configured feature declaration surface | V27 inventory plus V26 packet | Restore declaration-only config | future PR owner | `UNSPECIFIED` |
| `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | `UNSPECIFIED` | Placed feature declaration surface | V27 inventory plus V26 packet | Restore empty placement list or documented prior value | future PR owner | `UNSPECIFIED` |
| `src/main/java/com/oblixorprime/ioe/worldgen/IoeWorldgenPlacementGates.java` | `UNSPECIFIED` | Runtime placement gate surface | V26 packet plus V27 inventory | Preserve or restore explicit disable path | future PR owner | `UNSPECIFIED` |
| `src/main/java/com/oblixorprime/ioe/worldgen/IoeRuntimeProofFeatureGates.java` | `UNSPECIFIED` | Runtime proof feature gate surface | V26 packet plus V27 inventory | Preserve or restore explicit disable path | future PR owner | `UNSPECIFIED` |
| `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenRegistrationSmokeBridgeFeature.java` | `UNSPECIFIED` | Runtime feature invocation surface | V26 packet plus fresh smoke evidence | Restore no-op behavior behind default-off gate | future PR owner | `UNSPECIFIED` |
| `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenPlacementProof.java` | `UNSPECIFIED` | Placement proof behavior surface | V26 packet plus fresh smoke evidence | Restore resource-policy checked proof behavior | future PR owner | `UNSPECIFIED` |
| `src/main/resources/pack.mcmeta` | `MUST_NOT_CHANGE` | Resource pack metadata is not part of the planned runtime behavior diff | V27 inventory | Leave unchanged unless Minecraft target changes in a separate scope | future PR owner | `MUST_NOT_CHANGE` |
| `src/main/resources/META-INF/neoforge.mods.toml` | `MUST_NOT_CHANGE` | Mod metadata template is not part of the planned runtime behavior diff | V27 inventory | Leave unchanged unless metadata scope is separately approved | future PR owner | `MUST_NOT_CHANGE` |
|  | `UNSPECIFIED` |  |  |  |  | `UNSPECIFIED` |

## Explicit Future PR Constraints

- Exact runtime objective:
- Exact active resource files expected to change:
- Exact Java source files expected to change:
- Exact test or validation files expected to change:
- Exact docs expected to change:
- Files explicitly marked `MUST_NOT_CHANGE`:
- Default-off gate or disable path:
- Reason pre-change evidence is insufficient after the diff:
- Fresh smoke evidence required after the diff:

## Required Validation Before Future Runtime PR

- Recheck v23 result evidence and v24/v25/v26/v27 dispositions.
- Recheck active `src/main/resources` file list after rebase.
- Recheck the shipped smoke tag default state before editing.
- Recheck the active biome modifier target before editing.
- Recheck configured and placed feature declarations before editing.
- Recheck config gate defaults before editing.
- Run static diff/scope checks after edits.
- Run JSON/content checks after resource edits.
- Run permitted targeted tests or document why project policy requires GitHub Actions instead.
- Use GitHub Actions as automated validation source of truth.
- Capture fresh manual client/server/world smoke evidence after any runtime/resource change.

## Rollback/Disable Expectation

- Default-off config gate that keeps the future runtime change inert:
- Resource removal or source revert that disables the future runtime change:
- Expected behavior after rollback/disable:
- Validation required after rollback/disable:
- Manual smoke evidence required after rollback/disable:

## Out Of Scope For v28

- Active shipped biome tag edits.
- Active shipped biome modifier edits.
- Active configured feature or placed feature edits.
- Java source edits.
- Config default changes.
- Legacy split-module source tree edits.
- Runtime worldgen activation.
- Smoke execution or smoke evidence acceptance.
- Runtime PR authorization.

## Maintainer Preflight Disposition

Select one only after review:

- `PREFLIGHT_ONLY_ACCEPTED`
- `PREFLIGHT_NEEDS_CORRECTION`
- `PREFLIGHT_BLOCKED`
- `RECHECK_AFTER_REBASE`
- `DO_NOT_USE_FOR_RUNTIME_PR`

## Disposition Basis

- Accepted prerequisites:
- Missing or stale prerequisites:
- Manifest corrections required:
- Blocking issue:
- Rebase/recheck requirement:
- Reason this must not be used for a runtime PR:

## Boundaries

- `PREFLIGHT_ONLY_ACCEPTED` activates runtime worldgen: no
- `PREFLIGHT_ONLY_ACCEPTED` authorizes active resource changes: no
- Active `src/main/resources` may be changed by this v28 preflight: no
- Future implementation must be a separate PR: yes
- Future runtime/resource change needs fresh validation: yes
