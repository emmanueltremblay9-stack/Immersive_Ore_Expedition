package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AmethystGrowthSiteProviderTest {
    private final AmethystGrowthSiteProvider provider = new AmethystGrowthSiteProvider();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void plansStructureAnchoredAmethystSiteForLoadedVanillaResource() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalGrowthTestScanner scanner = scanner(Set.of(amethyst));

        Optional<CrystalGrowthSitePlan> plan = provider.planSite(anchor(), amethyst, scanner, policy);

        assertTrue(plan.isPresent());
        assertEquals(CrystalGrowthSiteType.AMETHYST, plan.get().siteType());
        assertEquals(amethyst, plan.get().coreResource());
        assertTrue(plan.get().structureAnchored());
        assertFalse(plan.get().randomFreeSite());
        assertFalse(plan.get().meteoriticVariant());
        assertFalse(plan.get().hasOuterCrust());
    }

    @Test
    void skipsMissingAmethystInsteadOfPlanningFallbackCrystalCave() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");

        Optional<CrystalGrowthSitePlan> plan = provider.planSite(anchor(), amethyst, scanner(Set.of()), policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsApprovedLoadedNonAmethystResourceAsAmethystCore() {
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");

        Optional<CrystalGrowthSitePlan> plan = provider.planSite(anchor(), diamond, scanner(Set.of(diamond)), policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsLoadedAmethystItemAsAmethystSiteCore() {
        ResourceRef shard = ResourceRef.item("minecraft", "amethyst_shard");

        Optional<CrystalGrowthSitePlan> plan = provider.planSite(anchor(), shard, scanner(Set.of(shard)), policy);

        assertTrue(plan.isEmpty());
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, new BlockPos(0, 64, 0), "crystal_growth_chamber", SiteQuality.NORMAL);
    }

    private static CrystalGrowthTestScanner scanner(Set<ResourceRef> resources) {
        return new CrystalGrowthTestScanner(Set.of(), resources);
    }
}
