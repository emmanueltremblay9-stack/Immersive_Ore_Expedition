# V30 Runtime Evidence Packet

This directory is a docs-only runtime evidence packet prepared after the v29 runtime traceability matrix. It turns each v29 traceability row into a concrete evidence capture plan for a future real smoke run without executing Minecraft, running local Gradle, copying jars, or changing active resources.

This template is documentation-only and does not activate worldgen or smoke behavior.

v30 does not run smoke, accept evidence, make any v29 trace row complete, authorize a runtime PR, activate runtime worldgen, change active `src/main/resources`, change active JSON, change config defaults, or modify legacy split-module source trees.

## Inputs

- V22 controlled external datapack/config profile: `../v22_worldgen_smoke_profile/`
- V23 runbook/result source: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 evidence gate/decision source: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- V25 readiness packet source: `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`
- V26 implementation packet source: `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md`
- V27 inventory source: `../v27_worldgen_runtime_resource_inventory/V27_WORLDGEN_RUNTIME_RESOURCE_INVENTORY_TEMPLATE.md`
- V28 preflight manifest source: `../v28_worldgen_runtime_pr_preflight/V28_WORLDGEN_RUNTIME_PR_PREFLIGHT_TEMPLATE.md`
- V29 traceability matrix source: `../v29_worldgen_runtime_traceability_matrix/V29_WORLDGEN_RUNTIME_TRACEABILITY_MATRIX_TEMPLATE.md`
- V30 evidence packet template: `V30_RUNTIME_EVIDENCE_PACKET_TEMPLATE.md`

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- No active JSON changed in this PR.
- This package is an evidence packet template only.
- No smoke evidence is invented or accepted by this package.
- A future real smoke run may copy the template to a task-specific evidence file only when the run actually occurs.
- A future runtime PR must still satisfy the v23 through v29 chain before changing active runtime resources or source.

## Evidence Packet Purpose

The packet records:

- run identifiers, including local date/time, branch, commit, PR, jar/build provenance, and target client/server/world profile;
- configuration proof for runtime and province gates, expected values, observed values, and proof sources;
- datapack/worldgen proof for the active smoke biome tag, active biome modifier, configured feature, placed feature, registry/load evidence, and capture method;
- runtime proof for logs, warnings, world creation/load, chunk or biome sampling, screenshots, and log artifact references;
- a correspondence table from v29 trace rows to expected v30 evidence;
- final `PASS`, `FAIL`, or `INCONCLUSIVE` criteria;
- missing-proof triage steps that prevent accidental promotion from incomplete evidence.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this packet.
- Do not modify legacy split-module source trees.
- Do not claim live placement from this packet.
- Do not backfill missing evidence after a run.
- Do not use pre-change evidence as proof for changed runtime behavior.
- Do not select a pass result without actual smoke artifacts.
