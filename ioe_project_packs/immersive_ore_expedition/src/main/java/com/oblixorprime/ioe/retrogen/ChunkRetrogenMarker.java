package com.oblixorprime.ioe.retrogen;

public record ChunkRetrogenMarker(int version, boolean processed) {
    public ChunkRetrogenMarker {
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
        if (processed && version < 1) {
            throw new IllegalArgumentException("processed marker version must be positive");
        }
    }

    public static ChunkRetrogenMarker missing() {
        return new ChunkRetrogenMarker(0, false);
    }

    public static ChunkRetrogenMarker processed(int version) {
        return new ChunkRetrogenMarker(version, true);
    }

    public boolean currentFor(int targetVersion) {
        if (targetVersion < 1) {
            throw new IllegalArgumentException("targetVersion must be positive");
        }
        return processed && version >= targetVersion;
    }

    public boolean needsRetrogen(int targetVersion) {
        return !currentFor(targetVersion);
    }
}
