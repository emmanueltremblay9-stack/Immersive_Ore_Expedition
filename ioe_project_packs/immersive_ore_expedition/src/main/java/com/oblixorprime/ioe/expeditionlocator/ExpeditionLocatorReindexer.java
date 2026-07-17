package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
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
                                RecoveredSurfaceEntry recoveredEntry = recoverSurfaceEntry(level, pos).orElse(null);
                                if (recoveredEntry == null) {
                                    skippedWithoutMineSignature++;
                                    rejectedPositions.add(pos.immutable());
                                    continue;
                                }
                                ExpeditionLocatorService.record(level, ExpeditionSite.anchor(
                                        level.dimension(),
                                        recoveredEntry.pos(),
                                        recoveredEntry.anchorId(),
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

    private static java.util.Optional<RecoveredSurfaceEntry> recoverSurfaceEntry(
            ServerLevel level,
            BlockPos growthSource
    ) {
        RecoveredSurfaceEntry highest = null;
        for (int dy = -SIGNATURE_VERTICAL_RADIUS; dy <= SIGNATURE_VERTICAL_RADIUS; dy++) {
            for (int dz = -SIGNATURE_HORIZONTAL_RADIUS; dz <= SIGNATURE_HORIZONTAL_RADIUS; dz++) {
                for (int dx = -SIGNATURE_HORIZONTAL_RADIUS; dx <= SIGNATURE_HORIZONTAL_RADIUS; dx++) {
                    BlockPos ladderPos = growthSource.offset(dx, dy, dz);
                    BlockState ladderState = loadedState(level, ladderPos);
                    if (ladderState == null || !ladderState.is(Blocks.LADDER)) {
                        continue;
                    }
                    BlockPos topLadder = topOfLoadedLadderColumn(level, ladderPos);
                    BlockState topState = loadedState(level, topLadder);
                    if (topState == null || !topState.hasProperty(LadderBlock.FACING)) {
                        continue;
                    }
                    BlockPos surfaceLevel = topLadder;
                    BlockState aboveTop = loadedState(level, topLadder.above());
                    if (aboveTop != null && aboveTop.is(Blocks.OAK_TRAPDOOR)) {
                        surfaceLevel = topLadder.above();
                    }
                    BlockPos surfaceOrigin = surfaceLevel
                            .relative(topState.getValue(LadderBlock.FACING))
                            .immutable();
                    ResourceLocation anchorId = detectSurfaceClue(level, surfaceOrigin);
                    if (anchorId == null) {
                        continue;
                    }
                    RecoveredSurfaceEntry candidate = new RecoveredSurfaceEntry(surfaceOrigin, anchorId);
                    if (highest == null || candidate.pos().getY() > highest.pos().getY()) {
                        highest = candidate;
                    }
                }
            }
        }
        return java.util.Optional.ofNullable(highest);
    }

    private static BlockPos topOfLoadedLadderColumn(ServerLevel level, BlockPos start) {
        BlockPos current = start.immutable();
        while (current.getY() + 1 < level.getMaxBuildHeight()) {
            BlockPos above = current.above();
            BlockState aboveState = loadedState(level, above);
            if (aboveState == null || !aboveState.is(Blocks.LADDER)) {
                break;
            }
            current = above;
        }
        return current;
    }

    private static ResourceLocation detectSurfaceClue(ServerLevel level, BlockPos origin) {
        if (isLoadedBlock(level, origin.offset(3, 0, 3), Blocks.CAMPFIRE)
                && isLoadedBlock(level, origin.offset(0, 0, 1), Blocks.OAK_TRAPDOOR)) {
            return IoeWorldgenFeatureKeys.MINER_CAMP;
        }
        if (isLoadedBlock(level, origin.offset(-2, 0, 0), Blocks.CHISELED_STONE_BRICKS)
                && isLoadedBlock(level, origin.offset(-2, 1, 0), Blocks.STONE_BRICK_WALL)
                && isLoadedBlock(level, origin.offset(0, 0, 1), Blocks.OAK_TRAPDOOR)) {
            return IoeWorldgenFeatureKeys.BURIED_SURVEY_MARKER;
        }
        if (isLoadedBlock(level, origin.offset(-3, 0, 0), Blocks.TUFF)
                && isLoadedBlock(level, origin.offset(-3, 0, 2), Blocks.STRIPPED_OAK_LOG)) {
            return IoeWorldgenFeatureKeys.COLLAPSED_SHAFT;
        }
        if (isLoadedBlock(level, origin.offset(0, 4, 0), Blocks.LANTERN)
                && isLoadedBlock(level, origin.offset(-2, 0, -2), Blocks.OAK_LOG)
                && isLoadedBlock(level, origin.offset(2, 0, 2), Blocks.OAK_LOG)) {
            return IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE;
        }
        return null;
    }

    private static boolean isLoadedBlock(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
        BlockState state = loadedState(level, pos);
        return state != null && state.is(block);
    }

    private static BlockState loadedState(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    public record Result(
            int loadedChunks,
            int growthBlocks,
            int recordedSites,
            int skippedExisting,
            int skippedWithoutMineSignature
    ) {
    }

    private record RecoveredSurfaceEntry(BlockPos pos, ResourceLocation anchorId) {
    }
}
