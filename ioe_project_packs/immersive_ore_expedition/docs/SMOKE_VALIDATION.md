# IOE Smoke Validation

This document describes manual smoke validation for Immersive Ore Expedition. Do not claim smoke passed unless the smoke was actually run and evidence was captured.

Local smoke validation is disabled by default for the Codex workflow. GitHub Actions remains the automated validation source of truth.

## Evidence To Record

- Smoke date and time.
- Minecraft version.
- NeoForge version.
- Java version.
- IOE jar filename.
- IOE jar SHA-256.
- Client or server log path.
- Fresh `latest.log` copy or excerpt location.
- Pass/fail status.
- Notes for any crash, fatal, missing dependency, or command failure.

## Client World-Entry Smoke

1. Install the release jar into a controlled Minecraft 1.21.1 / NeoForge client profile.
2. Start the client with a fresh log.
3. Enter or create a test world.
4. Confirm the mod loads without a crash.
5. Confirm `immersive_ore_expedition-common.toml` is generated or loaded.
6. Confirm there are no crash, fatal, or repeated error signatures in the checked log window.
7. If admin commands are available in the profile, confirm they respond safely and do not mutate the world unexpectedly.
8. Record the evidence listed above.

Expected current limitation: no visible IOE worldgen placement is expected from v7-v34 with default config. Current systems are scaffold, planning, policy, validation layers, a default-off placement proof gate, a default-off registration smoke bridge, declaration-only configured/placed feature data, a biome modifier smoke-tag bridge whose shipped tag binds zero real biomes by default, and docs-only controlled smoke, evidence, readiness, implementation packet, active resource inventory, runtime PR preflight, traceability matrix, runtime evidence packet, runtime evidence review checklist, runtime evidence decision record, runtime evidence remediation tracker, and runtime evidence remediation closure record documentation.

## v18 Runtime Placement Proof Smoke

v18 adds a default-off runtime placement proof path. With default config, no placement is expected:

```toml
worldgen.runtimePlacementEnabled = false
worldgen.runtimePlacementDiagnostics = false
```

Only use the proof path in a controlled smoke profile:

```toml
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

Evidence must include the normal smoke fields plus the exact config values above and the fresh log lines showing whether the v18 runtime placement proof was skipped, ready, or placed. A valid proof still requires anchor validation, a loaded block resource, resource-policy approval, strict-exclusion safety, a writable generation region, and an empty target block. Missing or denied resources must be recorded as skipped, not replaced with fallback blocks.

v18 does not register configured features, placed features, biome modifiers, structures, or the complete surface clue to ore-load gameplay loop. Do not mark live gameplay proof complete unless a manual world smoke run captures actual placement evidence.

## v19 Runtime Registration Smoke Bridge

v19 registers one custom feature type, `immersive_ore_expedition:tiny_vertical_mine_entrance`, for future controlled smoke profiles. It does not add a configured feature, placed feature, biome modifier, datapack JSON, or gameplay-loop placement by itself.

With default config, registered feature invocations remain no-op:

```toml
worldgen.runtimeProofFeatureEnabled = false
worldgen.runtimeProofFeatureDiagnostics = false
worldgen.runtimePlacementEnabled = false
worldgen.runtimePlacementDiagnostics = false
```

A controlled future smoke profile must explicitly enable the v19 bridge and the v18 runtime placement gate before any proof placement can be attempted. Evidence must record whether the registered bridge was skipped because a gate was disabled, skipped because resource policy denied or skipped the proof resource, skipped by world safety checks, ready, attempted, or placed. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v20 Configured/Placed Feature Declaration Bridge

v20 adds one configured feature JSON and one placed feature JSON for `immersive_ore_expedition:tiny_vertical_mine_entrance`. The configured feature points at the v19 custom feature type with empty `NoneFeatureConfiguration` config, and the placed feature references it with an empty placement modifier list.

v20 does not add a biome modifier or bind the placed feature to any biome, so default worlds still do not invoke the feature. A future smoke profile must explicitly add a controlled invocation path and still enable both v19 and v18 gates before placement can be attempted. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v21 Default-Off Biome Modifier Smoke-Tag Bridge

v21 adds one NeoForge biome modifier declaration, `immersive_ore_expedition:tiny_vertical_mine_entrance_smoke_bridge`, that points at the existing v20 placed feature through the IOE-owned tag `#immersive_ore_expedition:worldgen_smoke_test_biomes`. The shipped tag contains zero biome ids:

