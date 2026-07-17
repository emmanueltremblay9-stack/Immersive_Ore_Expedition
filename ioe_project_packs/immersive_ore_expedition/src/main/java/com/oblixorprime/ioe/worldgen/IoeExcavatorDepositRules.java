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
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

/** Admission and reserve rules for biome-owned IE Excavator deposits. */
public final class IoeExcavatorDepositRules {
    private static final String MODID = ImmersiveOreExpeditionMod.MODID;
    private static final TagKey<Biome> SPECIALIZED_BIOMES = biomeTag("ore_profile/specialized");
    private static final TagKey<Biome> ALLUVIAL_BIOMES = biomeTag("aquatic/alluvial_sift");
    private static final TagKey<Biome> SILT_BIOMES = biomeTag("aquatic/silt");
    private static final TagKey<Biome> ANCIENT_SEABED_BIOMES = biomeTag("aquatic/ancient_seabed");
    private static final ResourceLocation ALLUVIAL_MIX = ieMix("alluvial_sift");
    private static final ResourceLocation SILT_MIX = ieMix("silt");
    private static final ResourceLocation ANCIENT_SEABED_MIX = ieMix("ancient_seabed");

    private static final LongAdder NATIVE_SPECIALIZED_SUPPRESSED = new LongAdder();
    private static final LongAdder NATIVE_AQUATIC_ACCEPTED = new LongAdder();
    private static final LongAdder NATIVE_GENERIC_ACCEPTED = new LongAdder();
    private static final LongAdder NATIVE_MISMATCH_REJECTED = new LongAdder();
    private static final LongAdder REQUIRED_PRESENT = new LongAdder();
    private static final LongAdder REQUIRED_CREATED = new LongAdder();
    private static final LongAdder REQUIRED_FAILED = new LongAdder();
    private static final LongAdder REQUIRED_IE_ABSENT = new LongAdder();
    private static final LongAdder REQUIRED_ROLLED_BACK = new LongAdder();

    private IoeExcavatorDepositRules() {
    }

    /**
     * Keeps native IE generation only for the exact aquatic mix or for generic IE biomes. Specialized IOE
     * biomes are provisioned transactionally by their expedition site and never receive a second free vein.
     */
    public static boolean allowNativeCandidate(
            ServerLevel level,
            ColumnPos depositPos,
            ResourceLocation mineralMixId
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(depositPos, "depositPos");
        Objects.requireNonNull(mineralMixId, "mineralMixId");
        Holder<Biome> biome = level.getBiome(new BlockPos(depositPos.x(), level.getSeaLevel(), depositPos.z()));

        ResourceLocation requiredAquaticMix = null;
        int aquaticMatches = 0;
        if (biome.is(ALLUVIAL_BIOMES)) {
            requiredAquaticMix = ALLUVIAL_MIX;
            aquaticMatches++;
        }
        if (biome.is(SILT_BIOMES)) {
            requiredAquaticMix = SILT_MIX;
            aquaticMatches++;
        }
        if (biome.is(ANCIENT_SEABED_BIOMES)) {
            requiredAquaticMix = ANCIENT_SEABED_MIX;
            aquaticMatches++;
        }
        if (aquaticMatches > 0) {
            boolean accepted = aquaticMatches == 1 && mineralMixId.equals(requiredAquaticMix);
            if (accepted) {
                NATIVE_AQUATIC_ACCEPTED.increment();
            } else {
                NATIVE_MISMATCH_REJECTED.increment();
            }
            return accepted;
        }
        if (biome.is(SPECIALIZED_BIOMES) || columnContainsSpecializedBiome(level, depositPos)) {
            NATIVE_SPECIALIZED_SUPPRESSED.increment();
            return false;
        }
        boolean genericIeMix = !MODID.equals(mineralMixId.getNamespace());
        if (genericIeMix) {
            NATIVE_GENERIC_ACCEPTED.increment();
        } else {
            NATIVE_MISMATCH_REJECTED.increment();
        }
        return genericIeMix;
    }

    public static Optional<MotherDepositRequest> depositRequest(
            BlockPos anchorPos,
            SiteQuality quality,
            ResourceLocation provinceId,
            BiomeMineResourceProfile resourceProfile
    ) {
        Objects.requireNonNull(anchorPos, "anchorPos");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(provinceId, "provinceId");
        Objects.requireNonNull(resourceProfile, "resourceProfile");
        Optional<BiomeMineResourceDefinition.DepositTier> tierSpec = resourceProfile.depositTier(quality);
        if (tierSpec.isEmpty()) {
            return Optional.empty();
        }
        BiomeMineResourceDefinition.DepositTier spec = tierSpec.orElseThrow();
        return Optional.of(new MotherDepositRequest(
                anchorPos.immutable(),
                provinceId,
                resourceProfile.profileName(),
                resourceProfile.mineralMixId(),
                resourceProfile.sampledConnectedChunks(),
                DepositTier.from(quality),
                spec.radiusBlocks(),
                spec.capacity()
        ));
    }

