# IOE Prospector Camp + Geological Outcrop

## Active identity and pipeline

This surface family reuses `immersive_ore_expedition:miner_camp`. It is a component of the existing connected
expedition-site planner, not a new Minecraft structure, placed feature, or second worldgen engine.

```text
configured/placed miner_camp feature
-> ExpeditionSiteFeature (one quality roll and one site seed)
-> ExpeditionSiteBlueprints
-> ProspectorCampOutcropComposer (surface blocks only)
-> IoePendingExpeditionSites
-> IoeExpeditionPlanPlacement (state + typed payload, compensated)
-> existing IE reservation commit
-> existing locator record
```

The surface composer receives the already selected quality. It never calls `BiomeMineResourceProfile`, never
selects an ore, and emits no ore, budding block, crystal, mineral node, IE multiblock, AE2 component, train block,
or machine. Productive sites continue to use the single existing biome profile and transactional IE deposit.
`MINER_CAMP` uses the authoritative `SiteQualityRoll.DEFAULT` so `DRY` is naturally reachable; the other existing
surface IDs preserve their productive-only weights. A dry camp resolves the same single biome profile needed by
staging/locator metadata, but creates no IE/IP reservation and remains nonproductive.

## Structure grammar

Every variant composes the same readable field-survey grammar:

1. a stepped host-rock outcrop;
2. a temporary shelter;
3. a hearth whose lit state follows the deterministic narrative state;
4. a modest work/storage edge;
5. survey markers and a route marker;
6. a worn path and a protected shaft hatch;
7. one to three biome-local vegetation accents (zero in the volcanic family);
8. a compact observation platform only for `RICH` and `MOTHERLODE`.

The design stays inside the anchor chunk because the existing pending-plan signature rejects cross-chunk writes.
The Motherlode design retains the requested 17x17 target but uses a 15x15 placed envelope. This is the largest odd
footprint that can be centered safely inside one 16x16 chunk without redesigning the transaction model.

Before planning, the feature samples the complete placed envelope, rejects fluid-covered ground, non-replaceable
clearance, and elevation ranges above one block. Sparse ground cells and every table, container, sample, marker,
observation-deck cell, and non-shaft hatch-ring cell receive a local foundation layer; the two connector cells remain
reserved for the existing air/ladder path.

During composition, the 3x3 hatch is a reserved column set rather than a last-writer repair. The complete host-rock
footprint selects the first collision-free local quarter-turn, so even the smallest `DRY` variant retains its outcrop.
The shelter begins at the outer envelope edge; wetland and tropical shelters add a one-block raised deck over a
complete local foundation, while the other families remain ground-set. The shelter and observation platform use the
same deterministic quarter-turn rule. The complete shaft-to-center path is reserved before structural components.
Field-work roles scan deterministic inner-ring candidates, and every container either faces that path or reserves a
separate open access column. Markers, vegetation, and the route sign then scan remaining candidates. Placement fails
instead of allowing the path, center, outcrop, hatch, or a later semantic component to overwrite another reserved
camp detail. The plan writes two vertical `AIR` states at every reserved path/access column, clears functional-block
headroom, clears every remaining usable shelter cell around the intentional biome windbreaks/anchors, and preserves a
two-block exit volume above the shaft hatch. The compact DRY raised shelter deliberately omits one front-beam and
roof cell as an incomplete open bay, preserving two explicit standing-air blocks without exceeding its four-block
height target. Replaceable
plants accepted by the terrain gate therefore cannot remain in the circulation route or inside productive shelters.

Exact target/placed envelopes and the complete 5x8 static audit are in
[`STRUCTURE_DIMENSION_REPORT.csv`](STRUCTURE_DIMENSION_REPORT.csv). Its block, non-air, circulation-air, container,
block-entity, biome-geometry, nominal shelter-shell/share, and relative-extent counts are tied to audit seed
`0x51A7E5EED` and a
static mirror of the composer; the unit-test contract recomputes the vanilla rows from the Java composer when hosted
CI executes. These are static plan measurements, not observed world-write or render results.

