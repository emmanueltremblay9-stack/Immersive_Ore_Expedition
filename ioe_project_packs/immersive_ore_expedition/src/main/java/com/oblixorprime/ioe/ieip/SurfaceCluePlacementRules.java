package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;

import java.util.Objects;

final class SurfaceCluePlacementRules {
    private SurfaceCluePlacementRules() {
    }

    static SurfaceCluePlacementPlan validateDirect(
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            SurfaceCluePlacementPlan.ClueType clueType,
            ResourceRef clueResource,
            BlockPos origin,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null || scanner == null || policyService == null || sourceSystem == null) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_RUNTIME_DISABLED,
                    SurfaceCluePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED);
        }
        if (origin == null) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.NULL_ORIGIN);
        }
        if (clueResource == null) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    SurfaceCluePlacementPlan.SkipReason.NULL_RESOURCE);
        }
        if (!isValidClueType(sourceSystem, clueType) || !isValidResourceType(sourceSystem, clueResource)) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_INVALID_CLUE_TYPE,
                    SurfaceCluePlacementPlan.SkipReason.INVALID_CLUE_TYPE);
        }
        if (!optionalModLoaded(sourceSystem, scanner)) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT,
                    SurfaceCluePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT);
        }
        if (policyService.isExcludedResource(clueResource.id())) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                    SurfaceCluePlacementPlan.SkipReason.STRICT_EXCLUSION);
        }
        if (sourceSystem == SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP) {
            if (!scanner.isPresent(clueResource)) {
                return skipped(clueType, sourceSystem, clueResource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
            }
            ResourcePolicyDecision decision = policyService.evaluate(clueResource, scanner);
            if (decision.shouldSkip()) {
                return skipped(clueType, sourceSystem, clueResource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
            }
            if (!decision.shouldUse()) {
                return skipped(clueType, sourceSystem, clueResource, origin,
                        SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                        SurfaceCluePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
            }
            return null;
        }

        if (!ProspectingCompatGates.IMMERSIVE_PETROLEUM.equals(clueResource.id().getNamespace())) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                    SurfaceCluePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
        }
        if (!scanner.isPresent(clueResource)) {
            return skipped(clueType, sourceSystem, clueResource, origin,
                    SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        return null;
    }

    static boolean isValidClueType(
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            SurfaceCluePlacementPlan.ClueType clueType
    ) {
        if (sourceSystem == null || clueType == null) {
            return false;
        }
        return switch (sourceSystem) {
            case IE_MINERAL_OUTCROP -> clueType == SurfaceCluePlacementPlan.ClueType.IE_MINERAL_BOULDER
                    || clueType == SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP;
            case IP_RESERVOIR_SEEP -> clueType == SurfaceCluePlacementPlan.ClueType.IP_SEEP
                    || clueType == SurfaceCluePlacementPlan.ClueType.IP_POCKET_LAKE
                    || clueType == SurfaceCluePlacementPlan.ClueType.IP_GAS_VENT;
        };
    }

    private static boolean isValidResourceType(
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            ResourceRef clueResource
    ) {
        Objects.requireNonNull(clueResource, "clueResource");
        return switch (sourceSystem) {
            case IE_MINERAL_OUTCROP -> clueResource.type() == ResourceType.BLOCK;
            case IP_RESERVOIR_SEEP -> clueResource.type() == ResourceType.FLUID;
        };
    }

    private static boolean optionalModLoaded(
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            LoadedResourceScanner scanner
    ) {
        return switch (sourceSystem) {
            case IE_MINERAL_OUTCROP -> ProspectingCompatGates.immersiveEngineeringEnabled(scanner);
            case IP_RESERVOIR_SEEP -> ProspectingCompatGates.immersivePetroleumEnabled(scanner);
        };
    }

    private static SurfaceCluePlacementPlan skipped(
            SurfaceCluePlacementPlan.ClueType clueType,
            SurfaceCluePlacementPlan.SourceSystem sourceSystem,
            ResourceRef clueResource,
            BlockPos origin,
            SurfaceCluePlacementPlan.Decision decision,
            SurfaceCluePlacementPlan.SkipReason skipReason
    ) {
        return SurfaceCluePlacementPlan.skipped(
                clueType,
                sourceSystem,
                clueResource,
                origin,
                decision,
                skipReason,
                null,
                null,
                null,
                null
        );
    }
}
