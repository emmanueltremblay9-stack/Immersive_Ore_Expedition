# Nether native pipeline feasibility decision

## Scope of this historical strict-atomicity review

The 2026-10-09 19:14:50 UTC owner decision permits best-effort compensation and
permanent partial sites after invalidation. See `NETHER_PLACEMENT_CONTRACT.md`.
The findings below still describe native ownership limits, but strict atomicity
is no longer the only permitted backend contract. Automatic generation stays off;
this review must not imply the new compensation backend is an atomic publisher.

## Decision

Updated for the user's exact **74x74** choice at 2026-10-09 15:58 UTC. The earlier
47x47/same-chunk-shore proposal was not accepted. All other contract rules stand.


**An ordinary native Feature, Structure/StructurePiece, or ChunkEvent.Load hook is
not a conforming backend for the complete approved contract on the pinned pipeline.**
This is now grounded in concrete terrain-access and publication boundaries below.
No production prototype is introduced, and generation remains disabled.

This conclusion is about those integration points with the existing generation
steps. It is NOT a proof that a custom Minecraft/NeoForge scheduler could never
implement the contract. A scheduler/ownership integration would be a separate,
substantial backend, not another diagnostic or a Feature registration.

## Exact inputs and inspection method

Repository properties pin Minecraft **1.21.1**, NeoForge **21.1.230**. The exact
NeoForge userdev config pins NeoForm **1.21.1-20240808.144430**. See
[NETHER_PIPELINE_AUDIT_EVIDENCE.json](NETHER_PIPELINE_AUDIT_EVIDENCE.json) for primary
artifact URLs, verified Minecraft SHA-1 values, NeoForge SHA-256 values and class names.

Inspection used the official Mojang server bundle and server mappings, extracting
its nested version JAR, then the already available JDK's disassembler:

```sh
java --module jdk.jdeps/com.sun.tools.javap.Main -c -p -classpath server-inner.jar dvy
java --module jdk.jdeps/com.sun.tools.javap.Main -v -p -classpath server-inner.jar dvy
```

Repeat for the mapped class names in the evidence file. The `-v` bootstrap table
resolves lambda targets; official mappings resolve methods/fields and original
source line ranges. Exact NeoForge userdev patches and sources were inspected too.
This is static inspection, not a local build, installation, or Minecraft launch.
No third-party decompilation or moving branch is required for these conclusions.

## Concrete boundary: FEATURES

The exact call chain is:

`ChunkStep.apply` -> `ChunkStatusTasks.generateFeatures` ->
`ChunkGenerator.applyBiomeDecoration` -> native structure/feature placement.

- `ChunkPyramid.GENERATION_PYRAMID`, mapped source lines 42-46, configures the
  FEATURES step with `addRequirement(STRUCTURE_STARTS, 8)`,
  `addRequirement(CARVERS, 1)`, and `blockStateWriteRadius(1)`.
  In bytecode `dvy.q`, offsets 1/4/6 add STARTS/8; 9/12/13 add CARVERS/1;
  16/17 set write radius 1. Bootstrap #20 maps the FEATURES initializer to `q`;
  its task bootstrap #6 resolves to `dwb.i`, `generateFeatures`.
- `WorldGenRegion.getChunk(int,int,ChunkStatus,boolean)`, mapped lines 117-141,
  computes chessboard distance and consults the step's **direct** dependencies.
  It checks the requested status against the permitted status at that distance,
  then calls `GenerationChunkHolder.getChunkIfPresentUnchecked` for that dependency.
  Otherwise it throws the unavailable-during-generation error. Passing `false`
  is not an escape hatch: this implementation does not branch on that boolean.
- The two-int `getChunk` requests EMPTY. A successful plain block read outside the
  CARVERS neighborhood therefore does not certify fully formed terrain there.
  A farther holder may incidentally contain more advanced mutable data; the step
  neither guarantees that terrain nor grants a stable snapshot of it.
- `WorldGenRegion.ensureCanWrite`, mapped lines 243-262, compares absolute chunk
  offsets with `ChunkStep.blockStateWriteRadius`; offsets beyond 1 return false.
  Additional generation-height restrictions can also reject writes.

IOE's candidate uses chunk-local X/Z = 8. The exact 74-wide window uses offsets
-37..+36, spanning chunk offsets -2..+2 on each axis: **5x5 = 25 chunks**, not the
guaranteed CARVERS 3x3. Other alignments span five or six chunks per axis, hence
25/30/36 total; canonical candidates remain aligned at 8. The radius-eight
structure-start dependency is metadata availability, not carved lava/floor terrain.
Requiring CARVERS at radius two via new dependencies would cause additional
generation when that terrain was not otherwise scheduled. This cannot be presented
as a no-forced-generation read-only query.

The radius-seven chamber fits in the anchor chunk (local 1..15). The required shore
marker remains unrestricted within the analyzed window and can lie two chunks away.
A normal Feature invocation cannot write that general marker through its radius-one
WorldGenRegion. The 74 choice does not imply acceptance of a same-chunk shore.

## Ownership, completion and cancellation

`GenerationChunkHolder.acquireStatusBump` (mapped lines 277-288) performs an atomic
compare-and-exchange of the **one holder's** started-work status from parent to target.
`applyStep` (90-107) returns/completes that holder's future. This prevents duplicate
status work on the holder; it is not a Feature-owned exclusive lease on every
neighboring chunk or a transaction across all write chunks.

