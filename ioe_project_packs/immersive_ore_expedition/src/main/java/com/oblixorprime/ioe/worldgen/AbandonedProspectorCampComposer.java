package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Deterministic abandoned-state overlay for the canonical prospector-camp composition.
 * Surface 01 remains the intact source layout; this class normalizes its old wear mask and then applies one
 * coherent abandoned condition without changing the underground plan or transaction.
 */
public final class AbandonedProspectorCampComposer {
    static final ResourceLocation POOR_LOOT_TABLE = lootId("abandoned_prospector_camp_supplies");
    static final ResourceLocation NORMAL_LOOT_TABLE = lootId("abandoned_prospector_camp_supplies_normal");
    static final ResourceLocation RICH_LOOT_TABLE = lootId("abandoned_prospector_camp_supplies_rich");
    static final ResourceLocation MOTHERLODE_LOOT_TABLE = lootId("abandoned_prospector_camp_supplies_motherlode");
    static final ResourceLocation SPARSE_LOOT_TABLE = lootId("abandoned_prospector_camp_supplies_sparse");

    private static final long WEATHERED_EDGE_SALT = 0x5D73A92E1468C0BFL;
    private static final long COLLAPSED_QUADRANT_SALT = 0xC1E47B950A632DF8L;
    private static final long CONTAINER_LOOT_SALT = 0x39A7F1526CE80D4BL;

    private AbandonedProspectorCampComposer() {
    }

    public static Composition compose(
            BlockPos shaftOrigin,
            SiteQuality quality,
            ProspectorCampContext context
    ) {
        Objects.requireNonNull(shaftOrigin, "shaftOrigin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(context, "context");
        return compose(shaftOrigin, quality, context, context.rotation());
    }

    static Composition compose(
            BlockPos shaftOrigin,
            SiteQuality quality,
            ProspectorCampContext context,
            Rotation explicitRotation
    ) {
        Objects.requireNonNull(shaftOrigin, "shaftOrigin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(explicitRotation, "explicitRotation");
        if (context.archetype() != ProspectorCampArchetype.ABANDONED) {
            throw new IllegalArgumentException("The abandoned composer requires the ABANDONED archetype");
        }
        if (context.visualFamily() == ProspectorCampVisualFamily.AQUATIC) {
            throw new IllegalArgumentException("Aquatic biomes deliberately have no prospector camp");
        }

        AbandonedProspectorCampState abandonedState = AbandonedProspectorCampState.select(
                context.siteSeed(),
                quality
        );
        ProspectorCampOutcropComposer.Composition active = ProspectorCampOutcropComposer.compose(
                shaftOrigin,
                quality,
                context,
                explicitRotation
        );
        LinkedHashMap<BlockPos, BlockState> blocks = new LinkedHashMap<>(active.blocks());
        LinkedHashMap<BlockPos, ExpeditionBlockEntityPayload> payloads =
                new LinkedHashMap<>(active.blockEntityPayloads());
        LinkedHashMap<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles =
                new LinkedHashMap<>(active.reservedSurfaceColumns());

        normalizeSurfaceOneWear(blocks, payloads, active, context.visualFamily());
        clearActiveHazards(blocks);
        switch (abandonedState) {
            case RECENTLY_ABANDONED -> applyRecentlyAbandoned(blocks, payloads, roles, context);
            case WEATHERED -> applyWeathered(blocks, payloads, roles, active, context);
            case COLLAPSED -> applyCollapsed(blocks, payloads, roles, active, shaftOrigin, quality, context);
            case FAILED_PROSPECTION -> applyFailedProspection(blocks, payloads, roles, active, context);
            case EVACUATED -> applyEvacuated(blocks, payloads, roles, active, shaftOrigin, context);
        }
        applyContainerContract(blocks, payloads, roles, quality, abandonedState, context);

        BlockPos shelterEntrance = preserveWalkability(
                blocks,
                payloads,
                roles,
                active,
                shaftOrigin,
                context.visualFamily()
        );
        return new Composition(
                context.visualFamily(),
                abandonedState,
                active.qualitySpec(),
                active.rotation(),
                shaftOrigin,
                blocks,
                payloads,
                roles,
                active.biomeGeometryBlocks(),
                active.openRaisedShelterHeadroomBay(),
                shelterEntrance
        );
    }

    private static void normalizeSurfaceOneWear(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampVisualFamily family
    ) {
        int roofOffset = active.qualitySpec().postHeight();
        Optional<BlockPos> intentionalOpening = active.openRaisedShelterHeadroomBay();
        for (BlockPos shelterColumn : columnsForRole(
                active.reservedSurfaceColumns(),
                ProspectorCampOutcropComposer.ComponentRole.SHELTER
        )) {
            if (intentionalOpening.filter(shelterColumn::equals).isPresent()) {
                continue;
            }
            BlockPos roofPos = shelterColumn.above(roofOffset);
            BlockState existing = blocks.get(roofPos);
            if (existing == null || existing.isAir()) {
                put(blocks, payloads, roofPos, roofBlock(family).defaultBlockState());
            }
        }
    }

    private static void clearActiveHazards(Map<BlockPos, BlockState> blocks) {
        blocks.replaceAll((pos, state) -> state.hasProperty(CampfireBlock.LIT)
                ? state.setValue(CampfireBlock.LIT, false)
                : state);
        if (blocks.values().stream().anyMatch(state -> state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.LAVA))) {
            throw new IllegalStateException("Abandoned prospector camps cannot contain active fire or lava");
        }
    }

    private static void applyRecentlyAbandoned(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampContext context
    ) {
        firstColumn(roles, ProspectorCampOutcropComposer.ComponentRole.SAMPLE).ifPresent(column -> {
            put(blocks, payloads, column, timberBlock(context.visualFamily()).defaultBlockState());
            put(blocks, payloads, column.above(), Blocks.AIR.defaultBlockState());
        });
    }

    private static void applyWeathered(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampContext context
    ) {
        List<BlockPos> shelter = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.SHELTER);
        List<BlockPos> edge = selectedShelterEdge(shelter, context, WEATHERED_EDGE_SALT);
        int gapSize = Math.max(1, active.qualitySpec().shelterWidth() / 3);
        int start = deterministicIndex(context, WEATHERED_EDGE_SALT ^ 0x31L, edge.size());
        for (int index = 0; index < Math.min(gapSize, edge.size()); index++) {
            BlockPos column = edge.get((start + index) % edge.size());
            put(blocks, payloads, column.above(active.qualitySpec().postHeight()), Blocks.AIR.defaultBlockState());
        }
        if (!edge.isEmpty()) {
            BlockPos shortenedPost = edge.get(start % edge.size())
                    .above(Math.max(1, active.qualitySpec().postHeight() - 1));
            put(blocks, payloads, shortenedPost, Blocks.AIR.defaultBlockState());
        }
        fadeApproachPath(blocks, payloads, roles, context);
        addSafeDecayAccents(blocks, payloads, roles, context, familyIntrusionCount(context.visualFamily()));
    }

