# Immersive Ore Expedition: Core

Shared API, resource policy, config primitives, registry scanning, and province abstractions for the Immersive Ore Expedition ecosystem.

## Phase
Phase 1

## Status
Verified alpha foundation.

Current verified version: `0.1.1-alpha`.

Implemented:
- Resource whitelist / blacklist policy service.
- Runtime resource scanner for loaded mods, blocks, fluids, items, and tags.
- Shared data contracts for resources, provinces, expedition anchors, site quality rolls, and crystal-growth site types.
- Common NeoForge config registration for resource policy safety switches.
- Unit tests for policy decisions and site-quality roll behavior.

Verified with:
- `.\gradlew.bat clean build`
- Installed jar metadata/hash readback in the NeoForge 1.21.1 Prism LAB mods directory.

## Start here
1. Read `docs/PROJECT_SPEC.md`.
2. Read `docs/DECISION_LOG.md`.
3. Use `CODEX_IMPLEMENTATION_PROMPT.txt` as the implementation handoff.
4. Verify exact NeoForge/Gradle template before compiling.

## Shared constraints
See `docs/SHARED_DECISIONS.md` and `docs/RESOURCE_POLICY.md`.
