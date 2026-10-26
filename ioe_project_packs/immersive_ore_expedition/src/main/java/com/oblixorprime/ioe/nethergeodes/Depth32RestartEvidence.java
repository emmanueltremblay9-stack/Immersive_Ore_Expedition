package com.oblixorprime.ioe.nethergeodes;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import java.nio.file.*;
import java.util.*;

final class Depth32RestartEvidence {
    private Depth32RestartEvidence() { }
    static void record(ServerLevel level,ChunkPos chunk,Map<BlockPos,BlockState> states,Map<BlockPos,BlockState> resources,List<BlockPos> markers) {
        if(!Depth32RestartPermit.phase().equals("fresh"))return;
        try {
            Depth32RestartPermit.require(states.size()==3377 && resources.size()==20 && markers.size()==2,"Incomplete restart fixture");
            var origin=Depth32RestartPermit.read(Path.of(System.getProperty("user.dir")).resolve(".ioe-depth32-origin.json"));
            var proof=new JsonObject();proof.add("nonce",origin.get("nonce"));
            proof.addProperty("producer_pid",ProcessHandle.current().pid());proof.addProperty("producer_start",Depth32RestartPermit.processStart());
            proof.addProperty("world_generation_id",NetherWorldActivation.identity(level).orElseThrow().toString());
            proof.addProperty("world",level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toRealPath().toString());
            proof.addProperty("chunk_x",chunk.x);proof.addProperty("chunk_z",chunk.z);
            var blocks=new JsonArray();states.forEach((pos,state)->{var b=new JsonObject();b.addProperty("pos",pos.asLong());b.addProperty("state",state.toString());blocks.add(b);});proof.add("blocks",blocks);
            var ores=new JsonArray();resources.keySet().forEach(pos->ores.add(pos.asLong()));proof.add("resources",ores);
            var shore=new JsonArray();markers.forEach(pos->shore.add(pos.asLong()));proof.add("markers",shore);
            Files.writeString(Depth32RestartPermit.evidence().resolve("expected.json"),proof.toString(),StandardOpenOption.CREATE_NEW);
            com.mojang.logging.LogUtils.getLogger().info("IOE restart fixture recorded: producerPid={} observedBlocks=3377 resources=20 markers=2",ProcessHandle.current().pid());
        } catch(Exception e) {throw new IllegalStateException("Cannot record restart evidence",e);}
    }
}
