package com.oblixorprime.ioe.retrogen;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorResult;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSiteKind;
import com.oblixorprime.ioe.worldgen.IoeRuntimeScaffoldStatus;
import com.oblixorprime.ioe.worldgen.IoeRuntimeScaffoldStatusFormatter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
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

        root.then(Commands.literal("status")
                .executes(IoeAdminCommands::runtimeStatus));

        if (settings.anyLocateCommandEnabled()) {
            LiteralArgumentBuilder<CommandSourceStack> locate = Commands.literal("locate");
            if (settings.locateProvinceEnabled()) {
                locate.then(Commands.literal("province")
                        .executes(context -> locate(context, ExpeditionSiteKind.PROVINCE)));
            }
            if (settings.locateAnchorEnabled()) {
                locate.then(Commands.literal("anchor")
                        .executes(context -> locate(context, ExpeditionSiteKind.ANCHOR)));
            }
            root.then(locate);
        }

        if (settings.anyRetrogenCommandEnabled()) {
            LiteralArgumentBuilder<CommandSourceStack> retrogen = Commands.literal("retrogen");
            if (settings.retrogenStatusEnabled()) {
                retrogen.then(Commands.literal("status")
                        .executes(IoeAdminCommands::retrogenStatus));
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

    private static int runtimeStatus(CommandContext<CommandSourceStack> context) {
        return sendAll(context, runtimeStatusMessages(IoeRuntimeScaffoldStatus.fromConfig(
                resolveModVersion(),
                true
        )));
    }

    static List<String> runtimeStatusMessages(IoeRuntimeScaffoldStatus status) {
        return IoeRuntimeScaffoldStatusFormatter.format(status);
    }

    private static String resolveModVersion() {
        try {
            return ModList.get()
                    .getModContainerById(ImmersiveOreExpeditionMod.MODID)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse("unknown");
        } catch (RuntimeException exception) {
            return "unknown";
        }
    }

    private static int retrogenStatus(CommandContext<CommandSourceStack> context) {
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

    private static int locate(CommandContext<CommandSourceStack> context, ExpeditionSiteKind kind) {
        CommandSourceStack source = context.getSource();
        Vec3 position = source.getPosition();
        BlockPos origin = new BlockPos(Mth.floor(position.x), Mth.floor(position.y), Mth.floor(position.z));
        return send(context, locateMessage(
                kind,
                source.getLevel().dimension(),
                origin,
                ExpeditionLocatorService.index()
        ));
    }

    static String locateMessage(
            ExpeditionSiteKind kind,
            ResourceKey<Level> dimension,
            BlockPos origin,
            ExpeditionLocatorIndex locatorIndex
    ) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(locatorIndex, "locatorIndex");

        ExpeditionLocatorResult result = locatorIndex.nearest(dimension, origin, kind);
        String label = kind.messageLabel();
        if (!result.found()) {
            return "IOE locate " + label + ": no indexed " + label + " sites in "
                    + dimension.location() + " yet.";
        }

        ExpeditionSite site = result.site().orElseThrow();
        return "IOE locate " + label + ": nearest indexed " + label
                + " " + site.primaryId().map(Object::toString).orElse("unknown")
                + " at " + site.pos().getX() + " " + site.pos().getY() + " " + site.pos().getZ()
                + " in " + site.dimension().location()
                + ", distance=" + result.distanceBlocks().orElse(0L) + " blocks"
                + ", quality=" + site.quality().map(Enum::name).orElse("unknown")
                + ", source=" + site.source().orElse("unknown") + ".";
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

    private static int sendAll(CommandContext<CommandSourceStack> context, List<String> messages) {
        Objects.requireNonNull(messages, "messages");
        for (String message : messages) {
            send(context, message);
        }
        return messages.isEmpty() ? 0 : 1;
    }

    private static int send(CommandContext<CommandSourceStack> context, String message) {
        Supplier<Component> text = () -> Component.literal(message);
        context.getSource().sendSuccess(text, false);
        return 1;
    }
}
