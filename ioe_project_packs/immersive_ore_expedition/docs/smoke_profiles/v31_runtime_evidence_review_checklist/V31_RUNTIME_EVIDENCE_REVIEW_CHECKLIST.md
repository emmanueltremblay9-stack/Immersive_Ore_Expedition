# V31 Runtime Evidence Review Checklist

This checklist is documentation-only and does not activate worldgen or smoke behavior.

## Review Objective

Use this checklist to review a filled v30 runtime evidence packet after a future real smoke run. The reviewer must verify that proofs are not invented and must classify each proof as absent, incomplete, contradictory, or sufficient before making a final decision.

The checklist does not run Minecraft, run Gradle, copy jars, activate runtime worldgen, change active resources, change active JSON, or authorize a runtime PR.

## Review Identity

- Reviewer:
- Review local date/time:
- Time zone:
- PR evaluated:
- Branch evaluated:
- Commit evaluated:
- Expected chain base:
- V30 evidence packet source:
- Artifact bundle/source path:
- Initial packet status:
- Review checklist file created from template:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Evidence Classification

Use these classifications before selecting a final decision:

- `ABSENT`: required evidence is missing.
- `INCOMPLETE`: evidence exists but does not cover the required field, time window, config, coordinate, artifact, or trace row.
- `CONTRADICTORY`: evidence conflicts with another field, artifact, commit, branch, PR, config value, log, screenshot, coordinate, or v29/v30 row.
- `SUFFICIENT`: evidence is complete, current, readable, and tied to the evaluated commit, branch, PR, jar/build provenance, profile, and artifact bundle.

## Integrity Checklist

| Check | Expected | Observed | Classification | Reviewer status | Notes |
| --- | --- | --- | --- | --- | --- |
| Commit tested matches the declared commit | exact match |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Branch matches the expected chain | expected v30-or-later smoke branch |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| PR matches the expected chain | PR is the reviewed smoke/evidence scope |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Evidence packet source is identified | filled v30 packet path or URL |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Artifact bundle/source path is identified | logs/screenshots/config/jar provenance bundle |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Jar/build provenance is explicit or marked `not executed` | explicit artifact or `not executed` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Mandatory runtime fields are not ambiguous | blank/ambiguous fields triaged |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Timestamps are coherent | run/review/log/screenshot times align |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Logs are coherent | fresh log belongs to reviewed run |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Screenshots or visual notes are coherent when present | image/note matches run and coordinates |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| No evidence appears invented or backfilled | no unsupported claims |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |

## Configuration Checklist

Review config gates from the v30 packet and artifacts. Use `INCONCLUSIVE` when a value is not directly verifiable from the packet or artifact bundle.

| Config gate | Expected value | Observed value | Source of proof | Reviewer status | Reviewer notes |
| --- | --- | --- | --- | --- | --- |
| Active default `worldgen.runtimeProofFeatureEnabled` | `false` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active default `worldgen.runtimeProofFeatureDiagnostics` | `false` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active default `worldgen.runtimePlacementEnabled` | `false` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active default `worldgen.runtimePlacementDiagnostics` | `false` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active default `worldgen.provinces.runtimeIntegrationEnabled` | `false` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active default `worldgen.provinces.resourcePolicyRules` | `[]` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.runtimeProofFeatureEnabled` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.runtimeProofFeatureDiagnostics` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.runtimePlacementEnabled` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.runtimePlacementDiagnostics` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.provinces.runtimeIntegrationEnabled` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke `worldgen.provinces.resourcePolicyRules` | expected value from v30 packet |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |

## Datapack And Worldgen Checklist

| Surface | Expected value or state | Observed value | Source of proof | Reviewer status | Reviewer notes |
| --- | --- | --- | --- | --- | --- |
| Active smoke biome tag | `values` is `[]` unless separately scoped later |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Controlled smoke datapack biome tag | exactly `minecraft:plains` for the v22 profile |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active biome modifier type | `neoforge:add_features` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active biome modifier biome target | `#immersive_ore_expedition:worldgen_smoke_test_biomes` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active biome modifier feature target | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Active biome modifier step | `surface_structures` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Configured feature IDs | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Placed feature IDs | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Registry/load evidence | relevant registry or datapack load proof is readable |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Smoke scope non-regression | no broad biome tags, modded biomes, or multiple test biomes unless separately scoped |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |

## Runtime Checklist

