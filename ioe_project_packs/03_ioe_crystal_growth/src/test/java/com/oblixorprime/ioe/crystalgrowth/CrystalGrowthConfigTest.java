package com.oblixorprime.ioe.crystalgrowth;

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
}
