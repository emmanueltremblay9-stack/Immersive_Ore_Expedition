package com.oblixorprime.ioe.core;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ExpeditionAnchorRef(ResourceKey<Level> dimension, BlockPos pos, String anchorType, SiteQuality quality) {}