`ChunkStep.apply` (30-35) runs its task and attaches `completeChunkGeneration`
(40-46); the latter advances the returned ProtoChunk's persisted status. In
`ChunkStatusTasks.generateFeatures` (99-107), biome decoration executes synchronously
before `completedFuture(chunk)` is returned. There is no Feature continuation token
for keeping this whole neighborhood privately reserved between server ticks.

`ChunkGenerationTask.markForCancellation` (79-80) sets a boolean. `runUntilWait`,
`scheduleLayer` and `waitForScheduledLayer` govern further scheduling/waiting;
`releaseClaim` (83-87) removes the task and releases holder references. These paths
do not undo block mutations. Cancellation is **not** rollback of a previously
executed feature. Before any writes IOE can abandon its own plan safely; after
writes these APIs supply neither an undo journal nor all-or-nothing publication.
A retained ProtoChunk reference is not exclusive ownership or a private copy.

These are normal-execution limitations. No power-loss or cross-file crash atomicity
requirement is added to the approved contract.

## Comparison of integration points

| Integration | Actual benefit | Why it does not implement the full contract |
| --- | --- | --- |
| Native Feature in FEATURES | Pre-FULL execution, terrain through CARVERS within radius 1, writes within radius 1 | 74-square lacks guaranteed terrain; remote shore may exceed write radius; no group transaction/continuation |
| Structure + StructurePiece | Persistent geometry/start metadata; pieces can eventually cover multiple chunks | Start selection precedes NOISE/SURFACE/CARVERS; placement later remains per chunk, not an atomic group |
| ChunkEvent.Load | `isNewChunk()` identifies first loading; server-side lifecycle notification | Non-cancellable; converted LevelChunk already registered; no private multi-chunk ownership or rollback |

Specifically, `ChunkGenerator.getWritableArea` (386-394) builds the current chunk's
X/Z box from its minimum through minimum+15. `applyBiomeDecoration` passes this box
to `StructureStart.placeInChunk` (89-105), which invokes intersecting pieces.
`StructurePiece.placeBlock` (219-244) checks the supplied bounding box before writing.
Thus putting chamber and shore in different pieces defers them to distinct chunk
placements; it does not make their visibility or failures transactional. Bypassing
the clipping would just return to WorldGenRegion write limits. At STRUCTURE_STARTS,
noise-column predictions cannot substitute for the approved actual carved lake,
actual floor and protection checks.

The pinned NeoForge `ChunkStatusTasks.java.patch` inserts `ChunkEvent.Load` in the
FULL conversion after `runPostLoad`, `setLoaded(true)`, block-entity registration
and tick-container registration. `currentlyLoading` temporarily bypasses the future
chain. The pinned `ChunkEvent.java` declares Load non-cancellable and warns about
level interactions before FULL. It cannot veto publication of an IOE chunk group.

## Shared read budget

The budget remains 65,536 actual analysis reads per server tick and 262,144 per
candidate. Native decoration runs through a synchronous placement call, not an IOE
server-tick coroutine. Current `NetherAnalysisBudget.acquire(tick,count)` is suitable
for its server-thread callers, not directly for workers carrying captured epochs:
a stale epoch can reset that implementation's counter backwards. An asynchronous
backend needs server-owned monotonic epochs and debits for the tick in which reads
actually occur, shared with diagnostics. Reserving a task's whole quota at start
and spending it across later ticks is not equivalent.

The initial dense-surface scan now costs at most 27,380 reads (5,476 columns times
five states), below a fresh 65,536 quota. Remaining floor/geometry/shore phases and
other users still consume the same quota. The 262,144 candidate ceiling is unchanged.
The exact integer 60% threshold is 3,286 columns; 3,285 is insufficient. This improves
cost but supplies neither the missing radius-two terrain guarantee nor publication
ownership. Production budget/availability refusal must remain terminal without reroll.

## Closed conclusions and proposed scope

**Demonstrated:** the unchanged native FEATURES step does not guarantee CARVERS
terrain across the required radius two, and disallows the general radius-two
shore write. Native structure clipping distributes writes across chunk invocations.
The load event cannot cancel group publication. Existing live compensation has a
counterexample where invalidating an already-written chunk prevents restoration.

**Missing implementation:** any explicit multi-chunk publication/ownership backend,
async-safe shared accounting, and complete external protection integration.

**Not demonstrated:** impossibility of all custom engine integrations, or mutual
inconsistency of the approved gameplay constraints. No arbitrary off-thread mod
mutation or OS-crash guarantee is assumed.

With the approved contract unchanged, the native Feature/Structure/event route is
closed as an implementation shortcut. Generation stays off; no new diagnostic or
nominal prototype is presented as progress on the missing ownership boundary.

The user's 74x74 choice is now applied in the canonical planner and diagnostic.
No further size decision is requested. A 47x47 same-chunk-shore prototype is no longer
the selected scope and must not be substituted for it.

**Recommendation and feasible direction:** preserve the 74x74 contract and keep
production gated. A conforming custom scheduler/ownership backend remains conceivable,
not established: it would admit only regions whose required terrain is already
available/scheduled without IOE forcing it, acquire stable views and exclusive
pre-publication control of every write chunk, coordinate spacing/terminal attempts,
and account reads against a monotonic server-tick quota. Unavailable regions must
be rejected, never forced or rerolled. The ordinary Feature, Structure and load-event
interfaces examined above do not provide this group ownership boundary. No conforming
production prototype is claimed merely because the surface scan now fits the budget.
