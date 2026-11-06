package com.oblixorprime.ioe.expeditionlocator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Exact planned free-space boxes, never inferred from an anchor or surviving nodes. */
public final class ChamberVolume {
    private final List<AABB> boxes;
    private final Map<BlockPos, List<AABB>> cells;
    public List<AABB> boxes() { return boxes; }
    @Override public boolean equals(Object other) { return other instanceof ChamberVolume v && boxes.equals(v.boxes); }
    @Override public int hashCode() { return boxes.hashCode(); }
    public static final ChamberVolume EMPTY = new ChamberVolume(List.of());
    public static final int MAX_BOXES = 20000;
    public ChamberVolume(List<AABB> boxes) {
        this.boxes = List.copyOf(boxes);
        Map<BlockPos,List<AABB>> indexed = new HashMap<>();
        if (boxes.size() > MAX_BOXES) throw new IllegalArgumentException("Oversized chamber volume");
        for (AABB b : boxes) {
            if (!Double.isFinite(b.minX) || !Double.isFinite(b.minY) || !Double.isFinite(b.minZ)
                    || !Double.isFinite(b.maxX) || !Double.isFinite(b.maxY) || !Double.isFinite(b.maxZ)
                    || b.getXsize() <= 0 || b.getYsize() <= 0 || b.getZsize() <= 0
                    || b.getXsize() > 1 || b.getYsize() > 1 || b.getZsize() > 1
                    || b.maxX > Math.floor(b.minX)+1 || b.maxY > Math.floor(b.minY)+1 || b.maxZ > Math.floor(b.minZ)+1)
                throw new IllegalArgumentException("Invalid chamber cell");
            indexed.computeIfAbsent(BlockPos.containing(b.minX,b.minY,b.minZ), ignored -> new ArrayList<>()).add(b);
        }
        Map<BlockPos,List<AABB>> frozen = new HashMap<>();
        indexed.forEach((p, v) -> frozen.put(p, List.copyOf(v)));
        cells = Map.copyOf(frozen);
    }
    /** Half-open boxes: a boundary belongs to only the cell on its positive side. */
    public boolean contains(Vec3 feet) { return cells.getOrDefault(BlockPos.containing(feet), List.of()).stream().anyMatch(b -> b.contains(feet)); }
    public ChamberVolume clipTo(java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> blocks) {
        List<AABB> result = new ArrayList<>();
        for (AABB box : boxes) {
            BlockPos p = BlockPos.containing(box.minX,box.minY,box.minZ);
            var state = blocks.get(p);
            if (state == null) continue;
            var solid = state.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, p);
            var free = net.minecraft.world.phys.shapes.Shapes.joinUnoptimized(
                    net.minecraft.world.phys.shapes.Shapes.create(box.move(-p.getX(),-p.getY(),-p.getZ())),
                    solid, net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST);
            for (AABB part : free.toAabbs()) result.add(part.move(p));
        }
        return new ChamberVolume(result);
    }

    public Set<Long> chunks() {
        Set<Long> result = new HashSet<>();
        for (AABB b : boxes) result.add(net.minecraft.world.level.ChunkPos.asLong(
                net.minecraft.util.Mth.floor(b.minX) >> 4, net.minecraft.util.Mth.floor(b.minZ) >> 4));
        return Set.copyOf(result);
    }
    public ListTag save() {
        ListTag result = new ListTag();
        for (AABB b : boxes) {
            CompoundTag t = new CompoundTag();
            t.putDouble("x0", b.minX); t.putDouble("y0", b.minY); t.putDouble("z0", b.minZ);
            t.putDouble("x1", b.maxX); t.putDouble("y1", b.maxY); t.putDouble("z1", b.maxZ);
            result.add(t);
        }
        return result;
    }
    public static ChamberVolume load(ListTag tags, BlockPos anchor) {
        if (tags.size() > MAX_BOXES) return EMPTY;
        List<AABB> boxes = new ArrayList<>();
        try {
            for (Tag value : tags) {
                if (!(value instanceof CompoundTag t)) return EMPTY;
                for (String key : List.of("x0","y0","z0","x1","y1","z1"))
                    if (!t.contains(key, Tag.TAG_DOUBLE)) return EMPTY;
                double x=t.getDouble("x0"), y=t.getDouble("y0"), z=t.getDouble("z0");
                double xx=t.getDouble("x1"), yy=t.getDouble("y1"), zz=t.getDouble("z1");
                if (!(x < xx && y < yy && z < zz)) return EMPTY;
                // Serialization sanity bound, not a gameplay proximity rule.
                if (Math.abs(x-anchor.getX()) > 128 || Math.abs(y-anchor.getY()) > 128
                        || Math.abs(z-anchor.getZ()) > 128) return EMPTY;
                boxes.add(new AABB(x,y,z,xx,yy,zz));
            }
            return new ChamberVolume(boxes);
        } catch (IllegalArgumentException invalid) { return EMPTY; }
    }
}
