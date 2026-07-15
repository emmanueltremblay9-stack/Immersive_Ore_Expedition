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
    private static final int NODE_AND_OUTER_WALL_MARGIN = 3;

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
        return plan(
                requestedType,
                origin,
                quality,
                oreBlockId,
                oreState,
                oreBlockId,
                oreState,
                oreCount(quality),
                oreNodeCount(quality),
                random
        );
    }

    public static ExpeditionSiteBlockPlan plan(
            ExpeditionSiteType requestedType,
            BlockPos origin,
            SiteQuality quality,
            ResourceLocation oreBlockId,
            BlockState oreState,
            int oreBudget,
            int requestedOreNodeCount,
            RandomSource random
    ) {
        return plan(
                requestedType,
                origin,
                quality,
                oreBlockId,
                oreState,
                oreBlockId,
                oreState,
                oreBudget,
                requestedOreNodeCount,
                random
        );
    }

    public static ExpeditionSiteBlockPlan plan(
            ExpeditionSiteType requestedType,
            BlockPos origin,
            SiteQuality quality,
            ResourceLocation oreBlockId,
            BlockState oreState,
            ResourceLocation oreNodeHeartBlockId,
            BlockState oreNodeHeartState,
            int oreBudget,
            int requestedOreNodeCount,
            RandomSource random
    ) {
        return plan(
                requestedType,
                origin,
                quality,
                oreBlockId,
                oreState,
                oreNodeHeartBlockId,
                oreNodeHeartState,
                null,
                null,
                null,
                oreBudget,
                requestedOreNodeCount,
                random
        );
    }

    public static ExpeditionSiteBlockPlan plan(
            ExpeditionSiteType requestedType,
            BlockPos origin,
            SiteQuality quality,
            ResourceLocation oreBlockId,
            BlockState oreState,
            ResourceLocation oreNodeHeartBlockId,
            BlockState oreNodeHeartState,
            ResourceLocation specialGeodeComponentId,
            BlockState specialBuddingState,
            BlockState specialShellState,
            int oreBudget,
            int requestedOreNodeCount,
            RandomSource random
    ) {
        Objects.requireNonNull(requestedType, "requestedType");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(random, "random");
        boolean hasAnyOreNodeInput = oreBlockId != null
                || oreState != null
                || oreNodeHeartBlockId != null
                || oreNodeHeartState != null;
        boolean hasCompleteOreNodeInput = oreBlockId != null
                && oreState != null
                && oreNodeHeartBlockId != null
                && oreNodeHeartState != null;
        if (hasAnyOreNodeInput != hasCompleteOreNodeInput) {
            throw new IllegalArgumentException("Ore-node placement requires both material and budding-heart resources");
        }
        if ((specialBuddingState == null) != (specialShellState == null)) {
            throw new IllegalArgumentException("Special geode placement requires both budding and shell blocks");
        }
        boolean hasSpecialGeode = specialBuddingState != null;
        if ((specialGeodeComponentId != null) != hasSpecialGeode) {
            throw new IllegalArgumentException("Special geode placement requires one component identifier");
        }
        if (quality.isProductive()) {
            if (hasCompleteOreNodeInput == hasSpecialGeode) {
                throw new IllegalArgumentException(
                        "Productive mines require either a GeOre node-geode or one special geode"
                );
            }
            if (hasCompleteOreNodeInput && (oreBudget <= 0 || requestedOreNodeCount <= 0)) {
                throw new IllegalArgumentException("Productive chamber plans require positive ore and node budgets");
            }
            if (hasSpecialGeode && (oreBudget != 0 || requestedOreNodeCount != 0)) {
                throw new IllegalArgumentException("Special geodes cannot share a GeOre node budget");
            }
        } else if (oreBudget != 0 || requestedOreNodeCount != 0) {
            throw new IllegalArgumentException("Dry chamber plans require zero ore and node budgets");
        }

        Builder builder = new Builder();
        LinkedHashSet<ResourceLocation> components = new LinkedHashSet<>();
        BlockPos connectorEnd = origin;
        BlockPos chamberCenter = origin;
        int oreNodeCount = 0;

        if (requestedType.naturalSurfaceSite()) {
            int depth = MIN_CONNECTOR_DEPTH + random.nextInt(MAX_CONNECTOR_DEPTH - MIN_CONNECTOR_DEPTH + 1);
            int direction = directionTowardChunkCenter(origin);
            connectorEnd = addConnector(builder, origin, depth, direction, true);
            chamberCenter = connectedChamberCenter(connectorEnd, direction, quality);
            connectTunnelToChamber(builder, connectorEnd, direction, chamberCenter);
            oreNodeCount = addChamber(
                    builder,
                    chamberCenter,
                    quality,
                    oreState,
                    oreNodeHeartState,
                    specialBuddingState,
                    specialShellState,
                    oreBudget,
                    requestedOreNodeCount,
                    random
            );
            addSurfaceClue(builder, requestedType, origin);
            components.add(requestedType.id());
            components.add(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR);
            components.add(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER);
            if (quality.isProductive() && specialGeodeComponentId != null) {
                components.add(specialGeodeComponentId);
            }
        } else if (requestedType == ExpeditionSiteType.BASIC_MINESHAFT_CONNECTOR) {
            connectorEnd = addConnector(builder, origin, MIN_CONNECTOR_DEPTH, 1, false);
            chamberCenter = connectorEnd;
            components.add(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR);
        } else {
            oreNodeCount = addChamber(
                    builder,
                    origin,
                    quality,
                    oreState,
                    oreNodeHeartState,
                    null,
                    null,
                    oreBudget,
                    requestedOreNodeCount,
                    random
            );
            components.add(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER);
        }

        return new ExpeditionSiteBlockPlan(
                requestedType.id(),
                origin.immutable(),
                connectorEnd.immutable(),
                chamberCenter.immutable(),
                quality,
                oreNodeCount,
                quality.isProductive()
                        && !hasSpecialGeode
                        && components.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)
                        ? oreBlockId
                        : null,
                quality.isProductive()
                        && !hasSpecialGeode
                        && components.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)
                        ? oreNodeHeartBlockId
                        : null,
                List.copyOf(components),
                builder.blocks()
        );
    }

    private static int directionTowardChunkCenter(BlockPos origin) {
        int localX = Math.floorMod(origin.getX(), 16);
        return localX <= 7 ? 1 : -1;
    }

    private static BlockPos connectedChamberCenter(
            BlockPos connectorEnd,
            int horizontalDirection,
            SiteQuality quality
    ) {
        int horizontalExtent = horizontalRadius(quality) + NODE_AND_OUTER_WALL_MARGIN;
        int minimumLocalCoordinate = horizontalExtent;
        int maximumLocalCoordinate = 15 - horizontalExtent;
        if (minimumLocalCoordinate > maximumLocalCoordinate) {
            throw new IllegalStateException("Connected chamber cannot fit inside its source chunk");
        }

        int chunkMinX = Math.floorDiv(connectorEnd.getX(), 16) * 16;
        int chunkMinZ = Math.floorDiv(connectorEnd.getZ(), 16) * 16;
        int localZ = Math.floorMod(connectorEnd.getZ(), 16);
        int chamberLocalX = horizontalDirection > 0 ? minimumLocalCoordinate : maximumLocalCoordinate;
        int chamberLocalZ = Math.max(minimumLocalCoordinate, Math.min(maximumLocalCoordinate, localZ));
        return new BlockPos(
                chunkMinX + chamberLocalX,
                connectorEnd.getY(),
                chunkMinZ + chamberLocalZ
        );
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

    private static void connectTunnelToChamber(
            Builder builder,
            BlockPos connectorEnd,
            int horizontalDirection,
            BlockPos chamberCenter
    ) {
        BlockPos elbow = connectorEnd.offset(horizontalDirection * CHAMBER_HORIZONTAL_OFFSET, 0, 0);
        int zDirection = Integer.signum(chamberCenter.getZ() - elbow.getZ());
        int zDistance = Math.abs(chamberCenter.getZ() - elbow.getZ());
        for (int step = 1; step <= zDistance; step++) {
            carveNorthSouthTunnelSection(builder, elbow.offset(0, 0, zDirection * step));
        }
    }

    private static void carveNorthSouthTunnelSection(Builder builder, BlockPos center) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                builder.put(center.offset(dx, dy, 0), Blocks.AIR.defaultBlockState());
            }
        }
        builder.put(center.offset(0, -2, 0), Blocks.OAK_PLANKS.defaultBlockState());
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

    private static int addChamber(
            Builder builder,
            BlockPos center,
            SiteQuality quality,
            BlockState oreState,
            BlockState oreNodeHeartState,
            BlockState specialBuddingState,
            BlockState specialShellState,
            int oreBudget,
            int requestedOreNodeCount,
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
            return 0;
        }

        int placedNodeCount = 0;
        if (oreState != null) {
            List<BlockPos> seedCandidates = chamberShell(center, radius, halfHeight);
            seedCandidates.removeIf(candidate -> !touchesOpenChamber(
                    candidate,
                    center,
                    radius,
                    halfHeight,
                    chamberMarker
            ));
            List<BlockPos> nodeCandidates = chamberNodeCandidates(center, radius, halfHeight);
            nodeCandidates.remove(chamberMarker);
            nodeCandidates.remove(chamberMarker.above());
            placedNodeCount = addOreNodes(
                    builder,
                    nodeCandidates,
                    seedCandidates,
                    oreState,
                    oreNodeHeartState,
                    center,
                    radius,
                    halfHeight,
                    oreBudget,
                    requestedOreNodeCount,
                    random
            );
        }
        if (specialBuddingState != null) {
            addSpecialGeode(
                    builder,
                    center,
                    radius,
                    halfHeight,
                    chamberMarker,
                    specialBuddingState,
                    specialShellState,
                    random
            );
        }
        return placedNodeCount;
    }

    private static int addOreNodes(
            Builder builder,
            List<BlockPos> candidates,
            List<BlockPos> seedCandidates,
            BlockState oreState,
            BlockState oreNodeHeartState,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight,
            int requestedOreBudget,
            int requestedNodeCount,
            RandomSource random
    ) {
        shuffle(candidates, random);
        shuffle(seedCandidates, random);
        LinkedHashSet<BlockPos> candidatePositions = new LinkedHashSet<>(candidates);
        seedCandidates.removeIf(candidate -> !candidatePositions.contains(candidate));
        if (requestedNodeCount > requestedOreBudget || requestedNodeCount > seedCandidates.size()) {
            throw new IllegalStateException("Ore node candidate band cannot satisfy the requested node count");
        }

        int oreBudget = requestedOreBudget;
        int nodeCount = requestedNodeCount;
        List<BlockPos> seeds = chooseSeparatedSeeds(seedCandidates, nodeCount);
        LinkedHashSet<BlockPos> reservedAirFaces = reserveOpenFaces(
                builder,
                seeds,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        candidatePositions.removeAll(reservedAirFaces);
        if (requestedOreBudget > candidatePositions.size()) {
            throw new IllegalStateException("Ore node candidate band cannot satisfy the requested ore budget");
        }
        List<List<BlockPos>> growthOrders = connectedGrowthOrders(candidatePositions, seeds);
        int[] nodeBudgets = distributeOreBudget(growthOrders, oreBudget);
        LinkedHashSet<BlockPos> placedOrePositions = new LinkedHashSet<>();
        int placedOre = 0;
        for (int nodeIndex = 0; nodeIndex < seeds.size(); nodeIndex++) {
            List<BlockPos> growthOrder = growthOrders.get(nodeIndex);
            for (int index = 0; index < nodeBudgets[nodeIndex]; index++) {
                BlockPos orePos = growthOrder.get(index);
                builder.put(orePos, index == 0 ? oreNodeHeartState : oreState);
                placedOrePositions.add(orePos);
                placedOre++;
            }
        }
        if (placedOre != oreBudget) {
            throw new IllegalStateException("Ore node growth did not place the full ore budget");
        }
        addGeodeOuterWall(
                builder,
                placedOrePositions,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        return nodeCount;
    }

    private static LinkedHashSet<BlockPos> reserveOpenFaces(
            Builder builder,
            List<BlockPos> seeds,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight
    ) {
        LinkedHashSet<BlockPos> reserved = new LinkedHashSet<>();
        for (BlockPos seed : seeds) {
            BlockPos sharedFallback = null;
            BlockPos selected = null;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = seed.relative(direction);
                if (chamberDistance(neighbor, chamberCenter, chamberRadius, chamberHalfHeight) <= 1.0D
                        && builder.isAir(neighbor)) {
                    if (!reserved.contains(neighbor)) {
                        selected = neighbor;
                        break;
                    }
                    sharedFallback = neighbor;
                }
            }
            if (selected == null) {
                selected = sharedFallback;
            }
            if (selected == null) {
                throw new IllegalStateException("Ore-node budding heart has no chamber-facing air position");
            }
            reserved.add(selected.immutable());
        }
        return reserved;
    }

    private static void addGeodeOuterWall(
            Builder builder,
            LinkedHashSet<BlockPos> orePositions,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight
    ) {
        LinkedHashSet<BlockPos> calciteLayer = outwardLayer(
                builder,
                orePositions,
                orePositions,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        calciteLayer.forEach(pos -> builder.put(pos, Blocks.CALCITE.defaultBlockState()));

        LinkedHashSet<BlockPos> occupied = new LinkedHashSet<>(orePositions);
        occupied.addAll(calciteLayer);
        LinkedHashSet<BlockPos> basaltLayer = outwardLayer(
                builder,
                calciteLayer,
                occupied,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        basaltLayer.forEach(pos -> builder.put(pos, Blocks.SMOOTH_BASALT.defaultBlockState()));
    }

    private static void addSpecialGeode(
            Builder builder,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight,
            BlockPos chamberMarker,
            BlockState buddingState,
            BlockState shellState,
            RandomSource random
    ) {
        List<BlockPos> candidates = chamberShell(chamberCenter, chamberRadius, chamberHalfHeight);
        shuffle(candidates, random);
        BlockPos buddingPos = candidates.stream()
                .filter(candidate -> !builder.contains(candidate))
                .filter(candidate -> touchesOpenChamber(
                        candidate,
                        chamberCenter,
                        chamberRadius,
                        chamberHalfHeight,
                        chamberMarker
                ))
                .filter(candidate -> hasOpenAirFace(
                        builder,
                        candidate,
                        chamberCenter,
                        chamberRadius,
                        chamberHalfHeight
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Special geode has no chamber-facing budding position"));

        builder.put(buddingPos, buddingState);
        LinkedHashSet<BlockPos> buddingCore = new LinkedHashSet<>();
        buddingCore.add(buddingPos.immutable());
        LinkedHashSet<BlockPos> innerSkyStone = outwardLayer(
                builder,
                buddingCore,
                buddingCore,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        innerSkyStone.forEach(pos -> builder.put(pos, shellState));

        LinkedHashSet<BlockPos> occupied = new LinkedHashSet<>(buddingCore);
        occupied.addAll(innerSkyStone);
        LinkedHashSet<BlockPos> outerSkyStone = outwardLayer(
                builder,
                innerSkyStone,
                occupied,
                chamberCenter,
                chamberRadius,
                chamberHalfHeight
        );
        outerSkyStone.forEach(pos -> builder.put(pos, shellState));
    }

    private static boolean hasOpenAirFace(
            Builder builder,
            BlockPos candidate,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = candidate.relative(direction);
            if (chamberDistance(neighbor, chamberCenter, chamberRadius, chamberHalfHeight) <= 1.0D
                    && builder.isAir(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static LinkedHashSet<BlockPos> outwardLayer(
            Builder builder,
            Iterable<BlockPos> source,
            LinkedHashSet<BlockPos> occupied,
            BlockPos chamberCenter,
            int chamberRadius,
            int chamberHalfHeight
    ) {
        LinkedHashSet<BlockPos> layer = new LinkedHashSet<>();
        for (BlockPos sourcePos : source) {
            double sourceDistance = chamberDistance(
                    sourcePos,
                    chamberCenter,
                    chamberRadius,
                    chamberHalfHeight
            );
            for (Direction direction : Direction.values()) {
                BlockPos candidate = sourcePos.relative(direction);
                double candidateDistance = chamberDistance(
                        candidate,
                        chamberCenter,
                        chamberRadius,
                        chamberHalfHeight
                );
                if (candidateDistance > 1.0D
                        && candidateDistance > sourceDistance
                        && !occupied.contains(candidate)
                        && !builder.contains(candidate)) {
                    layer.add(candidate.immutable());
                }
            }
        }
        return layer;
    }

    private static double chamberDistance(
            BlockPos pos,
            BlockPos center,
            int horizontalRadius,
            int verticalHalfSize
    ) {
        return ellipsoidDistance(
                pos.getX() - center.getX(),
                pos.getY() - center.getY(),
                pos.getZ() - center.getZ(),
                horizontalRadius,
                verticalHalfSize
        );
    }

    private static List<List<BlockPos>> connectedGrowthOrders(
            LinkedHashSet<BlockPos> candidates,
            List<BlockPos> seeds
    ) {
        List<List<BlockPos>> growthOrders = new ArrayList<>(seeds.size());
        Map<BlockPos, Integer> owners = new LinkedHashMap<>();
        ArrayList<BlockPos> frontier = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < seeds.size(); nodeIndex++) {
            BlockPos seed = seeds.get(nodeIndex);
            ArrayList<BlockPos> growthOrder = new ArrayList<>();
            growthOrder.add(seed);
            growthOrders.add(growthOrder);
            owners.put(seed, nodeIndex);
            frontier.add(seed);
        }

        for (int cursor = 0; cursor < frontier.size(); cursor++) {
            BlockPos current = frontier.get(cursor);
            int nodeIndex = owners.get(current);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (candidates.contains(neighbor) && !owners.containsKey(neighbor)) {
                    owners.put(neighbor, nodeIndex);
                    growthOrders.get(nodeIndex).add(neighbor);
                    frontier.add(neighbor);
                }
            }
        }
        return growthOrders;
    }

    private static int[] distributeOreBudget(List<List<BlockPos>> growthOrders, int oreBudget) {
        int[] nodeBudgets = new int[growthOrders.size()];
        int remaining = oreBudget;
        while (remaining > 0) {
            boolean allocated = false;
            for (int nodeIndex = 0; nodeIndex < growthOrders.size() && remaining > 0; nodeIndex++) {
                if (nodeBudgets[nodeIndex] < growthOrders.get(nodeIndex).size()) {
                    nodeBudgets[nodeIndex]++;
                    remaining--;
                    allocated = true;
                }
            }
            if (!allocated) {
                throw new IllegalStateException("Connected ore node regions cannot satisfy the requested ore budget");
            }
        }
        return nodeBudgets;
    }

    private static List<BlockPos> chooseSeparatedSeeds(List<BlockPos> shell, int nodeCount) {
        List<BlockPos> seeds = new ArrayList<>(nodeCount);
        seeds.add(shell.getFirst());
        while (seeds.size() < nodeCount) {
            BlockPos next = shell.stream()
                    .filter(candidate -> !seeds.contains(candidate))
                    .max((first, second) -> Long.compare(
                            distanceFromNearestSeed(first, seeds),
                            distanceFromNearestSeed(second, seeds)
                    ))
                    .orElseThrow();
            seeds.add(next);
        }
        return seeds;
    }

    private static long distanceFromNearestSeed(BlockPos candidate, List<BlockPos> seeds) {
        return seeds.stream()
                .mapToLong(seed -> distanceSquared(candidate, seed))
                .min()
                .orElse(0L);
    }

    private static long distanceSquared(BlockPos first, BlockPos second) {
        long dx = (long) first.getX() - second.getX();
        long dy = (long) first.getY() - second.getY();
        long dz = (long) first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static List<BlockPos> chamberShell(BlockPos center, int radius, int halfHeight) {
        List<BlockPos> shell = new ArrayList<>();
        for (int dy = -halfHeight - 1; dy <= halfHeight + 1; dy++) {
            for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                    boolean outsideInterior = !insideEllipsoid(dx, dy, dz, radius, halfHeight, 1.0D);
                    boolean insideOuterShell = insideEllipsoid(dx, dy, dz, radius, halfHeight, 1.45D);
                    if (outsideInterior
                            && insideOuterShell
                            && touchesChamberInterior(dx, dy, dz, radius, halfHeight)) {
                        shell.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
        return shell;
    }

    private static boolean touchesChamberInterior(
            int dx,
            int dy,
            int dz,
            int horizontalRadius,
            int verticalHalfSize
    ) {
        for (Direction direction : Direction.values()) {
            if (insideEllipsoid(
                    dx + direction.getStepX(),
                    dy + direction.getStepY(),
                    dz + direction.getStepZ(),
                    horizontalRadius,
                    verticalHalfSize,
                    1.0D
            )) {
                return true;
            }
        }
        return false;
    }

    private static boolean touchesOpenChamber(
            BlockPos candidate,
            BlockPos center,
            int horizontalRadius,
            int verticalHalfSize,
            BlockPos chamberMarker
    ) {
        BlockPos torchPos = chamberMarker.above();
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = candidate.relative(direction);
            if (!neighbor.equals(chamberMarker)
                    && !neighbor.equals(torchPos)
                    && insideEllipsoid(
                    neighbor.getX() - center.getX(),
                    neighbor.getY() - center.getY(),
                    neighbor.getZ() - center.getZ(),
                    horizontalRadius,
                    verticalHalfSize,
                    1.0D
            )) {
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> chamberNodeCandidates(BlockPos center, int radius, int halfHeight) {
        List<BlockPos> candidates = new ArrayList<>();
        for (int dy = -halfHeight - 1; dy <= halfHeight + 1; dy++) {
            for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                    double distance = ellipsoidDistance(dx, dy, dz, radius, halfHeight);
                    if (distance >= 0.25D && distance <= 1.45D) {
                        candidates.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
        return candidates;
    }

    private static boolean insideEllipsoid(
            int dx,
            int dy,
            int dz,
            int horizontalRadius,
            int verticalHalfSize,
            double limit
    ) {
        return ellipsoidDistance(dx, dy, dz, horizontalRadius, verticalHalfSize) <= limit;
    }

    private static double ellipsoidDistance(
            int dx,
            int dy,
            int dz,
            int horizontalRadius,
            int verticalHalfSize
    ) {
        double horizontal = (double) (dx * dx + dz * dz)
                / (double) (horizontalRadius * horizontalRadius);
        double vertical = (double) (dy * dy) / (double) (verticalHalfSize * verticalHalfSize);
        return horizontal + vertical;
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

    private static int oreNodeCount(SiteQuality quality) {
        return switch (quality) {
            case DRY -> 0;
            case POOR -> 1;
            case NORMAL -> 2;
            case RICH -> 3;
            case MOTHERLODE -> 4;
        };
    }

    private static int horizontalRadius(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 2;
            case NORMAL -> 3;
            case RICH, MOTHERLODE -> 4;
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

        boolean contains(BlockPos pos) {
            return blocks.containsKey(pos);
        }

        boolean isAir(BlockPos pos) {
            BlockState state = blocks.get(pos);
            return state != null && state.isAir();
        }

        Map<BlockPos, BlockState> blocks() {
            return blocks;
        }
    }
}
