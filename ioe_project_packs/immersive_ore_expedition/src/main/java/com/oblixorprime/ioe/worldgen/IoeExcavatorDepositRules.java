package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * Spatial admission rules for IE's abstract Excavator deposits. Normal IE candidates remain filtered here;
 * confirmed Mother Nodes also expose a dependency-neutral request consumed by the optional IE bridge.
 */
public final class IoeExcavatorDepositRules {
    /** Major regions receive only one fifth of otherwise admissible IE deposit registrations. */
    private static final int MAJOR_NODE_CHANCE_DENOMINATOR = 5;
    private static final Map<String, Set<ResourceLocation>> COMPATIBLE_MINERAL_MIXES = Map.ofEntries(
            mixes("coal", "bituminous_coal"),
            mixes("iron", "banded_iron", "chalcopyrite", "laterite", "pentlandite", "wolframite"),
            mixes("copper", "auricupride", "chalcopyrite", "rich_auricupride"),
            mixes("diamond", "alluvial_sift"),
            mixes("emerald", "beryl"),
            mixes("gold", "auricupride", "galena", "lazulitic_intrusion", "rich_auricupride"),
            mixes("lapis", "lazulitic_intrusion"),
            mixes("redstone", "cinnabar"),
            mixes("aluminum", "laterite"),
            mixes("lead", "galena", "uraninite"),
            mixes("silver", "galena"),
            mixes("nickel", "laterite", "pentlandite"),
            mixes("uranium", "uraninite")
    );
    private static final LongAdder ATTEMPTS = new LongAdder();
    private static final LongAdder ACCEPTED_MOTHER = new LongAdder();
    private static final LongAdder ACCEPTED_MAJOR = new LongAdder();
    private static final LongAdder REJECTED_OUTSIDE = new LongAdder();
    private static final LongAdder REJECTED_MINOR = new LongAdder();
    private static final LongAdder REJECTED_PROFILE = new LongAdder();
    private static final LongAdder REJECTED_PROVINCE = new LongAdder();
    private static final LongAdder REJECTED_MAJOR_CHANCE = new LongAdder();
    private static final LongAdder REJECTED_MOTHER_RESERVED = new LongAdder();
    private static final LongAdder SUPPRESSED_NATIVE_REGISTRATION = new LongAdder();
    private static final LongAdder GUARANTEED_MOTHER_PRESENT = new LongAdder();
    private static final LongAdder GUARANTEED_MOTHER_CREATED = new LongAdder();
    private static final LongAdder GUARANTEED_MOTHER_FAILED = new LongAdder();
    private static final LongAdder GUARANTEED_MOTHER_IE_ABSENT = new LongAdder();
    private static final LongAdder GUARANTEED_MOTHER_ROLLED_BACK = new LongAdder();
    private static final LongAdder OPTIONAL_MAJOR_SELECTED = new LongAdder();
    private static final LongAdder OPTIONAL_MAJOR_PRESENT = new LongAdder();
    private static final LongAdder OPTIONAL_MAJOR_CREATED = new LongAdder();
    private static final LongAdder OPTIONAL_MAJOR_FAILED = new LongAdder();
    private static final LongAdder OPTIONAL_MAJOR_ROLLED_BACK = new LongAdder();

    private IoeExcavatorDepositRules() {
    }

