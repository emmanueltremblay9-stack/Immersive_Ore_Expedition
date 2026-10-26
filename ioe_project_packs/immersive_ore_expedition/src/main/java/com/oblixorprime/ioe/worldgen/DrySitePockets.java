package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Finite floor residues share the site's placement journal; they never create a budding node. */
final class DrySitePockets {
    private DrySitePockets() { }

    static ExpeditionSiteBlockPlan attach(ExpeditionSiteBlockPlan shape, ResourceLocation oreId, int count, long siteSeed) {
        if (shape.quality() != SiteQuality.DRY || count < 0 || count > 5) {
            throw new IllegalArgumentException("DRY pockets require 0..5 total residues");
        }
        if (count == 0) return shape;
        var ore = BuiltInRegistries.BLOCK.getOptional(oreId).orElseThrow(() ->
                new IllegalStateException("Missing DRY pocket resource " + oreId));
        var blocks = new LinkedHashMap<>(shape.blocks());
        var center = shape.chamberCenter();
        var candidates = new ArrayList<BlockPos>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0 || x * x + z * z > 4) continue;
                BlockPos pos = center.offset(x, -1, z);
                var state = blocks.get(pos);
                if (blocks.getOrDefault(pos.above(), Blocks.STONE.defaultBlockState()).isAir()
                        && (state == null || state.isAir()) && !shape.blockEntityPayloads().containsKey(pos)) {
                    candidates.add(pos);
                }
            }
        }
        var random = RandomSource.create(siteSeed ^ 0x504f434b4554L);
        for (int i = candidates.size() - 1; i > 0; i--) java.util.Collections.swap(candidates, i, random.nextInt(i + 1));
        if (candidates.size() < count) throw new IllegalStateException("DRY chamber cannot fit its residual budget");
        candidates.stream().limit(count).forEach(pos -> blocks.put(pos, ore.defaultBlockState()));
        return new ExpeditionSiteBlockPlan(shape.requestedFeatureId(), shape.anchorPos(), shape.connectorEnd(),
                center, shape.quality(), 0, oreId, null, shape.roomCenters(), shape.generatedComponents(),
                blocks, shape.blockEntityPayloads());
    }
}
