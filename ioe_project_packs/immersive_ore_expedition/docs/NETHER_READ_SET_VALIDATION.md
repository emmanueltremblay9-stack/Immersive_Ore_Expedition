# Complete Nether planning observation validation

Production generation remains disabled. This fixes incomplete plan validation,
not multi-chunk atomic publication, claim integration or a stable cross-tick lease.

## What is retained

The planner's Reader records every distinct position observed during surface,
connected coverage, depth, footprint floor, crust/geometry and shore selection.
Repeated probes use that immutable-snapshot observation instead of reading it again.
The retained set includes rejected shore alternatives and other conservative
observations, not just positions receiving writes. Unknown terrain still aborts.

`Plan.expected` is now the entire deduplicated state set. `protectedPositions`
preserves the protection bit for each observed position (absence means false).
Both collections defensively freeze coordinates and contents. Before any write,
the coordinator compares each state's value and protection bit with the current
host observation. An unavailable chunk, changed state/protection, or chunk identity
change during an observation rejects the attempt before mutation. Only write chunks
receive/require first-generation authority; reading an old loaded neighbor never
grants write authority. Targets also recheck protection immediately before writing;
compensation refuses newly protected positions.

The adapter's current protection bit is block-entity protection, matching capture.
External claim systems are still unqualified, and `BACKEND_UNVERIFIED` remains the
unconditional production result. These tests must not be advertised as proof of
complete third-party protection integration.

## Explicit read accounting

Let A be acquisition reads, N distinct retained observations, and W proposed writes.
The canonical planner acquires each snapshot position once, so A=N. Logical probes
(including cache hits) retain their separate 262,144 planner ceiling. Real capture
already debits each fresh read from the shared 65,536-per-server-tick quota.

Before validation the coordinator reserves **N + 2W** shared reads: N preflight
observations, up to W immediate target rechecks, and up to W compensation reads.
Unused reservation is not refunded in that tick. A block-state read plus its
protection metadata observation counts as one observation; `protectedAt` receives
the state and must not perform hidden unmetered world block reads. An integration
requiring such reads must account for them before it can qualify.

The candidate lifetime check is **A + N + 2W <= 262,144**. Failure of either this
check or the shared reservation returns BUDGET before validation reads or writes.
An attempt remains terminal in the ledger; refusal does not authorize reroll.
The structural observation ceiling is 262,144, replacing the former 16,384 target-
centric check limit. That larger retention ceiling does not raise the 65,536 tick
quota: a large plan is rejected if its whole validation cannot fit the quota.

## Explicit memory accounting

N <= 262,144; P protected entries <= N; W <= 4,096. The immutable plan retains
**N + P + W position entries**, exposed by `retainedPositionEntries()`. Collection
sizes are checked before defensive copying. No byte-accurate JVM heap claim is made:
map nodes, references and object layout depend on the JVM.

During canonical capture/planning/defensive copy, collection entries are bounded by
**3N + 2P + 2W** (capture cache, reader states, frozen plan, two protection sets and
two write maps), excluding fixed-size surface/BFS arrays and chunk identity metadata.
During validation/compensation, the plan plus journal retains at most **N + P + 2W**
position entries. Reader collections become unreachable after planning; capture
collections are explicitly cleared. Production has no queue/cache of staged plans.
The current capture entry point runs synchronously on one server thread, so these
per-invocation bounds are not multiplied by background captures. Any future queue
must impose its own aggregate bound; arbitrary callers retaining K pure Plans can
retain the sum of their entry counts and are not subject to a claimed global heap
quota. Existing coordinator lease limits remain unchanged.

## Regression and integration evidence

The test-only commit `13815db8f3f5b568946f9b557031f240f595ca67` ran on hosted CI
37964379143: 637 tests completed, exactly the two new lava/floor invalidation tests
failed at the expected result assertion. Both positions lie outside the write set.

The corrected suite checks those regressions, an unchanged full plan, protection-
only changes, retained true protection flags, read-only chunk loss, shared-budget
refusal before reads, candidate lifetime exhaustion, deduplication and immutable
read sets. A GameTest uses real Nether cells and the real experimental host to
reject a changed read-only observation before touching the target, using explicitly
synthetic new-chunk receipts. This is not natural-generation or visual acceptance.

A complete validation pass is not a lock: changes after an observation or callbacks
during subsequent writes can still defeat atomic publication. That separate backend
blocker is unchanged; no private-chunk ownership is claimed here.
