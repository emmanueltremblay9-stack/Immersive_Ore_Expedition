package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ProvinceBiomeRule(
        ProvinceId provinceId,
        List<ResourceLocation> allowedBiomeIds,
        List<ResourceLocation> allowedBiomeTags,
        List<ResourceLocation> deniedBiomeIds,
        List<ResourceLocation> deniedBiomeTags,
        List<ResourceLocation> excludedBiomeIds,
        List<ResourceLocation> excludedBiomeTags
) {
    public ProvinceBiomeRule {
        Objects.requireNonNull(provinceId, "provinceId");
        allowedBiomeIds = List.copyOf(Objects.requireNonNull(allowedBiomeIds, "allowedBiomeIds"));
        allowedBiomeTags = List.copyOf(Objects.requireNonNull(allowedBiomeTags, "allowedBiomeTags"));
        deniedBiomeIds = List.copyOf(Objects.requireNonNull(deniedBiomeIds, "deniedBiomeIds"));
        deniedBiomeTags = List.copyOf(Objects.requireNonNull(deniedBiomeTags, "deniedBiomeTags"));
        excludedBiomeIds = List.copyOf(Objects.requireNonNull(excludedBiomeIds, "excludedBiomeIds"));
        excludedBiomeTags = List.copyOf(Objects.requireNonNull(excludedBiomeTags, "excludedBiomeTags"));
    }

    public boolean matches(ResourceLocation biomeId, Set<ResourceLocation> biomeTags) {
        return diagnose(biomeId, biomeTags).matched();
    }

    public ProvinceMatchDiagnostic diagnose(ResourceLocation biomeId, Set<ResourceLocation> biomeTags) {
        Objects.requireNonNull(biomeId, "biomeId");
        Set<ResourceLocation> tags = Set.copyOf(Objects.requireNonNull(biomeTags, "biomeTags"));

        if (excludedBiomeIds.contains(biomeId)) {
            return ProvinceMatchDiagnostic.rejected(provinceId, biomeId,
                    "Biome id is excluded by province rule: " + biomeId);
        }
        ResourceLocation excludedTag = firstMatchingTag(excludedBiomeTags, tags);
        if (excludedTag != null) {
            return ProvinceMatchDiagnostic.rejected(provinceId, biomeId,
                    "Biome tag is excluded by province rule: " + excludedTag);
        }
        if (deniedBiomeIds.contains(biomeId)) {
            return ProvinceMatchDiagnostic.rejected(provinceId, biomeId,
                    "Biome id is denied by province rule: " + biomeId);
        }
        ResourceLocation deniedTag = firstMatchingTag(deniedBiomeTags, tags);
        if (deniedTag != null) {
            return ProvinceMatchDiagnostic.rejected(provinceId, biomeId,
                    "Biome tag is denied by province rule: " + deniedTag);
        }
        if (allowedBiomeIds.isEmpty() && allowedBiomeTags.isEmpty()) {
            return ProvinceMatchDiagnostic.matched(provinceId, biomeId,
                    "Province rule has no biome allow list; biome accepted by default");
        }
        if (allowedBiomeIds.contains(biomeId)) {
            return ProvinceMatchDiagnostic.matched(provinceId, biomeId,
                    "Biome id matched province allow list: " + biomeId);
        }
        ResourceLocation allowedTag = firstMatchingTag(allowedBiomeTags, tags);
        if (allowedTag != null) {
            return ProvinceMatchDiagnostic.matched(provinceId, biomeId,
                    "Biome tag matched province allow list: " + allowedTag);
        }

        return ProvinceMatchDiagnostic.rejected(provinceId, biomeId,
                "Biome did not match province allow list: " + biomeId);
    }

    private static ResourceLocation firstMatchingTag(List<ResourceLocation> configuredTags, Set<ResourceLocation> actualTags) {
        for (ResourceLocation configuredTag : configuredTags) {
            if (actualTags.contains(configuredTag)) {
                return configuredTag;
            }
        }
        return null;
    }
}
