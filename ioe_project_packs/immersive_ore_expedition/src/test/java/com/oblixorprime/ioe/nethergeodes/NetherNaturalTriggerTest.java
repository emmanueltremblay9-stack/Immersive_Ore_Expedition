package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class NetherNaturalTriggerTest {
    @Test void onlyCanonicalNewReceiptsDispatchOnceAfterTheirLoadTick() {
        var coordinator = new NetherPlacementCoordinator();
        var candidate = NetherSitePlanner.candidate(17, -3, 4);
        var chunk = new ChunkPos(candidate.x() >> 4, candidate.z() >> 4);
        var other = new ChunkPos(chunk.x + 1, chunk.z);
        coordinator.observe(chunk.toLong(), new Object(), true, 10);
        coordinator.observe(other.toLong(), new Object(), true, 10);
        assertTrue(coordinator.takeNaturalCandidates(17, 10).isEmpty());
        assertEquals(java.util.List.of(chunk), coordinator.takeNaturalCandidates(17, 11));
        assertTrue(coordinator.takeNaturalCandidates(17, 12).isEmpty());
    }

    @Test void oldInvalidatedAndRepeatedReceiptsNeverDispatch() {
        var candidate = NetherSitePlanner.candidate(17, 2, 2);
        long key = ChunkPos.asLong(candidate.x() >> 4, candidate.z() >> 4);
        for (int scenario = 0; scenario < 3; scenario++) {
            var coordinator = new NetherPlacementCoordinator(); var identity = new Object();
            coordinator.observe(key, identity, scenario != 0, 10);
            if (scenario == 1) coordinator.invalidate(key);
            if (scenario == 2) coordinator.observe(key, identity, true, 10);
            assertTrue(coordinator.takeNaturalCandidates(17, 11).isEmpty());
        }
    }
    @Test void incompleteNeighborhoodWaitsOnlyBeforeFirstAdmission() {
        var coordinator=new NetherPlacementCoordinator();
        var candidate=NetherSitePlanner.candidate(17,2,2);
        var chunk=new ChunkPos(candidate.x()>>4,candidate.z()>>4);
        coordinator.observe(chunk.toLong(),new Object(),true,10);
        assertTrue(coordinator.takeNaturalCandidates(17,11,ignored->false).isEmpty());
        assertTrue(coordinator.takeNaturalCandidates(17,18,ignored->false).isEmpty());
        assertEquals(java.util.List.of(chunk),coordinator.takeNaturalCandidates(17,19,ignored->true));
        assertTrue(coordinator.takeNaturalCandidates(17,20,ignored->true).isEmpty());
    }
    @Test void delayedReadinessDispatchesOnceWithinBoundWithoutRenewingBirth() {
        var coordinator = new NetherPlacementCoordinator();
        var candidate = NetherSitePlanner.candidate(17,2,2);
        var chunk = new ChunkPos(candidate.x()>>4,candidate.z()>>4);
        coordinator.observe(chunk.toLong(),new Object(),true,10);
        for(int tick=11;tick<400;tick++) assertTrue(coordinator.takeNaturalCandidates(17,tick,ignored->false).isEmpty());
        assertEquals(java.util.List.of(chunk), coordinator.takeNaturalCandidates(17,400,ignored->true));
        assertEquals(390, coordinator.preparationAge(chunk.toLong(),400));
        assertTrue(coordinator.takeNaturalCandidates(17,401,ignored->true).isEmpty());
        coordinator.endNaturalAdmission(chunk);
        assertEquals(-1, coordinator.preparationAge(chunk.toLong(),401));
    }
    @Test void preparationExpiryNeverRenewsOrRestoresInvalidatedReceipt() {
        var coordinator=new NetherPlacementCoordinator();
        var candidate=NetherSitePlanner.candidate(17,2,2);
        var chunk=new ChunkPos(candidate.x()>>4,candidate.z()>>4);
        coordinator.observe(chunk.toLong(),new Object(),true,10);
        for(int tick=11;tick<29;tick++)assertTrue(coordinator.takeNaturalCandidates(17,tick,ignored->false).isEmpty());
        assertTrue(coordinator.takeNaturalCandidates(17,29,ignored->false).isEmpty());
        assertTrue(coordinator.takeNaturalCandidates(17,10+NetherPlacementCoordinator.PREPARATION_TICKS,ignored->true).isEmpty());
        assertTrue(coordinator.takeNaturalCandidates(17,30,ignored->true).isEmpty());
        var invalidated=new NetherPlacementCoordinator();
        invalidated.observe(chunk.toLong(),new Object(),true,10);
        assertTrue(invalidated.takeNaturalCandidates(17,11,ignored->false).isEmpty());
        invalidated.invalidate(chunk.toLong());
        assertTrue(invalidated.takeNaturalCandidates(17,12,ignored->true).isEmpty());
    }
}
