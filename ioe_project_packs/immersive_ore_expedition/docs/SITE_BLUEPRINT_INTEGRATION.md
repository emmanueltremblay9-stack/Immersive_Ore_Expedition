# Catalogue geometry integration — incremental implementation

Mandate: integrate varied generation from the four GOV catalogues, on the existing
PR64 branch. Authority reference: GOV `60d1c9453aaf91f1e90a6ba113f106ac51238cf9`,
`governance/gameplay/site_blueprints/`. All four actual files were read, including
120 variant descriptions. Their PROPOSED parameters do not override FINAL_DESIGN.

| Catalogue | SHA256 of exact reference file |
| --- | --- |
| 001 | `8a580747a53410a5f0dacc66bb03493e88668bc09c9aafdef20fdac23660944f` |
| 002 | `250cf8998fc203dbc57cfe691e94a29280bfb1d58573d6fe3853e5b36b9eabd0` |
| 003 | `423064faffffdbe38f797aab5ca3593ceea73e9100ef796eee8e67713c668803` |
| 004 | `feed853a79d9f2003a4a0ed7e6758c91815927bce0059ddc605fca6bbc33fbc8` |

## Implemented first natural-generation slice

`CatalogueTerraces` composes BP-05 A (offset terraces), B (fan ledges around one shared vertical void)
and C (split middle terrace with an open void on either side of its supported
bridge), geometry version 2. B retains the offset ladder accesses and rock slabs
outside its 2×2 common opening; its ledge edges taper toward this opening.
The chamber occupies 15×19×15. Entry E=(7,15,1) and the three circulation levels
Y=3/9/15 have supported routes; the two interior ladders have solid backing.
The differing footprints and middle bridge change the actual solid/air map, not
only rotation, palette or seed. Only a one-block shell is written, not a 4,275-cell
bounding-box fill. Unused terrace corners are trimmed; reference node positions
and supported routes are retained. BP-16 A is described below; the remaining 36 families are not implemented; see the four-catalogue coverage map below.

The existing natural `ExpeditionSiteBlueprints` path now selects BP-05, BP-16, BP-27 or BP-37 A/B/C using one twelve-way draw
from its isolated shape stream, after the unchanged depth draw. All four natural
access types and all five qualities use this path for structural and canonical
GeOre/Certus sites. Legacy standalone/special-geode planning remains separate.
The same shape seed gives the same chamber, including on quality fallback. No
quality, DRY residue, seed, Flawless or industrial-reserve lottery is repeated.

Seven separate node masks preserve 3/4/5/7 hearts and 12/20/30/49 peripheral blocks.
The composer rejects other count/budget pairs. Existing canonical code assigns
ranks and persisted/Jade provenance. Unused alcoves remain stone. Entro stays
IE-only, without a new heart or physical residue. No new resource family, recipe,
loot, biome admission, natural frequency or default switch is introduced.

## Raccord and composed-plan budget

The direct-merge collision was reproduced at the old chunk-local shaft anchor
(4,y,9): ladder backing (4,y,11) conflicted with the east air face of N3=(3,15,11).
The new route avoids that cell entirely. The main shaft ends four blocks above
the upper terrace, traverses above the roof, then descends through E on a backed,
two-wide ladder. The total surface-to-chamber depth still uses the existing
20–28 draw; configured anchor-distance limits remain unchanged. Shaft roof cells
are reopened only along the original shaft, and no node coordinate is moved.

The underground survey room is retained with its original route, cartography table,
bookshelf and cobweb. It supplements the three catalogue terraces (four room
centers for every quality). The original quality-dependent underground assembly
is replaced by the catalogue chamber. Surface camps, clues, workstations,
containers, Domum payloads and their budgets continue through the existing composers.

