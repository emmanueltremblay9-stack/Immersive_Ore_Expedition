package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherSnapshotGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void capturesRealLoadedCellsWithoutMutationAndRefusesUnavailableRegion(GameTestHelper helper)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var server = helper.getLevel().getServer();
        var level = server.getLevel(Level.NETHER);
        var pos = new BlockPos(-8184, 40, -8184);
        var chunk = level.getChunk(pos); // Fixture allocation only, never done by diagnostic.
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 2);
        level.setBlock(pos.east(), Blocks.NETHERRACK.defaultBlockState(), 2);
        var budget = NetherAnalysisBudget.forServer(server);
        var capture = new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                () -> budget.acquire(server.getTickCount()));
        helper.assertTrue(capture.at(pos).state().is(Blocks.LAVA)
                && capture.at(pos.east()).state().is(Blocks.NETHERRACK), "Real chunk states were not captured");
        helper.assertTrue(capture.identities.get(chunk.getPos().toLong()) == chunk, "Lost real chunk identity");
        capture.validate(); capture.discard();
        helper.assertTrue(level.getBlockState(pos).is(Blocks.LAVA)
                && level.getBlockState(pos.east()).is(Blocks.NETHERRACK), "Capture mutated fixture");
        var absent = new BlockPos(20_000_008, 40, 20_000_008);
        int cx = absent.getX() >> 4, cz = absent.getZ() >> 4;
        helper.assertTrue(level.getChunkSource().getChunkNow(cx, cz) == null, "Absent fixture already loaded");
        var report = NetherSnapshotDiagnostic.inspect(level, absent, 40);
        helper.assertTrue(report.captureStatus().equals("UNAVAILABLE") && report.plannedWrites() == 0,
                "Incomplete region exposed a plan");
        helper.assertTrue(level.getChunkSource().getChunkNow(cx, cz) == null, "Capture loaded absent chunk");
        var source = server.createCommandSourceStack().withLevel(level).withPosition(Vec3.atCenterOf(absent));
        helper.assertTrue(server.getCommands().getDispatcher().execute("ioe diagnose nether_site 40", source) == 0,
                "Diagnostic command did not report incomplete data");
        helper.assertTrue(NetherSnapshotDiagnostic.inspect(server.overworld(), pos, 40).captureStatus()
                .equals("WRONG_DIMENSION"), "Overworld capture accepted");
        helper.succeed();
    }
}
