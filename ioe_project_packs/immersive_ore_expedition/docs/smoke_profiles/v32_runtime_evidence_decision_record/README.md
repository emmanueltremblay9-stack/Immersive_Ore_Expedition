# V32 Runtime Evidence Decision Record

This directory is a docs-only runtime evidence decision record prepared after the v31 runtime evidence review checklist. It gives the decision owner a traceable place to record the final disposition of a reviewed v30 evidence packet and v31 checklist after a future real smoke run.

This decision record is documentation-only and does not activate worldgen or smoke behavior.

v32 does not run smoke, replace the v29 matrix, replace the v30 evidence packet, replace the v31 review checklist, accept runtime behavior by itself, authorize a runtime PR, activate runtime worldgen, change active `src/main/resources`, change active JSON, change config defaults, or modify legacy split-module source trees.

## Inputs

- V29 traceability matrix source: `../v29_worldgen_runtime_traceability_matrix/V29_WORLDGEN_RUNTIME_TRACEABILITY_MATRIX_TEMPLATE.md`
- V30 evidence packet source: `../v30_runtime_evidence_packet/V30_RUNTIME_EVIDENCE_PACKET_TEMPLATE.md`
- V31 review checklist source: `../v31_runtime_evidence_review_checklist/V31_RUNTIME_EVIDENCE_REVIEW_CHECKLIST.md`
- V32 decision record: `V32_RUNTIME_EVIDENCE_DECISION_RECORD.md`

## Decision Record Purpose

Use this package to:

- transform a completed v31 review into a traceable final decision;
- preserve references to the v29, v30, and v31 sources rather than replacing them;
- document why evidence was accepted, rejected, missing, contradictory, or out of scope;
- prevent invented runtime evidence from entering the release or runtime-planning chain.

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- No active JSON changed in this PR.
- This package is a decision record template only.
- No runtime evidence is invented or accepted by this package alone.
- `ACCEPTED` applies only to the evidence packet and review scope recorded in the decision record.
- A future runtime PR must still be separate, explicitly scoped, validated, reviewed, and authorized.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this decision record.
- Do not modify legacy split-module source trees.
- Do not claim smoke passed from this record unless the referenced v30 packet and v31 checklist contain actual run artifacts.
- Do not use this record to fill gaps in v29, v30, or v31 evidence.
- Do not approve evidence that cannot be tied to the evaluated commit, PR, profile, and artifact bundle.
