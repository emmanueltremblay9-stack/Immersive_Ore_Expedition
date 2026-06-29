package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public record RetrogenQueueEntry(ChunkKey key, RetrogenMode mode, int markerVersion) {
    public RetrogenQueueEntry {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mode, "mode");
        if (markerVersion < 1) {
            throw new IllegalArgumentException("markerVersion must be positive");
        }
    }
}
