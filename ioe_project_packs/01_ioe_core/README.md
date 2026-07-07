# Immersive Ore Expedition: Core

Shared API, resource policy, config primitives, registry scanning, and province abstractions for the Immersive Ore Expedition ecosystem.

## Phase
Phase 1

## Status
Verified alpha foundation.

Current verified version: `0.1.7-alpha`.

Implemented:
- Resource whitelist / blacklist policy service.
- Runtime resource scanner for loaded mods, blocks, fluids, items, and tags.
- Shared data contracts for resources, provinces, expedition anchors, site quality rolls, and crystal-growth site types.
- Common NeoForge config registration for resource policy safety switches.
- Unit tests for policy decisions and site-quality roll behavior.

Fixed in `0.1.2-alpha`:
- Gradle jar artifacts now include compiled classes as well as resources, so composite consumers and installed runtime jars resolve the core API classes.

Fixed in `0.1.3-alpha`:
- Site-quality weight totals now reject integer overflow instead of allowing corrupted roll ranges.
- Resource policy token matching now handles nested path segments such as `ores/iron_ore`.

Fixed in `0.1.4-alpha`:
- Optional mod references now use loaded-mod checks instead of ore/resource token approval.

Fixed in `0.1.5-alpha`:
- Resource policy now rejects arbitrary resource namespaces even when their paths contain approved ore tokens.
- Loaded common ore tags such as `c:ores/iron` remain valid through typed `ResourceRef` tag evaluation.

Fixed in `0.1.6-alpha`:
- Generic resource policy now rejects loaded item and fluid refs instead of approving material-token names as ore resources.
- Common ore tag approval is limited to block tags so item/fluid tags cannot masquerade as ore-load resources.
- Specialized modules remain responsible for contextual non-block resources such as IP reservoir fluids.

Fixed in `0.1.7-alpha`:
- Resource policy decisions now reject blank reasons so downstream diagnostics cannot hide why a resource was used, skipped, or rejected.
- Missing-resource logging now requires both a concrete resource reference and a non-blank reason before emitting a warning.

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
