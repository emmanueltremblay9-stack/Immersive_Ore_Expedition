package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbandonedProspectorCampComposerTest {
    private static final BlockPos SHAFT_ORIGIN = new BlockPos(4, 90, 6);
    private static final BlockPos METAMORPHIC_SHAFT_ORIGIN = new BlockPos(4, 90, 4);
    private static final int EXPECTED_SUPPORTED_PAIR_COUNT = 18;
    private static final int EXPECTED_ROTATION_COUNT = 4;
    private static final int EXPECTED_SUPPORTED_CASE_COUNT = 72;
    private static final int EXPECTED_UNSUPPORTED_PAIR_COUNT = 7;
    private static final long SEED_SEARCH_LIMIT = 100_000L;
    private static final List<SupportedPair> SUPPORTED_PAIRS = List.of(
            new SupportedPair(SiteQuality.DRY, AbandonedProspectorCampState.FAILED_PROSPECTION),
            new SupportedPair(SiteQuality.DRY, AbandonedProspectorCampState.WEATHERED),
            new SupportedPair(SiteQuality.DRY, AbandonedProspectorCampState.COLLAPSED),
            new SupportedPair(SiteQuality.POOR, AbandonedProspectorCampState.FAILED_PROSPECTION),
            new SupportedPair(SiteQuality.POOR, AbandonedProspectorCampState.WEATHERED),
            new SupportedPair(SiteQuality.POOR, AbandonedProspectorCampState.COLLAPSED),
            new SupportedPair(SiteQuality.POOR, AbandonedProspectorCampState.RECENTLY_ABANDONED),
            new SupportedPair(SiteQuality.NORMAL, AbandonedProspectorCampState.RECENTLY_ABANDONED),
            new SupportedPair(SiteQuality.NORMAL, AbandonedProspectorCampState.WEATHERED),
            new SupportedPair(SiteQuality.NORMAL, AbandonedProspectorCampState.COLLAPSED),
            new SupportedPair(SiteQuality.NORMAL, AbandonedProspectorCampState.EVACUATED),
            new SupportedPair(SiteQuality.RICH, AbandonedProspectorCampState.RECENTLY_ABANDONED),
            new SupportedPair(SiteQuality.RICH, AbandonedProspectorCampState.WEATHERED),
            new SupportedPair(SiteQuality.RICH, AbandonedProspectorCampState.COLLAPSED),
            new SupportedPair(SiteQuality.RICH, AbandonedProspectorCampState.EVACUATED),
            new SupportedPair(SiteQuality.MOTHERLODE, AbandonedProspectorCampState.RECENTLY_ABANDONED),
            new SupportedPair(SiteQuality.MOTHERLODE, AbandonedProspectorCampState.COLLAPSED),
            new SupportedPair(SiteQuality.MOTHERLODE, AbandonedProspectorCampState.EVACUATED)
    );
    private static final Map<SupportedKey, Long> SUPPORTED_SEEDS = discoverSupportedSeeds();
    private static final Comparator<BlockPos> POSITION_ORDER = Comparator
            .comparingInt((BlockPos pos) -> pos.getX())
            .thenComparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getZ);

    static Stream<Arguments> supportedCases() {
        List<Arguments> cases = new ArrayList<>();
        for (SupportedPair pair : SUPPORTED_PAIRS) {
            for (Rotation rotation : Rotation.values()) {
                SupportedKey key = new SupportedKey(pair.quality(), pair.state(), rotation);
                cases.add(Arguments.of(new SupportedCase(
                        pair.quality(),
                        pair.state(),
                        rotation,
                        SUPPORTED_SEEDS.get(key)
                )));
            }
        }
        assertEquals(EXPECTED_ROTATION_COUNT, Rotation.values().length);
        assertEquals(EXPECTED_SUPPORTED_PAIR_COUNT * EXPECTED_ROTATION_COUNT, cases.size());
        assertEquals(EXPECTED_SUPPORTED_CASE_COUNT, cases.size());
        return cases.stream();
    }

    static Stream<Arguments> supportedPairs() {
        assertEquals(EXPECTED_SUPPORTED_PAIR_COUNT, SUPPORTED_PAIRS.size());
        return SUPPORTED_PAIRS.stream().map(pair -> Arguments.of(pair.quality(), pair.state()));
    }

    static Stream<Arguments> metamorphicCases() {
        List<Arguments> cases = SUPPORTED_PAIRS.stream()
                .map(pair -> Arguments.of(
                        pair.quality(),
                        pair.state(),
                        findCommonSeed(pair.quality(), pair.state())
                ))
                .toList();
        assertEquals(EXPECTED_SUPPORTED_PAIR_COUNT, cases.size());
        return cases.stream();
    }

    private static final Comparator<List<BlockPos>> POSITION_LIST_ORDER = (first, second) -> {
        int commonSize = Math.min(first.size(), second.size());
        for (int index = 0; index < commonSize; index++) {
            int comparison = POSITION_ORDER.compare(first.get(index), second.get(index));
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(first.size(), second.size());
    };
    private static final Comparator<List<BoundarySide>> BOUNDARY_LIST_ORDER = (first, second) -> {
        int commonSize = Math.min(first.size(), second.size());
        for (int index = 0; index < commonSize; index++) {
            int comparison = Integer.compare(first.get(index).ordinal(), second.get(index).ordinal());
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(first.size(), second.size());
    };
    private static final Comparator<NormalizedObservation> NORMALIZED_OBSERVATION_ORDER = Comparator
            .comparing(
                    (NormalizedObservation observation) -> observation.shelterColumns(),
                    POSITION_LIST_ORDER
            )
            .thenComparing(NormalizedObservation::cardinalAccessOffsets, POSITION_LIST_ORDER)
            .thenComparing(NormalizedObservation::entranceBoundaryClassification, BOUNDARY_LIST_ORDER)
            .thenComparingInt(NormalizedObservation::shelterPostHeight);

    static Stream<Arguments> roleSensitiveReachabilityRegressionCases() {
        return Stream.of(
                Arguments.of(SiteQuality.NORMAL, AbandonedProspectorCampState.COLLAPSED, Rotation.NONE, 21L),
                Arguments.of(SiteQuality.RICH, AbandonedProspectorCampState.RECENTLY_ABANDONED, Rotation.NONE, 50L),
                Arguments.of(SiteQuality.RICH, AbandonedProspectorCampState.WEATHERED, Rotation.NONE, 25L),
                Arguments.of(SiteQuality.RICH, AbandonedProspectorCampState.COLLAPSED, Rotation.NONE, 21L),
                Arguments.of(SiteQuality.MOTHERLODE, AbandonedProspectorCampState.RECENTLY_ABANDONED, Rotation.NONE, 51L),
                Arguments.of(
                        SiteQuality.MOTHERLODE,
                        AbandonedProspectorCampState.RECENTLY_ABANDONED,
                        Rotation.COUNTERCLOCKWISE_90,
                        8L
                ),
                Arguments.of(SiteQuality.MOTHERLODE, AbandonedProspectorCampState.COLLAPSED, Rotation.NONE, 6L)
        );
    }

    static Stream<Arguments> unsupportedCases() {
        List<Arguments> cases = new ArrayList<>();
        Set<SupportedPair> supported = Set.copyOf(SUPPORTED_PAIRS);
        for (SiteQuality quality : SiteQuality.values()) {
            for (AbandonedProspectorCampState state : AbandonedProspectorCampState.values()) {
                if (!supported.contains(new SupportedPair(quality, state))) {
                    cases.add(Arguments.of(quality, state));
                }
            }
        }
        assertEquals(EXPECTED_UNSUPPORTED_PAIR_COUNT, cases.size());
        return cases.stream();
    }

    @Test
    void liveEligibilityExactlyMatchesHandAuthoredGolden() {
        for (SiteQuality quality : SiteQuality.values()) {
            List<AbandonedProspectorCampState> expected = SUPPORTED_PAIRS.stream()
                    .filter(pair -> pair.quality() == quality)
                    .map(SupportedPair::state)
                    .toList();
            assertEquals(expected, AbandonedProspectorCampState.eligibleFor(quality), quality.name());
        }
    }

    @ParameterizedTest(name = "supported[{index}] {0}")
    @MethodSource("supportedCases")
    void everySupportedStateQualityRotationSatisfiesIndependentOutputInvariants(SupportedCase testCase) {
        String diagnostic = testCase.toString();
        ProspectorCampContext context = context(testCase.seed());
        assertEquals(testCase.rotation(), context.rotation(), diagnostic);
        assertEquals(
                testCase.state(),
                AbandonedProspectorCampState.select(testCase.seed(), testCase.quality()),
                diagnostic
        );

        ProspectorCampOutcropComposer.Composition active = ProspectorCampOutcropComposer.compose(
                SHAFT_ORIGIN,
                testCase.quality(),
                context
        );
        AbandonedProspectorCampComposer.Composition first = AbandonedProspectorCampComposer.compose(
                SHAFT_ORIGIN,
                testCase.quality(),
                context
        );
        AbandonedProspectorCampComposer.Composition second = AbandonedProspectorCampComposer.compose(
                SHAFT_ORIGIN,
                testCase.quality(),
                context
        );

        assertEquals(testCase.state(), first.abandonedState(), diagnostic);
        assertEquals(testCase.rotation(), first.rotation(), diagnostic);
        assertEquals(first.blocks(), second.blocks(), diagnostic);
        assertEquals(first.blockEntityPayloads(), second.blockEntityPayloads(), diagnostic);
        assertEquals(first.reservedSurfaceColumns(), second.reservedSurfaceColumns(), diagnostic);
        assertEquals(first.shelterEntrance(), second.shelterEntrance(), diagnostic);
        assertEquals(canonicalDigest(first), canonicalDigest(second), diagnostic);

        assertCommonOutputInvariants(first, active, diagnostic);
        assertStateSpecificObservable(first, active, diagnostic);
    }

    @ParameterizedTest(name = "{index}: {0}/{1}/{2}/seed={3}")
    @MethodSource("roleSensitiveReachabilityRegressionCases")
    void roleSensitiveTerrainSupportsKnownReachabilityRegressions(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            Rotation rotation,
            long seed
    ) {
        String diagnostic = diagnostic(quality, state, rotation, seed);
        ProspectorCampContext context = context(seed);
        assertEquals(rotation, context.rotation(), diagnostic);
        assertEquals(state, AbandonedProspectorCampState.select(seed, quality), diagnostic);

        ProspectorCampOutcropComposer.Composition active = ProspectorCampOutcropComposer.compose(
                SHAFT_ORIGIN,
                quality,
                context
        );
        AbandonedProspectorCampComposer.Composition composition = AbandonedProspectorCampComposer.compose(
                SHAFT_ORIGIN,
                quality,
                context
        );
        assertEquals(state, composition.abandonedState(), diagnostic);

        ReachabilityEvidence evidence = assertFinalOccupancyReachability(
                composition,
                active.shaftHatchColumns(),
                diagnostic
        );
        System.out.println(
                "ROLE_AWARE_CASE_EVIDENCE: CASE=" + diagnostic
                        + "; VEGETATION_COLUMNS_OBSERVED=" + evidence.vegetationColumnsObserved()
                        + "; SAMPLE_COLUMNS_OBSERVED=" + evidence.sampleColumnsObserved()
                        + "; ROLE_AWARE_COLUMNS_TRAVERSED=" + evidence.roleAwareColumnsTraversed()
                        + "; ENTRY_CANDIDATE_COUNT=" + evidence.entryCandidateCount()
                        + "; PATH_COUNT=" + evidence.pathCount()
                        + "; REACHABLE_PATH_COUNT=" + evidence.reachablePathCount()
                        + "; RESULT=PASS"
        );
    }

    @ParameterizedTest(name = "rotation topology[{index}] {0}/{1}")
    @MethodSource("metamorphicCases")
    void inverseNormalizedObservableTopologyIsRotationMetamorphic(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            long seed
    ) {
        String commonDiagnostic = "quality=" + quality + ", state=" + state + ", commonSeed=" + seed;
        assertEquals(state, AbandonedProspectorCampState.select(seed, quality), commonDiagnostic);
        ProspectorCampContext commonContext = context(seed);
        Map<Rotation, NormalizedObservation> observations = new EnumMap<>(Rotation.class);
        List<Rotation> explicitRotations = new ArrayList<>();
        int entrancesOnMaximumZ = 0;
        int accessesBeyondMaximumZ = 0;
        for (Rotation rotation : Rotation.values()) {
            String diagnostic = diagnostic(quality, state, rotation, seed);
            AbandonedProspectorCampComposer.Composition composition = AbandonedProspectorCampComposer.compose(
                    METAMORPHIC_SHAFT_ORIGIN,
                    quality,
                    commonContext,
                    rotation
            );
            assertEquals(state, composition.abandonedState(), diagnostic);
            assertEquals(rotation, composition.rotation(), diagnostic);
            explicitRotations.add(rotation);
            observations.put(rotation, inverseNormalizedObservation(composition, METAMORPHIC_SHAFT_ORIGIN));

            Set<BlockPos> shelter = columnsWithRole(
                    composition,
                    ProspectorCampOutcropComposer.ComponentRole.SHELTER
            );
            int maximumZ = shelter.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
            if (composition.shelterEntrance().getZ() == maximumZ) {
                entrancesOnMaximumZ++;
            }
            Set<BlockPos> route = routeColumns(composition);
            List<BlockPos> adjacentAccess = route.stream()
                    .filter(pos -> cardinallyAdjacent(pos, composition.shelterEntrance()))
                    .toList();
            assertFalse(adjacentAccess.isEmpty(), diagnostic);
            assertBoundaryOutwardAccess(composition.shelterEntrance(), shelter, adjacentAccess, diagnostic);
            if (adjacentAccess.stream().anyMatch(pos -> pos.getZ() > maximumZ)) {
                accessesBeyondMaximumZ++;
            }
        }

        int uniqueObservationCount = new HashSet<>(observations.values()).size();
        assertEquals(EXPECTED_ROTATION_COUNT, explicitRotations.size());
        assertEquals(EXPECTED_ROTATION_COUNT, observations.size());
        assertEquals(1, uniqueObservationCount, quality + "/" + state + observations);
        assertTrue(entrancesOnMaximumZ < EXPECTED_ROTATION_COUNT, quality + "/" + state);
        assertTrue(accessesBeyondMaximumZ < EXPECTED_ROTATION_COUNT, quality + "/" + state);
        System.out.println(
                "METAMORPHIC_CASE_EVIDENCE: QUALITY=" + quality
                        + "; STATE=" + state
                        + "; COMMON_SEED=" + seed
                        + "; ROTATIONS=" + explicitRotations
                        + "; UNIQUE_COUNT=" + uniqueObservationCount
                        + "; ENTRANCES_ON_MAXIMUM_Z=" + entrancesOnMaximumZ
                        + "; ACCESSES_BEYOND_MAXIMUM_Z=" + accessesBeyondMaximumZ
                        + "; RESULT=PASS"
        );
    }

    @ParameterizedTest(name = "unsupported[{index}] {0}/{1}")
    @MethodSource("unsupportedCases")
    void unsupportedStateQualityPairsRemainExcludedWithoutStateRemapping(
            SiteQuality quality,
            AbandonedProspectorCampState unsupportedState
    ) {
        assertFalse(AbandonedProspectorCampState.eligibleFor(quality).contains(unsupportedState));
        for (Rotation rotation : Rotation.values()) {
            long seed = findSeedForRotation(rotation);
            String diagnostic = diagnostic(quality, unsupportedState, rotation, seed);
            AbandonedProspectorCampComposer.Composition composition = AbandonedProspectorCampComposer.compose(
                    SHAFT_ORIGIN,
                    quality,
                    context(seed)
            );
            assertNotEquals(unsupportedState, composition.abandonedState(), diagnostic);
            assertTrue(
                    AbandonedProspectorCampState.eligibleFor(quality).contains(composition.abandonedState()),
                    diagnostic
            );
        }
    }

    private static void assertCommonOutputInvariants(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            String diagnostic
    ) {
        assertFalse(composition.blocks().isEmpty(), diagnostic);
        assertEquals(composition.blocks().size(), new HashSet<>(composition.blocks().keySet()).size(), diagnostic);
        assertTrue(composition.blocks().keySet().containsAll(composition.blockEntityPayloads().keySet()), diagnostic);
        composition.blockEntityPayloads().keySet().forEach(pos ->
                assertTrue(composition.blocks().get(pos).hasBlockEntity(), diagnostic + " payload=" + pos));

        ChunkPos anchorChunk = new ChunkPos(SHAFT_ORIGIN);
        int minimumY = SHAFT_ORIGIN.getY() - 1;
        int maximumY = SHAFT_ORIGIN.getY() + composition.qualitySpec().maxHeight() - 1;
        composition.blocks().keySet().forEach(pos -> {
            assertEquals(anchorChunk, new ChunkPos(pos), diagnostic + " pos=" + pos);
            assertTrue(pos.getY() >= minimumY && pos.getY() <= maximumY, diagnostic + " pos=" + pos);
        });
        assertTrue(horizontalSpan(composition.blocks().keySet(), true)
                <= composition.qualitySpec().placedFootprint(), diagnostic);
        assertTrue(horizontalSpan(composition.blocks().keySet(), false)
                <= composition.qualitySpec().placedFootprint(), diagnostic);

        Set<BlockPos> route = routeColumns(composition);
        assertFalse(route.isEmpty(), diagnostic);
        assertTrue(route.stream().noneMatch(active.shaftHatchColumns()::contains), diagnostic);
        assertTrue(active.shaftHatchColumns().stream().noneMatch(composition.shelterEntrance()::equals), diagnostic);
        assertTrue(route.stream().anyMatch(pos -> cardinallyAdjacent(pos, composition.shelterEntrance())), diagnostic);
        assertFinalOccupancyReachability(composition, active.shaftHatchColumns(), diagnostic);

        assertAir(composition.blocks(), composition.shelterEntrance(), diagnostic);
        assertAir(composition.blocks(), composition.shelterEntrance().above(), diagnostic);
        active.shaftHatchColumns().forEach(hatch -> {
            assertAir(composition.blocks(), hatch.above(), diagnostic);
            assertAir(composition.blocks(), hatch.above(2), diagnostic);
        });
        composition.blocks().values().forEach(state -> {
            assertFalse(state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA), diagnostic);
            if (state.hasProperty(CampfireBlock.LIT)) {
                assertFalse(state.getValue(CampfireBlock.LIT), diagnostic);
            }
        });
    }

    private static void assertStateSpecificObservable(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            String diagnostic
    ) {
        switch (composition.abandonedState()) {
            case RECENTLY_ABANDONED -> assertTrue(
                    changedAtRole(composition, active, ProspectorCampOutcropComposer.ComponentRole.SAMPLE),
                    diagnostic
            );
            case WEATHERED -> {
                assertTrue(changedBelowRole(composition, active, ProspectorCampOutcropComposer.ComponentRole.PATH),
                        diagnostic);
                assertTrue(hasClearedShelterRoof(composition, active), diagnostic);
            }
            case COLLAPSED -> {
                assertNotEquals(active.reservedSurfaceColumns(), composition.reservedSurfaceColumns(), diagnostic);
                assertTrue(hasClearedShelterRoof(composition, active), diagnostic);
            }
            case FAILED_PROSPECTION -> assertTrue(
                    changedAtAnyRole(
                            composition,
                            active,
                            Set.of(
                                    ProspectorCampOutcropComposer.ComponentRole.SAMPLE,
                                    ProspectorCampOutcropComposer.ComponentRole.OUTCROP
                            )
                    ),
                    diagnostic
            );
            case EVACUATED -> assertTrue(
                    changedToAirAtRole(
                            composition,
                            active,
                            ProspectorCampOutcropComposer.ComponentRole.WORKSTATION
                    ),
                    diagnostic
            );
        }
    }

    private static boolean changedAtRole(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return changedAtAnyRole(composition, active, Set.of(role));
    }

    private static boolean changedAtAnyRole(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            Set<ProspectorCampOutcropComposer.ComponentRole> roles
    ) {
        return composition.reservedSurfaceColumns().entrySet().stream()
                .filter(entry -> roles.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .anyMatch(pos -> !Objects.equals(composition.blocks().get(pos), active.blocks().get(pos)));
    }

    private static boolean changedBelowRole(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return composition.reservedSurfaceColumns().entrySet().stream()
                .filter(entry -> entry.getValue() == role)
                .map(entry -> entry.getKey().below())
                .anyMatch(pos -> !Objects.equals(composition.blocks().get(pos), active.blocks().get(pos)));
    }

    private static boolean changedToAirAtRole(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return composition.reservedSurfaceColumns().entrySet().stream()
                .filter(entry -> entry.getValue() == role)
                .map(Map.Entry::getKey)
                .anyMatch(pos -> isAir(composition.blocks().get(pos)) && !isAir(active.blocks().get(pos)));
    }

    private static boolean hasClearedShelterRoof(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.Composition active
    ) {
        int roofOffset = composition.qualitySpec().postHeight();
        return columnsWithRole(composition, ProspectorCampOutcropComposer.ComponentRole.SHELTER).stream()
                .map(pos -> pos.above(roofOffset))
                .anyMatch(pos -> isAir(composition.blocks().get(pos)) && !isAir(active.blocks().get(pos)));
    }

    private static NormalizedObservation inverseNormalizedObservation(
            AbandonedProspectorCampComposer.Composition composition,
            BlockPos shaftOrigin
    ) {
        BlockPos campCenter = new BlockPos(
                new ChunkPos(shaftOrigin).getMinBlockX() + 7,
                shaftOrigin.getY(),
                new ChunkPos(shaftOrigin).getMinBlockZ() + 7
        );
        Set<BlockPos> shelter = new HashSet<>();
        Set<BlockPos> route = new HashSet<>();
        composition.reservedSurfaceColumns().forEach((pos, role) -> {
            BlockPos normalizedPosition = relativeTo(pos, campCenter)
                    .rotate(inverse(composition.rotation()));
            if (role == ProspectorCampOutcropComposer.ComponentRole.SHELTER) {
                shelter.add(normalizedPosition);
            } else if (role == ProspectorCampOutcropComposer.ComponentRole.PATH
                    || role == ProspectorCampOutcropComposer.ComponentRole.ACCESS) {
                route.add(normalizedPosition);
            }
        });
        assertFalse(shelter.isEmpty(), "Normalized shelter footprint is empty");
        BlockPos normalizedEntrance = relativeTo(composition.shelterEntrance(), campCenter)
                .rotate(inverse(composition.rotation()));
        assertTrue(shelter.contains(normalizedEntrance),
                "Normalized shelter entrance is outside the shelter footprint: " + normalizedEntrance);
        List<BlockPos> adjacentAccess = route.stream()
                .filter(routePos -> cardinallyAdjacent(routePos, normalizedEntrance))
                .sorted(POSITION_ORDER)
                .toList();
        assertFalse(adjacentAccess.isEmpty(), "Normalized shelter entrance has no cardinal access");
        assertTrue(adjacentAccess.stream().allMatch(pos -> cardinallyAdjacent(pos, normalizedEntrance)),
                "Normalized shelter access is not same-Y cardinal");
        assertBoundaryOutwardAccess(
                normalizedEntrance,
                shelter,
                adjacentAccess,
                "rotation=" + composition.rotation()
        );
        List<BlockPos> accessOffsets = adjacentAccess.stream()
                .map(routePos -> relativeTo(routePos, normalizedEntrance))
                .sorted(POSITION_ORDER)
                .toList();
        assertFalse(accessOffsets.isEmpty(), "Normalized shelter access offsets are empty");
        assertTrue(accessOffsets.stream().allMatch(offset ->
                        offset.getY() == 0
                                && Math.abs(offset.getX()) + Math.abs(offset.getZ()) == 1),
                "Normalized shelter access offsets are not same-Y cardinal vectors");

        List<NormalizedObservation> candidates = new ArrayList<>();
        for (Rotation canonicalRotation : Rotation.values()) {
            List<BlockPos> rotatedShelter = shelter.stream()
                    .map(pos -> pos.rotate(canonicalRotation))
                    .toList();
            int minimumX = rotatedShelter.stream().mapToInt(BlockPos::getX).min().orElseThrow();
            int minimumY = rotatedShelter.stream().mapToInt(BlockPos::getY).min().orElseThrow();
            int minimumZ = rotatedShelter.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
            BlockPos anchor = new BlockPos(minimumX, minimumY, minimumZ);
            List<BlockPos> translatedShelter = rotatedShelter.stream()
                    .map(pos -> relativeTo(pos, anchor))
                    .sorted(POSITION_ORDER)
                    .toList();
            BlockPos translatedEntrance = relativeTo(normalizedEntrance.rotate(canonicalRotation), anchor);
            List<BlockPos> rotatedAccessOffsets = accessOffsets.stream()
                    .map(offset -> offset.rotate(canonicalRotation))
                    .sorted(POSITION_ORDER)
                    .toList();
            candidates.add(new NormalizedObservation(
                    translatedShelter,
                    composition.qualitySpec().postHeight(),
                    rotatedAccessOffsets,
                    boundarySides(translatedEntrance, Set.copyOf(translatedShelter))
            ));
        }
        assertEquals(EXPECTED_ROTATION_COUNT, candidates.size());
        return candidates.stream().min(NORMALIZED_OBSERVATION_ORDER).orElseThrow();
    }

    private static List<BoundarySide> boundarySides(BlockPos position, Set<BlockPos> shelter) {
        int minimumX = shelter.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maximumX = shelter.stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minimumZ = shelter.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maximumZ = shelter.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
        List<BoundarySide> sides = new ArrayList<>();
        if (position.getX() == minimumX) {
            sides.add(BoundarySide.MIN_X);
        }
        if (position.getX() == maximumX) {
            sides.add(BoundarySide.MAX_X);
        }
        if (position.getZ() == minimumZ) {
            sides.add(BoundarySide.MIN_Z);
        }
        if (position.getZ() == maximumZ) {
            sides.add(BoundarySide.MAX_Z);
        }
        assertFalse(sides.isEmpty(), "Shelter entrance is not on the observed shelter boundary: " + position);
        return List.copyOf(sides);
    }

    private static void assertBoundaryOutwardAccess(
            BlockPos entrance,
            Set<BlockPos> shelter,
            List<BlockPos> adjacentAccess,
            String diagnostic
    ) {
        assertTrue(shelter.contains(entrance), diagnostic + " entrance=" + entrance);
        assertFalse(boundarySides(entrance, shelter).isEmpty(), diagnostic);
        int minimumX = shelter.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maximumX = shelter.stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minimumZ = shelter.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maximumZ = shelter.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
        assertTrue(adjacentAccess.stream().anyMatch(pos ->
                pos.getX() < minimumX || pos.getX() > maximumX
                        || pos.getZ() < minimumZ || pos.getZ() > maximumZ), diagnostic);
    }

    private static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    private static BlockPos relativeTo(BlockPos position, BlockPos origin) {
        return new BlockPos(
                position.getX() - origin.getX(),
                position.getY() - origin.getY(),
                position.getZ() - origin.getZ()
        );
    }

    private static Set<BlockPos> routeColumns(AbandonedProspectorCampComposer.Composition composition) {
        return columnsWithRoles(
                composition.reservedSurfaceColumns(),
                Set.of(
                        ProspectorCampOutcropComposer.ComponentRole.PATH,
                        ProspectorCampOutcropComposer.ComponentRole.ACCESS
                )
        );
    }

    private static Set<BlockPos> columnsWithRole(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return columnsWithRole(composition.reservedSurfaceColumns(), role);
    }

    private static Set<BlockPos> columnsWithRole(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            ProspectorCampOutcropComposer.ComponentRole role
    ) {
        return columnsWithRoles(roles, Set.of(role));
    }

    private static Set<BlockPos> columnsWithRoles(
            Map<BlockPos, ProspectorCampOutcropComposer.ComponentRole> roles,
            Set<ProspectorCampOutcropComposer.ComponentRole> selected
    ) {
        Set<BlockPos> columns = new HashSet<>();
        roles.forEach((pos, role) -> {
            if (selected.contains(role)) {
                columns.add(pos);
            }
        });
        return columns;
    }

    private static ReachabilityEvidence assertFinalOccupancyReachability(
            AbandonedProspectorCampComposer.Composition composition,
            Set<BlockPos> shaftHatchColumns,
            String diagnostic
    ) {
        int standingY = composition.shelterEntrance().getY();
        Set<BlockPos> observableColumns = new HashSet<>();
        composition.blocks().keySet().forEach(pos -> observableColumns.add(standingColumn(pos, standingY)));
        composition.reservedSurfaceColumns().keySet().forEach(pos ->
                observableColumns.add(standingColumn(pos, standingY)));
        shaftHatchColumns.forEach(pos -> observableColumns.add(standingColumn(pos, standingY)));
        observableColumns.add(standingColumn(composition.shelterEntrance(), standingY));
        observableColumns.add(standingColumn(composition.shaftOrigin(), standingY));

        int domainMargin = 1;
        int minimumX = observableColumns.stream().mapToInt(BlockPos::getX).min().orElseThrow() - domainMargin;
        int maximumX = observableColumns.stream().mapToInt(BlockPos::getX).max().orElseThrow() + domainMargin;
        int minimumZ = observableColumns.stream().mapToInt(BlockPos::getZ).min().orElseThrow() - domainMargin;
        int maximumZ = observableColumns.stream().mapToInt(BlockPos::getZ).max().orElseThrow() + domainMargin;

        Set<BlockPos> excludedColumns = new HashSet<>();
        columnsWithRole(composition, ProspectorCampOutcropComposer.ComponentRole.SHELTER).forEach(pos ->
                excludedColumns.add(standingColumn(pos, standingY)));
        shaftHatchColumns.forEach(pos -> excludedColumns.add(standingColumn(pos, standingY)));
        excludedColumns.add(standingColumn(composition.shaftOrigin(), standingY));

        int supportY = standingY - 1;
        Set<BlockPos> syntheticSupport = new HashSet<>();
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                syntheticSupport.add(new BlockPos(x, supportY, z));
            }
        }
        Set<BlockPos> passable = new HashSet<>();
        for (BlockPos support : syntheticSupport) {
            BlockPos column = support.above();
            if (!excludedColumns.contains(column)
                    && isSyntheticPassableColumn(composition, column)) {
                passable.add(column);
            }
        }

        List<BlockPos> entryCandidates = columnsWithRole(
                composition,
                ProspectorCampOutcropComposer.ComponentRole.ACCESS
        ).stream()
                .map(pos -> standingColumn(pos, standingY))
                .filter(pos -> cardinallyAdjacent(pos, composition.shelterEntrance()))
                .distinct()
                .sorted(POSITION_ORDER)
                .limit(4)
                .toList();
        Set<BlockPos> pathTargets = new HashSet<>();
        columnsWithRole(composition, ProspectorCampOutcropComposer.ComponentRole.PATH).forEach(pos ->
                pathTargets.add(standingColumn(pos, standingY)));

        UnionFind occupancy = new UnionFind(passable);
        List<BlockPos> positions = List.copyOf(passable);
        for (int first = 0; first < positions.size(); first++) {
            for (int second = first + 1; second < positions.size(); second++) {
                if (cardinallyAdjacent(positions.get(first), positions.get(second))) {
                    occupancy.union(positions.get(first), positions.get(second));
                }
            }
        }
        List<BlockPos> passableEntries = entryCandidates.stream()
                .filter(passable::contains)
                .toList();
        long reachablePathCount = pathTargets.stream()
                .filter(passable::contains)
                .filter(path -> passableEntries.stream().anyMatch(entry -> occupancy.connected(entry, path)))
                .count();
        long vegetationColumnsObserved = countObservedRoleColumns(
                composition,
                ProspectorCampOutcropComposer.ComponentRole.VEGETATION,
                standingY
        );
        long sampleColumnsObserved = countObservedRoleColumns(
                composition,
                ProspectorCampOutcropComposer.ComponentRole.SAMPLE,
                standingY
        );
        long roleAwareColumnsTraversed = passable.stream()
                .filter(column -> {
                    ProspectorCampOutcropComposer.ComponentRole role =
                            composition.reservedSurfaceColumns().get(column);
                    return role == ProspectorCampOutcropComposer.ComponentRole.VEGETATION
                            || role == ProspectorCampOutcropComposer.ComponentRole.SAMPLE;
                })
                .filter(column -> passableEntries.stream()
                        .anyMatch(entry -> occupancy.connected(entry, column)))
                .count();
        String reachabilityDiagnostic = diagnostic
                + " entryCandidateCount=" + entryCandidates.size()
                + " entryCandidates=" + entryCandidates
                + " pathCount=" + pathTargets.size()
                + " reachablePathCount=" + reachablePathCount
                + " vegetationColumnsObserved=" + vegetationColumnsObserved
                + " sampleColumnsObserved=" + sampleColumnsObserved
                + " roleAwareColumnsTraversed=" + roleAwareColumnsTraversed
                + " syntheticBounds=[x=" + minimumX + ".." + maximumX
                + ", y=" + standingY + ", z=" + minimumZ + ".." + maximumZ + "]";
        assertTrue(reachablePathCount > 0, reachabilityDiagnostic);
        return new ReachabilityEvidence(
                vegetationColumnsObserved,
                sampleColumnsObserved,
                roleAwareColumnsTraversed,
                entryCandidates.size(),
                pathTargets.size(),
                reachablePathCount
        );
    }

    private static BlockPos standingColumn(BlockPos pos, int standingY) {
        return new BlockPos(pos.getX(), standingY, pos.getZ());
    }

    private static boolean isSyntheticAir(BlockState state) {
        return state == null || state.isAir();
    }

    private static boolean isSyntheticPassableColumn(
            AbandonedProspectorCampComposer.Composition composition,
            BlockPos column
    ) {
        ProspectorCampOutcropComposer.ComponentRole role = composition.reservedSurfaceColumns().get(column);
        if (role == ProspectorCampOutcropComposer.ComponentRole.VEGETATION
                || role == ProspectorCampOutcropComposer.ComponentRole.SAMPLE) {
            return isSyntheticAir(composition.blocks().get(column.above()))
                    && isSyntheticAir(composition.blocks().get(column.above(2)));
        }
        if (role == null
                || role == ProspectorCampOutcropComposer.ComponentRole.PATH
                || role == ProspectorCampOutcropComposer.ComponentRole.ACCESS) {
            return isSyntheticAir(composition.blocks().get(column))
                    && isSyntheticAir(composition.blocks().get(column.above()));
        }
        return false;
    }

    private static long countObservedRoleColumns(
            AbandonedProspectorCampComposer.Composition composition,
            ProspectorCampOutcropComposer.ComponentRole selectedRole,
            int standingY
    ) {
        return composition.reservedSurfaceColumns().entrySet().stream()
                .filter(entry -> entry.getValue() == selectedRole)
                .map(entry -> standingColumn(entry.getKey(), standingY))
                .distinct()
                .count();
    }

    private static boolean cardinallyAdjacent(BlockPos first, BlockPos second) {
        return first.getY() == second.getY()
                && Math.abs(first.getX() - second.getX()) + Math.abs(first.getZ() - second.getZ()) == 1;
    }

    private static int horizontalSpan(Set<BlockPos> positions, boolean xAxis) {
        assertFalse(positions.isEmpty());
        int minimum = positions.stream().mapToInt(pos -> xAxis ? pos.getX() : pos.getZ()).min().orElseThrow();
        int maximum = positions.stream().mapToInt(pos -> xAxis ? pos.getX() : pos.getZ()).max().orElseThrow();
        return maximum - minimum + 1;
    }

    private static void assertAir(Map<BlockPos, BlockState> blocks, BlockPos pos, String diagnostic) {
        BlockState state = blocks.get(pos);
        assertNotNull(state, diagnostic + " missing=" + pos);
        assertTrue(state.isAir(), diagnostic + " blocked=" + pos + " state=" + state);
    }

    private static boolean isAir(BlockState state) {
        return state != null && state.isAir();
    }

    private static String canonicalDigest(AbandonedProspectorCampComposer.Composition composition) {
        StringBuilder canonical = new StringBuilder();
        composition.blocks().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(POSITION_ORDER))
                .forEach(entry -> canonical.append("block:")
                        .append(position(entry.getKey())).append('=')
                        .append(BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock())).append('|')
                        .append(entry.getValue()).append('\n'));
        composition.blockEntityPayloads().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(POSITION_ORDER))
                .forEach(entry -> canonical.append("payload:")
                        .append(position(entry.getKey())).append('=')
                        .append(entry.getValue()).append('\n'));
        composition.reservedSurfaceColumns().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(POSITION_ORDER))
                .forEach(entry -> canonical.append("role:")
                        .append(position(entry.getKey())).append('=')
                        .append(entry.getValue().name()).append('\n'));
        canonical.append("state:").append(composition.abandonedState()).append('\n')
                .append("rotation:").append(composition.rotation()).append('\n')
                .append("entrance:").append(position(composition.shelterEntrance())).append('\n');
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }

    private static String position(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static ProspectorCampContext context(long seed) {
        return new ProspectorCampContext(
                ProspectorCampVisualFamily.TEMPERATE,
                seed,
                false,
                ProspectorCampArchetype.ABANDONED
        );
    }

    private static long findCommonSeed(
            SiteQuality quality,
            AbandonedProspectorCampState state
    ) {
        for (long seed = 0; seed < SEED_SEARCH_LIMIT; seed++) {
            if (AbandonedProspectorCampState.select(seed, quality) == state) {
                return seed;
            }
        }
        throw new AssertionError("No bounded common seed for " + quality + "/" + state);
    }

    private static long findSeed(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            Rotation rotation
    ) {
        for (long seed = 0; seed < SEED_SEARCH_LIMIT; seed++) {
            ProspectorCampContext context = context(seed);
            if (context.rotation() == rotation && AbandonedProspectorCampState.select(seed, quality) == state) {
                return seed;
            }
        }
        throw new AssertionError("No bounded seed for " + quality + "/" + state + "/" + rotation);
    }

    private static Map<SupportedKey, Long> discoverSupportedSeeds() {
        Map<SupportedKey, Long> seeds = new LinkedHashMap<>();
        for (SupportedPair pair : SUPPORTED_PAIRS) {
            for (Rotation rotation : Rotation.values()) {
                SupportedKey key = new SupportedKey(pair.quality(), pair.state(), rotation);
                seeds.put(key, findSeed(pair.quality(), pair.state(), rotation));
            }
        }
        assertEquals(EXPECTED_SUPPORTED_CASE_COUNT, seeds.size());
        return Map.copyOf(seeds);
    }

    private static long findSeedForRotation(Rotation rotation) {
        for (long seed = 0; seed < SEED_SEARCH_LIMIT; seed++) {
            if (context(seed).rotation() == rotation) {
                return seed;
            }
        }
        throw new AssertionError("No bounded seed for rotation " + rotation);
    }

    private static String diagnostic(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            Rotation rotation,
            long seed
    ) {
        return "quality=" + quality + ", state=" + state + ", rotation=" + rotation + ", seed=" + seed;
    }

    private record SupportedCase(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            Rotation rotation,
            long seed
    ) {
        @Override
        public String toString() {
            return diagnostic(quality, state, rotation, seed);
        }
    }

    private record SupportedPair(SiteQuality quality, AbandonedProspectorCampState state) {
    }

    private record SupportedKey(
            SiteQuality quality,
            AbandonedProspectorCampState state,
            Rotation rotation
    ) {
    }

    private record ReachabilityEvidence(
            long vegetationColumnsObserved,
            long sampleColumnsObserved,
            long roleAwareColumnsTraversed,
            int entryCandidateCount,
            int pathCount,
            long reachablePathCount
    ) {
    }

    private record NormalizedObservation(
            List<BlockPos> shelterColumns,
            int shelterPostHeight,
            List<BlockPos> cardinalAccessOffsets,
            List<BoundarySide> entranceBoundaryClassification
    ) {
    }

    private enum BoundarySide {
        MIN_X,
        MAX_X,
        MIN_Z,
        MAX_Z
    }

    private static final class UnionFind {
        private final Map<BlockPos, BlockPos> parent = new HashMap<>();

        private UnionFind(Set<BlockPos> positions) {
            positions.forEach(pos -> parent.put(pos, pos));
        }

        private BlockPos find(BlockPos position) {
            BlockPos current = parent.get(position);
            if (!current.equals(position)) {
                current = find(current);
                parent.put(position, current);
            }
            return current;
        }

        private void union(BlockPos first, BlockPos second) {
            BlockPos firstRoot = find(first);
            BlockPos secondRoot = find(second);
            if (!firstRoot.equals(secondRoot)) {
                parent.put(firstRoot, secondRoot);
            }
        }

        private boolean connected(BlockPos first, BlockPos second) {
            return find(first).equals(find(second));
        }

    }
}
