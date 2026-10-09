package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.HashMap;
import java.util.List;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class StructureCollisionGameTests {
    private static BoundingBox point(BlockPos pos) {
        return new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void unavailableFootprintIsRejectedWithoutLoading(GameTestHelper helper) {
        var level = helper.getLevel(); var pos = new BlockPos(22_000_008, 64, 22_000_008);
        var chunk = new ChunkPos(pos);
        helper.assertTrue(level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null, "Fixture already loaded");
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Unknown metadata was accepted");
        helper.assertTrue(level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null, "Collision check loaded a chunk");
        helper.succeed();
    }
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void referencedBoundsAndMissingStartsAreCheckedWithoutBlockMutation(GameTestHelper helper) {
        var level = helper.getLevel();
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.withDefaultNamespace("desert_pyramid"));
        var piece = new DesertPyramidPiece(RandomSource.create(73), -24000, -24000);
        var pos = piece.getBoundingBox().getCenter();
        var target = level.getChunk(pos); // Fixture allocation only.
        target.setAllStarts(new HashMap<>()); target.setAllReferences(new HashMap<>());
        var ownerPos = new ChunkPos(target.getPos().x + 3, target.getPos().z);
        var owner = level.getChunk(ownerPos.x, ownerPos.z);
        owner.setAllStarts(new HashMap<>());
        var before = level.getBlockState(pos);
        helper.assertFalse(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Known empty footprint rejected");
        target.addReferenceForStructure(structure, ownerPos.toLong());
        owner.setStartForStructure(structure, new StructureStart(structure, ownerPos, 0, new PiecesContainer(List.of(piece))));
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Referenced overlap was missed");
        var farPiece = new DesertPyramidPiece(RandomSource.create(73), -25000, -25000);
        owner.setStartForStructure(structure, new StructureStart(structure, ownerPos, 0, new PiecesContainer(List.of(farPiece))));
        helper.assertFalse(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Stale collision result was retained");
        owner.setStartForStructure(structure, StructureStart.INVALID_START);
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Invalid referenced start was accepted");
        target.setAllReferences(new HashMap<>());
        var absent = new ChunkPos(1_375_100, 1_375_100);
        helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Missing start fixture loaded");
        target.addReferenceForStructure(structure, absent.toLong());
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level, point(pos)), "Missing referenced start was accepted");
        helper.assertTrue(level.getChunkSource().getChunkNow(absent.x, absent.z) == null, "Referenced start was force-loaded");
        helper.assertTrue(level.getBlockState(pos).equals(before), "Collision checks mutated a block");
        helper.succeed();
    }
}
