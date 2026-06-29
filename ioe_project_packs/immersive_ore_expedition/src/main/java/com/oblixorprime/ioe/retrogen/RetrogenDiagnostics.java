package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class RetrogenDiagnostics {
    private final ResourcePolicyService resourcePolicy;
    private final LoadedResourceScanner scanner;

    public RetrogenDiagnostics(ResourcePolicyService resourcePolicy, LoadedResourceScanner scanner) {
        this.resourcePolicy = Objects.requireNonNull(resourcePolicy, "resourcePolicy");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public ResourceValidationReport validateResources(Collection<ResourceRef> resources) {
        Objects.requireNonNull(resources, "resources");
        int usable = 0;
        int skipped = 0;
        int rejected = 0;
        List<DiagnosticFinding> findings = new ArrayList<>();

        for (ResourceRef resource : resources) {
            ResourcePolicyDecision decision = resourcePolicy.evaluate(resource, scanner);
            switch (decision.action()) {
                case USE -> usable++;
                case SKIP -> skipped++;
                case REJECT -> rejected++;
            }
            findings.add(new DiagnosticFinding(resource, decision.action(), decision.reason()));
        }

        return new ResourceValidationReport(usable, skipped, rejected, findings);
    }
}
