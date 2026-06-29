package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

public final class IoeNetherGeodesConfig {
    private IoeNetherGeodesConfig() {
    }

    public static boolean enabled() {
        return ImmersiveOreExpeditionConfig.netherEnabled();
    }

    public static boolean requireGiantLavaLakeAbove() {
        return ImmersiveOreExpeditionConfig.netherRequireGiantLavaLakeAbove();
    }

    public static boolean allowRandomNetherGeodes() {
        return ImmersiveOreExpeditionConfig.netherAllowRandomNetherGeodes();
    }

    public static int lavaSampleRadius() {
        return ImmersiveOreExpeditionConfig.netherLavaSampleRadius();
    }

    public static double minimumLavaCoverage() {
        return ImmersiveOreExpeditionConfig.netherMinimumLavaCoverage();
    }

    public static int minimumLavaDepth() {
        return ImmersiveOreExpeditionConfig.netherMinimumLavaDepth();
    }

    public static int minBlocksBelowLava() {
        return ImmersiveOreExpeditionConfig.netherMinBlocksBelowLava();
    }

    public static int maxBlocksBelowLava() {
        return ImmersiveOreExpeditionConfig.netherMaxBlocksBelowLava();
    }

    public static boolean requireSafeCrust() {
        return ImmersiveOreExpeditionConfig.netherRequireSafeCrust();
    }

    public static boolean netherQuartz() {
        return ImmersiveOreExpeditionConfig.netherQuartz();
    }

    public static boolean ancientDebrisExtremeRare() {
        return ImmersiveOreExpeditionConfig.netherAncientDebrisExtremeRare();
    }

    public static double ancientDebrisMotherlodeChance() {
        return ImmersiveOreExpeditionConfig.netherAncientDebrisMotherlodeChance();
    }

    public static boolean clueStructuresEnabled() {
        return ImmersiveOreExpeditionConfig.netherClueStructuresEnabled();
    }
}
