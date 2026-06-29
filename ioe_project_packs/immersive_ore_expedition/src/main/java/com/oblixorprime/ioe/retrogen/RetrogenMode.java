package com.oblixorprime.ioe.retrogen;

import java.util.Locale;

public enum RetrogenMode {
    OFF("off"),
    UNEXPLORED_CHUNKS_ONLY("unexplored_chunks_only"),
    ADMIN_RADIUS("admin_radius"),
    ORE_POCKET_ONLY("ore_pocket_only"),
    CLUE_PLUS_POCKET("clue_plus_pocket");

    private final String configValue;

    RetrogenMode(String configValue) {
        this.configValue = configValue;
    }

    public String configValue() {
        return configValue;
    }

    public static RetrogenMode fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return OFF;
        }
        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_').trim();
        for (RetrogenMode mode : values()) {
            if (mode.configValue.equals(normalized) || mode.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return mode;
            }
        }
        return OFF;
    }
}
