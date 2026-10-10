package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/** Short-range observation of surviving surface clues at confirmed natural sites. */
public final class ProximityDiscovery {
    static final double RANGE = 8;
    static final double MIN_LOOK_DOT = 0.5; // sixty degrees from the look direction
    static final int INTERVAL = 20;
    static final int PLAYERS_PER_TICK = 8;
    private static final Map<ServerPlayer, Integer> LAST_SCAN = new WeakHashMap<>();
    private static int cursor;

    private ProximityDiscovery() { }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ProximityDiscovery::tick);
        NeoForge.EVENT_BUS.addListener(ProximityDiscovery::stop);
    }

    private static void stop(ServerStoppedEvent event) { LAST_SCAN.clear(); cursor = 0; }

    private static void tick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        int size = players.size();
        if (size == 0) { cursor = 0; return; }
        int now = event.getServer().getTickCount();
        for (int i = 0; i < Math.min(size, PLAYERS_PER_TICK); i++) {
            if (cursor >= size) cursor = 0;
            ServerPlayer player = players.get(cursor++);
            Integer last = LAST_SCAN.get(player);
            if (last != null && now - last >= 0 && now - last < INTERVAL) continue;
            LAST_SCAN.put(player, now);
            scan(player, player::sendSystemMessage);
        }
    }

    /** Same production scan, with a notice sink allowing headless delivery assertions. */
    static boolean scan(ServerPlayer player, Consumer<Component> notices) {
        if (!player.isAlive() || player.isSpectator()) return false;
        var level = player.serverLevel();
        var data = DiscoveryJournalService.data(level.getServer());
        for (ExpeditionSite site : ExpeditionLocatorService.index(level)
                .nearbyDiscoveryAnchors(level.dimension(), player.blockPosition())) {
            var key = DiscoverySiteKey.from(site);
            if (data.contains(player.getUUID(), key) || site.anchorId().isEmpty()
                    || !site.anchorId().get().getNamespace().equals("immersive_ore_expedition")) continue;
            // Witness offsets are at most four blocks from the confirmed surface anchor.
            if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(site.pos())) > 13 * 13) continue;
            for (BlockPos witness : witnesses(site)) {
                if (!loadedBetween(player, witness) || !survivingClue(player, site, witness)
                        || !visible(player, witness)) continue;
                if (DiscoveryJournalService.recordVerifiedEvidence(player, key,
                        new DiscoveryEvidence(DiscoveryStage.EVIDENCE_DISCOVERED, witness,
                                site.anchorId().orElseThrow(), null))) {
                    notices.accept(notice(ModList.get().isLoaded("immersiveengineering")));
                    return true; // at most one private notice per scan
                }
            }
        }
        return false;
    }

    static Component notice(boolean ie) {
        return Component.translatable(ie ? "journal.ioe.discovery.manual" : "journal.ioe.discovery.standalone")
                .append(" ").append(Component.translatable("journal.ioe.discovery.open")
                        .withStyle(style -> style.withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ioejournal"))));
    }

    static List<BlockPos> witnesses(ExpeditionSite site) {
        BlockPos p = site.pos();
        return switch (site.anchorId().orElseThrow().getPath()) {
            case "tiny_vertical_mine_entrance" -> List.of(p.above(4));
            case "collapsed_shaft" -> List.of(p.offset(-3, 3, 2));
            case "buried_survey_marker" -> List.of(p.offset(-2, 1, 0));
            case "miner_camp" -> List.of(p.offset(0, 0, 1), p);
            default -> List.of();
        };
    }

    static boolean survivingClue(ServerPlayer player, ExpeditionSite site, BlockPos p) {
        var level = player.serverLevel();
        // Check both witness and its structural neighbor, never just an index entry.
        if (!level.hasChunkAt(p) || !level.hasChunkAt(p.below()) || !level.hasChunkAt(p.west())) return false;
        var block = level.getBlockState(p);
        return switch (site.anchorId().orElseThrow().getPath()) {
            case "tiny_vertical_mine_entrance" -> block.is(Blocks.LANTERN)
                    && level.getBlockState(p.below()).is(Blocks.OAK_PLANKS);
            case "collapsed_shaft" -> block.is(Blocks.OAK_PLANKS)
                    && level.getBlockState(p.below()).is(Blocks.STRIPPED_OAK_LOG);
            case "buried_survey_marker" -> block.is(Blocks.STONE_BRICK_WALL)
                    && level.getBlockState(p.below()).is(Blocks.CHISELED_STONE_BRICKS);
            case "miner_camp" -> block.is(Blocks.OAK_TRAPDOOR)
                    && level.getBlockState(p.west()).is(BlockTags.PLANKS);
            default -> false;
        };
    }

    private static boolean loadedBetween(ServerPlayer player, BlockPos target) {
        Vec3 eye = player.getEyePosition();
        if (eye.distanceToSqr(Vec3.atCenterOf(target)) > RANGE * RANGE) return false;
        // Include a one-block halo for ray traversal endpoint epsilon and edge shapes.
        int minX = (Math.min(player.blockPosition().getX(), target.getX()) - 1) >> 4;
        int maxX = (Math.max(player.blockPosition().getX(), target.getX()) + 1) >> 4;
        int minZ = (Math.min(player.blockPosition().getZ(), target.getZ()) - 1) >> 4;
        int maxZ = (Math.max(player.blockPosition().getZ(), target.getZ()) + 1) >> 4;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            if (!player.serverLevel().getChunkSource().hasChunk(x, z)) return false;
        return true;
    }

    private static boolean visible(ServerPlayer player, BlockPos target) {
        var level = player.serverLevel();
        var shape = level.getBlockState(target).getCollisionShape(level, target);
        if (shape.isEmpty()) return false;
        Vec3 eye = player.getEyePosition();
        Vec3 center = Vec3.atLowerCornerOf(target).add(shape.bounds().getCenter());
        if (player.getLookAngle().dot(center.subtract(eye).normalize()) < MIN_LOOK_DOT) return false;
        var hit = player.serverLevel().clip(new ClipContext(eye, center,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        return hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(target);
    }
}
