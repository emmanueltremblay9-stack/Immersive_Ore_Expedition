# GeOre compatibility contract

## Current world-generation rule

The biome-to-mineral rework does not place GeOre material blocks, budding hearts, ore nodes, or artificial geodes. Every productive IOE site is a structure-only clue backed by one abstract Immersive Engineering mineral deposit. Resource outputs come from the selected `immersiveengineering:mineral_mix` recipe through IE's Core Sample Drill and Excavator systems.

GeOre remains a separately distributed required dependency on the supported `6.2.2` line because other project compatibility surfaces still refer to its registered blocks and growth behavior. IOE's normal physical-resource suppression continues to remove autonomous GeOre Overworld `*_geode` placed features. The dormant `GeOreNodeIntegration` registry helper is not called by the active `ExpeditionSiteFeature` and is not a resource-acquisition path.

The `ae2:growth_acceleratable` tag overlay remains compatibility wiring for upstream-owned GeOre budding blocks. It neither generates nor places those blocks. If a future supported system supplies one through an authorized non-IOE path, GeOre and AE2 retain their registered growth and acceleration behavior.

## Ownership and provenance

- GeOre owns its blocks, growth, drops, recipes, models, textures, sounds, translations, and upstream geode implementation.
- Immersive Engineering owns the abstract mineral deposits used by the reworked IOE distribution.
- IOE owns its suppression policy, mineral recipes, biome mapping, reserve tiers, and transactional site-to-deposit link.
- No GeOre Java source, data, model, texture, sound, language file, loot table, or recipe is copied into IOE.

The inspected artifact is `GeOre-1.21.1-6.2.2.jar` (`maven.modrinth:Xw6zG9hl:OV3DOSul`). See `THIRD_PARTY_NOTICES.md` for preserved MIT attribution.

## GeOre: Additions boundary

The separate GeOre: Additions restriction remains a defensive compatibility policy for its budding-harvester and extractor capabilities. It does not turn dormant IOE node code back into an active placement path. See `GEORE_ADDITIONS_COMPATIBILITY_PLAN.md`.
