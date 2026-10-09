package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingBlockIdentity;
import com.oblixorprime.ioe.budding.GeOreBuddingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class BuddingReindexGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void canonicalHeartsRecoverOnlySignedMinesWithoutInventedNodes(GameTestHelper helper)
            throws java.io.IOException {
        var level = helper.getLevel();
        BlockPos heartPos = helper.absolutePos(new BlockPos(4, 5, 4));
        BlockPos entrance = heartPos.offset(2, 0, 0);
        BlockPos campfire = entrance.offset(3, 0, 3);
        level.setBlock(entrance.east(), Blocks.LADDER.defaultBlockState()
                .setValue(LadderBlock.FACING, Direction.WEST), 2);
        level.setBlock(entrance.offset(0, 0, 1), Blocks.OAK_TRAPDOOR.defaultBlockState(), 2);
        var hearts = BuiltInRegistries.BLOCK.stream()
                .filter(BuddingBlockIdentity::isCanonical).toList();
        helper.assertTrue(hearts.stream().anyMatch(block -> block instanceof GeOreBuddingBlock),
                "No IOE GeOre hearts exercised");
        for (var heart : hearts) {
            ExpeditionLocatorService.index(level).clear();
            level.setBlock(heartPos, heart.defaultBlockState(), 2);
            level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 2);
            var unsigned = ExpeditionLocatorReindexer.scanLoadedChunks(level, heartPos, 0);
            helper.assertTrue(unsigned.growthBlocks() == 1 && unsigned.recordedSites() == 0,
                    "A lone heart must be recognized but must not invent a mine");
            level.setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState(), 2);
            var recovered = ExpeditionLocatorReindexer.scanLoadedChunks(level, heartPos, 0);
            helper.assertTrue(recovered.recordedSites() == 1,
                    "Canonical heart not recovered: " + BuiltInRegistries.BLOCK.getKey(heart));
            var site = ExpeditionLocatorService.index(level).sites().getFirst();
            helper.assertTrue(site.pos().equals(entrance) && site.buddingNodes().isEmpty()
                    && ExpeditionLocatorService.index(level).buddingNodeAt(level.dimension(), heartPos).isEmpty(),
                    "Recovery changed the entrance or invented original node provenance");
            helper.assertTrue(ExpeditionLocatorReindexer.scanLoadedChunks(level, heartPos, 0).recordedSites() == 0,
                    "Reindex duplicated an existing site");
            helper.assertTrue(level.getBlockState(heartPos).equals(heart.defaultBlockState())
                    && level.getBlockState(campfire).is(Blocks.CAMPFIRE), "Reindex mutated the world");
        }
        BuddingPersistenceRuntimeChecks.reloadFromDisk(level);
        helper.assertTrue(ExpeditionLocatorService.index(level).sites().size() == 1
                && ExpeditionLocatorService.index(level).sites().getFirst().buddingNodes().isEmpty(),
                "Recovered anchor/provenance changed after disk reload");
        helper.succeed();
    }
}
