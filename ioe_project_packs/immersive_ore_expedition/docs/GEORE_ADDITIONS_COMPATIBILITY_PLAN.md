# GeOre: Additions restriction policy

## Status

`ACTIVE STATIC RESTRICTION - RUNTIME UNVERIFIED`

The official GeOre: Additions page describes a Budding Harvester that drops budding blocks and a Geode Extractor that produces shards below compatible budding blocks. IOE intentionally rejects both mechanics because budding hearts are the controlled renewable core of expedition mines.

## Enforced contract

When the separately distributed `geore_additions` mod is loaded, IOE:

1. cancels breaking any recognized budding block only when the held `geore_additions` tool id contains `budding_harvester`, blocking that tool from recovering GeOre, AE2, ExtendedAE, or vanilla amethyst budding blocks;
2. cancels NeoForge entity-placement events only when the add-on block id contains `geode_extractor`, covering the current extractor and later extractor tiers without blocking unrelated add-on blocks;
3. scans each loaded server chunk for that same extractor family and removes matching legacy machines without scanning or changing ordinary ore blocks;
4. leaves IOE's original GeOre, AE2, and ExtendedAE budding hearts in their mutually exclusive mine modes;
5. gives the add-on no authority to add ore veins, geodes, meteorites, extractors, or alternate resource-production paths.

The event rule is deliberately capability-specific. It does not cancel every block or tool in the `geore_additions` namespace.

## Limits

IOE does not unregister another mod's registry entries. If GeOre: Additions is installed, the blocked tool and extractor items may still appear in creative tabs or recipe viewers even though their controlled actions are canceled. Complete removal from menus and registries requires not installing GeOre: Additions.

Commands or direct programmatic writes can temporarily bypass the entity-placement event, but a matching extractor is removed the next time its chunk loads. Recipes and registered items are not removed, so they can remain visible even though the Harvester action is canceled and extractor blocks cannot remain active in a loaded world.

The event behavior still requires GitHub-hosted CI or another explicitly approved runtime environment to verify exact NeoForge event cancellation against the official jar. No local Minecraft launch or runtime validation was performed.

## Provenance boundary

GeOre: Additions 4.0 is listed as All Rights Reserved. This policy uses only the public mod id, public feature descriptions, and registry identifiers inspected from the official artifact. No GeOre: Additions code, asset, recipe, data file, model, texture, or generated resource is copied, adapted, decompiled, or redistributed by IOE. See `THIRD_PARTY_NOTICES.md`.
