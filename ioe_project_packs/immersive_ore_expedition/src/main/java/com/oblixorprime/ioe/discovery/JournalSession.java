package com.oblixorprime.ioe.discovery;

import java.util.Optional;

/** Client cache model kept independent of Minecraft/IE for lifecycle verification. */
public final class JournalSession {
    private long sequence;
    private Long pending;
    private DiscoveryPage page;
    public JournalRequest request(int offset) {
        pending = ++sequence;
        page = null;
        return new JournalRequest(pending, offset);
    }
    public boolean accept(JournalResponse response) {
        if (pending == null || pending.longValue() != response.token()) return false;
        page = response.page(); pending = null; return true;
    }
    public Optional<DiscoveryPage> page() { return Optional.ofNullable(page); }
    public void clear() { pending = null; page = null; } // Keep sequence: stale responses cannot match a new session.
}
