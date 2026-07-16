package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Selects exactly one mine resource from the origin biome. Site shape never participates in resource selection.
 */
public record BiomeMineResourceProfile(
        ResourceLocation biomeId,
        ResourceKind resourceKind,
        String profileName,
        int sampledConnectedChunks
) {
    private static final int SURVEY_RADIUS_CHUNKS = 4;
    private static final List<ResourceDefinition> RESOURCE_DEFINITIONS = resourceDefinitions();

    public BiomeMineResourceProfile {
        Objects.requireNonNull(biomeId, "biomeId");
        Objects.requireNonNull(resourceKind, "resourceKind");
        Objects.requireNonNull(profileName, "profileName");
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

        List<ResourceDefinition> matchingDefinitions = RESOURCE_DEFINITIONS.stream()
                .filter(candidate -> originBiome.is(candidate.biomeTag()))
                .toList();
        if (matchingDefinitions.isEmpty()) {
            return Resolution.missing();
        }
        if (matchingDefinitions.size() > 1) {
            IoeExpeditionWorldgenMod.LOGGER.warn(
                    "Rejected ambiguous IOE mine resource biome={} origin={} matches={}",
                    originBiomeKey.location(),
                    origin,
                    matchingDefinitions.stream().map(ResourceDefinition::profileName).toList()
            );
            return Resolution.ambiguous();
        }

        ResourceDefinition definition = matchingDefinitions.getFirst();
        int connectedChunks = countConnectedChunks(
                biomeSource,
                randomState,
                originChunk,
                origin.getY(),
                originBiomeKey
        );
        return Resolution.resolved(new BiomeMineResourceProfile(
                originBiomeKey.location(),
                definition.resourceKind(),
                definition.profileName(),
                connectedChunks
        ));
    }

    public int oreBudget(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        if (resourceKind != ResourceKind.GEORE || !quality.isProductive()) {
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
        if (resourceKind != ResourceKind.GEORE || !quality.isProductive()) {
            return 0;
        }
        int base = switch (quality) {
            case DRY -> 0;
            case POOR -> 1;
            case NORMAL -> 2;
            case RICH -> 3;
            case MOTHERLODE -> 4;
        };
        return base + Math.min(2, sampledConnectedChunks / 24);
    }

    public int specialBuddingCount(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        if (!quality.isProductive()) {
            return 0;
        }
        return switch (resourceKind) {
            case GEORE -> 0;
            case AE2_CERTUS -> Math.min(4,
                    (quality == SiteQuality.POOR || quality == SiteQuality.NORMAL ? 1 : 2)
                            + (sampledConnectedChunks >= 33 ? 1 : 0)
                            + (sampledConnectedChunks >= 65 ? 1 : 0));
            case EXTENDEDAE_FLUIX -> Math.min(3,
                    (quality == SiteQuality.MOTHERLODE ? 2 : 1)
                            + (sampledConnectedChunks >= 49 ? 1 : 0));
        };
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

    private static List<ResourceDefinition> resourceDefinitions() {
        return List.of(
                definition("certus", ResourceKind.AE2_CERTUS),
                definition("entroized_fluix", ResourceKind.EXTENDEDAE_FLUIX),
                definition("uranium", ResourceKind.GEORE),
                definition("nickel", ResourceKind.GEORE),
                definition("silver", ResourceKind.GEORE),
                definition("lead", ResourceKind.GEORE),
                definition("aluminum", ResourceKind.GEORE),
                definition("emerald", ResourceKind.GEORE),
                definition("diamond", ResourceKind.GEORE),
                definition("gold", ResourceKind.GEORE),
                definition("lapis", ResourceKind.GEORE),
                definition("redstone", ResourceKind.GEORE),
                definition("copper", ResourceKind.GEORE),
                definition("iron", ResourceKind.GEORE),
                definition("coal", ResourceKind.GEORE)
        );
    }

    private static ResourceDefinition definition(String profileName, ResourceKind resourceKind) {
        return new ResourceDefinition(
                profileName,
                resourceKind,
                TagKey.create(
                        Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(
                                IoeExpeditionWorldgenMod.MODID,
                                "ore_profile/" + profileName
                        )
                )
        );
    }

    public enum ResourceKind {
        GEORE,
        AE2_CERTUS,
        EXTENDEDAE_FLUIX
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

    private record ResourceDefinition(
            String profileName,
            ResourceKind resourceKind,
            TagKey<Biome> biomeTag
    ) {
        private ResourceDefinition {
            Objects.requireNonNull(profileName, "profileName");
            Objects.requireNonNull(resourceKind, "resourceKind");
            Objects.requireNonNull(biomeTag, "biomeTag");
        }
    }
}
