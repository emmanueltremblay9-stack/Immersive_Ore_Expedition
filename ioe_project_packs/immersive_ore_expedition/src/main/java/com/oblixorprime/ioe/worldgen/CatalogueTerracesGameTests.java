package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
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
        for (var variant : CatalogueTerraces.Variant.values()) {
            var geometry = CatalogueTerraces.geometry(center, variant);
            helper.assertTrue(geometry.equals(CatalogueTerraces.geometry(center, variant)), "Unstable geometry");
            helper.assertTrue(geometry.keySet().stream().allMatch(p -> p.getX() >= 0 && p.getX() <= 14
                    && p.getZ() >= 0 && p.getZ() <= 14 && p.getY() >= 0 && p.getY() <= 18), "Escaped 15x19x15");
            for (var entry : geometry.entrySet()) if (entry.getValue().is(Blocks.LADDER)) {
                Direction facing = entry.getValue().getValue(LadderBlock.FACING);
                helper.assertTrue(solid(geometry, entry.getKey().relative(facing.getOpposite())), "Unbacked ladder");
            }
            for (var entry : geometry.entrySet()) if (entry.getValue().is(Blocks.STONE_STAIRS)) {
                helper.assertTrue(solid(geometry,entry.getKey().below()),"Stair relies on unplanned terrain support");
                helper.assertTrue(stand(geometry,entry.getKey().above()),"Stair lost three-high clearance: " + variant + " " + entry.getKey());
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
                    helper.assertTrue(reached.contains(heart.south()), "Unreachable alcove: " + heart + " variant=" + variant);
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
        var a = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.ALTERNATING);
        var c = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.SPLIT);
        helper.assertTrue(a.get(new BlockPos(5,8,8)).is(Blocks.STONE)
                && c.get(new BlockPos(5,8,8)).isAir()
                && c.get(new BlockPos(9,8,8)).isAir(), "C did not open voids on both sides of its bridge");
        helper.assertTrue(c.get(new BlockPos(7,9,7)).isAir()
                && c.get(new BlockPos(7,8,7)).is(Blocks.STONE)
                && c.get(new BlockPos(7,7,7)).isAir(), "C lost its bridge or the void beneath it");
        helper.assertTrue(a.values().stream().filter(BlockState::isAir).count()
                        != c.values().stream().filter(BlockState::isAir).count(),
                "Variants must differ beyond rotation/mirror, which preserve the air volume");
        var b = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.FAN);
        for (int y = 3; y <= 17; y++) for (int x = 8; x <= 9; x++) for (int z = 8; z <= 9; z++)
            helper.assertTrue(b.get(new BlockPos(x,y,z)).isAir(), "B lost its common three-terrace void");
        helper.assertTrue(a.get(new BlockPos(8,8,9)).is(Blocks.STONE)
                && c.get(new BlockPos(8,8,9)).is(Blocks.STONE), "B signature is shared by another variant");
        long bAir = b.values().stream().filter(BlockState::isAir).count();
        helper.assertTrue(bAir != a.values().stream().filter(BlockState::isAir).count()
                && bAir != c.values().stream().filter(BlockState::isAir).count(),
                "B must differ volumetrically from A and C");
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
        var level = helper.getLevel();
        for (var variant : CatalogueTerraces.Variant.values()) {
        var geometry = CatalogueTerraces.geometry(center,variant);
        geometry.keySet().forEach(p -> level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState()));
        var plan = new ExpeditionSiteBlockPlan(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER, center, center,
                center, com.oblixorprime.ioe.core.SiteQuality.DRY, 0, null, null,
                java.util.List.of(center), java.util.List.of(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER), geometry);
        var applied = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
        for (var entry : geometry.entrySet()) if (entry.getValue().is(Blocks.LADDER) || entry.getValue().is(Blocks.STONE_STAIRS))
            helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()), "Route support changed during placement");
        helper.assertTrue(applied.rollback(level) && applied.rollback(level), "Rollback was not idempotent");
        helper.assertTrue(geometry.keySet().stream().allMatch(p -> level.getBlockState(p).is(Blocks.STONE)),
                "Rollback left blueprint blocks");
        if (variant.isRails() || variant.isEight() || variant==CatalogueTerraces.Variant.PEARLS_SPLIT) {
            var again = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
            var foreign = geometry.entrySet().stream().filter(e -> e.getValue().is(Blocks.STONE_STAIRS) || variant.isEight() && e.getValue().is(Blocks.LADDER))
                    .findFirst().orElseThrow().getKey();
            level.setBlockAndUpdate(foreign,Blocks.DIAMOND_BLOCK.defaultBlockState());
            again.rollback(level);
            helper.assertTrue(level.getBlockState(foreign).is(Blocks.DIAMOND_BLOCK),
                    "Compensation overwrote a foreign route replacement");
            helper.assertTrue(geometry.keySet().stream().filter(p -> !p.equals(foreign))
                    .allMatch(p -> level.getBlockState(p).is(Blocks.STONE)),
                    "Foreign route edit prevented compensation of owned cells");
            level.setBlockAndUpdate(foreign,Blocks.STONE.defaultBlockState());
        }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void naturalCataloguePlansKeepBudgetsRoutesAndDryRewards(GameTestHelper helper) {
        int forms = CatalogueTerraces.Variant.values().length;
        var shapes = new java.util.LinkedHashMap<Integer,Long>();
        for (long seed = 0; seed < 10000 && shapes.size() < 9*forms; seed++) {
            var random = net.minecraft.util.RandomSource.create(seed);
            int depth = random.nextInt(9);
            shapes.putIfAbsent(depth*forms+random.nextInt(forms),seed);
        }
        helper.assertTrue(shapes.size() == 9*forms, "All catalogue variants at every approved depth must be selectable");
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
                    var variant = CatalogueTerraces.Variant.values()[shape.getKey()%forms];
                    if (variant.isEight()) assertEightSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isPearls()) assertPearlSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isRails()) assertRailsSignatures(helper,plan.blocks(),base,variant);
                    else {
                    helper.assertTrue(plan.blocks().getOrDefault(base.offset(5,8,8),Blocks.STONE.defaultBlockState()).isAir() == (shape.getKey()%forms==2),
                            "Quality or resource composition changed structural variant");
                    helper.assertTrue(plan.blocks().getOrDefault(base.offset(8,8,9),Blocks.STONE.defaultBlockState()).isAir() == (shape.getKey()%forms==1),
                            "Quality or composition changed the shared-void variant");
                    helper.assertTrue(plan.blocks().get(base.offset(7,9,7)).is(Blocks.STONE) == (shape.getKey()%forms>=3),
                            "Crossing lost its solid central separation");
                    helper.assertTrue(plan.blocks().getOrDefault(base.offset(7,8,8),Blocks.STONE.defaultBlockState()).isAir()
                            == (shape.getKey()%forms==4), "Window signature changed during composition");
                    helper.assertTrue(plan.blocks().getOrDefault(base.offset(2,12,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                            == (shape.getKey()%forms>=5), "Loop/entry-column signature changed during composition");
                    boolean siphon = shape.getKey()%forms>=6;
                    helper.assertTrue(plan.blocks().get(base.offset(8,15,7)).is(Blocks.STONE) == siphon,
                            "Siphon upper banks were joined");
                    if (siphon) {
                        helper.assertTrue(plan.blocks().get(base.offset(7,3,6)).is(Blocks.STONE) == (shape.getKey()%forms==7),
                                "Siphon elbow split changed during composition");
                        helper.assertTrue(plan.blocks().getOrDefault(base.offset(12,12,7),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                                == (shape.getKey()%forms==8), "Siphon shoulder changed during composition");
                    }
                    }
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
        for (var type : ExpeditionSiteType.naturalSurfaceSites()) for (var variant : CatalogueTerraces.Variant.values()) {
            long seed = 0;
            long planSeed = 0;
            boolean found = false;
            for (; seed < 10000; seed++) {
                var random = net.minecraft.util.RandomSource.create(seed);
                var quality = com.oblixorprime.ioe.core.SiteQualityRoll.DEFAULT.roll(random);
                planSeed = random.nextLong();
                var shapeRandom = net.minecraft.util.RandomSource.create(planSeed);
                shapeRandom.nextInt(9);
                if (quality == com.oblixorprime.ioe.core.SiteQuality.DRY && shapeRandom.nextInt(CatalogueTerraces.Variant.values().length)==variant.ordinal()) {
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
            helper.assertTrue(staged,"Natural catalogue Feature.place rejected " + type + " variant=" + variant);
            helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level,chunk).confirmedSites()==1,
                    "Natural catalogue transaction did not commit");
            var shapeRandom = net.minecraft.util.RandomSource.create(planSeed);
            int depth = 20+shapeRandom.nextInt(9);
            var center = new BlockPos(chunk.getMinBlockX()+7,origin.getY()-depth,chunk.getMinBlockZ()+7);
            if (variant.isRails() || variant.isPearls() || variant.isEight()) {
                var actual = new LinkedHashMap<BlockPos,BlockState>();
                CatalogueTerraces.geometry(center,variant).keySet().forEach(p -> actual.put(p,level.getBlockState(p)));
                if (variant.isEight()) assertEightSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isPearls()) assertPearlSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else assertRailsSignatures(helper,actual,CatalogueTerraces.base(center),variant);
            } else {
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(5,8,8)).isAir()==(variant==CatalogueTerraces.Variant.SPLIT),
                    "Committed natural blocks do not contain the selected structural variant");
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(8,8,9)).isAir()
                    == (variant==CatalogueTerraces.Variant.FAN), "Committed shared-void signature changed");
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(7,9,7)).is(Blocks.STONE)
                    == (variant.isCrossing() || variant.isSiphon()), "Committed bank separation changed");
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(7,8,8)).isAir()
                    == (variant==CatalogueTerraces.Variant.CROSSING_WINDOW), "Committed window signature changed");
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(2,12,6)).is(Blocks.LADDER)
                    == (variant==CatalogueTerraces.Variant.CROSSING_LOOP || variant.isSiphon()), "Committed loop/entry-column signature changed");
            helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(8,15,7)).is(Blocks.STONE)
                    == variant.isSiphon(), "Committed siphon bank separation changed");
            if (variant.isSiphon()) {
                helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(7,3,6)).is(Blocks.STONE)
                        == (variant==CatalogueTerraces.Variant.SIPHON_SPLIT), "Committed siphon elbow changed");
                helper.assertTrue(level.getBlockState(CatalogueTerraces.base(center).offset(12,12,7)).is(Blocks.LADDER)
                        == (variant==CatalogueTerraces.Variant.SIPHON_SHOULDER), "Committed siphon shoulder changed");
            }
            }
            helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level,chunk).confirmedSites()==0,
                    "Natural catalogue transaction replayed");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void crossingKeepsSeparateAxesAndOneLateralConnection(GameTestHelper helper) {
        var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.CROSSING);
        for (int x = 3; x <= 11; x++) helper.assertTrue(stand(geometry,new BlockPos(x,15,7)),
                "Upper east-west gallery is not supported and three-high");
        for (int z = 3; z <= 11; z++) helper.assertTrue(stand(geometry,new BlockPos(7,3,z)),
                "Lower north-south gallery is not supported and three-high");
        for (int y = 8; y <= 9; y++) for (int x = 6; x <= 8; x++) for (int z = 6; z <= 8; z++)
            helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Crossing diaphragm was perforated");
        helper.assertTrue(reachable(geometry,CatalogueTerraces.ENTRANCE).contains(new BlockPos(7,3,7)),
                "The lateral connection does not reach the lower axis");
        var severed = new LinkedHashMap<>(geometry);
        for (int x = 11; x <= 12; x++) for (int z = 6; z <= 7; z++)
            severed.put(new BlockPos(x,12,z),Blocks.STONE.defaultBlockState());
        helper.assertTrue(!reachable(severed,CatalogueTerraces.ENTRANCE).contains(new BlockPos(7,3,7)),
                "A second connection bypassed the single lateral route");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void crossingLoopKeepsTwoIndependentOpposedConnections(GameTestHelper helper) {
        var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.CROSSING_LOOP);
        for (int cutY : new int[]{6,12}) {
            for (int cut : new int[]{1,2,3}) {
                var severed = new LinkedHashMap<>(geometry);
                for (int z = 6; z <= 7; z++) {
                    if ((cut & 1) != 0) for (int x = 11; x <= 12; x++)
                        severed.put(new BlockPos(x,cutY,z),Blocks.STONE.defaultBlockState());
                    if ((cut & 2) != 0) for (int x = 2; x <= 3; x++)
                        severed.put(new BlockPos(x,cutY,z),Blocks.STONE.defaultBlockState());
                }
                helper.assertTrue(reachable(severed,CatalogueTerraces.ENTRANCE).contains(new BlockPos(7,3,7)) == (cut != 3),
                        "The loop did not preserve exactly two opposed connections: y=" + cutY + " cut=" + cut);
            }
        }
        for (int y = 8; y <= 9; y++) for (int x = 6; x <= 8; x++) for (int z = 6; z <= 8; z++)
            helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Loop opened the central diaphragm");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void crossingWindowHasSightlineGuardAndShiftedLandings(GameTestHelper helper) {
        var center = helper.absolutePos(new BlockPos(8,20,8));
        var base = CatalogueTerraces.base(center);
        var geometry = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.CROSSING_WINDOW);
        var level = helper.getLevel();
        geometry.keySet().forEach(p -> level.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState()));
        var plan = new ExpeditionSiteBlockPlan(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER,center,center,center,
                com.oblixorprime.ioe.core.SiteQuality.DRY,0,null,null,java.util.List.of(center),
                java.util.List.of(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER),geometry);
        var applied = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
        helper.assertTrue(level.getBlockState(base.offset(12,9,8)).is(Blocks.LADDER)
                && level.getBlockState(base.offset(2,9,5)).is(Blocks.LADDER),"Window variant lost its shifted landings");
        for (int x = 3; x <= 11; x++) helper.assertTrue(stand(geometry,base.offset(x,15,7)),
                "Window removed the supported upper crossing");
        for (int y = 3; y <= 14; y++) helper.assertTrue(level.getBlockState(base.offset(7,y,8)).isAir(),
                "Window does not expose the lower gallery");
        BlockPos guard = base.offset(7,15,8);
        var playerBox = new net.minecraft.world.phys.AABB(base.getX()+7.2,base.getY()+15,base.getZ()+7.2,
                base.getX()+7.8,base.getY()+16.8,base.getZ()+7.8);
        double allowed = level.getBlockState(guard).getCollisionShape(level,guard)
                .move(guard.getX(),guard.getY(),guard.getZ()).collide(Direction.Axis.Z,playerBox,1.0);
        helper.assertTrue(allowed > 0.0 && allowed < 1.0,"Window guard permits walking straight into the slit");
        var eye = new net.minecraft.world.phys.Vec3(base.getX()+6.94,base.getY()+16.62,base.getZ()+7.99);
        var target = new net.minecraft.world.phys.Vec3(base.getX()+7.5,base.getY()+2.5,base.getZ()+8.5);
        var observer = net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes(
                        "ioe_window_guard".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"ioe_window"));
        observer.setPos(eye.x,base.getY()+15,eye.z);
        var hit = level.clip(new net.minecraft.world.level.ClipContext(eye,target,
                net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,observer));
        helper.assertTrue(hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && hit.getBlockPos().equals(base.offset(7,2,8)),"Guarded visual slit does not reveal the lower floor");
        helper.assertTrue(applied.rollback(level) && applied.rollback(level),"Window placement did not compensate idempotently");
        helper.assertTrue(geometry.keySet().stream().allMatch(p -> level.getBlockState(p).is(Blocks.STONE)),
                "Window compensation left neighbor-dependent blocks");
        var foreign = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
        level.setBlockAndUpdate(guard,Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.assertTrue(foreign.rollback(level) && level.getBlockState(guard).is(Blocks.DIAMOND_BLOCK),
                "Ordered compensation overwrote a foreign edit");
        level.setBlockAndUpdate(guard,Blocks.STONE.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void drySiphonsRequireTheLowerElbowInBothDirections(GameTestHelper helper) {
        BlockPos exit = new BlockPos(11,15,4);
        for (var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.SIPHON,
                CatalogueTerraces.Variant.SIPHON_SPLIT,CatalogueTerraces.Variant.SIPHON_SHOULDER}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            helper.assertTrue(reachable(geometry,CatalogueTerraces.ENTRANCE).contains(exit)
                    && reachable(geometry,exit).contains(CatalogueTerraces.ENTRANCE),"Siphon is not bidirectional: " + variant);
            helper.assertTrue(geometry.values().stream().allMatch(state -> state.getFluidState().isEmpty()),
                    "Dry siphon added a fluid");
            var blocked = new LinkedHashMap<>(geometry);
            for (int y = 3; y <= 5; y++) for (int z = 1; z <= 13; z++)
                blocked.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            helper.assertTrue(!reachable(blocked,CatalogueTerraces.ENTRANCE).contains(exit)
                    && !reachable(blocked,exit).contains(CatalogueTerraces.ENTRANCE),
                    "Siphon bypasses its lower elbow: " + variant);
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void siphonVariantsKeepSplitElbowAndOffsetShoulder(GameTestHelper helper) {
        BlockPos center = new BlockPos(7,15,7);
        BlockPos exit = new BlockPos(11,15,4);
        var split = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.SIPHON_SPLIT);
        for (int cut : new int[]{1,2,3}) {
            var blocked = new LinkedHashMap<>(split);
            for (int y = 3; y <= 5; y++) {
                if ((cut & 1) != 0) for (int z = 4; z <= 5; z++)
                    blocked.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
                if ((cut & 2) != 0) for (int z = 8; z <= 9; z++)
                    blocked.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            }
            helper.assertTrue(reachable(blocked,CatalogueTerraces.ENTRANCE).contains(exit) == (cut != 3)
                    && reachable(blocked,exit).contains(CatalogueTerraces.ENTRANCE) == (cut != 3),
                    "Split elbow did not retain exactly two lower channels: " + cut);
        }
        var shoulder = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.SIPHON_SHOULDER);
        var plain = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.SIPHON);
        helper.assertTrue(shoulder.get(new BlockPos(8,9,7)).isAir()
                && !plain.getOrDefault(new BlockPos(8,9,7),Blocks.STONE.defaultBlockState()).isAir(),
                "C did not widen its lateral shoulder");
        helper.assertTrue(shoulder.get(new BlockPos(12,12,7)).is(Blocks.LADDER)
                && !shoulder.get(new BlockPos(12,12,4)).is(Blocks.LADDER),"C did not offset its upper return conduit");
        var blocked = new LinkedHashMap<>(shoulder);
        for (int x = 11; x <= 12; x++) for (int z = 7; z <= 8; z++)
            blocked.put(new BlockPos(x,12,z),Blocks.STONE.defaultBlockState());
        helper.assertTrue(!reachable(blocked,CatalogueTerraces.ENTRANCE).contains(exit),
                "C retained a straight upper shortcut around its elbow");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void railsHaveThreeRealBidirectionalCrossbars(GameTestHelper helper) {
        for (var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.RAILS,
                CatalogueTerraces.Variant.RAILS_SLOPES,CatalogueTerraces.Variant.RAILS_FORK}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertRailsSignatures(helper,geometry,BlockPos.ZERO,variant);
            for (int x=8;x<=11;x++) for(int z=8;z<=9;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,10,z)),"N2 landing narrowed at its turn");
            for (int x=4;x<=5;x++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,9)),"Upper west landing lost clearance");
            if (variant!=CatalogueTerraces.Variant.RAILS_SLOPES)
                for(int x=5;x<=6;x++) for(int z=8;z<=10;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"N7 bypass narrowed");
            if (variant==CatalogueTerraces.Variant.RAILS_FORK)
                for(int x=8;x<=9;x++) for(int z=8;z<=10;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower east branch narrowed");
            var west = new BlockPos(3,3,4);
            var east = new BlockPos(11,4,6);
            // Keep one crossing band at a time. Each must join both complete side rails.
            for (int keep = -1; keep < 3; keep++) {
                var cut = new LinkedHashMap<>(geometry);
                for (int y=0;y<=18;y++) for (int z=0;z<=14;z++) {
                    boolean retained = keep==0 && y>=3 && y<=6 || keep==1 && y>=9 && y<=13
                            || keep==2 && y>=15 && y<=17;
                    if (!retained) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
                }
                helper.assertTrue(reachable(cut,west).contains(east)==(keep>=0)
                        && reachable(cut,east).contains(west)==(keep>=0),
                        "Missing crossing, false overpass or extra connection: " + variant + " band=" + keep);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void railSlopesAndLowerForkKeepTheirDistinctRoutes(GameTestHelper helper) {
        var center = new BlockPos(7,15,7);
        var slopes = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.RAILS_SLOPES);
        for (int z=6;z<=7;z++) {
            helper.assertTrue(slopes.get(new BlockPos(4,9,z)).getValue(StairBlock.FACING)==Direction.EAST
                    && slopes.get(new BlockPos(5,10,z)).getValue(StairBlock.FACING)==Direction.EAST
                    && slopes.get(new BlockPos(8,10,z)).getValue(StairBlock.FACING)==Direction.WEST,
                    "B must rise then descend across a two-wide off-center landing");
            helper.assertTrue(stand(slopes,new BlockPos(6,11,z)) && stand(slopes,new BlockPos(7,11,z)),
                    "B landing lacks support or clearance");
        }
        var fork = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.RAILS_FORK);
        for(int mask=0;mask<4;mask++) {
            var cut = new LinkedHashMap<>(fork);
            // Remove the upper/middle crossbars, then cut either/both lower channels.
            for(int y=7;y<=18;y++) for(int z=0;z<=14;z++) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            for(int y=3;y<=6;y++) {
                if((mask&1)!=0) for(int z=4;z<=5;z++) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
                if((mask&2)!=0) for(int z=8;z<=9;z++) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            }
            var west = new BlockPos(3,3,4); var east = new BlockPos(11,4,6);
            helper.assertTrue(reachable(cut,west).contains(east)==(mask!=3)
                    && reachable(cut,east).contains(west)==(mask!=3),"C fork did not split and rejoin");
        }
        helper.succeed();
    }

    private static void assertRailsSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                               BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(7,3,4),Blocks.STONE.defaultBlockState()).is(Blocks.STONE_STAIRS)
                == (variant!=CatalogueTerraces.Variant.RAILS_FORK),"Lower rail step changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(7,3,8),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.RAILS_FORK),"Lower rail fork changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(6,10,6),Blocks.STONE.defaultBlockState()).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.RAILS_SLOPES),"Off-center rail landing changed");
        helper.assertTrue(blocks.get(base.offset(7,15,11)).isAir(),"Upper rail crossing changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void pearlsHaveThreeRoundedLobesAndExactlyTwoNecks(GameTestHelper helper) {
        for (var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.PEARLS,
                CatalogueTerraces.Variant.PEARLS_SPLIT,CatalogueTerraces.Variant.PEARLS_SATELLITE}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertPearlSignatures(helper,geometry,BlockPos.ZERO,variant);
            helper.assertTrue(!open(geometry,new BlockPos(2,3,12)) && !open(geometry,new BlockPos(2,15,2))
                    && open(geometry,new BlockPos(7,3,7)),"Pearls lost their rounded, opposite diagonal footprints");
            helper.assertTrue(solid(geometry,new BlockPos(7,7,7)) && solid(geometry,new BlockPos(7,14,7)),
                    "Lobes merged into one vertically open chamber");
            for(int cut=0;cut<4;cut++) {
                var blocks = new LinkedHashMap<>(geometry);
                if((cut&1)!=0) for(int x=11;x<=12;x++) for(int z=6;z<=7;z++)
                    blocks.put(new BlockPos(x,13,z),Blocks.STONE.defaultBlockState());
                if((cut&2)!=0) for(int x=2;x<=3;x++) for(int z=6;z<=7;z++)
                    blocks.put(new BlockPos(x,7,z),Blocks.STONE.defaultBlockState());
                var reached = reachable(blocks,CatalogueTerraces.ENTRANCE);
                for(int i=0;i<CatalogueTerraces.HEARTS.size();i++) {
                    boolean expected = i==2 || i==4 || (i==0 || i==3 ? cut==0 : (cut&1)==0);
                    helper.assertTrue(reached.contains(CatalogueTerraces.HEARTS.get(i).south())==expected,
                            "Pearl chain has a missing neck or bypass: " + variant + " cut=" + cut + " node=" + i);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void splitPearlAndSatelliteKeepTheirOwnTopology(GameTestHelper helper) {
        var center = new BlockPos(7,15,7);
        var split = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.PEARLS_SPLIT);
        for(int z=6;z<=7;z++) helper.assertTrue(split.get(new BlockPos(7,9,z)).is(Blocks.STONE_STAIRS)
                && stand(split,new BlockPos(6,9,z)) && stand(split,new BlockPos(8,10,z)),
                "Split pearl must connect distinct floor heights through two supported steps");
        var cut = new LinkedHashMap<>(split);
        for(int y=10;y<=12;y++) for(int z=6;z<=7;z++) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
        var west = new BlockPos(3,9,10); var east = new BlockPos(11,9,12);
        helper.assertTrue(!reachable(cut,west).contains(east) && !reachable(cut,east).contains(west),
                "Split pearl has an unintended second connection");
        var satellite = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.PEARLS_SATELLITE);
        var tip = new BlockPos(11,15,10);
        helper.assertTrue(reachable(satellite,CatalogueTerraces.ENTRANCE).contains(tip),"Satellite is inaccessible");
        var separated = new LinkedHashMap<>(satellite);
        for(int x=11;x<=12;x++) for(int y=15;y<=17;y++)
            separated.put(new BlockPos(x,y,8),Blocks.STONE.defaultBlockState());
        var reached = reachable(separated,CatalogueTerraces.ENTRANCE);
        helper.assertTrue(!reached.contains(tip),"Satellite merged with the upper lobe instead of branching from its neck");
        for(var heart : CatalogueTerraces.HEARTS)
            helper.assertTrue(reached.contains(heart.south()),"Satellite became a required mineral route");
        helper.succeed();
    }

    private static void assertPearlSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                               BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(7,6,7)).isAir(),"Lower pearl lost its rounded roof");
        helper.assertTrue(blocks.get(base.offset(7,9,7)).is(Blocks.STONE_STAIRS)
                == (variant==CatalogueTerraces.Variant.PEARLS_SPLIT),"Middle pearl subdivision changed");
        helper.assertTrue(blocks.get(base.offset(10,9,8)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.PEARLS_SPLIT),"Middle pearl floor offset changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(11,15,10),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.PEARLS_SATELLITE),"Satellite changed during composition");
        helper.assertTrue(blocks.getOrDefault(base.offset(10,15,8),Blocks.STONE.defaultBlockState()).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.PEARLS_SATELLITE),"Satellite lost its separating rock");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void eightLoopsUseFourSupportedArmsAndCentralJunction(GameTestHelper helper) {
        for(var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.EIGHT,
                CatalogueTerraces.Variant.EIGHT_RETURN,CatalogueTerraces.Variant.EIGHT_SLIT}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertEightSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=5;x<=12;x++) for(int z=9;z<=10;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper eight traverse narrowed or lost support");
            for(int x=3;x<=12;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower eight traverse narrowed or lost support");
            for(int x=6;x<=8;x++) for(int y=12;y<=13;y++) for(int z=8;z<=9;z++)
                helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Upper nucleus is not a solid body");
            for(int x=6;x<=8;x++) for(int y=3;y<=6;y++) for(int z=6;z<=8;z++) {
                if(x==8 && z==8 || variant==CatalogueTerraces.Variant.EIGHT_SLIT && y<=5 && z>=7) continue;
                helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Lower nucleus is not a solid body");
            }
            for(int x=6;x<=7;x++) for(int z=6;z<=9;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"J became a fall shaft or lost clearance");
            for(int mask=0;mask<4;mask++) {
                var upper = new LinkedHashMap<>(geometry);
                if((mask&1)!=0) closeWell(upper,2,3,6,7,12);
                if((mask&2)!=0) closeWell(upper,11,12,4,5,12);
                var junction = new BlockPos(7,9,7);
                helper.assertTrue(reachable(upper,CatalogueTerraces.ENTRANCE).contains(junction)==(mask!=3)
                        && reachable(upper,junction).contains(CatalogueTerraces.ENTRANCE)==(mask!=3),
                        "Upper loop lost an arm or gained a bypass: " + variant + " mask=" + mask);
                if(variant!=CatalogueTerraces.Variant.EIGHT_RETURN) {
                    var lower = new LinkedHashMap<>(geometry);
                    if((mask&1)!=0) closeWell(lower,2,3,10,11,6);
                    if((mask&2)!=0) closeWell(lower,11,12,8,9,6);
                    var low = new BlockPos(3,3,4);
                    helper.assertTrue(reachable(lower,junction).contains(low)==(mask!=3)
                            && reachable(lower,low).contains(junction)==(mask!=3),
                            "Lower loop lost an arm or gained a bypass: " + variant + " mask=" + mask);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void eightReturnAndSlitHaveOnlyTheirIntendedBypasses(GameTestHelper helper) {
        for(var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.EIGHT,
                CatalogueTerraces.Variant.EIGHT_RETURN,CatalogueTerraces.Variant.EIGHT_SLIT}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var cut = new LinkedHashMap<>(geometry);
            for(int x=6;x<=7;x++) for(int z=6;z<=9;z++) for(int y=9;y<=11;y++)
                cut.put(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());
            var high = new BlockPos(3,15,12); var low = new BlockPos(3,3,4);
            boolean returning = variant==CatalogueTerraces.Variant.EIGHT_RETURN;
            helper.assertTrue(reachable(cut,high).contains(low)==returning
                    && reachable(cut,low).contains(high)==returning,"Unexpected connection outside J: " + variant);
            if(returning) {
                var closed = new LinkedHashMap<>(cut);
                closeWell(closed,2,3,4,5,6);
                helper.assertTrue(!reachable(closed,high).contains(low) && !reachable(closed,low).contains(high),
                        "B exterior return is not its only bypass of J");
            }
            // With J blocked, the lower northern bar and C's slit are independently observable.
            for(int y=3;y<=5;y++) for(int z=4;z<=5;z++)
                cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            var east = new BlockPos(11,3,4);
            boolean slit = variant==CatalogueTerraces.Variant.EIGHT_SLIT;
            helper.assertTrue(reachable(cut,low).contains(east)==slit && reachable(cut,east).contains(low)==slit,
                    "Lower core slit is missing or shared by another variant");
            if(slit) {
                for(int y=3;y<=5;y++) for(int z=7;z<=8;z++)
                    cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(cut,low).contains(east) && !reachable(cut,east).contains(low),
                        "C added an unintended second lower shortcut");
            }
        }
        helper.succeed();
    }

    private static void closeWell(Map<BlockPos,BlockState> blocks,int x1,int x2,int z1,int z2,int y) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++)
            blocks.put(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void assertEightSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                               BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(7,4,7)).isAir()
                == (variant==CatalogueTerraces.Variant.EIGHT_SLIT),"Lower eight core changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(2,6,4),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.EIGHT_RETURN),"Exterior return changed");
        helper.assertTrue(blocks.get(base.offset(7,9,7)).isAir() && blocks.get(base.offset(7,8,7)).is(Blocks.STONE),
                "Eight junction lost its supported floor");
        helper.assertTrue(blocks.get(base.offset(7,13,9)).is(Blocks.STONE),"Upper eight core was perforated");
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
                if (!direction.getAxis().isVertical()) {
                    var rise = blocks.getOrDefault(next,Blocks.STONE.defaultBlockState());
                    if (rise.is(Blocks.STONE_STAIRS) && rise.getValue(StairBlock.FACING)==direction
                            && stand(blocks,next.above()) && visited.add(next.above())) queue.addLast(next.above());
                    var tread = blocks.getOrDefault(p.below(),Blocks.STONE.defaultBlockState());
                    if (tread.is(Blocks.STONE_STAIRS) && tread.getValue(StairBlock.FACING)==direction.getOpposite()
                            && open(blocks,next.above()) && stand(blocks,next.below())
                            && visited.add(next.below())) queue.addLast(next.below());
                }
            }
        }
        return visited;
    }
}
