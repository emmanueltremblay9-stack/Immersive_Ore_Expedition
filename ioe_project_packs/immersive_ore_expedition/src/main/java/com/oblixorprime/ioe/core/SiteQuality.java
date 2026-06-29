package com.oblixorprime.ioe.core;

public enum SiteQuality {
    DRY,
    POOR,
    NORMAL,
    RICH,
    MOTHERLODE;

    public boolean isProductive() {
        return this != DRY;
    }
}
