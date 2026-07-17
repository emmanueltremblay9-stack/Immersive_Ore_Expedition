package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exact in-memory authorization for resource blocks written by an IOE site during chunk generation.
 * The locator remains the durable site record; these positions only protect the first-load ore guard.
 */
final class IoeOrePlacementAuthorization {
    private static final ConcurrentHashMap<
            ResourceKey<Level>,
            ConcurrentHashMap<Long, ConcurrentHashMap<Long, ResourceLocation>>
    > POSITIONS =
            new ConcurrentHashMap<>();

    private IoeOrePlacementAuthorization() {
    }

    static void authorize(ResourceKey<Level> dimension, ExpeditionSiteBlockPlan plan) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(plan, "plan");
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, ResourceLocation>> positionsByChunk =
                POSITIONS.computeIfAbsent(dimension, ignored -> new ConcurrentHashMap<>());
        for (var entry : plan.blocks().entrySet()) {
            if (IoeNewChunkOreGuard.isCandidate(entry.getValue())) {
                BlockPos pos = entry.getKey();
                positionsByChunk
                        .computeIfAbsent(
                                ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4),
                                ignored -> new ConcurrentHashMap<>()
                        )
                        .put(pos.asLong(), BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()));
            }
        }
    }

    static boolean matches(ResourceKey<Level> dimension, BlockPos pos, BlockState state) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(state, "state");
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, ResourceLocation>> positionsByChunk = POSITIONS.get(dimension);
        if (positionsByChunk == null) {
            return false;
        }
        ConcurrentHashMap<Long, ResourceLocation> positions =
                positionsByChunk.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        return positions != null
                && BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(positions.get(pos.asLong()));
    }

    static int positionCount() {
        return POSITIONS.values().stream()
                .flatMap(positionsByChunk -> positionsByChunk.values().stream())
                .mapToInt(positions -> positions.size())
                .sum();
    }

    static void releaseChunk(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, ResourceLocation>> positionsByChunk = POSITIONS.get(dimension);
        if (positionsByChunk == null) {
            return;
        }
        positionsByChunk.remove(chunkPos.toLong());
    }

    static void releaseDimension(ResourceKey<Level> dimension) {
        POSITIONS.remove(Objects.requireNonNull(dimension, "dimension"));
    }

    static void clear() {
        POSITIONS.clear();
    }
}
