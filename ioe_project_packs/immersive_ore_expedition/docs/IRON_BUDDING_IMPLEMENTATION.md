# Iron Budding implementation scope

This implementation targets the pinned Minecraft 1.21.1, NeoForge 21.1.230, AE2 19.2.17,
GeOre 6.2.2 and AE2 Crystal Science 1.1.12 runtime. It is not a 1.0.0 release approval.

## Behavior

- Four IOE Iron blocks/items register when AE2 and GeOre are loaded. Growth resolves the
  real GeOre Iron buds through the registry and stops if required properties are absent.
- The AE2 19.2.17 JAR's `BuddingCertusQuartzBlock` was inspected directly: 1/5 growth gate,
  uniform six-face selection, air/source-water start, matching-facing bud progression,
  preserved waterlogging, then 1/12 degradation after growth. Flawless skips degradation.
  A differential GameTest compares actual loaded AE2 and Iron blocks across all ranks,
  faces, growth stages, obstructions, facing mismatches, water and random gate outcomes.
- Exhausted Damaged becomes `minecraft:iron_block`. Silk Touch retains non-Flawless ranks;
  otherwise they drop the next lower rank/storage. Flawless always drops Flawed.
- AE2 transform recipes restore Damaged to Chipped and Chipped to Flawed with charged
  Certus. Acquiring Damaged uses the canonical neutral-seed Aggregator recipe (16000 energy).
  There is no Iron Flawless recipe; the AE2CS Certus Flawless recipe is disabled.
- The `c:budding_blocks` tag exposes Iron to AE2's existing acceleration tag.
- Natural quality selection uses 10/25/45/17/3 for every site type. Productive sites with
  an explicit Iron profile resolve the canonical planner into actual Iron blocks.
  The Motherlode draw happens once before deposit resolution. Lower plans derive from
  that plan without another lottery. Existing staging, compensation and locator sequencing
  are retained; GameTests cover placement and a full failed IE commit/fallback chain.
- Iron nodes receive equal connected ore budgets and one open growth face. Nodes compose
  after galleries so later structural work cannot erase them. Poor Iron chambers need
  a two-block vertical half-height to fit three complete nodes.

## Remaining work

The 10% DRY neutral-seed reward is now planned once and placed in an underground cache
through the same compensated transaction. Winning loot yields exactly one neutral seed;
reopening/reconfirmation does not reroll it. Residual ore placement now uses the approved uniform 0–5 whole-pocket count for GeOre
profiles. All thirteen GeOre families share the implementation with ordinary storage
outputs; the five IE families are conditional. See `BUDDING_FAMILY_IMPLEMENTATION.md`
for the exact activation rules and remaining special-profile limits.

Models retain the installed GeOre Iron texture and add original, cumulative fracture
geometry to distinguish all four ranks without copying third-party images. Static
resource validation is automated; actual client visual acceptance remains outstanding.
See `PR63_REGRESSION_VALIDATION.md` for the specification and proof boundaries. Optional Jade 15.10.6 now displays the live rank and
committed site/node metadata. Full client progression testing and release smoke evidence remain outstanding.
