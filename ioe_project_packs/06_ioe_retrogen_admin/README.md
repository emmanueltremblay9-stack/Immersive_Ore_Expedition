# Immersive Ore Expedition: Retrogen & Admin

Safe retrogen, admin commands, chunk markers, and diagnostic tools for Immersive Ore Expedition.

## Phase
Phase 6

## Status
Verified alpha foundation.

Implemented in `0.1.1-alpha`:
- Server-side config defaults matching the module schema, with retrogen disabled by default.
- Chunk marker version records that prevent repeated processing for the same marker version.
- Conservative retrogen queue planning for off, unexplored-only, admin-radius, ore-pocket-only, and clue-plus-pocket modes.
- Admin command registration for locate/status/start/pause/radius command paths.
- Resource diagnostics that reuse IOE Core policy and loaded-resource scanning.
- Unit tests for config parsing, markers, duplicate prevention, radius filtering, queue ticking, and resource diagnostics.

Fixed in `0.1.2-alpha`:
- Admin command registration now honors `commands.*` config toggles and the admin-radius mode gate before exposing locate/status/start/pause/radius command paths.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
