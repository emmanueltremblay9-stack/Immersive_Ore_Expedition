package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ResourceRef(ResourceType type, ResourceLocation id) {
    public ResourceRef {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }

    public static ResourceRef block(String namespace, String path) {
        return new ResourceRef(ResourceType.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceRef fluid(String namespace, String path) {
        return new ResourceRef(ResourceType.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceRef item(String namespace, String path) {
        return new ResourceRef(ResourceType.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceRef blockTag(String namespace, String path) {
        return new ResourceRef(ResourceType.BLOCK_TAG, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceRef fluidTag(String namespace, String path) {
        return new ResourceRef(ResourceType.FLUID_TAG, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceRef itemTag(String namespace, String path) {
        return new ResourceRef(ResourceType.ITEM_TAG, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
