package com.oblixorprime.ioe.worldgen;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeWorldgenConfig {
    private static final double DEFAULT_RANDOM_ORE_DENSITY_MULTIPLIER = 0.03D;
    private static final boolean DEFAULT_REQUIRE_STRUCTURE_ANCHOR = true;
    private static final boolean DEFAULT_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES = true;
    private static final int DEFAULT_MIN_DISTANCE = 16;
    private static final int DEFAULT_MAX_DISTANCE = 96;
    private static final boolean DEFAULT_REQUIRE_TUNNEL_CONNECTION = true;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue RANDOM_ORE_DENSITY_MULTIPLIER = BUILDER
            .comment("Multiplier applied by future ore-suppression hooks to non-expedition useful ore placement.")
            .defineInRange("global.randomOreDensityMultiplier", DEFAULT_RANDOM_ORE_DENSITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue REQUIRE_STRUCTURE_ANCHOR_FOR_MAJOR_ORE_LOADS = BUILDER
            .comment("Require major ore loads to be tied to an expedition anchor.")
            .define("global.requireStructureAnchorForMajorOreLoads", DEFAULT_REQUIRE_STRUCTURE_ANCHOR);
    private static final ModConfigSpec.BooleanValue ALLOW_TINY_SCRAP_ORE_OUTSIDE_PROVINCES = BUILDER
            .comment("Allow future hooks to leave tiny scrap ore outside full expedition provinces.")
            .define("global.allowTinyScrapOreOutsideProvinces", DEFAULT_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES);

    private static final ModConfigSpec.IntValue ORE_LOAD_MIN_DISTANCE_FROM_ANCHOR = BUILDER
            .comment("Minimum Manhattan distance from an expedition anchor to a planned ore-load chamber.")
            .defineInRange("anchorRules.oreLoadMinDistanceFromAnchor", DEFAULT_MIN_DISTANCE, 1, 512);
    private static final ModConfigSpec.IntValue ORE_LOAD_MAX_DISTANCE_FROM_ANCHOR = BUILDER
            .comment("Maximum Manhattan distance from an expedition anchor to a planned ore-load chamber.")
            .defineInRange("anchorRules.oreLoadMaxDistanceFromAnchor", DEFAULT_MAX_DISTANCE, 1, 1024);
    private static final ModConfigSpec.BooleanValue REQUIRE_TUNNEL_CONNECTION = BUILDER
            .comment("Require future generated ore loads to be connected to the expedition route.")
            .define("anchorRules.requireTunnelConnection", DEFAULT_REQUIRE_TUNNEL_CONNECTION);

    private static final ModConfigSpec.BooleanValue TINY_VERTICAL_MINE_ENTRANCE = structure("tinyVerticalMineEntrance");
    private static final ModConfigSpec.BooleanValue COLLAPSED_SHAFT = structure("collapsedShaft");
    private static final ModConfigSpec.BooleanValue MINER_CAMP = structure("minerCamp");
    private static final ModConfigSpec.BooleanValue BURIED_SURVEY_MARKER = structure("buriedSurveyMarker");
    private static final ModConfigSpec.BooleanValue BASIC_MINESHAFT_CONNECTOR = structure("basicMineshaftConnector");
    private static final ModConfigSpec.BooleanValue ORE_LOAD_CHAMBER = structure("oreLoadChamber");

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeWorldgenConfig() {
    }

    public static double randomOreDensityMultiplier() {
        return getOrDefault(RANDOM_ORE_DENSITY_MULTIPLIER, DEFAULT_RANDOM_ORE_DENSITY_MULTIPLIER);
    }

    public static boolean requireStructureAnchorForMajorOreLoads() {
        return getOrDefault(REQUIRE_STRUCTURE_ANCHOR_FOR_MAJOR_ORE_LOADS, DEFAULT_REQUIRE_STRUCTURE_ANCHOR);
    }

    public static boolean allowTinyScrapOreOutsideProvinces() {
        return getOrDefault(ALLOW_TINY_SCRAP_ORE_OUTSIDE_PROVINCES, DEFAULT_ALLOW_TINY_SCRAP_OUTSIDE_PROVINCES);
    }

    public static int oreLoadMinDistanceFromAnchor() {
        return getOrDefault(ORE_LOAD_MIN_DISTANCE_FROM_ANCHOR, DEFAULT_MIN_DISTANCE);
    }

    public static int oreLoadMaxDistanceFromAnchor() {
        return getOrDefault(ORE_LOAD_MAX_DISTANCE_FROM_ANCHOR, DEFAULT_MAX_DISTANCE);
    }

    public static boolean requireTunnelConnection() {
        return getOrDefault(REQUIRE_TUNNEL_CONNECTION, DEFAULT_REQUIRE_TUNNEL_CONNECTION);
    }

    public static boolean tinyVerticalMineEntranceEnabled() {
        return getOrDefault(TINY_VERTICAL_MINE_ENTRANCE, true);
    }

    public static boolean collapsedShaftEnabled() {
        return getOrDefault(COLLAPSED_SHAFT, true);
    }

    public static boolean minerCampEnabled() {
        return getOrDefault(MINER_CAMP, true);
    }

    public static boolean buriedSurveyMarkerEnabled() {
        return getOrDefault(BURIED_SURVEY_MARKER, true);
    }

    public static boolean basicMineshaftConnectorEnabled() {
        return getOrDefault(BASIC_MINESHAFT_CONNECTOR, true);
    }

    public static boolean oreLoadChamberEnabled() {
        return getOrDefault(ORE_LOAD_CHAMBER, true);
    }

    private static ModConfigSpec.BooleanValue structure(String key) {
        return BUILDER.comment("Enable the " + key + " expedition structure catalog entry.")
                .define("structures." + key, true);
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
}
