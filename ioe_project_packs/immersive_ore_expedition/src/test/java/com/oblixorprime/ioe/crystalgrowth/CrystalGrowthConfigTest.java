package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrystalGrowthConfigTest {
    @Test
    void defaultsKeepCrystalSitesAnchoredAndDisableForbiddenGeneration() {
        assertTrue(IoeCrystalGrowthConfig.enabled());
        assertTrue(IoeCrystalGrowthConfig.requireStructureAnchor());
        assertFalse(IoeCrystalGrowthConfig.allowRandomFreeCrystalSites());
        assertFalse(IoeCrystalGrowthConfig.ae2SurfaceMeteorites());
        assertFalse(IoeCrystalGrowthConfig.allowFluixOreGeneration());
        assertTrue(IoeCrystalGrowthConfig.disableFreeGeoreWorldgen());
        assertTrue(IoeCrystalGrowthConfig.anchorAllGeoresToExpeditionStructures());
    }

    @Test
    void randomFreeSiteFlagDoesNotDisableAnchoredPlanning() {
        assertTrue(CrystalGrowthSiteRules.canPlanAnchoredSite(true, true, true, anchor()));
    }

    @Test
    void rejectsMissingAnchorEvenWhenStructureAnchorRequirementIsDisabled() {
        assertFalse(CrystalGrowthSiteRules.canPlanAnchoredSite(true, false, false, null));
    }

    @Test
    void disabledModuleRejectsAnchoredPlanning() {
        assertFalse(CrystalGrowthSiteRules.canPlanAnchoredSite(false, false, true, anchor()));
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, BlockPos.ZERO, "crystal_growth_chamber", SiteQuality.NORMAL);
    }
}
