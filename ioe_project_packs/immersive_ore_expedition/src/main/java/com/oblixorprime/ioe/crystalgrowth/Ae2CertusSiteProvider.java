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

public final class Ae2CertusSiteProvider implements CrystalGrowthSiteProvider {
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public Ae2CertusSiteProvider() {
        this(LoadedResourceScanner.runtime(), new ResourcePolicyService());
    }

    public Ae2CertusSiteProvider(LoadedResourceScanner scanner, ResourcePolicyService policyService) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    @Override
    public boolean isAvailable() {
        return CrystalGrowthCompatGates.ae2CrystalProcessingStackEnabled(scanner);
    }

    @Override
    public Optional<CrystalGrowthSitePlan> planSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        return planCertusSite(anchor, coreResource, Optional.empty(), scanner, policyService);
    }

    public Optional<CrystalGrowthSitePlan> planCertusSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            Optional<ResourceRef> skyStoneCrust,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(skyStoneCrust, "skyStoneCrust");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!CrystalGrowthCompatGates.ae2CrystalProcessingStackEnabled(scanner)
                || !IoeCrystalGrowthConfig.buriedMeteorites()
                || !IoeCrystalGrowthConfig.allowBuddingCertusSites()
                || !CrystalGrowthSiteRules.canPlanAnchoredSite(anchor)) {
            return Optional.empty();
        }

        if (!isCertusCoreResource(coreResource)) {
            return Optional.empty();
        }

        ResourcePolicyDecision coreDecision = policyService.evaluate(coreResource, scanner);
        if (!coreDecision.shouldUse()) {
            return Optional.empty();
        }

        Optional<ResourceRef> validatedCrust = Optional.empty();
        if (IoeCrystalGrowthConfig.skyStoneCrustAroundGeodes()) {
            if (skyStoneCrust.isEmpty() || !isSkyStoneCrustResource(skyStoneCrust.get())) {
                return Optional.empty();
            }
            ResourcePolicyDecision crustDecision = policyService.evaluate(skyStoneCrust.get(), scanner);
            if (!crustDecision.shouldUse()) {
                return Optional.empty();
            }
            validatedCrust = skyStoneCrust;
        }

        return Optional.of(new CrystalGrowthSitePlan(
                CrystalGrowthSiteType.AE2_CERTUS,
                anchor,
                coreResource,
                validatedCrust,
                true,
                false,
                validatedCrust.isPresent(),
                false,
                List.of(),
                List.of()
        ));
    }

    private static boolean isCertusCoreResource(ResourceRef resource) {
        return resource != null
                && resource.type() == ResourceType.BLOCK
                && isAe2Resource(resource)
                && CrystalGrowthCompatGates.isNativeBuddingCertusPath(resource.id().getPath());
    }

    private static boolean isSkyStoneCrustResource(ResourceRef resource) {
        return resource != null
                && resource.type() == ResourceType.BLOCK
                && isAe2Resource(resource)
                && isSkyStonePath(resource.id().getPath());
    }

    private static boolean isAe2Resource(ResourceRef resource) {
        if (resource == null) {
            return false;
        }
        String namespace = resource.id().getNamespace();
        return CrystalGrowthCompatGates.AE2.equals(namespace) || "appeng".equals(namespace);
    }

    private static boolean isSkyStonePath(String path) {
        return "sky_stone_block".equals(path);
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
        IoeCrystalGrowthMod.LOGGER.debug("Skipping direct AE2 Certus site placement at {}; alpha planning is enabled but configured-feature placement is not registered.", anchorPos);
        return false;
    }
}
