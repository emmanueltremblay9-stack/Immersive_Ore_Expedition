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

## GeOre node replacement

The suppressed physical resources re-enter IOE mines through GeOre-owned registry blocks. The mapping is exact and fail-closed:

| Immersive Engineering resource | IOE biome | GeOre material block | GeOre budding heart |
| --- | --- | --- | --- |
| Aluminium / bauxite | Eroded badlands | `geore:aluminum_block` | `geore:budding_aluminum` |
| Lead | Windswept gravelly hills | `geore:lead_block` | `geore:budding_lead` |
| Silver | Snowy slopes | `geore:silver_block` | `geore:budding_silver` |
| Nickel, including the suppressed deep-nickel feature | Old-growth pine taiga | `geore:nickel_block` | `geore:budding_nickel` |
| Uranium | Dark forest | `geore:uranium_block` | `geore:budding_uranium` |

GeOre registers the bauxite-equivalent family under the American-English material name `aluminum`. There is no `geore:budding_bauxite` or `geore:bauxite_block` fallback. If either exact block for a row is absent, IOE skips that mine instead of substituting an IE ore block.

## Mineral deposits are not ore blocks

The separate `immersiveengineering:mineral_veins` configured feature records hidden industrial deposits for the Core Sample Drill and Excavator; it does not place ore blocks into terrain. IOE leaves that system registered because it is an Immersive Engineering machine progression path, not free world ore.

IOE keeps `immersiveengineering:mineral_veins` out of the global suppression tag and constrains its registration before IE records the abstract deposit:

- a GeOre Mother Node with Immersive Engineering loaded guarantees one compatible native IE vein anchored on that Mother Node;
- an already registered compatible vein at the same anchor makes the operation idempotent;
- a Mother Node is rejected before locator registration if no compatible IE mineral mix can be selected or the native vein cannot be recorded;
- Major Node regions retain the reduced secondary registration chance;
- Minor Node regions and locations outside an IOE province reject normal IE deposit registration;
- IE continues to own Core Sample discovery, depletion and Excavator extraction.

No IE ore block is used as an uncontrolled outcrop. The bridge creates an abstract `MineralVein`; it does not convert the deposit into blocks, structures or IOE nodes. Runtime gameplay validation remains separate from this static integration proof.

## Scope boundary

IOE does not copy or replace IE machines, mineral recipes, Core Samples, Excavator logic, multiblocks, manual content, assets, or APIs. The integration uses public registry identifiers and original IOE code only. Immersive Petroleum reservoirs remain governed by the separate IOE IE/IP prospecting plan and are not block ores.

## Upstream evidence

- Official source branch: `1.21.1`
- Source revision inspected: `75a27f03e4243544243567e8d5c38d336f4f10f4`
- Custom configured feature type: `immersiveengineering:ie_ore`
- Physical ore placed features inspected: bauxite, lead, silver, nickel, deep nickel, uranium
- License: Blu's License of Common Sense

No Immersive Engineering source or asset is embedded in IOE. No local build, game launch, or runtime compatibility test was performed in this development pass.
