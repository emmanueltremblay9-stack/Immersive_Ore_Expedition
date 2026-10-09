package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** First-load capabilities, consumed by an immediate server-thread transaction, never restored from disk. */
final class NetherPlacementCoordinator {
    static final int LEASE_TICKS = 20;
    static final int MAX_LEASES = 256;
    static final int MAX_CHECKS = NetherSitePlanner.MAX_PROBES;
    static final int MAX_WRITES = 4_096;
    static final int MAX_WRITE_CHUNKS = 4;
    enum Result { BACKEND_UNVERIFIED, COMMITTED, DUPLICATE, INVALID_PLAN, NOT_FRESH, SPACING, BUDGET,
        TERRAIN_CHANGED, ROLLED_BACK, ROLLBACK_INCOMPLETE }
    interface Host {
        void requireServerThread();
        int tick();
        Object loadedChunk(long key);
        BlockState read(BlockPos pos);
        /** Protection observation using the supplied state; must not hide extra unmetered block reads. */
        boolean protectedAt(BlockPos pos, BlockState state);
        boolean safeToReplace(BlockPos pos, BlockState state);
        boolean write(BlockPos pos, BlockState state);
        boolean reserveReads(int count);
    }
    interface Ledger {
        boolean claim(BlockPos origin);
        boolean hasAcceptedWithin(BlockPos origin, int distance);
        void finish(BlockPos origin, Result result);
    }
    record Plan(BlockPos origin, Map<BlockPos, BlockState> expected, Map<BlockPos, BlockState> writes,
                Set<BlockPos> protectedPositions, int acquisitionReads) {
        // Small experimental fixtures already supply their complete observation set.
        Plan(BlockPos origin, Map<BlockPos, BlockState> expected, Map<BlockPos, BlockState> writes) {
            this(origin, expected, writes, Set.of(), expected.size());
        }
        int validationReadReservation() { return expected.size() + 2 * writes.size(); }
        long candidateReadReservation() { return (long) acquisitionReads + validationReadReservation(); }
        int retainedPositionEntries() { return expected.size() + protectedPositions.size() + writes.size(); }
        Plan {
            if (expected.size() > MAX_CHECKS || writes.size() > MAX_WRITES
                    || protectedPositions.size() > expected.size() || !expected.keySet().containsAll(protectedPositions)
                    || acquisitionReads < expected.size() || acquisitionReads > NetherSitePlanner.MAX_PROBES)
                throw new IllegalArgumentException("Unbounded or inconsistent observation set");
            origin = origin.immutable();
            expected = immutablePositions(expected);
            writes = immutablePositions(writes);
            var protectionCopy = new HashSet<BlockPos>();
            protectedPositions.forEach(pos -> protectionCopy.add(pos.immutable()));
            protectedPositions = Collections.unmodifiableSet(protectionCopy);
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
            } else if (plan.candidateReadReservation() > NetherSitePlanner.MAX_PROBES
                    || !host.reserveReads(plan.validationReadReservation())) {
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
            var identity = host.loadedChunk(chunk(entry.getKey()));
            if (identity == null) return Result.TERRAIN_CHANGED;
            var state = host.read(entry.getKey());
            if (!entry.getValue().equals(state)
                    || host.protectedAt(entry.getKey(), state) != plan.protectedPositions().contains(entry.getKey())
                    || host.loadedChunk(chunk(entry.getKey())) != identity)
                return Result.TERRAIN_CHANGED;
            if (plan.writes().containsKey(entry.getKey()) && !host.safeToReplace(entry.getKey(), entry.getValue()))
                return Result.TERRAIN_CHANGED;
        }
        var journal = new LinkedHashMap<BlockPos, BlockState>();
        try {
            for (var entry : plan.writes().entrySet()) {
                if (!chunks.stream().allMatch(key -> fresh(host, key))) return rollback(host, plan, journal);
                var current = host.read(entry.getKey());
                if (!plan.expected().get(entry.getKey()).equals(current)
                        || host.protectedAt(entry.getKey(), current)
                        || !host.safeToReplace(entry.getKey(), current)) return rollback(host, plan, journal);
                // Reads/protection checks may reenter the host; never spend a revoked capability.
                if (!chunks.stream().allMatch(key -> fresh(host, key))) return rollback(host, plan, journal);
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
                if (host.protectedAt(entry.getKey(), state)) { restored = false; continue; }
                if (!state.equals(plan.writes().get(entry.getKey()))) { restored = false; continue; }
                if (!fresh(host, chunk(entry.getKey()))) { restored = false; continue; }
                boolean written = host.write(entry.getKey(), entry.getValue());
                restored &= written && fresh(host, chunk(entry.getKey()));
            } catch (RuntimeException failure) { restored = false; }
        }
        return restored ? Result.ROLLED_BACK : Result.ROLLBACK_INCOMPLETE;
    }
}
