package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.crystalgrowth.MeteoriticAe2GeodePlacementPlan;
import com.oblixorprime.ioe.nethergeodes.NetherSubLavaGeodePlacementPlan;
import com.oblixorprime.ioe.worldgen.IoeWorldgenBootstrap;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import com.oblixorprime.ioe.worldgen.IoeWorldgenRegistration;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RetrogenStateStoreTest {
    private static final Set<String> LEGACY_SPLIT_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    @Test
    void processedChunkIsNotRequeuedAfterStateReload() {
        ChunkKey key = new ChunkKey(0, 0);
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(1, 4);
        RetrogenController firstController = new RetrogenController(1, 4, store);

        RetrogenStartResult first = firstController.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));
        List<RetrogenQueueEntry> processed = firstController.tickBatch();
        RetrogenController reloadedController = new RetrogenController(1, 4, store);
        RetrogenStartResult second = reloadedController.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));

        assertTrue(first.started());
        assertEquals(1, processed.size());
        assertFalse(second.started());
        assertEquals(1, second.skippedAlreadyMarked());
        assertTrue(store.hasProcessed(key, 1));
    }

    @Test
    void queuedChunkIsNotDuplicatedAfterStateReload() {
        ChunkKey key = new ChunkKey(0, 0);
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(1, 4);
        RetrogenController firstController = new RetrogenController(1, 4, store);

        RetrogenStartResult first = firstController.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));
        RetrogenController reloadedController = new RetrogenController(1, 4, store);
        RetrogenStartResult second = reloadedController.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));

        assertTrue(first.started());
        assertFalse(second.started());
        assertEquals(1, second.skippedAlreadyMarked());
        assertEquals(1, reloadedController.status().queuedChunks());
        assertEquals(1, store.statusSnapshot().queuedChunks());
    }

    @Test
    void skippedAndFailedMarkersCanBeRecordedDeterministically() {
        ChunkKey skipped = new ChunkKey(1, 0);
        ChunkKey failed = new ChunkKey(2, 0);
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(2, 4);

        PersistentRetrogenChunkMarker skippedMarker = store.markSkipped(
                skipped,
                RetrogenMode.ADMIN_RADIUS,
                2,
                PersistentRetrogenChunkMarker.FailureReason.INVALID_RADIUS,
                "outside configured radius"
        );
        PersistentRetrogenChunkMarker failedMarker = store.markFailed(
                failed,
                RetrogenMode.ADMIN_RADIUS,
                2,
                PersistentRetrogenChunkMarker.FailureReason.STORE_ERROR,
                "write failed"
        );

        assertEquals(PersistentRetrogenChunkMarker.MarkerStatus.SKIPPED, skippedMarker.status());
        assertEquals(PersistentRetrogenChunkMarker.FailureReason.INVALID_RADIUS, skippedMarker.failureReason());
        assertEquals(PersistentRetrogenChunkMarker.MarkerStatus.FAILED, failedMarker.status());
        assertEquals(PersistentRetrogenChunkMarker.FailureReason.STORE_ERROR, failedMarker.failureReason());
        assertEquals(1, store.statusSnapshot().skippedChunks());
        assertEquals(1, store.statusSnapshot().failedChunks());
    }

    @Test
    void pauseAndResumeStatePersistInStoreModel() {
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(3, 2);

        store.resume(RetrogenMode.CLUE_PLUS_POCKET);
        PersistentRetrogenState.StatusSnapshot resumed = store.statusSnapshot();
        store.pause(RetrogenMode.CLUE_PLUS_POCKET);
        PersistentRetrogenState.StatusSnapshot paused = store.statusSnapshot();

        assertFalse(resumed.paused());
        assertEquals(RetrogenMode.CLUE_PLUS_POCKET, resumed.mode());
        assertTrue(paused.paused());
        assertEquals(RetrogenMode.CLUE_PLUS_POCKET, paused.mode());
    }

    @Test
    void maxChunksPerTickMetadataIsPreservedAcrossControllerReload() {
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(7, 3);
        RetrogenController controller = new RetrogenController(7, 3, store);
        RetrogenController reloaded = new RetrogenController(7, 3, store);

        assertEquals(3, controller.persistentStatus().maxChunksPerTick());
        assertEquals(7, reloaded.persistentStatus().markerVersion());
        assertEquals(3, reloaded.persistentStatus().maxChunksPerTick());
    }

    @Test
    void existingRetrogenControllerQueueBehaviorRemainsUnchanged() {
        RetrogenController controller = new RetrogenController(1, 1);
        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.CLUE_PLUS_POCKET, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), true, ChunkRetrogenMarker.missing()),
                new RetrogenChunkSnapshot(new ChunkKey(1, 0), true, ChunkRetrogenMarker.missing())
        ));
        List<RetrogenQueueEntry> firstBatch = controller.tickBatch();

        assertTrue(result.started());
        assertEquals(2, result.acceptedChunks());
        assertEquals(1, firstBatch.size());
        assertEquals(1, controller.status().queuedChunks());
    }

    @Test
    void defaultDisabledRetrogenRemainsNoOpAdminSafe() {
        RetrogenController controller = new RetrogenController(1, 1);

        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.OFF, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), false, ChunkRetrogenMarker.missing())
        ));

        assertFalse(result.started());
        assertEquals(0, result.acceptedChunks());
        assertEquals(0, controller.status().queuedChunks());
        assertTrue(controller.status().paused());
    }

    @Test
    void v14AndV15PlanningEnumsRemainAvailable() {
        assertEquals(MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE,
                MeteoriticAe2GeodePlacementPlan.GeodeType.valueOf("BURIED_METEORITIC_AE2_GEODE"));
        assertEquals(NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE,
                NetherSubLavaGeodePlacementPlan.GeodeType.valueOf("SUB_LAVA_QUARTZ_GEODE"));
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithPersistentRetrogenMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.persistentConservativeRetrogenPlanningReady());
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroduced() {
        assertFalse(IoeWorldgenFeatureKeys.allFeatureKeys().isEmpty());
        for (ResourceLocation featureKey : IoeWorldgenFeatureKeys.allFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
            assertFalse(LEGACY_SPLIT_NAMESPACES.contains(featureKey.getNamespace()));
        }
    }
}
