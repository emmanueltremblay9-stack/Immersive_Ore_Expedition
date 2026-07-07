# V33 Runtime Evidence Remediation Tracker

This remediation tracker is documentation-only and does not activate worldgen or smoke behavior.

## Tracker Objective

Use this tracker to convert a completed v32 runtime evidence decision record into traceable follow-up actions. It does not replace the v29 traceability matrix, the v30 runtime evidence packet, the v31 review checklist, or the v32 decision record.

The tracker owner must preserve the original decision context, identify each missing or disputed proof, assign remediation work, and keep every follow-up action tied to verifiable artifacts. Runtime evidence must not be invented, inferred from unrelated artifacts, or backfilled after a run.

This tracker does not run Minecraft, run Gradle, copy jars, activate runtime worldgen, change active resources, change active JSON, or authorize a runtime PR.

## Tracking Identity

- Tracker owner:
- Decision owner:
- Reviewer:
- Tracker local date/time:
- Time zone:
- Branch evaluated:
- Commit evaluated:
- PR evaluated:
- V32 decision record source:
- V31 checklist source:
- V30 packet source:
- Artifact bundle/source path:
- Initial tracking status:
- Remediation tracker file created from template:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## V32 Decision Import

Import only values that are explicitly present in the v32 decision record. Leave missing values blank or mark them as `MISSING_EVIDENCE`; do not infer them from unrelated notes.

| V32 field | Imported value | Source row or artifact | Notes |
| --- | --- | --- | --- |
| Final v32 status |  |  |  |
| V32 reason codes |  |  |  |
| Open gaps |  |  |  |
| Blocking gaps |  |  |  |
| Non-blocking gaps |  |  |  |
| Actions already proposed |  |  |  |
| Affected artifacts |  |  |  |
| Accepted evidence |  |  |  |
| Rejected evidence |  |  |  |
| Missing evidence |  |  |  |
| Out-of-scope evidence |  |  |  |

## Follow-Up Statuses

Use only these statuses for remediation actions:

- `OPEN`
- `IN_PROGRESS`
- `BLOCKED`
- `READY_FOR_REVIEW`
- `CLOSED`
- `WONT_FIX`
- `OUT_OF_SCOPE`

## V32 Reason Codes

Use the v32 reason code that best explains why each action exists:

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

## Remediation Action Table

| Action ID | Source gap ID | Domain | Reason code | Action description | Required proof | Expected artifact | Owner | Priority | Blocking yes/no | Status | Acceptance criteria | Planned verification method | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| V33-ACT-001 |  |  |  |  |  |  |  |  |  | `OPEN` |  |  |  |

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
- v29/v30/v31/v32 correspondence

Priority values:

- `P0_BLOCKER`
- `P1_REQUIRED`
- `P2_FOLLOW_UP`
- `P3_OPTIONAL`

## Evidence Recollection Plan

Plan recollection before any future smoke workflow runs. Use `not executed` for jar/build or runtime fields when the action is documentation-only and no approved run has occurred.

| Evidence to recollect | Expected environment | Target client/server/world profile | Expected logs | Optional screenshots | Chunk/biome sampling, if applicable | Jar/build provenance or `not executed` | Planned capture method | Owner | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  | `not executed` |  |  |  |

## Closure Gates

### `READY_FOR_REVIEW`

An action may move to `READY_FOR_REVIEW` only when:

- required proof is attached or the action is explicitly marked `OUT_OF_SCOPE` with a reason;
- artifact paths or URLs are readable by the reviewer;
- branch, commit, PR, profile, jar/build provenance, and artifact bundle identity are recorded;
- any recollected runtime evidence comes from an approved future smoke workflow;
- the planned verification method has been executed or is clearly documented as blocked.

### `CLOSED`

An action may move to `CLOSED` only when:

- reviewer verification confirms the acceptance criteria;
- the new proof resolves the source gap for the tracked scope;
- the v33 to v32 return matrix records how the action affects the v32 decision;
- no required artifact is missing, stale, unreadable, contradictory, or tied to the wrong commit;
- closure does not claim worldgen activation, smoke success, or runtime authorization beyond the reviewed evidence scope.

### `BLOCKED`

An action must remain `BLOCKED` when:

- required evidence cannot be collected without an approved future smoke run;
- required artifacts are unavailable, unreadable, or outside the evaluated commit;
- the evaluated branch, commit, PR, jar/build provenance, or profile cannot be verified;
- logs, config, screenshots, coordinates, or registry/load evidence are incomplete or contradictory;
- the action depends on a separate runtime/resource/source PR that has not been authorized.

