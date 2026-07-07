package com.oblixorprime.ioe.retrogen;

public interface RetrogenStateStore {
    PersistentRetrogenState loadState();

    void saveState(PersistentRetrogenState state);

    PersistentRetrogenChunkMarker markQueued(ChunkKey key, RetrogenMode mode, int markerVersion);

    PersistentRetrogenChunkMarker markProcessing(ChunkKey key);

    PersistentRetrogenChunkMarker markProcessed(ChunkKey key);

    PersistentRetrogenChunkMarker markSkipped(
            ChunkKey key,
            RetrogenMode mode,
            int markerVersion,
            PersistentRetrogenChunkMarker.FailureReason reason,
            String message
    );

    PersistentRetrogenChunkMarker markFailed(
            ChunkKey key,
            RetrogenMode mode,
            int markerVersion,
            PersistentRetrogenChunkMarker.FailureReason reason,
            String message
    );

    boolean hasProcessed(ChunkKey key, int markerVersion);

    boolean isQueued(ChunkKey key);

    void pause(RetrogenMode mode);

    void resume(RetrogenMode mode);

    PersistentRetrogenState.StatusSnapshot statusSnapshot();
}