The existing 2,500-cell composed-plan regression ceiling is not raised. It is now
also checked by `Feature.place`, together with build height, before staging the
main plan and its fallbacks. An oversized plan is refused, never partially placed
or repaired by a reward reroll. The first composed run detected a RICH camp at 2,558 cells. The correction
removes its obsolete connector stub and trims unused terrace corners, without
changing the ceiling, masks, rewards or surface camp. The expanded all-depth
matrix then found a 2,563-cell Motherlode camp. The survey-room removal attempted afterward was withdrawn: the corrected plan
retains the room and compacts the shaft below its unchanged surface mouth to a
two-wide section with the original backed ladder. Unused outer shaft braces are
omitted; survey functions, nodes and supported chamber routes are preserved. Runtime tests must establish that the corrected admitted fixtures fit; a sparse chamber by itself is not proof of the complete budget.
DRY residues can occupy ordinary stone in the new floor, while excluding the
central cache marker and existing payloads. Counts and independent lotteries stay
with the existing DRY implementation.

## Validation and remaining scope

The isolated composer at `b275df08202b3912574bdb8ae6e45c1cd70ef155` passed 16 CI jobs,
716 JUnit and 81 minimal + 81 full GameTests per stack, plus streaming/restart.
Its separate CI JAR SHA256 is
`fb60003635d4e5f681f68e64e186ff6f0d2029441175bce6b8537dd01d01cdd0`.
That receipt does not qualify the subsequent natural-routing changes described
above. Their exact source and final CI receipt are recorded separately in PR64.

Added integration tests cover all nine shapes, four access types, all qualities,
source-chunk containment including negative coordinates, all nine variants at all nine
approved depths, unchanged depth and
anchor bounds, complete-plan size, three-high supported routes to all alcoves,
all growth masks, canonical counts, DRY 0–5 residues plus cache, and real
`Feature.place` staging/commit/deduplication for all nine variants at all four accesses.
The existing GeOre/Certus runtime tests additionally exercise ranks, fallbacks,
Jade metadata, persistence, compensation and actual resource-profile admission.

No new server/profile, PC operation, Nether activation, merge or publication is
part of this work. The installed `382e0b4` provenance remains separate. Client
visual qualification remains NOT_PERFORMED. This slice implements nine of 120 described
variants, not completion of the four catalogues. Runtime qualification of each successor must
be attributed to its own source, not to a predecessor receipt.


## Qualified predecessor and current qualification boundary

The natural A/C integration at `724e8d63afbc21e86a1b50c0bad3b2ace312895a` passed
16 CI jobs: baseline push 38897636317 / PR 38897643054 and current-pack push
38897636280 / PR 38897643351. Each stack: 716 JUnit (zero failures/errors/skips),
83 minimal + 83 full GameTests, plus streaming and separate-process restart.
Its 1,440-plan matrix maximum was 2,474 cells with the survey room retained.
JAR SHA256 `8ae0701673502ce7b7bc1d407225a9f2fa0b9e984e9f2e80b39ed3266eb4aa2e`,
469 compiled classes and metadata verified. This JAR is not installed.

The next cycle adds BP-05 B and expands the matrix to 2,160 plans and 12 real
Feature commits. It preserves admission/economic rules and uses the same
source-chunk, envelope, route and mask guards. Exact-source CI is recorded in PR64.

## Four-catalogue coverage map

Reference remains GOV `60d1c9453aaf91f1e90a6ba113f106ac51238cf9`.
`Implemented` means connected to natural generation in this source; qualification
is reported separately above and in PR64. `Pending` does not mean incompatible.
No entry is declared contract-conflicting merely because its implementation is
unfinished. PROPOSED dimensions, retry counts and reward parameters remain
subordinate to FINAL_DESIGN and the composed 2,500-cell/source-chunk limits.

| Catalogue | Families | Implemented variants | Pending variants |
| --- | --- | --- | --- |
| 001 | BP-01–BP-10 | BP-05 A/B/C | BP-01–04 and BP-06–10 A/B/C (27) |
| 002 | BP-11–BP-20 | BP-16 A/B/C | All A/B/C of the other nine families (27) |
| 003 | BP-21–BP-30 | BP-27 A/B/C | All A/B/C of the other nine families (27) |
| 004 | BP-31–BP-40 | BP-37 A/B/C | All A/B/C of the other nine families (27) |

