# Immersive Ore Expedition: Worldgen

Surface clues, mineshaft anchors, biome provinces, and structure-linked ore loads.

## Phase
Phase 1-2

## Status
Verified alpha foundation.

Current verified version: `0.1.7-alpha`.

Implemented:
- Gradle 8.8 wrapper and composite dependency on the verified `ioe_core` module.
- Server-authoritative common config for ore suppression, anchor-distance rules, tunnel-connection policy, and structure catalog toggles.
- Structure ID catalog for tiny vertical mine entrance, collapsed shaft, miner camp, buried survey marker, basic mineshaft connector, and ore load chamber.
- Anchored ore-load planning that validates anchor distance and delegates resource safety to `ioe_core`.
- Province rule validation that separates usable, skipped, and rejected resources without substituting missing resources.
- Unit tests for structure catalog, ore suppression, ore-load planning, and province validation.

Fixed in `0.1.2-alpha`:
- Inverted anchor-distance config values no longer crash ore-load planning; the effective maximum is raised to the configured minimum.
- Composite build dependency now targets the verified `ioe_core` `0.1.3-alpha` API.

Fixed in `0.1.3-alpha`:
- NeoForge metadata now requires `ioe_core` `0.1.3-alpha` or newer, matching the API this module is built against.

Fixed in `0.1.4-alpha`:
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.4-alpha` API.

Fixed in `0.1.5-alpha`:
- Ore-load planning now rejects unknown anchor type strings when structure anchors are required, instead of treating any nonblank anchor label as valid.

Fixed in `0.1.6-alpha`:
- Ore-load planning now rejects non-block and non-block-tag resource refs before policy evaluation.
- Ore-load plan records now reject unknown structure anchors, mismatched anchor distances, and out-of-window ore-load centers.
- Ore-load plan records now reject item/fluid resources even when constructed directly.

Fixed in `0.1.7-alpha`:
- Province validation now rejects otherwise-approved resources when the province has no enabled expedition anchor structure, preserving the structure/province-anchored ore-load contract.
- Gradle and NeoForge metadata now target the verified `ioe_core` `0.1.7-alpha` API.

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
