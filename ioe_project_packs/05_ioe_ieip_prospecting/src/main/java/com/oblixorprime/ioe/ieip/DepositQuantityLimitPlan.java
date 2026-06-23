package com.oblixorprime.ioe.ieip;

import java.util.Objects;

public record DepositQuantityLimitPlan(
        String depositId,
        int originalQuantity,
        int scaledQuantity,
        double multiplier,
        boolean hardMode
) {
    public DepositQuantityLimitPlan {
        Objects.requireNonNull(depositId, "depositId");
        if (depositId.isBlank()) {
            throw new IllegalArgumentException("depositId must not be blank");
        }
        if (originalQuantity < 0) {
            throw new IllegalArgumentException("originalQuantity must not be negative");
        }
        if (scaledQuantity < 0) {
            throw new IllegalArgumentException("scaledQuantity must not be negative");
        }
        if (scaledQuantity > originalQuantity) {
            throw new IllegalArgumentException("scaledQuantity must not exceed originalQuantity");
        }
        if (multiplier < 0.0D || multiplier > 1.0D) {
            throw new IllegalArgumentException("multiplier must be between 0 and 1");
        }
    }
}
