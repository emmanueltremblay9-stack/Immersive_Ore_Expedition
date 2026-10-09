# Approved family storage and DRY pocket integration

Development following `cf8a7ee24741d59f01c6fbbd828590b395ee5637` implements the user's
2026-10-09 choices: ordinary Minecraft/IE exhausted storage and an independent uniform
integer 0–5 for the entire DRY pocket. It does not certify a stable 1.0.0 client release.

## Family implementation and optional dependencies

`GeOreBuddingBlock` shares the previously verified Iron growth implementation across
13 canonical GeOre families. Existing Iron registry IDs/classes remain compatible.
`IoeGeOreBuddingBlocks` pre-registers four distinct ranks when their dependency mods
are loaded. Generation and growth additionally require the exact registered storage,
pocket block and all four growth stages with FACING/WATERLOGGED properties. An unavailable
family is rejected with a reason, never substituted with another mineral or GeOre storage.

| Families | Exhausted block | Required mods for rank registration |
| --- | --- | --- |
| coal, copper, diamond, emerald, gold, iron, lapis, redstone | `minecraft:<material>_block` | AE2 + GeOre |
| aluminum, lead, nickel, silver, uranium | `immersiveengineering:storage_<material>` | AE2 + GeOre + IE |

IE is not added as a global required dependency. Without it, the other eight rank families,
growth, restoration and crafting remain available. Existing rules requiring IE deposits
for productive natural expedition sites are unchanged; optional rank availability does
not remove that separate worldgen rule. DRY GeOre pockets do not require IE deposits.

The four ranks use the same vanilla/GeOre texture references and cumulative wear geometry
validated for Iron. Resources include 52 blockstates/models/items/loot tables, two charged-
Certus restoration recipes per family and one 16000-energy neutral-seed Aggregator recipe
per family. Recipe/tag guards tolerate absent optional families. Restoration stops at
Flawed; Flawless remains uncraftable and drops Flawed. Loot and exhausted storage agree.

`GeOreBuddingSitePlans` resolves the existing canonical 3/4/5/7 hearts and 12/20/30/49 ore
budgets for every available GeOre profile. One Motherlode Flawless draw is shared across
fallback planning, and lower tiers remove it. Each persisted node retains its actual
family identity; Jade validates that against the block currently present.

## DRY quantity, material and placement

`DryPocketRoll` draws `nextInt(6)` from an independent stream derived from the site seed.
It does not consume the seed-reward stream or change quality weights. One draw describes
the whole pocket, not each block/node. `DrySitePockets` uses the profile's existing
`geore:<material>_block` material (as used in productive GeOre nodes), deliberately distinct
from the approved ordinary exhausted-storage output. It does not add vanilla ore veins.

Residues replace up to five accessible chamber-floor positions, excluding the central
marker/reward cache and solid supports. Existing room centers and routes remain open.
Position order is deterministic and independent of count/reward RNG. Missing materials or
insufficient valid positions are not replaced with unrelated resources. The same placement
journal compensates residues and the optional seed chest together. Reconfirmation cannot
repeat an accepted placement. DRY metadata reports zero hearts/nodes and the real ore count.

The quantity policy is approved for DRY generally. The two special profiles Certus and
Entroized Fluix currently define crystal item outputs and special geode shells, not a
residual ore-block mapping. Their new residual placement remains pending an explicit
answer: 0–5 `ae2:quartz_block` for Certus, and an exception with no physical residues
for Entroized Fluix. Both proposals remain unapproved. Ordinary AE2 Fluix
(`ae2:fluix_block`) is not Entroized Fluix and is not an approved Entro residue.
Their prior structure-only DRY path and independent 10% seed reward remain unchanged.
Certus productive nodes are not yet wired into the runtime expedition planner.
Entro remains exclusive to IE extraction; no Entro Budding family is authorized.
See [Certus integration blockers](CERTUS_INTEGRATION_BLOCKERS.md) for the separate
productive-node material and meteorite replacement decisions.

## Files and validation

Core files: `budding/BuddingResourceFamily`, `GeOreBuddingBlock`, `IoeGeOreBuddingBlocks`,
`DryPocketRoll`; compatibility facades retain `IronBuddingBlock`/`IoeIronBuddingBlocks`.
Worldgen: `GeOreBuddingSitePlans`, `DrySitePockets`, `ExpeditionSiteFeature`,
`ExpeditionSiteBlueprints`, `ExpeditionSiteBlockPlan`, `BuddingPlanMetadata`.
Jade providers/runtime checks accept all GeOre families. Resources and translations follow
`<rank>_budding_<material>`; `scripts/validate_budding_models.py` checks their complete matrix.

Implementation order: approve mappings; check installed resources; share the growth/rank
implementation; extend guarded assets/recipes; wire profile selection and DRY placement;
verify real runtime behavior, compensation and persistence; run exact-head CI.

New tests cover dependency absence (real baseline plus injected missing resources),
864 native-AE2 differential growth cases per available family, actual restoration input
consumption, loot, no Flawless recipes, 64 geometry seeds per quality/family, actual family
profile placement and confirmation, uniform count limits, repeated planning, exact DRY
residue counts, reward independence and combined rollback without dropped objects.
Jade's runtime proof performs the real locator file save/reload for every available family.

Static model/recipe validation is not a client render. The cloud still lacks an available
graphical display/Xvfb. Client visual acceptance, full player progression and a complete
save/exit/reopen playthrough need the exact candidate and pinned pack on an OpenGL client.
No security settings, personal-PC install, merge or release are part of this checkpoint.
