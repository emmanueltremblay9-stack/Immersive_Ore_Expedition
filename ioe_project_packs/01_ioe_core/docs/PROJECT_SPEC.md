# Project Spec — IOE Core

## Responsibility
This module owns shared concepts used by every other Immersive Ore Expedition sub-mod.

## Must implement
- Resource whitelist / blacklist policy.
- Runtime registry scanner for blocks, fluids, tags, and optional loaded mods.
- Shared data types: ResourceRef, ProvinceId, SiteQuality, ExpeditionAnchor, CrystalGrowthSiteType.
- Config loading primitives.
- Logging helpers for skipped resources.
- No direct worldgen beyond shared helpers.

## Must not implement
- No structures.
- No ore pocket generation.
- No GeOre placement.
- No AE2 meteorite placement.
- No IE/IP clue placement.

## Public API targets
- `ResourcePolicyService`
- `LoadedResourceScanner`
- `ExpeditionAnchorRef`
- `ProvinceRule`
- `SiteQualityRoll`
