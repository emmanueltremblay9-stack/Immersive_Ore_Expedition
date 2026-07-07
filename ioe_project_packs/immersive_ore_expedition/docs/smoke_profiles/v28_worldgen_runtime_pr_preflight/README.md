# V28 Worldgen Runtime PR Preflight

This directory is a docs-only runtime PR preflight manifest package prepared after the v27 active runtime resource inventory. It turns the v23 through v27 evidence and planning chain into a checklist a future, separate runtime PR must complete before changing active resources or source.

v28 does not run smoke, accept evidence, make anything `READY_FOR_RUNTIME_SLICE`, select `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR`, accept `INVENTORY_ONLY_ACCEPTED`, activate runtime worldgen, change active `src/main/resources`, change config defaults, or modify legacy split-module source trees.

## Inputs

- V23 runbook/result source: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 evidence gate/decision source: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- V25 readiness packet source: `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`
- V26 implementation packet source: `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md`
- V27 inventory source: `../v27_worldgen_runtime_resource_inventory/V27_WORLDGEN_RUNTIME_RESOURCE_INVENTORY_TEMPLATE.md`
- V28 preflight manifest: `V28_WORLDGEN_RUNTIME_PR_PREFLIGHT_TEMPLATE.md`

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- This package is a preflight manifest only.
- No smoke evidence is invented or accepted by this package.
- v28 does not authorize a runtime PR, modifies no active resources, and activates no runtime worldgen.
- A future runtime PR must fill the manifest after rebasing, name every intended diff, and regenerate validation after edits.

## Required Prerequisite Chain

The future runtime PR owner must fill the preflight template with current evidence for:

- a completed v23 smoke result from an actual run;
- a v24 maintainer decision compatible with runtime planning;
- a v25 readiness packet that records `READY_FOR_RUNTIME_SLICE`;
- a v26 packet that records valid authorization for a separate runtime PR;
- a v27 inventory that has been rechecked after the latest rebase.

This README does not mark any prerequisite as present or accepted.

## Candidate Future Runtime Diff Manifest

The preflight template records candidate active resource/source paths, expected change type, reason, source prerequisite, rollback expectation, validation owner, and status. The allowed manifest statuses are:

- `UNSPECIFIED`
- `EXPECTED_RUNTIME_CHANGE`
- `EXPECTED_TEST_OR_VALIDATION_CHANGE`
- `EXPECTED_DOC_UPDATE`
- `MUST_NOT_CHANGE`
- `NEEDS_RECHECK`

`UNSPECIFIED` is the safe default until a future runtime PR owner names a concrete diff and evidence source.

## Future Separate Runtime PR Constraints

- Keep the future diff small and explicit.
- Preserve a default-off gate or documented disable path unless a separately approved future scope changes that policy.
- Avoid broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld`.
- Do not reuse pre-change v23 smoke evidence as proof for changed runtime behavior.
- Recheck the shipped smoke tag, biome modifier, configured feature, placed feature, and config gates after rebase.
- Run static, content, CI, and manual smoke validations appropriate to the future runtime diff.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this preflight package.
- Do not modify legacy split-module source trees.
- Do not claim live placement from this preflight package.
- Do not select a maintainer preflight disposition without actual review.
