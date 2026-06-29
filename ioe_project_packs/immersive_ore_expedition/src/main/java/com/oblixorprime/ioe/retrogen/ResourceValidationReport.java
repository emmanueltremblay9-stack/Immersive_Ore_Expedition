package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;

import java.util.List;
import java.util.Objects;

public record ResourceValidationReport(
        int usableCount,
        int skippedCount,
        int rejectedCount,
        List<DiagnosticFinding> findings
) {
    public ResourceValidationReport {
        findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
        if (usableCount < 0 || skippedCount < 0 || rejectedCount < 0) {
            throw new IllegalArgumentException("validation counts must not be negative");
        }
        validateCountsMatchFindings(usableCount, skippedCount, rejectedCount, findings);
    }

    public boolean safeToRun() {
        return rejectedCount == 0;
    }

    private static void validateCountsMatchFindings(
            int usableCount,
            int skippedCount,
            int rejectedCount,
            List<DiagnosticFinding> findings
    ) {
        int actualUsable = 0;
        int actualSkipped = 0;
        int actualRejected = 0;
        for (DiagnosticFinding finding : findings) {
            ResourcePolicyDecision.Action action = finding.action();
            switch (action) {
                case USE -> actualUsable++;
                case SKIP -> actualSkipped++;
                case REJECT -> actualRejected++;
            }
        }
        if (usableCount != actualUsable || skippedCount != actualSkipped || rejectedCount != actualRejected) {
            throw new IllegalArgumentException("validation counts must match diagnostic findings");
        }
    }
}
