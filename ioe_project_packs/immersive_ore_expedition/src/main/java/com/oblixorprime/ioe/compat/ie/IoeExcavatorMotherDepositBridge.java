package com.oblixorprime.ioe.compat.ie;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.common.IESaveData;
import com.oblixorprime.ioe.worldgen.IoeExcavatorDepositRules;
import com.oblixorprime.ioe.worldgen.IoeExcavatorDepositRules.MotherDepositRequest;
import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import com.oblixorprime.ioe.worldgen.IoeMotherDepositReservation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional IE bridge loaded only when Immersive Engineering is present. It provisions a normal abstract
 * MineralVein for a confirmed Mother Node and leaves discovery, depletion and extraction entirely to IE.
 */
public final class IoeExcavatorMotherDepositBridge {
    private static final int MINIMUM_RADIUS = 12;
    private static final int RADIUS_VARIATION = 32;

    private IoeExcavatorMotherDepositBridge() {
    }

    public static Optional<IoeMotherDepositReservation> reserveGuaranteedDeposit(
            ServerLevel level,
            MotherDepositRequest request
    ) {
        return reserveDeposit(level, request, true);
    }

    public static Optional<IoeMotherDepositReservation> reserveOptionalDeposit(
            ServerLevel level,
            MotherDepositRequest request
    ) {
        return reserveDeposit(level, request, false);
    }

    private static Optional<IoeMotherDepositReservation> reserveDeposit(
            ServerLevel level,
            MotherDepositRequest request,
            boolean requiredForSiteQuality
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(request, "request");
        Object veinLock = ExcavatorHandler.getMineralVeinList();
        synchronized (veinLock) {
            boolean observedExisting = hasCompatibleAnchoredVein(level, request);
            List<RecipeHolder<MineralMix>> candidates = MineralMix.RECIPES.getRecipes(level).stream()
                    .filter(holder -> holder.value().weight > 0)
                    .filter(holder -> IoeExcavatorDepositRules.acceptsMineralMix(request, holder.id()))
                    .sorted(Comparator.comparing(holder -> holder.id().toString()))
                    .toList();
            if (candidates.isEmpty()) {
                if (requiredForSiteQuality) {
                    IoeExcavatorDepositRules.recordGuaranteedMotherFailed();
                } else {
                    IoeExcavatorDepositRules.recordOptionalMajorFailed();
                }
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Unable to prepare {} IE Excavator deposit at {}: profile={} province={} has no loaded compatible mineral mix",
                        requiredForSiteQuality ? "required Mother" : "optional Major",
                        request.anchorPos(),
                        request.profileName(),
                        request.provinceId()
                );
                return Optional.empty();
            }

            RandomSource random = RandomSource.create(seed(level, request));
            RecipeHolder<MineralMix> selected = selectWeighted(candidates, random);
            int radius = MINIMUM_RADIUS + random.nextInt(RADIUS_VARIATION);
            MineralVein vein = new MineralVein(
                    new ColumnPos(request.anchorPos().getX(), request.anchorPos().getZ()),
                    selected.id(),
                    radius
            );
            if (ExcavatorHandler.initialVeinDepletion > 0) {
                vein.setDepletion((int) (
                        ExcavatorHandler.mineralVeinYield
                                * random.nextDouble()
                                * ExcavatorHandler.initialVeinDepletion
                ));
            }
            return Optional.of(new RevalidatingReservation(
                    level,
                    request,
                    selected.id(),
                    radius,
                    vein,
                    observedExisting,
                    requiredForSiteQuality
            ));
        }
    }

    private static void rollbackVein(ServerLevel level, MineralVein vein, boolean markDirty) {
        Object veinLock = ExcavatorHandler.getMineralVeinList();
        synchronized (veinLock) {
            if (ExcavatorHandler.getMineralVeinList().remove(level.dimension(), vein)) {
                ExcavatorHandler.resetCache();
                if (markDirty) {
                    IESaveData.markInstanceDirty();
                }
            }
        }
    }

    private static void recordCreatedBestEffort(
            MotherDepositRequest request,
            net.minecraft.resources.ResourceLocation mineralMixId,
            int radius,
            boolean requiredForSiteQuality
    ) {
        try {
            if (requiredForSiteQuality) {
                IoeExcavatorDepositRules.recordGuaranteedMotherCreated();
            } else {
                IoeExcavatorDepositRules.recordOptionalMajorCreated();
            }
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Committed {} IE Excavator deposit at {} mineral={} radius={} profile={} province={}",
                    requiredForSiteQuality ? "required Mother" : "optional Major",
                    request.anchorPos(),
                    mineralMixId,
                    radius,
                    request.profileName(),
                    request.provinceId()
            );
        } catch (RuntimeException | LinkageError ignored) {
            // The native IE vein is already durable. Diagnostics must not turn a successful guarantee into rejection.
        }
    }

    private static boolean hasCompatibleAnchoredVein(ServerLevel level, MotherDepositRequest request) {
        return ExcavatorHandler.getMineralVeinList().get(level.dimension()).stream()
                .filter(vein -> IoeExcavatorDepositRules.acceptsMineralMix(request, vein.getMineralName()))
                .anyMatch(vein -> vein.getPos().x() == request.anchorPos().getX()
                        && vein.getPos().z() == request.anchorPos().getZ());
    }

    private static RecipeHolder<MineralMix> selectWeighted(
            List<RecipeHolder<MineralMix>> candidates,
            RandomSource random
    ) {
        long totalWeight = candidates.stream().mapToLong(holder -> holder.value().weight).sum();
        long remaining = Math.floorMod(random.nextLong(), totalWeight);
        for (RecipeHolder<MineralMix> candidate : candidates) {
            remaining -= candidate.value().weight;
            if (remaining < 0) {
                return candidate;
            }
        }
        throw new IllegalStateException("Weighted IE mineral selection did not choose a candidate");
    }

    private static long seed(ServerLevel level, MotherDepositRequest request) {
        long seed = level.getSeed();
        seed = mix(seed ^ level.dimension().location().hashCode());
        seed = mix(seed ^ request.anchorPos().asLong());
        seed = mix(seed ^ request.provinceId().hashCode());
        seed = mix(seed ^ request.profileName().hashCode());
        return mix(seed ^ request.connectedBiomeChunks());
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    private static final class RevalidatingReservation implements IoeMotherDepositReservation {
        private final ServerLevel level;
        private final MotherDepositRequest request;
        private final ResourceLocation mineralMixId;
        private final int radius;
        private final MineralVein vein;
        private final boolean observedExisting;
        private final boolean requiredForSiteQuality;
        private State state = State.PREPARED;

        private RevalidatingReservation(
                ServerLevel level,
                MotherDepositRequest request,
                ResourceLocation mineralMixId,
                int radius,
                MineralVein vein,
                boolean observedExisting,
                boolean requiredForSiteQuality
        ) {
            this.level = Objects.requireNonNull(level, "level");
            this.request = Objects.requireNonNull(request, "request");
            this.mineralMixId = Objects.requireNonNull(mineralMixId, "mineralMixId");
            this.radius = radius;
            this.vein = Objects.requireNonNull(vein, "vein");
            this.observedExisting = observedExisting;
            this.requiredForSiteQuality = requiredForSiteQuality;
        }

        @Override
        public synchronized boolean createdByIoe() {
            return state == State.COMMITTED_CREATED
                    || state != State.COMMITTED_EXISTING && !observedExisting;
        }

        @Override
        public boolean requiredForSiteQuality() {
            return requiredForSiteQuality;
        }

        @Override
        public synchronized void commit() {
            if (!level.getServer().isSameThread()) {
                throw new IllegalStateException("IE deposit commits must run on the Minecraft server thread");
            }
            if (state == State.COMMITTED_CREATED || state == State.COMMITTED_EXISTING) {
                return;
            }
            if (state != State.PREPARED) {
                throw new IllegalStateException("Cannot commit a rolled-back IE deposit reservation");
            }

            Object veinLock = ExcavatorHandler.getMineralVeinList();
            synchronized (veinLock) {
                if (hasCompatibleAnchoredVein(level, request)) {
                    state = State.COMMITTED_EXISTING;
                    if (requiredForSiteQuality) {
                        IoeExcavatorDepositRules.recordGuaranteedMotherPresent();
                    } else {
                        IoeExcavatorDepositRules.recordOptionalMajorPresent();
                    }
                    return;
                }
                state = State.COMMITTING;
                try {
                    ExcavatorHandler.addVein(level.dimension(), vein);
                    IESaveData.markInstanceDirty();
                    state = State.COMMITTED_CREATED;
                } catch (RuntimeException | LinkageError failure) {
                    try {
                        rollbackVein(level, vein, false);
                        state = State.PREPARED;
                    } catch (RuntimeException | LinkageError rollbackFailure) {
                        failure.addSuppressed(rollbackFailure);
                    }
                    throw failure;
                }
            }
            recordCreatedBestEffort(request, mineralMixId, radius, requiredForSiteQuality);
        }

        @Override
        public synchronized void rollback() {
            if (state == State.ROLLED_BACK) {
                return;
            }
            if (state == State.COMMITTED_CREATED || state == State.COMMITTING) {
                boolean committed = state == State.COMMITTED_CREATED;
                rollbackVein(level, vein, committed);
                if (committed) {
                    if (requiredForSiteQuality) {
                        IoeExcavatorDepositRules.recordGuaranteedMotherRolledBack();
                    } else {
                        IoeExcavatorDepositRules.recordOptionalMajorRolledBack();
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