```json
{
  "replace": false,
  "values": []
}
```

With default shipped resources and config, no real biome receives the placed feature, so no world mutation is expected. A controlled future manual smoke profile may use an external datapack to add one explicit test biome to the smoke tag, but the smoke profile must also enable both default-off gates before any placement proof can be attempted:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

That external smoke setup is not shipped as a default binding and does not prove live placement unless manual client/server/world evidence is captured. Missing, denied, unsupported, or strictly excluded resources must still be recorded as skipped, not replaced with fallback blocks. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v22 Controlled External Worldgen Smoke Profile

v22 adds a docs-only smoke profile package at `docs/smoke_profiles/v22_worldgen_smoke_profile/`. It is a manual setup aid only, not active shipped data and not smoke evidence by itself.

The profile datapack appends exactly one explicit vanilla biome id to the IOE smoke tag:

```json
{
  "replace": false,
  "values": [
    "minecraft:plains"
  ]
}
```

The profile config template enables both default-off gates for a disposable test world:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
worldgen.provinces.runtimeIntegrationEnabled = false
worldgen.provinces.resourcePolicyRules = []
```

Do not use broad biome tags, modded biomes, or multiple test biomes for this controlled profile. Do not claim live placement unless the evidence template records a fresh log, enabled datapack, exact config values, checked coordinates, and observed placement evidence. Manual client/server/world smoke was not run by v22.

## v23 Controlled Worldgen Smoke Runbook

v23 adds a docs-only runbook and result template at `docs/smoke_profiles/v23_worldgen_smoke_runbook/`. It turns the v22 profile into a repeatable evidence collection procedure without changing shipped resources, config defaults, biome bindings, or runtime behavior.

Use the v23 runbook only with the v22 external datapack and config template. A valid result must record the release jar identity, jar SHA-256, enabled datapack confirmation, exact config values, fresh `latest.log` path or excerpt, checked biome/coordinates, client or dedicated-server status, and the observed outcome classification.

The v23 result template may record `not run`, `startup failed`, `world load failed`, `datapack rejected`, `config rejected`, `feature skipped`, `placement attempted`, or `placement observed`. Only `placement observed` with captured coordinate/log evidence can support a live placement proof claim. Manual smoke was not run by v23 unless a completed result file records that evidence.

## v24 Post-Smoke Evidence Gate

v24 adds a docs-only maintainer decision gate at `docs/smoke_profiles/v24_worldgen_smoke_evidence_gate/`. Use it only after a v23 smoke result has been filled from an actual run. The gate separates raw smoke execution from maintainer acceptance, keeps runtime worldgen disabled by default, and blocks any promotion to runtime integration unless the required client/server/datapack/log/coordinate evidence is present.

The v24 gate does not activate runtime worldgen, does not change shipped resources or config defaults, and does not replace the v23 runbook. If no completed v23 result exists, the correct v24 decision is `no-go`.

## v25 Runtime Promotion Readiness Packet

v25 adds a docs-only readiness packet at `docs/smoke_profiles/v25_worldgen_runtime_promotion_readiness/`. Use it only after a completed v23 smoke result and v24 maintainer decision exist. It translates the evidence chain into one allowed disposition: `DOCS_ONLY_HOLD`, `REPEAT_SMOKE`, `BLOCKED`, or `READY_FOR_RUNTIME_SLICE`.

`READY_FOR_RUNTIME_SLICE` does not activate runtime worldgen and does not authorize active resource changes by itself. It only means a future, separate PR may be scoped with explicit runtime diffs, rollback expectations, and validation commands.

## v26 Runtime Slice Implementation Packet

v26 adds a docs-only runtime slice implementation packet at `docs/smoke_profiles/v26_worldgen_runtime_slice_packet/`. It maps candidate future runtime files, required preconditions, rollback expectations, and validation requirements for a later separate runtime PR.

v26 does not activate runtime worldgen, does not change active `src/main/resources`, does not change config defaults, and does not create smoke evidence. The only allowed authorization states are `NOT_AUTHORIZED`, `AWAITING_V23_SMOKE`, `AWAITING_V24_DECISION`, `AWAITING_V25_READY_FOR_RUNTIME_SLICE`, and `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR`. `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` cannot be selected without a completed v23 smoke result, v24 maintainer decision, and v25 `READY_FOR_RUNTIME_SLICE` packet.

## v27 Runtime Resource Inventory Snapshot

v27 adds a docs-only active runtime resource inventory snapshot at `docs/smoke_profiles/v27_worldgen_runtime_resource_inventory/`. It records read-only observations of the shipped smoke tag, biome modifier, configured feature, placed feature, resource metadata, and config gate surfaces that a later separate runtime PR would need to recheck before editing.

v27 does not activate runtime worldgen, does not authorize a runtime PR, does not change active `src/main/resources`, does not change config defaults, does not modify legacy split-module source trees, and does not create smoke evidence. The inventory uses the classifications `READ_ONLY_OBSERVED`, `CANDIDATE_FUTURE_TOUCH`, `DO_NOT_TOUCH_THIS_SLICE`, and `UNKNOWN_NEEDS_RECHECK`; maintainer disposition must remain separate from smoke evidence and future runtime authorization.

## v28 Runtime PR Preflight Manifest

v28 adds a docs-only runtime PR preflight manifest at `docs/smoke_profiles/v28_worldgen_runtime_pr_preflight/`. It requires a future runtime PR owner to reconcile the v23 smoke result, v24 maintainer gate, v25 readiness packet, v26 implementation packet, and v27 inventory before naming active runtime diffs.

v28 does not activate runtime worldgen, does not authorize a runtime PR, does not change active `src/main/resources`, does not change config defaults, does not modify legacy split-module source trees, and does not create smoke evidence. The manifest statuses are `UNSPECIFIED`, `EXPECTED_RUNTIME_CHANGE`, `EXPECTED_TEST_OR_VALIDATION_CHANGE`, `EXPECTED_DOC_UPDATE`, `MUST_NOT_CHANGE`, and `NEEDS_RECHECK`; maintainer preflight disposition must remain separate from smoke evidence and future runtime authorization.

## v29 Runtime Traceability Matrix

v29 adds a docs-only runtime traceability matrix at `docs/smoke_profiles/v29_worldgen_runtime_traceability_matrix/`. It links candidate future runtime paths to v23 smoke evidence, the v24 maintainer gate, v25 readiness, the v26 implementation packet, the v27 inventory, and the v28 preflight manifest.

v29 does not activate runtime worldgen, does not authorize a runtime PR, does not change active `src/main/resources`, does not change config defaults, does not modify legacy split-module source trees, and does not create smoke evidence. The traceability statuses are `UNMAPPED`, `PARTIALLY_MAPPED`, `FULLY_MAPPED_NOT_AUTHORIZED`, `BLOCKED_BY_MISSING_EVIDENCE`, `BLOCKED_BY_STALE_INVENTORY`, and `READY_FOR_MAINTAINER_REVIEW_ONLY`; maintainer traceability disposition must remain separate from smoke evidence and future runtime authorization.

## v30 Runtime Evidence Packet

v30 adds a docs-only runtime evidence packet at `docs/smoke_profiles/v30_runtime_evidence_packet/`. It turns the v29 traceability matrix into a future smoke evidence capture template for run identity, config proof, datapack/worldgen proof, runtime logs, chunk/biome sampling, artifact references, and `PASS` / `FAIL` / `INCONCLUSIVE` criteria.

v30 does not execute smoke, does not activate runtime worldgen, does not authorize a runtime PR, does not change active `src/main/resources`, does not change active JSON, does not change config defaults, does not modify legacy split-module source trees, and does not create smoke evidence. The template accepts `not executed` as a placeholder during docs-only preparation, but any unexecuted runtime proof remains `INCONCLUSIVE` until a future real smoke run captures artifacts.

## v31 Runtime Evidence Review Checklist

v31 adds a docs-only runtime evidence review checklist at `docs/smoke_profiles/v31_runtime_evidence_review_checklist/`. It lets a reviewer inspect a filled v30 packet after a future real smoke run and classify proof as `ABSENT`, `INCOMPLETE`, `CONTRADICTORY`, or `SUFFICIENT` before selecting `ACCEPTED`, `REJECTED`, `NEEDS_MORE_EVIDENCE`, or `OUT_OF_SCOPE`.

v31 does not execute smoke, does not activate runtime worldgen, does not authorize a runtime PR, does not change active `src/main/resources`, does not change active JSON, does not change config defaults, does not modify legacy split-module source trees, and does not create or invent smoke evidence. `ACCEPTED` applies only to the reviewed evidence packet scope and does not authorize active runtime/resource changes by itself.

## v32 Runtime Evidence Decision Record

v32 adds a docs-only runtime evidence decision record at `docs/smoke_profiles/v32_runtime_evidence_decision_record/`. It turns a completed v31 review of a filled v30 packet into a traceable final decision with required reason codes, evidence sufficiency matrix, gap ledger, remediation/next-action section, audit trail, and closure criteria.

v32 does not execute smoke, does not activate runtime worldgen, does not authorize a runtime PR, does not replace v29, v30, or v31, does not change active `src/main/resources`, does not change active JSON, does not change config defaults, does not modify legacy split-module source trees, and does not create or invent smoke evidence. `ACCEPTED` applies only to the recorded evidence decision scope and does not authorize active runtime/resource changes by itself.

## v33 Runtime Evidence Remediation Tracker

v33 adds a docs-only runtime evidence remediation tracker at `docs/smoke_profiles/v33_runtime_evidence_remediation_tracker/`. It turns v32 decisions, reason codes, gaps, and proposed next actions into traceable remediation items with owners, statuses, required proof, planned verification methods, closure gates, recollection planning, and a v33-to-v32 return matrix.

v33 does not execute smoke, does not activate runtime worldgen, does not authorize a runtime PR, does not replace v29, v30, v31, or v32, does not change active `src/main/resources`, does not change active JSON, does not change config defaults, does not modify legacy split-module source trees, and does not create or invent smoke evidence. `CLOSED` applies only to the tracked remediation action scope and does not authorize active runtime/resource changes by itself.

## v34 Runtime Evidence Remediation Closure Record

v34 adds a docs-only runtime evidence remediation closure record at `docs/smoke_profiles/v34_runtime_evidence_remediation_closure_record/`. It turns v33 remediation actions into verifiable closure decisions and records whether actions are verified closed, reopened, deferred, not verified, or out of scope after reviewing recollected proof.

v34 does not execute smoke, does not activate runtime worldgen, does not authorize a runtime PR, does not replace v29, v30, v31, v32, or v33, does not change active `src/main/resources`, does not change active JSON, does not change config defaults, does not modify legacy split-module source trees, and does not create or invent smoke evidence. `VERIFIED_CLOSED` applies only to the reviewed remediation action scope and does not authorize active runtime/resource changes by itself.

## Dedicated Server Smoke

1. Install the release jar into a controlled Minecraft 1.21.1 / NeoForge dedicated server.
2. Start the server with a fresh log.
3. Confirm startup reaches a ready state without a crash.
4. Confirm `immersive_ore_expedition-common.toml` is generated or loaded.
5. Join with a compatible client if the smoke scope includes login validation.
6. Confirm there are no crash, fatal, or repeated error signatures in the checked log window.
7. If admin commands are available, confirm they respond safely and do not mutate chunks unexpectedly.
8. Record the evidence listed above.

Expected current limitation: server smoke should validate load, config, and safe command behavior. It should not expect live ore-load chambers, anchors, clues, crystal sites, AE2 geodes, Nether geodes, Ancient Debris hearts, or retrogen resources to appear.

## Log Review Hints

Use a fresh `latest.log` for each smoke pass. Treat these as blockers until understood:

- crash reports;
- fatal errors;
- missing required dependencies;
- classloading failures;
- config parse failures;
- command registration failures;
- repeated noisy diagnostics that are enabled by default.

Optional dependency warnings should be recorded with context. Do not convert a warning into a pass or failure without checking whether it affects the smoke goal.
