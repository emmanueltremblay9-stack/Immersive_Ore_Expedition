package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class CatalogueTerracesGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void terracesHaveSupportedRoutesAndExactMasks(GameTestHelper helper) {
        BlockPos center = new BlockPos(7,15,7);
        for (boolean split : new boolean[]{false,true}) {
            var geometry = CatalogueTerraces.geometry(center, split);
            helper.assertTrue(geometry.equals(CatalogueTerraces.geometry(center, split)), "Unstable geometry");
            helper.assertTrue(geometry.keySet().stream().allMatch(p -> p.getX() >= 0 && p.getX() <= 14
                    && p.getZ() >= 0 && p.getZ() <= 14 && p.getY() >= 0 && p.getY() <= 18), "Escaped 15x19x15");
            for (var entry : geometry.entrySet()) if (entry.getValue().is(Blocks.LADDER)) {
                Direction facing = entry.getValue().getValue(LadderBlock.FACING);
                helper.assertTrue(solid(geometry, entry.getKey().relative(facing.getOpposite())), "Unbacked ladder");
            }
            for (int nodes : new int[]{0,3,4,5,7}) {
                var composed = new LinkedHashMap<>(geometry);
                if (nodes > 0) composed.putAll(CatalogueTerraces.nodes(center, Blocks.IRON_ORE.defaultBlockState(),
                        Blocks.BUDDING_AMETHYST.defaultBlockState(), nodes * (nodes == 7 ? 8 : nodes+2), nodes));
                helper.assertTrue(composed.values().stream().filter(s -> s.is(Blocks.BUDDING_AMETHYST)).count() == nodes,
                        "Heart budget changed");
                int ore = nodes == 0 ? 0 : nodes * (nodes == 7 ? 7 : nodes+1);
                helper.assertTrue(composed.values().stream().filter(s -> s.is(Blocks.IRON_ORE)).count() == ore,
                        "Peripheral budget changed");
                Set<BlockPos> reached = reachable(composed, CatalogueTerraces.ENTRANCE);
                helper.assertTrue(reached.contains(center), "Entrance does not reach the upper terrace");
                for (BlockPos heart : CatalogueTerraces.HEARTS) {
                    helper.assertTrue(reached.contains(heart.south()), "Unreachable alcove: " + heart + " split=" + split);
                    for (BlockPos offset : CatalogueTerraces.AIR)
                        helper.assertTrue(composed.get(heart.offset(offset)).isAir(), "Growth mask overwritten");
                }
            }
        }
        for (int[] invalid : new int[][]{{2,10},{6,42},{3,24},{7,49}}) {
            boolean rejected = false;
            try { CatalogueTerraces.nodes(center, Blocks.IRON_ORE.defaultBlockState(),
                    Blocks.BUDDING_AMETHYST.defaultBlockState(), invalid[1],invalid[0]); }
            catch (IllegalArgumentException expected) { rejected = true; }
            helper.assertTrue(rejected, "Noncanonical budget accepted");
        }
        var a = CatalogueTerraces.geometry(center,false);
        var c = CatalogueTerraces.geometry(center,true);
        helper.assertTrue(a.get(new BlockPos(5,8,8)).is(Blocks.STONE)
                && c.get(new BlockPos(5,8,8)).isAir()
                && c.get(new BlockPos(9,8,8)).isAir(), "C did not open voids on both sides of its bridge");
        helper.assertTrue(c.get(new BlockPos(7,9,7)).isAir()
                && c.get(new BlockPos(7,8,7)).is(Blocks.STONE)
                && c.get(new BlockPos(7,7,7)).isAir(), "C lost its bridge or the void beneath it");
        helper.assertTrue(a.values().stream().filter(BlockState::isAir).count()
                        != c.values().stream().filter(BlockState::isAir).count(),
                "Variants must differ beyond rotation/mirror, which preserve the air volume");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void catalogueRaccordPreservesShaftAndN3(GameTestHelper helper) {
        var plan = ExpeditionSiteBlueprints.plan(ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4,90,9), com.oblixorprime.ioe.core.SiteQuality.DRY, null, null,
                net.minecraft.util.RandomSource.create(42));
        BlockPos formerConflict = CatalogueTerraces.base(plan.chamberCenter()).offset(4,15,11);
        helper.assertTrue(plan.blocks().get(formerConflict).isAir(), "N3 east growth face was overwritten");
        helper.assertTrue(plan.connectorEnd().getY() == plan.chamberCenter().getY()+4,
                "Main shaft did not stop above the chamber roof");
        BlockPos ladder = plan.connectorEnd().south();
        helper.assertTrue(plan.blocks().get(ladder).is(Blocks.LADDER)
                && solid(plan.blocks(),ladder.south()), "Main shaft lost its ladder or backing");
        Set<BlockPos> reached = reachable(plan.blocks(),plan.connectorEnd());
        helper.assertTrue(reached.contains(CatalogueTerraces.base(plan.chamberCenter()).offset(CatalogueTerraces.ENTRANCE)),
                "The shaft does not reach the catalogue entrance");
        helper.assertTrue(reached.contains(formerConflict.west().south()), "N3 is inaccessible after raccord");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void terracePlacementCompensatesIdempotently(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(8,20,8));
        var geometry = CatalogueTerraces.geometry(center,true);
        var level = helper.getLevel();
        geometry.keySet().forEach(p -> level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState()));
        var plan = new ExpeditionSiteBlockPlan(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER, center, center,
                center, com.oblixorprime.ioe.core.SiteQuality.DRY, 0, null, null,
                java.util.List.of(center), java.util.List.of(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER), geometry);
        var applied = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
        for (var entry : geometry.entrySet()) if (entry.getValue().is(Blocks.LADDER))
            helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()), "Ladder did not survive placement");
        helper.assertTrue(applied.rollback(level) && applied.rollback(level), "Rollback was not idempotent");
        helper.assertTrue(geometry.keySet().stream().allMatch(p -> level.getBlockState(p).is(Blocks.STONE)),
                "Rollback left blueprint blocks");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void naturalCataloguePlansKeepBudgetsRoutesAndDryRewards(GameTestHelper helper) {
        var shapes = new java.util.LinkedHashMap<Integer,Long>();
        for (long seed = 0; seed < 10000 && shapes.size() < 18; seed++) {
            var random = net.minecraft.util.RandomSource.create(seed);
            int depth = random.nextInt(9);
            shapes.putIfAbsent(depth*2+(random.nextBoolean()?1:0),seed);
        }
        helper.assertTrue(shapes.size() == 18, "Both variants at every approved depth must be selectable");
        int largest = 0;
        String largestContext = "";
        for (var shape : shapes.entrySet()) for (var type : ExpeditionSiteType.naturalSurfaceSites()) {
            for (int x : new int[]{4,11}) for (int z : new int[]{6,9}) {
                BlockPos origin = new BlockPos(x-32,90,z+48);
                for (var quality : com.oblixorprime.ioe.core.SiteQuality.values()) {
                    var budget = com.oblixorprime.ioe.budding.BuddingSitePlan.forQuality(quality,
                            net.minecraft.util.RandomSource.create(101),0);
                    var plan = quality.isProductive()
                            ? IronBuddingSitePlans.plan(type,origin,budget,shape.getValue(),
                                    new ProspectorCampContext(ProspectorCampVisualFamily.TEMPERATE,shape.getValue(),false))
                            : ExpeditionSiteBlueprints.plan(type,origin,quality,null,null,
                                    net.minecraft.util.RandomSource.create(shape.getValue()));
                    if (plan.blocks().size() > largest) {
                        largest = plan.blocks().size();
                        largestContext = type + " " + quality + " depth/variant=" + shape.getKey() + " anchor=" + origin;
                    }
                    helper.assertTrue(plan.blocks().keySet().stream().allMatch(p -> (p.getX()>>4)==(origin.getX()>>4)
                            && (p.getZ()>>4)==(origin.getZ()>>4)), "Catalogue escaped source chunk");
                    helper.assertTrue(plan.isConnectedExpeditionSite(), "Catalogue escaped anchor-distance range");
                    helper.assertTrue(origin.getY()-plan.chamberCenter().getY() >= 20
                            && origin.getY()-plan.chamberCenter().getY() <= 28, "Chamber depth range changed");
                    int direction = Math.floorMod(origin.getX(),16) <= 7 ? 1 : -1;
                    int zDirection = Math.floorMod(origin.getZ(),16) <= 7 ? 1 : -1;
                    BlockPos survey = origin.offset(direction*5,-8,zDirection*3);
                    helper.assertTrue(plan.roomCenters().size()==4 && plan.roomCenters().contains(survey),
                            "Catalogue removed the survey room");
                    helper.assertTrue(plan.blocks().get(survey.offset(-direction,-2,-zDirection)).is(Blocks.CARTOGRAPHY_TABLE)
                            && plan.blocks().get(survey.offset(-direction,-2,zDirection)).is(Blocks.BOOKSHELF)
                            && plan.blocks().get(survey.offset(direction,-2,zDirection)).is(Blocks.COBWEB),
                            "Survey room functions or decoration changed");
                    var base = CatalogueTerraces.base(plan.chamberCenter());
                    helper.assertTrue(plan.blocks().get(base.offset(5,8,8)).isAir() == (shape.getKey()%2==1),
                            "Quality or resource composition changed structural variant");
                    var reached = reachable(plan.blocks(),plan.connectorEnd());
                    for (BlockPos heart : CatalogueTerraces.HEARTS) {
                        helper.assertTrue(reached.contains(base.offset(heart).south()),
                                "Unreachable composed alcove: " + type + " " + quality + " " + heart);
                        for (BlockPos offset : CatalogueTerraces.AIR)
                            helper.assertTrue(plan.blocks().get(base.offset(heart).offset(offset)).isAir(), "Composed growth mask lost");
                    }
                    helper.assertTrue(plan.oreNodeHeartCount()==budget.nodeRanks().size(), "Canonical heart count changed");
                    helper.assertTrue(plan.oreBlockCount()==budget.nodeRanks().size()+budget.totalOreBlocks(),
                            "Canonical total changed");
                    if (!quality.isProductive()) for (int residues = 0; residues <= 5; residues++) {
                        var pocket = DrySitePockets.attach(plan,
                                com.oblixorprime.ioe.budding.BuddingResourceFamily.GEORE_IRON.pocketBlockId(),residues,99);
                        var rewarded = DrySiteRewards.attach(pocket,true,99);
                        helper.assertTrue(rewarded.oreBlockCount()==residues && rewarded.oreNodeHeartCount()==0,
                                "DRY residues were lost or turned into nodes");
                        helper.assertTrue(rewarded.blockEntityPayloads().size()==plan.blockEntityPayloads().size()+1,
                                "DRY cache overwrote an existing payload");
                    }
                }
            }
        }
        System.out.println("IOE catalogue composed maximum planned blocks=" + largest + " " + largestContext);
        helper.assertTrue(largest <= 2500,"Composed catalogue exceeds unchanged cap: " + largest + " " + largestContext);
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void naturalFeatureCommitsBothCatalogueVariants(GameTestHelper helper) {
        var level = helper.getLevel();
        var profileId = net.minecraft.resources.ResourceLocation.parse("immersive_ore_expedition:certus");
        var definition = level.registryAccess().registryOrThrow(BiomeMineResourceDefinition.REGISTRY_KEY)
                .getOptional(profileId).orElseThrow();
        var profile = new BiomeMineResourceProfile(net.minecraft.resources.ResourceLocation.parse("minecraft:plains"),
                profileId,definition,1);
        int index = 0;
        for (var type : ExpeditionSiteType.naturalSurfaceSites()) for (boolean split : new boolean[]{false,true}) {
            long seed = 0;
            long planSeed = 0;
            boolean found = false;
            for (; seed < 10000; seed++) {
                var random = net.minecraft.util.RandomSource.create(seed);
                var quality = com.oblixorprime.ioe.core.SiteQualityRoll.DEFAULT.roll(random);
                planSeed = random.nextLong();
                var shapeRandom = net.minecraft.util.RandomSource.create(planSeed);
                shapeRandom.nextInt(9);
                if (quality == com.oblixorprime.ioe.core.SiteQuality.DRY && shapeRandom.nextBoolean()==split) {
                    found = true; break;
                }
            }
            helper.assertTrue(found,"No bounded deterministic fixture seed for catalogue variant");
            var chunk = new net.minecraft.world.level.ChunkPos(helper.absolutePos(new BlockPos(512+index++*128,24,512)));
            var origin = new BlockPos(chunk.getMinBlockX()+4,41,chunk.getMinBlockZ()+9);
            ExpeditionWorldgenGameTests.fillTestChunk(level,chunk);
            var feature = new ExpeditionSiteFeature(type,(ignoredLevel,ignoredPos) ->
                    new BiomeMineResourceProfile.Resolution(java.util.Optional.of(profile),BiomeMineResourceProfile.Failure.NONE));
            boolean staged = feature.place(new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(
                    java.util.Optional.empty(),level,level.getChunkSource().getGenerator(),
                    net.minecraft.util.RandomSource.create(seed),origin,
                    net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE));
            helper.assertTrue(staged,"Natural catalogue Feature.place rejected " + type + " split=" + split);
            helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level,chunk).confirmedSites()==1,
                    "Natural catalogue transaction did not commit");
            var shapeRandom = net.minecraft.util.RandomSource.create(planSeed);
            int depth = 20+shapeRandom.nextInt(9);
            var center = new BlockPos(chunk.getMinBlockX()+7,origin.getY()-depth,chunk.getMinBlockZ()+7);
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(5,8,8)).isAir()==split,
                    "Committed natural blocks do not contain the selected structural variant");
            helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level,chunk).confirmedSites()==0,
                    "Natural catalogue transaction replayed");
        }
        helper.succeed();
    }

    private static boolean open(Map<BlockPos, BlockState> blocks, BlockPos p) {
        BlockState state = blocks.get(p);
        return state != null && (state.isAir() || state.is(Blocks.LADDER));
    }
    private static boolean solid(Map<BlockPos, BlockState> blocks, BlockPos p) {
        return blocks.containsKey(p) && !open(blocks,p);
    }
    private static boolean stand(Map<BlockPos, BlockState> blocks, BlockPos p) {
        return open(blocks,p) && open(blocks,p.above()) && open(blocks,p.above(2))
                && (solid(blocks,p.below()) || blocks.get(p).is(Blocks.LADDER));
    }
    private static Set<BlockPos> reachable(Map<BlockPos, BlockState> blocks, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        var queue = new ArrayDeque<BlockPos>();
        if (stand(blocks,start)) { visited.add(start); queue.add(start); }
        while (!queue.isEmpty()) {
            BlockPos p = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = p.relative(direction);
                if (direction.getAxis().isVertical() && !blocks.get(p).is(Blocks.LADDER)
                        && !blocks.getOrDefault(next,Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)) continue;
                if (stand(blocks,next) && visited.add(next)) queue.addLast(next);
            }
        }
        return visited;
    }
}
