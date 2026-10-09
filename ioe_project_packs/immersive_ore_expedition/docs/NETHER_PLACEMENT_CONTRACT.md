# Nether placement contract — amended 2026-10-09 15:58 UTC

The user approved this complete gameplay lot. These rules are not proposals and do
not require another gameplay approval. Approval does not authorize a release,
installation, retrogen, or an assertion of stable client/server qualification.

The 15:58 UTC user decision changes only the lake analysis window to exactly
**74x74**, superseding the previous 129x129 window. It does not accept the proposed
47x47 alternative or restrict chamber and shore to the same chunk.

The explicit even-width convention is `[candidate - 37, candidate + 37)` on both
X and Z: offsets **-37..+36 inclusive**. The candidate is the positive-side one of
the two central cells on each axis. This technical anchoring is deterministic,
including negative coordinates; no rounding to 75 and no candidate/quality reroll.

## Compensation amendment — approved 2026-10-09 19:14:50 UTC

The owner explicitly chose option 2: continue implementation with best-effort
compensation, without enabling generation. Only the all-or-nothing guarantee is
relaxed. If a chunk becomes unavailable/invalid during writes, a partial chamber
or clue may remain permanently. Persist `ROLLBACK_INCOMPLETE`; do not retry the
region, restore first-generation authority, force a chunk or repair an old chunk.
All other rules below, including exact 74x74, read-set validation, budgets, spacing
and persistent deduplication, remain unchanged. The previous strict-atomicity audit
is historical evidence, not a requirement to solve atomic publication before this
explicitly limited backend can be developed.

`NetherPlacementRuntime.commitPrepared` now connects prepared plans, the real
server host, the server coordinator and the world's SavedData ledger. It is a
package-scoped backend entry, with no command, automatic scheduling or generation
caller. `commit` still returns `BACKEND_UNVERIFIED`; the direct generator still
returns false. This backend does not certify candidate acquisition, external
protection or automatic natural generation. No synthetic test receipt is used by
production to create first-generation authority.

Freshness is checked again after immediate state/protection reads and immediately
before compensation; a compensation that invalidates its chunk cannot be reported
as a complete rollback. The host refuses writes when its chunk is unavailable.
The persisted outcome and spacing of partial sites survive a real SavedData file
round trip; reloaded claims reject replay without repairing remaining blocks.
GameTests inject faults and synthetic first-generation receipts around real chunks
and the production storage entry. This is not a server restart or a natural-generation
qualification. Persistence retains the existing ordinary-save boundary, without
promising cross-file atomicity on OS crashes.

## Approved rules

- New chunks only; no forced loads or retrogen.
- Exact 74 × 74 square anchored as specified above. One source-lava surface connected
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
4. Connected-surface analysis, deterministic selection and geometry are implemented
   in the pure planner. Complete external protection, production scheduling and
   a qualified transactional backend remain pending.

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
4,096 writes and at most 262,144 retained observations. Complete validation must
fit the unchanged shared tick quota. All writes require expected states;
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

## Callback audit and production gate

`NetherPlacementRuntime.commit` now returns `BACKEND_UNVERIFIED` before acquiring
SavedData or writing blocks. The experimental coordinator remains exercised directly
by tests; its journal is explicitly not a guaranteed rollback backend.

The NeoForge 1.21.1 Level patch routes normal writes through chunk mutation and then
`markAndNotifyBlock`, including block-state callbacks even when neighbor-update flag 1
is absent. Snapshot capture delays notification/physics and the patched chunk path
suppresses `onPlace`, but those provisions alone do not establish that all mutation,
removal, lighting and eventual publication paths are free of reentrant code. Calling
raw section setters would also bypass normal heightmap/light/ticking maintenance.
No such unsupported shortcut, forced chunk ticket or restored permission is used.

Sources inspected:
- https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/Level.java.patch
- https://github.com/neoforged/NeoForge/blob/1.21.1/patches/net/minecraft/world/level/chunk/LevelChunk.java.patch

