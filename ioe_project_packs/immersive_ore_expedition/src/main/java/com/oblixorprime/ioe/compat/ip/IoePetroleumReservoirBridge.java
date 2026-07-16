package com.oblixorprime.ioe.compat.ip;

import com.google.common.collect.Multimap;
import com.oblixorprime.ioe.mixin.compat.ie.ip.IoeReservoirRegionDataAccessor;
import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirReservation;
import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirRules;
import com.oblixorprime.ioe.worldgen.IoePetroleumReservoirRules.PetroleumReservoirRequest;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirPolygon;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionData;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional IP bridge. It creates IP's native abstract Reservoir objects so survey, Pumpjack extraction,
 * depletion, pressure and persistence remain owned by Immersive Petroleum.
 */
public final class IoePetroleumReservoirBridge {
    private static final int MIN_RADIUS = 12;
    private static final int MAX_RADIUS = 24;

    private IoePetroleumReservoirBridge() {
    }

    public static Optional<IoePetroleumReservoirReservation> reserveReservoir(
            ServerLevel level,
            PetroleumReservoirRequest request
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(request, "request");
        IoePetroleumReservoirRules.recordReservationPrepared();
        return Optional.of(new RevalidatingReservation(level, request));
    }

    private static List<RecipeHolder<ReservoirType>> reservoirTypes(
            ServerLevel level,
            PetroleumReservoirRequest request
    ) {
        return ReservoirType.map.values().stream()
                .filter(holder -> holder.value().weight > 0)
                .filter(holder -> request.reservoirKind().defaultRecipeId().equals(holder.id()))
                .filter(holder -> request.reservoirKind().fluidId().equals(holder.value().fluidLocation))
                .filter(holder -> holder.value().getDimensions().isValid(level.dimension()))
                .filter(holder -> holder.value().getBiomes().isValid(level.getBiome(request.anchorPos())))
                .toList();
    }

    private static long reservoirCapacity(PetroleumReservoirRequest request, ReservoirType type) {
        long minimum = type.minSize;
        long maximum = Math.max(minimum, type.maxSize);
        int diameter = request.surveyRadiusChunks() * 2 + 1;
        int maximumConnected = Math.max(1, diameter * diameter);
        int connected = Math.clamp(request.connectedBiomeChunks(), 1, maximumConnected);
        if (maximumConnected == 1) {
            return maximum;
        }
        return minimum + (maximum - minimum) * (connected - 1L) / (maximumConnected - 1L);
    }

    private static int reservoirRadius(PetroleumReservoirRequest request) {
        int connectedBonus = Math.max(0, request.connectedBiomeChunks() - 1) / 7;
        return Math.clamp(MIN_RADIUS + connectedBonus, MIN_RADIUS, MAX_RADIUS);
    }

    private static ReservoirPolygon squarePolygon(ColumnPos anchor, int radius) {
        int diameter = radius * 2;
        CompoundTag tag = new CompoundTag();
        tag.putInt("xMin", anchor.x() - radius);
        tag.putInt("zMin", anchor.z() - radius);
        tag.putInt("xMax", anchor.x() + radius);
        tag.putInt("zMax", anchor.z() + radius);
        tag.putByteArray("points", new byte[]{
                0, 0,
                (byte) diameter, 0,
                (byte) diameter, (byte) diameter,
                0, (byte) diameter
        });
        return ReservoirPolygon.fromNBT(tag);
    }

