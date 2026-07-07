# V26 Worldgen Runtime Slice Implementation Packet

This directory is a docs-only implementation packet for scoping the smallest future runtime worldgen PR after the v23 smoke result, v24 maintainer evidence gate, and v25 readiness packet. It does not run smoke, accept evidence, activate runtime worldgen, change active resources, or change config defaults.

Use this packet only to describe a future separate runtime PR. If the v23, v24, or v25 inputs are missing or incomplete, the authorization state must remain an awaiting or not-authorized state.

## Inputs

- V23 runbook: `../v23_worldgen_smoke_runbook/README.md`
- V23 result template: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 evidence gate: `../v24_worldgen_smoke_evidence_gate/README.md`
- V24 decision template: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- V25 readiness packet: `../v25_worldgen_runtime_promotion_readiness/README.md`
- V25 readiness template: `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`
- V26 implementation packet template: `V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md`

## Allowed Packet States

- `NOT_AUTHORIZED`: no runtime PR may be scoped from the current evidence chain.
- `AWAITING_V23_SMOKE`: a completed v23 smoke result from an actual run is missing.
- `AWAITING_V24_DECISION`: a maintainer decision over the v23 evidence is missing.
- `AWAITING_V25_READY_FOR_RUNTIME_SLICE`: the v25 readiness packet is missing or does not record `READY_FOR_RUNTIME_SLICE`.
- `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR`: the evidence chain is complete and permits a later separate runtime PR to be scoped.

`AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` cannot be selected unless the v23 smoke result, v24 decision, and v25 readiness packet are all referenced and complete. In plain terms, AUTHORIZED_FOR_SEPARATE_RUNTIME_PR cannot be checked without complete v23/v24/v25 evidence. v26 does not make anything `READY`, does not activate runtime worldgen, and does not authorize changes inside this PR.

## Candidate Future Runtime Surfaces

The active files below were observed read-only while preparing this packet. A future runtime PR may choose a smaller subset, but it must name every touched active resource or source file explicitly before editing:

- `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json`
- `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json`
- `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json`
- `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json`
- `src/main/java/com/oblixorprime/ioe/worldgen/IoeWorldgenPlacementGates.java`
- `src/main/java/com/oblixorprime/ioe/worldgen/IoeRuntimeProofFeatureGates.java`
- `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenRegistrationSmokeBridgeFeature.java`
- `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenPlacementProof.java`

## Future Runtime PR Rules

Any later runtime PR must be separate from this packet and must:

- keep the diff small enough to review as one runtime behavior change;
- preserve default-off behavior unless the approved scope explicitly changes it;
- include a disable or rollback path;
- regenerate smoke evidence after the runtime/resource change;
- avoid broad biome bindings such as `#minecraft:is_overworld` or `#c:is_overworld`;
- never reuse pre-change v23 smoke evidence as proof for changed behavior.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, or Java source from this packet.
- Do not change runtime config defaults from this packet.
- Do not claim live placement from this packet.
- Do not select `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` without complete v23, v24, and v25 evidence.
