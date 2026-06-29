package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;

public final class IoeRetrogenAdminConfig {
    private IoeRetrogenAdminConfig() {
    }

    public static boolean enabled() {
        return ImmersiveOreExpeditionConfig.retrogenEnabled();
    }

    public static RetrogenMode defaultMode() {
        return ImmersiveOreExpeditionConfig.retrogenDefaultMode();
    }

    public static boolean requireAdminCommand() {
        return ImmersiveOreExpeditionConfig.retrogenRequireAdminCommand();
    }

    public static int chunkMarkerVersion() {
        return ImmersiveOreExpeditionConfig.retrogenChunkMarkerVersion();
    }

    public static int maxChunksPerTick() {
        return ImmersiveOreExpeditionConfig.retrogenMaxChunksPerTick();
    }

    public static boolean modeAllowed(RetrogenMode mode) {
        return ImmersiveOreExpeditionConfig.retrogenModeAllowed(mode);
    }

    public static boolean fullProvinceAllowed() {
        return ImmersiveOreExpeditionConfig.retrogenFullProvinceAllowed();
    }

    public static boolean commandLocateProvinceEnabled() {
        return ImmersiveOreExpeditionConfig.retrogenCommandLocateProvinceEnabled();
    }

    public static boolean commandLocateAnchorEnabled() {
        return ImmersiveOreExpeditionConfig.retrogenCommandLocateAnchorEnabled();
    }

    public static boolean commandRetrogenStatusEnabled() {
        return ImmersiveOreExpeditionConfig.retrogenCommandRetrogenStatusEnabled();
    }

    public static boolean commandRetrogenStartEnabled() {
        return ImmersiveOreExpeditionConfig.retrogenCommandRetrogenStartEnabled();
    }

    public static boolean commandRetrogenPauseEnabled() {
        return ImmersiveOreExpeditionConfig.retrogenCommandRetrogenPauseEnabled();
    }
}
