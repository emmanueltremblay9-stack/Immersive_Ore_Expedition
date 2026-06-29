package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;

public final class CrystalGrowthSiteRules {
    private CrystalGrowthSiteRules() {
    }

    public static boolean canPlanAnchoredSite(ExpeditionAnchorRef anchor) {
        return canPlanAnchoredSite(
                IoeCrystalGrowthConfig.enabled(),
                IoeCrystalGrowthConfig.allowRandomFreeCrystalSites(),
                IoeCrystalGrowthConfig.requireStructureAnchor(),
                anchor
        );
    }

    static boolean canPlanAnchoredSite(
            boolean enabled,
            boolean allowRandomFreeCrystalSites,
            boolean requireStructureAnchor,
            ExpeditionAnchorRef anchor
    ) {
        if (!enabled) {
            return false;
        }
        return anchor != null;
    }
}
