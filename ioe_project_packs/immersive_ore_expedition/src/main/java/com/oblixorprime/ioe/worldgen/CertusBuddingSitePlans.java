package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingSitePlan;
import com.oblixorprime.ioe.budding.NativeCertusBudding;
import net.minecraft.core.BlockPos;

final class CertusBuddingSitePlans {
    private CertusBuddingSitePlans() { }

    static ExpeditionSiteBlockPlan plan(ExpeditionSiteType type, BlockPos origin, BuddingSitePlan budget,
            long shapeSeed, ProspectorCampContext context) {
        if (!NativeCertusBudding.available()) throw new IllegalStateException("Unavailable native Certus resources");
        return CanonicalBuddingSitePlans.plan(NativeCertusBudding.QUARTZ, NativeCertusBudding::block,
                type, origin, budget, shapeSeed, context);
    }
}
