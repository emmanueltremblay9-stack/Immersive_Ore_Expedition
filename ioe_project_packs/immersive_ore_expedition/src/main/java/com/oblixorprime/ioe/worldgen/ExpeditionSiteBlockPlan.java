package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ExpeditionSiteBlockPlan(
        ResourceLocation requestedFeatureId,
        BlockPos anchorPos,
        BlockPos connectorEnd,
        BlockPos chamberCenter,
        SiteQuality quality,
        ResourceLocation oreBlockId,
        List<ResourceLocation> generatedComponents,
        Map<BlockPos, BlockState> blocks
) {
    public ExpeditionSiteBlockPlan {
        Objects.requireNonNull(requestedFeatureId, "requestedFeatureId");
        Objects.requireNonNull(anchorPos, "anchorPos");
        Objects.requireNonNull(connectorEnd, "connectorEnd");
        Objects.requireNonNull(chamberCenter, "chamberCenter");
        Objects.requireNonNull(quality, "quality");
        generatedComponents = List.copyOf(Objects.requireNonNull(generatedComponents, "generatedComponents"));
        blocks = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(blocks, "blocks")));
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("Expedition site block plans must contain at least one block");
        }
        if (quality.isProductive() && oreBlockId == null
                && generatedComponents.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)) {
            throw new IllegalArgumentException("Productive chamber plans require a loaded ore block id");
        }
    }

    public long oreBlockCount() {
        if (oreBlockId == null) {
            return 0L;
        }
        return blocks.values().stream()
                .filter(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(oreBlockId))
                .count();
    }

    public boolean isConnectedExpeditionSite() {
        return generatedComponents.contains(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR)
                && generatedComponents.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)
                && anchorPos.distManhattan(chamberCenter) >= IoeWorldgenConfig.oreLoadMinDistanceFromAnchor()
                && anchorPos.distManhattan(chamberCenter) <= IoeWorldgenConfig.oreLoadMaxDistanceFromAnchor();
    }
}
