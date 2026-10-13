package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fixed earlier cohort: real unedited terrain, bounded current-planner observation. */
@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherFloorObservationGameTests {
    private static final int[][] REGIONS = { {-2006,-2000},
            {-2014,-2000},
            {-2024,-2000},
            {-2026,-2000},
            {-2028,-2000},
            {-2030,-2000},
            {-2034,-2000},
            {-2042,-2000},
            {-2048,-2000},
            {-2052,-2000},
            {-2058,-2000},
            {-2064,-2000},
            {-2068,-2000},
            {-2070,-2000},
            {-2076,-2000},
            {-2094,-2000},
            {-2004,-2000},
            {-2008,-2000},
            {-2010,-2000},
            {-2032,-2000},
            {-2036,-2000},
            {-2038,-2000},
            {-2044,-2000},
            {-2046,-2000},
            {-2000,-2000},
            {-2002,-2000},
            {-2022,-2000},
            {-2074,-2000},
            {-2012,-2000},
            {-2016,-2000},
            {-2018,-2000},
            {-2020,-2000} };
    @GameTest(template="expedition_worldgen_empty",batch="ioe_nether_floor_observation",timeoutTicks=1200)
    public static void approvedFloorsOnPreviouslyStudiedTerrain(GameTestHelper helper) {
        com.mojang.logging.LogUtils.getLogger().info("IOE floor cohort start: source={} seed=0 minecraft=1.21.1 neoforge=21.1.230 regions=32 baseline={} controlled_full_5x5=true planner_only=true",System.getenv("GITHUB_SHA"),!net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"));
        sample(helper,0);
    }
    @GameTest(template="expedition_worldgen_empty",batch="ioe_gravel_support",timeoutTicks=100)
    public static void gravelSupportMatchesVanillaTicks(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1,1,1),net.minecraft.world.level.block.Blocks.NETHERRACK);
        helper.setBlock(new BlockPos(1,2,1),net.minecraft.world.level.block.Blocks.GRAVEL);
        helper.setBlock(new BlockPos(3,0,1),net.minecraft.world.level.block.Blocks.BLACKSTONE);
        helper.setBlock(new BlockPos(3,1,1),net.minecraft.world.level.block.Blocks.AIR);
        helper.setBlock(new BlockPos(3,2,1),net.minecraft.world.level.block.Blocks.GRAVEL);
        var host=NetherPlacementRuntime.host(helper.getLevel().getServer().getLevel(Level.NETHER));
        for(var block:java.util.List.of(net.minecraft.world.level.block.Blocks.NETHERRACK,
                net.minecraft.world.level.block.Blocks.MAGMA_BLOCK,net.minecraft.world.level.block.Blocks.GRAVEL,
                net.minecraft.world.level.block.Blocks.BLACKSTONE,net.minecraft.world.level.block.Blocks.BASALT,
                net.minecraft.world.level.block.Blocks.SOUL_SAND,net.minecraft.world.level.block.Blocks.SOUL_SOIL))
            helper.assertTrue(host.safeToReplace(BlockPos.ZERO,block.defaultBlockState()),"Native substrate refused by host");
        helper.assertFalse(host.safeToReplace(BlockPos.ZERO,net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState()),"Host permits bedrock");
        helper.runAfterDelay(20,()->{
            helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.GRAVEL,new BlockPos(1,2,1));
            helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR,new BlockPos(3,2,1));
            helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.GRAVEL,new BlockPos(3,1,1));
            helper.succeed();
        });
    }
    private static void sample(GameTestHelper helper,int index) {
        var level=helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(level.getSeed()==0L,"Expected recorded seed");
        int rx=REGIONS[index][0],rz=REGIONS[index][1];
        var candidate=NetherSitePlanner.candidate(level.getSeed(),rx,rz);
        var chunk=new ChunkPos(new BlockPos(candidate.x(),0,candidate.z()));
        // Qualification fixture only. Production never requests these neighborhoods.
        for(int z=-2;z<=2;z++) for(int x=-2;x<=2;x++) level.getChunk(chunk.x+x,chunk.z+z);
        helper.runAfterDelay(1,()->{
            var coordinator=NetherPlacementRuntime.coordinator(level);
            helper.assertTrue(coordinator.takeNaturalCandidates(level.getSeed(),level.getServer().getTickCount()).contains(chunk)
                    && coordinator.hasFreshReceipt(NetherPlacementRuntime.host(level),chunk.toLong()),"Missing real new-chunk receipt");
            if(rx==-2014 && rz==-2000) {
                normalAdmission(helper,index,level,candidate,chunk);
                return;
            }
            var capture=new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                    ()->NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()));
            NetherPlacementCoordinator.Plan committedPlan=null;
            try {
                var result=NetherSitePlanner.planLowest(candidate,capture);
                var experimental=NetherSitePlanner.planLowest(candidate,capture,40);
                helper.assertTrue(experimental.status()!=NetherSitePlanner.Status.BUDGET
                        && experimental.status()!=NetherSitePlanner.Status.UNKNOWN_TERRAIN,"Experimental qualification failure");
                helper.assertTrue(result.connectedDeepColumns()==experimental.connectedDeepColumns(),"Paired coverage diverged");
                com.mojang.logging.LogUtils.getLogger().info("IOE coverage40 row: region={},{} candidate={},{} control={} experimental={} connected={} controlProbes={} experimentalProbes={} plannedWrites={} actualWrites=0",
                        rx,rz,candidate.x(),candidate.z(),result.status(),experimental.status(),experimental.connectedDeepColumns(),result.probes(),experimental.probes(),experimental.plan()==null?0:experimental.plan().writes().size());
                helper.assertTrue(result.status()!=NetherSitePlanner.Status.BUDGET
                        && result.status()!=NetherSitePlanner.Status.UNKNOWN_TERRAIN,"Technical qualification failure");
                if (experimental.plan()==null) for(var entry:capture.cells.entrySet()) {
                    helper.assertTrue(NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()),"Read-set budget exhausted");
                    var identity=capture.identities.get(new ChunkPos(entry.getKey()).toLong());
                    helper.assertTrue(entry.getValue().equals(capture.source.read(identity,entry.getKey())),"Read set changed");
                }
                capture.validate();
                if (candidate.x()==-512472 && candidate.z()==-511864) {
                    var failure=experimental.volumeRejection();
                    com.mojang.logging.LogUtils.getLogger().info("IOE bedrock target outcome: candidate={},{} status={} plannedWrites={} actualWrites=0 validated=true",
                            candidate.x(),candidate.z(),experimental.status(),experimental.plan()==null?0:experimental.plan().writes().size());
                    if (failure!=null) {
                    var state=failure.cell().state();
                    com.mojang.logging.LogUtils.getLogger().info("IOE targeted volume rejection: candidate={},{} center={} position={} offset={} state={} protected={} fluidEmpty={} blockEntity={} allowedRock={} validated=true actualWrites=0",
                            candidate.x(),candidate.z(),failure.center(),failure.position(),failure.position().subtract(failure.center()),
                            state,failure.cell().protectedBlock(),state.getFluidState().isEmpty(),state.hasBlockEntity(),
                            NetherSitePlanner.naturalSubstrate(state));
                    }
                    if (experimental.plan()!=null) {
                        helper.assertTrue(experimental.plan().writes().keySet().stream().noneMatch(pos ->
                                experimental.plan().expected().get(pos).is(net.minecraft.world.level.block.Blocks.BEDROCK)),"Plan writes bedrock");
                    }
                }
                com.mojang.logging.LogUtils.getLogger().info("IOE floor cohort row: region={},{} candidate={},{} status={} connected={} probes={} captureReads={} revalidationReads={} plannedWrites={} actualWrites=0 edits=0 injectedReceipts=0",
                        rx,rz,candidate.x(),candidate.z(),result.status(),result.connectedDeepColumns(),result.probes(),capture.reads,capture.cells.size(),result.plan()==null?0:result.plan().writes().size());
                if(experimental.plan()!=null) {
                    var ledger=level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY,NetherPlacementLedger.NAME);
                    var admission=ledger.prepare(experimental.plan().origin());
                    helper.assertTrue(admission!=null,"Duplicate experimental region");
                    var committed=NetherPlacementRuntime.commitPrepared(level,experimental.plan(),admission);
                    com.mojang.logging.LogUtils.getLogger().info("IOE natural palette placement: region={},{} result={} writes={} injectedReceipts=0",
                            rx,rz,committed,committed==NetherPlacementCoordinator.Result.COMMITTED?experimental.plan().writes().size():0);
                    helper.assertTrue(committed==NetherPlacementCoordinator.Result.COMMITTED,"Natural prepared placement failed: "+committed);
                    helper.assertTrue(ledger.prepare(experimental.plan().origin())==null,"Committed region can replay");
                    // Test-only radius 2 ticket keeps exactly the already loaded FULL 5x5;
                    // its center is block-ticking during the delayed stability assertion.
                    level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED,chunk,2,chunk);
                    committedPlan=experimental.plan();
                }
            } finally {capture.discard();}
            var placed=committedPlan;
            helper.runAfterDelay(20,()->{
                if(placed!=null) {
                    var host=NetherPlacementRuntime.host(level);
                    for(var entry:placed.expected().entrySet()) {
                        if(!placed.writes().containsKey(entry.getKey()) && !entry.getValue().is(net.minecraft.world.level.block.Blocks.GRAVEL)
                                && !entry.getValue().is(net.minecraft.world.level.block.Blocks.BEDROCK)) continue;
                        helper.assertTrue(host.reserveReads(1),"Post-placement verification budget exhausted");
                        helper.assertTrue(host.read(entry.getKey()).equals(placed.writes().getOrDefault(entry.getKey(),entry.getValue())),
                                "Placed geometry or retained gravel/bedrock changed after ticks: "+entry.getKey());
                    }
                    var storage=level.getDataStorage();
                    try {
                        storage.save();
                        net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
                        var disk=storage.readTagFromDisk(NetherPlacementLedger.NAME,null,
                                net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                        var reloaded=NetherPlacementLedger.FACTORY.deserializer().apply(disk.getCompound("data"),level.registryAccess());
                        helper.assertFalse(reloaded.claim(placed.origin()),"Reload permits cohort replay");
                        placed.writes().forEach((pos,state)->{
                            if(NetherOreProvenance.isResource(state)) helper.assertTrue(reloaded.preserves(pos,state),"Reload lost cohort resource at "+pos);
                        });
                        storage.set(NetherPlacementLedger.NAME,reloaded);
                        com.mojang.logging.LogUtils.getLogger().info("IOE depth32 cohort persistence: region={},{} negativeWrites={} savedLedger=true replay=false",rx,rz,placed.writes().keySet().stream().filter(pos->pos.getY()<0).count());
                    } catch(java.io.IOException failure) { throw new RuntimeException(failure); }
                    level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.FORCED,chunk,2,chunk);
                    com.mojang.logging.LogUtils.getLogger().info("IOE natural palette retained-state verification: region={},{} ticks=20 passed=true",rx,rz);
                }
                if(index+1==REGIONS.length) {
                    com.mojang.logging.LogUtils.getLogger().info("IOE floor cohort complete: regions=32 conditional_prepared_placement=true");
                    helper.succeed();
                } else sample(helper,index+1);
            });
        });
    }
    /** Replaces this candidate's paired/prepared experiment; no duplicate world or admission. */
    private static void normalAdmission(GameTestHelper helper,int index,net.minecraft.server.level.ServerLevel level,
                                        NetherSitePlanner.Candidate candidate,ChunkPos chunk) {
        var filtered=NetherGeneratedTerrainGameTests.filter(level,candidate);
        helper.assertTrue(filtered.reason().equals("SURVIVOR"),"Known candidate prefilter changed: "+filtered);
        var center=new BlockPos(candidate.x(),filtered.floor()-16,candidate.z());
        var origin=new BlockPos(candidate.x(),level.getMinBuildHeight(),candidate.z());
        var host=NetherPlacementRuntime.host(level);
        var before=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(int z=-7;z<=7;z++) for(int y=-7;y<=7;y++) for(int x=-7;x<=7;x++) {
            var pos=center.offset(x,y,z);
            helper.assertTrue(host.reserveReads(1),"Before-volume read budget exhausted");
            before.put(pos,host.read(pos));
        }
        int admissionTick=level.getServer().getTickCount();
        var report=NetherNaturalAdmission.attemptDetailed(level,chunk);
        boolean committed=report.result()==NetherPlacementCoordinator.Result.COMMITTED;
        helper.assertTrue(committed || report.result()==NetherPlacementCoordinator.Result.PLAN_REJECTED,
                "Normal admission technical failure: "+report);
        if(NetherDepth32Prototype.requested() && net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))
            helper.assertTrue(committed,"Known full-runtime candidate did not commit through normal 60% admission");
        var after=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        var resources=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        int changed=0,negative=0;
        for(var entry:before.entrySet()) {
            var pos=entry.getKey();
            helper.assertTrue(host.reserveReads(1),"After-volume read budget exhausted");
            var state=host.read(pos);after.put(pos,state);
            if(!state.equals(entry.getValue())) {
                changed++;if(pos.getY()<0)negative++;
                helper.assertFalse(entry.getValue().is(net.minecraft.world.level.block.Blocks.BEDROCK),"Normal admission wrote bedrock");
            }
            int dx=pos.getX()-center.getX(),dy=pos.getY()-center.getY(),dz=pos.getZ()-center.getZ();
            int radius=dx*dx+dy*dy+dz*dz;
            if(committed && radius<=9)helper.assertTrue(state.isAir(),"Normal cavity incomplete");
            if(committed && NetherOreProvenance.isResource(state)) {
                helper.assertTrue(radius>9 && radius<=25,"Resource outside canonical shell");
                resources.put(pos,state);
            }
        }
        helper.assertTrue(changed==(committed?report.plannedWrites()-2:0),"Observed cube changes disagree with admission excluding its two shore markers");
        if(committed) {
            helper.assertTrue(resources.size()==NetherSitePlanner.budget(candidate.quality(filtered.soulSoilFloor())),"Normal resource budget differs");
            helper.assertTrue(resources.values().stream().filter(state->state.is(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS)).count()
                    ==(candidate.debrisSelected(filtered.soulSoilFloor())?1:0),"Normal debris replacement differs");
        }
        helper.assertTrue(NetherNaturalAdmission.attempt(level,chunk)==NetherPlacementCoordinator.Result.DUPLICATE,"Normal admission replayed");
        com.mojang.logging.LogUtils.getLogger().info("IOE normal60 known candidate: candidate={},{} result={} planner={} connected={} admissionCaptureReads={} prefilterReads={} fixtureBeforeReads={} fixtureAfterReads={} cubeChanged={} committedPlanWrites={} shoreWritesOutsideObservation=2 negativeWrites={} admissionTick={} finalTick={} commitReadCounter=not_instrumented loading=controlled_full_5x5 terrainEditsBeforeAdmission=0 injectedReceipts=0",
                candidate.x(),candidate.z(),report.result(),report.plannerStatus(),report.connectedColumns(),report.worldReads(),filtered.reads(),before.size(),after.size(),changed,committed?report.plannedWrites():0,negative,admissionTick,level.getServer().getTickCount());
        level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED,chunk,2,chunk);
        helper.runAfterDelay(20,()->{
            if(committed) for(var entry:after.entrySet()) {
                helper.assertTrue(host.reserveReads(1),"Delayed-volume budget exhausted");
                helper.assertTrue(host.read(entry.getKey()).equals(entry.getValue()),"Normal volume changed after 20 ticks: "+entry.getKey());
            }
            var storage=level.getDataStorage();
            try {
                storage.save();net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
                var disk=storage.readTagFromDisk(NetherPlacementLedger.NAME,null,net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                var loaded=NetherPlacementLedger.FACTORY.deserializer().apply(disk.getCompound("data"),level.registryAccess());
                helper.assertFalse(loaded.claim(origin),"Disk ledger permits normal replay");
                resources.forEach((pos,state)->helper.assertTrue(loaded.preserves(pos,state),"Disk ledger lost normal resource at "+pos));
                storage.set(NetherPlacementLedger.NAME,loaded);
                helper.assertTrue(NetherNaturalAdmission.attempt(level,chunk)==NetherPlacementCoordinator.Result.DUPLICATE,"Reloaded normal admission replayed");
            } catch(java.io.IOException failure) {throw new RuntimeException(failure);}
            level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.FORCED,chunk,2,chunk);
            com.mojang.logging.LogUtils.getLogger().info("IOE normal60 known verification: committed={} delayedReads={} ticks=20 committedVolumeStable=true savedLedger=true replay=false chunkReload=false processRestart=false playerStreaming=false",committed,committed?after.size():0);
            sample(helper,index+1);
        });
    }
}
