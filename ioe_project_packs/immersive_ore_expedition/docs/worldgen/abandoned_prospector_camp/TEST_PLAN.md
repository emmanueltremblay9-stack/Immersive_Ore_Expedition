# Future Compiled and Runtime Test Plan

These checks are intentionally deferred by the Surface Structure 02 static-only authorization. This document is
a future execution plan, not evidence that any listed check ran.

## Java unit tests

1. Exhaust deterministic archetype thresholds for all five qualities and boundary percentiles.
2. Prove the same plan seed, quality, and chunk produce the same archetype with Domum present or absent.
3. Exhaust abandoned-state eligibility and uniform index selection for each quality.
4. Compose all eligible quality/family/state/rotation cases with vanilla and Domum contexts.
5. Assert Surface 01 ACTIVE compositions remain byte-for-byte/map-entry identical for fixed baseline seeds.
6. Assert quality envelopes, one-chunk containment, no active fire/lava, no surface ore/crystal/Budding block,
   container maxima, two-block route/entrance clearance, and 3×3 hatch headroom.
7. Assert every payload targets a block entity and Domum material payloads survive rotation.

## NeoForge GameTests

1. Place every abandoned state in representative terrain for all eight non-aquatic families.
2. Confirm access from perimeter to shelter, work/geology zone, hatch, and exit.
3. Confirm no unsupported falling block enters an access cell after neighbor updates.
4. Apply loot payloads and sample enough deterministic seeds to verify table eligibility and hard item maxima.
5. Place with and without Domum; compare archetype/state, coordinates, silhouette, routes, and container positions.
6. Force pending placement success/failure and confirm reservation commit, rollback, fallback, and locator behavior.
7. Save/reload and verify block entities, loot seeds, material payloads, and locator records.

## Hosted CI and artifact checks

1. Run the project’s authorized hosted compile/test jobs on a branch that contains the exact change.
2. Verify the runtime JAR contains compiled abandoned-camp classes, five loot tables, and
   `META-INF/neoforge.mods.toml`.
3. Run the existing optional-Domum full-runtime job and a Domum-free job.
4. Start a dedicated server with the built artifact and inspect startup/registry logs.

## Manual visual QA

1. Capture all five states in each eligible quality and all eight visual families.
2. Check coherent damage silhouettes, state readability, palette fit, and no random-debris appearance.
3. Walk every approach, shelter entrance, hatch route, and exit in first-person.
4. Verify DRY/FAILED camps never imply exposed ore or productive surface resources.

Status for every item above: `NOT_PERFORMED`.
