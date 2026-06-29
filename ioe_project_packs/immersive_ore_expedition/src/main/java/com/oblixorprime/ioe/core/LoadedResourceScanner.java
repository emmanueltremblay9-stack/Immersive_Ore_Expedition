package com.oblixorprime.ioe.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.fml.ModList;

import java.util.Objects;

public interface LoadedResourceScanner {
    LoadedResourceScanner RUNTIME = new RuntimeLoadedResourceScanner();

    boolean isModLoaded(String modId);

    boolean blockExists(ResourceLocation id);

    boolean fluidExists(ResourceLocation id);

    boolean itemExists(ResourceLocation id);

    boolean blockTagHasValues(ResourceLocation id);

    boolean fluidTagHasValues(ResourceLocation id);

    boolean itemTagHasValues(ResourceLocation id);

    default boolean isPresent(ResourceRef resourceRef) {
        Objects.requireNonNull(resourceRef, "resourceRef");
        return switch (resourceRef.type()) {
            case BLOCK -> blockExists(resourceRef.id());
            case FLUID -> fluidExists(resourceRef.id());
            case ITEM -> itemExists(resourceRef.id());
            case BLOCK_TAG -> blockTagHasValues(resourceRef.id());
            case FLUID_TAG -> fluidTagHasValues(resourceRef.id());
            case ITEM_TAG -> itemTagHasValues(resourceRef.id());
            case MOD -> isModLoaded(resourceRef.id().getPath());
        };
    }

    static LoadedResourceScanner runtime() {
        return RUNTIME;
    }

    final class RuntimeLoadedResourceScanner implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return BuiltInRegistries.BLOCK.containsKey(id);
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return BuiltInRegistries.FLUID.containsKey(id);
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return BuiltInRegistries.ITEM.containsKey(id);
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return tagHasValues(BuiltInRegistries.BLOCK, TagKey.create(Registries.BLOCK, id));
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return tagHasValues(BuiltInRegistries.FLUID, TagKey.create(Registries.FLUID, id));
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return tagHasValues(BuiltInRegistries.ITEM, TagKey.create(Registries.ITEM, id));
        }

        private static <T> boolean tagHasValues(Registry<T> registry, TagKey<T> tagKey) {
            return registry.getTag(tagKey).map(tag -> tag.iterator().hasNext()).orElse(false);
        }
    }
}
