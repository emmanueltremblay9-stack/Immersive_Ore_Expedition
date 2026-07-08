package com.oblixorprime.ioe.expeditionlocator;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ExpeditionSitePlacementState implements StringRepresentable {
    PLACED("placed", true),
    PLANNED("planned", false),
    DEBUG_MARKER("debug_marker", false),
    SKIPPED("skipped", false);

    public static final Codec<ExpeditionSitePlacementState> CODEC =
            StringRepresentable.fromEnum(ExpeditionSitePlacementState::values);

    private final String serializedName;
    private final boolean playable;

    ExpeditionSitePlacementState(String serializedName, boolean playable) {
        this.serializedName = serializedName;
        this.playable = playable;
    }

    public boolean playable() {
        return playable;
    }

    public String messageLabel() {
        return serializedName.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
