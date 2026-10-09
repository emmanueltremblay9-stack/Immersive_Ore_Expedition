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
            helper.assertTrue(NetherPlacementRuntime.commit(level, plan)
                    == NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED, "Unqualified production backend became writable");
            helper.assertTrue(level.getBlockState(first).is(Blocks.NETHERRACK)
                    && level.getBlockState(second).is(Blocks.NETHERRACK), "Production safety gate mutated terrain");
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
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void changedReadOnlyObservationRejectsBeforeRealChunkWrite(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var target = new BlockPos(-12280, 24, -12280);
        var observed = target.above(16);
        var chunk = level.getChunk(target); // Test fixture only.
        var rock = Blocks.NETHERRACK.defaultBlockState();
        var lava = Blocks.LAVA.defaultBlockState();
        level.setBlock(target, rock, 2); level.setBlock(observed, lava, 2);
        var coordinator = new NetherPlacementCoordinator();
        coordinator.observe(chunk.getPos().toLong(), chunk, true, level.getServer().getTickCount());
        var plan = new NetherPlacementCoordinator.Plan(target, Map.of(target, rock, observed, lava),
                Map.of(target, Blocks.NETHER_QUARTZ_ORE.defaultBlockState()));
        helper.runAfterDelay(1, () -> {
            level.setBlock(observed, Blocks.AIR.defaultBlockState(), 2);
            helper.assertTrue(coordinator.commit(NetherPlacementRuntime.host(level), new NetherPlacementLedger(), plan)
                    == NetherPlacementCoordinator.Result.TERRAIN_CHANGED, "Read-only terrain change was ignored");
            helper.assertTrue(level.getBlockState(target).is(Blocks.NETHERRACK), "Rejected plan wrote its target");
            helper.succeed();
        });
    }

}
