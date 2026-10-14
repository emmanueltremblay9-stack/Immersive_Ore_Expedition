package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** World-owned storage. Never pruned when a site is depleted, unloaded or reindexed. */
final class DiscoveryJournalData extends SavedData {
    static final String NAME = "immersive_ore_expedition_discoveries";
    static final Factory<DiscoveryJournalData> FACTORY = new Factory<>(DiscoveryJournalData::new, DiscoveryJournalData::load);
    private record Key(UUID player, DiscoverySiteKey site) { }
    private final Map<Key, DiscoveryView> records = new LinkedHashMap<>();
    private final Map<UUID, List<Key>> playerRows = new HashMap<>();
    private CompoundTag unsupportedData;

    List<DiscoveryView> views(UUID player) {
        return records.entrySet().stream().filter(e -> e.getKey().player().equals(player))
                .map(Map.Entry::getValue).toList();
    }

    DiscoveryPage page(UUID player, int requestedOffset) {
        if (requestedOffset < 0) throw new IllegalArgumentException("Negative page offset");
        List<Key> rows = playerRows.getOrDefault(player, List.of());
        if (rows.isEmpty()) return new DiscoveryPage(0, 0, Optional.empty());
        int offset = Math.min(requestedOffset, rows.size() - 1);
        return new DiscoveryPage(offset, rows.size(), Optional.of(records.get(rows.get(offset))));
    }

    boolean contains(UUID player, DiscoverySiteKey site) { return records.containsKey(new Key(player, site)); }

    Optional<DiscoveryStage> stage(UUID player, DiscoverySiteKey site) {
        return Optional.ofNullable(records.get(new Key(player, site))).map(DiscoveryView::stage);
    }

    boolean advance(UUID player, DiscoverySiteKey site, DiscoveryEvidence evidence) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(site);
        Objects.requireNonNull(evidence);
        if (unsupportedData != null) return false;
        Key key = new Key(player, site);
        DiscoveryView previous = records.get(key);
        int expected = previous == null ? 0 : previous.stage().ordinal() + 1;
        if (evidence.stage().ordinal() != expected) return false;
        DiscoveryView next = previous == null
                ? new DiscoveryView(UUID.randomUUID(), evidence.stage(), site.dimension(), evidence.identifier(),
                    evidence.location(), Optional.empty(), Optional.empty(), Optional.empty())
                : new DiscoveryView(previous.id(), evidence.stage(), previous.dimension(), previous.clueType(),
                    previous.clueLocation(), evidence.stage() == DiscoveryStage.SITE_LOCATED
                        ? Optional.of(evidence.location()) : previous.siteLocation(),
                    evidence.stage() == DiscoveryStage.RESOURCE_IDENTIFIED
                        ? Optional.of(evidence.identifier()) : previous.resource(),
                    evidence.stage() == DiscoveryStage.SITE_SURVEYED
                        ? Optional.of(evidence.quality()) : previous.quality());
        records.put(key, next);
        if (previous == null) playerRows.computeIfAbsent(player, ignored -> new ArrayList<>()).add(key);
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (unsupportedData != null) return tag.merge(unsupportedData.copy());
        tag.putInt("version", 1);
        ListTag entries = new ListTag();
        records.forEach((key, view) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", key.player());
            entry.putString("dimension", key.site().dimension().toString());
            entry.putLong("anchor", key.site().anchor().asLong());
            entry.putUUID("id", view.id());
            entry.putString("stage", view.stage().name());
            entry.putString("clue_type", view.clueType().toString());
            entry.putLong("clue", view.clueLocation().asLong());
            view.siteLocation().ifPresent(pos -> entry.putLong("located", pos.asLong()));
            view.resource().ifPresent(id -> entry.putString("resource", id.toString()));
            view.quality().ifPresent(q -> entry.putString("quality", q.name()));
            entries.add(entry);
        });
        tag.put("entries", entries);
        return tag;
    }

    private static DiscoveryJournalData load(CompoundTag tag, HolderLookup.Provider registries) {
        DiscoveryJournalData data = new DiscoveryJournalData();
        if (tag.getInt("version") != 1) {
            // Throwing here lets DimensionDataStorage replace an unreadable file with fresh data.
            // Preserve unknown formats intact and refuse progression instead.
            data.unsupportedData = tag.copy();
            com.oblixorprime.ioe.ImmersiveOreExpeditionMod.LOGGER.warn(
                    "Unsupported IOE discovery data version {}; journal is read-only until migrated", tag.getInt("version"));
            return data;
        }
        for (Tag value : tag.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            try {
                if (!entry.hasUUID("player") || !entry.hasUUID("id")
                        || !entry.contains("anchor", Tag.TAG_LONG) || !entry.contains("clue", Tag.TAG_LONG)) continue;
                ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("dimension"));
                ResourceLocation clueType = ResourceLocation.tryParse(entry.getString("clue_type"));
                if (dimension == null || clueType == null) continue;
                DiscoveryStage stage = DiscoveryStage.valueOf(entry.getString("stage"));
                boolean located = stage.ordinal() >= DiscoveryStage.SITE_LOCATED.ordinal();
                boolean identified = stage.ordinal() >= DiscoveryStage.RESOURCE_IDENTIFIED.ordinal();
                boolean surveyed = stage.ordinal() >= DiscoveryStage.SITE_SURVEYED.ordinal();
                if (located && !entry.contains("located", Tag.TAG_LONG)) continue;
                ResourceLocation resource = identified ? ResourceLocation.tryParse(entry.getString("resource")) : null;
                if (identified && resource == null) continue;
                // Read only earned fields: injected future details never become a player projection.
                DiscoveryView view = new DiscoveryView(entry.getUUID("id"), stage, dimension, clueType,
                        BlockPos.of(entry.getLong("clue")), located ? Optional.of(BlockPos.of(entry.getLong("located"))) : Optional.empty(),
                        Optional.ofNullable(resource),
                        surveyed ? Optional.of(SiteQuality.valueOf(entry.getString("quality"))) : Optional.empty());
                Key key = new Key(entry.getUUID("player"), new DiscoverySiteKey(dimension, BlockPos.of(entry.getLong("anchor"))));
                DiscoveryView old = data.records.get(key);
                if (old == null) {
                    data.records.put(key, view);
                    data.playerRows.computeIfAbsent(key.player(), ignored -> new ArrayList<>()).add(key);
                }
                // Duplicate/corrupt rows cannot silently award a higher stage or change known facts.
            } catch (IllegalArgumentException invalid) {
                // One malformed entry cannot discard other players' journals.
            }
        }
        return data;
    }
}
