# Catalogue geometry integration — incremental implementation

## Open client qualification issue — 2026-10-17

The owner reports ef222fd installed with its verified hash and an apparent
ore/biome mismatch at 08:42 UTC. **Not reproduced or resolved.** See the current
incident in [PATH_TO_3_0.md](PATH_TO_3_0.md) for the exact contract, source trace,
static artifact comparison and missing reproduction evidence. Automated catalogue
qualification below remains valid only within its stated scope; it does not close
this client report. The 480 DRY Certus fixtures inject a resource profile and use
prepared terrain, so they are not proof of live instance biome routing.

## Catalogue complet — reçu runtime qualifié

**120/120 variantes A/B/C des quarante familles GOV sont implémentées et qualifiées**
sur le source runtime `ef222fd0eb37f3d203b63304e803a16df08a2221`, branche existante PR64.
Les quatre workflows totalisent **16 jobs verts**. Chaque pile a **716 JUnit**
sans échec/erreur/ignoré, **164 GameTests minimaux + 164 complets**, puis streaming
et reprise dans deux processus distincts sans replay. Les reçus de reprise portent
ce source exact. API/ABI et admission des versions TesT play2 sont recontrôlées.

Matrice : **86 400 plans composés**, maximum **2494 cellules**, garde-fou
2 500 inchangé. **480 placements/commit/dédup réels sont DRY Certus seulement**
(120 variantes × quatre accès) ; les qualités productives relèvent de la matrice
de plans et des tests communs, pas de 480 placements par qualité.

JAR `immersive_ore_expedition-0.2.50-alpha-neoforge-1.21.1.jar` : **509 classes compilées**, métadonnées
NeoForge et bornes de compatibilité vérifiées. SHA256 recalculé :
`d0faa1857862fa92da96309489b5e7d275ba3e9b7ae9625aa08d00f6a04cc6d7`.
Artefact CI : https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/39010669260/artifacts/12093535116

- pull_request / baseline: https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/39010671587
- pull_request / TesT play2: https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/39010671629
- push / baseline: https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/39010669260
- push / TesT play2: https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/39010669238

Les assertions de parcours travaillent sur les cartes de blocs ; elles ne prouvent
pas une marche de joueur ni une exploration visuelle. Aucun corpus de similarité
normalisé par rotations/miroirs ni garantie numérique d'unicité globale n'est revendiqué.
La limite mesurée vaut pour ces fixtures ; le refus fail-closed reste actif ailleurs.
Au reçu initial le JAR catalogue n'était pas installé ; le retour utilisateur
ci-dessus actualise cet état. Aucun accès PC depuis ce fil, fusion, release, publication
ou activation Nether. Overworld TRUE, OreGuard indépendant, retrogen OFF,
Nether production OFF. **Visuel/client NOT_PERFORMED**.

Ce reçu distingue le source runtime ci-dessus du commit documentaire qui le porte.
L'identité de ce dernier est résolue dans l'historique Git et le reçu PR, jamais
prétendue auto-inscrite ici. Les étapes et chiffres antérieurs ci-dessous sont
historiques et ne sont pas transférés à un autre source.

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
and supported routes are retained. The earlier BP-16 slice is described below as history; all forty families are now implemented. See the four-catalogue coverage map and the pending final qualification below.

The existing natural `ExpeditionSiteBlueprints` path now selects BP-01 through BP-40 A/B/C using one 120-way draw
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

## Current validation scope and historical receipts

The isolated composer at `b275df08202b3912574bdb8ae6e45c1cd70ef155` passed 16 CI jobs,
716 JUnit and 81 minimal + 81 full GameTests per stack, plus streaming/restart.
Its separate CI JAR SHA256 is
`fb60003635d4e5f681f68e64e186ff6f0d2029441175bce6b8537dd01d01cdd0`.
That receipt does not qualify the subsequent natural-routing changes described
above. Their exact source and final CI receipt are recorded separately in PR64.

Integration tests cover all 120 implemented shapes, four access types and all five
qualities: source-chunk containment including negative coordinates, all nine
approved depths, unchanged anchor bounds, complete-plan size, three-high
supported routes to all alcoves,
all growth masks, canonical counts, DRY 0–5 residues plus cache, and real
`Feature.place` staging/commit/deduplication for all 120 variants at all four accesses.
The existing GeOre/Certus runtime tests additionally exercise ranks, fallbacks,
Jade metadata, persistence, compensation and actual resource-profile admission.

No new server/profile, PC operation, Nether activation, merge or publication is
part of this work. The installed `382e0b4` provenance remains separate. Client
visual qualification remains NOT_PERFORMED. Current coverage is 120 of 120 described
variants; 0 remain unimplemented. The exact-source receipt above qualifies the final thirty variants. The first-slice and prior-cycle receipts below are historical. Runtime qualification of each successor must
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
| 001 | BP-01–BP-10 | BP-01, BP-02, BP-03, BP-04, BP-05, BP-06, BP-07, BP-08, BP-09, BP-10 A/B/C | None; exact-source qualification recorded above |
| 002 | BP-11–BP-20 | BP-11, BP-12, BP-13, BP-14, BP-15, BP-16, BP-17, BP-18, BP-19, BP-20 A/B/C | None; exact-source qualification recorded above |
| 003 | BP-21–BP-30 | BP-21, BP-22, BP-23, BP-24, BP-25, BP-26, BP-27, BP-28, BP-29, BP-30 A/B/C | None; exact-source qualification recorded above |
| 004 | BP-31–BP-40 | BP-31, BP-32, BP-33, BP-34, BP-35, BP-36, BP-37, BP-38, BP-39, BP-40 A/B/C | None; exact-source qualification recorded above |

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

Follow-up width review adds explicit supported-turn assertions at N2, N7, the
upper west landing and C's lower east branch. The N2 landing occupies two rows;
C's east stair moves one cell along its lower channels so the alcove junction
retains both lanes. Unused outer alcove margins are trimmed instead of narrowing
these turns. Mineral reservations, all destinations and rewards remain fixed.

## BP-03 A/B/C: three rounded lobes in a chain

Catalogue 001 contributes BP-03 around the fixed seven mineral reservations.
Three rounded, differently sized cavities use opposite diagonal lower/upper
footprints and an offset middle ellipse. Lower and middle roofs have raised
central caps; the upper lobe fits below the unchanged envelope roof. Two narrow,
offset backed necks are the only inter-level connections. Rock between the lobes
prevents a single open view through the entire chamber. This is a closed-pocket
chain, not a relabeling of the terrace ledges. E, dimensions, masks and rewards
remain unchanged; the proposed catalogue coordinates are not substituted.

A retains three lobes. B divides the middle lobe with rock into a west floor at
Y=9 and an east floor at Y=10, joined by a two-wide stair opening. Short supported
branches preserve the north-middle and east-middle nodes at their original Y=9.
C adds a small satellite pocket to the upper neck, separated from the
upper lobe by rock. It contains no additional node, lottery, cache or reward and
is not required to reach any existing mineral alcove.

Dedicated tests cut each neck independently and together, checking the expected
set of reachable upper/middle/lower alcoves. B's stair opening is cut in both
travel directions to prove separation of its subpockets. Cutting C's attachment
must isolate only the satellite and preserve every mineral route. Common tests
retain the canonical masks/qualities, backed ladders, stair supports and clearance,
ordered compensation, idempotence and foreign-edit ownership. Actual Feature
placement verifies all fifteen forms across the four natural entrances.

The matrix expands to 10,800 composed plans (9 depths × 15 forms × 4 accesses ×
4 anchors × 5 qualities), plus 60 real placement/commit/dedup pairs. The production
cap remains 2,500 cells. Coverage is 15/120 implemented, 105 pending; qualification
is recorded separately at the exact head in PR64. No past success qualifies this
new source. No installation, merge, release or Nether activation is included;
client visual acceptance remains NOT_PERFORMED.

