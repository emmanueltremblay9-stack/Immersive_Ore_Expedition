# Immersive Ore Expedition: IE/IP Prospecting

Immersive Engineering mineral outcrop clues, IE deposit quantity nerf, and Immersive Petroleum surface seep clues.

## Phase
Phase 2 + Phase 7

## Status
Verified alpha foundation in progress.

Implemented in `0.1.2-alpha`:
- Server-side config defaults matching the module schema.
- Optional Immersive Engineering and Immersive Petroleum gates.
- IE outcrop clue planning from deposit-present block resources only.
- IP reservoir seep planning from loaded reservoir fluids only.
- IE mineral quantity reduction planning with normal and hard-mode multipliers.
- Unit tests for missing optional mods, missing resources, rejected resources, and no full underground rendering plans.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
