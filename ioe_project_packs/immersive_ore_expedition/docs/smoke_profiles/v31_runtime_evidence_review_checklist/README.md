# V31 Runtime Evidence Review Checklist

This directory is a docs-only runtime evidence review checklist prepared after the v30 runtime evidence packet. It gives a reviewer a structured way to inspect a filled v30 evidence packet after a future real smoke run, qualify the evidence, and record whether the packet is accepted, rejected, needs more evidence, or is out of scope.

This checklist is documentation-only and does not activate worldgen or smoke behavior.

v31 does not run smoke, accept runtime behavior by itself, authorize a runtime PR, activate runtime worldgen, change active `src/main/resources`, change active JSON, change config defaults, or modify legacy split-module source trees.

## Inputs

- V29 traceability matrix source: `../v29_worldgen_runtime_traceability_matrix/V29_WORLDGEN_RUNTIME_TRACEABILITY_MATRIX_TEMPLATE.md`
- V30 evidence packet source: `../v30_runtime_evidence_packet/V30_RUNTIME_EVIDENCE_PACKET_TEMPLATE.md`
- V31 review checklist: `V31_RUNTIME_EVIDENCE_REVIEW_CHECKLIST.md`

## Review Purpose

Use this checklist to review a filled v30 packet and distinguish:

- missing evidence;
- incomplete evidence;
- contradictory evidence;
- sufficient evidence.

The reviewer must verify that evidence is not invented, inferred, backfilled, stale, copied from an incompatible commit, or detached from the branch, PR, jar/build provenance, logs, config, screenshots, or coordinates under review.

## Scope And Non-Authorization

- Runtime worldgen remains disabled in this PR.
- No active resources changed in this PR.
- No active JSON changed in this PR.
- This package is a review checklist only.
- No runtime evidence is invented or accepted by this package alone.
- `ACCEPTED` means the reviewed evidence packet is acceptable for its stated scope only; it does not activate worldgen or authorize active resource/source changes.
- A future runtime PR must still be separate, explicitly scoped, validated, and reviewed.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, placed features, Java source, or config defaults from this checklist.
- Do not modify legacy split-module source trees.
- Do not claim smoke passed from this checklist unless the reviewed v30 packet contains actual run artifacts.
- Do not resolve ambiguous proof by guessing.
- Do not approve evidence that cannot be tied to the evaluated commit, PR, profile, and artifact bundle.
