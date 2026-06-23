package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;

import java.util.List;
import java.util.Objects;

public record IeMineralDepositRef(String id, List<ResourceRef> presentResources) {
    public IeMineralDepositRef {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        presentResources = List.copyOf(Objects.requireNonNull(presentResources, "presentResources"));
    }
}
