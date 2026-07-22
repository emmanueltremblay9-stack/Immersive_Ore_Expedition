# Biome -> mineral deposit distribution

Generated deterministically by `scripts/generate_biome_mineral_distribution.py`.

## Generated deliverables

- Complete registered-biome inventory: `registered_biomes.csv`.
- Active-biome inventory: `active_biomes.csv`.
- Separate RU removed inventory: `ru_removed_biomes.csv`.
- Active biome -> exclusive outcome/profile matrix: `biome_profile_matrix.csv`.
- Aquatic biome -> preserved IE deposit matrix: `aquatic_deposit_matrix.csv`.
- Deposit composition, semantic principal/secondary/trace/host-rock roles, exact chances, weight, fail chance, tier capacities and active profile counts: `mineral_mix_matrix.csv`.
- Machine-readable counts, rare allowlists, role maps and tier capacities: `biome_distribution_manifest.json`.
- Candidate multi-seed rarity model: `multi_seed_candidate_model.json` and `MULTI_SEED_CANDIDATE_MODEL.md`.
- Confirmed runtime multi-seed evidence after hosted GameTests: `build/reports/ioe/multi-seed-biome-frequency.json`.

## Inventory

- Registered biomes: 266
- Active biomes: 252
- Regions Unexplored: 78 registered, 14 removed, 64 active
- Oh The Biomes We've Gone: 55 active
- Specialized IOE profiles: 140 biomes
- Aquatic-only IE deposits: 30 biomes
- Generic IE outcome: 82 biomes

## Runtime model

- Each specialized biome owns exactly one `immersiveengineering:mineral_mix` recipe.
- `mineral_mix_matrix.csv` explicitly classifies every output as principal, secondary, trace or host rock. These are geological semantic roles, not a sort by chance; rare target resources can remain principal while host rock occupies most output rolls.
- Mother, Major, Minor and Direct share the same composition; only radius and capacity change.
- Aquatic biomes allow exactly one preserved native IE aquatic mix under an exact overridden biome predicate.
- Alluvial Sift preserves its official 20% diamond output and reports it as a secondary resource, never as the biome's principal IOE profile.
- Unassigned active biomes retain generic Immersive Engineering recipe selection.
- The final design permits validated GeOre and Certus Budding nodes in expedition structures; implementation and runtime acceptance are tracked separately in `docs/BUDDING_FINAL_DECISIONS.md`.
- Immersive Petroleum oil, lava and aquifer tags are disjoint and evaluated independently.

## Reserve capacities

Radius is measured in blocks and capacity is the configured initial Excavator reserve.

| Profile | Mother radius/capacity | Major radius/capacity | Minor radius/capacity | Direct radius/capacity |
| --- | ---: | ---: | ---: | ---: |
| diamond | 36 / 4096 | 24 / 2048 | 16 / 1024 | 10 / 512 |
| emerald | 36 / 4096 | 24 / 2048 | 16 / 1024 | 10 / 512 |
| certus | 36 / 4096 | 24 / 2048 | 16 / 1024 | 10 / 512 |
| entroized_fluix | 28 / 2048 | 20 / 1024 | 14 / 512 | 8 / 256 |
| redstone | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| gold | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| uranium | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| silver | 44 / 12288 | 30 / 6144 | 18 / 3072 | 10 / 1536 |
| lapis | 44 / 12288 | 30 / 6144 | 18 / 3072 | 10 / 1536 |
| copper | 44 / 12288 | 30 / 6144 | 18 / 3072 | 10 / 1536 |
| aluminum | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| coal | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| nickel | 44 / 12288 | 30 / 6144 | 18 / 3072 | 10 / 1536 |
| lead | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |
| iron | 48 / 16384 | 32 / 8192 | 20 / 4096 | 12 / 2048 |

## Curated priority exceptions

- `regions_unexplored:towering_cliffs` is Lead despite its conifer tags.
- `biomeswevegone:howling_peaks` is Lead despite its snowy/conifer tags.
- `biomeswevegone:basalt_barrera` is Silt because aquatic priority outranks basaltic Redstone.
- Redstone therefore has three compatible specialized biomes; it is intentionally not padded with an incompatible fourth.
- High-confidence snowy, swamp, jungle, taiga and redwood tags are retained even when a family exceeds 12 biome ids.
- Dense Silver, Lapis, Copper and Nickel families use lower weight, higher fail chance and/or reduced tier reserves instead of arbitrary biome exclusion.

## Evidence status

- Static generation and exclusivity: generated; validate with `scripts/validate_worldgen_assets.py`.
- Local Gradle/build/Minecraft/Prism execution: NOT_PERFORMED by project policy.
- Candidate rarity envelope: `SUPPORTED_INFERENCE` in `MULTI_SEED_CANDIDATE_MODEL.md`.
- Real multi-seed biome frequency: implemented by the full-runtime GameTest using a fresh normal Overworld `BiomeSource` whose TerraBlender state is independently initialized for each seed, a matching seed-specific `RandomState`, 6,400 chunk columns per seed and three altitudes; output is `build/reports/ioe/multi-seed-biome-frequency.json`.
- Core Sample, Excavator, fallback and real multi-seed runtime: NOT_PERFORMED until GitHub-hosted CI runs.
