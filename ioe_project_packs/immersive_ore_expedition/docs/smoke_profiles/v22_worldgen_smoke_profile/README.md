# V22 Controlled Worldgen Smoke Profile

This directory is a docs-only manual smoke profile package for the v21 biome modifier smoke-tag bridge. It is not shipped as active default data, is not copied into `src/main/resources`, and is not proof that live placement passed.

Use this profile only in a disposable Minecraft 1.21.1 / NeoForge 21.1.230 test world with a fresh log. GitHub Actions remains the automated validation source of truth; manual client/server/world smoke must be evidenced separately before any placement proof is claimed.

## Contents

- `datapack/pack.mcmeta`: external datapack metadata for Minecraft 1.21.1 server data.
- `datapack/data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json`: appends exactly one explicit vanilla test biome, `minecraft:plains`, to the IOE smoke biome tag.
- `immersive_ore_expedition-common.v22-worldgen-smoke.toml`: docs-only config template that enables the v19 proof feature bridge and v18 placement proof gates.
- `V22_WORLDGEN_SMOKE_EVIDENCE_TEMPLATE.md`: evidence checklist for any future manual smoke pass.

## Manual Smoke Setup

1. Start from a fresh disposable test world.
2. Install the profile datapack from `datapack/` into that world's datapacks directory and confirm it is enabled.
3. Copy the values from `immersive_ore_expedition-common.v22-worldgen-smoke.toml` into the generated `immersive_ore_expedition-common.toml` for that test profile.
4. Use a fresh `latest.log`.
5. Record results in `V22_WORLDGEN_SMOKE_EVIDENCE_TEMPLATE.md`.

Do not use broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld`. Do not add modded biomes or multiple vanilla biomes for this controlled profile. Do not use this on production worlds.

## Safety Boundaries

- The active shipped smoke tag remains empty by default.
- The active shipped config keeps all runtime gates default `false`.
- The profile does not add ores, gems, blocks, items, entities, configured features, placed features, mixins, access transformers, dependencies, or generated content.
- Manual client/server/world smoke was not run by this docs package.
