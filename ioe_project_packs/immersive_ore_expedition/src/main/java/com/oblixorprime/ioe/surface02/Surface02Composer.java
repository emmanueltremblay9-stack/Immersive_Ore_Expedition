package com.oblixorprime.ioe.surface02;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Independent, deterministic Surface 02 composer.
 *
 * <p>This class has no registration or placement side effects. The active 0.2.53 pipeline does not
 * reference it; future activation must supply a final quality, one resolved resource, one plan seed,
 * one visual biome family, and an approved single-chunk terrain envelope.</p>
 */
public final class Surface02Composer {
    /** Decorative aging only; never participates in quality, resource, slot, rank, or budget. */
    static final long AGING_SALT = 0x5355524630324147L;
    /** Facade asymmetry only. */
    static final long FACADE_SALT = 0x5355524630324641L;
    /** Narrative debris choice only. The bypass and slots remain fixed. */
    static final long NARRATIVE_SALT = 0x5355524630324E41L;

    private static final ResourceLocation AIR = vanilla("air");
    private static final ResourceLocation DEPLETED_FACE = vanilla("cobbled_deepslate");
    private Surface02Composer() {
    }

    /**
     * Canonical production bridge. Productive requests resolve their exact Budding rank through the
     * compiled foundation; DRY deliberately does not claim any resource-family support.
     */
    public static Decision resolveAndCompose(
            SiteQuality finalQuality,
            BuddingResourceFamily requestedFamily,
            long planSeed,
            Surface02BiomeFamily biomeFamily,
            BlockPos origin,
            Surface02TerrainContext terrain
    ) {
        Objects.requireNonNull(finalQuality, "finalQuality");
        Objects.requireNonNull(requestedFamily, "requestedFamily");
        Optional<Surface02ResourceResolver.Resolution> resolved = requiredRank(finalQuality)
                .map(rank -> Surface02ResourceResolver.resolve(requestedFamily, rank));
        return compose(new Request(finalQuality, resolved, planSeed, biomeFamily, origin, terrain));
    }

    public static Decision compose(Request request) {
        Objects.requireNonNull(request, "request");
        if (request.finalQuality() == SiteQuality.RICH || request.finalQuality() == SiteQuality.MOTHERLODE) {
            return Decision.refused(
                    Status.INELIGIBLE_QUALITY,
                    "Surface 02 accepts only final DRY, POOR, or NORMAL quality"
            );
        }

        Optional<Surface02BiomeFamily.Canonical> canonicalBiome = request.biomeFamily().canonical();
        if (canonicalBiome.isEmpty() || !request.terrain().admitsCanonicalEnvelope(request.origin())) {
            return Decision.refused(
                    Status.INVALID_TERRAIN,
                    "Surface 02 requires a stable, non-aquatic, non-shoreline single-anchor-chunk envelope"
            );
        }

        Optional<BuddingRank> rank = requiredRank(request.finalQuality());
        Optional<Surface02ResourceResolver.ResolvedResource> resource = Optional.empty();
        if (rank.isPresent()) {
            Surface02ResourceResolver.Resolution resolution = request.resolvedResourceProfile().orElse(null);
            if (resolution == null
                    || resolution.status() != Surface02ResourceResolver.Status.SUPPORTED
                    || resolution.resource().isEmpty()
                    || resolution.resource().orElseThrow().rank() != rank.orElseThrow()) {
                return Decision.refused(
                        Status.UNSUPPORTED_RESOURCE_FAMILY,
                        resolution == null
                                ? "Productive Surface 02 requests require one exact Budding foundation resolution"
                                : resolution.detail()
                );
            }
            resource = resolution.resource();
        }

        Surface02Plan plan = buildPlan(request, canonicalBiome.orElseThrow(), resource);
        return Decision.supported(
                plan,
                request.finalQuality() == SiteQuality.DRY
                        ? "Sterile Surface 02 geometry; resource rank capability was not used or claimed"
                        : "Exact Surface 02 resource rank and mineral budget composed"
        );
    }

