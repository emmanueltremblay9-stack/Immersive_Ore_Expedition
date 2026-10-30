package com.oblixorprime.ioe.surface02;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

/** A prevalidated write envelope supplied by the future active worldgen caller. */
public record Surface02TerrainContext(
        Bounds allowedBounds,
        boolean stableSurface,
        boolean clearEntrance,
        boolean aquatic,
        boolean shoreline
) {
    public Surface02TerrainContext {
        Objects.requireNonNull(allowedBounds, "allowedBounds");
    }

    public static Surface02TerrainContext canonicalEnvelope(BlockPos origin) {
        Objects.requireNonNull(origin, "origin");
        return new Surface02TerrainContext(
                new Bounds(
                        origin.getX() - Surface02Plan.HORIZONTAL_RADIUS,
                        origin.getX() + Surface02Plan.HORIZONTAL_RADIUS,
                        origin.getY() - Surface02Plan.MAX_DEPTH,
                        origin.getY() + Surface02Plan.FACADE_HEIGHT,
                        origin.getZ(),
                        origin.getZ() + Surface02Plan.LENGTH - 1
                ),
                true,
                true,
                false,
                false
        );
    }

    public boolean admitsCanonicalEnvelope(BlockPos origin) {
        Bounds required = canonicalEnvelope(origin).allowedBounds();
        ChunkPos anchorChunk = new ChunkPos(origin);
        boolean singleAnchorChunk = new ChunkPos(
                new BlockPos(required.minX(), origin.getY(), required.minZ())
        ).equals(anchorChunk)
                && new ChunkPos(
                new BlockPos(required.maxX(), origin.getY(), required.maxZ())
        ).equals(anchorChunk);
        return stableSurface
                && clearEntrance
                && !aquatic
                && !shoreline
                && singleAnchorChunk
                && allowedBounds.contains(required);
    }

    public record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        public Bounds {
            if (minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Surface 02 bounds must be ordered");
            }
        }

        public boolean contains(BlockPos pos) {
            return pos.getX() >= minX
                    && pos.getX() <= maxX
                    && pos.getY() >= minY
                    && pos.getY() <= maxY
                    && pos.getZ() >= minZ
                    && pos.getZ() <= maxZ;
        }

        public boolean contains(Bounds other) {
            return other.minX >= minX
                    && other.maxX <= maxX
                    && other.minY >= minY
                    && other.maxY <= maxY
                    && other.minZ >= minZ
                    && other.maxZ <= maxZ;
        }
    }
}
