package com.oblixorprime.ioe.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceIdTest {
    @Test
    void unqualifiedProvinceIdsUseConsolidatedNamespace() {
        ProvinceId id = ProvinceId.parse("temperate_iron");

        assertEquals("immersive_ore_expedition", id.namespace());
        assertEquals("temperate_iron", id.path());
        assertTrue(id.isConsolidatedNamespace());
        assertFalse(id.isLegacyNamespace());
    }

    @Test
    void consolidatedNamespaceIsAcceptedExplicitly() {
        ProvinceId id = ProvinceId.parse("immersive_ore_expedition:nether_quartz_lakes");

        assertEquals("immersive_ore_expedition:nether_quartz_lakes", id.toString());
        assertTrue(id.isConsolidatedNamespace());
    }

    @Test
    void arbitraryNamespacesAreRejectedForNewProvinceIds() {
        assertThrows(IllegalArgumentException.class, () -> ProvinceId.parse("example:iron"));
    }

    @Test
    void legacySplitNamespacesRequireExplicitOptIn() {
        assertThrows(IllegalArgumentException.class, () -> ProvinceId.parse("ioe_core:temperate_iron"));

        ProvinceId legacy = ProvinceId.parse("ioe_core:temperate_iron", true);

        assertEquals("ioe_core", legacy.namespace());
        assertTrue(legacy.isLegacyNamespace());
        assertFalse(legacy.isConsolidatedNamespace());
    }

    @Test
    void invalidProvinceIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ProvinceId.parse(" "));
        assertThrows(IllegalArgumentException.class, () -> ProvinceId.parse("Immersive_Ore_Expedition:Bad"));
    }
}
