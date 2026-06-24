# Immersive Ore Expedition: Crystal Growth

AE2, vanilla amethyst, and optional GeOre crystal-growth sites anchored to expedition structures.

## Phase
Phase 3-4

## Status
Verified alpha foundation in progress.

Implemented in `0.1.1-alpha`:
- Server-side config defaults matching the module schema.
- Structure-anchored vanilla amethyst site planning from loaded resources only.
- Optional AE2 Certus/meteoritic site planning gated by the `ae2` mod.
- Optional GeOre site planning gated by the `geore` mod and IOE resource policy.
- GeOre policy hooks that require expedition anchors and disable/avoid free random GeOre worldgen.
- Unit tests for missing optional mods, missing resources, excluded GeOre variants, and fake Fluix rejection.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
