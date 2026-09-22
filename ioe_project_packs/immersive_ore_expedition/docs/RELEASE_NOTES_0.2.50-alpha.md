# Immersive Ore Expedition 0.2.50-alpha

## Release Identity

- Release: `0.2.50-alpha`
- Status: `NOT_PUBLISHED`
- Minecraft: `1.21.1`
- NeoForge: `21.1.230`
- Java: 21

These tracked release notes describe stable release content. They are not a publication record and do not imply that a tag, GitHub Release, CurseForge file, Modrinth version, or other distribution exists.

## Release Qualification Provenance

These tracked notes are not the authoritative byte-level provenance record. After the candidate is frozen, a separate final external qualification/publication record must bind the exact source HEAD and tree; the exact CI workflow run, attempt, event, checkout SHA, and checkout tree; the exact artifact ID and digest; the runtime JAR filename, size, SHA-256, and inspection results; and the manual client, dedicated-server, and visual worldgen results and evidence produced from those exact artifact bytes.

Any repository write after final candidate selection creates a new candidate that requires qualification to be repeated and rebound. These notes make no manual-smoke pass or publication claim. Publication requires the complete external binding, every applicable release gate, and separate publication authorization.

## Non-Exhaustive Major Changes Since 0.2.0-alpha

The following is a bounded summary of major reviewed changes since `v0.2.0-alpha`, not a complete changelog:

- PR #45 corrected compass scrolling and expedition-site placement behavior.
- PR #46 added connected expedition-site generation, and PR #47 routed ore and crystal generation through expedition sites.
- PR #48 reworked biome mineral distribution, while PR #49 added new-chunk sanitation controls.
- PR #50 extended compass targeting and branding; PR #51 addressed shaft accessibility.
- PR #52 expanded mine generation and village relationships.
- PR #53 added the Immersive Petroleum 4.5 compatibility path.
- PR #54 added installer manifests, and PR #55 finalized the Budding design documentation.
- PR #56 added the Abandoned Prospector Camp Surface 01/02 implementation and its qualification surface: deterministic active and abandoned camp composers, state/context/quality handling, collision and placement validation, optional Domum Ornamentum materialized architecture, tiered loot behavior, and focused validators and tests.
- The final PR #56 DRY/profile GameTest remediation preserved the prospector shaft contract and injected the qualified DRY camp profile into the hosted GameTest path.

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

## Third-Party Notice Status

Domum Ornamentum remains an optional, separately distributed integration used by the prospector-camp materialized architecture. It must remain unembedded, with no Domum source or assets copied into IOE. The final external qualification/publication record must include artifact inspection that confirms those constraints for the exact runtime JAR.

Domum Ornamentum `1.0.231` has conflicting license metadata: the inspected source tag and runtime metadata identify GPL-3.0, while the published Maven POM identifies LGPL-3.0. `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`. `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`. `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`. The owner disposition is recorded in `docs/DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md` and is not legal approval. CI compatibility testing is not a legal conclusion or redistribution authorization.

## Publication Requirements and Limitations

- Successful applicable hosted qualification and runtime-JAR inspection are required for the exact frozen candidate.
- Manual client world-entry, dedicated-server, and visual worldgen smoke evidence are required from the exact runtime JAR bound by the final external qualification/publication record.
- Hosted tests and artifact inspection do not prove a manual launch, server lifecycle, newly generated world observation, visual composition, or player-facing gameplay result.
- No tag, GitHub Release, or third-party distribution may be created until every release gate has its actual evidence and publication is separately authorized.
- Status remains `NOT_PUBLISHED`.

## Compatibility and Rollback

- Required runtime: Minecraft `1.21.1`, NeoForge `21.1.230`, Java 21.
- Optional integrations remain separately distributed and subject to their own compatible versions and notices.
- Previous published release: `v0.2.0-alpha`.
- Config and save rollback must be evaluated before downgrading; this candidate does not claim downgrade compatibility.
