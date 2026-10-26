package com.oblixorprime.ioe.worldgen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import java.util.HashSet;

/** Bounded metadata-only collision check. Unknown metadata blocks placement. */
public final class LoadedStructureCollision {
    static final int MAX_FOOTPRINT_CHUNKS = 64, MAX_METADATA_ENTRIES = 256;
    private LoadedStructureCollision() { }

    public static boolean blocksPlacement(WorldGenLevel level, BoundingBox bounds) {
        int minX = Math.floorDiv(bounds.minX(), 16), maxX = Math.floorDiv(bounds.maxX(), 16);
        int minZ = Math.floorDiv(bounds.minZ(), 16), maxZ = Math.floorDiv(bounds.maxZ(), 16);
        if (((long) maxX - minX + 1) * ((long) maxZ - minZ + 1) > MAX_FOOTPRINT_CHUNKS) return true;
        var checked = new HashSet<StructureStart>();
        int entries = 0;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            var chunk = available(level, new ChunkPos(x, z), ChunkStatus.STRUCTURE_REFERENCES);
            if (chunk == null) return true;
            for (var start : chunk.getAllStarts().values()) {
                if (++entries > MAX_METADATA_ENTRIES) return true;
                if (start != null && start.isValid() && checked.add(start) && bounds.intersects(start.getBoundingBox())) return true;
            }
            for (var reference : chunk.getAllReferences().entrySet()) {
                if (++entries > MAX_METADATA_ENTRIES) return true;
                var origins = reference.getValue().iterator();
                while (origins.hasNext()) {
                    if (++entries > MAX_METADATA_ENTRIES) return true;
                    var owner = available(level, new ChunkPos(origins.nextLong()), ChunkStatus.STRUCTURE_STARTS);
                    if (owner == null) return true;
                    var start = owner.getStartForStructure(reference.getKey());
                    if (start == null || !start.isValid()) return true;
                    if (checked.add(start) && bounds.intersects(start.getBoundingBox())) return true;
                }
            }
        }
        return false;
    }

    private static ChunkAccess available(WorldGenLevel level, ChunkPos pos, ChunkStatus required) {
        ChunkAccess chunk;
        if (level instanceof ServerLevel server) {
            chunk = server.getChunkSource().getChunkNow(pos.x, pos.z);
        } else if (level instanceof WorldGenRegion region) {
            // Uses the region's existing dependency cache, never the ServerLevel chunk provider.
            if (!region.hasChunk(pos.x, pos.z)) return null;
            try {
                chunk = region.getChunk(pos.x, pos.z, required, false);
            } catch (net.minecraft.ReportedException | IllegalStateException unavailable) {
                return null; // Pinned WorldGenRegion throws for unavailable status even with false.
            }
        } else {
            return null;
        }
        if (chunk == null || !chunk.getPersistedStatus().isOrAfter(required)) return null;
        return chunk;
    }
}
