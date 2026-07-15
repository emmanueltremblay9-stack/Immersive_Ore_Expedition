# GeOre: Additions restriction policy

## Status

`ACTIVE STATIC RESTRICTION - RUNTIME UNVERIFIED`

The official GeOre: Additions page describes a Budding Harvester that drops budding blocks and a Geode Extractor that produces shards below compatible budding blocks. IOE intentionally rejects both mechanics because budding hearts are the controlled renewable core of expedition mines.

## Enforced contract

When the separately distributed `geore_additions` mod is loaded, IOE:

1. cancels breaking any recognized budding block when the player's held item is registered by `geore_additions`, blocking the Budding Harvester from recovering GeOre, AE2, ExtendedAE, or vanilla amethyst budding blocks;
2. cancels NeoForge entity-placement events for blocks registered under the `geore_additions` namespace, blocking ordinary placement of the current Geode Extractor and any future tier implemented as another add-on block;
3. leaves IOE's original GeOre, AE2, and ExtendedAE budding hearts in their mutually exclusive mine modes;
4. gives the add-on no authority to add ore veins, geodes, meteorites, extractors, or alternate resource-production paths.

The event rule is deliberately namespace-wide rather than tied to an unverified extractor registry name. It therefore fails closed for ordinary placement of the announced Iron, Gold, Diamond, and later extractor tiers.

## Limits

IOE does not unregister another mod's registry entries. If GeOre: Additions is installed, its items may still appear in creative tabs or recipe viewers even though its blocks cannot be placed and its budding-harvesting tool cannot recover a budding block. Complete removal from menus and registries requires not installing GeOre: Additions.

The restriction does not scan for or destructively remove add-on blocks already present in an existing world. It also cannot intercept commands, world generation, or direct programmatic block writes that bypass NeoForge's entity-placement event. Recipe removal is outside this pass because the official 4.0 artifact cannot currently be obtained through its advertised Curse Maven coordinate while third-party distribution is disabled.

The event behavior still requires GitHub-hosted CI or another explicitly approved runtime environment to verify exact NeoForge event cancellation against the official jar. No local build, Minecraft launch, jar download, or runtime validation was performed.

## Provenance boundary

GeOre: Additions 4.0 is listed as All Rights Reserved. This policy uses only the public mod id and public feature descriptions. No GeOre: Additions code, asset, recipe, data file, model, texture, or generated resource is copied, adapted, decompiled, or redistributed by IOE. See `THIRD_PARTY_NOTICES.md`.
