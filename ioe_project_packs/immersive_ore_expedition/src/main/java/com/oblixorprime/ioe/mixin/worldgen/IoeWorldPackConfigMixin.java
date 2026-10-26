package com.oblixorprime.ioe.mixin.worldgen;

import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.oblixorprime.ioe.nethergeodes.NetherWorldActivation;

@Mixin(WorldLoader.PackConfig.class)
abstract class IoeWorldPackConfigMixin {
    @Shadow @Final private PackRepository packRepository;
    @Shadow @Final private WorldDataConfiguration initialDataConfig;
    @Shadow @Final private boolean safeMode;
    @Shadow @Final private boolean initMode;
    @Inject(method="createResourceManager",at=@At("HEAD"))
    private void ioe$selectBeforeRegistries(CallbackInfoReturnable<?> callback) {
        NetherWorldActivation.beforeRegistries(packRepository,initialDataConfig,initMode,safeMode);
    }
    @Inject(method="createResourceManager",at=@At("RETURN"))
    private void ioe$verifyBeforeRegistries(CallbackInfoReturnable<?> callback) {
        NetherWorldActivation.afterPackSelection(packRepository);
    }
}
