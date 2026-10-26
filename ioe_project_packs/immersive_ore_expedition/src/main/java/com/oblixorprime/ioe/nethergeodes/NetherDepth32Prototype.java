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
        if(Depth32RestartPermit.phase().isEmpty()) requireFreshDirectory(Path.of(System.getProperty("user.dir")));
        else Depth32RestartPermit.authorizeRuntime(Path.of(System.getProperty("user.dir")));
        var root=Path.of(System.getProperty("ioe.depth32Pack", "")).toAbsolutePath();
        if (!Files.isRegularFile(root.resolve("pack.mcmeta")))
            throw new IllegalStateException("External depth32 prototype pack missing");
        // Per-world repository and pre-registry hooks now select this pack. No global finder.
    }
    static void requireFreshDirectory(Path directory) {
        try(var paths=Files.walk(directory)) {
            if(paths.anyMatch(path->{var n=path.getFileName().toString();
                return Files.isRegularFile(path) && (n.equals("level.dat") || n.equals("level.dat_old") || n.endsWith(".mca"));
            })) throw new IllegalStateException("Depth32 refuses existing saves/chunks; no deletion or migration is performed");
        } catch(IOException e) { throw new IllegalStateException("Cannot establish new-world-only scope",e); }
    }
}
