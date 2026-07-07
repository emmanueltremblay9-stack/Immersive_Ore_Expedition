package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Objects;
import java.util.Optional;

public final class OreLoadGenerator {
    public boolean generateAnchoredOreLoad(WorldGenLevel level, BlockPos anchorPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        RuntimeWorldgenPlacementProofResult result = new RuntimeWorldgenPlacementProof().placeAnchorProof(
                level,
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                anchorPos,
                SiteQuality.NORMAL,
                RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE,
                LoadedResourceScanner.runtime(),
                new ResourcePolicyService(),
                IoeWorldgenPlacementGates.fromConfig()
        );
        return result.blockPlaced();
    }

    public Optional<OreLoadPlan> planAnchoredOreLoad(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            BlockPos candidateLoadCenter,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        return planAnchoredOreLoad(
                anchor,
                resource,
                candidateLoadCenter,
                null,
                scanner,
                policyService
        );
    }

    public Optional<OreLoadPlan> planAnchoredOreLoad(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            BlockPos candidateLoadCenter,
            ResourceLocation biomeId,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");
        return planAnchoredOreLoad(
                anchor,
                resource,
                candidateLoadCenter,
                biomeId,
                scanner,
                policyService,
                ProvinceRuntimeIntegration.fromConfig(policyService, scanner)
        );
    }

    Optional<OreLoadPlan> planAnchoredOreLoad(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            BlockPos candidateLoadCenter,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ProvinceRuntimeIntegration provinceRuntimeIntegration
    ) {
        return planAnchoredOreLoad(
                anchor,
                resource,
                candidateLoadCenter,
                null,
                scanner,
                policyService,
                provinceRuntimeIntegration
        );
    }

    Optional<OreLoadPlan> planAnchoredOreLoad(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            BlockPos candidateLoadCenter,
            ResourceLocation biomeId,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ProvinceRuntimeIntegration provinceRuntimeIntegration
    ) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(candidateLoadCenter, "candidateLoadCenter");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");
        Objects.requireNonNull(provinceRuntimeIntegration, "provinceRuntimeIntegration");

        if (IoeWorldgenConfig.requireStructureAnchorForMajorOreLoads()
                && !ExpeditionStructureRegistry.isEnabledStructureId(anchor.anchorType())) {
            return Optional.empty();
        }

        int distance = anchor.pos().distManhattan(candidateLoadCenter);
        AnchorRule anchorRule = AnchorRule.fromConfig();
        if (!anchorRule.isDistanceAllowed(distance)) {
            return Optional.empty();
        }
        if (!canUseOreLoadResource(resource)) {
            return Optional.empty();
        }

        ResourcePolicyDecision decision = provinceRuntimeIntegration.enabled()
                ? provinceRuntimeIntegration.evaluateOreLoadResource(anchor, resource, biomeId)
                : policyService.evaluate(resource, scanner);
        if (!decision.shouldUse()) {
            return Optional.empty();
        }

        return Optional.of(new OreLoadPlan(
                anchor,
                resource,
                candidateLoadCenter,
                anchor.quality(),
                anchorRule.requireTunnelConnection(),
                distance
        ));
    }

    static boolean canUseOreLoadResource(ResourceRef resource) {
        Objects.requireNonNull(resource, "resource");
        return resource.type() == ResourceType.BLOCK || resource.type() == ResourceType.BLOCK_TAG;
    }
}
