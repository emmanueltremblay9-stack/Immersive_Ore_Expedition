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
public final class LavaLakeDiagnosticGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void samplesLoadedNetherAndCommandWithoutLoadingAbsentChunks(GameTestHelper helper)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var server = helper.getLevel().getServer();
        var nether = server.getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "Nether dimension unavailable");
        BlockPos center = new BlockPos(-1020, 64, -1020);
        // Fixture setup explicitly allocates this test chunk; the diagnostic must never do so.
        nether.getChunk(center.getX() >> 4, center.getZ() >> 4);
        for (int z = -1; z <= 1; z++) for (int x = -1; x <= 1; x++) {
            for (int depth = 0; depth < 4; depth++) nether.setBlock(center.offset(x, -depth, z),
                    (depth < 3 ? Blocks.LAVA : Blocks.STONE).defaultBlockState(), 2);
        }
        var report = LavaLakeDiagnostic.scan(nether, center, 1, 4);
        helper.assertTrue(report.status() == LavaLakeDiagnostic.Status.COMPLETE
                && report.lavaColumns() == 9 && report.minimumObservedDepth() == 3
                && report.depthCappedColumns() == 0, "Incorrect real fluid-state measurements");
        var source = server.createCommandSourceStack().withLevel(nether).withPosition(Vec3.atCenterOf(center));
        helper.assertTrue(server.getCommands().getDispatcher().execute("ioe diagnose lava_lake 1 4", source) == 1,
                "Registered diagnostic command failed");
        for (int z = -1; z <= 1; z++) for (int x = -1; x <= 1; x++) for (int depth = 0; depth < 4; depth++) {
            helper.assertTrue(nether.getBlockState(center.offset(x, -depth, z))
                    .is(depth < 3 ? Blocks.LAVA : Blocks.STONE), "Diagnostic changed fixture blocks");
        }
        BlockPos absent = new BlockPos(20_000_008, 64, 20_000_008);
        int chunkX = absent.getX() >> 4, chunkZ = absent.getZ() >> 4;
        helper.assertTrue(nether.getChunkSource().getChunkNow(chunkX, chunkZ) == null, "Absent fixture chunk already loaded");
        var unloaded = LavaLakeDiagnostic.scan(nether, absent, 1, 4);
        helper.assertTrue(unloaded.status() == LavaLakeDiagnostic.Status.UNLOADED && unloaded.reads() == 1,
                "Unloaded scan did not stop immediately");
        helper.assertTrue(nether.getChunkSource().getChunkNow(chunkX, chunkZ) == null, "Diagnostic loaded a chunk");
        helper.assertTrue(LavaLakeDiagnostic.scan(server.overworld(), center, 1, 4).status()
                == LavaLakeDiagnostic.Status.WRONG_DIMENSION, "Overworld accepted");
        helper.assertTrue(server.getCommands().getDispatcher().execute("ioe diagnose lava_lake 1 4",
                source.withLevel(server.overworld())) == 0, "Wrong-dimension command returned success");
        helper.succeed();
    }
}
