# V25 Worldgen Runtime Promotion Readiness

This directory is a docs-only readiness packet for planning a future runtime worldgen slice after the v23 smoke result and v24 maintainer evidence gate. It does not run smoke, accept evidence, activate runtime worldgen, change active resources, or change config defaults.

Use this packet only after:

- a v23 smoke result file was filled from an actual run; and
- a v24 maintainer decision reviewed that evidence.

If either input is missing, the disposition is `DOCS_ONLY_HOLD` or `BLOCKED`, not `READY_FOR_RUNTIME_SLICE`.

## Inputs

- V23 runbook: `../v23_worldgen_smoke_runbook/README.md`
- V23 result template: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V24 evidence gate: `../v24_worldgen_smoke_evidence_gate/README.md`
- V24 decision template: `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`
- Readiness template: `V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md`

## Allowed Dispositions

- `DOCS_ONLY_HOLD`: evidence is not enough to scope implementation, or the next safe work is documentation only.
- `REPEAT_SMOKE`: the controlled smoke must be rerun before implementation planning.
- `BLOCKED`: required evidence, maintainer decision, or technical constraints are missing.
- `READY_FOR_RUNTIME_SLICE`: evidence and maintainer review are enough to scope a future implementation PR.

`READY_FOR_RUNTIME_SLICE` is not permission to merge runtime behavior in this packet. It only means a later PR can be planned.

## Future Runtime Slice Rules

Any later runtime PR must be separate from this packet and must:

- keep the diff small and explicit;
- name every active `src/main/resources` or source file it changes;
- keep runtime worldgen default-off unless a later approved scope says otherwise;
- include a rollback or disable path;
- rerun relevant validation after the runtime/resource change;
- never reuse v23 smoke evidence as proof for changed runtime behavior.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, or placed features from this packet.
- Do not change runtime config defaults from this packet.
- Do not claim live placement from this packet.
- Do not mark `READY_FOR_RUNTIME_SLICE` without a referenced v23 result and v24 decision.
