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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeWorldgenRegistrationTest {
    private static final Set<String> LEGACY_SPLIT_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    @Test
    void bootstrapCreatesActiveGatedWorldgenRegistration() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertFalse(registration.customFeaturesRegistered());
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
        assertTrue(registration.configuredFeatureDeclarationPresent());
        assertTrue(registration.placedFeatureDeclarationPresent());
        assertTrue(registration.biomeModifierDeclarationPresent());
        assertTrue(registration.smokeBiomeTagDeclarationPresent());
        assertTrue(registration.realBiomeBindingByDefault());
        assertTrue(registration.runtimeBiomeInvocationExternalTagOptInPossible());
        assertTrue(registration.datapackResourceJsonPresent());
        assertTrue(registration.runtimeBiomeBindingPresent());
        assertTrue(registration.livePlacementProofComplete());
        assertTrue(registration.futureFeatureKeys().contains(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER));
    }

    @Test
    void runtimeBridgeRegistrationStatusTracksCustomFeatureAndDeclarationBridgeOnly() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.registration(
                IoeWorldgenPlacementGates.disabled(),
                true
        );

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.customFeaturesRegistered());
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
        assertTrue(registration.configuredFeatureDeclarationPresent());
        assertTrue(registration.placedFeatureDeclarationPresent());
        assertTrue(registration.biomeModifierDeclarationPresent());
        assertTrue(registration.smokeBiomeTagDeclarationPresent());
        assertTrue(registration.realBiomeBindingByDefault());
        assertTrue(registration.runtimeBiomeInvocationExternalTagOptInPossible());
        assertTrue(registration.datapackResourceJsonPresent());
        assertTrue(registration.runtimeBiomeBindingPresent());
        assertTrue(registration.livePlacementProofComplete());
    }

    @Test
    void configuredFeatureDeclarationReferencesRuntimeProofFeatureWithNoneConfig() throws IOException {
        String json = readClasspathResource(
                "data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json"
        );

        assertTrue(json.contains("\"type\": \"immersive_ore_expedition:tiny_vertical_mine_entrance\""));
        assertTrue(json.contains("\"config\": {}"));
    }

    @Test
    void placedFeatureDeclarationReferencesConfiguredFeatureWithNaturalSurfacePlacement() throws IOException {
        String json = readClasspathResource(
                "data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json"
        );

        assertTrue(json.contains("\"feature\": \"immersive_ore_expedition:tiny_vertical_mine_entrance\""));
        assertTrue(json.contains("\"type\": \"minecraft:rarity_filter\""));
        assertTrue(json.contains("\"type\": \"minecraft:in_square\""));
        assertTrue(json.contains("\"type\": \"minecraft:heightmap\""));
        assertTrue(json.contains("\"heightmap\": \"MOTION_BLOCKING_NO_LEAVES\""));
        assertTrue(json.contains("\"type\": \"minecraft:biome\""));
    }

    @Test
    void biomeModifierDeclarationReferencesGameplayBiomeTagAndPlacedFeature() throws IOException {
        String json = readClasspathResource(
                "data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance.json"
        );

        assertTrue(json.contains("\"type\": \"neoforge:add_features\""));
        assertTrue(json.contains("\"biomes\": \"#immersive_ore_expedition:mine_entrance_biomes\""));
        assertTrue(json.contains("\"features\": \"immersive_ore_expedition:tiny_vertical_mine_entrance\""));
        assertTrue(json.contains("\"step\": \"surface_structures\""));
        assertFalse(json.contains("#c:is_overworld"));
        assertFalse(json.contains("minecraft:plains"));
        assertFalse(json.contains("underground_ores"));
    }

    @Test
    void expeditionSiteBiomeTagBindsEveryDataDrivenOreProfile() throws IOException {
        String json = readClasspathResource(
                "data/immersive_ore_expedition/tags/worldgen/biome/expedition_site_biomes.json"
        );

        assertTrue(json.contains("\"replace\": true"));
        assertTrue(json.contains("#immersive_ore_expedition:ore_profile/coal"));
        assertTrue(json.contains("#immersive_ore_expedition:ore_profile/nickel"));
        assertTrue(json.contains("#immersive_ore_expedition:ore_profile/uranium"));
        assertFalse(json.contains("\"minecraft:plains\""));
        assertFalse(json.contains("#minecraft:is_overworld"));
        assertFalse(json.contains("#c:"));
    }

    @Test
    void naturalSiteTagsUseProfileBackedBiomesAndCoverCommonTemperateTerrain() throws IOException {
        for (String siteTag : List.of(
                "mine_entrance_biomes",
                "collapsed_shaft_biomes",
                "miner_camp_biomes",
                "survey_marker_biomes"
        )) {
            String json = readClasspathResource(
                    "data/immersive_ore_expedition/tags/worldgen/biome/" + siteTag + ".json"
            );
            assertTrue(json.contains("#immersive_ore_expedition:expedition_site_biomes"));
            assertFalse(json.contains("#c:is_overworld"));
        }

        String ironBiomes = readClasspathResource(
                "data/immersive_ore_expedition/tags/worldgen/biome/ore_profile/iron.json"
        );
        assertTrue(ironBiomes.contains("\"minecraft:plains\""));
        assertTrue(ironBiomes.contains("\"minecraft:forest\""));
    }

    @Test
    void statusModelDistinguishesBiomeModifierDeclarationFromRealBiomeBinding() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertTrue(registration.biomeModifiersRegistered());
        assertTrue(registration.biomeModifierDeclarationPresent());
        assertTrue(registration.smokeBiomeTagDeclarationPresent());
        assertTrue(registration.realBiomeBindingByDefault());
        assertTrue(registration.runtimeBiomeInvocationExternalTagOptInPossible());
        assertTrue(registration.runtimeBiomeBindingPresent());
        assertTrue(registration.livePlacementProofComplete());
    }

    @Test
    void declarationBridgeDoesNotLoosenRuntimeProofGates() {
        IoeRuntimeProofFeatureGates enabledBridge = new IoeRuntimeProofFeatureGates(true, false);
        IoeWorldgenPlacementGates disabledPlacement = IoeWorldgenPlacementGates.disabled();
        IoeWorldgenPlacementGates enabledPlacement = new IoeWorldgenPlacementGates(true, false, false);

        assertFalse(IoeRuntimeProofFeatureBridge.shouldInvokeProof(enabledBridge, disabledPlacement));
        assertTrue(IoeRuntimeProofFeatureBridge.shouldInvokeProof(enabledBridge, enabledPlacement));
    }

    @Test
    void featureKeysUseActiveIoeNamespaceOnly() {
        assertFalse(IoeWorldgenFeatureKeys.allFeatureKeys().isEmpty());

        for (ResourceLocation featureKey : IoeWorldgenFeatureKeys.allFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
            assertFalse(LEGACY_SPLIT_NAMESPACES.contains(featureKey.getNamespace()));
        }
    }

    @Test
    void defaultProvinceNamespaceDoesNotUseLegacySplitNamespace() {
        assertEquals(ImmersiveOreExpeditionMod.MODID, IoeWorldgenConfig.provinceNamespace());
        assertFalse(IoeWorldgenConfig.allowLegacyProvinceNamespaces());
        assertFalse(LEGACY_SPLIT_NAMESPACES.contains(IoeWorldgenConfig.provinceNamespace()));
    }

    @Test
    void scaffoldInitializationDoesNotMutateOreLoadPlanningBehavior() {
        OreLoadGenerator generator = new OreLoadGenerator();
        ResourcePolicyService policyService = new ResourcePolicyService();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithLoaded(iron);
        ProvinceRuntimeIntegration disabledIntegration = ProvinceRuntimeIntegration.disabled(policyService, scanner);

        Optional<OreLoadPlan> before = generator.planAnchoredOreLoad(
                anchor(),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                disabledIntegration
        );

        IoeWorldgenBootstrap.bootstrap();

        Optional<OreLoadPlan> after = generator.planAnchoredOreLoad(
                anchor(),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                disabledIntegration
        );

        assertEquals(before.isPresent(), after.isPresent());
        assertTrue(after.isPresent());
    }

    @Test
    void disabledRuntimePathStillUsesResourcePolicyService() {
        OreLoadGenerator generator = new OreLoadGenerator();
        ResourcePolicyService policyService = new ResourcePolicyService();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithNothingLoaded();

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor(),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                ProvinceRuntimeIntegration.disabled(policyService, scanner)
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void strictExclusionsRemainUnaffectedByScaffold() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        IoeWorldgenBootstrap.bootstrap();

        ResourcePolicyDecision decision = ProvinceResourcePolicy.defaults().evaluate(tin);
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertFalse(decision.shouldUse());
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                SiteQuality.NORMAL
        );
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(loaded);
    }

    private static LoadedResourceScanner scannerWithNothingLoaded() {
        return new TestScanner(null);
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new AssertionError("missing classpath resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private record TestScanner(ResourceRef loaded) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.BLOCK && loaded.id().equals(id);
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.FLUID && loaded.id().equals(id);
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.ITEM && loaded.id().equals(id);
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.BLOCK_TAG && loaded.id().equals(id);
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.FLUID_TAG && loaded.id().equals(id);
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.ITEM_TAG && loaded.id().equals(id);
        }
    }
}
