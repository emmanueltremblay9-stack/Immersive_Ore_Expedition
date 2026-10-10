package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class FirstLoadAdmissionGameTests {
    // Separate batch: this lifecycle test deliberately advances the session clock and clears it.
    @GameTest(template = "expedition_worldgen_empty", batch = "first_load_admissions", timeoutTicks = 100)
    public static void boundedSessionAdmissionsNeverReauthorizeOldChunks(GameTestHelper helper) {
        var level = helper.getLevel();
        IoeNewChunkOreGuard.clearPending();
        var existingSites = java.util.Set.copyOf(
                com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService.index(level).sites());
        try {
            var absent = new ChunkPos(1_500_000, 1_500_000);
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Fixture must be absent");
            IoeNewChunkOreGuard.scheduleChunk(level, absent, true);
            IoeNewChunkOreGuard.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(() -> true, level.getServer()));
            helper.assertTrue(IoeNewChunkOreGuard.pendingAdmissionCount() == 1
                    && IoeNewChunkOreGuard.queuedSanitizationCount() == 0, "Unavailable pass lost permission or retained queued work");
            IoeNewChunkOreGuard.scheduleChunk(level, absent, false);
            helper.assertTrue(IoeNewChunkOreGuard.pendingAdmissionCount() == 1, "Reload lost or duplicated admission");
            helper.assertTrue(IoeNewChunkOreGuard.queuedSanitizationCount() == 1, "Pending disk load did not reschedule");
            IoeNewChunkOreGuard.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(() -> true, level.getServer()));
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Guard forced chunk loading");

            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            var loaded = new ChunkPos(pos);
            level.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 2);
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, true);
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, false);
            IoeNewChunkOreGuard.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(() -> true, level.getServer()));
            helper.assertTrue(IoeNewChunkOreGuard.queuedSanitizationCount() == 1
                    && IoeNewChunkOreGuard.scheduledSanitizationCount() == 0, "Initial pass did not defer its final pass");
            helper.assertFalse(level.getBlockState(pos).is(Blocks.DIAMOND_ORE), "Valid admission did not sanitize");
            level.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 2);
            int[] expiredReservation = stageOwnedState(helper, pos);
            for (long tick = 0; tick < IoeNewChunkOreGuard.ADMISSION_LIFETIME_TICKS; tick++) {
                IoeNewChunkOreGuard.advanceAdmissionTick();
            }
            helper.assertTrue(IoeNewChunkOreGuard.pendingAdmissionCount() == 0
                    && IoeNewChunkOreGuard.queuedSanitizationCount() == 0
                    && IoeNewChunkOreGuard.scheduledSanitizationCount() == 0, "Expiry retained admission or queued work");
            assertReleased(helper, pos, expiredReservation);
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, false);
            helper.assertFalse(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, loaded, true), "Expired chunk was rewritten");
            helper.assertTrue(level.getBlockState(pos).is(Blocks.DIAMOND_ORE), "Expiry changed terrain");

            IoeNewChunkOreGuard.scheduleChunk(level, loaded, true);
            int[] evictedReservation = stageOwnedState(helper, pos);
            for (int i = 0; i < IoeNewChunkOreGuard.MAX_PENDING_ADMISSIONS * 2; i++) {
                IoeNewChunkOreGuard.scheduleChunk(level, new ChunkPos(absent.x + i, absent.z), true);
            }
            helper.assertTrue(IoeNewChunkOreGuard.pendingAdmissionCount() == IoeNewChunkOreGuard.MAX_PENDING_ADMISSIONS
                    && IoeNewChunkOreGuard.queuedSanitizationCount() == IoeNewChunkOreGuard.MAX_PENDING_ADMISSIONS
                    && IoeNewChunkOreGuard.scheduledSanitizationCount() == IoeNewChunkOreGuard.MAX_PENDING_ADMISSIONS,
                    "Saturation left unbounded admissions or queue tombstones");
            assertReleased(helper, pos, evictedReservation);
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, false);
            helper.assertFalse(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, loaded, true), "Evicted chunk was readmitted");
            helper.assertTrue(level.getBlockState(pos).is(Blocks.DIAMOND_ORE), "Eviction changed terrain");
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, true);
            int[] stoppedReservation = stageOwnedState(helper, pos);
            IoeNewChunkOreGuard.clearPending();
            assertReleased(helper, pos, stoppedReservation);
            IoeNewChunkOreGuard.scheduleChunk(level, absent, false);
            IoeNewChunkOreGuard.scheduleChunk(level, loaded, false);
            helper.assertFalse(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, loaded, true), "Session end retained write permission");
            helper.assertTrue(level.getBlockState(pos).is(Blocks.DIAMOND_ORE), "Session end changed terrain");
            helper.assertTrue(IoeNewChunkOreGuard.pendingAdmissionCount() == 0
                    && IoeNewChunkOreGuard.queuedSanitizationCount() == 0
                    && IoeNewChunkOreGuard.scheduledSanitizationCount() == 0, "Session clear restored an old admission");
            helper.assertTrue(java.util.Set.copyOf(
                    com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService.index(level).sites()).equals(existingSites),
                    "Admission cleanup changed confirmed locator data");
            helper.succeed();
        } finally {
            IoeNewChunkOreGuard.clearPending();
        }
    }

    private static int[] stageOwnedState(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "iron");
        var definition = level.registryAccess().registryOrThrow(BiomeMineResourceDefinition.REGISTRY_KEY).get(id);
        var profile = new BiomeMineResourceProfile(
                net.minecraft.resources.ResourceLocation.parse("minecraft:plains"), id, definition, 1);
        var plan = new ExpeditionSiteBlockPlan(IoeWorldgenFeatureKeys.MINER_CAMP, pos, pos, pos,
                com.oblixorprime.ioe.core.SiteQuality.DRY, 0, null, null,
                java.util.List.of(), java.util.List.of(), java.util.Map.of(pos, Blocks.DIAMOND_ORE.defaultBlockState()));
        int[] calls = new int[4];
        helper.assertTrue(IoePendingExpeditionSites.stage(level, plan, profile, new IoeMotherDepositReservation() {
            public boolean createdByIoe() { return false; }
            public void commit() { calls[0]++; }
            public void rollback() { calls[1]++; }
        }, new IoePetroleumReservoirReservation() {
            public boolean createdByIoe() { return false; }
            public void commit() { calls[2]++; }
            public void rollback() { calls[3]++; }
        }, java.util.List.of()), "Transient fixture was not staged");
        IoeOrePlacementAuthorization.authorize(level.dimension(), plan);
        return calls;
    }

    private static void assertReleased(GameTestHelper helper, BlockPos pos, int[] calls) {
        var level = helper.getLevel();
        helper.assertTrue(calls[0] == 0 && calls[1] == 1 && calls[2] == 0 && calls[3] == 1, "Abandoned reservation committed or was not released exactly once");
        helper.assertFalse(IoeOrePlacementAuthorization.matches(level.dimension(), pos, Blocks.DIAMOND_ORE.defaultBlockState()),
                "Ended admission retained block authorization");
        helper.assertTrue(IoePendingExpeditionSites.excavatorRegions(level.dimension(), new ChunkPos(pos)).isEmpty(),
                "Ended admission retained a pending plan");
    }

}
