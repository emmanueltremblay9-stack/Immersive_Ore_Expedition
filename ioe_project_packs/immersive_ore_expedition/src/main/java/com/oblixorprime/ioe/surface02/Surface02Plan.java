package com.oblixorprime.ioe.surface02;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.worldgen.ExpeditionSiteBlockPlan;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Immutable, planning-only Surface 02 output. It is not registered or selected by active worldgen. */
public record Surface02Plan(
        BlockPos origin,
        SiteQuality quality,
        long planSeed,
        Surface02BiomeFamily observedBiomeFamily,
        Surface02BiomeFamily.Canonical canonicalBiomeFamily,
        Optional<Surface02ResourceResolver.ResolvedResource> resource,
        List<Slot> slots,
        Map<BlockPos, PlannedCell> blocks,
        Set<BlockPos> walkableCells,
        Set<BlockPos> growthEnvelope,
        Set<BlockPos> obstructionCells
) {
    public static final boolean PLANNING_ONLY = true;
    public static final boolean DEFAULT_OFF = true;
    public static final int WIDTH = 15;
    public static final int LENGTH = 15;
    public static final int HORIZONTAL_RADIUS = 7;
    public static final int MIN_DEPTH = 8;
    public static final int MAX_DEPTH = 15;
    public static final int FACADE_HEIGHT = 4;
    public static final ResourceLocation SURFACE_02_ID =
            ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "surface_02_abandoned_gallery");

    public Surface02Plan {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(observedBiomeFamily, "observedBiomeFamily");
        Objects.requireNonNull(canonicalBiomeFamily, "canonicalBiomeFamily");
        Objects.requireNonNull(resource, "resource");
        slots = List.copyOf(Objects.requireNonNull(slots, "slots"));
        blocks = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(blocks, "blocks")));
        walkableCells = Set.copyOf(Objects.requireNonNull(walkableCells, "walkableCells"));
        growthEnvelope = Set.copyOf(Objects.requireNonNull(growthEnvelope, "growthEnvelope"));
        obstructionCells = Set.copyOf(Objects.requireNonNull(obstructionCells, "obstructionCells"));
        if (observedBiomeFamily.canonical().isEmpty()
                || observedBiomeFamily.canonical().orElseThrow() != canonicalBiomeFamily) {
            throw new IllegalArgumentException("Surface 02 observed and canonical biome families must match");
        }

        if (slots.size() != SlotId.values().length
                || !slots.stream().map(Slot::id).toList().equals(List.of(SlotId.values()))) {
            throw new IllegalArgumentException("Surface 02 requires exactly SLOT_1 through SLOT_4");
        }
        if (quality == SiteQuality.RICH || quality == SiteQuality.MOTHERLODE) {
            throw new IllegalArgumentException("Surface 02 accepts only DRY, POOR, or NORMAL");
        }
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("Surface 02 plans require concrete block placements");
        }
        if (!blocks.keySet().containsAll(walkableCells)
                || !blocks.keySet().containsAll(growthEnvelope)
                || !blocks.keySet().containsAll(obstructionCells)) {
            throw new IllegalArgumentException("Surface 02 safety sets must refer to planned cells");
        }
        if (!Collections.disjoint(growthEnvelope, obstructionCells)) {
            throw new IllegalArgumentException("Surface 02 growth envelopes cannot contain an obstruction");
        }
        if (!Collections.disjoint(growthEnvelope, walkableCells)) {
            throw new IllegalArgumentException("Surface 02 growth envelopes cannot consume a mandatory path");
        }
        ChunkPos anchorChunk = new ChunkPos(origin);
        for (Map.Entry<BlockPos, PlannedCell> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!new ChunkPos(pos).equals(anchorChunk)
                    || Math.abs(pos.getX() - origin.getX()) > HORIZONTAL_RADIUS
                    || pos.getZ() < origin.getZ()
                    || pos.getZ() >= origin.getZ() + LENGTH
                    || pos.getY() < origin.getY() - MAX_DEPTH
                    || pos.getY() > origin.getY() + FACADE_HEIGHT) {
                throw new IllegalArgumentException("Surface 02 planned a block outside its single-chunk envelope");
            }
            if (isForbiddenHazard(entry.getValue().blockId())) {
                throw new IllegalArgumentException("Surface 02 cannot plan active hazards or falling materials");
            }
        }
        int plannedMineralCount = (int) blocks.values().stream()
                .filter(cell -> cell.kind() == CellKind.MINERAL)
                .count();
        Set<BlockPos> slotMineralPositions = slots.stream()
                .flatMap(slot -> slot.mineralPositions().stream())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> plannedMineralPositions = blocks.entrySet().stream()
                .filter(entry -> entry.getValue().kind() == CellKind.MINERAL)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> slotGrowthEnvelope = slots.stream()
                .flatMap(slot -> slot.protectedGrowthEnvelope().stream())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> nodePositions = slots.stream()
                .map(Slot::nodePos)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> approachPositions = slots.stream()
                .map(Slot::approachPos)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> plannedBuddingPositions = blocks.entrySet().stream()
                .filter(entry -> entry.getValue().kind() == CellKind.BUDDING)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<BlockPos> plannedObstructionPositions = blocks.entrySet().stream()
                .filter(entry -> entry.getValue().kind() == CellKind.OBSTRUCTION)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        int expectedActiveCount = switch (quality) {
            case DRY -> 0;
            case POOR -> 3;
            case NORMAL -> 4;
            case RICH, MOTHERLODE -> throw new IllegalStateException("ineligible Surface 02 quality");
        };
        Optional<BuddingRank> expectedRank = switch (quality) {
            case DRY -> Optional.empty();
            case POOR -> Optional.of(BuddingRank.DAMAGED);
            case NORMAL -> Optional.of(BuddingRank.CHIPPED);
            case RICH, MOTHERLODE -> throw new IllegalStateException("ineligible Surface 02 quality");
        };
        int expectedMineralPerActiveSlot = quality == SiteQuality.POOR
                ? 4
                : quality == SiteQuality.NORMAL ? 5 : 0;
        if (resource.isPresent() != (expectedActiveCount > 0)
                || resource.isPresent()
                && !Optional.of(resource.orElseThrow().rank()).equals(expectedRank)
                || slots.stream().filter(Slot::active).count() != expectedActiveCount
                || nodePositions.size() != slots.size()
                || approachPositions.size() != slots.size()) {
            throw new IllegalArgumentException("Surface 02 quality, resource, and slot ownership disagree");
        }
        if (plannedMineralCount != slots.stream().mapToInt(slot -> slot.mineralPositions().size()).sum()
                || !slotMineralPositions.equals(plannedMineralPositions)) {
            throw new IllegalArgumentException("Surface 02 mineral blocks must be owned by the four-slot core");
        }
        if (!slotGrowthEnvelope.equals(growthEnvelope)
                || slotGrowthEnvelope.size() != expectedActiveCount * Direction.values().length) {
            throw new IllegalArgumentException("Surface 02 growth cells must be owned by active slots");
        }
        if (!plannedObstructionPositions.equals(obstructionCells)) {
            throw new IllegalArgumentException("Surface 02 obstruction cells must be declared exactly");
        }
        for (Slot slot : slots) {
            PlannedCell nodeCell = blocks.get(slot.nodePos());
            PlannedCell approachCell = blocks.get(slot.approachPos());
            if (nodeCell == null || approachCell == null || approachCell.kind() != CellKind.AIR) {
                throw new IllegalArgumentException("Every Surface 02 slot requires a planned node and open approach");
            }
            if (nodeCell.layer() != Layer.FOUR_SLOT_CORE || approachCell.layer() != Layer.FOUR_SLOT_CORE) {
                throw new IllegalArgumentException("The Surface 02 core must own every node and approach");
            }
            if (slot.active() != (nodeCell.kind() == CellKind.BUDDING)) {
                throw new IllegalArgumentException("Only active Surface 02 slots may contain a Budding node");
            }
            if (!slot.active()
                    && (nodeCell.kind() != CellKind.OBSTRUCTION
                    || !nodeCell.blockId().equals(ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "cobbled_deepslate"
            )))) {
                throw new IllegalArgumentException("Inactive Surface 02 slots require the canonical depleted face");
            }
            boolean expectedSlotActive = quality == SiteQuality.NORMAL
                    || quality == SiteQuality.POOR && slot.id() != SlotId.SLOT_4;
            if (slot.active() != expectedSlotActive) {
                throw new IllegalArgumentException("Surface 02 active-slot identity is canonical and fixed");
            }
            if (!slot.nodePos().equals(canonicalNodePos(origin, slot.id()))
                    || !slot.approachPos().equals(canonicalApproachPos(origin, slot.id()))
                    || !slot.mineralPositions().equals(canonicalMineralPositions(
                    origin,
                    slot.id(),
                    slot.active() ? expectedMineralPerActiveSlot : 0
            ))) {
                throw new IllegalArgumentException("Surface 02 slot geometry and mineral ring are canonical and fixed");
            }
            if (!slot.activeRank().equals(slot.active() ? expectedRank : Optional.empty())
                    || slot.mineralPositions().size() != (slot.active() ? expectedMineralPerActiveSlot : 0)
                    || slot.protectedGrowthEnvelope().size() != (slot.active() ? Direction.values().length : 0)) {
                throw new IllegalArgumentException("Surface 02 slot rank, mineral budget, or growth envelope is invalid");
            }
            Set<BlockPos> expectedGrowthEnvelope = slot.active()
                    ? java.util.Arrays.stream(Direction.values())
                    .map(slot.nodePos()::relative)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet())
                    : Set.of();
            if (!slot.protectedGrowthEnvelope().equals(expectedGrowthEnvelope)) {
                throw new IllegalArgumentException("Surface 02 active slots require all six adjacent growth cells");
            }
            for (BlockPos growthPos : slot.protectedGrowthEnvelope()) {
                PlannedCell growthCell = blocks.get(growthPos);
                if (growthCell == null
                        || growthCell.kind() != CellKind.AIR
                        || growthCell.layer() != Layer.FOUR_SLOT_CORE) {
                    throw new IllegalArgumentException("Every protected growth cell must remain air");
                }
            }
            for (BlockPos mineralPos : slot.mineralPositions()) {
                PlannedCell mineralCell = blocks.get(mineralPos);
                if (mineralCell == null
                        || mineralCell.kind() != CellKind.MINERAL
                        || mineralCell.layer() != Layer.FOUR_SLOT_CORE) {
                    throw new IllegalArgumentException("The Surface 02 core must own every mineral block");
                }
            }
        }
        Set<BlockPos> activeNodePositions = slots.stream()
                .filter(Slot::active)
                .map(Slot::nodePos)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!plannedBuddingPositions.equals(activeNodePositions)) {
            throw new IllegalArgumentException("Surface 02 cannot contain unowned or missing Budding cells");
        }
        if (resource.isPresent()) {
            ResolvedIds resolvedIds = new ResolvedIds(
                    resource.orElseThrow().buddingBlockId(),
                    resource.orElseThrow().mineralBlockId()
            );
            for (BlockPos nodePos : activeNodePositions) {
                if (!blocks.get(nodePos).blockId().equals(resolvedIds.buddingBlockId())) {
                    throw new IllegalArgumentException("Surface 02 Budding cells disagree with the resolved profile");
                }
            }
            for (BlockPos mineralPos : plannedMineralPositions) {
                if (!blocks.get(mineralPos).blockId().equals(resolvedIds.mineralBlockId())) {
                    throw new IllegalArgumentException("Surface 02 mineral cells disagree with the resolved profile");
                }
            }
        }
        for (BlockPos walkable : walkableCells) {
            PlannedCell feet = blocks.get(walkable);
            PlannedCell head = blocks.get(walkable.above());
            if (feet == null || head == null || feet.kind() != CellKind.AIR || head.kind() != CellKind.AIR) {
                throw new IllegalArgumentException("Every mandatory Surface 02 path requires two clear blocks");
            }
        }
        BlockPos entrance = origin.offset(0, 0, 2);
        if (!walkableCells.contains(entrance)) {
            throw new IllegalArgumentException("Every Surface 02 quality requires its canonical entrance");
        }
        Set<BlockPos> reachable = reachableWalkableCells(entrance, walkableCells);
        for (Slot slot : slots) {
            if (slot.active() && !reachable.contains(slot.approachPos())) {
                throw new IllegalArgumentException("Every active Surface 02 slot must be reachable from the entrance");
            }
        }
    }

    public int activeBuddingCount() {
        return (int) slots.stream().filter(Slot::active).count();
    }

    public int mineralBlockCount() {
        return (int) blocks.values().stream().filter(cell -> cell.kind() == CellKind.MINERAL).count();
    }

    public long lootContainerCount() {
        return 0L;
    }

    /**
     * Passive adapter to the existing 0.2.53 transaction plan. The caller supplies block states only
     * after registries and dependencies are available; this pass does not call the adapter.
     */
    public ExpeditionSiteBlockPlan toExpeditionSiteBlockPlan(BlockStateResolver stateResolver) {
        Objects.requireNonNull(stateResolver, "stateResolver");
        LinkedHashMap<BlockPos, BlockState> adapted = new LinkedHashMap<>();
        blocks.forEach((pos, cell) -> {
            BlockState state = Objects.requireNonNull(stateResolver.resolve(cell.blockId()), "resolved block state");
            if (cell.layer() == Layer.ACCESS
                    && cell.kind() == CellKind.SUPPORT
                    && cell.blockId().getPath().endsWith("_stairs")
                    && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
            }
            ResourceLocation actualId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            validateResolvedState(
                    cell.blockId(),
                    cell.kind(),
                    actualId,
                    state.isAir(),
                    state.hasBlockEntity(),
                    state.getBlock() instanceof FallingBlock,
                    !state.getFluidState().isEmpty()
            );
            adapted.put(pos, state);
        });
        ResourceLocation mineralId = resource.map(Surface02ResourceResolver.ResolvedResource::mineralBlockId)
                .orElse(null);
        ResourceLocation buddingId = resource.map(Surface02ResourceResolver.ResolvedResource::buddingBlockId)
                .orElse(null);
        return new ExpeditionSiteBlockPlan(
                SURFACE_02_ID,
                origin,
                origin.offset(0, -MIN_DEPTH, 9),
                origin.offset(0, -MIN_DEPTH, 12),
                quality,
                activeBuddingCount(),
                mineralId,
                buddingId,
                slots.stream().map(Slot::approachPos).toList(),
                List.of(
                        SURFACE_02_ID,
                        IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR,
                        IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER
                ),
                adapted
        );
    }

    @FunctionalInterface
    public interface BlockStateResolver {
        BlockState resolve(ResourceLocation blockId);
    }

    public record Slot(
            SlotId id,
            BlockPos nodePos,
            BlockPos approachPos,
            Optional<BuddingRank> activeRank,
            Set<BlockPos> mineralPositions,
            Set<BlockPos> protectedGrowthEnvelope
    ) {
        public Slot {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(nodePos, "nodePos");
            Objects.requireNonNull(approachPos, "approachPos");
            Objects.requireNonNull(activeRank, "activeRank");
            mineralPositions = Set.copyOf(Objects.requireNonNull(mineralPositions, "mineralPositions"));
            protectedGrowthEnvelope = Set.copyOf(Objects.requireNonNull(
                    protectedGrowthEnvelope,
                    "protectedGrowthEnvelope"
            ));
            if (activeRank.isEmpty() && (!mineralPositions.isEmpty() || !protectedGrowthEnvelope.isEmpty())) {
                throw new IllegalArgumentException("Only active Surface 02 slots own mineral blocks");
            }
        }

        public boolean active() {
            return activeRank.isPresent();
        }
    }

    public record PlannedCell(Layer layer, CellKind kind, ResourceLocation blockId) {
        public PlannedCell {
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(blockId, "blockId");
            boolean airId = blockId.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "air"));
            if (airId != (kind == CellKind.AIR)) {
                throw new IllegalArgumentException("Only AIR cells may use minecraft:air, and AIR cells require it");
            }
        }
    }

    public enum SlotId {
        SLOT_1,
        SLOT_2,
        SLOT_3,
        SLOT_4
    }

    public enum Layer {
        BIOME_FACADE,
        ACCESS,
        FOUR_SLOT_CORE,
        NARRATIVE_DECORATION
    }

    public enum CellKind {
        AIR,
        SUPPORT,
        FACING,
        BUDDING,
        MINERAL,
        DECORATION,
        OBSTRUCTION
    }

    private static boolean isForbiddenHazard(ResourceLocation blockId) {
        if (!blockId.getNamespace().equals("minecraft")) {
            return false;
        }
        return switch (blockId.getPath()) {
            case "lava", "fire", "soul_fire", "campfire", "soul_campfire", "magma_block",
                    "sand", "red_sand", "gravel", "suspicious_sand", "suspicious_gravel",
                    "dragon_egg", "anvil", "chipped_anvil", "damaged_anvil" -> true;
            default -> false;
        };
    }

    static void validateResolvedState(
            ResourceLocation requestedId,
            CellKind kind,
            ResourceLocation actualId,
            boolean actualAir,
            boolean blockEntity,
            boolean falling,
            boolean containsFluid
    ) {
        Objects.requireNonNull(requestedId, "requestedId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(actualId, "actualId");
        if (!requestedId.equals(actualId)
                || actualAir != (kind == CellKind.AIR)
                || blockEntity
                || falling
                || containsFluid
                || isForbiddenHazard(actualId)) {
            throw new IllegalArgumentException("Unsafe or mismatched Surface 02 block-state resolution: "
                    + requestedId + " -> " + actualId);
        }
    }

    private static BlockPos canonicalNodePos(BlockPos origin, SlotId slotId) {
        return switch (slotId) {
            case SLOT_1 -> origin.offset(-6, -MIN_DEPTH + 1, 10);
            case SLOT_2 -> origin.offset(6, -MIN_DEPTH + 1, 10);
            case SLOT_3 -> origin.offset(-6, -MIN_DEPTH + 1, 13);
            case SLOT_4 -> origin.offset(6, -MIN_DEPTH + 1, 13);
        };
    }

    private static BlockPos canonicalApproachPos(BlockPos origin, SlotId slotId) {
        return switch (slotId) {
            case SLOT_1 -> origin.offset(-4, -MIN_DEPTH, 10);
            case SLOT_2 -> origin.offset(4, -MIN_DEPTH, 10);
            case SLOT_3 -> origin.offset(-4, -MIN_DEPTH, 13);
            case SLOT_4 -> origin.offset(4, -MIN_DEPTH, 13);
        };
    }

    private static Set<BlockPos> canonicalMineralPositions(BlockPos origin, SlotId slotId, int count) {
        if (count == 0) {
            return Set.of();
        }
        BlockPos nodePos = canonicalNodePos(origin, slotId);
        int outerX = nodePos.getX() < origin.getX()
                ? origin.getX() - HORIZONTAL_RADIUS
                : origin.getX() + HORIZONTAL_RADIUS;
        List<BlockPos> ring = List.of(
                new BlockPos(outerX, origin.getY() - MIN_DEPTH, nodePos.getZ()),
                new BlockPos(outerX, origin.getY() - MIN_DEPTH + 2, nodePos.getZ()),
                new BlockPos(outerX, origin.getY() - MIN_DEPTH + 1, nodePos.getZ() - 1),
                new BlockPos(outerX, origin.getY() - MIN_DEPTH + 1, nodePos.getZ() + 1),
                new BlockPos(outerX, origin.getY() - MIN_DEPTH, nodePos.getZ() + 1)
        );
        return ring.stream()
                .limit(count)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static Set<BlockPos> reachableWalkableCells(BlockPos entrance, Set<BlockPos> walkableCells) {
        if (!walkableCells.contains(entrance)) {
            return Set.of();
        }
        LinkedHashSet<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (BlockPos candidate : walkableCells) {
                int horizontal = Math.abs(candidate.getX() - current.getX())
                        + Math.abs(candidate.getZ() - current.getZ());
                int vertical = Math.abs(candidate.getY() - current.getY());
                if (horizontal == 1 && vertical <= 1 && !visited.contains(candidate)) {
                    queue.addLast(candidate);
                }
            }
        }
        return Set.copyOf(visited);
    }

    private record ResolvedIds(ResourceLocation buddingBlockId, ResourceLocation mineralBlockId) {
    }
}
