package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
import java.util.*;

@GameTestHolder("ioe_restart")
@PrefixGameTestTemplate(false)
public final class Depth32RestartGameTests {
    @GameTest(template="restart_empty",templateNamespace="ioe_restart",timeoutTicks=200)
    public static void persistedBlocksAndLedgerInSecondProcess(GameTestHelper helper) throws Exception {
        Depth32RestartPermit.require(Depth32RestartPermit.phase().equals("resume"),"Restart test requires authorized resume");
        var session=Depth32RestartPermit.read(Depth32RestartPermit.evidence().resolve("session.json"));
        Depth32RestartPermit.require(session.get("phase").getAsString().equals("consumed") && session.get("consumer_pid").getAsLong()==ProcessHandle.current().pid(),"Resume not consumed by this process");
        var proofPath=Depth32RestartPermit.evidence().resolve("expected.json");
        Depth32RestartPermit.require(Depth32RestartPermit.digest(proofPath).equals(session.get("proof_sha256").getAsString()),"Proof changed after authorization");
        var proof=Depth32RestartPermit.read(proofPath);
        var level=helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(NetherWorldActivation.identity(level).orElseThrow().toString().equals(proof.get("world_generation_id").getAsString()),"Restart changed generation identity");
        helper.assertTrue(NetherWorldActivation.selectedBeforeRegistries(level.getServer().getPackRepository()),"Restart pack selected after registries");
        helper.assertTrue(level.getMinBuildHeight()==-32 && level.getMaxBuildHeight()==256 && level.getSeed()==0,"Prototype world configuration changed");
        helper.assertTrue(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toRealPath().toString().equals(proof.get("world").getAsString()),"Different world opened");
        var chunks=new LinkedHashSet<ChunkPos>();
        for(var value:proof.getAsJsonArray("blocks"))chunks.add(new ChunkPos(BlockPos.of(value.getAsJsonObject().get("pos").getAsLong())));
        helper.assertTrue(chunks.size()==2 && proof.getAsJsonArray("blocks").size()==3377,"Unexpected persisted fixture size");
        for(var pos:chunks) {level.getChunk(pos.x,pos.z);level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED,pos,0,pos);}
        helper.runAfterDelay(1,()->{
            var host=NetherPlacementRuntime.host(level);
            for(var pos:chunks)helper.assertFalse(NetherPlacementRuntime.coordinator(level).hasFreshReceipt(host,pos.toLong()),"Restart grants new-generation receipt");
            com.mojang.logging.LogUtils.getLogger().info("IOE process restart freshness: ticks=1 chunks=2 freshReceipt=false");
        });
        helper.runAfterDelay(20,()->{
            var host=NetherPlacementRuntime.host(level);
            var ledger=level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY,NetherPlacementLedger.NAME);
            for(var value:proof.getAsJsonArray("blocks")) {
                var b=value.getAsJsonObject();var pos=BlockPos.of(b.get("pos").getAsLong());
                helper.assertTrue(host.reserveReads(1),"Restart verification read budget exhausted");
                helper.assertTrue(host.read(pos).toString().equals(b.get("state").getAsString()),"Restart lost block at "+pos);
            }
            int negative=0;
            for(var value:proof.getAsJsonArray("resources")) {
                var pos=BlockPos.of(value.getAsLong());if(pos.getY()<0)negative++;
                helper.assertTrue(host.reserveReads(1),"Restart resource read budget exhausted");
                helper.assertTrue(ledger.preserves(pos,host.read(pos)),"Restart lost resource provenance at "+pos);
            }
            helper.assertTrue(negative==1 && proof.getAsJsonArray("resources").size()==20,"Restart resource evidence differs");
            for(var value:proof.getAsJsonArray("markers")) {
                var pos=BlockPos.of(value.getAsLong());helper.assertTrue(host.reserveReads(1),"Restart marker read budget exhausted");
                helper.assertTrue(host.read(pos).is(Blocks.BLACKSTONE),"Restart lost shore marker");
            }
            var origin=new ChunkPos(proof.get("chunk_x").getAsInt(),proof.get("chunk_z").getAsInt());
            helper.assertTrue(NetherNaturalAdmission.attempt(level,origin)==NetherPlacementCoordinator.Result.DUPLICATE,"Restart permits placement replay");
            for(var pos:chunks) {
                helper.assertFalse(NetherPlacementRuntime.coordinator(level).hasFreshReceipt(host,pos.toLong()),"Restart acquired fresh receipt");
                level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.FORCED,pos,0,pos);
            }
            try {
                var result=new com.google.gson.JsonObject();result.addProperty("producer_pid",proof.get("producer_pid").getAsLong());
                result.addProperty("consumer_pid",ProcessHandle.current().pid());result.addProperty("blocks",3377);result.addProperty("resources",20);result.addProperty("markers",2);result.addProperty("negative_resources",negative);
                result.addProperty("world_generation_id",NetherWorldActivation.identity(level).orElseThrow().toString());
                result.addProperty("selected_before_registries",true);
                result.addProperty("replay",false);result.addProperty("fresh_receipt",false);result.addProperty("player_streaming",false);
                Files.writeString(Depth32RestartPermit.evidence().resolve("verified.json"),result.toString(),StandardOpenOption.CREATE_NEW);
            } catch(Exception e) {throw new IllegalStateException(e);}
            com.mojang.logging.LogUtils.getLogger().info("IOE complete process restart verified: producerPid={} consumerPid={} blocks=3377 resources=20 markers=2 negativeResources=1 savedLedger=true replay=false freshReceipt=false blockReads=3399 playerStreaming=false",proof.get("producer_pid").getAsLong(),ProcessHandle.current().pid());
            helper.succeed();
        });
    }
}
