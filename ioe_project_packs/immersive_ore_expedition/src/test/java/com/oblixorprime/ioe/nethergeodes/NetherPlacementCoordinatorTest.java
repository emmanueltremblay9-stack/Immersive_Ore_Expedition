package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.oblixorprime.ioe.nethergeodes.NetherPlacementCoordinator.*;

final class NetherPlacementCoordinatorTest {
    private static final BlockPos A = new BlockPos(8, 30, 8), B = new BlockPos(24, 30, 8);
    private static final BlockState ROCK = Blocks.NETHERRACK.defaultBlockState(), ORE = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
    private static long key(BlockPos p) { return new ChunkPos(p).toLong(); }
    private static Plan plan() {
        var checks = new LinkedHashMap<BlockPos, BlockState>(); checks.put(A, ROCK); checks.put(B, ROCK);
        var writes = new LinkedHashMap<BlockPos, BlockState>(); writes.put(A, ORE); writes.put(B, ORE);
        return new Plan(A, checks, writes);
    }
    private static final class Fixture implements Host {
        final NetherPlacementCoordinator coordinator = new NetherPlacementCoordinator();
        final Map<Long, Object> chunks = new HashMap<>();
        final Map<BlockPos, BlockState> blocks = new HashMap<>(Map.of(A, ROCK, B, ROCK));
        final NetherAnalysisBudget budget = new NetherAnalysisBudget();
        int tick = 1, mutations, reads, failWrite = -1;
        boolean unsafe, replaceDuringWrite;
        int invalidateOnRead = -1;
        Fixture(boolean reverse) {
            for (var pos : reverse ? List.of(B, A) : List.of(A, B)) {
                var identity = new Object(); chunks.put(key(pos), identity);
                coordinator.observe(key(pos), identity, true, 0);
            }
        }
        public void requireServerThread() { }
        public int tick() { return tick; }
        public Object loadedChunk(long key) { return chunks.get(key); }
        public BlockState read(BlockPos pos) {
            if (++reads == invalidateOnRead) coordinator.invalidate(key(A));
            return blocks.get(pos);
        }
        public boolean protectedAt(BlockPos pos, BlockState state) { return false; }
        public boolean safeToReplace(BlockPos pos, BlockState state) { return !unsafe; }
        public boolean write(BlockPos pos, BlockState state) {
            mutations++;
            if (mutations == failWrite) return false;
            blocks.put(pos, state);
            if (replaceDuringWrite && mutations == 1) chunks.put(key(B), new Object());
            return true;
        }
        public boolean reserveReads(int count) { return budget.acquire(tick, count); }
    }
    @Test void twoFreshChunksCommitInEitherLoadOrderAndCannotReplay() {
        for (boolean reverse : new boolean[]{false, true}) {
            var f = new Fixture(reverse); var ledger = new NetherPlacementLedger();
            assertEquals(Result.COMMITTED, f.coordinator.commit(f, ledger, plan()));
            assertEquals(Map.of(A, ORE, B, ORE), f.blocks);
            assertEquals(Result.DUPLICATE, f.coordinator.commit(f, ledger, plan()));
            assertEquals(2, f.mutations);
        }
    }
    @Test void oldAdjacentAndReplacedChunksCannotReceiveWrites() {
        for (boolean old : new boolean[]{true, false}) {
            var f = new Fixture(false);
            if (old) f.coordinator.observe(key(B), f.chunks.get(key(B)), false, 0);
            else f.chunks.put(key(B), new Object());
            assertEquals(Result.NOT_FRESH, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
            assertEquals(0, f.mutations);
        }
    }
    @Test void sameTickExpiryUnloadAndRepeatedLoadAreTerminal() {
        for (int scenario = 0; scenario < 4; scenario++) {
            var f = new Fixture(false); var ledger = new NetherPlacementLedger();
            if (scenario == 0) f.tick = 0;
            if (scenario == 1) f.tick = LEASE_TICKS;
            if (scenario == 2) f.coordinator.invalidate(key(B));
            if (scenario == 3) f.coordinator.observe(key(B), f.chunks.get(key(B)), true, 1);
            assertEquals(Result.NOT_FRESH, f.coordinator.commit(f, ledger, plan()));
            f.tick = 2;
            assertEquals(Result.DUPLICATE, f.coordinator.commit(f, ledger, plan()));
            assertEquals(0, f.mutations);
        }
    }
    @Test void terrainChangesAndProtectionRejectBeforeAnyMutation() {
        for (boolean protection : new boolean[]{false, true}) {
            var f = new Fixture(false); f.unsafe = protection;
            if (!protection) f.blocks.put(B, Blocks.CHEST.defaultBlockState());
            assertEquals(Result.TERRAIN_CHANGED, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
            assertEquals(0, f.mutations);
        }
    }
    @Test void sharedBudgetMustBeReservedBeforeAnyReadOrMutation() {
        var f = new Fixture(false);
        assertTrue(f.budget.acquire(f.tick, NetherAnalysisBudget.READS_PER_TICK - 5));
        assertEquals(Result.BUDGET, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
        assertEquals(0, f.reads); assertEquals(0, f.mutations);
    }
    @Test void failedSecondWriteCompensatesFirstAndConsumesBothLeases() {
        var f = new Fixture(false); f.failWrite = 2;
        assertEquals(Result.ROLLED_BACK, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
        assertEquals(Map.of(A, ROCK, B, ROCK), f.blocks);
        assertEquals(Result.NOT_FRESH, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
    }
    @Test void chunkReplacementDuringMutationNeverWritesReplacementChunk() {
        var f = new Fixture(false); f.replaceDuringWrite = true;
        assertEquals(Result.ROLLED_BACK, f.coordinator.commit(f, new NetherPlacementLedger(), plan()));
        assertEquals(Map.of(A, ROCK, B, ROCK), f.blocks);
        assertEquals(2, f.mutations); // original A write and A compensation only
    }
    @Test void failedCompensationIsReportedAndNeverRetriedOnReload() {
        var f = new Fixture(false);
        Host host = new Host() {
            public void requireServerThread() { }
            public int tick() { return f.tick; }
            public Object loadedChunk(long key) { return f.loadedChunk(key); }
            public BlockState read(BlockPos pos) { return f.read(pos); }
            public boolean protectedAt(BlockPos pos, BlockState state) { return false; }
            public boolean safeToReplace(BlockPos pos, BlockState state) { return true; }
            public boolean reserveReads(int count) { return f.reserveReads(count); }
            public boolean write(BlockPos pos, BlockState state) {
                if (pos.equals(B)) { f.coordinator.invalidate(key(A)); return false; }
                return f.write(pos, state);
            }
        };
        var ledger = new NetherPlacementLedger();
        assertEquals(Result.ROLLBACK_INCOMPLETE, f.coordinator.commit(host, ledger, plan()));
        assertEquals(ORE, f.blocks.get(A)); // No forbidden late rollback into invalidated chunk.
        var loaded = NetherPlacementLedger.FACTORY.deserializer().apply(ledger.save(new CompoundTag(), null), null);
        assertFalse(loaded.claim(A));
        assertTrue(loaded.hasAcceptedWithin(A, 256));
    }
    @Test void interruptedClaimAndAcceptedSpacingSurviveSaveReload() {
        var ledger = new NetherPlacementLedger();
        assertTrue(ledger.claim(A));
        var interrupted = NetherPlacementLedger.FACTORY.deserializer().apply(ledger.save(new CompoundTag(), null), null);
        assertFalse(interrupted.claim(A.east()));
        ledger.finish(A, Result.COMMITTED);
        var loaded = NetherPlacementLedger.FACTORY.deserializer().apply(ledger.save(new CompoundTag(), null), null);
        assertTrue(loaded.hasAcceptedWithin(A.offset(255, 0, 0), 256));
        assertFalse(loaded.hasAcceptedWithin(A.offset(256, 0, 0), 256));
        assertTrue(loaded.claim(new BlockPos(-1, 30, 8))); // floor division, distinct negative region
        assertFalse(loaded.claim(new BlockPos(-256, 30, 8)));
    }
    @Test void retainedIdentityCapacityNeverEvictsIntoAWritePermission() {
        var f = new Fixture(false);
        for (int i = 2; i <= MAX_LEASES; i++) {
            var identity = new Object(); long key = ChunkPos.asLong(i, 0);
            f.chunks.put(key, identity);
            f.coordinator.observe(key, identity, true, 0);
        }
        var beyond = new BlockPos(MAX_LEASES * 16 + 8, 30, 8);
        var plan = new Plan(beyond, Map.of(beyond, ROCK), Map.of(beyond, ORE));
        assertEquals(Result.NOT_FRESH, f.coordinator.commit(f, new NetherPlacementLedger(), plan));
        assertEquals(0, f.mutations);
    }
    @Test void plansCannotWriteUncheckedPositionsOrFluidTargets() {
        var f = new Fixture(false);
        assertEquals(Result.INVALID_PLAN, f.coordinator.commit(f, new NetherPlacementLedger(),
                new Plan(A, Map.of(A, ROCK), Map.of(B, ORE))));
        f = new Fixture(false);
        assertEquals(Result.INVALID_PLAN, f.coordinator.commit(f, new NetherPlacementLedger(),
                new Plan(A, Map.of(A, ROCK), Map.of(A, Blocks.LAVA.defaultBlockState()))));
        assertEquals(0, f.mutations);
    }

    @Test void invalidationDuringImmediateReadCannotSpendRevokedWritePermission() {
        var f = new Fixture(false); f.invalidateOnRead = 3;
        var ledger = new NetherPlacementLedger();
        assertEquals(Result.ROLLED_BACK, f.coordinator.commit(f, ledger, plan()));
        assertEquals(0, f.mutations);
        assertEquals(Map.of(A, ROCK, B, ROCK), f.blocks);
        assertEquals(Result.DUPLICATE, f.coordinator.commit(f, ledger, plan()));
    }

}
