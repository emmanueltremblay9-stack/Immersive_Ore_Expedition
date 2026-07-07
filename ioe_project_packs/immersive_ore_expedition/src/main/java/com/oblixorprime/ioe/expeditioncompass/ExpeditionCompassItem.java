package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ExpeditionCompassItem extends CompassItem {
    private static final int USE_COOLDOWN_TICKS = 20;

    static final String TOOLTIP_KEY = "item.immersive_ore_expedition.expedition_compass.tooltip";
    static final String TARGET_BOUND_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.target_bound";
    static final String CURRENT_TARGET_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.current_target";
    static final String TARGET_OTHER_DIMENSION_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.target_other_dimension";
    static final String INVALID_TARGET_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.invalid_target";
    static final String RESET_KEY = "item.immersive_ore_expedition.expedition_compass.message.reset";

    public ExpeditionCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            if (!player.isShiftKeyDown()) {
                IoeCompassNetworking.openLocalMenuSnapshot(
                        level.dimension(),
                        player.blockPosition(),
                        usedHand,
                        target(stack)
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!level.isClientSide()) {
            UseOutcome outcome = useOutcome(player.isShiftKeyDown());
            applyOutcome(stack, outcome);
            player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
            IoeExpeditionCompassMod.LOGGER.debug(
                    "Expedition compass right-click reached server for {} hand={} reset={} openMenu={}",
                    player.getScoreboardName(),
                    usedHand,
                    player.isShiftKeyDown(),
                    outcome.openMenu()
            );
            outcome.message().ifPresent(message -> player.displayClientMessage(message, true));
            if (outcome.openMenu() && player instanceof ServerPlayer serverPlayer) {
                IoeCompassNetworking.sendMenuSnapshot(
                        serverPlayer,
                        usedHand,
                        stack,
                        ExpeditionLocatorService.index()
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static UseOutcome useOutcome(boolean reset) {
        if (reset) {
            return UseOutcome.clear(Component.translatable(RESET_KEY));
        }
        return UseOutcome.requestMenu();
    }

    public static Optional<ExpeditionCompassTarget> target(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return Optional.ofNullable(stack.get(IoeCompassDataComponents.targetComponent()));
    }

    static void setTarget(ItemStack stack, ExpeditionCompassTarget target) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(target, "target");
        stack.set(IoeCompassDataComponents.targetComponent(), target);
        stack.set(DataComponents.LODESTONE_TRACKER, target.asUntrackedLodestoneTracker());
    }

    static void clearTarget(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        stack.remove(IoeCompassDataComponents.targetComponent());
        stack.remove(DataComponents.LODESTONE_TRACKER);
    }

    private static void applyOutcome(ItemStack stack, UseOutcome outcome) {
        if (outcome.clearTarget()) {
            clearTarget(stack);
        }
    }

    static Component messageForCurrentTarget(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ExpeditionCompassTarget target
    ) {
        if (!target.dimension().equals(dimension)) {
            return Component.translatable(
                    TARGET_OTHER_DIMENSION_KEY,
                    target.dimension().location().toString(),
                    target.kind().messageLabel(),
                    target.primaryId().map(Object::toString).orElse("unknown"),
                    target.pos().getX(),
                    target.pos().getY(),
                    target.pos().getZ()
            );
        }

        return messageForTarget(CURRENT_TARGET_KEY, target, target.distanceBlocksFrom(origin));
    }

    static Component messageForTarget(String key, ExpeditionCompassTarget target, long distanceBlocks) {
        return Component.translatable(
                key,
                target.kind().messageLabel(),
                target.primaryId().map(Object::toString).orElse("unknown"),
                target.pos().getX(),
                target.pos().getY(),
                target.pos().getZ(),
                distanceBlocks
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(TOOLTIP_KEY));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    record UseOutcome(
            Optional<Component> message,
            boolean clearTarget,
            boolean openMenu
    ) {
        UseOutcome {
            message = message == null ? Optional.empty() : message;
        }

        static UseOutcome requestMenu() {
            return new UseOutcome(Optional.empty(), false, true);
        }

        static UseOutcome clear(Component message) {
            return new UseOutcome(Optional.of(message), true, false);
        }
    }
}
