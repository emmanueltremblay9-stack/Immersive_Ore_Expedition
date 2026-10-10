package com.oblixorprime.ioe.discovery;

import com.mojang.authlib.GameProfile;
import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class DiscoveryJournalGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void serverJournalPersistsPrivateEvidenceWithoutChangingLocator(GameTestHelper helper) throws java.io.IOException {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ioe_journal_a"));
        var other = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ioe_journal_b"));
        var anchor = helper.absolutePos(new BlockPos(3, 3, 3));
        var clue = anchor.above(4);
        var type = ResourceLocation.parse("immersive_ore_expedition:miner_camp");
        var site = ExpeditionSite.anchor(level.dimension(), anchor, type, null, SiteQuality.MOTHERLODE,
                "natural_connected_expedition_site");
        var key = DiscoverySiteKey.from(site);
        var evidence = new DiscoveryEvidence(DiscoveryStage.EVIDENCE_DISCOVERED, clue, type, null);
        helper.assertTrue(!DiscoveryJournalService.recordVerifiedEvidence(player, key, evidence), "Unregistered site admitted");
        var debug = ExpeditionSite.anchor(level.dimension(), anchor, type, null, SiteQuality.RICH,
                ExpeditionLocatorIndex.RUNTIME_PLACEMENT_PROOF_SOURCE);
        ExpeditionLocatorService.record(level, debug);
        helper.assertTrue(!DiscoveryJournalService.recordVerifiedEvidence(player, key, evidence), "Diagnostic site admitted");
        ExpeditionLocatorService.record(level, site);
        var before = ExpeditionLocatorService.index(level).diagnosticSites();
        helper.assertTrue(!DiscoveryJournalService.recordVerifiedEvidence(player,
                new DiscoverySiteKey(Level.NETHER.location(), anchor), evidence), "Wrong dimension admitted");
        helper.assertTrue(DiscoveryJournalService.recordVerifiedEvidence(player, key, evidence), "Verified evidence not stored");
        helper.assertTrue(!DiscoveryJournalService.recordVerifiedEvidence(player, key, evidence), "Repeat reported as new stage");
        var views = DiscoveryJournalService.journal(player);
        helper.assertTrue(views.size() == 1 && views.getFirst().siteLocation().isEmpty()
                && views.getFirst().resource().isEmpty() && views.getFirst().quality().isEmpty()
                && views.getFirst().clueLocation().equals(clue), "Unearned site metadata leaked");
        helper.assertTrue(DiscoveryJournalService.journal(other).isEmpty(), "Another player received journal data");
        helper.assertTrue(before.equals(ExpeditionLocatorService.index(level).diagnosticSites()), "Journal changed locator/Compass source");
        var storage = level.getServer().overworld().getDataStorage();
        storage.save();
        net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
        var disk = storage.readTagFromDisk(DiscoveryJournalData.NAME, null,
                SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var loaded = DiscoveryJournalData.FACTORY.deserializer().apply(disk.getCompound("data"), level.registryAccess());
        storage.set(DiscoveryJournalData.NAME, loaded);
        helper.assertTrue(views.equals(DiscoveryJournalService.journal(player)), "Disk roundtrip changed evidence or ID");
        helper.assertTrue(!DiscoveryJournalService.recordVerifiedEvidence(player, key, evidence), "Reload renewed discovery notification signal");
        // Removing world-index evidence must not erase a player's historical record.
        ExpeditionLocatorService.index(level).clear();
        helper.assertTrue(views.equals(DiscoveryJournalService.journal(player)), "Historical discovery depended on live locator");
        helper.succeed();
    }
}
