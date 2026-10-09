package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
public final class StructureCollisionGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void deferredCampRejectsStructureAddedAfterStaging(GameTestHelper helper) {
        var level = helper.getLevel();
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.withDefaultNamespace("desert_pyramid"));
        var piece = new DesertPyramidPiece(RandomSource.create(73), -32000, -32000);
        var pos = piece.getBoundingBox().getCenter();
        var target = level.getChunk(pos); // Controlled fixture allocation, never production loading.
        var ownerPos = new ChunkPos(target.getPos().x + 3, target.getPos().z);
        var owner = level.getChunk(ownerPos.x, ownerPos.z);
        target.setAllStarts(new HashMap<>());
        target.setAllReferences(new HashMap<>());
        owner.setAllStarts(new HashMap<>());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        var plan = new ExpeditionSiteBlockPlan(IoeWorldgenFeatureKeys.MINER_CAMP, pos, pos, pos,
                com.oblixorprime.ioe.core.SiteQuality.DRY, 0, null, null,
                List.of(), List.of(), Map.of(pos, Blocks.OAK_PLANKS.defaultBlockState()));
        var bounds = ExpeditionSiteFeature.expandedPlanBounds(plan.blocks().keySet());
        helper.assertFalse(LoadedStructureCollision.blocksPlacement(level, bounds), "Initial fixture is not admissible");
        var id = ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "iron");
        var profile = new BiomeMineResourceProfile(ResourceLocation.withDefaultNamespace("plains"), id,
                level.registryAccess().registryOrThrow(BiomeMineResourceDefinition.REGISTRY_KEY).get(id), 1);
        int[] calls = new int[2];
        helper.assertTrue(IoePendingExpeditionSites.stage(level, plan, profile, new IoeMotherDepositReservation() {
            public boolean createdByIoe() { return false; }
            public void commit() { calls[0]++; }
            public void rollback() { calls[1]++; }
        }), "Fixture plan was not staged");

        // Only metadata changes: air stays replaceable, so material/BE/fluid checks cannot catch the conflict.
        target.addReferenceForStructure(structure, ownerPos.toLong());
        owner.setStartForStructure(structure, new StructureStart(structure, ownerPos, 0, new PiecesContainer(List.of(piece))));
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level, bounds), "Late reference does not overlap fixture");
        var result = IoePendingExpeditionSites.confirmLoadedChunk(level, target.getPos());
        helper.assertTrue(result.confirmedSites() == 0 && result.rejectedSites() == 1,
                "Deferred camp accepted a structure added after staging");
        helper.assertTrue(level.getBlockState(pos).isAir(), "Deferred camp wrote inside newly protected bounds");
        helper.assertTrue(calls[0] == 0 && calls[1] == 1, "Rejected camp committed or retained its reservation");
        helper.assertTrue(com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService.index(level).sites().stream()
                .noneMatch(site -> site.pos().equals(pos)), "Rejected camp gained locator provenance");
        helper.succeed();
    }

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
