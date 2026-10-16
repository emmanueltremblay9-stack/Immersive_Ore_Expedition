# Catalogue geometry integration — first implementation slice

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
and supported routes are retained. BP-16 A is described below; the remaining 38 families are not implemented; see the four-catalogue coverage map below.

The existing natural `ExpeditionSiteBlueprints` path now selects BP-05 A/B/C or BP-16 A using one four-way draw
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

Added integration tests cover all four shapes, four access types, all qualities,
source-chunk containment including negative coordinates, all four variants at all nine
approved depths, unchanged depth and
anchor bounds, complete-plan size, three-high supported routes to all alcoves,
all growth masks, canonical counts, DRY 0–5 residues plus cache, and real
`Feature.place` staging/commit/deduplication for all four variants at all four accesses.
The existing GeOre/Certus runtime tests additionally exercise ranks, fallbacks,
Jade metadata, persistence, compensation and actual resource-profile admission.

No new server/profile, PC operation, Nether activation, merge or publication is
part of this work. The installed `382e0b4` provenance remains separate. Client
visual qualification remains NOT_PERFORMED. This slice implements four of 120 described
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
| 002 | BP-11–BP-20 | BP-16 A | BP-16 B/C and all A/B/C of the other nine families (29) |
| 003 | BP-21–BP-30 | None | All A/B/C (30) |
| 004 | BP-31–BP-40 | None | All A/B/C (30) |

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
Y=6–13 to keep the axes separated even when the source terrain contains a void.
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
