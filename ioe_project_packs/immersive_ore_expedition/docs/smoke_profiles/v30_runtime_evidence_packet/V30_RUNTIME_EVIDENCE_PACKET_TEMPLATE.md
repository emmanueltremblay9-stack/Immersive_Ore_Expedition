# V30 Runtime Evidence Packet

This template is documentation-only and does not activate worldgen or smoke behavior.

## Packet Identity

- PR/task:
- Packet author:
- Packet date/time:
- Local date/time of run:
- Time zone:
- Branch:
- Commit tested:
- PR:
- V29 matrix source:
- Evidence packet file created from template:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Run Identifiers

Use `not executed` for fields that are intentionally absent during docs-only packet preparation. A `not executed` artifact or run field makes runtime proof `INCONCLUSIVE` until a real smoke run supplies evidence.

| Field | Value | Evidence source | Notes |
| --- | --- | --- | --- |
| Local date/time |  |  |  |
| Commit tested |  |  |  |
| Branch |  |  |  |
| PR |  |  |  |
| Jar/build provenance | not executed |  | Record jar filename, SHA-256, workflow run, or `not executed`. |
| Minecraft version |  |  |  |
| NeoForge version |  |  |  |
| Java version |  |  |  |
| Client/server/world profile target |  |  |  |
| World or server name |  |  |  |
| World seed |  |  |  |
| Operator |  |  |  |

## Documentation-Only Boundary

| Check | Required | Observed | Notes |
| --- | --- | --- | --- |
| v30 docs-only packet | yes |  |  |
| no local Gradle build executed for this packet | yes |  |  |
| no Minecraft or Prism launch executed for this packet | yes |  |  |
| no jar copied for this packet | yes |  |  |
| no active `src/main/resources` changed | yes |  |  |
| no active JSON changed | yes |  |  |
| no legacy split-module source tree changed | yes |  |  |
| no smoke evidence invented | yes |  |  |
| v30 does not activate worldgen or smoke behavior | yes |  |  |

## Configuration Proof

Record both active defaults and the disposable controlled-smoke profile values used for an actual run. The active defaults are expected to stay disabled unless a later, separate approved runtime PR changes that policy.

| Config gate | Expected value | Observed value | Source of proof | Artifact | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| Active default `worldgen.runtimeProofFeatureEnabled` | `false` |  |  |  |  |  |
| Active default `worldgen.runtimeProofFeatureDiagnostics` | `false` |  |  |  |  |  |
| Active default `worldgen.runtimePlacementEnabled` | `false` |  |  |  |  |  |
| Active default `worldgen.runtimePlacementDiagnostics` | `false` |  |  |  |  |  |
| Active default `worldgen.provinces.runtimeIntegrationEnabled` | `false` |  |  |  |  |  |
| Active default `worldgen.provinces.resourcePolicyRules` | `[]` |  |  |  |  |  |
| Controlled smoke `worldgen.runtimeProofFeatureEnabled` | `true` for an actual v22/v23 smoke run |  |  |  |  |  |
| Controlled smoke `worldgen.runtimeProofFeatureDiagnostics` | `true` for an actual v22/v23 smoke run |  |  |  |  |  |
| Controlled smoke `worldgen.runtimePlacementEnabled` | `true` for an actual v22/v23 smoke run |  |  |  |  |  |
| Controlled smoke `worldgen.runtimePlacementDiagnostics` | `true` for an actual v22/v23 smoke run |  |  |  |  |  |
| Controlled smoke `worldgen.provinces.runtimeIntegrationEnabled` | `false` for the controlled profile |  |  |  |  |  |
| Controlled smoke `worldgen.provinces.resourcePolicyRules` | `[]` for the controlled profile |  |  |  |  |  |

## Datapack And Worldgen Proof

Record active shipped resources separately from the external controlled smoke datapack. Do not edit active shipped resources to fill this packet.

| Surface | Expected value or state | Observed value | Source of proof | Artifact | Capture method | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Active smoke biome tag | `values` is `[]` |  | `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` |  | file read or CI artifact |  |  |
| V22 external smoke datapack biome tag | exactly `minecraft:plains` |  | `docs/smoke_profiles/v22_worldgen_smoke_profile/datapack/` |  | file read plus enabled datapack confirmation |  |  |
| Active biome modifier type | `neoforge:add_features` |  | active biome modifier JSON |  | file read or registry load evidence |  |  |
| Active biome modifier biome target | `#immersive_ore_expedition:worldgen_smoke_test_biomes` |  | active biome modifier JSON |  | file read or registry load evidence |  |  |
| Active biome modifier feature target | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  | active biome modifier JSON |  | file read or registry load evidence |  |  |
| Active biome modifier step | `surface_structures` |  | active biome modifier JSON |  | file read or registry load evidence |  |  |
| Configured feature ID | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  | configured feature JSON |  | file read or registry load evidence |  |  |
| Placed feature ID | `immersive_ore_expedition:tiny_vertical_mine_entrance` |  | placed feature JSON |  | file read or registry load evidence |  |  |
| Registry/load evidence | controlled profile loads datapack and IOE worldgen data without rejection |  | fresh `latest.log` or server log |  | log excerpt |  |  |
| Planned capture method | fresh log, datapack list, config file copy, coordinate/biome check, screenshot or operator note |  | runbook notes |  | operator procedure |  |  |

## Runtime Proof

Runtime proof is valid only when captured from an actual client or server smoke run. Leave rows blank or mark `not executed` during docs-only preparation.