    private static Surface02Plan buildPlan(
            Request request,
            Surface02BiomeFamily.Canonical canonicalBiome,
            Optional<Surface02ResourceResolver.ResolvedResource> resource
    ) {
        PlanBuilder builder = new PlanBuilder(request.origin(), request.terrain().allowedBounds());
        composeFacade(builder, canonicalBiome, request.planSeed());
        composeAccess(builder, canonicalBiome);
        List<Surface02Plan.Slot> slots = composeCore(builder, request.finalQuality(), resource);
        composeNarrativeDecoration(builder, canonicalBiome, request.planSeed());
        builder.assertActiveSlotsReachable(slots);

        return new Surface02Plan(
                request.origin(),
                request.finalQuality(),
                request.planSeed(),
                request.biomeFamily(),
                canonicalBiome,
                resource,
                slots,
                builder.blocks,
                builder.walkable,
                builder.growthEnvelope,
                builder.obstructions
        );
    }

    private static void composeFacade(
            PlanBuilder builder,
            Surface02BiomeFamily.Canonical biome,
            long planSeed
    ) {
        Surface02BiomeFamily.Palette palette = biome.palette();
        int brokenSide = saltedBit(planSeed, FACADE_SALT) == 0 ? -1 : 1;
        for (int z = 0; z <= 2; z++) {
            for (int y = 0; y <= 3; y++) {
                builder.putRelative(
                        brokenSide * 3,
                        y,
                        z,
                        Surface02Plan.Layer.BIOME_FACADE,
                        Surface02Plan.CellKind.SUPPORT,
                        palette.supportBlockId()
                );
                if (y <= 2 || z != 1) {
                    builder.putRelative(
                            -brokenSide * 3,
                            y,
                            z,
                            Surface02Plan.Layer.BIOME_FACADE,
                            Surface02Plan.CellKind.SUPPORT,
                            palette.supportBlockId()
                    );
                }
            }
        }
        for (int x = -3; x <= 3; x++) {
            builder.putRelative(
                    x,
                    4,
                    1,
                    Surface02Plan.Layer.BIOME_FACADE,
                    Surface02Plan.CellKind.FACING,
                    palette.plankBlockId()
            );
        }
        for (int z = 0; z <= 2; z++) {
            for (int x = -1; x <= 1; x++) {
                builder.addWalkableRelative(x, 0, z, Surface02Plan.Layer.BIOME_FACADE);
            }
        }
        builder.putRelative(
                -4,
                0,
                1,
                Surface02Plan.Layer.BIOME_FACADE,
                Surface02Plan.CellKind.FACING,
                palette.facingBlockId()
        );
        builder.putRelative(
                4,
                0,
                2,
                Surface02Plan.Layer.BIOME_FACADE,
                Surface02Plan.CellKind.FACING,
                palette.facingBlockId()
        );
    }

    private static void composeAccess(PlanBuilder builder, Surface02BiomeFamily.Canonical biome) {
        Surface02BiomeFamily.Palette palette = biome.palette();
        ResourceLocation stairBlock = vanilla(
                palette.plankBlockId().getPath().replace("_planks", "_stairs")
        );
        for (int z = 2; z <= 10; z++) {
            int depth = z - 2;
            int floorY = -depth;
            for (int x = -1; x <= 1; x++) {
                builder.putRelative(
                        x,
                        floorY - 1,
                        z,
                        Surface02Plan.Layer.ACCESS,
                        Surface02Plan.CellKind.SUPPORT,
                        stairBlock
                );
                builder.addWalkableRelative(x, floorY, z, Surface02Plan.Layer.ACCESS);
            }
            if (z % 2 == 0 && z < 10) {
                for (int y = floorY; y <= floorY + 2; y++) {
                    builder.putRelative(
                            -2,
                            y,
                            z,
                            Surface02Plan.Layer.ACCESS,
                            Surface02Plan.CellKind.SUPPORT,
                            palette.supportBlockId()
                    );
                    builder.putRelative(
                            2,
                            y,
                            z,
                            Surface02Plan.Layer.ACCESS,
                            Surface02Plan.CellKind.SUPPORT,
                            palette.supportBlockId()
                    );
                }
            }
        }
    }

