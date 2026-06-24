package com.oblixorprime.ioe.retrogen;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeRetrogenAdminConfig {
    private static final boolean DEFAULT_ENABLED = false;
    private static final String DEFAULT_MODE = "unexplored_chunks_only";
    private static final boolean DEFAULT_REQUIRE_ADMIN_COMMAND = true;
    private static final int DEFAULT_CHUNK_MARKER_VERSION = 1;
    private static final int DEFAULT_MAX_CHUNKS_PER_TICK = 1;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Retrogen is disabled by default and must be explicitly enabled by server configuration.")
            .define("retrogen.enabled", DEFAULT_ENABLED);
    private static final ModConfigSpec.ConfigValue<String> DEFAULT_MODE_VALUE = BUILDER
            .comment("Default retrogen mode: off, unexplored_chunks_only, admin_radius, ore_pocket_only, or clue_plus_pocket.")
            .define("retrogen.defaultMode", DEFAULT_MODE);
    private static final ModConfigSpec.BooleanValue REQUIRE_ADMIN_COMMAND = BUILDER
            .comment("Require permission level 2 for IOE admin commands.")
            .define("retrogen.requireAdminCommand", DEFAULT_REQUIRE_ADMIN_COMMAND);
    private static final ModConfigSpec.IntValue CHUNK_MARKER_VERSION = BUILDER
            .comment("Chunk marker version used to prevent repeated retrogen in the same chunk.")
            .defineInRange("retrogen.chunkMarkerVersion", DEFAULT_CHUNK_MARKER_VERSION, 1, 1024);
    private static final ModConfigSpec.IntValue MAX_CHUNKS_PER_TICK = BUILDER
            .comment("Maximum queued chunks processed per server tick once runtime generation is bound.")
            .defineInRange("retrogen.maxChunksPerTick", DEFAULT_MAX_CHUNKS_PER_TICK, 1, 64);
    private static final ModConfigSpec.BooleanValue MODE_ORE_POCKET_ONLY = BUILDER
            .comment("Allow admin retrogen requests that only restore ore pockets.")
            .define("modes.orePocketOnly", true);
    private static final ModConfigSpec.BooleanValue MODE_CLUE_PLUS_POCKET = BUILDER
            .comment("Allow admin retrogen requests that restore clues and ore pockets.")
            .define("modes.cluePlusPocket", true);
    private static final ModConfigSpec.BooleanValue MODE_FULL_PROVINCE = BUILDER
            .comment("Full province retrogen remains disabled by default for safety.")
            .define("modes.fullProvince", false);
    private static final ModConfigSpec.BooleanValue MODE_ADMIN_RADIUS_ONLY = BUILDER
            .comment("Limit retrogen to explicit admin radius requests.")
            .define("modes.adminRadiusOnly", true);
    private static final ModConfigSpec.BooleanValue COMMAND_LOCATE_PROVINCE = BUILDER
            .define("commands.locateProvince", true);
    private static final ModConfigSpec.BooleanValue COMMAND_LOCATE_ANCHOR = BUILDER
            .define("commands.locateAnchor", true);
    private static final ModConfigSpec.BooleanValue COMMAND_RETROGEN_STATUS = BUILDER
            .define("commands.retrogenStatus", true);
    private static final ModConfigSpec.BooleanValue COMMAND_RETROGEN_START = BUILDER
            .define("commands.retrogenStart", true);
    private static final ModConfigSpec.BooleanValue COMMAND_RETROGEN_PAUSE = BUILDER
            .define("commands.retrogenPause", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeRetrogenAdminConfig() {
    }

    public static boolean enabled() {
        return getOrDefault(ENABLED, DEFAULT_ENABLED);
    }

    public static RetrogenMode defaultMode() {
        return RetrogenMode.fromConfig(getOrDefault(DEFAULT_MODE_VALUE, DEFAULT_MODE));
    }

    public static boolean requireAdminCommand() {
        return getOrDefault(REQUIRE_ADMIN_COMMAND, DEFAULT_REQUIRE_ADMIN_COMMAND);
    }

    public static int chunkMarkerVersion() {
        return getOrDefault(CHUNK_MARKER_VERSION, DEFAULT_CHUNK_MARKER_VERSION);
    }

    public static int maxChunksPerTick() {
        return getOrDefault(MAX_CHUNKS_PER_TICK, DEFAULT_MAX_CHUNKS_PER_TICK);
    }

    public static boolean modeAllowed(RetrogenMode mode) {
        return switch (mode) {
            case OFF, UNEXPLORED_CHUNKS_ONLY -> true;
            case ADMIN_RADIUS -> getOrDefault(MODE_ADMIN_RADIUS_ONLY, true);
            case ORE_POCKET_ONLY -> getOrDefault(MODE_ORE_POCKET_ONLY, true);
            case CLUE_PLUS_POCKET -> getOrDefault(MODE_CLUE_PLUS_POCKET, true);
        };
    }

    public static boolean fullProvinceAllowed() {
        return getOrDefault(MODE_FULL_PROVINCE, false);
    }

    public static boolean commandLocateProvinceEnabled() {
        return getOrDefault(COMMAND_LOCATE_PROVINCE, true);
    }

    public static boolean commandLocateAnchorEnabled() {
        return getOrDefault(COMMAND_LOCATE_ANCHOR, true);
    }

    public static boolean commandRetrogenStatusEnabled() {
        return getOrDefault(COMMAND_RETROGEN_STATUS, true);
    }

    public static boolean commandRetrogenStartEnabled() {
        return getOrDefault(COMMAND_RETROGEN_START, true);
    }

    public static boolean commandRetrogenPauseEnabled() {
        return getOrDefault(COMMAND_RETROGEN_PAUSE, true);
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

    private static String getOrDefault(ModConfigSpec.ConfigValue<String> value, String defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }
}
