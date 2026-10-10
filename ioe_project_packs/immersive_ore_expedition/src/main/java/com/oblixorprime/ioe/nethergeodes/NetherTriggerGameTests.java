package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherTriggerGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void closedTickGateAndExplicitDispatchUseActualLoadReceiptOnce(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var candidate = NetherSitePlanner.candidate(level.getSeed(), -1100, -1100);
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        var chunk = new ChunkPos(origin);
        level.getChunk(chunk.x, chunk.z); // Fixture creates the actual lifecycle receipt.
        helper.runAfterDelay(1, () -> {
            var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
            helper.assertTrue(ledger.resultAt(origin).isEmpty(), "Disabled tick gate admitted a region");
            // Other GameTests share this server; dispatch only this fixture's genuine receipt.
            var selected = NetherPlacementRuntime.coordinator(level)
                    .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount())
                    .stream().filter(chunk::equals).toList();
            var result = NetherNaturalTrigger.dispatchCandidates(level, selected).get(chunk.toLong());
            helper.assertTrue(result != null && result != NetherPlacementCoordinator.Result.NOT_FRESH,
                    "Trigger did not use the actual receipt: " + result);
            helper.assertTrue(ledger.resultAt(origin).isPresent(), "Dispatch did not persist its refusal");
            helper.assertFalse(NetherPlacementRuntime.coordinator(level)
                    .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount()).contains(chunk), "Trigger dispatched twice");
            helper.succeed();
        });
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void nativeStructureMetadataChangeBlocksPreparedWrite(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(Level.NETHER);
        var piece = new DesertPyramidPiece(RandomSource.create(1), -310008, -310008);
        var pos = piece.getBoundingBox().getCenter();
        var chunk = level.getChunk(pos);
        chunk.setAllStarts(new HashMap<>()); chunk.setAllReferences(new HashMap<>());
        var rock = Blocks.NETHERRACK.defaultBlockState(); level.setBlock(pos, rock, 2);
        helper.assertFalse(NetherNativeProtection.protectedAt(level, pos, rock), "Empty native metadata blocked fixture");
        helper.assertTrue(NetherNativeProtection.protectedAt(level, pos, Blocks.CHEST.defaultBlockState()), "Block entity unprotected");
        var plan = new NetherPlacementCoordinator.Plan(pos, Map.of(pos, rock), Map.of(pos, Blocks.NETHER_QUARTZ_ORE.defaultBlockState()));
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.withDefaultNamespace("fortress"));
        // Synthetic structure metadata tests a native bounding-box check, not real fortress generation.
        chunk.setStartForStructure(structure, new StructureStart(structure, chunk.getPos(), 0, new PiecesContainer(List.of(piece))));
        helper.assertTrue(NetherSnapshotDiagnostic.source(level).read(chunk, pos).protectedBlock(), "Capture omitted native protection");
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(NetherPlacementRuntime.commitPrepared(level, plan) == NetherPlacementCoordinator.Result.TERRAIN_CHANGED,
                    "Changed native protection allowed placement");
            helper.assertTrue(level.getBlockState(pos).equals(rock), "Rejected placement mutated terrain");
            chunk.setAllStarts(new HashMap<>());
            var absent = new ChunkPos(1_300_000, 1_300_000);
            chunk.addReferenceForStructure(structure, absent.toLong());
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Absent fixture loaded");
            helper.assertTrue(NetherNativeProtection.protectedAt(level, pos, rock), "Unknown native protection accepted");
            helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Protection forced a chunk");
            helper.succeed();
        });
    }
}
