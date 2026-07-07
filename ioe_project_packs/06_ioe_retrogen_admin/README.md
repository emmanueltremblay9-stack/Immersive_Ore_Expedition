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

Fixed in `0.1.3-alpha`:
- Retrogen radius command mode resolution now honors global enablement and per-mode config gates before queueing work.
- Processed internal chunk markers are no longer overwritten by fresh placeholder snapshots, preventing already marked chunks from being requeued.
- Gradle jar artifacts now include compiled classes as well as resources.

Fixed in `0.1.5-alpha`:
- Radius block-to-chunk conversion no longer overflows for very large admin radii.
- Radius containment checks now use long-distance math for extreme chunk coordinates.
- Resource diagnostics now report missing resource references as rejected findings instead of crashing.
- NeoForge metadata now requires the current verified `ioe_core` and `ioe_expedition_worldgen` APIs instead of allowing stale alpha dependencies.

Fixed in `0.1.6-alpha`:
- Retrogen radius planning now rejects radii above the admin command cap before allocating placeholder candidates.
- Placeholder candidate generation now clamps extreme chunk bounds instead of overflowing near integer limits.
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.4-alpha` and `ioe_expedition_worldgen` `0.1.4-alpha` APIs.

Fixed in `0.1.7-alpha`:
- Admin command controllers are now created lazily so retrogen marker/tick settings are read after server config is available instead of being frozen at class load.
- Radius retrogen now skips malformed null candidate snapshots and reports them as invalid candidates instead of crashing the request.

Fixed in `0.1.8-alpha`:
- Malformed retrogen mode strings now fall back to `off` instead of silently enabling unexplored-chunk retrogen.

Fixed in `0.1.9-alpha`:
- Completed retrogen queues now return status mode to `off` instead of reporting a stale active mode with no queued work.
- Retrogen marker and start-result records now reject impossible public states that the controller should never produce.

Fixed in `0.1.10-alpha`:
- Retrogen chunk snapshots now require an explicit chunk marker, using `ChunkRetrogenMarker.missing()` for unknown state instead of accepting null.
- Diagnostic findings now require a non-blank reason so admin reports cannot silently hide why a resource was used, skipped, or rejected.
- Resource validation reports now reject counts that do not match their diagnostic findings, keeping `safeToRun()` aligned with the underlying resource decisions.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.
5. Build with the module Gradle wrapper: `.\gradlew.bat clean build --no-daemon --stacktrace`.
6. Install only a verified build with `.\install-mod.ps1 -SkipBuild`; the script writes `build/install-report.json`.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
