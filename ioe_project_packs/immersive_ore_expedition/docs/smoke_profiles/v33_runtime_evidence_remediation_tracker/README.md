# V33 Runtime Evidence Remediation Tracker

This directory is a docs-only runtime evidence remediation tracker prepared after the v32 runtime evidence decision record. It gives maintainers a structured place to turn v32 decisions, gaps, and proposed next actions into traceable follow-up work without executing Minecraft, running local Gradle, copying jars, or changing active resources.

This remediation tracker is documentation-only and does not activate worldgen or smoke behavior.

v33 does not replace the v29 traceability matrix, the v30 evidence packet, the v31 review checklist, or the v32 decision record. It does not run smoke, accept runtime behavior by itself, authorize a runtime PR, activate runtime worldgen, change active `src/main/resources`, change active JSON, change config defaults, or modify legacy split-module source trees.

## Inputs

- V29 traceability matrix source: `../v29_worldgen_runtime_traceability_matrix/V29_WORLDGEN_RUNTIME_TRACEABILITY_MATRIX_TEMPLATE.md`
- V30 evidence packet source: `../v30_runtime_evidence_packet/V30_RUNTIME_EVIDENCE_PACKET_TEMPLATE.md`
- V31 review checklist source: `../v31_runtime_evidence_review_checklist/V31_RUNTIME_EVIDENCE_REVIEW_CHECKLIST.md`
- V32 decision record source: `../v32_runtime_evidence_decision_record/V32_RUNTIME_EVIDENCE_DECISION_RECORD.md`
- V33 remediation tracker: `V33_RUNTIME_EVIDENCE_REMEDIATION_TRACKER.md`

## Remediation Tracker Purpose

Use this package to:

- convert a v32 decision record into verifiable follow-up actions;
- preserve references to v29, v30, v31, and v32 rather than replacing them;
- track missing, contradictory, out-of-commit, illegible, or incomplete runtime evidence;
- keep remediation actions tied to the evaluated branch, commit, PR, artifact bundle, and decision record;
- prevent invented runtime evidence from entering the release or runtime-planning chain.

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- No active JSON changed in this PR.
- This package is a remediation tracker template only.
- No runtime evidence is invented or accepted by this package alone.
- `CLOSED` applies only to the remediation action scope recorded in the tracker.
- A future runtime PR must still be separate, explicitly scoped, validated, reviewed, and authorized.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this tracker.
- Do not modify legacy split-module source trees.
- Do not claim smoke passed from this tracker unless the referenced v30 packet, v31 checklist, v32 decision record, and new remediation artifacts contain actual run evidence.
- Do not close an action with inferred, stale, unreadable, off-commit, or invented proof.
- Do not approve evidence that cannot be tied to the evaluated commit, PR, profile, and artifact bundle.
