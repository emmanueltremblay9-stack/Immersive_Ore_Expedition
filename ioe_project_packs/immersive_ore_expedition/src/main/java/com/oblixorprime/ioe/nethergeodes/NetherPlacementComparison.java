package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Temporary paired read-only experiment, never a production placement route. */
final class NetherPlacementComparison {
    private static final int[][] REGIONS = { {-2006, -2000},
            {-2014, -2000},
            {-2024, -2000},
            {-2026, -2000},
            {-2028, -2000},
            {-2030, -2000},
            {-2034, -2000},
            {-2042, -2000},
            {-2048, -2000},
            {-2052, -2000},
            {-2058, -2000},
            {-2064, -2000},
            {-2068, -2000},
            {-2070, -2000},
            {-2076, -2000},
            {-2094, -2000},
            {-2004, -2000},
            {-2008, -2000},
            {-2010, -2000},
            {-2032, -2000},
            {-2036, -2000},
            {-2038, -2000},
            {-2044, -2000},
            {-2046, -2000},
            {-2000, -2000},
            {-2002, -2000},
            {-2022, -2000},
            {-2074, -2000},
            {-2012, -2000},
            {-2016, -2000},
            {-2018, -2000},
            {-2020, -2000} };
    private static final int[][] VARIANTS = {{16,7}, {12,7}, {8,7}, {12,6}};
    static void run(GameTestHelper helper) {
        if (!"push".equals(System.getenv("GITHUB_EVENT_NAME"))
                || !"1".equals(System.getenv("GITHUB_RUN_ATTEMPT"))
                || net.neoforged.fml.ModList.get().isLoaded("immersiveengineering")) {
            com.mojang.logging.LogUtils.getLogger().info("IOE comparison NOT_RUN: push baseline only");
            helper.succeed(); return;
        }
        com.mojang.logging.LogUtils.getLogger().info("IOE comparison start: source={} seed=0 minecraft=1.21.1 neoforge=21.1.230 pairedRegions=32 variants=4 loading=controlled_full_5x5 planner_only=true writes=0", System.getenv("GITHUB_SHA"));
        sample(helper, 0);
    }
    private static void sample(GameTestHelper helper, int index) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(level.getSeed() == 0L, "Expected original seed");
        int rx=REGIONS[index][0], rz=REGIONS[index][1];
        var candidate = NetherComparisonPlanner.candidate(level.getSeed(), rx, rz);
        var chunk = new ChunkPos(new BlockPos(candidate.x(), 0, candidate.z()));
        for (int z=-2; z<=2; z++) for (int x=-2; x<=2; x++) level.getChunk(chunk.x+x, chunk.z+z);
        helper.runAfterDelay(1, () -> {
            var coordinator = NetherPlacementRuntime.coordinator(level);
            helper.assertTrue(coordinator.takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount()).contains(chunk)
                    && coordinator.hasFreshReceipt(NetherPlacementRuntime.host(level), chunk.toLong()), "Missing actual NEW receipt");
            var capture = new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                    () -> NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()));
            try {
                var snapshot = new NetherComparisonPlanner.Snapshot() {
                    public boolean nether() { return capture.nether(); }
                    public int minY() { return capture.minY(); }
                    public int maxY() { return capture.maxY(); }
                    public NetherComparisonPlanner.Cell at(BlockPos p) {
                        var c=capture.at(p); return new NetherComparisonPlanner.Cell(c.state(), c.protectedBlock());
                    }
                };
                for (int v=0; v<VARIANTS.length; v++) {
                    NetherComparisonPlanner.studyDepth=VARIANTS[v][0];
                    NetherComparisonPlanner.studyCubeRadius=VARIANTS[v][1];
                    int before=capture.reads;
                    var outcome=NetherComparisonPlanner.planLowest(candidate, snapshot);
                    capture.validate();
                    helper.assertTrue(outcome.status()!=NetherComparisonPlanner.Status.BUDGET
                            && outcome.status()!=NetherComparisonPlanner.Status.UNKNOWN_TERRAIN, "Technical planning stop");
                    if (v==0) {
                        var original=NetherSitePlanner.planLowest(NetherSitePlanner.candidate(level.getSeed(),rx,rz),capture);
                        helper.assertTrue(original.status().name().equals(outcome.status().name())
                            && original.connectedDeepColumns()==outcome.connectedDeepColumns()
                            && java.util.Objects.equals(original.plan(),outcome.plan()), "Control diverges from canonical planner");
                    }
                    com.mojang.logging.LogUtils.getLogger().info("IOE comparison row: region={},{} candidate={},{} depth={} cube={} status={} connected={} probes={} extraWorldReads={} cumulativeReads={} plannedWrites={} actualWrites=0",
                        rx,rz,candidate.x(),candidate.z(),VARIANTS[v][0],2*VARIANTS[v][1]+1,outcome.status(),outcome.connectedDeepColumns(),outcome.probes(),capture.reads-before,capture.reads,outcome.plan()==null?0:outcome.plan().writes().size());
                }
                // Validate every captured state/protection again, charged to the SAME tick budget.
                // No writes occur and all variants share this identical same-turn snapshot.
                for (var entry:capture.cells.entrySet()) {
                    helper.assertTrue(NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()), "Revalidation budget exhausted");
                    var identity=capture.identities.get(new ChunkPos(entry.getKey()).toLong());
                    helper.assertTrue(entry.getValue().equals(capture.source.read(identity,entry.getKey())), "Read set changed");
                }
                capture.validate();
                com.mojang.logging.LogUtils.getLogger().info("IOE comparison validated: region={},{} captureReads={} revalidationReads={} edits=0 injectedReceipts=0",rx,rz,capture.reads,capture.cells.size());
            } finally { capture.discard(); }
            if (index+1==REGIONS.length) {
                com.mojang.logging.LogUtils.getLogger().info("IOE comparison complete: regions=32 variants=4 planner_only=true writes=0");
                helper.succeed();
            } else helper.runAfterDelay(2, () -> sample(helper,index+1));
        });
    }
}
