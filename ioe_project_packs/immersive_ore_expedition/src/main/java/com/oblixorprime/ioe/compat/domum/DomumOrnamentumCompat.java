package com.oblixorprime.ioe.compat.domum;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.Objects;

/** Guarded core-facing entry point; no Domum type occurs in this class's public signature. */
public final class DomumOrnamentumCompat {
    public static final String MOD_ID = "domum_ornamentum";

    private DomumOrnamentumCompat() {
    }

    public static boolean applyMaterialPayload(
            BlockEntity blockEntity,
            Map<ResourceLocation, ResourceLocation> materialBlocks
    ) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        Objects.requireNonNull(materialBlocks, "materialBlocks");
        if (materialBlocks.isEmpty() || !ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        try {
            return DomumOrnamentumAdapter.applyMaterialPayload(blockEntity, materialBlocks);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    public static boolean matchesMaterialPayload(
            BlockEntity blockEntity,
            Map<ResourceLocation, ResourceLocation> materialBlocks
    ) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        Objects.requireNonNull(materialBlocks, "materialBlocks");
        if (materialBlocks.isEmpty() || !ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        try {
            return DomumOrnamentumAdapter.matchesMaterialPayload(blockEntity, materialBlocks);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    public static boolean matchesMaterialPayload(
            ItemStack itemStack,
            Map<ResourceLocation, ResourceLocation> materialBlocks
    ) {
        Objects.requireNonNull(itemStack, "itemStack");
        Objects.requireNonNull(materialBlocks, "materialBlocks");
        if (itemStack.isEmpty() || materialBlocks.isEmpty() || !ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        try {
            return DomumOrnamentumAdapter.matchesMaterialPayload(itemStack, materialBlocks);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }
}
