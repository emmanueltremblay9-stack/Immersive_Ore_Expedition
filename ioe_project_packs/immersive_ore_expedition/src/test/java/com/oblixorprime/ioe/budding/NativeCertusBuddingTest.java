package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NativeCertusBuddingTest {
    @Test void requiresEachNativeResourceAndProcessingDependency() {
        assertTrue(NativeCertusBudding.available(mod -> true, id -> true));
        for (String absent : List.of("ae2", "ae2cs")) {
            assertFalse(NativeCertusBudding.available(mod -> !mod.equals(absent), id -> true));
        }
        var required = new ArrayList<>(BuddingResourceFamily.CERTUS_QUARTZ.growthProductIds());
        required.add(NativeCertusBudding.QUARTZ);
        for (BuddingRank rank : BuddingRank.values()) required.add(NativeCertusBudding.id(rank));
        for (var absent : required) {
            assertFalse(NativeCertusBudding.available(mod -> true, id -> !id.equals(absent)), absent.toString());
        }
    }

    @Test void nativeRankIdsAreExactAndEntroIsNotSelectedAsGeOre() {
        for (BuddingRank rank : BuddingRank.values()) {
            assertEquals("ae2:" + rank.path() + "_budding_quartz", NativeCertusBudding.id(rank).toString());
        }
        assertEquals("ae2:quartz_block", NativeCertusBudding.QUARTZ.toString());
        assertEquals("ae2:certus_quartz", NativeCertusBudding.FAMILY.toString());
        assertTrue(BuddingResourceFamily.fromGeOreMaterial("entroized_fluix").isEmpty());
    }
}
