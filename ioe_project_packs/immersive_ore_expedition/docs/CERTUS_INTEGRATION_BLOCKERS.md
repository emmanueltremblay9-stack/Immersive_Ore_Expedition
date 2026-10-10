# Certus integration: approved decisions and implementation

The documentation-only checkpoint was `c76fe8b858fdb09b4884c242962366ba32b0c2a0`.
On 2026-10-09 at 04:26 UTC the user approved all four previously pending rules.
This record supersedes the earlier blocker status; historical CI at that checkpoint
is not evidence for the following implementation.

## Approved gameplay

| Profile / case | Rule |
| --- | --- |
| DRY Certus | One independent uniform 0–5 draw of `ae2:quartz_block` for the whole pocket; no active heart. |
| DRY Entro | No physical Entro residue. The independent 10% neutral seed reward remains. |
| Productive Certus | Native AE2 hearts with 4/5/6/7 surrounding `ae2:quartz_block` per node for Poor/Normal/Rich/Motherlode. |
| Meteorite Flawless | Replace native `ae2:flawless_budding_quartz` with native `ae2:flawed_budding_quartz` only during new-chunk admission. No retrogen or edits to existing chunks. |

Canonical counts remain 3/4/5/7 hearts and 12/20/30/49 surrounding blocks. Initial
ranks are Damaged/Chipped/Flawed/Flawed. A single Motherlode draw at 7.77% replaces
one of its seven Flawed hearts with Flawless. Lower-quality fallback drops that
selection without rerolling it. Entro remains IE extraction only; ordinary
`ae2:fluix_block` is not an Entro resource or residue.

## Runtime integration

`NativeCertusBudding` resolves exact registry IDs and checks AE2/AE2CS, all four
ranks, four growth stages and quartz storage before productive admission. It
copies no AE2 code or assets and changes no native growth, repair or loot behavior.
`CanonicalBuddingSitePlans` shares geometry with GeOre; all rank states remain
owned by their original mod. Missing dependencies fail closed without resource
substitution. Certus uses the existing IE reserve transaction and fallback chain.

`BuddingBlockIdentity` distinguishes GeOre and native Certus only. Committed
Certus node positions, family, index/count and initial ore enter the existing
locator save format. Jade reads the current native rank and the original site
metadata; manual or replaced blocks do not gain invented provenance.

`IoeNewChunkOreGuard` admits only new chunks (or their already-pending first-load
passes within the session limits below). It replaces unauthorized native Flawless with Flawed while preserving
exactly authorized IOE Motherlode hearts. Sky Stone, ordinary Fluix, quartz,
non-Flawless budding and all quartz growth stages are preserved. The upstream
meteorite biome overlay stays empty with `replace: false`.

**Exclusivity is non-retroactive.** New IOE Motherlodes are the only authorized
source of newly generated Flawless under this policy. Pre-existing Flawless
blocks survive, including after reload; this update does not promise a global
zero-Flawless audit of older saves. Administrative commands are not a worldgen
guarantee. AE2CS Flawless crafting remains disabled by the existing override.

### Bounded first-load resumption

Approved session-only resumption uses **4,096 admissions across the whole server,
shared by all dimensions**, and **12,000 server ticks from initial admission**
(10 minutes at 20 TPS; longer in wall time if the server lags or pauses). Reloads
and duplicate load events do not renew that deadline. The clock is checked before
each tick's work and on admission/access; synchronous server-thread writes cannot
cross a tick deadline. The existing rate of one sanitation pass per tick needs
8,192 ticks for two passes on 4,096 loaded chunks, plus the final-pass delay of 20
ticks. The lifetime leaves headroom for that finite backlog; sustained overload
does not guarantee completion.

An unavailable chunk keeps its admission until the deadline. A disk reload may
resume only that still-valid admission. At capacity, admitting a genuinely new
chunk evicts the oldest admission, irrespective of dimension or recent reloads.
Expiration, eviction and session shutdown abandon the unfinished work: remove
its queue entries, exact block permissions and unconfirmed plan/reservations.
They neither load chunks nor sanitize/restore terrain, delete confirmed locator
records, or remove pre-existing IE/IP resources. No tombstone queue accumulates.
The independent pending-plan limits (256 chunks per dimension, 10 wall-clock
minutes) remain unchanged and may discard a plan sooner.

**Abandonment is final for subsequent existing-chunk loads.** There is no saved
admission and no reconstruction from locator metadata after restart. If no first
pass ran, unauthorized resources may remain; if finalization did not run, the
unconfirmed site may be absent. This is an explicit bounded-work limitation of
new-chunk exclusivity, not permission for retroactive cleanup. Jade continues to
describe confirmed persisted sites only; abandoned plans gain no site provenance.

## Validation and remaining boundary

Targeted coverage includes deterministic geometry/ranks for every productive
quality; forced Flawless and fallbacks; missing dependencies; DRY quantities and
reward independence; productive and reward rollback; actual Certus/Entro profile
transactions and nonduplication; locator disk save/reload; Jade callbacks for all
four native ranks; and new versus existing chunks with authorized-heart survival.
Hosted CI must pass at the implementation commit. Client visual acceptance and
full player progression remain separate and unproven. No merge or release is
authorized by this checkpoint.
