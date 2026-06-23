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
            return false;
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
