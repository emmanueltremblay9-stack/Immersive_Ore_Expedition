# AE2 meteorite integration contract

## World-generation rule

AE2 meteorites are brought under IOE placement control. The original `ae2:meteorite` structure is disabled by replacing the `ae2:has_meteorites` biome tag with an empty tag. Because AE2 19.2.17 is required, a productive IOE mine reached through the `buried_survey_marker` surface clue can become an AE2 meteoritic-geode mine. It receives one small meteoritic formation inside its ore-load chamber instead of a GeOre node-geode.

The two mine resource modes are mutually exclusive: one chamber contains either its biome-selected GeOre node-geode or the AE2 meteoritic geode, never both.

The IOE formation contains only:

- one repairable `ae2:flawed_budding_quartz` block exposed to chamber air;
- an inward, rock-facing body made from `ae2:sky_stone_block`.

IOE never places `ae2:quartz_block`, `ae2:flawless_budding_quartz`, Certus Quartz Ore, or a charged Certus ore variant. The flawed heart can degrade through AE2's chipped and damaged tiers, then AE2's native water transformations consume `ae2:charged_certus_quartz_crystal` to restore it. IOE adds no repair recipe and does not extend this conversion to GeOre. AE2 Crystal Science 1.1.12 reintroduces normal and charged Certus ore placed features, so IOE explicitly removes `ae2cs:certus_quartz_ore_placed` and `ae2cs:charged_certus_quartz_ore_placed`. The runtime filter also rejects AE2, appeng, and AE2CS `OreConfiguration` targets whose path combines `certus` and `ore`.

## Runtime ownership

AE2 remains a separately distributed required dependency. Development and CI pin `org.appliedenergistics:appliedenergistics2:19.2.17` at runtime, but IOE does not compile against AE2 Java classes. Registry identifiers are resolved through Minecraft registries only. AE2 Crystal Science 1.1.12 is also required for the purified-crystal and factory-automation progression.

- AE2 owns all code, block behavior, models, textures, sounds, translations, recipes, loot tables, and original meteorite implementation.
- IOE owns the empty meteorite-biome tag override, mutually exclusive mine-mode choice, budding-only composition, chamber-facing opening, and sky-stone placement inside the mine.

No AE2 Java source or asset is copied into IOE.

## Evidence inspected

- Official source tag: `neoforge/v19.2.17`
- Source commit: `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`
- Original structure set: `data/ae2/worldgen/structure_set/meteorite.json`
- Original structure: `data/ae2/worldgen/structure/meteorite.json`
- Official block/item identifiers: `ae2:flawed_budding_quartz`, `ae2:charged_certus_quartz_crystal`, `ae2:sky_stone_block`
- License: GNU LGPL version 3