Accepted BP-37 predecessor: `8332c198b181f8a89e875fce13aefdea98442b18`, 16 green
jobs (baseline 38917914872/38917925322; current pack 38917914849/38917925501),
716 JUnit and 90+90 GameTests per stack plus streaming/restart. Maximum 2,482
cells over 8,640 fixtures; non-installed JAR, 473 classes, SHA256
`d7d94ed7d3e411a29adc7f4b613f7a0114176dda0673cf8a2cb217d380d7df94`.

The first BP-03 CI at `3cfd48a` passed the topology, masks and compensation tests
but rejected a 2,511-cell C/camp fixture. Only the satellite depth is reduced: a
two-wide entrance now leads to a 2×2 terminal floor, retaining its distinct rock
separation and optional attachment. All three main lobes, mineral alcoves and
functions are unchanged. The 2,500-cell assertion and production cap remain fixed;
that failed run is not a qualification receipt.

## BP-23 A/B/C: two offset loops and a real central junction

Catalogue 003 contributes the asymmetric vertical eight. Its upper loop extends
south, its lower loop north, around distinct explicit rock cores. Four backed
vertical arms meet at a supported 2×4 central landing J at feet Y=9; J is never
an open fall shaft. Main passages and ladder routes are two-wide and three-high.
Terminal mineral approaches fit around the unchanged masks; no route requires
mining a heart or its peripheral rock. The fixed envelope, E and seven positions
remain authoritative over the proposed catalogue coordinates.

A connects the two circuits only at J. B adds an exterior backed return from the
lower western arm to the upper western arm, bypassing J without merging the
cores. C cuts one two-wide passage through the lower core; the upper core remains
closed. The lower ordinary crossing and this local slit remain separated by rock.
Core blocks are explicit in the plan, so an existing terrain cavity cannot silently
remove the intended rock mass. Existing fluid/protection/chunk checks still apply.

New tests close either/both upper arms and either/both lower arms, check J's floor
and clearance, and cut J to distinguish B's exterior return. Cutting that return
as well must separate the circuits again. With J and the ordinary lower crossing
blocked, only C must retain a route through the lower core; cutting the slit too
must disconnect it. All cuts are tested in both travel directions. Common masks,
all qualities, DRY rewards, room functions, placement and compensation remain
covered, including idempotence and preservation of a foreign route-block edit.

The matrix now contains 12,960 plans (9 depths × 18 forms × 4 accesses × 4 anchors
× 5 qualities) and 72 real generator placement/commit/dedup pairs. The composed
2,500-cell ceiling is unchanged. Coverage: 18/120 implemented, 102 pending;
catalogues 001/003 each have two families, 002/004 each one. Exact-source CI and
JAR identity remain separate qualification evidence in PR64. No installation,
merge, release or Nether activation; client/visual acceptance NOT_PERFORMED.

Accepted BP-03 predecessor: `82a9e16be7db690497617558f009fe2d428814ee`, 16 green
jobs (baseline 38923332852/38923345024; current pack 38923333228/38923345099),
716 JUnit and 92+92 GameTests per stack plus streaming/restart. Maximum 2,488
cells over 10,800 fixtures; non-installed JAR, 474 classes, SHA256
`5320a99af024ad1bc4a3681512b982a120ba05d66ca53df91d90055a312de91f`.

The first BP-23 CI at `590b4bf` passed routes, masks and compensation but rejected
a 2,600-cell B/camp fixture. Explicit core footprints are compacted to distinct
solid bodies (upper 3×2×2; lower 3×4×3 with the existing southeast notch and C slit).
J retains a two-wide, four-long supported floor; unused third-lane/terminal margins
are removed. Tests now assert every block of both compact cores, along with the
unchanged arm cuts, route clearance and 2,500-cell limit. No mineral position,
function or connection is removed; that failed run is not qualification evidence.

The next pass at `8dc9bc6` retained all route/mask/compensation checks but still
measured 2,522 cells. The upper circuit's southern traverse moves from Z=11–12
to Z=9–10, and its east side ends there. N3's fixed terminal branch still reaches
Z=12 on the west. Both full-width traverses are now explicitly tested for support
and three-high clearance; no arm, node or room function is removed. Upper/lower
circuits remain offset in Z around their distinct cores, within the same envelope.


## BP-02 A/B/C: a narrow bayonet fracture

GOV001's proposed dimensions are normalized to FINAL's 15×19×15 envelope,
fixed entrance and all seven mineral reservations. Two alternating elbows lead
from the upper east descent across the middle landing to the lower west descent.
The main fracture has supported two-wide, three-high passages; terminal mineral
fronts and their immutable masks use the same shared finalizer as every family.
A has only the two serial descents. B adds an independently backed east descent
from the middle level to the lower one, rejoining the lower terminal approach.
C widens the upper elbow into a terrace and extends a short sterile branch;
cutting that branch leaves all seven mineral fronts reachable. Neither B nor C
adds a node, reward, cache, reserve or resource draw. The lower room anchor lies
on the actual lower traverse; survey functions and the other room anchors remain.

Tests cut the upper neck, either/both lower routes, and C's branch in both
travel directions. Full traverse widths, unique signatures, every fixed mask,
actual natural placements, idempotent compensation and foreign ladder edits are
covered. The expanded shared matrix contains 15,120 composed plans and 84 actual
Feature placement/commit/dedup pairs. The production ceiling stays 2,500; these
counts describe fixtures, not yet a successful qualification of this source.
Coverage is 21/120 implemented and 99 pending. Exact CI/JAR evidence follows in
PR64 after the new source passes both baseline and current-pack runtimes.

Accepted BP-23 predecessor: `cf96d078560ea382b2ee0752045a1a579ce40e55`, 16 green
jobs, 716 JUnit and 94 minimal + 94 complete GameTests per stack, streaming and
separate-process restart. Maximum 2,488 over 12,960 plans; 475-class JAR SHA256
`2ac0f1436786cb96c33e0bf24fa0f304daa7757a2d665d3280c0dc9ee509df1a`.
Those results do not qualify BP-02. No installation, merge, release or Nether
activation. Client/visual qualification remains NOT_PERFORMED.


BP-02 qualification at `60b2121` passed all other GameTests but the expanded
21-form seed matrix exposed a BP-23 B/camp fixture at 2,502 cells. The earlier
18-form fixture seeds were different; their success was not a global bound.
The upper BP-23 core is narrowed from three to two cells in X, retaining an
explicit solid 2×2×2 body. Its lower boundary already belongs to the surrounding
shell, so this removes two unique writes. Both loops, every route and fixed
mineral mask remain unchanged. The upper core test checks every retained cell;
all families and composed contexts are requalified at the new source.


## BP-06 A/B/C: bifurcated roots

The fixed mineral alcoves branch from a narrow, explicitly backed central trunk.
A carries the trunk to the lower fork. B forks earlier: independent west/east
lower descents lead to separate terminal roots, connected only through the middle
junction. C retains A's deep fork and adds one east lateral connection, producing
one cycle between the middle and lower branches. The upper trunk remains the
only connection to the high branches. Separating rock between the middle terminal
branches prevents an accidental additional loop. Every vertical link has a
supported ladder, and the trunk's backing is explicit even beside a branch.

Cutset tests isolate high from middle/low branches, cut each staged descent,
check that low B terminals connect only through the middle level, and cut either
or both C routes in both travel directions. A terminal branch cut isolates only
its own alcove. Shared width, growth-mask, actual placement, foreign-edit ownership
and idempotent compensation checks cover all forms. Existing functions, qualities,
ranks, node counts and rewards remain unchanged. B's lower room anchor follows
the western lower terminal; A/C use the actual deep-fork landing.

The matrix expands to 17,280 composed plans and 96 Feature placement/commit/dedup
pairs. Coverage: 24/120 implemented, 96 pending. The 2,500-cell guard is unchanged;
CI must qualify the new source independently before its receipt is published.
No installation, merge, release or Nether activation. Visual/client NOT_PERFORMED.