    private static List<Surface02Plan.Slot> composeCore(
            PlanBuilder builder,
            SiteQuality quality,
            Optional<Surface02ResourceResolver.ResolvedResource> resource
    ) {
        int coreY = -Surface02Plan.MIN_DEPTH;
        for (int z = 10; z <= 14; z++) {
            for (int x = -1; x <= 1; x++) {
                builder.addWalkableRelative(x, coreY, z, Surface02Plan.Layer.FOUR_SLOT_CORE);
            }
        }

        List<SlotCoordinate> coordinates = List.of(
                new SlotCoordinate(Surface02Plan.SlotId.SLOT_1, -6, 10, -4),
                new SlotCoordinate(Surface02Plan.SlotId.SLOT_2, 6, 10, 4),
                new SlotCoordinate(Surface02Plan.SlotId.SLOT_3, -6, 13, -4),
                new SlotCoordinate(Surface02Plan.SlotId.SLOT_4, 6, 13, 4)
        );
        for (SlotCoordinate coordinate : coordinates) {
            for (int x = Math.min(0, coordinate.approachX()); x <= Math.max(0, coordinate.approachX()); x++) {
                for (int z = coordinate.z() - 1; z <= coordinate.z() + 1; z++) {
                    if (z == 9 && Math.abs(x) <= 1) {
                        continue;
                    }
                    builder.addWalkableRelative(x, coreY, z, Surface02Plan.Layer.FOUR_SLOT_CORE);
                }
            }
        }

        // A fixed two-block-high collapse interrupts the direct line but leaves x=-1 and x=1 open.
        builder.replaceCoreAirWithObstruction(0, coreY, 12, vanilla("tuff"));
        builder.replaceCoreAirWithObstruction(0, coreY + 1, 12, vanilla("cobblestone"));

        Optional<BuddingRank> rank = requiredRank(quality);
        int activeSlotCount = switch (quality) {
            case DRY -> 0;
            case POOR -> 3;
            case NORMAL -> 4;
            case RICH, MOTHERLODE -> throw new IllegalStateException("Ineligible quality reached Surface 02 core");
        };
        int mineralPerActiveSlot = quality == SiteQuality.POOR ? 4 : quality == SiteQuality.NORMAL ? 5 : 0;
        List<Surface02Plan.Slot> slots = new ArrayList<>(4);
        for (int slotIndex = 0; slotIndex < coordinates.size(); slotIndex++) {
            SlotCoordinate coordinate = coordinates.get(slotIndex);
            boolean active = slotIndex < activeSlotCount;
            BlockPos nodePos = builder.relative(coordinate.nodeX(), coreY + 1, coordinate.z());
            BlockPos approachPos = builder.relative(coordinate.approachX(), coreY, coordinate.z());
            if (!active) {
                builder.put(
                        nodePos,
                        Surface02Plan.Layer.FOUR_SLOT_CORE,
                        Surface02Plan.CellKind.OBSTRUCTION,
                        DEPLETED_FACE
                );
                builder.obstructions.add(nodePos);
                slots.add(new Surface02Plan.Slot(
                        coordinate.id(),
                        nodePos,
                        approachPos,
                        Optional.empty(),
                        Set.of(),
                        Set.of()
                ));
                continue;
            }

            Surface02ResourceResolver.ResolvedResource exactResource = resource.orElseThrow();
            builder.put(
                    nodePos,
                    Surface02Plan.Layer.FOUR_SLOT_CORE,
                    Surface02Plan.CellKind.BUDDING,
                    exactResource.buddingBlockId()
            );
            LinkedHashSet<BlockPos> growthCells = new LinkedHashSet<>();
            for (Direction direction : Direction.values()) {
                BlockPos growthPos = nodePos.relative(direction);
                builder.putAir(growthPos, Surface02Plan.Layer.FOUR_SLOT_CORE);
                builder.growthEnvelope.add(growthPos);
                growthCells.add(growthPos);
            }

            LinkedHashSet<BlockPos> mineralPositions = mineralRing(
                    builder,
                    coordinate,
                    coreY,
                    mineralPerActiveSlot
            );
            for (BlockPos mineralPos : mineralPositions) {
                builder.put(
                        mineralPos,
                        Surface02Plan.Layer.FOUR_SLOT_CORE,
                        Surface02Plan.CellKind.MINERAL,
                        exactResource.mineralBlockId()
                );
            }
            slots.add(new Surface02Plan.Slot(
                    coordinate.id(),
                    nodePos,
                    approachPos,
                    rank,
                    mineralPositions,
                    growthCells
            ));
        }
        return List.copyOf(slots);
    }

