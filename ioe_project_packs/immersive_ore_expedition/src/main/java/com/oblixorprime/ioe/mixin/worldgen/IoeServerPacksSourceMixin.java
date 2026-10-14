package com.oblixorprime.ioe.mixin.worldgen;

import net.minecraft.server.packs.repository.*;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.oblixorprime.ioe.nethergeodes.NetherWorldActivation;

@Mixin(ServerPacksSource.class)
abstract class IoeServerPacksSourceMixin {
    @Inject(method="createPackRepository(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/server/packs/repository/PackRepository;",at=@At("RETURN"))
    private static void ioe$bindWorld(LevelStorageSource.LevelStorageAccess access,
                                      CallbackInfoReturnable<PackRepository> callback) {
        NetherWorldActivation.bind(callback.getReturnValue(),access.getLevelPath(LevelResource.ROOT));
    }
}
