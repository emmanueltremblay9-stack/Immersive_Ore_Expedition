package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public record RetrogenStartResult(
        RetrogenMode mode,
        int acceptedChunks,
        int skippedAlreadyMarked,
        int skippedExplored,
        int skippedOutOfRadius,
        int skippedInvalidCandidates,
        boolean started,
        String reason
) {
    public RetrogenStartResult {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(reason, "reason");
        if (acceptedChunks < 0
                || skippedAlreadyMarked < 0
                || skippedExplored < 0
                || skippedOutOfRadius < 0
                || skippedInvalidCandidates < 0) {
            throw new IllegalArgumentException("chunk counts must not be negative");
        }
        if (started != (acceptedChunks > 0)) {
            throw new IllegalArgumentException("started must match whether chunks were accepted");
        }
    }
}
