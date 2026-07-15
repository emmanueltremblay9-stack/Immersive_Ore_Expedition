package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;

final class ExpeditionLocatorSavedData extends SavedData {
    static final String STORAGE_NAME = "immersive_ore_expedition_expedition_sites";
    static final SavedData.Factory<ExpeditionLocatorSavedData> FACTORY = new SavedData.Factory<>(
            ExpeditionLocatorSavedData::new,
            ExpeditionLocatorSavedData::load
    );

    private static final int DATA_VERSION = 2;
    private static final String NATURAL_CONNECTED_SITE_SOURCE = "natural_connected_expedition_site";
    private static final String LEGACY_UNVERIFIED_REASON = "legacy_site_requires_bounded_reindex";
    private static final String SITES = "sites";
    private final ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();

    ExpeditionLocatorIndex index() {
        return index;
    }

    void record(ExpeditionSite site) {
        index.record(site);
        setDirty();
    }

    void recordPlacedProof(ResourceKey<Level> dimension, RuntimeWorldgenPlacementProofResult result) {
        index.recordPlacedProof(dimension, result);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("data_version", DATA_VERSION);
        ListTag sites = new ListTag();
        for (ExpeditionSite site : index.diagnosticSites()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", site.dimension().location().toString());
            entry.putInt("x", site.pos().getX());
            entry.putInt("y", site.pos().getY());
            entry.putInt("z", site.pos().getZ());
            entry.putString("kind", site.kind().getSerializedName());
            site.anchorId().ifPresent(value -> entry.putString("anchor_id", value.toString()));
            site.provinceId().ifPresent(value -> entry.putString("province_id", value.toString()));
            site.quality().ifPresent(value -> entry.putString("quality", value.name()));
            site.source().ifPresent(value -> entry.putString("source", value));
            entry.putString("placement_state", site.placementState().getSerializedName());
            site.placementReason().ifPresent(value -> entry.putString("placement_reason", value));
            sites.add(entry);
        }
        tag.put(SITES, sites);
        return tag;
    }

    private static ExpeditionLocatorSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ExpeditionLocatorSavedData data = new ExpeditionLocatorSavedData();
        int loadedVersion = tag.getInt("data_version");
        ListTag sites = tag.getList(SITES, Tag.TAG_COMPOUND);
        for (int index = 0; index < sites.size(); index++) {
            readSite(sites.getCompound(index))
                    .map(site -> migrateLegacyNaturalSite(site, loadedVersion))
                    .ifPresent(data.index::record);
        }
        if (loadedVersion < DATA_VERSION) {
            data.setDirty();
        }
        return data;
    }

    private static ExpeditionSite migrateLegacyNaturalSite(ExpeditionSite site, int loadedVersion) {
        if (loadedVersion >= DATA_VERSION
                || site.source().filter(NATURAL_CONNECTED_SITE_SOURCE::equals).isEmpty()
                || !site.playable()) {
            return site;
        }
        return new ExpeditionSite(
                site.dimension(),
                site.pos(),
                site.kind(),
                site.anchorId(),
                site.provinceId(),
                site.quality(),
                site.source(),
                ExpeditionSitePlacementState.PLACEMENT_FAILED,
                Optional.of(LEGACY_UNVERIFIED_REASON)
        );
    }

    private static Optional<ExpeditionSite> readSite(CompoundTag tag) {
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("dimension"));
        ExpeditionSiteKind kind = enumBySerializedName(ExpeditionSiteKind.values(), tag.getString("kind"));
        ExpeditionSitePlacementState placementState = enumBySerializedName(
                ExpeditionSitePlacementState.values(),
                tag.getString("placement_state")
        );
        if (dimensionId == null || kind == null || placementState == null) {
            return Optional.empty();
        }
        if (!tag.contains("x", Tag.TAG_INT)
                || !tag.contains("y", Tag.TAG_INT)
                || !tag.contains("z", Tag.TAG_INT)) {
            return Optional.empty();
        }

        SiteQuality quality = enumByName(SiteQuality.values(), tag.getString("quality"));
        return Optional.of(new ExpeditionSite(
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
                kind,
                optionalLocation(tag, "anchor_id"),
                optionalLocation(tag, "province_id"),
                Optional.ofNullable(quality),
                optionalString(tag, "source"),
                placementState,
                optionalString(tag, "placement_reason")
        ));
    }

    private static Optional<ResourceLocation> optionalLocation(CompoundTag tag, String key) {
        return optionalString(tag, key).map(ResourceLocation::tryParse).filter(value -> value != null);
    }

    private static Optional<String> optionalString(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_STRING)
                ? Optional.of(tag.getString(key)).filter(value -> !value.isBlank())
                : Optional.empty();
    }

    private static <T extends Enum<T> & net.minecraft.util.StringRepresentable> T enumBySerializedName(
            T[] values,
            String serializedName
    ) {
        for (T value : values) {
            if (value.getSerializedName().equals(serializedName)) {
                return value;
            }
        }
        return null;
    }

    private static <T extends Enum<T>> T enumByName(T[] values, String name) {
        for (T value : values) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
