# Nether placement contract — approved 2026-10-09 14:45 UTC

The user approved this complete gameplay lot. These rules are not proposals and do
not require another gameplay approval. Approval does not authorize a release,
installation, retrogen, or an assertion of stable client/server qualification.

## Approved rules

- New chunks only; no forced loads or retrogen.
- Inclusive 129 × 129 square around a candidate. One source-lava surface connected
  horizontally to the candidate covers at least 60%. Separate pools and cascades
  cannot be added together. Counted columns have contiguous lava depth at least four.
- Find the real bottom below the chamber footprint. Chamber center is 16 blocks
  below that bottom. Keep at least three solid blocks between every carved cavity
  and lava. Reject incompatible terrain, cavities, collisions or protected blocks.
- Existing planning radii 7/5/3 may be reused according to their real layer meaning.
- Dry-shore clue required; no automatically dug tunnel crossing lava.
- One deterministic candidate per 256 × 256 region; accepted sites at least 256
  blocks apart. No reroll after failure or reload.
- DRY/POOR/NORMAL/RICH/MOTHERLODE weights 10/25/45/17/3, finite vanilla quartz-ore
  budgets 0/12/20/30/49. No new quartz Budding family.
- At most one ancient-debris block, replacing a quartz block, from one 0.5% draw
  per accepted Motherlode. No extra roll on reload or fallback.
- Effective global analysis ceiling: 65,536 reads per server tick; at most 262,144
  reads per candidate. Transactional placement and persistent duplicate prevention.

## Implementation checkpoints

1. Existing read-only diagnostic measures loaded terrain, rejects unavailable chunks,
   and exposes incomplete/capped results. It is not an eligibility test.
2. Shared server-tick budget now applies to real diagnostic calls. Multiple commands
   cannot multiply the ceiling. Future candidate analysis must acquire from the same
   budget; it must additionally retain its own lifetime read counter.
3. A first-load multi-chunk coordinator now tracks exact chunk object identities,
   reserves the shared read budget, validates expected states and compensates failed
   writes. A separate SavedData region ledger persists claimed/finished attempts.
   No automatic candidate or generation caller is registered.
4. Connected-surface analysis, deterministic candidate selection, physical geometry,
   complete terrain/protection checks and production activation remain unimplemented.

## Integration constraint verified in current source

`IoePendingExpeditionSites.PlanSignature.from` explicitly rejects any write outside
its anchor chunk. A Nether chamber and a shore clue can lie in distinct chunks.
`IoeNewChunkOreGuard` tracks only its own pending first-load chunks and releases the
per-chunk placement authorizations on its final sanitation pass. Those existing mechanisms
remain unchanged; `NetherPlacementCoordinator` now provides a separate bounded path.

The next integration must retain exact first-generation identities for every write
chunk, reject disk-loaded/unloaded/replaced chunks, revalidate terrain immediately
before commit, and never use ordinary loaded status as permission to write. The
surface scan may read older loaded neighbors, but cannot grant them write permission.
NeoForge's chunk-load event precedes FULL promotion; interacting with the world in
that callback is not a safe shortcut around deferred analysis. This is a missing
transaction architecture, not a claim that NeoForge cannot support the feature.

Until that lifecycle is implemented and tested, do not reuse the current one-chunk
transaction by dropping its boundary check, and do not schedule delayed generation
against arbitrary loaded chunks. A failed or expired attempt must stay terminal in
the region ledger; a restart must never turn an old chunk into a new candidate.

## Coordinator limits and guarantees

First-generation identities are recorded in chunk-load callbacks without accessing
world blocks. Only the same loaded chunk object may be written, from a later server
tick and before age 20 ticks. Leases are not renewed; duplicate/disk load, unload,
player break/entity placement and server stop invalidate them. Expired entries are
pruned; at most 256 live identities are retained, with overflow denied.

An immediate commit on the Nether server thread supports at most four write chunks,
4,096 writes and 16,384 expected-state checks. All writes require expected states;
fluid/block-entity targets are rejected. The adapter only replaces air, netherrack,
basalt or blackstone without fluid or a block entity. This is not a substitute for
future structure/protection and safe-crust planning. Expected read-only neighbors
may be older chunks; every write chunk needs its own first-generation lease.

Reads for preflight, immediate revalidation and possible compensation are reserved
atomically from the global budget before inspecting blocks. Insufficient budget
terminates the attempt; commit never waits until another tick. Regional claims are
made before validation and remain terminal after failure or reload. Spacing checks
inspect only nine neighboring region cells and reject distance below 256 from an
accepted or incompletely compensated attempt. The original world seed/candidate
selection must still be supplied by the future planner, not chosen by the coordinator.

The reverse-order compensation journal restores only still-fresh chunks and only
states still owned by the transaction. If another system unloads/replaces a chunk
reentrantly during mutation, compensation must not load it or overwrite later work:
`ROLLBACK_INCOMPLETE` is retained, blocks further attempts in that region and reserves
neighbor spacing. No deferred recovery writes are attempted. This exceptional state
is explicitly tested, not advertised as successful atomic placement. Generation must
remain disabled until the final pipeline and its failure handling are qualified.

Normal disk save/reload preserves all claims, including interrupted ones; leases
are never serialized. This does not claim atomicity across an operating-system crash
between Minecraft chunk saves and SavedData saves. Lost ledger data still cannot grant
first-generation permission to a disk-loaded chunk. Cross-file crash consistency and
full automated generation remain separate integration requirements.

Validation covers both load orders, adjacent old/replaced chunks, same-tick use,
expiry, unload/reload, player/terrain protection checks, global-budget refusal,
partial-write compensation and unrecoverable invalidation, negative region coordinates,
spacing boundaries, identity limits, save/reload and real two-chunk Nether writes.
GameTest fixtures explicitly simulate new-generation receipts; they do not prove
natural Nether generation or manual gameplay acceptance.
