package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GeOreSiteProvider implements CrystalGrowthSiteProvider {
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public GeOreSiteProvider() {
        this(LoadedResourceScanner.runtime(), new ResourcePolicyService());
    }

    public GeOreSiteProvider(LoadedResourceScanner scanner, ResourcePolicyService policyService) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    @Override
    public boolean isAvailable() {
        return CrystalGrowthCompatGates.georeEnabled(scanner);
    }

    @Override
    public Optional<CrystalGrowthSitePlan> planSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        return planGeOreSite(anchor, coreResource, scanner, policyService);
    }

    public Optional<CrystalGrowthSitePlan> planGeOreSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!CrystalGrowthCompatGates.georeEnabled(scanner)
                || !CrystalGrowthSiteRules.canPlanAnchoredSite(anchor)
                || !IoeCrystalGrowthConfig.anchorAllGeoresToExpeditionStructures()) {
            return Optional.empty();
        }

        if (!isGeOreBlockResource(coreResource)) {
            return Optional.empty();
        }

        ResourcePolicyDecision decision = policyService.evaluate(coreResource, scanner);
        if (!decision.shouldUse()) {
            return Optional.empty();
        }

        return Optional.of(new CrystalGrowthSitePlan(
                CrystalGrowthSiteType.GEORE,
                anchor,
                coreResource,
                Optional.empty(),
                true,
                false,
                false,
                IoeCrystalGrowthConfig.disableFreeGeoreWorldgen(),
                List.of(),
                List.of()
        ));
    }

    private static boolean isGeOreBlockResource(ResourceRef resource) {
        return resource != null
                && resource.type() == ResourceType.BLOCK
                && CrystalGrowthCompatGates.GEORE.equals(resource.id().getNamespace());
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
        IoeCrystalGrowthMod.LOGGER.debug("Skipping direct GeOre site placement at {}; alpha planning is enabled but GeOre placement hooks are not registered.", anchorPos);
        return false;
    }
}
