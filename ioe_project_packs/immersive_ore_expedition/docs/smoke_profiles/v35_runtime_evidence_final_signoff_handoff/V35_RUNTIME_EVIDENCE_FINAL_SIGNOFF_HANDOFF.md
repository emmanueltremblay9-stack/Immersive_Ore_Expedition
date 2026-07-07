# V35 Runtime Evidence Final Sign-Off Handoff

This final sign-off handoff is documentation-only and does not activate worldgen or smoke behavior.

## Handoff Objective

Use this handoff to transform v34 runtime evidence remediation closure results into a verifiable final review transmission packet. It does not replace the v29 traceability matrix, the v30 evidence packet, the v31 review checklist, the v32 decision record, the v33 remediation tracker, or the v34 remediation closure record.

The handoff owner must document the minimum conditions before any later review or merge recommendation, preserve the reason-code trail from v32 through v34, and prevent invented runtime evidence from entering the release chain. This file is a documentation handoff only; runtime proof must come from executed smoke artifacts captured outside this template.

This handoff does not run Minecraft, run Gradle, copy jars, launch Prism, activate runtime worldgen, change active resources, change active JSON, authorize a runtime PR, authorize merge by itself, or modify legacy split-module source trees.

## Handoff Identity

- Handoff owner:
- Closure owner v34:
- Tracker owner v33:
- Decision owner v32:
- Reviewer:
- Local date/time:
- Time zone:
- Branch evaluated:
- Commit evaluated:
- PR evaluated:
- Source closure record v34:
- Source tracker v33:
- Source decision record v32:
- Source checklist v31:
- Source packet v30:
- Source traceability matrix v29:
- Artifact bundle/source path:
- Initial handoff status:
- Handoff file created from template:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## V34 Closure Import

Import only values explicitly present in the v34 remediation closure record and linked source artifacts. Leave missing values blank or mark them as `MISSING_EVIDENCE`; do not infer values from unrelated notes.

| V34 import field | Imported value | Source row or artifact | Notes |
| --- | --- | --- | --- |
| Imported closure IDs |  |  |  |
| Associated action IDs v33 |  |  |  |
| Associated source gap IDs v32 |  |  |  |
| V34 statuses |  |  |  |
| Closure reason codes |  |  |  |
| Accepted artifacts |  |  |  |
| Rejected artifacts |  |  |  |
| Reopened actions |  |  |  |
| Deferred actions |  |  |  |
| Out-of-scope actions |  |  |  |
| Satisfied v33 criteria |  |  |  |
| V34 reviewer decisions |  |  |  |

## Handoff Statuses

Use only these statuses for v35 handoff decisions:

- `READY_FOR_FINAL_REVIEW`
- `READY_WITH_LIMITATIONS`
- `BLOCKED_BY_REOPENED_ACTIONS`
- `BLOCKED_BY_MISSING_EVIDENCE`
- `DEFERRED_TO_FOLLOWUP`
- `OUT_OF_SCOPE_FOR_RELEASE`

## V32, V33, And V34 Reason Codes

Use one or more carried-forward reason codes whenever they explain the upstream decision:

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
- `EVIDENCE_RECOLLECTED`
- `ARTIFACT_REPLACED`
- `LOG_COMPLETED`
- `COMMIT_MATCH_CONFIRMED`
- `REVIEW_ACCEPTED`
- `REVIEW_REJECTED`
- `REQUIRES_NEW_SMOKE`
- `ACTION_DUPLICATE`
- `ACTION_SUPERSEDED`

## V35 Reason Codes

Use one or more v35 reason codes for every handoff decision:

- `SIGNOFF_READY`
- `SIGNOFF_BLOCKED`
- `HANDOFF_LIMITED`
- `RELEASE_SCOPE_CONFIRMED`
- `RELEASE_SCOPE_BLOCKED`
- `FOLLOWUP_REQUIRED`
- `REVIEW_PACKET_COMPLETE`
- `REVIEW_PACKET_INCOMPLETE`
- `MERGE_REVIEW_READY`
- `MERGE_REVIEW_NOT_READY`

## Main Handoff Table

| Handoff ID | Closure ID v34 | Action ID v33 | Source gap ID v32 | Domain | V34 closure status | V34 reason code | V35 reason code | Accepted artifact | Rejected artifact | Owner | V35 handoff status | Known limitation | Release/review impact | Reviewer decision | Justification | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| V35-HANDOFF-001 |  |  |  |  |  |  |  |  |  |  | `BLOCKED_BY_MISSING_EVIDENCE` |  |  |  |  |  |

