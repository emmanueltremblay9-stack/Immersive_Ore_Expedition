package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record ProvinceRule(
        ResourceLocation id,
        List<ResourceLocation> biomeIds,
        List<ResourceLocation> resourceTags,
        List<String> anchorStructures
) {}
