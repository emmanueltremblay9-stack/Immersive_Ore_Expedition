# Crystal processing authority

## Authority split

IOE does not place crystal hearts or implement a crystal-processing machine. Rare Certus and Entroized Fluix resources are outputs of real Immersive Engineering mineral mixes backed by finite abstract deposits.

| Responsibility | Authority | IOE integration |
| --- | --- | --- |
| Mineral discovery, extraction and depletion | Immersive Engineering | Core Sample and Excavator operate on the exact IOE mineral-mix ID and finite reserve. |
| Certus item, meteorites, growth, repair and automation | AE2 | IOE references `ae2:certus_quartz_crystal` only as a rare mix output. |
| Purified Certus and factory progression | AE2 Crystal Science | No IOE duplicate machine, seed, purified item, or recipe family. |
| Entroized Fluix item and progression | ExtendedAE | IOE references `extendedae:entro_crystal` only as a rare mix output. |
| GeOre block growth | GeOre | No GeOre block is placed by the active IOE site path. |

## Parallel-method invariant

AE2 meteorites remain enabled: the IOE `ae2:has_meteorites` overlay merges with `replace: false`, and the new-chunk guard preserves AE2 meteorite materials. ExtendedAE's normal crafting, machine, budding, repair, and automation methods remain available. IOE does not add a fake geode or competing intermediate matrix for either resource.

AE2 Crystal Science's physical normal and charged Certus ore placed features remain suppressed under the separate no-free-ore policy. This does not disable AE2 meteorites or the processing systems owned by AE2, AE2CS, or ExtendedAE.

## Compatibility-only data

The `ae2:growth_acceleratable` overlay delegates compatible upstream budding blocks to AE2's existing accelerator. It contains no growth algorithm and creates no acquisition path. The GeOre: Additions capability restriction is likewise independent of the mineral distribution and creates no resource.

IOE must not register a growth accelerator, charged catalyst, geode extractor, budding harvester, crystal growth chamber, purified-crystal family, automated harvester, artificial geode, or physical crystal node.
