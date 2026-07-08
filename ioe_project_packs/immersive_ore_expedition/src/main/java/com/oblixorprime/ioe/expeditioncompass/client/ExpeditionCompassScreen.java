package com.oblixorprime.ioe.expeditioncompass.client;

import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassMenuEntry;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassMenuSnapshot;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassEmptyReason;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassClearPayload;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassRefreshPayload;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassSelectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

public final class ExpeditionCompassScreen extends Screen {
    private static final int ENTRY_TOP = 78;
    private static final int ENTRY_ROW_HEIGHT = 44;
    private static final int ENTRY_TEXT_LEFT_PADDING = 8;
    private static final int ENTRY_ACTION_GAP = 4;
    private static final int BIND_BUTTON_WIDTH = 54;
    private static final int JOURNEYMAP_BUTTON_WIDTH = 86;
    private static final int COMPACT_JOURNEYMAP_BUTTON_WIDTH = 42;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 6;

    static final String TITLE_KEY = "screen.immersive_ore_expedition.expedition_compass.title";
    static final String DIMENSION_KEY = "screen.immersive_ore_expedition.expedition_compass.dimension";
    static final String CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.current_target";
    static final String NO_CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.no_current_target";
    static final String EMPTY_KEY = "screen.immersive_ore_expedition.expedition_compass.empty";
    static final String EMPTY_WORLDGEN_DISABLED_KEY = "screen.immersive_ore_expedition.expedition_compass.empty_worldgen_disabled";
    static final String EMPTY_ONLY_DIAGNOSTIC_KEY = "screen.immersive_ore_expedition.expedition_compass.empty_only_diagnostic";
    static final String ENTRY_TITLE_KEY = "screen.immersive_ore_expedition.expedition_compass.entry_title";
    static final String ENTRY_TITLE_DIAGNOSTIC_KEY = "screen.immersive_ore_expedition.expedition_compass.entry_title_diagnostic";
    static final String ENTRY_DETAIL_KEY = "screen.immersive_ore_expedition.expedition_compass.entry_detail";
    static final String MORE_ENTRIES_KEY = "screen.immersive_ore_expedition.expedition_compass.more_entries";
    static final String SHOWING_ENTRIES_KEY = "screen.immersive_ore_expedition.expedition_compass.showing_entries";
    static final String SELECT_KEY = "screen.immersive_ore_expedition.expedition_compass.select";
    static final String JOURNEYMAP_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap";
    static final String JOURNEYMAP_SHORT_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_short";
    static final String JOURNEYMAP_CREATED_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_created";
    static final String JOURNEYMAP_NOT_PLAYABLE_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_not_playable";
    static final String JOURNEYMAP_NOT_LOADED_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_not_loaded";
    static final String JOURNEYMAP_NOT_READY_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_not_ready";
    static final String JOURNEYMAP_FAILED_KEY = "screen.immersive_ore_expedition.expedition_compass.journeymap_failed";
    static final String REFRESH_KEY = "screen.immersive_ore_expedition.expedition_compass.refresh";
    static final String CLEAR_KEY = "screen.immersive_ore_expedition.expedition_compass.clear";

    private ExpeditionCompassMenuSnapshot snapshot;
    private ExpeditionCompassListWindow listWindow = ExpeditionCompassListWindow.empty();
    private boolean draggingScrollbar;

