# V29 Worldgen Runtime Traceability Matrix

## Matrix Identity

- PR/task:
- Author:
- Matrix date/time:
- Branch/commit reviewed:
- Future runtime PR branch, if known:
- Active module reviewed: `ioe_project_packs/immersive_ore_expedition`

## Scope And Non-Authorization

| Check | Required | Observed | Notes |
| --- | --- | --- | --- |
| runtime still disabled in this PR | yes | | |
| no active resources changed in this PR | yes | | |
| traceability matrix only | yes | | |
| no smoke evidence invented | yes | | |
| v29 does not authorize a runtime PR | yes | | |
| v29 modifies no active resources | yes | | |
| v29 activates no runtime worldgen | yes | | |

## Prerequisite Chain

| Input | Required before future runtime PR | Evidence path | Present | Notes |
| --- | --- | --- | --- | --- |
| V23 smoke result from an actual run | yes | `../v23_worldgen_smoke_runbook/V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md` | | |
| V24 maintainer decision over that result | yes | `../v24_worldgen_smoke_evidence_gate/V24_WORLDGEN_SMOKE_DECISION_TEMPLATE.md` | | |
| V25 readiness packet | yes | `../v25_worldgen_runtime_promotion_readiness/V25_WORLDGEN_RUNTIME_PROMOTION_READINESS_TEMPLATE.md` | | |
| V26 runtime slice packet | yes | `../v26_worldgen_runtime_slice_packet/V26_WORLDGEN_RUNTIME_SLICE_PACKET_TEMPLATE.md` | | |
| V27 active runtime resource inventory | yes | `../v27_worldgen_runtime_resource_inventory/V27_WORLDGEN_RUNTIME_RESOURCE_INVENTORY_TEMPLATE.md` | | |
| V28 runtime PR preflight manifest | yes | `../v28_worldgen_runtime_pr_preflight/V28_WORLDGEN_RUNTIME_PR_PREFLIGHT_TEMPLATE.md` | | |
| V29 matrix rechecked after latest rebase | yes | this file | | |

## Required Upstream Dispositions

Fill these values from actual reviewed evidence. Do not preselect or infer them.

| Prerequisite | Required expected value | Observed value | Evidence file | Notes |
| --- | --- | --- | --- | --- |
| V23 smoke result | completed result from an actual client or server run | | | |
| V24 maintainer decision | decision compatible with runtime planning | | | |
| V25 maintainer disposition | `READY_FOR_RUNTIME_SLICE` | | | |
| V26 authorization state | `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` | | | |
| V27 maintainer disposition | `INVENTORY_ONLY_ACCEPTED` | | | |
| V28 maintainer disposition | `PREFLIGHT_ONLY_ACCEPTED` | | | |

## Allowed Traceability Statuses

Use only these values in the runtime traceability matrix:

- `UNMAPPED`
- `PARTIALLY_MAPPED`
- `FULLY_MAPPED_NOT_AUTHORIZED`
- `BLOCKED_BY_MISSING_EVIDENCE`
- `BLOCKED_BY_STALE_INVENTORY`
- `READY_FOR_MAINTAINER_REVIEW_ONLY`

## Runtime Traceability Matrix

Paths are relative to `ioe_project_packs/immersive_ore_expedition`.

| Trace ID | Candidate future runtime path | Expected future change type | v23 smoke evidence reference | v24 maintainer decision reference | v25 readiness reference | v26 implementation packet reference | v27 inventory reference | v28 preflight manifest reference | Rollback/disable reference | Validation reference | Traceability status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RTM-001 | `src/main/resources/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json` | unspecified future resource change |  |  |  |  |  |  | restore shipped empty tag or remove intentional binding | JSON/content check, CI, fresh smoke after change | `UNMAPPED` | Controlled biome binding surface only. |
| RTM-002 | `src/main/resources/data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json` | unspecified future resource change |  |  |  |  |  |  | restore IOE smoke-tag-only target | JSON/content check, CI, fresh smoke after change | `UNMAPPED` | Must not broaden biome binding without separate approval. |
| RTM-003 | `src/main/resources/data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json` | unspecified future resource change |  |  |  |  |  |  | restore declaration-only config | JSON/content check, CI, fresh smoke after change | `UNMAPPED` | Declaration-only in default shipped resources. |
| RTM-004 | `src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json` | unspecified future resource change |  |  |  |  |  |  | restore empty placement list or documented prior value | JSON/content check, CI, fresh smoke after change | `UNMAPPED` | Declaration-only unless invoked through a biome path. |
| RTM-005 | `src/main/java/com/oblixorprime/ioe/worldgen/IoeWorldgenPlacementGates.java` | unspecified future source change |  |  |  |  |  |  | preserve or restore explicit disable path | targeted tests if permitted, CI, fresh smoke after change | `UNMAPPED` | Runtime placement gate surface. |
| RTM-006 | `src/main/java/com/oblixorprime/ioe/worldgen/IoeRuntimeProofFeatureGates.java` | unspecified future source change |  |  |  |  |  |  | preserve or restore explicit disable path | targeted tests if permitted, CI, fresh smoke after change | `UNMAPPED` | Runtime proof feature gate surface. |
| RTM-007 | `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenRegistrationSmokeBridgeFeature.java` | unspecified future source change |  |  |  |  |  |  | restore no-op behavior behind default-off gate | targeted tests if permitted, CI, fresh smoke after change | `UNMAPPED` | Runtime feature invocation surface. |
| RTM-008 | `src/main/java/com/oblixorprime/ioe/worldgen/RuntimeWorldgenPlacementProof.java` | unspecified future source change |  |  |  |  |  |  | restore resource-policy checked proof behavior | targeted tests if permitted, CI, fresh smoke after change | `UNMAPPED` | Placement proof behavior surface. |
| RTM-009 | `src/main/resources/pack.mcmeta` | must not change for runtime traceability |  |  |  |  |  |  | leave unchanged unless Minecraft target changes in a separate scope | diff/scope check | `FULLY_MAPPED_NOT_AUTHORIZED` | Metadata surface, not runtime authorization. |
| RTM-010 | `src/main/resources/META-INF/neoforge.mods.toml` | must not change for runtime traceability |  |  |  |  |  |  | leave unchanged unless metadata scope is separately approved | diff/scope check | `FULLY_MAPPED_NOT_AUTHORIZED` | Mod metadata surface, not runtime authorization. |
| RTM-000 |  |  |  |  |  |  |  |  |  |  | `UNMAPPED` | Add only reviewed candidate future runtime paths. |

