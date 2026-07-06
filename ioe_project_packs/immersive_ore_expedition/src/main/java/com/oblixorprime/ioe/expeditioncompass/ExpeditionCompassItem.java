package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorResult;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ExpeditionCompassItem extends Item {
    static final String TOOLTIP_KEY = "item.immersive_ore_expedition.expedition_compass.tooltip";
    static final String NEAREST_SITE_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.nearest_site";
    static final String NO_INDEXED_SITES_KEY =
            "item.immersive_ore_expedition.expedition_compass.message.no_indexed_sites";
    static final String RESET_KEY = "item.immersive_ore_expedition.expedition_compass.message.reset";

    public ExpeditionCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            player.displayClientMessage(
                    messageForUse(
                            level.dimension(),
                            player.blockPosition(),
                            player.isShiftKeyDown(),
                            ExpeditionLocatorService.index()
                    ),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    static Component messageForUse(
            ResourceKey<Level> dimension,
            BlockPos origin,
            boolean reset,
            ExpeditionLocatorIndex locatorIndex
    ) {
        if (reset) {
            return Component.translatable(RESET_KEY);
        }

        ExpeditionLocatorResult result = locatorIndex.nearestAny(dimension, origin);
        if (!result.found()) {
            return Component.translatable(NO_INDEXED_SITES_KEY);
        }

        ExpeditionSite site = result.site().orElseThrow();
        return Component.translatable(
                NEAREST_SITE_KEY,
                site.kind().messageLabel(),
                site.primaryId().map(Object::toString).orElse("unknown"),
                site.pos().getX(),
                site.pos().getY(),
                site.pos().getZ(),
                result.distanceBlocks().orElse(0L)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(TOOLTIP_KEY));
    }
}
