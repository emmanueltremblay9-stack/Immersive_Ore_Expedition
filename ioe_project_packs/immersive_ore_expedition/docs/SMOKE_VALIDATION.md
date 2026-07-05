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

Expected current limitation: no visible IOE worldgen placement is expected from v7-v17 alone. Current systems are scaffold, planning, policy, and validation layers unless a future PR explicitly enables live placement.

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