    public static void recordNativeCandidate(
            ServerLevel level,
            ChunkPos generationChunk,
            ColumnPos depositPos,
            ResourceLocation mineralMixId
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(generationChunk, "generationChunk");
        Objects.requireNonNull(depositPos, "depositPos");
        Objects.requireNonNull(mineralMixId, "mineralMixId");
        ATTEMPTS.increment();

        java.util.List<IoePendingExpeditionSites.ExcavatorRegion> regions = IoePendingExpeditionSites
                .excavatorRegions(level.dimension(), generationChunk)
                .stream()
                .sorted(java.util.Comparator
                        .comparingInt(IoeExcavatorDepositRules::strength)
                        .reversed()
                        .thenComparingLong(region -> distanceSquared(depositPos, region.anchorPos())))
                .toList();
        if (regions.isEmpty()) {
            REJECTED_OUTSIDE.increment();
            return;
        }
        if (regions.stream().anyMatch(IoePendingExpeditionSites.ExcavatorRegion::motherDepositReserved)) {
            REJECTED_MOTHER_RESERVED.increment();
            return;
        }

        boolean eligibleTierFound = false;
        boolean matchingProfileFound = false;
        boolean allowedProvinceFound = false;
        for (IoePendingExpeditionSites.ExcavatorRegion region : regions) {
            NodeTier tier = NodeTier.from(region.quality());
            if (tier == NodeTier.MINOR) {
                continue;
            }
            eligibleTierFound = true;
            if (!matchesResourceProfile(region.resourceProfile(), mineralMixId)) {
                continue;
            }
            matchingProfileFound = true;
            if (!provinceAllows(region.provinceId(), mineralMixId)) {
                continue;
            }
            allowedProvinceFound = true;
            if (tier == NodeTier.MAJOR && !passesMajorNodeChance(
                    level,
                    generationChunk,
                    depositPos,
                    mineralMixId,
                    region
            )) {
                continue;
            }

            if (tier == NodeTier.MOTHER) {
                ACCEPTED_MOTHER.increment();
            } else {
                ACCEPTED_MAJOR.increment();
            }
            // Never mutate IE's SavedData from the native feature callback. IOE prepares a compensatable
            // reservation from the expedition feature and commits it only after the site survives final sanitation.
            SUPPRESSED_NATIVE_REGISTRATION.increment();
            return;
        }

        if (!eligibleTierFound) {
            REJECTED_MINOR.increment();
        } else if (!matchingProfileFound) {
            REJECTED_PROFILE.increment();
        } else if (!allowedProvinceFound) {
            REJECTED_PROVINCE.increment();
        } else {
            REJECTED_MAJOR_CHANCE.increment();
        }
    }

    public static Optional<MotherDepositRequest> guaranteedMotherRequest(
            BlockPos anchorPos,
            SiteQuality quality,
            ResourceLocation provinceId,
            BiomeMineResourceProfile resourceProfile
    ) {
        Objects.requireNonNull(anchorPos, "anchorPos");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(provinceId, "provinceId");
        Objects.requireNonNull(resourceProfile, "resourceProfile");
        if (quality != SiteQuality.MOTHERLODE) {
            return Optional.empty();
        }
        return Optional.of(new MotherDepositRequest(
                anchorPos.immutable(),
                provinceId,
                resourceProfile.profileName(),
                resourceProfile.sampledConnectedChunks()
        ));
    }

    public static Optional<MotherDepositRequest> optionalMajorRequest(
            ServerLevel level,
            BlockPos anchorPos,
            SiteQuality quality,
            ResourceLocation provinceId,
            BiomeMineResourceProfile resourceProfile
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(anchorPos, "anchorPos");
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(provinceId, "provinceId");
        Objects.requireNonNull(resourceProfile, "resourceProfile");
        if (quality != SiteQuality.RICH
                || resourceProfile.resourceKind() != BiomeMineResourceProfile.ResourceKind.GEORE) {
            return Optional.empty();
        }
        long seed = level.getSeed();
        seed = seed * 31L + level.dimension().location().hashCode();
        seed = seed * 31L + anchorPos.asLong();
        seed = seed * 31L + provinceId.hashCode();
        seed = seed * 31L + resourceProfile.profileName().hashCode();
        seed = seed * 31L + resourceProfile.sampledConnectedChunks();
        if (RandomSource.create(seed).nextInt(MAJOR_NODE_CHANCE_DENOMINATOR) != 0) {
            REJECTED_MAJOR_CHANCE.increment();
            return Optional.empty();
        }
        OPTIONAL_MAJOR_SELECTED.increment();
        return Optional.of(new MotherDepositRequest(
                anchorPos,
                provinceId,
                resourceProfile.profileName(),
                resourceProfile.sampledConnectedChunks()
        ));
    }

