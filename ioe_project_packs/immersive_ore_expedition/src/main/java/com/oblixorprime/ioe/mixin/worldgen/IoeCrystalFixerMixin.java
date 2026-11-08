package com.oblixorprime.ioe.mixin.worldgen;

import com.oblixorprime.ioe.budding.FlawlessFixerPayment;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.common.tileentities.TileCrystalFixer", remap = false)
abstract class IoeCrystalFixerMixin {
    // Persist the IOE ledger explicitly alongside the native machine's saved state.
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void ioe$savePayment(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        FlawlessFixerPayment.save((BlockEntity) (Object) this, tag);
    }
    @Inject(method = "loadTag", at = @At("TAIL"))
    private void ioe$loadPayment(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        FlawlessFixerPayment.load((BlockEntity) (Object) this, tag);
    }
    @Inject(method = "tickingRequest", at = @At("HEAD"), cancellable = true)
    private void ioe$payFlawless(@Coerce Object node, int elapsed, CallbackInfoReturnable<Object> callback) {
        Object result = FlawlessFixerPayment.tick((BlockEntity) (Object) this);
        if (result != null) callback.setReturnValue(result);
    }
}