    private ExpeditionCompassScreen(ExpeditionCompassMenuSnapshot snapshot) {
        super(Component.translatable(TITLE_KEY));
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    public static void open(ExpeditionCompassMenuSnapshot snapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof ExpeditionCompassScreen screen) {
                screen.setSnapshot(snapshot);
            } else {
                minecraft.setScreen(new ExpeditionCompassScreen(snapshot));
            }
        });
    }

    private void setSnapshot(ExpeditionCompassMenuSnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        updateListWindowForCurrentSize();
        if (minecraft != null) {
            rebuildWidgets();
        }
    }

    @Override
    protected void init() {
        updateListWindowForCurrentSize();
        int contentWidth = contentWidth();
        int left = (width - contentWidth) / 2;
        int y = ENTRY_TOP;
        List<ExpeditionCompassMenuEntry> entries = snapshot.entries();
        int journeyMapButtonWidth = journeyMapButtonWidth(contentWidth);
        int journeyMapButtonX = left + contentWidth - journeyMapButtonWidth;
        int bindButtonX = journeyMapButtonX - ENTRY_ACTION_GAP - BIND_BUTTON_WIDTH;

        for (int index = listWindow.firstVisibleIndex(); index < listWindow.endExclusive(); index++) {
            ExpeditionCompassMenuEntry entry = entries.get(index);
            Button bindButton = Button.builder(Component.translatable(SELECT_KEY), button -> select(entry))
                    .bounds(bindButtonX, y + 12, BIND_BUTTON_WIDTH, 20)
                    .build();
            bindButton.active = entry.target().playable();
            addRenderableWidget(bindButton);
            Button journeyMapButton = Button.builder(journeyMapButtonText(journeyMapButtonWidth), button -> createJourneyMapWaypoint(entry))
                    .bounds(journeyMapButtonX, y + 12, journeyMapButtonWidth, 20)
                    .build();
            journeyMapButton.active = entry.target().playable();
            addRenderableWidget(journeyMapButton);
            y += ENTRY_ROW_HEIGHT;
        }

        int bottomY = height - 28;
        int buttonWidth = Math.max(70, (contentWidth - 12) / 3);
        addRenderableWidget(Button.builder(Component.translatable(REFRESH_KEY), button -> refresh())
                .bounds(left, bottomY, buttonWidth, 20)
                .build());
        Button clearButton = Button.builder(Component.translatable(CLEAR_KEY), button -> clearTarget())
                .bounds(left + buttonWidth + 6, bottomY, buttonWidth, 20)
                .build();
        clearButton.active = snapshot.currentTarget().isPresent();
        addRenderableWidget(clearButton);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + (buttonWidth + 6) * 2, bottomY, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int contentWidth = contentWidth();
        int left = (width - contentWidth) / 2;
        int entryTextWidth = entryTextWidth(contentWidth);
        guiGraphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        drawClampedString(
                guiGraphics,
                Component.translatable(DIMENSION_KEY, snapshot.dimension().location().toString()),
                left,
                38,
                contentWidth,
                0xE0E0E0
        );
        drawClampedString(guiGraphics, currentTargetText(), left, 52, contentWidth, 0xC8C8C8);

        if (snapshot.entries().isEmpty()) {
            guiGraphics.drawCenteredString(font, emptyText(), width / 2, 86, 0xB8B8B8);
            return;
        }

        int y = ENTRY_TOP + 5;
        int endIndex = listWindow.endExclusive();
        for (int index = listWindow.firstVisibleIndex(); index < endIndex; index++) {
            ExpeditionCompassMenuEntry entry = snapshot.entries().get(index);
            if (index == listWindow.selectedIndex()) {
                guiGraphics.fill(left - 3, y - 4, left + contentWidth + 3, y + ENTRY_ROW_HEIGHT - 8, 0x40202020);
            }
            drawClampedString(guiGraphics, entryTitleText(entry), left + ENTRY_TEXT_LEFT_PADDING, y, entryTextWidth, 0xFFD35A);
            drawClampedString(guiGraphics, entryDetailText(entry), left + ENTRY_TEXT_LEFT_PADDING, y + 12, entryTextWidth, 0xC8C8C8);
            y += ENTRY_ROW_HEIGHT;
        }

        if (listWindow.scrollable()) {
            drawClampedString(
                    guiGraphics,
                    Component.translatable(
                            SHOWING_ENTRIES_KEY,
                            listWindow.firstVisibleIndex() + 1,
                            endIndex,
                            snapshot.entries().size()
                    ),
                    left,
                    height - 48,
                    contentWidth - SCROLLBAR_WIDTH - SCROLLBAR_GAP - 8,
                    0xA0A0A0
            );
            renderScrollbar(guiGraphics, left, contentWidth);
        } else {
            int hiddenEntries = snapshot.entries().size() - listWindow.visibleEntries();
            if (hiddenEntries > 0) {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable(MORE_ENTRIES_KEY, hiddenEntries),
                    width / 2,
                    Math.max(108, height - 48),
                    0xA0A0A0
                );
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!listWindow.scrollable()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (scrollY > 0.0D) {
            scrollBy(-1);
            return true;
        }
        if (scrollY < 0.0D) {
            scrollBy(1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            moveSelection(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            moveSelection(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            page(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            page(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            selectedEntry().filter(entry -> entry.target().playable()).ifPresent(this::select);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollbarContains(mouseX, mouseY)) {
            draggingScrollbar = true;
            updateScrollbarDrag(mouseY);
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int clickedEntry = entryIndexAt(mouseX, mouseY);
        if (clickedEntry >= 0) {
            listWindow = listWindow.select(clickedEntry);
            rebuildWidgets();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            updateScrollbarDrag(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int visibleEntryCount() {
        int availableRows = Math.max(0, (height - ENTRY_TOP - 62) / ENTRY_ROW_HEIGHT);
        if (snapshot.entries().isEmpty()) {
            return 0;
        }
        return Math.max(1, Math.min(snapshot.entries().size(), availableRows));
    }

    private void scrollBy(int delta) {
        ExpeditionCompassListWindow previousWindow = listWindow;
        listWindow = listWindow.scrollRows(delta);
        if (listWindow.firstVisibleIndex() != previousWindow.firstVisibleIndex()) {
            rebuildWidgets();
        }
    }

    private void moveSelection(int delta) {
        ExpeditionCompassListWindow previousWindow = listWindow;
        listWindow = listWindow.moveSelection(delta);
        if (listWindow.selectedIndex() != previousWindow.selectedIndex()
                || listWindow.firstVisibleIndex() != previousWindow.firstVisibleIndex()) {
            rebuildWidgets();
        }
    }

    private void page(int direction) {
        ExpeditionCompassListWindow previousWindow = listWindow;
        listWindow = listWindow.page(direction);
        if (listWindow.firstVisibleIndex() != previousWindow.firstVisibleIndex()) {
            rebuildWidgets();
        }
    }

    private void updateListWindowForCurrentSize() {
        listWindow = new ExpeditionCompassListWindow(
                snapshot.entries().size(),
                visibleEntryCount(),
                listWindow.firstVisibleIndex(),
                listWindow.selectedIndex()
        );
    }

    private Component currentTargetText() {
        return snapshot.currentTarget()
                .map(target -> Component.translatable(
                        CURRENT_TARGET_KEY,
                        target.displayName(),
                        target.coordinateText()
                ))
                .orElseGet(() -> Component.translatable(NO_CURRENT_TARGET_KEY));
    }

    private Component entryTitleText(ExpeditionCompassMenuEntry entry) {
        ExpeditionCompassTarget target = entry.target();
        if (!target.playable()) {
            return Component.translatable(
                    ENTRY_TITLE_DIAGNOSTIC_KEY,
                    target.placementState().messageLabel().toUpperCase(java.util.Locale.ROOT),
                    target.displayName()
            );
        }
        return Component.translatable(
                ENTRY_TITLE_KEY,
                target.displayName()
        );
    }

    private Component entryDetailText(ExpeditionCompassMenuEntry entry) {
        ExpeditionCompassTarget target = entry.target();
        return Component.translatable(
                ENTRY_DETAIL_KEY,
                readableKind(target),
                target.coordinateText(),
                entry.distanceBlocks()
        );
    }

    private static String readableKind(ExpeditionCompassTarget target) {
        String label = target.kind().messageLabel();
        if (label.isEmpty()) {
            return "Target";
        }
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    private void select(ExpeditionCompassMenuEntry entry) {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassSelectPayload(snapshot.hand(), entry.target()));
    }

    private void createJourneyMapWaypoint(ExpeditionCompassMenuEntry entry) {
        JourneyMapWaypointBridge.Result result = JourneyMapWaypointBridge.createWaypoint(entry.target());
        Component message = switch (result) {
            case CREATED -> Component.translatable(JOURNEYMAP_CREATED_KEY, entry.target().displayName());
            case NOT_PLAYABLE -> Component.translatable(JOURNEYMAP_NOT_PLAYABLE_KEY);
            case NOT_LOADED -> Component.translatable(JOURNEYMAP_NOT_LOADED_KEY);
            case NOT_READY -> Component.translatable(JOURNEYMAP_NOT_READY_KEY);
            case FAILED -> Component.translatable(JOURNEYMAP_FAILED_KEY);
        };
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(message, false);
        }
    }

    private void clearTarget() {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassClearPayload(snapshot.hand()));
    }

    private void refresh() {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassRefreshPayload(snapshot.hand()));
    }

    private int contentWidth() {
        return Math.min(560, Math.max(280, width - 48));
    }

    private int entryTextWidth(int contentWidth) {
        int actionWidth = BIND_BUTTON_WIDTH + ENTRY_ACTION_GAP + journeyMapButtonWidth(contentWidth);
        int scrollbarWidth = listWindow.scrollable() ? SCROLLBAR_WIDTH + SCROLLBAR_GAP : 0;
        return Math.max(40, contentWidth - actionWidth - scrollbarWidth - ENTRY_TEXT_LEFT_PADDING - 8);
    }

    private int journeyMapButtonWidth(int contentWidth) {
        return contentWidth < 360 ? COMPACT_JOURNEYMAP_BUTTON_WIDTH : JOURNEYMAP_BUTTON_WIDTH;
    }

    private Component journeyMapButtonText(int buttonWidth) {
        return Component.translatable(buttonWidth < 60 ? JOURNEYMAP_SHORT_KEY : JOURNEYMAP_KEY);
    }

    private Component emptyText() {
        return switch (snapshot.emptyReason()) {
            case WORLDGEN_DISABLED -> Component.translatable(EMPTY_WORLDGEN_DISABLED_KEY);
            case ONLY_DEBUG_OR_PLANNED_SITES -> Component.translatable(EMPTY_ONLY_DIAGNOSTIC_KEY);
            case NO_PLACED_SITES -> Component.translatable(EMPTY_KEY);
        };
    }

    private java.util.Optional<ExpeditionCompassMenuEntry> selectedEntry() {
        int selectedIndex = listWindow.selectedIndex();
        if (selectedIndex < 0 || selectedIndex >= snapshot.entries().size()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(snapshot.entries().get(selectedIndex));
    }

    private int entryIndexAt(double mouseX, double mouseY) {
        int contentWidth = contentWidth();
        int left = (width - contentWidth) / 2;
        if (mouseX < left || mouseX > left + contentWidth || mouseY < ENTRY_TOP) {
            return -1;
        }
        int row = (int) ((mouseY - ENTRY_TOP) / ENTRY_ROW_HEIGHT);
        if (row < 0 || row >= listWindow.visibleEntries()) {
            return -1;
        }
        int index = listWindow.firstVisibleIndex() + row;
        return index < snapshot.entries().size() ? index : -1;
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int left, int contentWidth) {
        int trackX = left + contentWidth - SCROLLBAR_WIDTH;
        int trackTop = ENTRY_TOP;
        int trackBottom = listBottom();
        guiGraphics.fill(trackX, trackTop, trackX + SCROLLBAR_WIDTH, trackBottom, 0x60101010);

        int thumbTop = scrollbarThumbTop(trackTop, trackBottom);
        int thumbBottom = scrollbarThumbBottom(trackTop, trackBottom);
        guiGraphics.fill(trackX, thumbTop, trackX + SCROLLBAR_WIDTH, thumbBottom, 0xFFD6A800);
    }

    private boolean scrollbarContains(double mouseX, double mouseY) {
        if (!listWindow.scrollable()) {
            return false;
        }
        int contentWidth = contentWidth();
        int left = (width - contentWidth) / 2;
        int trackX = left + contentWidth - SCROLLBAR_WIDTH;
        return mouseX >= trackX
                && mouseX <= trackX + SCROLLBAR_WIDTH
                && mouseY >= ENTRY_TOP
                && mouseY <= listBottom();
    }

    private void updateScrollbarDrag(double mouseY) {
        int trackTop = ENTRY_TOP;
        int trackBottom = listBottom();
        int thumbHeight = scrollbarThumbHeight(trackTop, trackBottom);
        int travel = Math.max(1, trackBottom - trackTop - thumbHeight);
        double ratio = (mouseY - trackTop - thumbHeight / 2.0D) / travel;
        ExpeditionCompassListWindow previousWindow = listWindow;
        listWindow = listWindow.jumpToScrollRatio(ratio);
        if (listWindow.firstVisibleIndex() != previousWindow.firstVisibleIndex()) {
            rebuildWidgets();
        }
    }

    private int scrollbarThumbTop(int trackTop, int trackBottom) {
        int thumbHeight = scrollbarThumbHeight(trackTop, trackBottom);
        int travel = Math.max(0, trackBottom - trackTop - thumbHeight);
        double ratio = listWindow.maxFirstVisibleIndex() == 0
                ? 0.0D
                : (double) listWindow.firstVisibleIndex() / listWindow.maxFirstVisibleIndex();
        return trackTop + (int) Math.round(ratio * travel);
    }

    private int scrollbarThumbBottom(int trackTop, int trackBottom) {
        return Math.min(trackBottom, scrollbarThumbTop(trackTop, trackBottom) + scrollbarThumbHeight(trackTop, trackBottom));
    }

    private int scrollbarThumbHeight(int trackTop, int trackBottom) {
        int trackHeight = Math.max(1, trackBottom - trackTop);
        double visibleRatio = (double) listWindow.visibleEntries() / Math.max(1, listWindow.totalEntries());
        return Math.max(18, (int) Math.round(trackHeight * visibleRatio));
    }

    private int listBottom() {
        return Math.max(ENTRY_TOP + ENTRY_ROW_HEIGHT, height - 62);
    }

    private void drawClampedString(GuiGraphics guiGraphics, Component text, int x, int y, int maxWidth, int color) {
        guiGraphics.drawString(font, fitToWidth(text.getString(), maxWidth), x, y, color);
    }

    private String fitToWidth(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        int suffixWidth = font.width(suffix);
        int targetWidth = Math.max(0, maxWidth - suffixWidth);
        StringBuilder builder = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (font.width(builder.toString() + character) > targetWidth) {
                break;
            }
            builder.append(character);
        }
        return builder + suffix;
    }
}
