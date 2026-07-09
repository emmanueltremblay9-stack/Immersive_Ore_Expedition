package com.oblixorprime.ioe.expeditioncompass.client;

public final class ExpeditionCompassListWindow {
    private final int totalEntries;
    private final int visibleEntries;
    private final int firstVisibleIndex;
    private final int selectedIndex;

    public ExpeditionCompassListWindow(
            int totalEntries,
            int visibleEntries,
            int firstVisibleIndex,
            int selectedIndex
    ) {
        this.totalEntries = Math.max(0, totalEntries);
        this.visibleEntries = clampVisibleEntries(this.totalEntries, visibleEntries);
        this.firstVisibleIndex = clampFirstVisibleIndex(this.totalEntries, this.visibleEntries, firstVisibleIndex);
        this.selectedIndex = clampSelectedIndex(this.totalEntries, selectedIndex);
    }

    public static ExpeditionCompassListWindow empty() {
        return new ExpeditionCompassListWindow(0, 0, 0, -1);
    }

    public int totalEntries() {
        return totalEntries;
    }

    public int visibleEntries() {
        return visibleEntries;
    }

    public int firstVisibleIndex() {
        return firstVisibleIndex;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public int endExclusive() {
        return Math.min(totalEntries, firstVisibleIndex + visibleEntries);
    }

    public int maxFirstVisibleIndex() {
        return Math.max(0, totalEntries - visibleEntries);
    }

    public boolean scrollable() {
        return maxFirstVisibleIndex() > 0;
    }

    public boolean isEmpty() {
        return totalEntries == 0;
    }

    public ExpeditionCompassListWindow withVisibleEntries(int newVisibleEntries) {
        return new ExpeditionCompassListWindow(totalEntries, newVisibleEntries, firstVisibleIndex, selectedIndex);
    }

    public ExpeditionCompassListWindow withTotalEntries(int newTotalEntries) {
        return new ExpeditionCompassListWindow(newTotalEntries, visibleEntries, firstVisibleIndex, selectedIndex);
    }

    public ExpeditionCompassListWindow scrollRows(int rows) {
        return new ExpeditionCompassListWindow(
                totalEntries,
                visibleEntries,
                firstVisibleIndex + rows,
                selectedIndex
        );
    }

    public ExpeditionCompassListWindow page(int direction) {
        int delta = direction < 0 ? -visibleEntries : visibleEntries;
        return scrollRows(delta);
    }

    public ExpeditionCompassListWindow select(int index) {
        int clampedSelection = clampSelectedIndex(totalEntries, index);
        int adjustedFirstVisible = firstVisibleIndex;
        if (clampedSelection >= 0) {
            if (clampedSelection < adjustedFirstVisible) {
                adjustedFirstVisible = clampedSelection;
            } else if (clampedSelection >= adjustedFirstVisible + visibleEntries) {
                adjustedFirstVisible = clampedSelection - visibleEntries + 1;
            }
        }
        return new ExpeditionCompassListWindow(totalEntries, visibleEntries, adjustedFirstVisible, clampedSelection);
    }

    public ExpeditionCompassListWindow moveSelection(int delta) {
        if (totalEntries == 0) {
            return empty();
        }
        int baseline = selectedIndex < 0 ? firstVisibleIndex : selectedIndex;
        return select(baseline + delta);
    }

    public ExpeditionCompassListWindow jumpToScrollRatio(double ratio) {
        if (!scrollable()) {
            return this;
        }
        double clampedRatio = Math.max(0.0D, Math.min(1.0D, ratio));
        int targetFirstIndex = (int) Math.round(clampedRatio * maxFirstVisibleIndex());
        return new ExpeditionCompassListWindow(totalEntries, visibleEntries, targetFirstIndex, selectedIndex);
    }

    private static int clampVisibleEntries(int totalEntries, int visibleEntries) {
        if (totalEntries == 0) {
            return 0;
        }
        return Math.max(1, Math.min(totalEntries, visibleEntries));
    }

    private static int clampFirstVisibleIndex(int totalEntries, int visibleEntries, int firstVisibleIndex) {
        return Math.max(0, Math.min(Math.max(0, totalEntries - visibleEntries), firstVisibleIndex));
    }

    private static int clampSelectedIndex(int totalEntries, int selectedIndex) {
        if (totalEntries == 0) {
            return -1;
        }
        return Math.max(0, Math.min(totalEntries - 1, selectedIndex));
    }
}
