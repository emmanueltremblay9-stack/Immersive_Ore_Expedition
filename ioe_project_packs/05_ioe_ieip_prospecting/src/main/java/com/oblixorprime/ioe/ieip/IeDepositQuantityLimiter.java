package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;

import java.util.Objects;
import java.util.Optional;

public final class IeDepositQuantityLimiter {
    private final LoadedResourceScanner scanner;

    public IeDepositQuantityLimiter() {
        this(LoadedResourceScanner.runtime());
    }

    public IeDepositQuantityLimiter(LoadedResourceScanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public void install() {
        if (ProspectingCompatGates.immersiveEngineeringEnabled(scanner)) {
            IoeIeipProspectingMod.LOGGER.info(
                    "IE deposit quantity limiting policy is active; supported IE quantity hook is not registered in this alpha build."
            );
        }
    }

    public Optional<DepositQuantityLimitPlan> planQuantityLimit(String depositId, int originalQuantity, boolean hardMode) {
        return planQuantityLimit(depositId, originalQuantity, hardMode, scanner);
    }

    public Optional<DepositQuantityLimitPlan> planQuantityLimit(
            String depositId,
            int originalQuantity,
            boolean hardMode,
            LoadedResourceScanner scanner
    ) {
        Objects.requireNonNull(depositId, "depositId");
        Objects.requireNonNull(scanner, "scanner");
        if (depositId.isBlank()
                || originalQuantity < 0
                || !ProspectingCompatGates.immersiveEngineeringEnabled(scanner)
                || !IoeIeipProspectingConfig.reduceMineralDepositQuantity()) {
            return Optional.empty();
        }

        double multiplier = hardMode
                ? IoeIeipProspectingConfig.hardModeDepositQuantityMultiplier()
                : IoeIeipProspectingConfig.depositQuantityMultiplier();
        int scaledQuantity = scaleDepositQuantity(originalQuantity, multiplier);
        return Optional.of(new DepositQuantityLimitPlan(
                depositId,
                originalQuantity,
                scaledQuantity,
                multiplier,
                hardMode
        ));
    }

    public static int scaleDepositQuantity(int originalQuantity, double multiplier) {
        if (originalQuantity <= 0 || !Double.isFinite(multiplier) || multiplier <= 0.0D) {
            return 0;
        }
        int scaled = Math.max(1, (int) Math.floor(originalQuantity * multiplier));
        return Math.min(originalQuantity, scaled);
    }
}
