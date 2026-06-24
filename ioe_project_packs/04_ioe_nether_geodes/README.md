# Immersive Ore Expedition: Nether Geodes

Sub-lava Nether geodes that spawn only beneath giant Nether lava lakes.

## Phase
Phase 5

## Status
Verified alpha foundation in progress.

Implemented in `0.1.1-alpha`:
- Server-side config defaults matching the module schema.
- Giant Nether lava lake anchor validation from explicit sample reports.
- Sub-lava geode planning that requires the Nether dimension, a valid giant lava lake, and configured depth below lava.
- Nether Quartz resource checks through IOE Core policy.
- Ancient Debris heart planning only when the resource is loaded, with the default chance capped at `0.005`.
- Nether clue structure ID catalog for lava shore/edge hints.
- Unit tests for invalid dimensions, weak lake samples, missing resources, clue IDs, and no random Nether geodes.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
