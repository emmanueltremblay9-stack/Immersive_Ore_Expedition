package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.crystalgrowth.Ae2CertusSiteProvider;
import com.oblixorprime.ioe.crystalgrowth.CrystalGrowthCompatGates;
import com.oblixorprime.ioe.crystalgrowth.MeteoriticAe2GeodePlacementPlan;
import com.oblixorprime.ioe.crystalgrowth.MeteoriticAe2GeodePlacementPlanner;
import com.oblixorprime.ioe.worldgen.IoeWorldgenBootstrap;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import com.oblixorprime.ioe.worldgen.IoeWorldgenRegistration;
import com.oblixorprime.ioe.worldgen.WorldgenBiomeProvinceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NetherSubLavaGeodePlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);
    private static final BlockPos ORIGIN = new BlockPos(24, 24, -24);
    private static final ResourceLocation ANCHOR_TYPE =
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "sub_lava_geode");
    private static final Set<String> LEGACY_SPLIT_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    private final ResourcePolicyService policyService = new ResourcePolicyService();

    @Test
    void defaultConfigGatesProduceNoOpNetherSubLavaGeodePlan() {
        ResourceRef quartz = quartz();
        NetherSubLavaGeodePlacementPlanner planner = planner(Set.of(quartz));

        NetherSubLavaGeodePlacementPlan plan = planner.planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                ORIGIN
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_RUNTIME_DISABLED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
    }

    @Test
    void enabledRuntimeGateCanPlanSubLavaQuartzGeode() {
        ResourceRef quartz = quartz();
        NetherSubLavaGeodePlacementPlanner planner = planner(Set.of(quartz));

        NetherSubLavaGeodePlacementPlan plan = planner.planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE, plan.geodeType());
        assertEquals(NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE, plan.sourceSystem());
        assertEquals(quartz, plan.primaryQuartzResource());
        assertTrue(plan.ancientDebrisHeartResource().isEmpty());
        assertEquals(Optional.of(Level.NETHER), plan.dimension());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
        assertTrue(plan.layerMetadata().orElseThrow().quartzShellPlanned());
        assertTrue(plan.layerMetadata().orElseThrow().quartzCorePlanned());
    }

    @Test
    void enabledRuntimeGateCanPlanAncientDebrisHeartWhenEnabledAndValid() {
        ResourceRef quartz = quartz();
        ResourceRef debris = ancientDebris();
        NetherSubLavaGeodePlacementPlanner planner = planner(Set.of(quartz, debris));

        NetherSubLavaGeodePlacementPlan plan = planner.planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz,
                debris,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.ancientDebrisHeartLayerMetadata(),
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART,
                plan.geodeType());
        assertEquals(Optional.of(debris), plan.ancientDebrisHeartResource());
        assertTrue(plan.layerMetadata().orElseThrow().ancientDebrisHeartEnabled());
    }

    @Test
    void nonNetherDimensionIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planSubLavaQuartzGeode(
                anchor(Level.OVERWORLD),
                validLake(),
                quartz(),
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_NOT_NETHER, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.NOT_NETHER, plan.skipReason());
    }

    @Test
    void invalidLavaAnchorMetadataIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata invalidMetadata =
                new NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata(0, 0.75D, 6, true);
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                null,
                ORIGIN,
                null,
                null,
                null,
                invalidMetadata,
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_LAVA_ANCHOR, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_LAVA_ANCHOR, plan.skipReason());
    }

    @Test
    void invalidDepthIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan.DepthMetadata invalidDepth =
                new NetherSubLavaGeodePlacementPlan.DepthMetadata(7, 8, 64);
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                null,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                invalidDepth,
                NetherSubLavaGeodePlacementPlanner.defaultLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_DEPTH, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_DEPTH, plan.skipReason());
    }

    @Test
    void unloadedQuartzResourceIsSkippedSafely() {
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of()).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void policyDeniedQuartzResourceIsSkippedSafely() {
        ResourceRef deniedQuartz = ResourceRef.item("minecraft", "nether_quartz_ore");
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(deniedQuartz)).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                deniedQuartz,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_DENIED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void strictExclusionsWin() {
        ResourceRef excludedQuartz = ResourceRef.block("minecraft", "tin_nether_quartz_ore");
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(excludedQuartz)).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                excludedQuartz,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_STRICT_EXCLUSION, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
    }

    @Test
    void ancientDebrisHeartDisabledCaseIsHandledSafely() {
        ResourceRef quartz = quartz();
        ResourceRef debris = ancientDebris();
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz, debris)).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz,
                debris,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_ANCIENT_DEBRIS_HEART_DISABLED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.ANCIENT_DEBRIS_HEART_DISABLED, plan.skipReason());
    }

    @Test
    void unloadedAncientDebrisResourceIsSkippedSafelyWhenHeartMetadataIsRequested() {
        ResourceRef quartz = quartz();
        ResourceRef debris = ancientDebris();
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz)).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz,
                debris,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.ancientDebrisHeartLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void policyDeniedAncientDebrisResourceIsSkippedSafelyWhenHeartMetadataIsRequested() {
        ResourceRef quartz = quartz();
        ResourceRef deniedDebris = ResourceRef.item("minecraft", "ancient_debris");
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz, deniedDebris)).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz,
                deniedDebris,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.ancientDebrisHeartLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_DENIED, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void nullOriginIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                null,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.NULL_ORIGIN, plan.skipReason());
    }

    @Test
    void nullResourceIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of()).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                null,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.NULL_RESOURCE, plan.skipReason());
    }

    @Test
    void invalidGeodeTypeIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planNetherSubLavaGeode(
                null,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                null,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_GEODE_TYPE, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_GEODE_TYPE, plan.skipReason());
    }

    @Test
    void invalidLayerMetadataIsRejectedSafely() {
        NetherSubLavaGeodePlacementPlan.LayerMetadata invalidLayer =
                new NetherSubLavaGeodePlacementPlan.LayerMetadata(2, 5, 3, false, 0.005D, true, true, true);
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz())).planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor(Level.NETHER),
                validLake(),
                quartz(),
                null,
                ORIGIN,
                null,
                null,
                null,
                NetherSubLavaGeodePlacementPlanner.defaultLavaLakeAnchorMetadata(),
                NetherSubLavaGeodePlacementPlanner.defaultDepthMetadata(),
                invalidLayer,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_LAYER_METADATA, plan.decision());
        assertEquals(NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_LAYER_METADATA, plan.skipReason());
    }

    @Test
    void biomeProvinceMetadataFromV11ContextIsPreserved() {
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceContext.resolved(
                ResourceLocation.fromNamespaceAndPath("minecraft", "nether_wastes"),
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "nether_lava_basin"),
                false
        );
        ResourceRef quartz = quartz();
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz)).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                ORIGIN,
                context.biomeId().orElse(null),
                context.provinceId().orElse(null),
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(context.biomeId(), plan.biomeId());
        assertEquals(context.provinceId(), plan.provinceId());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
    }

    @Test
    void existingSubLavaGeodeGeneratorBehaviorRemainsUnchanged() {
        ResourceRef quartz = quartz();
        Optional<SubLavaGeodePlan> plan = new SubLavaGeodeGenerator().planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                scanner(Set.of(quartz)),
                policyService
        );

        assertTrue(plan.isPresent());
        assertEquals(quartz, plan.get().quartzResource());
        assertFalse(plan.get().randomNetherGeode());
    }

    @Test
    void v14MeteoriticAe2GeodePlanningBehaviorRemainsUnchanged() {
        ResourceRef certus = ResourceRef.block("ae2", "flawless_budding_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone_block");
        MeteoriticAe2GeodePlacementPlanner planner = new MeteoriticAe2GeodePlacementPlanner(
                new Ae2CertusSiteProvider(),
                scanner(Set.of(
                        CrystalGrowthCompatGates.AE2,
                        CrystalGrowthCompatGates.AE2_CRYSTAL_SCIENCE
                ), Set.of(certus, crust)),
                policyService
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                new ExpeditionAnchorRef(Level.OVERWORLD, ORIGIN, "meteoritic_ae2_geode", SiteQuality.NORMAL),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "meteoritic_ae2_geode"),
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE, plan.geodeType());
        assertEquals(certus, plan.primaryResource());
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithNetherSubLavaGeodeMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.meteoriticAe2GeodePlacementPlanningReady());
        assertTrue(registration.netherSubLavaGeodePlacementPlanningReady());
        assertTrue(registration.futureFeatureKeys().contains(IoeWorldgenFeatureKeys.SUB_LAVA_GEODE));
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroduced() {
        ResourceRef quartz = quartz();
        NetherSubLavaGeodePlacementPlan plan = planner(Set.of(quartz)).planSubLavaQuartzGeode(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, plan.anchorType().orElseThrow().getNamespace());
        for (ResourceLocation featureKey : IoeWorldgenFeatureKeys.allFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
            assertFalse(LEGACY_SPLIT_NAMESPACES.contains(featureKey.getNamespace()));
        }
    }

    private NetherSubLavaGeodePlacementPlanner planner(Set<ResourceRef> resources) {
        return new NetherSubLavaGeodePlacementPlanner(
                new SubLavaGeodeGenerator(),
                scanner(resources),
                policyService
        );
    }

    private static LoadedResourceScanner scanner(Set<ResourceRef> resources) {
        return new NetherGeodesTestScanner(resources);
    }

    private static LoadedResourceScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new TestScanner(mods, resources);
    }

    private static ExpeditionAnchorRef anchor(net.minecraft.resources.ResourceKey<Level> dimension) {
        return new ExpeditionAnchorRef(dimension, new BlockPos(0, 32, 0), "sub_lava_geode", SiteQuality.NORMAL);
    }

    private static LavaLakeAnchorSample validLake() {
        return new LavaLakeAnchorSample(Level.NETHER, new BlockPos(0, 64, 0), 64, 0.75D, 6);
    }

    private static ResourceRef quartz() {
        return ResourceRef.block("minecraft", "nether_quartz_ore");
    }

    private static ResourceRef ancientDebris() {
        return ResourceRef.block("minecraft", "ancient_debris");
    }

    private record TestScanner(Set<String> loadedMods, Set<ResourceRef> loadedResources) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return loadedMods.contains(modId);
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK, id));
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID, id));
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM, id));
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK_TAG, id));
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID_TAG, id));
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM_TAG, id));
        }
    }
}
