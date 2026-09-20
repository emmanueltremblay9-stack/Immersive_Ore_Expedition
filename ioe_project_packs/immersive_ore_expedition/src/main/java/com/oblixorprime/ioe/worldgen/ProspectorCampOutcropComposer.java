package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Programmatic surface component used by the existing expedition blueprint engine. It produces host rock and
 * field-camp blocks only; mineral selection and deposit reservation remain outside this class.
 */
public final class ProspectorCampOutcropComposer {
    static final ResourceLocation POOR_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath(
            "immersive_ore_expedition",
            "chests/prospector_camp_supplies"
    );
    static final ResourceLocation NORMAL_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath(
            "immersive_ore_expedition",
            "chests/prospector_camp_supplies_normal"
    );
    static final ResourceLocation RICH_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath(
            "immersive_ore_expedition",
            "chests/prospector_camp_supplies_rich"
    );
    static final ResourceLocation MOTHERLODE_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath(
            "immersive_ore_expedition",
            "chests/prospector_camp_supplies_motherlode"
    );
    static final ResourceLocation DOMUM_POST = ResourceLocation.fromNamespaceAndPath("domum_ornamentum", "post");
    static final ResourceLocation DOMUM_FRAME = ResourceLocation.fromNamespaceAndPath("domum_ornamentum", "framed");
    static final ResourceLocation DOMUM_PRIMARY_COMPONENT = ResourceLocation.withDefaultNamespace("block/oak_planks");
    static final ResourceLocation DOMUM_SECONDARY_COMPONENT = ResourceLocation.withDefaultNamespace("block/dark_oak_planks");
    private static final Rotation[] COMPONENT_ROTATIONS = {
            Rotation.NONE,
            Rotation.CLOCKWISE_90,
            Rotation.CLOCKWISE_180,
            Rotation.COUNTERCLOCKWISE_90
    };
    private static final LocalColumn[] CARDINAL_OFFSETS = {
            new LocalColumn(1, 0),
            new LocalColumn(-1, 0),
            new LocalColumn(0, 1),
            new LocalColumn(0, -1)
    };

    private ProspectorCampOutcropComposer() {
    }

