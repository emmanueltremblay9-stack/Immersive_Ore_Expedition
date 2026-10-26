package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JournalConsultationTest {
    @Test void indexedPaginationIsPrivateBoundedAndStableAfterReload() {
        var data = new DiscoveryJournalData();
        var a = UUID.randomUUID(); var b = UUID.randomUUID();
        for (int i = 0; i < 30; i++) data.advance(i % 2 == 0 ? a : b,
                new DiscoverySiteKey(Level.OVERWORLD.location(), new BlockPos(i, 20, 0)),
                new DiscoveryEvidence(DiscoveryStage.EVIDENCE_DISCOVERED, new BlockPos(i, 70, 0),
                        ResourceLocation.parse("immersive_ore_expedition:miner_camp"), null));
        var saved = data.save(new CompoundTag(), null);
        data = DiscoveryJournalData.FACTORY.deserializer().apply(saved, null);
        assertEquals(15, data.page(a, 0).total());
        assertEquals(14, data.page(a, Integer.MAX_VALUE).offset());
        assertEquals(28, data.page(a, Integer.MAX_VALUE).entry().orElseThrow().clueLocation().getX());
        assertEquals(1, data.page(b, 0).entry().orElseThrow().clueLocation().getX());
        assertEquals(0, data.page(UUID.randomUUID(), 100).total());
        assertEquals(saved, data.save(new CompoundTag(), null));
        var finalData = data;
        assertThrows(IllegalArgumentException.class, () -> finalData.page(a, -1));
    }

    @Test void responseCodecRoundTripsEveryStageAndNeverSerializesUnearnedFields() {
        for (var stage : DiscoveryStage.values()) {
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
            try {
                var view = new DiscoveryView(UUID.randomUUID(), stage, Level.OVERWORLD.location(),
                        ResourceLocation.parse("immersive_ore_expedition:miner_camp"), new BlockPos(1, 70, 2),
                        Optional.of(new BlockPos(19, 20, 31)), Optional.of(ResourceLocation.parse("minecraft:diamond")), Optional.of(SiteQuality.MOTHERLODE));
                JournalResponse.CODEC.encode(buffer, new JournalResponse(99, new DiscoveryPage(0, 1, Optional.of(view))));
                assertTrue(buffer.readableBytes() < 1024);
                var decoded = JournalResponse.CODEC.decode(buffer);
                var visible = decoded.page().entry().orElseThrow();
                assertEquals(99, decoded.token());
                assertEquals(stage.ordinal() >= 1, visible.siteLocation().isPresent());
                assertEquals(stage.ordinal() >= 2, visible.resource().isPresent());
                assertEquals(stage.ordinal() >= 3, visible.quality().isPresent());
                assertEquals(0, buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }

    @Test void requestAndResponseRejectInvalidBoundsAndOversizedIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new JournalRequest(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new DiscoveryPage(1, 0, Optional.empty()));
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            JournalRequest.CODEC.encode(buf, new JournalRequest(7, Integer.MAX_VALUE));
            assertEquals(new JournalRequest(7, Integer.MAX_VALUE), JournalRequest.CODEC.decode(buf));
            buf.clear(); buf.writeLong(1); buf.writeVarInt(-1); buf.writeVarInt(1);
            assertThrows(IllegalArgumentException.class, () -> JournalResponse.CODEC.decode(buf));
            buf.clear();
            var view = new DiscoveryView(UUID.randomUUID(), DiscoveryStage.EVIDENCE_DISCOVERED, Level.OVERWORLD.location(),
                    ResourceLocation.parse("ioe:" + "a".repeat(300)), BlockPos.ZERO, Optional.empty(), Optional.empty(), Optional.empty());
            assertThrows(RuntimeException.class, () -> JournalResponse.CODEC.encode(buf,
                    new JournalResponse(1, new DiscoveryPage(0, 1, Optional.of(view)))));
        } finally { buf.release(); }
    }

    @Test void disconnectAndOutOfOrderResponsesCannotRestoreAnotherSession() {
        var cache = new JournalSession();
        var first = cache.request(0); var second = cache.request(1);
        var empty = new DiscoveryPage(0, 0, Optional.empty());
        assertFalse(cache.accept(new JournalResponse(first.token(), empty)));
        assertTrue(cache.accept(new JournalResponse(second.token(), empty)));
        assertFalse(cache.accept(new JournalResponse(second.token(), empty)));
        cache.clear();
        assertTrue(cache.page().isEmpty());
        var next = cache.request(0);
        assertFalse(cache.accept(new JournalResponse(second.token(), empty)));
        assertTrue(cache.accept(new JournalResponse(next.token(), empty)));
    }

    @Test void compiledCommonBridgeAndGenericClientContainNoIeLinkage() throws Exception {
        for (var type : new Class<?>[]{JournalNetworking.class, JournalRequest.class, JournalResponse.class, DiscoveryJournalService.class}) {
            try (var stream = type.getResourceAsStream(type.getSimpleName()+".class")) {
                var bytes = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
                assertFalse(bytes.contains("blusunrize/"));
                assertFalse(bytes.contains("net/minecraft/client/"));
            }
        }
        // Inspect client bytes without loading client classes in the dedicated-server/unit environment.
        Path root = Path.of(System.getProperty("ioe.moduleProjectDir", "."), "build/classes/java/main");
        String client = Files.readString(root.resolve("com/oblixorprime/ioe/discovery/client/JournalClient.class"), StandardCharsets.ISO_8859_1);
        assertFalse(client.contains("blusunrize/"));
        assertTrue(client.contains("immersiveengineering"));
        assertTrue(client.contains("FMLLoadCompleteEvent"));
        assertTrue(client.contains("LoggingOut"));
        assertTrue(Files.exists(root.resolve("com/oblixorprime/ioe/discovery/client/ie/JournalManualEntry.class")));
    }
}
