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
# Project Spec — IOE IE/IP Prospecting

## Responsibility
This module adds subtle surface clue features for Immersive Engineering mineral deposits and Immersive Petroleum reservoirs.

## Must implement
- IE mineral outcrop clues: small surface boulders/rubble made from resources present in the IE deposit.
- IE mineral deposit quantity reduction to keep Excavator deposits limited.
- IP reservoir seep clues: small surface pocket lake/seep/vent made from reservoir resource/fluid.

## Must not implement
- No full underground IE mineral deposit rendering.
- No full underground IP reservoir rendering.
- No fake ore/resource blocks.
- No Immersive Petro-Machinery integration.

## Design rule
Render clues, not deposits.