    private static boolean overlapsExisting(
            ReservoirRegionDataStorage storage,
            ReservoirPolygon polygon
    ) {
        for (int x = polygon.getBoundingBox().xMin(); x <= polygon.getBoundingBox().xMax(); x++) {
            for (int z = polygon.getBoundingBox().zMin(); z <= polygon.getBoundingBox().zMax(); z++) {
                if (storage.existsAt(new ColumnPos(x, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasRequestedFluid(Reservoir reservoir, PetroleumReservoirRequest request) {
        return reservoir != null
                && reservoir.getType() != null
                && request.reservoirKind().fluidId().equals(reservoir.getType().value().fluidLocation);
    }

    private static boolean removeCreatedReservoir(
            ServerLevel level,
            ReservoirRegionDataStorage storage,
            Reservoir reservoir
    ) {
        RegionData regionData = storage.getRegionData(reservoir.getBoundingBox().getCenter());
        if (regionData == null) {
            return false;
        }
        Multimap<net.minecraft.resources.ResourceKey<Level>, Reservoir> reservoirs =
                ((IoeReservoirRegionDataAccessor) (Object) regionData).ioe$getMutableReservoirList();
        boolean removed;
        synchronized (reservoirs) {
            removed = reservoirs.remove(level.dimension(), reservoir);
            if (removed) {
                regionData.setDirty();
            }
        }
        ReservoirHandler.clearCache();
        return removed;
    }

    private static final class RevalidatingReservation implements IoePetroleumReservoirReservation {
        private final ServerLevel level;
        private final PetroleumReservoirRequest request;
        private ReservoirRegionDataStorage storage;
        private Reservoir reservoir;
        private State state = State.PREPARED;

        private RevalidatingReservation(
                ServerLevel level,
                PetroleumReservoirRequest request
        ) {
            this.level = level;
            this.request = request;
        }

        @Override
        public synchronized boolean createdByIoe() {
            return state != State.COMMITTED_EXISTING;
        }

        @Override
        public synchronized void commit() {
            if (!level.getServer().isSameThread()) {
                throw new IllegalStateException("IP reservoir commits must run on the Minecraft server thread");
            }
            if (state == State.COMMITTED_CREATED || state == State.COMMITTED_EXISTING) {
                return;
            }
            if (state != State.PREPARED) {
                throw new IllegalStateException("Cannot commit a rolled-back IP reservoir reservation");
            }

            ReservoirRegionDataStorage currentStorage = ReservoirRegionDataStorage.get();
            if (currentStorage == null) {
                IoePetroleumReservoirRules.recordReservationFailed();
                throw new IllegalStateException("Immersive Petroleum reservoir storage is unavailable");
            }
            synchronized (currentStorage) {
                ColumnPos anchor = new ColumnPos(request.anchorPos().getX(), request.anchorPos().getZ());
                Reservoir existing = currentStorage.getReservoir(level, anchor);
                if (hasRequestedFluid(existing, request)) {
                    storage = currentStorage;
                    state = State.COMMITTED_EXISTING;
                    IoePetroleumReservoirRules.recordExistingReservoirReused();
                    return;
                }
                if (existing != null) {
                    IoePetroleumReservoirRules.recordReservationFailed();
                    throw new IllegalStateException("IP reservoir region became occupied before site confirmation");
                }

                RecipeHolder<ReservoirType> reservoirType = reservoirTypes(level, request)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (reservoirType == null) {
                    IoePetroleumReservoirRules.recordReservationFailed();
                    throw new IllegalStateException(
                            "No valid Immersive Petroleum " + request.reservoirKind().serializedName()
                                    + " reservoir recipe"
                    );
                }
                int radius = reservoirRadius(request);
                ReservoirPolygon polygon = squarePolygon(anchor, radius);
                if (polygon.isEmpty() || !polygon.contains(anchor) || overlapsExisting(currentStorage, polygon)) {
                    IoePetroleumReservoirRules.recordReservationFailed();
                    throw new IllegalStateException("IP reservoir polygon is invalid or overlaps an existing reservoir");
                }
                long capacity = reservoirCapacity(request, reservoirType.value());
                Reservoir candidate = new Reservoir(polygon, reservoirType, capacity);

                state = State.COMMITTING;
                try (IoePetroleumReservoirAuthorization.Scope ignored =
                             IoePetroleumReservoirAuthorization.authorize(level.dimension(), candidate)) {
                    currentStorage.addReservoir(level.dimension(), candidate);
                    ReservoirHandler.clearCache();
                    if (currentStorage.getReservoir(level, anchor) != candidate) {
                        throw new IllegalStateException("IP reservoir storage rejected the IOE-authorized reservoir");
                    }
                    storage = currentStorage;
                    reservoir = candidate;
                    state = State.COMMITTED_CREATED;
                } catch (RuntimeException | LinkageError failure) {
                    try {
                        removeCreatedReservoir(level, currentStorage, candidate);
                    } catch (RuntimeException | LinkageError rollbackFailure) {
                        failure.addSuppressed(rollbackFailure);
                    }
                    state = State.PREPARED;
                    IoePetroleumReservoirRules.recordReservationFailed();
                    throw failure;
                }
            }
            IoePetroleumReservoirRules.recordReservoirCreated();
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Committed Immersive Petroleum {} reservoir at {} type={} capacity={} radius={} biome={} province={} connectedBiomeChunks={} quality={}",
                    request.reservoirKind().serializedName(),
                    request.anchorPos(),
                    reservoir.getType().id(),
                    reservoir.getCapacity(),
                    (reservoir.getBoundingBox().xMax() - reservoir.getBoundingBox().xMin()) / 2,
                    request.biomeId(),
                    request.provinceId(),
                    request.connectedBiomeChunks(),
                    request.quality()
            );
        }

        @Override
        public synchronized void rollback() {
            if (state == State.ROLLED_BACK) {
                return;
            }
            if (state == State.COMMITTED_CREATED || state == State.COMMITTING) {
                if (!level.getServer().isSameThread()) {
                    throw new IllegalStateException("Committed IP reservoir rollbacks must run on the Minecraft server thread");
                }
                if (storage == null || reservoir == null) {
                    state = State.ROLLED_BACK;
                    return;
                }
                synchronized (storage) {
                    if (removeCreatedReservoir(level, storage, reservoir)) {
                        IoePetroleumReservoirRules.recordReservoirRolledBack();
                    }
                }
            }
            state = State.ROLLED_BACK;
        }

        private enum State {
            PREPARED,
            COMMITTING,
            COMMITTED_CREATED,
            COMMITTED_EXISTING,
            ROLLED_BACK
        }
    }
}
