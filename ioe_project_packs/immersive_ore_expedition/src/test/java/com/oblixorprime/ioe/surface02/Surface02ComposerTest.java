package com.oblixorprime.ioe.surface02;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Surface02ComposerTest {
    private static final BlockPos ORIGIN = new BlockPos(7, 80, 0);
    private static final List<Long> SEEDS = List.of(0L, 1L, -1L, 0x5EEDC0DEL);
    private static final List<Surface02BiomeFamily> CANONICAL_INPUTS = List.of(
            Surface02BiomeFamily.TEMPERATE,
            Surface02BiomeFamily.ARID,
            Surface02BiomeFamily.COLD,
            Surface02BiomeFamily.WET,
            Surface02BiomeFamily.MOUNTAIN
    );
    private static final Set<String> FORBIDDEN_BLOCK_PATHS = Set.of(
            "lava",
            "fire",
            "soul_fire",
            "campfire",
            "soul_campfire",
            "magma_block",
            "sand",
            "red_sand",
            "gravel",
            "suspicious_sand",
            "suspicious_gravel",
            "chest",
            "barrel",
            "trapped_chest"
    );

    @Test
    void supportedMatrixHasExactSlotsBudgetsRanksSafetyAndDeterminism() {
        int cases = 0;
        for (SiteQuality quality : List.of(SiteQuality.DRY, SiteQuality.POOR, SiteQuality.NORMAL)) {
            for (BuddingResourceFamily family : List.of(
                    BuddingResourceFamily.GEORE_IRON,
                    BuddingResourceFamily.CERTUS_QUARTZ
            )) {
                for (Surface02BiomeFamily biome : CANONICAL_INPUTS) {
                    for (long seed : SEEDS) {
                        Surface02Composer.Request request = request(quality, family, biome, seed);
                        Surface02Composer.Decision first = Surface02Composer.compose(request);
                        Surface02Composer.Decision second = Surface02Composer.compose(request);

                        assertEquals(Surface02Composer.Status.SUPPORTED, first.status());
                        assertEquals(first, second, "same input must produce an equal plan");
                        assertPlanContract(first.plan().orElseThrow(), quality);
                        cases++;
                    }
                }
            }
        }
        assertEquals(120, cases);
    }

    @Test
    void geOreAllowlistDelegatesEveryProductiveRankToFoundationContract() {
        int cases = 0;
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) {
                continue;
            }
            for (SiteQuality quality : List.of(SiteQuality.POOR, SiteQuality.NORMAL)) {
                BuddingRank expectedRank = quality == SiteQuality.POOR
                        ? BuddingRank.DAMAGED
                        : BuddingRank.CHIPPED;
                AtomicInteger resolverCalls = new AtomicInteger();
                Surface02ResourceResolver.Resolution resolution = Surface02ResourceResolver.resolve(
                        family,
                        expectedRank,
                        (requestedFamily, requestedRank) -> {
                            resolverCalls.incrementAndGet();
                            assertEquals(family, requestedFamily);
                            assertEquals(expectedRank, requestedRank);
                            return supportedFoundationResolution(requestedFamily, requestedRank);
                        }
                );

                assertEquals(1, resolverCalls.get());
                assertEquals(Surface02ResourceResolver.Status.SUPPORTED, resolution.status());
                assertEquals(
                        geOreBuddingId(family, expectedRank),
                        resolution.resource().orElseThrow().buddingBlockId()
                );
                Surface02Composer.Decision decision = Surface02Composer.compose(new Surface02Composer.Request(
                        quality,
                        Optional.of(resolution),
                        41L,
                        Surface02BiomeFamily.TEMPERATE,
                        ORIGIN,
                        validTerrain()
                ));
                assertEquals(Surface02Composer.Status.SUPPORTED, decision.status());
                assertPlanContract(decision.plan().orElseThrow(), quality);
                cases++;
            }
        }
        assertEquals(26, cases);
    }

    @Test
    void certusRequiresExactNativeDamagedAndChippedIds() {
        for (SiteQuality quality : List.of(SiteQuality.POOR, SiteQuality.NORMAL)) {
            BuddingRank rank = quality == SiteQuality.POOR ? BuddingRank.DAMAGED : BuddingRank.CHIPPED;
            Surface02ResourceResolver.Resolution exact = supportedResolution(
                    BuddingResourceFamily.CERTUS_QUARTZ,
                    rank
            );
            assertEquals(
                    ResourceLocation.fromNamespaceAndPath("ae2", rank.path() + "_budding_quartz"),
                    exact.resource().orElseThrow().buddingBlockId()
            );
            assertEquals(
                    ResourceLocation.fromNamespaceAndPath("ae2", "quartz_block"),
                    exact.resource().orElseThrow().mineralBlockId()
            );

            Surface02ResourceResolver.Resolution wrong = Surface02ResourceResolver.resolve(
                    BuddingResourceFamily.CERTUS_QUARTZ,
                    rank,
                    (family, requestedRank) -> new Surface02ResourceResolver.FoundationResult(
                            true,
                            Optional.of(ResourceLocation.fromNamespaceAndPath(
                                    "immersive_ore_expedition",
                                    "duplicate_certus"
                            )),
                            family.storageBlockId(),
                            "synthetic wrong Certus id"
                    )
            );
            assertEquals(Surface02ResourceResolver.Status.RANK_UNSUPPORTED, wrong.status());
        }
    }

    @Test
    void extendedAeUsesTheRealFoundationRefusalWithoutFallback() {
        for (BuddingRank rank : List.of(BuddingRank.DAMAGED, BuddingRank.CHIPPED)) {
            Surface02ResourceResolver.Resolution realResolution =
                    Surface02ResourceResolver.resolve(BuddingResourceFamily.ENTROIZED_FLUIX, rank);
            assertEquals(Surface02ResourceResolver.Status.RANK_UNSUPPORTED, realResolution.status());
            assertTrue(realResolution.resource().isEmpty());

            SiteQuality quality = rank == BuddingRank.DAMAGED ? SiteQuality.POOR : SiteQuality.NORMAL;
            for (Surface02BiomeFamily biome : CANONICAL_INPUTS) {
                for (long seed : SEEDS) {
                    Surface02Composer.Decision decision = Surface02Composer.compose(
                            new Surface02Composer.Request(
                                    quality,
                                    Optional.of(realResolution),
                                    seed,
                                    biome,
                                    ORIGIN,
                                    validTerrain()
                            )
                    );
                    assertEquals(Surface02Composer.Status.UNSUPPORTED_RESOURCE_FAMILY, decision.status());
                    assertTrue(decision.plan().isEmpty());
                }
            }
        }
    }

    @Test
    void dryExtendedAeIsSterileWithoutClaimingRankSupport() {
        Surface02ResourceResolver.Resolution unsupported = Surface02ResourceResolver.resolve(
                BuddingResourceFamily.ENTROIZED_FLUIX,
                BuddingRank.DAMAGED
        );
        int cases = 0;
        for (Surface02BiomeFamily biome : CANONICAL_INPUTS) {
            for (long seed : SEEDS) {
                Surface02Composer.Decision decision = Surface02Composer.compose(
                        new Surface02Composer.Request(
                                SiteQuality.DRY,
                                Optional.of(unsupported),
                                seed,
                                biome,
                                ORIGIN,
                                validTerrain()
                        )
                );
                assertEquals(Surface02Composer.Status.SUPPORTED, decision.status());
                Surface02Plan plan = decision.plan().orElseThrow();
                assertTrue(plan.resource().isEmpty());
                assertEquals(0, plan.activeBuddingCount());
                assertEquals(0, plan.mineralBlockCount());
                assertTrue(decision.detail().contains("not used or claimed"));
                cases++;
            }
        }
        assertEquals(20, cases);
    }

    @Test
    void richAndMotherlodeAreTypedIneligibleWithoutFallback() {
        for (SiteQuality quality : List.of(SiteQuality.RICH, SiteQuality.MOTHERLODE)) {
            for (Surface02BiomeFamily biome : CANONICAL_INPUTS) {
                for (long seed : SEEDS) {
                    Surface02Composer.Decision decision = Surface02Composer.compose(
                            new Surface02Composer.Request(
                                    quality,
                                    Optional.of(supportedResolution(
                                            BuddingResourceFamily.GEORE_IRON,
                                            BuddingRank.CHIPPED
                                    )),
                                    seed,
                                    biome,
                                    ORIGIN,
                                    validTerrain()
                            )
                    );
                    assertEquals(Surface02Composer.Status.INELIGIBLE_QUALITY, decision.status());
                    assertTrue(decision.plan().isEmpty());
                }
            }
        }
    }

    @Test
    void aquaticShorelineAndNonFittingEnvelopesAreTypedInvalidTerrain() {
        Surface02ResourceResolver.Resolution iron =
                supportedResolution(BuddingResourceFamily.GEORE_IRON, BuddingRank.CHIPPED);
        Surface02TerrainContext.Bounds validBounds = validTerrain().allowedBounds();
        List<Surface02Composer.Request> invalid = List.of(
                new Surface02Composer.Request(
                        SiteQuality.NORMAL,
                        Optional.of(iron),
                        3L,
                        Surface02BiomeFamily.AQUATIC,
                        ORIGIN,
                        validTerrain()
                ),
                new Surface02Composer.Request(
                        SiteQuality.NORMAL,
                        Optional.of(iron),
                        3L,
                        Surface02BiomeFamily.SHORELINE,
                        ORIGIN,
                        validTerrain()
                ),
                new Surface02Composer.Request(
                        SiteQuality.NORMAL,
                        Optional.of(iron),
                        3L,
                        Surface02BiomeFamily.TEMPERATE,
                        ORIGIN,
                        new Surface02TerrainContext(validBounds, true, true, true, false)
                ),
                new Surface02Composer.Request(
                        SiteQuality.NORMAL,
                        Optional.of(iron),
                        3L,
                        Surface02BiomeFamily.TEMPERATE,
                        ORIGIN,
                        new Surface02TerrainContext(validBounds, true, true, false, true)
                ),
                new Surface02Composer.Request(
                        SiteQuality.NORMAL,
                        Optional.of(iron),
                        3L,
                        Surface02BiomeFamily.TEMPERATE,
                        ORIGIN,
                        new Surface02TerrainContext(
                                new Surface02TerrainContext.Bounds(
                                        validBounds.minX() + 1,
                                        validBounds.maxX(),
                                        validBounds.minY(),
                                        validBounds.maxY(),
                                        validBounds.minZ(),
                                        validBounds.maxZ()
                                ),
                                true,
                                true,
                                false,
                                false
                        )
                )
        );

        for (Surface02Composer.Request request : invalid) {
            Surface02Composer.Decision decision = Surface02Composer.compose(request);
            assertEquals(Surface02Composer.Status.INVALID_TERRAIN, decision.status());
            assertTrue(decision.plan().isEmpty());
        }
    }

    @Test
    void slotsAndCoreAreInvariantAcrossBiomeAndDecorativeSeed() {
        Map<BlockPos, Surface02Plan.PlannedCell> baselineCore = null;
        List<Surface02Plan.Slot> baselineSlots = null;
        int cases = 0;
        for (Surface02BiomeFamily biome : CANONICAL_INPUTS) {
            for (long seed : SEEDS) {
                Surface02Plan plan = Surface02Composer.compose(
                        request(SiteQuality.NORMAL, BuddingResourceFamily.GEORE_IRON, biome, seed)
                ).plan().orElseThrow();
                Map<BlockPos, Surface02Plan.PlannedCell> core = plan.blocks().entrySet().stream()
                        .filter(entry -> entry.getValue().layer() == Surface02Plan.Layer.FOUR_SLOT_CORE)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (left, right) -> left,
                                java.util.LinkedHashMap::new
                        ));
                if (baselineCore == null) {
                    baselineCore = core;
                    baselineSlots = plan.slots();
                } else {
                    assertEquals(baselineCore, core);
                    assertEquals(baselineSlots, plan.slots());
                }
                cases++;
            }
        }
        assertEquals(20, cases);
    }

    @Test
    void biomeAliasesMapExplicitlyAndAquaticFamiliesRemainRefused() {
        assertEquals(
                Surface02BiomeFamily.Canonical.TEMPERATE,
                Surface02BiomeFamily.CONIFER.canonical().orElseThrow()
        );
        assertEquals(
                Surface02BiomeFamily.Canonical.WET,
                Surface02BiomeFamily.TROPICAL.canonical().orElseThrow()
        );
        assertEquals(
                Surface02BiomeFamily.Canonical.RELIEF,
                Surface02BiomeFamily.VOLCANIC.canonical().orElseThrow()
        );
        assertEquals(
                Surface02BiomeFamily.Canonical.RELIEF,
                Surface02BiomeFamily.ROCKY.canonical().orElseThrow()
        );
        assertTrue(Surface02BiomeFamily.AQUATIC.refusedByCurrentPipeline());
        assertTrue(Surface02BiomeFamily.SHORELINE.refusedByCurrentPipeline());
    }

    @Test
    void publicPlanAndResourceBoundariesRejectContradictoryCanonicalData() {
        ResourceLocation air = ResourceLocation.fromNamespaceAndPath("minecraft", "air");
        ResourceLocation stone = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan.PlannedCell(
                        Surface02Plan.Layer.ACCESS,
                        Surface02Plan.CellKind.AIR,
                        stone
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan.PlannedCell(
                        Surface02Plan.Layer.FOUR_SLOT_CORE,
                        Surface02Plan.CellKind.MINERAL,
                        air
                )
        );

        Surface02Plan valid = Surface02Composer.compose(
                request(SiteQuality.NORMAL, BuddingResourceFamily.GEORE_IRON, Surface02BiomeFamily.TEMPERATE, 9L)
        ).plan().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        valid.origin(),
                        valid.quality(),
                        valid.planSeed(),
                        valid.observedBiomeFamily(),
                        valid.canonicalBiomeFamily(),
                        Optional.empty(),
                        valid.slots(),
                        valid.blocks(),
                        valid.walkableCells(),
                        valid.growthEnvelope(),
                        valid.obstructionCells()
                )
        );
        Surface02Plan dry = Surface02Composer.compose(
                request(SiteQuality.DRY, BuddingResourceFamily.GEORE_IRON, Surface02BiomeFamily.TEMPERATE, 9L)
        ).plan().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        dry.origin(),
                        dry.quality(),
                        dry.planSeed(),
                        dry.observedBiomeFamily(),
                        dry.canonicalBiomeFamily(),
                        dry.resource(),
                        dry.slots(),
                        dry.blocks(),
                        Set.of(),
                        dry.growthEnvelope(),
                        dry.obstructionCells()
                )
        );
        LinkedHashMap<BlockPos, Surface02Plan.PlannedCell> fakeDepletedBlocks =
                new LinkedHashMap<>(dry.blocks());
        BlockPos drySlotOneNode = dry.slots().getFirst().nodePos();
        fakeDepletedBlocks.put(
                drySlotOneNode,
                new Surface02Plan.PlannedCell(
                        Surface02Plan.Layer.FOUR_SLOT_CORE,
                        Surface02Plan.CellKind.SUPPORT,
                        ResourceLocation.fromNamespaceAndPath("minecraft", "cobbled_deepslate")
                )
        );
        LinkedHashSet<BlockPos> fakeObstructions = new LinkedHashSet<>(dry.obstructionCells());
        fakeObstructions.remove(drySlotOneNode);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        dry.origin(),
                        dry.quality(),
                        dry.planSeed(),
                        dry.observedBiomeFamily(),
                        dry.canonicalBiomeFamily(),
                        dry.resource(),
                        dry.slots(),
                        fakeDepletedBlocks,
                        dry.walkableCells(),
                        dry.growthEnvelope(),
                        fakeObstructions
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        valid.origin(),
                        SiteQuality.RICH,
                        valid.planSeed(),
                        valid.observedBiomeFamily(),
                        valid.canonicalBiomeFamily(),
                        valid.resource(),
                        valid.slots(),
                        valid.blocks(),
                        valid.walkableCells(),
                        valid.growthEnvelope(),
                        valid.obstructionCells()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        valid.origin(),
                        valid.quality(),
                        valid.planSeed(),
                        valid.observedBiomeFamily(),
                        valid.canonicalBiomeFamily(),
                        valid.resource(),
                        valid.slots(),
                        valid.blocks(),
                        Set.of(),
                        valid.growthEnvelope(),
                        valid.obstructionCells()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02Plan(
                        valid.origin(),
                        valid.quality(),
                        valid.planSeed(),
                        Surface02BiomeFamily.AQUATIC,
                        Surface02BiomeFamily.Canonical.TEMPERATE,
                        valid.resource(),
                        valid.slots(),
                        valid.blocks(),
                        valid.walkableCells(),
                        valid.growthEnvelope(),
                        valid.obstructionCells()
                )
        );

        Surface02ResourceResolver.ResolvedResource iron =
                supportedResolution(BuddingResourceFamily.GEORE_IRON, BuddingRank.DAMAGED)
                        .resource()
                        .orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02ResourceResolver.Resolution(
                        Surface02ResourceResolver.Status.SUPPORTED,
                        Optional.of(iron),
                        BuddingResourceFamily.CERTUS_QUARTZ,
                        BuddingRank.DAMAGED,
                        "contradictory family"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Surface02ResourceResolver.ResolvedResource(
                        BuddingResourceFamily.CERTUS_QUARTZ,
                        BuddingRank.DAMAGED,
                        ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "duplicate_certus"),
                        ResourceLocation.fromNamespaceAndPath("ae2", "quartz_block")
                )
        );
    }

    @Test
    void passiveAdapterValidationRejectsMismatchedUnsafeStateSemantics() {
        ResourceLocation air = ResourceLocation.fromNamespaceAndPath("minecraft", "air");
        ResourceLocation stone = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
        assertThrows(
                IllegalArgumentException.class,
                () -> Surface02Plan.validateResolvedState(
                        air,
                        Surface02Plan.CellKind.AIR,
                        stone,
                        false,
                        false,
                        false,
                        false
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Surface02Plan.validateResolvedState(
                        stone,
                        Surface02Plan.CellKind.SUPPORT,
                        stone,
                        false,
                        true,
                        false,
                        false
                )
        );
        Surface02Plan.validateResolvedState(
                air,
                Surface02Plan.CellKind.AIR,
                air,
                true,
                false,
                false,
                false
        );
        assertTrue(Surface02Plan.PLANNING_ONLY);
        assertTrue(Surface02Plan.DEFAULT_OFF);
    }

    private static void assertPlanContract(Surface02Plan plan, SiteQuality quality) {
        assertEquals(4, plan.slots().size());
        assertEquals(List.of(Surface02Plan.SlotId.values()), plan.slots().stream()
                .map(Surface02Plan.Slot::id)
                .toList());

        int active = switch (quality) {
            case DRY -> 0;
            case POOR -> 3;
            case NORMAL -> 4;
            case RICH, MOTHERLODE -> throw new AssertionError("ineligible quality");
        };
        int mineralBudget = quality == SiteQuality.POOR ? 12 : quality == SiteQuality.NORMAL ? 20 : 0;
        Optional<BuddingRank> expectedRank = quality == SiteQuality.POOR
                ? Optional.of(BuddingRank.DAMAGED)
                : quality == SiteQuality.NORMAL ? Optional.of(BuddingRank.CHIPPED) : Optional.empty();
        assertEquals(active, plan.activeBuddingCount());
        assertEquals(mineralBudget, plan.mineralBlockCount());
        assertEquals(0, plan.lootContainerCount());

        for (int index = 0; index < plan.slots().size(); index++) {
            Surface02Plan.Slot slot = plan.slots().get(index);
            boolean expectedActive = index < active;
            assertEquals(expectedActive, slot.active());
            assertEquals(expectedActive ? expectedRank : Optional.empty(), slot.activeRank());
            assertEquals(expectedActive ? mineralBudget / active : 0, slot.mineralPositions().size());
            assertEquals(expectedActive ? 6 : 0, slot.protectedGrowthEnvelope().size());
            if (quality == SiteQuality.POOR && slot.id() == Surface02Plan.SlotId.SLOT_4) {
                assertFalse(slot.active());
            }
        }

        Set<BlockPos> ownedMinerals = plan.slots().stream()
                .flatMap(slot -> slot.mineralPositions().stream())
                .collect(Collectors.toSet());
        Set<BlockPos> plannedMinerals = matchingPositions(
                plan,
                cell -> cell.kind() == Surface02Plan.CellKind.MINERAL
        );
        assertEquals(ownedMinerals, plannedMinerals);
        Set<BlockPos> ownedGrowth = plan.slots().stream()
                .flatMap(slot -> slot.protectedGrowthEnvelope().stream())
                .collect(Collectors.toSet());
        assertEquals(ownedGrowth, plan.growthEnvelope());
        assertTrue(plan.growthEnvelope().stream().allMatch(pos ->
                plan.blocks().get(pos).kind() == Surface02Plan.CellKind.AIR
        ));
        assertTrue(java.util.Collections.disjoint(plan.growthEnvelope(), plan.obstructionCells()));
        assertTrue(java.util.Collections.disjoint(plan.growthEnvelope(), plannedMinerals));

        ChunkPos anchorChunk = new ChunkPos(plan.origin());
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        assertTrue(plan.blocks().keySet().stream().allMatch(bounds::contains));
        assertTrue(plan.blocks().keySet().stream().allMatch(pos -> new ChunkPos(pos).equals(anchorChunk)));
        assertTrue(plan.blocks().values().stream().noneMatch(cell ->
                cell.blockId().getNamespace().equals("minecraft")
                        && FORBIDDEN_BLOCK_PATHS.contains(cell.blockId().getPath())
        ));

        for (BlockPos path : plan.walkableCells()) {
            assertEquals(Surface02Plan.CellKind.AIR, plan.blocks().get(path).kind());
            assertEquals(Surface02Plan.CellKind.AIR, plan.blocks().get(path.above()).kind());
        }
        Set<BlockPos> reachable = reachablePaths(plan);
        for (Surface02Plan.Slot slot : plan.slots()) {
            if (slot.active()) {
                assertTrue(reachable.contains(slot.approachPos()), () -> "unreachable " + slot.id());
            }
        }
    }

    private static Set<BlockPos> reachablePaths(Surface02Plan plan) {
        BlockPos entrance = plan.origin().offset(0, 0, 2);
        LinkedHashSet<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (BlockPos candidate : plan.walkableCells()) {
                int horizontal = Math.abs(candidate.getX() - current.getX())
                        + Math.abs(candidate.getZ() - current.getZ());
                int vertical = Math.abs(candidate.getY() - current.getY());
                if (horizontal == 1 && vertical <= 1 && !visited.contains(candidate)) {
                    queue.addLast(candidate);
                }
            }
        }
        return visited;
    }

    private static Set<BlockPos> matchingPositions(
            Surface02Plan plan,
            Predicate<Surface02Plan.PlannedCell> predicate
    ) {
        return plan.blocks().entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static Surface02Composer.Request request(
            SiteQuality quality,
            BuddingResourceFamily family,
            Surface02BiomeFamily biome,
            long seed
    ) {
        Optional<Surface02ResourceResolver.Resolution> resolution = switch (quality) {
            case POOR -> Optional.of(supportedResolution(family, BuddingRank.DAMAGED));
            case NORMAL -> Optional.of(supportedResolution(family, BuddingRank.CHIPPED));
            case DRY, RICH, MOTHERLODE -> Optional.empty();
        };
        return new Surface02Composer.Request(quality, resolution, seed, biome, ORIGIN, validTerrain());
    }

    private static Surface02ResourceResolver.Resolution supportedResolution(
            BuddingResourceFamily family,
            BuddingRank rank
    ) {
        return Surface02ResourceResolver.resolve(
                family,
                rank,
                Surface02ComposerTest::supportedFoundationResolution
        );
    }

    private static Surface02ResourceResolver.FoundationResult supportedFoundationResolution(
            BuddingResourceFamily family,
            BuddingRank rank
    ) {
        ResourceLocation buddingId = switch (family.kind()) {
            case GEORE -> geOreBuddingId(family, rank);
            case CERTUS -> family.observedNativeBuddingId(rank).orElseThrow();
            case EXTENDED_AE -> throw new IllegalArgumentException("ExtendedAE is intentionally unsupported");
        };
        return new Surface02ResourceResolver.FoundationResult(
                true,
                Optional.of(buddingId),
                family.storageBlockId(),
                "synthetic supported foundation result"
        );
    }

    private static ResourceLocation geOreBuddingId(BuddingResourceFamily family, BuddingRank rank) {
        return ResourceLocation.fromNamespaceAndPath(
                "immersive_ore_expedition",
                rank.path() + "_budding_" + family.key()
        );
    }

    private static Surface02TerrainContext validTerrain() {
        return Surface02TerrainContext.canonicalEnvelope(ORIGIN);
    }
}
