package com.oblixorprime.ioe.retrogen;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record PersistentRetrogenChunkMarker(
        int chunkX,
        int chunkZ,
        Optional<ResourceLocation> dimensionId,
        MarkerStatus status,
        RetrogenMode mode,
        int markerVersion,
        long queuedSequence,
        long processedSequence,
        FailureReason failureReason,
        Optional<String> failureMessage,
        int attemptCount
) {
    public PersistentRetrogenChunkMarker {
        dimensionId = dimensionId == null ? Optional.empty() : dimensionId;
        status = Objects.requireNonNull(status, "status");
        mode = Objects.requireNonNull(mode, "mode");
        failureReason = Objects.requireNonNull(failureReason, "failureReason");
        failureMessage = failureMessage == null ? Optional.empty() : failureMessage;
        if (markerVersion < 0) {
            throw new IllegalArgumentException("markerVersion must not be negative");
        }
        if (queuedSequence < 0 || processedSequence < 0) {
            throw new IllegalArgumentException("retrogen marker sequences must not be negative");
        }
        if (attemptCount < 0) {
            throw new IllegalArgumentException("attemptCount must not be negative");
        }
        if ((status == MarkerStatus.QUEUED || status == MarkerStatus.PROCESSING)
                && (markerVersion < 1 || queuedSequence < 1)) {
            throw new IllegalArgumentException("queued retrogen markers require a positive marker version and queue sequence");
        }
        if (status == MarkerStatus.PROCESSED && markerVersion < 1) {
            throw new IllegalArgumentException("processed retrogen markers require a positive marker version");
        }
        if (status == MarkerStatus.PROCESSED && processedSequence < 1) {
            throw new IllegalArgumentException("processed retrogen markers require a processed sequence");
        }
        if ((status == MarkerStatus.FAILED || status == MarkerStatus.SKIPPED)
                && failureReason == FailureReason.NONE) {
            throw new IllegalArgumentException("failed or skipped retrogen markers require a failure reason");
        }
        if (status != MarkerStatus.FAILED
                && status != MarkerStatus.SKIPPED
                && failureReason != FailureReason.NONE) {
            throw new IllegalArgumentException("active retrogen markers must not carry a failure reason");
        }
    }

    public static PersistentRetrogenChunkMarker candidate(ChunkKey key) {
        Objects.requireNonNull(key, "key");
        return new PersistentRetrogenChunkMarker(
                key.x(),
                key.z(),
                Optional.empty(),
                MarkerStatus.CANDIDATE,
                RetrogenMode.OFF,
                0,
                0,
                0,
                FailureReason.NONE,
                Optional.empty(),
                0
        );
    }

    public static PersistentRetrogenChunkMarker queued(
            ChunkKey key,
            RetrogenMode mode,
            int markerVersion,
            long sequence
    ) {
        Objects.requireNonNull(key, "key");
        return new PersistentRetrogenChunkMarker(
                key.x(),
                key.z(),
                Optional.empty(),
                MarkerStatus.QUEUED,
                mode,
                markerVersion,
                sequence,
                0,
                FailureReason.NONE,
                Optional.empty(),
                0
        );
    }

    public PersistentRetrogenChunkMarker processing(long sequence) {
        return new PersistentRetrogenChunkMarker(
                chunkX,
                chunkZ,
                dimensionId,
                MarkerStatus.PROCESSING,
                mode,
                markerVersion,
                queuedSequence == 0 ? sequence : queuedSequence,
                0,
                FailureReason.NONE,
                Optional.empty(),
                attemptCount + 1
        );
    }

    public PersistentRetrogenChunkMarker processed(long sequence) {
        return new PersistentRetrogenChunkMarker(
                chunkX,
                chunkZ,
                dimensionId,
                MarkerStatus.PROCESSED,
                mode,
                markerVersion,
                queuedSequence,
                sequence,
                FailureReason.NONE,
                Optional.empty(),
                Math.max(1, attemptCount)
        );
    }

    public PersistentRetrogenChunkMarker skipped(FailureReason reason, String message, long sequence) {
        return terminal(MarkerStatus.SKIPPED, reason, message, sequence);
    }

    public PersistentRetrogenChunkMarker failed(FailureReason reason, String message, long sequence) {
        return terminal(MarkerStatus.FAILED, reason, message, sequence);
    }

    public ChunkKey key() {
        return new ChunkKey(chunkX, chunkZ);
    }

    public boolean sameChunk(PersistentRetrogenChunkMarker other) {
        return other != null
                && chunkX == other.chunkX
                && chunkZ == other.chunkZ
                && dimensionId.equals(other.dimensionId);
    }

    public boolean isQueuedLike() {
        return status == MarkerStatus.QUEUED || status == MarkerStatus.PROCESSING;
    }

    public boolean processedFor(int targetMarkerVersion) {
        if (targetMarkerVersion < 1) {
            throw new IllegalArgumentException("targetMarkerVersion must be positive");
        }
        return status == MarkerStatus.PROCESSED && markerVersion >= targetMarkerVersion;
    }

    private PersistentRetrogenChunkMarker terminal(
            MarkerStatus terminalStatus,
            FailureReason reason,
            String message,
            long sequence
    ) {
        if (reason == null || reason == FailureReason.NONE) {
            throw new IllegalArgumentException("terminal retrogen markers require a failure reason");
        }
        return new PersistentRetrogenChunkMarker(
                chunkX,
                chunkZ,
                dimensionId,
                terminalStatus,
                mode,
                markerVersion,
                queuedSequence,
                sequence,
                reason,
                Optional.ofNullable(message).filter(value -> !value.isBlank()),
                attemptCount
        );
    }

    public enum MarkerStatus {
        UNKNOWN,
        CANDIDATE,
        QUEUED,
        PROCESSING,
        PROCESSED,
        SKIPPED,
        FAILED,
        PAUSED
    }

    public enum FailureReason {
        NONE,
        RETROGEN_DISABLED,
        UNLOADED_CHUNK,
        INVALID_RADIUS,
        INVALID_DIMENSION,
        INVALID_INPUT,
        STORE_ERROR,
        UNKNOWN
    }
}