Accepted BP-02 predecessor: `eef708fbe0d4f182346aaa0d63f7276613778dc1`, 16 green jobs, 716 JUnit,
96 minimal + 96 complete GameTests per stack plus streaming/restart; maximum
2,500 over 15,120 plans. JAR SHA256
`10d830bd1ca97ef4506e2ea301e2b7800b2539597bba4e516fa2ccfa8252130d`. These results do not qualify BP-06.


## BP-10 A/B/C: seven pockets and an explicit connection graph

Seven small, irregular pockets surround the fixed mineral reservations. The
reference graph is retained: 1–6, 6–3, 4–2, 2–5, 6–7, 7–2, 1–4 and 3–5.
Two backed necks connect the left pockets through N6; the right neck serves N4,
N2 and N5. The middle spine passes through N7's pocket. Rock separates that spine
from a direct N6–N2 shortcut. A's lower and upper transverse links produce two
loops. B removes precisely those two links, making the middle spine mandatory.
C adds a separate connection from N3's upper pocket to the edge of N2's middle
pocket. Its shaft and upper approach remain separated from the original upper
cross-link. No eighth pocket, node, quality roll or independent reward is added.

Tests cut either/both lower and upper necks, temporarily excluding C's extra
edge when comparing the base graph. They check each pocket's reachable component
in both directions, cut B's mandatory middle spine, and isolate C's extra route
from both original upper necks and the upper transverse link. Cutting that extra
route then removes the last high-to-middle connection. Full corridor widths,
fixed masks, real natural placements and ownership-safe compensation are included.
The lower room anchor follows the actual N1 pocket. Survey functions and the
upper calcite marker remain unchanged. The 2,500-cell ceiling is unchanged.

Coverage: 27/120 implemented, 93 pending. The expanded matrix contains 19,440
plans and 108 real placement/commit/dedup pairs. These are fixture counts pending
exact-source CI qualification, not transferred predecessor results. No PC action,
merge, release or Nether activation. Visual/client qualification NOT_PERFORMED.

Accepted BP-06 predecessor: `6c47363207ee78b19921df94cd224d5af22dca13`, 16 green jobs, 716 JUnit,
98 minimal + 98 complete GameTests per stack plus streaming/restart; maximum
2,499 over 17,280 plans. JAR SHA256
`7f9d00ed6c2eda7c1238414c1c756d7b59be9c46f14bf88b0bfd33ef82aee16f`. These results do not qualify BP-10.


## BP-14 A/B/C: parallel grooves and an offset collector

Three tall, parallel grooves have unequal widths, lengths and vertical extents.
The left roof rises toward its rear alcove; the central floor starts higher and
its roof ends lower; the right floor steps down toward the rear shaft. Shelves
and backed ladders serve every mineral front. The collector crosses the left
lamina to the south, crosses the right one to the north, and rises one block on
two-wide stairs. The upper entrance reaches only the left groove; rock separates
it from the central roof and from the right upper shelf.

A connects the grooves only through the collector. B opens a low window through
the left/central lamina, with two-wide east-facing stairs up to the central floor.
C retains a 2×3×2 central rock massif beyond the common north descent; a two-wide
bottom detour reconnects the interrupted groove. Its outer wall prevents a new
connection into the right groove. No reward or extra resource occupies the bottom
detour or the ladder supports. The left upper exit has a plain-rock route beside
the reserved mineral periphery, and the lower room anchor remains in the N1 pocket.

Tests cut each collector connection in both directions; only B retains a left/
central route, and cutting its low window removes that last route. C's massif is
checked cell by cell; after excluding the upper descent, cutting the bottom detour
separates the interrupted floor while all seven mineral fronts remain reachable
from the entrance. Shared stair clearance/support, widths, masks, actual placement
and ownership-safe idempotent compensation checks include all new forms.

Coverage: 30/120 implemented, 90 pending. The matrix contains 21,600 composed
plans and 120 real placement/commit/dedup pairs. Exact-source CI remains required;
these fixture counts are not predecessor proofs. The 2,500-cell cap, functions,
quality/rank ordering, rewards and independent lotteries remain unchanged.
No PC operation, installation, merge, release or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Accepted BP-10 predecessor: a9b3ad273fe2ba89fb5c84e8d585ee1db836e961, 16 green jobs, 716 JUnit,
100 minimal + 100 complete GameTests per stack plus streaming/restart; maximum
2,499 over 19,440 plans. JAR SHA256
8aa9a9671d3c2c50f1ab5fb283eeda31a2f584c566f3dfcd607c74c6ae43f933. These results do not qualify BP-14.


The first BP-14 source b174d38 passed the topology/mask/compensation tests but
rejected a 2,734-cell C/camp fixture and a real A/camp placement. The right groove
is reduced to two-wide circulation with wider terminal node alcoves; its three-wide
raised corridor was unnecessary. The central floor moves from Y4 to Y5 and its
roof from Y13 to Y11, retaining three-high bottom and middle routes. B uses two
two-wide stair steps to reach that floor. C keeps the shared north descent and
detours around its massif on the bottom level, avoiding a second vertical column.
Its separating outer wall remains. No node, reward, survey function or required
route is removed; all cutsets and actual placements are requalified. The cap stays
2,500 cells. These corrections are not yet a successful qualification.

The next BP-14 source c62e4f8 passed the other tests but still measured 2,561
cells and rejected an A/camp placement. The unused right intermediate floor is
raised to the existing collector floor (Y10); its rear foot remains Y3 and all
nodes stay fixed. The left northern roof changes from Y11 to Y10, retaining N7's
reserved upper air face and every three-high route. C's redundant outer wall
rows below/above its three-high side wall are omitted; the shell already supplies
that entire wall, now asserted cell by cell. No room function, alcove or mandatory
route is removed. Full qualification restarts without raising the cap.


## BP-24 A/B/C: interleaved wall-rooted beaks

Three rock beaks remain attached alternately to the west/east/west walls. Their
points overlap in projection but are separated vertically by explicit air gaps.
The route contours the upper point, descends south of the middle beak, contours
its western point, descends to the north of the low beak, then contours its east
point. Each principal corridor is two blocks wide, three high and supported;
two backed ladder pairs provide the changes in height. No moving rock or trap.

B opens a two-wide, three-high window through the middle root, preserving its
floor, roof and side frames. C splits the upper mass with a walkable notch;
the unequal prongs retain their common solid base and wall attachment. The outer
contour remains available. The fixed mineral positions and growth/periphery masks
are unchanged. Room anchors follow the actual middle south and lower north routes;
all access, survey, cache and reward functions are retained.

The catalogue's proposed 23-high envelope, entrance and node orientation are
normalized to FINAL_DESIGN's 15×19×15 envelope, E=(7,15,1), seven fixed nodes and
existing masks. This changes geometry only, not quality/rank selection, node or
reward budgets, independent lotteries or resource admission. DRY remains sterile
apart from its existing contracted residues/cache.

Two specific GameTests verify wall roots, continuous bases, vertical separation,
supported contour widths and bidirectional reachability after cutting each point
contour. Only B's root window or C's upper notch can provide the corresponding
alternative; cutting that alternative removes the last connection. All forms
also enter the shared mask/quality/DRY/compensation and real-generator tests.
The existing fixture-only metadata halo covers wall-edge collision margins;
production still refuses unavailable metadata without loading chunks.

Coverage in this source: 33/120 variants, 87 pending. Matrix: 23,760 composed
plans and 132 real placement/commit/dedup pairs. Qualification is pending at the
new exact source; the 2,500-cell production limit remains unchanged.

Accepted BP-14 source: 17fc48887358e8ffb94a6cb4b67cb85b115b0cda, 16 successful jobs,
716 JUnit, 103 minimal + 103 complete GameTests per stack plus streaming/restart;
maximum 2,494 across 21,600 plans. Its 479-class JAR SHA256 is
8f42a09a7e420b1818b00940cb769af51d9db847e3d5217f0d44e36fa2b3fe78.
Those proofs remain independent of this new source. No install, merge, release or
Nether activation. Client/visual qualification remains NOT_PERFORMED.


## BP-35 A/B/C: chimney with alternating wall lodges

One four-by-four vertical void remains visible beside a west circulation spine.
Two backed ladder columns and supported landings serve seven wall destinations.
Rock rails separate the landings from the void; two-high sight openings preserve
views. The north/east columns of the void stay open throughout its full height.
The cache ledge and middle access bridge occupy only part of its rear/west side.
The bottom is accessible by a short side doorway, with no mandatory fall or jump.

A's wall lodges terminate their branches. B connects two middle lodges through a
bent rear balcony, adding only one local return. C instead connects the high rear
lodge to the lower eastern lodge by a separate backed descent. Other destinations
remain terminal, without an added peripheral ring. All seven fixed masks and
canonical ranks/budgets remain. Proposed heights/positions are normalized to the
existing FINAL envelope and mineral layout; no quality or reward reroll is added.

Specific tests assert the uninterrupted void, protected landings and views, then
cut the main spine: only C's rear descent can retain a high/low return. Cutting it
removes that alternative. A middle branch cut leaves only B's local balcony; its
own cut removes the final return. All checks are bidirectional, alongside the
shared masks, DRY/cache, real Feature placement and ownership-safe compensation.
The collision metadata fixture remains explicit; production never loads neighbors.

Coverage: 36/120 implemented, 84 pending. Expanded matrix: 25,920 composed plans
and 144 real placement/commit/dedup pairs. The production cap stays 2,500 cells;
new exact-source qualification remains required. No install, merge, release or
Nether activation. Client/visual qualification remains NOT_PERFORMED.

Accepted BP-24 source 33d7d3fd5f20fb04599c12f00c9b0543892f2860: 16 successful jobs, 716 JUnit,
105 minimal + 105 complete GameTests per stack plus streaming/restart; maximum
2,500 cells over 23,760 plans. Its JAR SHA256 is
30d8c8176bd7d98b8f14224accdab9fbbacf7dbe5fe5437c40a4c8c9b8a2b7cb. These are predecessor proofs only.


## BP-31 A/B/C: six directions around a hollow vestibule

A five-high hollow vestibule connects four unequal lateral arms and two backed
vertical arms. Its central circulation remains air, not a solid pillar. Fixed
mineral alcoves attach to those routes; the west/south alcove is a wall recess,
not a seventh arm. Both vertical arms have supported landings. The lower room
anchor follows its actual landing. The upper cache and all survey functions remain.

A has no exterior return between lateral destinations. B adds a staggered diagonal
connection between the north and east arms, around the north wall of the upper
shaft. C instead moves the upper shaft to the eastern corner, bends its route,
and adds a supported observation landing with a rock sill and a two-high viewing
opening toward the vestibule. It does not require a fall into the central room.

The proposed dimensions/node sectors are normalized to the existing FINAL envelope
and fixed masks. Quality/rank order, budgets and independent lotteries are unchanged.
Specific tests verify returns from all six endpoints to the hollow vestibule,
cut the straight/corner upper links independently, and check C's supported view.
A north-neck cut leaves only B's diagonal return; cutting that return isolates
the endpoint in both directions. Shared generator/mask/quality/DRY/compensation
checks include all earlier forms. Production never force-loads metadata.

Coverage: 39/120 implemented, 81 pending. Expanded matrix: 28,080 composed plans
and 156 real placement/commit/dedup pairs. The 2,500-cell cap is unchanged; exact
qualification is pending at this new source. No installation, merge, publication
or Nether activation. Client/visual qualification remains NOT_PERFORMED.

Accepted BP-35 source 8cda8fb3b227e7cffc3d6497b4692ccb1aeb4ae1: 16 successful jobs, 716 JUnit,
107 minimal + 107 complete GameTests per stack plus streaming/restart; maximum
2,488 cells over 25,920 plans. Its JAR SHA256 is
60785441781d6b2e916c11e0e51881011c121dceefe7b6bfdaabe1240020cbc9. These remain predecessor proofs only.

BP-31 review correction before qualification: candidate 2686f4f had a one-cell
corner beside the lower shaft's backing. The west arm now enters north of that
backing into a wider hollow vestibule; its two-wide support is asserted explicitly,
as are the north/east arms. The north endpoint is offset to the east, while the
fixed north mineral alcove remains a recess of the vestibule. B's outer return
follows that endpoint. No mineral position, mask, budget or write cap is changed.
The new exact-source tests replace the unqualified candidate's runs.

The initial BP-31 candidate 2686f4f also failed the positive GeOre-family fixture
for DRY Aluminum. With 39 forms, DRY seed 2 selects BP-24 A (ordinal 30), whose
west root requires neighboring collision metadata. The catalogue fixture already
prepared this halo; the older family fixtures did not. A shared test-only helper
now prepares the exact selected geometry's metadata halo for positive camp tests.
The absent-neighbor negative test and production fail-closed/no-load checks remain
unchanged. Initial candidate matrix maximum was 2,497; no success is transferred.

B's relocated north-east return keeps staggered two-wide turns, rather than a
rectangular corner. Both the offset turn and the full eastern segment have
explicit support/clearance assertions before the independent cut tests.


### BP-31 — complete-runtime fixture correction

At ea22bd7, the baseline PR complete runtime rejected the positive
`certusAndEntroFeatureTransactions` fixture for `entroized_fluix POOR`.
The catalogue matrix itself stayed within 2,497 cells. The special-profile
fixture and generic positive MINER_CAMP fixture now explicitly prepare and
check the same exact geometry metadata halo as the GeOre/catalogue fixtures.
The separate missing-neighbor test still requires rejection without forced
loads. Production guards, geometry, rewards and the 2,500-cell cap are unchanged.
This correction needs a new exact-source CI; ea22bd7 is not qualified.


## BP-26 A/B/C: rooted rock stem and visitable cap

A single two-by-two host-rock stem rises from the floor to a broad, irregular,
three-thick cap. Both its upper face and its overhanging underside are visitable
by supported routes, with a peripheral backed ladder and lower access around
the stem. This is rock geometry, not a biological mushroom or a mineral mass.
The main route does not wrap the stem in a mandatory helix.

A retains the full cap. B cuts a radial northern slit without severing the stem;
a second backed ladder and landing in that slit connect the top to the underside.
C instead makes two unequal cap lobes joined by a solid isthmus, with a separate
supported passage beneath their inner faces. The upper cache marker remains on
intact rock. Middle/lower room anchors follow the actual underside/base routes.

The proposed envelope and node sectors are normalized to FINAL_DESIGN's existing
geometry and seven fixed masks. All canonical quality/rank budgets, independent
lotteries, surface access and survey/cache functions remain. Specific tests check
the rooted stem, cap/underside/base returns and supported clearance. Cutting the
peripheral descent leaves only B's slit descent; cutting both removes that return.
Cutting C's upper isthmus separates the lobes while preserving the lower passage.
Shared masks, actual generator and ownership-safe compensation cover all forms.

Coverage: 42/120 implemented, 78 pending. Expanded matrix: 30,240 composed plans
and 168 real placement/commit/dedup pairs. The 2,500-cell production cap stays;
new exact-source qualification is required. No installation, merge, publication
or Nether activation. Client/visual qualification remains NOT_PERFORMED.

Accepted BP-31 source 05d8587fdd77e367b9f43bb8e7d1489e8f8f3670: 16 successful jobs, 716 JUnit,
109 minimal + 109 complete GameTests per stack plus streaming/restart; maximum
2,497 cells over 28,080 plans. Its JAR SHA256 is
57461f20eb775238abdf7e47b736eec465c06916df2f8afd76fc7c3649352983. These remain predecessor proofs only.


### BP-26 — composed-budget correction

Candidate 9b18ada reached 2,920 cells in the composed MINER_CAMP Motherlode
matrix and C was refused by actual placement. The new cap trims unused rear
shoulders and tapers the intermediate viewing space around the exposed single
stem. Walkable base/underside routes, upper cap, B's slit and C's isthmus remain;
no main passage is narrowed. The 2,500-cell guard, fixed masks and rewards are
unchanged. New exact-source CI is required; the failed candidate is not qualified.


