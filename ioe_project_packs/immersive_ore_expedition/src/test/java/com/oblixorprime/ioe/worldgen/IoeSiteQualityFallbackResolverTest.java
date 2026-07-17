package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IoeSiteQualityFallbackResolverTest {
    @Test
    void existingOrNewDepositKeepsMotherQuality() {
        for (String successfulPath : List.of("existing", "created")) {
            ArrayList<String> transitions = new ArrayList<>();
            IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                    SiteQuality.MOTHERLODE,
                    ignored -> true,
                    ignored -> IoeSiteQualityFallbackResolver.DepositAttempt.RESOLVED,
                    (current, lower) -> {
                        transitions.add(successfulPath + ":" + current + "->" + lower);
                        return true;
                    }
            );

            assertTrue(result.confirmed());
            assertEquals(SiteQuality.MOTHERLODE, result.finalQuality());
            assertEquals(List.of(SiteQuality.MOTHERLODE), result.attemptedQualities());
            assertTrue(transitions.isEmpty());
        }
    }

    @Test
    void failedMotherDepositDowngradesExactlyToRich() {
        ArrayList<String> transitions = new ArrayList<>();
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                ignored -> true,
                quality -> quality == SiteQuality.MOTHERLODE
                        ? IoeSiteQualityFallbackResolver.DepositAttempt.FAILED
                        : IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED,
                (current, lower) -> {
                    transitions.add(current + "->" + lower);
                    return true;
                }
        );

        assertTrue(result.confirmed());
        assertEquals(SiteQuality.RICH, result.finalQuality());
        assertEquals(List.of(SiteQuality.MOTHERLODE, SiteQuality.RICH), result.attemptedQualities());
        assertEquals(List.of("MOTHERLODE->RICH"), transitions);
    }

    @Test
    void repeatedRequiredFailuresWalkOneLevelAtATime() {
        ArrayList<String> transitions = new ArrayList<>();
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                ignored -> true,
                quality -> switch (quality) {
                    case MOTHERLODE, RICH -> IoeSiteQualityFallbackResolver.DepositAttempt.FAILED;
                    case NORMAL, POOR, DRY -> IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
                },
                (current, lower) -> {
                    transitions.add(current + "->" + lower);
                    return true;
                }
        );

        assertTrue(result.confirmed());
        assertEquals(SiteQuality.NORMAL, result.finalQuality());
        assertEquals(List.of("MOTHERLODE->RICH", "RICH->NORMAL"), transitions);
    }

    @Test
    void disabledIntermediateQualityIsSkippedDeterministically() {
        assertEquals(
                SiteQuality.NORMAL,
                IoeSiteQualityFallbackResolver.directLowerEnabledQuality(
                        SiteQuality.MOTHERLODE,
                        quality -> quality != SiteQuality.RICH
                ).orElseThrow()
        );
    }

    @Test
    void minimumQualityFailureRejectsWithoutLooping() {
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.DRY,
                ignored -> true,
                ignored -> IoeSiteQualityFallbackResolver.DepositAttempt.FAILED,
                (current, lower) -> true
        );

        assertFalse(result.confirmed());
        assertEquals(SiteQuality.DRY, result.finalQuality());
        assertEquals(List.of(SiteQuality.DRY), result.attemptedQualities());
    }

    @Test
    void nonDepositSiteDoesNotTriggerDowngrade() {
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                ignored -> true,
                ignored -> IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED,
                (current, lower) -> false
        );

        assertTrue(result.confirmed());
        assertEquals(SiteQuality.MOTHERLODE, result.finalQuality());
        assertEquals(List.of(SiteQuality.MOTHERLODE), result.attemptedQualities());
    }

    @Test
    void failedAtomicTransitionRejectsAtCurrentQuality() {
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                ignored -> true,
                ignored -> IoeSiteQualityFallbackResolver.DepositAttempt.FAILED,
                (current, lower) -> false
        );

        assertFalse(result.confirmed());
        assertEquals(SiteQuality.MOTHERLODE, result.finalQuality());
        assertEquals(List.of(SiteQuality.MOTHERLODE), result.attemptedQualities());
    }

    @Test
    void requiredDepositFailureWalksMotherMajorMinorDirectAndNeverFallsThroughToDry() {
        ArrayList<String> transitions = new ArrayList<>();
        IoeSiteQualityFallbackResolver.Resolution result = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                SiteQuality::isProductive,
                ignored -> IoeSiteQualityFallbackResolver.DepositAttempt.FAILED,
                (current, lower) -> {
                    transitions.add(current + "->" + lower);
                    return true;
                }
        );

        assertFalse(result.confirmed());
        assertEquals(SiteQuality.POOR, result.finalQuality());
        assertEquals(
                List.of(SiteQuality.MOTHERLODE, SiteQuality.RICH, SiteQuality.NORMAL, SiteQuality.POOR),
                result.attemptedQualities()
        );
        assertEquals(
                List.of("MOTHERLODE->RICH", "RICH->NORMAL", "NORMAL->POOR"),
                transitions
        );
    }
}
