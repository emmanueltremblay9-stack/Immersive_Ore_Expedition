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
}
