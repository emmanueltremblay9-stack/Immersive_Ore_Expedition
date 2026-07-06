package com.oblixorprime.ioe.expeditionlocator;

public enum ExpeditionSiteKind {
    ANCHOR("anchor"),
    PROVINCE("province");

    private final String messageLabel;

    ExpeditionSiteKind(String messageLabel) {
        this.messageLabel = messageLabel;
    }

    public String messageLabel() {
        return messageLabel;
    }
}
