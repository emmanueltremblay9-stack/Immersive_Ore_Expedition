package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** First-load capabilities, consumed by an immediate server-thread transaction, never restored from disk. */
final class NetherPlacementCoordinator {
    static final int LEASE_TICKS = 20;
    static final int MAX_LEASES = 256;
    static final int MAX_CHECKS = 16_384;
    static final int MAX_WRITES = 4_096;
    static final int MAX_WRITE_CHUNKS = 4;
    enum Result { BACKEND_UNVERIFIED, COMMITTED, DUPLICATE, INVALID_PLAN, NOT_FRESH, SPACING, BUDGET,
        TERRAIN_CHANGED, ROLLED_BACK, ROLLBACK_INCOMPLETE }
    interface Host {
        void requireServerThread();
        int tick();
        Object loadedChunk(long key);
        BlockState read(BlockPos pos);
        boolean safeToReplace(BlockPos pos, BlockState state);
        boolean write(BlockPos pos, BlockState state);
        boolean reserveReads(int count);
    }
    interface Ledger {
        boolean claim(BlockPos origin);
        boolean hasAcceptedWithin(BlockPos origin, int distance);
        void finish(BlockPos origin, Result result);
    }
    record Plan(BlockPos origin, Map<BlockPos, BlockState> expected, Map<BlockPos, BlockState> writes) {
        Plan {
            origin = origin.immutable();
            expected = immutablePositions(expected);
            writes = immutablePositions(writes);
        }
        private static Map<BlockPos, BlockState> immutablePositions(Map<BlockPos, BlockState> source) {
            var copy = new LinkedHashMap<BlockPos, BlockState>();
            source.forEach((pos, state) -> copy.put(pos.immutable(), Objects.requireNonNull(state)));
            return Collections.unmodifiableMap(copy);
        }
    }
    private record Lease(Object identity, int born) { }
    private final Map<Long, Lease> leases = new HashMap<>();
    private final Map<Object, Boolean> seen = new WeakHashMap<>();

    // Event callbacks record identities only; no level reads or mutations during ChunkEvent.Load.
    synchronized void observe(long chunk, Object identity, boolean isNew, int tick) {
        prune(tick);
        if (seen.put(identity, Boolean.TRUE) != null || !isNew) {
            leases.remove(chunk);
            return;
        }
        if (leases.containsKey(chunk) || leases.size() >= MAX_LEASES) {
            leases.remove(chunk);
            return;
        }
        leases.put(chunk, new Lease(identity, tick));
    }
    synchronized void invalidate(long chunk) { leases.remove(chunk); }
    private void prune(int tick) {
        leases.values().removeIf(lease -> tick - lease.born() < 0 || tick - lease.born() >= LEASE_TICKS);
    }
    private boolean fresh(Host host, long chunk) {
        var lease = leases.get(chunk);
        int age = lease == null ? -1 : host.tick() - lease.born();
        return age > 0 && age < LEASE_TICKS && host.loadedChunk(chunk) == lease.identity();
    }
    private static long chunk(BlockPos pos) { return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4); }

    synchronized Result commit(Host host, Ledger ledger, Plan plan) {
        host.requireServerThread();
        if (!ledger.claim(plan.origin())) return Result.DUPLICATE;
        var chunks = new HashSet<Long>();
        plan.writes().keySet().forEach(pos -> chunks.add(chunk(pos)));
        Result result;
        try {
            if (plan.writes().isEmpty() || plan.writes().size() > MAX_WRITES
                    || plan.expected().size() > MAX_CHECKS || chunks.size() > MAX_WRITE_CHUNKS
                    || !plan.expected().keySet().containsAll(plan.writes().keySet())
                    || plan.writes().values().stream().anyMatch(state -> state.hasBlockEntity()
                            || !state.getFluidState().isEmpty())) {
                result = Result.INVALID_PLAN;
            } else if (!chunks.stream().allMatch(key -> fresh(host, key))) {
                result = Result.NOT_FRESH;
            } else if (ledger.hasAcceptedWithin(plan.origin(), 256)) {
                result = Result.SPACING;
            } else if (!host.reserveReads(plan.expected().size() + 2 * plan.writes().size())) {
                result = Result.BUDGET;
            } else {
                result = apply(host, ledger, plan, chunks);
            }
        } catch (RuntimeException failure) {
            // apply handles any failure after a mutation through its compensation journal.
            result = Result.TERRAIN_CHANGED;
        } finally {
            chunks.forEach(leases::remove); // Single-use even if preflight or compensation failed.
        }
        if (result != Result.COMMITTED) ledger.finish(plan.origin(), result);
        return result;
    }
    private Result apply(Host host, Ledger ledger, Plan plan, Set<Long> chunks) {
        for (var entry : plan.expected().entrySet()) {
            if (host.loadedChunk(chunk(entry.getKey())) == null || !host.read(entry.getKey()).equals(entry.getValue()))
                return Result.TERRAIN_CHANGED;
            if (plan.writes().containsKey(entry.getKey()) && !host.safeToReplace(entry.getKey(), entry.getValue()))
                return Result.TERRAIN_CHANGED;
        }
        var journal = new LinkedHashMap<BlockPos, BlockState>();
        try {
            for (var entry : plan.writes().entrySet()) {
                if (!chunks.stream().allMatch(key -> fresh(host, key))
                        || !host.read(entry.getKey()).equals(plan.expected().get(entry.getKey())))
                    return rollback(host, plan, journal);
                journal.put(entry.getKey(), plan.expected().get(entry.getKey()));
                if (!host.write(entry.getKey(), entry.getValue())) return rollback(host, plan, journal);
            }
            if (!chunks.stream().allMatch(key -> fresh(host, key))) return rollback(host, plan, journal);
            ledger.finish(plan.origin(), Result.COMMITTED);
            return Result.COMMITTED;
        } catch (RuntimeException failure) {
            return rollback(host, plan, journal);
        }
    }
    private Result rollback(Host host, Plan plan, Map<BlockPos, BlockState> journal) {
        boolean restored = true;
        var entries = new ArrayList<>(journal.entrySet());
        Collections.reverse(entries);
        for (var entry : entries) {
            try {
                // Never load/rewrite a replacement or reloaded chunk merely to compensate.
                if (!fresh(host, chunk(entry.getKey()))) { restored = false; continue; }
                var state = host.read(entry.getKey());
                if (state.equals(entry.getValue())) continue;
                if (!state.equals(plan.writes().get(entry.getKey()))) { restored = false; continue; }
                restored &= host.write(entry.getKey(), entry.getValue());
            } catch (RuntimeException failure) { restored = false; }
        }
        return restored ? Result.ROLLED_BACK : Result.ROLLBACK_INCOMPLETE;
    }
}
