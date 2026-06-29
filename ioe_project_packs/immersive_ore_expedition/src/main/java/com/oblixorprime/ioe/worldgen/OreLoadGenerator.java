package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Objects;
import java.util.Optional;

public final class OreLoadGenerator {
    public boolean generateAnchoredOreLoad(WorldGenLevel level, BlockPos anchorPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        IoeExpeditionWorldgenMod.LOGGER.debug("Skipping direct ore placement at {}; alpha planning layer is enabled but configured-feature placement is not yet registered.", anchorPos);
        return false;
    }

    public Optional<OreLoadPlan> planAnchoredOreLoad(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            BlockPos candidateLoadCenter,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(candidateLoadCenter, "candidateLoadCenter");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

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

        ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
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