Domains may include:

- run identity
- jar/build provenance
- config gates
- active smoke biome tag
- controlled smoke datapack
- biome modifier smoke bridge
- configured feature
- placed feature
- registry/load evidence
- client log
- server log
- runtime warning or error
- world creation or load
- chunk sampling
- biome sampling
- visual proof
- v29/v30/v31/v32/v33/v34 correspondence
- final release review

Reviewer decision values:

- `ACCEPT_HANDOFF`
- `ACCEPT_WITH_LIMITATIONS`
- `REJECT_HANDOFF`
- `REOPEN_UPSTREAM_ACTION`
- `DEFER_TO_FOLLOWUP`
- `MARK_OUT_OF_SCOPE`

## Post-Closure Readiness Matrix

| Domain | v29/v30 expectation | v31 checklist | v32 decision | v33 action | v34 closure | v35 handoff | Readiness verdict | Impact on final review | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Run identity | branch, commit, PR, packet, closure, and artifacts match |  |  |  |  |  |  |  |  |
| Jar/build provenance | explicit artifact or `not executed` is recorded |  |  |  |  |  |  |  |  |
| Config gates | expected and observed gate values are verified |  |  |  |  |  |  |  |  |
| Active smoke biome tag | active tag remains empty unless later scope says otherwise |  |  |  |  |  |  |  |  |
| Controlled smoke datapack | v22 datapack uses exactly `minecraft:plains` when used |  |  |  |  |  |  |  |  |
| Biome modifier smoke bridge | modifier targets only `#immersive_ore_expedition:worldgen_smoke_test_biomes` |  |  |  |  |  |  |  |  |
| Configured feature | expected configured feature ID is verified |  |  |  |  |  |  |  |  |
| Placed feature | expected placed feature ID is verified |  |  |  |  |  |  |  |  |
| Registry/load evidence | registry or datapack load evidence is readable |  |  |  |  |  |  |  |  |
| Client/server logs | fresh logs cover the required run window when smoke was run |  |  |  |  |  |  |  |  |
| Runtime warnings/errors | warnings/errors are absent or triaged |  |  |  |  |  |  |  |  |
| World creation/load | world creation or load evidence is present when required |  |  |  |  |  |  |  |  |
| Chunk/biome sampling | coordinates, chunk state, and biome sample support the claim |  |  |  |  |  |  |  |  |
| Visual evidence | screenshots or visual notes are consistent when used |  |  |  |  |  |  |  |  |
| Chain correspondence | v29/v30/v31/v32/v33/v34 rows line up with v35 handoff rows |  |  |  |  |  |  |  |  |
| Final review packet | review packet includes closure, blockers, limitations, audit trail, and follow-up state |  |  |  |  |  |  |  |  |

Allowed readiness verdicts:

- `READY`
- `READY_WITH_LIMITATIONS`
- `BLOCKED`
- `DEFERRED`
- `OUT_OF_SCOPE`

## Remaining Blocker Ledger

| Blocker ID | Closure ID v34 | Action ID v33 | Source gap ID v32 | Blocker type | Missing or invalid proof | Affected artifact | Required action | Owner | Priority | Status | Unblock condition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| V35-BLOCKER-001 |  |  |  |  |  |  |  |  | `P1_REQUIRED` | `BLOCKED_BY_MISSING_EVIDENCE` |  |

Blocker types may include:

- `MISSING_EVIDENCE`
- `REOPENED_ACTION`
- `CONTRADICTORY_EVIDENCE`
- `OUT_OF_COMMIT_EVIDENCE`
- `ILLEGIBLE_ARTIFACT`
- `INCOMPLETE_LOG`
- `CONFIG_NOT_VERIFIABLE`
- `REVIEW_PACKET_INCOMPLETE`
- `RELEASE_SCOPE_BLOCKED`
- `FOLLOWUP_REQUIRED`

Priority values:

- `P0_BLOCKER`
- `P1_REQUIRED`
- `P2_FOLLOW_UP`
- `P3_OPTIONAL`

## Limitations, Deferred Items, And Out-Of-Scope Work

