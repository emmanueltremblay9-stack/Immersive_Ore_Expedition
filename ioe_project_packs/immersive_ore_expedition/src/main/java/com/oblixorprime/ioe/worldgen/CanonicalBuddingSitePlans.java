package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingSitePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashMap;
import java.util.function.Function;

/** Shared geometry and budget accounting; rank behavior stays with each owning mod. */
final class CanonicalBuddingSitePlans {
    private CanonicalBuddingSitePlans() { }

    static ExpeditionSiteBlockPlan plan(ResourceLocation oreId, Function<BuddingRank, Block> ranks,
            ExpeditionSiteType type, BlockPos origin, BuddingSitePlan budget, long shapeSeed,
            ProspectorCampContext context) {
        if (!budget.quality().isProductive()) throw new IllegalArgumentException("Productive nodes require productive quality");
        Block ore = BuiltInRegistries.BLOCK.getOptional(oreId).orElseThrow();
        Block ordinaryHeart = ranks.apply(budget.nodeRanks().stream()
                .filter(rank -> rank != BuddingRank.FLAWLESS).findFirst().orElseThrow());
        ExpeditionSiteBlockPlan shape = ExpeditionSiteBlueprints.plan(type, origin, budget.quality(),
                oreId, ore.defaultBlockState(), BuiltInRegistries.BLOCK.getKey(ordinaryHeart), ordinaryHeart.defaultBlockState(),
                null, null, null, budget.totalOreBlocks() + budget.nodeRanks().size(), budget.nodeRanks().size(), 0,
                RandomSource.create(shapeSeed), context);
        LinkedHashMap<BlockPos, BlockState> blocks = new LinkedHashMap<>(shape.blocks());
        int node = 0;
        for (var entry : blocks.entrySet()) {
            if (entry.getValue().is(ordinaryHeart)) {
                entry.setValue(ranks.apply(budget.nodeRanks().get(node++)).defaultBlockState());
            }
        }
        long oreCount = blocks.values().stream().filter(state -> state.is(ore)).count();
        if (node != budget.nodeRanks().size() || oreCount != budget.totalOreBlocks()) {
            throw new IllegalStateException("Canonical geometry does not preserve the canonical budget");
        }
        return new ExpeditionSiteBlockPlan(shape.requestedFeatureId(), shape.anchorPos(), shape.connectorEnd(),
                shape.chamberCenter(), shape.quality(), node, shape.oreBlockId(), shape.oreNodeHeartBlockId(),
                shape.roomCenters(), shape.generatedComponents(), blocks, shape.blockEntityPayloads());
    }
}
