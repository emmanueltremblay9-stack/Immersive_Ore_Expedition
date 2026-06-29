package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.Objects;

public record DiagnosticFinding(ResourceRef resource, ResourcePolicyDecision.Action action, String reason) {
    public DiagnosticFinding {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(reason, "reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }
}
