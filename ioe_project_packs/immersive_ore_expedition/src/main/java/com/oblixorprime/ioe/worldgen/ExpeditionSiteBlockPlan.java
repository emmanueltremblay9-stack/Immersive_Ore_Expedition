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
        int oreNodeCount,
        ResourceLocation oreBlockId,
        ResourceLocation oreNodeHeartBlockId,
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
        if (oreNodeCount < 0) {
            throw new IllegalArgumentException("Ore node count must not be negative");
        }
        boolean hasOreLoadChamber = generatedComponents.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER);
        boolean hasAe2Geode = generatedComponents.contains(IoeWorldgenFeatureKeys.METEORITIC_AE2_GEODE);
        boolean hasEntroizedFluixGeode = generatedComponents.contains(IoeWorldgenFeatureKeys.ENTROIZED_FLUIX_GEODE);
        boolean hasSpecialGeode = hasAe2Geode || hasEntroizedFluixGeode;
        if (hasAe2Geode && hasEntroizedFluixGeode) {
            throw new IllegalArgumentException("A mine cannot contain more than one special geode mode");
        }
        if ((oreBlockId == null) != (oreNodeHeartBlockId == null)) {
            throw new IllegalArgumentException("Embedded ore nodes require both material and budding-heart ids");
        }
        if (oreNodeCount > 0 && oreBlockId == null) {
            throw new IllegalArgumentException("Embedded ore-node counts require loaded resource ids");
        }
        if (hasSpecialGeode && (oreBlockId != null || oreNodeHeartBlockId != null || oreNodeCount != 0)) {
            throw new IllegalArgumentException("Special geodes and GeOre node-geodes are mutually exclusive");
        }
        if (!quality.isProductive() && oreNodeCount != 0) {
            throw new IllegalArgumentException("Dry chamber plans cannot contain ore nodes");
        }
    }

    public long oreBlockCount() {
        if (oreBlockId == null) {
            return 0L;
        }
        return blocks.values().stream()
                .map(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()))
                .filter(id -> id.equals(oreBlockId) || id.equals(oreNodeHeartBlockId))
                .count();
    }

    public long oreNodeHeartCount() {
        if (oreNodeHeartBlockId == null) {
            return 0L;
        }
        if (oreNodeHeartBlockId.equals(oreBlockId)) {
            return oreNodeCount;
        }
        return blocks.values().stream()
                .filter(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(oreNodeHeartBlockId))
                .count();
    }

    public boolean isConnectedExpeditionSite() {
        return generatedComponents.contains(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR)
                && generatedComponents.contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER)
                && anchorPos.distManhattan(chamberCenter) >= IoeWorldgenConfig.oreLoadMinDistanceFromAnchor()
                && anchorPos.distManhattan(chamberCenter) <= IoeWorldgenConfig.oreLoadMaxDistanceFromAnchor();
    }
}
