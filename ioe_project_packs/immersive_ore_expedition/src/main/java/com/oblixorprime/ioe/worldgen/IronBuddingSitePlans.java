package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingSitePlan;
import com.oblixorprime.ioe.budding.IoeIronBuddingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;

/** Resolves the validated Iron family into the existing transactional block-plan format. */
final class IronBuddingSitePlans {
    private IronBuddingSitePlans() {
    }

    static ExpeditionSiteBlockPlan plan(ExpeditionSiteType type, BlockPos origin, BuddingSitePlan budget,
                                       long shapeSeed, ProspectorCampContext context) {
        if (!budget.quality().isProductive()) {
            throw new IllegalArgumentException("DRY placement requires an explicit residual-pocket policy");
        }
        ResourceLocation oreId = ResourceLocation.parse("geore:iron_block");
        Block ore = BuiltInRegistries.BLOCK.getOptional(oreId).orElseThrow();
        Block ordinaryHeart = IoeIronBuddingBlocks.block(budget.nodeRanks().stream()
                .filter(rank -> rank != BuddingRank.FLAWLESS).findFirst().orElseThrow());
        ExpeditionSiteBlockPlan shape = ExpeditionSiteBlueprints.plan(type, origin, budget.quality(),
                oreId, ore.defaultBlockState(), BuiltInRegistries.BLOCK.getKey(ordinaryHeart), ordinaryHeart.defaultBlockState(),
                null, null, null, budget.totalOreBlocks() + budget.nodeRanks().size(), budget.nodeRanks().size(), 0,
                RandomSource.create(shapeSeed), context);
        LinkedHashMap<BlockPos, BlockState> blocks = new LinkedHashMap<>(shape.blocks());
        int node = 0;
        for (var entry : blocks.entrySet()) {
            if (entry.getValue().is(ordinaryHeart)) {
                entry.setValue(IoeIronBuddingBlocks.block(budget.nodeRanks().get(node++)).defaultBlockState());
            }
        }
        long oreCount = blocks.values().stream().filter(state -> state.is(ore)).count();
        if (node != budget.nodeRanks().size() || oreCount != budget.totalOreBlocks()) {
            throw new IllegalStateException("Iron geometry does not preserve the canonical budget");
        }
        return new ExpeditionSiteBlockPlan(shape.requestedFeatureId(), shape.anchorPos(), shape.connectorEnd(),
                shape.chamberCenter(), shape.quality(), node, shape.oreBlockId(), shape.oreNodeHeartBlockId(),
                shape.roomCenters(), shape.generatedComponents(), blocks, shape.blockEntityPayloads());
    }
}
