package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreLoadGeneratorTest {
    private final OreLoadGenerator generator = new OreLoadGenerator();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void createsPlanForApprovedLoadedResourceWithinAnchorRange() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                scannerWithLoaded(resource),
                policy
        );

        assertTrue(plan.isPresent());
        assertEquals(SiteQuality.NORMAL, plan.get().quality());
        assertEquals(16, plan.get().distanceFromAnchor());
        assertTrue(plan.get().requiresTunnelConnection());
    }

    @Test
    void skipsMissingResourcesInsteadOfPlanningFallbackOre() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.block("minecraft", "diamond_ore");

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(32, 64, 0),
                scannerWithNothingLoaded(),
                policy
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsOreLoadCentersOutsideConfiguredDistanceWindow() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        Optional<OreLoadPlan> tooClose = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(1, 64, 0),
                scannerWithLoaded(resource),
                policy
        );
        Optional<OreLoadPlan> tooFar = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(256, 64, 0),
                scannerWithLoaded(resource),
                policy
        );

        assertTrue(tooClose.isEmpty());
        assertTrue(tooFar.isEmpty());
    }

    @Test
    void rejectsUnknownStructureAnchorTypes() {
        ExpeditionAnchorRef anchor = new ExpeditionAnchorRef(Level.OVERWORLD, new BlockPos(0, 64, 0), "random_spot", SiteQuality.NORMAL);
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                scannerWithLoaded(resource),
                policy
        );

        assertTrue(plan.isEmpty());
        assertFalse(ExpeditionStructureRegistry.isEnabledStructureId("random_spot"));
    }

    @Test
    void rejectsLoadedItemResourcesInsteadOfPlanningOreLoadBlocks() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.item("minecraft", "diamond");

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                scannerWithLoaded(resource),
                policy
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsDirectPlansWithUnknownStructureAnchors() {
        ExpeditionAnchorRef anchor = new ExpeditionAnchorRef(Level.OVERWORLD, new BlockPos(0, 64, 0), "random_spot", SiteQuality.NORMAL);
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        assertThrows(IllegalArgumentException.class, () -> new OreLoadPlan(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                SiteQuality.NORMAL,
                true,
                16
        ));
    }

    @Test
    void rejectsDirectPlansWithMismatchedAnchorDistance() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        assertThrows(IllegalArgumentException.class, () -> new OreLoadPlan(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                SiteQuality.NORMAL,
                true,
                32
        ));
    }

    @Test
    void rejectsDirectPlansOutsideConfiguredDistanceWindow() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.block("minecraft", "iron_ore");

        assertThrows(IllegalArgumentException.class, () -> new OreLoadPlan(
                anchor,
                resource,
                new BlockPos(1, 64, 0),
                SiteQuality.NORMAL,
                true,
                1
        ));
    }

    @Test
    void rejectsDirectPlansWithNonOreLoadResourceTypes() {
        ExpeditionAnchorRef anchor = anchorAt(new BlockPos(0, 64, 0));
        ResourceRef resource = ResourceRef.item("minecraft", "diamond");

        assertThrows(IllegalArgumentException.class, () -> new OreLoadPlan(
                anchor,
                resource,
                new BlockPos(16, 64, 0),
                SiteQuality.NORMAL,
                true,
                16
        ));
    }

    private static ExpeditionAnchorRef anchorAt(BlockPos pos) {
        return new ExpeditionAnchorRef(Level.OVERWORLD, pos, "tiny_vertical_mine_entrance", SiteQuality.NORMAL);
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(loaded);
    }

    private static LoadedResourceScanner scannerWithNothingLoaded() {
        return new TestScanner(null);
    }

    private record TestScanner(ResourceRef loaded) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loaded != null && loaded.type().name().equals("BLOCK") && loaded.id().equals(id);
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loaded != null && loaded.type().name().equals("ITEM") && loaded.id().equals(id);
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return false;
        }
    }
}
