package com.oblixorprime.ioe.nethergeodes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeNetherGeodesConfig {
    private static final boolean DEFAULT_ENABLED = true;
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

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable IOE sub-lava Nether geode planning.")
            .define("nether.subLavaGeodes.enabled", DEFAULT_ENABLED);
    private static final ModConfigSpec.BooleanValue REQUIRE_GIANT_LAVA_LAKE_ABOVE = BUILDER
            .comment("Require a validated giant Nether lava lake anchor above every geode.")
            .define("nether.subLavaGeodes.requireGiantLavaLakeAbove", DEFAULT_REQUIRE_GIANT_LAVA_LAKE_ABOVE);
    private static final ModConfigSpec.BooleanValue ALLOW_RANDOM_NETHER_GEODES = BUILDER
            .comment("Keep false to prevent random geodes in ordinary netherrack.")
            .define("nether.subLavaGeodes.allowRandomNetherGeodes", DEFAULT_ALLOW_RANDOM_NETHER_GEODES);

    private static final ModConfigSpec.IntValue LAVA_SAMPLE_RADIUS = BUILDER
            .comment("Minimum radius represented by lava lake sample reports.")
            .defineInRange("lavaLakeAnchor.lavaSampleRadius", DEFAULT_LAVA_SAMPLE_RADIUS, 1, 512);
    private static final ModConfigSpec.DoubleValue MINIMUM_LAVA_COVERAGE = BUILDER
            .comment("Minimum lava coverage ratio needed to treat a sample as a giant lake.")
            .defineInRange("lavaLakeAnchor.minimumLavaCoverage", DEFAULT_MINIMUM_LAVA_COVERAGE, 0.0D, 1.0D);
    private static final ModConfigSpec.IntValue MINIMUM_LAVA_DEPTH = BUILDER
            .comment("Minimum observed lava depth needed to anchor sub-lava geodes.")
            .defineInRange("lavaLakeAnchor.minimumLavaDepth", DEFAULT_MINIMUM_LAVA_DEPTH, 1, 64);

    private static final ModConfigSpec.IntValue MIN_BLOCKS_BELOW_LAVA = BUILDER
            .comment("Minimum vertical distance below the lava body for a planned geode chamber.")
            .defineInRange("placement.minBlocksBelowLava", DEFAULT_MIN_BLOCKS_BELOW_LAVA, 1, 256);
    private static final ModConfigSpec.IntValue MAX_BLOCKS_BELOW_LAVA = BUILDER
            .comment("Maximum vertical distance below the lava body for a planned geode chamber.")
            .defineInRange("placement.maxBlocksBelowLava", DEFAULT_MAX_BLOCKS_BELOW_LAVA, 1, 512);
    private static final ModConfigSpec.BooleanValue REQUIRE_SAFE_CRUST = BUILDER
            .comment("Require future placement hooks to leave a safe shell/crust between lava and chamber.")
            .define("placement.requireSafeCrust", DEFAULT_REQUIRE_SAFE_CRUST);

    private static final ModConfigSpec.BooleanValue NETHER_QUARTZ = BUILDER
            .comment("Allow Nether Quartz as the primary sub-lava geode resource when loaded.")
            .define("resources.netherQuartz", DEFAULT_NETHER_QUARTZ);
    private static final ModConfigSpec.BooleanValue ANCIENT_DEBRIS_EXTREME_RARE = BUILDER
            .comment("Allow extremely rare ancient debris hearts only when the resource exists.")
            .define("resources.ancientDebrisExtremeRare", DEFAULT_ANCIENT_DEBRIS_EXTREME_RARE);
    private static final ModConfigSpec.DoubleValue ANCIENT_DEBRIS_MOTHERLODE_CHANCE = BUILDER
            .comment("Chance for an ancient debris heart in a valid planned sub-lava geode.")
            .defineInRange("resources.ancientDebrisMotherlodeChance", DEFAULT_ANCIENT_DEBRIS_MOTHERLODE_CHANCE, 0.0D, 0.05D);
    private static final ModConfigSpec.BooleanValue CLUE_STRUCTURES_ENABLED = BUILDER
            .comment("Enable small Nether clue structure IDs near valid lava shore or edge anchors.")
            .define("clues.enabled", DEFAULT_CLUE_STRUCTURES_ENABLED);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeNetherGeodesConfig() {
    }

    public static boolean enabled() {
        return getOrDefault(ENABLED, DEFAULT_ENABLED);
    }

    public static boolean requireGiantLavaLakeAbove() {
        return getOrDefault(REQUIRE_GIANT_LAVA_LAKE_ABOVE, DEFAULT_REQUIRE_GIANT_LAVA_LAKE_ABOVE);
    }

    public static boolean allowRandomNetherGeodes() {
        return getOrDefault(ALLOW_RANDOM_NETHER_GEODES, DEFAULT_ALLOW_RANDOM_NETHER_GEODES);
    }

    public static int lavaSampleRadius() {
        return getOrDefault(LAVA_SAMPLE_RADIUS, DEFAULT_LAVA_SAMPLE_RADIUS);
    }

    public static double minimumLavaCoverage() {
        return getOrDefault(MINIMUM_LAVA_COVERAGE, DEFAULT_MINIMUM_LAVA_COVERAGE);
    }

    public static int minimumLavaDepth() {
        return getOrDefault(MINIMUM_LAVA_DEPTH, DEFAULT_MINIMUM_LAVA_DEPTH);
    }

    public static int minBlocksBelowLava() {
        return getOrDefault(MIN_BLOCKS_BELOW_LAVA, DEFAULT_MIN_BLOCKS_BELOW_LAVA);
    }

    public static int maxBlocksBelowLava() {
        return Math.max(minBlocksBelowLava(), getOrDefault(MAX_BLOCKS_BELOW_LAVA, DEFAULT_MAX_BLOCKS_BELOW_LAVA));
    }

    public static boolean requireSafeCrust() {
        return getOrDefault(REQUIRE_SAFE_CRUST, DEFAULT_REQUIRE_SAFE_CRUST);
    }

    public static boolean netherQuartz() {
        return getOrDefault(NETHER_QUARTZ, DEFAULT_NETHER_QUARTZ);
    }

    public static boolean ancientDebrisExtremeRare() {
        return getOrDefault(ANCIENT_DEBRIS_EXTREME_RARE, DEFAULT_ANCIENT_DEBRIS_EXTREME_RARE);
    }

    public static double ancientDebrisMotherlodeChance() {
        return getOrDefault(ANCIENT_DEBRIS_MOTHERLODE_CHANCE, DEFAULT_ANCIENT_DEBRIS_MOTHERLODE_CHANCE);
    }

    public static boolean clueStructuresEnabled() {
        return getOrDefault(CLUE_STRUCTURES_ENABLED, DEFAULT_CLUE_STRUCTURES_ENABLED);
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
