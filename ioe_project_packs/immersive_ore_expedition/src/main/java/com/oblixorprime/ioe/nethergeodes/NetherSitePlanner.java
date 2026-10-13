package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.*;

/** Pure planning over an immutable terrain snapshot. No level access, writes, tickets or retries. */
final class NetherSitePlanner {
    static final int WIDTH = NetherLakeWindow.WIDTH, COLUMNS = NetherLakeWindow.COLUMNS;
    static final int ANCHOR_INDEX = -NetherLakeWindow.MIN_OFFSET;
    static final int MAX_PROBES = 262_144;
    record Candidate(int x, int z, SiteQuality quality, long shapeSeed, int debrisRoll, int soulQualityRoll) {
        Candidate {
            if (soulQualityRoll < 0 || soulQualityRoll >= 388) throw new IllegalArgumentException("Invalid soul quality roll");
            if (debrisRoll < 0 || debrisRoll >= 1000) throw new IllegalArgumentException("Invalid debris roll");
        }
        SiteQuality quality(boolean soulSoilFloor) { return soulSoilFloor ? soulQualityAt(soulQualityRoll) : quality; }
        boolean debrisSelected(boolean soulSoilFloor) { return debrisAt(quality(soulSoilFloor), debrisRoll, soulSoilFloor); }
    }
    record Cell(BlockState state, boolean protectedBlock) { }
    interface Snapshot {
        boolean nether();
        int minY();
        int maxY();
        Cell at(BlockPos pos); // null means unknown; callers must not load chunks to answer this.
    }
    enum Status { PLANNED, WRONG_DIMENSION, UNKNOWN_TERRAIN, BUDGET, SURFACE,
        COVERAGE, FLOOR, HEIGHT, CRUST_OR_PROTECTION, GRAVEL_SUPPORT, NO_SHORE }
    record VolumeRejection(BlockPos center, BlockPos position, Cell cell) { }
    record Outcome(Status status, int probes, int connectedDeepColumns, NetherPlacementCoordinator.Plan plan,
                   SiteQuality quality, VolumeRejection volumeRejection) {
        Outcome(Status status, int probes, int connectedDeepColumns, NetherPlacementCoordinator.Plan plan, SiteQuality quality) {
            this(status, probes, connectedDeepColumns, plan, quality, null);
        }
        Outcome(Status status, int probes, int connectedDeepColumns, NetherPlacementCoordinator.Plan plan) {
            this(status, probes, connectedDeepColumns, plan, null);
        }
    }
    private static final class Stop extends RuntimeException {
        final Status status;
        final VolumeRejection volumeRejection;
        Stop(Status status) { this(status, null); }
        Stop(Status status, VolumeRejection volumeRejection) {
            this.status = status; this.volumeRejection = volumeRejection;
        }
    }
    private static final class Reader {
        final Snapshot snapshot;
        int probes;
        final Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        final Set<BlockPos> protectedCells = new HashSet<>();
        Reader(Snapshot snapshot) { this.snapshot = snapshot; }
        Cell read(BlockPos pos) {
            if (probes == MAX_PROBES) throw new Stop(Status.BUDGET);
            probes++;
            if (pos.getY() < snapshot.minY() || pos.getY() >= snapshot.maxY()) throw new Stop(Status.HEIGHT);
            var cached = states.get(pos);
            if (cached != null) return new Cell(cached, protectedCells.contains(pos));
            Cell cell = snapshot.at(pos);
            if (cell == null) throw new Stop(Status.UNKNOWN_TERRAIN);
            var key = pos.immutable();
            states.put(key, cell.state());
            if (cell.protectedBlock()) protectedCells.add(key);
            return cell;
        }
    }
    static Candidate candidate(long worldSeed, int regionX, int regionZ) {
        long seed = worldSeed ^ (long) regionX * 0x632BE59BD9B4E019L ^ (long) regionZ * 0x9E3779B97F4A7C15L;
        var random = RandomSource.create(seed);
        int x = Math.addExact(Math.multiplyExact(regionX, 256), random.nextInt(16) * 16 + 8);
        int z = Math.addExact(Math.multiplyExact(regionZ, 256), random.nextInt(16) * 16 + 8);
        SiteQuality quality = SiteQualityRoll.DEFAULT.roll(random);
        // Independent stream leaves all existing ordinary-site draws and shape seeds unchanged.
        return new Candidate(x, z, quality, random.nextLong(), random.nextInt(1000),
                RandomSource.create(seed ^ 0xD1B54A32D192ED03L).nextInt(388));
    }
    static SiteQuality soulQualityAt(int roll) {
        if (roll < 0 || roll >= 388) throw new IllegalArgumentException("Invalid soul quality roll");
        // Exact weights 30:75:135:51:97; 97/388 = 25%, others retain 10:25:45:17.
        if (roll < 30) return SiteQuality.DRY;
        if (roll < 105) return SiteQuality.POOR;
        if (roll < 240) return SiteQuality.NORMAL;
        if (roll < 291) return SiteQuality.RICH;
        return SiteQuality.MOTHERLODE;
    }
    static boolean debrisAt(SiteQuality quality, int roll) { return debrisAt(quality, roll, false); }
    static boolean debrisAt(SiteQuality quality, int roll, boolean soulSoilFloor) {
        if (roll < 0 || roll >= 1000) throw new IllegalArgumentException("Invalid debris roll");
        return quality == SiteQuality.MOTHERLODE && roll < (soulSoilFloor ? 100 : 5);
    }
    static int budget(SiteQuality quality) {
        return switch (quality) { case DRY -> 0; case POOR -> 12; case NORMAL -> 20; case RICH -> 30; case MOTHERLODE -> 49; };
    }
    static boolean enoughCoverage(int count) { return enoughCoverage(count, 60); }
    static boolean enoughCoverage(int count, int coveragePercent) {
        if (coveragePercent != 60 && coveragePercent != 40) throw new IllegalArgumentException("Unsupported coverage study");
        return (long) count * 100 >= (long) COLUMNS * coveragePercent;
    }
    private static boolean lava(Cell cell) { return cell.state().getFluidState().is(Fluids.LAVA) || cell.state().getFluidState().is(Fluids.FLOWING_LAVA); }
    private static boolean source(Cell cell) { return lava(cell) && cell.state().getFluidState().isSource(); }
    static boolean naturalSubstrate(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL);
    }
    private static boolean rock(Cell cell) {
        return !cell.protectedBlock() && cell.state().getFluidState().isEmpty()
                && !cell.state().hasBlockEntity() && naturalSubstrate(cell.state());
    }
    static boolean floorRock(Cell cell) { return rock(cell); }
    private static boolean shoreRock(Cell cell) {
        return rock(cell) && (cell.state().is(Blocks.NETHERRACK)
                || cell.state().is(Blocks.BASALT) || cell.state().is(Blocks.BLACKSTONE));
    }
    private static BlockPos pos(Candidate candidate, int y, int index) {
        return new BlockPos(candidate.x() + index % WIDTH - ANCHOR_INDEX, y, candidate.z() + index / WIDTH - ANCHOR_INDEX);
    }
    /** Select once, before eligibility checks; every selection observation stays in the same Reader. */
    static Outcome planLowest(Candidate candidate, Snapshot snapshot) {
        return planLowest(candidate, snapshot, 60);
    }
    /** Explicit qualification-only alternative; normal callers retain the approved 60% rule. */
    static Outcome planLowest(Candidate candidate, Snapshot snapshot, int coveragePercent) {
        enoughCoverage(0, coveragePercent);
        var reader = new Reader(snapshot);
        if (!snapshot.nether()) return new Outcome(Status.WRONG_DIMENSION, 0, 0, null);
        try {
            for (int y = snapshot.minY(); y < snapshot.maxY() - 1; y++) {
                var pos = new BlockPos(candidate.x(), y, candidate.z());
                if (source(reader.read(pos)) && !lava(reader.read(pos.above())))
                    return plan(candidate, y, reader, coveragePercent);
            }
            return new Outcome(Status.SURFACE, reader.probes, 0, null);
        } catch (Stop stop) {
            return new Outcome(stop.status, reader.probes, 0, null);
        }
    }

    static Outcome plan(Candidate candidate, int surfaceY, Snapshot snapshot) {
        return plan(candidate, surfaceY, snapshot, 60);
    }
    static Outcome plan(Candidate candidate, int surfaceY, Snapshot snapshot, int coveragePercent) {
        enoughCoverage(0, coveragePercent);
        return plan(candidate, surfaceY, new Reader(snapshot), coveragePercent);
    }
    private static Outcome plan(Candidate candidate, int surfaceY, Reader reader, int coveragePercent) {
        int count = 0;
        if (!reader.snapshot.nether()) return new Outcome(Status.WRONG_DIMENSION, 0, 0, null);
        try {
            boolean[] surface = new boolean[COLUMNS], deep = new boolean[COLUMNS];
            for (int i = 0; i < COLUMNS; i++) {
                var pos = pos(candidate, surfaceY, i);
                if (!source(reader.read(pos)) || lava(reader.read(pos.above()))) continue;
                surface[i] = true;
                deep[i] = lava(reader.read(pos.below())) && lava(reader.read(pos.below(2))) && lava(reader.read(pos.below(3)));
            }
            int middle = ANCHOR_INDEX * WIDTH + ANCHOR_INDEX;
            if (!surface[middle]) return new Outcome(Status.SURFACE, reader.probes, 0, null);
            boolean[] connected = new boolean[COLUMNS];
            int[] queue = new int[COLUMNS]; int head = 0, tail = 0;
            queue[tail++] = middle; connected[middle] = true;
            while (head < tail) {
                int i = queue[head++]; if (deep[i]) count++;
                for (int direction = 0; direction < 4; direction++) {
                    int x = i % WIDTH + (direction == 0 ? -1 : direction == 1 ? 1 : 0);
                    int z = i / WIDTH + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
                    if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH) continue;
                    int next = z * WIDTH + x;
                    if (surface[next] && !connected[next]) { connected[next] = true; queue[tail++] = next; }
                }
            }
            if (!enoughCoverage(count, coveragePercent)) return new Outcome(Status.COVERAGE, reader.probes, count, null);
            int floor = surfaceY;
            boolean soulSoilFloor = false;
            for (int z = -7; z <= 7; z++) for (int x = -7; x <= 7; x++) {
                int y = surfaceY;
                Cell cell = reader.read(new BlockPos(candidate.x() + x, y, candidate.z() + z));
                if (!lava(cell)) throw new Stop(Status.FLOOR);
                while (lava(cell)) {
                    y--;
                    cell = reader.read(new BlockPos(candidate.x() + x, y, candidate.z() + z));
                }
                if (!floorRock(cell)) throw new Stop(Status.FLOOR);
                soulSoilFloor |= cell.state().is(Blocks.SOUL_SOIL);
                floor = Math.min(floor, y);
            }
            BlockPos center = new BlockPos(candidate.x(), floor - 16, candidate.z());
            var writes = new LinkedHashMap<BlockPos, BlockState>();
            var orePositions = new ArrayList<BlockPos>();
            var intactBedrock = new HashMap<BlockPos, Cell>();
            // Solid cube enclosing radius-3 cavity gives >=3 intact solid cells in every direction.
            for (int z = -7; z <= 7; z++) for (int y = -7; y <= 7; y++) for (int x = -7; x <= 7; x++) {
                BlockPos pos = center.offset(x, y, z); var cell = reader.read(pos);
                int squared = x * x + y * y + z * z;
                boolean bedrock = cell.state().is(Blocks.BEDROCK) && !cell.protectedBlock()
                        && cell.state().getFluidState().isEmpty() && !cell.state().hasBlockEntity();
                if (!rock(cell) && !(bedrock && squared > 9)) throw new Stop(Status.CRUST_OR_PROTECTION,
                        new VolumeRejection(center, pos, cell));
                if (bedrock) intactBedrock.put(pos, cell);
                if (squared <= 9) writes.put(pos, Blocks.AIR.defaultBlockState());
                else if (squared <= 25) orePositions.add(pos);
            }
            var quality = candidate.quality(soulSoilFloor);
            var random = RandomSource.create(candidate.shapeSeed());
            for (int i = orePositions.size() - 1; i > 0; i--) Collections.swap(orePositions, i, random.nextInt(i + 1));
            for (int i = 0; i < budget(quality); i++) {
                var pos = orePositions.get(i);
                // Keep the original full shell and deterministic draw. Never skip/reroll bedrock.
                if (intactBedrock.containsKey(pos)) throw new Stop(Status.CRUST_OR_PROTECTION,
                        new VolumeRejection(center, pos, intactBedrock.get(pos)));
                writes.put(pos, i == 0 && candidate.debrisSelected(soulSoilFloor)
                        ? Blocks.ANCIENT_DEBRIS.defaultBlockState() : Blocks.NETHER_QUARTZ_ORE.defaultBlockState());
            }
            // Validate final support, including retained gravel stacks crossing the inspected cube.
            // Every additional support observation shares this Reader's budget and complete read set.
            for (var entry : new ArrayList<>(reader.states.entrySet())) {
                if (!entry.getValue().is(Blocks.GRAVEL) || writes.containsKey(entry.getKey())) continue;
                var support = entry.getKey().below();
                while (true) {
                    var observed = reader.read(support);
                    var finalState = writes.getOrDefault(support, observed.state());
                    if (net.minecraft.world.level.block.FallingBlock.isFree(finalState))
                        throw new Stop(Status.GRAVEL_SUPPORT, new VolumeRejection(center, support, observed));
                    if (!finalState.is(Blocks.GRAVEL)) break;
                    support = support.below();
                }
            }
            BlockPos shore = null; int closest = Integer.MAX_VALUE;
            for (int i = 0; i < COLUMNS; i++) {
                if (surface[i]) continue;
                int x = i % WIDTH, z = i / WIDTH;
                boolean adjacent = x > 0 && connected[i - 1] || x + 1 < WIDTH && connected[i + 1]
                        || z > 0 && connected[i - WIDTH] || z + 1 < WIDTH && connected[i + WIDTH];
                if (!adjacent) continue;
                var pos = pos(candidate, surfaceY, i);
                int distance = (x - ANCHOR_INDEX) * (x - ANCHOR_INDEX) + (z - ANCHOR_INDEX) * (z - ANCHOR_INDEX);
                if (distance >= closest || !shoreRock(reader.read(pos))) continue;
                var above = reader.read(pos.above()); var top = reader.read(pos.above(2));
                if (!above.state().isAir() || above.protectedBlock() || !top.state().isAir() || top.protectedBlock()) continue;
                shore = pos; closest = distance;
            }
            if (shore == null) throw new Stop(Status.NO_SHORE);
            for (var pos : List.of(shore, shore.above(), shore.above(2))) reader.read(pos);
            writes.put(shore.above(), Blocks.BLACKSTONE.defaultBlockState());
            writes.put(shore.above(2), Blocks.BLACKSTONE.defaultBlockState());
            return new Outcome(Status.PLANNED, reader.probes, count,
                    new NetherPlacementCoordinator.Plan(center, reader.states, writes, reader.protectedCells, reader.states.size()), quality);
        } catch (Stop stop) { return new Outcome(stop.status, reader.probes, count, null, null, stop.volumeRejection); }
    }
}
