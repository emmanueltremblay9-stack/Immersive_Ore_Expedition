package com.oblixorprime.ioe.retrogen;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IoeAdminCommandsTest {
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
}
