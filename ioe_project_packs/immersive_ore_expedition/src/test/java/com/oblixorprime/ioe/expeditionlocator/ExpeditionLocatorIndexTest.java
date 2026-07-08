package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.worldgen.ExpeditionAnchorPlacementPlan;
import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionLocatorIndexTest {
    @Test
    void nearestSelectsClosestSameKindSite() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite distant = anchor("collapsed_shaft", new BlockPos(20, 64, 0));
        ExpeditionSite nearby = anchor("tiny_vertical_mine_entrance", new BlockPos(4, 64, 0));
        index.record(distant);
        index.record(nearby);

        ExpeditionLocatorResult result = index.nearest(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                ExpeditionSiteKind.ANCHOR
        );

        assertTrue(result.found());
        assertEquals(nearby, result.site().orElseThrow());
        assertEquals(4L, result.distanceBlocks().orElseThrow());
    }

    @Test
    void nearestFiltersToSameDimension() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite netherSite = ExpeditionSite.anchor(
                Level.NETHER,
                new BlockPos(1, 64, 0),
                id("tiny_vertical_mine_entrance"),
                null,
                SiteQuality.NORMAL,
                "test"
        );
        ExpeditionSite overworldSite = anchor("collapsed_shaft", new BlockPos(20, 64, 0));
        index.record(netherSite);
        index.record(overworldSite);

        ExpeditionLocatorResult result = index.nearest(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                ExpeditionSiteKind.ANCHOR
        );

        assertTrue(result.found());
        assertEquals(overworldSite, result.site().orElseThrow());
    }

    @Test
    void nearestUsesDeterministicTieBreaks() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite positiveX = anchor("miner_camp", new BlockPos(5, 64, 0));
        ExpeditionSite negativeX = anchor("buried_survey_marker", new BlockPos(-5, 64, 0));
        index.record(positiveX);
        index.record(negativeX);

        ExpeditionLocatorResult result = index.nearest(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                ExpeditionSiteKind.ANCHOR
        );

        assertTrue(result.found());
        assertEquals(negativeX, result.site().orElseThrow());
    }

    @Test
    void nearestReturnsExplicitNoResultWhenNoSitesAreIndexed() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();

        ExpeditionLocatorResult result = index.nearest(
                Level.OVERWORLD,
                BlockPos.ZERO,
                ExpeditionSiteKind.ANCHOR
        );

        assertFalse(result.found());
        assertEquals(ExpeditionLocatorResult.Status.NO_INDEXED_SITES, result.status());
        assertTrue(result.site().isEmpty());
        assertTrue(result.distanceBlocks().isEmpty());
    }

    @Test
    void foundResultRejectsSentinelDistanceThroughPublicConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new ExpeditionLocatorResult(
                ExpeditionLocatorResult.Status.FOUND,
                Optional.of(anchor("tiny_vertical_mine_entrance", BlockPos.ZERO)),
                -1L
        ));
    }

    @Test
    void noIndexedSitesResultRejectsRealDistanceThroughPublicConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new ExpeditionLocatorResult(
                ExpeditionLocatorResult.Status.NO_INDEXED_SITES,
                Optional.empty(),
                0L
        ));
    }

    @Test
    void placedProofRecordsAnchorAndProvinceOnlyWhenGroundedByResult() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ResourceLocation provinceId = id("granite_belt");
        RuntimeWorldgenPlacementProofResult placed = placedProofResult(provinceId);

        index.recordPlacedProof(Level.OVERWORLD, placed);

        ExpeditionLocatorResult anchor = index.nearest(
                Level.OVERWORLD,
                new BlockPos(6, 64, 8),
                ExpeditionSiteKind.ANCHOR
        );
        ExpeditionLocatorResult province = index.nearest(
                Level.OVERWORLD,
                new BlockPos(6, 64, 8),
                ExpeditionSiteKind.PROVINCE
        );

        assertTrue(anchor.found());
        assertTrue(province.found());
        assertEquals(id("tiny_vertical_mine_entrance"), anchor.site().orElseThrow().anchorId().orElseThrow());
        assertEquals(provinceId, province.site().orElseThrow().provinceId().orElseThrow());
        assertEquals(ExpeditionLocatorIndex.RUNTIME_PLACEMENT_PROOF_SOURCE, anchor.site().orElseThrow().source().orElseThrow());
        assertEquals(2, index.size());
    }

    @Test
    void placedProofWithoutProvinceDoesNotInventProvinceSite() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();

        index.recordPlacedProof(Level.OVERWORLD, placedProofResult(null));

        assertTrue(index.nearest(Level.OVERWORLD, BlockPos.ZERO, ExpeditionSiteKind.ANCHOR).found());
        assertFalse(index.nearest(Level.OVERWORLD, BlockPos.ZERO, ExpeditionSiteKind.PROVINCE).found());
        assertEquals(1, index.size());
    }

    @Test
    void placedProofWithoutAnchorTypeDoesNotRecordAnonymousAnchorSite() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        RuntimeWorldgenPlacementProofResult malformedPlacedProof = new RuntimeWorldgenPlacementProofResult(
                null,
                new BlockPos(6, 64, 8),
                SiteQuality.RICH,
                Optional.of(ResourceRef.block("minecraft", "amethyst_block")),
                true,
                true,
                RuntimeWorldgenPlacementProofResult.SkipReason.NONE,
                Optional.empty(),
                Optional.empty(),
                false
        );

        index.recordPlacedProof(Level.OVERWORLD, malformedPlacedProof);

        assertEquals(0, index.size());
        assertFalse(index.nearest(Level.OVERWORLD, BlockPos.ZERO, ExpeditionSiteKind.ANCHOR).found());
    }

    @Test
    void unplacedSitesStayAvailableOnlyForDiagnostics() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite planned = ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(4, 64, 0),
                id("tiny_vertical_mine_entrance"),
                null,
                SiteQuality.NORMAL,
                "catalog_debug",
                ExpeditionSitePlacementState.PLANNED,
                "not placed"
        );

        index.record(planned);

        assertEquals(0, index.size());
        assertTrue(index.sites().isEmpty());
        assertEquals(planned, index.diagnosticSites().getFirst());
        assertFalse(index.nearestAny(Level.OVERWORLD, BlockPos.ZERO).found());
    }

    @Test
    void placementDisabledProofDoesNotCreateGameplayTarget() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        RuntimeWorldgenPlacementProofResult skipped = new RuntimeWorldgenPlacementProofResult(
                id("tiny_vertical_mine_entrance"),
                new BlockPos(6, 64, 8),
                SiteQuality.RICH,
                Optional.of(ResourceRef.block("minecraft", "amethyst_block")),
                false,
                false,
                RuntimeWorldgenPlacementProofResult.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                Optional.of(ExpeditionAnchorPlacementPlan.skipped(
                        id("tiny_vertical_mine_entrance"),
                        new BlockPos(6, 64, 8),
                        SiteQuality.RICH,
                        ExpeditionAnchorPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                        null,
                        id("granite_belt")
                )),
                Optional.empty(),
                false
        );

        index.recordPlacedProof(Level.OVERWORLD, skipped);

        assertEquals(0, index.size());
        assertTrue(index.sites().isEmpty());
        assertTrue(index.diagnosticSites().isEmpty());
    }

    private static ExpeditionSite anchor(String path, BlockPos pos) {
        return ExpeditionSite.anchor(
                Level.OVERWORLD,
                pos,
                id(path),
                null,
                SiteQuality.NORMAL,
                "test"
        );
    }

    private static RuntimeWorldgenPlacementProofResult placedProofResult(ResourceLocation provinceId) {
        BlockPos pos = new BlockPos(6, 64, 8);
        ExpeditionAnchorPlacementPlan plan = ExpeditionAnchorPlacementPlan.allowed(
                id("tiny_vertical_mine_entrance"),
                pos,
                SiteQuality.RICH,
                null,
                provinceId
        );
        return new RuntimeWorldgenPlacementProofResult(
                id("tiny_vertical_mine_entrance"),
                pos,
                SiteQuality.RICH,
                Optional.of(ResourceRef.block("minecraft", "amethyst_block")),
                true,
                true,
                RuntimeWorldgenPlacementProofResult.SkipReason.NONE,
                Optional.of(plan),
                Optional.empty(),
                false
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", path);
    }
}
