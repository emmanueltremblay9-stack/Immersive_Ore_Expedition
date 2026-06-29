package com.oblixorprime.ioe.nethergeodes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NetherGeodesConfigTest {
    @Test
    void defaultsRequireGiantLavaLakeAndKeepDebrisRare() {
        assertTrue(IoeNetherGeodesConfig.enabled());
        assertTrue(IoeNetherGeodesConfig.requireGiantLavaLakeAbove());
        assertFalse(IoeNetherGeodesConfig.allowRandomNetherGeodes());
        assertEquals(64, IoeNetherGeodesConfig.lavaSampleRadius());
        assertEquals(0.60D, IoeNetherGeodesConfig.minimumLavaCoverage());
        assertEquals(0.005D, IoeNetherGeodesConfig.ancientDebrisMotherlodeChance());
    }
}
