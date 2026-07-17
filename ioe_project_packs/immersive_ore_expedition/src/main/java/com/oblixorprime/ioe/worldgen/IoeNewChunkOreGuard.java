package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Last-line enforcement for newly generated chunks in every dimension. Normal removal remains biome-modifier driven;
 * this guard catches ore or autonomous budding resources placed later by another generator.
 */
public final class IoeNewChunkOreGuard {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_CLIENTS;
    private static final int SANITIZATIONS_PER_TICK = 1;
    private static final int FINAL_SANITIZATION_DELAY_TICKS = 20;
    private static final Set<PendingChunk> PENDING_NEW_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final Set<PendingChunk> SCHEDULED_SANITIZATIONS = ConcurrentHashMap.newKeySet();
    private static final Queue<PendingChunk> INITIAL_SANITIZATION_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<PendingChunk, Integer> FINAL_SANITIZATION_TICKS =
            new ConcurrentHashMap<>();
    private static boolean prioritizeFinalSanitization;

    private IoeNewChunkOreGuard() {
    }

    public static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(IoeNewChunkOreGuard::onChunkLoad);
            NeoForge.EVENT_BUS.addListener(IoeNewChunkOreGuard::onServerTick);
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ChunkPos chunkPos = event.getChunk().getPos();
        PendingChunk chunkKey = new PendingChunk(level.dimension(), chunkPos.toLong());
        if (event.isNewChunk()) {
            PENDING_NEW_CHUNKS.add(chunkKey);
        } else if (!PENDING_NEW_CHUNKS.contains(chunkKey)) {
            return;
        }
        if (!SCHEDULED_SANITIZATIONS.add(chunkKey)) {
            return;
        }
        INITIAL_SANITIZATION_QUEUE.add(chunkKey);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        int currentTick = event.getServer().getTickCount();
        for (int pass = 0; pass < SANITIZATIONS_PER_TICK; pass++) {
            PendingFinal pendingFinal = findDueFinalSanitization(currentTick);
            boolean hasInitial = !INITIAL_SANITIZATION_QUEUE.isEmpty();
            if (!hasInitial && pendingFinal == null) {
                break;
            }

            boolean runFinal = shouldRunFinalSanitization(
                    hasInitial,
                    pendingFinal != null,
                    prioritizeFinalSanitization
            );
            if (pendingFinal != null && hasInitial) {
                prioritizeFinalSanitization = !prioritizeFinalSanitization;
            }
            if (runFinal) {
                runFinalSanitization(event, pendingFinal);
            } else {
                runInitialSanitization(event);
            }
        }
    }

    private static PendingFinal findDueFinalSanitization(int currentTick) {
        for (var entry : FINAL_SANITIZATION_TICKS.entrySet()) {
            if (isFinalSanitizationEligible(
                    entry.getValue(),
                    currentTick,
                    SCHEDULED_SANITIZATIONS.contains(entry.getKey())
            )) {
                return new PendingFinal(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    static boolean isFinalSanitizationEligible(
            int scheduledTick,
            int currentTick,
            boolean sameChunkInitialQueued
    ) {
        return scheduledTick <= currentTick && !sameChunkInitialQueued;
    }

    static boolean shouldRunFinalSanitization(
            boolean hasInitial,
            boolean hasDueFinal,
            boolean prioritizeFinal
    ) {
        return hasDueFinal && (!hasInitial || prioritizeFinal);
    }

    private static void runInitialSanitization(ServerTickEvent.Post event) {
        PendingChunk chunkKey;
        do {
            chunkKey = INITIAL_SANITIZATION_QUEUE.poll();
        } while (chunkKey != null && !SCHEDULED_SANITIZATIONS.remove(chunkKey));
        if (chunkKey == null) {
            return;
        }
        if (!PENDING_NEW_CHUNKS.contains(chunkKey)) {
            FINAL_SANITIZATION_TICKS.remove(chunkKey);
            return;
        }

        ServerLevel level = event.getServer().getLevel(chunkKey.dimension());
        ChunkPos chunkPos = new ChunkPos(chunkKey.chunkPos());
        if (level == null) {
            PENDING_NEW_CHUNKS.remove(chunkKey);
            FINAL_SANITIZATION_TICKS.remove(chunkKey);
            IoeOrePlacementAuthorization.releaseChunk(chunkKey.dimension(), chunkPos);
            return;
        }
        if (sanitizeLoadedChunk(level, chunkPos, false)) {
            int finalTick = event.getServer().getTickCount() + FINAL_SANITIZATION_DELAY_TICKS;
            FINAL_SANITIZATION_TICKS.put(chunkKey, finalTick);
        }
    }

    private static void runFinalSanitization(ServerTickEvent.Post event, PendingFinal pendingFinal) {
        PendingChunk chunkKey = pendingFinal.chunkKey();
        if (!FINAL_SANITIZATION_TICKS.remove(chunkKey, pendingFinal.scheduledTick())) {
            return;
        }

        ServerLevel level = event.getServer().getLevel(chunkKey.dimension());
        ChunkPos chunkPos = new ChunkPos(chunkKey.chunkPos());
        if (level == null) {
            PENDING_NEW_CHUNKS.remove(chunkKey);
            IoeOrePlacementAuthorization.releaseChunk(chunkKey.dimension(), chunkPos);
            return;
        }
        if (sanitizeLoadedChunk(level, chunkPos, true)) {
            PENDING_NEW_CHUNKS.remove(chunkKey);
        }
    }

    private static boolean sanitizeLoadedChunk(ServerLevel level, ChunkPos chunkPos, boolean finalizeSite) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) {
            return false;
        }

        List<Target> targets = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir() || !section.maybeHas(IoeNewChunkOreGuard::isCandidate)) {
                continue;
            }
            int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
            int minY = SectionPos.sectionToBlockCoord(sectionY);
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        TargetKind kind = targetKind(state);
                        if (kind == null) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(
                                chunkPos.getMinBlockX() + localX,
                                minY + localY,
                                chunkPos.getMinBlockZ() + localZ
                        );
                        if (IoeOrePlacementAuthorization.matches(level.dimension(), pos, state)) {
                            continue;
                        }
                        targets.add(new Target(pos, kind));
                    }
                }
            }
        }

        int removedOres = 0;
        int removedGrowthBlocks = 0;
        for (Target target : targets) {
            BlockState replacement = replacementState(level, target.pos());
            if (level.setBlock(target.pos(), replacement, BLOCK_UPDATE_FLAGS)) {
                if (target.kind() == TargetKind.ORE) {
                    removedOres++;
                } else {
                    removedGrowthBlocks++;
                }
            }
        }
        if (finalizeSite) {
            IoePendingExpeditionSites.Confirmation confirmation =
                    IoePendingExpeditionSites.confirmLoadedChunk(level, chunkPos);
            IoeOrePlacementAuthorization.releaseChunk(level.dimension(), chunkPos);
            for (BlockPos rejectedResourcePos : confirmation.rejectedResourcePositions()) {
                TargetKind kind = targetKind(level.getBlockState(rejectedResourcePos));
                if (kind == null || !level.setBlock(
                        rejectedResourcePos,
                        replacementState(level, rejectedResourcePos),
                        BLOCK_UPDATE_FLAGS
                )) {
                    continue;
                }
                if (kind == TargetKind.ORE) {
                    removedOres++;
                } else {
                    removedGrowthBlocks++;
                }
            }
        }
        IoeWorldgenRuntimeDiagnostics.recordGuardPass(removedOres, removedGrowthBlocks, finalizeSite);
        if (removedOres > 0 || removedGrowthBlocks > 0) {
            IoeExpeditionWorldgenMod.LOGGER.warn(
                    "IOE sanitized unauthorized resources in new chunk {} pass={}: ores={}, growthBlocks={}",
                    chunkPos,
                    finalizeSite ? "final" : "initial",
                    removedOres,
                    removedGrowthBlocks
            );
        }
        return true;
    }

    static void clearPending() {
        PENDING_NEW_CHUNKS.clear();
        SCHEDULED_SANITIZATIONS.clear();
        INITIAL_SANITIZATION_QUEUE.clear();
        FINAL_SANITIZATION_TICKS.clear();
        prioritizeFinalSanitization = false;
    }

    private static BlockState replacementState(ServerLevel level, BlockPos pos) {
        if (Level.NETHER.equals(level.dimension())) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (Level.END.equals(level.dimension())) {
            return Blocks.END_STONE.defaultBlockState();
        }
        return pos.getY() < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    static boolean isCandidate(BlockState state) {
        return targetKind(state) != null;
    }

    private static TargetKind targetKind(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.is(Tags.Blocks.ORES)
                || Ae2MeteoriteIntegration.forbiddenCertusOre(id)
                || forbiddenVanillaOre(id)) {
            return TargetKind.ORE;
        }
        String namespace = id.getNamespace();
        String path = id.getPath();
        if (GeOreNodeIntegration.MOD_ID.equals(namespace)
                && (path.startsWith("budding_")
                || path.endsWith("_block")
                || path.contains("_bud")
                || path.endsWith("_cluster")
                || state.is(Tags.Blocks.BUDS)
                || state.is(Tags.Blocks.CLUSTERS))) {
            return TargetKind.GROWTH_RESOURCE;
        }
        return null;
    }

    private static boolean forbiddenVanillaOre(ResourceLocation id) {
        if (!ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
            return false;
        }
        return id.getPath().equals("nether_quartz_ore")
                || id.getPath().equals("nether_gold_ore")
                || id.getPath().equals("ancient_debris");
    }

    private enum TargetKind {
        ORE,
        GROWTH_RESOURCE
    }

    private record Target(BlockPos pos, TargetKind kind) {
    }

    private record PendingFinal(PendingChunk chunkKey, int scheduledTick) {
    }

    private record PendingChunk(ResourceKey<Level> dimension, long chunkPos) {
    }
}
