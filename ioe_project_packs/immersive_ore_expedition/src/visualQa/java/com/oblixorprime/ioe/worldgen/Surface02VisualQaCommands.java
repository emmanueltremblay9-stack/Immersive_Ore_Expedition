package com.oblixorprime.ioe.worldgen;

import com.mojang.brigadier.CommandDispatcher;
import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.surface02.Surface02BiomeFamily;
import com.oblixorprime.ioe.surface02.Surface02Composer;
import com.oblixorprime.ioe.surface02.Surface02Plan;
import com.oblixorprime.ioe.surface02.Surface02TerrainContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Explicitly gated development harness for client-side visual QA of the passive Surface 02 plans.
 *
 * <p>This command never participates in natural worldgen. Registration requires both a non-production
 * NeoForge environment and the default-false {@value #ENABLED_PROPERTY} system property.</p>
 */
@EventBusSubscriber(modid = ImmersiveOreExpeditionMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class Surface02VisualQaCommands {
    static final String ENABLED_PROPERTY = "ioe.surface02.visualQa";
    private static final String ROOT_COMMAND = "ioe_surface02_visual";
    private static final Logger LOGGER = LoggerFactory.getLogger(Surface02VisualQaCommands.class);
    private static final int FIXTURE_UPDATE_FLAGS = 3;
    private static final int ORIGIN_Y = 80;
    private static final int ORIGIN_Z = 0;
    private static final List<VisualCase> CASES = List.of(
            new VisualCase(
                    "v1",
                    SiteQuality.DRY,
                    BuddingResourceFamily.GEORE_IRON,
                    Surface02BiomeFamily.TEMPERATE,
                    0L,
                    originAtChunkX(0),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState()
            ),
            new VisualCase(
                    "v2",
                    SiteQuality.POOR,
                    BuddingResourceFamily.GEORE_IRON,
                    Surface02BiomeFamily.ARID,
                    1L,
                    originAtChunkX(2),
                    Blocks.SANDSTONE.defaultBlockState()
            ),
            new VisualCase(
                    "v3",
                    SiteQuality.NORMAL,
                    BuddingResourceFamily.GEORE_COPPER,
                    Surface02BiomeFamily.COLD,
                    -1L,
                    originAtChunkX(4),
                    Blocks.STONE.defaultBlockState()
            ),
            new VisualCase(
                    "v4",
                    SiteQuality.POOR,
                    BuddingResourceFamily.CERTUS_QUARTZ,
                    Surface02BiomeFamily.WET,
                    0x5EEDC0DEL,
                    originAtChunkX(6),
                    Blocks.MOSS_BLOCK.defaultBlockState()
            ),
            new VisualCase(
                    "v5",
                    SiteQuality.NORMAL,
                    BuddingResourceFamily.CERTUS_QUARTZ,
                    Surface02BiomeFamily.MOUNTAIN,
                    0L,
                    originAtChunkX(8),
                    Blocks.TUFF.defaultBlockState()
            )
    );

    private Surface02VisualQaCommands() {
    }

    static boolean enabled() {
        return !FMLEnvironment.production && Boolean.getBoolean(ENABLED_PROPERTY);
    }

    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        if (!enabled()) {
            return;
        }
        LOGGER.warn(
                "Surface 02 visual QA command enabled for this non-production run: /{}",
                ROOT_COMMAND
        );
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(ROOT_COMMAND)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("place")
                        .then(Commands.literal("all")
                                .executes(context -> placeAll(context.getSource()))))
                .then(Commands.literal("verify")
                        .then(Commands.literal("all")
                                .executes(context -> verifyAll(context.getSource()))))
                .then(Commands.literal("probe")
                        .then(Commands.literal("return")
                                .then(Commands.literal("all")
                                        .executes(context -> probeReturnAccess(context.getSource())))))
                .then(Commands.literal("origins")
                        .executes(context -> showOrigins(context.getSource()))));
    }

    private static int placeAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int placed = 0;
        for (VisualCase visualCase : CASES) {
            try {
                Surface02Plan plan = compose(visualCase);
                prepareFixture(level, plan, visualCase.approachFloor());
                ExpeditionSiteBlockPlan adapted = plan.toExpeditionSiteBlockPlan(
                        Surface02VisualQaCommands::resolveBlockState
                );
                IoeOrePlacementAuthorization.authorize(level.dimension(), adapted);
                var applied = IoeExpeditionPlanPlacement.apply(level, adapted);
                if (applied.isEmpty()) {
                    IoeOrePlacementAuthorization.releaseChunk(level.dimension(), new ChunkPos(plan.origin()));
                    throw new IllegalStateException("real plan placement refused the prepared fixture");
                }
                applied.orElseThrow().accept();
                String digest = digest(level, plan.origin(), adapted.blocks());
                placed++;
                send(source, visualCase.id() + " placed at " + coordinates(plan.origin())
                        + " quality=" + plan.quality()
                        + " biome=" + plan.canonicalBiomeFamily()
                        + " slots=" + plan.activeBuddingCount()
                        + " minerals=" + plan.mineralBlockCount()
                        + " digest=" + digest);
                LOGGER.info(
                        "IOE_SURFACE02_VISUAL placed case={} origin={} quality={} biome={} family={} "
                                + "activeSlots={} minerals={} digest={}",
                        visualCase.id(),
                        coordinates(plan.origin()),
                        plan.quality(),
                        plan.canonicalBiomeFamily(),
                        visualCase.family(),
                        plan.activeBuddingCount(),
                        plan.mineralBlockCount(),
                        digest
                );
            } catch (RuntimeException | LinkageError failure) {
                source.sendFailure(Component.literal(
                        visualCase.id() + " placement failed: " + failure.getMessage()
                ));
                LOGGER.error("IOE_SURFACE02_VISUAL placement failed for {}", visualCase.id(), failure);
                return 0;
            }
        }
        send(source, "Placed " + placed + "/" + CASES.size()
                + " cases. Set randomTickSpeed=0 before persistence comparison.");
        return placed;
    }

    private static int verifyAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int verified = 0;
        for (VisualCase visualCase : CASES) {
            try {
                Surface02Plan plan = compose(visualCase);
                ExpeditionSiteBlockPlan adapted = plan.toExpeditionSiteBlockPlan(
                        Surface02VisualQaCommands::resolveBlockState
                );
                int mismatches = mismatchCount(level, adapted.blocks());
                String expectedDigest = digest(plan.origin(), adapted.blocks(), adapted.blocks()::get);
                String actualDigest = digest(level, plan.origin(), adapted.blocks());
                boolean match = mismatches == 0 && expectedDigest.equals(actualDigest);
                if (match) {
                    verified++;
                }
                String message = visualCase.id()
                        + " verify match=" + match
                        + " mismatches=" + mismatches
                        + " expected=" + expectedDigest
                        + " actual=" + actualDigest;
                if (match) {
                    send(source, message);
                } else {
                    source.sendFailure(Component.literal(message));
                }
                LOGGER.info("IOE_SURFACE02_VISUAL {}", message);
            } catch (RuntimeException | LinkageError failure) {
                source.sendFailure(Component.literal(
                        visualCase.id() + " verification failed: " + failure.getMessage()
                ));
                LOGGER.error("IOE_SURFACE02_VISUAL verification failed for {}", visualCase.id(), failure);
                return 0;
            }
        }
        send(source, "Verified " + verified + "/" + CASES.size() + " cases.");
        return verified == CASES.size() ? verified : 0;
    }

    private static int probeReturnAccess(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Return access probe requires a player"));
            return 0;
        }

        int passed = 0;
        for (VisualCase visualCase : CASES) {
            BlockPos origin = visualCase.origin();
            player.connection.teleport(
                    origin.getX() + 0.5,
                    origin.getY() - Surface02Plan.MIN_DEPTH,
                    origin.getZ() + 10.5,
                    180.0F,
                    0.0F
            );
            Vec3 start = player.position();
            for (int step = 0; step < 160; step++) {
                player.move(MoverType.SELF, new Vec3(0.0, -0.08, -0.1));
            }
            Vec3 end = player.position();
            boolean pass = end.z <= origin.getZ() - 4.5
                    && end.y >= origin.getY() - 0.01;
            if (pass) {
                passed++;
            }
            String message = visualCase.id()
                    + " returnProbe pass=" + pass
                    + " start=" + coordinates(start)
                    + " end=" + coordinates(end);
            if (pass) {
                send(source, message);
            } else {
                source.sendFailure(Component.literal(message));
            }
            LOGGER.info("IOE_SURFACE02_VISUAL {}", message);
        }
        send(source, "Return access probe passed " + passed + "/" + CASES.size() + " cases.");
        return passed == CASES.size() ? passed : 0;
    }

    private static int showOrigins(CommandSourceStack source) {
        for (VisualCase visualCase : CASES) {
            BlockPos origin = visualCase.origin();
            send(source, visualCase.id() + " origin=" + coordinates(origin)
                    + " approach=/tp @s " + origin.getX() + " " + origin.getY() + " " + (origin.getZ() - 10)
                    + " 0 0");
        }
        return CASES.size();
    }

    private static Surface02Plan compose(VisualCase visualCase) {
        BlockPos origin = visualCase.origin();
        Surface02Composer.Decision decision = Surface02Composer.resolveAndCompose(
                visualCase.quality(),
                visualCase.family(),
                visualCase.seed(),
                visualCase.biome(),
                origin,
                Surface02TerrainContext.canonicalEnvelope(origin)
        );
        if (decision.status() != Surface02Composer.Status.SUPPORTED) {
            throw new IllegalStateException(
                    "composer returned " + decision.status() + ": " + decision.detail()
            );
        }
        return decision.plan().orElseThrow();
    }

    private static void prepareFixture(
            ServerLevel level,
            Surface02Plan plan,
            BlockState approachFloor
    ) {
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    setBlock(level, new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
                }
            }
        }

        int approachMinZ = plan.origin().getZ() - 12;
        int approachMaxZ = plan.origin().getZ() - 1;
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = approachMinZ; z <= approachMaxZ; z++) {
                setBlock(level, new BlockPos(x, plan.origin().getY() - 1, z), approachFloor);
                for (int y = plan.origin().getY(); y <= plan.origin().getY() + 4; y++) {
                    setBlock(level, new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static int mismatchCount(ServerLevel level, Map<BlockPos, BlockState> expected) {
        int mismatches = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            if (!level.getBlockState(entry.getKey()).equals(entry.getValue())) {
                mismatches++;
            }
        }
        return mismatches;
    }

    private static String digest(
            ServerLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> expectedPositions
    ) {
        return digest(origin, expectedPositions, level::getBlockState);
    }

    private static String digest(
            BlockPos origin,
            Map<BlockPos, BlockState> expectedPositions,
            java.util.function.Function<BlockPos, BlockState> states
    ) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(expectedPositions, "expectedPositions");
        Objects.requireNonNull(states, "states");
        List<String> lines = new ArrayList<>();
        for (BlockPos pos : expectedPositions.keySet()) {
            BlockPos relative = pos.subtract(origin);
            BlockState state = states.apply(pos);
            lines.add(relative.getX() + "," + relative.getY() + "," + relative.getZ()
                    + "=" + BuiltInRegistries.BLOCK.getKey(state.getBlock()) + state.getValues());
        }
        lines.sort(Comparator.naturalOrder());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static BlockState resolveBlockState(ResourceLocation blockId) {
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .orElseThrow(() -> new IllegalStateException("Missing runtime block " + blockId))
                .defaultBlockState();
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        level.getChunk(pos);
        if (!level.setBlock(pos, state, FIXTURE_UPDATE_FLAGS) && !level.getBlockState(pos).equals(state)) {
            throw new IllegalStateException("Could not prepare visual fixture at " + coordinates(pos));
        }
    }

    private static void send(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }

    private static BlockPos originAtChunkX(int chunkX) {
        return new BlockPos(chunkX * 16 + 7, ORIGIN_Y, ORIGIN_Z);
    }

    private static String coordinates(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String coordinates(Vec3 pos) {
        return String.format("%.3f,%.3f,%.3f", pos.x, pos.y, pos.z);
    }

    private record VisualCase(
            String id,
            SiteQuality quality,
            BuddingResourceFamily family,
            Surface02BiomeFamily biome,
            long seed,
            BlockPos origin,
            BlockState approachFloor
    ) {
        private VisualCase {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(quality, "quality");
            Objects.requireNonNull(family, "family");
            Objects.requireNonNull(biome, "biome");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(approachFloor, "approachFloor");
        }
    }
}
