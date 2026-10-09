package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.oblixorprime.ioe.nethergeodes.NetherPlacementCoordinator.*;

final class NetherReadSetTest {
    static final BlockPos LAVA = new BlockPos(-20, 40, 8), FLOOR = new BlockPos(8, 36, 8);
    static final class World implements Host, NetherSitePlanner.Snapshot {
        final Map<BlockPos, BlockState> changes = new HashMap<>();
        final Map<Long, Object> chunks = new HashMap<>();
        final NetherAnalysisBudget budget = new NetherAnalysisBudget();
        final NetherPlacementCoordinator coordinator = new NetherPlacementCoordinator();
        int reads, writes;
        World() {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
                chunks.put(ChunkPos.asLong(x, z), new Object());
        }
        BlockState state(BlockPos pos) {
            return changes.getOrDefault(pos, pos.getY() > 40 ? Blocks.AIR.defaultBlockState()
                    : pos.getX() <= 28 && pos.getY() > 36 ? Blocks.LAVA.defaultBlockState()
                    : Blocks.NETHERRACK.defaultBlockState());
        }
        public boolean nether() { return true; }
        public int minY() { return 0; }
        public int maxY() { return 128; }
        public NetherSitePlanner.Cell at(BlockPos pos) { return new NetherSitePlanner.Cell(state(pos), false); }
        public void requireServerThread() { }
        public int tick() { return 1; }
        public Object loadedChunk(long key) { return chunks.get(key); }
        public BlockState read(BlockPos pos) { reads++; return state(pos); }
        public boolean safeToReplace(BlockPos pos, BlockState state) { return true; }
        public boolean write(BlockPos pos, BlockState state) { writes++; changes.put(pos, state); return true; }
        public boolean reserveReads(int count) { return budget.acquire(tick(), count); }
        Plan plan() {
            var result = NetherSitePlanner.plan(new NetherSitePlanner.Candidate(8, 8, SiteQuality.NORMAL, 73, false), 40, this);
            assertEquals(NetherSitePlanner.Status.PLANNED, result.status());
            var plan = result.plan();
            plan.writes().keySet().stream().map(ChunkPos::new).distinct().forEach(chunk ->
                    coordinator.observe(chunk.toLong(), chunks.get(chunk.toLong()), true, 0));
            return plan;
        }
    }
    @Test void changedLavaOutsideWritesRejectsBeforeMutation() { changedCellRejects(LAVA); }
    @Test void changedFloorOutsideWritesRejectsBeforeMutation() { changedCellRejects(FLOOR); }
    private void changedCellRejects(BlockPos position) {
        var world = new World(); var plan = world.plan();
        assertFalse(plan.writes().containsKey(position));
        world.changes.put(position, Blocks.AIR.defaultBlockState());
        assertEquals(Result.TERRAIN_CHANGED, world.coordinator.commit(world, new NetherPlacementLedger(), plan));
        assertEquals(0, world.writes);
    }
}
