package com.oblixorprime.ioe.mixin.compat.ie;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import com.oblixorprime.ioe.worldgen.IoeExcavatorDepositRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.api.excavator.ExcavatorHandler", remap = false)
abstract class IoeExcavatorHandlerMixin {
    @Redirect(
            method = "generatePotentialVein",
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/api/excavator/ExcavatorHandler;addVein("
                            + "Lnet/minecraft/resources/ResourceKey;"
                            + "Lblusunrize/immersiveengineering/api/excavator/MineralVein;)V"
            )
    )
    private static void ioe$registerSpatiallyAdmissibleVein(
            ResourceKey<Level> dimension,
            MineralVein vein,
            Level level,
            WorldGenLevel worldGenLevel,
            ChunkPos chunkPos,
            RandomSource random
    ) {
        if (level instanceof ServerLevel serverLevel
                && dimension.equals(serverLevel.dimension())
                && IoeExcavatorDepositRules.allowNativeCandidate(
                serverLevel,
                vein.getPos(),
                vein.getMineralName()
        )) {
            ExcavatorHandler.addVein(dimension, vein);
        }
    }
}
