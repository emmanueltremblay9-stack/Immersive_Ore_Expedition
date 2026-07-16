package com.oblixorprime.ioe.mixin.compat.ie.ip;

import com.oblixorprime.ioe.compat.ip.IoePetroleumReservoirAuthorization;
import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirRules;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rejects every IP reservoir registration not authorized by a confirmed IOE site transaction. */
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
        if (!IoePetroleumReservoirAuthorization.permits(dimension, reservoir)) {
            IoePetroleumReservoirRules.recordUnauthorizedRegistrationBlocked();
            callback.cancel();
        }
    }
}
