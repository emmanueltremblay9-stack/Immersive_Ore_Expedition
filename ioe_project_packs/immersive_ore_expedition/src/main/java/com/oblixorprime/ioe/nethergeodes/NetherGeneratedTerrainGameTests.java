package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherGeneratedTerrainGameTests {
    private static final int MAX_REGIONS = 64;
    private record Filter(String reason, Integer surfaceY, Integer floor, int reads, String detail) { }

    @GameTest(template = "expedition_worldgen_empty", batch = "ioe_nether_generated_terrain", timeoutTicks = 1200)
    public static void boundedSearchOnUneditedGeneratedTerrain(GameTestHelper helper) {
        sample(helper, 0, new LinkedHashMap<>());
    }

    private static boolean lava(NetherSitePlanner.Cell cell) {
        return cell.state().getFluidState().is(Fluids.LAVA) || cell.state().getFluidState().is(Fluids.FLOWING_LAVA);
    }
    private static boolean rock(NetherSitePlanner.Cell cell) {
        return !cell.protectedBlock() && cell.state().getFluidState().isEmpty() && !cell.state().hasBlockEntity()
                && (cell.state().is(Blocks.NETHERRACK) || cell.state().is(Blocks.BASALT) || cell.state().is(Blocks.BLACKSTONE));
    }
    // Qualification-only necessary conditions. Never pick another surface or relax full admission.
    // All reads debit the SAME server tick quota as the subsequent production admission.
    private static Filter filter(ServerLevel level, NetherSitePlanner.Candidate candidate) {
        var capture = new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                () -> NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()));
        Integer surface = null, floor = null;
        try {
            for (int y = capture.minY(); y < capture.maxY() - 1; y++) {
                var pos = new BlockPos(candidate.x(), y, candidate.z());
                var cell = capture.at(pos);
                if (lava(cell) && cell.state().getFluidState().isSource() && !lava(capture.at(pos.above()))) {
                    surface = y; break;
                }
            }
            if (surface == null) return new Filter("SURFACE", null, null, capture.reads, "no_LOWEST_source");
            for (int z = -7; z <= 7; z++) for (int x = -7; x <= 7; x++) {
                int y = surface;
                var pos = new BlockPos(candidate.x() + x, y, candidate.z() + z);
                var cell = capture.at(pos);
                if (!lava(cell)) return new Filter("FLOOR_START", surface, floor, capture.reads, pos + " " + cell);
                do {
                    y--; pos = pos.below();
                    if (y < capture.minY()) return new Filter("HEIGHT", surface, floor, capture.reads, pos + " outside_world");
                    cell = capture.at(pos);
                } while (lava(cell));
                if (!rock(cell)) return new Filter("FLOOR_BOTTOM", surface, floor, capture.reads, pos + " " + cell);
                floor = floor == null ? y : Math.min(floor, y);
                if (floor < capture.minY() + 23)
                    return new Filter("FLOOR_BELOW_23", surface, floor, capture.reads, pos + " " + cell + " cubeMinY=" + (floor - 23));
            }
            capture.validate();
            // Depth >=4 is checked by the full connected-coverage calculation, not imposed on every footprint column.
            return new Filter("SURVIVOR", surface, floor, capture.reads, "225_valid_floors;coverage_depth_and_crust_not_yet_proven");
        } catch (NetherSnapshotDiagnostic.Aborted failure) {
            return new Filter("TECHNICAL_" + failure.state, surface, floor, capture.reads, "capture_aborted");
        } finally { capture.discard(); }
    }

    private static void sample(GameTestHelper helper, int index, Map<String, Integer> counts) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(level.getSeed() == 0L, "Qualification requires recorded seed 0");
        int regionX = -1800 - 2 * index, regionZ = -1800;
        var candidate = NetherSitePlanner.candidate(level.getSeed(), regionX, regionZ);
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        var chunk = new ChunkPos(origin);
        // Test fixture only: 64 NEW fixed regions, disjoint 5x5 neighborhoods; no edits or synthetic receipts.
        for (int z = -2; z <= 2; z++) for (int x = -2; x <= 2; x++) level.getChunk(chunk.x + x, chunk.z + z);
        int loadedTick = level.getServer().getTickCount();
        helper.runAfterDelay(1, () -> {
            var coordinator = NetherPlacementRuntime.coordinator(level);
            var selected = coordinator.takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount());
            helper.assertTrue(selected.contains(chunk) && coordinator.hasFreshReceipt(NetherPlacementRuntime.host(level), chunk.toLong()),
                    "Technical stop: actual canonical receipt unavailable at region " + regionX + "," + regionZ);
            var filtered = filter(level, candidate);
            helper.assertFalse(filtered.reason().startsWith("TECHNICAL_"), "Technical stop: " + filtered);
            NetherNaturalAdmission.Report report = null;
            boolean accepted = false;
            String outcome = "PREFILTER/" + filtered.reason();
            if (filtered.reason().equals("SURVIVOR")) {
                // Fresh production capture rechecks everything, including LOWEST and all cheap-filter observations.
                // Extra qualification reads are charged, not refunded; no extra tick or lease extension is granted.
                report = NetherNaturalAdmission.attemptDetailed(level, chunk);
                var result = report.result();
                helper.assertTrue(result == NetherPlacementCoordinator.Result.PLAN_REJECTED
                        || result == NetherPlacementCoordinator.Result.COMMITTED,
                        "Technical stop during full admission: " + report);
                var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
                helper.assertTrue(ledger.resultAt(origin).orElseThrow().equals(result.name()), "Outcome not durable");
                helper.assertTrue(NetherNaturalAdmission.attempt(level, chunk) == NetherPlacementCoordinator.Result.DUPLICATE, "Region rerolled");
                outcome = result + "/" + report.plannerStatus();
                accepted = result == NetherPlacementCoordinator.Result.COMMITTED;
                if (accepted) verifyAccepted(helper, level, candidate, filtered.floor(), origin);
            }
            counts.merge(outcome, 1, Integer::sum);
            com.mojang.logging.LogUtils.getLogger().info("IOE bounded64 sample: seed={} region={},{} candidate={},{} quality={} loadTick={} admissionTick={} actualReceipt=true loading=controlled_full_5x5 filter={} admission={} terrainEditsBeforeAdmission=0 injectedReceipts=0",
                    level.getSeed(), regionX, regionZ, candidate.x(), candidate.z(), candidate.quality(), loadedTick,
                    level.getServer().getTickCount(), filtered, report);
            if (accepted || index + 1 == MAX_REGIONS) {
                com.mojang.logging.LogUtils.getLogger().info("IOE bounded64 complete: seed={} sampled={} limit={} accepted={} outcomes={} loading=controlled_full_5x5_not_player_stream",
                        level.getSeed(), index + 1, MAX_REGIONS, accepted, counts);
                helper.succeed();
            } else {
                helper.runAfterDelay(2, () -> sample(helper, index + 1, counts));
            }
        });
    }

    private static void verifyAccepted(GameTestHelper helper, ServerLevel level, NetherSitePlanner.Candidate candidate,
                                       int floor, BlockPos origin) {
        var center = new BlockPos(candidate.x(), floor - 16, candidate.z());
        var resources = new LinkedHashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
        for (int z = -5; z <= 5; z++) for (int y = -5; y <= 5; y++) for (int x = -5; x <= 5; x++) {
            int squared = x*x + y*y + z*z;
            if (squared > 25) continue;
            var pos = center.offset(x, y, z);
            var loaded = level.getChunkSource().getChunkNow(new ChunkPos(pos).x, new ChunkPos(pos).z);
            helper.assertTrue(loaded != null, "Accepted chamber unloaded before verification");
            var state = loaded.getBlockState(pos);
            if (squared <= 9) helper.assertTrue(state.isAir(), "Cavity not carved at " + pos);
            else if (NetherOreProvenance.isResource(state)) resources.put(pos, state);
        }
        helper.assertTrue(resources.size() == NetherSitePlanner.budget(candidate.quality()), "Wrong natural mineral budget");
        long debris = resources.values().stream().filter(s -> s.is(Blocks.ANCIENT_DEBRIS)).count();
        helper.assertTrue(debris == (candidate.debrisSelected() ? 1 : 0), "Wrong debris replacement");
        var storage = level.getDataStorage();
        try {
            storage.save();
            net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
            var disk = storage.readTagFromDisk(NetherPlacementLedger.NAME, null,
                    net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            var reloaded = NetherPlacementLedger.FACTORY.deserializer().apply(disk.getCompound("data"), level.registryAccess());
            helper.assertFalse(reloaded.claim(origin), "Reload allowed natural replay");
            resources.forEach((pos, state) -> helper.assertTrue(reloaded.preserves(pos, state), "Reload lost provenance at " + pos));
            storage.set(NetherPlacementLedger.NAME, reloaded);
            helper.assertTrue(com.oblixorprime.ioe.worldgen.IoeNewChunkOreGuard.sanitizeLoadedChunk(level, new ChunkPos(center), true),
                    "Natural ore conservation not tested: guard no longer pending");
            resources.forEach((pos, state) -> helper.assertTrue(level.getChunkSource().getChunkNow(new ChunkPos(pos).x,
                    new ChunkPos(pos).z).getBlockState(pos).equals(state), "Sanitation removed natural IOE ore at " + pos));
            helper.assertTrue(NetherNaturalAdmission.attempt(level, new ChunkPos(origin)) == NetherPlacementCoordinator.Result.DUPLICATE,
                    "Reloaded world ledger allowed replay");
            com.mojang.logging.LogUtils.getLogger().info("IOE bounded64 accepted verified: center={} resources={} cavity=true savedLedger=true sanitation=true replay=false clientVisual=UNPROVEN", center, resources.size());
        } catch (java.io.IOException failure) { throw new RuntimeException(failure); }
    }
}
