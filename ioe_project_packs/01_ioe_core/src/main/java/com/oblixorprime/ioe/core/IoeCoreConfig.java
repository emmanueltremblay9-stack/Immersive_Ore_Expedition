package com.oblixorprime.ioe.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IoeCoreConfig {
    private static final boolean DEFAULT_EXISTING_RESOURCES_ONLY = true;
    private static final boolean DEFAULT_ALLOW_NEW_ORES = false;
    private static final boolean DEFAULT_SKIP_MISSING_RESOURCES = true;
    private static final boolean DEFAULT_LOG_MISSING_RESOURCES = true;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue EXISTING_RESOURCES_ONLY = BUILDER
            .comment("Require IOE resources to resolve from loaded registries or tags before use.")
            .define("resourcePolicy.existingResourcesOnly", DEFAULT_EXISTING_RESOURCES_ONLY);
    private static final ModConfigSpec.BooleanValue ALLOW_NEW_ORES = BUILDER
            .comment("IOE Core must not register or approve synthetic ore resources by default.")
            .define("resourcePolicy.allowNewOres", DEFAULT_ALLOW_NEW_ORES);
    private static final ModConfigSpec.BooleanValue SKIP_MISSING_RESOURCES = BUILDER
            .comment("Skip missing approved resources instead of substituting fallback resources.")
            .define("resourcePolicy.skipMissingResources", DEFAULT_SKIP_MISSING_RESOURCES);
    private static final ModConfigSpec.BooleanValue LOG_MISSING_RESOURCES = BUILDER
            .comment("Log skipped missing resources so pack configuration mistakes are visible.")
            .define("resourcePolicy.logMissingResources", DEFAULT_LOG_MISSING_RESOURCES);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IoeCoreConfig() {
    }

    public static boolean existingResourcesOnly() {
        return getOrDefault(EXISTING_RESOURCES_ONLY, DEFAULT_EXISTING_RESOURCES_ONLY);
    }

    public static boolean allowNewOres() {
        return getOrDefault(ALLOW_NEW_ORES, DEFAULT_ALLOW_NEW_ORES);
    }

    public static boolean skipMissingResources() {
        return getOrDefault(SKIP_MISSING_RESOURCES, DEFAULT_SKIP_MISSING_RESOURCES);
    }

    public static boolean logMissingResources() {
        return getOrDefault(LOG_MISSING_RESOURCES, DEFAULT_LOG_MISSING_RESOURCES);
    }

    private static boolean getOrDefault(ModConfigSpec.BooleanValue value, boolean defaultValue) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return defaultValue;
        }
    }
}
