package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class WorldgenBiomeProvinceBindingRules {
    private final ProvinceBindingResolver bindingResolver;
    private final int malformedBindingCount;
    private final boolean invalidDefaultProvince;

    private WorldgenBiomeProvinceBindingRules(
            ProvinceBindingResolver bindingResolver,
            int malformedBindingCount,
            boolean invalidDefaultProvince
    ) {
        this.bindingResolver = Objects.requireNonNull(bindingResolver, "bindingResolver");
        this.malformedBindingCount = malformedBindingCount;
        this.invalidDefaultProvince = invalidDefaultProvince;
    }

    public static WorldgenBiomeProvinceBindingRules fromConfig() {
        return parse(
                IoeWorldgenConfig.defaultProvince(),
                IoeWorldgenConfig.biomeProvinceBindings(),
                IoeWorldgenConfig.allowLegacyProvinceNamespaces()
        );
    }

    public static WorldgenBiomeProvinceBindingRules defaults() {
        return parse("immersive_ore_expedition:default", List.of(), false);
    }

    static WorldgenBiomeProvinceBindingRules parse(
            String defaultProvinceSpec,
            Collection<String> bindingSpecs,
            boolean allowLegacyNamespaces
    ) {
        Collection<String> specs = bindingSpecs == null ? List.of() : bindingSpecs;
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                defaultProvinceSpec,
                specs,
                allowLegacyNamespaces
        );
        int requestedBindingCount = countNonBlank(specs);
        return new WorldgenBiomeProvinceBindingRules(
                resolver,
                Math.max(0, requestedBindingCount - resolver.bindingCount()),
                !isValidProvince(defaultProvinceSpec, allowLegacyNamespaces)
        );
    }

    WorldgenBiomeProvinceContext resolve(ResourceLocation biomeId) {
        if (biomeId == null) {
            return WorldgenBiomeProvinceContext.noOp(
                    null,
                    WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_INVALID_INPUT,
                    WorldgenBiomeProvinceContext.SkipReason.NULL_BIOME,
                    false,
                    false
            );
        }
        if (invalidDefaultProvince) {
            return WorldgenBiomeProvinceContext.noOp(
                    biomeId,
                    WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_INVALID_INPUT,
                    WorldgenBiomeProvinceContext.SkipReason.INVALID_PROVINCE_ID,
                    false,
                    false
            );
        }
        if (malformedBindingCount > 0 && bindingResolver.bindingCount() == 0) {
            return WorldgenBiomeProvinceContext.noOp(
                    biomeId,
                    WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_MALFORMED_BINDING,
                    WorldgenBiomeProvinceContext.SkipReason.MALFORMED_BINDING,
                    false,
                    false
            );
        }

        ProvinceId province = bindingResolver.resolve(biomeId);
        boolean fallbackUsed = province.equals(bindingResolver.defaultProvince());
        return WorldgenBiomeProvinceContext.resolved(
                biomeId,
                province.id(),
                fallbackUsed
        );
    }

    int malformedBindingCount() {
        return malformedBindingCount;
    }

    ProvinceBindingResolver bindingResolver() {
        return bindingResolver;
    }

    private static int countNonBlank(Collection<String> specs) {
        int count = 0;
        for (String spec : specs) {
            if (spec != null && !spec.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private static boolean isValidProvince(String provinceSpec, boolean allowLegacyNamespaces) {
        if (provinceSpec == null || provinceSpec.isBlank()) {
            return false;
        }
        try {
            ProvinceId.parse(provinceSpec, allowLegacyNamespaces);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
