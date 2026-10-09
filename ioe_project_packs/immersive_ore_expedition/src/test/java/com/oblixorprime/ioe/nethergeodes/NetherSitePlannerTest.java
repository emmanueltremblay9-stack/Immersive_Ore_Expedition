package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import org.junit.jupiter.api.Test;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;
import static com.oblixorprime.ioe.nethergeodes.NetherSitePlanner.*;

final class NetherSitePlannerTest {
    private static final Cell ROCK = new Cell(Blocks.NETHERRACK.defaultBlockState(), false);
    private static final Cell AIR = new Cell(Blocks.AIR.defaultBlockState(), false);
    private static final Cell LAVA = new Cell(Blocks.LAVA.defaultBlockState(), false);
    private static final int Y = 40;
    private static Candidate candidate(SiteQuality quality, boolean debris) { return new Candidate(8, 8, quality, 73L, debris); }
    private static Cell terrain(BlockPos pos) {
        if (pos.getY() > Y) return AIR;
        return pos.getX() - 8 <= 20 && pos.getY() > Y - 4 ? LAVA : ROCK;
    }
    private static Snapshot snapshot(Function<BlockPos, Cell> cells) {
        return new Snapshot() {
            public boolean nether() { return true; }
            public int minY() { return -32768; }
            public int maxY() { return 128; }
            public Cell at(BlockPos pos) { return cells.apply(pos); }
        };
    }
    @Test void exactEvenWindowAndChunkFootprintAcrossAlignments() {
        assertEquals(74, WIDTH); assertEquals(5476, COLUMNS);
        assertEquals(3286, NetherLakeWindow.MIN_CONNECTED_COLUMNS);
        for (int anchor = -32; anchor < 32; anchor++) {
            int local = Math.floorMod(anchor, 16);
            int chunks = Math.floorDiv(anchor + 36, 16) - Math.floorDiv(anchor - 37, 16) + 1;
            assertEquals(local >= 5 && local <= 11 ? 5 : 6, chunks);
        }
        for (int anchor : new int[]{8, -8}) {
            var visited = new java.util.HashSet<BlockPos>();
            var c = new Candidate(anchor, anchor, SiteQuality.NORMAL, 1, false);
            var result = plan(c, Y, snapshot(pos -> { visited.add(pos); return ROCK; }));
            assertEquals(Status.SURFACE, result.status());
            assertEquals(5476, visited.size());
            assertTrue(visited.contains(new BlockPos(anchor - 37, Y, anchor - 37)));
            assertTrue(visited.contains(new BlockPos(anchor + 36, Y, anchor + 36)));
            assertFalse(visited.contains(new BlockPos(anchor + 37, Y, anchor)));
            assertEquals(25, visited.stream().map(net.minecraft.world.level.ChunkPos::new).distinct().count());
        }
    }
    @Test void denseSurfaceFitsReadQuotaButStillRequiresShore() {
        var result = plan(candidate(SiteQuality.NORMAL, false), Y, snapshot(pos ->
                pos.getY() > Y ? AIR : pos.getY() > Y - 4 ? LAVA : ROCK));
        assertEquals(Status.NO_SHORE, result.status());
        assertEquals(5476, result.connectedDeepColumns());
        assertTrue(result.probes() < NetherAnalysisBudget.READS_PER_TICK);
    }
    @Test void selectionIsStableWithinPositiveAndNegativeRegions() {
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            var a = NetherSitePlanner.candidate(1234L, x, z);
            assertEquals(a, NetherSitePlanner.candidate(1234L, x, z));
            assertEquals(x, Math.floorDiv(a.x(), 256)); assertEquals(z, Math.floorDiv(a.z(), 256));
            assertEquals(8, Math.floorMod(a.x(), 16)); assertEquals(8, Math.floorMod(a.z(), 16));
        }
        assertNotEquals(NetherSitePlanner.candidate(1, 0, 0), NetherSitePlanner.candidate(2, 0, 0));
    }
    @Test void finiteBudgetsAndDebrisReplacementHoldAcrossEveryQuality() {
        for (SiteQuality quality : SiteQuality.values()) {
            var result = plan(candidate(quality, true), Y, snapshot(NetherSitePlannerTest::terrain));
            assertEquals(Status.PLANNED, result.status());
            assertEquals(Y - 4 - 16, result.plan().origin().getY());
            long quartz = result.plan().writes().values().stream().filter(s -> s.is(Blocks.NETHER_QUARTZ_ORE)).count();
            long debris = result.plan().writes().values().stream().filter(s -> s.is(Blocks.ANCIENT_DEBRIS)).count();
            assertEquals(budget(quality), quartz + debris);
            assertEquals(quality == SiteQuality.MOTHERLODE ? 1 : 0, debris);
            assertEquals(result, plan(candidate(quality, true), Y, snapshot(NetherSitePlannerTest::terrain)));
            assertTrue(result.plan().expected().keySet().containsAll(result.plan().writes().keySet()));
            assertTrue(result.plan().writes().size() < NetherPlacementCoordinator.MAX_WRITES);
            assertTrue(result.plan().expected().size() < NetherPlacementCoordinator.MAX_CHECKS);
        }
        for (SiteQuality quality : SiteQuality.values()) {
            int count = 0; for (int roll = 0; roll < 1000; roll++) if (debrisAt(quality, roll)) count++;
            assertEquals(quality == SiteQuality.MOTHERLODE ? 5 : 0, count);
        }
    }
    @Test void requiredShoreCanStillBeTwoChunksFromAnchor() {
        var result = plan(candidate(SiteQuality.NORMAL, false), Y, snapshot(pos ->
                pos.getY() > Y ? AIR : pos.getX() - 8 <= 30 && pos.getY() > Y - 4 ? LAVA : ROCK));
        assertEquals(Status.PLANNED, result.status());
        var marker = result.plan().writes().entrySet().stream()
                .filter(e -> e.getValue().is(Blocks.BLACKSTONE)).map(java.util.Map.Entry::getKey).toList();
        assertEquals(2, marker.size());
        assertTrue(marker.stream().allMatch(p -> Math.floorDiv(p.getX(), 16) == 2));
    }
    @Test void exactlySixtyPercentUsesIntegerBoundary() {
        assertFalse(enoughCoverage(3285)); assertTrue(enoughCoverage(3286));
        for (int size : new int[]{3285, 3286}) {
            var result = plan(candidate(SiteQuality.NORMAL, false), Y, snapshot(pos -> {
                if (pos.getY() > Y) return AIR;
                int index = (pos.getZ() - 8 + 37) * 74 + Math.floorMod(pos.getX() - 8 + 37 - 23, 74);
                return index >= 0 && index < size && pos.getY() > Y - 4 ? LAVA : ROCK;
            }));
            assertEquals(size, result.connectedDeepColumns());
            assertEquals(size == 3285 ? Status.COVERAGE : Status.PLANNED, result.status());
        }
    }
    @Test void separatedPoolsAndFlowingSurfaceDoNotCombine() {
        var split = plan(candidate(SiteQuality.NORMAL, false), Y, snapshot(pos ->
                pos.getX() - 8 == 1 && pos.getY() <= Y ? ROCK : terrain(pos)));
        assertEquals(Status.COVERAGE, split.status());
        var flowing = new Cell(Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 2), false);
        assertEquals(Status.SURFACE, plan(candidate(SiteQuality.NORMAL, false), Y,
                snapshot(pos -> pos.getY() == Y ? flowing : terrain(pos))).status());
    }
    @Test void shallowCoverageAndCoveredLavaAreRejected() {
        assertEquals(Status.COVERAGE, plan(candidate(SiteQuality.NORMAL, false), Y,
                snapshot(pos -> pos.getY() == Y - 3 ? ROCK : terrain(pos))).status());
        assertEquals(Status.SURFACE, plan(candidate(SiteQuality.NORMAL, false), Y,
                snapshot(pos -> pos.getY() == Y + 1 ? LAVA : terrain(pos))).status());
    }
    @Test void actualFloorCrustAndProtectedCellsAreMandatory() {
        var c = candidate(SiteQuality.NORMAL, false);
        assertEquals(Status.FLOOR, plan(c, Y, snapshot(pos -> pos.getY() == Y - 4 ? AIR : terrain(pos))).status());
        BlockPos center = new BlockPos(8, Y - 4 - 16, 8);
        assertEquals(Status.CRUST_OR_PROTECTION, plan(c, Y, snapshot(pos ->
                pos.equals(center.east(6)) ? LAVA : terrain(pos))).status());
        assertEquals(Status.CRUST_OR_PROTECTION, plan(c, Y, snapshot(pos ->
                pos.equals(center) ? new Cell(ROCK.state(), true) : terrain(pos))).status());
        assertEquals(Status.NO_SHORE, plan(c, Y, snapshot(pos ->
                pos.getY() > Y ? AIR : pos.getY() > Y - 4 ? LAVA : ROCK)).status());
    }
    @Test void unknownTerrainAndProbeCapAreFailClosed() {
        var c = candidate(SiteQuality.NORMAL, false);
        assertEquals(Status.UNKNOWN_TERRAIN, plan(c, Y, snapshot(pos -> null)).status());
        var deep = plan(c, Y, snapshot(pos -> pos.getY() > Y ? AIR : pos.getY() > -2000 ? LAVA : ROCK));
        assertEquals(Status.BUDGET, deep.status()); assertEquals(MAX_PROBES, deep.probes());
        Snapshot wrong = new Snapshot() {
            public boolean nether() { return false; }
            public int minY() { return 0; }
            public int maxY() { return 128; }
            public Cell at(BlockPos pos) { fail("Wrong dimension read"); return null; }
        };
        assertEquals(Status.WRONG_DIMENSION, plan(c, Y, wrong).status());
    }
}
