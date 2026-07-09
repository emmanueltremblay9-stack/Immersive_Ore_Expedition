package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.expeditioncompass.client.ExpeditionCompassListWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionCompassListWindowTest {
    @Test
    void exposesVisibleWindowWithoutDroppingValidEntries() {
        ExpeditionCompassListWindow window = new ExpeditionCompassListWindow(6, 3, 0, -1);

        assertEquals(6, window.totalEntries());
        assertEquals(3, window.visibleEntries());
        assertEquals(0, window.firstVisibleIndex());
        assertEquals(3, window.endExclusive());
        assertTrue(window.scrollable());
    }

    @Test
    void wheelScrollClampsToLastVisibleWindow() {
        ExpeditionCompassListWindow window = new ExpeditionCompassListWindow(6, 3, 0, -1);

        ExpeditionCompassListWindow scrolled = window.scrollRows(99);

        assertEquals(3, scrolled.firstVisibleIndex());
        assertEquals(6, scrolled.endExclusive());
    }

    @Test
    void keyboardSelectionScrollsToKeepSelectedEntryVisible() {
        ExpeditionCompassListWindow window = new ExpeditionCompassListWindow(6, 3, 0, 0);

        ExpeditionCompassListWindow selected = window.moveSelection(4);

        assertEquals(4, selected.selectedIndex());
        assertEquals(2, selected.firstVisibleIndex());
        assertEquals(5, selected.endExclusive());
    }

    @Test
    void pageDownMovesByVisibleRows() {
        ExpeditionCompassListWindow window = new ExpeditionCompassListWindow(10, 4, 0, -1);

        ExpeditionCompassListWindow paged = window.page(1);

        assertEquals(4, paged.firstVisibleIndex());
        assertEquals(8, paged.endExclusive());
    }

    @Test
    void scrollbarRatioMapsToScrollRange() {
        ExpeditionCompassListWindow window = new ExpeditionCompassListWindow(10, 4, 0, -1);

        ExpeditionCompassListWindow dragged = window.jumpToScrollRatio(1.0D);

        assertEquals(6, dragged.firstVisibleIndex());
        assertEquals(10, dragged.endExclusive());
    }

    @Test
    void emptyWindowHasNoSelectionOrScrolling() {
        ExpeditionCompassListWindow window = ExpeditionCompassListWindow.empty();

        assertFalse(window.scrollable());
        assertTrue(window.isEmpty());
        assertEquals(-1, window.selectedIndex());
        assertEquals(0, window.endExclusive());
    }
}
