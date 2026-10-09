package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherGeneratedTerrainGameTests {
    private static final int MAX_REGIONS = 16;

    @GameTest(template = "expedition_worldgen_empty", batch = "ioe_nether_generated_terrain", timeoutTicks = 600)
    public static void boundedSearchOnUneditedGeneratedTerrain(GameTestHelper helper) {
        sample(helper, 0, new LinkedHashMap<>());
    }

    private static void sample(GameTestHelper helper, int index, Map<String, Integer> counts) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        int regionX = -1600 - 2 * index, regionZ = -1600;
        var candidate = NetherSitePlanner.candidate(level.getSeed(), regionX, regionZ);
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        var chunk = new ChunkPos(origin);
        // Bounded qualification search only. Production neither requests these chunks nor retries a region.
        // Each sample has a disjoint 5x5 neighborhood; no terrain/metadata editing or synthetic receipts.
        for (int z = -2; z <= 2; z++) for (int x = -2; x <= 2; x++) level.getChunk(chunk.x + x, chunk.z + z);
        helper.runAfterDelay(1, () -> {
            var selected = NetherPlacementRuntime.coordinator(level)
                    .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount());
            helper.assertTrue(selected.contains(chunk), "Actual canonical receipt unavailable for sample " + index);
            // The dispatcher uses attempt(), which delegates to this same detailed entry without a second scan.
            var report = NetherNaturalAdmission.attemptDetailed(level, chunk);
            var result = report.result();
            helper.assertTrue(result != NetherPlacementCoordinator.Result.NOT_FRESH
                    && result != NetherPlacementCoordinator.Result.DUPLICATE
                    && result != NetherPlacementCoordinator.Result.NOT_CANDIDATE
                    && result != NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED, "Admission not reached: " + report);
            var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
            helper.assertTrue(ledger.resultAt(origin).orElseThrow().equals(result.name()), "Outcome not durable");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, chunk) == NetherPlacementCoordinator.Result.DUPLICATE, "Region rerolled");
            counts.merge(result + "/" + report.plannerStatus(), 1, Integer::sum);
            com.mojang.logging.LogUtils.getLogger().info("IOE natural sample: seed={}, region={},{} candidate={},{} quality={} result={} planner={} reads={} connected={} writes={} terrainEdits=0 injectedReceipts=0",
                    level.getSeed(), regionX, regionZ, candidate.x(), candidate.z(), candidate.quality(), result,
                    report.plannerStatus(), report.worldReads(), report.connectedColumns(), report.plannedWrites());
            if (result == NetherPlacementCoordinator.Result.COMMITTED || index + 1 == MAX_REGIONS) {
                com.mojang.logging.LogUtils.getLogger().info("IOE natural search complete: seed={}, sampled={}, limit={}, accepted={}, outcomes={}, loading=controlled_full_5x5_not_player_stream",
                        level.getSeed(), index + 1, MAX_REGIONS, result == NetherPlacementCoordinator.Result.COMMITTED, counts);
                helper.succeed();
            } else {
                // Separate server ticks preserve shared quotas and let old receipts expire normally.
                helper.runAfterDelay(2, () -> sample(helper, index + 1, counts));
            }
        });
    }
}