A future family must pass the same route/node-mask/complete-budget qualification
before activation. A proposed outline alone does not authorize resource changes,
larger write limits, loading extra chunks or removing the survey room. If a
specific family cannot fit those contracts, record the precise geometric or
functional conflict before asking for a product decision.


## Next family: BP-16 A crossing, with unchanged mineral layout

Catalogue 002's BP-16 A provides the topology: two axes crossing in plan view,
separated vertically by rock, connected on a flank. Its proposed node/entry
coordinates are not imposed on the current final layout. This implementation keeps
15×19×15, E=(7,15,1), the exact seven existing node masks, quality ordering and
canonical rewards. Its upper east-west gallery is at feet Y=15 and its lower
north-south gallery at Y=3. The lateral route passes the unchanged middle alcoves
at Y=9, with the same backed links. Rock is explicitly written at X/Z=6–8,
Y=8–9 as a two-block horizontal diaphragm to keep the axes separated even when
the source terrain contains a void; the lower roof and upper floor remain intact.
No extra resources, rolls, rooms, containers or new gameplay are introduced.

`CatalogueCrossing` supplies its own air volume; the canonical shell, node masks,
backed links and cache marker use the existing shared finalizer. The middle room
anchor moves into the lateral route because the center is now solid rock. This
changes a circulation anchor, not a node, resource count or quality decision.
The survey room, surface camp and overhead raccord remain unchanged.

The deterministic matrix expands to 2,880 plans (9 depths × 4 forms × 4 accesses
× 4 anchors × 5 qualities), plus 16 real generator placement/commit/dedup pairs.
A dedicated test verifies both supported axes and the solid diaphragm, then
severs the single lateral link and requires the lower axis to become unreachable.
The common tests still verify every node face, count, route, rollback and DRY
reward on every implemented geometry. The required CI is hosted, on both stacks;
its exact source and final receipt are recorded in PR64. No local runtime is run.

Qualified predecessor BP-05 A/B/C: `768e8c48b61c030d57c58aa6afb5fb686dc70b00`,
16 successful jobs (baseline 38899947396/38899956408; current pack
38899947682/38899956448), 716 JUnit and 83+83 GameTests per stack, plus streaming
and restart. Maximum 2,482 cells over 2,160 fixtures. Its non-installed JAR has
470 classes and SHA256 `26149d8a06056f87b44b6283fc22744e2df99c6a263f8158326abbaff3a892a5`.
Those results do not qualify the BP-16 successor.


BP-16 first CI finding at `7042ac8`: 83 of 84 minimal GameTests passed;
the expanded composed-plan test measured 2,514 cells for a Motherlode camp.
The correction reduces the redundant eight-layer central fill to the catalogue's
horizontal diaphragm, two blocks thick. It preserves the axes, supported routes,
all seven node masks, survey room and surface functions. The 2,500-cell cap and
its failing assertion remain unchanged; the corrected source requires fresh CI.


## BP-16 B/C: guarded opening and opposed loop

The next coherent cycle completes BP-16's two remaining documented topologies:
B has an off-path visual slit and shifted landings; C has two opposed connections
forming a loop without a central hole. Both retain the same 15×19×15 envelope,
entry, seven node coordinates/masks, quality/reward rules and survey functions.
The gallery margins are compacted while retaining two-wide routes and three-high
clearance; no room function or node alcove is removed to make budget space.

B opens the column (7,Y,8) beside the supported upper gallery. A stone-brick wall
from the existing IOE structural palette guards its upper edge. East ladder
landings shift from Z=6/7 to 7/8, west from 6/7 to 5/6, with matching backing and
floors. The central walking slab and cache at Z=7 are preserved. An actual-level
GameTest checks the collision guard, a visual ray to the lower floor, placement,
shifted ladders and compensation. This is a geometric/runtime test, not client
visual acceptance.