### `WONT_FIX`

An action may become `WONT_FIX` only when:

- the decision owner records why the source gap will not be remediated;
- the unresolved risk is still visible in the tracker;
- the v32 decision impact is recorded;
- the action is not being used to hide missing evidence or convert unsupported smoke claims into accepted proof.

### `OUT_OF_SCOPE`

An action must become `OUT_OF_SCOPE` when:

- the action would require active `src/main/resources` edits, active JSON edits, Java source edits, config default changes, legacy split-module source edits, or runtime authorization outside this docs-only tracker;
- the proof belongs to a different branch, commit, PR, profile, mod version, artifact bundle, or world;
- the requested behavior is not part of the evaluated v32 decision record.

## V33 To V32 Return Matrix

| Action ID | Source gap ID v32 | Impacted v32 status | New proof expected | Artifact | Possible decision after review | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| V33-ACT-001 |  |  |  |  |  |  |

Possible decisions after review:

- keep v32 decision unchanged
- update v32 status after reviewer acceptance
- require another evidence packet
- reject the evidence
- mark the action out of scope
- carry the gap forward as blocked

## Audit Trail

- PR link:
- Commit link:
- Artifact bundle link:
- V29 matrix link:
- V30 packet link:
- V31 checklist link:
- V32 decision record link:
- V33 remediation tracker link:
- Chain relation: v29 -> v30 -> v31 -> v32 -> v33

## Tracker Update History

| Revision | Date/time | Tracker owner | Action IDs changed | Status change | Evidence or artifact added | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 1 |  |  |  |  |  |  |

## Triage Procedure

### Evidence Still Missing

1. Keep the action `OPEN` or `BLOCKED`.
2. Record the exact missing artifact, log window, config file, screenshot, coordinate, command output, or packet field.
3. Do not infer the proof from older branches, unrelated PRs, screenshots without provenance, or incomplete notes.
4. Keep the v32 impact as unresolved until a reviewer accepts new proof.

### Evidence Recollected But Contradictory

1. Set the action to `BLOCKED` unless the contradiction is resolved by a corrected artifact bundle from the same run.
2. Record each conflicting field, timestamp, commit, branch, PR, config value, log, screenshot, coordinate, or matrix row.
3. Do not choose the more convenient artifact without explaining why the other artifact is invalid.
4. Return the action to review only after the contradiction is resolved or explicitly rejected.

### Evidence Recollected Outside The Evaluated Commit

1. Use reason code `OUT_OF_COMMIT_EVIDENCE`.
2. Record the declared commit, artifact commit, branch, PR, jar/build provenance, and profile mismatch.
3. Keep the action `BLOCKED` or move it to `OUT_OF_SCOPE` if the evidence cannot apply to the reviewed decision.
4. Require fresh proof for the evaluated commit before closure.

### Illegible Artifact

1. Use reason code `ILLEGIBLE_ARTIFACT`.
2. Record the artifact path or URL and the readability failure.
3. Keep the action `BLOCKED` until a readable replacement from the same run is available.
4. Do not accept transcribed claims without the readable source artifact.

### Incomplete Log

1. Use reason code `INCOMPLETE_LOG`.
2. Record the missing startup, datapack, config, world-load, registry, coordinate, or runtime diagnostic window.
3. Do not accept placement, skip, or no-error claims that rely on the missing log window.
4. Require a fresh complete log or an approved rerun before closure.

### Config Not Verifiable

1. Use reason code `CONFIG_NOT_VERIFIABLE`.
2. Record which active default or controlled smoke config gate cannot be verified.
3. Keep the action `BLOCKED` unless config proof is explicitly out of scope.
4. Require the actual config file, log excerpt, command output, or artifact that proves the value.

### Action Out Of Scope

1. Move the action to `OUT_OF_SCOPE`.
2. Record the exact branch, commit, PR, behavior, resource, source file, runtime claim, or artifact that falls outside the v32 decision scope.
3. Preserve the gap in the v33 to v32 return matrix so later reviewers can see why it did not close the source decision gap.

## Final Tracker Summary

- Tracker status:
- Actions closed:
- Actions ready for review:
- Actions blocked:
- Actions wont fix:
- Actions out of scope:
- New proof accepted:
- New proof rejected:
- Remaining v32 gaps:
- Required correction, rerun, or follow-up:
- Tracker owner signature:
