package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.budding.BuddingSitePlan;
import net.minecraft.core.BlockPos;

final class IronBuddingSitePlans {
    private IronBuddingSitePlans() { }
    static ExpeditionSiteBlockPlan plan(ExpeditionSiteType type, BlockPos origin, BuddingSitePlan budget,
                                       long shapeSeed, ProspectorCampContext context) {
        return GeOreBuddingSitePlans.plan(BuddingResourceFamily.GEORE_IRON, type, origin, budget, shapeSeed, context);
    }
}
