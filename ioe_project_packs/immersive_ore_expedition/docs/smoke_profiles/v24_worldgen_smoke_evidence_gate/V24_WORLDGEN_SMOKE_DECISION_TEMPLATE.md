# V24 Worldgen Smoke Decision

## Review Identity

- PR/task:
- Reviewer:
- Review date/time:
- Branch/commit reviewed:
- V23 result file reviewed:

## Evidence Inventory

| Evidence item | Required | Present | Notes |
| --- | --- | --- | --- |
| IOE jar filename and SHA-256 | yes | | |
| Branch and commit tested | yes | | |
| Fresh `latest.log` path or excerpt | yes | | |
| V22 datapack enabled confirmation | yes | | |
| Exact smoke config values | yes | | |
| V22 datapack appends only `minecraft:plains` | yes | | |
| Province runtime integration disabled | yes | | |
| World, seed, biome, and coordinates | yes | | |
| Observed result classification | yes | | |
| Crash/fatal/dependency/classloading/config/datapack status | yes | | |

## Runtime Safety Review

| Check | Expected | Observed |
| --- | --- | --- |
| Active shipped smoke tag remains empty | yes | |
| Active biome modifier still targets only the IOE smoke tag | yes | |
| Runtime proof feature default remains `false` | yes | |
| Runtime placement default remains `false` | yes | |
| Active `src/main/resources` changes required for this decision | no | |

## Decision

Select one:

- no-go
- hold
- go-docs-only
- go-runtime-planning

## Decision Basis

- Accepted evidence:
- Missing or contradictory evidence:
- Follow-up required:

## Boundaries For Next Slice

- Runtime worldgen still disabled by default: yes / no
- Active shipped resources may be changed by this decision: no
- Manual smoke must be rerun after any future runtime/resource change: yes / no
