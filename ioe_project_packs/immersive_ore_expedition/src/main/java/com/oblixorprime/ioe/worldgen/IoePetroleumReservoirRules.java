package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

/**
 * Spatial admission policy for Immersive Petroleum. Oil is a parallel abstract reservoir, never a second
 * exclusive mine profile: Mother coal sites in configured deserts always request oil, while Rich coal sites
 * receive a deterministic secondary chance. Lower tiers and non-desert provinces never create reservoirs.
 */
public final class IoePetroleumReservoirRules {
    public static final String MOD_ID = "immersivepetroleum";
    public static final ResourceLocation CRUDE_OIL_ID = ResourceLocation.fromNamespaceAndPath(
            MOD_ID,
            "crudeoil"
    );
    public static final TagKey<Biome> OIL_COAL_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "ip_oil_coal")
    );
    private static final int RICH_SITE_CHANCE = 4;
    private static final LongAdder NATIVE_SCANS_SUPPRESSED = new LongAdder();
    private static final LongAdder UNAUTHORIZED_REGISTRATIONS_BLOCKED = new LongAdder();
    private static final LongAdder RESERVATIONS_PREPARED = new LongAdder();
    private static final LongAdder RESERVOIRS_CREATED = new LongAdder();
    private static final LongAdder EXISTING_RESERVOIRS_REUSED = new LongAdder();
    private static final LongAdder RESERVATIONS_FAILED = new LongAdder();
    private static final LongAdder RESERVOIRS_ROLLED_BACK = new LongAdder();

    private IoePetroleumReservoirRules() {
    }

    public static Optional<OilReservoirRequest> request(
            ServerLevel level,
            BlockPos anchorPos,
            SiteQuality quality,
            ProvinceId province,
            BiomeMineResourceProfile resourceProfile
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(province, "province");
        Objects.requireNonNull(resourceProfile, "resourceProfile");
        if (!quality.isProductive()
                || resourceProfile.resourceKind() != BiomeMineResourceProfile.ResourceKind.GEORE
                || !"coal".equals(resourceProfile.profileName())
                || !level.getBiome(anchorPos).is(OIL_COAL_BIOMES)) {
            return Optional.empty();
        }
        if (quality != SiteQuality.MOTHERLODE
                && (quality != SiteQuality.RICH || !selectedRichSite(level, anchorPos, province, resourceProfile))) {
            return Optional.empty();
        }
        return Optional.of(new OilReservoirRequest(
                anchorPos.immutable(),
                resourceProfile.biomeId(),
                province.id(),
                resourceProfile.profileName(),
                resourceProfile.sampledConnectedChunks(),
                resourceProfile.definition().surveyRadiusChunks(),
                quality
        ));
    }

    public static void recordNativeScanSuppressed(ServerLevel level, ChunkPos chunkPos) {
        NATIVE_SCANS_SUPPRESSED.increment();
        if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            IoeExpeditionWorldgenMod.LOGGER.debug(
                    "Suppressed free Immersive Petroleum reservoir scan in {} chunk={}",
                    level.dimension().location(),
                    chunkPos
            );
        }
    }

    public static void recordUnauthorizedRegistrationBlocked() {
        UNAUTHORIZED_REGISTRATIONS_BLOCKED.increment();
    }

    public static void recordReservationPrepared() {
        RESERVATIONS_PREPARED.increment();
    }

    public static void recordReservoirCreated() {
        RESERVOIRS_CREATED.increment();
    }

    public static void recordExistingReservoirReused() {
        EXISTING_RESERVOIRS_REUSED.increment();
    }

    public static void recordReservationFailed() {
        RESERVATIONS_FAILED.increment();
    }

    public static void recordReservoirRolledBack() {
        RESERVOIRS_ROLLED_BACK.increment();
    }

    public static String statusMessage() {
        return "IOE Immersive Petroleum reservoirs: nativeScansSuppressed=" + NATIVE_SCANS_SUPPRESSED.sum()
                + ", unauthorizedRegistrationsBlocked=" + UNAUTHORIZED_REGISTRATIONS_BLOCKED.sum()
                + ", prepared=" + RESERVATIONS_PREPARED.sum()
                + ", created=" + RESERVOIRS_CREATED.sum()
                + ", existingReused=" + EXISTING_RESERVOIRS_REUSED.sum()
                + ", failed=" + RESERVATIONS_FAILED.sum()
                + ", rolledBack=" + RESERVOIRS_ROLLED_BACK.sum();
    }

    public static void resetDiagnostics() {
        NATIVE_SCANS_SUPPRESSED.reset();
        UNAUTHORIZED_REGISTRATIONS_BLOCKED.reset();
        RESERVATIONS_PREPARED.reset();
        RESERVOIRS_CREATED.reset();
        EXISTING_RESERVOIRS_REUSED.reset();
        RESERVATIONS_FAILED.reset();
        RESERVOIRS_ROLLED_BACK.reset();
    }

    private static boolean selectedRichSite(
            ServerLevel level,
            BlockPos anchorPos,
            ProvinceId province,
            BiomeMineResourceProfile resourceProfile
    ) {
        long seed = mix(level.getSeed() ^ anchorPos.asLong());
        seed = mix(seed ^ level.dimension().location().hashCode());
        seed = mix(seed ^ province.id().hashCode());
        seed = mix(seed ^ resourceProfile.biomeId().hashCode());
        return Math.floorMod(seed, RICH_SITE_CHANCE) == 0;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    public record OilReservoirRequest(
            BlockPos anchorPos,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            String profileName,
            int connectedBiomeChunks,
            int surveyRadiusChunks,
            SiteQuality quality
    ) {
        public OilReservoirRequest {
            Objects.requireNonNull(anchorPos, "anchorPos");
            Objects.requireNonNull(biomeId, "biomeId");
            Objects.requireNonNull(provinceId, "provinceId");
            Objects.requireNonNull(profileName, "profileName");
            Objects.requireNonNull(quality, "quality");
            if (connectedBiomeChunks <= 0 || surveyRadiusChunks < 0) {
                throw new IllegalArgumentException("Oil reservoir biome sampling values are invalid");
            }
        }
    }
}
