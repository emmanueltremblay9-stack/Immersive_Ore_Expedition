# Crystal processing authority

## Required stack

IOE requires both Applied Energistics 2 and AE2 Crystal Science on Minecraft 1.21.1 NeoForge.

AE2 and AE2 Crystal Science jointly own crystal growth, powered acceleration, purification, and crystal automation. IOE only selects and places scarce expedition sites, then leaves every processing mechanic to that required stack and to the registered behavior of the upstream crystal blocks. An AE2 Certus site is unavailable unless both required mods are loaded.

- AE2: mod id `ae2`, supported line `19.2.17` up to but excluding `20.0.0`.
- AE2 Crystal Science: mod id `ae2cs`, supported line `1.1.12` up to but excluding `1.2.0`.
- GeOre: mod id `geore`, supported line `6.2.2` up to but excluding `7.0.0`.

## Ownership boundary

| Responsibility | Authority | IOE integration |
| --- | --- | --- |
| Natural site and mine placement | IOE | Chooses biome material, connected-biome quantity, mine type, and one budding heart for each selected node. |
| Bud and cluster behavior | AE2/AE2 Crystal Science stack and owning crystal blocks | GeOre and AE2 keep their registered block behavior; IOE does not clone or tick it. |
| Powered acceleration | AE2 | GeOre hearts and optional ExtendedAE Entroized Fluix hearts are added to `ae2:growth_acceleratable`. |
| Charged Certus transformations | AE2 | IOE places a repairable flawed Certus heart; AE2 consumes its Charged Certus Quartz Crystal through native water transformations after degradation. IOE adds no recipe and does not extend the conversion to GeOre. |
| Purified crystal progression | AE2 Crystal Science | Dust, seeds, purified crystals, and growth stages remain AE2CS-owned. |
| Crystal factory automation | AE2 and AE2 Crystal Science | Growth Accelerator, Growth Chamber, Aggregator, Pattern Providers, ME buses and planes remain upstream systems. |

## Prohibited IOE duplicates

IOE must not register its own growth accelerator, charged growth catalyst, geode extractor, budding harvester, crystal growth chamber, purified-crystal item family, or automated crystal harvester. IOE also must not copy textures, models, sounds, language files, recipes, or Java implementations from AE2 or AE2 Crystal Science.

IOE's `ae2:growth_acceleratable` data entry is compatibility wiring only: it delegates eligible budding hearts to AE2's existing powered accelerator and contains no acceleration algorithm. Likewise, the GeOre: Additions restriction removes competing harvesting/extractor paths so the required AE2/AE2CS stack remains the accepted crystal-automation authority; it does not produce crystals itself.

## Scarcity invariant

Only IOE-controlled mines place GeOre budding hearts. Charged Certus Quartz Crystal is not allowed to convert ordinary GeOre material blocks into new budding hearts, because that would let mined node blocks bypass biome-defined quantity and expedition-site scarcity. The IOE AE2 mine instead starts with `ae2:flawed_budding_quartz`, so AE2's own degradation and Charged Certus repair loop is part of progression without creating an IOE recipe.

## No free ore generation

This authority split does not relax IOE's world rule: normal ore veins, autonomous GeOre Overworld geodes, autonomous AE2 meteorites, and AE2 Crystal Science's normal/charged Certus ore placed features remain suppressed by IOE's world-generation integration. AE2/AE2CS control processing after the player reaches an IOE-controlled crystal site; they do not add a second natural ore source.
