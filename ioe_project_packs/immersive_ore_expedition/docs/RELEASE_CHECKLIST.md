# IOE Release Checklist

Use this checklist before publishing an Immersive Ore Expedition release from the consolidated NeoForge module. Every status must describe the exact candidate being qualified; do not promote a historical result or an unrun check to a pass.

## Release Inputs

- Active module: `ioe_project_packs/immersive_ore_expedition`
- Active mod id: `immersive_ore_expedition`
- Minecraft target: `1.21.1`
- Loader target: NeoForge
- Java target: 21
- Automated validation source of truth: GitHub Actions

## Source and CI Identity

- Record the exact source commit and Git tree for the candidate.
- Confirm the release branch is based on the intended `main` commit.
- Confirm the required GitHub Actions run passed for that exact candidate HEAD.
- Record the workflow run, run attempt, event, artifact ID, artifact digest, runtime JAR filename, JAR size, and JAR SHA-256.
- Confirm the runtime JAR contains compiled classes under `com/oblixorprime/ioe/` and `META-INF/neoforge.mods.toml`.
- Confirm the inspected runtime JAR has no unexpected embedded JARs or duplicate entries.
- Confirm the candidate version is synchronized in authoritative metadata.
- Confirm release notes describe the exact candidate and remain marked `NOT_PUBLISHED` until publication actually occurs.

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

- Record manual client world-entry smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL`.
- Record manual dedicated-server smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL`.
- Record manual visual worldgen smoke status as `NOT_PERFORMED`, `PASS`, or `FAIL`.
- Do not mark smoke as passed unless that exact candidate was run and evidence was captured.
- Attach or link the relevant fresh `latest.log` files and observation notes when smoke is run.

## Dependency and Notice Gates

- Confirm every required or optional integration changed by the candidate is represented accurately in dependency metadata and `THIRD_PARTY_NOTICES.md`.
- Confirm no third-party source or asset is described as copied when the integration only compiles or runs against a separately distributed dependency.
- Record any discrepancy between source metadata, published Maven metadata, and runtime metadata as `AMBIGUOUS / UNRESOLVED`.
- Keep release publication blocked until the project owner or qualified legal reviewer explicitly resolves every licensing ambiguity. A CI pass is not redistribution authorization or a legal conclusion.

## Release Decision

- Record compatibility, migration, rollback, and known limitations.
- Record publication status separately from source and CI qualification.
- Confirm no tag, GitHub Release, or third-party publication is created before all release gates are satisfied.
- If manual smoke is not performed or a third-party licensing discrepancy remains unresolved, preserve those blockers in the release notes and do not call the release ready for publication.

## Known Limitation

The production natural-worldgen path is implemented and currently enabled by default, but source inspection and hosted CI do not prove a manual client, dedicated-server, or visual worldgen smoke result. Those runtime observations remain separate release evidence.
