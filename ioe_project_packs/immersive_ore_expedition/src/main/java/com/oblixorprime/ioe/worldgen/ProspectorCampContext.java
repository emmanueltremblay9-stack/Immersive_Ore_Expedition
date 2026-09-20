package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

import java.util.Objects;

/** One stable visual input shared by the initial plan and every quality fallback rebuilt from that plan. */
public record ProspectorCampContext(
        ProspectorCampVisualFamily visualFamily,
        long siteSeed,
        boolean domumEnabled,
        ProspectorCampArchetype archetype
) {
    public ProspectorCampContext {
        Objects.requireNonNull(visualFamily, "visualFamily");
        Objects.requireNonNull(archetype, "archetype");
    }

    public ProspectorCampContext(
            ProspectorCampVisualFamily visualFamily,
            long siteSeed,
            boolean domumEnabled
    ) {
        this(visualFamily, siteSeed, domumEnabled, ProspectorCampArchetype.ACTIVE);
    }

    static ProspectorCampContext vanillaFallback(BlockPos origin, SiteQuality quality) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(quality, "quality");
        long seed = mix64(origin.asLong() ^ (0x9E3779B97F4A7C15L * (quality.ordinal() + 1L)));
        return new ProspectorCampContext(
                ProspectorCampVisualFamily.TEMPERATE,
                seed,
                false,
                ProspectorCampArchetype.ACTIVE
        );
    }

    public ProspectorCampContext withArchetype(ProspectorCampArchetype selectedArchetype) {
        return new ProspectorCampContext(visualFamily, siteSeed, domumEnabled, selectedArchetype);
    }

    public ProspectorCampState stateFor(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        ProspectorCampState[] allowed = switch (quality) {
            case DRY -> new ProspectorCampState[]{ProspectorCampState.ABANDONED, ProspectorCampState.COLLAPSED};
            case POOR -> new ProspectorCampState[]{ProspectorCampState.WEATHERED, ProspectorCampState.ABANDONED};
            case NORMAL -> new ProspectorCampState[]{ProspectorCampState.WEATHERED, ProspectorCampState.ACTIVE_RECENT};
            case RICH, MOTHERLODE -> new ProspectorCampState[]{
                    ProspectorCampState.ACTIVE_RECENT,
                    ProspectorCampState.WEATHERED
            };
        };
        long visualFamilySalt = (visualFamily.ordinal() + 1L) * 0x632BE59BD9B4E019L;
        int index = (int) Math.floorMod(
                mix64(siteSeed ^ visualFamilySalt ^ (quality.ordinal() * 0xD1B54A32D192ED03L)),
                allowed.length
        );
        return allowed[index];
    }

    public Rotation rotation() {
        return switch ((int) Math.floorMod(mix64(siteSeed ^ 0x94D049BB133111EBL), 4)) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    long decorationValue(BlockPos pos, long salt) {
        return mix64(siteSeed ^ pos.asLong() ^ salt);
    }

    static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public enum ProspectorCampState {
        ABANDONED,
        ACTIVE_RECENT,
        WEATHERED,
        COLLAPSED
    }
}
