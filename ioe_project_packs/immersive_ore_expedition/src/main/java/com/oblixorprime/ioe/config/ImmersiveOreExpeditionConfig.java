package com.oblixorprime.ioe.config;

import com.oblixorprime.ioe.retrogen.RetrogenMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ImmersiveOreExpeditionConfig {
    private static final boolean DEFAULT_CORE_EXISTING_RESOURCES_ONLY = true;
    private static final boolean DEFAULT_CORE_ALLOW_NEW_ORES = false;
    private static final boolean DEFAULT_CORE_SKIP_MISSING_RESOURCES = true;
    private static final boolean DEFAULT_CORE_LOG_MISSING_RESOURCES = true;
    private static final boolean DEFAULT_RESOURCE_POLICY_DEBUG_DIAGNOSTICS = false;
    private static final List<String> DEFAULT_RESOURCE_POLICY_ALLOWED_CATEGORIES = List.of(
            "vanilla",
            "immersive_engineering",
            "ae2",
            "geore",
            "draconic_evolution",
            "common_ore_tag"
    );
    private static final List<String> DEFAULT_RESOURCE_POLICY_DENIED_CATEGORIES = List.of();
    private static final List<String> DEFAULT_RESOURCE_POLICY_EXCLUDED_RESOURCES = List.of(
            "Apatite",
            "Tin",
            "Forestry Copper",
            "Platinum",
            "Osmium",
            "Tungsten",
            "Black Quartz",
            "Uraninite",
            "Monazite"
    );

    private static final double DEFAULT_WORLDGEN_RANDOM_ORE_DENSITY_MULTIPLIER = 0.03D;
    private static final boolean DEFAULT_WORLDGEN_REQUIRE_STRUCTURE_ANCHOR = true;
    private static final boolean DEFAULT_WORLDGEN_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES = true;
    private static final boolean DEFAULT_WORLDGEN_NATURAL_EXPEDITION_SITE_GENERATION_ENABLED = true;
    private static final boolean DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_ENABLED = false;
    private static final boolean DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_DIAGNOSTICS = false;
    private static final boolean DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_ENABLED = false;
    private static final boolean DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_DIAGNOSTICS = false;
    private static final boolean DEFAULT_WORLDGEN_COMPASS_SHOW_DIAGNOSTIC_SITES = false;
    private static final int DEFAULT_WORLDGEN_MIN_DISTANCE = 16;
    private static final int DEFAULT_WORLDGEN_MAX_DISTANCE = 96;
    private static final boolean DEFAULT_WORLDGEN_REQUIRE_TUNNEL_CONNECTION = true;
    private static final String DEFAULT_WORLDGEN_PROVINCE_NAMESPACE = "immersive_ore_expedition";
    private static final boolean DEFAULT_WORLDGEN_ALLOW_LEGACY_PROVINCE_NAMESPACES = false;
    private static final boolean DEFAULT_WORLDGEN_PROVINCE_RUNTIME_INTEGRATION_ENABLED = false;
    private static final String DEFAULT_WORLDGEN_DEFAULT_PROVINCE = "immersive_ore_expedition:default";
    private static final List<String> DEFAULT_WORLDGEN_BIOME_PROVINCE_BINDINGS = List.of();
    private static final List<String> DEFAULT_WORLDGEN_PROVINCE_RESOURCE_POLICY_RULES = List.of();
    private static final boolean DEFAULT_WORLDGEN_PROVINCE_DEBUG_DIAGNOSTICS = false;
    private static final List<String> DEFAULT_WORLDGEN_PROVINCE_ALLOW_BIOMES = List.of();
    private static final List<String> DEFAULT_WORLDGEN_PROVINCE_DENY_BIOMES = List.of();
    private static final List<String> DEFAULT_WORLDGEN_PROVINCE_EXCLUDE_BIOMES = List.of();

    private static final boolean DEFAULT_CRYSTAL_ENABLED = true;
    private static final boolean DEFAULT_CRYSTAL_REQUIRE_STRUCTURE_ANCHOR = true;
    private static final boolean DEFAULT_CRYSTAL_ALLOW_RANDOM_FREE_CRYSTAL_SITES = false;
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
    private static final boolean DEFAULT_GEORE_EXISTING_RESOURCES_ONLY = true;
    private static final boolean DEFAULT_SKIP_MISSING_GEORES = true;
    private static final boolean DEFAULT_METEORITIC_GEODE_ENABLED = true;
    private static final boolean DEFAULT_METEORITIC_GEODE_REQUIRES_AE2 = true;

    private static final boolean DEFAULT_NETHER_ENABLED = true;
    private static final boolean DEFAULT_REQUIRE_GIANT_LAVA_LAKE_ABOVE = true;
    private static final boolean DEFAULT_ALLOW_RANDOM_NETHER_GEODES = false;
    private static final int DEFAULT_LAVA_SAMPLE_RADIUS = 64;
    private static final double DEFAULT_MINIMUM_LAVA_COVERAGE = 0.60D;
    private static final int DEFAULT_MINIMUM_LAVA_DEPTH = 4;
    private static final int DEFAULT_MIN_BLOCKS_BELOW_LAVA = 8;
    private static final int DEFAULT_MAX_BLOCKS_BELOW_LAVA = 48;
    private static final boolean DEFAULT_REQUIRE_SAFE_CRUST = true;
    private static final boolean DEFAULT_NETHER_QUARTZ = true;
    private static final boolean DEFAULT_ANCIENT_DEBRIS_EXTREME_RARE = true;
    private static final double DEFAULT_ANCIENT_DEBRIS_MOTHERLODE_CHANCE = 0.005D;
    private static final boolean DEFAULT_CLUE_STRUCTURES_ENABLED = true;

    private static final boolean DEFAULT_IE_ENABLED_IF_LOADED = true;
    private static final boolean DEFAULT_REDUCE_MINERAL_DEPOSIT_QUANTITY = true;
    private static final double DEFAULT_DEPOSIT_QUANTITY_MULTIPLIER = 0.10D;
    private static final double DEFAULT_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER = 0.05D;
    private static final boolean DEFAULT_RENDER_FULL_DEPOSIT = false;
    private static final boolean DEFAULT_CREATE_UNDERGROUND_VISUAL_PROXY = false;
    private static final boolean DEFAULT_SURFACE_OUTCROPS_ENABLED = true;
    private static final int DEFAULT_BOULDER_COUNT_MIN = 1;
    private static final int DEFAULT_BOULDER_COUNT_MAX = 5;
    private static final boolean DEFAULT_USE_DEPOSIT_PRESENT_RESOURCES_ONLY = true;
    private static final int DEFAULT_FREE_ORE_REWARD_LIMIT_BLOCKS = 3;
    private static final boolean DEFAULT_IP_ENABLED_IF_LOADED = true;
    private static final boolean DEFAULT_RENDER_FULL_RESERVOIR = false;
    private static final boolean DEFAULT_CREATE_UNDERGROUND_RESERVOIR_PROXY = false;
    private static final boolean DEFAULT_SURFACE_SEEPS_ENABLED = true;
    private static final boolean DEFAULT_SMALL_SURFACE_POCKET_LAKES = true;
    private static final int DEFAULT_MAX_SURFACE_FLUID_BLOCKS = 12;
    private static final boolean DEFAULT_VENT_FOR_GAS_LIKE_RESERVOIRS = true;

    private static final boolean DEFAULT_RETROGEN_ENABLED = false;
    private static final String DEFAULT_RETROGEN_MODE = "unexplored_chunks_only";
    private static final boolean DEFAULT_RETROGEN_REQUIRE_ADMIN_COMMAND = true;
    private static final int DEFAULT_RETROGEN_CHUNK_MARKER_VERSION = 1;
    private static final int DEFAULT_RETROGEN_MAX_CHUNKS_PER_TICK = 1;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue CORE_EXISTING_RESOURCES_ONLY = BUILDER
            .comment("Require IOE resources to resolve from loaded registries or tags before use.")
            .define("resourcePolicy.existingResourcesOnly", DEFAULT_CORE_EXISTING_RESOURCES_ONLY);
    private static final ModConfigSpec.BooleanValue CORE_ALLOW_NEW_ORES = BUILDER
            .comment("IOE must not register or approve synthetic ore resources by default.")
            .define("resourcePolicy.allowNewOres", DEFAULT_CORE_ALLOW_NEW_ORES);
    private static final ModConfigSpec.BooleanValue CORE_SKIP_MISSING_RESOURCES = BUILDER
            .comment("Skip missing approved resources instead of substituting fallback resources.")
            .define("resourcePolicy.skipMissingResources", DEFAULT_CORE_SKIP_MISSING_RESOURCES);
    private static final ModConfigSpec.BooleanValue CORE_LOG_MISSING_RESOURCES = BUILDER
            .comment("Log skipped missing resources so pack configuration mistakes are visible.")
            .define("resourcePolicy.logMissingResources", DEFAULT_CORE_LOG_MISSING_RESOURCES);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RESOURCE_POLICY_ALLOWED_CATEGORIES = BUILDER
            .comment("Resource categories that province rules may use. No blocks or items are registered from this list.")
            .defineList("resourcePolicy.allowedCategories",
                    DEFAULT_RESOURCE_POLICY_ALLOWED_CATEGORIES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RESOURCE_POLICY_DENIED_CATEGORIES = BUILDER
            .comment("Resource categories that province rules must reject even if an individual resource is otherwise valid.")
            .defineList("resourcePolicy.deniedCategories",
                    DEFAULT_RESOURCE_POLICY_DENIED_CATEGORIES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RESOURCE_POLICY_EXCLUDED_RESOURCES = BUILDER
            .comment("Strict resource exclusions preserved by Province System v1.")
            .defineList("resourcePolicy.excludedResources",
                    DEFAULT_RESOURCE_POLICY_EXCLUDED_RESOURCES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.BooleanValue RESOURCE_POLICY_DEBUG_DIAGNOSTICS = BUILDER
            .comment("Emit diagnostic resource policy lines when a caller chooses to log province checks.")
            .define("resourcePolicy.debugDiagnostics", DEFAULT_RESOURCE_POLICY_DEBUG_DIAGNOSTICS);

    private static final ModConfigSpec.DoubleValue WORLDGEN_RANDOM_ORE_DENSITY_MULTIPLIER = BUILDER
            .comment("Multiplier applied by future ore-suppression hooks to non-expedition useful ore placement.")
            .defineInRange("worldgen.global.randomOreDensityMultiplier",
                    DEFAULT_WORLDGEN_RANDOM_ORE_DENSITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue WORLDGEN_REQUIRE_STRUCTURE_ANCHOR_FOR_MAJOR_ORE_LOADS = BUILDER
            .comment("Require major ore loads to be tied to an expedition anchor.")
            .define("worldgen.global.requireStructureAnchorForMajorOreLoads",
                    DEFAULT_WORLDGEN_REQUIRE_STRUCTURE_ANCHOR);
    private static final ModConfigSpec.BooleanValue WORLDGEN_ALLOW_TINY_SCRAP_ORE_OUTSIDE_PROVINCES = BUILDER
            .comment("Allow future hooks to leave tiny scrap ore outside full expedition provinces.")
            .define("worldgen.global.allowTinyScrapOreOutsideProvinces",
                    DEFAULT_WORLDGEN_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES);
    private static final ModConfigSpec.BooleanValue WORLDGEN_NATURAL_EXPEDITION_SITE_GENERATION_ENABLED = BUILDER
            .comment("Generate connected expedition sites in new chunks: a surface clue, mineshaft connector, and ore-load chamber.")
            .define("worldgen.global.naturalExpeditionSiteGenerationEnabled",
                    DEFAULT_WORLDGEN_NATURAL_EXPEDITION_SITE_GENERATION_ENABLED);
    private static final ModConfigSpec.BooleanValue WORLDGEN_RUNTIME_PLACEMENT_ENABLED = BUILDER
            .comment("Enable IOE runtime placement proof hooks. Default false keeps worldgen placement no-op.")
            .define("worldgen.runtimePlacementEnabled", DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_ENABLED);
    private static final ModConfigSpec.BooleanValue WORLDGEN_RUNTIME_PLACEMENT_DIAGNOSTICS = BUILDER
            .comment("Emit opt-in diagnostics for runtime placement proof decisions.")
            .define("worldgen.runtimePlacementDiagnostics", DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_DIAGNOSTICS);
    private static final ModConfigSpec.BooleanValue WORLDGEN_RUNTIME_PROOF_FEATURE_ENABLED = BUILDER
            .comment("Enable the registered runtime proof feature bridge diagnostics. Biome-invoked proof features remain non-placing; ore loads require explicit anchor/chamber planning.")
            .define("worldgen.runtimeProofFeatureEnabled", DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_ENABLED);
    private static final ModConfigSpec.BooleanValue WORLDGEN_RUNTIME_PROOF_FEATURE_DIAGNOSTICS = BUILDER
            .comment("Emit opt-in diagnostics for the v19 runtime proof feature bridge.")
            .define("worldgen.runtimeProofFeatureDiagnostics", DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_DIAGNOSTICS);
    private static final ModConfigSpec.BooleanValue WORLDGEN_COMPASS_SHOW_DIAGNOSTIC_SITES = BUILDER
            .comment("Show planned/debug/unplaced expedition sites in the Compass with explicit diagnostic badges.")
            .define("worldgen.compass.showDiagnosticSites", DEFAULT_WORLDGEN_COMPASS_SHOW_DIAGNOSTIC_SITES);
    private static final ModConfigSpec.IntValue WORLDGEN_ORE_LOAD_MIN_DISTANCE_FROM_ANCHOR = BUILDER
            .comment("Minimum Manhattan distance from an expedition anchor to a planned ore-load chamber.")
            .defineInRange("worldgen.anchorRules.oreLoadMinDistanceFromAnchor", DEFAULT_WORLDGEN_MIN_DISTANCE, 1, 512);
    private static final ModConfigSpec.IntValue WORLDGEN_ORE_LOAD_MAX_DISTANCE_FROM_ANCHOR = BUILDER
            .comment("Maximum Manhattan distance from an expedition anchor to a planned ore-load chamber.")
            .defineInRange("worldgen.anchorRules.oreLoadMaxDistanceFromAnchor", DEFAULT_WORLDGEN_MAX_DISTANCE, 1, 1024);
    private static final ModConfigSpec.BooleanValue WORLDGEN_REQUIRE_TUNNEL_CONNECTION = BUILDER
            .comment("Require future generated ore loads to be connected to the expedition route.")
            .define("worldgen.anchorRules.requireTunnelConnection", DEFAULT_WORLDGEN_REQUIRE_TUNNEL_CONNECTION);
    private static final ModConfigSpec.ConfigValue<String> WORLDGEN_PROVINCE_NAMESPACE = BUILDER
            .comment("Namespace used by new IOE province ids.")
            .define("worldgen.provinces.namespace", DEFAULT_WORLDGEN_PROVINCE_NAMESPACE);
    private static final ModConfigSpec.BooleanValue WORLDGEN_ALLOW_LEGACY_PROVINCE_NAMESPACES = BUILDER
            .comment("Allow explicitly documented old split IOE namespaces while reading legacy province references.")
            .define("worldgen.provinces.allowLegacyNamespaces", DEFAULT_WORLDGEN_ALLOW_LEGACY_PROVINCE_NAMESPACES);
    private static final ModConfigSpec.BooleanValue WORLDGEN_PROVINCE_RUNTIME_INTEGRATION_ENABLED = BUILDER
            .comment("Enable Province System runtime decisions for ore-load planning. Default false preserves the existing planning path.")
            .define("worldgen.provinces.runtimeIntegrationEnabled",
                    DEFAULT_WORLDGEN_PROVINCE_RUNTIME_INTEGRATION_ENABLED);
    private static final ModConfigSpec.ConfigValue<String> WORLDGEN_DEFAULT_PROVINCE = BUILDER
            .comment("Default province id used when no biome binding matches. No content is generated from this id.")
            .define("worldgen.provinces.defaultProvince", DEFAULT_WORLDGEN_DEFAULT_PROVINCE);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_BIOME_PROVINCE_BINDINGS = BUILDER
            .comment("Biome province bindings as biome_selector=province_id. Supports exact biome ids and namespace:* selectors.")
            .defineList("worldgen.provinces.biomeProvinceBindings",
                    DEFAULT_WORLDGEN_BIOME_PROVINCE_BINDINGS, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_PROVINCE_RESOURCE_POLICY_RULES =
            BUILDER.comment("Per-province resource policy rules as province_id|resource_selector|decision; selectors are exact resource ids or namespace:* wildcards, and decisions are allow, deny, or exclude. Empty preserves Province v1 resource behavior.")
                    .defineList("worldgen.provinces.resourcePolicyRules",
                            DEFAULT_WORLDGEN_PROVINCE_RESOURCE_POLICY_RULES,
                            ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_PROVINCE_ALLOW_BIOMES = BUILDER
            .comment("Default biome id/tag allow list for province matching. Empty means rules decide locally.")
            .defineList("worldgen.provinces.allowBiomes",
                    DEFAULT_WORLDGEN_PROVINCE_ALLOW_BIOMES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_PROVINCE_DENY_BIOMES = BUILDER
            .comment("Default biome id/tag deny list for province matching.")
            .defineList("worldgen.provinces.denyBiomes",
                    DEFAULT_WORLDGEN_PROVINCE_DENY_BIOMES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_PROVINCE_EXCLUDE_BIOMES = BUILDER
            .comment("Default hard biome id/tag exclusion list for province matching.")
            .defineList("worldgen.provinces.excludeBiomes",
                    DEFAULT_WORLDGEN_PROVINCE_EXCLUDE_BIOMES, ImmersiveOreExpeditionConfig::isNonBlankString);
    private static final ModConfigSpec.BooleanValue WORLDGEN_PROVINCE_DEBUG_DIAGNOSTICS = BUILDER
            .comment("Emit diagnostic province/biome match lines when a caller chooses to log province checks.")
            .define("worldgen.provinces.debugDiagnostics", DEFAULT_WORLDGEN_PROVINCE_DEBUG_DIAGNOSTICS);
    private static final ModConfigSpec.BooleanValue WORLDGEN_TINY_VERTICAL_MINE_ENTRANCE =
            worldgenStructure("tinyVerticalMineEntrance");
    private static final ModConfigSpec.BooleanValue WORLDGEN_COLLAPSED_SHAFT = worldgenStructure("collapsedShaft");
    private static final ModConfigSpec.BooleanValue WORLDGEN_MINER_CAMP = worldgenStructure("minerCamp");
    private static final ModConfigSpec.BooleanValue WORLDGEN_BURIED_SURVEY_MARKER =
            worldgenStructure("buriedSurveyMarker");
    private static final ModConfigSpec.BooleanValue WORLDGEN_BASIC_MINESHAFT_CONNECTOR =
            worldgenStructure("basicMineshaftConnector");
    private static final ModConfigSpec.BooleanValue WORLDGEN_ORE_LOAD_CHAMBER = worldgenStructure("oreLoadChamber");

    private static final ModConfigSpec.BooleanValue CRYSTAL_ENABLED = BUILDER
            .comment("Enable IOE crystal-site placement planning; AE2 and AE2 Crystal Science retain crystal processing authority.")
            .define("crystalGrowth.enabled", DEFAULT_CRYSTAL_ENABLED);
    private static final ModConfigSpec.BooleanValue CRYSTAL_REQUIRE_STRUCTURE_ANCHOR = BUILDER
            .comment("Require IOE-placed crystal sites to be tied to an expedition anchor.")
            .define("crystalGrowth.requireStructureAnchor", DEFAULT_CRYSTAL_REQUIRE_STRUCTURE_ANCHOR);
    private static final ModConfigSpec.BooleanValue CRYSTAL_ALLOW_RANDOM_FREE_CRYSTAL_SITES = BUILDER
            .comment("Keep false to prevent random free crystal caves outside IOE anchors.")
            .define("crystalGrowth.allowRandomFreeCrystalSites", DEFAULT_CRYSTAL_ALLOW_RANDOM_FREE_CRYSTAL_SITES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AMETHYST_STRUCTURE_ANCHORED_SITES = BUILDER
            .comment("Enable structure-anchored vanilla amethyst growth chamber plans.")
            .define("crystalGrowth.amethyst.structureAnchoredSites", DEFAULT_AMETHYST_STRUCTURE_ANCHORED_SITES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AMETHYST_METEORITE_WRAPPED_VARIANT = BUILDER
            .comment("Allow amethyst cores to be used in future meteoritic geode plans.")
            .define("crystalGrowth.amethyst.meteoriteWrappedVariant", DEFAULT_AMETHYST_METEORITE_WRAPPED_VARIANT);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_ENABLED_IF_LOADED = BUILDER
            .comment("Enable AE2 site planning only when the ae2 mod is loaded.")
            .define("crystalGrowth.ae2.enabledIfLoaded", DEFAULT_AE2_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_SURFACE_METEORITES = BUILDER
            .comment("Do not add random surface AE2 meteorites from this module.")
            .define("crystalGrowth.ae2.surfaceMeteorites", DEFAULT_AE2_SURFACE_METEORITES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_BURIED_METEORITES = BUILDER
            .comment("Allow buried AE2 Certus/meteorite site plans when the required AE2 and AE2 Crystal Science stack is loaded.")
            .define("crystalGrowth.ae2.buriedMeteorites", DEFAULT_AE2_BURIED_METEORITES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_ALLOW_BUDDING_CERTUS_SITES = BUILDER
            .comment("Allow supplied budding Certus resources to pass site planning when present and approved.")
            .define("crystalGrowth.ae2.allowBuddingCertusSites", DEFAULT_AE2_ALLOW_BUDDING_CERTUS_SITES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_ALLOW_FLUIX_ORE_GENERATION = BUILDER
            .comment("Hard-off switch; IOE does not generate fake Fluix ore.")
            .define("crystalGrowth.ae2.allowFluixOreGeneration", DEFAULT_AE2_ALLOW_FLUIX_ORE_GENERATION);
    private static final ModConfigSpec.BooleanValue CRYSTAL_AE2_SKY_STONE_CRUST_AROUND_GEODES = BUILDER
            .comment("Require a supplied and loaded AE2 sky-stone crust resource for meteoritic AE2 geodes.")
            .define("crystalGrowth.ae2.skyStoneCrustAroundGeodes", DEFAULT_AE2_SKY_STONE_CRUST_AROUND_GEODES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_GEORE_ENABLED_IF_LOADED = BUILDER
            .comment("Enable GeOre site planning only when the geore mod is loaded.")
            .define("crystalGrowth.geore.enabledIfLoaded", DEFAULT_GEORE_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue CRYSTAL_DISABLE_FREE_GEORE_WORLDGEN = BUILDER
            .comment("Plan to disable or avoid free/random GeOre generation when GeOre is loaded.")
            .define("crystalGrowth.geore.disableFreeGeoreWorldgen", DEFAULT_DISABLE_FREE_GEORE_WORLDGEN);
    private static final ModConfigSpec.BooleanValue CRYSTAL_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES = BUILDER
            .comment("Require all planned GeOre resources to be attached to IOE expedition anchors.")
            .define("crystalGrowth.geore.anchorAllGeoresToExpeditionStructures",
                    DEFAULT_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_GEORE_EXISTING_RESOURCES_ONLY = BUILDER
            .comment("Only plan GeOre sites from loaded resources; do not invent variants.")
            .define("crystalGrowth.geore.existingResourcesOnly", DEFAULT_GEORE_EXISTING_RESOURCES_ONLY);
    private static final ModConfigSpec.BooleanValue CRYSTAL_SKIP_MISSING_GEORES = BUILDER
            .comment("Skip missing GeOre resources instead of substituting fallbacks.")
            .define("crystalGrowth.geore.skipMissingGeores", DEFAULT_SKIP_MISSING_GEORES);
    private static final ModConfigSpec.BooleanValue CRYSTAL_METEORITIC_GEODE_ENABLED = BUILDER
            .comment("Enable meteoritic geode plans from supplied amethyst, AE2 Certus, or loaded GeOre cores.")
            .define("crystalGrowth.meteoriticGeode.enabled", DEFAULT_METEORITIC_GEODE_ENABLED);
    private static final ModConfigSpec.BooleanValue CRYSTAL_METEORITIC_GEODE_REQUIRES_AE2 = BUILDER
            .comment("Require AE2 for meteoritic geode plans because the outer crust is sky stone.")
            .define("crystalGrowth.meteoriticGeode.requiresAe2", DEFAULT_METEORITIC_GEODE_REQUIRES_AE2);

    private static final ModConfigSpec.BooleanValue NETHER_ENABLED = BUILDER
            .comment("Enable IOE sub-lava Nether geode planning.")
            .define("netherGeodes.subLavaGeodes.enabled", DEFAULT_NETHER_ENABLED);
    private static final ModConfigSpec.BooleanValue NETHER_REQUIRE_GIANT_LAVA_LAKE_ABOVE = BUILDER
            .comment("Require a validated giant Nether lava lake anchor above every geode.")
            .define("netherGeodes.subLavaGeodes.requireGiantLavaLakeAbove", DEFAULT_REQUIRE_GIANT_LAVA_LAKE_ABOVE);
    private static final ModConfigSpec.BooleanValue NETHER_ALLOW_RANDOM_NETHER_GEODES = BUILDER
            .comment("Keep false to prevent random geodes in ordinary netherrack.")
            .define("netherGeodes.subLavaGeodes.allowRandomNetherGeodes", DEFAULT_ALLOW_RANDOM_NETHER_GEODES);
    private static final ModConfigSpec.IntValue NETHER_LAVA_SAMPLE_RADIUS = BUILDER
            .comment("Minimum radius represented by lava lake sample reports.")
            .defineInRange("netherGeodes.lavaLakeAnchor.lavaSampleRadius", DEFAULT_LAVA_SAMPLE_RADIUS, 1, 512);
    private static final ModConfigSpec.DoubleValue NETHER_MINIMUM_LAVA_COVERAGE = BUILDER
            .comment("Minimum lava coverage ratio needed to treat a sample as a giant lake.")
            .defineInRange("netherGeodes.lavaLakeAnchor.minimumLavaCoverage",
                    DEFAULT_MINIMUM_LAVA_COVERAGE, 0.0D, 1.0D);
    private static final ModConfigSpec.IntValue NETHER_MINIMUM_LAVA_DEPTH = BUILDER
            .comment("Minimum observed lava depth needed to anchor sub-lava geodes.")
            .defineInRange("netherGeodes.lavaLakeAnchor.minimumLavaDepth", DEFAULT_MINIMUM_LAVA_DEPTH, 1, 64);
    private static final ModConfigSpec.IntValue NETHER_MIN_BLOCKS_BELOW_LAVA = BUILDER
            .comment("Minimum vertical distance below the lava body for a planned geode chamber.")
            .defineInRange("netherGeodes.placement.minBlocksBelowLava", DEFAULT_MIN_BLOCKS_BELOW_LAVA, 1, 256);
    private static final ModConfigSpec.IntValue NETHER_MAX_BLOCKS_BELOW_LAVA = BUILDER
            .comment("Maximum vertical distance below the lava body for a planned geode chamber.")
            .defineInRange("netherGeodes.placement.maxBlocksBelowLava", DEFAULT_MAX_BLOCKS_BELOW_LAVA, 1, 512);
    private static final ModConfigSpec.BooleanValue NETHER_REQUIRE_SAFE_CRUST = BUILDER
            .comment("Require future placement hooks to leave a safe shell/crust between lava and chamber.")
            .define("netherGeodes.placement.requireSafeCrust", DEFAULT_REQUIRE_SAFE_CRUST);
    private static final ModConfigSpec.BooleanValue NETHER_NETHER_QUARTZ = BUILDER
            .comment("Allow Nether Quartz as the primary sub-lava geode resource when loaded.")
            .define("netherGeodes.resources.netherQuartz", DEFAULT_NETHER_QUARTZ);
    private static final ModConfigSpec.BooleanValue NETHER_ANCIENT_DEBRIS_EXTREME_RARE = BUILDER
            .comment("Allow extremely rare ancient debris hearts only when the resource exists.")
            .define("netherGeodes.resources.ancientDebrisExtremeRare", DEFAULT_ANCIENT_DEBRIS_EXTREME_RARE);
    private static final ModConfigSpec.DoubleValue NETHER_ANCIENT_DEBRIS_MOTHERLODE_CHANCE = BUILDER
            .comment("Chance for an ancient debris heart in a valid planned sub-lava geode.")
            .defineInRange("netherGeodes.resources.ancientDebrisMotherlodeChance",
                    DEFAULT_ANCIENT_DEBRIS_MOTHERLODE_CHANCE, 0.0D, 0.05D);
    private static final ModConfigSpec.BooleanValue NETHER_CLUE_STRUCTURES_ENABLED = BUILDER
            .comment("Enable small Nether clue structure IDs near valid lava shore or edge anchors.")
            .define("netherGeodes.clues.enabled", DEFAULT_CLUE_STRUCTURES_ENABLED);

    private static final ModConfigSpec.BooleanValue IEIP_IE_ENABLED_IF_LOADED = BUILDER
            .comment("Enable Immersive Engineering clue policies only when immersiveengineering is loaded.")
            .define("ieipProspecting.immersiveEngineering.enabledIfLoaded", DEFAULT_IE_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue IEIP_REDUCE_MINERAL_DEPOSIT_QUANTITY = BUILDER
            .comment("Plan IE mineral deposit quantity reduction so the Excavator does not dominate IOE progression.")
            .define("ieipProspecting.immersiveEngineering.reduceMineralDepositQuantity",
                    DEFAULT_REDUCE_MINERAL_DEPOSIT_QUANTITY);
    private static final ModConfigSpec.DoubleValue IEIP_DEPOSIT_QUANTITY_MULTIPLIER = BUILDER
            .comment("Normal-mode multiplier for supported IE deposit quantity hooks.")
            .defineInRange("ieipProspecting.immersiveEngineering.depositQuantityMultiplier",
                    DEFAULT_DEPOSIT_QUANTITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.DoubleValue IEIP_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER = BUILDER
            .comment("Hard-mode multiplier for supported IE deposit quantity hooks.")
            .defineInRange("ieipProspecting.immersiveEngineering.hardModeDepositQuantityMultiplier",
                    DEFAULT_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue IEIP_RENDER_FULL_DEPOSIT = BUILDER
            .comment("Documented hard-off switch; full underground IE deposit rendering is outside this module.")
            .define("ieipProspecting.immersiveEngineering.renderFullDeposit", DEFAULT_RENDER_FULL_DEPOSIT);
    private static final ModConfigSpec.BooleanValue IEIP_CREATE_UNDERGROUND_VISUAL_PROXY = BUILDER
            .comment("Documented hard-off switch; underground IE visual proxies are outside this module.")
            .define("ieipProspecting.immersiveEngineering.createUndergroundVisualProxy",
                    DEFAULT_CREATE_UNDERGROUND_VISUAL_PROXY);
    private static final ModConfigSpec.BooleanValue IEIP_SURFACE_OUTCROPS_ENABLED = BUILDER
            .comment("Enable small surface mineral outcrop clues for IE deposits.")
            .define("ieipProspecting.immersiveEngineering.surfaceOutcrops.enabled",
                    DEFAULT_SURFACE_OUTCROPS_ENABLED);
    private static final ModConfigSpec.IntValue IEIP_BOULDER_COUNT_MIN = BUILDER
            .comment("Minimum boulder count in an IE outcrop clue plan.")
            .defineInRange("ieipProspecting.immersiveEngineering.surfaceOutcrops.boulderCountMin",
                    DEFAULT_BOULDER_COUNT_MIN, 0, 64);
    private static final ModConfigSpec.IntValue IEIP_BOULDER_COUNT_MAX = BUILDER
            .comment("Maximum boulder count in an IE outcrop clue plan.")
            .defineInRange("ieipProspecting.immersiveEngineering.surfaceOutcrops.boulderCountMax",
                    DEFAULT_BOULDER_COUNT_MAX, 0, 64);
    private static final ModConfigSpec.BooleanValue IEIP_USE_DEPOSIT_PRESENT_RESOURCES_ONLY = BUILDER
            .comment("Outcrop clues must use resources reported by the IE deposit; no fallback ores are invented.")
            .define("ieipProspecting.immersiveEngineering.surfaceOutcrops.useDepositPresentResourcesOnly",
                    DEFAULT_USE_DEPOSIT_PRESENT_RESOURCES_ONLY);
    private static final ModConfigSpec.IntValue IEIP_FREE_ORE_REWARD_LIMIT_BLOCKS = BUILDER
            .comment("Maximum loose ore/value blocks an outcrop clue may expose before future placement hooks stop.")
            .defineInRange("ieipProspecting.immersiveEngineering.surfaceOutcrops.freeOreRewardLimitBlocks",
                    DEFAULT_FREE_ORE_REWARD_LIMIT_BLOCKS, 0, 64);
    private static final ModConfigSpec.BooleanValue IEIP_IP_ENABLED_IF_LOADED = BUILDER
            .comment("Enable Immersive Petroleum clue policies only when immersivepetroleum is loaded.")
            .define("ieipProspecting.immersivePetroleum.enabledIfLoaded", DEFAULT_IP_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue IEIP_RENDER_FULL_RESERVOIR = BUILDER
            .comment("Documented hard-off switch; full underground IP reservoir rendering is outside this module.")
            .define("ieipProspecting.immersivePetroleum.renderFullReservoir", DEFAULT_RENDER_FULL_RESERVOIR);
    private static final ModConfigSpec.BooleanValue IEIP_CREATE_UNDERGROUND_RESERVOIR_PROXY = BUILDER
            .comment("Documented hard-off switch; underground IP visual proxies are outside this module.")
            .define("ieipProspecting.immersivePetroleum.createUndergroundVisualProxy",
                    DEFAULT_CREATE_UNDERGROUND_RESERVOIR_PROXY);
    private static final ModConfigSpec.BooleanValue IEIP_SURFACE_SEEPS_ENABLED = BUILDER
            .comment("Enable small surface seep, pocket-lake, or vent clue plans for IP reservoirs.")
            .define("ieipProspecting.immersivePetroleum.surfaceSeeps.enabled", DEFAULT_SURFACE_SEEPS_ENABLED);
    private static final ModConfigSpec.BooleanValue IEIP_SMALL_SURFACE_POCKET_LAKES = BUILDER
            .comment("Allow liquid reservoir clues to plan small surface pocket lakes.")
            .define("ieipProspecting.immersivePetroleum.surfaceSeeps.smallSurfacePocketLakes",
                    DEFAULT_SMALL_SURFACE_POCKET_LAKES);
    private static final ModConfigSpec.IntValue IEIP_MAX_SURFACE_FLUID_BLOCKS = BUILDER
            .comment("Maximum surface fluid blocks a future IP seep placement hook may expose.")
            .defineInRange("ieipProspecting.immersivePetroleum.surfaceSeeps.maxSurfaceFluidBlocks",
                    DEFAULT_MAX_SURFACE_FLUID_BLOCKS, 0, 64);
    private static final ModConfigSpec.BooleanValue IEIP_VENT_FOR_GAS_LIKE_RESERVOIRS = BUILDER
            .comment("Plan vents instead of pocket lakes for gas-like reservoir fluids.")
            .define("ieipProspecting.immersivePetroleum.surfaceSeeps.ventForGasLikeReservoirs",
                    DEFAULT_VENT_FOR_GAS_LIKE_RESERVOIRS);

    private static final ModConfigSpec.BooleanValue RETROGEN_ENABLED = BUILDER
            .comment("Retrogen is disabled by default and must be explicitly enabled by server configuration.")
            .define("retrogen.enabled", DEFAULT_RETROGEN_ENABLED);
    private static final ModConfigSpec.ConfigValue<String> RETROGEN_DEFAULT_MODE_VALUE = BUILDER
            .comment("Default retrogen mode: off, unexplored_chunks_only, admin_radius, ore_pocket_only, or clue_plus_pocket.")
            .define("retrogen.defaultMode", DEFAULT_RETROGEN_MODE);
    private static final ModConfigSpec.BooleanValue RETROGEN_REQUIRE_ADMIN_COMMAND = BUILDER
            .comment("Require permission level 2 for IOE admin commands.")
            .define("retrogen.requireAdminCommand", DEFAULT_RETROGEN_REQUIRE_ADMIN_COMMAND);
    private static final ModConfigSpec.IntValue RETROGEN_CHUNK_MARKER_VERSION = BUILDER
            .comment("Chunk marker version used to prevent repeated retrogen in the same chunk.")
            .defineInRange("retrogen.chunkMarkerVersion", DEFAULT_RETROGEN_CHUNK_MARKER_VERSION, 1, 1024);
    private static final ModConfigSpec.IntValue RETROGEN_MAX_CHUNKS_PER_TICK = BUILDER
            .comment("Maximum queued chunks processed per server tick once runtime generation is bound.")
            .defineInRange("retrogen.maxChunksPerTick", DEFAULT_RETROGEN_MAX_CHUNKS_PER_TICK, 1, 64);
    private static final ModConfigSpec.BooleanValue RETROGEN_MODE_ORE_POCKET_ONLY = BUILDER
            .comment("Allow admin retrogen requests that only restore ore pockets.")
            .define("retrogen.modes.orePocketOnly", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_MODE_CLUE_PLUS_POCKET = BUILDER
            .comment("Allow admin retrogen requests that restore clues and ore pockets.")
            .define("retrogen.modes.cluePlusPocket", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_MODE_FULL_PROVINCE = BUILDER
            .comment("Full province retrogen remains disabled by default for safety.")
            .define("retrogen.modes.fullProvince", false);
    private static final ModConfigSpec.BooleanValue RETROGEN_MODE_ADMIN_RADIUS_ONLY = BUILDER
            .comment("Limit retrogen to explicit admin radius requests.")
            .define("retrogen.modes.adminRadiusOnly", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_COMMAND_LOCATE_PROVINCE = BUILDER
            .define("retrogen.commands.locateProvince", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_COMMAND_LOCATE_ANCHOR = BUILDER
            .define("retrogen.commands.locateAnchor", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_COMMAND_RETROGEN_STATUS = BUILDER
            .define("retrogen.commands.retrogenStatus", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_COMMAND_RETROGEN_START = BUILDER
            .define("retrogen.commands.retrogenStart", true);
    private static final ModConfigSpec.BooleanValue RETROGEN_COMMAND_RETROGEN_PAUSE = BUILDER
            .define("retrogen.commands.retrogenPause", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ImmersiveOreExpeditionConfig() {
    }

    public static boolean coreExistingResourcesOnly() {
        return getOrDefault(CORE_EXISTING_RESOURCES_ONLY, DEFAULT_CORE_EXISTING_RESOURCES_ONLY);
    }

    public static boolean coreAllowNewOres() {
        return getOrDefault(CORE_ALLOW_NEW_ORES, DEFAULT_CORE_ALLOW_NEW_ORES);
    }

    public static boolean coreSkipMissingResources() {
        return getOrDefault(CORE_SKIP_MISSING_RESOURCES, DEFAULT_CORE_SKIP_MISSING_RESOURCES);
    }

    public static boolean coreLogMissingResources() {
        return getOrDefault(CORE_LOG_MISSING_RESOURCES, DEFAULT_CORE_LOG_MISSING_RESOURCES);
    }

    public static List<String> resourcePolicyAllowedCategories() {
        return getOrDefault(RESOURCE_POLICY_ALLOWED_CATEGORIES, DEFAULT_RESOURCE_POLICY_ALLOWED_CATEGORIES);
    }

    public static List<String> resourcePolicyDeniedCategories() {
        return getOrDefault(RESOURCE_POLICY_DENIED_CATEGORIES, DEFAULT_RESOURCE_POLICY_DENIED_CATEGORIES);
    }

    public static List<String> resourcePolicyExcludedResources() {
        return getOrDefault(RESOURCE_POLICY_EXCLUDED_RESOURCES, DEFAULT_RESOURCE_POLICY_EXCLUDED_RESOURCES);
    }

    public static boolean resourcePolicyDebugDiagnostics() {
        return getOrDefault(RESOURCE_POLICY_DEBUG_DIAGNOSTICS, DEFAULT_RESOURCE_POLICY_DEBUG_DIAGNOSTICS);
    }

    public static double worldgenRandomOreDensityMultiplier() {
        return getOrDefault(WORLDGEN_RANDOM_ORE_DENSITY_MULTIPLIER,
                DEFAULT_WORLDGEN_RANDOM_ORE_DENSITY_MULTIPLIER);
    }

    public static boolean worldgenRequireStructureAnchorForMajorOreLoads() {
        return getOrDefault(WORLDGEN_REQUIRE_STRUCTURE_ANCHOR_FOR_MAJOR_ORE_LOADS,
                DEFAULT_WORLDGEN_REQUIRE_STRUCTURE_ANCHOR);
    }

    public static boolean worldgenAllowTinyScrapOreOutsideProvinces() {
        return getOrDefault(WORLDGEN_ALLOW_TINY_SCRAP_ORE_OUTSIDE_PROVINCES,
                DEFAULT_WORLDGEN_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES);
    }

    public static boolean worldgenNaturalExpeditionSiteGenerationEnabled() {
        return getOrDefault(
                WORLDGEN_NATURAL_EXPEDITION_SITE_GENERATION_ENABLED,
                DEFAULT_WORLDGEN_NATURAL_EXPEDITION_SITE_GENERATION_ENABLED
        );
    }

    public static boolean worldgenRuntimePlacementEnabled() {
        return getOrDefault(WORLDGEN_RUNTIME_PLACEMENT_ENABLED, DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_ENABLED);
    }

    public static boolean worldgenRuntimePlacementDiagnostics() {
        return getOrDefault(WORLDGEN_RUNTIME_PLACEMENT_DIAGNOSTICS,
                DEFAULT_WORLDGEN_RUNTIME_PLACEMENT_DIAGNOSTICS);
    }

    public static boolean worldgenRuntimeProofFeatureEnabled() {
        return getOrDefault(WORLDGEN_RUNTIME_PROOF_FEATURE_ENABLED,
                DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_ENABLED);
    }

    public static boolean worldgenRuntimeProofFeatureDiagnostics() {
        return getOrDefault(WORLDGEN_RUNTIME_PROOF_FEATURE_DIAGNOSTICS,
                DEFAULT_WORLDGEN_RUNTIME_PROOF_FEATURE_DIAGNOSTICS);
    }

    public static boolean worldgenCompassShowDiagnosticSites() {
        return getOrDefault(WORLDGEN_COMPASS_SHOW_DIAGNOSTIC_SITES,
                DEFAULT_WORLDGEN_COMPASS_SHOW_DIAGNOSTIC_SITES);
    }

    public static int worldgenOreLoadMinDistanceFromAnchor() {
        return getOrDefault(WORLDGEN_ORE_LOAD_MIN_DISTANCE_FROM_ANCHOR, DEFAULT_WORLDGEN_MIN_DISTANCE);
    }

    public static int worldgenOreLoadMaxDistanceFromAnchor() {
        return getOrDefault(WORLDGEN_ORE_LOAD_MAX_DISTANCE_FROM_ANCHOR, DEFAULT_WORLDGEN_MAX_DISTANCE);
    }

    public static boolean worldgenRequireTunnelConnection() {
        return getOrDefault(WORLDGEN_REQUIRE_TUNNEL_CONNECTION, DEFAULT_WORLDGEN_REQUIRE_TUNNEL_CONNECTION);
    }

    public static String worldgenProvinceNamespace() {
        return getOrDefault(WORLDGEN_PROVINCE_NAMESPACE, DEFAULT_WORLDGEN_PROVINCE_NAMESPACE);
    }

    public static boolean worldgenAllowLegacyProvinceNamespaces() {
        return getOrDefault(WORLDGEN_ALLOW_LEGACY_PROVINCE_NAMESPACES,
                DEFAULT_WORLDGEN_ALLOW_LEGACY_PROVINCE_NAMESPACES);
    }

    public static boolean worldgenProvinceRuntimeIntegrationEnabled() {
        return getOrDefault(WORLDGEN_PROVINCE_RUNTIME_INTEGRATION_ENABLED,
                DEFAULT_WORLDGEN_PROVINCE_RUNTIME_INTEGRATION_ENABLED);
    }

    public static String worldgenDefaultProvince() {
        return getOrDefault(WORLDGEN_DEFAULT_PROVINCE, DEFAULT_WORLDGEN_DEFAULT_PROVINCE);
    }

    public static List<String> worldgenBiomeProvinceBindings() {
        return getOrDefault(WORLDGEN_BIOME_PROVINCE_BINDINGS, DEFAULT_WORLDGEN_BIOME_PROVINCE_BINDINGS);
    }

    public static List<String> worldgenProvinceResourcePolicyRules() {
        return getOrDefault(WORLDGEN_PROVINCE_RESOURCE_POLICY_RULES,
                DEFAULT_WORLDGEN_PROVINCE_RESOURCE_POLICY_RULES);
    }

    public static List<String> worldgenProvinceAllowBiomes() {
        return getOrDefault(WORLDGEN_PROVINCE_ALLOW_BIOMES, DEFAULT_WORLDGEN_PROVINCE_ALLOW_BIOMES);
    }

    public static List<String> worldgenProvinceDenyBiomes() {
        return getOrDefault(WORLDGEN_PROVINCE_DENY_BIOMES, DEFAULT_WORLDGEN_PROVINCE_DENY_BIOMES);
    }

    public static List<String> worldgenProvinceExcludeBiomes() {
        return getOrDefault(WORLDGEN_PROVINCE_EXCLUDE_BIOMES, DEFAULT_WORLDGEN_PROVINCE_EXCLUDE_BIOMES);
    }

    public static boolean worldgenProvinceDebugDiagnostics() {
        return getOrDefault(WORLDGEN_PROVINCE_DEBUG_DIAGNOSTICS,
                DEFAULT_WORLDGEN_PROVINCE_DEBUG_DIAGNOSTICS);
    }

    public static boolean worldgenTinyVerticalMineEntranceEnabled() {
        return getOrDefault(WORLDGEN_TINY_VERTICAL_MINE_ENTRANCE, true);
    }

    public static boolean worldgenCollapsedShaftEnabled() {
        return getOrDefault(WORLDGEN_COLLAPSED_SHAFT, true);
    }

    public static boolean worldgenMinerCampEnabled() {
        return getOrDefault(WORLDGEN_MINER_CAMP, true);
    }

    public static boolean worldgenBuriedSurveyMarkerEnabled() {
        return getOrDefault(WORLDGEN_BURIED_SURVEY_MARKER, true);
    }

    public static boolean worldgenBasicMineshaftConnectorEnabled() {
        return getOrDefault(WORLDGEN_BASIC_MINESHAFT_CONNECTOR, true);
    }

    public static boolean worldgenOreLoadChamberEnabled() {
        return getOrDefault(WORLDGEN_ORE_LOAD_CHAMBER, true);
    }

    public static boolean crystalEnabled() {
        return getOrDefault(CRYSTAL_ENABLED, DEFAULT_CRYSTAL_ENABLED);
    }

    public static boolean crystalRequireStructureAnchor() {
        return getOrDefault(CRYSTAL_REQUIRE_STRUCTURE_ANCHOR, DEFAULT_CRYSTAL_REQUIRE_STRUCTURE_ANCHOR);
    }

    public static boolean crystalAllowRandomFreeCrystalSites() {
        return getOrDefault(CRYSTAL_ALLOW_RANDOM_FREE_CRYSTAL_SITES,
                DEFAULT_CRYSTAL_ALLOW_RANDOM_FREE_CRYSTAL_SITES);
    }

    public static boolean crystalAmethystStructureAnchoredSites() {
        return getOrDefault(CRYSTAL_AMETHYST_STRUCTURE_ANCHORED_SITES,
                DEFAULT_AMETHYST_STRUCTURE_ANCHORED_SITES);
    }

    public static boolean crystalAmethystMeteoriteWrappedVariant() {
        return getOrDefault(CRYSTAL_AMETHYST_METEORITE_WRAPPED_VARIANT,
                DEFAULT_AMETHYST_METEORITE_WRAPPED_VARIANT);
    }

    public static boolean crystalAe2EnabledIfLoaded() {
        return getOrDefault(CRYSTAL_AE2_ENABLED_IF_LOADED, DEFAULT_AE2_ENABLED_IF_LOADED);
    }

    public static boolean crystalAe2SurfaceMeteorites() {
        return getOrDefault(CRYSTAL_AE2_SURFACE_METEORITES, DEFAULT_AE2_SURFACE_METEORITES);
    }

    public static boolean crystalBuriedMeteorites() {
        return getOrDefault(CRYSTAL_AE2_BURIED_METEORITES, DEFAULT_AE2_BURIED_METEORITES);
    }

    public static boolean crystalAllowBuddingCertusSites() {
        return getOrDefault(CRYSTAL_AE2_ALLOW_BUDDING_CERTUS_SITES,
                DEFAULT_AE2_ALLOW_BUDDING_CERTUS_SITES);
    }

    public static boolean crystalAllowFluixOreGeneration() {
        return getOrDefault(CRYSTAL_AE2_ALLOW_FLUIX_ORE_GENERATION, DEFAULT_AE2_ALLOW_FLUIX_ORE_GENERATION);
    }

    public static boolean crystalSkyStoneCrustAroundGeodes() {
        return getOrDefault(CRYSTAL_AE2_SKY_STONE_CRUST_AROUND_GEODES,
                DEFAULT_AE2_SKY_STONE_CRUST_AROUND_GEODES);
    }

    public static boolean crystalGeoreEnabledIfLoaded() {
        return getOrDefault(CRYSTAL_GEORE_ENABLED_IF_LOADED, DEFAULT_GEORE_ENABLED_IF_LOADED);
    }

    public static boolean crystalDisableFreeGeoreWorldgen() {
        return getOrDefault(CRYSTAL_DISABLE_FREE_GEORE_WORLDGEN, DEFAULT_DISABLE_FREE_GEORE_WORLDGEN);
    }

    public static boolean crystalAnchorAllGeoresToExpeditionStructures() {
        return getOrDefault(CRYSTAL_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES,
                DEFAULT_ANCHOR_ALL_GEORES_TO_EXPEDITION_STRUCTURES);
    }

    public static boolean crystalExistingResourcesOnly() {
        return getOrDefault(CRYSTAL_GEORE_EXISTING_RESOURCES_ONLY, DEFAULT_GEORE_EXISTING_RESOURCES_ONLY);
    }

    public static boolean crystalSkipMissingGeores() {
        return getOrDefault(CRYSTAL_SKIP_MISSING_GEORES, DEFAULT_SKIP_MISSING_GEORES);
    }

    public static boolean crystalMeteoriticGeodeEnabled() {
        return getOrDefault(CRYSTAL_METEORITIC_GEODE_ENABLED, DEFAULT_METEORITIC_GEODE_ENABLED);
    }

    public static boolean crystalMeteoriticGeodeRequiresAe2() {
        return getOrDefault(CRYSTAL_METEORITIC_GEODE_REQUIRES_AE2, DEFAULT_METEORITIC_GEODE_REQUIRES_AE2);
    }

    public static boolean netherEnabled() {
        return getOrDefault(NETHER_ENABLED, DEFAULT_NETHER_ENABLED);
    }

    public static boolean netherRequireGiantLavaLakeAbove() {
        return getOrDefault(NETHER_REQUIRE_GIANT_LAVA_LAKE_ABOVE, DEFAULT_REQUIRE_GIANT_LAVA_LAKE_ABOVE);
    }

    public static boolean netherAllowRandomNetherGeodes() {
        return getOrDefault(NETHER_ALLOW_RANDOM_NETHER_GEODES, DEFAULT_ALLOW_RANDOM_NETHER_GEODES);
    }

    public static int netherLavaSampleRadius() {
        return getOrDefault(NETHER_LAVA_SAMPLE_RADIUS, DEFAULT_LAVA_SAMPLE_RADIUS);
    }

    public static double netherMinimumLavaCoverage() {
        return getOrDefault(NETHER_MINIMUM_LAVA_COVERAGE, DEFAULT_MINIMUM_LAVA_COVERAGE);
    }

    public static int netherMinimumLavaDepth() {
        return getOrDefault(NETHER_MINIMUM_LAVA_DEPTH, DEFAULT_MINIMUM_LAVA_DEPTH);
    }

    public static int netherMinBlocksBelowLava() {
        return getOrDefault(NETHER_MIN_BLOCKS_BELOW_LAVA, DEFAULT_MIN_BLOCKS_BELOW_LAVA);
    }

    public static int netherMaxBlocksBelowLava() {
        return Math.max(netherMinBlocksBelowLava(),
                getOrDefault(NETHER_MAX_BLOCKS_BELOW_LAVA, DEFAULT_MAX_BLOCKS_BELOW_LAVA));
    }

    public static boolean netherRequireSafeCrust() {
        return getOrDefault(NETHER_REQUIRE_SAFE_CRUST, DEFAULT_REQUIRE_SAFE_CRUST);
    }

    public static boolean netherQuartz() {
        return getOrDefault(NETHER_NETHER_QUARTZ, DEFAULT_NETHER_QUARTZ);
    }

    public static boolean netherAncientDebrisExtremeRare() {
        return getOrDefault(NETHER_ANCIENT_DEBRIS_EXTREME_RARE, DEFAULT_ANCIENT_DEBRIS_EXTREME_RARE);
    }

    public static double netherAncientDebrisMotherlodeChance() {
        return getOrDefault(NETHER_ANCIENT_DEBRIS_MOTHERLODE_CHANCE, DEFAULT_ANCIENT_DEBRIS_MOTHERLODE_CHANCE);
    }

    public static boolean netherClueStructuresEnabled() {
        return getOrDefault(NETHER_CLUE_STRUCTURES_ENABLED, DEFAULT_CLUE_STRUCTURES_ENABLED);
    }

    public static boolean ieipImmersiveEngineeringEnabledIfLoaded() {
        return getOrDefault(IEIP_IE_ENABLED_IF_LOADED, DEFAULT_IE_ENABLED_IF_LOADED);
    }

    public static boolean ieipReduceMineralDepositQuantity() {
        return getOrDefault(IEIP_REDUCE_MINERAL_DEPOSIT_QUANTITY, DEFAULT_REDUCE_MINERAL_DEPOSIT_QUANTITY);
    }

    public static double ieipDepositQuantityMultiplier() {
        return getOrDefault(IEIP_DEPOSIT_QUANTITY_MULTIPLIER, DEFAULT_DEPOSIT_QUANTITY_MULTIPLIER);
    }

    public static double ieipHardModeDepositQuantityMultiplier() {
        return getOrDefault(IEIP_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER,
                DEFAULT_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER);
    }

    public static boolean ieipRenderFullDeposit() {
        return getOrDefault(IEIP_RENDER_FULL_DEPOSIT, DEFAULT_RENDER_FULL_DEPOSIT);
    }

    public static boolean ieipCreateUndergroundVisualProxy() {
        return getOrDefault(IEIP_CREATE_UNDERGROUND_VISUAL_PROXY, DEFAULT_CREATE_UNDERGROUND_VISUAL_PROXY);
    }

    public static boolean ieipSurfaceOutcropsEnabled() {
        return getOrDefault(IEIP_SURFACE_OUTCROPS_ENABLED, DEFAULT_SURFACE_OUTCROPS_ENABLED);
    }

    public static int ieipBoulderCountMin() {
        return getOrDefault(IEIP_BOULDER_COUNT_MIN, DEFAULT_BOULDER_COUNT_MIN);
    }

    public static int ieipBoulderCountMax() {
        return Math.max(ieipBoulderCountMin(), getOrDefault(IEIP_BOULDER_COUNT_MAX, DEFAULT_BOULDER_COUNT_MAX));
    }

    public static boolean ieipUseDepositPresentResourcesOnly() {
        return getOrDefault(IEIP_USE_DEPOSIT_PRESENT_RESOURCES_ONLY, DEFAULT_USE_DEPOSIT_PRESENT_RESOURCES_ONLY);
    }

    public static int ieipFreeOreRewardLimitBlocks() {
        return getOrDefault(IEIP_FREE_ORE_REWARD_LIMIT_BLOCKS, DEFAULT_FREE_ORE_REWARD_LIMIT_BLOCKS);
    }

    public static boolean ieipImmersivePetroleumEnabledIfLoaded() {
        return getOrDefault(IEIP_IP_ENABLED_IF_LOADED, DEFAULT_IP_ENABLED_IF_LOADED);
    }

    public static boolean ieipRenderFullReservoir() {
        return getOrDefault(IEIP_RENDER_FULL_RESERVOIR, DEFAULT_RENDER_FULL_RESERVOIR);
    }

    public static boolean ieipCreateUndergroundReservoirProxy() {
        return getOrDefault(IEIP_CREATE_UNDERGROUND_RESERVOIR_PROXY, DEFAULT_CREATE_UNDERGROUND_RESERVOIR_PROXY);
    }

    public static boolean ieipSurfaceSeepsEnabled() {
        return getOrDefault(IEIP_SURFACE_SEEPS_ENABLED, DEFAULT_SURFACE_SEEPS_ENABLED);
    }

    public static boolean ieipSmallSurfacePocketLakes() {
        return getOrDefault(IEIP_SMALL_SURFACE_POCKET_LAKES, DEFAULT_SMALL_SURFACE_POCKET_LAKES);
    }

    public static int ieipMaxSurfaceFluidBlocks() {
        return getOrDefault(IEIP_MAX_SURFACE_FLUID_BLOCKS, DEFAULT_MAX_SURFACE_FLUID_BLOCKS);
    }

    public static boolean ieipVentForGasLikeReservoirs() {
        return getOrDefault(IEIP_VENT_FOR_GAS_LIKE_RESERVOIRS, DEFAULT_VENT_FOR_GAS_LIKE_RESERVOIRS);
    }

    public static boolean retrogenEnabled() {
        return getOrDefault(RETROGEN_ENABLED, DEFAULT_RETROGEN_ENABLED);
    }

    public static RetrogenMode retrogenDefaultMode() {
        return RetrogenMode.fromConfig(getOrDefault(RETROGEN_DEFAULT_MODE_VALUE, DEFAULT_RETROGEN_MODE));
    }

    public static boolean retrogenRequireAdminCommand() {
        return getOrDefault(RETROGEN_REQUIRE_ADMIN_COMMAND, DEFAULT_RETROGEN_REQUIRE_ADMIN_COMMAND);
    }

    public static int retrogenChunkMarkerVersion() {
        return getOrDefault(RETROGEN_CHUNK_MARKER_VERSION, DEFAULT_RETROGEN_CHUNK_MARKER_VERSION);
    }

    public static int retrogenMaxChunksPerTick() {
        return getOrDefault(RETROGEN_MAX_CHUNKS_PER_TICK, DEFAULT_RETROGEN_MAX_CHUNKS_PER_TICK);
    }

    public static boolean retrogenModeAllowed(RetrogenMode mode) {
        return switch (mode) {
            case OFF, UNEXPLORED_CHUNKS_ONLY -> true;
            case ADMIN_RADIUS -> getOrDefault(RETROGEN_MODE_ADMIN_RADIUS_ONLY, true);
            case ORE_POCKET_ONLY -> getOrDefault(RETROGEN_MODE_ORE_POCKET_ONLY, true);
            case CLUE_PLUS_POCKET -> getOrDefault(RETROGEN_MODE_CLUE_PLUS_POCKET, true);
        };
    }

    public static boolean retrogenFullProvinceAllowed() {
        return getOrDefault(RETROGEN_MODE_FULL_PROVINCE, false);
    }

    public static boolean retrogenCommandLocateProvinceEnabled() {
        return getOrDefault(RETROGEN_COMMAND_LOCATE_PROVINCE, true);
    }

    public static boolean retrogenCommandLocateAnchorEnabled() {
        return getOrDefault(RETROGEN_COMMAND_LOCATE_ANCHOR, true);
    }

    public static boolean retrogenCommandRetrogenStatusEnabled() {
        return getOrDefault(RETROGEN_COMMAND_RETROGEN_STATUS, true);
    }

    public static boolean retrogenCommandRetrogenStartEnabled() {
        return getOrDefault(RETROGEN_COMMAND_RETROGEN_START, true);
    }

    public static boolean retrogenCommandRetrogenPauseEnabled() {
        return getOrDefault(RETROGEN_COMMAND_RETROGEN_PAUSE, true);
    }

    private static ModConfigSpec.BooleanValue worldgenStructure(String key) {
        return BUILDER.comment("Enable the " + key + " expedition structure catalog entry.")
                .define("worldgen.structures." + key, true);
    }

    private static boolean getOrDefault(ModConfigSpec.BooleanValue value, boolean defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }

    private static int getOrDefault(ModConfigSpec.IntValue value, int defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }

    private static double getOrDefault(ModConfigSpec.DoubleValue value, double defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }

    private static String getOrDefault(ModConfigSpec.ConfigValue<String> value, String defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }

    private static List<String> getOrDefault(ModConfigSpec.ConfigValue<List<? extends String>> value,
                                             List<String> defaultValue) {
        try {
            return value.get().stream().map(String::valueOf).toList();
        } catch (IllegalStateException ignored) {
            return List.copyOf(defaultValue);
        }
    }

    private static boolean isNonBlankString(Object value) {
        return value instanceof String string && !string.isBlank();
    }
}
