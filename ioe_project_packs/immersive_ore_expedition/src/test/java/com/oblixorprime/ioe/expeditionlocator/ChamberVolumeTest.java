package com.oblixorprime.ioe.expeditionlocator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.oblixorprime.ioe.core.SiteQuality;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ChamberVolumeTest {
    private static final BlockPos ANCHOR = new BlockPos(-16,80,-16);
    private static ChamberVolume volume() { return new ChamberVolume(List.of(new AABB(-16,40,-16,-15,41,-15))); }
    private static ExpeditionSite site(BlockPos anchor) {
        return ExpeditionSite.anchor(Level.OVERWORLD,anchor,ResourceLocation.parse("immersive_ore_expedition:miner_camp"),
                null,SiteQuality.DRY,"natural_connected_expedition_site").withChamberVolume(volume());
    }
    @Test void boundariesNegativeCoordinatesAndEmptyLegacyVolume() {
        var v=volume();
        assertTrue(v.contains(new Vec3(-16,40,-16)));
        assertFalse(v.contains(new Vec3(-15,40,-16)));
        assertFalse(v.contains(new Vec3(-16,41,-16)));
        assertFalse(v.contains(new Vec3(-16.000001,40,-16)));
        assertFalse(ChamberVolume.EMPTY.contains(new Vec3(-16,40,-16)));
        assertEquals(v.boxes(),ChamberVolume.load(v.save(),ANCHOR).boxes());
    }
    @Test void persistenceAndV2MigrationNeverInventGeometryOrInvalidateExistingSites() {
        var data=new ExpeditionLocatorSavedData();data.record(site(ANCHOR));
        var saved=data.save(new CompoundTag(),null);
        var loaded=ExpeditionLocatorSavedData.FACTORY.deserializer().apply(saved,null);
        assertEquals(volume().boxes(),loaded.index().sites().getFirst().chamberVolume().boxes());
        saved.putInt("data_version",2);
        loaded=ExpeditionLocatorSavedData.FACTORY.deserializer().apply(saved,null);
        assertTrue(loaded.index().sites().getFirst().playable());
        assertTrue(loaded.index().sites().getFirst().chamberVolume().boxes().isEmpty());
    }
    @Test void malformedVolumeDoesNotGrantEntry() {
        var tags=volume().save();tags.getCompound(0).putDouble("x1",Double.NaN);
        assertTrue(ChamberVolume.load(tags,ANCHOR).boxes().isEmpty());
        tags=volume().save();tags.getCompound(0).remove("y0");
        assertTrue(ChamberVolume.load(tags,ANCHOR).boxes().isEmpty());
    }
    @Test void boundedIndexRotatesAndReplacementRemovesStaleGeometry() {
        var index=new ExpeditionLocatorIndex();
        for(int i=0;i<33;i++) index.record(site(ANCHOR.offset(i,0,0)));
        var seen=new java.util.HashSet<BlockPos>();
        for(int i=0;i<3;i++) {
            var batch=index.chamberCandidates(Level.OVERWORLD,new BlockPos(-16,40,-16));
            assertTrue(batch.size()<=16);batch.forEach(s->seen.add(s.pos()));
        }
        assertEquals(33,seen.size());
        assertTrue(index.chamberCandidates(Level.NETHER,new BlockPos(-16,40,-16)).isEmpty());
        var one=new ExpeditionLocatorIndex();one.record(site(ANCHOR));
        one.record(site(ANCHOR).withChamberVolume(ChamberVolume.EMPTY));
        assertTrue(one.chamberCandidates(Level.OVERWORLD,new BlockPos(-16,40,-16)).isEmpty());
    }
}
