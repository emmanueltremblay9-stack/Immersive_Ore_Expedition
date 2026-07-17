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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Datapack-owned definition of one biome-selected IE mineral mix.
 *
 * <p>The mineral composition is invariant across all site qualities. Mother, Major, Minor and Direct
 * only change the radius and remaining Excavator yield.</p>
 */
public record BiomeMineResourceDefinition(
        ResourceLocation biomeTagId,
        ResourceLocation mineralMixId,
        String resourceName,
        int surveyRadiusChunks,
        Map<String, DepositTier> depositTiers
) {
    public static final ResourceKey<Registry<BiomeMineResourceDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IoeExpeditionWorldgenMod.MODID,
                    "mine_resource_profile"
            ));
    private static final Set<String> REQUIRED_TIER_NAMES = Set.of("mother", "major", "minor", "direct");

    public static final Codec<BiomeMineResourceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("biome_tag").forGetter(BiomeMineResourceDefinition::biomeTagId),
            ResourceLocation.CODEC.fieldOf("mineral_mix").forGetter(BiomeMineResourceDefinition::mineralMixId),
            Codec.STRING.fieldOf("resource_name").forGetter(BiomeMineResourceDefinition::resourceName),
            Codec.intRange(1, 16).fieldOf("survey_radius_chunks")
                    .forGetter(BiomeMineResourceDefinition::surveyRadiusChunks),
            Codec.unboundedMap(Codec.STRING, DepositTier.CODEC).fieldOf("deposit_tiers")
                    .forGetter(BiomeMineResourceDefinition::depositTiers)
    ).apply(instance, BiomeMineResourceDefinition::new));

    public BiomeMineResourceDefinition {
        Objects.requireNonNull(biomeTagId, "biomeTagId");
        Objects.requireNonNull(mineralMixId, "mineralMixId");
        resourceName = requireNonBlank(resourceName, "resourceName");
        Objects.requireNonNull(depositTiers, "depositTiers");
        depositTiers = Map.copyOf(depositTiers);
        if (!depositTiers.keySet().equals(REQUIRED_TIER_NAMES)) {
            throw new IllegalArgumentException(
                    "Mine resource profiles require exactly these deposit tiers: " + REQUIRED_TIER_NAMES
            );
        }
        int previousRadius = Integer.MAX_VALUE;
        int previousCapacity = Integer.MAX_VALUE;
        for (String tierName : new String[]{"mother", "major", "minor", "direct"}) {
            DepositTier tier = depositTiers.get(tierName);
            if (tier.radiusBlocks() > previousRadius || tier.capacity() > previousCapacity) {
                throw new IllegalArgumentException("Deposit radius and capacity must not increase while downgrading");
            }
            previousRadius = tier.radiusBlocks();
            previousCapacity = tier.capacity();
        }
    }

    static void registerDatapackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY_KEY, CODEC);
    }

    public TagKey<Biome> biomeTag() {
        return TagKey.create(Registries.BIOME, biomeTagId);
    }

    public Optional<DepositTier> tier(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        String tierName = switch (quality) {
            case MOTHERLODE -> "mother";
            case RICH -> "major";
            case NORMAL -> "minor";
            case POOR -> "direct";
            case DRY -> null;
        };
        return tierName == null ? Optional.empty() : Optional.of(depositTiers.get(tierName));
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    public record DepositTier(int radiusBlocks, int capacity) {
        public static final Codec<DepositTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 128).fieldOf("radius_blocks").forGetter(DepositTier::radiusBlocks),
                Codec.intRange(1, 1_000_000).fieldOf("capacity").forGetter(DepositTier::capacity)
        ).apply(instance, DepositTier::new));
    }
}
