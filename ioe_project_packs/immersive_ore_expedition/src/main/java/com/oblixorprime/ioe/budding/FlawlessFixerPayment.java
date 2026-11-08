package com.oblixorprime.ioe.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Optional ExtendedAE boundary: no optional classes are linked when ExtendedAE is absent. */
public final class FlawlessFixerPayment {
    private static final String KEY = "ioe_flawless_payment";
    private FlawlessFixerPayment() { }

    public static void save(BlockEntity host, CompoundTag tag) {
        if (host.getPersistentData().contains(KEY)) tag.put(KEY, host.getPersistentData().getCompound(KEY).copy());
    }
    public static void load(BlockEntity host, CompoundTag tag) {
        host.getPersistentData().remove(KEY);
        if (tag.contains(KEY)) host.getPersistentData().put(KEY, tag.getCompound(KEY).copy());
    }

    /** Null delegates ordinary repairs to the unmodified ExtendedAE executor. */
    public static Object tick(BlockEntity host) {
        try {
            var level = host.getLevel();
            if (level == null || level.isClientSide()) return null;
            var pending = host.getPersistentData().getCompound(KEY);
            if (pending.contains("target")) {
                BlockPos oldTarget = BlockPos.of(pending.getLong("target"));
                if (level.hasChunkAt(oldTarget) && !level.getBlockState(oldTarget).toString().equals(pending.getString("state")))
                    clear(host);
            }
            Object ctx = call(host, "getContext");
            var field = ctx.getClass().getField("currentRecipe");
            field.setAccessible(true);
            RecipeHolder<?> holder = (RecipeHolder<?>) field.get(ctx);
            if (holder == null) return null; // Native recipe discovery, no payment yet.
            Object recipe = holder.value();
            Block input = (Block) call(recipe, "getInput");
            Block output = (Block) call(recipe, "getOutput");
            if (!isFlawlessAttempt(input, output)) {
                clear(host);
                return null;
            }
            call(host, "markForUpdate"); // Preserve the native tick's client synchronization.
            Object urgent = enumValue("appeng.api.networking.ticking.TickRateModulation", "URGENT");
            var front = (net.minecraft.core.Direction) call(host, "getFront");
            BlockPos target = host.getBlockPos().relative(front);
            if (!level.hasChunkAt(target)) return urgent;
            if (level.getBlockState(target).getBlock() != input) {
                clear(host); field.set(ctx, null); call(host, "setProgress", int.class, 0);
                return urgent;
            }
            String identity = holder.id() + "/" + target.asLong() + "/" + level.getBlockState(target);
            CompoundTag saved = host.getPersistentData().getCompound(KEY);
            FlawlessPayment payment;
            try {
                payment = new FlawlessPayment(saved.getString("identity"), saved.getDouble("cost"), saved.getDouble("paid"));
                if (!payment.identity().equals(identity)) throw new IllegalArgumentException("Changed target");
            } catch (IllegalArgumentException invalid) {
                payment = new FlawlessPayment(identity, FlawlessFixerConfig.COST_FE.get(), 0);
            }
            // Fuel shortage pauses the same attempt; it must not erase already paid energy.
            if (!(boolean) call(ctx, "testRecipe", RecipeHolder.class, holder)) return urgent;
            double limitFE = convert("AE", "FE", 50.0 * level.random.nextInt(2, 4));
            double requestFE = payment.request(limitFE);
            double requestAE = convert("FE", "AE", requestFE);
            double takenAE = debit(host, requestAE);
            Object managed = call(host, "getMainNode");
            Object node = call(managed, "getNode");
            if (node != null && takenAE < requestAE) {
                Object grid = call(node, "getGrid");
                takenAE += debit(call(grid, "getEnergyService"), requestAE - takenAE);
            }
            payment.credit(Math.min(requestFE, convert("AE", "FE", takenAE)));
            CompoundTag next = new CompoundTag();
            next.putString("identity", payment.identity()); next.putDouble("cost", payment.cost());
            next.putLong("target", target.asLong()); next.putString("state", level.getBlockState(target).toString());
            next.putDouble("paid", payment.paid()); host.getPersistentData().put(KEY, next);
            call(host, "setProgress", int.class, (int) Math.min(99, payment.paid() / payment.cost() * 100));
            host.setChanged();
            if (payment.complete()) {
                // Native fuel consumption and native roll, exactly once for this completed attempt.
                clear(host); call(host, "setProgress", int.class, 0); field.set(ctx, null);
                call(ctx, "runRecipe", RecipeHolder.class, holder);
                host.setChanged();
            }
            return urgent;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported ExtendedAE Crystal Fixer API; refusing an unpaid attempt", e);
        }
    }

    public static boolean isFlawlessAttempt(Block input, Block output) {
        if (input instanceof GeOreBuddingBlock a && output instanceof GeOreBuddingBlock b)
            return a.family() == b.family() && a.rank() == BuddingRank.FLAWED && b.rank() == BuddingRank.FLAWLESS;
        return BuiltInRegistries.BLOCK.getKey(input).toString().equals("ae2:flawed_budding_quartz")
                && BuiltInRegistries.BLOCK.getKey(output).toString().equals("ae2:flawless_budding_quartz");
    }
    private static void clear(BlockEntity host) {
        if (host.getPersistentData().contains(KEY)) { host.getPersistentData().remove(KEY); host.setChanged(); }
    }
    private static double debit(Object source, double requested) throws ReflectiveOperationException {
        if (requested <= 0) return 0;
        Object modulate = enumValue("appeng.api.config.Actionable", "MODULATE");
        Object one = enumValue("appeng.api.config.PowerMultiplier", "ONE");
        var method = source.getClass().getMethod("extractAEPower", double.class, modulate.getClass(), one.getClass());
        method.setAccessible(true);
        double actual = (double) method.invoke(source, requested, modulate, one);
        if (!Double.isFinite(actual) || actual < 0 || actual > requested)
            throw new IllegalStateException("Invalid actual AE debit");
        return actual;
    }
    public static double convert(String from, String to, double amount) throws ReflectiveOperationException {
        Object source = enumValue("appeng.api.config.PowerUnit", from);
        Object target = enumValue("appeng.api.config.PowerUnit", to);
        double value = (double) source.getClass().getMethod("convertTo", source.getClass(), double.class).invoke(source, target, amount);
        if (!Double.isFinite(value) || value < 0) throw new IllegalStateException("Invalid AE/FE conversion");
        return value;
    }
    private static Object enumValue(String type, String name) throws ReflectiveOperationException {
        return Class.forName(type).getField(name).get(null);
    }
    private static Object call(Object owner, String name) throws ReflectiveOperationException {
        var method = owner.getClass().getMethod(name); method.setAccessible(true); return method.invoke(owner);
    }
    private static Object call(Object owner, String name, Class<?> type, Object argument) throws ReflectiveOperationException {
        var method = owner.getClass().getMethod(name, type); method.setAccessible(true); return method.invoke(owner, argument);
    }
}
