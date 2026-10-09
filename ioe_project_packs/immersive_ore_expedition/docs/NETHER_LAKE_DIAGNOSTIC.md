# Nether lava-lake diagnostic — first bounded increment

Implements the detector/debug-command direction in the historical Nether roadmap.
This is measurement infrastructure, not geode generation or a stable 2.0 release.

## Command and strategy

`/ioe diagnose lava_lake <radius> <depth>` requires operator permission level 2
and the existing IOE root-command permission gate. It is registered only when at
least one existing locate-command setting is enabled; disabling all configured
commands still leaves only `status`. It samples the command source's
current dimension and block position (use vanilla `/execute positioned` to choose Y).
Radius is 1–32, depth is 1–16; no default placement thresholds are introduced.

Strategy: every column in the inclusive `(2r+1)²` square at the exact source Y,
ordered by Z then X. Source and flowing fluids in `minecraft:lava` count as lava.
For each column, measure contiguous lava downward from that plane, stopping at the
first non-lava cell or depth cap. Dry surface columns do not search for hidden lava.
There is no search for a lake surface, connectivity test or safe-crust assessment.

The report exposes completed/requested columns, lava columns among completed columns,
minimum/maximum observed depth among those lava columns, capped-column count and reads.
Zero lava columns yields depth zero. A capped depth means **at least** that depth;
it does not locate the bottom. These raw measurements are not automatically converted
to a `LavaLakeAnchorSample` or compared to the historical placement thresholds.

## Safety and cost

Only the Nether is accepted. The full requested vertical interval must be within build
height. Invalid dimensions/heights perform zero cell reads. Each scan permits at most
65,536 cell probes; a probe obtains only an already loaded chunk via `getChunkNow`.
The first missing chunk or exhausted budget stops the scan. The partially inspected
column is excluded from the counters. Reports distinguish `COMPLETE`, `UNLOADED`,
`BUDGET_EXHAUSTED`, `WRONG_DIMENSION` and `OUTSIDE_HEIGHT`; incomplete data must not be
interpreted as whole-footprint coverage. The largest all-lava request can exhaust the
budget intentionally. There are no block writes, chunk tickets, forced chunk loads,
saved-data updates, retrogen or automatic scans. All real diagnostic calls on one server share a total of 65,536 probes per server
tick. Repeated commands cannot multiply that allowance; later requests report
`BUDGET_EXHAUSTED` with their own actual read count. The command is synchronous
and restricted to the server thread. A new tick refreshes the shared allowance.

## Validation and next decisions

Unit tests cover mixed/dry/capped columns, depth and argument bounds, wrong dimensions,
missing cells, immutable report coordinates and exact budget exhaustion. A hosted
GameTest exercises the real Nether fluid reader and registered command, confirms an
unloaded chunk remains unloaded and checks that fixture blocks remain unchanged.
This does not prove manual server/client usability or natural lake placement quality.

Before a placement increment, decide the sampling plane/surface search and whether
coverage/depth refer to this square or to a connected lake; then specify safe crust,
access, generation frequency and physical resource budgets. The approved canonical analysis now uses exactly 74x74, coverage 60% and minimum
depth four; see `NETHER_PLACEMENT_CONTRACT.md`. The older radius-based command is a
separate measurement tool, not the canonical eligibility calculation. The direct
`GiantLavaLakeDetector.isValidAnchor(WorldGenLevel, BlockPos)` remains fail-closed;
this diagnostic supplies observations, not permission to place a geode.
