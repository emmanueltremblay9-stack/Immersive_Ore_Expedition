package com.oblixorprime.ioe.retrogen;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSiteKind;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IoeAdminCommandsTest {
    @AfterEach
    void resetControllerFactory() {
        IoeAdminCommands.resetControllerForTesting(RetrogenController::createDefault);
    }

    @Test
    void allConfiguredCommandsAreRegisteredWhenEnabled() {
        LiteralCommandNode<CommandSourceStack> root = IoeAdminCommands.buildRootCommand(
                new IoeAdminCommandSettings(true, true, true, true, true, true)
        ).build();

        assertNotNull(root.getChild("locate").getChild("province"));
        assertNotNull(root.getChild("locate").getChild("anchor"));
        assertNotNull(root.getChild("retrogen").getChild("status"));
        assertNotNull(root.getChild("retrogen").getChild("pause"));
        assertNotNull(root.getChild("retrogen").getChild("radius"));
        assertNotNull(root.getChild("retrogen").getChild("start").getChild("radius"));
    }

    @Test
    void disabledConfiguredCommandsAreNotRegistered() {
        LiteralCommandNode<CommandSourceStack> root = IoeAdminCommands.buildRootCommand(
                new IoeAdminCommandSettings(false, true, false, false, true, true)
        ).build();

        assertNotNull(root.getChild("locate"));
        assertNull(root.getChild("locate").getChild("province"));
        assertNotNull(root.getChild("locate").getChild("anchor"));
        assertNotNull(root.getChild("retrogen"));
        assertNull(root.getChild("retrogen").getChild("status"));
        assertNotNull(root.getChild("retrogen").getChild("pause"));
        assertNull(root.getChild("retrogen").getChild("radius"));
        assertNull(root.getChild("retrogen").getChild("start"));
    }

    @Test
    void disabledAdminRadiusModeRemovesStartAndRadiusCommands() {
        LiteralCommandNode<CommandSourceStack> root = IoeAdminCommands.buildRootCommand(
                new IoeAdminCommandSettings(false, false, true, true, false, false)
        ).build();

        assertNull(root.getChild("locate"));
        assertNotNull(root.getChild("retrogen").getChild("status"));
        assertNull(root.getChild("retrogen").getChild("radius"));
        assertNull(root.getChild("retrogen").getChild("start"));
    }

    @Test
    void allCommandsDisabledLeavesOnlyRootNode() {
        LiteralCommandNode<CommandSourceStack> root = IoeAdminCommands.buildRootCommand(
                new IoeAdminCommandSettings(false, false, false, false, false, false)
        ).build();

        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void locateProvinceNoResultDoesNotUsePendingBindingPlaceholder() {
        String message = IoeAdminCommands.locateMessage(
                ExpeditionSiteKind.PROVINCE,
                Level.OVERWORLD,
                BlockPos.ZERO,
                new ExpeditionLocatorIndex()
        );

        assertTrue(message.contains("no indexed province sites"));
        assertFalse(message.contains("runtime province index binding is pending"));
    }

    @Test
    void locateAnchorReportsNearestIndexedAnchorInsteadOfPendingBindingPlaceholder() {
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        index.record(ExpeditionSite.anchor(
                Level.OVERWORLD,
                new BlockPos(12, 64, 4),
                ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "tiny_vertical_mine_entrance"),
                null,
                SiteQuality.NORMAL,
                "test"
        ));

        String message = IoeAdminCommands.locateMessage(
                ExpeditionSiteKind.ANCHOR,
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                index
        );

        assertTrue(message.contains("nearest indexed anchor"));
        assertTrue(message.contains("12 64 4"));
        assertFalse(message.contains("runtime anchor index binding is pending"));
    }

    @Test
    void commandModeHonorsGlobalAndPerModeConfigGates() {
        assertEquals(RetrogenMode.OFF, IoeAdminCommands.resolveCommandMode(
                false,
                RetrogenMode.ADMIN_RADIUS,
                mode -> true
        ));
        assertEquals(RetrogenMode.OFF, IoeAdminCommands.resolveCommandMode(
                true,
                RetrogenMode.ORE_POCKET_ONLY,
                mode -> mode != RetrogenMode.ORE_POCKET_ONLY
        ));
        assertEquals(RetrogenMode.ADMIN_RADIUS, IoeAdminCommands.resolveCommandMode(
                true,
                RetrogenMode.UNEXPLORED_CHUNKS_ONLY,
                mode -> mode == RetrogenMode.ADMIN_RADIUS
        ));
        assertEquals(RetrogenMode.OFF, IoeAdminCommands.resolveCommandMode(
                true,
                RetrogenMode.UNEXPLORED_CHUNKS_ONLY,
                mode -> false
        ));
    }

    @Test
    void controllerIsCreatedLazilyFromCurrentFactory() {
        AtomicInteger created = new AtomicInteger();
        IoeAdminCommands.resetControllerForTesting(() -> {
            created.incrementAndGet();
            return new RetrogenController(7, 3);
        });

        assertEquals(0, created.get());

        RetrogenController controller = IoeAdminCommands.controller();

        assertEquals(1, created.get());
        assertEquals(7, controller.status().markerVersion());
        assertEquals(3, controller.status().maxChunksPerTick());
        assertSame(controller, IoeAdminCommands.controller());
        assertEquals(1, created.get());
    }

    @Test
    void controllerFactoryCanProvidePersistenceReadyControllerWithoutChangingCommandTree() {
        InMemoryRetrogenStateStore store = new InMemoryRetrogenStateStore(4, 2);
        IoeAdminCommands.resetControllerForTesting(() -> new RetrogenController(4, 2, store));

        RetrogenController controller = IoeAdminCommands.controller();

        assertEquals(4, controller.status().markerVersion());
        assertEquals(2, controller.status().maxChunksPerTick());
        assertEquals(4, controller.persistentStatus().markerVersion());
        assertEquals(2, controller.persistentStatus().maxChunksPerTick());
    }
}
