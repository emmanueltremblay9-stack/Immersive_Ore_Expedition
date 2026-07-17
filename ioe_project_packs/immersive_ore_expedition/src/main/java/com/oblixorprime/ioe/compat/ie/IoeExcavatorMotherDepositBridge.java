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
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional IE bridge loaded only when Immersive Engineering is present. It provisions one exact abstract
 * MineralVein for the confirmed site tier and leaves discovery, depletion and extraction entirely to IE.
 */
public final class IoeExcavatorMotherDepositBridge {
    private IoeExcavatorMotherDepositBridge() {
    }

    public static Optional<IoeMotherDepositReservation> reserveGuaranteedDeposit(
            ServerLevel level,
            MotherDepositRequest request
    ) {
        return reserveDeposit(level, request);
    }

    private static Optional<IoeMotherDepositReservation> reserveDeposit(
            ServerLevel level,
            MotherDepositRequest request
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(request, "request");
        Object veinLock = ExcavatorHandler.getMineralVeinList();
        synchronized (veinLock) {
            boolean observedExisting = hasCompatibleAnchoredVein(level, request);
            List<RecipeHolder<MineralMix>> candidates = MineralMix.RECIPES.getRecipes(level).stream()
                    .filter(holder -> holder.value().weight > 0)
                    .filter(holder -> IoeExcavatorDepositRules.acceptsMineralMix(request, holder.id()))
                    .toList();
            if (candidates.size() != 1) {
                IoeExcavatorDepositRules.recordGuaranteedMotherFailed();
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Unable to prepare required IE Excavator deposit at {}: profile={} tier={} province={} exactMix={} matches={}",
                        request.anchorPos(),
                        request.profileName(),
                        request.tier(),
                        request.provinceId(),
                        request.mineralMixId(),
                        candidates.size()
                );
                return Optional.empty();
            }

            RecipeHolder<MineralMix> selected = candidates.getFirst();
            int radius = request.radiusBlocks();
            MineralVein vein = new MineralVein(
                    new ColumnPos(request.anchorPos().getX(), request.anchorPos().getZ()),
                    selected.id(),
                    radius
            );
            int configuredYield = Math.max(0, ExcavatorHandler.mineralVeinYield);
            vein.setDepletion(Math.max(0, configuredYield - request.capacity()));
            return Optional.of(new RevalidatingReservation(
                    level,
                    request,
                    selected.id(),
                    radius,
                    vein,
                    observedExisting
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
            int radius
    ) {
        try {
            IoeExcavatorDepositRules.recordGuaranteedMotherCreated();
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Committed IE Excavator deposit at {} mineral={} tier={} radius={} capacity={} profile={} province={}",
                    request.anchorPos(),
                    mineralMixId,
                    request.tier(),
                    radius,
                    request.capacity(),
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
                        && vein.getPos().z() == request.anchorPos().getZ()
                        && vein.getRadius() == request.radiusBlocks());
    }

    private static final class RevalidatingReservation implements IoeMotherDepositReservation {
        private final ServerLevel level;
        private final MotherDepositRequest request;
        private final ResourceLocation mineralMixId;
        private final int radius;
        private final MineralVein vein;
        private final boolean observedExisting;
        private State state = State.PREPARED;

        private RevalidatingReservation(
                ServerLevel level,
                MotherDepositRequest request,
                ResourceLocation mineralMixId,
                int radius,
                MineralVein vein,
                boolean observedExisting
        ) {
            this.level = Objects.requireNonNull(level, "level");
            this.request = Objects.requireNonNull(request, "request");
            this.mineralMixId = Objects.requireNonNull(mineralMixId, "mineralMixId");
            this.radius = radius;
            this.vein = Objects.requireNonNull(vein, "vein");
            this.observedExisting = observedExisting;
        }

        @Override
        public synchronized boolean createdByIoe() {
            return state == State.COMMITTED_CREATED
                    || state != State.COMMITTED_EXISTING && !observedExisting;
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
                    IoeExcavatorDepositRules.recordGuaranteedMotherPresent();
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
            recordCreatedBestEffort(request, mineralMixId, radius);
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
                    IoeExcavatorDepositRules.recordGuaranteedMotherRolledBack();
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
