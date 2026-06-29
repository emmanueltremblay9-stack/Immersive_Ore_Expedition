package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

public final class IoeWorldgenConfig {
    private IoeWorldgenConfig() {
    }

    public static double randomOreDensityMultiplier() {
        return ImmersiveOreExpeditionConfig.worldgenRandomOreDensityMultiplier();
    }

    public static boolean requireStructureAnchorForMajorOreLoads() {
        return ImmersiveOreExpeditionConfig.worldgenRequireStructureAnchorForMajorOreLoads();
    }

    public static boolean allowTinyScrapOreOutsideProvinces() {
        return ImmersiveOreExpeditionConfig.worldgenAllowTinyScrapOreOutsideProvinces();
    }

    public static int oreLoadMinDistanceFromAnchor() {
        return ImmersiveOreExpeditionConfig.worldgenOreLoadMinDistanceFromAnchor();
    }

    public static int oreLoadMaxDistanceFromAnchor() {
        return ImmersiveOreExpeditionConfig.worldgenOreLoadMaxDistanceFromAnchor();
    }

    public static boolean requireTunnelConnection() {
        return ImmersiveOreExpeditionConfig.worldgenRequireTunnelConnection();
    }

    public static boolean tinyVerticalMineEntranceEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenTinyVerticalMineEntranceEnabled();
    }

    public static boolean collapsedShaftEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenCollapsedShaftEnabled();
    }

    public static boolean minerCampEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenMinerCampEnabled();
    }

    public static boolean buriedSurveyMarkerEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenBuriedSurveyMarkerEnabled();
    }

    public static boolean basicMineshaftConnectorEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenBasicMineshaftConnectorEnabled();
    }

    public static boolean oreLoadChamberEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenOreLoadChamberEnabled();
    }
}
