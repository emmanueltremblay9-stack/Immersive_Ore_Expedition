package com.oblixorprime.ioe.compat.domum;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/** Loaded only after the guarded compat entry point has confirmed that Domum Ornamentum is present. */
final class DomumOrnamentumAdapter {
    private DomumOrnamentumAdapter() {
    }

    static boolean applyMaterialPayload(
            BlockEntity blockEntity,
            Map<ResourceLocation, ResourceLocation> materialBlockIds
    ) {
        if (!(blockEntity instanceof IMateriallyTexturedBlockEntity materiallyTextured)) {
            return false;
        }
        MaterialTextureData textureData = resolveTextureData(materialBlockIds);
        if (textureData == null) {
            return false;
        }
        materiallyTextured.updateTextureDataWith(textureData);
        blockEntity.setChanged();
        return materiallyTextured.getTextureData().getTexturedComponents().equals(textureData.getTexturedComponents());
    }

    static boolean matchesMaterialPayload(
            BlockEntity blockEntity,
            Map<ResourceLocation, ResourceLocation> materialBlockIds
    ) {
        if (!(blockEntity instanceof IMateriallyTexturedBlockEntity materiallyTextured)) {
            return false;
        }
        MaterialTextureData expected = resolveTextureData(materialBlockIds);
        return expected != null
                && materiallyTextured.getTextureData().getTexturedComponents()
                .equals(expected.getTexturedComponents());
    }

    static boolean matchesMaterialPayload(
            ItemStack itemStack,
            Map<ResourceLocation, ResourceLocation> materialBlockIds
    ) {
        MaterialTextureData expected = resolveTextureData(materialBlockIds);
        return expected != null
                && MaterialTextureData.readFromItemStack(itemStack).getTexturedComponents()
                .equals(expected.getTexturedComponents());
    }

    private static MaterialTextureData resolveTextureData(
            Map<ResourceLocation, ResourceLocation> materialBlockIds
    ) {
        LinkedHashMap<ResourceLocation, Block> resolved = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, ResourceLocation> material : materialBlockIds.entrySet()) {
            Block block = BuiltInRegistries.BLOCK.getOptional(material.getValue()).orElse(null);
            if (block == null) {
                return null;
            }
            resolved.put(material.getKey(), block);
        }
        return new MaterialTextureData(Map.copyOf(resolved));
    }
}