    private static void applyCollapsed(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.Composition active,
            BlockPos shaftOrigin,
            SiteQuality quality,
            ProspectorCampContext context
    ) {
        List<BlockPos> shelter = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.SHELTER);
        Bounds bounds = Bounds.of(shelter);
        int quadrant = deterministicIndex(context, COLLAPSED_QUADRANT_SALT, 4);
        List<BlockPos> failedQuadrant = shelter.stream()
                .filter(pos -> bounds.inQuadrant(pos, quadrant))
                .toList();
        int roofOffset = active.qualitySpec().postHeight();
        int shortenedFrom = Math.max(1, roofOffset / 2);
        for (BlockPos column : failedQuadrant) {
            put(blocks, payloads, column.above(roofOffset), Blocks.AIR.defaultBlockState());
        }
        if (!failedQuadrant.isEmpty()) {
            BlockPos support = failedQuadrant.get(0);
            for (int y = shortenedFrom; y < roofOffset; y++) {
                put(blocks, payloads, support.above(y), Blocks.AIR.defaultBlockState());
            }
        }
        int debrisCount = switch (quality) {
            case DRY -> 1;
            case POOR -> 2;
            case NORMAL -> 3;
            case RICH, MOTHERLODE -> 4;
        };
        List<BlockPos> debrisColumns = offRouteColumns(roles);
        for (int index = 0; index < Math.min(debrisCount, debrisColumns.size()); index++) {
            BlockPos column = debrisColumns.get(index);
            Block debris = index % 2 == 0
                    ? timberBlock(context.visualFamily())
                    : roofBlock(context.visualFamily());
            put(blocks, payloads, column, debris.defaultBlockState());
            put(blocks, payloads, column.above(), Blocks.AIR.defaultBlockState());
        }
        addSafeDecayAccents(blocks, payloads, roles, context, Math.min(1, familyIntrusionCount(
                context.visualFamily()
        )));
        addCollapsedDetour(
                blocks,
                payloads,
                roles,
                shaftOrigin,
                active.shaftHatchColumns(),
                context.visualFamily()
        );
    }

    private static void applyFailedProspection(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampContext context
    ) {
        List<BlockPos> examination = columnsForRoles(
                roles,
                Set.of(
                        ProspectorCampOutcropComposer.ComponentRole.SAMPLE,
                        ProspectorCampOutcropComposer.ComponentRole.OUTCROP
                )
        );
        for (int index = 0; index < Math.min(4, examination.size()); index++) {
            BlockPos column = examination.get(index);
            Block geologicalBlock = index % 2 == 0
                    ? rockBlock(context.visualFamily())
                    : rockAccentBlock(context.visualFamily());
            put(blocks, payloads, column, geologicalBlock.defaultBlockState());
            put(blocks, payloads, column.above(), Blocks.AIR.defaultBlockState());
        }
        List<BlockPos> shelter = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.SHELTER);
        int roofOffset = active.qualitySpec().postHeight();
        for (int index = shelter.size() / 2; index < shelter.size(); index++) {
            put(blocks, payloads, shelter.get(index).above(roofOffset), Blocks.AIR.defaultBlockState());
        }
        List<BlockPos> markers = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.MARKER);
        for (int index = 1; index < markers.size(); index++) {
            put(blocks, payloads, markers.get(index), Blocks.AIR.defaultBlockState());
            put(blocks, payloads, markers.get(index).above(), Blocks.AIR.defaultBlockState());
        }
        addSafeDecayAccents(blocks, payloads, roles, context, Math.min(1, familyIntrusionCount(
                context.visualFamily()
        )));
    }

    private static void applyEvacuated(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.Composition active,
            BlockPos shaftOrigin,
            ProspectorCampContext context
    ) {
        List<BlockPos> workstations = columnsForRole(
                roles,
                ProspectorCampOutcropComposer.ComponentRole.WORKSTATION
        );
        if (!workstations.isEmpty()) {
            BlockPos omitted = workstations.get(deterministicIndex(
                    context,
                    ABANDONED_EVACUATION_SALT,
                    workstations.size()
            ));
            put(blocks, payloads, omitted, Blocks.AIR.defaultBlockState());
            put(blocks, payloads, omitted.above(), Blocks.AIR.defaultBlockState());
        }
        List<BlockPos> shelter = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.SHELTER);
        ShelterFacade facade = campFacingShelterEdge(
                shelter,
                shaftOrigin,
                active.qualitySpec(),
                active.rotation()
        );
        for (BlockPos opening : facade.columns().stream().limit(2).toList()) {
            put(blocks, payloads, opening, Blocks.AIR.defaultBlockState());
            put(blocks, payloads, opening.above(), Blocks.AIR.defaultBlockState());
        }
        addSafeDecayAccents(blocks, payloads, roles, context, Math.min(1, familyIntrusionCount(
                context.visualFamily()
        )));
        emphasizeExitRoute(blocks, payloads, roles, context.visualFamily());
    }

    private static final long ABANDONED_EVACUATION_SALT = 0xE8B13C579A6402DFL;

    private static void fadeApproachPath(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampContext context
    ) {
        List<BlockPos> path = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.PATH);
        if (path.isEmpty()) {
            throw new IllegalStateException("Weathered abandoned camp has no approach path");
        }
        BlockPos faded = path.get(deterministicIndex(context, WEATHERED_EDGE_SALT ^ 0x52L, path.size()));
        put(blocks, payloads, faded.below(), decayAccentBlock(context.visualFamily()).defaultBlockState());
    }

    private static void emphasizeExitRoute(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampVisualFamily family
    ) {
        Block routeBlock = isRaised(family) ? planksBlock(family) : Blocks.COARSE_DIRT;
        for (BlockPos pathColumn : columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.PATH)) {
            put(blocks, payloads, pathColumn.below(), routeBlock.defaultBlockState());
        }
    }

    private static void addCollapsedDetour(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            BlockPos shaftOrigin,
            Set<BlockPos> shaftHatchColumns,
            ProspectorCampVisualFamily family
    ) {
        Set<BlockPos> path = new LinkedHashSet<>(columnsForRole(
                roles,
                ProspectorCampOutcropComposer.ComponentRole.PATH
        ));
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (BlockPos middle : path) {
            List<BlockPos> neighbors = new ArrayList<>();
            for (int[] direction : directions) {
                BlockPos neighbor = middle.offset(direction[0], 0, direction[1]);
                if (path.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            for (int first = 0; first < neighbors.size(); first++) {
                for (int second = first + 1; second < neighbors.size(); second++) {
                    BlockPos before = neighbors.get(first);
                    BlockPos after = neighbors.get(second);
                    List<List<BlockPos>> candidates = detourCandidates(before, middle, after);
                    for (List<BlockPos> detour : candidates) {
                        if (!detourAvailable(detour, roles, shaftOrigin, shaftHatchColumns)) {
                            continue;
                        }
                        roles.remove(middle);
                        put(blocks, payloads, middle, timberBlock(family).defaultBlockState());
                        put(blocks, payloads, middle.above(), Blocks.AIR.defaultBlockState());
                        Block detourFloor = isRaised(family) ? planksBlock(family) : Blocks.COARSE_DIRT;
                        for (BlockPos detourColumn : detour) {
                            roles.put(detourColumn.immutable(), ProspectorCampOutcropComposer.ComponentRole.ACCESS);
                            put(blocks, payloads, detourColumn.below(), detourFloor.defaultBlockState());
                            put(blocks, payloads, detourColumn, Blocks.AIR.defaultBlockState());
                            put(blocks, payloads, detourColumn.above(), Blocks.AIR.defaultBlockState());
                        }
                        return;
                    }
                }
            }
        }
        for (BlockPos before : path) {
            for (int[] direction : directions) {
                BlockPos after = before.offset(direction[0], 0, direction[1]);
                if (!path.contains(after)) {
                    continue;
                }
                int[][] perpendiculars = {{-direction[1], direction[0]}, {direction[1], -direction[0]}};
                for (int[] perpendicular : perpendiculars) {
                    List<BlockPos> detour = List.of(
                            before.offset(perpendicular[0], 0, perpendicular[1]),
                            after.offset(perpendicular[0], 0, perpendicular[1])
                    );
                    if (!detourAvailable(detour, roles, shaftOrigin, shaftHatchColumns)) {
                        continue;
                    }
                    Block detourFloor = isRaised(family) ? planksBlock(family) : Blocks.COARSE_DIRT;
                    for (BlockPos detourColumn : detour) {
                        roles.put(detourColumn.immutable(), ProspectorCampOutcropComposer.ComponentRole.ACCESS);
                        put(blocks, payloads, detourColumn.below(), detourFloor.defaultBlockState());
                        put(blocks, payloads, detourColumn, Blocks.AIR.defaultBlockState());
                        put(blocks, payloads, detourColumn.above(), Blocks.AIR.defaultBlockState());
                    }
                    return;
                }
            }
        }
        throw new IllegalStateException("Collapsed abandoned camp cannot reserve a clear alternate route");
    }

    private static List<List<BlockPos>> detourCandidates(
            BlockPos before,
            BlockPos middle,
            BlockPos after
    ) {
        int beforeDx = before.getX() - middle.getX();
        int beforeDz = before.getZ() - middle.getZ();
        int afterDx = after.getX() - middle.getX();
        int afterDz = after.getZ() - middle.getZ();
        if (beforeDx + afterDx == 0 && beforeDz + afterDz == 0) {
            int perpendicularX = -beforeDz;
            int perpendicularZ = beforeDx;
            return List.of(
                    List.of(
                            before.offset(perpendicularX, 0, perpendicularZ),
                            middle.offset(perpendicularX, 0, perpendicularZ),
                            after.offset(perpendicularX, 0, perpendicularZ)
                    ),
                    List.of(
                            before.offset(-perpendicularX, 0, -perpendicularZ),
                            middle.offset(-perpendicularX, 0, -perpendicularZ),
                            after.offset(-perpendicularX, 0, -perpendicularZ)
                    )
            );
        }
        return List.of(List.of(middle.offset(beforeDx + afterDx, 0, beforeDz + afterDz)));
    }

    private static boolean detourAvailable(
            List<BlockPos> detour,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            BlockPos shaftOrigin,
            Set<BlockPos> shaftHatchColumns
    ) {
        ChunkPos anchorChunk = new ChunkPos(shaftOrigin);
        for (BlockPos column : detour) {
            if (!new ChunkPos(column).equals(anchorChunk) || shaftHatchColumns.contains(column)) {
                return false;
            }
            ProspectorCampOutcropComposer.ComponentRole existing = roles.get(column);
            if (existing != null
                    && existing != ProspectorCampOutcropComposer.ComponentRole.PATH
                    && existing != ProspectorCampOutcropComposer.ComponentRole.ACCESS
                    && existing != ProspectorCampOutcropComposer.ComponentRole.VEGETATION
                    && existing != ProspectorCampOutcropComposer.ComponentRole.SAMPLE
                    && existing != ProspectorCampOutcropComposer.ComponentRole.HEARTH
                    && existing != ProspectorCampOutcropComposer.ComponentRole.WORKSTATION
                    && existing != ProspectorCampOutcropComposer.ComponentRole.CONTAINER) {
                return false;
            }
        }
        return true;
    }

    private static void applyContainerContract(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            SiteQuality quality,
            AbandonedProspectorCampState state,
            ProspectorCampContext context
    ) {
        List<BlockPos> containers = columnsForRole(
                roles,
                ProspectorCampOutcropComposer.ComponentRole.CONTAINER
        );
        for (BlockPos container : containers) {
            put(blocks, payloads, container, Blocks.AIR.defaultBlockState());
            put(blocks, payloads, container.above(), Blocks.AIR.defaultBlockState());
        }
        int limit = containerLimit(quality, state);
        if (limit == 0 || containers.isEmpty()) {
            return;
        }
        BlockPos retained = containers.get(deterministicIndex(context, CONTAINER_LOOT_SALT, containers.size()));
        put(blocks, payloads, retained, Blocks.BARREL.defaultBlockState());
        payloads.put(
                retained,
                ExpeditionBlockEntityPayload.loot(
                        lootTableFor(quality, state),
                        context.decorationValue(retained, CONTAINER_LOOT_SALT)
                )
        );
        put(blocks, payloads, retained.above(), Blocks.AIR.defaultBlockState());
    }

    static int containerLimit(SiteQuality quality, AbandonedProspectorCampState state) {
        if (quality == SiteQuality.DRY || state == AbandonedProspectorCampState.FAILED_PROSPECTION) {
            return 0;
        }
        if (state == AbandonedProspectorCampState.EVACUATED) {
            return quality.ordinal() >= SiteQuality.RICH.ordinal() ? 1 : 0;
        }
        if (state == AbandonedProspectorCampState.COLLAPSED) {
            return quality.ordinal() >= SiteQuality.RICH.ordinal() ? 1 : 0;
        }
        return 1;
    }

    private static ResourceLocation lootTableFor(
            SiteQuality quality,
            AbandonedProspectorCampState state
    ) {
        if (state == AbandonedProspectorCampState.EVACUATED) {
            return SPARSE_LOOT_TABLE;
        }
        return switch (quality) {
            case POOR -> POOR_LOOT_TABLE;
            case NORMAL -> NORMAL_LOOT_TABLE;
            case RICH -> RICH_LOOT_TABLE;
            case MOTHERLODE -> MOTHERLODE_LOOT_TABLE;
            case DRY -> throw new IllegalArgumentException("Dry abandoned camps cannot request loot");
        };
    }

    private static BlockPos preserveWalkability(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.Composition active,
            BlockPos shaftOrigin,
            ProspectorCampVisualFamily family
    ) {
        for (Map.Entry<BlockPos, ProspectorCampOutcropComposer.ComponentRole> entry : roles.entrySet()) {
            if (entry.getValue() == ProspectorCampOutcropComposer.ComponentRole.PATH
                    || entry.getValue() == ProspectorCampOutcropComposer.ComponentRole.ACCESS) {
                put(blocks, payloads, entry.getKey(), Blocks.AIR.defaultBlockState());
                put(blocks, payloads, entry.getKey().above(), Blocks.AIR.defaultBlockState());
            }
        }
        for (BlockPos hatchColumn : active.shaftHatchColumns()) {
            put(blocks, payloads, hatchColumn.above(), Blocks.AIR.defaultBlockState());
            put(blocks, payloads, hatchColumn.above(2), Blocks.AIR.defaultBlockState());
        }
        List<BlockPos> shelter = columnsForRole(roles, ProspectorCampOutcropComposer.ComponentRole.SHELTER);
        ShelterFacade facade = campFacingShelterEdge(
                shelter,
                shaftOrigin,
                active.qualitySpec(),
                active.rotation()
        );
        BlockPos entrance = reserveShelterAccess(
                blocks,
                payloads,
                roles,
                active.shaftHatchColumns(),
                shaftOrigin,
                active.qualitySpec().placedRadius(),
                facade
        );
        int standingOffset = isRaised(family) ? 1 : 0;
        put(blocks, payloads, entrance.above(standingOffset), Blocks.AIR.defaultBlockState());
        put(blocks, payloads, entrance.above(standingOffset + 1), Blocks.AIR.defaultBlockState());
        if (isRaised(family)) {
            put(blocks, payloads, entrance, planksBlock(family).defaultBlockState());
        }
        return entrance.immutable();
    }

    private static void addSafeDecayAccents(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampContext context,
            int count
    ) {
        List<BlockPos> candidates = columnsForRoles(
                roles,
                Set.of(
                        ProspectorCampOutcropComposer.ComponentRole.VEGETATION,
                        ProspectorCampOutcropComposer.ComponentRole.SAMPLE
                )
        );
        for (int index = 0; index < Math.min(count, candidates.size()); index++) {
            BlockPos column = candidates.get(index);
            put(blocks, payloads, column, decayAccentBlock(context.visualFamily()).defaultBlockState());
            put(blocks, payloads, column.above(), Blocks.AIR.defaultBlockState());
        }
    }

    private static int familyIntrusionCount(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE, CONIFER, SNOWY, WETLAND -> 2;
            case TROPICAL -> 3;
            case ARID, ROCKY -> 1;
            case VOLCANIC -> 0;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no abandoned camp");
        };
    }

    private static List<BlockPos> selectedShelterEdge(
            List<BlockPos> shelter,
            ProspectorCampContext context,
            long salt
    ) {
        Bounds bounds = Bounds.of(shelter);
        int edge = deterministicIndex(context, salt, 4);
        return shelter.stream().filter(pos -> switch (edge) {
            case 0 -> pos.getX() == bounds.minX();
            case 1 -> pos.getX() == bounds.maxX();
            case 2 -> pos.getZ() == bounds.minZ();
            default -> pos.getZ() == bounds.maxZ();
        }).toList();
    }

    static ShelterFacade campFacingShelterEdge(
            List<BlockPos> shelter,
            BlockPos shaftOrigin,
            ProspectorCampQualitySpec spec,
            Rotation campRotation
    ) {
        int radius = spec.placedRadius();
        int endX = radius;
        int startX = endX - spec.shelterWidth() + 1;
        int startZ = -radius + 1;
        int endZ = Math.min(radius - 1, startZ + spec.shelterDepth() - 1);
        ChunkPos anchorChunk = new ChunkPos(shaftOrigin);
        BlockPos campCenter = new BlockPos(
                anchorChunk.getMinBlockX() + 7,
                shaftOrigin.getY(),
                anchorChunk.getMinBlockZ() + 7
        );
        Set<BlockPos> actualShelter = new LinkedHashSet<>(shelter);
        List<Rotation> matches = new ArrayList<>();
        for (Rotation componentRotation : Rotation.values()) {
            Set<BlockPos> rotatedFootprint = new LinkedHashSet<>();
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos transformed = new BlockPos(x, 0, z)
                            .rotate(componentRotation)
                            .rotate(campRotation);
                    rotatedFootprint.add(campCenter.offset(transformed).immutable());
                }
            }
            if (rotatedFootprint.equals(actualShelter)) {
                matches.add(componentRotation);
            }
        }
        if (matches.size() != 1) {
            throw new IllegalStateException(
                    "Abandoned camp shelter must match exactly one canonical component rotation; matches="
                            + matches.size()
            );
        }
        Rotation componentRotation = matches.get(0);
        List<Integer> centerFirstX = new ArrayList<>();
        for (int x = startX; x <= endX; x++) {
            centerFirstX.add(x);
        }
        centerFirstX.sort(
                Comparator.comparingInt((Integer x) -> Math.abs((x * 2) - (startX + endX)))
                        .thenComparingInt(Integer::intValue)
        );
        List<BlockPos> columns = new ArrayList<>();
        List<ShelterAccessCandidate> accessCandidates = new ArrayList<>();
        for (int x : centerFirstX) {
            BlockPos entrance = transformShelterColumn(
                    campCenter,
                    x,
                    endZ,
                    componentRotation,
                    campRotation
            );
            columns.add(entrance);
            accessCandidates.add(new ShelterAccessCandidate(
                    entrance,
                    transformShelterColumn(
                            campCenter,
                            x,
                            endZ + 1,
                            componentRotation,
                            campRotation
                    )
            ));
        }
        accessCandidates.add(new ShelterAccessCandidate(
                transformShelterColumn(
                        campCenter,
                        startX,
                        endZ,
                        componentRotation,
                        campRotation
                ),
                transformShelterColumn(
                        campCenter,
                        startX - 1,
                        endZ,
                        componentRotation,
                        campRotation
                )
        ));
        Direction outward = campRotation.rotate(componentRotation.rotate(Direction.SOUTH));
        return new ShelterFacade(columns, outward, accessCandidates);
    }

    private static BlockPos transformShelterColumn(
            BlockPos campCenter,
            int x,
            int z,
            Rotation componentRotation,
            Rotation campRotation
    ) {
        BlockPos transformed = new BlockPos(x, 0, z)
                .rotate(componentRotation)
                .rotate(campRotation);
        return campCenter.offset(transformed).immutable();
    }

    private static BlockPos reserveShelterAccess(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            Set<BlockPos> hatchColumns,
            BlockPos shaftOrigin,
            int placedRadius,
            ShelterFacade facade
    ) {
        ChunkPos anchorChunk = new ChunkPos(shaftOrigin);
        for (ShelterAccessCandidate candidate : facade.accessCandidates()) {
            BlockPos accessStart = candidate.access();
            if (!new ChunkPos(accessStart).equals(anchorChunk)
                    || accessStart.equals(shaftOrigin)
                    || hatchColumns.contains(accessStart)
                    || facade.columns().contains(accessStart)) {
                continue;
            }
            ProspectorCampOutcropComposer.ComponentRole existingRole = roles.get(accessStart);
            if (existingRole != null
                    && existingRole != ProspectorCampOutcropComposer.ComponentRole.PATH
                    && existingRole != ProspectorCampOutcropComposer.ComponentRole.ACCESS) {
                continue;
            }
            if (!hasReachableSurfacePath(
                    accessStart,
                    blocks,
                    roles,
                    hatchColumns,
                    shaftOrigin,
                    placedRadius,
                    facade
            )) {
                continue;
            }
            if (existingRole == null) {
                roles.put(accessStart, ProspectorCampOutcropComposer.ComponentRole.ACCESS);
            }
            put(blocks, payloads, accessStart, Blocks.AIR.defaultBlockState());
            put(blocks, payloads, accessStart.above(), Blocks.AIR.defaultBlockState());
            return candidate.entrance();
        }
        throw new IllegalStateException("Abandoned camp shelter has no valid deterministic facade access");
    }

    private static boolean hasReachableSurfacePath(
            BlockPos accessStart,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            Set<BlockPos> hatchColumns,
            BlockPos shaftOrigin,
            int placedRadius,
            ShelterFacade facade
    ) {
        ChunkPos anchorChunk = new ChunkPos(shaftOrigin);
        BlockPos campCenter = new BlockPos(
                anchorChunk.getMinBlockX() + 7,
                shaftOrigin.getY(),
                anchorChunk.getMinBlockZ() + 7
        );
        if (!isReachableSurfaceColumn(
                accessStart,
                blocks,
                roles,
                hatchColumns,
                shaftOrigin,
                anchorChunk,
                campCenter,
                placedRadius,
                facade
        )) {
            return false;
        }

        Direction clockwise = Rotation.CLOCKWISE_90.rotate(facade.outward());
        List<Direction> searchOrder = List.of(
                facade.outward(),
                clockwise,
                clockwise.getOpposite(),
                facade.outward().getOpposite()
        );
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new LinkedHashSet<>();
        BlockPos start = accessStart.immutable();
        frontier.add(start);
        visited.add(start);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.removeFirst();
            if (roles.get(current) == ProspectorCampOutcropComposer.ComponentRole.PATH) {
                return true;
            }
            for (Direction direction : searchOrder) {
                BlockPos next = current.relative(direction).immutable();
                if (visited.add(next) && isReachableSurfaceColumn(
                        next,
                        blocks,
                        roles,
                        hatchColumns,
                        shaftOrigin,
                        anchorChunk,
                        campCenter,
                        placedRadius,
                        facade
                )) {
                    frontier.addLast(next);
                }
            }
        }
        return false;
    }

    private static boolean isReachableSurfaceColumn(
            BlockPos column,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            Set<BlockPos> hatchColumns,
            BlockPos shaftOrigin,
            ChunkPos anchorChunk,
            BlockPos campCenter,
            int placedRadius,
            ShelterFacade facade
    ) {
        if (!new ChunkPos(column).equals(anchorChunk)
                || Math.abs(column.getX() - campCenter.getX()) > placedRadius
                || Math.abs(column.getZ() - campCenter.getZ()) > placedRadius
                || column.equals(shaftOrigin)
                || hatchColumns.contains(column)
                || facade.columns().contains(column)) {
            return false;
        }
        ProspectorCampOutcropComposer.ComponentRole role = roles.get(column);
        BlockState existingState = blocks.get(column);
        if (role == null && existingState != null && !existingState.isAir()) {
            return false;
        }
        if (role == ProspectorCampOutcropComposer.ComponentRole.VEGETATION
                || role == ProspectorCampOutcropComposer.ComponentRole.SAMPLE) {
            BlockState standingClearance = blocks.get(column.above());
            BlockState headClearance = blocks.get(column.above(2));
            if ((standingClearance != null && !standingClearance.isAir())
                    || (headClearance != null && !headClearance.isAir())) {
                return false;
            }
        }
        // Field vegetation and samples remain one-block terrain; proving reachability must not clear them.
        return role == null
                || role == ProspectorCampOutcropComposer.ComponentRole.PATH
                || role == ProspectorCampOutcropComposer.ComponentRole.ACCESS
                || role == ProspectorCampOutcropComposer.ComponentRole.VEGETATION
                || role == ProspectorCampOutcropComposer.ComponentRole.SAMPLE;
    }

    private static List<BlockPos> offRouteColumns(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles
    ) {
        return columnsForRoles(
                roles,
                Set.of(
                        ProspectorCampOutcropComposer.ComponentRole.SAMPLE,
                        ProspectorCampOutcropComposer.ComponentRole.VEGETATION,
                        ProspectorCampOutcropComposer.ComponentRole.MARKER
                )
        );
    }

    private static Optional<BlockPos> firstColumn(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return columnsForRole(roles, role).stream().findFirst();
    }

    private static List<BlockPos> columnsForRole(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return columnsForRoles(roles, Set.of(role));
    }

    private static List<BlockPos> columnsForRoles(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            Set<ProspectorCampOutcropComposer.ComponentRole> selectedRoles
    ) {
        return roles.entrySet().stream()
                .filter(entry -> selectedRoles.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparingInt((BlockPos pos) -> pos.getX())
                        .thenComparingInt(BlockPos::getZ)
                        .thenComparingInt(BlockPos::getY))
                .toList();
    }

    private static int deterministicIndex(ProspectorCampContext context, long salt, int bound) {
        if (bound <= 0) {
            return 0;
        }
        return (int) Math.floorMod(ProspectorCampContext.mix64(context.siteSeed() ^ salt), bound);
    }

    private static void put(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> payloads,
            BlockPos pos,
            BlockState state
    ) {
        BlockPos key = pos.immutable();
        blocks.put(key, state);
        payloads.remove(key);
    }

    private static ResourceLocation lootId(String path) {
        return ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "chests/" + path);
    }

    private static boolean isRaised(ProspectorCampVisualFamily family) {
        return family == ProspectorCampVisualFamily.WETLAND || family == ProspectorCampVisualFamily.TROPICAL;
    }

    private static Block timberBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE -> Blocks.OAK_LOG;
            case CONIFER, SNOWY, ROCKY -> Blocks.SPRUCE_LOG;
            case WETLAND -> Blocks.MANGROVE_LOG;
            case TROPICAL -> Blocks.JUNGLE_LOG;
            case ARID -> Blocks.ACACIA_LOG;
            case VOLCANIC -> Blocks.DARK_OAK_LOG;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    private static Block planksBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE -> Blocks.OAK_PLANKS;
            case CONIFER, SNOWY -> Blocks.SPRUCE_PLANKS;
            case WETLAND -> Blocks.MANGROVE_PLANKS;
            case TROPICAL -> Blocks.JUNGLE_PLANKS;
            case ARID -> Blocks.ACACIA_PLANKS;
            case ROCKY, VOLCANIC -> Blocks.DARK_OAK_PLANKS;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    private static Block roofBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE, ARID -> Blocks.WHITE_WOOL;
            case CONIFER, ROCKY -> Blocks.GRAY_WOOL;
            case SNOWY -> Blocks.LIGHT_GRAY_WOOL;
            case WETLAND -> Blocks.BROWN_WOOL;
            case TROPICAL -> Blocks.YELLOW_WOOL;
            case VOLCANIC -> Blocks.BLACK_WOOL;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    private static Block rockBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE, SNOWY -> Blocks.STONE;
            case CONIFER -> Blocks.COBBLESTONE;
            case WETLAND -> Blocks.MUD_BRICKS;
            case TROPICAL -> Blocks.MOSSY_COBBLESTONE;
            case ARID -> Blocks.SANDSTONE;
            case ROCKY -> Blocks.ANDESITE;
            case VOLCANIC -> Blocks.BASALT;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    private static Block rockAccentBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE -> Blocks.ANDESITE;
            case CONIFER, WETLAND -> Blocks.MOSSY_COBBLESTONE;
            case SNOWY -> Blocks.COBBLESTONE;
            case TROPICAL, ROCKY -> Blocks.TUFF;
            case ARID -> Blocks.RED_SANDSTONE;
            case VOLCANIC -> Blocks.POLISHED_BLACKSTONE;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    private static Block decayAccentBlock(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE, CONIFER, WETLAND, TROPICAL -> Blocks.MOSSY_COBBLESTONE;
            case SNOWY -> Blocks.COBBLESTONE;
            case ARID -> Blocks.RED_SANDSTONE;
            case ROCKY -> Blocks.TUFF;
            case VOLCANIC -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS;
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes have no camp palette");
        };
    }

    public record Composition(
            ProspectorCampVisualFamily visualFamily,
            AbandonedProspectorCampState abandonedState,
            ProspectorCampQualitySpec qualitySpec,
            Rotation rotation,
            BlockPos shaftOrigin,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> blockEntityPayloads,
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> reservedSurfaceColumns,
            Set<BlockPos> biomeGeometryBlocks,
            Optional<BlockPos> openRaisedShelterHeadroomBay,
            BlockPos shelterEntrance
    ) {
        public Composition {
            Objects.requireNonNull(visualFamily, "visualFamily");
            Objects.requireNonNull(abandonedState, "abandonedState");
            Objects.requireNonNull(qualitySpec, "qualitySpec");
            Objects.requireNonNull(rotation, "rotation");
            shaftOrigin = Objects.requireNonNull(shaftOrigin, "shaftOrigin").immutable();
            final int shaftOriginY = shaftOrigin.getY();
            blocks = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(blocks, "blocks")));
            blockEntityPayloads = Collections.unmodifiableMap(new LinkedHashMap<>(
                    Objects.requireNonNull(blockEntityPayloads, "blockEntityPayloads")
            ));
            reservedSurfaceColumns = Collections.unmodifiableMap(new LinkedHashMap<>(
                    Objects.requireNonNull(reservedSurfaceColumns, "reservedSurfaceColumns")
            ));
            biomeGeometryBlocks = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(
                    biomeGeometryBlocks,
                    "biomeGeometryBlocks"
            )));
            openRaisedShelterHeadroomBay = Objects.requireNonNull(
                    openRaisedShelterHeadroomBay,
                    "openRaisedShelterHeadroomBay"
            ).map(BlockPos::immutable);
            shelterEntrance = Objects.requireNonNull(shelterEntrance, "shelterEntrance").immutable();
            if (!blocks.keySet().containsAll(blockEntityPayloads.keySet())) {
                throw new IllegalArgumentException("Every abandoned-camp payload requires a planned block");
            }
            ChunkPos anchorChunk = new ChunkPos(shaftOrigin);
            if (blocks.keySet().stream().anyMatch(pos -> !new ChunkPos(pos).equals(anchorChunk))) {
                throw new IllegalArgumentException("Abandoned-camp blocks must remain in the anchor chunk");
            }
            if (blocks.entrySet().stream().anyMatch(entry -> {
                int relativeY = entry.getKey().getY() - shaftOriginY;
                return relativeY < -1 || relativeY >= qualitySpec.maxHeight();
            })) {
                throw new IllegalArgumentException("Abandoned-camp blocks exceed the quality height envelope");
            }
            BlockState entranceFeet = blocks.get(shelterEntrance.above(isRaised(visualFamily) ? 1 : 0));
            BlockState entranceHead = blocks.get(shelterEntrance.above(isRaised(visualFamily) ? 2 : 1));
            if (entranceFeet == null || !entranceFeet.isAir() || entranceHead == null || !entranceHead.isAir()) {
                throw new IllegalArgumentException("Abandoned-camp shelter entrance is not two blocks clear");
            }
        }

        public long lootContainerCount() {
            return blockEntityPayloads.values().stream()
                    .filter(ExpeditionBlockEntityPayload::hasLootTable)
                    .count();
        }

        public long domumBlockEntityCount() {
            return blockEntityPayloads.values().stream()
                    .filter(ExpeditionBlockEntityPayload::hasMaterialBlocks)
                    .count();
        }
    }

    record ShelterAccessCandidate(BlockPos entrance, BlockPos access) {
        ShelterAccessCandidate {
            entrance = Objects.requireNonNull(entrance, "entrance").immutable();
            access = Objects.requireNonNull(access, "access").immutable();
            int horizontalDistance = Math.abs(entrance.getX() - access.getX())
                    + Math.abs(entrance.getZ() - access.getZ());
            if (entrance.getY() != access.getY() || horizontalDistance != 1) {
                throw new IllegalArgumentException("Shelter access must be cardinally adjacent to its entrance");
            }
        }
    }

    record ShelterFacade(
            List<BlockPos> columns,
            Direction outward,
            List<ShelterAccessCandidate> accessCandidates
    ) {
        ShelterFacade {
            columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
            final List<BlockPos> resolvedColumns = columns;
            Objects.requireNonNull(outward, "outward");
            accessCandidates = List.copyOf(Objects.requireNonNull(accessCandidates, "accessCandidates"));
            if (columns.isEmpty() || accessCandidates.isEmpty()) {
                throw new IllegalArgumentException("Shelter facade requires columns and access candidates");
            }
            if (accessCandidates.stream().anyMatch(candidate -> !resolvedColumns.contains(candidate.entrance()))) {
                throw new IllegalArgumentException("Shelter access candidate entrance must belong to the facade");
            }
        }
    }

    private record Bounds(int minX, int maxX, int minZ, int maxZ) {
        private static Bounds of(List<BlockPos> positions) {
            if (positions.isEmpty()) {
                throw new IllegalArgumentException("Cannot measure empty shelter bounds");
            }
            return new Bounds(
                    positions.stream().mapToInt(BlockPos::getX).min().orElseThrow(),
                    positions.stream().mapToInt(BlockPos::getX).max().orElseThrow(),
                    positions.stream().mapToInt(BlockPos::getZ).min().orElseThrow(),
                    positions.stream().mapToInt(BlockPos::getZ).max().orElseThrow()
            );
        }

        private boolean inQuadrant(BlockPos pos, int quadrant) {
            int middleX = (minX + maxX) / 2;
            int middleZ = (minZ + maxZ) / 2;
            return switch (quadrant) {
                case 0 -> pos.getX() <= middleX && pos.getZ() <= middleZ;
                case 1 -> pos.getX() >= middleX && pos.getZ() <= middleZ;
                case 2 -> pos.getX() <= middleX && pos.getZ() >= middleZ;
                default -> pos.getX() >= middleX && pos.getZ() >= middleZ;
            };
        }
    }
}