C extends the backed east and west links through all three levels and connects
the lower east landing to the existing lower axis. Upper-west and middle-east
landing floors are restored explicitly where the extended shafts pass them.
The central diaphragm stays closed. A cut-set test independently severs each
connection at both upper and lower heights: either remaining side must still
connect the two axes, and cutting both must disconnect them.

The shared deterministic matrix now has 4,320 plans (9 depths × 6 forms × 4
accesses × 4 anchors × 5 qualities), plus 24 real Feature commit/dedup pairs.
All six forms retain the canonical mask/budget, three-high route, DRY, survey,
source-chunk, height, compensation and complete 2,500-cell tests. The qualification
boundary is the exact new source recorded in PR64, not the predecessor below.

Accepted BP-16 A predecessor: `2f2d16a1baf98396628a2707c2b24dec9d324797`, 16 green
jobs (baseline 38903350234/38903357582; current pack 38903350173/38903357570),
716 JUnit and 84+84 GameTests per stack, plus streaming/restart; maximum 2,487
cells over 2,880 fixtures. Its non-installed JAR contains 471 classes and has
SHA256 `8bcee2f6efd43fcfe8837e1066ef33ec8dfd787abe1c73f84d4653d055e5b1fa`.


First B/C CI findings at `39804fd`: 84 of 86 minimal GameTests passed; the 4,320
plan matrix measured 2,482 cells, and both structural variants passed placement.
The visual-ray fixture incorrectly supplied a null entity; it now uses the same
NeoForge fake-player facility as existing discovery tests. The wall also exposed
an order-sensitive compensation defect: restoring its neighbors first could
change its state and make the exact-ownership check skip it. The correction puts
the guard after its surroundings and retains journal insertion order, undoing
writes in reverse order. The exact current-state/target comparison is unchanged;
foreign edits are still preserved, not force-restored. Runtime tests require all
original stone to return, repeated rollback to succeed and a foreign guard edit
to survive. This correction requires new exact-source CI, including the existing
placement/persistence/failure regressions on both stacks.


## BP-27 A/B/C: dry siphon with a mandatory lower elbow

Catalogue 003 contributes the next family. Its documented topology is preserved:
two diagonally offset ascending conduits connect only at their lower elbow.
Neither the upper galleries nor the middle alcoves bridge the two banks. The
proposed 25-high envelope and alternative node/entry coordinates are not adopted;
the existing 15×19×15 envelope, E=(7,15,1), seven node positions, +Z masks, quality
order and rewards remain fixed. Supported routes and circulation anchors are
fitted around those fixed resources, without changing surface or survey functions.

A has a single lower elbow. B divides only that elbow with a rock blade into two
channels which rejoin at the column feet. C widens the return-side middle shoulder
and offsets the upper return conduit in Z; it still does not connect to the entry
bank above the bottom. All routes are dry and bidirectional. No water, swimming,
hydraulic puzzle, additional deposit or automatic end-of-route reward is added.
Existing fluid rejection remains applicable to source terrain.

The family supplies its own ladder locations and landing floors while reusing
the canonical reservations, shell and cache finalizer. Existing BP-05/BP-16 links
are unchanged. The entry-side column spans all levels; the return column is either
continuous (A/B) or split at the widened middle elbow (C). A dedicated test cuts
the complete lower crossing and requires both directions between upper endpoints
to become disconnected. Another cuts either/both B channels and verifies the
alternative, then checks C's shifted upper ladder and lack of a straight bypass.

The common matrix expands to 6,480 plans (9 depths × 9 forms × 4 accesses × 4
anchors × 5 qualities), plus 36 real generator placement/commit/dedup pairs.
It retains survey functions, canonical budgets/ranks, growth air, source-chunk
containment, three-high supported access, DRY/cache and the 2,500-cell ceiling.
All nine forms also pass through the existing ordered-compensation tests; the
foreign-edit ownership check remains unchanged. Exact new-source CI and artifact
identity are recorded in PR64; no predecessor result qualifies this successor.