    public static Composition compose(
            BlockPos shaftOrigin,
            SiteQuality quality,
            ProspectorCampContext context
    ) {
        Objects.requireNonNull(shaftOrigin, "shaftOrigin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(context, "context");
        return compose(shaftOrigin, quality, context, context.rotation());
    }

    static Composition compose(
            BlockPos shaftOrigin,
            SiteQuality quality,
            ProspectorCampContext context,
            Rotation explicitRotation
    ) {
        Objects.requireNonNull(shaftOrigin, "shaftOrigin");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(explicitRotation, "explicitRotation");
        if (context.visualFamily() == ProspectorCampVisualFamily.AQUATIC) {
            throw new IllegalArgumentException("Aquatic biomes deliberately have no prospector-camp composition");
        }

        ProspectorCampQualitySpec spec = ProspectorCampQualitySpec.forQuality(quality);
        ProspectorCampContext.ProspectorCampState narrativeState = context.stateFor(quality);
        Palette palette = Palette.forFamily(context.visualFamily());
        BlockPos campCenter = new BlockPos(
                Math.floorDiv(shaftOrigin.getX(), 16) * 16 + 7,
                shaftOrigin.getY(),
                Math.floorDiv(shaftOrigin.getZ(), 16) * 16 + 7
        );
        Builder builder = new Builder(shaftOrigin, campCenter, spec, explicitRotation, context.archetype());
        Optional<DomumPalette> domum = resolveDomumPalette(palette, context);

        addSparseGroundAndPath(builder, shaftOrigin, spec, palette, context);
        addHostRockOutcrop(builder, spec, quality, palette, context);
        addShelter(builder, spec, quality, narrativeState, palette, domum, context);
        addFieldWork(builder, spec, quality, narrativeState, palette, context);
        addObservationPlatform(builder, spec, quality, palette);
        addSurveyMarkers(builder, spec, quality, palette);
        addVillageRouteMarker(builder, spec, palette);
        addBiomeVegetation(builder, spec, palette);
        addShaftHatch(builder, shaftOrigin, palette);

        return builder.build(context.visualFamily(), narrativeState, spec);
    }

    private static void addSparseGroundAndPath(
            Builder builder,
            BlockPos shaftOrigin,
            ProspectorCampQualitySpec spec,
            Palette palette,
            ProspectorCampContext context
    ) {
        int radius = spec.placedRadius();
        for (int coordinate = -radius; coordinate <= radius; coordinate++) {
            if (Math.floorMod(coordinate + (int) context.siteSeed(), 2) == 0) {
                builder.putLocal(coordinate, -1, -radius, palette.ground().defaultBlockState());
                builder.putLocal(coordinate, -1, radius, palette.ground().defaultBlockState());
                builder.putLocal(-radius, -1, coordinate, palette.ground().defaultBlockState());
                builder.putLocal(radius, -1, coordinate, palette.ground().defaultBlockState());
            }
        }
        builder.putLocal(-radius, -1, -radius, palette.ground().defaultBlockState());
        builder.putLocal(radius, -1, radius, palette.ground().defaultBlockState());

        BlockPos cursor = shaftOrigin.below();
        BlockPos destination = new BlockPos(builder.campCenter().getX(), shaftOrigin.getY() - 1, builder.campCenter().getZ());
        while (cursor.getX() != destination.getX()) {
            builder.reserveOpenWorldColumn(cursor, ComponentRole.PATH);
            builder.putWorld(cursor, palette.path().defaultBlockState());
            cursor = cursor.offset(Integer.signum(destination.getX() - cursor.getX()), 0, 0);
        }
        while (cursor.getZ() != destination.getZ()) {
            builder.reserveOpenWorldColumn(cursor, ComponentRole.PATH);
            builder.putWorld(cursor, palette.path().defaultBlockState());
            cursor = cursor.offset(0, 0, Integer.signum(destination.getZ() - cursor.getZ()));
        }
        builder.reserveOpenWorldColumn(destination, ComponentRole.PATH);
        builder.putWorld(destination, palette.path().defaultBlockState());
    }

    private static void addHostRockOutcrop(
            Builder builder,
            ProspectorCampQualitySpec spec,
            SiteQuality quality,
            Palette palette,
            ProspectorCampContext context
    ) {
        int radius = spec.placedRadius();
        int width = spec.outcropWidth();
        int startX = -radius + 1;
        int startZ = -radius + 1;
        int depth = Math.max(2, (width + 1) / 2);
        int baseHeight = 1 + Math.min(2, quality.ordinal() / 2);
        ArrayList<LocalColumn> outcropColumns = new ArrayList<>();
        for (int x = startX; x < startX + width; x++) {
            for (int z = startZ; z < startZ + depth; z++) {
                outcropColumns.add(new LocalColumn(x, z));
            }
        }
        Rotation componentRotation = claimRotatedColumns(
                builder,
                outcropColumns,
                ComponentRole.OUTCROP,
                "host-rock outcrop"
        );
        for (LocalColumn column : outcropColumns) {
            int x = column.x();
            int z = column.z();
            LocalColumn rotatedColumn = rotateColumn(x, z, componentRotation);
            BlockPos sample = builder.localPos(rotatedColumn.x(), 0, rotatedColumn.z());
            long value = context.decorationValue(sample, 0x6A09E667F3BCC909L);
            if (Math.floorMod(value, 5) == 0 && x != startX && z != startZ) {
                continue;
            }
            int edgeDistance = Math.min(
                    Math.min(x - startX, startX + width - 1 - x),
                    Math.min(z - startZ, startZ + depth - 1 - z)
            );
            int height = Math.min(spec.maxHeight() - 2, baseHeight + Math.max(0, edgeDistance)
                    + (int) Math.floorMod(value, 2));
            for (int y = -1; y < height; y++) {
                Block rock = Math.floorMod(value + y, 4) == 0 ? palette.rockAccent() : palette.rock();
                putComponentLocal(builder, x, y, z, rock.defaultBlockState(), componentRotation);
            }
        }
    }

    private static void addShelter(
            Builder builder,
            ProspectorCampQualitySpec spec,
            SiteQuality quality,
            ProspectorCampContext.ProspectorCampState narrativeState,
            Palette palette,
            Optional<DomumPalette> domum,
            ProspectorCampContext context
    ) {
        int radius = spec.placedRadius();
        int endX = radius;
        int startX = endX - spec.shelterWidth() + 1;
        int startZ = -radius + 1;
        int endZ = Math.min(radius - 1, startZ + spec.shelterDepth() - 1);
        ArrayList<LocalColumn> shelterColumns = new ArrayList<>();
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                shelterColumns.add(new LocalColumn(x, z));
            }
        }
        Rotation componentRotation = claimRotatedColumns(
                builder,
                shelterColumns,
                ComponentRole.SHELTER,
                "shelter"
        );
        int standingY = palette.raisedShelter() ? 1 : 0;
        boolean openRaisedHeadroomBay = spec.requiresOpenRaisedHeadroomBay(palette.raisedShelter());
        int headroomBayX = startX + spec.shelterWidth() / 2;
        int headroomBayZ = startZ;
        if (openRaisedHeadroomBay) {
            LocalColumn bayColumn = rotateColumn(headroomBayX, headroomBayZ, componentRotation);
            builder.markOpenRaisedShelterHeadroomBay(builder.localPos(bayColumn.x(), 0, bayColumn.z()));
        }

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (palette.raisedShelter()) {
                    putComponentLocal(
                            builder,
                            x,
                            -1,
                            z,
                            palette.ground().defaultBlockState(),
                            componentRotation
                    );
                }
                putComponentLocal(
                        builder,
                        x,
                        palette.raisedShelter() ? 0 : -1,
                        z,
                        palette.planks().defaultBlockState(),
                        componentRotation
                );
                for (int y = standingY; y < standingY + 2; y++) {
                    putComponentLocal(
                            builder,
                            x,
                            y,
                            z,
                            Blocks.AIR.defaultBlockState(),
                            componentRotation
                    );
                }
            }
        }

        int[][] corners = {{startX, startZ}, {startX, endZ}, {endX, startZ}, {endX, endZ}};
        int postBaseY = palette.raisedShelter() ? 1 : 0;
        int postBlockCount = spec.postHeight() - postBaseY;
        for (int[] corner : corners) {
            boolean materializedPost = domum.isPresent()
                    && builder.remainingDomumBudget() >= postBlockCount
                    && quality != SiteQuality.DRY;
            for (int y = postBaseY; y < spec.postHeight(); y++) {
                if (materializedPost) {
                    putComponentLocal(
                            builder,
                            corner[0],
                            y,
                            corner[1],
                            domum.orElseThrow().postState(),
                            domum.orElseThrow().postPayload(),
                            componentRotation
                    );
                } else {
                    putComponentLocal(
                            builder,
                            corner[0],
                            y,
                            corner[1],
                            palette.timber().defaultBlockState(),
                            componentRotation
                    );
                }
            }
        }

        for (int x = startX; x <= endX; x++) {
            if (openRaisedHeadroomBay && x == headroomBayX) {
                continue;
            }
            LocalColumn beamColumn = rotateColumn(x, startZ, componentRotation);
            BlockPos beamPos = builder.localPos(beamColumn.x(), spec.postHeight() - 1, beamColumn.z());
            if (builder.hasMaterialPayload(beamPos)) {
                continue;
            }
            if (domum.isPresent() && builder.remainingDomumBudget() > 0 && quality.ordinal() >= SiteQuality.POOR.ordinal()) {
                putComponentLocal(
                        builder,
                        x,
                        spec.postHeight() - 1,
                        startZ,
                        domum.orElseThrow().frameState(),
                        domum.orElseThrow().framePayload(),
                        componentRotation
                );
            } else {
                putComponentLocal(
                        builder,
                        x,
                        spec.postHeight() - 1,
                        startZ,
                        palette.planks().defaultBlockState(),
                        componentRotation
                );
            }
        }

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (openRaisedHeadroomBay && x == headroomBayX && z == headroomBayZ) {
                    continue;
                }
                LocalColumn roofColumn = rotateColumn(x, z, componentRotation);
                BlockPos roofPos = builder.localPos(roofColumn.x(), spec.postHeight(), roofColumn.z());
                boolean damaged = (narrativeState == ProspectorCampContext.ProspectorCampState.COLLAPSED
                        && Math.floorMod(context.decorationValue(roofPos, 0xBB67AE8584CAA73BL), 3) == 0
                        || (narrativeState == ProspectorCampContext.ProspectorCampState.ABANDONED
                        && Math.floorMod(context.decorationValue(roofPos, 0x3C6EF372FE94F82BL), 4) == 0));
                if (!damaged) {
                    if (domum.isPresent()
                            && builder.remainingDomumBudget() > 0
                            && quality.ordinal() >= SiteQuality.POOR.ordinal()) {
                        putComponentLocal(
                                builder,
                                x,
                                spec.postHeight(),
                                z,
                                domum.orElseThrow().frameState(),
                                domum.orElseThrow().framePayload(),
                                componentRotation
                        );
                    } else {
                        putComponentLocal(
                                builder,
                                x,
                                spec.postHeight(),
                                z,
                                palette.roof().defaultBlockState(),
                                componentRotation
                        );
                    }
                }
            }
        }
        addBiomeShelterGeometry(
                builder,
                palette,
                context.visualFamily(),
                startX,
                endX,
                startZ,
                endZ,
                componentRotation
        );
    }

    private static void addBiomeShelterGeometry(
            Builder builder,
            Palette palette,
            ProspectorCampVisualFamily visualFamily,
            int startX,
            int endX,
            int startZ,
            int endZ,
            Rotation componentRotation
    ) {
        int standingY = palette.raisedShelter() ? 1 : 0;
        switch (visualFamily) {
            case TEMPERATE, WETLAND -> {
                // Temperate remains the open baseline; wetland's raised deck is its geometry adaptation.
            }
            case CONIFER -> {
                for (int x = startX + 1; x < endX; x++) {
                    putBiomeDetailLocal(builder, x, standingY, endZ, palette.planks(), componentRotation);
                }
                for (int z = startZ + 1; z < endZ; z++) {
                    putBiomeDetailLocal(builder, startX, standingY, z, palette.planks(), componentRotation);
                }
                putBiomeDetailLocal(builder, endX - 1, standingY, startZ, palette.timber(), componentRotation);
            }
            case SNOWY -> {
                for (int x = startX + 1; x < endX; x++) {
                    putBiomeDetailLocal(builder, x, standingY, endZ, palette.planks(), componentRotation);
                }
                putBiomeDetailLocal(
                        builder,
                        endX - 1,
                        standingY,
                        startZ,
                        Blocks.STRIPPED_SPRUCE_LOG,
                        componentRotation
                );
            }
            case TROPICAL -> {
                for (int x = startX + 1; x < endX; x += 2) {
                    putBiomeDetailLocal(builder, x, standingY, endZ, Blocks.JUNGLE_FENCE, componentRotation);
                }
            }
            case ARID -> {
                for (int x = startX + 1; x < endX; x++) {
                    putBiomeDetailLocal(builder, x, standingY + 1, endZ, palette.roof(), componentRotation);
                }
            }
            case ROCKY -> {
                LinkedHashSet<Integer> anchorColumns = new LinkedHashSet<>(List.of(startX + 1, endX - 1));
                for (int x : anchorColumns) {
                    putBiomeDetailLocal(builder, x, standingY, endZ, palette.rockAccent(), componentRotation);
                    putBiomeDetailLocal(builder, x, standingY + 1, endZ, palette.rockAccent(), componentRotation);
                }
            }
            case VOLCANIC -> {
                for (int x = startX + 1; x < endX; x++) {
                    putBiomeDetailLocal(builder, x, standingY, endZ, palette.rockAccent(), componentRotation);
                }
                putBiomeDetailLocal(
                        builder,
                        endX - 1,
                        standingY,
                        startZ,
                        Blocks.CHISELED_POLISHED_BLACKSTONE,
                        componentRotation
                );
            }
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes do not receive a camp shelter");
        }
    }

    private static void addFieldWork(
            Builder builder,
            ProspectorCampQualitySpec spec,
            SiteQuality quality,
            ProspectorCampContext.ProspectorCampState narrativeState,
            Palette palette,
            ProspectorCampContext context
    ) {
        List<LocalColumn> candidates = fieldWorkCandidates(spec.placedRadius());
        LocalColumn hearth = claimFirstAvailableColumn(builder, candidates, ComponentRole.HEARTH, "hearth");
        builder.putLocal(hearth.x(), -1, hearth.z(), palette.rock().defaultBlockState());
        BlockState campfire = Blocks.CAMPFIRE.defaultBlockState().setValue(
                CampfireBlock.LIT,
                narrativeState == ProspectorCampContext.ProspectorCampState.ACTIVE_RECENT
                        && quality != SiteQuality.DRY
                        && context.visualFamily() != ProspectorCampVisualFamily.VOLCANIC
        );
        builder.putLocal(hearth.x(), 0, hearth.z(), campfire);
        builder.putLocal(hearth.x(), 1, hearth.z(), Blocks.AIR.defaultBlockState());

        if (quality.ordinal() >= SiteQuality.NORMAL.ordinal()) {
            List<LocalColumn> workstations = claimAvailableColumns(
                    builder,
                    candidates,
                    ComponentRole.WORKSTATION,
                    2,
                    "workstations"
            );
            LocalColumn cartography = workstations.get(0);
            LocalColumn crafting = workstations.get(1);
            builder.putLocal(cartography.x(), -1, cartography.z(), palette.ground().defaultBlockState());
            builder.putLocal(crafting.x(), -1, crafting.z(), palette.ground().defaultBlockState());
            builder.putLocal(cartography.x(), 0, cartography.z(), Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
            builder.putLocal(crafting.x(), 0, crafting.z(), Blocks.CRAFTING_TABLE.defaultBlockState());
            builder.putLocal(cartography.x(), 1, cartography.z(), Blocks.AIR.defaultBlockState());
            builder.putLocal(crafting.x(), 1, crafting.z(), Blocks.AIR.defaultBlockState());
        }

        for (int index = 0; index < spec.containerCount(); index++) {
            LocalColumn container = claimAccessibleContainer(builder, candidates, spec.placedRadius());
            BlockPos pos = builder.localPos(container.x(), 0, container.z());
            BlockState state = index == 0 ? Blocks.BARREL.defaultBlockState() : Blocks.CHEST.defaultBlockState();
            long lootSeed = context.decorationValue(pos, 0xA54FF53A5F1D36F1L + index);
            builder.putLocal(container.x(), -1, container.z(), palette.ground().defaultBlockState());
            builder.putLocal(
                    container.x(),
                    0,
                    container.z(),
                    state,
                    ExpeditionBlockEntityPayload.loot(lootTableFor(quality), lootSeed)
            );
            builder.putLocal(container.x(), 1, container.z(), Blocks.AIR.defaultBlockState());
        }

        int sampleCount = Math.max(1, quality.ordinal());
        List<LocalColumn> sampleColumns = claimAvailableColumns(
                builder,
                candidates,
                ComponentRole.SAMPLE,
                sampleCount,
                "field samples"
        );
        for (int index = 0; index < sampleColumns.size(); index++) {
            LocalColumn sampleColumn = sampleColumns.get(index);
            Block sample = index % 2 == 0 ? palette.rock() : palette.rockAccent();
            builder.putLocal(sampleColumn.x(), -1, sampleColumn.z(), palette.ground().defaultBlockState());
            builder.putLocal(sampleColumn.x(), 0, sampleColumn.z(), sample.defaultBlockState());
            builder.putLocal(sampleColumn.x(), 1, sampleColumn.z(), Blocks.AIR.defaultBlockState());
        }
    }

    private static void addSurveyMarkers(
            Builder builder,
            ProspectorCampQualitySpec spec,
            SiteQuality quality,
            Palette palette
    ) {
        int radius = spec.placedRadius();
        ArrayList<LocalColumn> selected = new ArrayList<>();
        for (LocalColumn candidate : perimeterCandidates(radius)) {
            if (!builder.tryClaimLocalColumns(List.of(candidate), ComponentRole.MARKER)) {
                continue;
            }
            selected.add(candidate);
            if (selected.size() == spec.markerCount()) {
                break;
            }
        }
        if (selected.size() != spec.markerCount()) {
            throw new IllegalStateException("Prospector camp cannot place its survey-marker budget");
        }
        for (int index = 0; index < selected.size(); index++) {
            LocalColumn marker = selected.get(index);
            builder.putLocal(marker.x(), -1, marker.z(), palette.rockAccent().defaultBlockState());
            builder.putLocal(marker.x(), 0, marker.z(), Blocks.OAK_FENCE.defaultBlockState());
            if (quality.ordinal() >= SiteQuality.NORMAL.ordinal() && index == selected.size() - 1) {
                builder.putLocal(marker.x(), 1, marker.z(), Blocks.LANTERN.defaultBlockState());
            }
        }
    }

    private static void addObservationPlatform(
            Builder builder,
            ProspectorCampQualitySpec spec,
            SiteQuality quality,
            Palette palette
    ) {
        if (quality.ordinal() < SiteQuality.RICH.ordinal()) {
            return;
        }
        int radius = spec.placedRadius();
        ArrayList<LocalColumn> platformColumns = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = radius - 3; z <= radius - 1; z++) {
                platformColumns.add(new LocalColumn(x, z));
            }
        }
        Rotation componentRotation = claimRotatedColumns(
                builder,
                platformColumns,
                ComponentRole.OBSERVATION,
                "observation platform"
        );
        for (LocalColumn platformColumn : platformColumns) {
            putComponentLocal(
                    builder,
                    platformColumn.x(),
                    -1,
                    platformColumn.z(),
                    palette.timber().defaultBlockState(),
                    componentRotation
            );
            putComponentLocal(
                    builder,
                    platformColumn.x(),
                    0,
                    platformColumn.z(),
                    palette.planks().defaultBlockState(),
                    componentRotation
            );
            putComponentLocal(
                    builder,
                    platformColumn.x(),
                    1,
                    platformColumn.z(),
                    Blocks.AIR.defaultBlockState(),
                    componentRotation
            );
            putComponentLocal(
                    builder,
                    platformColumn.x(),
                    2,
                    platformColumn.z(),
                    Blocks.AIR.defaultBlockState(),
                    componentRotation
            );
        }
        int mastHeight = spec.maxHeight() - 2;
        for (int y = 1; y < mastHeight; y++) {
            putComponentLocal(
                    builder,
                    0,
                    y,
                    radius - 2,
                    palette.timber().defaultBlockState(),
                    componentRotation
            );
        }
        putComponentLocal(
                builder,
                0,
                mastHeight,
                radius - 2,
                Blocks.LANTERN.defaultBlockState(),
                componentRotation
        );
    }

    private static void addVillageRouteMarker(
            Builder builder,
            ProspectorCampQualitySpec spec,
            Palette palette
    ) {
        int radius = spec.placedRadius();
        for (LocalColumn candidate : List.of(
                new LocalColumn(radius, 0),
                new LocalColumn(0, radius),
                new LocalColumn(-radius, 0),
                new LocalColumn(0, -radius)
        )) {
            if (!builder.tryClaimLocalColumns(List.of(candidate), ComponentRole.ROUTE)) {
                continue;
            }
            builder.putLocal(candidate.x(), -1, candidate.z(), palette.rock().defaultBlockState());
            builder.putLocal(candidate.x(), 0, candidate.z(), Blocks.YELLOW_TERRACOTTA.defaultBlockState());
            builder.putLocal(candidate.x(), 1, candidate.z(), Blocks.AIR.defaultBlockState());
            return;
        }
        throw new IllegalStateException("Prospector camp cannot place its village-route marker");
    }

    private static void addBiomeVegetation(
            Builder builder,
            ProspectorCampQualitySpec spec,
            Palette palette
    ) {
        List<LocalColumn> vegetationColumns = claimAvailableColumns(
                builder,
                fieldWorkCandidates(spec.placedRadius()),
                ComponentRole.VEGETATION,
                palette.vegetationCount(),
                "biome vegetation"
        );
        BlockState vegetationState = palette.vegetation().defaultBlockState();
        if (vegetationState.hasProperty(LeavesBlock.PERSISTENT)) {
            vegetationState = vegetationState.setValue(LeavesBlock.PERSISTENT, true);
        }
        for (LocalColumn vegetationColumn : vegetationColumns) {
            builder.putLocal(
                    vegetationColumn.x(),
                    -1,
                    vegetationColumn.z(),
                    palette.vegetationGround().defaultBlockState()
            );
            builder.putLocal(
                    vegetationColumn.x(),
                    0,
                    vegetationColumn.z(),
                    vegetationState
            );
        }
    }

    private static void addShaftHatch(Builder builder, BlockPos shaftOrigin, Palette palette) {
        BlockPos preferredHatchPos = shaftOrigin.offset(0, 0, 1);
        BlockPos hatchPos = builder.isShaftHatchColumn(preferredHatchPos) ? preferredHatchPos : shaftOrigin;
        for (BlockPos surfacePos : builder.shaftHatchColumns()) {
            if (builder.isReservedSurfaceColumn(surfacePos)) {
                throw new IllegalStateException("Shaft hatch overlaps a reserved camp detail at " + surfacePos);
            }
            if (!surfacePos.equals(shaftOrigin) && !surfacePos.equals(hatchPos)) {
                builder.putWorld(surfacePos.below(), palette.planks().defaultBlockState());
            }
            builder.putWorld(
                    surfacePos,
                    surfacePos.equals(hatchPos)
                            ? Blocks.OAK_TRAPDOOR.defaultBlockState()
                            : palette.planks().defaultBlockState()
            );
            builder.putWorld(surfacePos.above(), Blocks.AIR.defaultBlockState());
            builder.putWorld(surfacePos.above(2), Blocks.AIR.defaultBlockState());
        }
    }

    private static List<LocalColumn> perimeterCandidates(int radius) {
        int inset = radius - 1;
        LinkedHashSet<LocalColumn> candidates = new LinkedHashSet<>();
        for (int coordinate = -inset; coordinate <= inset; coordinate += 2) {
            candidates.add(new LocalColumn(coordinate, inset));
            candidates.add(new LocalColumn(inset, -coordinate));
            candidates.add(new LocalColumn(-coordinate, -inset));
            candidates.add(new LocalColumn(-inset, coordinate));
        }
        return List.copyOf(candidates);
    }

    private static List<LocalColumn> fieldWorkCandidates(int radius) {
        LinkedHashSet<LocalColumn> candidates = new LinkedHashSet<>();
        for (int distance = 1; distance <= Math.max(1, radius - 2); distance++) {
            for (int x = -distance; x <= distance; x++) {
                candidates.add(new LocalColumn(x, -distance));
                candidates.add(new LocalColumn(x, distance));
            }
            for (int z = -distance + 1; z < distance; z++) {
                candidates.add(new LocalColumn(-distance, z));
                candidates.add(new LocalColumn(distance, z));
            }
        }
        return List.copyOf(candidates);
    }

    private static LocalColumn claimFirstAvailableColumn(
            Builder builder,
            List<LocalColumn> candidates,
            ComponentRole role,
            String componentName
    ) {
        return claimAvailableColumns(builder, candidates, role, 1, componentName).get(0);
    }

    private static List<LocalColumn> claimAvailableColumns(
            Builder builder,
            List<LocalColumn> candidates,
            ComponentRole role,
            int count,
            String componentName
    ) {
        if (count == 0) {
            return List.of();
        }
        ArrayList<LocalColumn> selected = new ArrayList<>();
        for (LocalColumn candidate : candidates) {
            if (!builder.tryClaimLocalColumns(List.of(candidate), role)) {
                continue;
            }
            selected.add(candidate);
            if (selected.size() == count) {
                return List.copyOf(selected);
            }
        }
        throw new IllegalStateException("Prospector camp cannot place its " + componentName + " budget");
    }

    private static LocalColumn claimAccessibleContainer(
            Builder builder,
            List<LocalColumn> candidates,
            int radius
    ) {
        for (LocalColumn candidate : candidates) {
            if (builder.tryClaimAccessibleLocalColumn(candidate, radius)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Prospector camp cannot place an accessible utility container");
    }

    private static ResourceLocation lootTableFor(SiteQuality quality) {
        return switch (quality) {
            case POOR -> POOR_LOOT_TABLE;
            case NORMAL -> NORMAL_LOOT_TABLE;
            case RICH -> RICH_LOOT_TABLE;
            case MOTHERLODE -> MOTHERLODE_LOOT_TABLE;
            case DRY -> throw new IllegalArgumentException("Dry prospector camps cannot request container loot");
        };
    }

    private static Rotation claimRotatedColumns(
            Builder builder,
            List<LocalColumn> columns,
            ComponentRole role,
            String componentName
    ) {
        for (Rotation rotation : COMPONENT_ROTATIONS) {
            List<LocalColumn> rotated = columns.stream()
                    .map(column -> rotateColumn(column.x(), column.z(), rotation))
                    .toList();
            if (builder.tryClaimLocalColumns(rotated, role)) {
                return rotation;
            }
        }
        throw new IllegalStateException("Prospector camp cannot reserve a collision-free " + componentName);
    }

    private static LocalColumn rotateColumn(int x, int z, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new LocalColumn(-z, x);
            case CLOCKWISE_180 -> new LocalColumn(-x, -z);
            case COUNTERCLOCKWISE_90 -> new LocalColumn(z, -x);
            default -> new LocalColumn(x, z);
        };
    }

    private static void putComponentLocal(
            Builder builder,
            int x,
            int y,
            int z,
            BlockState state,
            Rotation componentRotation
    ) {
        LocalColumn column = rotateColumn(x, z, componentRotation);
        builder.putLocal(column.x(), y, column.z(), state.rotate(componentRotation));
    }

    private static void putComponentLocal(
            Builder builder,
            int x,
            int y,
            int z,
            BlockState state,
            ExpeditionBlockEntityPayload payload,
            Rotation componentRotation
    ) {
        LocalColumn column = rotateColumn(x, z, componentRotation);
        builder.putLocal(column.x(), y, column.z(), state.rotate(componentRotation), payload);
    }

    private static void putBiomeDetailLocal(
            Builder builder,
            int x,
            int y,
            int z,
            Block block,
            Rotation componentRotation
    ) {
        LocalColumn column = rotateColumn(x, z, componentRotation);
        BlockPos worldPos = builder.localPos(column.x(), y, column.z()).immutable();
        BlockState previous = builder.blocks.get(worldPos);
        if (previous == null || !previous.isAir()) {
            throw new IllegalStateException("Biome shelter detail does not replace explicit air at " + worldPos);
        }
        if (!builder.biomeGeometryBlocks.add(worldPos)) {
            throw new IllegalStateException("Duplicate biome shelter detail at " + worldPos);
        }
        builder.putLocal(column.x(), y, column.z(), block.defaultBlockState().rotate(componentRotation));
    }

    private static Optional<DomumPalette> resolveDomumPalette(Palette palette, ProspectorCampContext context) {
        if (!context.domumEnabled()) {
            return Optional.empty();
        }
        Optional<Block> post = BuiltInRegistries.BLOCK.getOptional(DOMUM_POST);
        Optional<Block> frame = BuiltInRegistries.BLOCK.getOptional(DOMUM_FRAME);
        if (post.isEmpty() || frame.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation frameMaterial = BuiltInRegistries.BLOCK.getKey(palette.planks());
        ResourceLocation centerMaterial = BuiltInRegistries.BLOCK.getKey(palette.rock());
        ExpeditionBlockEntityPayload postPayload = ExpeditionBlockEntityPayload.materials(Map.of(
                DOMUM_PRIMARY_COMPONENT,
                frameMaterial
        ));
        ExpeditionBlockEntityPayload framePayload = ExpeditionBlockEntityPayload.materials(Map.of(
                DOMUM_PRIMARY_COMPONENT,
                frameMaterial,
                DOMUM_SECONDARY_COMPONENT,
                centerMaterial
        ));
        return Optional.of(new DomumPalette(
                post.orElseThrow().defaultBlockState(),
                frame.orElseThrow().defaultBlockState(),
                postPayload,
                framePayload
        ));
    }

    public record Composition(
            ProspectorCampVisualFamily visualFamily,
            ProspectorCampContext.ProspectorCampState narrativeState,
            ProspectorCampQualitySpec qualitySpec,
            Rotation rotation,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, ExpeditionBlockEntityPayload> blockEntityPayloads,
            Map<BlockPos, ComponentRole> reservedSurfaceColumns,
            Set<BlockPos> shaftHatchColumns,
            Set<BlockPos> biomeGeometryBlocks,
            Optional<BlockPos> openRaisedShelterHeadroomBay
    ) {
        public Composition {
            Objects.requireNonNull(visualFamily, "visualFamily");
            Objects.requireNonNull(narrativeState, "narrativeState");
            Objects.requireNonNull(qualitySpec, "qualitySpec");
            Objects.requireNonNull(rotation, "rotation");
            blocks = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(blocks, "blocks")));
            blockEntityPayloads = Collections.unmodifiableMap(new LinkedHashMap<>(
                    Objects.requireNonNull(blockEntityPayloads, "blockEntityPayloads")
            ));
            reservedSurfaceColumns = Collections.unmodifiableMap(new LinkedHashMap<>(
                    Objects.requireNonNull(reservedSurfaceColumns, "reservedSurfaceColumns")
            ));
            shaftHatchColumns = Collections.unmodifiableSet(new LinkedHashSet<>(
                    Objects.requireNonNull(shaftHatchColumns, "shaftHatchColumns")
            ));
            biomeGeometryBlocks = Collections.unmodifiableSet(new LinkedHashSet<>(
                    Objects.requireNonNull(biomeGeometryBlocks, "biomeGeometryBlocks")
            ));
            openRaisedShelterHeadroomBay = Objects.requireNonNull(
                    openRaisedShelterHeadroomBay,
                    "openRaisedShelterHeadroomBay"
            ).map(BlockPos::immutable);
            if (!blocks.keySet().containsAll(blockEntityPayloads.keySet())) {
                throw new IllegalArgumentException("Every camp block-entity payload requires a planned block");
            }
            final Map<BlockPos, BlockState> resolvedBlocks = blocks;
            if (!blocks.keySet().containsAll(biomeGeometryBlocks)
                    || biomeGeometryBlocks.stream().anyMatch(pos -> resolvedBlocks.get(pos).isAir())) {
                throw new IllegalArgumentException("Every biome-geometry position requires a non-air planned block");
            }
        }

        public long domumBlockEntityCount() {
            return blockEntityPayloads.values().stream()
                    .filter(ExpeditionBlockEntityPayload::hasMaterialBlocks)
                    .count();
        }

        public long lootContainerCount() {
            return blockEntityPayloads.values().stream()
                    .filter(ExpeditionBlockEntityPayload::hasLootTable)
                    .count();
        }

        public long reservedColumnCount(ComponentRole role) {
            Objects.requireNonNull(role, "role");
            return reservedSurfaceColumns.values().stream()
                    .filter(role::equals)
                    .count();
        }
    }

    public enum ComponentRole {
        PATH,
        ACCESS,
        OUTCROP,
        SHELTER,
        HEARTH,
        WORKSTATION,
        CONTAINER,
        SAMPLE,
        OBSERVATION,
        MARKER,
        ROUTE,
        VEGETATION
    }

    private record LocalColumn(int x, int z) {
    }

    private record DomumPalette(
            BlockState postState,
            BlockState frameState,
            ExpeditionBlockEntityPayload postPayload,
            ExpeditionBlockEntityPayload framePayload
    ) {
    }

    private record Palette(
            Block timber,
            Block planks,
            Block roof,
            Block ground,
            Block path,
            Block rock,
            Block rockAccent,
            Block vegetationGround,
            Block vegetation,
            int vegetationCount,
            boolean raisedShelter
    ) {
        private static Palette forFamily(ProspectorCampVisualFamily family) {
            return switch (family) {
                case TEMPERATE -> new Palette(Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.WHITE_WOOL,
                        Blocks.COARSE_DIRT, Blocks.DIRT_PATH, Blocks.STONE, Blocks.ANDESITE,
                        Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, 2, false);
                case CONIFER -> new Palette(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.GRAY_WOOL,
                        Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE,
                        Blocks.PODZOL, Blocks.FERN, 2, false);
                case SNOWY -> new Palette(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.LIGHT_GRAY_WOOL,
                        Blocks.GRAVEL, Blocks.COARSE_DIRT, Blocks.STONE, Blocks.COBBLESTONE,
                        Blocks.COARSE_DIRT, Blocks.SPRUCE_LEAVES, 2, false);
                case WETLAND -> new Palette(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.BROWN_WOOL,
                        Blocks.PACKED_MUD, Blocks.MANGROVE_PLANKS, Blocks.MUD_BRICKS, Blocks.MOSSY_COBBLESTONE,
                        Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, 2, true);
                case TROPICAL -> new Palette(Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS, Blocks.YELLOW_WOOL,
                        Blocks.ROOTED_DIRT, Blocks.COARSE_DIRT, Blocks.MOSSY_COBBLESTONE, Blocks.TUFF,
                        Blocks.ROOTED_DIRT, Blocks.FERN, 3, true);
                case ARID -> new Palette(Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.WHITE_WOOL,
                        Blocks.SANDSTONE, Blocks.COARSE_DIRT, Blocks.SANDSTONE, Blocks.RED_SANDSTONE,
                        Blocks.SAND, Blocks.DEAD_BUSH, 1, false);
                case ROCKY -> new Palette(Blocks.SPRUCE_LOG, Blocks.DARK_OAK_PLANKS, Blocks.GRAY_WOOL,
                        Blocks.GRAVEL, Blocks.COARSE_DIRT, Blocks.ANDESITE, Blocks.TUFF,
                        Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, 1, false);
                case VOLCANIC -> new Palette(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.BLACK_WOOL,
                        Blocks.GRAVEL, Blocks.COARSE_DIRT, Blocks.BASALT, Blocks.POLISHED_BLACKSTONE,
                        Blocks.GRAVEL, Blocks.DEAD_BUSH, 0, false);
                case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes do not receive a camp palette");
            };
        }
    }

    private static final class Builder {
        private final BlockPos shaftOrigin;
        private final BlockPos campCenter;
        private final ProspectorCampQualitySpec spec;
        private final Rotation rotation;
        private final ChunkPos anchorChunk;
        private final LinkedHashSet<BlockPos> shaftHatchColumns;
        private final LinkedHashMap<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        private final LinkedHashMap<BlockPos, ExpeditionBlockEntityPayload> payloads = new LinkedHashMap<>();
        private final LinkedHashMap<BlockPos, ComponentRole> reservedSurfaceColumns = new LinkedHashMap<>();
        private final LinkedHashSet<BlockPos> biomeGeometryBlocks = new LinkedHashSet<>();
        private BlockPos openRaisedShelterHeadroomBay;

        private Builder(
                BlockPos shaftOrigin,
                BlockPos campCenter,
                ProspectorCampQualitySpec spec,
                Rotation rotation,
                ProspectorCampArchetype archetype
        ) {
            this.shaftOrigin = shaftOrigin.immutable();
            this.campCenter = campCenter.immutable();
            this.spec = spec;
            this.rotation = rotation;
            this.anchorChunk = new ChunkPos(shaftOrigin);
            this.shaftHatchColumns = alignedShaftHatchColumns(
                    shaftOrigin,
                    campCenter,
                    spec.placedRadius(),
                    archetype
            );
        }

        private BlockPos campCenter() {
            return campCenter;
        }

        private BlockPos localPos(int x, int y, int z) {
            int rotatedX = switch (rotation) {
                case CLOCKWISE_90 -> -z;
                case CLOCKWISE_180 -> -x;
                case COUNTERCLOCKWISE_90 -> z;
                default -> x;
            };
            int rotatedZ = switch (rotation) {
                case CLOCKWISE_90 -> x;
                case CLOCKWISE_180 -> -z;
                case COUNTERCLOCKWISE_90 -> -x;
                default -> z;
            };
            return campCenter.offset(rotatedX, y, rotatedZ);
        }

        private boolean tryClaimLocalColumns(List<LocalColumn> columns, ComponentRole role) {
            Objects.requireNonNull(columns, "columns");
            Objects.requireNonNull(role, "role");
            LinkedHashSet<BlockPos> proposed = new LinkedHashSet<>();
            for (LocalColumn column : columns) {
                BlockPos worldColumn = localPos(column.x(), 0, column.z()).immutable();
                if (!proposed.add(worldColumn)
                        || isShaftHatchColumn(worldColumn)
                        || reservedSurfaceColumns.containsKey(worldColumn)) {
                    return false;
                }
            }
            for (BlockPos worldColumn : proposed) {
                reservedSurfaceColumns.put(worldColumn, role);
            }
            return true;
        }

        private void reserveOpenWorldColumn(BlockPos pos, ComponentRole role) {
            BlockPos worldColumn = new BlockPos(pos.getX(), shaftOrigin.getY(), pos.getZ());
            if (isShaftHatchColumn(worldColumn)) {
                return;
            }
            ComponentRole previous = reservedSurfaceColumns.putIfAbsent(worldColumn, role);
            if (previous != null && previous != role) {
                throw new IllegalStateException("Open camp route collides with " + previous + " at " + worldColumn);
            }
            putWorld(worldColumn, Blocks.AIR.defaultBlockState());
            putWorld(worldColumn.above(), Blocks.AIR.defaultBlockState());
        }

        private boolean tryClaimAccessibleLocalColumn(LocalColumn column, int radius) {
            BlockPos worldColumn = localPos(column.x(), 0, column.z()).immutable();
            if (isShaftHatchColumn(worldColumn) || reservedSurfaceColumns.containsKey(worldColumn)) {
                return false;
            }
            for (LocalColumn offset : CARDINAL_OFFSETS) {
                LocalColumn accessColumn = new LocalColumn(column.x() + offset.x(), column.z() + offset.z());
                if (Math.abs(accessColumn.x()) > radius || Math.abs(accessColumn.z()) > radius) {
                    continue;
                }
                BlockPos worldAccess = localPos(accessColumn.x(), 0, accessColumn.z()).immutable();
                if (isShaftHatchColumn(worldAccess)) {
                    continue;
                }
                ComponentRole accessRole = reservedSurfaceColumns.get(worldAccess);
                if (accessRole == ComponentRole.PATH || accessRole == ComponentRole.ACCESS) {
                    reservedSurfaceColumns.put(worldColumn, ComponentRole.CONTAINER);
                    return true;
                }
                if (accessRole == null) {
                    reservedSurfaceColumns.put(worldColumn, ComponentRole.CONTAINER);
                    reserveOpenWorldColumn(worldAccess, ComponentRole.ACCESS);
                    return true;
                }
            }
            return false;
        }

        private boolean isReservedSurfaceColumn(BlockPos pos) {
            return reservedSurfaceColumns.containsKey(
                    new BlockPos(pos.getX(), shaftOrigin.getY(), pos.getZ())
            );
        }

        private Set<BlockPos> shaftHatchColumns() {
            return Collections.unmodifiableSet(shaftHatchColumns);
        }

        private boolean isShaftHatchColumn(BlockPos pos) {
            return shaftHatchColumns.contains(new BlockPos(pos.getX(), shaftOrigin.getY(), pos.getZ()));
        }

        private void putLocal(int x, int y, int z, BlockState state) {
            putWorld(localPos(x, y, z), state.rotate(rotation));
        }

        private void putLocal(
                int x,
                int y,
                int z,
                BlockState state,
                ExpeditionBlockEntityPayload payload
        ) {
            putWorld(localPos(x, y, z), state.rotate(rotation), payload);
        }

        private void putWorld(BlockPos pos, BlockState state) {
            putWorld(pos, state, null);
        }

        private void putWorld(BlockPos pos, BlockState state, ExpeditionBlockEntityPayload payload) {
            Objects.requireNonNull(pos, "pos");
            Objects.requireNonNull(state, "state");
            if (!new ChunkPos(pos).equals(anchorChunk)) {
                throw new IllegalArgumentException("Prospector-camp blocks must remain in the anchor chunk: " + pos);
            }
            int relativeY = pos.getY() - shaftOrigin.getY();
            if (relativeY < -1 || relativeY >= spec.maxHeight()) {
                throw new IllegalArgumentException("Prospector-camp block exceeds its height budget: " + pos);
            }
            BlockPos key = pos.immutable();
            blocks.put(key, state);
            payloads.remove(key);
            if (payload != null) {
                payloads.put(key, payload);
            }
        }

        private int remainingDomumBudget() {
            long used = payloads.values().stream()
                    .filter(ExpeditionBlockEntityPayload::hasMaterialBlocks)
                    .count();
            return Math.max(0, spec.maxDomumAccents() - (int) used);
        }

        private boolean hasMaterialPayload(BlockPos pos) {
            ExpeditionBlockEntityPayload payload = payloads.get(pos);
            return payload != null && payload.hasMaterialBlocks();
        }

        private void markOpenRaisedShelterHeadroomBay(BlockPos surfacePos) {
            Objects.requireNonNull(surfacePos, "surfacePos");
            if (openRaisedShelterHeadroomBay != null) {
                throw new IllegalStateException("Prospector camp cannot define multiple raised-shelter headroom bays");
            }
            openRaisedShelterHeadroomBay = surfacePos.immutable();
        }

        private Composition build(
                ProspectorCampVisualFamily visualFamily,
                ProspectorCampContext.ProspectorCampState narrativeState,
                ProspectorCampQualitySpec qualitySpec
        ) {
            if (blocks.isEmpty()) {
                throw new IllegalStateException("Prospector-camp composition cannot be empty");
            }
            if (reservedSurfaceColumns.keySet().stream().anyMatch(this::isShaftHatchColumn)) {
                throw new IllegalStateException("Prospector-camp detail reservation intersects the shaft hatch");
            }
            if (openRaisedShelterHeadroomBay != null) {
                BlockState footSpace = blocks.get(openRaisedShelterHeadroomBay.above());
                BlockState headSpace = blocks.get(openRaisedShelterHeadroomBay.above(2));
                if (reservedSurfaceColumns.get(openRaisedShelterHeadroomBay) != ComponentRole.SHELTER
                        || footSpace == null || !footSpace.isAir()
                        || headSpace == null || !headSpace.isAir()) {
                    throw new IllegalStateException("Raised-shelter headroom bay is not explicitly open");
                }
            }
            return new Composition(
                    visualFamily,
                    narrativeState,
                    qualitySpec,
                    rotation,
                    blocks,
                    payloads,
                    reservedSurfaceColumns,
                    shaftHatchColumns,
                    biomeGeometryBlocks,
                    Optional.ofNullable(openRaisedShelterHeadroomBay)
            );
        }
    }

    private static LinkedHashSet<BlockPos> alignedShaftHatchColumns(
            BlockPos shaftOrigin,
            BlockPos campCenter,
            int placedRadius,
            ProspectorCampArchetype archetype
    ) {
        int hatchCenterX = shaftOrigin.getX();
        int hatchCenterZ = shaftOrigin.getZ();
        if (archetype == ProspectorCampArchetype.ACTIVE
                || archetype == ProspectorCampArchetype.ABANDONED) {
            hatchCenterX = clamp(
                    hatchCenterX,
                    campCenter.getX() - placedRadius + 1,
                    campCenter.getX() + placedRadius - 1
            );
            hatchCenterZ = clamp(
                    hatchCenterZ,
                    campCenter.getZ() - placedRadius + 1,
                    campCenter.getZ() + placedRadius - 1
            );
        }
        LinkedHashSet<BlockPos> columns = new LinkedHashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                columns.add(new BlockPos(hatchCenterX + dx, shaftOrigin.getY(), hatchCenterZ + dz));
            }
        }
        if (!columns.contains(shaftOrigin)) {
            throw new IllegalStateException("Aligned shaft hatch must retain the shaft origin");
        }
        return columns;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
