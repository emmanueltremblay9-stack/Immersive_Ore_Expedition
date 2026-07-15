package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.List;

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
        NeoForge.EVENT_BUS.addListener(GeOreAdditionsRestrictions::onChunkLoad);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded(MOD_ID) || !isBuddingBlock(event.getState())) {
            return;
        }
        ResourceLocation toolId = BuiltInRegistries.ITEM.getKey(event.getPlayer().getMainHandItem().getItem());
        if (isHarvester(toolId)) {
            event.setCanceled(true);
        }
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return;
        }
        ResourceLocation placedId = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        if (isExtractor(placedId)) {
            event.setCanceled(true);
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!ModList.get().isLoaded(MOD_ID)
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        List<BlockPos> extractors = extractorPositions(chunk);
        if (extractors.isEmpty()) {
            return;
        }
        level.getServer().execute(() -> {
            if (level.getChunkSource().getChunkNow(chunk.getPos().x, chunk.getPos().z) != chunk) {
                return;
            }
            int removed = 0;
            for (BlockPos pos : extractors) {
                if (isExtractor(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()))
                        && level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                    removed++;
                }
            }
            if (removed > 0) {
                IoeExpeditionWorldgenMod.LOGGER.warn(
                        "Removed {} forbidden GeOre Additions extractor block(s) from loaded chunk {}",
                        removed,
                        chunk.getPos()
                );
            }
        });
    }

    private static List<BlockPos> extractorPositions(LevelChunk chunk) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir() || !section.maybeHas(GeOreAdditionsRestrictions::isExtractorState)) {
                continue;
            }
            int minY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        if (isExtractorState(section.getBlockState(localX, localY, localZ))) {
                            positions.add(new BlockPos(
                                    chunk.getPos().getMinBlockX() + localX,
                                    minY + localY,
                                    chunk.getPos().getMinBlockZ() + localZ
                            ));
                        }
                    }
                }
            }
        }
        return List.copyOf(positions);
    }

    private static boolean isExtractorState(BlockState state) {
        return isExtractor(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean isExtractor(ResourceLocation id) {
        return MOD_ID.equals(id.getNamespace()) && id.getPath().contains("geode_extractor");
    }

    private static boolean isHarvester(ResourceLocation id) {
        return MOD_ID.equals(id.getNamespace()) && id.getPath().contains("budding_harvester");
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
