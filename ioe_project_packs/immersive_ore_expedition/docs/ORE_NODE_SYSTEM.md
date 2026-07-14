# Ore Node System

## Gameplay rule

Hybrid node-geodes replace normal vanilla ore veins across the Overworld. The surface biome chooses the mine's resource, while the number of contiguous chunks belonging to that same biome increases the node budget. A mine never rolls a second resource type independently of its biome. The GeOre growth system is embedded directly into each mine node rather than generating at a separate point.

## Biome ore table

| Surface biome | Ore node resource |
| --- | --- |
| Plains, forest | Coal |
| Meadow, taiga, windswept hills | Iron |
| Savanna | Copper |
| Badlands | Gold |
| Desert | Redstone |
| Swamp | Lapis |
| Jagged peaks | Diamond |
| Stony peaks | Emerald |
| Eroded badlands | Aluminium / bauxite IE through GeOre `aluminum` |
| Windswept gravelly hills | Lead |
| Snowy slopes | Silver |
| Old-growth pine taiga | Nickel |
| Dark forest | Uranium |

## Connected-biome survey

The generator performs a four-direction flood fill over biome noise samples. It inspects at most a 9 by 9 chunk window centered on the mine chunk, so the calculation does not load or generate neighboring chunks. The resulting value is a bounded local measurement from 1 to 81 connected chunks, not an unbounded scan of the entire biome.

## Quantity scaling

| Quality | Base ore blocks | Extra block per additional connected chunks | Base nodes | Extra nodes |
| --- | ---: | ---: | ---: | ---: |
| Dry | 0 | none | 0 | 0 |
| Poor | 4 | 6 | 1 | up to 2 |
| Normal | 8 | 4 | 2 | up to 2 |
| Rich | 14 | 3 | 3 | up to 2 |
| Motherlode | 24 | 2 | 4 | up to 2 |

Extra nodes are awarded at 24 and 48 sampled connected chunks. Separated seeds grow simultaneously through face-adjacent chamber blocks, producing one connected growth order per node. The budget is then distributed across those growth orders until every computed block is assigned. Each node seed becomes one matching GeOre budding heart; its remaining blocks use the matching GeOre material block. Seeds are chosen only from the chamber-facing boundary, then one adjacent chamber-air block per heart is reserved outside the node-growth candidates so the formation cannot seal its own growth face. Behind that mineral layer, the rock-facing side receives the geode's exterior wall: calcite followed by smooth basalt. Those wall blocks are not counted as ore and do not alter the biome-scaled budget.

## Normal ore replacement scope

The `replace_normal_ores_with_nodes` NeoForge biome modifier removes the vanilla coal, iron, copper, gold, redstone, lapis, diamond, and emerald placed features from every Overworld biome. It scans every decoration step and also removes any standard modded ore configuration whose output block belongs to `#c:ores`. Dirt, gravel, clay, infested stone, stone variants, and decorative geology use the same underlying ore algorithm in vanilla but remain unchanged because their output blocks are not ore-tagged.

The `normal_ore_generation` placed-feature tag explicitly lists the vanilla ore veins and the two vanilla fossil features. Those fossils are removed because their overlays place coal or deepslate diamond ore outside expedition nodes. The tag is also the integration point for non-standard modded ore generators. A mod that uses a custom feature type, or does not tag its ore output under `#c:ores`, must add its placed feature to this tag when that resource must obey the node-only rule.

GeOre's autonomous Overworld `*_geode` placed features are removed directly by namespace and path during NeoForge's final biome-modifier phase, after feature additions have run. Their registered material and budding blocks remain available and are mixed at the same point inside each IOE mine node. See `GEORE_INTEGRATION.md` and `THIRD_PARTY_NOTICES.md` for the ownership and reuse boundary.

AE2 is a required dependency and its meteorites follow the same structure-anchored rule. IOE empties AE2's meteorite biome tag so the autonomous `ae2:meteorite` structure cannot start elsewhere. A productive `buried_survey_marker` mine can become the AE2 variant: it contains one chamber-facing, repairable `ae2:flawed_budding_quartz` heart with a rock-facing sky-stone body instead of a GeOre node-geode. A mine is therefore either a GeOre geode mine or an AE2 meteoritic-geode mine, never both. No Certus ore block, charged Certus ore variant, regular Certus block, or infinite flawless heart is placed. AE2's native Charged Certus water transformations maintain the heart after it degrades. See `AE2_METEORITE_INTEGRATION.md`.

AE2 and AE2 Crystal Science are the required processing authorities. AE2 supplies powered growth acceleration and Charged Certus Quartz transformations; AE2 Crystal Science supplies seed growth, purification, growth chambers, aggregators, and ME-oriented automation. IOE only controls where the natural hearts appear and extends AE2's growth-acceleratable block tag for GeOre and the optional ExtendedAE Entroized Fluix heart. See `CRYSTAL_PROCESSING_AUTHORITY.md`.

GeOre: Additions is restricted rather than enabled. IOE cancels its tools when they try to recover a recognized budding block and cancels NeoForge entity-placement events for blocks in the add-on namespace. This blocks normal player use of the Budding Harvester, current Geode Extractor, and future extractor block tiers while keeping the original GeOre, AE2, and ExtendedAE hearts inside IOE mines. See `GEORE_ADDITIONS_COMPATIBILITY_PLAN.md`.

ExtendedAE adds a third mutually exclusive resource mode. When it is loaded, a productive `collapsed_shaft` mine receives one `extendedae:entro_budding_fully` heart with an `ae2:fluix_block` body instead of a GeOre or Certus formation. See `EXTENDEDAE_GEODE_INTEGRATION.md`.

Immersive Engineering's six custom physical ore features are explicitly removed because they do not use vanilla `OreConfiguration`. Their five resource families re-enter terrain progression only through the biome-selected GeOre node materials `aluminum`, `lead`, `silver`, `nickel`, and `uranium`; IE bauxite maps to GeOre aluminum and never to a nonexistent `geore:budding_bauxite`. IE's hidden mineral-deposit feature remains available to the Core Sample Drill and Excavator because it does not place ore blocks. See `IMMERSIVE_ENGINEERING_RESOURCE_INTEGRATION.md`.

The removal modifier is unconditional for its Overworld biome set. Disabling expedition generation or a mine component does not restore normal ore veins; this preserves the node-only world rule, but a configuration that disables all node-producing mines intentionally leaves those resources unobtainable.

Biome tags and biome modifiers remain data-pack override points. Adding a biome to the mine tags also requires adding a matching ore profile before a mine can generate there.

## Dimension boundary

This node-only replacement applies to Overworld biomes. Nether quartz, Nether gold, and Ancient Debris remain on their existing generation path until the planned sub-lava geode system has a live configured feature that can replace them without making those resources unobtainable.
