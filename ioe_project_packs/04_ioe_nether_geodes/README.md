# Immersive Ore Expedition: Nether Geodes

Sub-lava Nether geodes that spawn only beneath giant Nether lava lakes.

## Phase
Phase 5

## Status
Verified alpha foundation in progress.

Implemented in `0.1.4-alpha`:
- Server-side config defaults matching the module schema.
- Giant Nether lava lake anchor validation from explicit sample reports.
- Sub-lava geode planning that requires the Nether dimension, a valid giant lava lake, and configured depth below lava.
- Nether Quartz resource checks through IOE Core policy.
- Ancient Debris heart planning only when the resource is loaded, with the default chance capped at `0.005`.
- Nether clue structure ID catalog for lava shore/edge hints.
- Unit tests for invalid dimensions, weak lake samples, missing resources, clue IDs, and no random Nether geodes.

Fixed in `0.1.2-alpha`:
- The `resources.netherQuartz` config gate now actually controls whether Nether Quartz can back sub-lava geode plans.
- Gradle jar artifacts now include compiled classes as well as resources.

Fixed in `0.1.5-alpha`:
- `allowRandomNetherGeodes` no longer disables anchored sub-lava geode planning.
- Disabling the giant-lava-lake requirement now permits Nether anchored samples instead of rejecting every sample.
- Lava lake samples now reject non-finite coverage values.
- NeoForge metadata now requires `ioe_core` `0.1.3-alpha` or newer, matching the API this module is built against.

Fixed in `0.1.6-alpha`:
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.4-alpha` API.

Fixed in `0.1.7-alpha`:
- Sub-lava geode plans now reject non-finite Ancient Debris motherlode chances instead of accepting `NaN` through the rarity bounds check.

Fixed in `0.1.8-alpha`:
- Sub-lava geode plans now require the exact `minecraft:nether_quartz_ore` primary resource instead of accepting any approved loaded block as quartz.
- Ancient Debris hearts now require the exact `minecraft:ancient_debris` resource instead of accepting other approved loaded resources.

Fixed in `0.1.9-alpha`:
- Sub-lava geode plan records now reject non-Nether anchors or lava samples even when constructed directly.
- Sub-lava geode plan records now reject weak lava-lake samples and out-of-band below-lava depths.
- Sub-lava geode plan records now enforce exact Nether Quartz and Ancient Debris resource identities.

Fixed in `0.1.10-alpha`:
- Missing optional Ancient Debris heart resources are now preserved in plan skipped-resource diagnostics instead of being silently dropped.
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.7-alpha` API.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
