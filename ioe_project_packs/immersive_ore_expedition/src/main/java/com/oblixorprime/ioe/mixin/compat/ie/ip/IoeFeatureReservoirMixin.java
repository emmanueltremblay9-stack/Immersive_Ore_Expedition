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

/** Limits native IP scans to the three explicit, mutually exclusive biome reservoir groups. */
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
        if (!IoePetroleumReservoirRules.allowsNativeScan(level, chunkPos)) {
            IoePetroleumReservoirRules.recordNativeScanSuppressed(level, chunkPos);
            callback.cancel();
        }
    }
}
