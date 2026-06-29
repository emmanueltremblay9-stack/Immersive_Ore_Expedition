package com.oblixorprime.ioe.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class ResourcePolicyDecisionTest {
    @Test
    void rejectsBlankUseReasons() {
        assertThrows(IllegalArgumentException.class, () -> ResourcePolicyDecision.use(" "));
    }

    @Test
    void rejectsBlankSkipReasons() {
        assertThrows(IllegalArgumentException.class, () -> ResourcePolicyDecision.skip(" "));
    }

    @Test
    void rejectsBlankRejectReasons() {
        assertThrows(IllegalArgumentException.class, () -> ResourcePolicyDecision.reject(" "));
    }
}
