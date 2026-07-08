package com.oblixorprime.ioe.expeditioncompass;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSiteKind;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionCompassItemTest {
    private static final float ANGLE_TOLERANCE = 0.0001F;

    @Test
    void targetCodecRoundTripsPersistentData() {
        ExpeditionCompassTarget target = target(Level.OVERWORLD, new BlockPos(8, 64, -3));

        JsonElement encoded = ExpeditionCompassTarget.CODEC.encodeStart(JsonOps.INSTANCE, target)
                .result()
                .orElseThrow();
        ExpeditionCompassTarget decoded = ExpeditionCompassTarget.CODEC.parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertEquals(target, decoded);
        assertEquals(SiteQuality.RICH, decoded.quality().orElseThrow());
        assertEquals("runtime_worldgen_placement_proof", decoded.source().orElseThrow());
    }

    @Test
    void targetStreamCodecRoundTripsSyncedComponentData() {
        ExpeditionCompassTarget target = target(Level.OVERWORLD, new BlockPos(8, 64, -3));
        ByteBuf buffer = Unpooled.buffer();
        try {
            ExpeditionCompassTarget.STREAM_CODEC.encode(buffer, target);

            ExpeditionCompassTarget decoded = ExpeditionCompassTarget.STREAM_CODEC.decode(buffer);

            assertEquals(target, decoded);
        } finally {
            buffer.release();
        }
    }

    @Test
    void vanillaLodestoneMirrorIsUntrackedAndDoesNotRequireALodestoneBlock() {
        ExpeditionCompassTarget target = target(Level.OVERWORLD, new BlockPos(8, 64, -3));

        LodestoneTracker tracker = target.asUntrackedLodestoneTracker();

        assertFalse(tracker.tracked());
        assertEquals(Level.OVERWORLD, tracker.target().orElseThrow().dimension());
        assertEquals(new BlockPos(8, 64, -3), tracker.target().orElseThrow().pos());
    }

    @Test
    void targetDisplayTextUsesReadableNamesAndCoordinates() {
        ExpeditionCompassTarget target = target(Level.OVERWORLD, new BlockPos(8, 64, -3));

        assertEquals("Tiny Vertical Mine Entrance", target.displayName());
        assertEquals("8 64 -3", target.coordinateText());
        assertEquals("IOE Tiny Vertical Mine Entrance", target.waypointName());
    }

    @Test
    void resetUseClearsStoredTargetAndKeepsResetMessageKey() {
        ExpeditionCompassItem.UseOutcome outcome = ExpeditionCompassItem.useOutcome(true);

        assertEquals(ExpeditionCompassItem.RESET_KEY, translatableKey(outcome.message().orElseThrow()));
        assertTrue(outcome.clearTarget());
        assertFalse(outcome.openMenu());
    }

    @Test
    void normalUseRequestsMenuWithoutNoIndexedSitesMessage() {
        ExpeditionCompassItem.UseOutcome outcome = ExpeditionCompassItem.useOutcome(false);

        assertTrue(outcome.openMenu());
        assertFalse(outcome.clearTarget());
        assertTrue(outcome.message().isEmpty());
    }

    @Test
    void menuSnapshotListsIndexedSameDimensionTargetsNearestFirst() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        index.record(ExpeditionSite.anchor(
                Level.NETHER,
                new BlockPos(1, 64, 0),
                id("nether_anchor"),
                null,
                SiteQuality.NORMAL,
                "test"
        ));
        index.record(ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(32, 64, 0),
                id("distant_anchor"),
                null,
                SiteQuality.NORMAL,
                "test"
        ));
        ExpeditionSite nearest = ExpeditionSite.province(
                Level.OVERWORLD,
                new BlockPos(8, 64, 0),
                id("tiny_vertical_mine_entrance"),
                id("granite_belt"),
                SiteQuality.RICH,
                "test"
        );
        index.record(nearest);

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                InteractionHand.MAIN_HAND,
                Optional.empty(),
                index
        );

        assertEquals(Level.OVERWORLD, snapshot.dimension());
        assertEquals(InteractionHand.MAIN_HAND, snapshot.hand());
        assertEquals(2, snapshot.entries().size());
        ExpeditionCompassMenuEntry firstEntry = snapshot.entries().getFirst();
        assertEquals(nearest.pos(), firstEntry.target().pos());
        assertEquals(ExpeditionSiteKind.PROVINCE, firstEntry.target().kind());
        assertEquals(id("granite_belt"), firstEntry.target().primaryId().orElseThrow());
        assertEquals(8L, firstEntry.distanceBlocks());
    }

    @Test
    void menuSnapshotUsesDeterministicTieOrdering() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        index.record(anchor("positive_x", new BlockPos(5, 64, 0)));
        index.record(anchor("negative_x", new BlockPos(-5, 64, 0)));

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                InteractionHand.MAIN_HAND,
                Optional.empty(),
                index
        );

        ExpeditionCompassTarget firstTarget = snapshot.entries().getFirst().target();
        assertEquals(new BlockPos(-5, 64, 0), firstTarget.pos());
        assertEquals(id("negative_x"), firstTarget.anchorId().orElseThrow());
    }

    @Test
    void emptyMenuSnapshotDropsStoredTargetWhenItIsNoLongerIndexed() {
        ExpeditionCompassTarget existing = target(Level.OVERWORLD, new BlockPos(10, 64, 0));

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                InteractionHand.OFF_HAND,
                Optional.of(existing),
                new ExpeditionLocatorIndex()
        );

        assertEquals(InteractionHand.OFF_HAND, snapshot.hand());
        assertEquals(ExpeditionCompassEmptyReason.WORLDGEN_DISABLED, snapshot.emptyReason());
        assertTrue(snapshot.currentTarget().isEmpty());
        assertTrue(snapshot.entries().isEmpty());
    }

    @Test
    void menuSnapshotKeepsStoredTargetWhenItIsStillIndexed() {
        ExpeditionSite existingSite = anchor("tiny_vertical_mine_entrance", new BlockPos(10, 64, 0));
        ExpeditionCompassTarget existing = ExpeditionCompassTarget.fromSite(existingSite);
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        index.record(existingSite);

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                InteractionHand.OFF_HAND,
                Optional.of(existing),
                index
        );

        assertEquals(existing, snapshot.currentTarget().orElseThrow());
        assertEquals(ExpeditionCompassEmptyReason.NO_PLACED_SITES, snapshot.emptyReason());
        assertEquals(1, snapshot.entries().size());
    }

    @Test
    void compassIndexDoesNotSeedCatalogSurveyTargetsWhenRuntimeIndexIsEmpty() {
        ExpeditionLocatorService.clearForTesting();
        try {
            BlockPos origin = new BlockPos(0, 64, 0);
            ExpeditionLocatorIndex index = ExpeditionLocatorService.compassIndex(Level.OVERWORLD, origin);

            assertEquals(0, index.size());
            assertTrue(index.sites().isEmpty());
            assertTrue(index.diagnosticSites().isEmpty());
            assertFalse(index.nearestAny(Level.OVERWORLD, origin).found());
        } finally {
            ExpeditionLocatorService.clearForTesting();
        }
    }

    @Test
    void compassIndexKeepsExistingRuntimeSitesInsteadOfSeedingSurveyTargets() {
        ExpeditionLocatorService.clearForTesting();
        try {
            ExpeditionSite existing = anchor("runtime_anchor", new BlockPos(8, 64, 0));
            ExpeditionLocatorService.index().record(existing);

            ExpeditionLocatorIndex index = ExpeditionLocatorService.compassIndex(Level.OVERWORLD, BlockPos.ZERO);

            assertEquals(1, index.sites().size());
            assertEquals(existing, index.sites().getFirst());
        } finally {
            ExpeditionLocatorService.clearForTesting();
        }
    }

    @Test
    void serverSelectionValidationRejectsUnplacedSurveyTargets() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite plannedSite = ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(96, 64, 0),
                id("tiny_vertical_mine_entrance"),
                id("compass_survey"),
                SiteQuality.NORMAL,
                "expedition_compass_survey",
                ExpeditionSitePlacementState.PLANNED,
                "catalog target is not backed by a placed structure"
        );
        index.record(plannedSite);
        ExpeditionCompassTarget surveyTarget = ExpeditionCompassTarget.fromSite(plannedSite);

        assertFalse(surveyTarget.playable());
        assertTrue(index.sites().isEmpty());
        assertEquals(1, index.diagnosticSites().size());
        assertTrue(IoeCompassNetworking.validateSelection(
                Level.OVERWORLD,
                BlockPos.ZERO,
                surveyTarget,
                index
        ).isEmpty());
    }

    @Test
    void diagnosticModeCanExposeUnplacedTargetsWithoutMakingThemBindable() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite plannedSite = ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(96, 64, 0),
                id("tiny_vertical_mine_entrance"),
                id("compass_survey"),
                SiteQuality.NORMAL,
                "expedition_compass_survey",
                ExpeditionSitePlacementState.PLANNED,
                "catalog target is not backed by a placed structure"
        );
        index.record(plannedSite);

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                BlockPos.ZERO,
                InteractionHand.MAIN_HAND,
                Optional.empty(),
                index,
                true,
                new IoeWorldgenPlacementGates(true, false, false)
        );

        assertEquals(ExpeditionCompassEmptyReason.ONLY_DEBUG_OR_PLANNED_SITES, snapshot.emptyReason());
        assertEquals(1, snapshot.entries().size());
        assertFalse(snapshot.entries().getFirst().target().playable());
        assertTrue(IoeCompassNetworking.validateSelection(
                Level.OVERWORLD,
                BlockPos.ZERO,
                snapshot.entries().getFirst().target(),
                index
        ).isEmpty());
    }

    @Test
    void menuSnapshotExposesEveryValidIndexedSiteForScrollingUi() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        for (int siteIndex = 0; siteIndex < 6; siteIndex++) {
            index.record(anchor("placed_site_" + siteIndex, new BlockPos(siteIndex * 8, 64, 0)));
        }

        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                Level.OVERWORLD,
                BlockPos.ZERO,
                InteractionHand.MAIN_HAND,
                Optional.empty(),
                index
        );

        assertEquals(6, snapshot.entries().size());
        assertEquals(new BlockPos(0, 64, 0), snapshot.entries().getFirst().target().pos());
    }

    @Test
    void currentTargetMessageReportsSameDimensionDistanceWithoutRebinding() {
        ExpeditionCompassTarget existing = target(Level.OVERWORLD, new BlockPos(10, 64, 0));

        Component message = ExpeditionCompassItem.messageForCurrentTarget(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                existing
        );

        assertEquals(ExpeditionCompassItem.CURRENT_TARGET_KEY, translatableKey(message));
        assertEquals(10L, translatableArgs(message)[5]);
    }

    @Test
    void currentTargetMessageReportsCrossDimensionWithoutDistance() {
        ExpeditionCompassTarget existing = target(Level.NETHER, new BlockPos(10, 64, 0));

        Component message = ExpeditionCompassItem.messageForCurrentTarget(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                existing
        );

        assertEquals(ExpeditionCompassItem.TARGET_OTHER_DIMENSION_KEY, translatableKey(message));
        assertEquals(Level.NETHER.location().toString(), translatableArgs(message)[0]);
        assertEquals(6, translatableArgs(message).length);
    }

    @Test
    void menuSnapshotPayloadRoundTripsIndexedEntriesAndCurrentTarget() {
        ExpeditionCompassMenuSnapshot snapshot = new ExpeditionCompassMenuSnapshot(
                Level.OVERWORLD,
                InteractionHand.MAIN_HAND,
                Optional.of(target(Level.OVERWORLD, new BlockPos(10, 64, 0))),
                ExpeditionCompassEmptyReason.NO_PLACED_SITES,
                List.of(new ExpeditionCompassMenuEntry(target(Level.OVERWORLD, new BlockPos(8, 64, -3)), 11L))
        );
        RegistryFriendlyByteBuf buffer = registryBuffer(Unpooled.buffer());
        try {
            ClientboundExpeditionCompassMenuPayload.STREAM_CODEC.encode(
                    buffer,
                    new ClientboundExpeditionCompassMenuPayload(snapshot)
            );

            ClientboundExpeditionCompassMenuPayload decoded =
                    ClientboundExpeditionCompassMenuPayload.STREAM_CODEC.decode(buffer);

            assertEquals(snapshot, decoded.snapshot());
        } finally {
            buffer.release();
        }
    }

    @Test
    void serverboundSelectPayloadRoundTripsTargetKey() {
        ServerboundExpeditionCompassSelectPayload payload = new ServerboundExpeditionCompassSelectPayload(
                InteractionHand.OFF_HAND,
                target(Level.OVERWORLD, new BlockPos(8, 64, -3))
        );
        RegistryFriendlyByteBuf buffer = registryBuffer(Unpooled.buffer());
        try {
            ServerboundExpeditionCompassSelectPayload.STREAM_CODEC.encode(buffer, payload);

            ServerboundExpeditionCompassSelectPayload decoded =
                    ServerboundExpeditionCompassSelectPayload.STREAM_CODEC.decode(buffer);

            assertEquals(payload, decoded);
        } finally {
            buffer.release();
        }
    }

    @Test
    void serverSelectionValidationRejectsInventedClientTargetCoordinates() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ExpeditionSite indexed = anchor("tiny_vertical_mine_entrance", new BlockPos(8, 64, -3));
        index.record(indexed);

        assertTrue(IoeCompassNetworking.validateSelection(
                Level.OVERWORLD,
                BlockPos.ZERO,
                ExpeditionCompassTarget.fromSite(indexed),
                index
        ).isPresent());
        assertTrue(IoeCompassNetworking.validateSelection(
                Level.OVERWORLD,
                BlockPos.ZERO,
                target(Level.OVERWORLD, new BlockPos(1024, 64, 1024)),
                index
        ).isEmpty());
    }

    @Test
    void angleUsesNeutralFallbackWhenTargetOrViewerDimensionIsMissing() {
        ExpeditionCompassTarget target = target(Level.OVERWORLD, new BlockPos(0, 64, 16));

        assertEquals(
                ExpeditionCompassAngle.UNBOUND_ANGLE,
                ExpeditionCompassAngle.angleToTarget(Level.OVERWORLD, 0.5D, 0.5D, 0.0F, null),
                ANGLE_TOLERANCE
        );
        assertEquals(
                ExpeditionCompassAngle.UNBOUND_ANGLE,
                ExpeditionCompassAngle.angleToTarget(null, 0.5D, 0.5D, 0.0F, target),
                ANGLE_TOLERANCE
        );
    }

    @Test
    void crossDimensionAngleUsesNonTargetedFallback() {
        ExpeditionCompassTarget target = target(Level.NETHER, new BlockPos(0, 64, 16));

        assertEquals(
                ExpeditionCompassAngle.CROSS_DIMENSION_ANGLE,
                ExpeditionCompassAngle.angleToTarget(Level.OVERWORLD, 0.5D, 0.5D, 0.0F, target),
                ANGLE_TOLERANCE
        );
        assertNotEquals(ExpeditionCompassAngle.UNBOUND_ANGLE, ExpeditionCompassAngle.CROSS_DIMENSION_ANGLE);
    }

    @Test
    void sameDimensionAngleMatchesVanillaCompassFramePhase() {
        assertEquals(
                0.5F,
                ExpeditionCompassAngle.angleToTarget(
                        Level.OVERWORLD,
                        0.5D,
                        0.5D,
                        0.0F,
                        target(Level.OVERWORLD, new BlockPos(0, 64, 16))
                ),
                ANGLE_TOLERANCE
        );
        assertEquals(
                0.25F,
                ExpeditionCompassAngle.angleToTarget(
                        Level.OVERWORLD,
                        0.5D,
                        0.5D,
                        0.0F,
                        target(Level.OVERWORLD, new BlockPos(-16, 64, 0))
                ),
                ANGLE_TOLERANCE
        );
        assertEquals(
                0.0F,
                ExpeditionCompassAngle.angleToTarget(
                        Level.OVERWORLD,
                        0.5D,
                        0.5D,
                        0.0F,
                        target(Level.OVERWORLD, new BlockPos(0, 64, -16))
                ),
                ANGLE_TOLERANCE
        );
        assertEquals(
                0.75F,
                ExpeditionCompassAngle.angleToTarget(
                        Level.OVERWORLD,
                        0.5D,
                        0.5D,
                        0.0F,
                        target(Level.OVERWORLD, new BlockPos(16, 64, 0))
                ),
                ANGLE_TOLERANCE
        );
    }

    @Test
    void sameDimensionAngleTracksViewerYaw() {
        ExpeditionCompassTarget westTarget = target(Level.OVERWORLD, new BlockPos(-16, 64, 0));

        assertEquals(
                0.0F,
                ExpeditionCompassAngle.angleToTarget(Level.OVERWORLD, 0.5D, 0.5D, 90.0F, westTarget),
                ANGLE_TOLERANCE
        );
    }

    private static String translatableKey(Component component) {
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        return contents.getKey();
    }

    private static Object[] translatableArgs(Component component) {
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        return contents.getArgs();
    }

    private static RegistryFriendlyByteBuf registryBuffer(ByteBuf buffer) {
        return new RegistryFriendlyByteBuf(buffer, RegistryAccess.EMPTY);
    }

    private static ExpeditionCompassTarget target(ResourceKey<Level> dimension, BlockPos pos) {
        return new ExpeditionCompassTarget(
                dimension,
                pos,
                ExpeditionSiteKind.ANCHOR,
                Optional.of(id("tiny_vertical_mine_entrance")),
                Optional.of(id("granite_belt")),
                Optional.of(SiteQuality.RICH),
                Optional.of("runtime_worldgen_placement_proof")
        );
    }

    private static ExpeditionSite anchor(String path, BlockPos pos) {
        return ExpeditionSite.anchor(
                Level.OVERWORLD,
                pos,
                id(path),
                null,
                SiteQuality.NORMAL,
                "test"
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", path);
    }
}
