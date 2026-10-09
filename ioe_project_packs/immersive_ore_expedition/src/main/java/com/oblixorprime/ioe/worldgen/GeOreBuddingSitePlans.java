package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingSitePlan;
import com.oblixorprime.ioe.budding.IoeGeOreBuddingBlocks;
import net.minecraft.core.BlockPos;


/** Resolves the validated GeOre family into the existing transactional block-plan format. */
final class GeOreBuddingSitePlans {
    private GeOreBuddingSitePlans() {
    }

    static ExpeditionSiteBlockPlan plan(com.oblixorprime.ioe.budding.BuddingResourceFamily family, ExpeditionSiteType type, BlockPos origin, BuddingSitePlan budget,
                                       long shapeSeed, ProspectorCampContext context) {
        if (!budget.quality().isProductive()) {
            throw new IllegalArgumentException("DRY placement requires an explicit residual-pocket policy");
        }
        if (!IoeGeOreBuddingBlocks.available(family)) throw new IllegalStateException("Unavailable Budding family: " + family);
        return CanonicalBuddingSitePlans.plan(family.pocketBlockId(), rank -> IoeGeOreBuddingBlocks.block(family, rank),
                type, origin, budget, shapeSeed, context);
    }
}
