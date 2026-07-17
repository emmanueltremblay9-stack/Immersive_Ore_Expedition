# Abstract Mineral Deposit System

> Historical file name retained for links. The active system no longer places ore nodes.

## Gameplay rule

Biome families select exactly one specialized IOE mineral profile, one aquatic IE profile, or no IOE profile. Generic biomes remain eligible only for ordinary non-IOE Immersive Engineering deposits. Productive expedition sites contain structural blocks only; they never contain loose ore, GeOre nodes, budding crystals, artificial geodes, meteorite replicas, or intermediate matrices.

Each specialized profile maps to one real `immersiveengineering:mineral_mix`. Mother, Major, Minor and Direct describe the radius and finite capacity of the same recipe, not different compositions:

| IOE quality | Deposit tier |
| --- | --- |
| Motherlode | Mother |
| Rich | Major |
| Normal | Minor |
| Poor | Direct |

The transactional fallback is strict: Mother → Major → Minor → Direct. Each failed reservation or commit is compensated before the next tier is attempted. A productive locator entry is persisted only after the final structure and its required IE deposit are both confirmed.

## Physical-resource suppression

The existing `replace_normal_ores_with_nodes` identifier is retained for data compatibility, but its active result is suppression, not node placement. The final biome modifier and new-chunk guard remove controlled free ore features, AE2 Crystal Science Certus ore features, autonomous GeOre geodes, and the listed IE physical ore features. They do not place a replacement block formation.

AE2's normal meteorite biome tag is preserved with a merge-only `replace: false` overlay, and AE2 meteorite blocks are not new-chunk guard candidates. ExtendedAE-owned budding progression blocks are also left intact. Certus and Entroized Fluix enter the IOE distribution only as verified direct mineral-mix item outputs.

Aquatic biomes receive only the official IE Alluvial Sift, Silt, or Ancient Seabed mix with IOE biome-predicate overrides. Immersive Petroleum oil, lava, and aquifer reservoirs remain parallel transactions and never replace or consume the mineral profile.

## Evidence

The complete inventories, biome matrices, exact mix chances, weights, failure chances, tier capacities, and modeled seed analysis are under `docs/biome_mineral_distribution/`. Runtime build and GameTest evidence is produced only by the GitHub-hosted CI workflow.
