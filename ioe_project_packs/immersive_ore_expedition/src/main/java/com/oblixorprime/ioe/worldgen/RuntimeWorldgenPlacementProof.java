package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public final class RuntimeWorldgenPlacementProof {
    public static final ResourceRef DEFAULT_PROOF_RESOURCE = ResourceRef.block("minecraft", "amethyst_block");

    private static final int BLOCK_UPDATE_FLAGS = 2;

    private final ExpeditionAnchorPlacementPlanner anchorPlanner;

    public RuntimeWorldgenPlacementProof() {
        this(ExpeditionAnchorPlacementPlanner.defaults());
    }

    RuntimeWorldgenPlacementProof(ExpeditionAnchorPlacementPlanner anchorPlanner) {
        this.anchorPlanner = Objects.requireNonNull(anchorPlanner, "anchorPlanner");
    }

    public RuntimeWorldgenPlacementProofResult evaluateAnchorProof(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        return evaluateAnchorProof(
                anchorType,
                origin,
                siteQuality,
                proofResource,
                scanner,
                policyService,
                null,
                null,
                placementGates,
                true
        );
    }

    public RuntimeWorldgenPlacementProofResult evaluateAnchorProof(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            IoeWorldgenPlacementGates placementGates
    ) {
        return evaluateAnchorProof(
                anchorType,
                origin,
                siteQuality,
                proofResource,
                scanner,
                policyService,
                biomeId,
                provinceId,
                placementGates,
                true
        );
    }

    public RuntimeWorldgenPlacementProofResult placeAnchorProof(
            WorldGenLevel level,
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        RuntimeWorldgenPlacementProofResult readyResult = evaluateAnchorProof(
                anchorType,
                origin,
                siteQuality,
                proofResource,
                scanner,
                policyService,
                null,
                null,
                placementGates,
                false
        );
        if (!readyResult.placementPathAllowed()) {
            return logIfRequested(readyResult);
        }
        if (level == null) {
            return logIfRequested(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.INVALID_INPUT,
                    readyResult.anchorPlan().orElse(null),
                    readyResult.resourceDecision().orElse(null),
                    readyResult.diagnosticsEnabled()
            ));
        }
        if (!level.ensureCanWrite(origin)) {
            return logIfRequested(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.TARGET_OUTSIDE_WRITE_REGION,
                    readyResult.anchorPlan().orElse(null),
                    readyResult.resourceDecision().orElse(null),
                    readyResult.diagnosticsEnabled()
            ));
        }
        if (!level.isEmptyBlock(origin)) {
            return logIfRequested(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.TARGET_NOT_REPLACEABLE,
                    readyResult.anchorPlan().orElse(null),
                    readyResult.resourceDecision().orElse(null),
                    readyResult.diagnosticsEnabled()
            ));
        }

        Block proofBlock = BuiltInRegistries.BLOCK.get(proofResource.id());
        if (!level.setBlock(origin, proofBlock.defaultBlockState(), BLOCK_UPDATE_FLAGS)) {
            return logIfRequested(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.WORLD_WRITE_FAILED,
                    readyResult.anchorPlan().orElse(null),
                    readyResult.resourceDecision().orElse(null),
                    readyResult.diagnosticsEnabled()
            ));
        }

        RuntimeWorldgenPlacementProofResult placedResult = RuntimeWorldgenPlacementProofResult.placed(readyResult);
        ExpeditionLocatorService.recordPlacedProof(level.getLevel(), placedResult);
        return logIfRequested(placedResult);
    }

    private RuntimeWorldgenPlacementProofResult evaluateAnchorProof(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            IoeWorldgenPlacementGates placementGates,
            boolean emitDiagnostics
    ) {
        boolean diagnosticsEnabled = placementGates != null && placementGates.diagnosticsEnabled();
        if (placementGates == null) {
            return finish(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.INVALID_INPUT,
                    null,
                    null,
                    false
            ), emitDiagnostics);
        }

        ExpeditionAnchorPlacementPlan anchorPlan = anchorPlanner.planAnchorPlacement(
                anchorType,
                origin,
                siteQuality,
                biomeId,
                provinceId,
                placementGates
        );
        if (!anchorPlan.placementAllowed()) {
            RuntimeWorldgenPlacementProofResult.SkipReason skipReason =
                    anchorPlan.skipReason() == ExpeditionAnchorPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED
                            ? RuntimeWorldgenPlacementProofResult.SkipReason.RUNTIME_WORLDGEN_DISABLED
                            : RuntimeWorldgenPlacementProofResult.SkipReason.ANCHOR_PLANNING_REJECTED;
            return finish(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    skipReason,
                    anchorPlan,
                    null,
                    diagnosticsEnabled
            ), emitDiagnostics);
        }

        if (proofResource == null || scanner == null || policyService == null) {
            return finish(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.INVALID_INPUT,
                    anchorPlan,
                    null,
                    diagnosticsEnabled
            ), emitDiagnostics);
        }
        if (proofResource.type() != ResourceType.BLOCK) {
            return finish(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    RuntimeWorldgenPlacementProofResult.SkipReason.UNSUPPORTED_PROOF_RESOURCE,
                    anchorPlan,
                    null,
                    diagnosticsEnabled
            ), emitDiagnostics);
        }

        ResourcePolicyDecision decision = policyService.evaluate(proofResource, scanner);
        if (!decision.shouldUse()) {
            return finish(RuntimeWorldgenPlacementProofResult.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    proofResource,
                    policySkipReason(proofResource, decision, policyService),
                    anchorPlan,
                    decision,
                    diagnosticsEnabled
            ), emitDiagnostics);
        }

        return finish(RuntimeWorldgenPlacementProofResult.ready(
                anchorType,
                origin,
                siteQuality,
                proofResource,
                anchorPlan,
                decision,
                diagnosticsEnabled
        ), emitDiagnostics);
    }

    private RuntimeWorldgenPlacementProofResult finish(
            RuntimeWorldgenPlacementProofResult result,
            boolean emitDiagnostics
    ) {
        return emitDiagnostics ? logIfRequested(result) : result;
    }

    private RuntimeWorldgenPlacementProofResult logIfRequested(RuntimeWorldgenPlacementProofResult result) {
        if (!result.diagnosticsEnabled()) {
            return result;
        }
        if (result.blockPlaced()) {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "IOE v18 runtime placement proof placed {} at {} for anchor {}",
                    result.proofResource().orElse(null),
                    result.origin(),
                    result.anchorType()
            );
        } else if (result.placementPathAllowed()) {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "IOE v18 runtime placement proof ready at {} for anchor {} using {}",
                    result.origin(),
                    result.anchorType(),
                    result.proofResource().orElse(null)
            );
        } else {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "IOE v18 runtime placement proof skipped at {} for anchor {}: {}",
                    result.origin(),
                    result.anchorType(),
                    result.skipReason()
            );
        }
        return result;
    }

    private static RuntimeWorldgenPlacementProofResult.SkipReason policySkipReason(
            ResourceRef proofResource,
            ResourcePolicyDecision decision,
            ResourcePolicyService policyService
    ) {
        if (policyService.isExcludedResource(proofResource.id())) {
            return RuntimeWorldgenPlacementProofResult.SkipReason.STRICT_EXCLUSION;
        }
        if (decision.shouldSkip()) {
            return RuntimeWorldgenPlacementProofResult.SkipReason.RESOURCE_NOT_LOADED;
        }
        return RuntimeWorldgenPlacementProofResult.SkipReason.RESOURCE_DENIED_BY_POLICY;
    }
}
