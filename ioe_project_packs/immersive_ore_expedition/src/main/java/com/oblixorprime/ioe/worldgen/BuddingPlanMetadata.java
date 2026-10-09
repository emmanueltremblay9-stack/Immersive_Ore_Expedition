package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.budding.BuddingNodeInfo;
import com.oblixorprime.ioe.budding.BuddingBlockIdentity;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

final class BuddingPlanMetadata {
    private BuddingPlanMetadata() { }

    static List<BuddingNodeInfo> nodes(ExpeditionSiteBlockPlan plan) {
        var hearts = plan.blocks().entrySet().stream()
                .filter(entry -> BuddingBlockIdentity.isCanonical(entry.getValue().getBlock())).toList();
        if (hearts.isEmpty()) return List.of();
        int orePerNode = Math.toIntExact(plan.oreBlockCount() - hearts.size()) / hearts.size();
        List<BuddingNodeInfo> nodes = new ArrayList<>();
        for (int i = 0; i < hearts.size(); i++) {
            nodes.add(new BuddingNodeInfo(hearts.get(i).getKey(), i + 1, hearts.size(), orePerNode,
                    BuddingBlockIdentity.of(hearts.get(i).getValue().getBlock()).orElseThrow().family()));
        }
        return List.copyOf(nodes);
    }
}
