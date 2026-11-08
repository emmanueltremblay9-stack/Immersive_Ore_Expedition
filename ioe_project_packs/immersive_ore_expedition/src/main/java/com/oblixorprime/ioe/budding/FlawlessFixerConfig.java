package com.oblixorprime.ioe.budding;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FlawlessFixerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue COST_FE;
    static {
        var builder = new ModConfigSpec.Builder();
        COST_FE = builder.comment("Total FE per Flawed-to-Flawless Crystal Fixer attempt; ordinary repairs are unchanged.",
                "An attempt snapshots this cost when payment starts. Fuel and native success chance remain applicable.")
                .defineInRange("flawlessAttemptCostFE", 1_000_000, 1, Integer.MAX_VALUE);
        SPEC = builder.build();
    }
    private FlawlessFixerConfig() { }
}
