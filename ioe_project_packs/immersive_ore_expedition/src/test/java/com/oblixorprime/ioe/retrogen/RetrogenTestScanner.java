package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

record RetrogenTestScanner(Set<ResourceRef> loadedResources, Set<String> loadedMods) implements LoadedResourceScanner {
    RetrogenTestScanner(Set<ResourceRef> loadedResources) {
        this(loadedResources, Set.of());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return loadedMods.contains(modId);
    }

    @Override
    public boolean blockExists(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.BLOCK, id));
    }

    @Override
    public boolean fluidExists(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.FLUID, id));
    }

    @Override
    public boolean itemExists(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.ITEM, id));
    }

    @Override
    public boolean blockTagHasValues(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.BLOCK_TAG, id));
    }

    @Override
    public boolean fluidTagHasValues(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.FLUID_TAG, id));
    }

    @Override
    public boolean itemTagHasValues(ResourceLocation id) {
        return loadedResources.contains(new ResourceRef(ResourceType.ITEM_TAG, id));
    }
}
