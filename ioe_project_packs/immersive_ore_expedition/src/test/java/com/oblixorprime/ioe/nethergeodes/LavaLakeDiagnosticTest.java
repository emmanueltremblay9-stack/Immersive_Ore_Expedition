package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;
import static com.oblixorprime.ioe.nethergeodes.LavaLakeDiagnostic.*;

final class LavaLakeDiagnosticTest {
    private static View view(ResourceKey<Level> dimension, Function<BlockPos, Cell> cells) {
        return new View() {
            public ResourceKey<Level> dimension() { return dimension; }
            public int minY() { return 0; }
            public int maxY() { return 128; }
            public Cell read(BlockPos pos) { return cells.apply(pos); }
        };
    }

    @Test void measuresPlaneCoverageAndContiguousDepthWithoutSearchingBelowDryColumns() {
        var report = scan(view(Level.NETHER, pos -> pos.getX() == 0 && pos.getY() >= 9
                ? Cell.LAVA : Cell.OTHER), new BlockPos(0, 10, 0), 1, 4);
        assertEquals(Status.COMPLETE, report.status());
        assertEquals(9, report.completedColumns());
        assertEquals(3, report.lavaColumns());
        assertEquals(2, report.minimumObservedDepth());
        assertEquals(2, report.maximumObservedDepth());
        assertEquals(0, report.depthCappedColumns());
        assertEquals(15, report.reads());
    }

    @Test void dryAndDepthLimitedSamplesHaveUnambiguousCounters() {
        var dry = scan(view(Level.NETHER, pos -> Cell.OTHER), new BlockPos(0, 10, 0), 1, 4);
        assertEquals(0, dry.lavaColumns());
        assertEquals(0, dry.minimumObservedDepth());
        assertEquals(9, dry.reads());
        var deep = scan(view(Level.NETHER, pos -> Cell.LAVA), new BlockPos(0, 10, 0), 1, 4);
        assertEquals(9, deep.depthCappedColumns());
        assertEquals(4, deep.minimumObservedDepth());
        assertEquals(36, deep.reads());
    }

    @Test void invalidDimensionAndHeightNeverReadTheWorld() {
        Function<BlockPos, Cell> forbidden = pos -> { fail("World accessed on rejected request"); return Cell.OTHER; };
        assertEquals(Status.WRONG_DIMENSION, scan(view(Level.OVERWORLD, forbidden), new BlockPos(0, 10, 0), 1, 4).status());
        assertEquals(Status.WRONG_DIMENSION, scan(view(Level.END, forbidden), new BlockPos(0, 10, 0), 1, 4).status());
        assertEquals(Status.OUTSIDE_HEIGHT, scan(view(Level.NETHER, forbidden), new BlockPos(0, 2, 0), 1, 4).status());
        assertEquals(Status.OUTSIDE_HEIGHT, scan(view(Level.NETHER, forbidden), new BlockPos(0, 128, 0), 1, 1).status());
        assertEquals(Status.COMPLETE, scan(view(Level.NETHER, pos -> Cell.LAVA), new BlockPos(0, 3, 0), 1, 4).status());
    }

    @Test void unavailableCellsStopImmediatelyAndExcludeIncompleteColumns() {
        AtomicInteger reads = new AtomicInteger();
        var report = scan(view(Level.NETHER, pos -> reads.incrementAndGet() == 3 ? Cell.UNLOADED : Cell.LAVA),
                new BlockPos(0, 10, 0), 1, 4);
        assertEquals(Status.UNLOADED, report.status());
        assertEquals(3, reads.get());
        assertEquals(0, report.completedColumns());
        assertEquals(0, report.lavaColumns());
    }

    @Test void maximumRequestStopsAtHardReadBudget() {
        AtomicInteger reads = new AtomicInteger();
        var report = scan(view(Level.NETHER, pos -> { reads.incrementAndGet(); return Cell.LAVA; }),
                new BlockPos(-16, 64, -16), MAX_RADIUS, MAX_DEPTH);
        assertEquals(Status.BUDGET_EXHAUSTED, report.status());
        assertEquals(MAX_READS, reads.get());
        assertEquals(MAX_READS / MAX_DEPTH, report.completedColumns());
        assertTrue(report.completedColumns() < report.requestedColumns());
        assertTrue(report.message().contains("placement eligibility NOT_EVALUATED"));
    }

    @Test void rejectsUnboundedArgumentsAndCopiesMutableOrigin() {
        var view = view(Level.NETHER, pos -> Cell.OTHER);
        for (int radius : new int[]{0, -1, MAX_RADIUS + 1})
            assertThrows(IllegalArgumentException.class, () -> scan(view, BlockPos.ZERO, radius, 1));
        for (int depth : new int[]{0, -1, MAX_DEPTH + 1})
            assertThrows(IllegalArgumentException.class, () -> scan(view, BlockPos.ZERO, 1, depth));
        var pos = new BlockPos.MutableBlockPos(1, 10, 1);
        var report = scan(view, pos, 1, 1);
        pos.set(9, 9, 9);
        assertEquals(new BlockPos(1, 10, 1), report.center());
    }
}
