# Read-only Nether snapshot diagnostic

`/ioe diagnose nether_site <surface_y>` inspects the deterministic candidate in the
command origin's 256-block region. It requires the existing diagnostic permissions
and enabled locate-command configuration. It never generates terrain, claims a
region, updates SavedData, places blocks, or enables the production backend.
Surface Y is supplied by the operator; automatic surface selection remains pending.

## Acquisition boundary

`NetherSnapshotDiagnostic` runs entirely in one server-thread turn. The real adapter
uses `getChunkNow`, then reads the returned `LevelChunk` directly. Missing chunks
abort instead of being loaded. Each touched chunk's object identity and the server
tick epoch are checked before/after fresh reads and in final validation. Cached
reads still check the current identity and epoch. Identity replacement, unload, or
epoch change invalidates the whole capture. Final validation covers earlier chunks,
not only the last one read.

The epoch is NOT a chunk content revision. No universal mutation counter was
established in the inspected API. `isUnsaved` is not used as a version. This design
relies on ordinary synchronous block reads on the server thread, with no yielding;
it does not claim consistency against unsupported off-thread mod mutations. It is
not a multi-tick snapshot or authorization to write later. Reconnection/reload starts
with a new source and fresh identities; no snapshot data is persisted.

Each uncached block read consumes the shared `NetherAnalysisBudget` quota of 65,536
reads per server tick (also used by lava diagnostics/coordinator), and the capture
has an independent 262,144-read candidate ceiling. The planner separately bounds all
snapshot probes to 262,144. Cache hits do not spend fresh-world-read quota. These
are read bounds, not elapsed-time guarantees.

If the shared quota is insufficient, `CAPTURE_BUDGET` discards the entire capture.
Nothing resumes on a later tick. Dense valid lakes can exceed the single-turn
quota: 16,641 source columns require 83,205 surface/depth reads before geometry.
Supporting such cases requires a qualified stable multi-tick acquisition lifecycle;
it is not silently approximated or declared ineligible terrain.

## Result and remaining limits

Only immutable summary metrics escape: candidate, quality, epoch, read/chunk counts,
capture status, planner status, connected columns and proposed write count. No plan
or terrain map escapes. All captured references/maps are cleared in `finally`, and
a closed capture rejects subsequent access. Incomplete/invalidated captures report
`NOT_EVALUATED` and zero proposed writes. `COMPLETE` means capture completed, not
that the planner accepted the terrain. Command success follows capture completion.

The real adapter marks block entities protected; external claim/protection systems
remain `NOT_EVALUATED`. Fresh-generation provenance, accepted-site spacing, durable
attempts and transactional publication are not inferred from this diagnostic.
`NetherPlacementRuntime.commit` remains `BACKEND_UNVERIFIED`.

JUnit coverage includes connected successful planning over the acquisition adapter,
shared/global and independent candidate limits, missing chunks, identity/epoch
invalidation, final validation, unload/reconnection and discarded capture reuse.
The GameTest reads real Nether cells without changing them, exercises the actual
command, refuses missing chunks without loading them, and rejects the Overworld.
These are automated fixtures, not natural-generation or visual/client acceptance.
