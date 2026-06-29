package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public record RetrogenChunkSnapshot(ChunkKey key, boolean explored, ChunkRetrogenMarker marker) {
    public RetrogenChunkSnapshot {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(marker, "marker");
    }
}
