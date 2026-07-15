package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bounded, admin-triggered recovery for locator data in already generated chunks.
 * It never loads chunks, changes blocks, or treats ordinary ore blocks as expedition sites.
 */
public final class ExpeditionLocatorReindexer {
    public static final int MAX_RADIUS_BLOCKS = 256;
    private static final int SITE_DEDUPLICATION_RADIUS_BLOCKS = 48;
    private static final long SITE_DEDUPLICATION_RADIUS_SQUARED =
            (long) SITE_DEDUPLICATION_RADIUS_BLOCKS * SITE_DEDUPLICATION_RADIUS_BLOCKS;
    private static final int SIGNATURE_HORIZONTAL_RADIUS = 12;
    private static final int SIGNATURE_VERTICAL_RADIUS = 8;
    private static final String SOURCE = "bounded_admin_reindex_mine_signature";

    private ExpeditionLocatorReindexer() {
    }

    public static Result scanLoadedChunks(ServerLevel level, BlockPos origin, int radiusBlocks) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        if (radiusBlocks < 0 || radiusBlocks > MAX_RADIUS_BLOCKS) {
            throw new IllegalArgumentException("radiusBlocks must be between 0 and " + MAX_RADIUS_BLOCKS);
        }

        ExpeditionLocatorIndex locatorIndex = ExpeditionLocatorService.index(level);
        List<ExpeditionSite> existingSites = locatorIndex.sites().stream()
                .filter(site -> site.dimension().equals(level.dimension()))
                .toList();
        List<BlockPos> recordedPositions = new ArrayList<>();
        List<BlockPos> rejectedPositions = new ArrayList<>();
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        int minChunkX = (origin.getX() - radiusBlocks) >> 4;
        int maxChunkX = (origin.getX() + radiusBlocks) >> 4;
        int minChunkZ = (origin.getZ() - radiusBlocks) >> 4;
        int maxChunkZ = (origin.getZ() + radiusBlocks) >> 4;
        int loadedChunks = 0;
        int growthBlocks = 0;
        int skippedExisting = 0;
        int skippedWithoutMineSignature = 0;

        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                loadedChunks++;
                LevelChunkSection[] sections = chunk.getSections();
                ChunkPos chunkPos = chunk.getPos();
                for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                    LevelChunkSection section = sections[sectionIndex];
                    if (section.hasOnlyAir() || !section.maybeHas(ExpeditionLocatorReindexer::isGrowthSource)) {
                        continue;
                    }
                    int minY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
                    for (int localY = 0; localY < 16; localY++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            for (int localX = 0; localX < 16; localX++) {
                                BlockState state = section.getBlockState(localX, localY, localZ);
                                if (!isGrowthSource(state)) {
                                    continue;
                                }
                                BlockPos pos = new BlockPos(
                                        chunkPos.getMinBlockX() + localX,
                                        minY + localY,
                                        chunkPos.getMinBlockZ() + localZ
                                );
                                if (horizontalDistanceSquared(origin, pos) > radiusSquared) {
                                    continue;
                                }
                                growthBlocks++;
                                if (nearExistingSite(pos, existingSites) || nearRecordedSite(pos, recordedPositions)) {
                                    skippedExisting++;
                                    continue;
                                }
                                if (nearRecordedSite(pos, rejectedPositions)) {
                                    skippedWithoutMineSignature++;
                                    continue;
                                }
                                if (!hasMineStructureSignature(level, pos)) {
                                    skippedWithoutMineSignature++;
                                    rejectedPositions.add(pos.immutable());
                                    continue;
                                }
                                ResourceLocation anchorId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                                ExpeditionLocatorService.record(level, ExpeditionSite.anchor(
                                        level.dimension(),
                                        pos.immutable(),
                                        anchorId,
                                        null,
                                        SiteQuality.NORMAL,
                                        SOURCE,
                                        ExpeditionSitePlacementState.PROVEN,
                                        null
                                ));
                                recordedPositions.add(pos.immutable());
                            }
                        }
                    }
                }
            }
        }
        return new Result(
                loadedChunks,
                growthBlocks,
                recordedPositions.size(),
                skippedExisting,
                skippedWithoutMineSignature
        );
    }

    private static boolean nearExistingSite(BlockPos candidate, List<ExpeditionSite> sites) {
        return sites.stream().anyMatch(site -> horizontalDistanceSquared(candidate, site.pos())
                <= SITE_DEDUPLICATION_RADIUS_SQUARED);
    }

    private static boolean nearRecordedSite(BlockPos candidate, List<BlockPos> recordedPositions) {
        return recordedPositions.stream().anyMatch(pos -> horizontalDistanceSquared(candidate, pos)
                <= SITE_DEDUPLICATION_RADIUS_SQUARED);
    }

    private static long horizontalDistanceSquared(BlockPos first, BlockPos second) {
        long dx = (long) first.getX() - second.getX();
        long dz = (long) first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private static boolean isGrowthSource(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String namespace = id.getNamespace();
        String path = id.getPath();
        return (namespace.equals("geore") && path.startsWith("budding_"))
                || (namespace.equals("ae2") && path.contains("budding_quartz"))
                || (namespace.equals("extendedae") && path.startsWith("entro_budding_"));
    }

    private static boolean hasMineStructureSignature(ServerLevel level, BlockPos origin) {
        int supports = 0;
        boolean marker = false;
        for (int dy = -SIGNATURE_VERTICAL_RADIUS; dy <= SIGNATURE_VERTICAL_RADIUS; dy++) {
            for (int dz = -SIGNATURE_HORIZONTAL_RADIUS; dz <= SIGNATURE_HORIZONTAL_RADIUS; dz++) {
                for (int dx = -SIGNATURE_HORIZONTAL_RADIUS; dx <= SIGNATURE_HORIZONTAL_RADIUS; dx++) {
                    BlockPos scanPos = origin.offset(dx, dy, dz);
                    LevelChunk chunk = level.getChunkSource().getChunkNow(scanPos.getX() >> 4, scanPos.getZ() >> 4);
                    if (chunk == null) {
                        continue;
                    }
                    BlockState state = chunk.getBlockState(scanPos);
                    if (state.is(Blocks.OAK_PLANKS)
                            || state.is(Blocks.OAK_LOG)
                            || state.is(Blocks.STRIPPED_OAK_LOG)) {
                        supports++;
                    }
                    if (state.is(Blocks.TORCH) || state.is(Blocks.LADDER)) {
                        marker = true;
                    }
                    if (supports >= 2 && marker) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public record Result(
            int loadedChunks,
            int growthBlocks,
            int recordedSites,
            int skippedExisting,
            int skippedWithoutMineSignature
    ) {
    }
}
