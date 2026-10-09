# Nether pre-publication backend review

Architecture review only; no backend activation or worldgen changes.

## Evidence and scope

The [NeoForge 1.21 migration primer](https://docs.neoforged.net/primer/docs/1.21/#chunk-generation-reorganization)
describes asynchronous generation tasks/holders, dependency-ordered steps and
separate generation/loading pyramids. This is a promising lifecycle to investigate,
not evidence of a multi-chunk atomic publication API.

Existing IOE `IoeExpeditionPlanPlacement.canWrite` checks `WorldGenLevel.ensureCanWrite`.
`IoePendingExpeditionSites` explicitly treats temporary dependency chunks as potentially
non-durable; its `PlanSignature` rejects resources outside the anchor chunk. These
existing safeguards do not provide a multi-chunk Nether transaction.

The [NeoForge Level patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/Level.java.patch)
and [LevelChunk patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/chunk/LevelChunk.java.patch)
remain the basis for blocking the current live-world write backend. Snapshotting
and flag 2 do not establish a callback-free transaction. The small
[WorldGenRegion patch](https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/server/level/WorldGenRegion.java.patch)
does not establish atomic publication either. No absence-of-patch argument is used
as proof that ProtoChunk setters have no callbacks.

## Required proof before implementation can qualify

1. Own every chamber/clue write chunk within one unpublished generation lifecycle;
   reject loaded/old chunks and distinguish generation from disk loading. Merely
   receiving a ProtoChunk or passing `ensureCanWrite` is insufficient.
2. Read the 129-square and complete floor/geometry footprint only from available,
   stable generation data, without additional loads or escaped mutable region refs.
   Establish all affected generation dependencies and a content-stability boundary.
3. Publish all affected chunks and the spacing/attempt decision consistently; handle
   abandoned temporary dependencies, failures, unloads, retries and restart without
   partial visible sites or rerolls. A per-chunk placement callback cannot establish
   this multi-chunk guarantee by itself. Crash atomicity is not currently proved.
4. Preserve normal heightmaps, lighting, fluid/block scheduling and callbacks at a
   defined publication boundary; raw section mutation is not an approved shortcut.
5. Reconcile asynchronous generation with the shared server-tick read quota. A dense
   129-square source surface alone needs 83,205 fresh reads in the current algorithm,
   exceeding 65,536 per tick. Retaining a mutable WorldGenRegion across ticks or
   blocking a generation worker while awaiting tick budget is not a qualified design.
6. Integrate protection checks, deterministic no-reroll attempts and spacing across
   concurrent regions before publication, including the dry-shore marker chunk.

## Decision boundary

No compliant backend has yet been established, but this review does NOT prove one
impossible. The approved contract remains unchanged and generation stays disabled.
The next contract-preserving research step is a dependency/publication ownership
prototype with immutable terrain acquisition, failure-injection tests, and explicit
server-tick accounting before any production writes are considered.

Alternatives requiring a separate user decision, not implemented here:

- Restrict every write to one chunk and omit/move a distant shore clue: changes the
  approved placement/clue contract and still needs durability qualification.
- Accept best-effort rollback for multi-chunk live writes: weakens the transactional
  guarantee and permits partial sites; it is not the current approved behavior.
- Change the global per-server-tick quota to a worldgen-task quota: changes the
  approved performance contract and does not by itself solve atomic publication.

No such relaxation is necessary merely to continue the architecture investigation.
There is no claimed client, natural-generation or manual acceptance in this review.
