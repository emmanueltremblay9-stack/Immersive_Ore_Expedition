package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Registry-only bridge for optional industrial mine furniture.
 *
 * <p>IOE never compiles against the decor mod. Each placement keeps a vanilla
 * fallback so expedition sites remain valid when the optional mod is absent.</p>
 */
public final class MineLifeFurnitureIntegration {
    public static final String MOD_ID = "immersive_engineer_decor_controls_tool_reforged";

    private MineLifeFurnitureIntegration() {
    }

    static BlockState surveyDesk(Block fallback) {
        return resolve("steel_table", fallback);
    }

    static BlockState workbench(Block fallback) {
        return resolve("metal_crafting_table", fallback);
    }

    static BlockState storageCrate(Block fallback) {
        return resolve("labeled_crate", fallback);
    }

    static BlockState stool(Block fallback) {
        return resolve("treated_wood_stool", fallback);
    }

    static BlockState treatedTable(Block fallback) {
        return resolve("treated_wood_table", fallback);
    }

    static BlockState fluidBarrel(Block fallback) {
        return resolve("fluid_barrel", fallback);
    }

    static BlockState ceilingAlarmLamp(Block fallback) {
        BlockState state = resolve("industrial_alarm_lamp", fallback);
        if (state.hasProperty(DirectionalBlock.FACING)) {
            return state.setValue(DirectionalBlock.FACING, Direction.DOWN);
        }
        if (state.hasProperty(LanternBlock.HANGING)) {
            return state.setValue(LanternBlock.HANGING, true);
        }
        return state;
    }

    private static BlockState resolve(String blockPath, Block fallback) {
        Objects.requireNonNull(blockPath, "blockPath");
        Objects.requireNonNull(fallback, "fallback");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, blockPath);
        return BuiltInRegistries.BLOCK.getOptional(id)
                .map(Block::defaultBlockState)
                .orElseGet(fallback::defaultBlockState);
    }
}
