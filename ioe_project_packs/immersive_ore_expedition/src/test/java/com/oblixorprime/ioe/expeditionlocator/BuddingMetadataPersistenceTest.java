package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.budding.BuddingNodeInfo;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BuddingMetadataPersistenceTest {
    private static final BlockPos NODE = new BlockPos(8, 20, 8);
    private static final BuddingNodeInfo INFO = new BuddingNodeInfo(NODE, 3, 7, 7, ResourceLocation.parse("geore:iron"));

    private static ExpeditionSite site() {
        return ExpeditionSite.anchor(Level.OVERWORLD, new BlockPos(4, 40, 6), ResourceLocation.parse("immersive_ore_expedition:miner_camp"),
                null, SiteQuality.MOTHERLODE, "natural_connected_expedition_site").withBuddingNodes(List.of(INFO));
    }

    @Test
    void nodeMetadataRoundTripsWithTheExistingLocatorSave() {
        var data = new ExpeditionLocatorSavedData();
        data.record(site());
        var loaded = ExpeditionLocatorSavedData.FACTORY.deserializer().apply(data.save(new CompoundTag(), null), null);
        assertEquals(site(), loaded.index().sites().getFirst());
        assertEquals(INFO, loaded.index().buddingNodeAt(Level.OVERWORLD, NODE).orElseThrow().node());
        assertTrue(loaded.index().buddingNodeAt(Level.NETHER, NODE).isEmpty());
    }

    @Test
    void versionTwoSitesWithoutNodesRemainPlayableAndDoNotInventMetadata() {
        var data = new ExpeditionLocatorSavedData();
        data.record(site());
        CompoundTag tag = data.save(new CompoundTag(), null);
        tag.getList("sites", Tag.TAG_COMPOUND).getCompound(0).remove("budding_nodes");
        var loaded = ExpeditionLocatorSavedData.FACTORY.deserializer().apply(tag, null);
        assertTrue(loaded.index().sites().getFirst().playable());
        assertTrue(loaded.index().sites().getFirst().buddingNodes().isEmpty());
        assertTrue(loaded.index().buddingNodeAt(Level.OVERWORLD, NODE).isEmpty());
    }

    @Test
    void replacementAndPlayerRemovalCannotRetainOldNodeMetadata() {
        var index = new ExpeditionLocatorIndex();
        index.record(site());
        var lower = new BuddingNodeInfo(NODE.east(), 1, 3, 4, INFO.family());
        index.record(site().withBuddingNodes(List.of(lower)));
        assertTrue(index.buddingNodeAt(Level.OVERWORLD, NODE).isEmpty());
        assertTrue(index.removeBuddingNode(Level.OVERWORLD, NODE.east()));
        assertFalse(index.removeBuddingNode(Level.OVERWORLD, NODE.east()));
        assertTrue(index.sites().getFirst().buddingNodes().isEmpty());
        index.record(site());
        index.clear();
        assertTrue(index.buddingNodeAt(Level.OVERWORLD, NODE).isEmpty());
    }

    @Test
    void malformedNodeMetadataDoesNotInvalidateTheExistingSite() {
        var data = new ExpeditionLocatorSavedData();
        data.record(site());
        CompoundTag tag = data.save(new CompoundTag(), null);
        tag.getList("sites", Tag.TAG_COMPOUND).getCompound(0).getList("budding_nodes", Tag.TAG_COMPOUND)
                .getCompound(0).putInt("index", 99);
        var loaded = ExpeditionLocatorSavedData.FACTORY.deserializer().apply(tag, null);
        assertTrue(loaded.index().sites().getFirst().playable());
        assertTrue(loaded.index().sites().getFirst().buddingNodes().isEmpty());
    }
    @Test
    void recoveredLegacyQualityIsUnknownButAnchorRemainsPlayable() {
        var data = new ExpeditionLocatorSavedData();
        data.record(site());
        CompoundTag tag = data.save(new CompoundTag(), null);
        var savedSite = tag.getList("sites", Tag.TAG_COMPOUND).getCompound(0);
        savedSite.putString("source", "bounded_admin_reindex_mine_signature");
        savedSite.putString("quality", "NORMAL");
        savedSite.remove("budding_nodes");
        var loaded = ExpeditionLocatorSavedData.FACTORY.deserializer().apply(tag, null);
        var recovered = loaded.index().sites().getFirst();
        assertTrue(recovered.playable());
        assertEquals(site().pos(), recovered.pos());
        assertTrue(recovered.quality().isEmpty(), "Mine signature cannot prove original quality");
        assertTrue(recovered.buddingNodes().isEmpty());
        assertTrue(loaded.isDirty(), "Corrected provenance must be saved");
        assertFalse(loaded.save(new CompoundTag(), null).getList("sites", Tag.TAG_COMPOUND)
                .getCompound(0).contains("quality"));
        var target = com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.fromSite(recovered);
        assertTrue(target.playable() && target.quality().isEmpty());
        var roundTrip = ExpeditionLocatorSavedData.FACTORY.deserializer()
                .apply(loaded.save(new CompoundTag(), null), null);
        assertEquals(recovered, roundTrip.index().sites().getFirst());
        assertFalse(roundTrip.isDirty());
    }

    @Test
    void savedCompassRecoveryTargetCannotRetainInventedQuality() {
        var original = com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.fromSite(site());
        var encoded = com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, original).getOrThrow();
        var tag = (CompoundTag) encoded;
        tag.putString("source", "bounded_admin_reindex_mine_signature");
        tag.putString("quality", "NORMAL");
        var decoded = com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).getOrThrow();
        assertTrue(decoded.quality().isEmpty(), "Legacy item component retained fabricated quality");
        assertEquals(original.pos(), decoded.pos());
        assertTrue(decoded.playable());
        assertEquals(original, com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget.CODEC
                        .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, original).getOrThrow()).getOrThrow());
    }

}
