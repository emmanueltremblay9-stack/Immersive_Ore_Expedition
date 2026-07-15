package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BiomeOreNodeProfile(
        ResourceLocation biomeId,
        ResourceRef resource,
        Block oreBlock,
        ResourceRef buddingResource,
        Block buddingBlock,
        int sampledConnectedChunks
) {
    private static final int SURVEY_RADIUS_CHUNKS = 4;
    private static final List<OreDefinition> ORE_DEFINITIONS = oreDefinitions();

    public BiomeOreNodeProfile {
        Objects.requireNonNull(biomeId, "biomeId");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(oreBlock, "oreBlock");
        Objects.requireNonNull(buddingResource, "buddingResource");
        Objects.requireNonNull(buddingBlock, "buddingBlock");
        if (sampledConnectedChunks <= 0) {
            throw new IllegalArgumentException("Connected biome chunk count must be positive");
        }
    }

    public static Optional<BiomeOreNodeProfile> resolve(WorldGenLevel level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");

        ServerChunkCache chunkSource = level.getLevel().getChunkSource();
        BiomeSource biomeSource = chunkSource.getGenerator().getBiomeSource();
        RandomState randomState = chunkSource.randomState();
        ChunkPos originChunk = new ChunkPos(origin);
        Holder<Biome> originBiome = sampleBiomeAtBlock(biomeSource, randomState, origin);
        ResourceKey<Biome> originBiomeKey = originBiome.unwrapKey().orElse(null);
        if (originBiomeKey == null) {
            return Optional.empty();
        }

        OreDefinition definition = ORE_DEFINITIONS.stream()
                .filter(candidate -> originBiome.is(candidate.biomeTag()))
                .findFirst()
                .orElse(null);
        if (definition == null) {
            return Optional.empty();
        }
        GeOreNodeIntegration.NodeMaterial material = GeOreNodeIntegration.resolve(definition.materialName())
                .orElse(null);
        if (material == null) {
            return Optional.empty();
        }
        int connectedChunks = countConnectedChunks(
                biomeSource,
                randomState,
                originChunk,
                origin.getY(),
                originBiomeKey
        );
        return Optional.of(new BiomeOreNodeProfile(
                originBiomeKey.location(),
                material.nodeResource(),
                material.nodeBlock(),
                material.buddingResource(),
                material.buddingBlock(),
                connectedChunks
        ));
    }

    public int oreBudget(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        if (!quality.isProductive()) {
            return 0;
        }
        int base = switch (quality) {
            case DRY -> 0;
            case POOR -> 4;
            case NORMAL -> 8;
            case RICH -> 14;
            case MOTHERLODE -> 24;
        };
        int chunksPerBonus = switch (quality) {
            case DRY -> Integer.MAX_VALUE;
            case POOR -> 6;
            case NORMAL -> 4;
            case RICH -> 3;
            case MOTHERLODE -> 2;
        };
        return base + Math.max(0, sampledConnectedChunks - 1) / chunksPerBonus;
    }

    public int nodeCount(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        int base = switch (quality) {
            case DRY -> 0;
            case POOR -> 1;
            case NORMAL -> 2;
            case RICH -> 3;
            case MOTHERLODE -> 4;
        };
        return quality.isProductive() ? base + Math.min(2, sampledConnectedChunks / 24) : 0;
    }

    private static int countConnectedChunks(
            BiomeSource biomeSource,
            RandomState randomState,
            ChunkPos origin,
            int sampleY,
            ResourceKey<Biome> targetBiome
    ) {
        ArrayDeque<ChunkPos> pending = new ArrayDeque<>();
        LinkedHashSet<Long> visited = new LinkedHashSet<>();
        pending.add(origin);
        int connected = 0;
        while (!pending.isEmpty()) {
            ChunkPos current = pending.removeFirst();
            if (Math.abs(current.x - origin.x) > SURVEY_RADIUS_CHUNKS
                    || Math.abs(current.z - origin.z) > SURVEY_RADIUS_CHUNKS
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

    private static Holder<Biome> sampleBiomeAtBlock(
            BiomeSource biomeSource,
            RandomState randomState,
            BlockPos pos
    ) {
        return biomeSource.getNoiseBiome(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ()),
                randomState.sampler()
        );
    }

    private static List<OreDefinition> oreDefinitions() {
        return List.of(
                definition("uranium"),
                definition("nickel"),
                definition("silver"),
                definition("lead"),
                definition("aluminum"),
                definition("emerald"),
                definition("diamond"),
                definition("gold"),
                definition("lapis"),
                definition("redstone"),
                definition("copper"),
                definition("iron"),
                definition("coal")
        );
    }

    private static OreDefinition definition(String materialName) {
        return new OreDefinition(
                materialName,
                TagKey.create(
                        Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(
                                IoeExpeditionWorldgenMod.MODID,
                                "ore_profile/" + materialName
                        )
                )
        );
    }

    private record OreDefinition(String materialName, TagKey<Biome> biomeTag) {
        private OreDefinition {
            Objects.requireNonNull(materialName, "materialName");
            Objects.requireNonNull(biomeTag, "biomeTag");
        }
    }
}
