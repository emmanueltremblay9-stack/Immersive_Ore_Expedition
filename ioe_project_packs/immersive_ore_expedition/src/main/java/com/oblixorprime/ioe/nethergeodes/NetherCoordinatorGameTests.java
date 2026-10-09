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
        var coordinator = NetherPlacementRuntime.coordinator(level);
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
            var ledger = storage.computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
            helper.assertTrue(NetherPlacementRuntime.commitPrepared(level, plan)
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

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 300)
    public static void preparedBackendPersistsPartialFailuresWithoutReplayOrRepair(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var coordinator = NetherPlacementRuntime.coordinator(level);
        var rock = Blocks.NETHERRACK.defaultBlockState();
        var ore = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        var origins = new java.util.ArrayList<BlockPos>();
        for (int scenario = 0; scenario < 4; scenario++) {
            var first = new BlockPos(-20008 - scenario * 4096, 40, -20008);
            origins.add(first);
            for (var pos : java.util.List.of(first, first.east(16))) {
                level.getChunk(pos); // Fixture allocation, not a backend load.
                level.setBlock(pos, rock, 2);
            }
        }
        for (var first : origins) for (var pos : java.util.List.of(first, first.east(16))) {
            var chunk = level.getChunk(pos);
            coordinator.observe(chunk.getPos().toLong(), chunk, true, level.getServer().getTickCount());
        }
        helper.runAfterDelay(1, () -> {
            var plans = new java.util.ArrayList<NetherPlacementCoordinator.Plan>();
            for (int scenario = 0; scenario < 4; scenario++) {
                final int fault = scenario;
                var first = origins.get(scenario); var second = first.east(16);
                var expected = new java.util.LinkedHashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
                expected.put(first, rock); expected.put(second, rock);
                var writes = new java.util.LinkedHashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
                writes.put(first, ore); writes.put(second, ore);
                var plan = new NetherPlacementCoordinator.Plan(first, expected, writes); plans.add(plan);
                var delegate = NetherPlacementRuntime.host(level);
                var host = new NetherPlacementCoordinator.Host() {
                    boolean failed;
                    public void requireServerThread() { delegate.requireServerThread(); }
                    public int tick() { return delegate.tick(); }
                    public Object loadedChunk(long key) { return delegate.loadedChunk(key); }
                    public net.minecraft.world.level.block.state.BlockState read(BlockPos pos) {
                        var state = delegate.read(pos);
                        if (fault == 3 && failed && pos.equals(first)) coordinator.invalidate(new ChunkPos(first).toLong());
                        return state;
                    }
                    public boolean protectedAt(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        return delegate.protectedAt(pos, state);
                    }
                    public boolean safeToReplace(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        return delegate.safeToReplace(pos, state);
                    }
                    public boolean reserveReads(int count) { return delegate.reserveReads(count); }
                    public boolean write(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        if (pos.equals(second) && state.equals(ore)) {
                            failed = true;
                            if (fault == 2) coordinator.invalidate(new ChunkPos(first).toLong());
                            return false;
                        }
                        return delegate.write(pos, state);
                    }
                };
                if (scenario == 0) coordinator.invalidate(new ChunkPos(second).toLong());
                var result = NetherPlacementRuntime.commitPrepared(level, plan, host);
                var wanted = scenario == 0 ? NetherPlacementCoordinator.Result.NOT_FRESH
                        : scenario == 1 ? NetherPlacementCoordinator.Result.ROLLED_BACK
                        : NetherPlacementCoordinator.Result.ROLLBACK_INCOMPLETE;
                helper.assertTrue(result == wanted, "Wrong failure outcome: " + scenario + ": " + result);
                helper.assertTrue(level.getBlockState(first).equals(scenario < 2 ? rock : ore)
                        && level.getBlockState(second).equals(rock), "Unexpected compensation writes");
            }
            try {
                var storage = level.getDataStorage(); storage.save();
                net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
                var disk = storage.readTagFromDisk(NetherPlacementLedger.NAME, null,
                        SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                var loaded = NetherPlacementLedger.FACTORY.deserializer().apply(disk.getCompound("data"), level.registryAccess());
                storage.set(NetherPlacementLedger.NAME, loaded);
                for (int i = 0; i < plans.size(); i++) {
                    var plan = plans.get(i);
                    helper.assertTrue(loaded.resultAt(plan.origin()).orElseThrow().equals(i == 0 ? "NOT_FRESH"
                            : i == 1 ? "ROLLED_BACK" : "ROLLBACK_INCOMPLETE"), "Lost durable result");
                    if (i >= 2) helper.assertTrue(loaded.hasAcceptedWithin(plan.origin().east(255), 256), "Lost partial-site spacing");
                    helper.assertTrue(NetherPlacementRuntime.commitPrepared(level, plan)
                            == NetherPlacementCoordinator.Result.DUPLICATE, "Reload allowed retry");
                    helper.assertTrue(level.getBlockState(plan.origin()).equals(i < 2 ? rock : ore), "Reload repaired a partial site");
                }
            } catch (java.io.IOException failure) { throw new RuntimeException(failure); }
            helper.succeed();
        });
    }

}
