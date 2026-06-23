package com.oblixorprime.ioe.core;

import java.util.Objects;

public record ResourcePolicyDecision(Action action, String reason) {
    public ResourcePolicyDecision {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(reason, "reason");
    }

    public boolean shouldUse() {
        return action == Action.USE;
    }

    public boolean shouldSkip() {
        return action == Action.SKIP;
    }

    public boolean rejected() {
        return action == Action.REJECT;
    }

    public static ResourcePolicyDecision use(String reason) {
        return new ResourcePolicyDecision(Action.USE, reason);
    }

    public static ResourcePolicyDecision skip(String reason) {
        return new ResourcePolicyDecision(Action.SKIP, reason);
    }

    public static ResourcePolicyDecision reject(String reason) {
        return new ResourcePolicyDecision(Action.REJECT, reason);
    }

    public enum Action {
        USE,
        SKIP,
        REJECT
    }
}
