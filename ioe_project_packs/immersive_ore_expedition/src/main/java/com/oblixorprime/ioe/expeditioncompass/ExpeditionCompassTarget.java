package com.oblixorprime.ioe.expeditioncompass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSiteKind;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public record ExpeditionCompassTarget(
        ResourceKey<Level> dimension,
        BlockPos pos,
        ExpeditionSiteKind kind,
        Optional<ResourceLocation> anchorId,
        Optional<ResourceLocation> provinceId,
        Optional<SiteQuality> quality,
        Optional<String> source,
        ExpeditionSitePlacementState placementState,
        Optional<String> placementReason
) {
    private static final Codec<SiteQuality> SITE_QUALITY_CODEC =
            Codec.STRING.xmap(SiteQuality::valueOf, SiteQuality::name);

    public static final Codec<ExpeditionCompassTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(ExpeditionCompassTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(ExpeditionCompassTarget::pos),
            ExpeditionSiteKind.CODEC.fieldOf("kind").forGetter(ExpeditionCompassTarget::kind),
            ResourceLocation.CODEC.optionalFieldOf("anchor_id").forGetter(ExpeditionCompassTarget::anchorId),
            ResourceLocation.CODEC.optionalFieldOf("province_id").forGetter(ExpeditionCompassTarget::provinceId),
            SITE_QUALITY_CODEC.optionalFieldOf("quality").forGetter(ExpeditionCompassTarget::quality),
            Codec.STRING.optionalFieldOf("source").forGetter(ExpeditionCompassTarget::source),
            ExpeditionSitePlacementState.CODEC
                    .optionalFieldOf("placement_state", ExpeditionSitePlacementState.PLACED)
                    .forGetter(ExpeditionCompassTarget::placementState),
            Codec.STRING.optionalFieldOf("placement_reason").forGetter(ExpeditionCompassTarget::placementReason)
    ).apply(instance, ExpeditionCompassTarget::new));

    public static final StreamCodec<ByteBuf, ExpeditionCompassTarget> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public ExpeditionCompassTarget {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(kind, "kind");
        anchorId = anchorId == null ? Optional.empty() : anchorId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        quality = quality == null ? Optional.empty() : quality;
        source = source == null ? Optional.empty() : source.map(String::trim).filter(value -> !value.isBlank());
        placementState = placementState == null ? ExpeditionSitePlacementState.PLACED : placementState;
        placementReason = placementReason == null
                ? Optional.empty()
                : placementReason.map(String::trim).filter(value -> !value.isBlank());
    }

    public ExpeditionCompassTarget(
            ResourceKey<Level> dimension,
            BlockPos pos,
            ExpeditionSiteKind kind,
            Optional<ResourceLocation> anchorId,
            Optional<ResourceLocation> provinceId,
            Optional<SiteQuality> quality,
            Optional<String> source
    ) {
        this(
                dimension,
                pos,
                kind,
                anchorId,
                provinceId,
                quality,
                source,
                ExpeditionSitePlacementState.PLACED,
                Optional.empty()
        );
    }

    public static ExpeditionCompassTarget fromSite(ExpeditionSite site) {
        Objects.requireNonNull(site, "site");
        return new ExpeditionCompassTarget(
                site.dimension(),
                site.pos(),
                site.kind(),
                site.anchorId(),
                site.provinceId(),
                site.quality(),
                site.source(),
                site.placementState(),
                site.placementReason()
        );
    }

    public Optional<ResourceLocation> primaryId() {
        return switch (kind) {
            case ANCHOR -> anchorId;
            case PROVINCE -> provinceId;
        };
    }

    public String displayName() {
        return primaryId()
                .map(ExpeditionCompassTarget::readableIdPath)
                .orElseGet(() -> readableWords(kind.messageLabel()));
    }

    public String coordinateText() {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public String waypointName() {
        return "IOE " + displayName();
    }

    public boolean playable() {
        return placementState.playable();
    }

    public long distanceBlocksFrom(BlockPos origin) {
        Objects.requireNonNull(origin, "origin");
        long dx = (long) origin.getX() - pos.getX();
        long dy = (long) origin.getY() - pos.getY();
        long dz = (long) origin.getZ() - pos.getZ();
        return Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    public LodestoneTracker asUntrackedLodestoneTracker() {
        return new LodestoneTracker(Optional.of(GlobalPos.of(dimension, pos)), false);
    }

    private static String readableIdPath(ResourceLocation id) {
        return readableWords(id.getPath());
    }

    private static String readableWords(String value) {
        String normalized = value.replace('_', ' ').replace('-', ' ').trim();
        if (normalized.isEmpty()) {
            return "Unknown";
        }

        StringBuilder builder = new StringBuilder(normalized.length());
        boolean capitalizeNext = true;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (Character.isWhitespace(character)) {
                if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                capitalizeNext = true;
            } else if (capitalizeNext) {
                builder.append(Character.toUpperCase(character));
                capitalizeNext = false;
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
