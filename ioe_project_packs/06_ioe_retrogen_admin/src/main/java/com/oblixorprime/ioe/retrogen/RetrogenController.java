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
    private final int markerVersion;
    private final int maxChunksPerTick;
    private final Queue<RetrogenQueueEntry> queue = new ArrayDeque<>();
    private final Set<ChunkKey> queuedKeys = new HashSet<>();
    private final Map<ChunkKey, ChunkRetrogenMarker> markers = new HashMap<>();
    private boolean paused = true;
    private RetrogenMode mode = RetrogenMode.OFF;

    public RetrogenController(int markerVersion, int maxChunksPerTick) {
        if (markerVersion < 1) {
            throw new IllegalArgumentException("markerVersion must be positive");
        }
        if (maxChunksPerTick < 1) {
            throw new IllegalArgumentException("maxChunksPerTick must be positive");
        }
        this.markerVersion = markerVersion;
        this.maxChunksPerTick = maxChunksPerTick;
    }

    public static RetrogenController createDefault() {
        return new RetrogenController(IoeRetrogenAdminConfig.chunkMarkerVersion(), IoeRetrogenAdminConfig.maxChunksPerTick());
    }

    public static List<RetrogenChunkSnapshot> placeholderRadiusCandidates(int centerChunkX, int centerChunkZ, int radiusBlocks) {
        if (radiusBlocks < 0) {
            throw new IllegalArgumentException("radiusBlocks must not be negative");
        }

        int radiusChunks = ChunkKey.radiusBlocksToChunks(radiusBlocks);
        List<RetrogenChunkSnapshot> candidates = new ArrayList<>();
        for (int x = centerChunkX - radiusChunks; x <= centerChunkX + radiusChunks; x++) {
            for (int z = centerChunkZ - radiusChunks; z <= centerChunkZ + radiusChunks; z++) {
                ChunkKey key = new ChunkKey(x, z);
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
        if (radiusBlocks < 0) {
            throw new IllegalArgumentException("radiusBlocks must not be negative");
        }
        if (requestedMode == RetrogenMode.OFF) {
            return new RetrogenStartResult(requestedMode, 0, 0, 0, 0, false, "retrogen is disabled");
        }

        int accepted = 0;
        int skippedMarked = 0;
        int skippedExplored = 0;
        int skippedOutOfRadius = 0;

        for (RetrogenChunkSnapshot candidate : candidates) {
            if (!candidate.key().withinRadius(centerChunkX, centerChunkZ, radiusBlocks)) {
                skippedOutOfRadius++;
                continue;
            }
            if (requestedMode == RetrogenMode.UNEXPLORED_CHUNKS_ONLY && candidate.explored()) {
                skippedExplored++;
                continue;
            }
            ChunkRetrogenMarker marker = candidate.marker() == null
                    ? markers.getOrDefault(candidate.key(), ChunkRetrogenMarker.missing())
                    : candidate.marker();
            if (!marker.needsRetrogen(markerVersion) || queuedKeys.contains(candidate.key())) {
                skippedMarked++;
                continue;
            }
            RetrogenQueueEntry entry = new RetrogenQueueEntry(candidate.key(), requestedMode, markerVersion);
            queue.add(entry);
            queuedKeys.add(candidate.key());
            accepted++;
        }

        if (accepted > 0) {
            mode = requestedMode;
            paused = false;
        }
        return new RetrogenStartResult(requestedMode, accepted, skippedMarked, skippedExplored, skippedOutOfRadius,
                accepted > 0, accepted > 0 ? "queued conservative retrogen work" : "no eligible chunks were queued");
    }

    public void pause() {
        paused = true;
    }

    public List<RetrogenQueueEntry> tickBatch() {
        List<RetrogenQueueEntry> processed = new ArrayList<>();
        if (paused) {
            return processed;
        }

        while (processed.size() < maxChunksPerTick && !queue.isEmpty()) {
            RetrogenQueueEntry entry = queue.remove();
            queuedKeys.remove(entry.key());
            markers.put(entry.key(), ChunkRetrogenMarker.processed(entry.markerVersion()));
            processed.add(entry);
        }

        if (queue.isEmpty()) {
            paused = true;
        }
        return processed;
    }

    public ChunkRetrogenMarker markerFor(ChunkKey key) {
        return markers.getOrDefault(key, ChunkRetrogenMarker.missing());
    }

    public RetrogenStatus status() {
        return new RetrogenStatus(mode, queue.size(), paused, markerVersion, maxChunksPerTick);
    }
}
