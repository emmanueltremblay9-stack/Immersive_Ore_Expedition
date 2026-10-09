package com.oblixorprime.ioe.worldgen;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

/** Session-only, insertion-ordered permissions. Reloading never refreshes their lifetime. */
final class FirstLoadAdmissions<K> {
    private final int capacity;
    private final long lifetimeTicks;
    private final Consumer<K> onRemoval;
    private final LinkedHashMap<K, Long> admittedAt = new LinkedHashMap<>();

    FirstLoadAdmissions(int capacity, long lifetimeTicks, Consumer<K> onRemoval) {
        if (capacity < 1 || lifetimeTicks < 1) throw new IllegalArgumentException("Positive limits required");
        this.capacity = capacity;
        this.lifetimeTicks = lifetimeTicks;
        this.onRemoval = Objects.requireNonNull(onRemoval);
    }

    synchronized boolean admit(K key, boolean genuinelyNew, long tick) {
        expire(tick);
        if (admittedAt.containsKey(key)) return true;
        if (!genuinelyNew) return false;
        while (admittedAt.size() >= capacity) remove(admittedAt.firstEntry().getKey());
        admittedAt.put(key, tick);
        return true;
    }

    synchronized boolean contains(K key, long tick) {
        expire(tick);
        return admittedAt.containsKey(key);
    }

    synchronized void expire(long tick) {
        while (!admittedAt.isEmpty() && tick - admittedAt.firstEntry().getValue() >= lifetimeTicks) {
            remove(admittedAt.firstEntry().getKey());
        }
    }

    synchronized void remove(K key) {
        if (admittedAt.remove(key) != null) onRemoval.accept(key);
    }

    synchronized void clear() {
        while (!admittedAt.isEmpty()) remove(admittedAt.firstEntry().getKey());
    }

    synchronized int size() {
        return admittedAt.size();
    }
}
