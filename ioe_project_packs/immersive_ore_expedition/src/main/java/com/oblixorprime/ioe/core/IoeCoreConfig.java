package com.oblixorprime.ioe.core;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

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
}
