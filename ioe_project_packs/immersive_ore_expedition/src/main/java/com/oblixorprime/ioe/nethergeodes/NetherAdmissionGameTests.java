package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherAdmissionGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void realLoadReceiptsConnectCanonicalCaptureToPreparedPlacement(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var c = NetherSitePlanner.candidate(level.getSeed(), -700, -700);
        var center = new BlockPos(c.x(), 20, c.z());
        // Fixture generation/terrain editing only. Admission never calls getChunk or observe.
        for (int z = -2; z <= 2; z++) for (int x = -2; x <= 2; x++)
            level.getChunk((c.x() >> 4) + x, (c.z() >> 4) + z);
        for (int z = -37; z <= 36; z++) for (int x = -37; x <= 36; x++) {
            for (int y = 37; y <= 42; y++) level.setBlock(new BlockPos(c.x() + x, y, c.z() + z),
                    y > 40 ? Blocks.AIR.defaultBlockState()
                            : x <= 20 ? Blocks.LAVA.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState(), 2);
        }
        for (int z = -7; z <= 7; z++) for (int x = -7; x <= 7; x++) {
            level.setBlock(new BlockPos(c.x() + x, 36, c.z() + z), Blocks.NETHERRACK.defaultBlockState(), 2);
            for (int y = -7; y <= 7; y++) level.setBlock(center.offset(x, y, z), Blocks.NETHERRACK.defaultBlockState(), 2);
        }
        // Remove accidental lower fixture pools; add an upper pool that must not be selected.
        for (int y = level.getMinBuildHeight(); y <= 36; y++)
            level.setBlock(new BlockPos(c.x(), y, c.z()), Blocks.NETHERRACK.defaultBlockState(), 2);
        for (int y = 70; y <= 73; y++) level.setBlock(new BlockPos(c.x(), y, c.z()), Blocks.LAVA.defaultBlockState(), 2);
        level.setBlock(new BlockPos(c.x(), 74, c.z()), Blocks.AIR.defaultBlockState(), 2);
        helper.runAfterDelay(1, () -> {
            var trigger = new ChunkPos(center);
            helper.assertTrue(NetherPlacementRuntime.coordinator(level).hasFreshReceipt(NetherPlacementRuntime.host(level), trigger.toLong()),
                    "Actual load event did not supply a fresh receipt");
            var result = NetherNaturalAdmission.attempt(level, trigger);
            helper.assertTrue(result == NetherPlacementCoordinator.Result.COMMITTED, "Real admission failed: " + result);
            helper.assertTrue(level.getBlockState(center).isAir(), "Chamber was not carved");
            var shore = new BlockPos(c.x() + 21, 41, c.z());
            helper.assertTrue(level.getBlockState(shore).is(Blocks.BLACKSTONE)
                    && level.getBlockState(shore.above()).is(Blocks.BLACKSTONE), "Remote shore marker missing");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, trigger) == NetherPlacementCoordinator.Result.DUPLICATE,
                    "Repeated admission allowed a reroll");
            helper.assertTrue(NetherPlacementRuntime.commit(level, new NetherPlacementCoordinator.Plan(center,
                    java.util.Map.of(), java.util.Map.of())) == NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED,
                    "Automatic generation gate changed");
            helper.succeed();
        });
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void realReceiptWithMissingTerrainRejectsDurablyWithoutLoadingNeighbors(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var c = NetherSitePlanner.candidate(level.getSeed(), -900, -900);
        var origin = new BlockPos(c.x(), 40, c.z()); var trigger = new ChunkPos(origin);
        level.getChunk(trigger.x, trigger.z); // Fixture allocation; real event supplies the receipt.
        for (int y = level.getMinBuildHeight(); y < 40; y++)
            level.setBlock(new BlockPos(c.x(), y, c.z()), Blocks.NETHERRACK.defaultBlockState(), 2);
        level.setBlock(origin, Blocks.LAVA.defaultBlockState(), 2);
        level.setBlock(origin.above(), Blocks.AIR.defaultBlockState(), 2);
        var absent = new ChunkPos(trigger.x - 2, trigger.z - 2);
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Fixture neighbor already FULL");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, new ChunkPos(trigger.x + 1, trigger.z))
                    == NetherPlacementCoordinator.Result.NOT_CANDIDATE, "Noncanonical trigger accepted");
            helper.assertTrue(NetherPlacementRuntime.coordinator(level).hasFreshReceipt(NetherPlacementRuntime.host(level), trigger.toLong()),
                    "Missing real receipt");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, trigger) == NetherPlacementCoordinator.Result.TERRAIN_CHANGED,
                    "Missing terrain was not rejected");
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Admission forced a neighbor");
            var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
            var loaded = NetherPlacementLedger.FACTORY.deserializer().apply(ledger.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()), level.registryAccess());
            helper.assertFalse(loaded.claim(origin), "Reload lost terminal admission refusal");
            helper.assertTrue(NetherNaturalAdmission.attempt(level, trigger) == NetherPlacementCoordinator.Result.DUPLICATE,
                    "Refusal was rerolled");
            helper.succeed();
        });
    }
}
