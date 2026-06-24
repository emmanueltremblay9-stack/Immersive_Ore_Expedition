package com.oblixorprime.ioe.retrogen;

import java.util.List;

public record ResourceValidationReport(
        int usableCount,
        int skippedCount,
        int rejectedCount,
        List<DiagnosticFinding> findings
) {
    public ResourceValidationReport {
        findings = List.copyOf(findings);
        if (usableCount < 0 || skippedCount < 0 || rejectedCount < 0) {
            throw new IllegalArgumentException("validation counts must not be negative");
        }
    }

    public boolean safeToRun() {
        return rejectedCount == 0;
    }
}
