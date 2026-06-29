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

final class Ae2CertusSiteProviderTest {
    private final Ae2CertusSiteProvider provider = new Ae2CertusSiteProvider();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void plansBuriedCertusSiteWhenAe2AndSuppliedResourcesAreLoaded() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus, crust));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), certus, Optional.of(crust), scanner, policy);

        assertTrue(plan.isPresent());
        assertEquals(CrystalGrowthSiteType.AE2_CERTUS, plan.get().siteType());
        assertEquals(certus, plan.get().coreResource());
        assertEquals(crust, plan.get().outerCrustResource().orElseThrow());
        assertTrue(plan.get().meteoriticVariant());
        assertFalse(plan.get().randomFreeSite());
    }

    @Test
    void skipsCertusSiteWhenAe2IsMissing() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(), Set.of(certus, crust));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), certus, Optional.of(crust), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsFluixInsteadOfCreatingFakeOrePlan() {
        ResourceRef fluix = ResourceRef.block("ae2", "fluix_ore");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(fluix, crust));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), fluix, Optional.of(crust), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsSkyStoneAsCertusCore() {
        ResourceRef skyStone = ResourceRef.block("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(skyStone));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), skyStone, Optional.of(skyStone), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsCertusAsSkyStoneCrust() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), certus, Optional.of(certus), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsItemFormSkyStoneAsCrust() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef skyStoneItem = ResourceRef.item("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus, skyStoneItem));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), certus, Optional.of(skyStoneItem), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsItemFormCertusAsCore() {
        ResourceRef certusItem = ResourceRef.item("ae2", "certus_quartz_crystal");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalGrowthTestScanner scanner = scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certusItem, crust));

        Optional<CrystalGrowthSitePlan> plan = provider.planCertusSite(anchor(), certusItem, Optional.of(crust), scanner, policy);

        assertTrue(plan.isEmpty());
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, new BlockPos(0, 64, 0), "buried_meteorite", SiteQuality.NORMAL);
    }

    private static CrystalGrowthTestScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new CrystalGrowthTestScanner(mods, resources);
    }
}
