package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class IeMineralOutcropFeature {
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public IeMineralOutcropFeature() {
        this(LoadedResourceScanner.runtime(), new ResourcePolicyService());
    }

    public IeMineralOutcropFeature(LoadedResourceScanner scanner, ResourcePolicyService policyService) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    public boolean placeOutcropClue(WorldGenLevel level, BlockPos nearSurfacePos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(nearSurfacePos, "nearSurfacePos");
        IoeIeipProspectingMod.LOGGER.debug(
                "Skipping direct IE outcrop placement at {}; alpha planning is enabled but IE mineral placement hooks are not registered.",
                nearSurfacePos
        );
        return false;
    }

    public Optional<MineralOutcropPlan> planOutcropClue(IeMineralDepositRef deposit, int requestedBoulderCount) {
        return planOutcropClue(deposit, requestedBoulderCount, scanner, policyService);
    }

    public Optional<MineralOutcropPlan> planOutcropClue(
            IeMineralDepositRef deposit,
            int requestedBoulderCount,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(deposit, "deposit");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!ProspectingCompatGates.immersiveEngineeringEnabled(scanner)
                || !IoeIeipProspectingConfig.surfaceOutcropsEnabled()
                || deposit.presentResources().isEmpty()) {
            return Optional.empty();
        }

        List<ResourcePolicyDecision> skipped = new ArrayList<>();
        List<ResourcePolicyDecision> rejected = new ArrayList<>();
        for (ResourceRef resource : deposit.presentResources()) {
            if (!canRenderAsSurfaceBlock(resource)) {
                rejected.add(ResourcePolicyDecision.reject("IE outcrop clues require a concrete block resource: " + resource.id()));
                continue;
            }

            ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
            if (decision.shouldUse()) {
                return Optional.of(new MineralOutcropPlan(
                        deposit.id(),
                        resource,
                        clampBoulderCount(requestedBoulderCount),
                        IoeIeipProspectingConfig.freeOreRewardLimitBlocks(),
                        IoeIeipProspectingConfig.useDepositPresentResourcesOnly(),
                        false,
                        skipped,
                        rejected
                ));
            }
            if (decision.shouldSkip()) {
                skipped.add(decision);
            } else {
                rejected.add(decision);
            }
        }

        return Optional.empty();
    }

    private static boolean canRenderAsSurfaceBlock(ResourceRef resource) {
        return resource.type() == ResourceType.BLOCK;
    }

    private static int clampBoulderCount(int requestedBoulderCount) {
        int min = IoeIeipProspectingConfig.boulderCountMin();
        int max = IoeIeipProspectingConfig.boulderCountMax();
        return Math.max(min, Math.min(max, requestedBoulderCount));
    }
}
