package com.oblixorprime.ioe.expeditionlocator;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ExpeditionSiteKind implements StringRepresentable {
    ANCHOR("anchor"),
    PROVINCE("province");

    public static final Codec<ExpeditionSiteKind> CODEC = StringRepresentable.fromEnum(ExpeditionSiteKind::values);

    private final String messageLabel;

    ExpeditionSiteKind(String messageLabel) {
        this.messageLabel = messageLabel;
    }

    public String messageLabel() {
        return messageLabel;
    }

    @Override
    public String getSerializedName() {
        return messageLabel;
    }
}
