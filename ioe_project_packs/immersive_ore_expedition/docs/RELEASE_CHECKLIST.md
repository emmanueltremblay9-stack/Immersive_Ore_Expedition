# IOE Release Checklist

Use this checklist before publishing an Immersive Ore Expedition release from the consolidated NeoForge module.

## Release Inputs

- Active module: `ioe_project_packs/immersive_ore_expedition`
- Active mod id: `immersive_ore_expedition`
- Minecraft target: `1.21.1`
- Loader target: NeoForge
- Java target: 21
- Automated validation source of truth: GitHub Actions

## Pre-Release Checks

- Update from `main` and confirm the release branch is based on the intended merge commit.
- Confirm GitHub Actions passed for `CI / Consolidated NeoForge module`.
- Confirm the CI jar artifact inspection passed.
- Confirm the release jar contains compiled classes under `com/oblixorprime/ioe/`.
- Confirm the release jar contains `META-INF/neoforge.mods.toml`.
- Confirm no config defaults changed unexpectedly.
- Confirm no legacy six-module source trees were edited.
- Confirm `.codegraph/` was not staged or committed.
- Confirm release notes or changelog text is updated.
- Confirm Java 21 and NeoForge 1.21.1 compatibility notes are present.

## Smoke Status

- Record manual client world-entry smoke status: not run, pass, or fail.
- Record manual dedicated server smoke status: not run, pass, or fail.
- Do not mark smoke as passed unless it was actually run and evidence was captured.
- Attach or link the relevant fresh `latest.log` files when smoke is run.

## Safety Gates

- Confirm strict exclusions and the no-fake-resources policy remain enforced.
- Confirm runtime worldgen placement remains gated and default-off unless a later explicit release changes that policy.
- Confirm `worldgen.runtimePlacementEnabled` and `worldgen.runtimePlacementDiagnostics` remain default `false`.
- Confirm `worldgen.runtimeProofFeatureEnabled` and `worldgen.runtimeProofFeatureDiagnostics` remain default `false`.
- Confirm any configured/placed feature declarations remain unbound from biome modifiers and biome generation unless a later explicit release changes that policy.
- Confirm v20 declaration-only configured/placed feature resources do not claim live placement or smoke proof.
- Confirm retrogen mutation remains default-off and admin-controlled.
- Confirm no configured features, placed features, or biome modifiers were enabled unexpectedly.
- Confirm no new blocks, items, entities, ores, gems, fluids, recipes, loot tables, creative tabs, mixins, access transformers, embedded jars, or dependencies were added unexpectedly.

## Known Limitation

Most IOE worldgen systems are currently scaffold or planning layers. A release may validate loading, config generation, command safety, and artifact structure without claiming the full gameplay loop or visible live worldgen placement is complete.
