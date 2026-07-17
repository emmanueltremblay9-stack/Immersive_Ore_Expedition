package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
 * Spatial admission policy for Immersive Petroleum. Reservoirs are parallel abstract resources, never a
 * second exclusive mine profile. Mother sites in an admitted biome always request the biome's reservoir,
 * while Rich sites receive a deterministic secondary chance. Lower tiers do not create reservoirs.
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
    public static final TagKey<Biome> LAVA_VOLCANO_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "ip_lava_volcano")
    );
    public static final TagKey<Biome> AQUIFER_WATER_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "ip_aquifer_water")
    );
    private static final int RICH_SITE_CHANCE = 4;
    private static final LongAdder NATIVE_SCANS_ALLOWED = new LongAdder();
    private static final LongAdder NATIVE_SCANS_SUPPRESSED = new LongAdder();
    private static final LongAdder UNAUTHORIZED_REGISTRATIONS_BLOCKED = new LongAdder();
    private static final LongAdder RESERVATIONS_PREPARED = new LongAdder();
    private static final LongAdder RESERVOIRS_CREATED = new LongAdder();
    private static final LongAdder EXISTING_RESERVOIRS_REUSED = new LongAdder();
    private static final LongAdder RESERVATIONS_FAILED = new LongAdder();
    private static final LongAdder RESERVOIRS_ROLLED_BACK = new LongAdder();
    private static final LongAdder AMBIGUOUS_BIOME_MATCHES_REJECTED = new LongAdder();

    private IoePetroleumReservoirRules() {
    }

    public static Optional<PetroleumReservoirRequest> request(
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
        if (!quality.isProductive()) {
            return Optional.empty();
        }
        Holder<Biome> biome = level.getBiome(anchorPos);
        boolean oil = biome.is(OIL_COAL_BIOMES);
        boolean lava = biome.is(LAVA_VOLCANO_BIOMES);
        boolean aquifer = biome.is(AQUIFER_WATER_BIOMES);
        int matches = (oil ? 1 : 0) + (lava ? 1 : 0) + (aquifer ? 1 : 0);
        if (matches != 1) {
            if (matches > 1) {
                AMBIGUOUS_BIOME_MATCHES_REJECTED.increment();
                if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
                    IoeExpeditionWorldgenMod.LOGGER.warn(
                            "Rejected ambiguous Immersive Petroleum reservoir admission at {} biome={} oil={} lava={} aquifer={}",
                            anchorPos,
                            resourceProfile.biomeId(),
                            oil,
                            lava,
                            aquifer
                    );
                }
            }
            return Optional.empty();
        }
        ReservoirKind reservoirKind = oil
                ? ReservoirKind.OIL
                : lava ? ReservoirKind.LAVA : ReservoirKind.AQUIFER;
        if (IoeWorldgenConfig.provinceRuntimeIntegrationEnabled()
                && !ProvinceResourcePolicyResolver.fromConfig()
                .evaluate(province, new ResourceRef(ResourceType.FLUID, reservoirKind.fluidId()))
                .shouldUse()) {
            return Optional.empty();
        }
        if (quality != SiteQuality.MOTHERLODE
                && (quality != SiteQuality.RICH
                || !selectedRichSite(level, anchorPos, province, resourceProfile, reservoirKind))) {
            return Optional.empty();
        }
        return Optional.of(new PetroleumReservoirRequest(
                reservoirKind,
                anchorPos.immutable(),
                resourceProfile.biomeId(),
                province.id(),
                resourceProfile.profileName(),
                resourceProfile.sampledConnectedChunks(),
                resourceProfile.definition().surveyRadiusChunks(),
                quality
        ));
    }

    public static boolean allowsNativeScan(ServerLevel level, ChunkPos chunkPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");
        boolean allowed = reservoirKindAt(
                level,
                new BlockPos(chunkPos.getMiddleBlockX(), level.getSeaLevel(), chunkPos.getMiddleBlockZ())
        ).isPresent();
        if (allowed) {
            NATIVE_SCANS_ALLOWED.increment();
        }
        return allowed;
    }

    public static boolean allowsNativeRegistration(
            ServerLevel level,
            BlockPos reservoirCenter,
            ResourceLocation fluidId
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(reservoirCenter, "reservoirCenter");
        Objects.requireNonNull(fluidId, "fluidId");
        BlockPos surfaceCenter = new BlockPos(reservoirCenter.getX(), level.getSeaLevel(), reservoirCenter.getZ());
        return reservoirKindAt(level, surfaceCenter)
                .map(kind -> kind.fluidId().equals(fluidId))
                .orElse(false);
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
        return "IOE Immersive Petroleum reservoirs: nativeScansAllowed=" + NATIVE_SCANS_ALLOWED.sum()
                + ", nativeScansSuppressed=" + NATIVE_SCANS_SUPPRESSED.sum()
                + ", unauthorizedRegistrationsBlocked=" + UNAUTHORIZED_REGISTRATIONS_BLOCKED.sum()
                + ", prepared=" + RESERVATIONS_PREPARED.sum()
                + ", created=" + RESERVOIRS_CREATED.sum()
                + ", existingReused=" + EXISTING_RESERVOIRS_REUSED.sum()
                + ", failed=" + RESERVATIONS_FAILED.sum()
                + ", rolledBack=" + RESERVOIRS_ROLLED_BACK.sum()
                + ", ambiguousBiomeMatchesRejected=" + AMBIGUOUS_BIOME_MATCHES_REJECTED.sum();
    }

    public static void resetDiagnostics() {
        NATIVE_SCANS_ALLOWED.reset();
        NATIVE_SCANS_SUPPRESSED.reset();
        UNAUTHORIZED_REGISTRATIONS_BLOCKED.reset();
        RESERVATIONS_PREPARED.reset();
        RESERVOIRS_CREATED.reset();
        EXISTING_RESERVOIRS_REUSED.reset();
        RESERVATIONS_FAILED.reset();
        RESERVOIRS_ROLLED_BACK.reset();
        AMBIGUOUS_BIOME_MATCHES_REJECTED.reset();
    }

    private static boolean selectedRichSite(
            ServerLevel level,
            BlockPos anchorPos,
            ProvinceId province,
            BiomeMineResourceProfile resourceProfile,
            ReservoirKind reservoirKind
    ) {
        long seed = mix(level.getSeed() ^ anchorPos.asLong());
        seed = mix(seed ^ level.dimension().location().hashCode());
        seed = mix(seed ^ province.id().hashCode());
        seed = mix(seed ^ resourceProfile.biomeId().hashCode());
        seed = mix(seed ^ reservoirKind.serializedName().hashCode());
        return Math.floorMod(seed, RICH_SITE_CHANCE) == 0;
    }

    private static Optional<ReservoirKind> reservoirKindAt(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        boolean oil = biome.is(OIL_COAL_BIOMES);
        boolean lava = biome.is(LAVA_VOLCANO_BIOMES);
        boolean aquifer = biome.is(AQUIFER_WATER_BIOMES);
        if ((oil ? 1 : 0) + (lava ? 1 : 0) + (aquifer ? 1 : 0) != 1) {
            return Optional.empty();
        }
        return Optional.of(oil ? ReservoirKind.OIL : lava ? ReservoirKind.LAVA : ReservoirKind.AQUIFER);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    public enum ReservoirKind {
        OIL("oil", CRUDE_OIL_ID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "reservoirs/oil")),
        LAVA("lava", ResourceLocation.fromNamespaceAndPath("minecraft", "lava"),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "reservoirs/lava")),
        AQUIFER("aquifer", ResourceLocation.fromNamespaceAndPath("minecraft", "water"),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "reservoirs/aquifer"));

        private final String serializedName;
        private final ResourceLocation fluidId;
        private final ResourceLocation defaultRecipeId;

        ReservoirKind(String serializedName, ResourceLocation fluidId, ResourceLocation defaultRecipeId) {
            this.serializedName = serializedName;
            this.fluidId = fluidId;
            this.defaultRecipeId = defaultRecipeId;
        }

        public String serializedName() {
            return serializedName;
        }

        public ResourceLocation fluidId() {
            return fluidId;
        }

        public ResourceLocation defaultRecipeId() {
            return defaultRecipeId;
        }
    }

    public record PetroleumReservoirRequest(
            ReservoirKind reservoirKind,
            BlockPos anchorPos,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            String profileName,
            int connectedBiomeChunks,
            int surveyRadiusChunks,
            SiteQuality quality
    ) {
        public PetroleumReservoirRequest {
            Objects.requireNonNull(reservoirKind, "reservoirKind");
            Objects.requireNonNull(anchorPos, "anchorPos");
            Objects.requireNonNull(biomeId, "biomeId");
            Objects.requireNonNull(provinceId, "provinceId");
            Objects.requireNonNull(profileName, "profileName");
            Objects.requireNonNull(quality, "quality");
            if (connectedBiomeChunks <= 0 || surveyRadiusChunks < 0) {
                throw new IllegalArgumentException("Petroleum reservoir biome sampling values are invalid");
            }
        }
    }
}
