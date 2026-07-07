# V32 Runtime Evidence Decision Record

This decision record is documentation-only and does not activate worldgen or smoke behavior.

## Decision Objective

Use this record to transform a completed v31 runtime evidence review into a traceable final decision. It does not replace the v29 traceability matrix, the v30 runtime evidence packet, or the v31 review checklist.

The decision owner must document the reasons for acceptance, rejection, missing evidence, or out-of-scope status, and must prevent invented runtime evidence from entering the decision trail.

This record does not run Minecraft, run Gradle, copy jars, activate runtime worldgen, change active resources, change active JSON, or authorize a runtime PR.

## Decision Identity

- Decision owner:
- Reviewer:
- Decision local date/time:
- Time zone:
- Branch evaluated:
- Commit evaluated:
- PR evaluated:
- V30 packet source:
- V31 checklist source:
- Artifact bundle/source path:
- Initial final status:
- Decision record file created from template:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Input Inventory

| Input | Required | Source/path/link | Available | Notes |
| --- | --- | --- | --- | --- |
| V29 matrix used | yes |  |  |  |
| Filled V30 evidence packet | yes |  |  |  |
| Filled V31 review checklist | yes |  |  |  |
| Client logs, if available | conditional |  |  |  |
| Server logs, if available | conditional |  |  |  |
| Screenshots, if available | optional |  |  |  |
| CI/build provenance, if available | conditional |  |  |  |
| Jar/build provenance or `not executed` field | yes |  |  |  |
| Artifact bundle/source path | yes |  |  |  |

## Decision Taxonomy

Select exactly one final decision:

- `ACCEPTED`
- `REJECTED`
- `NEEDS_MORE_EVIDENCE`
- `OUT_OF_SCOPE`

`ACCEPTED` means the referenced v30 packet and v31 review are sufficient for the exact reviewed evidence scope. It does not activate runtime worldgen and does not authorize active resource or source changes by itself.

## Required Reason Codes

Use one or more reason codes for every decision:

- `CONFIG_CONFIRMED`
- `WORLDGEN_CONFIRMED`
- `RUNTIME_CONFIRMED`
- `MISSING_EVIDENCE`
- `CONTRADICTORY_EVIDENCE`
- `OUT_OF_COMMIT_EVIDENCE`
- `ILLEGIBLE_ARTIFACT`
- `INCOMPLETE_LOG`
- `CONFIG_NOT_VERIFIABLE`
- `SCOPE_MISMATCH`

## Evidence Sufficiency Matrix

| Domain | v29/v30 expectation | v31 review result | Artifact | Verdict | Decision notes |
| --- | --- | --- | --- | --- | --- |
| Run identity | branch, commit, PR, packet, and artifacts match |  |  |  |  |
| Jar/build provenance | explicit artifact or `not executed` is recorded |  |  |  |  |
| Config gates | expected and observed gate values are verified |  |  |  |  |
| Active smoke biome tag | active tag remains empty unless later scope says otherwise |  |  |  |  |
| Controlled smoke datapack | v22 datapack uses exactly `minecraft:plains` when used |  |  |  |  |
| Biome modifier smoke bridge | modifier targets only `#immersive_ore_expedition:worldgen_smoke_test_biomes` |  |  |  |  |
| Configured feature | expected configured feature ID is verified |  |  |  |  |
| Placed feature | expected placed feature ID is verified |  |  |  |  |
| Registry/load evidence | registry or datapack load evidence is readable |  |  |  |  |
| Client/server logs | fresh logs cover the required run window when smoke was run |  |  |  |  |
| Runtime warnings/errors | warnings/errors are absent or triaged |  |  |  |  |
| World creation/load | world creation or load evidence is present when required |  |  |  |  |
| Chunk/biome sampling | coordinates, chunk state, and biome sample support the claim |  |  |  |  |
| Visual evidence | screenshots or visual notes are consistent when used |  |  |  |  |
| V29/V30 correspondence | every required trace row has v30 proof and v31 verdict |  |  |  |  |

Allowed verdicts:

- `SUFFICIENT`
- `MISSING`
- `INCOMPLETE`
- `CONTRADICTORY`
- `OUT_OF_SCOPE`

## Gap Ledger

| Gap ID | Gap type | Affected evidence | Impact | Severity | Required action | Owner | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GAP-001 |  |  |  |  |  |  |  |

Gap types may include:

- `MISSING_EVIDENCE`
- `CONTRADICTORY_EVIDENCE`
- `OUT_OF_COMMIT_EVIDENCE`
- `ILLEGIBLE_ARTIFACT`
- `INCOMPLETE_LOG`
- `CONFIG_NOT_VERIFIABLE`
- `SCOPE_MISMATCH`

Severity values:

- `BLOCKING`
- `NON_BLOCKING`
- `INFO`

Status values:

- `OPEN`
- `RESOLVED`
- `WAIVED_OUT_OF_SCOPE`
- `REQUIRES_RERUN`

## Remediation And Next Action

| Item | Required before acceptance | Evidence to recollect | Artifact to provide | Follow-up PR/issue | Blocking | Owner | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| REM-001 |  |  |  |  |  |  |  |

Use this section to record:

- actions necessary before acceptance;
- evidence that must be recollected;
- artifacts that must be provided;
- follow-up PRs or issues;
- whether each item is blocking or non-blocking.

## Audit Trail

- PR link:
- Commit link:
- Artifact bundle link:
- V29 matrix link:
- V30 packet link:
- V31 checklist link:
- V32 decision record link:
- Chain relation: v29 -> v30 -> v31 -> v32

## Decision History

| Revision | Date/time | Decision owner | Decision | Reason codes | Change summary |
| --- | --- | --- | --- | --- | --- |
| 1 |  |  |  |  |  |

## Closure Criteria

The record is complete when:

- decision identity is filled;
- all required inputs are listed;
- the decision taxonomy value is selected exactly once;
- reason codes are recorded;
- the evidence sufficiency matrix covers the applicable v29/v30/v31 domains;
- gaps and remediation items are either resolved, explicitly out of scope, or carried as blockers;
- audit trail links are present.

The record must remain `NEEDS_MORE_EVIDENCE` when:

- required evidence is missing, incomplete, stale, unreadable, indirect, or not tied to the reviewed commit/profile;
- jar/build provenance is absent and not explicitly marked `not executed`;
- logs, config, artifact bundle, screenshots, coordinates, or registry/load evidence cannot be verified.

The record must be `REJECTED` when:

- evidence is invented, falsified, contradictory, or tied to the wrong commit or PR;
- the reviewed evidence proves a blocking crash, fatal error, config parse failure, datapack rejection, missing dependency, or classloading failure;
- smoke claims are unsupported by artifacts.

The record must be `OUT_OF_SCOPE` when:

- the packet claims behavior, branches, commits, worlds, artifacts, runtime changes, or resource changes outside the reviewed evidence scope;
- the decision would require active resource/source changes not covered by a separate authorized PR.

## Final Decision

- Decision:
- Reason codes:
- Decision basis:
- Accepted evidence:
- Rejected evidence:
- Missing evidence:
- Out-of-scope evidence:
- Blocking gaps:
- Non-blocking gaps:
- Required correction, rerun, or follow-up:
- Decision owner signature:
