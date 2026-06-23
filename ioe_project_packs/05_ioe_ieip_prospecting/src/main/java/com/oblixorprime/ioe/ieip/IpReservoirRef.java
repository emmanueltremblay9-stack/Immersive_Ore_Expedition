package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;

import java.util.Objects;

public record IpReservoirRef(String id, ResourceRef fluid, boolean gasLike) {
    public IpReservoirRef {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(fluid, "fluid");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }
}
