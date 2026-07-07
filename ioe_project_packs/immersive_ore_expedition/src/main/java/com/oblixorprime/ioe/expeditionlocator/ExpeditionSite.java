package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public record ExpeditionSite(
        ResourceKey<Level> dimension,
        BlockPos pos,
        ExpeditionSiteKind kind,
        Optional<ResourceLocation> anchorId,
        Optional<ResourceLocation> provinceId,
        Optional<SiteQuality> quality,
        Optional<String> source
) {
    public ExpeditionSite {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(kind, "kind");
        anchorId = anchorId == null ? Optional.empty() : anchorId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        quality = quality == null ? Optional.empty() : quality;
        source = source == null ? Optional.empty() : source.map(String::trim).filter(value -> !value.isBlank());
    }

    public static ExpeditionSite anchor(
            ResourceKey<Level> dimension,
            BlockPos pos,
            ResourceLocation anchorId,
            ResourceLocation provinceId,
            SiteQuality quality,
            String source
    ) {
        return new ExpeditionSite(
                dimension,
                pos,
                ExpeditionSiteKind.ANCHOR,
                Optional.ofNullable(anchorId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(quality),
                Optional.ofNullable(source)
        );
    }

    public static ExpeditionSite province(
            ResourceKey<Level> dimension,
            BlockPos pos,
            ResourceLocation anchorId,
            ResourceLocation provinceId,
            SiteQuality quality,
            String source
    ) {
        return new ExpeditionSite(
                dimension,
                pos,
                ExpeditionSiteKind.PROVINCE,
                Optional.ofNullable(anchorId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(quality),
                Optional.ofNullable(source)
        );
    }

    public Optional<ResourceLocation> primaryId() {
        return switch (kind) {
            case ANCHOR -> anchorId;
            case PROVINCE -> provinceId;
        };
    }
}
