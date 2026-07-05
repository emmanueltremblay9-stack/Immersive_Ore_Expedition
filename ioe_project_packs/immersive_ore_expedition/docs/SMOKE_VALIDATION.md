# IOE Smoke Validation

This document describes manual smoke validation for Immersive Ore Expedition. Do not claim smoke passed unless the smoke was actually run and evidence was captured.

Local smoke validation is disabled by default for the Codex workflow. GitHub Actions remains the automated validation source of truth.

## Evidence To Record

- Smoke date and time.
- Minecraft version.
- NeoForge version.
- Java version.
- IOE jar filename.
- IOE jar SHA-256.
- Client or server log path.
- Fresh `latest.log` copy or excerpt location.
- Pass/fail status.
- Notes for any crash, fatal, missing dependency, or command failure.

## Client World-Entry Smoke

1. Install the release jar into a controlled Minecraft 1.21.1 / NeoForge client profile.
2. Start the client with a fresh log.
3. Enter or create a test world.
4. Confirm the mod loads without a crash.
5. Confirm `immersive_ore_expedition-common.toml` is generated or loaded.
6. Confirm there are no crash, fatal, or repeated error signatures in the checked log window.
7. If admin commands are available in the profile, confirm they respond safely and do not mutate the world unexpectedly.
8. Record the evidence listed above.

Expected current limitation: no visible IOE worldgen placement is expected from v7-v22 with default config. Current systems are scaffold, planning, policy, validation layers, a default-off placement proof gate, a default-off registration smoke bridge, declaration-only configured/placed feature data, a biome modifier smoke-tag bridge whose shipped tag binds zero real biomes by default, and a docs-only controlled external smoke profile package.

## v18 Runtime Placement Proof Smoke

v18 adds a default-off runtime placement proof path. With default config, no placement is expected:

```toml
worldgen.runtimePlacementEnabled = false
worldgen.runtimePlacementDiagnostics = false
```

Only use the proof path in a controlled smoke profile:

```toml
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

Evidence must include the normal smoke fields plus the exact config values above and the fresh log lines showing whether the v18 runtime placement proof was skipped, ready, or placed. A valid proof still requires anchor validation, a loaded block resource, resource-policy approval, strict-exclusion safety, a writable generation region, and an empty target block. Missing or denied resources must be recorded as skipped, not replaced with fallback blocks.

v18 does not register configured features, placed features, biome modifiers, structures, or the complete surface clue to ore-load gameplay loop. Do not mark live gameplay proof complete unless a manual world smoke run captures actual placement evidence.

## v19 Runtime Registration Smoke Bridge

v19 registers one custom feature type, `immersive_ore_expedition:tiny_vertical_mine_entrance`, for future controlled smoke profiles. It does not add a configured feature, placed feature, biome modifier, datapack JSON, or gameplay-loop placement by itself.

With default config, registered feature invocations remain no-op:

```toml
worldgen.runtimeProofFeatureEnabled = false
worldgen.runtimeProofFeatureDiagnostics = false
worldgen.runtimePlacementEnabled = false
worldgen.runtimePlacementDiagnostics = false
```

A controlled future smoke profile must explicitly enable the v19 bridge and the v18 runtime placement gate before any proof placement can be attempted. Evidence must record whether the registered bridge was skipped because a gate was disabled, skipped because resource policy denied or skipped the proof resource, skipped by world safety checks, ready, attempted, or placed. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v20 Configured/Placed Feature Declaration Bridge

v20 adds one configured feature JSON and one placed feature JSON for `immersive_ore_expedition:tiny_vertical_mine_entrance`. The configured feature points at the v19 custom feature type with empty `NoneFeatureConfiguration` config, and the placed feature references it with an empty placement modifier list.

v20 does not add a biome modifier or bind the placed feature to any biome, so default worlds still do not invoke the feature. A future smoke profile must explicitly add a controlled invocation path and still enable both v19 and v18 gates before placement can be attempted. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v21 Default-Off Biome Modifier Smoke-Tag Bridge

v21 adds one NeoForge biome modifier declaration, `immersive_ore_expedition:tiny_vertical_mine_entrance_smoke_bridge`, that points at the existing v20 placed feature through the IOE-owned tag `#immersive_ore_expedition:worldgen_smoke_test_biomes`. The shipped tag contains zero biome ids:

```json
{
  "replace": false,
  "values": []
}
```

With default shipped resources and config, no real biome receives the placed feature, so no world mutation is expected. A controlled future manual smoke profile may use an external datapack to add one explicit test biome to the smoke tag, but the smoke profile must also enable both default-off gates before any placement proof can be attempted:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
```

That external smoke setup is not shipped as a default binding and does not prove live placement unless manual client/server/world evidence is captured. Missing, denied, unsupported, or strictly excluded resources must still be recorded as skipped, not replaced with fallback blocks. Manual client/server/world smoke was not run unless a smoke report records the evidence listed above.

## v22 Controlled External Worldgen Smoke Profile

v22 adds a docs-only smoke profile package at `docs/smoke_profiles/v22_worldgen_smoke_profile/`. It is a manual setup aid only, not active shipped data and not smoke evidence by itself.

The profile datapack appends exactly one explicit vanilla biome id to the IOE smoke tag:

```json
{
  "replace": false,
  "values": [
    "minecraft:plains"
  ]
}
```

The profile config template enables both default-off gates for a disposable test world:

```toml
worldgen.runtimeProofFeatureEnabled = true
worldgen.runtimeProofFeatureDiagnostics = true
worldgen.runtimePlacementEnabled = true
worldgen.runtimePlacementDiagnostics = true
worldgen.provinces.runtimeIntegrationEnabled = false
worldgen.provinces.resourcePolicyRules = []
```

Do not use broad biome tags, modded biomes, or multiple test biomes for this controlled profile. Do not claim live placement unless the evidence template records a fresh log, enabled datapack, exact config values, checked coordinates, and observed placement evidence. Manual client/server/world smoke was not run by v22.

## Dedicated Server Smoke

1. Install the release jar into a controlled Minecraft 1.21.1 / NeoForge dedicated server.
2. Start the server with a fresh log.
3. Confirm startup reaches a ready state without a crash.
4. Confirm `immersive_ore_expedition-common.toml` is generated or loaded.
5. Join with a compatible client if the smoke scope includes login validation.
6. Confirm there are no crash, fatal, or repeated error signatures in the checked log window.
7. If admin commands are available, confirm they respond safely and do not mutate chunks unexpectedly.
8. Record the evidence listed above.

Expected current limitation: server smoke should validate load, config, and safe command behavior. It should not expect live ore-load chambers, anchors, clues, crystal sites, AE2 geodes, Nether geodes, Ancient Debris hearts, or retrogen resources to appear.

## Log Review Hints

Use a fresh `latest.log` for each smoke pass. Treat these as blockers until understood:

- crash reports;
- fatal errors;
- missing required dependencies;
- classloading failures;
- config parse failures;
- command registration failures;
- repeated noisy diagnostics that are enabled by default.

Optional dependency warnings should be recorded with context. Do not convert a warning into a pass or failure without checking whether it affects the smoke goal.
