package com.oblixorprime.ioe.core;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

import java.util.List;

public final class IoeCoreConfig {
    private IoeCoreConfig() {
    }

    public static boolean existingResourcesOnly() {
        return ImmersiveOreExpeditionConfig.coreExistingResourcesOnly();
    }

    public static boolean allowNewOres() {
        return ImmersiveOreExpeditionConfig.coreAllowNewOres();
    }

    public static boolean skipMissingResources() {
        return ImmersiveOreExpeditionConfig.coreSkipMissingResources();
    }

    public static boolean logMissingResources() {
        return ImmersiveOreExpeditionConfig.coreLogMissingResources();
    }

    public static List<String> allowedResourceCategories() {
        return ImmersiveOreExpeditionConfig.resourcePolicyAllowedCategories();
    }

    public static List<String> deniedResourceCategories() {
        return ImmersiveOreExpeditionConfig.resourcePolicyDeniedCategories();
    }

    public static List<String> excludedResourceNames() {
        return ImmersiveOreExpeditionConfig.resourcePolicyExcludedResources();
    }

    public static boolean resourcePolicyDebugDiagnostics() {
        return ImmersiveOreExpeditionConfig.resourcePolicyDebugDiagnostics();
    }
}
