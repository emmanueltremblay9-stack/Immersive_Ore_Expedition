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

## Implemented bounded slice

`CatalogueTerraces` composes explicit voxel geometry for BP-05 A (offset terraces)
and C (split middle terrace with bridge), in the proposed 15×19×15 envelope.
The three circulation levels are Y=3/9/15; two offset, backed ladder routes connect
them. Differing footprints and the middle bridge change the actual solid/air map,
not only rotation, palette or a seed. Only a one-block shell is written; no whole
bounding-box fill. This is a reusable geometry composer, **not yet selected by
natural generation**. BP-05 B and the remaining 39 families are not implemented.

Seven reference-A node masks are composed separately from the geometry. C retains
them only because its alcoves remain on its two balconies. Productive compositions
preserve 3/4/5/7 hearts and 12/20/30/49 peripheral blocks. The composer has no random
source, quality roll, reserve roll, DRY seed roll or Flawless roll. Unused alcoves
remain ordinary stone. No resource family or reward is added.

Three hosted GameTests cover supported three-block-high player routes to every
alcove, backed ladders, bounds, stable output, exact masks/budgets, the A/C split,
a concrete connector conflict, and placement with idempotent compensation. These
are not visual acceptance. Three-block clearance over the entire composed access,
all terrain rejection paths and integration with natural placement remain gates.

## Concrete integration conflict; no merge prerequisite

The existing source-chunk anchor may be `(4, surfaceY, 9)`. Its ladder column is
`(4, y, 10)` with backing `(4, y, 11)` (`addConnector` and
`restoreConnectorLadder` in `ExpeditionSiteBlueprints`). At the upper terrace,
BP-05 A node N3 `(3,15,11)` reserves its east neighbor `(4,15,11)` as air.
Thus direct composition overwrites either the ladder backing or the growth mask.
The conflict is reproduced in a dedicated GameTest. Simply applying the catalogue
last is unsafe. Current canonical node placement also assumes an ellipsoidal
candidate band and cannot place these masks faithfully.

The next implementation must route the existing shaft to E=(7,15,1) without
crossing reserved masks, or explicitly remap/revalidate the affected alcove.
This is an engineering integration gate, not a request to merge PR64. The same
branch remains usable. The existing 2,500-cell complete-plan regression limit
must be measured after adding the surface and galleries: a naive full-box fill
already has 4,275 cells before those components. No limit was raised; sparse
geometry alone is not proof that the full plan fits. DRY floor residues and the
floor cache also require composition against the new floors, not old ellipsoid
coordinates. None of these checks may reroll the site's economic outcome.

No natural selector, default, frequency, resource budget, Nether code or existing
placement transaction was changed by this slice. No new world/server/profile,
PC action, merge or publication. Existing installed-runtime and compatibility
receipts remain attributed to their original source; the new CI source and results
are recorded separately in PR64. Client visual qualification remains NOT_PERFORMED.
