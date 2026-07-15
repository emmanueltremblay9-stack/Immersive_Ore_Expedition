package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.worldgen.IoeWorldgenConfig;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class IoeCompassNetworking {
    private static final String NETWORK_VERSION = "2";
    private static final String CLIENT_REGISTRATION_CLASS =
            "com.oblixorprime.ioe.expeditioncompass.client.ExpeditionCompassClient";

    private IoeCompassNetworking() {
    }

    static void register(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        modEventBus.addListener(IoeCompassNetworking::registerPayloadHandlers);
    }

    public static void sendMenuSnapshot(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack stack,
            ExpeditionLocatorIndex locatorIndex
    ) {
        ExpeditionCompassMenuSnapshot snapshot = snapshotFor(player, hand, stack, locatorIndex);
        if (ExpeditionCompassItem.target(stack).isPresent() && snapshot.currentTarget().isEmpty()) {
            ExpeditionCompassItem.clearTarget(stack);
        }
        IoeExpeditionCompassMod.LOGGER.debug(
                "Sending expedition compass menu snapshot to {} hand={} entries={} currentTarget={}",
                player.getScoreboardName(),
                hand,
                snapshot.entries().size(),
                snapshot.currentTarget().isPresent()
        );
        PacketDistributor.sendToPlayer(
                player,
                new ClientboundExpeditionCompassMenuPayload(snapshot)
        );
    }

    static ExpeditionCompassMenuSnapshot snapshotFor(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack stack,
            ExpeditionLocatorIndex locatorIndex
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(stack, "stack");
        return ExpeditionCompassMenuSnapshot.fromIndex(
                player.level().dimension(),
                player.blockPosition(),
                Objects.requireNonNull(hand, "hand"),
                ExpeditionCompassItem.target(stack),
                Objects.requireNonNull(locatorIndex, "locatorIndex"),
                IoeWorldgenConfig.compassShowDiagnosticSites(),
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    static Optional<ExpeditionCompassMenuEntry> validateSelection(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ExpeditionCompassTarget requestedTarget,
            ExpeditionLocatorIndex locatorIndex
    ) {
        ExpeditionCompassMenuSnapshot snapshot = ExpeditionCompassMenuSnapshot.fromIndex(
                dimension,
                origin,
                InteractionHand.MAIN_HAND,
                Optional.empty(),
                locatorIndex
        );
        return snapshot.matchingEntry(requestedTarget);
    }

    static void openLocalMenuSnapshot(
            ResourceKey<Level> dimension,
            BlockPos origin,
            InteractionHand hand,
            Optional<ExpeditionCompassTarget> currentTarget
    ) {
        Objects.requireNonNull(origin, "origin");
        openClientMenu(new ExpeditionCompassMenuSnapshot(
                Objects.requireNonNull(dimension, "dimension"),
                Objects.requireNonNull(hand, "hand"),
                Objects.requireNonNull(currentTarget, "currentTarget"),
                ExpeditionCompassEmptyReason.NO_PLACED_SITES,
                List.of()
        ));
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(
                ClientboundExpeditionCompassMenuPayload.TYPE,
                ClientboundExpeditionCompassMenuPayload.STREAM_CODEC,
                IoeCompassNetworking::handleMenuSnapshot
        );
        registrar.playToServer(
                ServerboundExpeditionCompassSelectPayload.TYPE,
                ServerboundExpeditionCompassSelectPayload.STREAM_CODEC,
                IoeCompassNetworking::handleSelect
        );
        registrar.playToServer(
                ServerboundExpeditionCompassClearPayload.TYPE,
                ServerboundExpeditionCompassClearPayload.STREAM_CODEC,
                IoeCompassNetworking::handleClear
        );
        registrar.playToServer(
                ServerboundExpeditionCompassRefreshPayload.TYPE,
                ServerboundExpeditionCompassRefreshPayload.STREAM_CODEC,
                IoeCompassNetworking::handleRefresh
        );
        IoeExpeditionCompassMod.LOGGER.debug("Registered expedition compass menu payload handlers");
    }

    private static void handleMenuSnapshot(
            ClientboundExpeditionCompassMenuPayload payload,
            IPayloadContext context
    ) {
        openClientMenu(payload.snapshot());
    }

    private static void handleSelect(
            ServerboundExpeditionCompassSelectPayload payload,
            IPayloadContext context
    ) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Optional<ItemStack> stack = compassStack(serverPlayer, payload.hand());
        Optional<ExpeditionCompassMenuEntry> selection = validateSelection(
                serverPlayer.level().dimension(),
                serverPlayer.blockPosition(),
                payload.target(),
                compassIndexFor(serverPlayer)
        );
        if (stack.isEmpty() || selection.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable(ExpeditionCompassItem.INVALID_TARGET_KEY), true);
            stack.ifPresent(itemStack -> sendMenuSnapshot(
                    serverPlayer,
                    payload.hand(),
                    itemStack,
                    compassIndexFor(serverPlayer)
            ));
            return;
        }

        ExpeditionCompassMenuEntry selected = selection.orElseThrow();
        ExpeditionCompassItem.setTarget(stack.orElseThrow(), selected.target());
        serverPlayer.displayClientMessage(
                ExpeditionCompassItem.messageForTarget(
                        ExpeditionCompassItem.TARGET_BOUND_KEY,
                        selected.target(),
                        selected.distanceBlocks()
                ),
                true
        );
        sendMenuSnapshot(serverPlayer, payload.hand(), stack.orElseThrow(), compassIndexFor(serverPlayer));
    }

    private static void handleClear(
            ServerboundExpeditionCompassClearPayload payload,
            IPayloadContext context
    ) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        compassStack(serverPlayer, payload.hand()).ifPresent(stack -> {
            ExpeditionCompassItem.clearTarget(stack);
            serverPlayer.displayClientMessage(Component.translatable(ExpeditionCompassItem.RESET_KEY), true);
            sendMenuSnapshot(serverPlayer, payload.hand(), stack, compassIndexFor(serverPlayer));
        });
    }

    private static void handleRefresh(
            ServerboundExpeditionCompassRefreshPayload payload,
            IPayloadContext context
    ) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        compassStack(serverPlayer, payload.hand()).ifPresent(stack ->
                sendMenuSnapshot(serverPlayer, payload.hand(), stack, compassIndexFor(serverPlayer))
        );
    }

    private static ExpeditionLocatorIndex compassIndexFor(ServerPlayer player) {
        return ExpeditionLocatorService.compassIndex(player.serverLevel(), player.blockPosition());
    }

    private static Optional<ItemStack> compassStack(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ExpeditionCompassItem) {
            return Optional.of(stack);
        }
        return Optional.empty();
    }

    private static void openClientMenu(ExpeditionCompassMenuSnapshot snapshot) {
        IoeExpeditionCompassMod.LOGGER.debug(
                "Opening expedition compass client menu hand={} entries={} currentTarget={}",
                snapshot.hand(),
                snapshot.entries().size(),
                snapshot.currentTarget().isPresent()
        );
        try {
            Class.forName(CLIENT_REGISTRATION_CLASS)
                    .getMethod("openMenu", ExpeditionCompassMenuSnapshot.class)
                    .invoke(null, snapshot);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Expedition compass client menu bridge is unavailable", exception);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Expedition compass client menu bridge is invalid", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Expedition compass client menu bridge failed", exception.getCause());
        }
    }
}
