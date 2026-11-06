package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryJournalDataTest {
    private static final UUID PLAYER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final DiscoverySiteKey KEY = new DiscoverySiteKey(Level.OVERWORLD.location(), new BlockPos(-18, 30, 42));
    private static final ResourceLocation TYPE = ResourceLocation.parse("immersive_ore_expedition:miner_camp");
    private static final BlockPos CLUE = new BlockPos(-20, 70, 40);
    private static DiscoveryEvidence evidence(DiscoveryStage stage) {
        return switch (stage) {
            case EVIDENCE_DISCOVERED -> new DiscoveryEvidence(stage, CLUE, TYPE, null);
            case SITE_LOCATED -> new DiscoveryEvidence(stage, new BlockPos(-18, 35, 42), null, null);
            case RESOURCE_IDENTIFIED -> new DiscoveryEvidence(stage, null, ResourceLocation.parse("geore:iron"), null);
            case SITE_SURVEYED -> new DiscoveryEvidence(stage, null, null, SiteQuality.RICH);
            case EXPEDITION_DOCUMENTED -> new DiscoveryEvidence(stage, null, null, null);
        };
    }
    private static DiscoveryJournalData reload(DiscoveryJournalData data) {
        return DiscoveryJournalData.FACTORY.deserializer().apply(data.save(new CompoundTag(), null), null);
    }

    @Test void chamberEntryIsIndependentPersistentPrivateAndIdempotent() {
        var data = new DiscoveryJournalData();
        assertTrue(data.recordChamberEntry(PLAYER, KEY));
        assertTrue(data.views(PLAYER).isEmpty());
        assertTrue(data.stage(PLAYER, KEY).isEmpty());
        data = reload(data);
        assertTrue(data.enteredChamber(PLAYER, KEY));
        data.setDirty(false);
        assertFalse(data.recordChamberEntry(PLAYER, KEY));
        assertFalse(data.isDirty());
        assertFalse(data.enteredChamber(UUID.randomUUID(), KEY));
        assertFalse(data.enteredChamber(PLAYER, new DiscoverySiteKey(Level.NETHER.location(), KEY.anchor())));
        data.advance(PLAYER, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED));
        assertFalse(data.views(PLAYER).getFirst().chamberEntered());
        var id = data.views(PLAYER).getFirst().id();
        data.advance(PLAYER, KEY, evidence(DiscoveryStage.SITE_LOCATED));
        var view = reload(data).views(PLAYER).getFirst();
        assertEquals(id, view.id());
        assertTrue(view.chamberEntered());
        assertEquals(DiscoveryStage.SITE_LOCATED, view.stage());
        assertTrue(view.resource().isEmpty());
        assertTrue(view.quality().isEmpty());
    }

    @Test void legacySurveyRetainsIdentityAndHistoryButProvesNoChamberEntry() {
        var data = new DiscoveryJournalData();
        for (var stage : DiscoveryStage.values()) data.advance(PLAYER, KEY, evidence(stage));
        var before = data.views(PLAYER).getFirst();
        data.recordChamberEntry(PLAYER, KEY);
        var legacy = data.save(new CompoundTag(), null);
        legacy.putInt("version", 1); // A v1 file cannot assert the new fact, even with injected fields.
        var loaded = DiscoveryJournalData.FACTORY.deserializer().apply(legacy, null);
        assertEquals(before, loaded.views(PLAYER).getFirst());
        assertFalse(loaded.enteredChamber(PLAYER, KEY));
        assertEquals(2, loaded.save(new CompoundTag(), null).getInt("version"));
    }

    @Test void stagesAreOrderedMonotoneAndRepeatedEvidenceDoesNotDirtyOrDuplicate() {
        var data = new DiscoveryJournalData();
        assertFalse(data.advance(PLAYER, KEY, evidence(DiscoveryStage.SITE_SURVEYED)));
        UUID id = null;
        for (var stage : DiscoveryStage.values()) {
            assertTrue(data.advance(PLAYER, KEY, evidence(stage)));
            var view = data.views(PLAYER).getFirst();
            if (id == null) id = view.id();
            assertEquals(id, view.id());
            data.setDirty(false);
            assertFalse(data.advance(PLAYER, KEY, evidence(stage)));
            assertFalse(data.isDirty());
            data = reload(data);
            assertEquals(view, data.views(PLAYER).getFirst());
        }
        assertFalse(data.advance(PLAYER, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED)));
        assertEquals(1, data.views(PLAYER).size());
    }

    @Test void playerAndDimensionIdentitiesAreIsolatedAcrossReload() {
        var data = new DiscoveryJournalData();
        UUID other = UUID.randomUUID();
        var nether = new DiscoverySiteKey(Level.NETHER.location(), KEY.anchor());
        data.advance(PLAYER, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED));
        data.advance(PLAYER, nether, evidence(DiscoveryStage.EVIDENCE_DISCOVERED));
        assertTrue(data.views(other).isEmpty());
        data.advance(other, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED));
        data = reload(data);
        assertEquals(2, data.views(PLAYER).size());
        assertEquals(1, data.views(other).size());
        assertNotEquals(data.views(PLAYER).getFirst().id(), data.views(other).getFirst().id());
    }

    @Test void projectionsRevealOnlyEarnedFieldsAndIgnoreInjectedFutureDetails() {
        var data = new DiscoveryJournalData();
        for (var stage : DiscoveryStage.values()) {
            data.advance(PLAYER, KEY, evidence(stage));
            CompoundTag tag = data.save(new CompoundTag(), null);
            var row = tag.getList("entries", Tag.TAG_COMPOUND).getCompound(0);
            row.putLong("located", new BlockPos(999, 12, 999).asLong());
            row.putString("resource", "minecraft:diamond");
            row.putString("quality", "MOTHERLODE");
            var view = DiscoveryJournalData.FACTORY.deserializer().apply(tag, null).views(PLAYER).getFirst();
            assertEquals(stage.ordinal() >= 1, view.siteLocation().isPresent());
            assertEquals(stage.ordinal() >= 2, view.resource().isPresent());
            assertEquals(stage.ordinal() >= 3, view.quality().isPresent());
            assertEquals(CLUE, view.clueLocation());
            assertNotEquals(KEY.anchor(), view.clueLocation());
        }
        assertThrows(IllegalArgumentException.class, () -> new DiscoveryEvidence(
                DiscoveryStage.EVIDENCE_DISCOVERED, CLUE, TYPE, SiteQuality.MOTHERLODE));
    }

    @Test void malformedAndDuplicateRowsDoNotCreatePhantomOrUpgradedDiscoveries() {
        var data = new DiscoveryJournalData();
        data.advance(PLAYER, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED));
        CompoundTag tag = data.save(new CompoundTag(), null);
        var entries = tag.getList("entries", Tag.TAG_COMPOUND);
        var duplicate = entries.getCompound(0).copy();
        duplicate.putString("stage", "EXPEDITION_DOCUMENTED");
        duplicate.putLong("located", CLUE.asLong());
        duplicate.putString("resource", "minecraft:diamond");
        duplicate.putString("quality", "MOTHERLODE");
        entries.add(duplicate);
        var malformed = duplicate.copy();
        malformed.putString("dimension", "INVALID ID");
        entries.add(malformed);
        var loaded = DiscoveryJournalData.FACTORY.deserializer().apply(tag, null);
        assertEquals(data.views(PLAYER), loaded.views(PLAYER));
        tag.putInt("version", 99);
        var future = DiscoveryJournalData.FACTORY.deserializer().apply(tag, null);
        assertTrue(future.views(PLAYER).isEmpty());
        assertFalse(future.advance(PLAYER, KEY, evidence(DiscoveryStage.EVIDENCE_DISCOVERED)));
        assertFalse(future.recordChamberEntry(PLAYER, KEY));
        assertEquals(tag, future.save(new CompoundTag(), null));
    }

    @Test void identitySurvivesQualityAndReindexMetadataChanges() {
        var original = ExpeditionSite.anchor(Level.OVERWORLD, KEY.anchor(), TYPE, null, SiteQuality.RICH,
                "natural_connected_expedition_site");
        var recovered = ExpeditionSite.anchor(Level.OVERWORLD, KEY.anchor(), TYPE, null, null,
                ExpeditionSite.RECOVERED_MINE_SOURCE);
        assertEquals(DiscoverySiteKey.from(original), DiscoverySiteKey.from(recovered));
        var mutable = new BlockPos.MutableBlockPos(1, 2, 3);
        var key = new DiscoverySiteKey(Level.OVERWORLD.location(), mutable);
        mutable.set(8, 9, 10);
        assertEquals(new BlockPos(1, 2, 3), key.anchor());
    }
}
