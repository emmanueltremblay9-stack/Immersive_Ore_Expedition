package com.oblixorprime.ioe.nethergeodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.fml.loading.FMLLoader;

/** External qualification pack only: never shipped as generation data or enabled in production. */
public final class NetherDepth32Prototype {
    private NetherDepth32Prototype() { }
    public static boolean requested() { return Boolean.getBoolean("ioe.depth32Prototype"); }
    public static void register(IEventBus bus) {
        if (!requested()) return;
        if (FMLLoader.isProduction() || !GameTestHooks.isGametestServer())
            throw new IllegalStateException("Depth32 is restricted to development GameTest servers");
        requireFreshDirectory(Path.of(System.getProperty("user.dir")));
        var root=Path.of(System.getProperty("ioe.depth32Pack", "")).toAbsolutePath();
        if (!Files.isRegularFile(root.resolve("pack.mcmeta")))
            throw new IllegalStateException("External depth32 prototype pack missing");
        bus.addListener((AddPackFindersEvent event)->{
            if(event.getPackType()!=PackType.SERVER_DATA) return;
            var pack=Pack.readMetaAndCreate(
                    new PackLocationInfo("ioe_depth32_new_world_test",Component.literal("IOE depth32 test only"),PackSource.BUILT_IN,Optional.empty()),
                    BuiltInPackSource.fromName(info->new PathPackResources(info,root)),
                    PackType.SERVER_DATA,new PackSelectionConfig(true,Pack.Position.TOP,true));
            if(pack==null) throw new IllegalStateException("Invalid depth32 prototype pack");
            event.addRepositorySource(consumer->consumer.accept(pack));
        });
    }
    static void requireFreshDirectory(Path directory) {
        try(var paths=Files.walk(directory)) {
            if(paths.anyMatch(path->{var n=path.getFileName().toString();
                return Files.isRegularFile(path) && (n.equals("level.dat") || n.equals("level.dat_old") || n.endsWith(".mca"));
            })) throw new IllegalStateException("Depth32 refuses existing saves/chunks; no deletion or migration is performed");
        } catch(IOException e) { throw new IllegalStateException("Cannot establish new-world-only scope",e); }
    }
}