    public static boolean acceptsMineralMix(MotherDepositRequest request, ResourceLocation mineralMixId) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(mineralMixId, "mineralMixId");
        return COMPATIBLE_MINERAL_MIXES
                .getOrDefault(request.profileName(), Set.of())
                .contains(mineralMixId)
                && provinceAllows(request.provinceId(), mineralMixId);
    }

    public static void recordGuaranteedMotherPresent() {
        GUARANTEED_MOTHER_PRESENT.increment();
    }

    public static void recordGuaranteedMotherCreated() {
        GUARANTEED_MOTHER_CREATED.increment();
    }

    public static void recordGuaranteedMotherFailed() {
        GUARANTEED_MOTHER_FAILED.increment();
    }

    static void recordGuaranteedMotherIeAbsent() {
        GUARANTEED_MOTHER_IE_ABSENT.increment();
    }

    public static void recordGuaranteedMotherRolledBack() {
        GUARANTEED_MOTHER_ROLLED_BACK.increment();
    }

    public static void recordOptionalMajorPresent() {
        OPTIONAL_MAJOR_PRESENT.increment();
    }

    public static void recordOptionalMajorCreated() {
        OPTIONAL_MAJOR_CREATED.increment();
    }

    public static void recordOptionalMajorFailed() {
        OPTIONAL_MAJOR_FAILED.increment();
    }

    public static void recordOptionalMajorRolledBack() {
        OPTIONAL_MAJOR_ROLLED_BACK.increment();
    }

    static String statusMessage() {
        return "IOE IE Excavator gate: attempts=" + ATTEMPTS.sum()
                + ", admissibleNativeMother=" + ACCEPTED_MOTHER.sum()
                + ", admissibleNativeMajor=" + ACCEPTED_MAJOR.sum()
                + ", rejectedOutside=" + REJECTED_OUTSIDE.sum()
                + ", rejectedMinor=" + REJECTED_MINOR.sum()
                + ", rejectedProfile=" + REJECTED_PROFILE.sum()
                + ", rejectedProvince=" + REJECTED_PROVINCE.sum()
                + ", rejectedMajorChance=" + REJECTED_MAJOR_CHANCE.sum()
                + ", rejectedMotherReserved=" + REJECTED_MOTHER_RESERVED.sum()
                + ", suppressedNativeRegistration=" + SUPPRESSED_NATIVE_REGISTRATION.sum()
                + ", guaranteedMotherPresent=" + GUARANTEED_MOTHER_PRESENT.sum()
                + ", guaranteedMotherCreated=" + GUARANTEED_MOTHER_CREATED.sum()
                + ", guaranteedMotherFailed=" + GUARANTEED_MOTHER_FAILED.sum()
                + ", guaranteedMotherIeAbsent=" + GUARANTEED_MOTHER_IE_ABSENT.sum()
                + ", guaranteedMotherRolledBack=" + GUARANTEED_MOTHER_ROLLED_BACK.sum()
                + ", optionalMajorSelected=" + OPTIONAL_MAJOR_SELECTED.sum()
                + ", optionalMajorPresent=" + OPTIONAL_MAJOR_PRESENT.sum()
                + ", optionalMajorCreated=" + OPTIONAL_MAJOR_CREATED.sum()
                + ", optionalMajorFailed=" + OPTIONAL_MAJOR_FAILED.sum()
                + ", optionalMajorRolledBack=" + OPTIONAL_MAJOR_ROLLED_BACK.sum();
    }

    static void resetDiagnostics() {
        ATTEMPTS.reset();
        ACCEPTED_MOTHER.reset();
        ACCEPTED_MAJOR.reset();
        REJECTED_OUTSIDE.reset();
        REJECTED_MINOR.reset();
        REJECTED_PROFILE.reset();
        REJECTED_PROVINCE.reset();
        REJECTED_MAJOR_CHANCE.reset();
        REJECTED_MOTHER_RESERVED.reset();
        SUPPRESSED_NATIVE_REGISTRATION.reset();
        GUARANTEED_MOTHER_PRESENT.reset();
        GUARANTEED_MOTHER_CREATED.reset();
        GUARANTEED_MOTHER_FAILED.reset();
        GUARANTEED_MOTHER_IE_ABSENT.reset();
        GUARANTEED_MOTHER_ROLLED_BACK.reset();
        OPTIONAL_MAJOR_SELECTED.reset();
        OPTIONAL_MAJOR_PRESENT.reset();
        OPTIONAL_MAJOR_CREATED.reset();
        OPTIONAL_MAJOR_FAILED.reset();
        OPTIONAL_MAJOR_ROLLED_BACK.reset();
    }

    private static boolean matchesResourceProfile(
            BiomeMineResourceProfile resourceProfile,
            ResourceLocation mineralMixId
    ) {
        if (resourceProfile.resourceKind() != BiomeMineResourceProfile.ResourceKind.GEORE) {
            return false;
        }
        return COMPATIBLE_MINERAL_MIXES
                .getOrDefault(resourceProfile.profileName(), Set.of())
                .contains(mineralMixId);
    }

    private static boolean provinceAllows(ResourceLocation provinceId, ResourceLocation mineralMixId) {
        if (!IoeWorldgenConfig.provinceRuntimeIntegrationEnabled()) {
            return true;
        }
        return ProvinceResourcePolicyResolver.fromConfig()
                .evaluate(new ProvinceId(provinceId), new ResourceRef(ResourceType.ITEM, mineralMixId))
                .shouldUse();
    }

    private static boolean passesMajorNodeChance(
            ServerLevel level,
            ChunkPos generationChunk,
            ColumnPos depositPos,
            ResourceLocation mineralMixId,
            IoePendingExpeditionSites.ExcavatorRegion region
    ) {
        long seed = level.getSeed();
        seed = seed * 31L + level.dimension().location().hashCode();
        seed = seed * 31L + generationChunk.toLong();
        seed = seed * 31L + depositPos.toLong();
        seed = seed * 31L + mineralMixId.hashCode();
        seed = seed * 31L + region.anchorPos().asLong();
        seed = seed * 31L + region.resourceProfile().sampledConnectedChunks();
        return RandomSource.create(seed).nextInt(MAJOR_NODE_CHANCE_DENOMINATOR) == 0;
    }

    private static int strength(IoePendingExpeditionSites.ExcavatorRegion region) {
        return switch (region.quality()) {
            case MOTHERLODE -> 3;
            case RICH -> 2;
            case NORMAL, POOR, DRY -> 1;
        };
    }

    private static long distanceSquared(ColumnPos depositPos, net.minecraft.core.BlockPos anchorPos) {
        long dx = (long) depositPos.x() - anchorPos.getX();
        long dz = (long) depositPos.z() - anchorPos.getZ();
        return dx * dx + dz * dz;
    }

    private static Map.Entry<String, Set<ResourceLocation>> mixes(String profileName, String... mineralMixPaths) {
        Set<ResourceLocation> ids = java.util.Arrays.stream(mineralMixPaths)
                .map(path -> ResourceLocation.fromNamespaceAndPath("immersiveengineering", "mineral/" + path))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return Map.entry(profileName, ids);
    }

    public record MotherDepositRequest(
            BlockPos anchorPos,
            ResourceLocation provinceId,
            String profileName,
            int connectedBiomeChunks
    ) {
        public MotherDepositRequest {
            Objects.requireNonNull(anchorPos, "anchorPos");
            Objects.requireNonNull(provinceId, "provinceId");
            Objects.requireNonNull(profileName, "profileName");
            if (connectedBiomeChunks <= 0) {
                throw new IllegalArgumentException("Connected biome chunk count must be positive");
            }
            anchorPos = anchorPos.immutable();
        }
    }

    private enum NodeTier {
        MOTHER,
        MAJOR,
        MINOR;

        private static NodeTier from(SiteQuality quality) {
            // IOE currently stores node strength as SiteQuality; keep this adapter explicit until a dedicated
            // Mother/Major/Minor region model exists.
            return switch (quality) {
                case MOTHERLODE -> MOTHER;
                case RICH -> MAJOR;
                case NORMAL, POOR, DRY -> MINOR;
            };
        }
    }
}
