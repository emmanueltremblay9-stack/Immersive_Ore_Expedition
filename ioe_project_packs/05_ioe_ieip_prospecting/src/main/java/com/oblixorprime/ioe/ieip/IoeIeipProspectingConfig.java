package com.oblixorprime.ioe.ieip;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeIeipProspectingConfig {
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

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue IE_ENABLED_IF_LOADED = BUILDER
            .comment("Enable Immersive Engineering clue policies only when immersiveengineering is loaded.")
            .define("immersiveEngineering.enabledIfLoaded", DEFAULT_IE_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue REDUCE_MINERAL_DEPOSIT_QUANTITY = BUILDER
            .comment("Plan IE mineral deposit quantity reduction so the Excavator does not dominate IOE progression.")
            .define("immersiveEngineering.reduceMineralDepositQuantity", DEFAULT_REDUCE_MINERAL_DEPOSIT_QUANTITY);
    private static final ModConfigSpec.DoubleValue DEPOSIT_QUANTITY_MULTIPLIER = BUILDER
            .comment("Normal-mode multiplier for supported IE deposit quantity hooks.")
            .defineInRange("immersiveEngineering.depositQuantityMultiplier", DEFAULT_DEPOSIT_QUANTITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.DoubleValue HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER = BUILDER
            .comment("Hard-mode multiplier for supported IE deposit quantity hooks.")
            .defineInRange("immersiveEngineering.hardModeDepositQuantityMultiplier", DEFAULT_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue RENDER_FULL_DEPOSIT = BUILDER
            .comment("Documented hard-off switch; full underground IE deposit rendering is outside this module.")
            .define("immersiveEngineering.renderFullDeposit", DEFAULT_RENDER_FULL_DEPOSIT);
    private static final ModConfigSpec.BooleanValue CREATE_UNDERGROUND_VISUAL_PROXY = BUILDER
            .comment("Documented hard-off switch; underground IE visual proxies are outside this module.")
            .define("immersiveEngineering.createUndergroundVisualProxy", DEFAULT_CREATE_UNDERGROUND_VISUAL_PROXY);
    private static final ModConfigSpec.BooleanValue SURFACE_OUTCROPS_ENABLED = BUILDER
            .comment("Enable small surface mineral outcrop clues for IE deposits.")
            .define("immersiveEngineering.surfaceOutcrops.enabled", DEFAULT_SURFACE_OUTCROPS_ENABLED);
    private static final ModConfigSpec.IntValue BOULDER_COUNT_MIN = BUILDER
            .comment("Minimum boulder count in an IE outcrop clue plan.")
            .defineInRange("immersiveEngineering.surfaceOutcrops.boulderCountMin", DEFAULT_BOULDER_COUNT_MIN, 0, 64);
    private static final ModConfigSpec.IntValue BOULDER_COUNT_MAX = BUILDER
            .comment("Maximum boulder count in an IE outcrop clue plan.")
            .defineInRange("immersiveEngineering.surfaceOutcrops.boulderCountMax", DEFAULT_BOULDER_COUNT_MAX, 0, 64);
    private static final ModConfigSpec.BooleanValue USE_DEPOSIT_PRESENT_RESOURCES_ONLY = BUILDER
            .comment("Outcrop clues must use resources reported by the IE deposit; no fallback ores are invented.")
            .define("immersiveEngineering.surfaceOutcrops.useDepositPresentResourcesOnly", DEFAULT_USE_DEPOSIT_PRESENT_RESOURCES_ONLY);
    private static final ModConfigSpec.IntValue FREE_ORE_REWARD_LIMIT_BLOCKS = BUILDER
            .comment("Maximum loose ore/value blocks an outcrop clue may expose before future placement hooks stop.")
            .defineInRange("immersiveEngineering.surfaceOutcrops.freeOreRewardLimitBlocks", DEFAULT_FREE_ORE_REWARD_LIMIT_BLOCKS, 0, 64);

    private static final ModConfigSpec.BooleanValue IP_ENABLED_IF_LOADED = BUILDER
            .comment("Enable Immersive Petroleum clue policies only when immersivepetroleum is loaded.")
            .define("immersivePetroleum.enabledIfLoaded", DEFAULT_IP_ENABLED_IF_LOADED);
    private static final ModConfigSpec.BooleanValue RENDER_FULL_RESERVOIR = BUILDER
            .comment("Documented hard-off switch; full underground IP reservoir rendering is outside this module.")
            .define("immersivePetroleum.renderFullReservoir", DEFAULT_RENDER_FULL_RESERVOIR);
    private static final ModConfigSpec.BooleanValue CREATE_UNDERGROUND_RESERVOIR_PROXY = BUILDER
            .comment("Documented hard-off switch; underground IP visual proxies are outside this module.")
            .define("immersivePetroleum.createUndergroundVisualProxy", DEFAULT_CREATE_UNDERGROUND_RESERVOIR_PROXY);
    private static final ModConfigSpec.BooleanValue SURFACE_SEEPS_ENABLED = BUILDER
            .comment("Enable small surface seep, pocket-lake, or vent clue plans for IP reservoirs.")
            .define("immersivePetroleum.surfaceSeeps.enabled", DEFAULT_SURFACE_SEEPS_ENABLED);
    private static final ModConfigSpec.BooleanValue SMALL_SURFACE_POCKET_LAKES = BUILDER
            .comment("Allow liquid reservoir clues to plan small surface pocket lakes.")
            .define("immersivePetroleum.surfaceSeeps.smallSurfacePocketLakes", DEFAULT_SMALL_SURFACE_POCKET_LAKES);
    private static final ModConfigSpec.IntValue MAX_SURFACE_FLUID_BLOCKS = BUILDER
            .comment("Maximum surface fluid blocks a future IP seep placement hook may expose.")
            .defineInRange("immersivePetroleum.surfaceSeeps.maxSurfaceFluidBlocks", DEFAULT_MAX_SURFACE_FLUID_BLOCKS, 0, 64);
    private static final ModConfigSpec.BooleanValue VENT_FOR_GAS_LIKE_RESERVOIRS = BUILDER
            .comment("Plan vents instead of pocket lakes for gas-like reservoir fluids.")
            .define("immersivePetroleum.surfaceSeeps.ventForGasLikeReservoirs", DEFAULT_VENT_FOR_GAS_LIKE_RESERVOIRS);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeIeipProspectingConfig() {
    }

    public static boolean immersiveEngineeringEnabledIfLoaded() {
        return getOrDefault(IE_ENABLED_IF_LOADED, DEFAULT_IE_ENABLED_IF_LOADED);
    }

    public static boolean reduceMineralDepositQuantity() {
        return getOrDefault(REDUCE_MINERAL_DEPOSIT_QUANTITY, DEFAULT_REDUCE_MINERAL_DEPOSIT_QUANTITY);
    }

    public static double depositQuantityMultiplier() {
        return getOrDefault(DEPOSIT_QUANTITY_MULTIPLIER, DEFAULT_DEPOSIT_QUANTITY_MULTIPLIER);
    }

    public static double hardModeDepositQuantityMultiplier() {
        return getOrDefault(HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER, DEFAULT_HARD_MODE_DEPOSIT_QUANTITY_MULTIPLIER);
    }

    public static boolean renderFullDeposit() {
        return getOrDefault(RENDER_FULL_DEPOSIT, DEFAULT_RENDER_FULL_DEPOSIT);
    }

    public static boolean createUndergroundVisualProxy() {
        return getOrDefault(CREATE_UNDERGROUND_VISUAL_PROXY, DEFAULT_CREATE_UNDERGROUND_VISUAL_PROXY);
    }

    public static boolean surfaceOutcropsEnabled() {
        return getOrDefault(SURFACE_OUTCROPS_ENABLED, DEFAULT_SURFACE_OUTCROPS_ENABLED);
    }

    public static int boulderCountMin() {
        return getOrDefault(BOULDER_COUNT_MIN, DEFAULT_BOULDER_COUNT_MIN);
    }

    public static int boulderCountMax() {
        return Math.max(boulderCountMin(), getOrDefault(BOULDER_COUNT_MAX, DEFAULT_BOULDER_COUNT_MAX));
    }

    public static boolean useDepositPresentResourcesOnly() {
        return getOrDefault(USE_DEPOSIT_PRESENT_RESOURCES_ONLY, DEFAULT_USE_DEPOSIT_PRESENT_RESOURCES_ONLY);
    }

    public static int freeOreRewardLimitBlocks() {
        return getOrDefault(FREE_ORE_REWARD_LIMIT_BLOCKS, DEFAULT_FREE_ORE_REWARD_LIMIT_BLOCKS);
    }

    public static boolean immersivePetroleumEnabledIfLoaded() {
        return getOrDefault(IP_ENABLED_IF_LOADED, DEFAULT_IP_ENABLED_IF_LOADED);
    }

    public static boolean renderFullReservoir() {
        return getOrDefault(RENDER_FULL_RESERVOIR, DEFAULT_RENDER_FULL_RESERVOIR);
    }

    public static boolean createUndergroundReservoirProxy() {
        return getOrDefault(CREATE_UNDERGROUND_RESERVOIR_PROXY, DEFAULT_CREATE_UNDERGROUND_RESERVOIR_PROXY);
    }

    public static boolean surfaceSeepsEnabled() {
        return getOrDefault(SURFACE_SEEPS_ENABLED, DEFAULT_SURFACE_SEEPS_ENABLED);
    }

    public static boolean smallSurfacePocketLakes() {
        return getOrDefault(SMALL_SURFACE_POCKET_LAKES, DEFAULT_SMALL_SURFACE_POCKET_LAKES);
    }

    public static int maxSurfaceFluidBlocks() {
        return getOrDefault(MAX_SURFACE_FLUID_BLOCKS, DEFAULT_MAX_SURFACE_FLUID_BLOCKS);
    }

    public static boolean ventForGasLikeReservoirs() {
        return getOrDefault(VENT_FOR_GAS_LIKE_RESERVOIRS, DEFAULT_VENT_FOR_GAS_LIKE_RESERVOIRS);
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
