package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Objects;

public final class RuntimeWorldgenRegistrationSmokeBridgeFeature extends Feature<NoneFeatureConfiguration> {
    private static final RuntimeWorldgenPlacementProof PROOF = new RuntimeWorldgenPlacementProof();
    private static final ResourcePolicyService POLICY_SERVICE = new ResourcePolicyService();
    private static final int BLOCK_UPDATE_FLAGS = 2;

    public RuntimeWorldgenRegistrationSmokeBridgeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Objects.requireNonNull(context, "context");

        IoeRuntimeProofFeatureGates bridgeGates = IoeRuntimeProofFeatureGates.fromConfig();
        IoeWorldgenPlacementGates placementGates = IoeWorldgenPlacementGates.fromConfig();
        if (!IoeRuntimeProofFeatureBridge.shouldInvokeProof(bridgeGates, placementGates)) {
            logSkipped(context.origin(), bridgeGates, placementGates);
            return false;
        }

        if (!IoeRuntimeProofFeatureBridge.shouldPlaceBlocksFromBiomeInvocation(bridgeGates, placementGates)) {
            logSuppressedNaturalInvocation(context.origin(), bridgeGates, placementGates);
            return false;
        }

        RuntimeWorldgenPlacementProofResult readyResult = PROOF.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                context.origin(),
                SiteQuality.NORMAL,
                RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE,
                LoadedResourceScanner.runtime(),
                POLICY_SERVICE,
                placementGates
        );
        if (!readyResult.placementPathAllowed()) {
            return RuntimeWorldgenProofSiteRecorder.placeAndRecordProvenSite(
                    context.level().getLevel().dimension(),
                    readyResult,
                    () -> false,
                    ExpeditionLocatorService::recordPlacedProof
            );
        }

        return RuntimeWorldgenProofSiteRecorder.placeAndRecordProvenSite(
                context.level().getLevel().dimension(),
                readyResult,
                () -> placeTinyVerticalMineEntrance(context.level(), context.origin()),
                ExpeditionLocatorService::recordPlacedProof
        );
    }

    private static boolean placeTinyVerticalMineEntrance(WorldGenLevel level, BlockPos origin) {
        if (level == null || origin == null || !canPlaceFootprint(level, origin)) {
            return false;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                BlockState state = x == 0 && z == 0 ? centerState() : ringState();
                if (!level.setBlock(pos, state, BLOCK_UPDATE_FLAGS)) {
                    return false;
                }
            }
        }
        level.setBlock(origin.above(), capState(), BLOCK_UPDATE_FLAGS);
        return true;
    }

    private static BlockState centerState() {
        return Blocks.CALCITE.defaultBlockState();
    }

    private static BlockState ringState() {
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private static BlockState capState() {
        return Blocks.OAK_PLANKS.defaultBlockState();
    }

    private static boolean canPlaceFootprint(WorldGenLevel level, BlockPos origin) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                if (!level.ensureCanWrite(pos) || !level.isEmptyBlock(pos)) {
                    return false;
                }
            }
        }
        BlockPos capPos = origin.above();
        return level.ensureCanWrite(capPos) && level.isEmptyBlock(capPos);
    }

    private static void logSkipped(
            BlockPos origin,
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (!bridgeGates.diagnosticsEnabled()) {
            return;
        }
        String reason = bridgeGates.shouldNoOpRuntimeProofFeature()
                ? "runtime proof feature gate disabled"
                : "runtime placement gate disabled";
        IoeExpeditionWorldgenMod.LOGGER.info(
                "IOE v19 runtime proof feature skipped at {}: {}",
                origin,
                reason
        );
    }

    private static void logSuppressedNaturalInvocation(
            BlockPos origin,
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (!bridgeGates.diagnosticsEnabled() && !placementGates.diagnosticsEnabled()) {
            return;
        }
        IoeExpeditionWorldgenMod.LOGGER.info(
                "IOE runtime proof feature suppressed placement at {}: runtime proof or placement gates are not both enabled",
                origin
        );
    }
}
