# IOE Release Checklist

Use this checklist before publishing an Immersive Ore Expedition release from the consolidated NeoForge module. Every status must describe the exact frozen candidate being qualified; do not promote a historical result or an unrun check to a pass. Tracked release notes describe stable release content. Mutable byte-level provenance and qualification evidence belong in the final external qualification/publication record created after the candidate is frozen.

## Release Inputs

- Active module: `ioe_project_packs/immersive_ore_expedition`
- Active mod id: `immersive_ore_expedition`
- Minecraft target: `1.21.1`
- Loader target: NeoForge
- Java target: 21
- Automated validation source of truth: GitHub Actions

## Source and CI Identity

- After the candidate is frozen, record its exact source commit and Git tree in the final external qualification/publication record.
- Confirm the release branch is based on the intended `main` commit.
- Confirm the required GitHub Actions run passed for that exact candidate HEAD.
- Record the workflow run, run attempt, event, exact CI checkout SHA and tree, artifact ID, artifact digest, runtime JAR filename, JAR size, and JAR SHA-256 in the final external qualification/publication record.
- Confirm the runtime JAR contains compiled classes under `com/oblixorprime/ioe/` and `META-INF/neoforge.mods.toml`.
- Confirm the inspected runtime JAR has no unexpected embedded JARs or duplicate entries.
- Confirm the candidate version is synchronized in authoritative metadata.
- Confirm tracked release notes match the release version, included changes, runtime and config behavior, compatibility and migration notes, third-party notices, known limitations, and rollback guidance, and retain `NOT_PUBLISHED` as their stable prepublication status. Record actual publication status only in the final external qualification/publication record after successful destination readback.
- Confirm the final external qualification/publication record binds those tracked notes to the exact frozen source identity and exact artifact bytes.
- After final candidate selection, treat any repository write as a new candidate that requires qualification to be repeated and rebound.

## Automated Validation

- Record JUnit status as `NOT_RUN`, `PASS`, or `FAIL`.
- Record static validator status as `NOT_RUN`, `PASS`, or `FAIL`.
- Record baseline hosted GameTest status as `NOT_RUN`, `PASS`, or `FAIL`.
- Record the complete pinned-runtime hosted GameTest status as `NOT_RUN`, `PASS`, or `FAIL`.
- Record build and JAR inspection status as `NOT_RUN`, `PASS`, or `FAIL`.
- Do not treat a successful hosted check as manual client, server, or visual worldgen proof.

## Current Worldgen and Config Gates

- Confirm `worldgen.global.naturalExpeditionSiteGenerationEnabled` is still default `true`; natural expedition-site worldgen is active when this current gate is enabled.
- Confirm the four legacy proof controls remain separate and default `false`: `worldgen.runtimePlacementEnabled`, `worldgen.runtimePlacementDiagnostics`, `worldgen.runtimeProofFeatureEnabled`, and `worldgen.runtimeProofFeatureDiagnostics`.
- Confirm the active configured features, placed features, and production biome modifiers expected by this release are present and resolve to the intended expedition-site placement path.
- Confirm broad `#c:is_overworld` biome membership is used only by the validated production wrapper tags. Historical controlled-smoke tags and procedures must remain narrowly scoped and must not be presented as the current production binding.
- Confirm retrogen mutation remains default-off and administrator-controlled.
- Confirm strict exclusions, unsupported-material skipping, and the no-fake-resources policy remain enforced.
- Confirm no new blocks, items, entities, ores, gems, fluids, recipes, loot tables, creative tabs, mixins, access transformers, embedded JARs, or dependencies were added unexpectedly.
- Confirm no legacy six-module source tree was edited and `.codegraph/` or other generated analysis output was not staged.

## Manual Smoke Status

- Record manual client world-entry smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL` in the final external qualification/publication record.
- Record manual dedicated-server smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL` in the final external qualification/publication record.
- Record manual visual worldgen smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL` in the final external qualification/publication record.
- Require every smoke to use the exact runtime JAR filename and SHA-256 bound by that final record.
- Do not mark smoke as passed unless that exact artifact was run and evidence was captured.
- Attach or link the relevant fresh `latest.log` files and observation notes in the final external qualification/publication record when smoke is run.

## Dependency and Notice Gates

- Confirm every required or optional integration changed by the candidate is represented accurately in dependency metadata and `THIRD_PARTY_NOTICES.md`.
- Confirm no third-party source or asset is described as copied when the integration only compiles or runs against a separately distributed dependency.
- Record any discrepancy between source metadata, published Maven metadata, and runtime metadata as `AMBIGUOUS / UNRESOLVED`; when that mismatch is confirmed, also record `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`.
- Link the exact owner distribution disposition for any candidate that proceeds with disclosed mixed metadata.
- Require `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION` and verify that Domum remains optional, separately distributed, unembedded, and without copied source or assets before recording `DOMUM_OWNER_DECISION_GATE: PASS`.
- Keep legal review and compatibility separate from owner distribution authority. For this candidate, legal review remains `NOT_PERFORMED` and `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`; the owner decision is not legal clearance.

## Release Decision

- Record compatibility, migration, rollback, and known limitations.
- Record publication status separately from source and CI qualification.
- Require the complete final external qualification/publication record before publication.
- Confirm no tag, GitHub Release, or third-party publication is created before all release gates are satisfied.
- If any manual smoke is `NOT_PERFORMED`, preserve `PUBLICATION_READY: BLOCKED_MANUAL_SMOKE` even when the owner-decision gate passes. Keep unresolved legal compatibility explicit without converting owner authority into legal clearance.

## Known Limitation

The production natural-worldgen path is implemented and currently enabled by default, but source inspection and hosted CI do not prove a manual client, dedicated-server, or visual worldgen smoke result. Those runtime observations remain separate release evidence.
