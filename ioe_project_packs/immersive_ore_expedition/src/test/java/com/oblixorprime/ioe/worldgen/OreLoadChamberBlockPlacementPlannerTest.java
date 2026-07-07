package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreLoadChamberBlockPlacementPlannerTest {
    private final OreLoadChamberBlockPlacementPlanner planner = new OreLoadChamberBlockPlacementPlanner();

    @Test
    void allowedConcreteBlockChamberProducesDeterministicCandidateTargets() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        OreLoadChamberPlacementPlan chamberPlan = allowedChamberPlan(iron, SiteQuality.NORMAL);

        OreLoadChamberBlockPlacementPlan blockPlan = planner.planBlockPlacements(chamberPlan);

        assertTrue(blockPlan.placementReady());
        assertEquals(OreLoadChamberBlockPlacementPlan.SkipReason.NONE, blockPlan.skipReason());
        assertFalse(blockPlan.oreTargets().isEmpty());
        assertTrue(blockPlan.oreTargets().stream().allMatch(target -> iron.equals(target.resource())));
        assertTrue(blockPlan.oreTargets().stream().anyMatch(target -> chamberPlan.chamberCenter().equals(target.pos())));
        assertEquals(blockPlan.oreTargets().size(), uniquePositions(blockPlan).size());

        OreLoadChamberPlacementPlan.ChamberMetadata metadata = chamberPlan.chamberMetadata().orElseThrow();
        assertTrue(blockPlan.oreTargets().size() <= metadata.approximateVolume());
        for (OreLoadChamberBlockPlacementPlan.OreBlockTarget target : blockPlan.oreTargets()) {
            assertTrue(Math.abs(target.pos().getX() - chamberPlan.chamberCenter().getX()) <= metadata.horizontalRadius());
            assertTrue(Math.abs(target.pos().getY() - chamberPlan.chamberCenter().getY()) <= metadata.verticalHalfSize());
            assertTrue(Math.abs(target.pos().getZ() - chamberPlan.chamberCenter().getZ()) <= metadata.horizontalRadius());
        }
    }

    @Test
    void skippedChamberPlanNeverBecomesBlockPlacementReady() {
        OreLoadChamberPlacementPlan skippedChamberPlan = OreLoadChamberPlacementPlan.skipped(
                null,
                null,
                OreLoadChamberPlacementPlan.SkipReason.NULL_ORE_LOAD_PLAN,
                null,
                null,
                null
        );

        OreLoadChamberBlockPlacementPlan blockPlan = planner.planBlockPlacements(skippedChamberPlan);

        assertFalse(blockPlan.placementReady());
        assertEquals(
                OreLoadChamberBlockPlacementPlan.SkipReason.CHAMBER_PLAN_NOT_ALLOWED,
                blockPlan.skipReason()
        );
        assertTrue(blockPlan.oreTargets().isEmpty());
    }

    @Test
    void blockTagsAreNotSubstitutedIntoConcreteOreTargets() {
        ResourceRef ironTag = ResourceRef.blockTag("c", "ores/iron");
        OreLoadChamberPlacementPlan chamberPlan = allowedChamberPlan(ironTag, SiteQuality.RICH);

        OreLoadChamberBlockPlacementPlan blockPlan = planner.planBlockPlacements(chamberPlan);

        assertFalse(blockPlan.placementReady());
        assertEquals(
                OreLoadChamberBlockPlacementPlan.SkipReason.UNSUPPORTED_RESOURCE_TYPE,
                blockPlan.skipReason()
        );
        assertTrue(blockPlan.oreTargets().isEmpty());
    }

    @Test
    void replacementRulesRejectUnsafeTargets() {
        assertFalse(OreLoadChamberReplacementRules.canReplace(null));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.AIR.defaultBlockState()));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.WATER.defaultBlockState()));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.BEDROCK.defaultBlockState()));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.BARRIER.defaultBlockState()));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.STRUCTURE_BLOCK.defaultBlockState()));
        assertFalse(OreLoadChamberReplacementRules.canReplace(Blocks.CHEST.defaultBlockState()));

        assertTrue(OreLoadChamberReplacementRules.canReplace(Blocks.STONE.defaultBlockState()));
        assertTrue(OreLoadChamberReplacementRules.canReplace(Blocks.DEEPSLATE.defaultBlockState()));
    }

    private static OreLoadChamberPlacementPlan allowedChamberPlan(ResourceRef resource, SiteQuality quality) {
        OreLoadPlan oreLoadPlan = oreLoadPlan(resource, quality);
        return OreLoadChamberPlacementPlan.allowed(
                oreLoadPlan,
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                null,
                null,
                OreLoadChamberPlacementRules.defaults().metadataFor(oreLoadPlan).orElseThrow()
        );
    }

    private static OreLoadPlan oreLoadPlan(ResourceRef resource, SiteQuality quality) {
        ExpeditionAnchorRef anchor = new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                quality
        );
        BlockPos chamberCenter = new BlockPos(16, 64, 0);
        return new OreLoadPlan(
                anchor,
                resource,
                chamberCenter,
                quality,
                true,
                anchor.pos().distManhattan(chamberCenter)
        );
    }

    private static Set<BlockPos> uniquePositions(OreLoadChamberBlockPlacementPlan blockPlan) {
        Set<BlockPos> positions = new HashSet<>();
        for (OreLoadChamberBlockPlacementPlan.OreBlockTarget target : blockPlan.oreTargets()) {
            positions.add(target.pos());
        }
        return positions;
    }
}
