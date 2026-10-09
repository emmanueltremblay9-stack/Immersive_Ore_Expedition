package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

import java.util.Objects;

/** Read-only measurements, deliberately not an anchor eligibility or placement decision. */
public final class LavaLakeDiagnostic {
    public static final int MAX_RADIUS = 32;
    public static final int MAX_DEPTH = 16;
    public static final int MAX_READS = 65_536;

    private LavaLakeDiagnostic() { }

    public enum Cell { LAVA, OTHER, UNLOADED }
    public enum Status { COMPLETE, WRONG_DIMENSION, OUTSIDE_HEIGHT, UNLOADED, BUDGET_EXHAUSTED }

    interface View {
        ResourceKey<Level> dimension();
        int minY();
        int maxY();
        Cell read(BlockPos pos);
    }

    public static Report scan(ServerLevel level, BlockPos center, int radius, int depth) {
        Objects.requireNonNull(level, "level");
        return scan(new View() {
            public ResourceKey<Level> dimension() { return level.dimension(); }
            public int minY() { return level.getMinBuildHeight(); }
            public int maxY() { return level.getMaxBuildHeight(); }
            public Cell read(BlockPos pos) {
                var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                return chunk == null ? Cell.UNLOADED
                        : chunk.getFluidState(pos).is(FluidTags.LAVA) ? Cell.LAVA : Cell.OTHER;
            }
        }, center, radius, depth);
    }

    static Report scan(View view, BlockPos center, int radius, int depth) {
        Objects.requireNonNull(view, "view");
        center = Objects.requireNonNull(center, "center").immutable();
        if (radius < 1 || radius > MAX_RADIUS || depth < 1 || depth > MAX_DEPTH) {
            throw new IllegalArgumentException("Radius/depth outside diagnostic limits");
        }
        int requested = (2 * radius + 1) * (2 * radius + 1);
        Status status = !Level.NETHER.equals(view.dimension()) ? Status.WRONG_DIMENSION
                : center.getY() >= view.maxY() || (long) center.getY() - depth + 1 < view.minY()
                ? Status.OUTSIDE_HEIGHT : Status.COMPLETE;
        int reads = 0, columns = 0, lava = 0, capped = 0, minimum = depth, maximum = 0;
        if (status == Status.COMPLETE) {
            scan:
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    int columnDepth = 0;
                    for (int dy = 0; dy < depth; dy++) {
                        if (reads == MAX_READS) { status = Status.BUDGET_EXHAUSTED; break scan; }
                        Cell cell = view.read(center.offset(dx, -dy, dz));
                        reads++;
                        if (cell == Cell.UNLOADED) { status = Status.UNLOADED; break scan; }
                        if (cell != Cell.LAVA) break;
                        columnDepth++;
                    }
                    columns++;
                    if (columnDepth > 0) {
                        lava++;
                        minimum = Math.min(minimum, columnDepth);
                        maximum = Math.max(maximum, columnDepth);
                        if (columnDepth == depth) capped++;
                    }
                }
            }
        }
        return new Report(status, center, radius, depth, requested, columns, lava,
                lava == 0 ? 0 : minimum, maximum, capped, reads);
    }

    public record Report(Status status, BlockPos center, int radius, int depthLimit,
                         int requestedColumns, int completedColumns, int lavaColumns,
                         int minimumObservedDepth, int maximumObservedDepth,
                         int depthCappedColumns, int reads) {
        public String message() {
            return "IOE lava diagnostic: status=" + status + ", strategy=square_at_y_downward_contiguous"
                    + ", center=" + center.getX() + "," + center.getY() + "," + center.getZ()
                    + ", radius=" + radius + ", depthLimit=" + depthLimit
                    + ", completedColumns=" + completedColumns + "/" + requestedColumns
                    + ", lavaColumns=" + lavaColumns + ", minObservedDepth=" + minimumObservedDepth
                    + ", maxObservedDepth=" + maximumObservedDepth + ", depthCappedColumns=" + depthCappedColumns
                    + ", reads=" + reads + "/" + MAX_READS
                    + ". Source and flowing lava counted; incomplete columns excluded."
                    + " Capped depths are lower bounds. Measurements only; placement eligibility NOT_EVALUATED."
                    + " No chunks loaded or blocks changed.";
        }
    }
}
