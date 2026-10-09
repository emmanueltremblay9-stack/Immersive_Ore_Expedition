package com.oblixorprime.ioe.nethergeodes;

/** Exact even-width window: candidate is the positive-side central cell on both axes. */
public final class NetherLakeWindow {
    public static final int WIDTH = 74;
    public static final int MIN_OFFSET = -37;
    public static final int MAX_OFFSET = 36;
    public static final int COLUMNS = WIDTH * WIDTH;
    public static final int MIN_CONNECTED_COLUMNS = (COLUMNS * 60 + 99) / 100;
    private NetherLakeWindow() { }
}
