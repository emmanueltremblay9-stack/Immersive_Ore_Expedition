package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherGeneratedTerrainGameTests {
    @GameTest(template = "expedition_worldgen_empty", batch = "ioe_nether_generated_terrain", timeoutTicks = 400)
    public static void dispatchesOneUneditedGeneratedNeighborhoodWithoutReroll(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var candidate = NetherSitePlanner.candidate(level.getSeed(), -1400, -1400);
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        var chunk = new net.minecraft.world.level.ChunkPos(origin);
        // Harness requests the 25 chunks; admission/dispatch never requests generation.
        // No terrain or structure metadata is edited, and no receipts are injected.
        for (int z = -2; z <= 2; z++) for (int x = -2; x <= 2; x++) level.getChunk(chunk.x + x, chunk.z + z);
        helper.runAfterDelay(1, () -> {
            var candidates = NetherPlacementRuntime.coordinator(level)
                    .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount()).stream().filter(chunk::equals).toList();
            var result = NetherNaturalTrigger.dispatchCandidates(level, candidates).get(chunk.toLong());
            helper.assertTrue(result != null && result != NetherPlacementCoordinator.Result.NOT_FRESH
                    && result != NetherPlacementCoordinator.Result.DUPLICATE
                    && result != NetherPlacementCoordinator.Result.NOT_CANDIDATE
                    && result != NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED, "Generated terrain admission not reached: " + result);
            var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
            helper.assertTrue(ledger.resultAt(origin).orElseThrow().equals(result.name()), "Outcome was not durable");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, chunk) == NetherPlacementCoordinator.Result.DUPLICATE, "Generated region rerolled");
            com.mojang.logging.LogUtils.getLogger().info("IOE generated Nether qualification: seed={}, region=-1400,-1400, result={}, constructedTerrain=false, injectedReceipts=false",
                    level.getSeed(), result);
            helper.succeed();
        });
    }
}