| Runtime evidence | Expected | Observed | Artifact reference | Reviewer status | Reviewer notes |
| --- | --- | --- | --- | --- | --- |
| Client log, if client smoke was run | fresh client `latest.log` for evaluated commit/profile |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Server log, if server smoke was run | fresh dedicated-server log for evaluated commit/profile |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Warnings/errors observed | absent or triaged with impact |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Crash/fatal/missing dependency/classloading/config parse errors | absent, or packet is rejected |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| World creation or load | world created or loaded without smoke-blocking errors |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Chunk sampling | coordinates and chunk status recorded when needed |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Biome sampling | sampled biome supports the claimed smoke scope |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Placement outcome | skipped, attempted, observed, or not observed with evidence basis |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Optional visual proof | screenshot or visual note is readable and consistent when present |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |
| Artifact references | every claimed artifact is present and readable |  |  | `PASS` / `FAIL` / `INCONCLUSIVE` |  |

## V29/V30 Correspondence Checklist

| V29 line | V30 field/proof | Reviewer verdict | Artifact | Notes |
| --- | --- | --- | --- | --- |
| RTM-001 active smoke biome tag | v30 active tag and controlled datapack proof |  |  |  |
| RTM-002 active biome modifier smoke bridge | v30 biome modifier proof |  |  |  |
| RTM-003 configured feature declaration | v30 configured feature proof |  |  |  |
| RTM-004 placed feature declaration | v30 placed feature proof |  |  |  |
| RTM-005 runtime placement gate source | v30 configuration proof |  |  |  |
| RTM-006 runtime proof feature gate source | v30 configuration proof |  |  |  |
| RTM-007 runtime feature invocation surface | v30 runtime log/outcome proof |  |  |  |
| RTM-008 runtime placement proof behavior | v30 coordinate/log/resource-policy proof |  |  |  |
| RTM-009 resource pack metadata | v30 unchanged-resource proof |  |  |  |
| RTM-010 NeoForge mod metadata | v30 unchanged-metadata proof |  |  |  |
| RTM-000 additional reviewed path | v30 packet row for the added path |  |  |  |

Allowed reviewer verdicts:

- `ABSENT`
- `INCOMPLETE`
- `CONTRADICTORY`
- `SUFFICIENT`
- `OUT_OF_SCOPE`

## Decision Criteria

Select exactly one final decision:

- `ACCEPTED`
- `REJECTED`
- `NEEDS_MORE_EVIDENCE`
- `OUT_OF_SCOPE`

Use `ACCEPTED` only when every required integrity, configuration, datapack/worldgen, runtime, and v29/v30 correspondence item is `PASS` or explicitly out of scope with a stated reason.

Use `REJECTED` when evidence is invented, contradictory, tied to the wrong commit, tied to the wrong PR, falsified, or proves a blocking smoke failure.

Use `NEEDS_MORE_EVIDENCE` when required proof is absent, incomplete, stale, unreadable, indirect, or not tied to the reviewed commit/profile.

Use `OUT_OF_SCOPE` when the packet claims behavior, artifacts, branches, resources, worlds, or runtime changes outside the reviewed v30 evidence scope.

## Triage Procedure

### Missing Evidence

1. Mark the affected row `ABSENT`.
2. Set final decision to `NEEDS_MORE_EVIDENCE` unless the missing proof is explicitly out of scope.
3. Name the exact missing artifact, log window, config file, screenshot, coordinate, command output, or packet field.
4. Do not infer the missing proof from unrelated artifacts.

### Contradictory Evidence

1. Mark the affected row `CONTRADICTORY`.
2. Set final decision to `REJECTED` unless a corrected packet and artifact bundle is supplied for the same run.
3. Record the conflicting fields or artifacts.
4. Do not choose the more convenient artifact without explaining why the other one is invalid.

### Off-Commit Evidence

1. Mark the affected row `CONTRADICTORY`.
2. Set final decision to `REJECTED` or `NEEDS_MORE_EVIDENCE`.
3. Record the declared commit, artifact commit, branch, PR, and jar/build provenance mismatch.
4. Require fresh evidence for the evaluated commit before acceptance.

### Unreadable Artifact

1. Mark the affected row `INCOMPLETE`.
2. Set final decision to `NEEDS_MORE_EVIDENCE`.
3. Record the artifact path or URL and the read failure.
4. Request a readable replacement artifact from the same run.

### Incomplete Log

1. Mark the affected runtime row `INCOMPLETE`.
2. Record the missing time window or missing startup/world-load/datapack/config section.
3. Do not accept placement or skip claims that rely on the missing log window.
4. Require a fresh complete log or rerun within an approved future smoke workflow.

### Non-Verifiable Config

1. Mark the affected config row `INCOMPLETE`.
2. Set final decision to `NEEDS_MORE_EVIDENCE` unless config was explicitly out of scope.
3. Record which gate could not be verified.
4. Require the actual config file, log excerpt, or command output that proves the value.

## Final Review Decision

- Decision:
- Decision basis:
- Accepted evidence:
- Rejected evidence:
- Missing evidence:
- Out-of-scope evidence:
- Required correction or rerun:
- Reviewer signature:
