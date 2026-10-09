package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Map;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherCoordinatorGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void multiChunkCommitPersistsWithoutRestoringWritePermissions(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var first = new BlockPos(-4088, 40, -4088);
        var second = first.east(16);
        var rock = Blocks.NETHERRACK.defaultBlockState();
        var ore = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        var coordinator = new NetherPlacementCoordinator();
        for (var pos : java.util.List.of(first, second)) {
            var chunk = level.getChunk(pos);
            level.setBlock(pos, rock, 2);
            // Explicit synthetic first-generation receipts for fixture chunks, using their real identities.
            coordinator.observe(chunk.getPos().toLong(), chunk, true, level.getServer().getTickCount());
        }
        var plan = new NetherPlacementCoordinator.Plan(first, Map.of(first, rock, second, rock), Map.of(first, ore, second, ore));
        helper.runAfterDelay(1, () -> {
            var storage = level.getDataStorage();
            var ledger = new NetherPlacementLedger();
            storage.set(NetherPlacementLedger.NAME, ledger);
            helper.assertTrue(coordinator.commit(NetherPlacementRuntime.host(level), ledger, plan)
                    == NetherPlacementCoordinator.Result.COMMITTED, "Fresh multi-chunk commit failed");
            helper.assertTrue(level.getBlockState(first).is(Blocks.NETHER_QUARTZ_ORE)
                    && level.getBlockState(second).is(Blocks.NETHER_QUARTZ_ORE), "Lost one side of transaction");
            try {
                storage.save();
                net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
                var disk = storage.readTagFromDisk(NetherPlacementLedger.NAME, null,
                        SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                var reloaded = NetherPlacementLedger.FACTORY.deserializer().apply(disk.getCompound("data"), level.registryAccess());
                helper.assertFalse(reloaded.claim(first), "Disk reload allowed reroll");
                helper.assertTrue(reloaded.hasAcceptedWithin(second, 256), "Accepted spacing disappeared");
                var freshCoordinator = new NetherPlacementCoordinator();
                helper.assertTrue(freshCoordinator.commit(NetherPlacementRuntime.host(level), new NetherPlacementLedger(), plan)
                        == NetherPlacementCoordinator.Result.NOT_FRESH, "Restart reconstructed write permission");
                storage.set(NetherPlacementLedger.NAME, reloaded);
            } catch (java.io.IOException failure) { throw new RuntimeException(failure); }
            helper.succeed();
        });
    }
}
