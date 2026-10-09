package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** First-generation tracking and an explicit prepared backend; no automatic placement caller. */
final class NetherPlacementRuntime {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final Map<MinecraftServer, NetherPlacementCoordinator> COORDINATORS = new WeakHashMap<>();
    static synchronized NetherPlacementCoordinator coordinator(ServerLevel level) {
        return COORDINATORS.computeIfAbsent(level.getServer(), ignored -> new NetherPlacementCoordinator());
    }
    static void register() {
        if (!REGISTERED.compareAndSet(false, true)) return;
        NeoForge.EVENT_BUS.addListener(NetherPlacementRuntime::load);
        NeoForge.EVENT_BUS.addListener(NetherPlacementRuntime::unload);
        NeoForge.EVENT_BUS.addListener(NetherPlacementRuntime::placed);
        NeoForge.EVENT_BUS.addListener(NetherPlacementRuntime::broken);
        NeoForge.EVENT_BUS.addListener(NetherPlacementRuntime::stopped);
    }
    private static void load(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.NETHER))
            coordinator(level).observe(event.getChunk().getPos().toLong(), event.getChunk(), event.isNewChunk(), level.getServer().getTickCount());
    }
    private static void unload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.NETHER))
            coordinator(level).invalidate(event.getChunk().getPos().toLong());
    }
    private static void invalidate(BlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.NETHER))
            coordinator(level).invalidate(new net.minecraft.world.level.ChunkPos(event.getPos()).toLong());
    }
    private static void placed(BlockEvent.EntityPlaceEvent event) { invalidate(event); }
    private static void broken(BlockEvent.BreakEvent event) { invalidate(event); }
    private static synchronized void stopped(ServerStoppedEvent event) { COORDINATORS.remove(event.getServer()); }

    static NetherPlacementCoordinator.Result commit(ServerLevel level, NetherPlacementCoordinator.Plan plan) {
        var host = host(level);
        host.requireServerThread();
        // setBlock may invoke reentrant callbacks; the journal is not an atomicity guarantee.
        // Do not consume a region claim while the production backend is unqualified.
        return NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED;
    }

    /** Explicit prepared placement backend. No command, tick hook or generator calls this entry. */
    static NetherPlacementCoordinator.Result commitPrepared(ServerLevel level, NetherPlacementCoordinator.Plan plan) {
        return commitPrepared(level, plan, coordinator(level), host(level));
    }

    // Package-scoped capability/host seam for controlled faults against real storage/chunks.
    static NetherPlacementCoordinator.Result commitPrepared(ServerLevel level, NetherPlacementCoordinator.Plan plan,
                                                            NetherPlacementCoordinator coordinator,
                                                            NetherPlacementCoordinator.Host host) {
        host(level).requireServerThread();
        var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
        return coordinator.commit(host, ledger, plan);
    }

    static NetherPlacementCoordinator.Host host(ServerLevel level) {
        return new NetherPlacementCoordinator.Host() {
            public void requireServerThread() {
                if (!level.dimension().equals(Level.NETHER) || !level.getServer().isSameThread())
                    throw new IllegalStateException("Nether server thread required");
            }
            public int tick() { return level.getServer().getTickCount(); }
            public Object loadedChunk(long key) {
                var pos = new net.minecraft.world.level.ChunkPos(key);
                return level.getChunkSource().getChunkNow(pos.x, pos.z);
            }
            public BlockState read(BlockPos pos) {
                var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk == null) throw new IllegalStateException("Chunk unavailable");
                return chunk.getBlockState(pos);
            }
            // External claim integration remains unqualified; production commit stays gated.
            public boolean protectedAt(BlockPos pos, BlockState state) { return state.hasBlockEntity(); }
            public boolean safeToReplace(BlockPos pos, BlockState state) {
                return !state.hasBlockEntity() && state.getFluidState().isEmpty()
                        && (state.isAir() || state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE));
            }
            public boolean write(BlockPos pos, BlockState state) {
                if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) return false;
                return level.setBlock(pos, state, 2);
            }
            public boolean reserveReads(int count) {
                return NetherAnalysisBudget.forServer(level.getServer()).acquire(tick(), count);
            }
        };
    }
}
