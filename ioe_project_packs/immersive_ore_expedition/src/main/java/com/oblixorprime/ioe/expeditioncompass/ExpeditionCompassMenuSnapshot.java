package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ExpeditionCompassMenuSnapshot(
        ResourceKey<Level> dimension,
        InteractionHand hand,
        Optional<ExpeditionCompassTarget> currentTarget,
        List<ExpeditionCompassMenuEntry> entries
) {
    static final int MAX_MENU_ENTRIES = 128;

    public static final StreamCodec<RegistryFriendlyByteBuf, ExpeditionCompassMenuSnapshot> STREAM_CODEC =
            StreamCodec.ofMember(ExpeditionCompassMenuSnapshot::write, ExpeditionCompassMenuSnapshot::new);

    public ExpeditionCompassMenuSnapshot {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(hand, "hand");
        currentTarget = currentTarget == null ? Optional.empty() : currentTarget;
        Objects.requireNonNull(entries, "entries");
        if (entries.size() > MAX_MENU_ENTRIES) {
            throw new IllegalArgumentException("entries must not exceed " + MAX_MENU_ENTRIES);
        }
        entries = List.copyOf(entries);
    }

    public static ExpeditionCompassMenuSnapshot fromIndex(
            ResourceKey<Level> dimension,
            BlockPos origin,
            InteractionHand hand,
            Optional<ExpeditionCompassTarget> currentTarget,
            ExpeditionLocatorIndex locatorIndex
    ) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(hand, "hand");
        Objects.requireNonNull(currentTarget, "currentTarget");
        Objects.requireNonNull(locatorIndex, "locatorIndex");

        List<ExpeditionCompassMenuEntry> entries = locatorIndex.sites().stream()
                .filter(site -> site.dimension().equals(dimension))
                .map(site -> entryFromSite(site, origin))
                .sorted(entryComparator())
                .limit(MAX_MENU_ENTRIES)
                .toList();

        return new ExpeditionCompassMenuSnapshot(
                dimension,
                hand,
                validCurrentTarget(currentTarget, locatorIndex),
                entries
        );
    }

    public Optional<ExpeditionCompassMenuEntry> matchingEntry(ExpeditionCompassTarget requestedTarget) {
        Objects.requireNonNull(requestedTarget, "requestedTarget");
        return entries.stream()
                .filter(entry -> entry.target().equals(requestedTarget))
                .findFirst();
    }

    private static ExpeditionCompassMenuEntry entryFromSite(ExpeditionSite site, BlockPos origin) {
        ExpeditionCompassTarget target = ExpeditionCompassTarget.fromSite(site);
        return new ExpeditionCompassMenuEntry(target, target.distanceBlocksFrom(origin));
    }

    private static Optional<ExpeditionCompassTarget> validCurrentTarget(
            Optional<ExpeditionCompassTarget> currentTarget,
            ExpeditionLocatorIndex locatorIndex
    ) {
        return currentTarget
                .filter(ExpeditionCompassTarget::playable)
                .filter(target -> locatorIndex.sites().stream()
                        .map(ExpeditionCompassTarget::fromSite)
                        .anyMatch(target::equals));
    }

    private static Comparator<ExpeditionCompassMenuEntry> entryComparator() {
        return Comparator
                .comparingLong(ExpeditionCompassMenuEntry::distanceBlocks)
                .thenComparing(entry -> entry.target().kind().getSerializedName())
                .thenComparing(entry -> locationKey(entry.target().primaryId()))
                .thenComparingInt(entry -> entry.target().pos().getX())
                .thenComparingInt(entry -> entry.target().pos().getY())
                .thenComparingInt(entry -> entry.target().pos().getZ())
                .thenComparing(entry -> locationKey(entry.target().anchorId()))
                .thenComparing(entry -> locationKey(entry.target().provinceId()))
                .thenComparing(entry -> entry.target().quality().map(Enum::name).orElse(""))
                .thenComparing(entry -> entry.target().source().orElse(""));
    }

    private ExpeditionCompassMenuSnapshot(RegistryFriendlyByteBuf buffer) {
        this(
                buffer.readResourceKey(Registries.DIMENSION),
                buffer.readEnum(InteractionHand.class),
                readOptionalTarget(buffer),
                readEntries(buffer)
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceKey(dimension);
        buffer.writeEnum(hand);
        buffer.writeBoolean(currentTarget.isPresent());
        currentTarget.ifPresent(target -> ExpeditionCompassTarget.STREAM_CODEC.encode(buffer, target));
        buffer.writeVarInt(entries.size());
        for (ExpeditionCompassMenuEntry entry : entries) {
            ExpeditionCompassMenuEntry.STREAM_CODEC.encode(buffer, entry);
        }
    }

    private static Optional<ExpeditionCompassTarget> readOptionalTarget(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return Optional.empty();
        }
        return Optional.of(ExpeditionCompassTarget.STREAM_CODEC.decode(buffer));
    }

    private static List<ExpeditionCompassMenuEntry> readEntries(RegistryFriendlyByteBuf buffer) {
        int entryCount = buffer.readVarInt();
        if (entryCount < 0 || entryCount > MAX_MENU_ENTRIES) {
            throw new IllegalArgumentException("Invalid expedition compass entry count " + entryCount);
        }

        List<ExpeditionCompassMenuEntry> decodedEntries = new ArrayList<>(entryCount);
        for (int index = 0; index < entryCount; index++) {
            decodedEntries.add(ExpeditionCompassMenuEntry.STREAM_CODEC.decode(buffer));
        }
        return decodedEntries;
    }

    private static String locationKey(Optional<ResourceLocation> id) {
        return id.map(ResourceLocation::toString).orElse("");
    }
}
