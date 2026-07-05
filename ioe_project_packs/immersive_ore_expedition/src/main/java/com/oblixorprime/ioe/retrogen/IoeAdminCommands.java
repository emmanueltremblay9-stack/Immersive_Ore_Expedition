package com.oblixorprime.ioe.retrogen;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class IoeAdminCommands {
    private static Supplier<RetrogenController> controllerFactory = RetrogenController::createDefault;
    private static RetrogenController controller;

    private IoeAdminCommands() {
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(buildRootCommand(IoeAdminCommandSettings.fromConfig()));
    }

    static LiteralArgumentBuilder<CommandSourceStack> buildRootCommand(IoeAdminCommandSettings settings) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ioe")
                .requires(IoeAdminCommands::canUseAdminCommands);

        if (settings.anyLocateCommandEnabled()) {
            LiteralArgumentBuilder<CommandSourceStack> locate = Commands.literal("locate");
            if (settings.locateProvinceEnabled()) {
                locate.then(Commands.literal("province")
                        .executes(context -> send(context, "IOE province diagnostics are available; runtime province index binding is pending.")));
            }
            if (settings.locateAnchorEnabled()) {
                locate.then(Commands.literal("anchor")
                        .executes(context -> send(context, "IOE anchor diagnostics are available; runtime anchor index binding is pending.")));
            }
            root.then(locate);
        }

        if (settings.anyRetrogenCommandEnabled()) {
            LiteralArgumentBuilder<CommandSourceStack> retrogen = Commands.literal("retrogen");
            if (settings.retrogenStatusEnabled()) {
                retrogen.then(Commands.literal("status")
                        .executes(IoeAdminCommands::status));
            }
            if (settings.retrogenPauseEnabled()) {
                retrogen.then(Commands.literal("pause")
                        .executes(IoeAdminCommands::pause));
            }
            if (settings.adminRadiusStartEnabled()) {
                retrogen.then(Commands.literal("radius")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(0, RetrogenController.MAX_ADMIN_RADIUS_BLOCKS))
                                .executes(context -> startRadius(context, IntegerArgumentType.getInteger(context, "blocks")))));
                retrogen.then(Commands.literal("start")
                        .then(Commands.literal("radius")
                                .then(Commands.argument("blocks", IntegerArgumentType.integer(0, RetrogenController.MAX_ADMIN_RADIUS_BLOCKS))
                                        .executes(context -> startRadius(context, IntegerArgumentType.getInteger(context, "blocks"))))));
            }
            root.then(retrogen);
        }

        return root;
    }

    static synchronized RetrogenController controller() {
        if (controller == null) {
            controller = Objects.requireNonNull(controllerFactory.get(), "controller");
        }
        return controller;
    }

    static synchronized void resetControllerForTesting(Supplier<RetrogenController> factory) {
        controllerFactory = Objects.requireNonNull(factory, "factory");
        controller = null;
    }

    private static boolean canUseAdminCommands(CommandSourceStack source) {
        return !IoeRetrogenAdminConfig.requireAdminCommand() || source.hasPermission(2);
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        RetrogenController retrogenController = controller();
        RetrogenStatus status = retrogenController.status();
        PersistentRetrogenState.StatusSnapshot persistentStatus = retrogenController.persistentStatus();
        return send(context, "IOE retrogen mode=" + status.mode().configValue()
                + ", queued=" + status.queuedChunks()
                + ", paused=" + status.paused()
                + ", markerVersion=" + status.markerVersion()
                + ", maxChunksPerTick=" + status.maxChunksPerTick()
                + ", persistentState=ready"
                + ", persistentProcessed=" + persistentStatus.processedChunks()
                + ", persistentFailed=" + persistentStatus.failedChunks()
                + ", persistentSkipped=" + persistentStatus.skippedChunks());
    }

    private static int pause(CommandContext<CommandSourceStack> context) {
        controller().pause();
        return send(context, "IOE retrogen queue paused.");
    }

    private static int startRadius(CommandContext<CommandSourceStack> context, int radiusBlocks) {
        Vec3 position = context.getSource().getPosition();
        int centerChunkX = Mth.floor(position.x) >> 4;
        int centerChunkZ = Mth.floor(position.z) >> 4;
        List<RetrogenChunkSnapshot> candidates = RetrogenController.placeholderRadiusCandidates(centerChunkX, centerChunkZ, radiusBlocks);
        RetrogenMode mode = resolveCommandMode(
                IoeRetrogenAdminConfig.enabled(),
                IoeRetrogenAdminConfig.defaultMode(),
                IoeRetrogenAdminConfig::modeAllowed
        );
        RetrogenStartResult result = controller().startAdminRadiusRetrogen(centerChunkX, centerChunkZ, radiusBlocks, mode, candidates);
        return send(context, "IOE retrogen radius request: " + result.reason()
                + " accepted=" + result.acceptedChunks()
                + " skippedMarked=" + result.skippedAlreadyMarked()
                + " skippedExplored=" + result.skippedExplored()
                + " skippedOutOfRadius=" + result.skippedOutOfRadius()
                + " skippedInvalid=" + result.skippedInvalidCandidates());
    }

    static RetrogenMode resolveCommandMode(
            boolean retrogenEnabled,
            RetrogenMode configuredDefaultMode,
            Predicate<RetrogenMode> modeAllowed
    ) {
        Objects.requireNonNull(configuredDefaultMode, "configuredDefaultMode");
        Objects.requireNonNull(modeAllowed, "modeAllowed");
        if (!retrogenEnabled || configuredDefaultMode == RetrogenMode.OFF) {
            return RetrogenMode.OFF;
        }
        RetrogenMode effectiveMode = configuredDefaultMode == RetrogenMode.UNEXPLORED_CHUNKS_ONLY
                ? RetrogenMode.ADMIN_RADIUS
                : configuredDefaultMode;
        return modeAllowed.test(effectiveMode) ? effectiveMode : RetrogenMode.OFF;
    }

    private static int send(CommandContext<CommandSourceStack> context, String message) {
        Supplier<Component> text = () -> Component.literal(message);
        context.getSource().sendSuccess(text, false);
        return 1;
    }
}
