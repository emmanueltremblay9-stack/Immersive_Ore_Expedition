package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Optional;

public interface CrystalGrowthSiteProvider {
    boolean isAvailable();

    Optional<CrystalGrowthSitePlan> planSite(
            ExpeditionAnchorRef anchor,
            ResourceRef coreResource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    );

    boolean canGenerateAt(WorldGenLevel level, BlockPos anchorPos);

    boolean generate(WorldGenLevel level, BlockPos anchorPos);
}
