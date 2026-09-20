package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionSiteFeatureCollisionTest {
    private static final BlockPos ORIGIN = new BlockPos(4, 90, 6);

    @Test
    void expandsSyntheticPlannedPositionsByExactlyOneBlock() {
        BoundingBox bounds = ExpeditionSiteFeature.expandedPlanBounds(
                List.of(new BlockPos(10, 64, 10), new BlockPos(18, 69, 16))
        );

        assertEquals(List.of(9, 63, 9, 19, 70, 17), List.of(
                bounds.minX(), bounds.minY(), bounds.minZ(),
                bounds.maxX(), bounds.maxY(), bounds.maxZ()
        ));
    }

    @Test
    void detectsOneBlockOverlap() {
        Collection<BlockPos> planned = List.of(new BlockPos(10, 64, 10), new BlockPos(18, 69, 16));

        assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(
                planned,
                List.of(new BoundingBox(18, 68, 14, 22, 72, 20))
        ));
    }

    @Test
    void rejectsBoundaryFaceAdjacency() {
        Collection<BlockPos> planned = List.of(new BlockPos(10, 64, 10), new BlockPos(18, 69, 16));

        assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(
                planned,
                List.of(new BoundingBox(19, 64, 12, 22, 67, 15))
        ));
    }

    @Test
    void allowsOneEmptyBlockSeparation() {
        Collection<BlockPos> planned = List.of(new BlockPos(10, 64, 10), new BlockPos(18, 69, 16));

        assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(
                planned,
                List.of(new BoundingBox(20, 64, 12, 22, 67, 15))
        ));
    }

    @Test
    void activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure() {
        ProspectorCampContext activeContext = new ProspectorCampContext(
                ProspectorCampVisualFamily.TEMPERATE,
                6L,
                false,
                ProspectorCampArchetype.ACTIVE
        );
        ProspectorCampOutcropComposer.Composition active = ProspectorCampOutcropComposer.compose(
                ORIGIN,
                SiteQuality.NORMAL,
                activeContext
        );
        Collection<BlockPos> positions = active.blocks().keySet();
        BlockPos high = positions.stream().max((left, right) -> Integer.compare(left.getY(), right.getY()))
                .orElseThrow();
        BlockPos lateral = positions.stream().max((left, right) -> Integer.compare(left.getX(), right.getX()))
                .orElseThrow();

        assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(
                positions,
                List.of(new BoundingBox(high.getX(), high.getY(), high.getZ(),
                        high.getX(), high.getY(), high.getZ()))
        ));
        assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(
                positions,
                List.of(new BoundingBox(lateral.getX() + 2, lateral.getY(), lateral.getZ(),
                        lateral.getX() + 2, lateral.getY(), lateral.getZ()))
        ));
    }

    @Test
    void abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure() {
        ProspectorCampContext abandonedContext = new ProspectorCampContext(
                ProspectorCampVisualFamily.TEMPERATE,
                6L,
                false,
                ProspectorCampArchetype.ABANDONED
        );
        AbandonedProspectorCampComposer.Composition abandoned = AbandonedProspectorCampComposer.compose(
                ORIGIN,
                SiteQuality.NORMAL,
                abandonedContext
        );
        Collection<BlockPos> positions = abandoned.blocks().keySet();
        BlockPos lateral = positions.stream().max((left, right) -> Integer.compare(left.getX(), right.getX()))
                .orElseThrow();
        BlockPos rear = positions.stream().min((left, right) -> Integer.compare(left.getZ(), right.getZ()))
                .orElseThrow();

        assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(
                positions,
                List.of(new BoundingBox(lateral.getX(), lateral.getY(), lateral.getZ(),
                        lateral.getX(), lateral.getY(), lateral.getZ()))
        ));
        assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(
                positions,
                List.of(new BoundingBox(rear.getX(), rear.getY(), rear.getZ() - 2,
                        rear.getX(), rear.getY(), rear.getZ() - 2))
        ));
    }

    @Test
    void collisionHelperDoesNotMutateInputs() {
        ArrayList<BlockPos> positions = new ArrayList<>(List.of(
                new BlockPos(1, 2, 3),
                new BlockPos(4, 5, 6)
        ));
        ArrayList<BoundingBox> bounds = new ArrayList<>(List.of(new BoundingBox(8, 2, 3, 9, 5, 6)));
        List<BlockPos> positionsBefore = List.copyOf(positions);
        List<BoundingBox> boundsBefore = List.copyOf(bounds);

        assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(positions, bounds));
        assertEquals(positionsBefore, positions);
        assertEquals(boundsBefore, bounds);
    }

    @Test
    void rejectsEmptyPlannedPositions() {
        assertThrows(IllegalArgumentException.class, () ->
                ExpeditionSiteFeature.intersectsStructureBounds(List.of(), List.of()));
    }
}
