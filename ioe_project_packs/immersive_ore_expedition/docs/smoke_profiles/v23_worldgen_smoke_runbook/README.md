# V23 Controlled Worldgen Smoke Runbook

This directory is a docs-only execution runbook for the v22 controlled external worldgen smoke profile. It does not add active datapack resources, config defaults, generated files, biome bindings, or smoke evidence by itself.

Use this runbook only with a disposable Minecraft 1.21.1 / NeoForge 21.1.230 test client or dedicated server profile, a release jar whose SHA-256 has been recorded, the v22 external datapack, and the v22 config template.

## Inputs

- V22 datapack: `../v22_worldgen_smoke_profile/datapack/`
- V22 config template: `../v22_worldgen_smoke_profile/immersive_ore_expedition-common.v22-worldgen-smoke.toml`
- Result template: `V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md`
- Fresh `latest.log` for each run.
- Release jar filename and SHA-256.

## Hard Boundaries

- Do not use production worlds.
- Do not copy this directory into `src/main/resources`.
- Do not add broad biome tags such as `#minecraft:is_overworld` or `#c:is_overworld`.
- Do not add modded biomes or multiple test biomes to the controlled smoke tag.
- Do not claim smoke passed unless the result template records evidence from a run.
- Do not claim live placement unless the result records observed placement with coordinates and fresh log evidence.

## Static Preflight

Before launching a client or server smoke run, record:

- The branch and commit under test.
- The IOE jar filename and SHA-256.
- That the active shipped smoke tag still binds zero real biomes by default.
- That the v22 external datapack appends exactly `minecraft:plains`.
- That the active default config keeps all runtime gates `false`.
- That the disposable smoke config enables the v19 proof feature bridge and v18 placement proof gates.
- That province runtime integration remains disabled for this profile.

## Execution Matrix

| Pass | Required setup | Required evidence | Passing condition |
| --- | --- | --- | --- |
| Client world-entry | Fresh disposable client profile, v22 datapack enabled, v22 smoke config applied | Fresh client `latest.log`, world name/seed, biome/coordinates checked | Client loads world without crash/fatal/config/datapack errors and records the observed worldgen outcome |
| Dedicated server startup | Fresh disposable dedicated server profile, v22 datapack enabled, v22 smoke config applied | Fresh server `latest.log`, datapack enabled confirmation, ready-state confirmation | Server reaches ready state without crash/fatal/config/datapack errors and records the observed worldgen outcome |
| Controlled placement observation | A generated or visited `minecraft:plains` area in the disposable world | Coordinates, relevant log excerpt, screenshot or operator note if available | Only passes live placement proof if placement is observed and tied to the checked coordinates/log evidence |

## Result Classification

Use one result classification per run:

- not run
- startup failed
- world load failed
- datapack rejected
- config rejected
- feature skipped because gate disabled
- feature skipped because resource policy denied/missing/excluded
- feature skipped because target outside writable region
- feature skipped because target not replaceable
- placement attempted
- placement observed

`placement observed` is the only classification that can support a live placement proof claim. Every other classification must be reported as no live proof claimed.

## Output

Copy `V23_WORLDGEN_SMOKE_RESULT_TEMPLATE.md` to a task-specific evidence file only when a smoke run is actually performed. Leave the template unmodified for docs-only preparation work.