| Evidence item | Expected evidence | Observed evidence | Artifact reference | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| Fresh log | Fresh client `latest.log` or dedicated-server log for this run |  |  |  |  |
| Crash/fatal signatures | Absent, or present and triaged as `FAIL` |  |  |  |  |
| Missing dependency/classloading/config parse errors | Absent, or present and triaged as `FAIL` |  |  |  |  |
| Datapack load warnings | Absent, or present and triaged with impact |  |  |  |  |
| IOE runtime diagnostics | Present when diagnostics are enabled, or absence explained |  |  |  |  |
| World creation/load evidence | World created or loaded without crash/fatal/datapack/config blockers |  |  |  |  |
| Chunk sampling evidence | Coordinates and chunk status recorded |  |  |  |  |
| Biome sampling evidence | Checked biome is the controlled target biome when required |  |  |  |  |
| Placement outcome | skipped, attempted, observed, or not observed with evidence basis |  |  |  |  |
| Screenshot artifact | Screenshot path or `not captured` with reason |  |  |  |  |
| Operator note | Notes tied to coordinates/log window |  |  |  |  |

## V29 Matrix Correspondence

Use this table to connect each v29 traceability row to concrete v30 evidence. Row status may be `PASS`, `FAIL`, `INCONCLUSIVE`, or `NOT_RUN`; `NOT_RUN` rolls up to `INCONCLUSIVE` for runtime proof.

| V29 trace ID | V29 row | Expected v30 proof | Status | Artifact | Notes |
| --- | --- | --- | --- | --- | --- |
| RTM-001 | Active smoke biome tag | Active tag observed empty before run; controlled datapack tag observed exactly `minecraft:plains` when used |  |  |  |
| RTM-002 | Active biome modifier smoke bridge | Modifier targets only the IOE smoke tag, expected placed feature, and `surface_structures` step |  |  |  |
| RTM-003 | Configured feature declaration | Configured feature loads as `immersive_ore_expedition:tiny_vertical_mine_entrance` with expected config |  |  |  |
| RTM-004 | Placed feature declaration | Placed feature references the expected configured feature and placement list |  |  |  |
| RTM-005 | Runtime placement gate source | Active defaults observed disabled; controlled smoke config enables placement gate only for disposable run |  |  |  |
| RTM-006 | Runtime proof feature gate source | Active defaults observed disabled; controlled smoke config enables proof feature gate only for disposable run |  |  |  |
| RTM-007 | Runtime feature invocation surface | Log evidence shows skip, attempt, or placement outcome when invoked |  |  |  |
| RTM-008 | Runtime placement proof behavior | Log/coordinate evidence ties placement proof behavior to resource-policy and writable-region checks |  |  |  |
| RTM-009 | Resource pack metadata | Confirmed unchanged unless separate metadata scope exists |  |  |  |
| RTM-010 | NeoForge mod metadata | Confirmed unchanged unless separate metadata scope exists |  |  |  |
| RTM-000 | Additional reviewed future path | Add only when v29 matrix has a reviewed row for the path |  |  |  |

## PASS Criteria

Use `PASS` only when all applicable conditions are supported by artifacts from the same run:

- run identifiers are complete and point to the exact branch, commit, PR, jar/build provenance, and profile tested;
- active defaults and controlled smoke config values match expectations;
- active shipped resources are observed unchanged before the run;
- the controlled datapack, if used, binds exactly `minecraft:plains`;
- fresh logs show no untriaged crash, fatal, missing dependency, classloading, config parse, or datapack rejection blockers;
- world creation or load evidence is captured;
- coordinate and biome sampling evidence is captured when placement or skip behavior is evaluated;
- every required v29 trace row has a matching v30 artifact and `PASS` status;
- screenshots or operator notes are linked when they are part of the run evidence.

## FAIL Criteria

Use `FAIL` when any blocker is proven by evidence:

- branch, commit, PR, jar, profile, or config identity does not match the intended run;
- active shipped resources or config defaults were changed unexpectedly;
- controlled datapack uses a broad biome tag, modded biome, or more than one test biome;
- required runtime gates are disabled for a run that was intended to invoke the feature;
- crash, fatal, missing dependency, classloading, config parse, or datapack rejection errors block the smoke goal;
- registry/load evidence shows the configured feature, placed feature, biome modifier, or datapack failed to load;
- chunk/biome sampling contradicts the claimed observation;
- evidence is falsified, reused across incompatible commits, or backfilled after the run.

## INCONCLUSIVE Criteria

Use `INCONCLUSIVE` when evidence is missing, stale, indirect, or too weak:

- Minecraft, Prism, server, or smoke was not executed;
- jar/build provenance is `not executed`;
- logs are absent, stale, or not tied to the tested commit/profile;
- config values were not captured from the actual run;
- datapack enabled confirmation is missing;
- warnings are present but not triaged;
- no coordinate or biome sample supports the claimed runtime outcome;
- any required v29 trace row lacks a v30 artifact.

## Missing Proof Triage

1. Mark the affected row and final packet result `INCONCLUSIVE`.
2. Do not invent, infer, or backfill evidence.
3. Identify the missing artifact, source command, log window, screenshot, coordinate, or config file.
4. Check whether the missing proof is caused by wrong branch, stale jar, missing datapack, wrong world, wrong profile, or disabled gates.
5. Re-run only inside an explicitly approved future smoke workflow.
6. Capture fresh artifacts after any runtime/resource/source diff.
7. Recheck v23 through v29 references before reusing this packet for maintainer review.
8. Do not promote a runtime PR from an `INCONCLUSIVE` packet.

## Final Packet Result

Select one:

- `PASS`
- `FAIL`
- `INCONCLUSIVE`

Evidence basis:

- Passing rows:
- Failing rows:
- Inconclusive rows:
- Missing artifacts:
- Required rerun or follow-up:
