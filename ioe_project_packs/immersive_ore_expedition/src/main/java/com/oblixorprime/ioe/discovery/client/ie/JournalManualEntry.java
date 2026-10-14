package com.oblixorprime.ioe.discovery.client.ie;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.oblixorprime.ioe.discovery.client.JournalScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** Loaded only after the client-side IE presence gate. Original functional IOE content. */
public final class JournalManualEntry {
    private static final ResourceLocation ID = ResourceLocation.parse("immersive_ore_expedition:journal");
    private JournalManualEntry() { }
    public static void register() {
        var manual = ManualHelper.getManual();
        if (manual == null) throw new IllegalStateException("IE manual not initialized");
        if (manual.getAllEntries().anyMatch(entry -> ID.equals(entry.getLocation()))) return;
        var category = manual.getRoot().getOrCreateSubnode(ResourceLocation.parse("immersive_ore_expedition:field_records"));
        var builder = new ManualEntry.ManualEntryBuilder(manual);
        builder.setLocation(ID);
        builder.setContent(() -> JournalScreen.text("title").getString(), () -> "",
                () -> JournalScreen.text("manual_description").getString() + "<br><br><&open>");
        builder.addSpecialElement(new ManualEntry.SpecialElementData("open", 0, new OpenJournal()));
        manual.addEntry(category, builder.create());
    }
    private static final class OpenJournal extends SpecialManualElement {
        @Override public int getPixelsTaken() { return 24; }
        @Override public void onOpened(ManualScreen screen, int x, int y, List<Button> buttons) {
            buttons.add(Button.builder(JournalScreen.text("open"), b -> JournalScreen.open(screen)).bounds(x, y, 110, 20).build());
        }
        @Override public void render(GuiGraphics graphics, ManualScreen screen, int x, int y, int mouseX, int mouseY) { }
        @Override public void mouseDragged(int x, int y, double clickX, double clickY, double mx, double my, double lastX, double lastY, int button) { }
        @Override public boolean listForSearch(String query) { return false; }
        @Override public void recalculateCraftingRecipes() { }
    }
}
