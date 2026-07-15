package com.oblixorprime.ioe.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class IoeOreNodeBiomeModifiers {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, IoeExpeditionWorldgenMod.MODID);

    private static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<OreReplacement>>
            ORE_REPLACEMENT = MODIFIERS.register(
                    "replace_normal_ores_with_nodes",
                    () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(OreReplacement::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(OreReplacement::features),
                            GenerationStep.Decoration.CODEC.listOf()
                                    .xmap(Set::copyOf, List::copyOf)
                                    .fieldOf("steps")
                                    .forGetter(OreReplacement::steps)
                    ).apply(instance, OreReplacement::new))
            );

    private IoeOreNodeBiomeModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        MODIFIERS.register(Objects.requireNonNull(modEventBus, "modEventBus"));
    }

    public record OreReplacement(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            Set<GenerationStep.Decoration> steps
    ) implements BiomeModifier {
        public OreReplacement {
            Objects.requireNonNull(biomes, "biomes");
            Objects.requireNonNull(features, "features");
            steps = Set.copyOf(Objects.requireNonNull(steps, "steps"));
        }

        @Override
        public void modify(
                Holder<Biome> biome,
                Phase phase,
                ModifiableBiomeInfo.BiomeInfo.Builder builder
        ) {
            if (phase != Phase.REMOVE || !biomes.contains(biome)) {
                return;
            }
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            int removedFeatures = 0;
            for (GenerationStep.Decoration step : steps) {
                List<Holder<PlacedFeature>> stepFeatures = generationSettings.getFeatures(step);
                int before = stepFeatures.size();
                stepFeatures.removeIf(this::isNormalOreFeature);
                removedFeatures += before - stepFeatures.size();
            }
            int totalRemovedFeatures = removedFeatures;
            biome.unwrapKey().ifPresent(key -> IoeWorldgenRuntimeDiagnostics.recordModifierApplication(
                    key.location(),
                    totalRemovedFeatures
            ));
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return ORE_REPLACEMENT.get();
        }

        private boolean isNormalOreFeature(Holder<PlacedFeature> placedFeature) {
            if (features.contains(placedFeature)) {
                return true;
            }
            if (placedFeature.unwrapKey()
                    .map(key -> GeOreNodeIntegration.autonomousWorldgenFeature(key.location())
                            || Ae2MeteoriteIntegration.forbiddenCertusOre(key.location()))
                    .orElse(false)) {
                return true;
            }
            Object configuration = placedFeature.value().feature().value().config();
            if (!(configuration instanceof OreConfiguration oreConfiguration)) {
                return false;
            }
            return oreConfiguration.targetStates.stream()
                    .anyMatch(target -> target.state.is(Tags.Blocks.ORES)
                            || Ae2MeteoriteIntegration.forbiddenCertusOre(
                            BuiltInRegistries.BLOCK.getKey(target.state.getBlock())
                    ));
        }
    }
}
