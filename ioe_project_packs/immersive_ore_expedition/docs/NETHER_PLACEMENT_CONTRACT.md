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
3. Connected-surface analysis, deterministic region ledger, geometry, atomic placement
   and production activation remain unimplemented. No natural Nether generation is
   enabled by the budget prerequisite.

## Integration constraint verified in current source

`IoePendingExpeditionSites.PlanSignature.from` explicitly rejects any write outside
its anchor chunk. A Nether chamber and a shore clue can lie in distinct chunks.
`IoeNewChunkOreGuard` tracks only its own pending first-load chunks and releases the
per-chunk placement authorizations on its final sanitation pass. Neither mechanism
provides a multi-chunk first-generation transaction for the approved Nether lot.

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
