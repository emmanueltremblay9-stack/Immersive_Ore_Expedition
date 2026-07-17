package com.oblixorprime.ioe.mixin.compat.ie.ip;

import com.oblixorprime.ioe.compat.ip.IoePetroleumReservoirAuthorization;
import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirRules;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Allows an exact IOE transaction or a native reservoir whose center and fluid match an explicit biome group. */
@Pseudo
@Mixin(
        targets = "flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage",
        remap = false
)
abstract class IoeReservoirStorageMixin {
    @Inject(method = "addReservoir", at = @At("HEAD"), cancellable = true)
    private void ioe$guardReservoirRegistration(
            ResourceKey<Level> dimension,
            Reservoir reservoir,
            CallbackInfo callback
    ) {
        if (IoePetroleumReservoirAuthorization.permits(dimension, reservoir)) {
            return;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer() == null
                ? null
                : ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
        if (level != null && IoePetroleumReservoirRules.allowsNativeRegistration(
                level,
                reservoir.getBoundingBox().getCenter(),
                BuiltInRegistries.FLUID.getKey(reservoir.getFluid())
        )) {
            return;
        }
        IoePetroleumReservoirRules.recordUnauthorizedRegistrationBlocked();
        callback.cancel();
    }
}
