# ExtendedAE Entroized Fluix geode integration

## Mine mode

When ExtendedAE 2.2.x is loaded, a productive IOE mine reached through a `collapsed_shaft` becomes the Entroized Fluix variant. Its chamber contains:

- one chamber-facing `extendedae:entro_budding_fully` heart;
- a rock-facing body of `ae2:fluix_block` around that heart.

The block choice follows ExtendedAE's own 2.2.33 progression description: an Entro Seed converts an AE2 Fluix Block into fully budding Entroized Fluix. IOE only places those public registry blocks; ExtendedAE remains responsible for growth, degradation, buds, drops, repair recipes, models, textures, and all other behavior.

## Exclusive resource rule

Each productive mine has one resource mode:

- ordinary supported surface clues: biome-selected GeOre node-geode;
- `buried_survey_marker` with AE2 loaded: AE2 meteoritic geode;
- `collapsed_shaft` with ExtendedAE loaded: Entroized Fluix geode.

These modes are mutually exclusive. The ExtendedAE mine does not also receive a GeOre node or AE2 Certus geode. IOE places only the fully budding Entroized Fluix tier; the mostly, half, and hardly budding tiers are left to ExtendedAE's own degradation and repair mechanics.

ExtendedAE 2.2.33 does not register autonomous Entroized Fluix world generation, so there is no upstream placed feature to remove. IOE's mine placement is the only natural-world entry point added by this integration.

## GeOre: Additions restriction

Although the public GeOre: Additions description lists Budding Entroized Fluix as supported, IOE blocks its tools from recovering the budding heart and cancels ordinary entity placement of add-on blocks. The original ExtendedAE heart remains part of the mine; normal player use of the Harvester, Extractor, and future extractor block tiers cannot create a second acquisition path.

## Provenance

- Official source tag: `1.21-2.2.33-neoforge`
- Source revision inspected: `90005ee29839fb9fa83bbe6544919c722f8b0dc6`
- Mod id: `extendedae`
- License: GNU LGPL version 3

No ExtendedAE source or asset is embedded in IOE. No local build, game launch, or runtime compatibility test was performed in this development pass.
