package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExpeditionSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final int BLOCK_UPDATE_FLAGS = 2;
    private static final ResourcePolicyService RESOURCE_POLICY = new ResourcePolicyService();
    private static final List<OreChoice> VANILLA_ORE_CHOICES = List.of(
            ore("coal_ore", Blocks.COAL_ORE),
            ore("iron_ore", Blocks.IRON_ORE),
            ore("copper_ore", Blocks.COPPER_ORE),
            ore("gold_ore", Blocks.GOLD_ORE),
            ore("redstone_ore", Blocks.REDSTONE_ORE),
            ore("lapis_ore", Blocks.LAPIS_ORE),
            ore("diamond_ore", Blocks.DIAMOND_ORE),
            ore("emerald_ore", Blocks.EMERALD_ORE)
    );

    private final ExpeditionSiteType siteType;

    public ExpeditionSiteFeature(ExpeditionSiteType siteType) {
        super(NoneFeatureConfiguration.CODEC);
        this.siteType = Objects.requireNonNull(siteType, "siteType");
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Objects.requireNonNull(context, "context");
        if (!IoeWorldgenConfig.naturalExpeditionSiteGenerationEnabled()
                || !siteType.enabledFromConfig()
                || !requiredComponentsEnabled(siteType)) {
            return false;
        }
        if (siteType == ExpeditionSiteType.ORE_LOAD_CHAMBER
                && IoeWorldgenConfig.requireStructureAnchorForMajorOreLoads()) {
            logSkipped(context.origin(), "standalone ore-load chambers are forbidden by anchor policy");
            return false;
        }

        BlockPos origin = siteType.naturalSurfaceSite()
                ? resolveSurfaceOrigin(context.level(), context.origin(), siteType)
                : context.origin();
        if (origin == null) {
            return false;
        }

        SiteQuality quality = SiteQualityRoll.DEFAULT.roll(context.random());
        OreChoice oreChoice = quality.isProductive() ? chooseLoadedOre(context.random()) : null;
        if (quality.isProductive() && oreChoice == null) {
            logSkipped(origin, "no loaded and approved ore block was available");
            return false;
        }

        ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                siteType,
                origin,
                quality,
                oreChoice == null ? null : oreChoice.resource().id(),
                oreChoice == null ? null : oreChoice.block().defaultBlockState(),
                context.random()
        );
        if (siteType.naturalSurfaceSite() && !plan.isConnectedExpeditionSite()) {
            logSkipped(origin, "the configured anchor distance window excludes the connected chamber");
            return false;
        }
        if (!withinBuildHeight(context.level(), plan) || !apply(context.level(), plan)) {
            logSkipped(origin, "the connected block plan could not be written safely");
            return false;
        }

        if (siteType.naturalSurfaceSite()) {
            recordPlacedSite(context.level(), plan);
        }
        return true;
    }

    private static BlockPos resolveSurfaceOrigin(
            WorldGenLevel level,
            BlockPos requestedOrigin,
            ExpeditionSiteType siteType
    ) {
        int localX = Math.floorMod(requestedOrigin.getX(), 16);
        int localZ = Math.floorMod(requestedOrigin.getZ(), 16);
        int safeLocalX = localX <= 7 ? 4 : 11;
        int x = requestedOrigin.getX() + safeLocalX - localX;
        int z = requestedOrigin.getZ() + clamp(localZ, 6, 9) - localZ;
        int radius = siteType == ExpeditionSiteType.MINER_CAMP ? 4 : 3;
        int allowedSlope = siteType == ExpeditionSiteType.MINER_CAMP ? 2 : 4;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int dx : new int[]{-radius, 0, radius}) {
            for (int dz : new int[]{-radius, 0, radius}) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
                minY = Math.min(minY, surfaceY);
                maxY = Math.max(maxY, surfaceY);
            }
        }
        if (maxY - minY > allowedSlope) {
            return null;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 5; dy++) {
                    BlockState state = level.getBlockState(new BlockPos(x + dx, maxY + dy, z + dz));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return null;
                    }
                }
            }
        }
        return new BlockPos(x, maxY, z);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean requiredComponentsEnabled(ExpeditionSiteType type) {
        return !type.naturalSurfaceSite()
                || IoeWorldgenConfig.basicMineshaftConnectorEnabled()
                && IoeWorldgenConfig.oreLoadChamberEnabled();
    }

    private static OreChoice chooseLoadedOre(RandomSource random) {
        int start = random.nextInt(VANILLA_ORE_CHOICES.size());
        for (int offset = 0; offset < VANILLA_ORE_CHOICES.size(); offset++) {
            OreChoice choice = VANILLA_ORE_CHOICES.get((start + offset) % VANILLA_ORE_CHOICES.size());
            ResourcePolicyDecision decision = RESOURCE_POLICY.evaluate(choice.resource(), LoadedResourceScanner.runtime());
            if (decision.shouldUse()) {
                return choice;
            }
        }
        return null;
    }

    private static boolean withinBuildHeight(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        return plan.blocks().keySet().stream()
                .allMatch(pos -> pos.getY() >= minBuildHeight && pos.getY() < maxBuildHeight);
    }

    private static boolean apply(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        for (BlockPos pos : plan.blocks().keySet()) {
            if (!canWrite(level, pos)) {
                return false;
            }
        }
        for (Map.Entry<BlockPos, BlockState> placement : plan.blocks().entrySet()) {
            BlockPos pos = placement.getKey();
            BlockState target = placement.getValue();
            boolean changed = level.setBlock(pos, target, BLOCK_UPDATE_FLAGS);
            if (!changed && !level.getBlockState(pos).equals(target)) {
                return false;
            }
        }
        return true;
    }

    private static boolean canWrite(WorldGenLevel level, BlockPos pos) {
        if (!level.ensureCanWrite(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (!existing.getFluidState().isEmpty() || existing.hasBlockEntity()) {
            return false;
        }
        return existing.isAir() || OreLoadChamberReplacementRules.canReplace(existing);
    }

    private static void recordPlacedSite(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        ResourceLocation provinceId = ResourceLocation.tryParse(IoeWorldgenConfig.defaultProvince());
        ExpeditionLocatorService.index().record(ExpeditionSite.anchor(
                level.getLevel().dimension(),
                plan.anchorPos(),
                plan.requestedFeatureId(),
                provinceId,
                plan.quality(),
                "natural_connected_expedition_site",
                ExpeditionSitePlacementState.PROVEN,
                null
        ));
        if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Generated connected IOE expedition site type={} anchor={} chamber={} quality={} ore={} blocks={}",
                    plan.requestedFeatureId(),
                    plan.anchorPos(),
                    plan.chamberCenter(),
                    plan.quality(),
                    plan.oreBlockId(),
                    plan.blocks().size()
            );
        }
    }

    private static void logSkipped(BlockPos origin, String reason) {
        if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            IoeExpeditionWorldgenMod.LOGGER.info("Skipped IOE expedition site at {}: {}", origin, reason);
        }
    }

    private static OreChoice ore(String path, Block block) {
        ResourceRef resource = ResourceRef.block("minecraft", path);
        ResourceLocation actualId = BuiltInRegistries.BLOCK.getKey(block);
        if (!resource.id().equals(actualId)) {
            throw new IllegalStateException("Vanilla ore palette mismatch for " + resource.id() + ": " + actualId);
        }
        return new OreChoice(resource, block);
    }

    private record OreChoice(ResourceRef resource, Block block) {
        private OreChoice {
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(block, "block");
        }
    }
}