BP-26 follow-up: ca30d42 reduced the matrix maximum to 2,573 cells; only the
composed-cap test still failed, on C/Motherlode camp. C now excavates the upper
faces of its two lobes and the actual separating views beside the isthmus,
instead of the full cap's unused upper shoulders. Supported routes, underside,
fixed masks and the production cap remain unchanged. Exact CI required again.


BP-26 follow-up: a9b7c6c reached 2,549 cells, now on B/Motherlode camp;
the cap remained the only failing test. The underside now follows the supported
routes and exposed stem faces instead of unused cap corners; an unused lower
pocket north of the peripheral descent is removed. Width/return/stem tests are
unchanged, as are the upper cap, B's slit, C's isthmus, rewards and 2,500-cell guard.
New exact-source CI is required.


## BP-36 A/B/C: one perforated slab and two shared volumes

One irregular rock slab separates the upper surface from the shared lower
volume. Three differently shaped openings preserve views between them.
A equips two peripheral openings with backed descents; the rear opening has
rock rims and stays a view. B equips that third opening with a supported
ladder and landing. C joins the two side openings into a long slit while
retaining a two-wide upper rock rib. No central stem or stack of sealed rooms.

All FINAL node masks, access/cache/survey functions, independent lotteries and
quality budgets remain unchanged. Tests cut the two side descents, verify only
B retains the third return, then cut that last descent. C's rock rib is checked
for support/clearance and its sole isolated upper connection is cut in both
directions. Shared matrices retain the 2,500-cell cap and actual generator,
metadata-refusal, persistence and ownership-safe rollback checks.

Coverage: 45/120 implemented, 75 pending. Matrix: 32,400 composed plans and
180 real placement/commit/dedup pairs. Exact-source CI still required; no
installation, merge, publication or Nether activation. Visual/client NOT_PERFORMED.

Predecessor BP-26 source 116aac3dbd6f98e509cd04ee5d729afe74bd73c2: 16 successful jobs, 716 JUnit,
111 minimal + 111 complete GameTests per stack plus streaming/restart; maximum
2494 cells over 30,240 plans. JAR SHA256
e57ceff931766dcdebfb10bd42de59c011796f25aa89e4c1f1ba065c0fac23d5. These proofs do not qualify BP-36.


### BP-36 — supported N2 approach

Review of the first candidate found that the rear descent's backing occupied
one cell of the N2 approach turn. B now places its rear ladder on the western
rim (X7, backing X6), with the landing at X8. The N2 branch uses X8/9 and its
full two-wide turn stays open. Explicit support/clearance assertions cover the
branch and landing. The rear L-shaped view, fixed masks and budgets remain;
qualification must use the corrected source, not e4ab137.


## BP-11 A/B/C: offset funnels and an equipped narrow waist

Two continuous, differently centered funnel volumes narrow toward a shared
waist. Supported shoulders connect the fixed alcoves separately. A has one
backed descent on the waist shoulder. B divides the waist with one rock blade
and provides two backed descents, each reaching both basins. C keeps the waist
visual, extends the upper balcony over the lower volume and uses a separate
lateral descent. This is two continuous funnels, not a three-pocket chain.

The upper cache marker and survey/access functions remain. Middle/base anchors
follow supported routes. FINAL masks, quality budgets, rewards and independent
lotteries are unchanged. Specific tests check broad basins/narrow waist and
continuous flares, supported returns, the two-wide upper shoulder, distinct
vertical cut sets and C's supported overhang with an open view beneath it.
Shared masks, actual placement, persistence and ownership-safe rollback remain.

Coverage: 48/120 implemented, 72 pending. Matrix: 34,560 composed plans and
192 real placement/commit/dedup pairs. The 2,500-cell production cap stays.
Exact-source CI required; no installation, merge, publication or Nether activation.
Visual/client qualification remains NOT_PERFORMED.

Predecessor BP-36 source 0c7386ddbbf8df75f00ae8fbb32f020966f25f42: 16 successful jobs, 716 JUnit,
113 minimal + 113 complete GameTests per stack plus streaming/restart; maximum
2499 cells over 32,400 plans. JAR SHA256
073616dc2c66f37e9c12667d89e7f5e2b20c8f10d3101b690f149d87d68860a4. These proofs do not qualify BP-11.


## BP-17 A/B/C: ceiling-rooted clapper with a free tip

The hanging host-rock core ends above a three-high supported lower crossing.
A has one offset tapering core, B two free tips joined at the roof with a
supported window between them, C one pierced body whose outer bands preserve
its roof connection. Separate backed side descents connect the upper and lower
routes. The upper cache marker, survey room, masks, ranks and rewards remain.

Tests require continuous roof attachment, free tips above the lower floor,
return from both side levels, supported two-wide middle windows, and isolated
window cut sets that preserve the distinct lower crossing. Shared generator,
DRY/cache, fixed-mask, persistence and ownership-safe rollback tests remain.
Coverage 51/120 implemented, 69 pending; 36,720 composed plans and 204 real
placement/commit/dedup pairs. Exact-source CI required. The 2,500-cell guard
is unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-11 source 03aa1afb2b6bd6f726d90e692d16e8266b57cc9b: 16 successful jobs, 716 JUnit,
115 minimal + 115 complete GameTests per stack plus streaming/restart; maximum
2499 over 34,560 plans. JAR SHA256
d2754df8654da3d53a4d8d204b1389240ec5ed85c00683daf0e2c8035ed1d209. These proofs do not qualify BP-17.


## BP-20 A/B/C: faceted belt between displaced narrow tips

The volume is broadest at its middle belt and narrows toward different upper
and lower ends, opposite to the hourglass. A uses two supported face routes;
B truncates the upper tip into a platform with a second upper/belt return;
C divides a lower face with a rock blade into unequal equipped passages that
rejoin the belt and lower end, without adding a satellite pocket.

Specific tests check relative belt/tip widths, continuous facets and displaced
ends, supported returns, B's two upper cut sets and C's two lower cut sets.
Shared masks, budgets, DRY/cache, generator, persistence and ownership-safe
rollback tests remain. The surface access and survey functions are unchanged.
Coverage 54/120 implemented, 66 pending; 38,880 composed plans and 216 actual
placement/commit/dedup pairs. Exact-source qualification required. The 2,500
cap remains; no installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-17 source e4380462931e8074b3c5bac05f52eedcfe51917b: 16 successful jobs, 716 JUnit,
117 minimal + 117 complete GameTests per stack plus streaming/restart; maximum
2498 over 36,720 plans. JAR SHA256
e40f2f24586ccae8b9f86c55f76fd8d2205795ada5c8964cb366bbb245c019f2. These proofs do not qualify BP-20.


## BP-34 A/B/C: unequal rooted piers and an inclined accessible deck

Two continuous host-rock piers of unequal thickness frame a three-high lower
crossing. Oriented stairs raise the deck from the west support to the east.
A retains both solid piers; B opens a small supported window in the thick pier
at the lower crossing level with a separate lower bypass; C splits the
upper deck into two two-wide bands around a slit, reunited at both ends.

Fixed N3 uses a short supported upper stair approach, preserving
its mineral mask and full approach headroom. The cache marker stays on the
flat deck; surface access, survey room, budgets and rewards remain unchanged.
Tests check rooted frames, stair orientation and returns, lower clearance,
B's isolated level-window cut with lower bypass retained, and C's independent band
cuts after isolating the main descents. Shared generator/mask/DRY/cache/
persistence/ownership-safe rollback coverage remains.
Coverage 57/120 implemented, 63 pending; 41,040 composed plans and 228 actual
placement/commit/dedup pairs. Exact-source qualification required; 2,500 cap
unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-20 source 3c853032c01a856c7646a9f5d07511d28cf7f250: 16 successful jobs, 716 JUnit,
119 minimal + 119 complete GameTests per stack plus streaming/restart; maximum
2499 over 38,880 plans. JAR SHA256
3877fbfb495279817340b266c8d0859afc625308f211f4197ad3c2240a5f0330. These proofs do not qualify BP-34.

