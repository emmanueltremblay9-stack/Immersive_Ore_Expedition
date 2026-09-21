# Surface Structure 02 — Abandoned Prospector Camp

This static content pass adds `ABANDONED` as a second structural archetype inside the existing
`MINER_CAMP` pipeline. `ACTIVE` continues to call `ProspectorCampOutcropComposer` unchanged. Both
archetypes retain the same quality roll, plan seed, biome family, terrain preflight, underground plan,
resource profile, deposit reservation, pending transaction, rollback, confirmation, spacing, and locator path.

## Architecture reconciliation

Surface Structure 01 already had `ProspectorCampContext.ProspectorCampState`. Its values describe legacy
wear inside the active composer, so they remain unchanged for seeded Surface 01 behavior. The independent
structure decision is `ProspectorCampArchetype`, while `AbandonedProspectorCampState` owns only the five
Surface Structure 02 narrative conditions. The abandoned composer starts from the established semantic layout,
normalizes the old per-cell roof wear, forces a cold/no-lava state, and applies one coherent state overlay.

The archetype is selected once after the final pre-stage quality is known:

| Quality | Abandoned | Active |
|---|---:|---:|
| DRY | 70% | 30% |
| POOR | 45% | 55% |
| NORMAL | 25% | 75% |
| RICH | 15% | 85% |
| MOTHERLODE | 10% | 90% |

The decision is `mix64(planSeed ^ ARCHETYPE_SELECTION_SALT) mod 100`. It consumes no `RandomSource` and
does not consult Domum availability. A separate `ABANDONED_STATE_SELECTION_SALT` selects uniformly among
the quality-eligible abandoned states.

## Narrative grammar

Every eligible plan preserves an approach path, shelter footprint and entrance, work/survey zone, ordinary
host-rock observation trace, shaft/hatch route, optional storage point, and clear exit.

- `RECENTLY_ABANDONED`: intact shelter rhythm, cold hearth, abrupt-departure timber cue.
- `WEATHERED`: one contiguous roof-edge gap, shortened adjacent support, bounded biome decay accents.
- `COLLAPSED`: one deterministic failed quadrant, shortened support, low off-route timber/canvas debris.
- `FAILED_PROSPECTION`: DRY/POOR only; ordinary host-rock examination, half-roof remnant, no container.
- `EVACUATED`: NORMAL+ only; stripped work point, open shelter bay, depleted or absent storage.

All eight non-aquatic visual families reuse the established Surface 01 palette. Aquatic and shoreline biomes
remain an explicit placement skip. Wetland and tropical entrances retain their raised deck; arid overlays do
not place falling blocks; volcanic overlays contain neither fire nor lava.

## Static evidence

- `ABANDONED_STATE_MATRIX.csv` covers all 200 quality/family/state eligibility combinations.
- `STRUCTURE_DIMENSION_REPORT.csv` covers all 576 eligible quality/family/state/rotation combinations.
- `scripts/validate_abandoned_prospector_camp.py` also evaluates all eight legal edge-anchor representatives
  in both Domum modes, producing 9,216 abstract chunk/access cases with applied collapsed-route detours.

CSV counts are explicitly marked `STATIC_MODEL_UPPER_BOUND`. They are conservative bounds derived from the
validated Surface 01 static composer report, live Java quality dimensions, role-claim reconstruction, maximum
roof normalization, and state-specific detour additions. Cross-chunk/access/hatch fields are calculated from
all 9,216 modeled cases. The counts are not JVM, GameTest, world-save, or visual-runtime measurements.

## Deferred proof

Compilation, Java tests, GameTests, dedicated-server startup, Minecraft/Prism launch, save/reload, screenshots,
and hosted CI are intentionally `NOT_PERFORMED` in this prompt-authorized static pass. See `TEST_PLAN.md` for
the future validation matrix.
