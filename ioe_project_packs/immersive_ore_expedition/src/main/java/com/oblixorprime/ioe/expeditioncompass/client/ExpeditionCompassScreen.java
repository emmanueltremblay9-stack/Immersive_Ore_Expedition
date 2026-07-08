package com.oblixorprime.ioe.expeditioncompass.client;

import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassMenuEntry;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassMenuSnapshot;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassClearPayload;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassRefreshPayload;
import com.oblixorprime.ioe.expeditioncompass.ServerboundExpeditionCompassSelectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

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
    private static final int PAGINATION_BUTTON_WIDTH = 24;
    private static final int PAGINATION_BUTTON_GAP = 4;

    static final String TITLE_KEY = "screen.immersive_ore_expedition.expedition_compass.title";
    static final String DIMENSION_KEY = "screen.immersive_ore_expedition.expedition_compass.dimension";
    static final String CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.current_target";
    static final String NO_CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.no_current_target";
    static final String EMPTY_KEY = "screen.immersive_ore_expedition.expedition_compass.empty";
    static final String ENTRY_TITLE_KEY = "screen.immersive_ore_expedition.expedition_compass.entry_title";
    static final String ENTRY_DETAIL_KEY = "screen.immersive_ore_expedition.expedition_compass.entry_detail";
    static final String MORE_ENTRIES_KEY = "screen.immersive_ore_expedition.expedition_compass.more_entries";
    static final String SHOWING_ENTRIES_KEY = "screen.immersive_ore_expedition.expedition_compass.showing_entries";
    static final String PREVIOUS_KEY = "screen.immersive_ore_expedition.expedition_compass.previous";
    static final String NEXT_KEY = "screen.immersive_ore_expedition.expedition_compass.next";
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
    private int scrollOffset;

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
        clampScrollOffset();
        if (minecraft != null) {
            rebuildWidgets();
        }
    }

    @Override
    protected void init() {
        clampScrollOffset();
        int contentWidth = contentWidth();
        int left = (width - contentWidth) / 2;
        int y = ENTRY_TOP;
        int visibleEntries = visibleEntryCount();
        List<ExpeditionCompassMenuEntry> entries = snapshot.entries();
        int journeyMapButtonWidth = journeyMapButtonWidth(contentWidth);
        int journeyMapButtonX = left + contentWidth - journeyMapButtonWidth;
        int bindButtonX = journeyMapButtonX - ENTRY_ACTION_GAP - BIND_BUTTON_WIDTH;

        int endIndex = Math.min(entries.size(), scrollOffset + visibleEntries);
        for (int index = scrollOffset; index < endIndex; index++) {
            ExpeditionCompassMenuEntry entry = entries.get(index);
            addRenderableWidget(Button.builder(Component.translatable(SELECT_KEY), button -> select(entry))
                    .bounds(bindButtonX, y + 12, BIND_BUTTON_WIDTH, 20)
                    .build());
            addRenderableWidget(Button.builder(journeyMapButtonText(journeyMapButtonWidth), button -> createJourneyMapWaypoint(entry))
                    .bounds(journeyMapButtonX, y + 12, journeyMapButtonWidth, 20)
                    .build());
            y += ENTRY_ROW_HEIGHT;
        }

        if (maxScrollOffset() > 0) {
            int paginationY = height - 52;
            Button previousButton = Button.builder(Component.translatable(PREVIOUS_KEY), button -> scrollBy(-1))
                    .bounds(
                            left + contentWidth - PAGINATION_BUTTON_WIDTH * 2 - PAGINATION_BUTTON_GAP,
                            paginationY,
                            PAGINATION_BUTTON_WIDTH,
                            20
                    )
                    .build();
            previousButton.active = scrollOffset > 0;
            addRenderableWidget(previousButton);
            Button nextButton = Button.builder(Component.translatable(NEXT_KEY), button -> scrollBy(1))
                    .bounds(left + contentWidth - PAGINATION_BUTTON_WIDTH, paginationY, PAGINATION_BUTTON_WIDTH, 20)
                    .build();
            nextButton.active = scrollOffset < maxScrollOffset();
            addRenderableWidget(nextButton);
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
            guiGraphics.drawCenteredString(font, Component.translatable(EMPTY_KEY), width / 2, 86, 0xB8B8B8);
            return;
        }

        int y = ENTRY_TOP + 5;
        int visibleEntries = visibleEntryCount();
        int endIndex = Math.min(snapshot.entries().size(), scrollOffset + visibleEntries);
        for (int index = scrollOffset; index < endIndex; index++) {
            ExpeditionCompassMenuEntry entry = snapshot.entries().get(index);
            drawClampedString(guiGraphics, entryTitleText(entry), left + ENTRY_TEXT_LEFT_PADDING, y, entryTextWidth, 0xFFD35A);
            drawClampedString(guiGraphics, entryDetailText(entry), left + ENTRY_TEXT_LEFT_PADDING, y + 12, entryTextWidth, 0xC8C8C8);
            y += ENTRY_ROW_HEIGHT;
        }

        if (maxScrollOffset() > 0) {
            drawClampedString(
                    guiGraphics,
                    Component.translatable(SHOWING_ENTRIES_KEY, scrollOffset + 1, endIndex, snapshot.entries().size()),
                    left,
                    height - 48,
                    contentWidth - PAGINATION_BUTTON_WIDTH * 2 - PAGINATION_BUTTON_GAP - 8,
                    0xA0A0A0
            );
        } else {
            int hiddenEntries = snapshot.entries().size() - visibleEntryCount();
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
        if (maxScrollOffset() <= 0) {
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

    private int visibleEntryCount() {
        int availableRows = Math.max(0, (height - ENTRY_TOP - 62) / ENTRY_ROW_HEIGHT);
        if (snapshot.entries().isEmpty()) {
            return 0;
        }
        return Math.max(1, Math.min(snapshot.entries().size(), availableRows));
    }

    private int maxScrollOffset() {
        return Math.max(0, snapshot.entries().size() - visibleEntryCount());
    }

    private void scrollBy(int delta) {
        int previousOffset = scrollOffset;
        scrollOffset = Math.max(0, Math.min(maxScrollOffset(), scrollOffset + delta));
        if (scrollOffset != previousOffset) {
            rebuildWidgets();
        }
    }

    private void clampScrollOffset() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset()));
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
        return Math.max(40, contentWidth - actionWidth - ENTRY_TEXT_LEFT_PADDING - 8);
    }

    private int journeyMapButtonWidth(int contentWidth) {
        return contentWidth < 360 ? COMPACT_JOURNEYMAP_BUTTON_WIDTH : JOURNEYMAP_BUTTON_WIDTH;
    }

    private Component journeyMapButtonText(int buttonWidth) {
        return Component.translatable(buttonWidth < 60 ? JOURNEYMAP_SHORT_KEY : JOURNEYMAP_KEY);
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
