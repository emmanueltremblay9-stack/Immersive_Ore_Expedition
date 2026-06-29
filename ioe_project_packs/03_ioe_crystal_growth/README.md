# Immersive Ore Expedition: Crystal Growth

AE2, vanilla amethyst, and optional GeOre crystal-growth sites anchored to expedition structures.

## Phase
Phase 3-4

## Status
Verified alpha foundation in progress.

Implemented in `0.1.2-alpha`:
- Server-side config defaults matching the module schema.
- Structure-anchored vanilla amethyst site planning from loaded resources only.
- Optional AE2 Certus/meteoritic site planning gated by the `ae2` mod.
- Optional GeOre site planning gated by the `geore` mod and IOE resource policy.
- GeOre policy hooks that require expedition anchors and disable/avoid free random GeOre worldgen.
- Unit tests for missing optional mods, missing resources, excluded GeOre variants, and fake Fluix rejection.

Fixed in `0.1.4-alpha`:
- Gradle now compiles against the verified `ioe_core:0.1.3-alpha` API instead of stale `0.1.1-alpha`.
- ModDevGradle jar output explicitly includes compiled classes for downstream modules and Prism installs.
- NeoForge metadata now requires `ioe_core` `0.1.3-alpha` or newer, matching the API this module is built against.

Fixed in `0.1.5-alpha`:
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.4-alpha` API.

Fixed in `0.1.6-alpha`:
- Vanilla amethyst plans no longer advertise a meteoritic variant unless a validated sky-stone crust path exists.

Fixed in `0.1.7-alpha`:
- Amethyst growth plans now require an actual vanilla amethyst core instead of accepting other approved loaded vanilla resources.
- AE2 Certus plans now require a Certus core and sky-stone crust resource instead of accepting any approved AE2 resource in either slot.

Fixed in `0.1.8-alpha`:
- Amethyst and AE2 crust planning now reject item-form resources where a placeable block core or crust is required.
- GeOre site planning now requires a loaded `geore` block core instead of accepting unrelated approved resources while GeOre is loaded.

Fixed in `0.1.9-alpha`:
- Crystal-growth plan construction now rejects direct item-form amethyst, AE2 Certus, and GeOre cores instead of allowing callers to bypass provider resource-family checks.
- AE2 Certus site planning now treats budding Certus block resources as valid cores and rejects item-form Certus crystals under the shared IOE resource policy.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
