package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExpeditionSiteBlueprints {
    static final int MIN_CONNECTOR_DEPTH = 20;
    static final int MAX_CONNECTOR_DEPTH = 28;
    static final int CHAMBER_HORIZONTAL_OFFSET = 5;

    private ExpeditionSiteBlueprints() {
    }

    public static ExpeditionSiteBlockPlan plan(
            ExpeditionSiteType requestedType,
            BlockPos origin,
            SiteQuality quality,
            ResourceLocation oreBlockId,
            BlockState oreState,
            RandomSource random
    ) {
        Objects.requireNonNull(requestedType, "requestedType");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(random, "random");
        if (quality.isProductive()) {
            Objects.requireNonNull(oreBlockId, "oreBlockId");
            Objects.requireNonNull(oreState, "oreState");
        }

        Builder builder = new Builder();
        LinkedHashSet<ResourceLocation> components = new LinkedHashSet<>();
        BlockPos connectorEnd = origin;
        BlockPos chamberCenter = origin;

        if (requestedType.naturalSurfaceSite()) {
            int depth = MIN_CONNECTOR_DEPTH + random.nextInt(MAX_CONNECTOR_DEPTH - MIN_CONNECTOR_DEPTH + 1);
            int direction = directionTowardChunkCenter(origin);
            connectorEnd = addConnector(builder, origin, depth, direction, true);
            chamberCenter = connectorEnd.offset(direction * CHAMBER_HORIZONTAL_OFFSET, 0, 0);
            addChamber(builder, chamberCenter, quality, oreState, random);
            addSurfaceClue(builder, requestedType, origin);
            components.add(requestedType.id());
            components.add(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR);
            components.add(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER);
        } else if (requestedType == ExpeditionSiteType.BASIC_MINESHAFT_CONNECTOR) {
            connectorEnd = addConnector(builder, origin, MIN_CONNECTOR_DEPTH, 1, false);
            chamberCenter = connectorEnd;
            components.add(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR);
        } else {
            addChamber(builder, origin, quality, oreState, random);
            components.add(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER);
        }

        return new ExpeditionSiteBlockPlan(
                requestedType.id(),
                origin.immutable(),
                connectorEnd.immutable(),
                chamberCenter.immutable(),
                quality,
                quality.isProductive() && components.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)
                        ? oreBlockId
                        : null,
                List.copyOf(components),
                builder.blocks()
        );
    }

    private static int directionTowardChunkCenter(BlockPos origin) {
        int localX = Math.floorMod(origin.getX(), 16);
        return localX <= 7 ? 1 : -1;
    }

    private static BlockPos addConnector(
            Builder builder,
            BlockPos top,
            int depth,
            int horizontalDirection,
            boolean includeBranch
    ) {
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        for (int dy = 0; dy <= depth; dy++) {
            int y = top.getY() - dy;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    builder.put(new BlockPos(top.getX() + dx, y, top.getZ() + dz), Blocks.AIR.defaultBlockState());
                }
            }
            builder.put(new BlockPos(top.getX(), y, top.getZ() + 2), Blocks.OAK_LOG.defaultBlockState());
            builder.put(new BlockPos(top.getX(), y, top.getZ() + 1), ladder);

            if (dy % 5 == 0) {
                builder.put(new BlockPos(top.getX() - 2, y, top.getZ() - 2), Blocks.OAK_LOG.defaultBlockState());
                builder.put(new BlockPos(top.getX() + 2, y, top.getZ() - 2), Blocks.OAK_LOG.defaultBlockState());
                builder.put(new BlockPos(top.getX() - 2, y, top.getZ() + 2), Blocks.OAK_LOG.defaultBlockState());
                builder.put(new BlockPos(top.getX() + 2, y, top.getZ() + 2), Blocks.OAK_LOG.defaultBlockState());
            }
        }

        BlockPos bottom = top.below(depth);
        for (int step = 0; step <= CHAMBER_HORIZONTAL_OFFSET; step++) {
            int x = bottom.getX() + horizontalDirection * step;
            carveTunnelSection(builder, new BlockPos(x, bottom.getY(), bottom.getZ()));
            if (step % 3 == 0) {
                addTunnelSupport(builder, new BlockPos(x, bottom.getY(), bottom.getZ()));
            }
        }
        if (includeBranch) {
            addDryBranch(builder, bottom.offset(horizontalDirection * 2, 0, 0));
        }
        return bottom;
    }

    private static void carveTunnelSection(Builder builder, BlockPos center) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                builder.put(center.offset(0, dy, dz), Blocks.AIR.defaultBlockState());
            }
        }
        builder.put(center.offset(0, -2, 0), Blocks.OAK_PLANKS.defaultBlockState());
    }

    private static void addTunnelSupport(Builder builder, BlockPos center) {
        for (int dy = -1; dy <= 2; dy++) {
            builder.put(center.offset(0, dy, -2), Blocks.OAK_LOG.defaultBlockState());
            builder.put(center.offset(0, dy, 2), Blocks.OAK_LOG.defaultBlockState());
        }
        for (int dz = -2; dz <= 2; dz++) {
            builder.put(center.offset(0, 2, dz), Blocks.OAK_PLANKS.defaultBlockState());
        }
    }

    private static void addDryBranch(Builder builder, BlockPos junction) {
        for (int step = 1; step <= 4; step++) {
            BlockPos center = junction.offset(0, 0, step);
            carveTunnelSection(builder, center);
            if (step == 4) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        builder.put(center.offset(dx, dy, 1), Blocks.GRAVEL.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void addChamber(
            Builder builder,
            BlockPos center,
            SiteQuality quality,
            BlockState oreState,
            RandomSource random
    ) {
        int radius = horizontalRadius(quality);
        int halfHeight = verticalHalfSize(quality);
        for (int dy = -halfHeight; dy <= halfHeight; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (insideEllipsoid(dx, dy, dz, radius, halfHeight, 1.0D)) {
                        builder.put(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        BlockPos chamberMarker = center.offset(0, -halfHeight, 0);
        builder.put(chamberMarker, Blocks.CALCITE.defaultBlockState());
        builder.put(chamberMarker.above(), Blocks.TORCH.defaultBlockState());
        if (!quality.isProductive()) {
            return;
        }

        List<BlockPos> shell = chamberShell(center, radius, halfHeight);
        shuffle(shell, random);
        int targetCount = Math.min(oreCount(quality), shell.size());
        for (int index = 0; index < targetCount; index++) {
            builder.put(shell.get(index), oreState);
        }
    }

    private static List<BlockPos> chamberShell(BlockPos center, int radius, int halfHeight) {
        List<BlockPos> shell = new ArrayList<>();
        for (int dy = -halfHeight - 1; dy <= halfHeight + 1; dy++) {
            for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                    boolean outsideInterior = !insideEllipsoid(dx, dy, dz, radius, halfHeight, 1.0D);
                    boolean insideOuterShell = insideEllipsoid(dx, dy, dz, radius, halfHeight, 1.45D);
                    if (outsideInterior && insideOuterShell) {
                        shell.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
        return shell;
    }

    private static boolean insideEllipsoid(
            int dx,
            int dy,
            int dz,
            int horizontalRadius,
            int verticalHalfSize,
            double limit
    ) {
        double horizontal = (double) (dx * dx + dz * dz)
                / (double) (horizontalRadius * horizontalRadius);
        double vertical = (double) (dy * dy) / (double) (verticalHalfSize * verticalHalfSize);
        return horizontal + vertical <= limit;
    }

    private static void shuffle(List<BlockPos> positions, RandomSource random) {
        for (int index = positions.size() - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            BlockPos current = positions.get(index);
            positions.set(index, positions.get(swapIndex));
            positions.set(swapIndex, current);
        }
    }

    private static int oreCount(SiteQuality quality) {
        return switch (quality) {
            case DRY -> 0;
            case POOR -> 4;
            case NORMAL -> 8;
            case RICH -> 14;
            case MOTHERLODE -> 24;
        };
    }

    private static int horizontalRadius(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 2;
            case NORMAL -> 3;
            case RICH -> 4;
            case MOTHERLODE -> 5;
        };
    }

    private static int verticalHalfSize(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 1;
            case NORMAL, RICH -> 2;
            case MOTHERLODE -> 3;
        };
    }

    private static void addSurfaceClue(Builder builder, ExpeditionSiteType type, BlockPos origin) {
        switch (type) {
            case TINY_VERTICAL_MINE_ENTRANCE -> addMineEntrance(builder, origin);
            case COLLAPSED_SHAFT -> addCollapsedShaft(builder, origin);
            case MINER_CAMP -> addMinerCamp(builder, origin);
            case BURIED_SURVEY_MARKER -> addSurveyMarker(builder, origin);
            case BASIC_MINESHAFT_CONNECTOR, ORE_LOAD_CHAMBER -> throw new IllegalArgumentException(
                    "Underground components do not have surface clues"
            );
        }
    }

    private static void addMineEntrance(Builder builder, BlockPos origin) {
        addStoneRing(builder, origin, 2);
        for (int dx : new int[]{-2, 2}) {
            for (int dz : new int[]{-2, 2}) {
                for (int dy = 0; dy <= 2; dy++) {
                    builder.put(origin.offset(dx, dy, dz), Blocks.OAK_LOG.defaultBlockState());
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            builder.put(origin.offset(dx, 3, -2), Blocks.OAK_PLANKS.defaultBlockState());
            builder.put(origin.offset(dx, 3, 2), Blocks.OAK_PLANKS.defaultBlockState());
        }
        builder.put(origin.offset(0, 3, 0), Blocks.OAK_PLANKS.defaultBlockState());
        builder.put(origin.offset(0, 4, 0), Blocks.LANTERN.defaultBlockState());
    }

    private static void addCollapsedShaft(Builder builder, BlockPos origin) {
        addStoneRing(builder, origin, 3);
        for (int offset = -3; offset <= 3; offset++) {
            if (Math.floorMod(offset, 2) == 0) {
                builder.put(origin.offset(offset, 0, -3), Blocks.GRAVEL.defaultBlockState());
                builder.put(origin.offset(-3, 0, offset), Blocks.TUFF.defaultBlockState());
            }
        }
        for (int dy = 0; dy <= 3; dy++) {
            builder.put(origin.offset(-3, dy, 2), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        }
        for (int dx = -3; dx <= 1; dx++) {
            builder.put(origin.offset(dx, 3, 2), Blocks.OAK_PLANKS.defaultBlockState());
        }
        builder.put(origin.offset(2, 0, -2), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        builder.put(origin.offset(-2, 1, 3), Blocks.GRAVEL.defaultBlockState());
    }

    private static void addMinerCamp(Builder builder, BlockPos origin) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    builder.put(origin.offset(dx, -1, dz), Blocks.COARSE_DIRT.defaultBlockState());
                }
            }
        }
        for (int dx = -4; dx <= -1; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                builder.put(origin.offset(dx, -1, dz), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        for (int dx : new int[]{-4, -1}) {
            for (int dz : new int[]{-2, 2}) {
                for (int dy = 0; dy <= 3; dy++) {
                    builder.put(origin.offset(dx, dy, dz), Blocks.OAK_LOG.defaultBlockState());
                }
            }
        }
        for (int dx = -4; dx <= -1; dx++) {
            builder.put(origin.offset(dx, 3, -2), Blocks.WHITE_WOOL.defaultBlockState());
            builder.put(origin.offset(dx, 3, 2), Blocks.WHITE_WOOL.defaultBlockState());
            builder.put(origin.offset(dx, 4, 0), Blocks.WHITE_WOOL.defaultBlockState());
        }
        builder.put(origin.offset(3, -1, 3), Blocks.COBBLESTONE.defaultBlockState());
        builder.put(origin.offset(3, 0, 3), Blocks.CAMPFIRE.defaultBlockState());
        sealShaftWithHatch(builder, origin, Blocks.OAK_PLANKS.defaultBlockState());
    }

    private static void addSurveyMarker(Builder builder, BlockPos origin) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    builder.put(origin.offset(dx, -1, dz), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
            }
        }
        builder.put(origin.offset(-2, 0, 0), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        builder.put(origin.offset(-2, 1, 0), Blocks.STONE_BRICK_WALL.defaultBlockState());
        builder.put(origin.offset(-2, 2, 0), Blocks.LANTERN.defaultBlockState());
        sealShaftWithHatch(builder, origin, Blocks.STONE_BRICKS.defaultBlockState());
    }

    private static void sealShaftWithHatch(Builder builder, BlockPos origin, BlockState coverState) {
        BlockPos hatchPos = origin.offset(0, 0, 1);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos surfacePos = origin.offset(dx, 0, dz);
                builder.put(surfacePos, surfacePos.equals(hatchPos)
                        ? Blocks.OAK_TRAPDOOR.defaultBlockState()
                        : coverState);
            }
        }
    }

    private static void addStoneRing(Builder builder, BlockPos origin, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                    BlockState state = Math.floorMod(dx + dz, 3) == 0
                            ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            : Blocks.COBBLESTONE.defaultBlockState();
                    builder.put(origin.offset(dx, -1, dz), state);
                }
            }
        }
    }

    private static final class Builder {
        private final LinkedHashMap<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        void put(BlockPos pos, BlockState state) {
            blocks.put(pos.immutable(), Objects.requireNonNull(state, "state"));
        }

        Map<BlockPos, BlockState> blocks() {
            return blocks;
        }
    }
}
