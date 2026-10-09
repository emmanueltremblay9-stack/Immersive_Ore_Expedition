package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class NetherSurfaceSelectionTest {
    private static final NetherSitePlanner.Candidate C = new NetherSitePlanner.Candidate(8, 8, SiteQuality.NORMAL, 73, 999, 0);

    @Test void lowestSurfaceWinsAndAllSelectionObservationsAreRevalidated() {
        for (boolean protectionOnly : new boolean[]{false, true}) {
            var world = new NetherReadSetTest.World();
            for (int y = 70; y <= 73; y++) world.changes.put(new BlockPos(8, y, 8), Blocks.LAVA.defaultBlockState());
            var result = NetherSitePlanner.planLowest(C, world);
            assertEquals(NetherSitePlanner.Status.PLANNED, result.status());
            var plan = result.plan();
            assertEquals(20, plan.origin().getY());
            for (int y = 0; y <= 41; y++) assertTrue(plan.expected().containsKey(new BlockPos(8, y, 8)));
            assertEquals(world.sampled, plan.expected().keySet());
            assertEquals(world.acquisitionReads, plan.acquisitionReads());
            assertEquals(2L * world.acquisitionReads + 2L * plan.writes().size(), plan.candidateReadReservation());
            plan.writes().keySet().stream().map(ChunkPos::new).distinct().forEach(chunk ->
                    world.coordinator.observe(chunk.toLong(), world.chunks.get(chunk.toLong()), true, 0));
            var selectionOnly = new BlockPos(8, 1, 8);
            assertFalse(plan.writes().containsKey(selectionOnly));
            if (protectionOnly) world.protectedCells.add(selectionOnly);
            else world.changes.put(selectionOnly, Blocks.LAVA.defaultBlockState());
            assertEquals(NetherPlacementCoordinator.Result.TERRAIN_CHANGED,
                    world.coordinator.commit(world, new NetherPlacementLedger(), plan));
            assertEquals(0, world.writes);
        }
    }

    @Test void failedLowestPocketDoesNotTryTheValidUpperLake() {
        var world = new NetherReadSetTest.World();
        world.changes.put(new BlockPos(8, 8, 8), Blocks.LAVA.defaultBlockState());
        var result = NetherSitePlanner.planLowest(C, world);
        assertEquals(NetherSitePlanner.Status.COVERAGE, result.status());
        assertNull(result.plan());
        assertTrue(world.sampled.stream().noneMatch(pos -> pos.getY() >= 37));
    }

    @Test void flowingLavaAndSourceCoveredByFlowingLavaAreNotSurfaces() {
        var world = new NetherReadSetTest.World();
        var flowing = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 1);
        world.changes.put(new BlockPos(8, 4, 8), flowing);
        world.changes.put(new BlockPos(8, 6, 8), Blocks.LAVA.defaultBlockState());
        world.changes.put(new BlockPos(8, 7, 8), flowing);
        assertEquals(20, NetherSitePlanner.planLowest(C, world).plan().origin().getY());
    }

    @Test void selectionConsumesSharedBudgetAndCannotSkipUnavailableTerrain() {
        var budget = new NetherAnalysisBudget();
        assertTrue(budget.acquire(1, NetherAnalysisBudget.READS_PER_TICK - 3));
        var identity = new Object();
        var source = new NetherSnapshotDiagnostic.Source() {
            public boolean nether() { return true; }
            public int minY() { return 0; }
            public int maxY() { return 128; }
            public long epoch() { return 1; }
            public Object loaded(long key) { return identity; }
            public NetherSitePlanner.Cell read(Object chunk, BlockPos pos) {
                return new NetherSitePlanner.Cell(Blocks.NETHERRACK.defaultBlockState(), false);
            }
        };
        var capture = new NetherSnapshotDiagnostic.Capture(source, () -> budget.acquire(1));
        var failure = assertThrows(NetherSnapshotDiagnostic.Aborted.class, () -> NetherSitePlanner.planLowest(C, capture));
        assertEquals(NetherSnapshotDiagnostic.State.CAPTURE_BUDGET, failure.state);
        assertEquals(3, capture.reads);
        assertFalse(budget.acquire(1));
        capture.discard();
        var unavailable = new NetherSitePlanner.Snapshot() {
            public boolean nether() { return true; }
            public int minY() { return 0; }
            public int maxY() { return 128; }
            public NetherSitePlanner.Cell at(BlockPos pos) { return null; }
        };
        assertEquals(NetherSitePlanner.Status.UNKNOWN_TERRAIN, NetherSitePlanner.planLowest(C, unavailable).status());
    }
}