BP-34 budget correction: the first candidate 70bca21 measured 2,828 cells
and failed the unchanged cap. Unused viewing shoulders are removed; B's window
is at the lower crossing level, retaining its frame and independent bypass.
N3 joins the upper deck by a short stair approach instead of a full extra
shaft; the east ladder serves middle/upper routes. The north middle shoulder
is narrowed around its actual connection. New exact-source CI is required;
static footprint estimates are not runtime proof.


## BP-40 A/B/C: oblique bar rooted at both ends

One host-rock bar shifts along both horizontal axes as it rises from its floor
root to the opposite ceiling attachment. Side routes go around the foot and
both middle flanks without treating the unprepared rock diagonal as stairs.
B adds a two-wide framed middle window; C preserves one foot and divides the
upper attachment into two branches with a separate supported front flank.

Tests verify slice overlap and outer-band continuity, floor/ceiling roots,
prepared returns and lower/middle widths, B's isolated middle-window cut and
C's two attachments and gap. Fixed node/growth masks, entry, cache marker,
survey room and rewards are unchanged. Shared placement, DRY, persistence,
no-load refusal and ownership-safe rollback matrices remain.
Coverage 60/120 implemented, 60 pending; 43,200 composed plans and 240 actual
placement/commit/dedup pairs. Exact-source qualification required; 2,500 cap
unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-34 source 798da98cb632cb386812a89d3f7a5568cdcc6d28: 16 successful jobs, 716 JUnit,
121 minimal + 121 complete GameTests per stack plus streaming/restart; maximum
2494 over 41,040 plans. JAR SHA256
0f6d60e271bd1639605fa010b9910dce2db37d6e0d2316fddff5a0c76a00b621. These proofs do not qualify BP-40.


## BP-13 A/B/C: serial diaphragm windows in a tall nave

Three transverse host-rock diaphragms separate narrow tall bays. Main windows
are high/front, middle/east and low/rear; backed ladders connect their landings.
B adds a second upper middle-wall window, closing a local loop. C adds a rounded
upper arch through the last wall while preserving its low passage. Node fronts
in a wall remain backed alcoves, not unqualified through-window shortcuts.

Tests check supported window widths and bay/alcove returns. Cutting the first
window isolates entry; independent middle-window cuts distinguish B; independent
last-wall cuts distinguish C. Fixed masks, entry, cache, survey room and rewards
remain. Generic generator, DRY, persistence, no-load and rollback matrices extend.
Coverage 63/120 implemented, 57 pending; 45,360 composed plans and 252 actual
placement/commit/dedup pairs. Exact-source qualification required; 2,500 cap
unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-40 source 77c5e5e4f27b2ded8b8f99835fb13dafbb54a295: 16 successful jobs, 716 JUnit,
123 minimal + 123 complete GameTests per stack plus streaming/restart; maximum
2495 over 43,200 plans. JAR SHA256
d9a4800023df92434c1585aa376675a4624a9772b171e5f8d006847b80346602. These proofs do not qualify BP-13.


## BP-19 A/B/C: perpendicular sheets and a lower gallery

Two vertical wings meet at a common edge, with a connected lower gallery.
A retains the sharp middle corner; B replaces it by a chamfered supported
connection; C keeps the corner and adds a diagonal middle piercing through
the opposite host-rock mass. Overlapping passage sections retain two-wide
faces instead of joining diagonal squares through a single-cell corner.

Tests check supported routes in both perpendicular planes, their lower and
upper returns, the retained mass, and independent middle cut sets after
isolating vertical bypasses. The lower gallery survives those middle cuts.
Fixed masks, entry, cache, survey room and rewards remain. Generic generator,
DRY, persistence, absent-neighbor refusal and rollback matrices extend.
Coverage 66/120 implemented, 54 pending; 47,520 composed plans and 264 actual
placement/commit/dedup pairs. Exact-source qualification required; 2,500 cap
unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-13 source c493b79867a1d559129c468d6f89096353612463: 16 successful jobs, 716 JUnit,
125 minimal + 125 complete GameTests per stack plus streaming/restart; maximum
2500 over 45,360 plans. JAR SHA256
ba8c3b3e5a905d76331cbf53dc2f8dd0bbbb7a012e8d513a59b5abfb8b10eea5. These proofs do not qualify BP-19.


## BP-21 A/B/C: separate inner and outer shell domains

The hollow inner envelope has supported lower and upper landings, a backed
internal ladder and a separating roof. A has one high east window; B adds an
opposite lower west window; C opens one east sector across both levels, with
two retained lips and a supported upper walkway. Short oriented stair approaches
preserve the lower outside route. Two exterior returns remain independent of
the interior entrances; the cache stays on the outer cap route.

Normalization retains all FINAL mineral positions outside this small inner
exploration volume. No extra inner node, ore, cache or reward is introduced.
Tests check both supported domains, all alcove returns, window widths, roof
separation, non-mineral lips and independent entrance cuts that isolate the
interior while leaving outside returns open. Fixed masks and shared generator,
DRY, persistence, no-load and ownership-safe rollback matrices remain.
Coverage 69/120 implemented, 51 pending; 49,680 composed plans and 276 actual
placement/commit/dedup pairs. Exact-source qualification required; 2,500 cap
unchanged. No installation, merge, publication or Nether activation.
Client/visual qualification remains NOT_PERFORMED.

Predecessor BP-19 source 01504b296ed83ab7b46beaf204f3ad82b7d5a0b1: 16 successful jobs, 716 JUnit,
127 minimal + 127 complete GameTests per stack plus streaming/restart; maximum
2500 over 47,520 plans. JAR SHA256
746e363b08a76b8207339056899e171077ca626ede538016e99574753628aabf. These proofs do not qualify BP-21.


## BP-04 A/B/C: broken unequal arcs around a retained core

The common lower, middle and upper arcs remain asymmetric and supported.
A uses a short inner return; B adds a supported diagonal upper bridge across
the non-mineral core, closing a separate loop. C removes both other upper
descents and connects its unequal crescents only through a side neck.
The solid central mass stays intact below the bridge and contains no added
resource. FINAL mineral positions, masks, cache and survey room remain.

Tests check supported two-wide arcs, the intact mass and independent cuts:
the bridge still connects upper ends after the ordinary descents are cut;
cutting it disconnects them. C's sole side-neck cut separates its crescents
while preserving the lower arc. Shared DRY/reward, generator, persistence,
no-load and ownership-safe rollback coverage extends to all new variants.
Coverage 72/120, 48 pending; 51,840 composed plans, 288 actual placements.
Qualification at the exact source is required. Cap 2,500 unchanged; no PC,
installation, merge, publication or Nether activation. Visual NOT_PERFORMED.

BP-21 predecessor 68ed832a4cb037e86b85ec078094deb729538c1c: 16 successful jobs, 716 JUnit,
129+129 GameTests per stack plus streaming/restart, maximum
2498. JAR SHA256 b790a7479a18bbe65c197a5d467cebe244271d6f3ebf271d575f0e048918818d.
These proofs do not qualify BP-04.


## BP-15 A/B/C: turning horn sections and distinct mouth terminations

The narrow north/south neck widens into an east/west middle section and a
broad lower mouth; this is internal section variation, not a rotated copy.
A retains a split lower lip and a thicker upper lip. B divides the lower
mouth with a host-rock blade and retains a rear connection. C folds a lip
over a supported lower passage and provides an independent upper ledge.
Backed flank returns preserve the seven fixed mineral alcoves and cache.

Tests check section axes/widths, supported alcove returns, a rear-connection
cut with vertical bypasses isolated, the solid folded lip, and independent
upper/lower passage cuts. Shared budgets, DRY, generation, persistence and
ownership-safe rollback coverage extends. 75/120 variants, 45 pending;
54,000 composed plans and 300 actual placements. Exact qualification required.
The 2,500 cap is unchanged; no install, merge, publication or Nether activation.
Visual/client qualification remains NOT_PERFORMED.