## Quality contract

| Quality | Target | Placed | Height | Containers | Domum material BEs | Narrative states |
|---|---:|---:|---:|---:|---:|---|
| DRY | 9x9 | 9x9 | 4 | 0 | 0 | abandoned or collapsed |
| POOR | 11x11 | 11x11 | 5 | 1 | 7 | weathered or abandoned |
| NORMAL | 13x13 | 13x13 | 6 | 2 | 12 | weathered or recently active |
| RICH | 15x15 | 15x15 | 7 | 2 | 18 | recently active or weathered |
| MOTHERLODE | 17x17 max | 15x15 | 8 | 2 | 24 | recently active or weathered |

Quality changes geometry, preparedness, marker count, containers, loot tier, and the Domum accent budget. It does not
change mineral composition. [`BIOME_QUALITY_MATRIX.csv`](BIOME_QUALITY_MATRIX.csv) records both allowed narrative
states for every quality/family pair with a deterministic coverage seed (5 x 8 x 2 rows).

## Visual biome families

Classification priority is fixed:

```text
aquatic/shoreline skip -> snowy -> volcanic -> arid -> wetland -> tropical -> rocky -> conifer -> temperate
```

| Family | Structural palette | Host-rock palette | Vegetation/geometry |
|---|---|---|---|
| TEMPERATE | oak, pale wool, coarse dirt/path | stone, andesite | 2 short-grass accents, ground-set shelter |
| CONIFER | spruce, gray wool, podzol | cobble, mossy cobble | 2 fern accents, two-sided windbreak, sheltered log reserve |
| SNOWY | spruce, low gray roof, gravel | stone, cobble | 2 persistent low spruce-leaf shrubs, rear windbreak, covered log cache |
| WETLAND | mangrove, brown wool, packed mud walk | mud brick, mossy cobble | 2 moss-carpet accents, raised shelter deck |
| TROPICAL | jungle wood, warm roof, rooted dirt | mossy cobble, tuff | 3 fern accents, raised deck, open jungle-fence lattice |
| ARID | acacia, pale roof, sandstone ground | sandstone, red sandstone | 1 dead-bush accent, upper pale-wool shade valance |
| ROCKY | sparse spruce/dark wood, gray roof, gravel | andesite, tuff | 1 moss-carpet accent, paired two-block rock anchors |
| VOLCANIC | dark oak, black roof, gravel | basalt, polished blackstone | no vegetation, compact stone screen, gear plinth, unlit fire |

Volcanic membership reuses the existing audited `ip_lava_volcano` biome tag through an IOE visual tag. Aquatic,
ocean, river, and beach tags skip cleanly; no shoreline heuristic is invented. Visual classification is independent
from mineral-profile resolution.

## Determinism

The feature performs one quality roll (`SiteQualityRoll.DEFAULT` for `MINER_CAMP`) and draws one plan seed.
`ProspectorCampContext` derives the orientation, biome-salted allowed narrative state, roof wear, layout decoration,
and container loot seeds with a stable `mix64` transform. Component-local collision avoidance follows fixed
quarter-turn/ring/perimeter order and consumes no random draw. The initial plan and every deposit-quality fallback
reuse the same context and seed. No palette or composer owns a `RandomSource`, and Domum availability cannot reroll
quality or mineral selection.

## Domum dependency policy

Domum Ornamentum is optional:

- pinned API: `com.ldtteam:domum-ornamentum:1.0.231:api` (`compileOnly`);
- opt-in Gradle runtime: classifier `main`, gated by `ioeIncludeDomumRuntime`;
- full-runtime CI lock: `domum-ornamentum-1.0.231-main.jar`, SHA-512 pinned;
- NeoForge metadata: optional `[1.0.231,1.0.232)`;
- absence path: a separately composed vanilla palette with no Domum block IDs or material payloads;
- presence path: registry lookup for the audited `post` and `framed` blocks plus typed material API placement.

