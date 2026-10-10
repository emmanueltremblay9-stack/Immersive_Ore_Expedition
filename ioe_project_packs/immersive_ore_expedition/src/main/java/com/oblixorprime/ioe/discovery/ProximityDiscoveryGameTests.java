package com.oblixorprime.ioe.discovery;

import com.mojang.authlib.GameProfile;
import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.UUID;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class ProximityDiscoveryGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void actualBlueprintsRetainRecognizableSurfaceWitnesses(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ioe_signature"));
        BlockPos anchor = helper.absolutePos(new BlockPos(6, 4, 6));
        for (var type : com.oblixorprime.ioe.worldgen.ExpeditionSiteType.naturalSurfaceSites()) {
            for (SiteQuality quality : SiteQuality.values()) {
                var plan = com.oblixorprime.ioe.worldgen.ExpeditionSiteBlueprints.plan(type, anchor, quality,
                        ResourceLocation.parse("minecraft:iron_ore"), Blocks.IRON_ORE.defaultBlockState(),
                        net.minecraft.util.RandomSource.create(19));
                var site = ExpeditionSite.anchor(level.dimension(), anchor, type.id(), null, quality,
                        "natural_connected_expedition_site");
                boolean found = false;
                for (BlockPos witness : ProximityDiscovery.witnesses(site)) {
                    for (BlockPos pos : java.util.List.of(witness, witness.below(), witness.west()))
                        level.setBlockAndUpdate(pos, plan.blocks().getOrDefault(pos, Blocks.AIR.defaultBlockState()));
                    found |= ProximityDiscovery.survivingClue(player, site, witness);
                }
                helper.assertTrue(found, "Actual blueprint lacks clue witness: " + type + ":" + quality);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void visibleClueDiscoversPrivatelyOnceAcrossReload(GameTestHelper helper) throws java.io.IOException {
        var level = helper.getLevel();
        var a = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ioe_observer_a"));
        var b = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ioe_observer_b"));
        BlockPos anchor = helper.absolutePos(new BlockPos(6, 3, 6));
        BlockPos clue = anchor.offset(-2, 1, 0);
        // Controlled terrain, not evidence of naturally encountered worldgen.
        for (BlockPos p : BlockPos.betweenClosed(clue.offset(-2, -1, -5), clue.offset(2, 3, 1)))
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(clue.below(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(clue, Blocks.STONE_BRICK_WALL.defaultBlockState());
        a.moveTo(clue.getX() + .5, clue.getY() - 1, clue.getZ() - 3.5, 0, 0);
        b.moveTo(a.getX(), a.getY(), a.getZ(), 0, 0);
        var notices = new ArrayList<Component>();
        var type = ResourceLocation.parse("immersive_ore_expedition:buried_survey_marker");
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Unregistered clue discovered");
        ExpeditionLocatorService.record(level, ExpeditionSite.anchor(level.dimension(), anchor, type, null,
                SiteQuality.RICH, ExpeditionLocatorIndex.RUNTIME_PLACEMENT_PROOF_SOURCE));
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Diagnostic clue discovered");
        var site = ExpeditionSite.anchor(level.dimension(), anchor, type, null,
                SiteQuality.MOTHERLODE, "natural_connected_expedition_site");
        ExpeditionLocatorService.record(level, site);
        a.setYRot(180);
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Clue behind player discovered");
        a.setYRot(0);
        BlockPos obstruction = clue.offset(0, 0, -2);
        level.setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Through-wall discovery");
        level.setBlockAndUpdate(obstruction, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(clue, Blocks.AIR.defaultBlockState());
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Destroyed clue discovered");
        level.setBlockAndUpdate(clue, Blocks.STONE_BRICK_WALL.defaultBlockState());
        a.moveTo(clue.getX() + .5, clue.getY() - 1, clue.getZ() - 9, 0, 0);
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add), "Distant clue discovered");
        a.moveTo(b.getX(), b.getY(), b.getZ(), 0, 0);
        helper.assertTrue(ProximityDiscovery.scan(a, notices::add) && notices.size() == 1, "Visible clue not notified once");
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add) && notices.size() == 1, "Repeat notification");
        helper.assertTrue(DiscoveryJournalService.journal(b).isEmpty(), "Other player's journal changed");
        var view = DiscoveryJournalService.journal(a).getFirst();
        helper.assertTrue(view.stage() == DiscoveryStage.EVIDENCE_DISCOVERED && view.clueLocation().equals(clue)
                && view.siteLocation().isEmpty() && view.resource().isEmpty() && view.quality().isEmpty(), "Hidden stage or data leaked");
        var otherNotices = new ArrayList<Component>();
        helper.assertTrue(ProximityDiscovery.scan(b, otherNotices::add) && otherNotices.size() == 1,
                "Independent player could not discover clue");
        var storage = level.getServer().overworld().getDataStorage();
        storage.save();
        net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
        var disk = storage.readTagFromDisk(DiscoveryJournalData.NAME, null,
                SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        storage.set(DiscoveryJournalData.NAME, DiscoveryJournalData.FACTORY.deserializer()
                .apply(disk.getCompound("data"), level.registryAccess()));
        helper.assertTrue(!ProximityDiscovery.scan(a, notices::add) && notices.size() == 1,
                "Ordinary save/reload repeated notification");
        helper.assertTrue(DiscoveryJournalService.journal(a).getFirst().equals(view), "Reload changed private record");
        for (boolean ie : new boolean[]{false, true}) {
            var notice = ProximityDiscovery.notice(ie);
            helper.assertTrue(notice.getSiblings().getLast().getStyle().getClickEvent().getValue().equals("/ioejournal"),
                    "Missing IE-independent journal action");
        }
        helper.succeed();
    }
}
