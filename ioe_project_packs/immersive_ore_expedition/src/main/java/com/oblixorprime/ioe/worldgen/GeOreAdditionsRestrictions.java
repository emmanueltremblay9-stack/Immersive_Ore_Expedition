package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Progression guard for the separately installed GeOre: Additions mod.
 * Only the Budding Harvester and Geode Extractor families are blocked; unrelated add-on content remains usable.
 */
public final class GeOreAdditionsRestrictions {
    public static final String MOD_ID = "geore_additions";

    private GeOreAdditionsRestrictions() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(GeOreAdditionsRestrictions::onBreak);
        NeoForge.EVENT_BUS.addListener(GeOreAdditionsRestrictions::onPlace);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded(MOD_ID) || !isBuddingBlock(event.getState())) {
            return;
        }
        ResourceLocation toolId = BuiltInRegistries.ITEM.getKey(event.getPlayer().getMainHandItem().getItem());
        if (MOD_ID.equals(toolId.getNamespace()) && toolId.getPath().contains("budding_harvester")) {
            event.setCanceled(true);
        }
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return;
        }
        ResourceLocation placedId = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        if (MOD_ID.equals(placedId.getNamespace()) && placedId.getPath().contains("geode_extractor")) {
            event.setCanceled(true);
        }
    }

    private static boolean isBuddingBlock(BlockState state) {
        if (state.is(Tags.Blocks.BUDDING_BLOCKS)) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        return GeOreNodeIntegration.MOD_ID.equals(id.getNamespace()) && path.startsWith("budding_")
                || Ae2MeteoriteIntegration.MOD_ID.equals(id.getNamespace()) && path.contains("budding_quartz")
                || ExtendedAeGeodeIntegration.MOD_ID.equals(id.getNamespace()) && path.startsWith("entro_budding_");
    }
}
