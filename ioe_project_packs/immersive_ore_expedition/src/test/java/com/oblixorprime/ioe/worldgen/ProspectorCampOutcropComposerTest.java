package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProspectorCampOutcropComposerTest {
    private static final BlockPos ORIGIN = new BlockPos(4, 90, 6);
    private static final Set<String> FORBIDDEN_NAMESPACES = Set.of(
            "immersiveengineering",
            "immersivepetroleum",
            "ae2",
            "ae2cs",
            "extendedae",
            "immersiverailroading"
    );

    @Test
    void coversEveryQualityAndVisualFamilyWithAStableVanillaFallback() {
        EnumSet<ProspectorCampVisualFamily> supportedFamilies = EnumSet.allOf(ProspectorCampVisualFamily.class);
        supportedFamilies.remove(ProspectorCampVisualFamily.AQUATIC);
        assertEquals(8, supportedFamilies.size());

        for (SiteQuality quality : SiteQuality.values()) {
            ProspectorCampQualitySpec spec = ProspectorCampQualitySpec.forQuality(quality);
            for (ProspectorCampVisualFamily family : supportedFamilies) {
                ProspectorCampContext context = new ProspectorCampContext(family, 0x51A7E5EEDL, false);
                ProspectorCampOutcropComposer.Composition first = ProspectorCampOutcropComposer.compose(
                        ORIGIN,
                        quality,
                        context
                );
                ProspectorCampOutcropComposer.Composition second = ProspectorCampOutcropComposer.compose(
                        ORIGIN,
                        quality,
                        context
                );

                assertEquals(first.blocks(), second.blocks(), quality + " / " + family + " changed for an equal seed");
                assertEquals(first.blockEntityPayloads(), second.blockEntityPayloads());
                assertEquals(first.reservedSurfaceColumns(), second.reservedSurfaceColumns());
                assertEquals(context.rotation(), first.rotation());
                assertEquals(spec.placedFootprint(), span(first, true));
                assertEquals(spec.placedFootprint(), span(first, false));
                assertTrue(first.blocks().keySet().stream().allMatch(pos -> (pos.getX() >> 4) == 0
                        && (pos.getZ() >> 4) == 0));
                assertTrue(first.blocks().keySet().stream().allMatch(pos -> pos.getY() >= ORIGIN.getY() - 1
                        && pos.getY() < ORIGIN.getY() + spec.maxHeight()));
                assertEquals(spec.containerCount(), first.lootContainerCount());
                assertEquals(0L, first.domumBlockEntityCount());
                assertTrue(first.blockEntityPayloads().values().stream()
                        .noneMatch(ExpeditionBlockEntityPayload::hasMaterialBlocks));
                assertSemanticContract(first, ORIGIN, quality);
                assertNoFreeResources(first);
            }
        }
    }

    @Test
    void coversEveryAllowedNarrativeStateForEveryQualityAndVisualFamily() {
        for (SiteQuality quality : SiteQuality.values()) {
            for (ProspectorCampVisualFamily family : ProspectorCampVisualFamily.values()) {
                if (family == ProspectorCampVisualFamily.AQUATIC) {
                    continue;
                }
                EnumSet<ProspectorCampContext.ProspectorCampState> observed =
                        EnumSet.noneOf(ProspectorCampContext.ProspectorCampState.class);
                for (long seed = 0L; seed < 256L && observed.size() < 2; seed++) {
                    ProspectorCampContext context = new ProspectorCampContext(family, seed, false);
                    ProspectorCampOutcropComposer.Composition composition =
                            ProspectorCampOutcropComposer.compose(ORIGIN, quality, context);
                    observed.add(composition.narrativeState());
                    assertEquals(context.stateFor(quality), composition.narrativeState());
                    assertSemanticContract(composition, ORIGIN, quality);
                    assertNoFreeResources(composition);
                }
                assertEquals(allowedStates(quality), observed, quality + " / " + family + " state coverage");
            }
        }
    }

    @Test
    void preservesTheRequestedQualityTargetsAndSingleChunkMotherlodeEnvelope() {
        assertEquals(
                Arrays.asList(9, 11, 13, 15, 17),
                Arrays.stream(SiteQuality.values())
                        .map(ProspectorCampQualitySpec::forQuality)
                        .map(ProspectorCampQualitySpec::targetFootprint)
                        .toList()
        );
        assertEquals(
                Arrays.asList(4, 5, 6, 7, 8),
                Arrays.stream(SiteQuality.values())
                        .map(ProspectorCampQualitySpec::forQuality)
                        .map(ProspectorCampQualitySpec::maxHeight)
                        .toList()
        );
        ProspectorCampQualitySpec motherlode = ProspectorCampQualitySpec.forQuality(SiteQuality.MOTHERLODE);
        assertEquals(17, motherlode.targetFootprint());
        assertEquals(15, motherlode.placedFootprint());
    }

    @Test
    void productiveDomumBudgetsStayWithinTheNominalShelterMaterialShare() {
        for (SiteQuality quality : SiteQuality.values()) {
            ProspectorCampQualitySpec spec = ProspectorCampQualitySpec.forQuality(quality);
            if (quality == SiteQuality.DRY) {
                assertEquals(0, spec.maxDomumAccents());
                continue;
            }
            for (boolean raisedShelter : new boolean[]{false, true}) {
                for (ProspectorCampVisualFamily family : ProspectorCampVisualFamily.values()) {
                    if (family == ProspectorCampVisualFamily.AQUATIC) {
                        continue;
                    }
                    double share = (double) spec.maxDomumAccents()
                            / spec.shelterShellBlockCount(raisedShelter, family);
                    assertTrue(share >= 0.20 && share <= 0.35,
                            quality + " / " + family + " Domum shelter share is outside 20-35%: " + share);
                }
            }
        }
    }

    @Test
    void rejectsAquaticCompositionWithoutInventingAShorelineFallback() {
        ProspectorCampContext aquatic = new ProspectorCampContext(
                ProspectorCampVisualFamily.AQUATIC,
                7L,
                false
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ProspectorCampOutcropComposer.compose(ORIGIN, SiteQuality.NORMAL, aquatic)
        );
    }

    @Test
    void coversAllFourDeterministicRotationsWithoutChangingTheSafetyContract() {
        Map<Rotation, Long> seedByRotation = new HashMap<>();
        for (long seed = 0L; seed < 256L && seedByRotation.size() < Rotation.values().length; seed++) {
            ProspectorCampContext context = new ProspectorCampContext(
                    ProspectorCampVisualFamily.TEMPERATE,
                    seed,
                    false
            );
            seedByRotation.putIfAbsent(context.rotation(), seed);
        }
        assertEquals(EnumSet.allOf(Rotation.class), seedByRotation.keySet());

        for (int localX : new int[]{4, 11}) {
            for (int localZ : new int[]{6, 7, 8, 9}) {
                BlockPos origin = new BlockPos(localX, ORIGIN.getY(), localZ);
                for (Map.Entry<Rotation, Long> rotationSeed : seedByRotation.entrySet()) {
                    ProspectorCampContext context = new ProspectorCampContext(
                            ProspectorCampVisualFamily.TEMPERATE,
                            rotationSeed.getValue(),
                            false
                    );
                    for (SiteQuality quality : SiteQuality.values()) {
                        ProspectorCampOutcropComposer.Composition composition =
                                ProspectorCampOutcropComposer.compose(origin, quality, context);
                        ProspectorCampQualitySpec spec = ProspectorCampQualitySpec.forQuality(quality);
                        assertEquals(rotationSeed.getKey(), composition.rotation());
                        assertEquals(spec.placedFootprint(), span(composition, true));
                        assertEquals(spec.placedFootprint(), span(composition, false));
                        assertEquals(spec.containerCount(), composition.lootContainerCount());
                        assertEquals(0L, composition.domumBlockEntityCount());
                        assertSemanticContract(composition, origin, quality);
                        assertNoFreeResources(composition);
                    }
                }
            }
        }
    }

    @Test
    void staticDimensionReportMatchesTheAuditedComposerSeed() throws IOException {
        Path report = Path.of("docs/worldgen/prospector_camp/STRUCTURE_DIMENSION_REPORT.csv");
        Map<String, String[]> rowsByKey = new HashMap<>();
        List<String> reportLines = Files.readAllLines(report);
        for (String line : reportLines.subList(1, reportLines.size())) {
            String[] columns = line.split(",", -1);
            rowsByKey.put(columns[0] + "/" + columns[1], columns);
        }
        assertEquals(SiteQuality.values().length * 8, rowsByKey.size());

        for (SiteQuality quality : SiteQuality.values()) {
            for (ProspectorCampVisualFamily family : ProspectorCampVisualFamily.values()) {
                if (family == ProspectorCampVisualFamily.AQUATIC) {
                    continue;
                }
                ProspectorCampOutcropComposer.Composition composition = ProspectorCampOutcropComposer.compose(
                        ORIGIN,
                        quality,
                        new ProspectorCampContext(family, 0x51A7E5EEDL, false)
                );
                String[] row = rowsByKey.get(quality.name() + "/" + family.name());
                assertTrue(row != null, "Missing report row for " + quality + " / " + family);
                assertEquals(composition.narrativeState().name(), row[2]);
                assertEquals(composition.rotation().name(), row[4]);
                assertEquals(composition.blocks().size(), Integer.parseInt(row[9]));
                assertEquals(
                        composition.blocks().values().stream().filter(state -> !state.isAir()).count(),
                        Long.parseLong(row[10])
                );
                assertEquals(
                        composition.blocks().values().stream().filter(state -> state.hasBlockEntity()).count(),
                        Long.parseLong(row[11])
                );
                assertEquals(composition.lootContainerCount(), Long.parseLong(row[14]));
                assertEquals(span(composition, true), Integer.parseInt(row[21]));
                assertEquals(span(composition, false), Integer.parseInt(row[22]));
                assertEquals(composition.biomeGeometryBlocks().size(), Integer.parseInt(row[27]));
            }
        }
    }

    private static int span(ProspectorCampOutcropComposer.Composition composition, boolean xAxis) {
        int minimum = composition.blocks().keySet().stream()
                .mapToInt(pos -> xAxis ? pos.getX() : pos.getZ())
                .min()
                .orElseThrow();
        int maximum = composition.blocks().keySet().stream()
                .mapToInt(pos -> xAxis ? pos.getX() : pos.getZ())
                .max()
                .orElseThrow();
        return maximum - minimum + 1;
    }

    private static void assertSemanticContract(
            ProspectorCampOutcropComposer.Composition composition,
            BlockPos shaftOrigin,
            SiteQuality quality
    ) {
        ProspectorCampQualitySpec spec = ProspectorCampQualitySpec.forQuality(quality);
        assertEquals(
                (long) spec.outcropWidth() * Math.max(2, (spec.outcropWidth() + 1) / 2),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.OUTCROP)
        );
        assertEquals(
                (long) spec.shelterWidth() * spec.shelterDepth(),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.SHELTER)
        );
        assertEquals(1L, composition.reservedColumnCount(
                ProspectorCampOutcropComposer.ComponentRole.HEARTH
        ));
        assertEquals(
                quality.ordinal() >= SiteQuality.NORMAL.ordinal() ? 2L : 0L,
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.WORKSTATION)
        );
        assertEquals(
                spec.containerCount(),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.CONTAINER)
        );
        assertEquals(
                Math.max(1, quality.ordinal()),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.SAMPLE)
        );
        assertEquals(
                quality.ordinal() >= SiteQuality.RICH.ordinal() ? 9L : 0L,
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.OBSERVATION)
        );
        assertEquals(
                spec.markerCount(),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.MARKER)
        );
        assertEquals(1L, composition.reservedColumnCount(
                ProspectorCampOutcropComposer.ComponentRole.ROUTE
        ));
        assertEquals(
                expectedVegetationCount(composition.visualFamily()),
                composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.VEGETATION)
        );
        assertEquals(
                spec.biomeShelterDetailBlockCount(composition.visualFamily()),
                composition.biomeGeometryBlocks().size(),
                "Biome-specific shelter geometry count drifted for " + composition.visualFamily()
        );
        composition.biomeGeometryBlocks().forEach(pos -> assertFalse(
                composition.blocks().get(pos).isAir(),
                "Biome-specific shelter geometry is air at " + pos
        ));
        switch (composition.visualFamily()) {
            case CONIFER -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .anyMatch(pos -> composition.blocks().get(pos).is(Blocks.SPRUCE_LOG)));
            case SNOWY -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .anyMatch(pos -> composition.blocks().get(pos).is(Blocks.STRIPPED_SPRUCE_LOG)));
            case TROPICAL -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .allMatch(pos -> composition.blocks().get(pos).is(Blocks.JUNGLE_FENCE)));
            case ARID -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .allMatch(pos -> composition.blocks().get(pos).is(Blocks.WHITE_WOOL)));
            case ROCKY -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .anyMatch(pos -> composition.biomeGeometryBlocks().contains(pos.above())));
            case VOLCANIC -> assertTrue(composition.biomeGeometryBlocks().stream()
                    .anyMatch(pos -> composition.blocks().get(pos).is(Blocks.CHISELED_POLISHED_BLACKSTONE)));
            case TEMPERATE, WETLAND -> assertTrue(composition.biomeGeometryBlocks().isEmpty());
            case AQUATIC -> throw new AssertionError("Aquatic camps must not compose");
        }
        if (composition.visualFamily() == ProspectorCampVisualFamily.SNOWY) {
            composition.reservedSurfaceColumns().entrySet().stream()
                    .filter(entry -> entry.getValue() == ProspectorCampOutcropComposer.ComponentRole.VEGETATION)
                    .forEach(entry -> assertTrue(
                            composition.blocks().get(entry.getKey()).getValue(LeavesBlock.PERSISTENT),
                            "Snowy spruce-leaf vegetation must not decay"
                    ));
        }
        assertTrue(composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.PATH) > 0L);
        assertTrue(composition.reservedSurfaceColumns().keySet().stream().noneMatch(pos ->
                        Math.abs(pos.getX() - shaftOrigin.getX()) <= 1
                                && Math.abs(pos.getZ() - shaftOrigin.getZ()) <= 1),
                "A semantic camp detail intersects the reserved shaft hatch");

        composition.reservedSurfaceColumns().forEach((pos, role) -> {
            if (role == ProspectorCampOutcropComposer.ComponentRole.PATH
                    || role == ProspectorCampOutcropComposer.ComponentRole.ACCESS) {
                assertTrue(composition.blocks().containsKey(pos), "Open circulation is not explicitly cleared at " + pos);
                assertTrue(composition.blocks().get(pos).isAir(), "Open circulation is blocked at " + pos);
                assertTrue(composition.blocks().containsKey(pos.above()),
                        "Open circulation headroom is not explicitly cleared at " + pos.above());
                assertTrue(composition.blocks().get(pos.above()).isAir(),
                        "Open circulation headroom is blocked at " + pos.above());
            }
        });
        BlockPos campCenter = new BlockPos(
                Math.floorDiv(shaftOrigin.getX(), 16) * 16 + 7,
                shaftOrigin.getY(),
                Math.floorDiv(shaftOrigin.getZ(), 16) * 16 + 7
        );
        assertTrue(composition.blocks().containsKey(campCenter), "The camp center is not explicitly cleared");
        assertTrue(composition.blocks().get(campCenter).isAir(), "The camp center is not open");

        Set<ProspectorCampOutcropComposer.ComponentRole> foundedSurfaceRoles = Set.of(
                ProspectorCampOutcropComposer.ComponentRole.HEARTH,
                ProspectorCampOutcropComposer.ComponentRole.WORKSTATION,
                ProspectorCampOutcropComposer.ComponentRole.CONTAINER,
                ProspectorCampOutcropComposer.ComponentRole.SAMPLE,
                ProspectorCampOutcropComposer.ComponentRole.OBSERVATION,
                ProspectorCampOutcropComposer.ComponentRole.MARKER,
                ProspectorCampOutcropComposer.ComponentRole.ROUTE,
                ProspectorCampOutcropComposer.ComponentRole.VEGETATION
        );
        composition.reservedSurfaceColumns().forEach((pos, role) -> {
            if (foundedSurfaceRoles.contains(role)) {
                assertTrue(composition.blocks().containsKey(pos), "Missing surface block for " + role + " at " + pos);
                assertTrue(composition.blocks().containsKey(pos.below()), "Missing foundation for " + role + " at " + pos);
            }
        });

        composition.blockEntityPayloads().forEach((pos, payload) -> {
            if (!payload.hasLootTable()) {
                return;
            }
            boolean accessible = List.of(pos.east(), pos.west(), pos.south(), pos.north()).stream()
                    .anyMatch(neighbor -> {
                        ProspectorCampOutcropComposer.ComponentRole role =
                                composition.reservedSurfaceColumns().get(neighbor);
                        return (role == ProspectorCampOutcropComposer.ComponentRole.PATH
                                || role == ProspectorCampOutcropComposer.ComponentRole.ACCESS)
                                && composition.blocks().containsKey(neighbor)
                                && composition.blocks().get(neighbor).isAir();
                    });
            assertTrue(accessible, "Loot container lacks an open access column at " + pos);
        });

        long shelterSurfaceBlocks = composition.reservedSurfaceColumns().entrySet().stream()
                .filter(entry -> entry.getValue() == ProspectorCampOutcropComposer.ComponentRole.SHELTER)
                .filter(entry -> composition.blocks().containsKey(entry.getKey()))
                .count();
        boolean raisedShelter = composition.visualFamily() == ProspectorCampVisualFamily.WETLAND
                || composition.visualFamily() == ProspectorCampVisualFamily.TROPICAL;
        if (raisedShelter) {
            assertEquals(
                    composition.reservedColumnCount(ProspectorCampOutcropComposer.ComponentRole.SHELTER),
                    shelterSurfaceBlocks,
                    "Raised biome shelter lost deck cells"
            );
            long twoBlockHeadroomColumns = composition.reservedSurfaceColumns().entrySet().stream()
                    .filter(entry -> entry.getValue() == ProspectorCampOutcropComposer.ComponentRole.SHELTER)
                    .map(Map.Entry::getKey)
                    .filter(pos -> composition.blocks().containsKey(pos.above())
                            && composition.blocks().get(pos.above()).isAir()
                            && composition.blocks().containsKey(pos.above(2))
                            && composition.blocks().get(pos.above(2)).isAir())
                    .count();
            assertTrue(twoBlockHeadroomColumns > 0,
                    "Raised shelter has no explicit two-block standing headroom");
            if (quality == SiteQuality.DRY) {
                BlockPos forcedBay = composition.openRaisedShelterHeadroomBay().orElseThrow(
                        () -> new AssertionError("DRY raised shelter lost its designated incomplete open bay")
                );
                assertEquals(ProspectorCampOutcropComposer.ComponentRole.SHELTER,
                        composition.reservedSurfaceColumns().get(forcedBay));
                assertTrue(composition.blocks().get(forcedBay.above()).isAir(),
                        "DRY raised shelter foot space is blocked at its designated bay");
                assertTrue(composition.blocks().get(forcedBay.above(2)).isAir(),
                        "DRY raised shelter head space is blocked at its designated bay");
            } else {
                assertTrue(composition.openRaisedShelterHeadroomBay().isEmpty(),
                        "Productive raised shelter unexpectedly exposes a DRY headroom bay marker");
            }
        } else {
            assertTrue(composition.openRaisedShelterHeadroomBay().isEmpty(),
                    "Ground-set shelter unexpectedly exposes a raised headroom bay marker");
            assertTrue(shelterSurfaceBlocks < composition.reservedColumnCount(
                    ProspectorCampOutcropComposer.ComponentRole.SHELTER
            ), "Grounded shelter unexpectedly became a raised deck");
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                assertTrue(composition.blocks().containsKey(shaftOrigin.offset(dx, 0, dz)),
                        "Missing shaft-hatch surface cell at " + dx + "," + dz);
            }
        }
        assertTrue(composition.blocks().get(shaftOrigin.offset(0, 0, 1)).is(Blocks.OAK_TRAPDOOR));
        assertEquals(1L, composition.blocks().values().stream().filter(state -> state.is(Blocks.CAMPFIRE)).count());
        composition.blocks().entrySet().stream()
                .filter(entry -> entry.getValue().is(Blocks.CAMPFIRE))
                .forEach(entry -> {
                    assertTrue(composition.blocks().containsKey(entry.getKey().above()),
                            "Campfire headroom is not explicitly cleared at " + entry.getKey());
                    assertTrue(composition.blocks().get(entry.getKey().above()).isAir(),
                            "Campfire clearance is blocked at " + entry.getKey());
                    if (composition.visualFamily() == ProspectorCampVisualFamily.VOLCANIC) {
                        assertFalse(entry.getValue().getValue(CampfireBlock.LIT),
                                "Volcanic campfire must remain unlit");
                    }
                });
        assertEquals(
                quality.ordinal() >= SiteQuality.NORMAL.ordinal() ? 1L : 0L,
                composition.blocks().values().stream().filter(state -> state.is(Blocks.CARTOGRAPHY_TABLE)).count()
        );
        assertEquals(
                quality.ordinal() >= SiteQuality.NORMAL.ordinal() ? 1L : 0L,
                composition.blocks().values().stream().filter(state -> state.is(Blocks.CRAFTING_TABLE)).count()
        );
        assertEquals(1L, composition.blocks().values().stream()
                .filter(state -> state.is(Blocks.YELLOW_TERRACOTTA)).count());
        assertEquals(spec.markerCount(), composition.blocks().values().stream()
                .filter(state -> state.is(Blocks.OAK_FENCE)).count());

        Set<ResourceLocation> lootTables = composition.blockEntityPayloads().values().stream()
                .filter(ExpeditionBlockEntityPayload::hasLootTable)
                .map(ExpeditionBlockEntityPayload::lootTable)
                .collect(java.util.stream.Collectors.toSet());
        if (quality == SiteQuality.DRY) {
            assertTrue(lootTables.isEmpty());
        } else {
            ResourceLocation expectedLootTable = switch (quality) {
                case POOR -> ProspectorCampOutcropComposer.POOR_LOOT_TABLE;
                case NORMAL -> ProspectorCampOutcropComposer.NORMAL_LOOT_TABLE;
                case RICH -> ProspectorCampOutcropComposer.RICH_LOOT_TABLE;
                case MOTHERLODE -> ProspectorCampOutcropComposer.MOTHERLODE_LOOT_TABLE;
                case DRY -> throw new IllegalStateException("DRY handled above");
            };
            assertEquals(Set.of(expectedLootTable), lootTables);
        }
    }

    private static int expectedVegetationCount(ProspectorCampVisualFamily family) {
        return switch (family) {
            case TEMPERATE, CONIFER, SNOWY, WETLAND -> 2;
            case TROPICAL -> 3;
            case ARID, ROCKY -> 1;
            case VOLCANIC, AQUATIC -> 0;
        };
    }

    private static EnumSet<ProspectorCampContext.ProspectorCampState> allowedStates(SiteQuality quality) {
        return switch (quality) {
            case DRY -> EnumSet.of(
                    ProspectorCampContext.ProspectorCampState.ABANDONED,
                    ProspectorCampContext.ProspectorCampState.COLLAPSED
            );
            case POOR -> EnumSet.of(
                    ProspectorCampContext.ProspectorCampState.WEATHERED,
                    ProspectorCampContext.ProspectorCampState.ABANDONED
            );
            case NORMAL, RICH, MOTHERLODE -> EnumSet.of(
                    ProspectorCampContext.ProspectorCampState.WEATHERED,
                    ProspectorCampContext.ProspectorCampState.ACTIVE_RECENT
            );
        };
    }

    private static void assertNoFreeResources(ProspectorCampOutcropComposer.Composition composition) {
        composition.blocks().values().forEach(state -> {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            assertFalse(FORBIDDEN_NAMESPACES.contains(id.getNamespace()), "Forbidden integration block: " + id);
            assertFalse(id.getPath().endsWith("_ore") || id.getPath().contains("budding_")
                    || id.getPath().contains("raw_"), "Free resource block: " + id);
        });
    }
}
