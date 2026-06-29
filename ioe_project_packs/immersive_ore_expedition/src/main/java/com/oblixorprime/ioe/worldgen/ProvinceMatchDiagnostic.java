package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record ProvinceMatchDiagnostic(
        ProvinceId provinceId,
        ResourceLocation biomeId,
        boolean matched,
        List<String> reasons
) {
    public ProvinceMatchDiagnostic {
        Objects.requireNonNull(provinceId, "provinceId");
        Objects.requireNonNull(biomeId, "biomeId");
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
        if (reasons.isEmpty() || reasons.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("diagnostic reasons must not be blank");
        }
    }

    public static ProvinceMatchDiagnostic matched(ProvinceId provinceId, ResourceLocation biomeId, String reason) {
        return new ProvinceMatchDiagnostic(provinceId, biomeId, true, List.of(reason));
    }

    public static ProvinceMatchDiagnostic rejected(ProvinceId provinceId, ResourceLocation biomeId, String reason) {
        return new ProvinceMatchDiagnostic(provinceId, biomeId, false, List.of(reason));
    }

    public String summary() {
        return "province=" + provinceId
                + ", biome=" + biomeId
                + ", matched=" + matched
                + ", reasons=" + String.join("; ", reasons);
    }
}