The precise unresolved risk is a write followed by callback-driven invalidation of
an already-written chunk: a later compensation would violate the no-old-chunk rule.
An incomplete journal cannot solve that contradiction. Production stays blocked
until a backend with an established callback/publication boundary is validated or a
separate explicit decision accepts a weaker transactional guarantee. No crash
atomicity is claimed.

## Connected planning over snapshots

`NetherSitePlanner` now composes deterministic per-region candidate selection,
10/25/45/17/3 quality, a stable 5-in-1000 Motherlode-only debris draw, connected lake
analysis and geometry into the existing coordinator Plan type. It performs no world
access: its Snapshot must be immutable and return unknown for unavailable terrain.
A hard 262,144 snapshot-probe bound covers all phases together. This bound does not
replace the shared server-tick budget needed when acquiring a real snapshot.

Surface analysis visits the 74-square at a supplied surface Y, excludes flowing
surface lava and lava-covered source cells, and flood-fills four-neighbor source
cells from the candidate. Only connected columns with four contiguous lava cells
count toward the integer 60% threshold (3,286 of 5,476). Separate pools cannot sum.
The whole footprint must be known. The floor search examines the complete 15-square
chamber footprint, follows lava to the first solid rock and selects the lowest floor
Y; the center is exactly 16 below it. Unknown, hollow or protected floors fail.

Geometry requires a solid, unprotected 15-cube of vanilla netherrack/basalt/blackstone,
carves only a radius-3 interior, and places the exact finite mineral budget within
the radius-5 shell. This conservatively retains at least three solid cells around
the cavity; existing voids, fluids, ores, block entities and protected cells reject
the plan. The nearest admissible dry shore adjacent to the connected surface gets a
two-block blackstone marker, with no tunnel. All distinct planning observations, including lake/depth/floor states and protection
bits outside the write set, are supplied to the coordinator for revalidation. Region-spacing and freshness
remain the coordinator's responsibility, not inferred from a successful plan.

Pure tests cover integer coverage boundaries, disconnected pools, flowing/covered
surfaces, shallow columns, actual floor rejection, crust/protection, absent shore,
unknown terrain, total probe exhaustion, negative-region determinism, exact budgets
and debris replacement. The runtime GameTest verifies that the production gate
refuses a plan without changing either chunk.

Read-only real acquisition is now available through `diagnose nether_site`; see
[NETHER_SNAPSHOT_CAPTURE.md](NETHER_SNAPSHOT_CAPTURE.md). It is a bounded single
server-thread turn with identity/epoch validation, not a multi-tick content revision
or first-load write authorization. Incomplete data is discarded.

Remaining integration: qualify stable acquisition across ticks where required,
select/verify surface Y, apply external protection checks, and qualify the
transactional backend. [Pre-publication review](NETHER_PREPUBLICATION_BACKEND_REVIEW.md)
records the proof obligations and alternatives. No automatic region scheduling,
natural generation or client acceptance is claimed here.


## Geometry and cost after the 74x74 decision

At the unchanged candidate alignment (chunk-local X/Z = 8), the window spans chunk
offsets -2..+2: **25 chunks**. For arbitrary alignment, one axis spans five chunks
at local coordinates 5..11 and six at 0..4 or 12..15; 2D totals are 25, 30 or 36.
Floor division is required at negative coordinates. The radius-seven chamber stays
in the anchor chunk, but the shore is still free to lie elsewhere in the window.

The exact threshold is `ceil(5476 * 60 / 100) = 3286`; 3285 fails. A fully deep,
uncovered source surface costs at most `5476 * 5 = 27380` initial snapshot probes
(and fresh reads before cache reuse). That phase fits one otherwise unused 65,536
read quota. Floor search, geometry, shore validation and concurrent diagnostic work
still consume budget; 262,144 per candidate and the global limit remain unchanged.
Budget availability is not placement authorization.

The native FEATURES terrain guarantee/write radius remains one chunk. Radius two
terrain and general shore placement are still outside that guarantee. The 74 choice
reduces cost but does not establish ownership, group publication or cancellation.
Generation remains blocked until a conforming backend is qualified.

