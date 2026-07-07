package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreLoadChamberBlockPlacementApplierTest {
    private static final ResourceRef IRON = ResourceRef.block("minecraft", "iron_ore");

    private final OreLoadChamberBlockPlacementApplier applier = new OreLoadChamberBlockPlacementApplier();

    @Test
    void skippedPlanReturnsNoPlacements() {
        OreLoadChamberBlockPlacementPlan skippedPlan = OreLoadChamberBlockPlacementPlan.skipped(
                null,
                OreLoadChamberBlockPlacementPlan.SkipReason.NULL_CHAMBER_PLAN
        );

        OreLoadChamberBlockPlacementResult result = applier.apply(skippedPlan, new TestPlacementTarget());

        assertEquals(0, result.candidateCount());
        assertEquals(0, result.placedCount());
        assertEquals(0, result.skippedCount());
        assertFalse(result.anyPlaced());
        assertEquals(OreLoadChamberBlockPlacementResult.SkipReason.PLAN_NOT_READY, result.skipReason());
    }

    @Test
    void unsafeReplacementIsSkippedWithoutPlacement() {
        BlockPos targetPos = new BlockPos(1, 64, 1);
        TestPlacementTarget target = new TestPlacementTarget()
                .withPlacementResource(IRON)
                .writable(targetPos)
                .withCandidate(targetPos, candidate("bedrock", false, false, false));

        OreLoadChamberBlockPlacementResult result = applier.apply(planWithTargets(targetPos), target);

        assertEquals(1, result.candidateCount());
        assertEquals(0, result.placedCount());
        assertEquals(1, result.skippedCount());
        assertEquals(1, result.skippedFor(OreLoadChamberBlockPlacementResult.SkipReason.TARGET_NOT_REPLACEABLE));
        assertTrue(target.writes().isEmpty());
    }

    @Test
    void safeReplacementIsCountedAsPlaced() {
        BlockPos targetPos = new BlockPos(2, 64, 2);
        TestPlacementTarget target = new TestPlacementTarget()
                .withPlacementResource(IRON)
                .writable(targetPos)
                .withCandidate(targetPos, candidate("stone", false, false, false));

        OreLoadChamberBlockPlacementResult result = applier.apply(planWithTargets(targetPos), target);

        assertEquals(1, result.candidateCount());
        assertEquals(1, result.placedCount());
        assertEquals(0, result.skippedCount());
        assertTrue(result.anyPlaced());
        assertEquals(List.of(targetPos), target.writes());
    }

    @Test
    void mixedPlanReportsPlacedAndSkippedCountsDeterministically() {
        BlockPos safePos = new BlockPos(3, 64, 3);
        BlockPos unsafePos = new BlockPos(4, 64, 4);
        BlockPos failedWritePos = new BlockPos(5, 64, 5);
        BlockPos outsideWriteRegionPos = new BlockPos(6, 64, 6);
        TestPlacementTarget target = new TestPlacementTarget()
                .withPlacementResource(IRON)
                .writable(safePos)
                .writable(unsafePos)
                .writable(failedWritePos)
                .withCandidate(safePos, candidate("stone", false, false, false))
                .withCandidate(unsafePos, candidate("stone", false, true, false))
                .withCandidate(failedWritePos, candidate("deepslate", false, false, false))
                .failWrite(failedWritePos);

        OreLoadChamberBlockPlacementResult result = applier.apply(
                planWithTargets(safePos, unsafePos, failedWritePos, outsideWriteRegionPos),
                target
        );

        assertEquals(4, result.candidateCount());
        assertEquals(1, result.placedCount());
        assertEquals(3, result.skippedCount());
        assertEquals(1, result.skippedFor(OreLoadChamberBlockPlacementResult.SkipReason.TARGET_NOT_REPLACEABLE));
        assertEquals(1, result.skippedFor(OreLoadChamberBlockPlacementResult.SkipReason.WORLD_WRITE_FAILED));
        assertEquals(1, result.skippedFor(OreLoadChamberBlockPlacementResult.SkipReason.TARGET_OUTSIDE_WRITE_REGION));
        assertEquals(List.of(safePos, failedWritePos), target.writes());
    }

    @Test
    void missingPlacementResourceIsSkippedBeforeReplacementInspection() {
        BlockPos targetPos = new BlockPos(7, 64, 7);
        TestPlacementTarget target = new TestPlacementTarget()
                .writable(targetPos)
                .withCandidate(targetPos, candidate("stone", false, false, false));

        OreLoadChamberBlockPlacementResult result = applier.apply(planWithTargets(targetPos), target);

        assertEquals(1, result.candidateCount());
        assertEquals(0, result.placedCount());
        assertEquals(1, result.skippedFor(OreLoadChamberBlockPlacementResult.SkipReason.TARGET_RESOURCE_MISSING));
        assertTrue(target.writes().isEmpty());
        assertTrue(target.inspections().isEmpty());
    }

    private static OreLoadChamberBlockPlacementPlan planWithTargets(BlockPos... positions) {
        List<OreLoadChamberBlockPlacementPlan.OreBlockTarget> targets = new ArrayList<>();
        for (BlockPos pos : positions) {
            targets.add(new OreLoadChamberBlockPlacementPlan.OreBlockTarget(pos, IRON));
        }
        return OreLoadChamberBlockPlacementPlan.ready(allowedChamberPlan(), targets);
    }

    private static OreLoadChamberPlacementPlan allowedChamberPlan() {
        ExpeditionAnchorRef anchor = new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                SiteQuality.NORMAL
        );
        OreLoadPlan oreLoadPlan = new OreLoadPlan(
                anchor,
                IRON,
                new BlockPos(16, 64, 0),
                SiteQuality.NORMAL,
                true,
                anchor.pos().distManhattan(new BlockPos(16, 64, 0))
        );
        return OreLoadChamberPlacementPlan.allowed(
                oreLoadPlan,
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                null,
                null,
                OreLoadChamberPlacementRules.defaults().metadataFor(oreLoadPlan).orElseThrow()
        );
    }

    private static OreLoadChamberBlockPlacementApplier.ReplacementCandidate candidate(
            String path,
            boolean air,
            boolean containsFluid,
            boolean hasBlockEntity
    ) {
        return new OreLoadChamberBlockPlacementApplier.ReplacementCandidate(
                ResourceLocation.fromNamespaceAndPath("minecraft", path),
                air,
                containsFluid,
                hasBlockEntity
        );
    }

    private static final class TestPlacementTarget implements OreLoadChamberBlockPlacementApplier.PlacementTarget {
        private final Set<BlockPos> writablePositions = new HashSet<>();
        private final Set<ResourceLocation> placementResources = new HashSet<>();
        private final Map<BlockPos, OreLoadChamberBlockPlacementApplier.ReplacementCandidate> candidates =
                new HashMap<>();
        private final Set<BlockPos> failedWrites = new HashSet<>();
        private final List<BlockPos> inspections = new ArrayList<>();
        private final List<BlockPos> writes = new ArrayList<>();

        private TestPlacementTarget writable(BlockPos pos) {
            writablePositions.add(pos);
            return this;
        }

        private TestPlacementTarget withPlacementResource(ResourceRef resource) {
            placementResources.add(resource.id());
            return this;
        }

        private TestPlacementTarget withCandidate(
                BlockPos pos,
                OreLoadChamberBlockPlacementApplier.ReplacementCandidate candidate
        ) {
            candidates.put(pos, candidate);
            return this;
        }

        private TestPlacementTarget failWrite(BlockPos pos) {
            failedWrites.add(pos);
            return this;
        }

        private List<BlockPos> writes() {
            return List.copyOf(writes);
        }

        private List<BlockPos> inspections() {
            return List.copyOf(inspections);
        }

        @Override
        public boolean canWrite(BlockPos pos) {
            return writablePositions.contains(pos);
        }

        @Override
        public boolean hasPlacementResource(ResourceRef resource) {
            return placementResources.contains(resource.id());
        }

        @Override
        public OreLoadChamberBlockPlacementApplier.ReplacementCandidate replacementCandidate(BlockPos pos) {
            inspections.add(pos);
            return candidates.get(pos);
        }

        @Override
        public boolean place(BlockPos pos, ResourceRef resource) {
            writes.add(pos);
            return !failedWrites.contains(pos);
        }
    }
}
