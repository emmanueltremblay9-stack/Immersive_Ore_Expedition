package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;

/** Bounded player-ticket observation in the existing full-runtime CI profile. */
@GameTestHolder("ioe_streaming")
@PrefixGameTestTemplate(false)
public final class NetherPlayerStreamingGameTests {
    private static final class PreparationObservation {
        int firstFullTick = -1, oldestAtAdmission = -1, candidateAgeAtAdmission = -1;
    }
    @GameTest(template="streaming_empty",templateNamespace="ioe_streaming",timeoutTicks=4200)
    public static void playerTicketsReachKnownCandidate(GameTestHelper helper) {
        var level=helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(NetherDepth32Prototype.requested() && level.getSeed()==0 && level.getMinBuildHeight()==-32,"Expected fresh depth32 fixture");
        var c=NetherSitePlanner.candidate(0,-2014,-2000);
        var chunk=new ChunkPos(new BlockPos(c.x(),0,c.z()));
        helper.assertTrue(level.getChunkSource().getChunkNow(chunk.x,chunk.z)==null,"Candidate already loaded before player approach");
        var player=new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ioe-stream-observer")) {
            @Override public int requestedViewDistance() { return 4; }
        };
        level.getServer().getPlayerList().setViewDistance(4);
        player.setPos(c.x()-320,100,c.z());
        level.addNewPlayer(player);
        player.setNoGravity(true);player.noPhysics=true;
        player.getAbilities().flying=true;player.getAbilities().invulnerable=true;
        observe(helper,level,player,c,chunk,0,null,0,-1,new PreparationObservation());
    }
    private static int loaded(ServerLevel level,ChunkPos chunk) {
        int count=0;
        for(int z=-2;z<=2;z++)for(int x=-2;x<=2;x++)
            if(level.getChunkSource().getChunkNow(chunk.x+x,chunk.z+z)!=null)count++;
        return count;
    }
    private static void observe(GameTestHelper helper,ServerLevel level,ServerPlayer player,
            NetherSitePlanner.Candidate c,ChunkPos chunk,int elapsed,
            NetherPlacementCoordinator.Result recorded,int countAtAdmission,int admissionTick,PreparationObservation observation) {
        // GameTestServer runs without normal tick pacing. Give asynchronous terrain workers
        // the same nominal 50 ms between observations; this fixture owns no chunk tickets.
        long until=System.nanoTime()+50_000_000L;
        for(long remaining;(remaining=until-System.nanoTime())>0;)
            java.util.concurrent.locks.LockSupport.parkNanos(remaining);
        // Position advances at ordinary walking speed; vanilla player tracking owns all tickets.
        double x=Math.min(c.x(),c.x()-320+elapsed*0.22);
        player.setPos(x,100,c.z());level.getChunkSource().move(player);
        int count=loaded(level,chunk);
        if(count==25 && observation.firstFullTick<0) observation.firstFullTick=elapsed;
        var coordinator=NetherPlacementRuntime.coordinator(level);
        int tick=level.getServer().getTickCount(), oldest=-1;
        for(int dz=-2;dz<=2;dz++)for(int dx=-2;dx<=2;dx++)
            oldest=Math.max(oldest,coordinator.preparationAge(ChunkPos.asLong(chunk.x+dx,chunk.z+dz),tick));
        int candidateAge=coordinator.preparationAge(chunk.toLong(),tick);
        var result=NetherNaturalTrigger.dispatch(level).get(chunk.toLong());
        if(result!=null) {
            helper.assertTrue(recorded==null,"Candidate dispatched twice");
            recorded=result;countAtAdmission=count;admissionTick=elapsed;
            observation.oldestAtAdmission=oldest;observation.candidateAgeAtAdmission=candidateAge;
            com.mojang.logging.LogUtils.getLogger().info("IOE player streaming admission: candidate={},{} elapsed={} fullNeighbors={} result={} forcedByIOE=false injectedReceipts=0",c.x(),c.z(),elapsed,count,result);
        }
        if(elapsed>=1800 && count==25 && recorded!=null) {
            var origin=new BlockPos(c.x(),level.getMinBuildHeight(),c.z());
            var ledger=level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY,NetherPlacementLedger.NAME);
            helper.assertTrue(ledger.resultAt(origin).orElseThrow().equals(recorded.name()),"Terminal outcome missing");
            helper.assertTrue(NetherNaturalAdmission.attempt(level,chunk)==NetherPlacementCoordinator.Result.DUPLICATE,"Later neighborhood permits reroll");
            try {
                var out=new com.google.gson.JsonObject();
                out.addProperty("source",System.getenv("GITHUB_SHA"));out.addProperty("pid",ProcessHandle.current().pid());
                out.addProperty("world",level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toRealPath().toString());
                out.addProperty("candidate_x",c.x());out.addProperty("candidate_z",c.z());out.addProperty("admission_elapsed",admissionTick);
                out.addProperty("full_neighbors_at_admission",countAtAdmission);out.addProperty("full_neighbors_final",count);
                out.addProperty("first_full_elapsed",observation.firstFullTick);
                out.addProperty("oldest_preparation_age_at_admission",observation.oldestAtAdmission);
                out.addProperty("candidate_preparation_age_at_admission",observation.candidateAgeAtAdmission);
                out.addProperty("preparation_limit_ticks",NetherPlacementCoordinator.PREPARATION_TICKS);
                out.addProperty("result",recorded.name());out.addProperty("activation_qualified",recorded==NetherPlacementCoordinator.Result.COMMITTED);
                out.addProperty("forced_by_ioe",false);out.addProperty("injected_receipts",0);out.addProperty("walking_blocks_per_tick",0.22);out.addProperty("fixture_tick_pause_ms",50);
                out.addProperty("requested_view_distance",player.requestedViewDistance());
                out.addProperty("server_view_distance",level.getServer().getPlayerList().getViewDistance());out.addProperty("replay",false);
                var path=Depth32RestartPermit.module().resolve("build/reports/ioe/player-streaming.json");Files.createDirectories(path.getParent());Files.writeString(path,out.toString(),StandardOpenOption.CREATE_NEW);
                com.mojang.logging.LogUtils.getLogger().info("IOE player streaming observation complete: {}",out);
            } catch(Exception e) {throw new IllegalStateException(e);}
            helper.assertTrue(recorded==NetherPlacementCoordinator.Result.COMMITTED,"Natural progressive placement rejected: "+recorded);
            level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            drain(helper,level,0,0);return;
        }
        helper.assertTrue(elapsed<2800,"Player stream did not complete: neighbors="+count+" result="+recorded);
        var saved=recorded;int savedCount=countAtAdmission,savedTick=admissionTick;
        helper.runAfterDelay(1,()->observe(helper,level,player,c,chunk,elapsed+1,saved,savedCount,savedTick,observation));
    }
    private static void drain(GameTestHelper helper,ServerLevel level,int elapsed,int idleTicks) {
        // Continue ordinary server ticks while player tickets and asynchronous generation drain.
        // Never save/close synchronously with outstanding generation work.
        long until=System.nanoTime()+50_000_000L;
        for(long remaining;(remaining=until-System.nanoTime())>0;)
            java.util.concurrent.locks.LockSupport.parkNanos(remaining);
        boolean idle=level.players().isEmpty() && level.getChunkSource().getPendingTasksCount()==0
                && !level.getChunkSource().getLightEngine().hasLightWork();
        int consecutive=idle ? idleTicks+1 : 0;
        if(elapsed>=1000 && consecutive>=5) {
            com.mojang.logging.LogUtils.getLogger().info("IOE player streaming cleanup: ticks={} idleTicks={} pendingTasks=0 lightWork=false players=0 loadedChunks={}",elapsed,consecutive,level.getChunkSource().getLoadedChunksCount());
            helper.succeed();return;
        }
        helper.assertTrue(elapsed<1200,"Player fixture tasks did not settle after removing player");
        helper.runAfterDelay(1,()->drain(helper,level,elapsed+1,consecutive));
    }
}
