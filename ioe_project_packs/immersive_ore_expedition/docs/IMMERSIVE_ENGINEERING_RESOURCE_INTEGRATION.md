# Immersive Engineering resource integration

## Physical ore rule

Immersive Engineering 1.21.1 uses its custom `immersiveengineering:ie_ore` feature rather than vanilla `OreConfiguration`. IOE therefore lists every IE physical ore placed feature explicitly in `#immersive_ore_expedition:normal_ore_generation`:

- `immersiveengineering:bauxite`;
- `immersiveengineering:lead`;
- `immersiveengineering:silver`;
- `immersiveengineering:nickel`;
- `immersiveengineering:deep_nickel`;
- `immersiveengineering:uranium`.

The final IOE biome-modifier phase removes those features from Overworld biomes. This prevents the corresponding stone and deepslate ore blocks from appearing outside IOE-controlled resource progression.

## Coexistence with Budding nodes

The final Budding design permits validated GeOre nodes and native Certus Budding blocks inside expedition sites. The abstract Immersive Engineering `MineralVein` remains a separate finite industrial reserve for Core Sample and Excavator progression; the Budding decision does not delete or redefine that reserve system. Artificial geodes and competing processing machines remain out of scope.

## Mineral deposits are not ore blocks

The separate `immersiveengineering:mineral_veins` configured feature records hidden industrial deposits for the Core Sample Drill and Excavator; it does not place ore blocks into terrain. IOE leaves that system registered because it is an Immersive Engineering machine progression path, not free world ore.

IOE keeps `immersiveengineering:mineral_veins` out of the global suppression tag and constrains registration as follows:

- natural-site placement is admitted across the Overworld, then the biome profile is sampled at the planned underground chamber center; generic and aquatic chambers fail the specialized profile gate, while allowlisted cave biomes can own a deposit beneath a surface clue;
- one exact IOE `mineral_mix` and one reserve tier are prepared without writing structure blocks from the worldgen worker;
- site blocks and the prepared vein are committed only during final chunk confirmation on the server thread;
- an already registered compatible vein at the same anchor and radius makes the commit idempotent;
- reserve failure walks the complete `MOTHERLODE → RICH → NORMAL → POOR` chain, mapped to Mother → Major → Minor → Direct, without changing composition;
- every failed tier is compensated before the next lower tier is attempted; exhaustion rejects the site instead of generating free ore or silently dropping its resource;
- a committed IOE vein remains compensatable until the locator entry is recorded, while a pre-existing compatible IE vein is never removed;
- natural productive sites fail closed when IE is absent; the baseline GameTests assert that no blocks or locator entry leak in that mode;
- IE continues to own Core Sample discovery, depletion and Excavator extraction.

## Aquatic recipes

`Alluvial Sift`, `Silt`, and `Ancient Seabed` keep their native Immersive Engineering recipe identifiers, compositions, weights, failure chances, and official spoils. IOE overrides only their `biome_predicates` so each aquatic family selects exactly one of those native recipes. This avoids duplicate IOE/native aquatic candidates and preserves the official 20% diamond by-product in `Alluvial Sift`.

Specialized terrestrial and subterranean biomes suppress native random IE vein creation because their exact deposit is committed transactionally with the expedition site. The native gate scans the full biome column at quart-height intervals, so a cave-owned profile is not misclassified from its surface biome. Unassigned columns retain generic native IE recipe selection.

No IE ore block is used as an uncontrolled outcrop. The bridge creates an abstract `MineralVein`; it does not convert the deposit into blocks, structures, IOE nodes, or artificial geodes. Runtime gameplay validation remains separate from static integration proof.

## Scope boundary

IOE does not copy or replace IE machines, mineral recipes, Core Samples, Excavator logic, multiblocks, manual content, assets, or APIs. The integration uses public registry identifiers and original IOE code only. Immersive Petroleum reservoirs remain governed by the separate IOE IE/IP prospecting plan and are not block ores.

## Upstream evidence

- Official source branch: `1.21.1`
- Source revision inspected: `75a27f03e4243544243567e8d5c38d336f4f10f4`
- Custom configured feature type: `immersiveengineering:ie_ore`
- Physical ore placed features inspected: bauxite, lead, silver, nickel, deep nickel, uranium
- License: Blu's License of Common Sense

No Immersive Engineering source or asset is embedded in IOE. No local build, game launch, or runtime compatibility test was performed in this development pass.