    private static LinkedHashSet<BlockPos> mineralRing(
            PlanBuilder builder,
            SlotCoordinate coordinate,
            int coreY,
            int requested
    ) {
        int outerX = coordinate.nodeX() < 0 ? -7 : 7;
        List<BlockPos> candidates = List.of(
                builder.relative(outerX, coreY, coordinate.z()),
                builder.relative(outerX, coreY + 2, coordinate.z()),
                builder.relative(outerX, coreY + 1, coordinate.z() - 1),
                builder.relative(outerX, coreY + 1, coordinate.z() + 1),
                builder.relative(outerX, coreY, coordinate.z() + 1)
        );
        return candidates.stream()
                .limit(requested)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static void composeNarrativeDecoration(
            PlanBuilder builder,
            Surface02BiomeFamily.Canonical biome,
            long planSeed
    ) {
        int agingSide = saltedBit(planSeed, AGING_SALT) == 0 ? -1 : 1;
        int debrisSide = saltedBit(planSeed, NARRATIVE_SALT) == 0 ? -1 : 1;
        builder.putRelative(
                agingSide * 6,
                -6,
                8,
                Surface02Plan.Layer.NARRATIVE_DECORATION,
                Surface02Plan.CellKind.DECORATION,
                vanilla("cobweb")
        );
        builder.putRelative(
                debrisSide * 5,
                -8,
                8,
                Surface02Plan.Layer.NARRATIVE_DECORATION,
                Surface02Plan.CellKind.DECORATION,
                vanilla("rail")
        );
        builder.putRelative(
                -debrisSide * 6,
                -6,
                7,
                Surface02Plan.Layer.NARRATIVE_DECORATION,
                Surface02Plan.CellKind.DECORATION,
                vanilla("chain")
        );
        for (int x : List.of(-5, 5)) {
            builder.putRelative(
                    x,
                    -Surface02Plan.MIN_DEPTH,
                    12,
                    Surface02Plan.Layer.NARRATIVE_DECORATION,
                    Surface02Plan.CellKind.DECORATION,
                    vanilla("soul_lantern")
            );
        }
        if (biome == Surface02BiomeFamily.Canonical.COLD) {
            builder.putRelative(
                    7,
                    0,
                    4,
                    Surface02Plan.Layer.NARRATIVE_DECORATION,
                    Surface02Plan.CellKind.DECORATION,
                    vanilla("packed_ice")
            );
        } else if (biome == Surface02BiomeFamily.Canonical.WET) {
            builder.putRelative(
                    7,
                    0,
                    4,
                    Surface02Plan.Layer.NARRATIVE_DECORATION,
                    Surface02Plan.CellKind.DECORATION,
                    vanilla("moss_block")
            );
        } else if (biome == Surface02BiomeFamily.Canonical.RELIEF) {
            builder.putRelative(
                    7,
                    0,
                    4,
                    Surface02Plan.Layer.NARRATIVE_DECORATION,
                    Surface02Plan.CellKind.DECORATION,
                    vanilla("tuff")
            );
        }
    }

    private static Optional<BuddingRank> requiredRank(SiteQuality quality) {
        return switch (quality) {
            case DRY, RICH, MOTHERLODE -> Optional.empty();
            case POOR -> Optional.of(BuddingRank.DAMAGED);
            case NORMAL -> Optional.of(BuddingRank.CHIPPED);
        };
    }

    private static int saltedBit(long planSeed, long salt) {
        long value = planSeed ^ salt;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return (int) ((value ^ (value >>> 31)) & 1L);
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    public record Request(
            SiteQuality finalQuality,
            Optional<Surface02ResourceResolver.Resolution> resolvedResourceProfile,
            long planSeed,
            Surface02BiomeFamily biomeFamily,
            BlockPos origin,
            Surface02TerrainContext terrain
    ) {
        public Request {
            Objects.requireNonNull(finalQuality, "finalQuality");
            Objects.requireNonNull(resolvedResourceProfile, "resolvedResourceProfile");
            Objects.requireNonNull(biomeFamily, "biomeFamily");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(terrain, "terrain");
        }
    }

    public record Decision(Status status, Optional<Surface02Plan> plan, String detail) {
        public Decision {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(plan, "plan");
            Objects.requireNonNull(detail, "detail");
            if (plan.isPresent() != (status == Status.SUPPORTED)) {
                throw new IllegalArgumentException("Only supported Surface 02 decisions may contain a plan");
            }
        }

        private static Decision supported(Surface02Plan plan, String detail) {
            return new Decision(Status.SUPPORTED, Optional.of(plan), detail);
        }

        private static Decision refused(Status status, String detail) {
            return new Decision(status, Optional.empty(), detail);
        }
    }

    public enum Status {
        SUPPORTED,
        INELIGIBLE_QUALITY,
        UNSUPPORTED_RESOURCE_FAMILY,
        INVALID_TERRAIN
    }

    private record SlotCoordinate(
            Surface02Plan.SlotId id,
            int nodeX,
            int z,
            int approachX
    ) {
    }

    private static final class PlanBuilder {
        private final BlockPos origin;
        private final Surface02TerrainContext.Bounds allowedBounds;
        private final LinkedHashMap<BlockPos, Surface02Plan.PlannedCell> blocks = new LinkedHashMap<>();
        private final LinkedHashSet<BlockPos> walkable = new LinkedHashSet<>();
        private final LinkedHashSet<BlockPos> growthEnvelope = new LinkedHashSet<>();
        private final LinkedHashSet<BlockPos> obstructions = new LinkedHashSet<>();

        private PlanBuilder(BlockPos origin, Surface02TerrainContext.Bounds allowedBounds) {
            this.origin = origin;
            this.allowedBounds = allowedBounds;
        }

        private BlockPos relative(int x, int y, int z) {
            return origin.offset(x, y, z);
        }

        private void putRelative(
                int x,
                int y,
                int z,
                Surface02Plan.Layer layer,
                Surface02Plan.CellKind kind,
                ResourceLocation blockId
        ) {
            put(relative(x, y, z), layer, kind, blockId);
        }

        private void put(
                BlockPos pos,
                Surface02Plan.Layer layer,
                Surface02Plan.CellKind kind,
                ResourceLocation blockId
        ) {
            if (!allowedBounds.contains(pos)) {
                throw new IllegalArgumentException("Surface 02 write escaped its approved terrain bounds: " + pos);
            }
            Surface02Plan.PlannedCell cell = new Surface02Plan.PlannedCell(layer, kind, blockId);
            Surface02Plan.PlannedCell previous = blocks.putIfAbsent(pos.immutable(), cell);
            if (previous != null && !previous.equals(cell)) {
                if (previous.kind() == Surface02Plan.CellKind.AIR && kind == Surface02Plan.CellKind.AIR) {
                    return;
                }
                throw new IllegalStateException(
                        "Surface 02 layer collision at " + pos + ": " + previous + " vs " + cell
                );
            }
        }

        private void putAirRelative(int x, int y, int z, Surface02Plan.Layer layer) {
            putAir(relative(x, y, z), layer);
        }

        private void putAir(BlockPos pos, Surface02Plan.Layer layer) {
            put(pos, layer, Surface02Plan.CellKind.AIR, AIR);
        }

        private void addWalkableRelative(int x, int y, int z, Surface02Plan.Layer layer) {
            BlockPos feet = relative(x, y, z);
            putAir(feet, layer);
            putAir(feet.above(), layer);
            putAir(feet.above(2), layer);
            walkable.add(feet);
        }

        private void replaceCoreAirWithObstruction(int x, int y, int z, ResourceLocation blockId) {
            BlockPos pos = relative(x, y, z);
            Surface02Plan.PlannedCell previous = blocks.get(pos);
            if (previous == null
                    || previous.layer() != Surface02Plan.Layer.FOUR_SLOT_CORE
                    || previous.kind() != Surface02Plan.CellKind.AIR) {
                throw new IllegalStateException("Surface 02 collapse may replace only core air");
            }
            blocks.put(
                    pos,
                    new Surface02Plan.PlannedCell(
                            Surface02Plan.Layer.FOUR_SLOT_CORE,
                            Surface02Plan.CellKind.OBSTRUCTION,
                            blockId
                    )
            );
            walkable.remove(pos);
            obstructions.add(pos);
        }

        private void assertActiveSlotsReachable(List<Surface02Plan.Slot> slots) {
            BlockPos entrance = relative(0, 0, 2);
            LinkedHashSet<BlockPos> visited = new LinkedHashSet<>();
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            if (!walkable.contains(entrance)) {
                throw new IllegalStateException("Surface 02 entrance is not part of the mandatory path");
            }
            queue.add(entrance);
            while (!queue.isEmpty()) {
                BlockPos current = queue.removeFirst();
                if (!visited.add(current)) {
                    continue;
                }
                for (BlockPos candidate : walkable) {
                    int horizontal = Math.abs(candidate.getX() - current.getX())
                            + Math.abs(candidate.getZ() - current.getZ());
                    int vertical = Math.abs(candidate.getY() - current.getY());
                    if (horizontal == 1 && vertical <= 1 && !visited.contains(candidate)) {
                        queue.addLast(candidate);
                    }
                }
            }
            for (Surface02Plan.Slot slot : slots) {
                if (slot.active() && !visited.contains(slot.approachPos())) {
                    throw new IllegalStateException("Surface 02 active slot is inaccessible: " + slot.id());
                }
            }
        }
    }
}
