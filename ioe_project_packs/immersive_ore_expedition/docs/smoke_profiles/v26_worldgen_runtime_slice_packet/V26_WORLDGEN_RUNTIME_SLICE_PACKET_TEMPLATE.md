# V26 Worldgen Runtime Slice Packet

## Packet Identity

- PR/task:
- Author:
- Review date/time:
- Branch/commit reviewed:
- V23 smoke result file:
- V24 maintainer decision file:
- V25 readiness packet file:

## Evidence Chain

| Input | Required | Present | Notes |
| --- | --- | --- | --- |
| Completed v23 smoke result from an actual run | yes | | |
| V24 maintainer decision over that result | yes | | |
| V24 decision allows runtime planning | yes | | |
| V25 packet records `READY_FOR_RUNTIME_SLICE` | yes | | |
| Runtime still disabled in this PR | yes | | |
| No active resources changed in this PR | yes | | |
| No smoke evidence invented | yes | | |

Reference paths:

- V23 runbook/result source: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 decision source: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- V25 readiness source: `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`

## Candidate Future Runtime Files

Record only read-only observations in this packet. A future implementation PR must update this table with the exact files it will change before editing them.

| Candidate path | Current status | Future role | Notes |
| --- | --- | --- | --- |
| `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | READ_ONLY_OBSERVED | optional future biome binding surface | Active shipped tag must remain empty in this PR. |
| `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | READ_ONLY_OBSERVED | optional future biome modifier surface | Currently targets only the IOE smoke tag. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | READ_ONLY_OBSERVED | configured feature declaration surface | Currently declaration-only. |
| `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | READ_ONLY_OBSERVED | placed feature declaration surface | Currently has an empty placement list. |
| `src/main/java/com/oblixorprime/ioe/worldgen/IoeWorldgenPlacementGates.java` | READ_ONLY_OBSERVED | placement gate surface | Future changes must preserve a disable path. |
| `src/main/java/com/oblixorprime/ioe/worldgen/IoeRuntimeProofFeatureGates.java` | READ_ONLY_OBSERVED | proof feature gate surface | Future changes must preserve a disable path. |
| `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenRegistrationSmokeBridgeFeature.java` | READ_ONLY_OBSERVED | runtime feature invocation surface | Future changes need fresh smoke evidence. |
| `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenPlacementProof.java` | READ_ONLY_OBSERVED | placement proof behavior surface | Future changes need fresh smoke evidence. |
|  | READ_ONLY_OBSERVED |  | Fill only with observed active files. |

## Minimum Separate Runtime PR Scope

- Proposed runtime objective:
- Smallest behavior or resource diff:
- Exact active files expected to change:
- Exact active files explicitly not expected to change:
- Config default changes expected:
- Runtime still default-off after the change: yes / no
- Fresh smoke evidence required after the change:

## Explicit Out Of Scope

- Broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld`.
- Production-world testing.
- Claiming live placement from pre-change evidence.
- Adding ores, gems, blocks, items, entities, fluids, mixins, access transformers, embedded jars, or dependencies unless a separate approved runtime scope names them.
- Changing legacy split-module source trees.
- Changing active `src/main/resources` from this v26 docs packet.

## Required Validations For Future Runtime PR

- Static diff/scope check:
- Active resource JSON/content check:
- Default-off config check:
- Gate/disable behavior check:
- GitHub Actions check:
- Manual client smoke evidence:
- Manual dedicated-server smoke evidence:
- Fresh log and coordinate evidence:
- Confirmation that v23 evidence was not reused as proof for changed behavior:

## Rollback Or Disable Plan

- Default-off config gate that keeps the runtime change inert:
- Resource or source revert that disables the runtime change:
- Expected behavior after rollback/disable:
- Evidence needed to confirm rollback/disable:

## Maintainer Authorization State

Select one:

- NOT_AUTHORIZED
- AWAITING_V23_SMOKE
- AWAITING_V24_DECISION
- AWAITING_V25_READY_FOR_RUNTIME_SLICE
- AUTHORIZED_FOR_SEPARATE_RUNTIME_PR

`AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` cannot be selected unless the completed v23 smoke result, v24 maintainer decision, and v25 `READY_FOR_RUNTIME_SLICE` packet are all present and referenced above. AUTHORIZED_FOR_SEPARATE_RUNTIME_PR cannot be checked without complete v23/v24/v25 evidence.

## Authorization Basis

- Accepted evidence:
- Missing evidence:
- Runtime PR scope summary, if authorized:
- Reason authorization is blocked or awaiting, if applicable:

## Boundaries

- runtime still disabled in this PR: yes
- no active resources changed in this PR: yes
- no smoke evidence invented: yes
- Future implementation must be a separate PR: yes
- Future runtime/resource change needs fresh validation: yes
