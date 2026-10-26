package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;

/** Adds one underground reward cache to the same compensated block plan as the site. */
final class DrySiteRewards {
    static final ResourceLocation LOOT = ResourceLocation.parse("immersive_ore_expedition:worldgen/dry_resonating_seed");

    private DrySiteRewards() {
    }

    static ExpeditionSiteBlockPlan attach(ExpeditionSiteBlockPlan plan, boolean selected, long lootSeed) {
        if (!selected) return plan;
        if (plan.quality() != SiteQuality.DRY) {
            throw new IllegalArgumentException("Only a DRY site may carry its neutral-seed reward");
        }
        BlockPos cache = plan.chamberCenter().below();
        if (!plan.blocks().getOrDefault(cache.above(), Blocks.STONE.defaultBlockState()).isAir()
                || !plan.blocks().getOrDefault(cache, Blocks.STONE.defaultBlockState()).is(Blocks.CALCITE)) {
            throw new IllegalStateException("DRY reward requires the accessible chamber-floor marker");
        }
        var blocks = new LinkedHashMap<>(plan.blocks());
        var payloads = new LinkedHashMap<>(plan.blockEntityPayloads());
        blocks.put(cache, Blocks.CHEST.defaultBlockState());
        payloads.put(cache, ExpeditionBlockEntityPayload.loot(LOOT, lootSeed));
        return new ExpeditionSiteBlockPlan(plan.requestedFeatureId(), plan.anchorPos(), plan.connectorEnd(),
                plan.chamberCenter(), plan.quality(), plan.oreNodeCount(), plan.oreBlockId(), plan.oreNodeHeartBlockId(),
                plan.roomCenters(), plan.generatedComponents(), blocks, payloads);
    }
}
