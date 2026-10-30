# Surface 02 — abandoned camp and gallery

Status: `PLANNING_ONLY` / `DEFAULT_OFF` in 0.2.53-alpha.

Surface 02 is an independent deterministic composer. It does not call Surface 01,
`ExpeditionSiteBlueprints`, active feature registration, placement, pending transactions, the
locator, or compensation. The passive adapter can produce the existing `ExpeditionSiteBlockPlan`
type after a future caller supplies exact registered block states.

## Canonical decision contract

| Final quality | Slots | Active Budding rank | Mineral blocks, excluding hearts |
| --- | --- | --- | ---: |
| `DRY` | four exhausted | none | 0 |
| `POOR` | `SLOT_1`–`SLOT_3` active; `SLOT_4` exhausted | `DAMAGED` | 12 (4 per active slot) |
| `NORMAL` | four active | `CHIPPED` | 20 (5 per active slot) |
| `RICH` | ineligible | — | — |
| `MOTHERLODE` | ineligible | — | — |

The 0 positive residual blocks for `DRY` are intentional. No canonical positive distribution exists
in 0.2.53, so random `0–5` residual placement remains an unresolved game-design gap.

Productive GeOre and Certus inputs must come through the compiled Budding foundation. Certus uses
the native `ae2:damaged_budding_quartz` and `ae2:chipped_budding_quartz` ranks with
`ae2:quartz_block` as the mineral shell. ExtendedAE remains rank-unsupported with no fallback.
`DRY` may still produce sterile geometry for an ExtendedAE profile, but the result carries no
resolved resource and makes no rank-support claim.

## Four independent layers

1. biome facade;
2. descending access;
3. functional four-slot core;
4. narrative decoration.

The core exclusively owns slots, Budding hearts, mineral positions, mandatory paths, the stabilized
collapse, and protected growth envelopes. Other layers cannot overwrite core cells. No loot,
container, active fire, lava, gravity material, entity, reward, or extra mineral decoration is
planned.

## Bounded geometry decision

The design reference names an approximate `23 x 27` footprint. The existing 0.2.53 pending-site
transaction rejects every placement outside the anchor chunk, so a literal footprint cannot pass
through the existing pipeline. Surface 02 therefore uses a bounded `15 x 15` single-anchor-chunk
compatibility envelope at depth 8, while reserving the full allowed vertical range of 8–15 blocks.
This preserves four branches and the existing transaction instead of introducing a forbidden
multi-chunk placement pipeline.

The caller must position the origin so `x = origin - 7 .. origin + 7` and
`z = origin .. origin + 14` are in the same anchor chunk. A non-fitting envelope, unstable surface,
blocked entrance, aquatic family, or shoreline family returns `INVALID_TERRAIN`.

Every active slot has:

- a fixed typed node position;
- an approach reachable through a deterministic bypass;
- two clear blocks on the mandatory path;
- six adjacent protected air cells;
- four or five owned mineral-ring positions outside those six cells.

## Biome mapping

| Observed input | Canonical presentation |
| --- | --- |
| `TEMPERATE`, `CONIFER` | temperate |
| `ARID` | arid |
| `COLD` | cold |
| `WET`, `TROPICAL` | wet |
| `VOLCANIC`, `ROCKY`, `MOUNTAIN` | relief |
| `AQUATIC`, `SHORELINE` | refused |

Only facade palette and narrative details vary. Quality, slots, ranks, mineral budgets, resource
support, and the functional core do not vary by biome.

## Determinism salts

All variation is derived directly from the single `planSeed`; there is no mutable RNG.

- `FACADE_SALT = 0x5355524630324641`: facade asymmetry only;
- `AGING_SALT = 0x5355524630324147`: aging decoration side only;
- `NARRATIVE_SALT = 0x5355524630324E41`: non-functional debris side only.

No salt selects quality, resource, archetype, active slot, Budding rank, or mineral budget.

## Activation and proof boundary

No configured feature, placed feature, biome modifier, selection weight, rarity filter, natural
dispatch, or loot table is added. Minecraft, Prism, client, server, GameTest runtime, visual runtime,
and hosted CI execution are `NOT_PERFORMED` in this implementation pass.

Plain JUnit exercises the exact pre-resolved resource contract and the real ExtendedAE refusal. A
successful live `BuddingRankResolver.resolve` for GeOre/Certus requires the FML mod list and populated
registries, so that productive runtime boundary is `NOT_PERFORMED`; the source-linked validator
proves that the production entry point delegates to the real foundation resolver. The passive
`ExpeditionSiteBlockPlan` adapter compiles and its pure state-safety guard is unit-tested, but actual
registry-backed adaptation and pending-transaction staging are likewise `NOT_PERFORMED`.
