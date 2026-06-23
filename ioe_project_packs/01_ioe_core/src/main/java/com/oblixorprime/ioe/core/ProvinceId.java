package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ProvinceId(ResourceLocation id) {
    public ProvinceId {
        Objects.requireNonNull(id, "id");
    }

    public static ProvinceId of(String namespace, String path) {
        return new ProvinceId(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
