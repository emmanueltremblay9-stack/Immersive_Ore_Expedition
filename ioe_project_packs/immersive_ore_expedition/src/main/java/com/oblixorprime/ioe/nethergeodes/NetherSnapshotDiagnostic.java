package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.*;
import java.util.function.BooleanSupplier;

/** A single server-thread capture turn, never a cross-tick write authorization. */
public final class NetherSnapshotDiagnostic {
    enum State { COMPLETE, WRONG_DIMENSION, UNAVAILABLE, INVALIDATED, CAPTURE_BUDGET }
    interface Source {
        boolean nether();
        int minY();
        int maxY();
        long epoch();
        Object loaded(long chunk);
        NetherSitePlanner.Cell read(Object identity, BlockPos pos);
    }
    private static final class Aborted extends RuntimeException {
        final State state;
        Aborted(State state) { this.state = state; }
    }
    static final class Capture implements NetherSitePlanner.Snapshot {
        final Source source;
        final BooleanSupplier permit;
        final long epoch;
        final Map<Long, Object> identities = new LinkedHashMap<>();
        final Map<BlockPos, NetherSitePlanner.Cell> cells = new LinkedHashMap<>();
        int reads;
        boolean closed;
        Capture(Source source, BooleanSupplier permit) {
            this.source = source; this.permit = permit; this.epoch = source.epoch();
        }
        public boolean nether() { return source.nether(); }
        public int minY() { return source.minY(); }
        public int maxY() { return source.maxY(); }
        public NetherSitePlanner.Cell at(BlockPos pos) {
            if (closed || source.epoch() != epoch) throw new Aborted(State.INVALIDATED);
            long key = new ChunkPos(pos).toLong();
            Object current = source.loaded(key);
            if (current == null) throw new Aborted(identities.containsKey(key) ? State.INVALIDATED : State.UNAVAILABLE);
            Object previous = identities.putIfAbsent(key, current);
            if (previous != null && previous != current) throw new Aborted(State.INVALIDATED);
            var cached = cells.get(pos);
            if (cached != null) return cached;
            if (reads == NetherSitePlanner.MAX_PROBES || !permit.getAsBoolean()) throw new Aborted(State.CAPTURE_BUDGET);
            reads++;
            var cell = source.read(current, pos);
            if (cell == null) throw new Aborted(State.UNAVAILABLE);
            if (source.epoch() != epoch || source.loaded(key) != current) throw new Aborted(State.INVALIDATED);
            cells.put(pos.immutable(), cell);
            return cell;
        }
        void validate() {
            if (closed || source.epoch() != epoch) throw new Aborted(State.INVALIDATED);
            for (var entry : identities.entrySet()) {
                if (source.loaded(entry.getKey()) != entry.getValue()) throw new Aborted(State.INVALIDATED);
            }
        }
        void discard() { closed = true; identities.clear(); cells.clear(); }
    }
    public record Report(String captureStatus, long epoch, int worldReads, int chunks,
                         int candidateX, int candidateZ, String quality, String plannerStatus,
                         int connectedColumns, int plannedWrites) {
        public String message() {
            return "IOE Nether snapshot: window=" + NetherLakeWindow.WIDTH + "x" + NetherLakeWindow.WIDTH
                    + " offsets=[" + NetherLakeWindow.MIN_OFFSET + ",+" + NetherLakeWindow.MAX_OFFSET + "], capture=" + captureStatus + ", epoch=" + epoch
                    + ", worldReads=" + worldReads + ", chunks=" + chunks
                    + ", candidate=" + candidateX + "," + candidateZ + ", quality=" + quality
                    + ", planner=" + plannerStatus + ", connectedDeepColumns=" + connectedColumns
                    + ", plannedWrites=" + plannedWrites
                    + ". Single server-thread capture turn; no cross-tick chunk revision claim."
                    + " External claim protection NOT_EVALUATED; production backend BLOCKED. No world mutation or forced loading.";
        }
    }
    static Report inspect(Source source, BooleanSupplier permit, NetherSitePlanner.Candidate candidate, int surfaceY) {
        var capture = new Capture(source, permit);
        try {
            if (!source.nether()) return report(State.WRONG_DIMENSION, capture, candidate, null);
            var outcome = NetherSitePlanner.plan(candidate, surfaceY, capture);
            capture.validate();
            return report(State.COMPLETE, capture, candidate, outcome);
        } catch (Aborted failure) {
            return report(failure.state, capture, candidate, null);
        } finally { capture.discard(); }
    }
    private static Report report(State state, Capture capture, NetherSitePlanner.Candidate candidate,
                                 NetherSitePlanner.Outcome outcome) {
        return new Report(state.name(), capture.epoch, capture.reads, capture.identities.size(),
                candidate.x(), candidate.z(), candidate.quality().name(), outcome == null ? "NOT_EVALUATED" : outcome.status().name(),
                outcome == null ? 0 : outcome.connectedDeepColumns(),
                outcome == null || outcome.plan() == null ? 0 : outcome.plan().writes().size());
    }
    static Source source(ServerLevel level) {
        return new Source() {
            public boolean nether() { return level.dimension().equals(Level.NETHER); }
            public int minY() { return level.getMinBuildHeight(); }
            public int maxY() { return level.getMaxBuildHeight(); }
            public long epoch() { return level.getServer().getTickCount(); }
            public Object loaded(long key) {
                var pos = new ChunkPos(key);
                return level.getChunkSource().getChunkNow(pos.x, pos.z);
            }
            public NetherSitePlanner.Cell read(Object identity, BlockPos pos) {
                var state = ((LevelChunk) identity).getBlockState(pos);
                return new NetherSitePlanner.Cell(state, state.hasBlockEntity());
            }
        };
    }
    public static Report inspect(ServerLevel level, BlockPos origin, int surfaceY) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Server thread required");
        var candidate = NetherSitePlanner.candidate(level.getSeed(), Math.floorDiv(origin.getX(), 256), Math.floorDiv(origin.getZ(), 256));
        int tick = level.getServer().getTickCount();
        return inspect(source(level), () -> NetherAnalysisBudget.forServer(level.getServer()).acquire(tick), candidate, surfaceY);
    }
}
