# Immersive Ore Expedition — Shared Decisions

## Project identity
Immersive Ore Expedition is a NeoForge 1.21.1 / Java 21 mod ecosystem focused on replacing random branch mining with resource expeditions.

Core gameplay loop:

```text
Explore surface or landmark biome
→ find clue structure
→ enter mine/shaft/geode route
→ reach ore load or crystal-growth site
→ exploit the site locally
→ move to a different biome/province for different resources
```

## Hard rules
1. No fake ores, gems, or fantasy resources.
2. Use only loaded/approved resources from the modpack registry.
3. Missing resources must be skipped and logged, never substituted.
4. Ore loads must be structure/province anchored.
5. Random useful ore outside expedition systems should be drastically reduced.
6. Some sites are dry, poor, normal, rich, or motherlode.
7. Immersive Petro-Machinery, MineColonies, and CIVITAS are out of scope.
8. GeOre is optional; if loaded, free/random GeOre geodes should be disabled or redirected to structure-anchored sites.
9. AE2 resources are handled as buried meteorite / Certus crystal-growth sites, not fake Fluix ore.
10. Nether geodes spawn only under giant Nether lava lakes.
11. No full underground IE/IP deposit rendering.
12. IE deposits may have small surface mineral boulder clues.
13. IP reservoirs may have small surface seep/pocket-lake clues.
14. IE mineral deposit quantities should be drastically reduced so IE Excavator does not dominate the resource economy.

## Approved resource whitelist
Vanilla:
- Coal
- Iron
- Copper
- Gold
- Redstone
- Lapis Lazuli
- Diamond
- Emerald
- Amethyst
- Nether Quartz
- Ancient Debris

Immersive Engineering:
- Bauxite / Aluminum
- Lead
- Silver
- Nickel
- Uranium

Applied Energistics 2:
- Certus Quartz
- Budding Certus variants
- Sky Stone / meteorite blocks

Draconic Evolution:
- Draconium

GeOre:
- Only variants that correspond to the approved loaded resources above.

Immersive Petroleum:
- Reservoir fluids only as surface clue features, not as full underground rendered reservoirs.

## Explicitly excluded unless user re-adds later
- Apatite
- Tin
- Forestry Copper
- Platinum
- Osmium
- Tungsten
- Black Quartz
- Uraninite
- Monazite

## Compatibility principles
- Core should remain usable without optional compat mods.
- Each sub-mod should fail soft when optional mods are missing.
- Runtime registry/tags are source of truth.
- Config must be server-authoritative.


## Module decision
# Project Spec — IOE Worldgen

## Responsibility
This module owns the main anti-branch-mining gameplay loop.

## Must implement
- Random ore suppression hooks/config strategy.
- Biome province rules.
- Surface clue structures: tiny vertical mine entrance, collapsed shaft, miner camp, survey marker.
- Underground mineshaft anchors and ore-load chambers.
- Site quality roll: dry, poor, normal, rich, motherlode.
- Ore loads must be near/connected to expedition anchors.

## Must not implement
- No GeOre-specific code.
- No AE2-specific code.
- No IE/IP-specific clue logic.
- No retrogen beyond hooks for future retrogen module.

## Design rule
The player should find resources by following clues and mine networks, not by branch-mining at a universal Y-level.