BP-04 predecessor a76cf03f65e4471c8761669ae27779edc825abb2: 16 successful jobs, 716 JUnit,
131+131 GameTests per stack plus streaming/restart; maximum
2499. JAR SHA256 66ae70cb9248a36b7f2f0a48b1cfbe398eef836fa7d27479fab25413c180bf2a.
These proofs do not qualify BP-15.


## BP-32 A/B/C: open tightening volute with controlled terminal variants

An upper outer lip descends through the west arc, crosses the rear arc and
turns inward before a separate east descent and a short supported rise reach
the lower terminal.
A retains the open route with no return shortcut. B adds a backed return
window through the separation between outer and inner portions. C adds two
terminal pockets at different heights, linked by correctly oriented stairs;
there is no second winding. The central rock blade remains between nearby
middle portions. Fixed alcoves are supported side retreats, not new loops.

Tests check supported routes, the blade and independent cuts: closing the
main inner descent leaves a return only in B; closing B's window removes it.
C verifies stair direction, two-wide terminal support and return without jumps.
The common budget, mask, DRY, actual-placement, restart and rollback matrices
extend to 78/120 variants; 42 pending, 56,160 plans and 312 actual placements.
Exact-source qualification required. Cap 2,500 unchanged. No installation,
merge, publication or Nether activation; visual/client NOT_PERFORMED.

BP-15 predecessor 5a40164910e1a58c50fbff4d27d143e9c7964e75: 16 successful jobs, 716 JUnit,
133+133 GameTests per stack plus streaming/restart; maximum
2496. JAR SHA256 70e511ecb674c8822d9947e604c314be47dc538af136d2202220137b63822ffe.
These proofs do not qualify BP-32.


## BP-30 A/B/C: broad wedge with one rising ceiling axis

The prism uses u=x+z for increasing roof height and v=x-z for transverse
width. This diagonal orientation keeps the canonical high alcoves near the
high front without relocating any mineral. Only the outside viewing edges
are beveled; low gorge widths and three-high clearance remain. The fixed
entry connector reaches the front above the nose. A has a continuous main
roof and one upper descent; B retains a ceiling-connected diagonal nose rib
with two gorges rejoining behind it; C adds a short front gallery and a
second supported upper landing/return.

Tests sample the rising roof at a constant transverse offset, verify both
two-wide gorge floors and every canonical alcove return, cut both near-tip and rear gorge
confluences with vertical bypasses isolated, and independently cut the two
upper returns. Shared generation, masks, budgets, DRY, persistence and
ownership-safe compensation tests extend. Coverage 81/120, 39 pending;
58,320 plans and 324 actual placements. Exact-source qualification required;
cap 2,500 unchanged. No install/merge/publication/Nether. Visual NOT_PERFORMED.

BP-32 predecessor d93870a29346698bb622d7773d8e30fcdb857f29: 16 successful jobs, 716 JUnit,
135+135 GameTests per stack plus streaming/restart; maximum
2494. JAR SHA256 23e1a225c3164bf001f15b111377fa2ada1e05d0952a0487c761992742f496e7.
These proofs do not qualify BP-30.


## BP-25 A/B/C: four vertices and separated cage edges

Three lower vertices and an upper vertex surround a retained central rock mass.
A provides all six edges. B omits BC from the air volume at construction; C
splits only the upper half of AD around a rock blade and rejoins at both ends.
Backed ladders and supported landings provide every ascent and return.

FINAL mineral coordinates override the catalogue reference pose. In this
normalization N6 is a BD retreat and N7 a CD retreat in all variants; neither
belongs to the omitted BC edge. No mineral coordinate, budget or reward changes.
The tests close all edges, restore each separately and require exactly its
endpoint pair to reconnect in both directions. C retains its AD route with
one arm cut and loses it with both cut. Shared mineral-mask, DRY, actual
placement, persistence and compensation tests remain.
84/120 variants, 36 pending; 60,480 composed plans and 336 actual placements.
Exact-source qualification required. The 2,500 cap remains unchanged;
no installation, merge, publication or Nether activation. Visual NOT_PERFORMED.

BP-30 predecessor e0d4cbbd5b6048f07dfd4b1068ef10b602b0ac92: 16 successful jobs, 716 JUnit,
137+137 GameTests per stack plus streaming/restart; maximum
2499. JAR SHA256 4a0029d01a6936ae344755724012c0114b6edb8bd759c1d49e6b32e2a5bb0e52.
These proofs do not qualify BP-25.


## BP-08 A/B/C: buttressed vaults and two independent openings

A tall cavity retains two asymmetric host-rock buttresses and supported wall ledges. A has a capped high fissure, B a dominant rooted mass, and C two unequal vaults around a continuous pillar. The twin vaults communicate through a low rear opening and a higher front window. Tests verify supports, roof differences and fixed alcove returns, then isolate the external access descents and cut each window independently to reject a third interior bypass. FINAL masks, nodes and rewards remain unchanged.

87/120 variants, 33 pending; 62,640 composed plans and 348 actual placements. Exact-source qualification required. The 2,500 cap remains unchanged; no installation, merge, publication or Nether activation. Visual NOT_PERFORMED.

BP-25 predecessor dfd349931ec16c82c52fd8b9c7b937775b16971a: 16 successful jobs, 716 JUnit, 139+139 GameTests per stack plus streaming/restart; maximum 2497. JAR SHA256 2a28f1fd9c5fe1826aaf383cd897ae1ce3b60008d786317633178f4cbe69d7d9. These proofs do not qualify BP-08.


### BP-08 cycle — BP-03 satellite composed-budget correction

e1e718e failed only the composed-cap test in the current minimal runtime:
2,502 cells, MINER_CAMP/MOTHERLODE, depth/variant710 (depth28, PEARLS_SATELLITE),
anchor(-21,90,57). The 87-way shape matrix exposes a different camp fixture
for the older BP-03 C form. Its extra middle-roof height now tapers to a smaller
rounded crown; all three walking/headroom cells, footprint, necks and satellite
remain. An assertion checks three-high clearance and the retained raised centre.
No cap, mineral mask, budget or route assertion is relaxed. This correction needs
fresh exact-source CI; 84 variants remain qualified and 87 implemented.


## BP-09 A/B/C: unequal rock islands, roofed branches and a connected depression

A retains three unequal host-rock masses and one low slab underpass. B uses a dominant inclined roof and two lower branches around its rooted mass. C replaces the first island with a connected depression whose oriented stairs permit a supported return. Tests verify the masses, alcove returns, independent branch cuts and the two-wide pit ascent/descent. FINAL positions, growth masks, budgets and independent reward draws remain unchanged.

90/120 variants, 30 pending; 64,800 composed plans and 360 actual placements. Exact-source qualification required. The 2,500 cap remains unchanged; no installation, merge, publication or Nether activation. Visual NOT_PERFORMED.

BP-08 predecessor d01bb261b9b40c0d4aacd5c1115b2ebbc6137043: 16 successful jobs, 716 JUnit, 141+141 GameTests per stack plus streaming/restart; maximum 2494. JAR SHA256 6cae4def8272220a2bad32e80b55df7a1ebe015c15ded44295b324de9698e20a. These proofs do not qualify BP-09.

BP-09's lower east elbow is two-wide outside N4's reserved mask: B recesses
only X8..10/Y3..5/Z8 beneath its retained east shoulder. Its root remains
continuous at Z6/7. Dedicated assertions cover the full elbow and rear return.
Unused lower south-east shoulders and excavation inside masses immediately
restored as solid are omitted; all canonical AIR/ORE reservations remain.
Static footprint estimates are not runtime or composed-plan qualification.


## Final catalogue implementation batch — historical pending stage

BP-09 predecessor 0d272dd81e8b873ff2d393a3a1aec27859b30af9 passed 16 jobs, 716 JUnit and 143+143 GameTests per stack, streaming/restart. Its JAR SHA256 is abafb5659f3c8ac592d25b2d3bb87ea14dc6d14b6860c1c779f3494bc5699b2b. Those results qualify only 90/120 variants.