    public static boolean acceptsMineralMix(MotherDepositRequest request, ResourceLocation mineralMixId) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(mineralMixId, "mineralMixId");
        return request.mineralMixId().equals(mineralMixId)
                && provinceAllows(request.provinceId(), mineralMixId);
    }

    public static void recordGuaranteedMotherPresent() {
        REQUIRED_PRESENT.increment();
    }

    public static void recordGuaranteedMotherCreated() {
        REQUIRED_CREATED.increment();
    }

    public static void recordGuaranteedMotherFailed() {
        REQUIRED_FAILED.increment();
    }

    public static void recordGuaranteedMotherIeAbsent() {
        REQUIRED_IE_ABSENT.increment();
    }

    public static void recordGuaranteedMotherRolledBack() {
        REQUIRED_ROLLED_BACK.increment();
    }

    static String statusMessage() {
        return "IOE IE Excavator gate: nativeSpecializedSuppressed=" + NATIVE_SPECIALIZED_SUPPRESSED.sum()
                + ", nativeAquaticAccepted=" + NATIVE_AQUATIC_ACCEPTED.sum()
                + ", nativeGenericAccepted=" + NATIVE_GENERIC_ACCEPTED.sum()
                + ", nativeMismatchRejected=" + NATIVE_MISMATCH_REJECTED.sum()
                + ", requiredPresent=" + REQUIRED_PRESENT.sum()
                + ", requiredCreated=" + REQUIRED_CREATED.sum()
                + ", requiredFailed=" + REQUIRED_FAILED.sum()
                + ", requiredIeAbsent=" + REQUIRED_IE_ABSENT.sum()
                + ", requiredRolledBack=" + REQUIRED_ROLLED_BACK.sum();
    }

    static void resetDiagnostics() {
        NATIVE_SPECIALIZED_SUPPRESSED.reset();
        NATIVE_AQUATIC_ACCEPTED.reset();
        NATIVE_GENERIC_ACCEPTED.reset();
        NATIVE_MISMATCH_REJECTED.reset();
        REQUIRED_PRESENT.reset();
        REQUIRED_CREATED.reset();
        REQUIRED_FAILED.reset();
        REQUIRED_IE_ABSENT.reset();
        REQUIRED_ROLLED_BACK.reset();
    }

    private static boolean provinceAllows(ResourceLocation provinceId, ResourceLocation mineralMixId) {
        if (!IoeWorldgenConfig.provinceRuntimeIntegrationEnabled()) {
            return true;
        }
        return ProvinceResourcePolicyResolver.fromConfig()
                .evaluate(new ProvinceId(provinceId), new ResourceRef(ResourceType.ITEM, mineralMixId))
                .shouldUse();
    }

    private static boolean columnContainsSpecializedBiome(ServerLevel level, ColumnPos depositPos) {
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y += 4) {
            if (level.getBiome(new BlockPos(depositPos.x(), y, depositPos.z())).is(SPECIALIZED_BIOMES)) {
                return true;
            }
        }
        return false;
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MODID, path));
    }

    private static ResourceLocation ieMix(String path) {
        return ResourceLocation.fromNamespaceAndPath("immersiveengineering", "mineral/" + path);
    }

    public enum DepositTier {
        MOTHER,
        MAJOR,
        MINOR,
        DIRECT;

        private static DepositTier from(SiteQuality quality) {
            return switch (quality) {
                case MOTHERLODE -> MOTHER;
                case RICH -> MAJOR;
                case NORMAL -> MINOR;
                case POOR -> DIRECT;
                case DRY -> throw new IllegalArgumentException("Dry sites do not own IE deposits");
            };
        }
    }

    public record MotherDepositRequest(
            BlockPos anchorPos,
            ResourceLocation provinceId,
            String profileName,
            ResourceLocation mineralMixId,
            int connectedBiomeChunks,
            DepositTier tier,
            int radiusBlocks,
            int capacity
    ) {
        public MotherDepositRequest {
            Objects.requireNonNull(anchorPos, "anchorPos");
            Objects.requireNonNull(provinceId, "provinceId");
            Objects.requireNonNull(profileName, "profileName");
            Objects.requireNonNull(mineralMixId, "mineralMixId");
            Objects.requireNonNull(tier, "tier");
            if (profileName.isBlank() || connectedBiomeChunks <= 0 || radiusBlocks <= 0 || capacity <= 0) {
                throw new IllegalArgumentException("IE deposit request contains invalid profile, sampling or reserve data");
            }
            anchorPos = anchorPos.immutable();
        }
    }
}
