package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class OreLoadChamberReplacementRules {
    private static final Set<ResourceLocation> PROTECTED_BLOCK_IDS = Set.of(
            id("bedrock"),
            id("barrier"),
            id("command_block"),
            id("chain_command_block"),
            id("repeating_command_block"),
            id("structure_block"),
            id("jigsaw"),
            id("end_portal_frame"),
            id("end_portal"),
            id("end_gateway"),
            id("nether_portal")
    );

    private OreLoadChamberReplacementRules() {
    }

    public static boolean canReplace(BlockState state) {
        if (state == null) {
            return false;
        }
        return canReplace(
                BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                state.isAir(),
                !state.getFluidState().isEmpty(),
                state.hasBlockEntity()
        );
    }

    static boolean canReplace(
            ResourceLocation blockId,
            boolean air,
            boolean containsFluid,
            boolean hasBlockEntity
    ) {
        if (blockId == null || air || containsFluid || hasBlockEntity) {
            return false;
        }
        return !PROTECTED_BLOCK_IDS.contains(blockId);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }
}
