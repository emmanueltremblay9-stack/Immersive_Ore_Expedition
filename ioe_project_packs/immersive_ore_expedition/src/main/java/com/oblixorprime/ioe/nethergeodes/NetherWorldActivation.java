package com.oblixorprime.ioe.nethergeodes;

import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.storage.LevelResource;

/** Per-world activation routing. Production remains closed while hosted qualification runs. */
public final class NetherWorldActivation {
    public static final boolean PRODUCTION_ENABLED = false;
    public static final String PACK_ID = "ioe_depth32_new_world_test";
    static final String MANIFEST = "ioe-world-generation.json";
    private static final Map<PackRepository, Binding> WORLDS = new WeakHashMap<>();
    private static final Set<PackRepository> SOURCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<PackRepository> SELECTED_BEFORE_REGISTRIES = Collections.newSetFromMap(new WeakHashMap<>());
    record Identity(UUID id, int schema, int depth) { }
    private record Binding(Path root, Identity identity) { }
    private NetherWorldActivation() { }
    private static boolean routingEnabled() { return PRODUCTION_ENABLED || NetherDepth32Prototype.requested(); }

    /** Called on the repository attached to the actual locked LevelStorageAccess, before registries. */
    public static synchronized void bind(PackRepository repository, Path world) {
        if (!routingEnabled()) return;
        try {
            Path root = world.toRealPath();
            Identity identity = selectIdentity(root);
            WORLDS.put(repository, new Binding(root, identity));
            if (identity != null) addPack(repository);
        } catch (IOException e) { throw new IllegalStateException("Cannot establish IOE world identity", e); }
    }
    /** An unmarked old save remains excluded, even when it generates previously unseen chunks. */
    static Identity selectIdentity(Path root) throws IOException {
        Path marker = root.resolve(MANIFEST);
        if (Files.exists(marker)) return readIdentity(marker);
        try (var paths = Files.walk(root)) {
            if (paths.anyMatch(path -> Files.isRegularFile(path) && (
                    path.getFileName().toString().equals("level.dat")
                    || path.getFileName().toString().equals("level.dat_old")
                    || path.getFileName().toString().endsWith(".mca")))) return null;
        }
        var identity = new Identity(UUID.randomUUID(), 1, 32);
        var json = new JsonObject();
        json.addProperty("world_id", identity.id().toString());
        json.addProperty("schema", identity.schema());json.addProperty("depth", identity.depth());
        json.addProperty("pack_id", PACK_ID);
        Files.writeString(marker, json.toString(), StandardOpenOption.CREATE_NEW);
        return identity;
    }
    static Identity readIdentity(Path marker) throws IOException {
        try {
            var json = JsonParser.parseString(Files.readString(marker)).getAsJsonObject();
            var identity = new Identity(UUID.fromString(json.get("world_id").getAsString()),
                    json.get("schema").getAsInt(),json.get("depth").getAsInt());
            if (identity.schema()!=1 || identity.depth()!=32 || !PACK_ID.equals(json.get("pack_id").getAsString()))
                throw new IllegalStateException("Unsupported IOE generation identity; no migration");
            return identity;
        } catch (RuntimeException e) { throw new IOException("Invalid IOE generation identity", e); }
    }
    /** HEAD of PackConfig.createResourceManager, before configurePackRepository/WORLDGEN registries. */
    public static synchronized void beforeRegistries(PackRepository repository, WorldDataConfiguration data,
                                                       boolean initMode, boolean safeMode) {
        if (!routingEnabled()) return;
        var binding = WORLDS.get(repository);
        boolean selected = data.dataPacks().getEnabled().contains(PACK_ID);
        if (binding != null) {
            if (binding.identity() == null) {
                if (selected) throw new IllegalStateException("IOE pack selected on an unmarked existing save");
                return;
            }
            if (safeMode) throw new IllegalStateException("Depth32 world requires its generation pack");
            addPack(repository);
        } else if (initMode && !safeMode) {
            // CreateWorldScreen's temporary creation repository has no save yet. The actual
            // new directory receives its identity later through the LevelStorageAccess hook.
            addPack(repository);
        } else if (selected) {
            throw new IllegalStateException("IOE pack has no bound world identity");
        }
    }
    public static synchronized void afterPackSelection(PackRepository repository) {
        if (!routingEnabled() || !SOURCES.contains(repository)) return;
        if (!repository.getSelectedIds().contains(PACK_ID))
            throw new IllegalStateException("Depth32 pack was not selected before registry loading");
        SELECTED_BEFORE_REGISTRIES.add(repository);
        com.mojang.logging.LogUtils.getLogger().info("IOE generation pack selected before WORLDGEN registries: {}", PACK_ID);
    }
    static synchronized boolean selectedBeforeRegistries(PackRepository repository) {
        return SELECTED_BEFORE_REGISTRIES.contains(repository);
    }
    private static void addPack(PackRepository repository) {
        if (!SOURCES.add(repository)) return;
        Path root = NetherDepth32Prototype.requested()
                ? Path.of(System.getProperty("ioe.depth32Pack")).toAbsolutePath()
                : net.neoforged.fml.ModList.get().getModFileById("immersive_ore_expedition")
                    .getFile().findResource("packs", "ioe_depth32");
        var pack = Pack.readMetaAndCreate(new PackLocationInfo(PACK_ID,
                Component.literal("IOE new-world Nether depth 32"),PackSource.BUILT_IN,Optional.empty()),
                BuiltInPackSource.fromName(info -> new PathPackResources(info, root)),
                PackType.SERVER_DATA,new PackSelectionConfig(true,Pack.Position.TOP,true));
        if (pack == null) throw new IllegalStateException("IOE generation pack missing");
        repository.addPackFinder(consumer -> consumer.accept(pack));
    }
    static synchronized Optional<UUID> identity(ServerLevel level) {
        var server = level.getServer();
        var binding = WORLDS.get(server.getPackRepository());
        if (binding == null || binding.identity() == null) return Optional.empty();
        if (!server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(binding.root())) return Optional.empty();
        if (!server.getWorldData().getDataConfiguration().dataPacks().getEnabled().contains(PACK_ID)) return Optional.empty();
        if (level.getMinBuildHeight()!=-32 || level.getMaxBuildHeight()!=256) return Optional.empty();
        return Optional.of(binding.identity().id());
    }
    static boolean permits(ServerLevel level) { return routingEnabled() && identity(level).isPresent(); }
}
