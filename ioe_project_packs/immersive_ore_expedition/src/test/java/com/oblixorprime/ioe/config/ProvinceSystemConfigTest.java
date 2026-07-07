package com.oblixorprime.ioe.config;

import com.oblixorprime.ioe.core.IoeCoreConfig;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.worldgen.IoeWorldgenConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceSystemConfigTest {
    @Test
    void defaultProvinceAndResourcePolicyConfigIsConservative() {
        assertEquals("immersive_ore_expedition", IoeWorldgenConfig.provinceNamespace());
        assertFalse(IoeWorldgenConfig.allowLegacyProvinceNamespaces());
        assertFalse(IoeWorldgenConfig.provinceRuntimeIntegrationEnabled());
        assertEquals("immersive_ore_expedition:default", IoeWorldgenConfig.defaultProvince());
        assertTrue(IoeWorldgenConfig.biomeProvinceBindings().isEmpty());
        assertTrue(IoeWorldgenConfig.provinceResourcePolicyRules().isEmpty());
        assertFalse(IoeWorldgenConfig.provinceDebugDiagnostics());

        assertTrue(IoeCoreConfig.allowedResourceCategories().contains("vanilla"));
        assertTrue(IoeCoreConfig.allowedResourceCategories().contains("immersive_engineering"));
        assertTrue(IoeCoreConfig.allowedResourceCategories().contains("ae2"));
        assertTrue(IoeCoreConfig.allowedResourceCategories().contains("geore"));
        assertFalse(IoeCoreConfig.resourcePolicyDebugDiagnostics());
    }

    @Test
    void strictExcludedResourceDefaultsAreComplete() {
        assertEquals(ResourcePolicyService.STRICT_EXCLUDED_RESOURCE_NAMES, IoeCoreConfig.excludedResourceNames());
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Apatite"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Tin"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Forestry Copper"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Platinum"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Osmium"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Tungsten"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Black Quartz"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Uraninite"));
        assertTrue(IoeCoreConfig.excludedResourceNames().contains("Monazite"));
    }
}
