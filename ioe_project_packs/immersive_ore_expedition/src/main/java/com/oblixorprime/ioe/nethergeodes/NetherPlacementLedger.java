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
    private final Map<Long, Map<Long, String>> resources = new LinkedHashMap<>();
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
    /** A single-use transfer of a durable admission claim to the placement coordinator. */
    NetherPlacementCoordinator.Ledger prepare(BlockPos origin) {
        if (!claim(origin)) return null;
        var admitted = origin.immutable();
        return new NetherPlacementCoordinator.Ledger() {
            boolean consumed;
            public boolean claim(BlockPos target) {
                if (consumed || target.getX() != admitted.getX() || target.getZ() != admitted.getZ()) return false;
                consumed = true;
                return true;
            }
            public boolean hasAcceptedWithin(BlockPos target, int distance) {
                return NetherPlacementLedger.this.hasAcceptedWithin(target, distance);
            }
            public void recordResource(BlockPos target, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                NetherPlacementLedger.this.recordResource(admitted, pos, state);
            }
            public void finish(BlockPos target, NetherPlacementCoordinator.Result result) {
                NetherPlacementLedger.this.finish(admitted, result);
            }
        };
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
        if (result != NetherPlacementCoordinator.Result.COMMITTED
                && result != NetherPlacementCoordinator.Result.ROLLBACK_INCOMPLETE) resources.remove(region(origin));
        attempts.put(region(origin), new Attempt(origin.immutable(), result.name()));
        setDirty();
    }
    public void recordResource(BlockPos origin, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (region(pos) != region(origin) && !NetherOreProvenance.isResource(state)) return;
        if (region(pos) != region(origin) || !attempts.containsKey(region(origin)))
            throw new IllegalArgumentException("Resource outside claimed region");
        var entries = resources.computeIfAbsent(region(origin), ignored -> new LinkedHashMap<>());
        if (NetherOreProvenance.isResource(state)) {
            if (!entries.containsKey(pos.asLong()) && entries.size() >= NetherPlacementCoordinator.MAX_WRITES)
                throw new IllegalArgumentException("Unbounded resource provenance");
            entries.put(pos.asLong(), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        } else entries.remove(pos.asLong());
        setDirty();
    }
    boolean preserves(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (!NetherOreProvenance.isResource(state)) return false;
        var attempt = attempts.get(region(pos));
        if (attempt == null || !(attempt.result().equals("COMMITTED") || attempt.result().equals("ROLLBACK_INCOMPLETE")
                || attempt.result().equals("INTERRUPTED"))) return false;
        var entries = resources.get(region(pos));
        return entries != null && net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()
                .equals(entries.get(pos.asLong()));
    }
    Optional<String> resultAt(BlockPos origin) {
        var attempt = attempts.get(region(origin));
        return attempt == null ? Optional.empty() : Optional.of(attempt.result());
    }
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        attempts.values().forEach(attempt -> {
            var entry = new CompoundTag();
            entry.putLong("origin", attempt.origin().asLong());
            entry.putString("result", attempt.result());
            var blocks = new ListTag();
            resources.getOrDefault(region(attempt.origin()), Map.of()).forEach((pos, id) -> {
                var block = new CompoundTag(); block.putLong("pos", pos); block.putString("block", id); blocks.add(block);
            });
            entry.put("resources", blocks);
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
            if (result.attempts.putIfAbsent(region(origin), new Attempt(origin, entry.getString("result"))) != null) continue;
            var blocks = entry.getList("resources", Tag.TAG_COMPOUND);
            var retained = new LinkedHashMap<Long, String>();
            for (int j = 0; j < Math.min(blocks.size(), NetherPlacementCoordinator.MAX_WRITES); j++) {
                var block = blocks.getCompound(j);
                if (!block.contains("pos", Tag.TAG_LONG)) continue;
                long pos = block.getLong("pos"); String id = block.getString("block");
                if (region(BlockPos.of(pos)) == region(origin)
                        && (id.equals("minecraft:nether_quartz_ore") || id.equals("minecraft:ancient_debris"))) retained.put(pos, id);
            }
            if (!retained.isEmpty()) result.resources.put(region(origin), retained);
        }
        return result;
    }
}
