package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrogenDiagnosticsTest {
    @Test
    void diagnosticsUseCorePolicyAndLoadedResourceScanner() {
        ResourceRef loadedIron = ResourceRef.block("minecraft", "iron_ore");
        ResourceRef missingDiamond = ResourceRef.block("minecraft", "diamond_ore");
        ResourceRef excludedTin = ResourceRef.block("example", "tin_ore");
        RetrogenDiagnostics diagnostics = new RetrogenDiagnostics(
                new ResourcePolicyService(),
                new RetrogenTestScanner(Set.of(loadedIron))
        );

        ResourceValidationReport report = diagnostics.validateResources(List.of(loadedIron, missingDiamond, excludedTin));

        assertEquals(1, report.usableCount());
        assertEquals(1, report.skippedCount());
        assertEquals(1, report.rejectedCount());
        assertFalse(report.safeToRun());
        assertEquals(ResourcePolicyDecision.Action.REJECT, report.findings().get(2).action());
    }

    @Test
    void reportIsSafeWhenResourcesAreUsableOrSkippedOnly() {
        ResourceRef loadedCoal = ResourceRef.block("minecraft", "coal_ore");
        ResourceRef missingAmethyst = ResourceRef.block("minecraft", "amethyst_cluster");
        RetrogenDiagnostics diagnostics = new RetrogenDiagnostics(
                new ResourcePolicyService(),
                new RetrogenTestScanner(Set.of(loadedCoal))
        );

        ResourceValidationReport report = diagnostics.validateResources(List.of(loadedCoal, missingAmethyst));

        assertEquals(1, report.usableCount());
        assertEquals(1, report.skippedCount());
        assertEquals(0, report.rejectedCount());
        assertTrue(report.safeToRun());
    }
}