`NetherLakeWindow` is the canonical width/offset contract used by the connected
planner and capture diagnostic. The deprecated radius setting and scalar
`LavaLakeAnchorSample` belong to the older synthetic metadata adapter; they cannot
represent an even-width window and are not used to size the canonical capture.
Their radius is not silently reinterpreted as 37 (which would suggest width 75).


See [complete read-set validation](NETHER_READ_SET_VALIDATION.md) for acquisition,
preflight/compensation read reservations, retained-entry memory bounds and the
before/after regression evidence. Complete preflight does not establish publication
atomicity; the production backend remains blocked.

## Explicit canonical admission at a supplied surface

`NetherNaturalAdmission.attempt` connects the world-seed canonical candidate to
loaded-terrain capture, the existing planner and the prepared backend in one
server-thread turn. It accepts only the candidate's actual chunk and requires its
live first-generation receipt from `ChunkEvent.Load`; it never constructs receipts.
Every write chunk still needs its own live receipt at placement. The surface Y is
explicit input: automatic lake-height selection and natural scheduling are NOT
implemented or enabled by this entry. No command or tick callback invokes it.

A durable region claim is made before capture. Wrong candidate chunks do not claim;
duplicates, unavailable/invalidated terrain, exhausted budgets and planning refusal
cannot retry at another height or after reload. A transient single-use claim token
hands the same canonical X/Z to placement without claiming the region twice; it is
never serialized and is not itself a chunk-write permission. A process interrupted
after admission leaves the existing terminal INTERRUPTED record on ordinary save.
No OS-crash cross-file guarantee is added.

Capture uses only already FULL loaded chunks, exact 74x74 and the shared server-tick
read budget. Placement retains the complete read set and its acquisition plus
validation/compensation accounting. Capture caches are discarded on exit.

Hosted tests use a real newly generated fixture neighborhood and actual NeoForge
load events, without calling observe or injecting first-generation receipts. Terrain
is deliberately constructed to test the canonical chamber/remote-shore path; this
is not discovery of a natural lake. Refusal coverage includes a noncandidate trigger,
an unavailable FULL neighbor without forced loading, persistent claim round-trip and
retry at a different Y. Existing coordinator tests retain old/replaced/expired chunks,
read-set invalidation, budgets and partial compensation coverage. External protection
remains unqualified (the host currently observes block entities); automatic generation
and the production gate remain disabled. This entry is not an activation authorization.

## Lowest-surface selection — approved 2026-10-09 20:25:47 UTC

The owner chose the lowest surface. Natural admission no longer accepts a supplied
Y: at the canonical X/Z it scans from minimum build height upward, choosing the
first lava-source block whose immediately higher block contains no lava. The top
build-height cell cannot be a certified surface because its upper cell is outside
the available world interval. Flowing lava is not a source, and either source or
flowing lava above disqualifies a cell as a surface.

Only this selected level receives the 74x74 eligibility analysis. No surface,
unknown terrain, insufficient budget or failure of any lake/placement criterion
terminates the regional attempt; no second level or later retry is tried. The
existing durable claim precedes selection. Selection and planning share one Reader
and one capture: every distinct selection state/protection observation is retained
in the placement read set, including cells below the eventual chamber. Fresh reads
consume the shared 65,536 tick quota and the 262,144 candidate accounting; overlap
with planning is deduplicated, not billed as a second world read. Immediate preflight
revalidates these selection observations before any write.

This supersedes earlier statements that natural admission requires an externally
chosen surface. The diagnostic command still intentionally takes an explicit Y.
There is still no automatic command/tick/generator caller, and protection qualification
remains outstanding; the generation gate stays closed. Tests use real load receipts
and constructed terrain with two lava levels, plus unit coverage for lower-pocket
failure without fallback, flowing/covered cells, unknown cells, shared quota exhaustion
and state/protection changes affecting selection-only observations.
