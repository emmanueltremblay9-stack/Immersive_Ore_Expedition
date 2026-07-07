package com.oblixorprime.ioe.retrogen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record PersistentRetrogenState(
        List<PersistentRetrogenChunkMarker> markers,
        boolean paused,
        RetrogenMode mode,
        int markerVersion,
        int maxChunksPerTick,
        long nextSequence
) {
    public PersistentRetrogenState {
        markers = List.copyOf(Objects.requireNonNull(markers, "markers"));
        mode = Objects.requireNonNull(mode, "mode");
        if (markerVersion < 1) {
            throw new IllegalArgumentException("markerVersion must be positive");
        }
        if (maxChunksPerTick < 1) {
            throw new IllegalArgumentException("maxChunksPerTick must be positive");
        }
        if (nextSequence < 1) {
            throw new IllegalArgumentException("nextSequence must be positive");
        }
    }

    public static PersistentRetrogenState empty(int markerVersion, int maxChunksPerTick) {
        return new PersistentRetrogenState(List.of(), true, RetrogenMode.OFF, markerVersion, maxChunksPerTick, 1);
    }

    public List<PersistentRetrogenChunkMarker> queuedMarkers() {
        return markers.stream()
                .filter(PersistentRetrogenChunkMarker::isQueuedLike)
                .toList();
    }

    public List<PersistentRetrogenChunkMarker> processedMarkers() {
        return markersWithStatus(PersistentRetrogenChunkMarker.MarkerStatus.PROCESSED);
    }

    public List<PersistentRetrogenChunkMarker> skippedMarkers() {
        return markersWithStatus(PersistentRetrogenChunkMarker.MarkerStatus.SKIPPED);
    }

    public List<PersistentRetrogenChunkMarker> failedMarkers() {
        return markersWithStatus(PersistentRetrogenChunkMarker.MarkerStatus.FAILED);
    }

    public Optional<PersistentRetrogenChunkMarker> markerFor(ChunkKey key) {
        Objects.requireNonNull(key, "key");
        return markers.stream()
                .filter(marker -> marker.key().equals(key))
                .findFirst();
    }

    public boolean hasProcessed(ChunkKey key, int targetMarkerVersion) {
        return markerFor(key)
                .map(marker -> marker.processedFor(targetMarkerVersion))
                .orElse(false);
    }

    public boolean isQueued(ChunkKey key) {
        return markerFor(key)
                .map(PersistentRetrogenChunkMarker::isQueuedLike)
                .orElse(false);
    }

    public PersistentRetrogenState withMarker(PersistentRetrogenChunkMarker marker) {
        Objects.requireNonNull(marker, "marker");
        List<PersistentRetrogenChunkMarker> updatedMarkers = new ArrayList<>();
        for (PersistentRetrogenChunkMarker existing : markers) {
            if (!existing.sameChunk(marker)) {
                updatedMarkers.add(existing);
            }
        }
        updatedMarkers.add(marker);
        long next = Math.max(nextSequence, Math.max(marker.queuedSequence(), marker.processedSequence()) + 1);
        return new PersistentRetrogenState(updatedMarkers, paused, mode, markerVersion, maxChunksPerTick, next);
    }

    public PersistentRetrogenState withPaused(boolean paused, RetrogenMode mode) {
        return new PersistentRetrogenState(markers, paused, mode, markerVersion, maxChunksPerTick, nextSequence);
    }

    public PersistentRetrogenState withMetadata(int markerVersion, int maxChunksPerTick) {
        return new PersistentRetrogenState(markers, paused, mode, markerVersion, maxChunksPerTick, nextSequence);
    }

    public StatusSnapshot statusSnapshot() {
        return new StatusSnapshot(
                queuedMarkers().size(),
                processedMarkers().size(),
                skippedMarkers().size(),
                failedMarkers().size(),
                paused,
                mode,
                markerVersion,
                maxChunksPerTick
        );
    }

    private List<PersistentRetrogenChunkMarker> markersWithStatus(PersistentRetrogenChunkMarker.MarkerStatus status) {
        return markers.stream()
                .filter(marker -> marker.status() == status)
                .toList();
    }

    public record StatusSnapshot(
            int queuedChunks,
            int processedChunks,
            int skippedChunks,
            int failedChunks,
            boolean paused,
            RetrogenMode mode,
            int markerVersion,
            int maxChunksPerTick
    ) {
        public StatusSnapshot {
            Objects.requireNonNull(mode, "mode");
            if (queuedChunks < 0
                    || processedChunks < 0
                    || skippedChunks < 0
                    || failedChunks < 0
                    || markerVersion < 1
                    || maxChunksPerTick < 1) {
                throw new IllegalArgumentException("persistent retrogen snapshot values must be non-negative and versioned");
            }
        }
    }
}