## Blocking Conditions

- Missing completed v23 smoke result.
- Missing or incompatible v24 maintainer decision.
- Missing v25 `READY_FOR_RUNTIME_SLICE` disposition.
- Missing v26 `AUTHORIZED_FOR_SEPARATE_RUNTIME_PR` state.
- Missing or stale v27 `INVENTORY_ONLY_ACCEPTED` disposition.
- Missing or stale v28 `PREFLIGHT_ONLY_ACCEPTED` disposition.
- Candidate future runtime path absent from the latest v27 inventory or v28 preflight manifest.
- Rollback or disable expectation missing for a candidate future runtime path.
- Validation owner or validation reference missing.
- Any referenced evidence predates a future runtime/resource diff and is reused as proof for changed behavior.

## Future Separate Runtime PR Constraints

- Exact future runtime objective:
- Exact active resource files expected to change:
- Exact Java source files expected to change:
- Exact test or validation files expected to change:
- Exact docs expected to change:
- Trace IDs covered by the future PR:
- Trace IDs intentionally out of scope:
- Default-off gate or disable path:
- Fresh smoke evidence required after the diff:
- Confirmation that pre-change evidence is not reused as proof:

## Validation Required Before Using This Matrix

- Recheck every v23-v28 evidence and planning source after rebase.
- Recheck active `src/main/resources` file list after rebase.
- Recheck the shipped smoke tag default state before editing.
- Recheck the active biome modifier target before editing.
- Recheck configured and placed feature declarations before editing.
- Recheck config gate defaults before editing.
- Run static diff/scope checks after future runtime edits.
- Run JSON/content checks after future resource edits.
- Run permitted targeted tests or document why project policy requires GitHub Actions instead.
- Use GitHub Actions as automated validation source of truth.
- Capture fresh manual client/server/world smoke evidence after any runtime/resource change.

## Rollback/Disable Expectation

- Default-off config gate that keeps the future runtime change inert:
- Resource removal or source revert that disables the future runtime change:
- Expected behavior after rollback/disable:
- Validation required after rollback/disable:
- Manual smoke evidence required after rollback/disable:

## Out Of Scope For v29

- Active shipped biome tag edits.
- Active shipped biome modifier edits.
- Active configured feature or placed feature edits.
- Java source edits.
- Config default changes.
- Legacy split-module source tree edits.
- Runtime worldgen activation.
- Smoke execution or smoke evidence acceptance.
- Runtime PR authorization.

## Maintainer Traceability Disposition

Select one only after review:

- `TRACEABILITY_ONLY_ACCEPTED`
- `TRACEABILITY_NEEDS_CORRECTION`
- `TRACEABILITY_BLOCKED`
- `RECHECK_AFTER_REBASE`
- `DO_NOT_USE_FOR_RUNTIME_PR`

## Disposition Basis

- Accepted traceability links:
- Missing or stale traceability links:
- Matrix corrections required:
- Blocking issue:
- Rebase/recheck requirement:
- Reason this must not be used for a runtime PR:

## Boundaries

- `TRACEABILITY_ONLY_ACCEPTED` activates runtime worldgen: no
- `TRACEABILITY_ONLY_ACCEPTED` authorizes active resource changes: no
- Active `src/main/resources` may be changed by this v29 matrix: no
- Future implementation must be a separate PR: yes
- Future runtime/resource change needs fresh validation: yes