| Handoff ID | Closure ID v34 | Action ID v33 | Justification | Impact | Resume condition | Follow-up PR/issue | Blocking or non-blocking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| V35-HANDOFF-001 |  |  |  |  |  |  |  |

## Handoff Gates

### `READY_FOR_FINAL_REVIEW`

The handoff may become `READY_FOR_FINAL_REVIEW` only when:

- every applicable v34 closure row is accepted, readable, current, and tied to the evaluated branch, commit, PR, profile, jar/build provenance, and artifact bundle;
- no v33 action required by the reviewed scope remains reopened, blocked, or missing a reviewer decision;
- every required v32 gap is resolved, explicitly out of scope, or carried with a non-blocking limitation approved by the reviewer;
- the v35 main handoff table, readiness matrix, blocker ledger, limitation section, return matrix, and audit trail are complete;
- the reviewer selects `ACCEPT_HANDOFF`;
- the handoff does not claim worldgen activation, smoke success, or runtime authorization beyond the reviewed evidence scope.

### `READY_WITH_LIMITATIONS`

The handoff may become `READY_WITH_LIMITATIONS` only when:

- limitations are explicit, non-blocking for the proposed final review scope, and recorded in the limitations table;
- unresolved items have follow-up PRs, issues, owners, or resume conditions;
- the reviewer accepts the limitations and records why they do not block final review;
- no limitation is being used to hide missing runtime evidence, contradicted artifacts, or unsupported smoke claims.

### `BLOCKED_BY_REOPENED_ACTIONS`

The handoff must become `BLOCKED_BY_REOPENED_ACTIONS` when:

- any v34 closure reopens a required v33 action;
- the reviewer rejects closure for a required action;
- a required action needs another evidence packet, new smoke workflow, or corrected artifact before closure;
- the v35 return matrix shows unresolved impact on v33 or v32 status.

### `BLOCKED_BY_MISSING_EVIDENCE`

The handoff must become `BLOCKED_BY_MISSING_EVIDENCE` when:

- required closure, tracker, decision, checklist, packet, traceability, log, config, screenshot, coordinate, jar/build provenance, or artifact bundle proof is missing;
- required proof is stale, unreadable, incomplete, contradictory, off-commit, or detached from the evaluated PR;
- the review packet is incomplete enough that final review cannot evaluate readiness.

### `DEFERRED_TO_FOLLOWUP`

The handoff may become `DEFERRED_TO_FOLLOWUP` when:

- the unresolved item depends on an approved future smoke workflow, separate runtime/resource/source PR, external artifact, or owner action outside this docs-only handoff;
- the impact is recorded and remains visible;
- the resume condition and follow-up PR or issue are recorded;
- the deferral is not being used to convert unsupported runtime claims into accepted proof.

### `OUT_OF_SCOPE_FOR_RELEASE`

The handoff must become `OUT_OF_SCOPE_FOR_RELEASE` when:

- the requested sign-off would require active `src/main/resources` edits, active JSON edits, Java source edits, config default changes, legacy split-module source edits, local smoke execution, jar copying, or runtime authorization outside this docs-only handoff;
- the proof belongs to a different branch, commit, PR, profile, mod version, artifact bundle, or world;
- the requested release or merge claim is not part of the evaluated v34 closure record, v33 tracker, or v32 decision record.

## V35 To V34 To V33 To V32 Return Matrix

| Handoff ID | Closure ID v34 | Action ID v33 | Source gap ID v32 | V35 status | Impacted v34 status | Impacted v33 status | Impacted v32 status | Possible decision after handoff | Artifact | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| V35-HANDOFF-001 |  |  |  |  |  |  |  |  |  |  |

Possible decisions after handoff:

- recommend final review without changing v34, v33, or v32 status;
- recommend final review with recorded limitations;
- reopen the v34 closure row;
- reopen the v33 action;
- keep the v32 decision unchanged;
- require another evidence packet;
- require a new smoke workflow;
- defer to a follow-up PR or issue;
- mark the item out of release scope;
- block merge review until evidence is corrected.

## Audit Trail

- V35 PR link:
- V35 commit link:
- V34 PR link:
- V34 commit link:
- Artifact bundle link:
- V29 matrix link:
- V30 packet link:
- V31 checklist link:
- V32 decision record link:
- V33 remediation tracker link:
- V34 closure record link:
- V35 final sign-off handoff link:
- Chain relation: v29 -> v30 -> v31 -> v32 -> v33 -> v34 -> v35

