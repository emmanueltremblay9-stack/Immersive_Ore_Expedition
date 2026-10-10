package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;
import java.util.UUID;

/** Player-safe projection: no internal anchor, province, nodes, budgets or original roll. */
public record DiscoveryView(UUID id, DiscoveryStage stage, ResourceLocation dimension,
                            ResourceLocation clueType, BlockPos clueLocation,
                            Optional<BlockPos> siteLocation, Optional<ResourceLocation> resource,
                            Optional<SiteQuality> quality) { }
