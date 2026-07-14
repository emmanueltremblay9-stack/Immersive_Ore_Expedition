# GeOre integration contract

## Active gameplay contract

IOE creates one legitimate Overworld resource form: a hybrid node-geode located inside an IOE expedition mine. The node and the GeOre growth system occupy the same connected formation rather than generating as separate sites.

Normal vanilla ore veins, standard modded `OreConfiguration` outputs tagged as `#c:ores`, and GeOre's autonomous Overworld `*_geode` placed features are removed. GeOre remains responsible for the blocks and their growth behavior, while IOE controls their world placement.

## Runtime ownership

GeOre is a required runtime dependency from version 6.2.2 up to, but excluding, 7.0 for Minecraft 1.21.1. Development and CI pin the official Modrinth 6.2.2 artifact (`maven.modrinth:Xw6zG9hl:OV3DOSul`) at runtime. IOE does not compile against GeOre Java classes. The bridge resolves public Minecraft registry identifiers at placement time, which keeps ownership clear:

- GeOre owns budding growth, drops, recipes, models, textures, sounds, translations, and the upstream geode implementation used as the design basis.
- IOE owns biome-to-resource selection, connected-biome quantity scaling, mine/node placement, and the choice of which GeOre blocks form each mine node.
- AE2 owns acceleration through its powered `ae2:growth_accelerator`; IOE only adds the GeOre budding hearts to `ae2:growth_acceleratable`.
- AE2 Crystal Science owns the purified-crystal progression and crystal-factory automation. IOE does not register a competing growth chamber, accelerator, purification machine, harvester, extractor, or charged catalyst.

If either required registry entry is absent, IOE refuses that mine placement instead of silently substituting a vanilla ore block.

## Node composition

Each logical IOE node is also its GeOre-style geode. It contains exactly one GeOre budding heart, and the rest of its computed block budget uses the matching GeOre material block. The heart is placed at the seed of the face-connected growth order, so every material block in that hybrid formation remains connected to its renewable source. Seed candidates are restricted to the chamber-facing boundary, and one specific adjacent chamber-air position is reserved outside the node-growth candidates for every selected heart.

The formation keeps the recognizable exterior wall of a GeOre geode on its rock-facing side: calcite directly behind the mineral layer, then smooth basalt farther into the surrounding stone. This envelope is structural and does not consume or increase the biome-scaled mineral budget. The chamber-facing side stays open so the budding heart is visible and can grow crystals into mine air.

| IOE resource | GeOre material block | GeOre budding heart |
| --- | --- | --- |
| Coal | `geore:coal_block` | `geore:budding_coal` |
| Iron | `geore:iron_block` | `geore:budding_iron` |
| Copper | `geore:copper_block` | `geore:budding_copper` |
| Gold | `geore:gold_block` | `geore:budding_gold` |
| Redstone | `geore:redstone_block` | `geore:budding_redstone` |
| Lapis | `geore:lapis_block` | `geore:budding_lapis` |
| Diamond | `geore:diamond_block` | `geore:budding_diamond` |
| Emerald | `geore:emerald_block` | `geore:budding_emerald` |
| Aluminium / IE bauxite | `geore:aluminum_block` | `geore:budding_aluminum` |
| Lead | `geore:lead_block` | `geore:budding_lead` |
| Silver | `geore:silver_block` | `geore:budding_silver` |
| Nickel | `geore:nickel_block` | `geore:budding_nickel` |
| Uranium | `geore:uranium_block` | `geore:budding_uranium` |

The biome still defines the material, and the number of contiguous chunks in that biome still increases the total node budget. GeOre does not override those IOE rules. GeOre's official registry name for the IE aluminium/bauxite resource is `aluminum`; IOE deliberately does not probe `budding_bauxite` because that registry entry does not exist.

Charged Certus Quartz Crystal remains the AE2-owned catalyst for AE2's native water transformations. IOE does not make GeOre budding hearts craftable from their material blocks, because that would bypass biome quantity and mine scarcity. See `CRYSTAL_PROCESSING_AUTHORITY.md`.

## Reuse inventory

| IOE path | Classification | Upstream material embedded |
| --- | --- | --- |
| `src/main/java/com/oblixorprime/ioe/worldgen/GeOreNodeIntegration.java` | Original IOE registry bridge | None |
| `src/main/java/com/oblixorprime/ioe/worldgen/BiomeOreNodeProfile.java` | Original IOE integration change | None |
| `src/main/java/com/oblixorprime/ioe/worldgen/ExpeditionSiteBlueprints.java` | Original IOE placement and vanilla calcite/smooth-basalt envelope | None |
| `src/main/resources/META-INF/neoforge.mods.toml` | Dependency declaration | None |
| `THIRD_PARTY_NOTICES.md` | Attribution and license notice | GeOre MIT license text |

No GeOre texture, model, sound, language file, loot table, recipe, Java source file, or generated worldgen JSON is copied into IOE. Those resources remain in the separately installed GeOre jar. IOE suppresses GeOre's autonomous Overworld placement by recognizing the registered `geore:*_geode` placed-feature identifiers during NeoForge's final biome-modifier phase, after GeOre has added its features.

## Upstream evidence

- CurseForge project ID: `530544`
- Minimum supported runtime artifact: `GeOre-1.21.1-6.2.2.jar`, NeoForge, Modrinth version `OV3DOSul`
- Latest source revision inspected: GeOre 6.2.3 development line
- Official source: `Mrbysco/GeOre`, branch `1.21`
- Source revision inspected: `307174eaf237e82236b8534d59c554031000ba05`
- Upstream license file SHA-256: `4987EEFAB67A9C3554F94592A8C77D1E7D9F23D8B42ADE19D4A723BA14B7729A`

Any future copied or adapted upstream file must be added to this inventory and must preserve the MIT copyright and permission notice.

## GeOre: Additions restriction

IOE preserves the original GeOre budding hearts but blocks GeOre: Additions tools from recovering any budding block and cancels ordinary entity placement of blocks registered by that add-on. This prevents the Budding Harvester, the Geode Extractor, and future extractor block tiers from bypassing expedition-mine control through normal player use. The exact limits and provenance boundary are documented in `GEORE_ADDITIONS_COMPATIBILITY_PLAN.md`; no GeOre: Additions runtime dependency or copied asset is introduced.
