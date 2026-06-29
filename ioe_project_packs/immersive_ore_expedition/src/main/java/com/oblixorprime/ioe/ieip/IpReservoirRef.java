package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;

import java.util.Objects;

public record IpReservoirRef(String id, ResourceRef fluid, boolean gasLike) {
    public IpReservoirRef {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(fluid, "fluid");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (fluid.type() != ResourceType.FLUID) {
            throw new IllegalArgumentException("IP reservoir reference requires a concrete fluid resource");
        }
        if (!ProspectingCompatGates.IMMERSIVE_PETROLEUM.equals(fluid.id().getNamespace())) {
            throw new IllegalArgumentException("IP reservoir reference requires an Immersive Petroleum fluid");
        }
    }
}
