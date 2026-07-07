# Immersive Ore Expedition: IE/IP Prospecting

Immersive Engineering mineral outcrop clues, IE deposit quantity nerf, and Immersive Petroleum surface seep clues.

## Phase
Phase 2 + Phase 7

## Status
Verified alpha foundation in progress.

Implemented in `0.1.3-alpha`:
- Server-side config defaults matching the module schema.
- Optional Immersive Engineering and Immersive Petroleum gates.
- IE outcrop clue planning from deposit-present block resources only.
- IP reservoir seep planning from loaded reservoir fluids only.
- IE mineral quantity reduction planning with normal and hard-mode multipliers.
- Unit tests for missing optional mods, missing resources, rejected resources, and no full underground rendering plans.

Fixed in `0.1.5-alpha`:
- Gradle now compiles against the verified `ioe_core:0.1.3-alpha` API instead of stale `0.1.1-alpha`.
- ModDevGradle jar output explicitly includes compiled classes for downstream modules and Prism installs.
- NeoForge metadata now requires `ioe_core` `0.1.3-alpha` or newer, matching the API this module is built against.

Fixed in `0.1.6-alpha`:
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.4-alpha` API.

Fixed in `0.1.7-alpha`:
- IP seep planning now rejects loaded fluids outside the `immersivepetroleum` namespace.
- IE/IP clue plan records now reject forbidden full underground deposit or reservoir rendering states.
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.5-alpha` API.

Fixed in `0.1.8-alpha`:
- IE outcrop clue planning now rejects block tags and only emits concrete block resources.
- IP seep clue planning now rejects fluid tags and only emits concrete fluid resources.
- IE/IP clue plan records now reject invalid resource types even when constructed directly.

Fixed in `0.1.9-alpha`:
- IP reservoir references now reject block resources, fluid tags, and non-Immersive-Petroleum fluids at construction instead of allowing invalid seep inputs to reach planner logic.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
