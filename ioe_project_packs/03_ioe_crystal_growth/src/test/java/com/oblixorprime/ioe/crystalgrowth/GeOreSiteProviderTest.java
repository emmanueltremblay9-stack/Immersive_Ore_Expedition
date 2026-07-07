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

final class GeOreSiteProviderTest {
    private final GeOreSiteProvider provider = new GeOreSiteProvider();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void plansLoadedApprovedGeOreOnlyWhenGeOreIsLoaded() {
        ResourceRef diamondGeOre = ResourceRef.block("geore", "diamond_geore");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of(diamondGeOre));

        Optional<CrystalGrowthSitePlan> plan = provider.planGeOreSite(anchor(), diamondGeOre, scanner, policy);

        assertTrue(plan.isPresent());
        assertEquals(CrystalGrowthSiteType.GEORE, plan.get().siteType());
        assertEquals(diamondGeOre, plan.get().coreResource());
        assertTrue(plan.get().disablesFreeGeOreWorldgen());
        assertFalse(plan.get().randomFreeSite());
    }

    @Test
    void rejectsExcludedGeOreVariantsWithoutFallback() {
        ResourceRef tinGeOre = ResourceRef.block("geore", "tin_geore");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of(tinGeOre));

        Optional<CrystalGrowthSitePlan> plan = provider.planGeOreSite(anchor(), tinGeOre, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void skipsGeOreWhenOptionalModIsMissing() {
        ResourceRef diamondGeOre = ResourceRef.block("geore", "diamond_geore");
        CrystalGrowthTestScanner scanner = scanner(Set.of(), Set.of(diamondGeOre));

        Optional<CrystalGrowthSitePlan> plan = provider.planGeOreSite(anchor(), diamondGeOre, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsApprovedLoadedNonGeOreResourceAsGeOreCore() {
        ResourceRef diamondOre = ResourceRef.block("minecraft", "diamond_ore");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of(diamondOre));

        Optional<CrystalGrowthSitePlan> plan = provider.planGeOreSite(anchor(), diamondOre, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsItemFormGeOreCore() {
        ResourceRef diamondGeOreItem = ResourceRef.item("geore", "diamond_geore");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of(diamondGeOreItem));

        Optional<CrystalGrowthSitePlan> plan = provider.planGeOreSite(anchor(), diamondGeOreItem, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void geOrePolicyRequiresAnchoringAndDisablesFreeWorldgenWhenLoaded() {
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of());
        GeOreWorldgenPolicy policy = new GeOreWorldgenPolicy(scanner);

        assertTrue(policy.shouldDisableFreeGeOreWorldgen());
        assertTrue(policy.mustAnchorAllGeOreSites());
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, new BlockPos(0, 64, 0), "geore_growth_anchor", SiteQuality.NORMAL);
    }

    private static CrystalGrowthTestScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new CrystalGrowthTestScanner(mods, resources);
    }
}
