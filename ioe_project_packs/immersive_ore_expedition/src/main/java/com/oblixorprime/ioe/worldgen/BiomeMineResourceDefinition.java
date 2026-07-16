package com.oblixorprime.ioe.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Datapack-owned definition of one biome-selected mine resource and its connected-biome scaling.
 */
public record BiomeMineResourceDefinition(
        ResourceLocation biomeTagId,
        BiomeMineResourceProfile.ResourceKind resourceKind,
        String resourceName,
        int surveyRadiusChunks,
        Map<String, QuantitySet> qualityCounts
) {
    public static final ResourceKey<Registry<BiomeMineResourceDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IoeExpeditionWorldgenMod.MODID,
                    "mine_resource_profile"
            ));

    private static final Set<String> REQUIRED_QUALITY_NAMES = Arrays.stream(SiteQuality.values())
            .map(BiomeMineResourceDefinition::qualityName)
            .collect(Collectors.toUnmodifiableSet());

    public static final Codec<BiomeMineResourceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("biome_tag").forGetter(BiomeMineResourceDefinition::biomeTagId),
            BiomeMineResourceProfile.ResourceKind.CODEC.fieldOf("resource_kind")
                    .forGetter(BiomeMineResourceDefinition::resourceKind),
            Codec.STRING.fieldOf("resource_name").forGetter(BiomeMineResourceDefinition::resourceName),
            Codec.intRange(1, 16).fieldOf("survey_radius_chunks")
                    .forGetter(BiomeMineResourceDefinition::surveyRadiusChunks),
            Codec.unboundedMap(Codec.STRING, QuantitySet.CODEC).fieldOf("quality_counts")
                    .forGetter(BiomeMineResourceDefinition::qualityCounts)
    ).apply(instance, BiomeMineResourceDefinition::new));

    public BiomeMineResourceDefinition {
        Objects.requireNonNull(biomeTagId, "biomeTagId");
        Objects.requireNonNull(resourceKind, "resourceKind");
        resourceName = requireNonBlank(resourceName, "resourceName");
        Objects.requireNonNull(qualityCounts, "qualityCounts");
        qualityCounts = Map.copyOf(qualityCounts);
        if (!qualityCounts.keySet().equals(REQUIRED_QUALITY_NAMES)) {
            throw new IllegalArgumentException(
                    "Mine resource profiles require exactly these quality keys: " + REQUIRED_QUALITY_NAMES
            );
        }
        validateStructuralCounts(resourceKind, qualityCounts);
    }

    static void registerDatapackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY_KEY, CODEC);
    }

    public TagKey<Biome> biomeTag() {
        return TagKey.create(Registries.BIOME, biomeTagId);
    }

    public QuantitySet counts(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        return qualityCounts.get(qualityName(quality));
    }

    private static void validateStructuralCounts(
            BiomeMineResourceProfile.ResourceKind resourceKind,
            Map<String, QuantitySet> qualityCounts
    ) {
        QuantitySet dry = qualityCounts.get(qualityName(SiteQuality.DRY));
        if (!dry.allZero()) {
            throw new IllegalArgumentException("Dry mine resource counts must all be zero");
        }
        for (SiteQuality quality : SiteQuality.values()) {
            if (!quality.isProductive()) {
                continue;
            }
            QuantitySet counts = qualityCounts.get(qualityName(quality));
            if (resourceKind == BiomeMineResourceProfile.ResourceKind.GEORE) {
                if (counts.oreBudget().base() <= 0
                        || counts.nodeCount().base() <= 0
                        || !counts.specialBuddingCount().alwaysZero()) {
                    throw new IllegalArgumentException(
                            "Productive GeOre profiles require positive ore/node bases and zero special budding"
                    );
                }
            } else if (!counts.oreBudget().alwaysZero()
                    || !counts.nodeCount().alwaysZero()
                    || counts.specialBuddingCount().base() <= 0) {
                throw new IllegalArgumentException(
                        "Productive crystal profiles require zero ore/node counts and positive budding bases"
                );
            }
        }
    }

    private static String qualityName(SiteQuality quality) {
        return quality.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    public record QuantitySet(
            CountRule oreBudget,
            CountRule nodeCount,
            CountRule specialBuddingCount
    ) {
        public static final Codec<QuantitySet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CountRule.CODEC.fieldOf("ore_budget").forGetter(QuantitySet::oreBudget),
                CountRule.CODEC.fieldOf("node_count").forGetter(QuantitySet::nodeCount),
                CountRule.CODEC.fieldOf("special_budding_count").forGetter(QuantitySet::specialBuddingCount)
        ).apply(instance, QuantitySet::new));

        public QuantitySet {
            Objects.requireNonNull(oreBudget, "oreBudget");
            Objects.requireNonNull(nodeCount, "nodeCount");
            Objects.requireNonNull(specialBuddingCount, "specialBuddingCount");
        }

        boolean allZero() {
            return oreBudget.alwaysZero() && nodeCount.alwaysZero() && specialBuddingCount.alwaysZero();
        }
    }

    public record CountRule(int base, Optional<BonusRule> bonus) {
        public static final Codec<CountRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 4096).fieldOf("base").forGetter(CountRule::base),
                BonusRule.CODEC.optionalFieldOf("bonus").forGetter(CountRule::bonus)
        ).apply(instance, CountRule::new));

        public CountRule {
            Objects.requireNonNull(bonus, "bonus");
        }

        public int valueAt(int connectedBiomeChunks) {
            if (connectedBiomeChunks <= 0) {
                throw new IllegalArgumentException("Connected biome chunk count must be positive");
            }
            return base + bonus.map(rule -> rule.valueAt(connectedBiomeChunks)).orElse(0);
        }

        boolean alwaysZero() {
            return base == 0 && bonus.isEmpty();
        }
    }

    public record BonusRule(int firstChunk, int chunksPerBonus, int maxBonus) {
        public static final Codec<BonusRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 4096).fieldOf("first_chunk").forGetter(BonusRule::firstChunk),
                Codec.intRange(1, 4096).fieldOf("chunks_per_bonus").forGetter(BonusRule::chunksPerBonus),
                Codec.intRange(1, 4096).optionalFieldOf("max_bonus", 4096).forGetter(BonusRule::maxBonus)
        ).apply(instance, BonusRule::new));

        int valueAt(int connectedBiomeChunks) {
            if (connectedBiomeChunks < firstChunk) {
                return 0;
            }
            return Math.min(maxBonus, 1 + (connectedBiomeChunks - firstChunk) / chunksPerBonus);
        }
    }
}
