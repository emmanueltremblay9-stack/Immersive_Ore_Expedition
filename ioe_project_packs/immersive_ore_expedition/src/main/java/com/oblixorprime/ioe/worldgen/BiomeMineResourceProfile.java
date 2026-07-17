package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Selects exactly one biome-owned IE mineral mix. Site shape never participates in resource selection. */
public record BiomeMineResourceProfile(
        ResourceLocation biomeId,
        ResourceLocation definitionId,
        BiomeMineResourceDefinition definition,
        int sampledConnectedChunks
) {
    public BiomeMineResourceProfile {
        Objects.requireNonNull(biomeId, "biomeId");
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(definition, "definition");
        if (sampledConnectedChunks <= 0) {
            throw new IllegalArgumentException("Connected biome chunk count must be positive");
        }
    }

    public static Resolution resolve(WorldGenLevel level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");

        ServerChunkCache chunkSource = level.getLevel().getChunkSource();
        BiomeSource biomeSource = chunkSource.getGenerator().getBiomeSource();
        RandomState randomState = chunkSource.randomState();
        ChunkPos originChunk = new ChunkPos(origin);
        Holder<Biome> originBiome = sampleBiomeAtBlock(biomeSource, randomState, origin);
        ResourceKey<Biome> originBiomeKey = originBiome.unwrapKey().orElse(null);
        if (originBiomeKey == null) {
            return Resolution.missing();
        }

        Registry<BiomeMineResourceDefinition> definitionRegistry = level.registryAccess()
                .registry(BiomeMineResourceDefinition.REGISTRY_KEY)
                .orElse(null);
        if (definitionRegistry == null) {
            IoeExpeditionWorldgenMod.LOGGER.error("Missing IOE mine resource profile registry at origin={}", origin);
            return Resolution.missing();
        }

        List<Map.Entry<ResourceKey<BiomeMineResourceDefinition>, BiomeMineResourceDefinition>> matchingDefinitions =
                definitionRegistry.entrySet().stream()
                        .filter(candidate -> originBiome.is(candidate.getValue().biomeTag()))
                        .toList();
        if (matchingDefinitions.isEmpty()) {
            return Resolution.missing();
        }
        if (matchingDefinitions.size() > 1) {
            IoeExpeditionWorldgenMod.LOGGER.warn(
                    "Rejected ambiguous IOE mine resource biome={} origin={} matches={}",
                    originBiomeKey.location(),
                    origin,
                    matchingDefinitions.stream().map(candidate -> candidate.getKey().location()).toList()
            );
            return Resolution.ambiguous();
        }

        Map.Entry<ResourceKey<BiomeMineResourceDefinition>, BiomeMineResourceDefinition> selected =
                matchingDefinitions.getFirst();
        BiomeMineResourceDefinition definition = selected.getValue();
        int connectedChunks = countConnectedChunks(
                biomeSource,
                randomState,
                originChunk,
                origin.getY(),
                originBiomeKey,
                definition.surveyRadiusChunks()
        );
        return Resolution.resolved(new BiomeMineResourceProfile(
                originBiomeKey.location(),
                selected.getKey().location(),
                definition,
                connectedChunks
        ));
    }

    public String profileName() {
        return definition.resourceName();
    }

    public ResourceLocation mineralMixId() {
        return definition.mineralMixId();
    }

    public Optional<BiomeMineResourceDefinition.DepositTier> depositTier(SiteQuality quality) {
        return definition.tier(quality);
    }

    private static int countConnectedChunks(
            BiomeSource biomeSource,
            RandomState randomState,
            ChunkPos origin,
            int sampleY,
            ResourceKey<Biome> targetBiome,
            int surveyRadiusChunks
    ) {
        ArrayDeque<ChunkPos> pending = new ArrayDeque<>();
        LinkedHashSet<Long> visited = new LinkedHashSet<>();
        pending.add(origin);
        int connected = 0;
        while (!pending.isEmpty()) {
            ChunkPos current = pending.removeFirst();
            if (Math.abs(current.x - origin.x) > surveyRadiusChunks
                    || Math.abs(current.z - origin.z) > surveyRadiusChunks
                    || !visited.add(current.toLong())) {
                continue;
            }
            boolean matchesTargetBiome = current.equals(origin)
                    || sampleBiome(biomeSource, randomState, current, sampleY).is(targetBiome);
            if (!matchesTargetBiome) {
                continue;
            }
            connected++;
            pending.add(new ChunkPos(current.x + 1, current.z));
            pending.add(new ChunkPos(current.x - 1, current.z));
            pending.add(new ChunkPos(current.x, current.z + 1));
            pending.add(new ChunkPos(current.x, current.z - 1));
        }
        return Math.max(1, connected);
    }

    private static Holder<Biome> sampleBiome(
            BiomeSource biomeSource,
            RandomState randomState,
            ChunkPos chunk,
            int sampleY
    ) {
        return biomeSource.getNoiseBiome(
                QuartPos.fromBlock(chunk.getMiddleBlockX()),
                QuartPos.fromBlock(sampleY),
                QuartPos.fromBlock(chunk.getMiddleBlockZ()),
                randomState.sampler()
        );
    }

    private static Holder<Biome> sampleBiomeAtBlock(BiomeSource biomeSource, RandomState randomState, BlockPos pos) {
        return biomeSource.getNoiseBiome(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ()),
                randomState.sampler()
        );
    }

    public enum Failure {
        NONE,
        MISSING,
        AMBIGUOUS
    }

    public record Resolution(Optional<BiomeMineResourceProfile> profile, Failure failure) {
        public Resolution {
            Objects.requireNonNull(profile, "profile");
            Objects.requireNonNull(failure, "failure");
            if (profile.isPresent() != (failure == Failure.NONE)) {
                throw new IllegalArgumentException("Resolved biome profiles require exactly one profile and no failure");
            }
        }

        private static Resolution resolved(BiomeMineResourceProfile profile) {
            return new Resolution(Optional.of(profile), Failure.NONE);
        }

        private static Resolution missing() {
            return new Resolution(Optional.empty(), Failure.MISSING);
        }

        private static Resolution ambiguous() {
            return new Resolution(Optional.empty(), Failure.AMBIGUOUS);
        }
    }
}
