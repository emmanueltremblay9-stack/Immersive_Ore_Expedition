package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

final class NetherSnapshotDiagnosticTest {
    private static final int Y = 40;
    private static final NetherSitePlanner.Candidate CANDIDATE = new NetherSitePlanner.Candidate(8, 8, SiteQuality.NORMAL, 73, false);
    private static final class World implements NetherSnapshotDiagnostic.Source {
        final Map<Long, Object> identities = new HashMap<>();
        int reads, identityLookups;
        long tick = 7;
        boolean nether = true, available = true, changeEpoch, replaceIdentity;
        public boolean nether() { return nether; }
        public int minY() { return 0; }
        public int maxY() { return 128; }
        public long epoch() { return tick; }
        public Object loaded(long key) {
            identityLookups++;
            return available ? identities.computeIfAbsent(key, ignored -> new Object()) : null;
        }
        public NetherSitePlanner.Cell read(Object identity, BlockPos pos) {
            reads++;
            if (changeEpoch && reads == 10) tick++;
            if (replaceIdentity && reads == 10) identities.clear();
            int index = (pos.getZ() - 8 + 37) * 74 + Math.floorMod(pos.getX() - 8 + 37 - 23, 74);
            var state = pos.getY() > Y ? Blocks.AIR.defaultBlockState()
                    : index >= 0 && index < 3286 && pos.getY() > Y - 4
                    ? Blocks.LAVA.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
            return new NetherSitePlanner.Cell(state, false);
        }
    }
    @Test void capturesAndPlansWithOneSharedBudgetAndNoPartialPlanEscapes() {
        var world = new World(); var budget = new NetherAnalysisBudget();
        var report = NetherSnapshotDiagnostic.inspect(world, () -> budget.acquire((int) world.tick), CANDIDATE, Y);
        assertEquals("COMPLETE", report.captureStatus());
        assertEquals("PLANNED", report.plannerStatus());
        assertEquals(3286, report.connectedColumns());
        assertTrue(report.plannedWrites() > 0);
        assertEquals(world.reads, report.worldReads());
        assertTrue(report.worldReads() < NetherAnalysisBudget.READS_PER_TICK);
        assertTrue(budget.acquire((int) world.tick, NetherAnalysisBudget.READS_PER_TICK - report.worldReads() - 10));
        var second = NetherSnapshotDiagnostic.inspect(world, () -> budget.acquire((int) world.tick), CANDIDATE, Y);
        assertEquals("CAPTURE_BUDGET", second.captureStatus());
        assertEquals("NOT_EVALUATED", second.plannerStatus()); assertEquals(0, second.plannedWrites());
        assertEquals(10, second.worldReads());
    }
    @Test void rejectedDimensionsAndMissingChunksCannotReadOrForceLoad() {
        var world = new World(); world.nether = false;
        assertEquals("WRONG_DIMENSION", NetherSnapshotDiagnostic.inspect(world, () -> true, CANDIDATE, Y).captureStatus());
        assertEquals(0, world.identityLookups); assertEquals(0, world.reads);
        world.nether = true; world.available = false;
        assertEquals("UNAVAILABLE", NetherSnapshotDiagnostic.inspect(world, () -> true, CANDIDATE, Y).captureStatus());
        assertEquals(0, world.reads);
    }
    @Test void epochChangesAndReplacementIdentitiesAbortWithoutResult() {
        for (boolean epoch : new boolean[]{true, false}) {
            var world = new World(); world.changeEpoch = epoch; world.replaceIdentity = !epoch;
            var report = NetherSnapshotDiagnostic.inspect(world, () -> true, CANDIDATE, Y);
            assertEquals("INVALIDATED", report.captureStatus());
            assertEquals(10, report.worldReads()); assertEquals(0, report.plannedWrites());
        }
    }
    @Test void unloadAndDiscardCannotBeUsedAsAReusableSnapshot() {
        var world = new World(); var capture = new NetherSnapshotDiagnostic.Capture(world, () -> true);
        BlockPos pos = new BlockPos(8, 40, 8);
        capture.at(pos); capture.at(pos); assertEquals(1, world.reads);
        world.available = false;
        assertThrows(RuntimeException.class, capture::validate);
        capture.discard(); world.available = true;
        assertThrows(RuntimeException.class, () -> capture.at(pos));
        assertTrue(capture.cells.isEmpty()); assertTrue(capture.identities.isEmpty());
        var reconnected = new NetherSnapshotDiagnostic.Capture(new World(), () -> true);
        assertNotNull(reconnected.at(pos)); reconnected.validate(); reconnected.discard();
    }
    @Test void deniedGlobalBudgetReadsNothingAndFreshRequestDoesNotResumeOldData() {
        var world = new World();
        var denied = NetherSnapshotDiagnostic.inspect(world, () -> false, CANDIDATE, Y);
        assertEquals("CAPTURE_BUDGET", denied.captureStatus()); assertEquals(0, world.reads);
        world.tick++;
        var fresh = NetherSnapshotDiagnostic.inspect(world, () -> true, CANDIDATE, Y);
        assertEquals("COMPLETE", fresh.captureStatus()); assertEquals(8, fresh.epoch());
    }
    @Test void candidateCaptureLimitIsIndependentOfGlobalPermit() {
        var world = new World(); var capture = new NetherSnapshotDiagnostic.Capture(world, () -> true);
        for (int i = 0; i < NetherSitePlanner.MAX_PROBES; i++) {
            capture.at(new BlockPos(i % 512, 40, i / 512));
        }
        assertThrows(RuntimeException.class, () -> capture.at(new BlockPos(513, 40, 513)));
        assertEquals(NetherSitePlanner.MAX_PROBES, world.reads);
        capture.discard();
    }
    @Test void finalValidationRejectsReplacementOfAnEarlierChunk() {
        var world = new World(); var capture = new NetherSnapshotDiagnostic.Capture(world, () -> true);
        capture.at(new BlockPos(8, 40, 8));
        capture.at(new BlockPos(40, 40, 40));
        world.identities.put(new net.minecraft.world.level.ChunkPos(0, 0).toLong(), new Object());
        assertThrows(RuntimeException.class, capture::validate);
        capture.discard();
    }

}
