package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

final class ExpeditionCompassItemTest {
    @Test
    void resetUseKeepsResetMessageKey() {
        Component message = ExpeditionCompassItem.messageForUse(
                Level.OVERWORLD,
                BlockPos.ZERO,
                true,
                new ExpeditionLocatorIndex()
        );

        assertEquals(ExpeditionCompassItem.RESET_KEY, translatableKey(message));
    }

    @Test
    void normalUseWithoutIndexedSitesUsesNoResultMessageKey() {
        Component message = ExpeditionCompassItem.messageForUse(
                Level.OVERWORLD,
                BlockPos.ZERO,
                false,
                new ExpeditionLocatorIndex()
        );

        assertEquals(ExpeditionCompassItem.NO_INDEXED_SITES_KEY, translatableKey(message));
    }

    @Test
    void normalUseWithIndexedSiteUsesNearestSiteMessageKey() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        index.record(ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(8, 64, 0),
                ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "tiny_vertical_mine_entrance"),
                null,
                SiteQuality.NORMAL,
                "test"
        ));

        Component message = ExpeditionCompassItem.messageForUse(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                false,
                index
        );

        assertEquals(ExpeditionCompassItem.NEAREST_SITE_KEY, translatableKey(message));
    }

    private static String translatableKey(Component component) {
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        return contents.getKey();
    }
}
