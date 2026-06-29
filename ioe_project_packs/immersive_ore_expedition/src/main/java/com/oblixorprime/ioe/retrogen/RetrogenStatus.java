package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public record RetrogenStatus(RetrogenMode mode, int queuedChunks, boolean paused, int markerVersion, int maxChunksPerTick) {
    public RetrogenStatus {
        Objects.requireNonNull(mode, "mode");
        if (queuedChunks < 0 || markerVersion < 1 || maxChunksPerTick < 1) {
            throw new IllegalArgumentException("retrogen status values must be non-negative and versioned");
        }
    }
}
