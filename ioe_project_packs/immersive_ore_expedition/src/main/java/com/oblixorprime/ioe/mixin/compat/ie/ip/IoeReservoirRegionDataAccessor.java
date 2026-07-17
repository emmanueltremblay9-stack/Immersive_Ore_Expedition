package com.oblixorprime.ioe.mixin.compat.ie.ip;

import com.google.common.collect.Multimap;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exact-object rollback access for the IP collection, whose public getter returns an immutable copy. */
@Pseudo
@Mixin(targets = "flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionData", remap = false)
public interface IoeReservoirRegionDataAccessor {
    @Accessor("reservoirlist")
    Multimap<ResourceKey<Level>, Reservoir> ioe$getMutableReservoirList();
}
