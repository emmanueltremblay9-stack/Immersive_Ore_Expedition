package com.oblixorprime.ioe.nethergeodes;

import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

final class Depth32RestartPermitTest {
    @TempDir Path temp;
    final Depth32RestartPermit.Context context=new Depth32RestartPermit.Context(true,"123","1","a".repeat(40));
    Path root,session;
    void setup() throws Exception {
        root=Files.createDirectory(temp.resolve("profile"));var report=Files.createDirectory(temp.resolve("report"));session=report.resolve("session.json");
        var p=new JsonObject();p.addProperty("schema",1);p.addProperty("phase","fresh");p.addProperty("root",root.toRealPath().toString());
        p.addProperty("nonce","b".repeat(64));p.addProperty("run","123");p.addProperty("attempt","1");p.addProperty("sha","a".repeat(40));Files.writeString(session,p.toString());
    }
    void seal() throws Exception {
        Depth32RestartPermit.authorize(root,session,"fresh",context,11,"first",pid->false);
        var world=Files.createDirectory(root.resolve("world"));Files.writeString(world.resolve("level.dat"),"fixture");
        var proof=new JsonObject();proof.addProperty("nonce","b".repeat(64));proof.addProperty("producer_pid",11);proof.addProperty("world",world.toRealPath().toString());
        var proofPath=session.getParent().resolve("expected.json");Files.writeString(proofPath,proof.toString());
        var p=Depth32RestartPermit.read(session);p.addProperty("phase","sealed");p.addProperty("producer_pid",11);p.addProperty("producer_start","first");
        p.addProperty("origin_sha256",Depth32RestartPermit.digest(root.resolve(".ioe-depth32-origin.json")));
        p.addProperty("proof_sha256",Depth32RestartPermit.digest(proofPath));p.add("world_files",Depth32RestartPermit.snapshot(world));Files.writeString(session,p.toString());
    }
    void resume() throws Exception {Depth32RestartPermit.authorize(root,session,"resume",context,22,"second",pid->false);}
    @Test void exactFixtureAllowedOnceOnly() throws Exception {
        setup();seal();resume();assertEquals("consumed",Depth32RestartPermit.read(session).get("phase").getAsString());
        assertThrows(IllegalStateException.class,this::resume);
    }
    @Test void existingUnknownWorldRefusedBeforeOriginCreation() throws Exception {
        setup();Files.createDirectory(root.resolve("world"));var file=root.resolve("world/level.dat");Files.writeString(file,"player save");
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"fresh",context,11,"first",pid->false));
        assertFalse(Files.exists(root.resolve(".ioe-depth32-origin.json")));assertEquals("player save",Files.readString(file));
    }
    @Test void tamperedWorldAndProofRefusedWithoutConsumption() throws Exception {
        setup();seal();var file=root.resolve("world/level.dat");Files.writeString(file,"unknown replacement");
        assertThrows(IllegalStateException.class,this::resume);assertEquals("sealed",Depth32RestartPermit.read(session).get("phase").getAsString());
        Files.writeString(file,"fixture");Files.writeString(session.getParent().resolve("expected.json"),"{}");assertThrows(IllegalStateException.class,this::resume);
        Files.delete(root.resolve(".ioe-depth32-origin.json"));assertThrows(IllegalStateException.class,this::resume);
    }
    @Test void wrongExecutionNonCiAndLiveProducerRefused() throws Exception {
        setup();seal();
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",new Depth32RestartPermit.Context(true,"other","1","a".repeat(40)),22,"second",pid->false));
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",new Depth32RestartPermit.Context(false,"123","1","a".repeat(40)),22,"second",pid->false));
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",new Depth32RestartPermit.Context(true,"123","2","a".repeat(40)),22,"second",pid->false));
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",new Depth32RestartPermit.Context(true,"123","1","c".repeat(40)),22,"second",pid->false));
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",context,22,"second",pid->true));
        assertThrows(IllegalStateException.class,()->Depth32RestartPermit.authorize(root,session,"resume",context,11,"second",pid->false));
    }
    @Test void playerDataUnknownAdditionalWorldAndLinksRefused() throws Exception {
        setup();seal();var players=Files.createDirectory(root.resolve("world/playerdata"));var player=players.resolve("owner.dat");Files.writeString(player,"player");
        assertThrows(IllegalStateException.class,this::resume);Files.delete(player);
        var extra=Files.createDirectory(root.resolve("unknown"));var save=extra.resolve("level.dat");Files.writeString(save,"unknown");assertThrows(IllegalStateException.class,this::resume);Files.delete(save);
        var link=root.resolve("world/external");Files.createSymbolicLink(link,temp.resolve("report"));assertThrows(IllegalStateException.class,this::resume);
    }
}
