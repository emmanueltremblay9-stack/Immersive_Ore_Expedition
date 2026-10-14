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
    @GameTest(template="streaming_empty",templateNamespace="ioe_streaming",timeoutTicks=3000)
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
        observe(helper,level,player,c,chunk,0,null,0,-1);
    }
    private static int loaded(ServerLevel level,ChunkPos chunk) {
        int count=0;
        for(int z=-2;z<=2;z++)for(int x=-2;x<=2;x++)
            if(level.getChunkSource().getChunkNow(chunk.x+x,chunk.z+z)!=null)count++;
        return count;
    }
    private static void observe(GameTestHelper helper,ServerLevel level,ServerPlayer player,
            NetherSitePlanner.Candidate c,ChunkPos chunk,int elapsed,
            NetherPlacementCoordinator.Result recorded,int countAtAdmission,int admissionTick) {
        // Position advances at ordinary walking speed; vanilla player tracking owns all tickets.
        double x=Math.min(c.x(),c.x()-320+elapsed*0.22);
        player.setPos(x,100,c.z());level.getChunkSource().move(player);
        int count=loaded(level,chunk);
        var result=NetherNaturalTrigger.dispatch(level).get(chunk.toLong());
        if(result!=null) {
            helper.assertTrue(recorded==null,"Candidate dispatched twice");
            recorded=result;countAtAdmission=count;admissionTick=elapsed;
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
                out.addProperty("result",recorded.name());out.addProperty("activation_qualified",recorded==NetherPlacementCoordinator.Result.COMMITTED);
                out.addProperty("forced_by_ioe",false);out.addProperty("injected_receipts",0);out.addProperty("walking_blocks_per_tick",0.22);
                out.addProperty("requested_view_distance",player.requestedViewDistance());
                out.addProperty("server_view_distance",level.getServer().getPlayerList().getViewDistance());out.addProperty("replay",false);
                var path=Depth32RestartPermit.module().resolve("build/reports/ioe/player-streaming.json");Files.createDirectories(path.getParent());Files.writeString(path,out.toString(),StandardOpenOption.CREATE_NEW);
                com.mojang.logging.LogUtils.getLogger().info("IOE player streaming observation complete: {}",out);
            } catch(Exception e) {throw new IllegalStateException(e);}
            level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            helper.succeed();return;
        }
        helper.assertTrue(elapsed<2800,"Player stream did not complete: neighbors="+count+" result="+recorded);
        var saved=recorded;int savedCount=countAtAdmission,savedTick=admissionTick;
        helper.runAfterDelay(1,()->observe(helper,level,player,c,chunk,elapsed+1,saved,savedCount,savedTick));
    }
}
