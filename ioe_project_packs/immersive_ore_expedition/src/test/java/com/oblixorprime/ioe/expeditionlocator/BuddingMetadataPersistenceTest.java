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
}
