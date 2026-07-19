# Connected Ore Node and Mineral Deposit System

## Gameplay rule

Biome families select exactly one specialized IOE mineral profile, one aquatic IE profile, or no IOE profile. Generic biomes remain eligible only for ordinary non-IOE Immersive Engineering deposits. Every productive specialized expedition site contains one bounded physical formation inside its connected ore-load chamber: a GeOre material node and budding heart, a Certus budding chamber with Sky Stone, or an Entroized Fluix geode.

Each specialized profile maps to one real `immersiveengineering:mineral_mix`. Mother, Major, Minor and Direct describe the radius and finite capacity of the same recipe, not different compositions:

| IOE quality | Deposit tier |
| --- | --- |
| Motherlode | Mother |
| Rich | Major |
| Normal | Minor |
| Poor | Direct |

The transactional fallback is strict: Mother → Major → Minor → Direct. Each failed reservation or commit is compensated before the next tier is attempted. A productive locator entry is persisted only after the final structure and its required IE deposit are both confirmed.

## Physical-resource suppression

The existing `replace_normal_ores_with_nodes` identifier removes controlled free ore features, AE2 Crystal Science Certus ore features, autonomous GeOre geodes, and the listed IE physical ore features. Replacement formations are placed only by the connected expedition-site transaction after the new-chunk guard's final sanitization pass.

AE2's normal meteorite biome tag is preserved with a merge-only `replace: false` overlay, and AE2 meteorite blocks are not new-chunk guard candidates. ExtendedAE-owned budding progression blocks are also left intact. Certus and Entroized Fluix retain their mineral-mix outputs and gain a bounded chamber formation made from upstream-owned registered blocks.

Aquatic biomes receive only the official IE Alluvial Sift, Silt, or Ancient Seabed mix with IOE biome-predicate overrides. Immersive Petroleum oil, lava, and aquifer reservoirs remain parallel transactions and never replace or consume the mineral profile.

## Mine-life furniture

When `immersive_engineer_decor_controls_tool_reforged` is present, expedition galleries and the surface supply outpost resolve its registered tables, stools, crates, barrel, workbench, and alarm lamp through a registry-only bridge. Every placement has a vanilla fallback, so the décor mod remains optional and no external code or asset is bundled with IOE.

## Evidence

The complete inventories, biome matrices, exact mix chances, weights, failure chances, tier capacities, and modeled seed analysis are under `docs/biome_mineral_distribution/`. Runtime build and GameTest evidence is produced only by the GitHub-hosted CI workflow.