The remaining ten families are integrated together below. No intermediate family is claimed qualified. Twenty dedicated tests extend the common matrix to **86,400 composed plans and 480 actual placements**, with **163 minimal and 163 complete-runtime GameTests expected per stack**. The final exact-source CI must independently qualify all 120 variants.

### BP-29 A/B/C: branched interleaved networks and explicit transfers

P serves N1/N3/N6, Q serves N4/N2/N5/N7. A has one middle transfer; B adds one lower transfer; C moves the only transfer high and turns a terminal P branch into an internal loop. Tests close every admitted transfer and require each complete network to remain internally connected but separated from the other, then restore each transfer independently. A separate cut set verifies the P terminal loop without creating a Q bypass. No per-network quality, reserve or reward draw is added.

### BP-07 A/B/C: separated helical strands and explicit transfers

Two supported windings pass around a retained pillar at separate heights. A has one middle transfer, B adds a high transfer, and C displaces the pillar through overlapping bands while an upper outer gallery goes around it. Tests cut every admitted transfer, require each complete strand to retain its assigned fixed alcoves, then restore each transfer independently. Projected crossings retain separating rock, the middle bridge and upper winding stay two-wide, and the displaced pillar stays continuous. No node, reserve or reward is added.

### BP-39 A/B/C: nine inclined grid intersections and four retained cores

Nine two-wide intersections rise diagonally around four retained rock cores. Short rises finish before the flat intersections; longer differences use backed descents. B offsets one north-west bay with two supported bends while retaining all four cores. C closes only one middle traverse and keeps the grid detour. Tests verify shared floors, stair direction, all fixed alcove returns and every grid edge independently: with all admitted edges cut, the nine intersections separate; restoring one edge reconnects only its endpoint pair. N2/N4 use supported rear retreats and N7 a side landing without moving a mineral mask.

### BP-22 A/B/C: three inclined radial sheets and an explicit hinge

Three widening sheets meet at a backed front hinge. The lower and upper sheets descend rearward at different heights while the middle sheet slopes transversely. B adds one far lower/middle window; C divides the middle sheet into two lobes whose only connection is their far inclined neck. Tests isolate the hinge, window and neck independently, verify supported slopes and preserve every canonical mineral retreat. No per-sheet rewards or budgets are introduced.

### BP-38 A/B/C: five separated spokes and an inclined hollow-hub wheel

Five spokes join a hollow hub to an inclined continuous rim. B omits only the east connection while retaining its blind rim-side stump; C splits the lower rim around a retained rock blade. Tests cut every spoke and rim edge, then restore each independently and require only its endpoint pair to reconnect. Fixed mineral retreats remain on supported parts of the same site.

### BP-28 A/B/C: one trilobed circuit and separated overpasses

One closed circuit traverses three unequal lobes at different heights; overlapping projections remain separated by rock. B adds one explicit middle transfer and C splits only the lower lobe around a retained blade. Tests check supported returns and separating shells, isolate every cycle edge, then restore each edge or split arc independently. No new node, budget or reward draw is added.

### BP-18 A/B/C: dry braided channels and two distinct confluences

Three supported upper channels reunite at a rear middle confluence, then three lower branches reunite at the front bottom. B captures the central channel into the west before the middle reunion; C joins two lower channels on a raised plateau before their final two-wide descent, while the third stays below. Tests cut each upper and lower branch independently and isolate the early capture and suspended final descent. No water, node, reward or extra quality draw is added.

### BP-12 A/B/C: continuous saddle surface with supported opposite curvatures

A continuous rotated saddle joins opposed crests through a lower col and retains two sloping depressions. B notches one crest; C adds one upper bridge over a lower passage. Tests check the supported discretized surface, fixed mineral retreats and independent upper/lower passage cuts.

### BP-33 A/B/C: crossed undulations with a following roof

Two crossing undulations retain a continuous traversable floor and a roof following its clearance. B adds a lower notch in one ridge; C divides the central depression with a low ridge whose two ends remain independently traversable. Tests check every supported surface cell, mineral returns and the isolated basin end routes.

### BP-01 A/B/C: folded flattened lens and inclined facing ledges

A flattened lenticular cavity contains an oblique retained fold and two non-flat facing ledges. A has two offset side passages; B retains overlapping blades around one under/over transverse link; C opens the upper fold around one staggered diagonal stone bridge. Tests cut every admitted connection independently and retain supported access to all fixed mineral fronts.

All FINAL positions, masks, reserves/ranks, independent lotteries, access functions, ownership-safe rollback and the 2,500-cell guard remain unchanged. Overworld TRUE, retrogen OFF, OreGuard independent, Nether production OFF. No installation, merge or publication. Visual/client NOT_PERFORMED.


### Final-batch first CI failures

Candidate `a198a3d` failed seven minimal GameTests on both dependency stacks. Explicit stair foundations are now included in the new slope plans. BP-22's two-wide N2 terminal approach moves to Z12/13, outside the canonical ore cell X10/Y8/Z11. BP-33's notch probe moves to X5/Y8/Z6: the old X4 probe was also open inside the shared shaft. Lens separation and native placement assertions remain strict and now report precise missing fronts, crossing cells and changed states. No topology assertion or production guard is removed.

Coverage limits: 480 actual placement/commit/dedup cases are DRY Certus (120 variants × four accesses). Productive qualities are covered by the 86,400 composed-plan matrix, not 480 placements per quality. Route assertions traverse block maps; they are not walking-player or visual exploration evidence. No rotation/mirror-normalized similarity corpus or numerical global uniqueness guarantee is claimed.


### Native slope and folded-lens corrections

The current-stack run at `69044c6` isolated five remaining failures. The A front passage had overwritten BP-01's north-facing return step; restoring those steps after composing the passage keeps both sides internally traversable when the two links are cut. BP-33's first one-cell crest could not support opposed stair directions. A two-cell crest and its matching shaft clearance preserve the continuous crossed undulations, B's lower notch and C's split depression. The fixed mineral masks and FINAL envelope are retained. Native placement changed a planned straight stair at X6/Y8/Z10 to OUTER_LEFT; the new slopes now resolve their planned corner states before placement. The exact native-state equality and compensation assertions remain unchanged. These corrections are unqualified until their own exact-source CI passes.


### Native-corner compensation regression

The current-stack `5a4d572` matrix completed all 86,400 composed plans with a 2,494-cell maximum. Its remaining failure was compensation: restoring a neighbour changed the SHAPE of a still-owned stair. Rollback now captures exact ownership before writes, excludes pre-existing foreign edits permanently, and journals only synchronous SHAPE-only changes it causes on previously matching owned stairs. Block identity, facing, half and waterlogging must remain exact. This journal survives retries; LIFO restoration remains. A focused native GameTest also protects a pre-existing SHAPE-only foreign edit and checks repeated rollback. The expected suite is now **164 minimal + 164 complete-runtime GameTests per stack**; final qualification still requires a fresh exact-source pass.

### Interrupted native-state journal correction

df5fb07 failed compilation in the new fixture (one extra position argument),
not runtime qualification. The corrected fixture also injects a throw after an
actual restoration write and a failed immediate neighbour read. A finally block
captures synchronous native changes even when the write throws. If attribution
cannot be observed, the journal persistently reports incomplete and protects that
cell from later reclamation; other owned cells still restore. Only observing its
original state resolves that uncertainty. A noncoincident foreign facing edit is
preserved across retry. Ownership capture failures defer writes. Exact hosted CI
is required; no failed-source evidence is transferred.

### Bounded spatial footprint of catalogue placement fixtures

506c66d baseline push exhausted its approximately 4 GiB Java heap during native
chunk saving, after the composed matrix completed at 2,494 cells. The 480 actual
placements formerly occupied a 61,440-block strip. They now use an isolated 24-column
grid with 64-block anchor spacing, explicitly exceeding the unchanged 48-block
production camp guard. All 480 distinct source chunks, placements, confirmations,
signatures and replay checks remain. Only test coordinates change; no production
chunk-loading policy, heap, dependency, security or gameplay setting changes.
Exact qualification must restart at the corrected source.
