# V25 Worldgen Runtime Promotion Readiness

## Packet Identity

- PR/task:
- Author:
- Review date/time:
- Branch/commit reviewed:
- V23 smoke result file:
- V24 maintainer decision file:

## Evidence Chain

| Input | Required | Present | Notes |
| --- | --- | --- | --- |
| Completed v23 smoke result from an actual run | yes | | |
| V24 maintainer decision | yes | | |
| V24 decision is compatible with runtime planning | yes | | |
| Runtime still disabled by default | yes | | |
| Active shipped resources changed by this packet | no | | |
| Future PR required for runtime/resource changes | yes | | |

## Runtime Safety Preconditions

| Check | Expected | Observed |
| --- | --- | --- |
| Active shipped smoke tag remains empty before future runtime PR | yes | |
| Active biome modifier still targets only the IOE smoke tag | yes | |
| Runtime proof feature default remains `false` | yes | |
| Runtime placement default remains `false` | yes | |
| Province runtime integration remains default `false` | yes | |
| No broad biome tags are used by default | yes | |

## Minimum Future Runtime Slice

- Proposed runtime objective:
- Files expected to change:
- Files explicitly not expected to change:
- Active resource/data changes expected:
- Config default changes expected:
- Manual smoke evidence that must be regenerated after the change:

## Rollback Or Disable Expectation

- Default-off gate that keeps the change inert:
- Config key or resource removal that disables the change:
- Expected behavior after rollback/disable:

## Validation Commands Required For Future Runtime PR

- Static diff/scope checks:
- JSON/content checks:
- Targeted Gradle/test command, if permitted by project rules:
- GitHub Actions checks:
- Manual smoke requirement:

## Maintainer Disposition

Select one:

- DOCS_ONLY_HOLD
- REPEAT_SMOKE
- BLOCKED
- READY_FOR_RUNTIME_SLICE

## Disposition Basis

- Accepted evidence:
- Missing evidence:
- Reason smoke must be repeated, if applicable:
- Blocking issue, if applicable:
- Runtime slice summary, if ready:

## Boundaries

- `READY_FOR_RUNTIME_SLICE` activates runtime worldgen: no
- Active `src/main/resources` may be changed by this packet: no
- Future implementation must be a separate PR: yes
- Future runtime/resource change needs fresh validation: yes
