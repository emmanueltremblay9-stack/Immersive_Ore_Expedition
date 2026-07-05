package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public final class SurfaceCluePlacementPlanner {
    private final IeMineralOutcropFeature outcropFeature;
    private final IpReservoirSeepFeature seepFeature;
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public SurfaceCluePlacementPlanner() {
        this(
                new IeMineralOutcropFeature(),
                new IpReservoirSeepFeature(),
                LoadedResourceScanner.runtime(),
                new ResourcePolicyService()
        );
    }

    public SurfaceCluePlacementPlanner(
            IeMineralOutcropFeature outcropFeature,
            IpReservoirSeepFeature seepFeature,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        this.outcropFeature = Objects.requireNonNull(outcropFeature, "outcropFeature");
        this.seepFeature = Objects.requireNonNull(seepFeature, "seepFeature");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    public SurfaceCluePlacementPlan planMineralOutcropClue(
            IeMineralDepositRef deposit,
            BlockPos origin,
            int requestedBoulderCount
    ) {
        return planMineralOutcropClue(
                deposit,
                origin,
                requestedBoulderCount,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public SurfaceCluePlacementPlan planMineralOutcropClue(
            IeMineralDepositRef deposit,
            BlockPos origin,
            int requestedBoulderCount,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        ResourceRef candidate = firstDepositResource(deposit).orElse(null);
        SurfaceCluePlacementPlan.ClueType clueType = requestedBoulderCount <= 1
                ? SurfaceCluePlacementPlan.ClueType.IE_MINERAL_BOULDER
                : SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP;
        SurfaceCluePlacementPlan rejected = validateMineralOutcropRequest(
                clueType,
                deposit,
                candidate,
                origin,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, anchorType);
        }

        Optional<MineralOutcropPlan> plan = outcropFeature.planOutcropClue(
                Objects.requireNonNull(deposit, "deposit"),
                requestedBoulderCount,
                scanner,
                policyService
        );
        if (plan.isEmpty()) {
            return withContext(skipRejectedMineralOutcropPlan(clueType, deposit, candidate, origin),
                    biomeId, provinceId, anchorType);
        }

        MineralOutcropPlan outcropPlan = plan.get();
        SurfaceCluePlacementPlan.ClueMetadata metadata = new SurfaceCluePlacementPlan.ClueMetadata(
                1.0D,
                outcropPlan.boulderCount(),
                false,
                false,
                outcropPlan.rendersFullDeposit()
        );
        SurfaceCluePlacementPlan.ClueType allowedClueType = outcropPlan.boulderCount() <= 1
                ? SurfaceCluePlacementPlan.ClueType.IE_MINERAL_BOULDER
                : SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP;
        return SurfaceCluePlacementPlan.allowed(
                allowedClueType,
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                outcropPlan.clueResource(),
                origin,
                biomeId,
                provinceId,
                anchorType,
                metadata
        );
    }

    public SurfaceCluePlacementPlan planReservoirSeepClue(
            IpReservoirRef reservoir,
            BlockPos origin
    ) {
        return planReservoirSeepClue(
                reservoir,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public SurfaceCluePlacementPlan planReservoirSeepClue(
            IpReservoirRef reservoir,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        ResourceRef fluid = reservoir == null ? null : reservoir.fluid();
        SurfaceCluePlacementPlan.ClueType clueType = reservoir != null && reservoir.gasLike()
                ? SurfaceCluePlacementPlan.ClueType.IP_GAS_VENT
                : SurfaceCluePlacementPlan.ClueType.IP_SEEP;
        SurfaceCluePlacementPlan rejected = SurfaceCluePlacementRules.validateDirect(
                SurfaceCluePlacementPlan.SourceSystem.IP_RESERVOIR_SEEP,
                clueType,
                fluid,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, anchorType);
        }

        Optional<ReservoirSeepPlan> plan = seepFeature.planSeepClue(
                Objects.requireNonNull(reservoir, "reservoir"),
                scanner,
                policyService
        );
        if (plan.isEmpty()) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IP_RESERVOIR_SEEP,
                    fluid,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    anchorType,
                    null
            );
        }

        ReservoirSeepPlan seepPlan = plan.get();
        SurfaceCluePlacementPlan.ClueType allowedClueType = resolveReservoirClueType(seepPlan);
        SurfaceCluePlacementPlan.ClueMetadata metadata = new SurfaceCluePlacementPlan.ClueMetadata(
                1.0D,
                seepPlan.maxSurfaceFluidBlocks(),
                seepPlan.pocketLake(),
                seepPlan.vent(),
                seepPlan.rendersFullReservoir()
        );
        return SurfaceCluePlacementPlan.allowed(
                allowedClueType,
                SurfaceCluePlacementPlan.SourceSystem.IP_RESERVOIR_SEEP,
                seepPlan.clueFluid(),
                origin,
                biomeId,
                provinceId,
                anchorType,
                metadata
        );
    }

    public SurfaceCluePlacementPlan planSurfaceClue(
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            SurfaceCluePlacementPlan.ClueType clueType,
            ResourceRef clueResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            SurfaceCluePlacementPlan.ClueMetadata clueMetadata,
            IoeWorldgenPlacementGates placementGates
    ) {
        SurfaceCluePlacementPlan rejected = SurfaceCluePlacementRules.validateDirect(
                sourceSystem,
                clueType,
                clueResource,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, anchorType);
        }
        if (clueMetadata == null) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    sourceSystem,
                    clueResource,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    anchorType,
                    null
            );
        }
        return SurfaceCluePlacementPlan.allowed(
                clueType,
                sourceSystem,
                clueResource,
                origin,
                biomeId,
                provinceId,
                anchorType,
                clueMetadata
        );
    }

    private static Optional<ResourceRef> firstDepositResource(IeMineralDepositRef deposit) {
        if (deposit == null) {
            return Optional.empty();
        }
        return deposit.presentResources().stream().findFirst();
    }

    private SurfaceCluePlacementPlan validateMineralOutcropRequest(
            SurfaceCluePlacementPlan.ClueType clueType,
            IeMineralDepositRef deposit,
            ResourceRef candidate,
            BlockPos origin,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                    candidate,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT,
                    null,
                    null,
                    null,
                    null
            );
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                    candidate,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_RUNTIME_DISABLED,
                    SurfaceCluePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                    null,
                    null,
                    null,
                    null
            );
        }
        if (origin == null) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                    candidate,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.NULL_ORIGIN,
                    null,
                    null,
                    null,
                    null
            );
        }
        if (deposit == null || deposit.presentResources().isEmpty()) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                    candidate,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.NULL_RESOURCE,
                    null,
                    null,
                    null,
                    null
            );
        }
        if (!ProspectingCompatGates.immersiveEngineeringEnabled(scanner)) {
            return SurfaceCluePlacementPlan.skipped(
                    clueType,
                    SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                    candidate,
                    origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT,
                    SurfaceCluePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    private SurfaceCluePlacementPlan skipRejectedMineralOutcropPlan(
            SurfaceCluePlacementPlan.ClueType clueType,
            IeMineralDepositRef deposit,
            ResourceRef fallbackResource,
            BlockPos origin
    ) {
        for (ResourceRef resource : deposit.presentResources()) {
            if (resource.type() != ResourceType.BLOCK) {
                return skippedMineralOutcrop(clueType, resource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_INVALID_CLUE_TYPE,
                        SurfaceCluePlacementPlan.SkipReason.INVALID_CLUE_TYPE);
            }
            if (policyService.isExcludedResource(resource.id())) {
                return skippedMineralOutcrop(clueType, resource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                        SurfaceCluePlacementPlan.SkipReason.STRICT_EXCLUSION);
            }
            if (!scanner.isPresent(resource)) {
                return skippedMineralOutcrop(clueType, resource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
            }
            ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
            if (decision.shouldSkip()) {
                return skippedMineralOutcrop(clueType, resource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
            }
            if (!decision.shouldUse()) {
                return skippedMineralOutcrop(clueType, resource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
            }
        }
        return skippedMineralOutcrop(clueType, fallbackResource, origin,
                SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT);
    }

    private static SurfaceCluePlacementPlan skippedMineralOutcrop(
            SurfaceCluePlacementPlan.ClueType clueType,
            ResourceRef resource,
            BlockPos origin,
            SurfaceCluePlacementPlan.Decision decision,
            SurfaceCluePlacementPlan.SkipReason skipReason
    ) {
        return SurfaceCluePlacementPlan.skipped(
                clueType,
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                resource,
                origin,
                decision,
                skipReason,
                null,
                null,
                null,
                null
        );
    }

    private static SurfaceCluePlacementPlan.ClueType resolveReservoirClueType(ReservoirSeepPlan seepPlan) {
        if (seepPlan.vent()) {
            return SurfaceCluePlacementPlan.ClueType.IP_GAS_VENT;
        }
        if (seepPlan.pocketLake()) {
            return SurfaceCluePlacementPlan.ClueType.IP_POCKET_LAKE;
        }
        return SurfaceCluePlacementPlan.ClueType.IP_SEEP;
    }

    private static SurfaceCluePlacementPlan withContext(
            SurfaceCluePlacementPlan plan,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType
    ) {
        return SurfaceCluePlacementPlan.skipped(
                plan.clueType(),
                plan.sourceSystem(),
                plan.clueResource(),
                plan.origin(),
                plan.decision(),
                plan.skipReason(),
                biomeId,
                provinceId,
                anchorType,
                plan.clueMetadata().orElse(null)
        );
    }
}
