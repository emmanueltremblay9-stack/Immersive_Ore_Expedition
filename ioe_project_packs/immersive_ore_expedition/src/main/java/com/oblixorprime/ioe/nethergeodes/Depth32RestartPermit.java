package com.oblixorprime.ioe.nethergeodes;

import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.LongPredicate;

/** CI fixture provenance only. No player-save migration or production permission. */
final class Depth32RestartPermit {
    private Depth32RestartPermit() { }
    record Context(boolean ci,String run,String attempt,String sha) { }
    static String phase() { return System.getProperty("ioe.depth32RestartPhase", ""); }
    static Path module() { return Path.of(System.getProperty("ioe.depth32Pack")).toAbsolutePath().getParent().getParent(); }
    static Path evidence() { return module().resolve("build/reports/ioe/depth32-restart"); }
    static String processStart() { return ProcessHandle.current().info().startInstant().orElseThrow().toString(); }
    static void authorizeRuntime(Path root) {
        try {
            require(root.toRealPath().equals(module().resolve("run/gametest-full").toRealPath()),"Only existing full-runtime profile allowed");
            require(Boolean.getBoolean("ioe.expectDomumRuntime"),"Full runtime required");
            var ctx=new Context("true".equals(System.getenv("GITHUB_ACTIONS")),System.getenv("GITHUB_RUN_ID"),
                    System.getenv("GITHUB_RUN_ATTEMPT"),System.getenv("GITHUB_SHA"));
            authorize(root,evidence().resolve("session.json"),phase(),ctx,ProcessHandle.current().pid(),processStart(),
                    pid->ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false));
        } catch(Exception e) { throw new IllegalStateException("Depth32 CI fixture authorization refused",e); }
    }
    static void authorize(Path root,Path session,String phase,Context ctx,long pid,String start,LongPredicate alive) throws Exception {
        require(ctx.ci() && ctx.run()!=null && ctx.attempt()!=null && ctx.sha()!=null,"GitHub Actions identity required");
        root=root.toRealPath();
        require(!Files.isSymbolicLink(session),"Session symlink refused");
        var permit=read(session);
        require(permit.get("schema").getAsInt()==1,"Unknown permit schema");
        require(root.toString().equals(permit.get("root").getAsString()),"World root mismatch");
        require(ctx.run().equals(permit.get("run").getAsString()) && ctx.attempt().equals(permit.get("attempt").getAsString())
                && ctx.sha().equals(permit.get("sha").getAsString()),"Run/attempt/commit mismatch");
        require(permit.get("nonce").getAsString().matches("[0-9a-f]{64}"),"Invalid execution nonce");
        var origin=root.resolve(".ioe-depth32-origin.json");
        if(phase.equals("fresh")) {
            NetherDepth32Prototype.requireFreshDirectory(root);
            require(permit.get("phase").getAsString().equals("fresh"),"Fresh phase mismatch");
            var created=permit.deepCopy();created.addProperty("producer_pid",pid);created.addProperty("producer_start",start);
            Files.writeString(origin,created.toString(),StandardOpenOption.CREATE_NEW);
            return;
        }
        require(phase.equals("resume") && permit.get("phase").getAsString().equals("sealed"),"Resume permit missing or consumed");
        require(Files.isRegularFile(origin) && !Files.isSymbolicLink(origin),"Unknown world: missing origin");
        require(digest(origin).equals(permit.get("origin_sha256").getAsString()),"Origin changed");
        var created=read(origin);
        for(var key:List.of("nonce","root","run","attempt","sha"))
            require(created.get(key).equals(permit.get(key)),"Origin binding mismatch: "+key);
        long producer=permit.get("producer_pid").getAsLong();
        require(created.get("producer_pid").getAsLong()==producer && created.get("producer_start").equals(permit.get("producer_start")),"Producer identity mismatch");
        require(producer!=pid && !alive.test(producer),"Producer process has not stopped");
        var proof=session.getParent().resolve("expected.json");
        require(digest(proof).equals(permit.get("proof_sha256").getAsString()),"Expected-state proof changed");
        var expected=read(proof);
        require(expected.get("nonce").equals(permit.get("nonce")) && expected.get("producer_pid").getAsLong()==producer,"Proof binding mismatch");
        var world=root.resolve("world");
        require(world.toRealPath().toString().equals(expected.get("world").getAsString()),"Unknown saved world path");
        require(Files.isRegularFile(world.resolve("level.dat")),"Saved level missing");
        var files=snapshot(world);
        require(files.equals(permit.getAsJsonObject("world_files")),"World files changed or unknown");
        try(var paths=Files.walk(root)) {
            for(var path:paths.toList()) {
                String n=path.getFileName().toString();
                if(n.equals("level.dat") || n.equals("level.dat_old") || n.endsWith(".mca"))
                    require(path.toAbsolutePath().normalize().startsWith(world),"Additional unknown world in profile");
            }
        }
        permit.addProperty("phase","consumed");permit.addProperty("consumer_pid",pid);permit.addProperty("consumer_start",start);
        Files.writeString(session,permit.toString(),StandardOpenOption.TRUNCATE_EXISTING);
    }
    static JsonObject snapshot(Path world) throws Exception {
        var result=new JsonObject();long bytes=0;int count=0;
        try(var paths=Files.walk(world)) {
            for(var p:paths.sorted().toList()) {
                require(!Files.isSymbolicLink(p),"World symlink refused");
                if(!Files.isRegularFile(p))continue;
                String relative=world.relativize(p).toString().replace('\\','/');
                require(Arrays.stream(relative.split("/")).noneMatch(s->List.of("playerdata","players","stats","advancements").contains(s)),"Player data refused");
                if(relative.equals("session.lock"))continue;
                require(++count<=4096 && (bytes+=Files.size(p))<=2147483648L,"Fixture world exceeds bounded snapshot");
                result.addProperty(relative,digest(p));
            }
        }
        return result;
    }
    static String digest(Path file) throws Exception {
        require(Files.isRegularFile(file) && !Files.isSymbolicLink(file),"Missing or linked evidence file");
        var hash=MessageDigest.getInstance("SHA-256");
        try(var in=Files.newInputStream(file)) {var b=new byte[65536];int n;while((n=in.read(b))!=-1)hash.update(b,0,n);}
        return HexFormat.of().formatHex(hash.digest());
    }
    static JsonObject read(Path p) throws Exception { return JsonParser.parseString(Files.readString(p,StandardCharsets.UTF_8)).getAsJsonObject(); }
    static void require(boolean condition,String message) { if(!condition)throw new IllegalStateException(message); }
}
