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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Objects;

public final class ExpeditionCompassScreen extends Screen {
    static final String TITLE_KEY = "screen.immersive_ore_expedition.expedition_compass.title";
    static final String DIMENSION_KEY = "screen.immersive_ore_expedition.expedition_compass.dimension";
    static final String CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.current_target";
    static final String NO_CURRENT_TARGET_KEY = "screen.immersive_ore_expedition.expedition_compass.no_current_target";
    static final String EMPTY_KEY = "screen.immersive_ore_expedition.expedition_compass.empty";
    static final String ENTRY_KEY = "screen.immersive_ore_expedition.expedition_compass.entry";
    static final String MORE_ENTRIES_KEY = "screen.immersive_ore_expedition.expedition_compass.more_entries";
    static final String SELECT_KEY = "screen.immersive_ore_expedition.expedition_compass.select";
    static final String REFRESH_KEY = "screen.immersive_ore_expedition.expedition_compass.refresh";
    static final String CLEAR_KEY = "screen.immersive_ore_expedition.expedition_compass.clear";

    private ExpeditionCompassMenuSnapshot snapshot;

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
        if (minecraft != null) {
            rebuildWidgets();
        }
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(360, Math.max(220, width - 40));
        int left = (width - contentWidth) / 2;
        int y = 82;
        int visibleEntries = visibleEntryCount();
        List<ExpeditionCompassMenuEntry> entries = snapshot.entries();

        for (int index = 0; index < visibleEntries; index++) {
            ExpeditionCompassMenuEntry entry = entries.get(index);
            addRenderableWidget(Button.builder(entryButtonText(entry), button -> select(entry))
                    .bounds(left, y, contentWidth, 20)
                    .build());
            y += 24;
        }

        int bottomY = Math.max(y + 12, height - 28);
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

        int contentWidth = Math.min(360, Math.max(220, width - 40));
        int left = (width - contentWidth) / 2;
        guiGraphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        guiGraphics.drawString(
                font,
                Component.translatable(DIMENSION_KEY, snapshot.dimension().location().toString()),
                left,
                38,
                0xE0E0E0
        );
        guiGraphics.drawString(font, currentTargetText(), left, 52, 0xC8C8C8);

        if (snapshot.entries().isEmpty()) {
            guiGraphics.drawCenteredString(font, Component.translatable(EMPTY_KEY), width / 2, 86, 0xB8B8B8);
            return;
        }

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

    private int visibleEntryCount() {
        int availableRows = Math.max(0, (height - 130) / 24);
        return Math.min(snapshot.entries().size(), availableRows);
    }

    private Component currentTargetText() {
        return snapshot.currentTarget()
                .map(target -> Component.translatable(
                        CURRENT_TARGET_KEY,
                        target.kind().messageLabel(),
                        target.primaryId().map(ResourceLocation::toString).orElse("unknown"),
                        target.pos().getX(),
                        target.pos().getY(),
                        target.pos().getZ()
                ))
                .orElseGet(() -> Component.translatable(NO_CURRENT_TARGET_KEY));
    }

    private Component entryButtonText(ExpeditionCompassMenuEntry entry) {
        ExpeditionCompassTarget target = entry.target();
        return Component.translatable(
                ENTRY_KEY,
                Component.translatable(SELECT_KEY),
                target.kind().messageLabel(),
                target.primaryId().map(ResourceLocation::toString).orElse("unknown"),
                target.pos().getX(),
                target.pos().getY(),
                target.pos().getZ(),
                entry.distanceBlocks()
        );
    }

    private void select(ExpeditionCompassMenuEntry entry) {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassSelectPayload(snapshot.hand(), entry.target()));
    }

    private void clearTarget() {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassClearPayload(snapshot.hand()));
    }

    private void refresh() {
        PacketDistributor.sendToServer(new ServerboundExpeditionCompassRefreshPayload(snapshot.hand()));
    }
}