The guarded compat surface exposes only Minecraft types. Domum classes occur only in the isolated adapter, invoked
after `ModList` confirms `domum_ornamentum`. Details and remaining runtime gaps are recorded in
[`DOMUM_BLOCK_AND_NBT_INVENTORY.md`](DOMUM_BLOCK_AND_NBT_INVENTORY.md).

## Loot and progression

Four quality-routed tables (`prospector_camp_supplies`, `_normal`, `_rich`, and `_motherlode`) contain only modest
vanilla field supplies: torches, charcoal, bread, paper, string, feathers, and a wooden pickaxe. Every pickaxe uses
`minecraft:set_damage` with only 8-30% durability remaining. `DRY` has no container; `POOR` has one low-roll table;
the higher tiers retain two containers while increasing only utility rolls/counts. The tables contain no ore, raw
material, ingot, nugget, gem, redstone, industrial component, machine, blueprint, Domum block, Digger, or Excavator.

## Validation boundary

`scripts/validate_prospector_camp.py` checks the quality/family/state matrix, dependency contract and checksums,
optional classloading guard, canonical insertion count, typed payload route, biome tag, tiered loot allowlist and
tool damage, absence of surface ore, vertical circulation clearance, 160 legal anchor/rotation/quality layouts, the
source-level DRY `Feature.place` → pending confirmation → locator contract, minimum spacing, and documentation
reports. It is invoked by the existing worldgen validator used in hosted CI. The surface gate rejects fluid-covered
ground, powder snow, and lava within a two-block horizontal margin; it also enforces slope, natural ground,
generated-structure absence across the footprint, replaceability, height, and the single-chunk envelope.

Natural expedition sites use `naturalExpeditionSiteGenerationEnabled`. The older `runtimePlacementEnabled` flag is
a legacy proof/planning hook only and does not disable the production `ExpeditionSiteFeature` path. Before any camp
blocks or reservations are committed, the final server-thread transaction rejects a miner camp when an existing
playable IOE anchor in the same dimension is less than 48 horizontal blocks away. The lookup reads the existing
locator `SavedData` only and loads no neighboring chunk. The surface preflight separately rejects registered generated
structure pieces and artificial/non-natural ground inside the complete footprint. The placed feature remains at
rarity 1/128; spacing is therefore a hard post-candidate invariant rather than a probability claim.

Local Gradle, GameTest, Minecraft, Prism, save/load, and visual smoke tests are prohibited by the repository policy.
Hosted GameTest source now applies all four Domum rotations through the compensated production placement, reads every
material block entity back, performs an NBT serialization round trip, checks recovered-item material data, and tests
the 48-block rejection transaction. Execution, client rendering/missing-texture inspection, an actual world
save/reload, exact runtime block counts, biome placement, and observed coexistence remain `NOT_PERFORMED` until
GitHub-hosted CI runs the exact change.

Known limits: the 48-block spacing query scans the in-memory locator site set linearly for each accepted camp
candidate, although it loads no neighboring chunks and the placed feature is gated at rarity 1/128. Domum accents
also retain their upstream recoverable-item behavior; the static contract caps them at 7-24 blocks per productive
camp and excludes them from chest loot, but the survival-economy impact of salvaging those blocks is `UNVERIFIED`.

```text
HOSTED_CI: NOT_PERFORMED
RUNTIME_GAMETEST: NOT_PERFORMED
CLIENT_SMOKE: NOT_PERFORMED
SERVER_SMOKE: NOT_PERFORMED
WORLD_VISUAL_REVIEW: NOT_PERFORMED
SCREENSHOTS: NOT_PERFORMED
VISUAL_RUNTIME_APPROVAL: NOT_PERFORMED
```
