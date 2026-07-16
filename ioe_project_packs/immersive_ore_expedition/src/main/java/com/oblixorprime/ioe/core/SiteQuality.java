package com.oblixorprime.ioe.core;

import java.util.Optional;

public enum SiteQuality {
    DRY,
    POOR,
    NORMAL,
    RICH,
    MOTHERLODE;

    public boolean isProductive() {
        return this != DRY;
    }

    public Optional<SiteQuality> directLower() {
        return switch (this) {
            case MOTHERLODE -> Optional.of(RICH);
            case RICH -> Optional.of(NORMAL);
            case NORMAL -> Optional.of(POOR);
            case POOR -> Optional.of(DRY);
            case DRY -> Optional.empty();
        };
    }
}
