package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeRuntimeScaffoldStatusTest {
    @Test
    void defaultOffStatusReportsActiveRegistrationWithGatedPlacement() {
        IoeRuntimeScaffoldStatus status = IoeRuntimeScaffoldStatus.fromRegistration(
                "test-version",
                IoeWorldgenRegistration.scaffold(
                        IoeWorldgenFeatureKeys.allFeatureKeys(),
                        IoeWorldgenPlacementGates.disabled(),
                        false
                ),
                true
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, status.modId());
        assertEquals("test-version", status.modVersion());
        assertFalse(status.runtimeWorldgenEnabled());
        assertFalse(status.provinceRuntimeIntegrationEnabled());
        assertFalse(status.diagnosticsEnabled());
        assertTrue(status.worldgenRegistrationScaffoldReady());
        assertTrue(status.anchorPlacementPlanningReady());
        assertTrue(status.oreLoadChamberPlacementPlanningReady());
        assertTrue(status.randomOreSuppressionPlanningReady());
        assertTrue(status.biomeProvinceBindingScaffoldReady());
        assertTrue(status.ieIpSurfaceCluePlanningReady());
        assertFalse(status.scaffoldOnly());
        assertTrue(status.livePlacementRegistered());
    }

    @Test
    void formatterExplainsWhyNothingVisibleChanged() {
        IoeRuntimeScaffoldStatus status = IoeRuntimeScaffoldStatus.fromRegistration(
                "",
                IoeWorldgenRegistration.scaffold(
                        IoeWorldgenFeatureKeys.allFeatureKeys(),
                        IoeWorldgenPlacementGates.disabled(),
                        false
                ),
                true
        );

        String output = String.join("\n", IoeRuntimeScaffoldStatusFormatter.format(status));

        assertTrue(output.contains("version=unknown"));
        assertTrue(output.contains("runtimeWorldgenEnabled=false"));
        assertTrue(output.contains("provinceRuntimeIntegrationEnabled=false"));
        assertTrue(output.contains("planning-only"));
        assertTrue(output.contains("JourneyMap"));
        assertTrue(output.contains("visible world and JourneyMap targets require runtime placement gates"));
    }

    @Test
    void formatterReportsEnabledProvinceAndDiagnosticsGates() {
        IoeRuntimeScaffoldStatus status = IoeRuntimeScaffoldStatus.fromRegistration(
                "test",
                IoeWorldgenRegistration.scaffold(
                        IoeWorldgenFeatureKeys.allFeatureKeys(),
                        new IoeWorldgenPlacementGates(true, true, true),
                        false
                ),
                true
        );

        String output = String.join("\n", IoeRuntimeScaffoldStatusFormatter.format(status));

        assertTrue(output.contains("runtimeWorldgenEnabled=true"));
        assertTrue(output.contains("provinceRuntimeIntegrationEnabled=true"));
        assertTrue(output.contains("diagnosticsEnabled=true"));
        assertTrue(output.contains("scaffoldOnly=false"));
    }

    @Test
    void statusModelCreationDoesNotMutateOreLoadPlanningBehavior() {
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

        IoeRuntimeScaffoldStatus.fromRegistration(
                "test",
                IoeWorldgenRegistration.scaffold(
                        IoeWorldgenFeatureKeys.allFeatureKeys(),
                        IoeWorldgenPlacementGates.disabled(),
                        false
                ),
                true
        );

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
    void statusModelDoesNotIntroduceLegacyNamespace() {
        IoeRuntimeScaffoldStatus status = IoeRuntimeScaffoldStatus.fromRegistration(
                "test",
                IoeWorldgenRegistration.scaffold(
                        IoeWorldgenFeatureKeys.allFeatureKeys(),
                        IoeWorldgenPlacementGates.disabled(),
                        false
                ),
                true
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, status.modId());
        assertFalse(status.modId().startsWith("ioe_"));
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
