package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public final class IoeWorldgenRuntimeDiagnostics {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final LongAdder SITE_ATTEMPTS = new LongAdder();
    private static final LongAdder SITES_PLACED = new LongAdder();
    private static final LongAdder REMOVED_FEATURES = new LongAdder();
    private static final LongAdder GUARDED_CHUNKS = new LongAdder();
    private static final LongAdder SANITIZED_ORES = new LongAdder();
    private static final LongAdder SANITIZED_GROWTH_BLOCKS = new LongAdder();
    private static final Set<ResourceLocation> MODIFIED_BIOMES = ConcurrentHashMap.newKeySet();
    private static final EnumMap<SiteSkipReason, LongAdder> SITE_SKIPS = new EnumMap<>(SiteSkipReason.class);
    private static final List<ResourceLocation> MODIFIER_IDS = List.of(
            id("tiny_vertical_mine_entrance"),
            id("collapsed_shaft"),
            id("miner_camp"),
            id("buried_survey_marker"),
            id("remove_known_ore_features"),
            id("replace_normal_ores_with_nodes")
    );

    static {
        Arrays.stream(SiteSkipReason.values()).forEach(reason -> SITE_SKIPS.put(reason, new LongAdder()));
    }

    private IoeWorldgenRuntimeDiagnostics() {
    }

    public static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(IoeWorldgenRuntimeDiagnostics::onServerStarted);
            NeoForge.EVENT_BUS.addListener(IoeWorldgenRuntimeDiagnostics::onServerStopped);
        }
    }

    static void recordSiteAttempt() {
        SITE_ATTEMPTS.increment();
    }

    static void recordSiteSkip(SiteSkipReason reason) {
        SITE_SKIPS.get(reason).increment();
    }

    static void recordSitePlaced() {
        SITES_PLACED.increment();
    }

    static void recordModifierApplication(ResourceLocation biomeId, int removedFeatures) {
        MODIFIED_BIOMES.add(biomeId);
        if (removedFeatures > 0) {
            REMOVED_FEATURES.add(removedFeatures);
        }
    }

    static void recordGuardPass(int removedOres, int removedGrowthBlocks, boolean finalPass) {
        if (finalPass) {
            GUARDED_CHUNKS.increment();
        }
        SANITIZED_ORES.add(removedOres);
        SANITIZED_GROWTH_BLOCKS.add(removedGrowthBlocks);
    }

    public static List<String> statusMessages(MinecraftServer server) {
        RegistryAudit audit = audit(server);
        String skipSummary = SITE_SKIPS.entrySet().stream()
                .filter(entry -> entry.getValue().sum() > 0)
                .map(entry -> entry.getKey().id() + "=" + entry.getValue().sum())
                .collect(Collectors.joining(","));
        if (skipSummary.isBlank()) {
            skipSummary = "none";
        }
        return List.of(
                "IOE registry audit: features=" + audit.features() + "/" + audit.expectedFeatures()
                        + ", configured=" + audit.configuredFeatures() + "/" + audit.expectedFeatures()
                        + ", placed=" + audit.placedFeatures() + "/" + audit.expectedFeatures()
                        + ", biomeModifiers=" + audit.biomeModifiers() + "/" + audit.expectedModifiers()
                        + ", mineResourceProfiles=" + audit.mineResourceProfiles(),
                "IOE worldgen application: modifiedBiomes=" + MODIFIED_BIOMES.size()
                        + ", dynamicallyRemovedPlacedFeatures=" + REMOVED_FEATURES.sum()
                        + ", authorizedResourcePositions=" + IoeOrePlacementAuthorization.positionCount(),
                "IOE site outcomes: attempts=" + SITE_ATTEMPTS.sum()
                        + ", placed=" + SITES_PLACED.sum()
                        + ", skips=" + skipSummary,
                "IOE new-chunk guard: checked=" + GUARDED_CHUNKS.sum()
                        + ", sanitizedOres=" + SANITIZED_ORES.sum()
                        + ", sanitizedGrowthBlocks=" + SANITIZED_GROWTH_BLOCKS.sum(),
                IoeExcavatorDepositRules.statusMessage()
        );
    }

    private static RegistryAudit audit(MinecraftServer server) {
        Registry<Feature<?>> featureRegistry = server.registryAccess().registryOrThrow(Registries.FEATURE);
        Registry<ConfiguredFeature<?, ?>> configuredRegistry =
                server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        Registry<PlacedFeature> placedRegistry = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Registry<BiomeModifier> modifierRegistry =
                server.registryAccess().registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
        Registry<BiomeMineResourceDefinition> mineProfileRegistry =
                server.registryAccess().registryOrThrow(BiomeMineResourceDefinition.REGISTRY_KEY);
        List<ResourceLocation> featureIds = ExpeditionSiteType.registeredFeatureIds();
        return new RegistryAudit(
                count(featureRegistry, featureIds),
                count(configuredRegistry, featureIds),
                count(placedRegistry, featureIds),
                count(modifierRegistry, MODIFIER_IDS),
                (int) mineProfileRegistry.stream().count(),
                featureIds.size(),
                MODIFIER_IDS.size()
        );
    }

    private static int count(Registry<?> registry, List<ResourceLocation> ids) {
        return (int) ids.stream().filter(registry::containsKey).count();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, path);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        statusMessages(event.getServer()).forEach(IoeExpeditionWorldgenMod.LOGGER::info);
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        IoePendingExpeditionSites.clear();
        IoeOrePlacementAuthorization.clear();
        IoeNewChunkOreGuard.clearPending();
        SITE_ATTEMPTS.reset();
        SITES_PLACED.reset();
        REMOVED_FEATURES.reset();
        GUARDED_CHUNKS.reset();
        SANITIZED_ORES.reset();
        SANITIZED_GROWTH_BLOCKS.reset();
        IoeExcavatorDepositRules.resetDiagnostics();
        MODIFIED_BIOMES.clear();
        SITE_SKIPS.values().forEach(LongAdder::reset);
    }

    public enum SiteSkipReason {
        CONFIG_DISABLED("config_disabled"),
        STANDALONE_CHAMBER_FORBIDDEN("standalone_chamber_forbidden"),
        SURFACE_UNSUITABLE("surface_unsuitable"),
        AE2_RESOURCE_MISSING("ae2_resource_missing"),
        EXTENDED_AE_RESOURCE_MISSING("extended_ae_resource_missing"),
        PROFILE_MISSING("profile_missing"),
        PROFILE_AMBIGUOUS("profile_ambiguous"),
        RESOURCE_POLICY_DENIED("resource_policy_denied"),
        IE_DEPOSIT_MISSING("ie_deposit_missing"),
        DISCONNECTED_PLAN("disconnected_plan"),
        UNSAFE_WRITE("unsafe_write");

        private final String id;

        SiteSkipReason(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private record RegistryAudit(
            int features,
            int configuredFeatures,
            int placedFeatures,
            int biomeModifiers,
            int mineResourceProfiles,
            int expectedFeatures,
            int expectedModifiers
    ) {
    }
}
