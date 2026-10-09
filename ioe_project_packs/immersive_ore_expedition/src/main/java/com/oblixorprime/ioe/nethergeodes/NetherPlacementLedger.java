package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Claims are terminal on reload, including interrupted attempts. No world-write permission is persisted. */
final class NetherPlacementLedger extends SavedData implements NetherPlacementCoordinator.Ledger {
    static final String NAME = "immersive_ore_expedition_nether_attempts";
    static final Factory<NetherPlacementLedger> FACTORY = new Factory<>(NetherPlacementLedger::new, NetherPlacementLedger::load);
    private record Attempt(BlockPos origin, String result) { }
    private final Map<Long, Attempt> attempts = new LinkedHashMap<>();
    private static long region(BlockPos pos) {
        return net.minecraft.world.level.ChunkPos.asLong(Math.floorDiv(pos.getX(), 256), Math.floorDiv(pos.getZ(), 256));
    }
    public boolean claim(BlockPos origin) {
        if (attempts.containsKey(region(origin))) return false;
        attempts.put(region(origin), new Attempt(origin.immutable(), "INTERRUPTED"));
        setDirty();
        return true;
    }
    public boolean hasAcceptedWithin(BlockPos origin, int distance) {
        if (distance != 256) throw new IllegalArgumentException("Canonical spacing is 256");
        int rx = Math.floorDiv(origin.getX(), 256), rz = Math.floorDiv(origin.getZ(), 256);
        for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
            var a = attempts.get(net.minecraft.world.level.ChunkPos.asLong(rx + dx, rz + dz));
            if (a == null || !(a.result().equals("COMMITTED") || a.result().equals("ROLLBACK_INCOMPLETE"))) continue;
            long x = (long) origin.getX() - a.origin().getX(), z = (long) origin.getZ() - a.origin().getZ();
            if (x * x + z * z < 65_536L) return true;
        }
        return false;
    }

    public void finish(BlockPos origin, NetherPlacementCoordinator.Result result) {
        attempts.put(region(origin), new Attempt(origin.immutable(), result.name()));
        setDirty();
    }
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        attempts.values().forEach(attempt -> {
            var entry = new CompoundTag();
            entry.putLong("origin", attempt.origin().asLong());
            entry.putString("result", attempt.result());
            list.add(entry);
        });
        tag.put("attempts", list);
        return tag;
    }
    private static NetherPlacementLedger load(CompoundTag tag, HolderLookup.Provider registries) {
        var result = new NetherPlacementLedger();
        var entries = tag.getList("attempts", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            if (!entry.contains("origin", Tag.TAG_LONG)) continue;
            var origin = BlockPos.of(entry.getLong("origin"));
            result.attempts.putIfAbsent(region(origin), new Attempt(origin, entry.getString("result")));
        }
        return result;
    }
}
