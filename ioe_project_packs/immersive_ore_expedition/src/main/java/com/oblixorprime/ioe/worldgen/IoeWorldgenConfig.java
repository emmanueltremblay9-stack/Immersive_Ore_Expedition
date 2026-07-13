package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

import java.util.List;

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

    public static boolean naturalExpeditionSiteGenerationEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenNaturalExpeditionSiteGenerationEnabled();
    }

    public static boolean runtimePlacementEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenRuntimePlacementEnabled();
    }

    public static boolean runtimePlacementDiagnostics() {
        return ImmersiveOreExpeditionConfig.worldgenRuntimePlacementDiagnostics();
    }

    public static boolean runtimeProofFeatureEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenRuntimeProofFeatureEnabled();
    }

    public static boolean runtimeProofFeatureDiagnostics() {
        return ImmersiveOreExpeditionConfig.worldgenRuntimeProofFeatureDiagnostics();
    }

    public static boolean compassShowDiagnosticSites() {
        return ImmersiveOreExpeditionConfig.worldgenCompassShowDiagnosticSites();
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

    public static String provinceNamespace() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceNamespace();
    }

    public static boolean allowLegacyProvinceNamespaces() {
        return ImmersiveOreExpeditionConfig.worldgenAllowLegacyProvinceNamespaces();
    }

    public static boolean provinceRuntimeIntegrationEnabled() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceRuntimeIntegrationEnabled();
    }

    public static String defaultProvince() {
        return ImmersiveOreExpeditionConfig.worldgenDefaultProvince();
    }

    public static List<String> biomeProvinceBindings() {
        return ImmersiveOreExpeditionConfig.worldgenBiomeProvinceBindings();
    }

    public static List<String> provinceResourcePolicyRules() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceResourcePolicyRules();
    }

    public static List<String> provinceAllowBiomes() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceAllowBiomes();
    }

    public static List<String> provinceDenyBiomes() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceDenyBiomes();
    }

    public static List<String> provinceExcludeBiomes() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceExcludeBiomes();
    }

    public static boolean provinceDebugDiagnostics() {
        return ImmersiveOreExpeditionConfig.worldgenProvinceDebugDiagnostics();
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
