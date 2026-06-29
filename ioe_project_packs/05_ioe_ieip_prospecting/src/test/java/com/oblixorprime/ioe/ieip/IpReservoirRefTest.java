package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class IpReservoirRefTest {
    @Test
    void rejectsBlockResources() {
        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef(
                "ip:not_fluid",
                ResourceRef.block("immersivepetroleum", "crude_oil"),
                false
        ));
    }

    @Test
    void rejectsFluidTags() {
        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef(
                "ip:tagged_oil",
                ResourceRef.fluidTag("immersivepetroleum", "crude_oil"),
                false
        ));
    }

    @Test
    void rejectsNonPetroleumFluids() {
        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef(
                "ip:water",
                ResourceRef.fluid("minecraft", "water"),
                false
        ));
    }
}
