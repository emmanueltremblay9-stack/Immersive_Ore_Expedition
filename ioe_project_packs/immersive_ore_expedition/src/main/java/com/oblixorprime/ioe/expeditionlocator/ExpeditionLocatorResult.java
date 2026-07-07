package com.oblixorprime.ioe.expeditionlocator;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public record ExpeditionLocatorResult(
        Status status,
        Optional<ExpeditionSite> site,
        long distanceSquared
) {
    public ExpeditionLocatorResult {
        status = Objects.requireNonNull(status, "status");
        site = site == null ? Optional.empty() : site;
        if (status == Status.FOUND && site.isEmpty()) {
            throw new IllegalArgumentException("found locator results require a site");
        }
        if (status == Status.FOUND && distanceSquared < 0L) {
            throw new IllegalArgumentException("found locator results require a non-negative distance");
        }
        if (status == Status.NO_INDEXED_SITES && site.isPresent()) {
            throw new IllegalArgumentException("no-result locator results must not carry a site");
        }
        if (status == Status.NO_INDEXED_SITES && distanceSquared != -1L) {
            throw new IllegalArgumentException("no-result locator results must use -1 distance");
        }
        if (distanceSquared < -1L) {
            throw new IllegalArgumentException("distanceSquared must be non-negative or -1 for no result");
        }
    }

    public static ExpeditionLocatorResult found(ExpeditionSite site, long distanceSquared) {
        Objects.requireNonNull(site, "site");
        if (distanceSquared < 0L) {
            throw new IllegalArgumentException("distanceSquared must not be negative");
        }
        return new ExpeditionLocatorResult(Status.FOUND, Optional.of(site), distanceSquared);
    }

    public static ExpeditionLocatorResult noIndexedSites() {
        return new ExpeditionLocatorResult(Status.NO_INDEXED_SITES, Optional.empty(), -1L);
    }

    public boolean found() {
        return status == Status.FOUND;
    }

    public OptionalLong distanceBlocks() {
        if (!found()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Math.round(Math.sqrt(distanceSquared)));
    }

    public enum Status {
        FOUND,
        NO_INDEXED_SITES
    }
}
