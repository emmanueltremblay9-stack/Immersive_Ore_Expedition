package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceResourcePolicy;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldgenBiomeProvinceBindingAdapterTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, true, false);
    private static final IoeWorldgenPlacementGates RUNTIME_DISABLED_GATES =
            new IoeWorldgenPlacementGates(false, true, false);
    private static final IoeWorldgenPlacementGates PROVINCE_DISABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);

    private final ResourcePolicyService policyService = new ResourcePolicyService();

    @Test
    void defaultConfigGatesProduceNoOpContext() {
        ResourceLocation plains = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");

        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceBindingAdapter.defaults().resolve(plains);

        assertFalse(context.contextResolved());
        assertFalse(context.provinceIntegrationAllowed());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.NO_OP_RUNTIME_WORLDGEN_DISABLED,
                context.decision()
        );
        assertEquals(WorldgenBiomeProvinceContext.SkipReason.RUNTIME_WORLDGEN_DISABLED, context.skipReason());
        assertTrue(context.runtimeWorldgenGateDisabled());
    }

    @Test
    void explicitlyEnabledGatesResolveConfiguredBiomeToProvince() {
        ResourceLocation plains = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        WorldgenBiomeProvinceBindingAdapter adapter = adapter(
                "immersive_ore_expedition:default",
                List.of("minecraft:plains=temperate_iron")
        );

        WorldgenBiomeProvinceContext context = adapter.resolve(plains, ENABLED_GATES);

        assertTrue(context.contextResolved());
        assertTrue(context.provinceIntegrationAllowed());
        assertEquals(WorldgenBiomeProvinceContext.ResolutionDecision.RESOLVED_FROM_BIOME, context.decision());
        assertEquals(Optional.of(plains), context.biomeId());
        assertEquals(
                Optional.of(ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "temperate_iron")),
                context.provinceId()
        );
        assertFalse(context.defaultProvinceFallbackUsed());
    }

    @Test
    void unmatchedBiomeFallsBackToDefaultProvince() {
        ResourceLocation desert = ResourceLocation.fromNamespaceAndPath("minecraft", "desert");
        WorldgenBiomeProvinceBindingAdapter adapter = adapter(
                "immersive_ore_expedition:default",
                List.of("minecraft:plains=temperate_iron")
        );

        WorldgenBiomeProvinceContext context = adapter.resolve(desert, ENABLED_GATES);

        assertTrue(context.contextResolved());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.RESOLVED_FROM_DEFAULT_PROVINCE,
                context.decision()
        );
        assertEquals(
                Optional.of(ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "default")),
                context.provinceId()
        );
        assertTrue(context.defaultProvinceFallbackUsed());
    }

    @Test
    void nullBiomeIsHandledSafely() {
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceBindingAdapter.defaults()
                .resolve(null, ENABLED_GATES);

        assertFalse(context.contextResolved());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_INVALID_INPUT,
                context.decision()
        );
        assertEquals(WorldgenBiomeProvinceContext.SkipReason.NULL_BIOME, context.skipReason());
        assertTrue(context.biomeId().isEmpty());
        assertTrue(context.provinceId().isEmpty());
    }

    @Test
    void malformedBindingIsHandledSafely() {
        WorldgenBiomeProvinceBindingAdapter adapter = adapter(
                "immersive_ore_expedition:default",
                List.of("not a binding")
        );

        WorldgenBiomeProvinceContext context = adapter.resolve(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                ENABLED_GATES
        );

        assertFalse(context.contextResolved());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_MALFORMED_BINDING,
                context.decision()
        );
        assertEquals(WorldgenBiomeProvinceContext.SkipReason.MALFORMED_BINDING, context.skipReason());
    }

    @Test
    void provinceRuntimeDisabledReturnsNoOpContext() {
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceBindingAdapter.defaults().resolve(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                PROVINCE_DISABLED_GATES
        );

        assertFalse(context.contextResolved());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.NO_OP_PROVINCE_INTEGRATION_DISABLED,
                context.decision()
        );
        assertEquals(
                WorldgenBiomeProvinceContext.SkipReason.PROVINCE_RUNTIME_INTEGRATION_DISABLED,
                context.skipReason()
        );
        assertTrue(context.provinceRuntimeIntegrationGateDisabled());
    }

    @Test
    void runtimeWorldgenDisabledReturnsNoOpContext() {
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceBindingAdapter.defaults().resolve(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                RUNTIME_DISABLED_GATES
        );

        assertFalse(context.contextResolved());
        assertEquals(
                WorldgenBiomeProvinceContext.ResolutionDecision.NO_OP_RUNTIME_WORLDGEN_DISABLED,
                context.decision()
        );
        assertEquals(WorldgenBiomeProvinceContext.SkipReason.RUNTIME_WORLDGEN_DISABLED, context.skipReason());
        assertTrue(context.runtimeWorldgenGateDisabled());
    }

    @Test
    void oldOreLoadGeneratorOverloadBehaviorRemainsUnchanged() {
        OreLoadGenerator generator = new OreLoadGenerator();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithLoaded(iron);

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor(SiteQuality.NORMAL),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                ProvinceRuntimeIntegration.disabled(policyService, scanner)
        );

        assertTrue(plan.isPresent());
    }

    @Test
    void biomeAwareOreLoadGeneratorPathCanConsumeResolvedContext() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithLoaded(iron);
        WorldgenBiomeProvinceContext context = adapter(
                "immersive_ore_expedition:default",
                List.of("minecraft:plains=temperate_iron")
        ).resolve(ResourceLocation.fromNamespaceAndPath("minecraft", "plains"), ENABLED_GATES);
        ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                true,
                ProvinceBindingResolver.parse(
                        "immersive_ore_expedition:default",
                        List.of("minecraft:plains=temperate_iron"),
                        false
                ),
                ProvinceResourcePolicy.defaults(),
                ProvinceResourcePolicyResolver.parse(
                        List.of(
                                "default|minecraft:iron_ore|deny",
                                "temperate_iron|minecraft:iron_ore|allow"
                        ),
                        false
                ),
                policyService,
                scanner
        );

        Optional<OreLoadPlan> plan = new OreLoadGenerator().planAnchoredOreLoad(
                anchor(SiteQuality.NORMAL),
                iron,
                new BlockPos(16, 64, 0),
                context.biomeId().orElseThrow(),
                scanner,
                policyService,
                integration
        );

        assertTrue(context.contextResolved());
        assertTrue(plan.isPresent());
    }

    @Test
    void v8AnchorPlacementPlanningPreservesContextMetadata() {
        WorldgenBiomeProvinceContext context = resolvedPlainsContext();

        ExpeditionAnchorPlacementPlan plan = ExpeditionAnchorPlacementPlanner.defaults().planAnchorPlacement(
                IoeWorldgenFeatureKeys.COLLAPSED_SHAFT,
                BlockPos.ZERO,
                SiteQuality.RICH,
                context.biomeId().orElse(null),
                context.provinceId().orElse(null),
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(context.biomeId(), plan.biomeId());
        assertEquals(context.provinceId(), plan.provinceId());
    }

    @Test
    void v9OreLoadChamberPlanningPreservesContextMetadata() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        WorldgenBiomeProvinceContext context = resolvedPlainsContext();

        OreLoadChamberPlacementPlan plan = OreLoadChamberPlacementPlanner.defaults().planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.NORMAL),
                scannerWithLoaded(iron),
                policyService,
                context.biomeId().orElse(null),
                context.provinceId().orElse(null),
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(context.biomeId(), plan.biomeId());
        assertEquals(context.provinceId(), plan.provinceId());
    }

    @Test
    void v10RandomOreSuppressionPlanningPreservesContextMetadata() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        WorldgenBiomeProvinceContext context = resolvedPlainsContext();

        RandomOreSuppressionPlan plan = RandomOreSuppressionPlanner.defaults().planRandomOreSuppression(
                iron,
                scannerWithLoaded(iron),
                policyService,
                context.biomeId().orElse(null),
                context.provinceId().orElse(null),
                ENABLED_GATES
        );

        assertTrue(plan.decisionProduced());
        assertEquals(context.biomeId(), plan.biomeId());
        assertEquals(context.provinceId(), plan.provinceId());
    }

    @Test
    void strictExclusionsAndResourcePolicyRemainUnaffected() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        ResourcePolicyDecision decision = policyService.evaluate(tin, scannerWithLoaded(tin));

        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertFalse(decision.shouldUse());
        assertTrue(decision.reason().contains("explicitly excluded"));
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithBindingMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertTrue(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertTrue(registration.oreLoadChamberPlacementPlanningReady());
        assertTrue(registration.randomOreSuppressionPlanningReady());
        assertTrue(registration.liveBiomeProvinceBindingPlanningReady());
        assertFalse(registration.configuredFeaturesRegistered());
        assertFalse(registration.placedFeaturesRegistered());
        assertFalse(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroducedAsDefault() {
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceBindingAdapter.defaults().resolve(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                ENABLED_GATES
        );

        assertTrue(context.contextResolved());
        assertEquals(
                Optional.of(ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "default")),
                context.provinceId()
        );
    }

    private static WorldgenBiomeProvinceBindingAdapter adapter(String defaultProvince, List<String> bindings) {
        return new WorldgenBiomeProvinceBindingAdapter(
                WorldgenBiomeProvinceBindingRules.parse(defaultProvince, bindings, false)
        );
    }

    private static WorldgenBiomeProvinceContext resolvedPlainsContext() {
        return adapter("immersive_ore_expedition:default", List.of("minecraft:plains=temperate_iron"))
                .resolve(ResourceLocation.fromNamespaceAndPath("minecraft", "plains"), ENABLED_GATES);
    }

    private static OreLoadPlan oreLoadPlan(ResourceRef resource, SiteQuality quality) {
        ExpeditionAnchorRef anchor = anchor(quality);
        BlockPos center = new BlockPos(16, 64, 0);
        return new OreLoadPlan(
                anchor,
                resource,
                center,
                quality,
                false,
                anchor.pos().distManhattan(center)
        );
    }

    private static ExpeditionAnchorRef anchor(SiteQuality quality) {
        return new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                quality
        );
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded));
    }

    private record TestScanner(Set<ResourceRef> loadedResources) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
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
