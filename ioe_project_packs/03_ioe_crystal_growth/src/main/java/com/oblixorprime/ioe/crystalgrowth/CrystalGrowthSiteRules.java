package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;

public final class CrystalGrowthSiteRules {
    private CrystalGrowthSiteRules() {
    }

    public static boolean canPlanAnchoredSite(ExpeditionAnchorRef anchor) {
        if (!IoeCrystalGrowthConfig.enabled() || IoeCrystalGrowthConfig.allowRandomFreeCrystalSites()) {
            return false;
        }
        return !IoeCrystalGrowthConfig.requireStructureAnchor() || anchor != null;
    }
}
