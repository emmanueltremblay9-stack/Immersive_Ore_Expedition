package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Resolves one quality at a time and distinguishes a level that does not require an IE deposit from a
 * level whose required deposit attempt actually failed.
 */
final class IoeSiteQualityFallbackResolver {
    private IoeSiteQualityFallbackResolver() {
    }

    static Resolution resolve(
            SiteQuality initialQuality,
            Predicate<SiteQuality> enabledQuality,
            DepositGate depositGate,
            QualityTransition transition
    ) {
        Objects.requireNonNull(initialQuality, "initialQuality");
        Objects.requireNonNull(enabledQuality, "enabledQuality");
        Objects.requireNonNull(depositGate, "depositGate");
        Objects.requireNonNull(transition, "transition");

        EnumSet<SiteQuality> attempted = EnumSet.noneOf(SiteQuality.class);
        ArrayList<SiteQuality> orderedAttempts = new ArrayList<>();
        SiteQuality quality = initialQuality;
        while (attempted.add(quality)) {
            orderedAttempts.add(quality);
            DepositAttempt attempt = Objects.requireNonNull(
                    depositGate.attempt(quality),
                    "depositGate result"
            );
            if (attempt != DepositAttempt.FAILED) {
                return Resolution.confirmed(quality, orderedAttempts);
            }

            Optional<SiteQuality> lower = directLowerEnabledQuality(quality, enabledQuality);
            if (lower.isEmpty() || !transition.apply(quality, lower.orElseThrow())) {
                return Resolution.rejected(quality, orderedAttempts);
            }
            quality = lower.orElseThrow();
        }
        return Resolution.rejected(quality, orderedAttempts);
    }

    static Optional<SiteQuality> directLowerEnabledQuality(
            SiteQuality quality,
            Predicate<SiteQuality> enabledQuality
    ) {
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(enabledQuality, "enabledQuality");
        Optional<SiteQuality> candidate = quality.directLower();
        while (candidate.isPresent() && !enabledQuality.test(candidate.orElseThrow())) {
            candidate = candidate.orElseThrow().directLower();
        }
        return candidate;
    }

    enum DepositAttempt {
        NOT_REQUIRED,
        RESOLVED,
        FAILED
    }

    @FunctionalInterface
    interface DepositGate {
        DepositAttempt attempt(SiteQuality quality);
    }

    @FunctionalInterface
    interface QualityTransition {
        boolean apply(SiteQuality currentQuality, SiteQuality lowerQuality);
    }

    record Resolution(
            boolean confirmed,
            SiteQuality finalQuality,
            List<SiteQuality> attemptedQualities
    ) {
        Resolution {
            Objects.requireNonNull(finalQuality, "finalQuality");
            attemptedQualities = List.copyOf(Objects.requireNonNull(attemptedQualities, "attemptedQualities"));
            if (attemptedQualities.isEmpty() || attemptedQualities.getLast() != finalQuality) {
                throw new IllegalArgumentException("Final quality must be the last attempted quality");
            }
        }

        private static Resolution confirmed(SiteQuality quality, List<SiteQuality> attemptedQualities) {
            return new Resolution(true, quality, attemptedQualities);
        }

        private static Resolution rejected(SiteQuality quality, List<SiteQuality> attemptedQualities) {
            return new Resolution(false, quality, attemptedQualities);
        }
    }
}
