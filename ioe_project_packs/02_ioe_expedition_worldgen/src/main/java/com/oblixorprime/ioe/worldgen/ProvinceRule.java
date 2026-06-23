package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record ProvinceRule(
        ProvinceId id,
        List<ResourceLocation> biomeTags,
        List<ResourceRef> resources,
        List<ResourceLocation> anchorStructures
) {
    public ProvinceRule {
        Objects.requireNonNull(id, "id");
        biomeTags = List.copyOf(Objects.requireNonNull(biomeTags, "biomeTags"));
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
        anchorStructures = List.copyOf(Objects.requireNonNull(anchorStructures, "anchorStructures"));
    }
}
