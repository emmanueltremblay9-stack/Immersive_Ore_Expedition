package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticFindingTest {
    @Test
    void allowsNullResourceForMissingReferences() {
        assertDoesNotThrow(() -> new DiagnosticFinding(
                null,
                ResourcePolicyDecision.Action.REJECT,
                "resource reference is missing"
        ));
    }

    @Test
    void rejectsBlankReasons() {
        assertThrows(IllegalArgumentException.class, () -> new DiagnosticFinding(
                null,
                ResourcePolicyDecision.Action.REJECT,
                " "
        ));
    }
}
