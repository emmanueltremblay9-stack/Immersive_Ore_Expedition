# Immersive Ore Expedition: Worldgen

Surface clues, mineshaft anchors, biome provinces, and structure-linked ore loads.

## Phase
Phase 1-2

## Status
Verified alpha foundation.

Current verified version: `0.1.1-alpha`.

Implemented:
- Gradle 8.8 wrapper and composite dependency on the verified `ioe_core` module.
- Server-authoritative common config for ore suppression, anchor-distance rules, tunnel-connection policy, and structure catalog toggles.
- Structure ID catalog for tiny vertical mine entrance, collapsed shaft, miner camp, buried survey marker, basic mineshaft connector, and ore load chamber.
- Anchored ore-load planning that validates anchor distance and delegates resource safety to `ioe_core`.
- Province rule validation that separates usable, skipped, and rejected resources without substituting missing resources.
- Unit tests for structure catalog, ore suppression, ore-load planning, and province validation.

Not yet implemented:
- Runtime configured features or placed structures.
- Direct ore block placement.
- Client/server smoke task; this skeleton currently exposes build/test tasks only.

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
