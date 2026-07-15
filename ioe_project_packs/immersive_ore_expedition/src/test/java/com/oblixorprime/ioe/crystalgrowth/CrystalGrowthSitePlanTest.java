package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CrystalGrowthSitePlanTest {
    @Test
    void rejectsDirectAmethystPlansWithItemCore() {
        assertThrows(IllegalArgumentException.class, () -> plan(
                CrystalGrowthSiteType.AMETHYST,
                ResourceRef.item("minecraft", "amethyst_shard"),
                Optional.empty(),
                false,
                false
        ));
    }

    @Test
    void rejectsDirectAe2PlansWithItemCertusCore() {
        assertThrows(IllegalArgumentException.class, () -> plan(
                CrystalGrowthSiteType.AE2_CERTUS,
                ResourceRef.item("ae2", "certus_quartz_crystal"),
                Optional.of(ResourceRef.block("ae2", "sky_stone_block")),
                true,
                false
        ));
    }

    @Test
    void rejectsDirectGeOrePlansWithItemCore() {
        assertThrows(IllegalArgumentException.class, () -> plan(
                CrystalGrowthSiteType.GEORE,
                ResourceRef.item("geore", "diamond_geore"),
                Optional.empty(),
                false,
                true
        ));
    }

    @Test
    void acceptsValidMeteoriticCertusPlanShape() {
        assertDoesNotThrow(() -> plan(
                CrystalGrowthSiteType.AE2_CERTUS,
                ResourceRef.block("ae2", "flawless_budding_quartz"),
                Optional.of(ResourceRef.block("ae2", "sky_stone_block")),
                true,
                false
        ));
    }

    private static CrystalGrowthSitePlan plan(
            CrystalGrowthSiteType siteType,
            ResourceRef coreResource,
            Optional<ResourceRef> outerCrustResource,
            boolean meteoriticVariant,
            boolean disablesFreeGeOreWorldgen
    ) {
        return new CrystalGrowthSitePlan(
                siteType,
                anchor(),
                coreResource,
                outerCrustResource,
                true,
                false,
                meteoriticVariant,
                disablesFreeGeOreWorldgen,
                List.of(ResourcePolicyDecision.skip("test skip")),
                List.of(ResourcePolicyDecision.reject("test reject"))
        );
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, BlockPos.ZERO, "crystal_growth_chamber", SiteQuality.NORMAL);
    }
}
