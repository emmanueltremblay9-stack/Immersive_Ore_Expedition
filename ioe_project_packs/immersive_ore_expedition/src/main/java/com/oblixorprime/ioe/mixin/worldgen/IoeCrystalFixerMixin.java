package com.oblixorprime.ioe.mixin.worldgen;

import com.oblixorprime.ioe.budding.FlawlessFixerPayment;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.common.tileentities.TileCrystalFixer", remap = false)
abstract class IoeCrystalFixerMixin {
    @Inject(method = "tickingRequest", at = @At("HEAD"), cancellable = true)
    private void ioe$payFlawless(@Coerce Object node, int elapsed, CallbackInfoReturnable<Object> callback) {
        Object result = FlawlessFixerPayment.tick((BlockEntity) (Object) this);
        if (result != null) callback.setReturnValue(result);
    }
}
