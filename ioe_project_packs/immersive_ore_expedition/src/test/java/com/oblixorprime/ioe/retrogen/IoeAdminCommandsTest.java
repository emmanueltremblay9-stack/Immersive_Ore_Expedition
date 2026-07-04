package com.oblixorprime.ioe.retrogen;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oblixorprime.ioe.worldgen.IoeRuntimeScaffoldStatus;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import com.oblixorprime.ioe.worldgen.IoeWorldgenRegistration;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertNotNull(root.getChild("status"));
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

        assertNotNull(root.getChild("status"));
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

        assertNotNull(root.getChild("status"));
        assertNull(root.getChild("locate"));
        assertNotNull(root.getChild("retrogen").getChild("status"));
        assertNull(root.getChild("retrogen").getChild("radius"));
        assertNull(root.getChild("retrogen").getChild("start"));
    }

    @Test
    void allConfigGatedCommandsDisabledLeavesStatusProofOnly() {
        LiteralCommandNode<CommandSourceStack> root = IoeAdminCommands.buildRootCommand(
                new IoeAdminCommandSettings(false, false, false, false, false, false)
        ).build();

        assertEquals(1, root.getChildren().size());
        assertNotNull(root.getChild("status"));
    }

    @Test
    void statusMessagesExplainPlanningOnlyRuntimeVisibility() {
        IoeWorldgenRegistration registration = IoeWorldgenRegistration.scaffold(
                List.of(),
                IoeWorldgenPlacementGates.disabled()
        );
        IoeRuntimeScaffoldStatus status = IoeRuntimeScaffoldStatus.fromRegistration(
                "test",
                registration,
                true
        );

        String output = String.join("\n", IoeAdminCommands.runtimeStatusMessages(status));

        assertTrue(output.contains("runtimeWorldgenEnabled=false"));
        assertTrue(output.contains("provinceRuntimeIntegrationEnabled=false"));
        assertTrue(output.contains("planning-only"));
        assertTrue(output.contains("No visible world or JourneyMap changes are expected"));
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
}
