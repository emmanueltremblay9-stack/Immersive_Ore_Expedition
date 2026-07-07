package com.oblixorprime.ioe.retrogen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public final class RetrogenController {
    public static final int MAX_ADMIN_RADIUS_BLOCKS = 1024;

    private final int markerVersion;
    private final int maxChunksPerTick;
    private final RetrogenStateStore stateStore;
    private final Queue<RetrogenQueueEntry> queue = new ArrayDeque<>();
    private final Set<ChunkKey> queuedKeys = new HashSet<>();
    private final Map<ChunkKey, ChunkRetrogenMarker> markers = new HashMap<>();
    private boolean paused = true;
    private RetrogenMode mode = RetrogenMode.OFF;

    public RetrogenController(int markerVersion, int maxChunksPerTick) {
        this(markerVersion, maxChunksPerTick, new InMemoryRetrogenStateStore(markerVersion, maxChunksPerTick));
    }

    public RetrogenController(int markerVersion, int maxChunksPerTick, RetrogenStateStore stateStore) {
        if (markerVersion < 1) {
            throw new IllegalArgumentException("markerVersion must be positive");
        }
        if (maxChunksPerTick < 1) {
            throw new IllegalArgumentException("maxChunksPerTick must be positive");
        }
        this.markerVersion = markerVersion;
        this.maxChunksPerTick = maxChunksPerTick;
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        restorePersistentState();
    }

    public static RetrogenController createDefault() {
        return new RetrogenController(IoeRetrogenAdminConfig.chunkMarkerVersion(), IoeRetrogenAdminConfig.maxChunksPerTick());
    }

    public static List<RetrogenChunkSnapshot> placeholderRadiusCandidates(int centerChunkX, int centerChunkZ, int radiusBlocks) {
        validateRadiusBlocks(radiusBlocks);

        int radiusChunks = ChunkKey.radiusBlocksToChunks(radiusBlocks);
        List<RetrogenChunkSnapshot> candidates = new ArrayList<>();
        long minX = Math.max(Integer.MIN_VALUE, (long) centerChunkX - radiusChunks);
        long maxX = Math.min(Integer.MAX_VALUE, (long) centerChunkX + radiusChunks);
        long minZ = Math.max(Integer.MIN_VALUE, (long) centerChunkZ - radiusChunks);
        long maxZ = Math.min(Integer.MAX_VALUE, (long) centerChunkZ + radiusChunks);
        for (long x = minX; x <= maxX; x++) {
            for (long z = minZ; z <= maxZ; z++) {
                ChunkKey key = new ChunkKey((int) x, (int) z);
                if (key.withinRadius(centerChunkX, centerChunkZ, radiusBlocks)) {
                    candidates.add(new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing()));
                }
            }
        }
        return candidates;
    }

    public RetrogenStartResult startAdminRadiusRetrogen(int centerChunkX, int centerChunkZ, int radiusBlocks) {
        return startAdminRadiusRetrogen(centerChunkX, centerChunkZ, radiusBlocks, IoeRetrogenAdminConfig.defaultMode(),
                placeholderRadiusCandidates(centerChunkX, centerChunkZ, radiusBlocks));
    }

    public RetrogenStartResult startAdminRadiusRetrogen(
            int centerChunkX,
            int centerChunkZ,
            int radiusBlocks,
            RetrogenMode requestedMode,
            List<RetrogenChunkSnapshot> candidates
    ) {
        Objects.requireNonNull(requestedMode, "requestedMode");
        Objects.requireNonNull(candidates, "candidates");
        validateRadiusBlocks(radiusBlocks);
        if (requestedMode == RetrogenMode.OFF) {
            return new RetrogenStartResult(requestedMode, 0, 0, 0, 0, 0, false, "retrogen is disabled");
        }

        int accepted = 0;
        int skippedMarked = 0;
        int skippedExplored = 0;
        int skippedOutOfRadius = 0;
        int skippedInvalid = 0;

        for (RetrogenChunkSnapshot candidate : candidates) {
            if (candidate == null) {
                skippedInvalid++;
                continue;
            }
            if (!candidate.key().withinRadius(centerChunkX, centerChunkZ, radiusBlocks)) {
                skippedOutOfRadius++;
                continue;
            }
            if (requestedMode == RetrogenMode.UNEXPLORED_CHUNKS_ONLY && candidate.explored()) {
                skippedExplored++;
                continue;
            }
            ChunkRetrogenMarker storedMarker = markers.getOrDefault(candidate.key(), ChunkRetrogenMarker.missing());
            ChunkRetrogenMarker candidateMarker = candidate.marker();
            if (queuedKeys.contains(candidate.key())
                    || stateStore.isQueued(candidate.key())
                    || stateStore.hasProcessed(candidate.key(), markerVersion)
                    || !storedMarker.needsRetrogen(markerVersion)
                    || (candidateMarker != null && !candidateMarker.needsRetrogen(markerVersion))) {
                skippedMarked++;
                continue;
            }
            RetrogenQueueEntry entry = new RetrogenQueueEntry(candidate.key(), requestedMode, markerVersion);
            queue.add(entry);
            queuedKeys.add(candidate.key());
            stateStore.markQueued(candidate.key(), requestedMode, markerVersion);
            accepted++;
        }

        if (accepted > 0) {
            mode = requestedMode;
            paused = false;
            stateStore.resume(requestedMode);
        }
        return new RetrogenStartResult(requestedMode, accepted, skippedMarked, skippedExplored, skippedOutOfRadius, skippedInvalid,
                accepted > 0, accepted > 0 ? "queued conservative retrogen work" : "no eligible chunks were queued");
    }

    public void pause() {
        paused = true;
        stateStore.pause(mode);
    }

    public List<RetrogenQueueEntry> tickBatch() {
        List<RetrogenQueueEntry> processed = new ArrayList<>();
        if (paused) {
            return processed;
        }

        while (processed.size() < maxChunksPerTick && !queue.isEmpty()) {
            RetrogenQueueEntry entry = queue.remove();
            queuedKeys.remove(entry.key());
            stateStore.markProcessing(entry.key());
            markers.put(entry.key(), ChunkRetrogenMarker.processed(entry.markerVersion()));
            stateStore.markProcessed(entry.key());
            processed.add(entry);
        }

        if (queue.isEmpty()) {
            paused = true;
            mode = RetrogenMode.OFF;
            stateStore.pause(RetrogenMode.OFF);
        }
        return processed;
    }

    public ChunkRetrogenMarker markerFor(ChunkKey key) {
        return markers.getOrDefault(key, ChunkRetrogenMarker.missing());
    }

    public RetrogenStatus status() {
        return new RetrogenStatus(mode, queue.size(), paused, markerVersion, maxChunksPerTick);
    }

    public PersistentRetrogenState.StatusSnapshot persistentStatus() {
        return stateStore.statusSnapshot();
    }

    private void restorePersistentState() {
        PersistentRetrogenState state = stateStore.loadState().withMetadata(markerVersion, maxChunksPerTick);
        stateStore.saveState(state);
        paused = state.paused();
        mode = state.mode();
        for (PersistentRetrogenChunkMarker marker : state.queuedMarkers()) {
            if (!queuedKeys.contains(marker.key())) {
                queue.add(new RetrogenQueueEntry(marker.key(), marker.mode(), marker.markerVersion()));
                queuedKeys.add(marker.key());
            }
        }
        for (PersistentRetrogenChunkMarker marker : state.processedMarkers()) {
            if (marker.processedFor(markerVersion)) {
                markers.put(marker.key(), ChunkRetrogenMarker.processed(marker.markerVersion()));
            }
        }
    }

    private static void validateRadiusBlocks(int radiusBlocks) {
        if (radiusBlocks < 0) {
            throw new IllegalArgumentException("radiusBlocks must not be negative");
        }
        if (radiusBlocks > MAX_ADMIN_RADIUS_BLOCKS) {
            throw new IllegalArgumentException("radiusBlocks must not exceed " + MAX_ADMIN_RADIUS_BLOCKS);
        }
    }
}
