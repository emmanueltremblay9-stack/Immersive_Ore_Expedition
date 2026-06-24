package com.oblixorprime.ioe.crystalgrowth;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeCrystalGrowthConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_REQUIRE_STRUCTURE_ANCHOR = true;
    private static final boolean DEFAULT_ALLOW_RANDOM_FREE_CRYSTAL_SITES = false;
    private static final boolean DEFAULT_AMETHYST_STRUCTURE_ANCHORED_SITES = true;
    private static final boolean DEFAULT_AMETHYST_METEORITE_WRAPPED_VARIANT = true;
    private static final boolean DEFAULT_AE2_ENABLED_IF_LOADED = true;
    private static final boolean DEFAULT_AE2_SURFACE_METEORITES = false;
    private static final boolean DEFAULT_AE2_BURIED_METEORITES = true;
    private static final boolean DEFAULT_AE2_ALLOW_BUDDING_CERTUS_SITES = true;
    private static final boolean DEFAULT_AE2_ALLOW_FLUIX_ORE_GENERATION = false;
    private static final boolean DEFAULT_AE2_SKY_STONE_CRUST_AROUND_GEODES = true;
    private static final boolean DEFAULT_GEORE_ENABLED_IF_LOADED = true;
    private static final boolean DEFAULT_DISABLE_FREE_GEORE_WORLDGEN = true;
    private static final boolean DEFAULT_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES = true;
    private static final boolean DEFAULT_EXISTING_RESOURCES_ONLY = true;
    private static final boolean DEFAULT_SKIP_MISSING_GEORES = true;
    private static final boolean DEFAULT_METEORITIC_GEODE_ENABLED = true;
    private static final boolean DEFAULT_METEORITIC_GEODE_REQUIRES_AE2 = true;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable IOE crystal-growth planning services.")
            .define("crystalGrowth.enabled", DEFAULT_ENABLED);
    private static final ModConfigSpec.BooleanValue REQUIRE_STRUCTURE_ANCHOR = BUILDER
            .comment("Require crystal-growth sites to be tied to an expedition anchor.")
            .define("crystalGrowth.requireStructureAnchor", DEFAULT_REQUIRE_STRUCTURE_ANCHOR);
    private static final ModConfigSpec.BooleanValue ALLOW_RANDOM_FREE_CRYSTAL_SITES = BUILDER
            .comment("Keep false to prevent random free crystal caves outside IOE anchors.")
            .define("crystalGrowth.allowRandomFreeCrystalSites", DEFAULT_ALLOW_RANDOM_FREE_CRYSTAL_SITES);

    private static final ModConfigSpec.BooleanValue AMETHYST_STRUCTURE_ANCHORED_SITES = BUILDER
            .comment("Enable structure-anchored vanilla amethyst growth chamber plans.")
            .define("amethyst.structureAnchoredSites", DEFAULT_AMETHYST_STRUCTURE_ANCHORED_SITES);
    private static final ModConfigSpec.BooleanValue AMETHYST_METEORITE_WRAPPED_VARIANT = BUILDER
            .comment("Allow amethyst cores to be used in future meteoritic geode plans.")
            .define("amethyst.meteoriteWrappedVariant", DEFAULT_AMETHYST_METEORITE_WRAPPED_VARIANT);

    private static final ModConfigSpec.BooleanValue AE2_ENABLED_IF_LOADED = BUILDER
            .comment("Enable AE2 site planning only when the ae2 mod is loaded.")
            .define("ae2.enabledIfLoaded", DEFAULT_AE2_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue AE2_SURFACE_METEORITES = BUILDER
            .comment("Do not add random surface AE2 meteorites from this module.")
            .define("ae2.surfaceMeteorites", DEFAULT_AE2_SURFACE_METEORITES);
    private static final ModConfigSpec.BooleanValue AE2_BURIED_METEORITES = BUILDER
            .comment("Allow buried AE2 Certus/meteorite site plans when AE2 is loaded.")
            .define("ae2.buriedMeteorites", DEFAULT_AE2_BURIED_METEORITES);
    private static final ModConfigSpec.BooleanValue AE2_ALLOW_BUDDING_CERTUS_SITES = BUILDER
            .comment("Allow supplied budding Certus resources to pass site planning when present and approved.")
            .define("ae2.allowBuddingCertusSites", DEFAULT_AE2_ALLOW_BUDDING_CERTUS_SITES);
    private static final ModConfigSpec.BooleanValue AE2_ALLOW_FLUIX_ORE_GENERATION = BUILDER
            .comment("Hard-off switch; IOE does not generate fake Fluix ore.")
            .define("ae2.allowFluixOreGeneration", DEFAULT_AE2_ALLOW_FLUIX_ORE_GENERATION);
    private static final ModConfigSpec.BooleanValue AE2_SKY_STONE_CRUST_AROUND_GEODES = BUILDER
            .comment("Require a supplied and loaded AE2 sky-stone crust resource for meteoritic AE2 geodes.")
            .define("ae2.skyStoneCrustAroundGeodes", DEFAULT_AE2_SKY_STONE_CRUST_AROUND_GEODES);

    private static final ModConfigSpec.BooleanValue GEORE_ENABLED_IF_LOADED = BUILDER
            .comment("Enable GeOre site planning only when the geore mod is loaded.")
            .define("geore.enabledIfLoaded", DEFAULT_GEORE_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue DISABLE_FREE_GEORE_WORLDGEN = BUILDER
            .comment("Plan to disable or avoid free/random GeOre generation when GeOre is loaded.")
            .define("geore.disableFreeGeoreWorldgen", DEFAULT_DISABLE_FREE_GEORE_WORLDGEN);
    private static final ModConfigSpec.BooleanValue ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES = BUILDER
            .comment("Require all planned GeOre resources to be attached to IOE expedition anchors.")
            .define("geore.anchorAllGeoresToExpeditionStructures", DEFAULT_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES);
    private static final ModConfigSpec.BooleanValue EXISTING_RESOURCES_ONLY = BUILDER
            .comment("Only plan GeOre sites from loaded resources; do not invent variants.")
            .define("geore.existingResourcesOnly", DEFAULT_EXISTING_RESOURCES_ONLY);
    private static final ModConfigSpec.BooleanValue SKIP_MISSING_GEORES = BUILDER
            .comment("Skip missing GeOre resources instead of substituting fallbacks.")
            .define("geore.skipMissingGeores", DEFAULT_SKIP_MISSING_GEORES);

    private static final ModConfigSpec.BooleanValue METEORITIC_GEODE_ENABLED = BUILDER
            .comment("Enable meteoritic geode plans from supplied amethyst, AE2 Certus, or loaded GeOre cores.")
            .define("meteoriticGeode.enabled", DEFAULT_METEORITIC_GEODE_ENABLED);
    private static final ModConfigSpec.BooleanValue METEORITIC_GEODE_REQUIRES_AE2 = BUILDER
            .comment("Require AE2 for meteoritic geode plans because the outer crust is sky stone.")
            .define("meteoriticGeode.requiresAe2", DEFAULT_METEORITIC_GEODE_REQUIRES_AE2);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeCrystalGrowthConfig() {
    }

    public static boolean enabled() {
        return getOrDefault(ENABLED, DEFAULT_ENABLED);
    }

    public static boolean requireStructureAnchor() {
        return getOrDefault(REQUIRE_STRUCTURE_ANCHOR, DEFAULT_REQUIRE_STRUCTURE_ANCHOR);
    }

    public static boolean allowRandomFreeCrystalSites() {
        return getOrDefault(ALLOW_RANDOM_FREE_CRYSTAL_SITES, DEFAULT_ALLOW_RANDOM_FREE_CRYSTAL_SITES);
    }

    public static boolean amethystStructureAnchoredSites() {
        return getOrDefault(AMETHYST_STRUCTURE_ANCHORED_SITES, DEFAULT_AMETHYST_STRUCTURE_ANCHORED_SITES);
    }

    public static boolean amethystMeteoriteWrappedVariant() {
        return getOrDefault(AMETHYST_METEORITE_WRAPPED_VARIANT, DEFAULT_AMETHYST_METEORITE_WRAPPED_VARIANT);
    }

    public static boolean ae2EnabledIfLoaded() {
        return getOrDefault(AE2_ENABLED_IF_LOADED, DEFAULT_AE2_ENABLED_IF_LOADED);
    }

    public static boolean ae2SurfaceMeteorites() {
        return getOrDefault(AE2_SURFACE_METEORITES, DEFAULT_AE2_SURFACE_METEORITES);
    }

    public static boolean buriedMeteorites() {
        return getOrDefault(AE2_BURIED_METEORITES, DEFAULT_AE2_BURIED_METEORITES);
    }

    public static boolean allowBuddingCertusSites() {
        return getOrDefault(AE2_ALLOW_BUDDING_CERTUS_SITES, DEFAULT_AE2_ALLOW_BUDDING_CERTUS_SITES);
    }

    public static boolean allowFluixOreGeneration() {
        return getOrDefault(AE2_ALLOW_FLUIX_ORE_GENERATION, DEFAULT_AE2_ALLOW_FLUIX_ORE_GENERATION);
    }

    public static boolean skyStoneCrustAroundGeodes() {
        return getOrDefault(AE2_SKY_STONE_CRUST_AROUND_GEODES, DEFAULT_AE2_SKY_STONE_CRUST_AROUND_GEODES);
    }

    public static boolean georeEnabledIfLoaded() {
        return getOrDefault(GEORE_ENABLED_IF_LOADED, DEFAULT_GEORE_ENABLED_IF_LOADED);
    }

    public static boolean disableFreeGeoreWorldgen() {
        return getOrDefault(DISABLE_FREE_GEORE_WORLDGEN, DEFAULT_DISABLE_FREE_GEORE_WORLDGEN);
    }

    public static boolean anchorAllGeoresToExpeditionStructures() {
        return getOrDefault(ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES, DEFAULT_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES);
    }

    public static boolean existingResourcesOnly() {
        return getOrDefault(EXISTING_RESOURCES_ONLY, DEFAULT_EXISTING_RESOURCES_ONLY);
    }

    public static boolean skipMissingGeores() {
        return getOrDefault(SKIP_MISSING_GEORES, DEFAULT_SKIP_MISSING_GEORES);
    }

    public static boolean meteoriticGeodeEnabled() {
        return getOrDefault(METEORITIC_GEODE_ENABLED, DEFAULT_METEORITIC_GEODE_ENABLED);
    }

    public static boolean meteoriticGeodeRequiresAe2() {
        return getOrDefault(METEORITIC_GEODE_REQUIRES_AE2, DEFAULT_METEORITIC_GEODE_REQUIRES_AE2);
    }

    private static boolean getOrDefault(ModConfigSpec.BooleanValue value, boolean defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }
}
