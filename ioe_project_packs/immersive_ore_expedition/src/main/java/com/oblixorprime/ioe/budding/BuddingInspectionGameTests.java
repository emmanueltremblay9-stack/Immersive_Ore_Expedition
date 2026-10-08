package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class BuddingInspectionGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void jadeShowsActualRankAndCommittedSiteMetadata(GameTestHelper helper) throws ReflectiveOperationException {
        if (!ModList.get().isLoaded("jade")) {
            helper.succeed();
            return;
        }
        Class.forName("com.oblixorprime.ioe.compat.jade.JadeRuntimeChecks")
                .getMethod("run", GameTestHelper.class).invoke(null, helper);
    }
}
