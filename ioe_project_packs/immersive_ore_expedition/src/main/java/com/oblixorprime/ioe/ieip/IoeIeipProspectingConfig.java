package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

public final class IoeIeipProspectingConfig {
    private IoeIeipProspectingConfig() {
    }

    public static boolean immersiveEngineeringEnabledIfLoaded() {
        return ImmersiveOreExpeditionConfig.ieipImmersiveEngineeringEnabledIfLoaded();
    }

    public static boolean reduceMineralDepositQuantity() {
        return ImmersiveOreExpeditionConfig.ieipReduceMineralDepositQuantity();
    }

    public static double depositQuantityMultiplier() {
        return ImmersiveOreExpeditionConfig.ieipDepositQuantityMultiplier();
    }

    public static double hardModeDepositQuantityMultiplier() {
        return ImmersiveOreExpeditionConfig.ieipHardModeDepositQuantityMultiplier();
    }

    public static boolean renderFullDeposit() {
        return ImmersiveOreExpeditionConfig.ieipRenderFullDeposit();
    }

    public static boolean createUndergroundVisualProxy() {
        return ImmersiveOreExpeditionConfig.ieipCreateUndergroundVisualProxy();
    }

    public static boolean surfaceOutcropsEnabled() {
        return ImmersiveOreExpeditionConfig.ieipSurfaceOutcropsEnabled();
    }

    public static int boulderCountMin() {
        return ImmersiveOreExpeditionConfig.ieipBoulderCountMin();
    }

    public static int boulderCountMax() {
        return ImmersiveOreExpeditionConfig.ieipBoulderCountMax();
    }

    public static boolean useDepositPresentResourcesOnly() {
        return ImmersiveOreExpeditionConfig.ieipUseDepositPresentResourcesOnly();
    }

    public static int freeOreRewardLimitBlocks() {
        return ImmersiveOreExpeditionConfig.ieipFreeOreRewardLimitBlocks();
    }

    public static boolean immersivePetroleumEnabledIfLoaded() {
        return ImmersiveOreExpeditionConfig.ieipImmersivePetroleumEnabledIfLoaded();
    }

    public static boolean renderFullReservoir() {
        return ImmersiveOreExpeditionConfig.ieipRenderFullReservoir();
    }

    public static boolean createUndergroundReservoirProxy() {
        return ImmersiveOreExpeditionConfig.ieipCreateUndergroundReservoirProxy();
    }

    public static boolean surfaceSeepsEnabled() {
        return ImmersiveOreExpeditionConfig.ieipSurfaceSeepsEnabled();
    }

    public static boolean smallSurfacePocketLakes() {
        return ImmersiveOreExpeditionConfig.ieipSmallSurfacePocketLakes();
    }

    public static int maxSurfaceFluidBlocks() {
        return ImmersiveOreExpeditionConfig.ieipMaxSurfaceFluidBlocks();
    }

    public static boolean ventForGasLikeReservoirs() {
        return ImmersiveOreExpeditionConfig.ieipVentForGasLikeReservoirs();
    }
}
