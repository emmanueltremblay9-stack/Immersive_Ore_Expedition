package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AmethystGrowthSiteProvider implements CrystalGrowthSiteProvider {
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public AmethystGrowthSiteProvider() {
        this(LoadedResourceScanner.runtime(), new ResourcePolicyService());
    }

    public AmethystGrowthSiteProvider(LoadedResourceScanner scanner, ResourcePolicyService policyService) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    @Override
    public boolean isAvailable() {
        return IoeCrystalGrowthConfig.enabled() && IoeCrystalGrowthConfig.amethystStructureAnchoredSites();
    }

    @Override
    public Optional<CrystalGrowthSitePlan> planSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!isAvailable() || !CrystalGrowthSiteRules.canPlanAnchoredSite(anchor)) {
            return Optional.empty();
        }

        ResourcePolicyDecision decision = policyService.evaluate(coreResource, scanner);
        if (!decision.shouldUse()) {
            return Optional.empty();
        }

        return Optional.of(new CrystalGrowthSitePlan(
                CrystalGrowthSiteType.AMETHYST,
                anchor,
                coreResource,
                Optional.empty(),
                true,
                false,
                IoeCrystalGrowthConfig.amethystMeteoriteWrappedVariant(),
                false,
                List.of(),
                List.of()
        ));
    }

    public Optional<CrystalGrowthSitePlan> planAmethystSite(ExpeditionAnchorRef anchor, ResourceRef coreResource) {
        return planSite(anchor, coreResource, scanner, policyService);
    }

    @Override
    public boolean canGenerateAt(WorldGenLevel level, BlockPos anchorPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        return false;
    }

    @Override
    public boolean generate(WorldGenLevel level, BlockPos anchorPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        IoeCrystalGrowthMod.LOGGER.debug("Skipping direct amethyst site placement at {}; alpha planning is enabled but configured-feature placement is not registered.", anchorPos);
        return false;
    }
}
