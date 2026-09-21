package com.oblixorprime.ioe.budding;

import java.util.Optional;

public enum BuddingRank {
    DAMAGED("damaged"),
    CHIPPED("chipped"),
    FLAWED("flawed"),
    FLAWLESS("flawless");

    public static final BuddingRank MAXIMUM_RESTORABLE = FLAWED;

    private final String path;

    BuddingRank(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public boolean canGrowthDegrade() {
        return this != FLAWLESS;
    }

    public Optional<BuddingRank> degradedRank() {
        return switch (this) {
            case FLAWED -> Optional.of(CHIPPED);
            case CHIPPED -> Optional.of(DAMAGED);
            case DAMAGED, FLAWLESS -> Optional.empty();
        };
    }

    public Optional<BuddingRank> restoredRank() {
        return switch (this) {
            case DAMAGED -> Optional.of(CHIPPED);
            case CHIPPED -> Optional.of(FLAWED);
            case FLAWED, FLAWLESS -> Optional.empty();
        };
    }
}
