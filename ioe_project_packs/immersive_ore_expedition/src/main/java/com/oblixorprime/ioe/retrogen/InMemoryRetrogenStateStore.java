package com.oblixorprime.ioe.retrogen;

import java.util.Objects;

public final class InMemoryRetrogenStateStore implements RetrogenStateStore {
    private PersistentRetrogenState state;

    public InMemoryRetrogenStateStore(int markerVersion, int maxChunksPerTick) {
        this(PersistentRetrogenState.empty(markerVersion, maxChunksPerTick));
    }

    public InMemoryRetrogenStateStore(PersistentRetrogenState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    @Override
    public synchronized PersistentRetrogenState loadState() {
        return state;
    }

    @Override
    public synchronized void saveState(PersistentRetrogenState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    @Override
    public synchronized PersistentRetrogenChunkMarker markQueued(ChunkKey key, RetrogenMode mode, int markerVersion) {
        PersistentRetrogenChunkMarker marker = PersistentRetrogenChunkMarker.queued(
                key,
                mode,
                markerVersion,
                state.nextSequence()
        );
        state = state.withMarker(marker);
        return marker;
    }

    @Override
    public synchronized PersistentRetrogenChunkMarker markProcessing(ChunkKey key) {
        PersistentRetrogenChunkMarker marker = existingOrCandidate(key).processing(state.nextSequence());
        state = state.withMarker(marker);
        return marker;
    }

    @Override
    public synchronized PersistentRetrogenChunkMarker markProcessed(ChunkKey key) {
        PersistentRetrogenChunkMarker marker = existingOrCandidate(key).processed(state.nextSequence());
        state = state.withMarker(marker);
        return marker;
    }

    @Override
    public synchronized PersistentRetrogenChunkMarker markSkipped(
            ChunkKey key,
            RetrogenMode mode,
            int markerVersion,
            PersistentRetrogenChunkMarker.FailureReason reason,
            String message
    ) {
        PersistentRetrogenChunkMarker marker = existingOrQueued(key, mode, markerVersion)
                .skipped(reason, message, state.nextSequence());
        state = state.withMarker(marker);
        return marker;
    }

    @Override
    public synchronized PersistentRetrogenChunkMarker markFailed(
            ChunkKey key,
            RetrogenMode mode,
            int markerVersion,
            PersistentRetrogenChunkMarker.FailureReason reason,
            String message
    ) {
        PersistentRetrogenChunkMarker marker = existingOrQueued(key, mode, markerVersion)
                .failed(reason, message, state.nextSequence());
        state = state.withMarker(marker);
        return marker;
    }

    @Override
    public synchronized boolean hasProcessed(ChunkKey key, int markerVersion) {
        return state.hasProcessed(key, markerVersion);
    }

    @Override
    public synchronized boolean isQueued(ChunkKey key) {
        return state.isQueued(key);
    }

    @Override
    public synchronized void pause(RetrogenMode mode) {
        state = state.withPaused(true, mode == null ? RetrogenMode.OFF : mode);
    }

    @Override
    public synchronized void resume(RetrogenMode mode) {
        state = state.withPaused(false, Objects.requireNonNull(mode, "mode"));
    }

    @Override
    public synchronized PersistentRetrogenState.StatusSnapshot statusSnapshot() {
        return state.statusSnapshot();
    }

    private PersistentRetrogenChunkMarker existingOrCandidate(ChunkKey key) {
        Objects.requireNonNull(key, "key");
        return state.markerFor(key).orElseGet(() -> PersistentRetrogenChunkMarker.candidate(key));
    }

    private PersistentRetrogenChunkMarker existingOrQueued(ChunkKey key, RetrogenMode mode, int markerVersion) {
        PersistentRetrogenChunkMarker existing = existingOrCandidate(key);
        if (existing.status() == PersistentRetrogenChunkMarker.MarkerStatus.CANDIDATE) {
            return PersistentRetrogenChunkMarker.queued(key, mode, markerVersion, state.nextSequence());
        }
        return existing;
    }
}
