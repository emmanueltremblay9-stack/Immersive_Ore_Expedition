package com.oblixorprime.ioe.discovery.client;

import com.oblixorprime.ioe.discovery.DiscoveryPage;
import com.oblixorprime.ioe.discovery.DiscoveryStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JournalScreen extends Screen {
    private final Screen parent;
    private Button previous, next;
    private int scroll;
    private List<FormattedCharSequence> lines = List.of();
    private JournalScreen(Screen parent) { super(text("title")); this.parent = parent; }
    public static void open(Screen parent) {
        JournalClient.SESSION.clear();
        Minecraft.getInstance().setScreen(new JournalScreen(parent));
        JournalClient.request(0);
    }
    public static Component text(String key, Object... args) { return Component.translatable("journal.ioe." + key, args); }
    @Override protected void init() {
        int left = width / 2 - 150;
        previous = addRenderableWidget(Button.builder(text("previous"), b -> navigate(-1)).bounds(left, height - 28, 70, 20).build());
        next = addRenderableWidget(Button.builder(text("next"), b -> navigate(1)).bounds(left + 75, height - 28, 70, 20).build());
        addRenderableWidget(Button.builder(text("refresh"), b -> {
            int offset = JournalClient.SESSION.page().map(DiscoveryPage::offset).orElse(0);
            JournalClient.request(offset); refresh();
        }).bounds(left + 150, height - 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose()).bounds(left + 225, height - 28, 75, 20).build());
        refresh();
    }
    private void navigate(int delta) {
        JournalClient.SESSION.page().ifPresent(page -> JournalClient.request(page.offset() + delta));
        refresh();
    }
    public void refresh() {
        if (font == null || previous == null) return;
        scroll = 0;
        var page = JournalClient.SESSION.page();
        previous.active = page.isPresent() && page.get().offset() > 0;
        next.active = page.isPresent() && page.get().offset() + 1 < page.get().total();
        List<Component> text = new ArrayList<>();
        text.add(text("stages"));
        for (var stage : DiscoveryStage.values()) text.add(text("stage." + stage.name().toLowerCase(Locale.ROOT)));
        text.add(Component.empty());
        if (page.isEmpty()) text.add(text("loading"));
        else if (page.get().total() == 0) text.add(text("empty"));
        else {
            var p = page.get(); var v = p.entry().orElseThrow();
            text.add(text("page", p.offset() + 1, p.total()));
            text.add(text("current", text("stage." + v.stage().name().toLowerCase(Locale.ROOT))));
            text.add(text("dimension", v.dimension().toString()));
            text.add(text("clue_type", v.clueType().toString()));
            text.add(text("clue_location", v.clueLocation().toShortString()));
            v.siteLocation().ifPresent(pos -> text.add(text("site_location", pos.toShortString())));
            v.resource().ifPresent(id -> text.add(text("resource", id.toString())));
            v.quality().ifPresent(q -> text.add(text("quality", Component.translatable("budding.ioe.quality." + q.name().toLowerCase(Locale.ROOT)))));
        }
        lines = text.stream().flatMap(line -> font.split(line, Math.max(80, width - 40)).stream()).toList();
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.enableScissor(18, 28, width - 18, height - 38);
        int y = 30 - scroll;
        for (var line : lines) { graphics.drawString(font, line, 20, y, 0xFFFFFF); y += 12; }
        graphics.disableScissor();
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        scroll = Math.clamp(scroll - (int)(vertical * 24), 0, Math.max(0, lines.size() * 12 - (height - 68)));
        return true;
    }
    @Override public void onClose() { JournalClient.SESSION.clear(); minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
