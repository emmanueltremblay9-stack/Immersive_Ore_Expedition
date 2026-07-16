package com.oblixorprime.ioe.compat.ie;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.common.IESaveData;
import com.oblixorprime.ioe.worldgen.IoeExcavatorDepositRules;
import com.oblixorprime.ioe.worldgen.IoeExcavatorDepositRules.MotherDepositRequest;
import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Optional IE bridge loaded only when Immersive Engineering is present. It provisions a normal abstract
 * MineralVein for a confirmed Mother Node and leaves discovery, depletion and extraction entirely to IE.
 */
public final class IoeExcavatorMotherDepositBridge {
    private static final int MINIMUM_RADIUS = 12;
    private static final int RADIUS_VARIATION = 32;

    private IoeExcavatorMotherDepositBridge() {
    }

    public static boolean ensureGuaranteedDeposit(ServerLevel level, MotherDepositRequest request) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(request, "request");
        Object veinLock = ExcavatorHandler.getMineralVeinList();
        synchronized (veinLock) {
            if (hasCompatibleAnchoredVein(level, request)) {
                IoeExcavatorDepositRules.recordGuaranteedMotherPresent();
                return true;
            }

            List<RecipeHolder<MineralMix>> candidates = MineralMix.RECIPES.getRecipes(level).stream()
                    .filter(holder -> holder.value().weight > 0)
                    .filter(holder -> IoeExcavatorDepositRules.acceptsMineralMix(request, holder.id()))
                    .sorted(Comparator.comparing(holder -> holder.id().toString()))
                    .toList();
            if (candidates.isEmpty()) {
                IoeExcavatorDepositRules.recordGuaranteedMotherFailed();
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Unable to guarantee IE Excavator deposit for Mother Node at {}: profile={} province={} has no loaded compatible mineral mix",
                        request.anchorPos(),
                        request.profileName(),
                        request.provinceId()
                );
                return false;
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
            try {
                ExcavatorHandler.addVein(level.dimension(), vein);
                IESaveData.markInstanceDirty();
            } catch (RuntimeException | LinkageError failure) {
                rollbackVein(level, vein, failure);
                throw failure;
            }
            recordCreatedBestEffort(request, selected.id(), radius);
            return true;
        }
    }

    private static void rollbackVein(ServerLevel level, MineralVein vein, Throwable originalFailure) {
        try {
            if (ExcavatorHandler.getMineralVeinList().remove(level.dimension(), vein)) {
                ExcavatorHandler.resetCache();
                IESaveData.markInstanceDirty();
            }
        } catch (RuntimeException | LinkageError rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
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
                    "Guaranteed IE Excavator deposit for Mother Node at {} mineral={} radius={} profile={} province={}",
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
}
