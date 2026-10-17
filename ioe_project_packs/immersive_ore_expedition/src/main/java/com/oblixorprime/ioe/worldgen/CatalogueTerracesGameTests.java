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
        if (variant.isRails() || variant.isEight() || variant.isBayonet() || variant.isRoots() || variant.isCells() || variant.isComb() || variant.isTeeth() || variant.isChimney() || variant.isAxial() || variant.isMushroom() || variant.isSlab() || variant.isHourglass() || variant.isBell() || variant.isBipyramid() || variant.isArch() || variant.isTraverse() || variant.isNave() || variant.isTrihedral() || variant.isShells() || variant.isAnnulus() || variant==CatalogueTerraces.Variant.PEARLS_SPLIT) {
            var again = IoeExpeditionPlanPlacement.apply(level,plan).orElseThrow();
            var foreign = geometry.entrySet().stream().filter(e -> e.getValue().is(Blocks.STONE_STAIRS) || (variant.isEight() || variant.isBayonet() || variant.isRoots() || variant.isCells() || variant.isComb() || variant.isTeeth() || variant.isChimney() || variant.isAxial() || variant.isMushroom() || variant.isSlab() || variant.isHourglass() || variant.isBell() || variant.isBipyramid() || variant.isArch() || variant.isTraverse() || variant.isNave() || variant.isTrihedral() || variant.isShells() || variant.isAnnulus()) && e.getValue().is(Blocks.LADDER))
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
                    if (variant.isAnnulus()) assertAnnulusSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isShells()) assertShellSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isTrihedral()) assertTrihedralSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isNave()) assertNaveSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isTraverse()) assertTraverseSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isArch()) assertArchSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isBipyramid()) assertBipyramidSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isBell()) assertBellSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isHourglass()) assertHourglassSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isSlab()) assertSlabSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isMushroom()) assertMushroomSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isAxial()) assertAxialSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isChimney()) assertChimneySignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isTeeth()) assertTeethSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isComb()) assertCombSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isCells()) assertCellSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isRoots()) assertRootsSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isBayonet()) assertBayonetSignatures(helper,plan.blocks(),base,variant);
                    else if (variant.isEight()) assertEightSignatures(helper,plan.blocks(),base,variant);
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
            if (type == ExpeditionSiteType.MINER_CAMP) {
                // Prepare only this controlled fixture's halo; production still refuses absent metadata.
                var bounds = ExpeditionWorldgenGameTests.prepareCatalogueCampMetadata(level,origin,seed);
                helper.assertFalse(LoadedStructureCollision.blocksPlacement(level,bounds),
                        "Catalogue fixture metadata is unavailable or overlaps a structure: " + variant);
            }
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
            if (variant.isRails() || variant.isPearls() || variant.isEight() || variant.isBayonet() || variant.isRoots() || variant.isCells() || variant.isComb() || variant.isTeeth() || variant.isChimney() || variant.isAxial() || variant.isMushroom() || variant.isSlab() || variant.isHourglass() || variant.isBell() || variant.isBipyramid() || variant.isArch() || variant.isTraverse() || variant.isNave() || variant.isTrihedral() || variant.isShells() || variant.isAnnulus()) {
                var actual = new LinkedHashMap<BlockPos,BlockState>();
                CatalogueTerraces.geometry(center,variant).keySet().forEach(p -> actual.put(p,level.getBlockState(p)));
                if (variant.isAnnulus()) assertAnnulusSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isShells()) assertShellSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isTrihedral()) assertTrihedralSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isNave()) assertNaveSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isTraverse()) assertTraverseSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isArch()) assertArchSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isBipyramid()) assertBipyramidSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isBell()) assertBellSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isHourglass()) assertHourglassSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isSlab()) assertSlabSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isMushroom()) assertMushroomSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isAxial()) assertAxialSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isChimney()) assertChimneySignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isTeeth()) assertTeethSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isComb()) assertCombSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isCells()) assertCellSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isRoots()) assertRootsSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isBayonet()) assertBayonetSignatures(helper,actual,CatalogueTerraces.base(center),variant);
                else if (variant.isEight()) assertEightSignatures(helper,actual,CatalogueTerraces.base(center),variant);
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
    public static void combCampMarginRejectsMissingNeighborWithoutLoading(GameTestHelper helper) {
        var level = helper.getLevel();
        var chunk = new net.minecraft.world.level.ChunkPos(1_420_000,1_420_000);
        var west = new net.minecraft.world.level.ChunkPos(chunk.x-1,chunk.z);
        var center = new BlockPos(chunk.getMinBlockX()+7,25,chunk.getMinBlockZ()+7);
        var geometry = CatalogueTerraces.geometry(center,CatalogueTerraces.Variant.COMB);
        helper.assertTrue(geometry.keySet().stream().allMatch(p -> new net.minecraft.world.level.ChunkPos(p).equals(chunk)),
                "BP-14 writes outside its source chunk");
        var bounds = ExpeditionSiteFeature.expandedPlanBounds(geometry.keySet());
        helper.assertTrue(Math.floorDiv(bounds.minX(),16)==west.x,
                "BP-14 collision margin no longer exercises the west neighbor");
        helper.assertTrue(level.getChunkSource().getChunkNow(west.x,west.z)==null,
                "Missing-metadata fixture is already loaded");
        // All other metadata is explicitly available, isolating the west-neighbor refusal.
        for (int cx=Math.floorDiv(bounds.minX(),16);cx<=Math.floorDiv(bounds.maxX(),16);cx++)
            for (int cz=Math.floorDiv(bounds.minZ(),16);cz<=Math.floorDiv(bounds.maxZ(),16);cz++)
                if (cx!=west.x || cz!=west.z) level.getChunk(cx,cz);
        helper.assertTrue(LoadedStructureCollision.blocksPlacement(level,bounds),
                "Missing west metadata was admitted");
        helper.assertTrue(level.getChunkSource().getChunkNow(west.x,west.z)==null,
                "Collision check force-loaded the missing neighbor");
        level.getChunk(west.x,west.z); // Controlled fixture allocation only.
        helper.assertFalse(LoadedStructureCollision.blocksPlacement(level,bounds),
                "Known empty metadata still rejects the BP-14 bounds");
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
            for(int x=7;x<=8;x++) for(int y=12;y<=13;y++) for(int z=8;z<=9;z++)
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

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bayonetElbowsHaveOnlyTheirSupportedDescents(GameTestHelper helper) {
        for(var variant : new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.BAYONET,
                CatalogueTerraces.Variant.BAYONET_SPLIT,CatalogueTerraces.Variant.BAYONET_SHOULDER}) {
            var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertBayonetSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=6;x<=12;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper bayonet turn narrowed");
            for(int x=4;x<=10;x++) for(int z=6;z<=7;z++) {
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Middle bayonet traverse narrowed");
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower bayonet traverse narrowed");
            }
            var middle = new BlockPos(7,9,7); var low = new BlockPos(3,3,4);
            var upperCut = new LinkedHashMap<>(geometry);
            closeWell(upperCut,11,12,6,7,13);
            helper.assertTrue(!reachable(upperCut,CatalogueTerraces.ENTRANCE).contains(middle)
                    && !reachable(upperCut,middle).contains(CatalogueTerraces.ENTRANCE),"Upper elbow gained a bypass");
            for(int mask=0;mask<4;mask++) {
                var cut = new LinkedHashMap<>(geometry);
                if((mask&1)!=0) closeWell(cut,2,3,6,7,7);
                if((mask&2)!=0) closeWell(cut,11,12,8,9,7);
                boolean connected = variant==CatalogueTerraces.Variant.BAYONET_SPLIT ? mask!=3 : (mask&1)==0;
                helper.assertTrue(reachable(cut,middle).contains(low)==connected
                        && reachable(cut,low).contains(middle)==connected,
                        "Middle split does not have exactly its intended routes: " + variant + " mask=" + mask);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bayonetShoulderBranchIsSterileAndOptional(GameTestHelper helper) {
        var geometry = CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.BAYONET_SHOULDER);
        for(int x=9;x<=12;x++) for(int z=6;z<=9;z++)
            helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper shoulder lost its widened floor");
        var terminal = new BlockPos(11,15,11);
        helper.assertTrue(reachable(geometry,CatalogueTerraces.ENTRANCE).contains(terminal),"Sterile branch unreachable");
        var cut = new LinkedHashMap<>(geometry);
        for(int x=11;x<=12;x++) for(int y=15;y<=17;y++) cut.put(new BlockPos(x,y,10),Blocks.STONE.defaultBlockState());
        var reached = reachable(cut,CatalogueTerraces.ENTRANCE);
        helper.assertTrue(!reached.contains(terminal) && !reachable(cut,terminal).contains(CatalogueTerraces.ENTRANCE),
                "Branch has an unintended second connection");
        for(var heart:CatalogueTerraces.HEARTS)
            helper.assertTrue(reached.contains(heart.south()),"Optional branch cut disconnected a mineral alcove");
        helper.succeed();
    }

    private static void assertBayonetSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                               BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(7,15,6),Blocks.STONE.defaultBlockState()).is(Blocks.STONE),
                "Bayonet entry bypasses the upper elbow");
        helper.assertTrue(blocks.getOrDefault(base.offset(9,15,6),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.BAYONET_SHOULDER),"Upper shoulder changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(12,6,8),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.BAYONET_SPLIT),"Middle split changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(11,15,11),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.BAYONET_SHOULDER),"Sterile branch changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void rootsKeepTheTrunkAndEachStagedBranchNecessary(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.ROOTS,
                CatalogueTerraces.Variant.ROOTS_STAGED,CatalogueTerraces.Variant.ROOTS_JOINED}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertRootsSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=6;x<=12;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper root branch narrowed");
            for(int x=8;x<=9;x++) for(int z=8;z<=10;z++) {
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Middle root branch narrowed");
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower root branch narrowed");
            }
            var upperCut=new LinkedHashMap<>(geometry);
            closeWell(upperCut,6,7,6,7,13);
            var reached=reachable(upperCut,CatalogueTerraces.ENTRANCE);
            for(int i=0;i<CatalogueTerraces.HEARTS.size();i++)
                helper.assertTrue(reached.contains(CatalogueTerraces.HEARTS.get(i).south())==(i==2 || i==4),
                        "Root trunk cut left an unintended high/low connection: " + variant);
            helper.assertTrue(!reachable(upperCut,new BlockPos(7,9,7)).contains(CatalogueTerraces.ENTRANCE),
                    "Root trunk has a reverse bypass");
        }
        var staged=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.ROOTS_STAGED);
        var middle=new BlockPos(7,9,7);
        var west=new BlockPos(3,3,4); var east=new BlockPos(11,3,12);
        for(int mask=0;mask<4;mask++) {
            var cut=new LinkedHashMap<>(staged);
            if((mask&1)!=0) closeWell(cut,2,3,6,7,6);
            if((mask&2)!=0) closeWell(cut,11,12,8,9,6);
            helper.assertTrue(reachable(cut,middle).contains(west)==((mask&1)==0)
                    && reachable(cut,west).contains(middle)==((mask&1)==0),"West staged root is not an independent branch");
            helper.assertTrue(reachable(cut,middle).contains(east)==((mask&2)==0)
                    && reachable(cut,east).contains(middle)==((mask&2)==0),"East staged root is not an independent branch");
            helper.assertTrue(reachable(cut,west).contains(east)==(mask==0),"Staged root ends gained a lower crossconnection");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void rootsJoinedVariantAddsExactlyOneLowerBypass(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.ROOTS,CatalogueTerraces.Variant.ROOTS_JOINED}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            for(int mask=0;mask<4;mask++) {
                var cut=new LinkedHashMap<>(geometry);
                if((mask&1)!=0) closeWell(cut,6,7,6,7,6);
                if((mask&2)!=0) closeWell(cut,11,12,8,9,6);
                boolean connected=variant==CatalogueTerraces.Variant.ROOTS_JOINED ? mask!=3 : (mask&1)==0;
                var middle=new BlockPos(7,9,7);
                for(var low:new BlockPos[]{new BlockPos(3,3,4),new BlockPos(11,3,12)})
                    helper.assertTrue(reachable(cut,middle).contains(low)==connected
                            && reachable(cut,low).contains(middle)==connected,"Root gained or lost a lower bypass: " + variant);
            }
            var westCut=new LinkedHashMap<>(geometry);
            for(int x=5;x<=7;x++) for(int y=9;y<=11;y++) westCut.put(new BlockPos(x,y,9),Blocks.STONE.defaultBlockState());
            var reached=reachable(westCut,CatalogueTerraces.ENTRANCE);
            helper.assertTrue(!reached.contains(CatalogueTerraces.HEARTS.get(6).south()),"West terminal root gained a second attachment");
            for(int i=0;i<6;i++) helper.assertTrue(reached.contains(CatalogueTerraces.HEARTS.get(i).south()),
                    "West terminal cut disconnected another branch");
        }
        helper.succeed();
    }

    private static void assertRootsSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                              BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(6,6,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant!=CatalogueTerraces.Variant.ROOTS_STAGED),"Deep fork changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(2,6,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.ROOTS_STAGED),"West staged fork changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(12,6,8),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant!=CatalogueTerraces.Variant.ROOTS),"Lateral branch changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(7,9,10),Blocks.STONE.defaultBlockState()).is(Blocks.STONE),
                "Terminal roots gained an unintended crossconnection");
        helper.assertTrue(blocks.get(base.offset(5,12,6)).is(Blocks.STONE)
                && blocks.get(base.offset(6,12,6)).is(Blocks.LADDER),"Trunk backing changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void cellPocketsKeepTheirTwoLoopsOrTheirMiddleSpine(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.CELLS,
                CatalogueTerraces.Variant.CELLS_SPINE,CatalogueTerraces.Variant.CELLS_DIAGONAL}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertCellSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=5;x<=10;x++) for(int z=11;z<=12;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Middle pocket spine narrowed or lost support");
            for(int x=12;x<=13;x++) for(int z=6;z<=9;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper right pocket route narrowed");
            if(variant!=CatalogueTerraces.Variant.CELLS_SPINE) {
                for(int x=8;x<=11;x++) for(int z=6;z<=7;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper transverse pocket link narrowed");
                for(int x=7;x<=8;x++) for(int z=6;z<=10;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower transverse pocket link narrowed");
            }
            // Remove C's extra edge while checking the common eight-edge reference graph.
            if(variant==CatalogueTerraces.Variant.CELLS_DIAGONAL) closeWell(geometry,8,9,11,12,12);
            boolean spine=variant==CatalogueTerraces.Variant.CELLS_SPINE;
            var middle=CatalogueTerraces.HEARTS.get(6).south();
            for(boolean upper:new boolean[]{false,true}) for(int mask=0;mask<4;mask++) {
                var cut=new LinkedHashMap<>(geometry);
                if((mask&1)!=0) {
                    if(upper) closeWell(cut,2,3,6,7,12); else closeWell(cut,7,8,4,5,6);
                }
                if((mask&2)!=0) closeWell(cut,12,13,11,12,upper ? 12 : 6);
                var left=CatalogueTerraces.HEARTS.get(upper ? 2 : 0).south();
                var right=CatalogueTerraces.HEARTS.get(upper ? 4 : 3).south();
                boolean leftConnected=spine ? (mask&1)==0 : mask!=3;
                boolean rightConnected=spine ? (mask&2)==0 : mask!=3;
                helper.assertTrue(reachable(cut,middle).contains(left)==leftConnected
                        && reachable(cut,left).contains(middle)==leftConnected,"Left pocket neck topology changed: " + variant);
                helper.assertTrue(reachable(cut,middle).contains(right)==rightConnected
                        && reachable(cut,right).contains(middle)==rightConnected,"Right pocket neck topology changed: " + variant);
                helper.assertTrue(reachable(cut,left).contains(right)==(!spine || mask==0)
                        && reachable(cut,right).contains(left)==(!spine || mask==0),"Transverse pocket link changed: " + variant);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void cellDiagonalIsAnIndependentHighToMiddleConnection(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.CELLS,
                CatalogueTerraces.Variant.CELLS_SPINE,CatalogueTerraces.Variant.CELLS_DIAGONAL}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var bridgeCut=new LinkedHashMap<>(geometry);
            for(int y=9;y<=11;y++) for(int z=11;z<=12;z++) bridgeCut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
            var left=CatalogueTerraces.HEARTS.get(5).south(); var right=CatalogueTerraces.HEARTS.get(1).south();
            boolean loop=variant!=CatalogueTerraces.Variant.CELLS_SPINE;
            helper.assertTrue(reachable(bridgeCut,left).contains(right)==loop
                    && reachable(bridgeCut,right).contains(left)==loop,"Middle spine is bypassed in B or missing a loop in A/C");
            var cut=new LinkedHashMap<>(geometry);
            closeWell(cut,2,3,6,7,12);
            closeWell(cut,12,13,11,12,12);
            for(int y=15;y<=17;y++) for(int z=6;z<=7;z++) cut.put(new BlockPos(8,y,z),Blocks.STONE.defaultBlockState());
            var high=CatalogueTerraces.HEARTS.get(2).south(); var otherHigh=CatalogueTerraces.HEARTS.get(4).south();
            boolean diagonal=variant==CatalogueTerraces.Variant.CELLS_DIAGONAL;
            helper.assertTrue(reachable(cut,high).contains(right)==diagonal
                    && reachable(cut,right).contains(high)==diagonal,"C diagonal is not independent of the original upper necks");
            helper.assertTrue(!reachable(cut,otherHigh).contains(right),"Diagonal accidentally connected the other high pocket");
            if(diagonal) {
                closeWell(cut,8,9,11,12,12);
                helper.assertTrue(!reachable(cut,high).contains(right) && !reachable(cut,right).contains(high),
                        "C added more than one independent high-to-middle connection");
            }
        }
        helper.succeed();
    }

    private static void assertCellSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                             BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(8,15,7),Blocks.STONE.defaultBlockState()).isAir()
                == (variant!=CatalogueTerraces.Variant.CELLS_SPINE),"Upper transverse pocket link changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(7,3,8),Blocks.STONE.defaultBlockState()).isAir()
                == (variant!=CatalogueTerraces.Variant.CELLS_SPINE),"Lower transverse pocket link changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(8,12,12),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.CELLS_DIAGONAL),"Diagonal pocket neck changed");
        helper.assertTrue(blocks.get(base.offset(2,10,10)).isAir() && blocks.get(base.offset(13,10,10)).isAir(),
                "Middle pockets lost their irregular side volumes");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void combGroovesMeetOnlyAtTheOffsetCollector(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.COMB,
                CatalogueTerraces.Variant.COMB_WINDOW,CatalogueTerraces.Variant.COMB_DIVIDED}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertCombSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=5;x<=6;x++) for(int z=8;z<=10;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Left collector narrowed");
            for(int x=12;x<=13;x++) for(int z=6;z<=9;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,10,z)),"Right collector lost its raised supported floor");
            var left=CatalogueTerraces.HEARTS.get(6).south();
            var middle=CatalogueTerraces.HEARTS.get(5).south();
            var right=CatalogueTerraces.HEARTS.get(1).south();
            var cut=new LinkedHashMap<>(geometry);
            for(int y=9;y<=11;y++) for(int z=8;z<=11;z++) cut.put(new BlockPos(5,y,z),Blocks.STONE.defaultBlockState());
            boolean window=variant==CatalogueTerraces.Variant.COMB_WINDOW;
            helper.assertTrue(reachable(cut,left).contains(middle)==window && reachable(cut,middle).contains(left)==window,
                    "Left groove gained a connection outside the collector/window");
            if(window) {
                for(int y=3;y<=5;y++) for(int z=9;z<=10;z++) cut.put(new BlockPos(5,y,z),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(cut,left).contains(middle) && !reachable(cut,middle).contains(left),
                        "Lower window is not the only bypass");
            }
            var rightCut=new LinkedHashMap<>(geometry);
            for(int y=9;y<=12;y++) for(int z=4;z<=5;z++) rightCut.put(new BlockPos(8,y,z),Blocks.STONE.defaultBlockState());
            helper.assertTrue(!reachable(rightCut,middle).contains(right) && !reachable(rightCut,right).contains(middle),
                    "Right groove gained a second attachment outside the stepped collector");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void combDividedGrooveUsesItsSupportedSideDetour(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.COMB,
                CatalogueTerraces.Variant.COMB_WINDOW,CatalogueTerraces.Variant.COMB_DIVIDED}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            boolean divided=variant==CatalogueTerraces.Variant.COMB_DIVIDED;
            if(divided) for(int x=6;x<=7;x++) for(int y=6;y<=8;y++) for(int z=6;z<=7;z++)
                helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Central massif lost its solid volume");
            if(divided) for(int y=5;y<=7;y++) for(int z=4;z<=9;z++)
                helper.assertTrue(geometry.get(new BlockPos(10,y,z)).is(Blocks.STONE),"Side detour lost its separating wall");
            var cut=new LinkedHashMap<>(geometry);
            closeWell(cut,6,7,4,5,8);
            var north=new BlockPos(7,5,5); var south=new BlockPos(7,5,10);
            helper.assertTrue(reachable(cut,north).contains(south) && reachable(cut,south).contains(north),
                    "Bottom groove cannot be explored in both directions");
            for(int x=8;x<=9;x++) for(int y=5;y<=7;y++) cut.put(new BlockPos(x,y,8),Blocks.STONE.defaultBlockState());
            helper.assertTrue(reachable(cut,north).contains(south)==!divided
                    && reachable(cut,south).contains(north)==!divided,"Split groove does not depend on its lateral detour");
            var reached=reachable(cut,CatalogueTerraces.ENTRANCE);
            for(var heart:CatalogueTerraces.HEARTS)
                helper.assertTrue(reached.contains(heart.south()),"Sterile detour cut removed access to a mineral alcove");
        }
        helper.succeed();
    }

    private static void assertCombSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                             BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(5,3,9),Blocks.STONE.defaultBlockState()).is(Blocks.STONE_STAIRS)
                == (variant==CatalogueTerraces.Variant.COMB_WINDOW),"Low intergroove window changed");
        helper.assertTrue(blocks.get(base.offset(6,6,4)).is(Blocks.LADDER),"Central north descent changed");
        helper.assertTrue(blocks.get(base.offset(6,6,6)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.COMB_DIVIDED),"Central groove interruption changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(8,5,7),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.COMB_DIVIDED),"Side detour changed");
        helper.assertTrue(blocks.get(base.offset(8,9,4)).is(Blocks.STONE_STAIRS)
                && stand(blocks,base.offset(9,10,4)),"Collector lost its level change");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void teethStayRootedAndTheirPointContoursStayWalkable(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.TEETH,
                CatalogueTerraces.Variant.TEETH_WINDOW,CatalogueTerraces.Variant.TEETH_NOTCH}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertTeethSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=0;x<=8;x++) helper.assertTrue(geometry.get(new BlockPos(x,14,6)).is(Blocks.STONE),
                    "Upper prongs lost their common wall-rooted base");
            for(int x=7;x<=14;x++) helper.assertTrue(geometry.get(new BlockPos(x,8,6)).is(Blocks.STONE),
                    "Middle beak lost its root");
            for(int x=0;x<=7;x++) helper.assertTrue(geometry.get(new BlockPos(x,2,6)).is(Blocks.STONE),
                    "Lower beak lost its root");
            for(int x=7;x<=8;x++) helper.assertTrue(geometry.get(new BlockPos(x,13,6)).isAir(),
                    "Upper and middle beaks touched");
            for(int y=6;y<=7;y++) for(int z=6;z<=7;z++) helper.assertTrue(geometry.get(new BlockPos(7,y,z)).isAir(),
                    "Middle and lower beaks touched");
            for(int z=5;z<=6;z++) for(int x=9;x<=10;x++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper point contour narrowed");
            for(int z=6;z<=7;z++) {
                for(int x=5;x<=6;x++) helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Middle point contour narrowed");
                for(int x=8;x<=9;x++) helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"Lower point contour narrowed");
            }
            var cut=new LinkedHashMap<>(geometry);
            for(int x=8;x<=9;x++) for(int y=3;y<=5;y++) cut.put(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());
            var north=CatalogueTerraces.HEARTS.get(0).south(); var south=CatalogueTerraces.HEARTS.get(3).south();
            helper.assertTrue(!reachable(cut,north).contains(south) && !reachable(cut,south).contains(north),
                    "Lower beak is bypassed outside its point");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void teethWindowAndUpperNotchAddOnlyTheirOwnAlternatives(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.TEETH,
                CatalogueTerraces.Variant.TEETH_WINDOW,CatalogueTerraces.Variant.TEETH_NOTCH}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var upper=new LinkedHashMap<>(geometry);
            for(int x=9;x<=10;x++) for(int y=15;y<=17;y++) upper.put(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());
            var high=CatalogueTerraces.HEARTS.get(2).south();
            boolean notch=variant==CatalogueTerraces.Variant.TEETH_NOTCH;
            helper.assertTrue(reachable(upper,CatalogueTerraces.ENTRANCE).contains(high)==notch
                    && reachable(upper,high).contains(CatalogueTerraces.ENTRANCE)==notch,"Upper notch missing or shared by another variant");
            if(notch) {
                for(int x=5;x<=6;x++) for(int y=15;y<=17;y++) upper.put(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(upper,CatalogueTerraces.ENTRANCE).contains(high)
                        && !reachable(upper,high).contains(CatalogueTerraces.ENTRANCE),"Upper beak gained a third passage");
            }
            var middle=new LinkedHashMap<>(geometry);
            for(int x=5;x<=6;x++) for(int y=9;y<=11;y++) middle.put(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());
            var north=CatalogueTerraces.HEARTS.get(5).south(); var south=CatalogueTerraces.HEARTS.get(1).south();
            boolean window=variant==CatalogueTerraces.Variant.TEETH_WINDOW;
            helper.assertTrue(reachable(middle,north).contains(south)==window
                    && reachable(middle,south).contains(north)==window,"Middle root window missing or shared by another variant");
            if(window) {
                for(int x=11;x<=12;x++) for(int y=9;y<=11;y++) middle.put(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(middle,north).contains(south) && !reachable(middle,south).contains(north),
                        "Middle beak gained a third passage");
            }
        }
        helper.succeed();
    }

    private static void assertTeethSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                              BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(11,9,6)).isAir()
                == (variant==CatalogueTerraces.Variant.TEETH_WINDOW),"Middle root window changed");
        helper.assertTrue(blocks.get(base.offset(5,15,6)).isAir()
                == (variant==CatalogueTerraces.Variant.TEETH_NOTCH),"Upper notch changed");
        helper.assertTrue(blocks.get(base.offset(0,15,6)).is(Blocks.STONE)
                && blocks.get(base.offset(14,9,7)).is(Blocks.STONE)
                && blocks.get(base.offset(0,3,6)).is(Blocks.STONE),"Alternating wall roots changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void chimneyKeepsOneVisibleVoidBesideItsProtectedSpine(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.CHIMNEY,
                CatalogueTerraces.Variant.CHIMNEY_BALCONY,CatalogueTerraces.Variant.CHIMNEY_REAR}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertChimneySignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=9;x<=10;x++) for(int z=6;z<=7;z++) for(int y=3;y<=17;y++)
                helper.assertTrue(geometry.get(new BlockPos(x,y,z)).isAir(),"Chimney lost its continuous vertical void");
            for(int z=6;z<=7;z++) {
                for(int y=3;y<=15;y++) helper.assertTrue(geometry.get(new BlockPos(4,y,z)).is(Blocks.LADDER)
                        && solid(geometry,new BlockPos(3,y,z)),"Protected spine lost its backed ladder");
                for(int foot:new int[]{3,9,15}) {
                    helper.assertTrue(stand(geometry,new BlockPos(5,foot,z)),"Spine landing is unsupported");
                    helper.assertTrue(geometry.get(new BlockPos(6,foot,z)).is(Blocks.STONE),"Void-side rock rail is absent");
                    for(int y=foot+1;y<=foot+2;y++) helper.assertTrue(geometry.get(new BlockPos(6,y,z)).isAir(),
                            "Chimney is no longer visible from its landing");
                }
            }
            var cut=new LinkedHashMap<>(geometry);
            closeWell(cut,4,5,6,7,12);
            var low=CatalogueTerraces.HEARTS.get(0).south();
            boolean rear=variant==CatalogueTerraces.Variant.CHIMNEY_REAR;
            helper.assertTrue(reachable(cut,CatalogueTerraces.ENTRANCE).contains(low)==rear
                    && reachable(cut,low).contains(CatalogueTerraces.ENTRANCE)==rear,
                    "Terminal lodges bypass the spine without C's rear descent");
            if(rear) {
                closeWell(cut,12,13,11,12,12);
                helper.assertTrue(!reachable(cut,CatalogueTerraces.ENTRANCE).contains(low)
                        && !reachable(cut,low).contains(CatalogueTerraces.ENTRANCE),"Rear descent gained an extra connection");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void chimneyBalconyClosesOnlyItsLocalLodgeLoop(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.CHIMNEY,
                CatalogueTerraces.Variant.CHIMNEY_BALCONY,CatalogueTerraces.Variant.CHIMNEY_REAR}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var left=CatalogueTerraces.HEARTS.get(6).south(); var right=CatalogueTerraces.HEARTS.get(1).south();
            helper.assertTrue(reachable(geometry,left).contains(right) && reachable(geometry,right).contains(left),
                    "Initial lodge connection is missing");
            var cut=new LinkedHashMap<>(geometry);
            for(int x=5;x<=6;x++) for(int y=9;y<=11;y++) cut.put(new BlockPos(x,y,10),Blocks.STONE.defaultBlockState());
            boolean balcony=variant==CatalogueTerraces.Variant.CHIMNEY_BALCONY;
            helper.assertTrue(reachable(cut,left).contains(right)==balcony && reachable(cut,right).contains(left)==balcony,
                    "Middle lodges have an uncontracted peripheral connection");
            if(balcony) {
                for(int z=11;z<=12;z++) for(int y=9;y<=11;y++) cut.put(new BlockPos(7,y,z),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(cut,left).contains(right) && !reachable(cut,right).contains(left),
                        "Local balcony gained a second return");
            }
        }
        helper.succeed();
    }

    private static void assertChimneySignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(7,9,11),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.CHIMNEY_BALCONY),"Bent middle balcony changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(13,12,11),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.CHIMNEY_REAR),"Rear lodge descent changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(8,15,12),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.CHIMNEY_REAR),"High rear lodge connection changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void axialVestibuleConnectsSixSupportedDirections(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.AXIAL,
                CatalogueTerraces.Variant.AXIAL_RETURN,CatalogueTerraces.Variant.AXIAL_BENT}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var hub=new BlockPos(7,9,7);
            assertAxialSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int y=9;y<=13;y++) helper.assertTrue(geometry.get(hub.atY(y)).isAir(),"Hollow vestibule became a pillar");
            for(var end:java.util.List.of(new BlockPos(10,9,1),new BlockPos(13,9,6),new BlockPos(2,9,4),
                    new BlockPos(5,9,12),new BlockPos(7,15,7),new BlockPos(4,3,7)))
                helper.assertTrue(reachable(geometry,hub).contains(end) && reachable(geometry,end).contains(hub),
                        "Axial direction lacks a supported return: " + end + " " + variant);
            for(int x=2;x<=7;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"West arm narrows beside the lower shaft");
            for(int x=10;x<=11;x++) for(int z=1;z<=4;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"North arm lost its full supported width");
            for(int x=12;x<=13;x++) for(int z=6;z<=7;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"East arm lost its full supported width");
            var cut=new LinkedHashMap<>(geometry);
            closeWell(cut,8,9,6,7,14);
            boolean bent=variant==CatalogueTerraces.Variant.AXIAL_BENT;
            helper.assertTrue(reachable(cut,CatalogueTerraces.ENTRANCE).contains(hub)==bent
                    && reachable(cut,hub).contains(CatalogueTerraces.ENTRANCE)==bent,"Upper arm has an unexpected route");
            if(bent) {
                for(int z=8;z<=9;z++) {
                    helper.assertTrue(stand(geometry,new BlockPos(11,11,z)),"Observation landing lacks clearance or support");
                    helper.assertTrue(geometry.get(new BlockPos(10,11,z)).is(Blocks.STONE)
                            && geometry.get(new BlockPos(10,12,z)).isAir()
                            && geometry.get(new BlockPos(10,13,z)).isAir(),"Observation window lost its rock sill");
                }
                closeWell(cut,11,12,8,9,14);
                helper.assertTrue(!reachable(cut,CatalogueTerraces.ENTRANCE).contains(hub)
                        && !reachable(cut,hub).contains(CatalogueTerraces.ENTRANCE),"Bent upper arm gained another descent");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void axialDiagonalReturnClosesOnlyTheNorthEastLoop(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.AXIAL,
                CatalogueTerraces.Variant.AXIAL_RETURN,CatalogueTerraces.Variant.AXIAL_BENT}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var north=new BlockPos(10,9,1); var east=new BlockPos(13,9,6);
            var cut=new LinkedHashMap<>(geometry);
            for(int x=10;x<=11;x++) for(int y=9;y<=11;y++) cut.put(new BlockPos(x,y,3),Blocks.STONE.defaultBlockState());
            boolean diagonal=variant==CatalogueTerraces.Variant.AXIAL_RETURN;
            helper.assertTrue(reachable(cut,north).contains(east)==diagonal && reachable(cut,east).contains(north)==diagonal,
                    "North-east arms bypass the hub without the diagonal return");
            if(diagonal) {
                for(int x=11;x<=12;x++) for(int z=2;z<=3;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Diagonal turn is not two-wide and supported");
                for(int x=12;x<=13;x++) for(int z=3;z<=6;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"Diagonal return narrowed before the east arm");
                for(int x=12;x<=13;x++) for(int y=9;y<=11;y++) cut.put(new BlockPos(x,y,3),Blocks.STONE.defaultBlockState());
                helper.assertTrue(!reachable(cut,north).contains(east) && !reachable(cut,east).contains(north),
                        "Diagonal return gained a second bypass");
            }
        }
        helper.succeed();
    }

    private static void assertAxialSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                              BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(13,9,3),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.AXIAL_RETURN),"Diagonal north-east return changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(8,14,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant!=CatalogueTerraces.Variant.AXIAL_BENT),"Straight upper arm changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(12,14,8),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.AXIAL_BENT),"Bent upper arm changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void mushroomStemSupportsTheCapAndBothFacesRemainAccessible(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.MUSHROOM,
                CatalogueTerraces.Variant.MUSHROOM_SLIT,CatalogueTerraces.Variant.MUSHROOM_LOBES}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertMushroomSignatures(helper,geometry,BlockPos.ZERO,variant);
            for(int x=7;x<=8;x++) for(int z=6;z<=7;z++) for(int y=2;y<=11;y++)
                helper.assertTrue(geometry.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Single rooted stem was cut or mineralized");
            for(int y=6;y<=7;y++) {
                for(int x:new int[]{6,9}) for(int z=6;z<=7;z++)
                    helper.assertTrue(geometry.get(new BlockPos(x,y,z)).isAir(),"Stem merged into a side wall at its waist");
                for(int z:new int[]{5,8}) for(int x=7;x<=8;x++)
                    helper.assertTrue(geometry.get(new BlockPos(x,y,z)).isAir(),"Stem merged into a front or rear wall at its waist");
            }
            var top=new BlockPos(7,15,7); var below=new BlockPos(7,9,9); var bottom=new BlockPos(5,3,7);
            for(var end:java.util.List.of(below,bottom)) helper.assertTrue(reachable(geometry,top).contains(end)
                    && reachable(geometry,end).contains(top),"Cap top/underside/base lacks a supported return");
            for(int x=6;x<=10;x++) for(int z=8;z<=9;z++) helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),
                    "Route beneath the cap narrowed or lost headroom");
            for(int x=5;x<=6;x++) for(int z=6;z<=7;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"West base route narrowed beside the stem");
            for(int x=9;x<=10;x++) for(int z=6;z<=7;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"East base route narrowed beside the stem");
            for(int x=6;x<=10;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,3,z)),"North base route narrowed beside the stem");
            var cut=new LinkedHashMap<>(geometry);
            closeWell(cut,12,13,6,7,12);
            boolean slit=variant==CatalogueTerraces.Variant.MUSHROOM_SLIT;
            helper.assertTrue(reachable(cut,top).contains(below)==slit && reachable(cut,below).contains(top)==slit,
                    "Cap has an uncontracted second descent");
            if(slit) {
                closeWell(cut,5,6,5,6,12);
                helper.assertTrue(!reachable(cut,top).contains(below) && !reachable(cut,below).contains(top),
                        "Radial slit gained another vertical route");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void mushroomLobesKeepTheirRockIsthmusAndLowerPassage(GameTestHelper helper) {
        var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.MUSHROOM_LOBES);
        for(int x=6;x<=8;x++) for(int y=12;y<=14;y++)
            helper.assertTrue(geometry.get(new BlockPos(x,y,8)).is(Blocks.STONE),"Two cap lobes lost the shared isthmus");
        for(int x=4;x<=5;x++) for(int y=12;y<=14;y++)
            helper.assertTrue(geometry.get(new BlockPos(x,y,7)).isAir(),"Cap split was filled into the full plateau");
        for(int x=6;x<=7;x++) for(int z=7;z<=9;z++)
            helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Lobe isthmus turn is not supported and two-wide");
        var rear=CatalogueTerraces.HEARTS.get(2).south(); var front=CatalogueTerraces.HEARTS.get(4).south();
        helper.assertTrue(reachable(geometry,rear).contains(front) && reachable(geometry,front).contains(rear),
                "Cap lobes are not connected initially");
        var cut=new LinkedHashMap<>(geometry);
        for(int x=6;x<=8;x++) for(int y=15;y<=17;y++) cut.put(new BlockPos(x,y,8),Blocks.STONE.defaultBlockState());
        helper.assertTrue(!reachable(cut,rear).contains(front) && !reachable(cut,front).contains(rear),
                "Two lobes have an extra upper bridge or rear descent");
        helper.assertTrue(reachable(cut,new BlockPos(6,9,8)).contains(new BlockPos(10,9,8))
                && reachable(cut,new BlockPos(10,9,8)).contains(new BlockPos(6,9,8)),
                "Cutting the upper isthmus removed the separate lower passage");
        helper.succeed();
    }

    private static void assertMushroomSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                 BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(5,12,5)).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.MUSHROOM_SLIT),"Radial slit descent changed");
        helper.assertTrue(blocks.get(base.offset(4,13,7)).isAir()
                == (variant==CatalogueTerraces.Variant.MUSHROOM_LOBES),"Cap lobe separation changed");
        helper.assertTrue(blocks.get(base.offset(7,6,6)).is(Blocks.STONE)
                && blocks.get(base.offset(8,13,6)).is(Blocks.STONE),"Rooted stem/cap connection changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void slabHasTwoSharedVolumesAndOnlyEquippedDescents(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.SLAB,
                CatalogueTerraces.Variant.SLAB_THIRD,CatalogueTerraces.Variant.SLAB_JOINED}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertSlabSignatures(helper,geometry,BlockPos.ZERO,variant);
            var top=new BlockPos(7,15,7); var below=new BlockPos(7,9,9); var low=new BlockPos(5,3,7);
            helper.assertTrue(reachable(geometry,top).contains(below) && reachable(geometry,below).contains(top)
                    && reachable(geometry,top).contains(low) && reachable(geometry,low).contains(top),
                    "Slab volumes lack a supported outward and return route");
            for(int y=3;y<=11;y++) helper.assertTrue(geometry.get(new BlockPos(7,y,7)).isAir(),
                    "Slab gained a central stem or multiple stacked floors");
            for(int z=6;z<=9;z++) {
                for(int x=3;x<=4;x++) helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),
                        "West descent lost its two-wide upper approach");
            }
            for(int z=4;z<=7;z++) {
                for(int x=12;x<=13;x++) helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),
                        "East descent lost its two-wide upper approach");
            }
            var cut=new LinkedHashMap<>(geometry);
            for(int x=8;x<=9;x++) for(int z=8;z<=12;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"N2 approach narrowed beside the rear descent");
            for(int x=9;x<=10;x++) for(int z=11;z<=12;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,9,z)),"N2 turn lost its two-wide supported landing");
            closeWell(cut,3,5,6,7,12);
            closeWell(cut,11,13,6,8,12);
            boolean third=variant==CatalogueTerraces.Variant.SLAB_THIRD;
            helper.assertTrue(reachable(cut,top).contains(below)==third
                    && reachable(cut,below).contains(top)==third,
                    "Visual opening became an unequipped descent or third descent was lost");
            if(third) {
                closeWell(cut,7,8,10,11,12);
                helper.assertTrue(!reachable(cut,top).contains(below) && !reachable(cut,below).contains(top),
                        "Three equipped descents gained an extra return");
            } else for(int x=7;x<=8;x++) {
                helper.assertTrue(geometry.get(new BlockPos(x,15,9)).is(Blocks.STONE),
                        "Visual rear opening lost its front rock rim");
                helper.assertTrue(geometry.get(new BlockPos(x,12,10)).isAir(),
                        "Visual rear opening was filled");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void slabJoinedOpeningsPreserveTheirSupportedUpperRib(GameTestHelper helper) {
        var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.SLAB_JOINED);
        for(int x=7;x<=8;x++) for(int z=6;z<=7;z++) {
            helper.assertTrue((geometry.get(new BlockPos(x,14,z)).is(Blocks.STONE)
                    || geometry.get(new BlockPos(x,14,z)).is(Blocks.CALCITE))
                    && stand(geometry,new BlockPos(x,15,z)),"Joined opening lost its two-wide upper rock rib");
            for(int y=12;y<=13;y++) helper.assertTrue(geometry.get(new BlockPos(x,y,z)).isAir(),
                    "Rock rib sealed the long opening beneath it");
        }
        for(int x=6;x<=10;x++) for(int z=6;z<=7;z++) for(int y=12;y<=13;y++)
            helper.assertTrue(geometry.get(new BlockPos(x,y,z)).isAir(),"Two side openings no longer share a continuous slit");
        var north=new BlockPos(7,15,5); var south=new BlockPos(7,15,8);
        helper.assertTrue(reachable(geometry,north).contains(south) && reachable(geometry,south).contains(north),
                "Upper rib does not connect both rims");
        var cut=new LinkedHashMap<>(geometry);
        // Isolate the central route from the two separate peripheral landings.
        for(int x:new int[]{3,4,5,11,12,13}) for(int y=15;y<=17;y++) for(int z=6;z<=7;z++)
            cut.put(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());
        closeWell(cut,3,5,6,7,12);
        closeWell(cut,11,13,6,8,12);
        helper.assertTrue(reachable(cut,north).contains(south) && reachable(cut,south).contains(north),
                "Central rib depends on a peripheral descent");
        for(int x=7;x<=8;x++) for(int y=15;y<=17;y++) for(int z=6;z<=7;z++)
            cut.put(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());
        helper.assertTrue(!reachable(cut,north).contains(south) && !reachable(cut,south).contains(north),
                "Central rib cut left an uncontracted bridge");
        helper.succeed();
    }

    private static void assertSlabSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                            BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(7,12,11)).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.SLAB_THIRD),"Third supported descent changed");
        helper.assertTrue(blocks.get(base.offset(10,12,7)).isAir()
                == (variant==CatalogueTerraces.Variant.SLAB_JOINED),"Long joined opening changed");
        helper.assertTrue(blocks.get(base.offset(4,12,6)).isAir()
                && blocks.get(base.offset(11,12,6)).isAir()
                && blocks.get(base.offset(8,12,10)).isAir(),"Three slab opening contours lost their views");
        helper.assertTrue(blocks.get(base.offset(9,12,11)).isAir()
                && blocks.get(base.offset(9,12,10)).is(Blocks.STONE),"Rear opening lost its distinct L-shaped contour");
        helper.assertTrue(blocks.get(base.offset(11,12,7)).isAir()
                && blocks.get(base.offset(13,12,7)).is(Blocks.LADDER)
                && blocks.get(base.offset(14,12,7)).is(Blocks.STONE),"Wide eastern opening lost its backed descent");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void hourglassFunnelsShareAnOffsetWaistAndSupportedReturns(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.HOURGLASS,
                CatalogueTerraces.Variant.HOURGLASS_SPLIT,CatalogueTerraces.Variant.HOURGLASS_BALCONY}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertHourglassSignatures(helper,geometry,BlockPos.ZERO,variant);
            helper.assertTrue(geometry.get(new BlockPos(5,4,4)).isAir()
                    && geometry.get(new BlockPos(4,16,4)).isAir()
                    && solid(geometry,new BlockPos(4,10,4)),"Two broad basins lost their narrow middle section");
            helper.assertTrue(geometry.get(new BlockPos(5,6,5)).isAir()
                    && geometry.get(new BlockPos(5,12,5)).isAir(),"Funnel flares became separate sealed rooms");
            var top=new BlockPos(7,15,7); var middle=new BlockPos(6,9,5); var bottom=new BlockPos(7,3,7);
            for(var end:java.util.List.of(middle,bottom)) helper.assertTrue(reachable(geometry,top).contains(end)
                    && reachable(geometry,end).contains(top),"Hourglass lacks a supported basin/waist return");
            for(int x=6;x<=7;x++) for(int z=1;z<=7;z++)
                helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Upper shoulder approach narrowed or lost support");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void hourglassNecksHaveExactlyTheirEquippedVerticalConnections(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.HOURGLASS,
                CatalogueTerraces.Variant.HOURGLASS_SPLIT,CatalogueTerraces.Variant.HOURGLASS_BALCONY}) {
            var geometry=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var cut=new LinkedHashMap<>(geometry);
            var top=new BlockPos(7,15,7); var bottom=new BlockPos(7,3,7);
            boolean split=variant==CatalogueTerraces.Variant.HOURGLASS_SPLIT;
            boolean balcony=variant==CatalogueTerraces.Variant.HOURGLASS_BALCONY;
            if(split) closeWell(cut,5,6,6,7,12);
            else if(balcony) closeWell(cut,11,12,8,9,12);
            else closeWell(cut,8,9,6,7,12);
            helper.assertTrue(reachable(cut,top).contains(bottom)==split
                    && reachable(cut,bottom).contains(top)==split,"Hourglass has an incorrect number of vertical returns");
            if(split) {
                closeWell(cut,9,10,6,7,12);
                helper.assertTrue(!reachable(cut,top).contains(bottom) && !reachable(cut,bottom).contains(top),
                        "Double neck retained an uncontracted third descent");
            }
            if(balcony) {
                for(int x=6;x<=10;x++) for(int z=8;z<=9;z++)
                    helper.assertTrue(stand(geometry,new BlockPos(x,15,z)),"Overhanging balcony lost its supported surface");
                helper.assertTrue(geometry.get(new BlockPos(9,13,8)).isAir()
                        && geometry.get(new BlockPos(7,12,7)).isAir(),"Balcony filled the visual opening below");
            }
        }
        helper.succeed();
    }

    private static void assertHourglassSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                 BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(7,10,7)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.HOURGLASS_SPLIT),"Bifid neck lost its central blade");
        helper.assertTrue(blocks.get(base.offset(9,12,6)).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.HOURGLASS),"Single shoulder descent changed");
        helper.assertTrue(blocks.get(base.offset(5,12,6)).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.HOURGLASS_SPLIT),"Bifid neck lost its second descent");
        helper.assertTrue(blocks.getOrDefault(base.offset(12,12,8),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.HOURGLASS_BALCONY),"Balcony lateral descent changed");
        helper.assertTrue(blocks.get(base.offset(9,14,8)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.HOURGLASS_BALCONY),"Large balcony overhang changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bellCoresHangFromRoofAboveAnUnobstructedLowerReturn(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.BELL,
                CatalogueTerraces.Variant.BELL_FORK,CatalogueTerraces.Variant.BELL_WINDOW}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertBellSignatures(helper,g,BlockPos.ZERO,variant);
            for(int x=5;x<=11;x++) for(int z=6;z<=7;z++) {
                helper.assertTrue(stand(g,new BlockPos(x,3,z)),"Hanging core narrowed the supported lower crossing");
                for(int y=3;y<=5;y++) helper.assertTrue(g.get(new BlockPos(x,y,z)).isAir(),
                        "Hanging core became a pillar or blocked the three-high lower crossing");
            }
            for(int y:new int[]{6,7,10,11,16,17}) {
                int left=y<=7 ? (variant==CatalogueTerraces.Variant.BELL_FORK?5:8)
                        : (variant==CatalogueTerraces.Variant.BELL_FORK?4:7);
                int right=y<=7 ? (variant==CatalogueTerraces.Variant.BELL_WINDOW?11:10)
                        : (variant==CatalogueTerraces.Variant.BELL && y==10?10
                        : variant==CatalogueTerraces.Variant.BELL_WINDOW?12:11);
                helper.assertTrue(g.get(new BlockPos(left,y,6)).isAir()
                        && g.get(new BlockPos(right,y,6)).isAir(),"Clapper flank merged into a side wall");
            }
            var top=new BlockPos(7,15,7);var mid=new BlockPos(7,9,5);var low=new BlockPos(7,3,7);
            for(var end:java.util.List.of(mid,low)) helper.assertTrue(reachable(g,top).contains(end)
                    && reachable(g,end).contains(top),"Bell side descents lack a supported return");
            if(variant==CatalogueTerraces.Variant.BELL_WINDOW) {
                for(int y=8;y<=18;y++) helper.assertTrue(g.get(new BlockPos(8,y,6)).is(Blocks.STONE),
                        "Pierced clapper lost its continuous roof-side attachment");
                for(int y=6;y<=8;y++) helper.assertTrue(g.get(new BlockPos(9,y,6)).is(Blocks.STONE),
                        "Pierced clapper lost its free tip connection");
            } else {
                for(int y=6;y<=18;y++) helper.assertTrue(g.get(new BlockPos(9,y,6)).is(Blocks.STONE),
                        "Clapper detached from the roof");
                if(variant==CatalogueTerraces.Variant.BELL_FORK) {
                    for(int y=6;y<=18;y++) helper.assertTrue(g.get(new BlockPos(6,y,6)).is(Blocks.STONE),
                            "Second free tip detached from its common roof attachment");
                    for(int x=6;x<=9;x++) helper.assertTrue(g.get(new BlockPos(x,18,6)).is(Blocks.STONE),
                            "Bifid clapper lost its common roof attachment");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bellMiddleWindowsRemainSeparateFromTheLowerCrossing(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.BELL_FORK,
                CatalogueTerraces.Variant.BELL_WINDOW}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            boolean fork=variant==CatalogueTerraces.Variant.BELL_FORK;
            int x1=fork?7:9;
            for(int x=x1;x<=x1+1;x++) for(int z=6;z<=7;z++)
                helper.assertTrue(stand(g,new BlockPos(x,9,z)),"Middle window lacks a two-wide supported crossing");
            var cut=new LinkedHashMap<>(g);
            // Isolate the middle level from the independent side descents.
            for(int y:new int[]{6,12}) {closeWell(cut,3,4,6,7,y);closeWell(cut,12,13,4,5,y);}
            if(!fork) closeWell(cut,6,7,6,7,9);
            var north=new BlockPos(7,9,5);var south=new BlockPos(7,9,8);
            helper.assertTrue(reachable(cut,north).contains(south) && reachable(cut,south).contains(north),
                    "Middle window depends on a lower or upper bypass");
            closeWell(cut,x1,x1+1,6,7,9);
            helper.assertTrue(!reachable(cut,north).contains(south) && !reachable(cut,south).contains(north),
                    "Middle window cut retained an unintended same-level bypass");
            var lowNorth=new BlockPos(7,3,5);var lowSouth=new BlockPos(7,3,8);
            helper.assertTrue(reachable(cut,lowNorth).contains(lowSouth) && reachable(cut,lowSouth).contains(lowNorth),
                    "Middle window cut removed the distinct lower crossing");
        }
        helper.succeed();
    }

    private static void assertBellSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                             BlockPos base,CatalogueTerraces.Variant variant) {
        if(variant==CatalogueTerraces.Variant.BELL_FORK)
            helper.assertTrue(blocks.get(base.offset(6,6,6)).is(Blocks.STONE)
                    && blocks.get(base.offset(6,5,6)).isAir(),"Second hanging tip lost its free underside");
        helper.assertTrue(blocks.get(base.offset(8,9,6)).isAir()
                == (variant==CatalogueTerraces.Variant.BELL_FORK),"Space between the two tips changed");
        helper.assertTrue(blocks.get(base.offset(9,9,6)).isAir()
                == (variant==CatalogueTerraces.Variant.BELL_WINDOW),"Pierced single-body window changed");
        helper.assertTrue(blocks.get(base.offset(9,5,6)).isAir()
                && blocks.get(base.offset(9,6,6)).is(Blocks.STONE),"Clapper no longer stops above the floor");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bipyramidWidensAtTheBeltBetweenDisplacedAccessibleTips(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.BIPYRAMID,
                CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED,CatalogueTerraces.Variant.BIPYRAMID_DIVIDED}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertBipyramidSignatures(helper,g,BlockPos.ZERO,variant);
            helper.assertTrue(g.get(new BlockPos(4,10,9)).isAir() && g.get(new BlockPos(11,10,7)).isAir()
                    && solid(g,new BlockPos(4,4,9)) && solid(g,new BlockPos(4,16,9)),
                    "Bipyramid lost its broad belt and narrower ends");
            helper.assertTrue(g.get(new BlockPos(6,4,8)).isAir() && g.get(new BlockPos(8,16,5)).isAir()
                    && g.get(new BlockPos(6,7,8)).isAir() && g.get(new BlockPos(6,13,7)).isAir(),
                    "Offset tips or continuous faceted transitions were lost");
            var top=new BlockPos(7,15,7);var belt=new BlockPos(7,9,5);var low=new BlockPos(7,3,8);
            for(var end:java.util.List.of(belt,low)) helper.assertTrue(reachable(g,top).contains(end)
                    && reachable(g,end).contains(top),"Bipyramid face route lacks a supported return");
            for(int x=6;x<=7;x++) for(int z=1;z<=7;z++)
                helper.assertTrue(stand(g,new BlockPos(x,15,z)),"Upper tip approach lost its two-wide support");
            if(variant==CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED)
                for(int x=7;x<=11;x++) for(int z=8;z<=9;z++)
                    helper.assertTrue(stand(g,new BlockPos(x,15,z)),"Truncated upper platform lost its supported surface");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void bipyramidUpperLoopAndDividedLowerFaceHaveSeparateCutSets(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.BIPYRAMID,
                CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED,CatalogueTerraces.Variant.BIPYRAMID_DIVIDED}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var top=new BlockPos(7,15,7);var belt=new BlockPos(7,9,5);var low=new BlockPos(7,3,8);
            boolean truncated=variant==CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED;
            boolean divided=variant==CatalogueTerraces.Variant.BIPYRAMID_DIVIDED;
            var upper=new LinkedHashMap<>(g);closeWell(upper,11,12,6,7,12);
            helper.assertTrue(reachable(upper,top).contains(belt)==truncated
                    && reachable(upper,belt).contains(top)==truncated,"Upper face gained or lost a vertical return");
            if(truncated) {
                closeWell(upper,5,6,6,7,12);
                helper.assertTrue(!reachable(upper,top).contains(belt) && !reachable(upper,belt).contains(top),
                        "Truncated upper face retained an extra vertical connection");
            }
            var lower=new LinkedHashMap<>(g);
            if(divided) closeWell(lower,5,6,6,7,6);else closeWell(lower,2,3,6,7,6);
            helper.assertTrue(reachable(lower,belt).contains(low)==divided
                    && reachable(lower,low).contains(belt)==divided,"Lower face gained or lost its second passage");
            if(divided) {
                closeWell(lower,8,10,8,9,6);
                helper.assertTrue(!reachable(lower,belt).contains(low) && !reachable(lower,low).contains(belt),
                        "Divided lower face retained an unintended third connection");
                for(int y=6;y<=8;y++) for(int z=6;z<=9;z++)
                    helper.assertTrue(g.get(new BlockPos(7,y,z)).is(Blocks.STONE),"Lower face lost its separating rock blade");
            }
        }
        helper.succeed();
    }

    private static void assertBipyramidSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                  BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(11,16,9),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED),"Upper truncation changed");
        helper.assertTrue(blocks.get(base.offset(7,7,8)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.BIPYRAMID_DIVIDED),"Divided lower face blade changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(10,6,8),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.BIPYRAMID_DIVIDED),"Unequal second lower passage changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(2,6,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                != (variant==CatalogueTerraces.Variant.BIPYRAMID_DIVIDED),"Original lower face descent changed");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void archPiersSupportARisingDeckAboveASeparateLowerCrossing(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.ARCH,
                CatalogueTerraces.Variant.ARCH_WINDOW,CatalogueTerraces.Variant.ARCH_FORK}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertArchSignatures(helper,g,BlockPos.ZERO,variant);
            for(int z=5;z<=6;z++) {
                for(int y=2;y<=12;y++) for(int x:new int[]{3,6})
                    helper.assertTrue(g.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Thick rooted pier lost its continuous frame");
                for(int y=2;y<=14;y++) for(int x=10;x<=11;x++)
                    helper.assertTrue(g.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Thin rooted pier lost its deck connection");
            }
            for(int x=8;x<=9;x++) for(int z=4;z<=8;z++)
                helper.assertTrue(stand(g,new BlockPos(x,3,z)),"Arch lower crossing narrowed or lost its three-high clearance");
            for(int x=1;x<=2;x++) for(int z=4;z<=8;z++)
                helper.assertTrue(stand(g,new BlockPos(x,3,z)),"West lower bypass lost its supported two-wide route");
            for(int z:new int[]{4,5,7,8}) for(int x=5;x<=6;x++) {
                var stair=g.get(new BlockPos(x,x+8,z));
                helper.assertTrue(stair.is(Blocks.STONE_STAIRS) && stair.getValue(StairBlock.FACING)==Direction.EAST,
                        "Sloping deck lost its correctly oriented rise");
            }
            var west=new BlockPos(3,13,7);var east=new BlockPos(10,15,7);
            helper.assertTrue(reachable(g,west).contains(east) && reachable(g,east).contains(west),
                    "Unequal deck heights require a jump or lack a return");
            var top=new BlockPos(7,15,7);
            for(var end:java.util.List.of(new BlockPos(8,3,7),new BlockPos(8,9,7),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,top).contains(end) && reachable(g,end).contains(top),
                        "Arch lost a supported lower/middle/upper-alcove return");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void archPierWindowAndForkedDeckHaveIndependentCutSets(GameTestHelper helper) {
        var window=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.ARCH_WINDOW);
        for(int x=4;x<=5;x++) for(int z=3;z<=8;z++)
            helper.assertTrue(stand(window,new BlockPos(x,3,z)),"Pier window lacks a two-wide supported local crossing");
        var cut=new LinkedHashMap<>(window);
        closeWell(cut,3,3,3,4,3);closeWell(cut,6,6,3,4,3);
        var front=new BlockPos(4,3,4);var back=new BlockPos(4,3,7);
        helper.assertTrue(reachable(cut,front).contains(back) && reachable(cut,back).contains(front),
                "Pier window depends on its lower bypass");
        closeWell(cut,4,5,5,6,3);
        helper.assertTrue(!reachable(cut,front).contains(back) && !reachable(cut,back).contains(front),
                "Pier window cut retained an unintended crossing through its frame");
        var lowWest=new BlockPos(2,3,6);var lowEast=new BlockPos(8,3,7);
        helper.assertTrue(reachable(cut,lowWest).contains(lowEast) && reachable(cut,lowEast).contains(lowWest),
                "Pier window cut removed the separate lower arch route");
        var fork=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.ARCH_FORK);
        for(int x=7;x<=9;x++) for(int y=12;y<=17;y++)
            helper.assertTrue(fork.get(new BlockPos(x,y,6)).isAir(),"Upper deck fork lost its continuous central slit");
        var upper=new LinkedHashMap<>(fork);
        closeWell(upper,1,2,7,8,10);closeWell(upper,12,13,7,8,12);
        var west=new BlockPos(6,15,5);var east=new BlockPos(10,15,5);
        closeWell(upper,8,8,4,5,15);
        helper.assertTrue(reachable(upper,west).contains(east) && reachable(upper,east).contains(west),
                "Forked deck lost its second supported band");
        closeWell(upper,8,8,7,8,15);
        helper.assertTrue(!reachable(upper,west).contains(east) && !reachable(upper,east).contains(west),
                "Both upper bands cut retained an unintended upper bridge");
        helper.succeed();
    }

    private static void assertArchSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                             BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(4,4,5)).isAir()
                == (variant==CatalogueTerraces.Variant.ARCH_WINDOW),"Thick pier window changed");
        // X=9 lies outside the radius-two DRY residue candidates around (7,15,7).
        helper.assertTrue(blocks.get(base.offset(9,14,6)).isAir()
                == (variant==CatalogueTerraces.Variant.ARCH_FORK),"Forked deck slit changed");
        helper.assertTrue(blocks.get(base.offset(8,5,5)).isAir()
                && blocks.get(base.offset(3,5,5)).is(Blocks.STONE)
                && blocks.get(base.offset(10,5,5)).is(Blocks.STONE),"Two rooted piers no longer frame the lower arch");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void traverseKeepsAnObliqueRootedAxisAndSupportedFlankReturns(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.TRAVERSE,
                CatalogueTerraces.Variant.TRAVERSE_WINDOW,CatalogueTerraces.Variant.TRAVERSE_FORK}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertTraverseSignatures(helper,g,BlockPos.ZERO,variant);
            for(int y=2;y<=18;y++) {
                int x=y<=5?5:y<=7?5:y<=12?6:8;
                int z=y<=7?5:y<=12?6:y<=14?7:8;
                helper.assertTrue(g.get(new BlockPos(x,y,z)).is(Blocks.STONE),"Diagonal bar lost its continuous rooted outer band");
                if(y>2) {
                    int px=y-1<=5?5:y-1<=7?5:y-1<=12?6:8;
                    int pz=y-1<=7?5:y-1<=12?6:y-1<=14?7:8;
                    helper.assertTrue(g.get(new BlockPos(x,y-1,z)).is(Blocks.STONE)
                            && x>=px && z>=pz,"Adjacent diagonal slices lost their load-bearing overlap");
                }
            }
            for(int x=8;x<=9;x++) for(int z=4;z<=9;z++)
                helper.assertTrue(stand(g,new BlockPos(x,3,z)),"Lower route no longer goes around the rooted foot");
            for(int x=10;x<=11;x++) for(int z=4;z<=9;z++)
                helper.assertTrue(stand(g,new BlockPos(x,9,z)),"Middle outside flank lost its two-wide passage");
            var top=new BlockPos(7,15,7);
            for(var end:java.util.List.of(new BlockPos(8,3,8),new BlockPos(7,9,5),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,top).contains(end) && reachable(g,end).contains(top),
                        "Oblique bar requires climbing an unprepared rock face");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void traverseMiddleWindowRetainsItsFrameAndUpperForkRetainsOneFoot(GameTestHelper helper) {
        var window=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.TRAVERSE_WINDOW);
        for(int x=7;x<=8;x++) for(int z=6;z<=7;z++) {
            helper.assertTrue(stand(window,new BlockPos(x,9,z)),"Middle window lost supported clearance");
            for(int y:new int[]{8,12}) helper.assertTrue(window.get(new BlockPos(x,y,z)).is(Blocks.STONE),
                    "Middle window removed its lower or upper frame");
        }
        var cut=new LinkedHashMap<>(window);
        for(int y:new int[]{6,12}) {
            closeWell(cut,3,4,6,7,y);closeWell(cut,11,12,4,5,y);
        }
        closeWell(cut,3,4,6,7,9);closeWell(cut,10,11,6,7,9);
        var north=new BlockPos(7,9,5);var south=new BlockPos(7,9,8);
        helper.assertTrue(reachable(cut,north).contains(south) && reachable(cut,south).contains(north),
                "Window depends on outside or vertical bypasses");
        closeWell(cut,7,8,6,7,9);
        helper.assertTrue(!reachable(cut,north).contains(south) && !reachable(cut,south).contains(north),
                "Window cut retained an unintended middle crossing");
        var fork=CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.TRAVERSE_FORK);
        for(int y=15;y<=18;y++) for(int z=8;z<=9;z++) {
            for(int x:new int[]{7,8,11,12}) helper.assertTrue(fork.get(new BlockPos(x,y,z)).is(Blocks.STONE),
                    "Fork lost one of its two ceiling attachments");
            if(y<18) for(int x=9;x<=10;x++) helper.assertTrue(fork.get(new BlockPos(x,y,z)).isAir(),
                    "Fork no longer separates its two upper branches");
        }
        for(int x=7;x<=12;x++) for(int z=6;z<=7;z++)
            helper.assertTrue(stand(fork,new BlockPos(x,15,z)),"Upper fork lost its separate supported front flank");
        helper.succeed();
    }

    private static void assertTraverseSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                 BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(7,10,6)).isAir()
                == (variant==CatalogueTerraces.Variant.TRAVERSE_WINDOW),"Diagonal middle window changed");
        helper.assertTrue(blocks.get(base.offset(10,16,8)).isAir()
                == (variant==CatalogueTerraces.Variant.TRAVERSE_FORK),"Diagonal upper fork changed");
        helper.assertTrue(blocks.get(base.offset(5,2,5)).is(Blocks.STONE)
                && blocks.get(base.offset(8,18,8)).is(Blocks.STONE),"Diagonal bar lost its floor or ceiling root");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void naveWindowsDescendInSeriesWithSupportedBayReturns(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.NAVE,
                CatalogueTerraces.Variant.NAVE_DOUBLE,CatalogueTerraces.Variant.NAVE_ARCH}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertNaveSignatures(helper,g,BlockPos.ZERO,variant);
            for(var p:java.util.List.of(new BlockPos(6,15,4),new BlockPos(7,15,4),
                    new BlockPos(10,9,8),new BlockPos(11,9,8),new BlockPos(8,3,11),new BlockPos(9,3,11)))
                helper.assertTrue(stand(g,p),"Serial diaphragm window lost two-wide supported clearance");
            var entry=new BlockPos(6,15,2);
            for(var end:java.util.List.of(new BlockPos(7,15,7),new BlockPos(3,3,4),new BlockPos(7,9,4),
                    new BlockPos(3,9,10),new BlockPos(11,3,12),new BlockPos(11,9,12),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,entry).contains(end) && reachable(g,end).contains(entry),
                        "Nave bay or alcove lacks a supported return");
            var first=new LinkedHashMap<>(g);closeWell(first,6,7,4,4,15);
            helper.assertTrue(!reachable(first,entry).contains(new BlockPos(7,9,6)),
                    "First diaphragm retained an unintended bypass around its high window");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void naveSecondWindowAndLastUpperArchHaveIndependentCutSets(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.NAVE,
                CatalogueTerraces.Variant.NAVE_DOUBLE,CatalogueTerraces.Variant.NAVE_ARCH}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var entry=new BlockPos(7,15,7);var middle=new BlockPos(3,9,10);var rear=new BlockPos(11,3,12);
            var second=new LinkedHashMap<>(g);closeWell(second,10,11,8,8,9);
            helper.assertTrue(reachable(second,entry).contains(middle)==(variant==CatalogueTerraces.Variant.NAVE_DOUBLE),
                    "Middle diaphragm has the wrong number of independent windows");
            if(variant==CatalogueTerraces.Variant.NAVE_DOUBLE) {
                for(int x=8;x<=9;x++) helper.assertTrue(stand(g,new BlockPos(x,15,8)),"Upper middle window narrowed");
                closeWell(second,8,9,8,8,15);
                helper.assertTrue(!reachable(second,entry).contains(middle),"Both middle windows cut retained a bypass");
            }
            var last=new LinkedHashMap<>(g);closeWell(last,8,9,11,11,3);
            helper.assertTrue(reachable(last,entry).contains(rear)==(variant==CatalogueTerraces.Variant.NAVE_ARCH),
                    "Last diaphragm has the wrong independent upper passage");
            if(variant==CatalogueTerraces.Variant.NAVE_ARCH) {
                for(int x=7;x<=8;x++) helper.assertTrue(stand(g,new BlockPos(x,15,11)),"Upper arch lost supported headroom");
                closeWell(last,7,8,11,11,15);
                helper.assertTrue(!reachable(last,entry).contains(rear),"Both last-wall passages cut retained a bypass");
            }
        }
        helper.succeed();
    }

    private static void assertNaveSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                             BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(8,16,8)).isAir()
                == (variant==CatalogueTerraces.Variant.NAVE_DOUBLE),"Upper middle diaphragm window changed");
        helper.assertTrue(blocks.get(base.offset(7,16,11)).isAir()
                == (variant==CatalogueTerraces.Variant.NAVE_ARCH),"Last diaphragm arch changed");
        for(var p:java.util.List.of(new BlockPos(7,12,4),new BlockPos(10,12,8),new BlockPos(8,6,11)))
            helper.assertTrue(!blocks.getOrDefault(base.offset(p),Blocks.STONE.defaultBlockState()).isAir(),
                    "Diaphragm lost its separating frame above a window");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void trihedralSheetsRemainPerpendicularWithSupportedFootAndShoulderRoutes(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.TRIHEDRAL,
                CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER,CatalogueTerraces.Variant.TRIHEDRAL_PIERCED}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertTrihedralSignatures(helper,g,BlockPos.ZERO,variant);
            for(int y:new int[]{3,9,15}) {
                for(int x=5;x<=6;x++) for(int z=4;z<=12;z++) {
                    if(variant==CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER && y==9 && z<=5) continue;
                    helper.assertTrue(stand(g,new BlockPos(x,y,z)),"West sheet lost its supported two-wide route");
                }
                for(int x=5;x<=12;x++) for(int z=4;z<=5;z++) {
                    if(variant==CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER && y==9 && x<=6) continue;
                    helper.assertTrue(stand(g,new BlockPos(x,y,z)),"North sheet lost its supported two-wide route");
                }
            }
            var top=new BlockPos(7,15,7);
            for(var end:java.util.List.of(new BlockPos(6,3,8),new BlockPos(6,9,9),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,top).contains(end) && reachable(g,end).contains(top),
                        "Trihedral planes lack a supported return");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void trihedralChamferAndDiagonalPiercingKeepDistinctMiddleConnections(GameTestHelper helper) {
        var north=new BlockPos(10,9,4);var west=new BlockPos(6,9,9);
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER,
                CatalogueTerraces.Variant.TRIHEDRAL_PIERCED}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var cut=new LinkedHashMap<>(g);
            for(int y:new int[]{6,12}) {
                closeWell(cut,5,6,6,7,y);closeWell(cut,10,11,5,6,y);
            }
            closeWell(cut,5,6,4,5,9);
            helper.assertTrue(reachable(cut,north).contains(west) && reachable(cut,west).contains(north),
                    "Oblique middle route depends on the sharp edge or vertical return");
            if(variant==CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER) closeWell(cut,6,8,6,6,9);
            else closeWell(cut,7,10,7,7,9);
            helper.assertTrue(!reachable(cut,north).contains(west) && !reachable(cut,west).contains(north),
                    "Oblique route cut retained an unintended middle bypass");
            helper.assertTrue(reachable(cut,new BlockPos(6,3,9)).contains(new BlockPos(10,3,5)),
                    "Middle cut removed the independent lower gallery");
        }
        helper.succeed();
    }

    private static void assertTrihedralSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                  BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(6,9,4)).is(Blocks.STONE)
                == (variant==CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER),"Middle sharp edge/chamfer changed");
        helper.assertTrue(blocks.get(base.offset(9,9,7)).isAir()
                == (variant==CatalogueTerraces.Variant.TRIHEDRAL_PIERCED),"Opposite-corner piercing changed");
        helper.assertTrue(blocks.get(base.offset(9,12,9)).is(Blocks.STONE)
                && blocks.get(base.offset(9,7,8)).is(Blocks.STONE),"Opposite corner lost its retained host-rock mass");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void nestedShellsKeepTwoSupportedDomainsAndTheCanonicalOutsideAlcoves(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.SHELLS,
                CatalogueTerraces.Variant.SHELLS_OPPOSED,CatalogueTerraces.Variant.SHELLS_OPEN}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertShellSignatures(helper,g,BlockPos.ZERO,variant);
            var outside=new BlockPos(7,15,7);var inside=new BlockPos(7,4,9);
            for(var end:java.util.List.of(inside,new BlockPos(8,9,8),new BlockPos(3,3,4),
                    new BlockPos(11,3,12),new BlockPos(3,9,10),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,outside).contains(end) && reachable(g,end).contains(outside),
                        "Nested shell lost a supported inner/outer return");
            for(int x=6;x<=9;x++) for(int z=7;z<=9;z++) {
                helper.assertTrue(stand(g,new BlockPos(x,4,z)),"Inner lower domain lost three-high support");
                helper.assertTrue(stand(g,new BlockPos(x,9,z)),"Inner upper landing lost three-high support");
                helper.assertTrue(g.get(new BlockPos(x,12,z)).is(Blocks.STONE),"Inner ceiling merged with the outer cap route");
            }
            int z1=variant==CatalogueTerraces.Variant.SHELLS_OPEN?7:8;
            for(int z=z1;z<=z1+1;z++) helper.assertTrue(stand(g,new BlockPos(10,9,z)),"High shell window narrowed");
            if(variant==CatalogueTerraces.Variant.SHELLS_OPPOSED) for(int z=8;z<=9;z++)
                helper.assertTrue(stand(g,new BlockPos(5,4,z)),"Opposite low shell window narrowed");
            if(variant==CatalogueTerraces.Variant.SHELLS_OPEN) {
                for(int z=7;z<=8;z++) helper.assertTrue(stand(g,new BlockPos(10,4,z)),"Opened-sector low approach narrowed");
                for(int y=4;y<=11;y++) for(int z:new int[]{6,9})
                    helper.assertTrue(g.get(new BlockPos(10,y,z)).is(Blocks.STONE),"Opened sector lost a non-mineral structural lip");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void nestedShellWindowCutsSeparateTheInteriorWithoutClosingTheOutsideReturn(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.SHELLS,
                CatalogueTerraces.Variant.SHELLS_OPPOSED,CatalogueTerraces.Variant.SHELLS_OPEN}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            var outside=new BlockPos(7,15,7);var inside=new BlockPos(7,4,9);
            var cut=new LinkedHashMap<>(g);
            int z1=variant==CatalogueTerraces.Variant.SHELLS_OPEN?7:8;
            closeWell(cut,10,10,z1,z1+1,9);
            helper.assertTrue(reachable(cut,outside).contains(inside)==(variant!=CatalogueTerraces.Variant.SHELLS),
                    "Inner shell has the wrong low return after its high window is cut");
            if(variant==CatalogueTerraces.Variant.SHELLS_OPPOSED) closeWell(cut,5,5,8,9,4);
            if(variant==CatalogueTerraces.Variant.SHELLS_OPEN) closeWell(cut,10,10,7,8,4);
            helper.assertTrue(!reachable(cut,outside).contains(inside) && !reachable(cut,inside).contains(outside),
                    "Closed shell windows retained an unintended wall or roof opening");
            for(var end:java.util.List.of(new BlockPos(11,3,12),new BlockPos(3,9,10),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(cut,outside).contains(end) && reachable(cut,end).contains(outside),
                        "Inner-window cuts removed the independent outside return");
        }
        helper.succeed();
    }

    private static void assertShellSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                               BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.get(base.offset(5,5,8)).isAir()
                == (variant==CatalogueTerraces.Variant.SHELLS_OPPOSED),"Opposite low window changed");
        helper.assertTrue(blocks.get(base.offset(10,6,7)).isAir()
                == (variant==CatalogueTerraces.Variant.SHELLS_OPEN),"Opened shell sector changed");
        helper.assertTrue(blocks.get(base.offset(8,5,8)).isAir()
                && blocks.get(base.offset(8,10,8)).isAir()
                && blocks.get(base.offset(8,12,8)).is(Blocks.STONE),"Inner domain or separating roof disappeared");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void brokenAnnulusKeepsItsCoreAndSupportedUnequalArcs(GameTestHelper helper) {
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.ANNULUS,
                CatalogueTerraces.Variant.ANNULUS_BRIDGE,CatalogueTerraces.Variant.ANNULUS_CRESCENTS}) {
            var g=CatalogueTerraces.geometry(new BlockPos(7,15,7),variant);
            assertAnnulusSignatures(helper,g,BlockPos.ZERO,variant);
            for(int x=7;x<=9;x++) for(int z=7;z<=9;z++) for(int y=6;y<=14;y++)
                helper.assertTrue(g.get(new BlockPos(x,y,z)).is(y==14 && x==7 && z==7?Blocks.CALCITE:Blocks.STONE),
                        "Broken annulus excavated or mineralized its retained core");
            for(int x=3;x<=4;x++) for(int z=4;z<=12;z++)
                helper.assertTrue(stand(g,new BlockPos(x,3,z)),"Lower arc lost its two-wide supported return");
            for(int x=5;x<=11;x++) for(int z=4;z<=5;z++)
                helper.assertTrue(stand(g,new BlockPos(x,15,z)),"Upper arc lost its two-wide supported return");
            var top=new BlockPos(7,15,7);
            for(var end:java.util.List.of(new BlockPos(7,9,4),new BlockPos(3,3,6),new BlockPos(3,15,12)))
                helper.assertTrue(reachable(g,top).contains(end) && reachable(g,end).contains(top),
                        "Unequal arcs do not have a supported return");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void annulusBridgeClosesItsOwnLoopAndCrescentsUseOnlyTheSideNeck(GameTestHelper helper) {
        var front=new BlockPos(7,15,4);var rear=new BlockPos(5,15,11);
        for(var variant:new CatalogueTerraces.Variant[]{CatalogueTerraces.Variant.ANNULUS,
                CatalogueTerraces.Variant.ANNULUS_BRIDGE}) {
            var cut=new LinkedHashMap<>(CatalogueTerraces.geometry(new BlockPos(7,15,7),variant));
            closeWell(cut,5,6,11,12,12);closeWell(cut,11,12,8,9,12);
            helper.assertTrue(reachable(cut,front).contains(rear)==(variant==CatalogueTerraces.Variant.ANNULUS_BRIDGE),
                    "Upper oblique bridge does not distinguish the closed loop");
            if(variant==CatalogueTerraces.Variant.ANNULUS_BRIDGE) {
                closeWell(cut,5,6,10,10,15);
                helper.assertTrue(!reachable(cut,front).contains(rear) && !reachable(cut,rear).contains(front),
                        "Bridge cut retained an unintended upper bypass");
            }
        }
        var c=new LinkedHashMap<>(CatalogueTerraces.geometry(new BlockPos(7,15,7),CatalogueTerraces.Variant.ANNULUS_CRESCENTS));
        var bottom=new BlockPos(3,3,6);
        helper.assertTrue(reachable(c,front).contains(bottom),"Side neck missing before cut");
        closeWell(c,3,4,6,7,12);
        helper.assertTrue(!reachable(c,front).contains(bottom) && !reachable(c,bottom).contains(front),
                "Two crescents retained a central bridge or second vertical neck");
        helper.assertTrue(reachable(c,bottom).contains(new BlockPos(7,9,4)),
                "Side-neck cut removed the independent lower crescent");
        helper.succeed();
    }

    private static void assertAnnulusSignatures(GameTestHelper helper,Map<BlockPos,BlockState> blocks,
                                                 BlockPos base,CatalogueTerraces.Variant variant) {
        helper.assertTrue(blocks.getOrDefault(base.offset(7,15,9),Blocks.STONE.defaultBlockState()).isAir()
                == (variant==CatalogueTerraces.Variant.ANNULUS_BRIDGE),"Oblique bridge signature changed");
        helper.assertTrue(blocks.getOrDefault(base.offset(3,12,6),Blocks.STONE.defaultBlockState()).is(Blocks.LADDER)
                == (variant==CatalogueTerraces.Variant.ANNULUS_CRESCENTS),"Side-neck signature changed");
        helper.assertTrue(blocks.get(base.offset(8,10,8)).is(Blocks.STONE),"Non-mineral annulus core changed");
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
