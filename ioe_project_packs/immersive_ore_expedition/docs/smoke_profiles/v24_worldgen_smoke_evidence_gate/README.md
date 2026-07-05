# V24 Worldgen Smoke Evidence Gate

This directory is a docs-only maintainer decision gate for results produced with the v23 controlled worldgen smoke runbook. It does not run smoke, add active data, change config defaults, enable runtime worldgen, or accept evidence by itself.

Use this gate only after a v23 result file has been filled from an actual client or dedicated-server smoke run. If no completed v23 result exists, the decision is `no-go`.

## Inputs

- V23 runbook: `../v23_worldgen_smoke_runbook/README.md`
- V23 result template: `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- V22 datapack and config profile: `../v22_worldgen_smoke_profile/`
- Decision template: `V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md`

## Required Evidence

A maintainer may record a go decision only when every required item is present:

- IOE jar filename and SHA-256.
- Branch and commit tested.
- Fresh client or dedicated-server `latest.log` path or excerpt.
- Enabled v22 datapack confirmation.
- Exact v22 smoke config values used for the run.
- Confirmation that the v22 datapack still appends only `minecraft:plains`.
- Confirmation that province runtime integration remained disabled for the smoke profile.
- Checked world, seed, biome, and coordinates.
- Observed result classification from the v23 template.
- Crash, fatal, dependency, classloading, config parse, and datapack rejection status.

## Decision Rules

Use one decision:

- `no-go`: required evidence is missing, contradictory, stale, or records a failed setup.
- `hold`: evidence exists but needs maintainer follow-up before planning more implementation.
- `go-docs-only`: evidence is accepted for documentation or release-note updates only.
- `go-runtime-planning`: evidence is accepted for planning a later implementation slice, while runtime worldgen still remains disabled by default.

`go-runtime-planning` is not permission to change active shipped resources in this v24 gate. It only says a later PR may be scoped after review.

## Hard Boundaries

- Do not copy this directory into `src/main/resources`.
- Do not modify active shipped biome tags, biome modifiers, configured features, or placed features from this gate.
- Do not change runtime config defaults from this gate.
- Do not claim live placement unless the referenced v23 result records `placement observed` with coordinate and log evidence.
- Do not treat `placement attempted`, skipped, or startup-only results as live placement proof.
