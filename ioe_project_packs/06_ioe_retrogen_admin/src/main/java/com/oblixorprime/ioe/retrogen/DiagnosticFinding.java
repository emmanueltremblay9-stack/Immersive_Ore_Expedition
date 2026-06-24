package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.Objects;

public record DiagnosticFinding(ResourceRef resource, ResourcePolicyDecision.Action action, String reason) {
    public DiagnosticFinding {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(reason, "reason");
    }
}
