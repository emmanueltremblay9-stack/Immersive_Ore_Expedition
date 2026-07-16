package com.oblixorprime.ioe.mixin.compat.ie.ip;

import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stops IP's expensive free reservoir scan; the storage mixin remains the authoritative admission gate. */
@Pseudo
@Mixin(targets = "flaxbeard.immersivepetroleum.common.world.FeatureReservoir", remap = false)
abstract class IoeFeatureReservoirMixin {
    @Inject(method = "scanChunkForNewReservoirs", at = @At("HEAD"), cancellable = true)
    private static void ioe$suppressFreeReservoirScan(
            ServerLevel level,
            ChunkPos chunkPos,
            RandomSource random,
            CallbackInfo callback
    ) {
        IoePetroleumReservoirRules.recordNativeScanSuppressed(level, chunkPos);
        callback.cancel();
    }
}