## Handoff Update History

| Revision | Date/time | Handoff owner | Handoff IDs changed | Status change | Evidence or artifact added | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 1 |  |  |  |  |  |  |

## Triage Procedure

### V34 Closure Accepted But Handoff Limited

1. Set the v35 handoff status to `READY_WITH_LIMITATIONS` only if the limitation is non-blocking and accepted by the reviewer.
2. Record the limitation, impact, resume condition, and follow-up PR or issue.
3. Preserve the v34 closure status and explain why the handoff is limited.
4. Do not use a limitation to hide missing or contradictory runtime evidence.

### V34 Closure Accepted But Artifact Incomplete

1. Use reason code `REVIEW_PACKET_INCOMPLETE` or the carried-forward reason code that explains the gap.
2. Set the v35 handoff status to `BLOCKED_BY_MISSING_EVIDENCE` unless the artifact is explicitly non-blocking and reviewer accepted.
3. Record the missing artifact fields, unreadable path, stale timestamp, or unsupported claim.
4. Require a corrected artifact bundle from the same evaluated scope before final review readiness.

### V33 Action Reopened After Handoff

1. Set the affected handoff row to `BLOCKED_BY_REOPENED_ACTIONS`.
2. Update the return matrix with the impacted v34, v33, and v32 statuses.
3. Record the reopened action in the blocker ledger.
4. Do not recommend final review until the reopened action is closed, deferred with approval, or out of scope.

### Proof Recollected Outside The Evaluated Commit

1. Use carried-forward reason code `OUT_OF_COMMIT_EVIDENCE`.
2. Set the handoff status to `BLOCKED_BY_MISSING_EVIDENCE` or `OUT_OF_SCOPE_FOR_RELEASE` if the proof cannot apply to the reviewed decision.
3. Record the declared commit, artifact commit, branch, PR, jar/build provenance, profile, and mismatch.
4. Require fresh proof for the evaluated commit before final review readiness.

### Artifact Replaced But Not Accepted

1. Use carried-forward reason codes `ARTIFACT_REPLACED` and `REVIEW_REJECTED`.
2. Set the affected handoff row to `BLOCKED_BY_MISSING_EVIDENCE` unless reviewer-approved follow-up makes it non-blocking.
3. Record the replacement artifact, rejection reason, and required correction.
4. Do not accept transcribed claims without the readable source artifact.

### Review Packet Incomplete

1. Use v35 reason code `REVIEW_PACKET_INCOMPLETE`.
2. Record which required sections, artifacts, owners, dates, statuses, reason codes, or audit links are missing.
3. Set the handoff status to `BLOCKED_BY_MISSING_EVIDENCE`.
4. Complete the packet before selecting `READY_FOR_FINAL_REVIEW`.

### Release Scope Contested

1. Use v35 reason code `RELEASE_SCOPE_BLOCKED`.
2. Record the contested behavior, resource, source file, runtime claim, branch, commit, PR, or artifact.
3. Set the handoff status to `OUT_OF_SCOPE_FOR_RELEASE` or `DEFERRED_TO_FOLLOWUP`.
4. Require owner/reviewer agreement before using the handoff for final review.

### Action Duplicate Or Superseded

1. Use carried-forward reason code `ACTION_DUPLICATE` or `ACTION_SUPERSEDED`.
2. Record the duplicate or superseding action, PR, issue, closure row, or handoff row.
3. Set the handoff status to `READY_WITH_LIMITATIONS`, `DEFERRED_TO_FOLLOWUP`, or `OUT_OF_SCOPE_FOR_RELEASE` only when the reviewer records why that status is valid.
4. Preserve the source gap in the return matrix so later reviewers can see why the action did not independently close the v32 gap.

## Final Handoff Summary

- Handoff status:
- Handoff rows ready for final review:
- Handoff rows ready with limitations:
- Handoff rows blocked by reopened actions:
- Handoff rows blocked by missing evidence:
- Handoff rows deferred to follow-up:
- Handoff rows out of release scope:
- Accepted artifacts:
- Rejected artifacts:
- Remaining v34 closure risks:
- Remaining v33 actions:
- Remaining v32 gaps:
- Required correction, rerun, or follow-up:
- Final review recommendation:
- Merge review readiness:
- Handoff owner signature:
