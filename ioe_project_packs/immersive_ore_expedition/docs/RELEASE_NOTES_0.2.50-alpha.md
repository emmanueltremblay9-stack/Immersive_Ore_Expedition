# Immersive Ore Expedition 0.2.50-alpha

## Candidate Identity

- Release candidate: `0.2.50-alpha`
- Status: `NOT_PUBLISHED`
- Qualification date: 2026-09-21
- Source HEAD: `d482389be248ad770bb9c98e3a656820f2873660`
- Source tree: `a24a7dc4cc470cdebec51214e47897765f908500`
- Minecraft: `1.21.1`
- NeoForge: `21.1.230`
- Java: 21

This document records source, hosted-CI, and artifact qualification evidence for a release candidate. Overall release qualification is `RELEASE_QUALIFIED: PASS_WITH_GAPS` after the owner distribution disposition recorded for Domum Ornamentum. Manual client, dedicated-server, and visual worldgen smoke remain `NOT_PERFORMED`, so `PUBLICATION_READY: BLOCKED_MANUAL_SMOKE`. This is not a publication record and does not imply that a tag, GitHub Release, CurseForge file, Modrinth version, or other distribution exists.

## Non-Exhaustive Major Changes Since 0.2.0-alpha

The comparison from `v0.2.0-alpha` to the qualified source contains 100 commits. The following is a bounded summary of major reviewed changes, not a complete changelog:

- PR #45 corrected compass scrolling and expedition-site placement behavior.
- PR #46 added connected expedition-site generation, and PR #47 routed ore and crystal generation through expedition sites.
- PR #48 reworked biome mineral distribution, while PR #49 added new-chunk sanitation controls.
- PR #50 extended compass targeting and branding; PR #51 addressed shaft accessibility.
- PR #52 expanded mine generation and village relationships.
- PR #53 added the Immersive Petroleum 4.5 compatibility path.
- PR #54 added installer manifests, and PR #55 finalized the Budding design documentation.
- PR #56 added the Abandoned Prospector Camp Surface 01/02 implementation and its qualification surface: deterministic active and abandoned camp composers, state/context/quality handling, collision and placement validation, optional Domum Ornamentum materialized architecture, tiered loot behavior, and focused validators and tests.
- The final PR #56 DRY/profile GameTest remediation preserved the prospector shaft contract and injected the qualified DRY camp profile into the hosted GameTest path, including commits `524e30b` and `407c892a`.

These descriptions are based on merged history and reviewed source. They do not substitute for manual client, server, or visual gameplay evidence.

## Current Worldgen and Config State

- `worldgen.global.naturalExpeditionSiteGenerationEnabled`: default `true`.
- Current natural expedition-site worldgen: active when the current gate is enabled. Direct source inspection supports the production configured-feature, placed-feature, biome-modifier, and `ExpeditionSiteFeature` path.
- Production wrapper tags use `#c:is_overworld` and remain subject to IOE's runtime eligibility and placement controls. Historical controlled-smoke tags are not the current production binding.
- `worldgen.runtimePlacementEnabled`: default `false`.
- `worldgen.runtimePlacementDiagnostics`: default `false`.
- `worldgen.runtimeProofFeatureEnabled`: default `false`.
- `worldgen.runtimeProofFeatureDiagnostics`: default `false`.
- Retrogen mutation: default-off and administrator-controlled.
- No-fake-resources and unsupported-material protections remain release gates.

PR #56 did not change these defaults. The active natural-worldgen gate and the four disabled legacy proof controls are distinct mechanisms.

## Hosted CI Qualification

- Workflow: `CI / Consolidated NeoForge module`
- Workflow run ID: `35563895610`
- Run number: `264`
- Attempt: `1`
- Event: `push`
- Conclusion: `success`
- Exact CI head SHA: `d482389be248ad770bb9c98e3a656820f2873660`
- JUnit: `PASS`
- Static validators: `PASS`
- Baseline hosted GameTests: `PASS`
- Complete pinned-runtime hosted GameTests: `PASS`
- Build and JAR inspection: `PASS`

No workflow dispatch or manual rerun was used for this qualification record.

## Qualified Artifact

- Artifact ID: `10623335948`
- Artifact digest: `sha256:fb3b476894e4a48edeab222f661c65efae012190a6266b5377f98d6c9b2ddf9d`
- Runtime JAR: `immersive_ore_expedition-0.2.50-alpha-neoforge-1.21.1.jar`
- JAR SHA-256: `f1fe4573da24df0d2a9f7a23e0e940cc64dedd6970726263640c8f871f737fbf`
- JAR size: 2,028,288 bytes
- Compiled class count: 329
- Embedded JAR count: 0

The CI artifact inspection found compiled IOE classes and `META-INF/neoforge.mods.toml` in the qualified runtime JAR.

## Manual Runtime Evidence

- Manual client world-entry smoke: `NOT_PERFORMED`
- Manual dedicated-server smoke: `NOT_PERFORMED`
- Manual visual worldgen smoke: `NOT_PERFORMED`

Hosted tests and artifact inspection do not prove a manual launch, server lifecycle, newly generated world observation, visual composition, or player-facing gameplay result.

## Third-Party Notice Status

The qualified IOE JAR contains zero embedded JARs, and no Domum Ornamentum source or assets are copied into IOE. Domum Ornamentum is an optional compile/runtime integration used by the prospector-camp materialized architecture.

Domum Ornamentum `1.0.231` has conflicting license metadata: the inspected source tag and runtime metadata identify GPL-3.0, while the published Maven POM identifies LGPL-3.0. `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`. `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`. `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`. The owner disposition is recorded in `docs/DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md` and is not legal approval. CI compatibility testing is not a legal conclusion or redistribution authorization.

## Release Readiness and Publication Blockers

- `SOURCE_READY: PASS`
- `RUNTIME_READY: PASS_CI_SCOPE_ONLY`
- `ARTIFACT_READY: PASS`
- `RELEASE_NOTES_READY: PASS`
- `DOCUMENTATION_READY: PASS`
- `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`
- `DOMUM_OWNER_DECISION_GATE: PASS`
- `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`
- `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`
- Manual client world-entry smoke: `NOT_PERFORMED`
- Manual dedicated-server smoke: `NOT_PERFORMED`
- Manual visual worldgen smoke: `NOT_PERFORMED`
- `RELEASE_QUALIFIED: PASS_WITH_GAPS`
- `PUBLICATION_READY: BLOCKED_MANUAL_SMOKE`
- Release publication: `NOT_PUBLISHED`

No tag, GitHub Release, or third-party distribution should be created from this qualification record until the required manual smoke evidence is recorded and publication is separately authorized.

## Compatibility and Rollback

- Required runtime: Minecraft `1.21.1`, NeoForge `21.1.230`, Java 21.
- Optional integrations remain separately distributed and subject to their own compatible versions and notices.
- Previous published release: `v0.2.0-alpha`.
- Config and save rollback must be evaluated before downgrading; this candidate does not claim downgrade compatibility.
