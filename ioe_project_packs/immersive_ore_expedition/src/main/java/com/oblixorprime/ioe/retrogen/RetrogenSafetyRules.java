package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public final class RetrogenSafetyRules {
    private RetrogenSafetyRules() {
    }

    public static Decision evaluateQueueRequest(
            PersistentRetrogenState state,
            ChunkKey key,
            RetrogenMode mode,
            int radiusBlocks,
            boolean chunkLoaded,
            int markerVersion
    ) {
        if (state == null || key == null || mode == null || markerVersion < 1) {
            return Decision.SKIP_INVALID_INPUT;
        }
        if (radiusBlocks < 0 || radiusBlocks > RetrogenController.MAX_ADMIN_RADIUS_BLOCKS) {
            return Decision.SKIP_INVALID_RADIUS;
        }
        if (mode == RetrogenMode.OFF) {
            return Decision.SKIP_DISABLED;
        }
        if (!chunkLoaded) {
            return Decision.SKIP_UNLOADED_CHUNK;
        }
        if (state.hasProcessed(key, markerVersion)) {
            return Decision.SKIP_ALREADY_PROCESSED;
        }
        if (state.isQueued(key)) {
            return Decision.SKIP_ALREADY_QUEUED;
        }
        return Decision.QUEUE_ALLOWED;
    }

    public static Decision evaluateTickAllowed(PersistentRetrogenState state) {
        Objects.requireNonNull(state, "state");
        return state.paused() ? Decision.PAUSED : Decision.QUEUE_ALLOWED;
    }

    public enum Decision {
        QUEUE_ALLOWED,
        SKIP_ALREADY_PROCESSED,
        SKIP_ALREADY_QUEUED,
        SKIP_DISABLED,
        SKIP_UNLOADED_CHUNK,
        SKIP_INVALID_RADIUS,
        SKIP_INVALID_DIMENSION,
        SKIP_INVALID_INPUT,
        PAUSED
    }
}
