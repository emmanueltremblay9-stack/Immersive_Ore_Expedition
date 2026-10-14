package com.oblixorprime.ioe.discovery;

import java.util.Optional;

/** One record per response: payload size is independent of journal size. */
public record DiscoveryPage(int offset, int total, Optional<DiscoveryView> entry) {
    public DiscoveryPage {
        if (offset < 0 || total < 0 || entry == null
                || (total == 0 ? offset != 0 || entry.isPresent() : offset >= total || entry.isEmpty()))
            throw new IllegalArgumentException("Invalid discovery page");
    }
}
