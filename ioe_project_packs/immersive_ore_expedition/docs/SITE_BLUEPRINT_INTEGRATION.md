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

`CatalogueTerraces` composes BP-05 A (offset terraces) and C (split middle terrace
with an open void on either side of its supported bridge), geometry version 1.
The chamber occupies 15×19×15. Entry E=(7,15,1) and the three circulation levels
Y=3/9/15 have supported routes; the two interior ladders have solid backing.
The differing footprints and middle bridge change the actual solid/air map, not
only rotation, palette or seed. Only a one-block shell is written, not a 4,275-cell
bounding-box fill. Unused terrace corners are trimmed; reference node positions
and supported routes are retained. BP-05 B and the remaining 39 families are not implemented.

The existing natural `ExpeditionSiteBlueprints` path now selects A/C using one bit
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

Added integration tests cover both shapes, four access types, all qualities,
source-chunk containment including negative coordinates, both variants at all nine
approved depths, unchanged depth and
anchor bounds, complete-plan size, three-high supported routes to all alcoves,
all growth masks, canonical counts, DRY 0–5 residues plus cache, and real
`Feature.place` staging/commit/deduplication for both variants at all four accesses.
The existing GeOre/Certus runtime tests additionally exercise ranks, fallbacks,
Jade metadata, persistence, compensation and actual resource-profile admission.

No new server/profile, PC operation, Nether activation, merge or publication is
part of this work. The installed `382e0b4` provenance remains separate. Client
visual qualification remains NOT_PERFORMED. This slice is two of 120 described
variants, not completion of the four catalogues.
