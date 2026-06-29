package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Objects;
import java.util.Optional;

public final class IpReservoirSeepFeature {
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public IpReservoirSeepFeature() {
        this(LoadedResourceScanner.runtime(), new ResourcePolicyService());
    }

    public IpReservoirSeepFeature(LoadedResourceScanner scanner, ResourcePolicyService policyService) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    public boolean placeSeepClue(WorldGenLevel level, BlockPos nearSurfacePos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(nearSurfacePos, "nearSurfacePos");
        IoeIeipProspectingMod.LOGGER.debug(
                "Skipping direct IP seep placement at {}; alpha planning is enabled but IP reservoir placement hooks are not registered.",
                nearSurfacePos
        );
        return false;
    }

    public Optional<ReservoirSeepPlan> planSeepClue(IpReservoirRef reservoir) {
        return planSeepClue(reservoir, scanner, policyService);
    }

    public Optional<ReservoirSeepPlan> planSeepClue(
            IpReservoirRef reservoir,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(reservoir, "reservoir");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!ProspectingCompatGates.immersivePetroleumEnabled(scanner)
                || !IoeIeipProspectingConfig.surfaceSeepsEnabled()) {
            return Optional.empty();
        }

        ResourcePolicyDecision decision = evaluateReservoirFluid(reservoir.fluid(), scanner, policyService);
        if (!decision.shouldUse()) {
            if (decision.shouldSkip()) {
                policyService.logMissingResource(reservoir.fluid(), decision.reason());
            }
            return Optional.empty();
        }

        boolean vent = reservoir.gasLike() && IoeIeipProspectingConfig.ventForGasLikeReservoirs();
        boolean pocketLake = !reservoir.gasLike() && IoeIeipProspectingConfig.smallSurfacePocketLakes();
        return Optional.of(new ReservoirSeepPlan(
                reservoir.id(),
                reservoir.fluid(),
                IoeIeipProspectingConfig.maxSurfaceFluidBlocks(),
                pocketLake,
                vent,
                false
        ));
    }

    private static ResourcePolicyDecision evaluateReservoirFluid(
            ResourceRef fluid,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        if (fluid.type() != ResourceType.FLUID) {
            return ResourcePolicyDecision.reject("IP reservoir clues require a concrete fluid resource: " + fluid.id());
        }
        if (!ProspectingCompatGates.IMMERSIVE_PETROLEUM.equals(fluid.id().getNamespace())) {
            return ResourcePolicyDecision.reject("IP reservoir clues require an Immersive Petroleum fluid: " + fluid.id());
        }
        if (policyService.isExcludedResource(fluid.id())) {
            return ResourcePolicyDecision.reject("IP reservoir fluid is explicitly excluded by IOE policy: " + fluid.id());
        }
        if (!scanner.isPresent(fluid)) {
            return policyService.shouldSkipMissing(fluid.id())
                    ? ResourcePolicyDecision.skip("IP reservoir fluid is approved by reservoir context but not loaded: " + fluid.id())
                    : ResourcePolicyDecision.reject("IP reservoir fluid is approved by reservoir context but not loaded: " + fluid.id());
        }
        return ResourcePolicyDecision.use("IP reservoir fluid passed runtime availability checks: " + fluid.id());
    }
}