Accepted BP-16 B/C predecessor: `f5478f0f62cea895d45f35486406148c30f6d3c4`, 16 green
jobs (baseline 38907833196/38907844655; current pack 38907833561/38907844705),
716 JUnit and 86+86 GameTests per stack plus streaming/restart. Maximum 2,482
cells over 4,320 fixtures; non-installed JAR, 471 classes, SHA256
`b21ab05e3c15f61e90ac322e1c161434f8ac5ecce4911378d02112af654851c8`.

## BP-37 A/B/C: rising rails and three joined crossbars

Catalogue 004 now contributes BP-37, normalized to the unchanged 15×19×15,
E and seven canonical mineral positions/masks. Both side rails rise toward +Z
through staggered, backed ladder wells. West landings use feet Y=3/9/14 and east
landings Y=4/10/15; the upper west alcove has its own supported step. Three real
crossbars connect the rails at low, middle and high levels. Rock remains between
them. Native stone stairs handle the small height offsets without mandatory jumps;
three-high clearance and two-wide circulation remain required. Every stair writes
its own stone support instead of relying on existing terrain.

A has three crossbars. B replaces only the middle one with an ascent to a two-wide,
off-center landing and a descent to the east rail. C divides only the lower bar
into two channels around a rock core, then rejoins them. The middle and upper bars
remain direct. Canonical alcoves whose Y differs from a rail landing have short
supported terminal branches; they do not become extra crossbars or new rewards.

The shared route oracle admits vertical transitions only on backed ladders or
explicit oriented stair treads, never generic one-block jumping. Dedicated tests
remove all but each crossbar in turn and require both travel directions to work;
removing all three must disconnect the rails. C tests either/both fork channels
with the other crossbars blocked. B tests its two slopes, landing and clearance.
Actual placement must retain stair/ladder states; compensation must restore the
original stone and preserve foreign stair replacements. Existing growth masks,
all five qualities, DRY/cache, survey functions and source chunk checks continue.

The matrix now covers 8,640 plans (9 depths × 12 forms × 4 accesses × 4 anchors ×
5 qualities), plus 48 actual Feature placement/commit/dedup pairs. The 2,500-cell
production ceiling is unchanged. Coverage is 12/120 implemented, 108 pending;
qualification remains distinct and is recorded against the exact source in PR64.
No previous test result qualifies this new geometry. Client visual acceptance is
NOT_PERFORMED; no PC operation, merge, release or Nether activation is included.

Accepted BP-27 predecessor: `4f0eeeb8474348289932baff551116232cffe697`, 16 green
jobs (baseline 38911681369/38911688246; current pack 38911681025/38911687823),
716 JUnit and 88+88 GameTests per stack, streaming/restart, 2,482 cells maximum
over 6,480 fixtures. Its non-installed 472-class JAR SHA256 is
`a709d07f03b52c4eb0395d5fb7d88ee06aeb94b0c316b86abe03ff7d7d74a3dc`.

The first BP-37 CI at `43d31b3` found an inaccessible N7 in B: its rising middle
bar obstructed the old flat approach. The fixed node/mask is retained, and a
two-wide terminal descent now joins its Y=9 front to the raised landing. Both
pure and composed route tests had rejected the defect; no failed run is treated
as qualification. Foreign-edit compensation now also requires every other owned
cell to be restored, and every stair requires explicit support and clearance.

The second pass at `9ab7d8a` restored N7 but rejected a high stair under N3's
reserved rock and measured 2,597 composed cells in the largest C/camp fixture.
The upper step is moved north of that immutable mask. Unused third/fourth gallery
lanes and alcove margins are trimmed while retaining two-wide rails, every branch,
all three crossbars, the split lower route and every room function. A/C also keep
a two-wide bypass around N7's back mask. The cap remains 2,500; no test is relaxed.
