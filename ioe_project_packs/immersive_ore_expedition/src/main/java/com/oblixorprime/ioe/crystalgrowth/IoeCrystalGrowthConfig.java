package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

public final class IoeCrystalGrowthConfig {
    private IoeCrystalGrowthConfig() {
    }

    public static boolean enabled() {
        return ImmersiveOreExpeditionConfig.crystalEnabled();
    }

    public static boolean requireStructureAnchor() {
        return ImmersiveOreExpeditionConfig.crystalRequireStructureAnchor();
    }

    public static boolean allowRandomFreeCrystalSites() {
        return ImmersiveOreExpeditionConfig.crystalAllowRandomFreeCrystalSites();
    }

    public static boolean amethystStructureAnchoredSites() {
        return ImmersiveOreExpeditionConfig.crystalAmethystStructureAnchoredSites();
    }

    public static boolean amethystMeteoriteWrappedVariant() {
        return ImmersiveOreExpeditionConfig.crystalAmethystMeteoriteWrappedVariant();
    }

    public static boolean ae2EnabledIfLoaded() {
        return ImmersiveOreExpeditionConfig.crystalAe2EnabledIfLoaded();
    }

    public static boolean ae2SurfaceMeteorites() {
        return ImmersiveOreExpeditionConfig.crystalAe2SurfaceMeteorites();
    }

    public static boolean buriedMeteorites() {
        return ImmersiveOreExpeditionConfig.crystalBuriedMeteorites();
    }

    public static boolean allowBuddingCertusSites() {
        return ImmersiveOreExpeditionConfig.crystalAllowBuddingCertusSites();
    }

    public static boolean allowFluixOreGeneration() {
        return ImmersiveOreExpeditionConfig.crystalAllowFluixOreGeneration();
    }

    public static boolean skyStoneCrustAroundGeodes() {
        return ImmersiveOreExpeditionConfig.crystalSkyStoneCrustAroundGeodes();
    }

    public static boolean georeEnabledIfLoaded() {
        return ImmersiveOreExpeditionConfig.crystalGeoreEnabledIfLoaded();
    }

    public static boolean disableFreeGeoreWorldgen() {
        return ImmersiveOreExpeditionConfig.crystalDisableFreeGeoreWorldgen();
    }

    public static boolean anchorAllGeoresToExpeditionStructures() {
        return ImmersiveOreExpeditionConfig.crystalAnchorAllGeoresToExpeditionStructures();
    }

    public static boolean existingResourcesOnly() {
        return ImmersiveOreExpeditionConfig.crystalExistingResourcesOnly();
    }

    public static boolean skipMissingGeores() {
        return ImmersiveOreExpeditionConfig.crystalSkipMissingGeores();
    }

    public static boolean meteoriticGeodeEnabled() {
        return ImmersiveOreExpeditionConfig.crystalMeteoriticGeodeEnabled();
    }

    public static boolean meteoriticGeodeRequiresAe2() {
        return ImmersiveOreExpeditionConfig.crystalMeteoriticGeodeRequiresAe2();
    }
}
