package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceValidationReportTest {
    @Test
    void rejectsCountsThatDoNotMatchFindings() {
        List<DiagnosticFinding> findings = List.of(
                new DiagnosticFinding(ResourceRef.block("minecraft", "iron_ore"), ResourcePolicyDecision.Action.USE, "usable"),
                new DiagnosticFinding(ResourceRef.block("minecraft", "diamond_ore"), ResourcePolicyDecision.Action.SKIP, "missing"),
                new DiagnosticFinding(ResourceRef.block("example", "tin_ore"), ResourcePolicyDecision.Action.REJECT, "excluded")
        );

        assertThrows(IllegalArgumentException.class, () -> new ResourceValidationReport(1, 1, 0, findings));
    }

    @Test
    void rejectsPositiveCountsWithoutFindings() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceValidationReport(1, 0, 0, List.of()));
    }
}
