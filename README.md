# Immersive Ore Expedition

Immersive Ore Expedition is a consolidated NeoForge 1.21.1 / Java 21 mod focused on replacing random branch mining with structure- and province-anchored resource expeditions.

The active mod source lives in `ioe_project_packs/immersive_ore_expedition` with the mod id `immersive_ore_expedition`. The older six source packs remain in the repository as legacy reference material and are not the active build target.

## Repository Layout

| Path | Purpose |
| --- | --- |
| `ioe_project_packs/immersive_ore_expedition` | Active consolidated NeoForge module and CI build root. |
| `ioe_project_packs/00_master_planning_pack` | Shared plan, scope decisions, module order, resource policy, and workspace scripts. |
| `ioe_project_packs/01_ioe_core` through `ioe_project_packs/06_ioe_retrogen_admin` | Legacy split-module source retained for audit and reference only. |

## Development Order

Follow the order from the master planning pack:

1. `01_ioe_core`
2. `02_ioe_expedition_worldgen`
3. `05_ioe_ieip_prospecting`
4. `03_ioe_crystal_growth`
5. `04_ioe_nether_geodes`
6. `06_ioe_retrogen_admin`

Open one module folder at a time, read its `docs/` directory, and use its `CODEX_IMPLEMENTATION_PROMPT.txt` before implementation work.

## Current Constraints

- Minecraft target: 1.21.1
- Loader: NeoForge
- Java: 21
- License policy in module packs: All Rights Reserved
- Out of scope unless explicitly re-added later: Immersive Petro-Machinery, MineColonies, and CIVITAS integration
- Resource policy: skip missing resources and log them; do not invent fake ores or substitute unavailable registry entries
- Province namespace: new province ids use `immersive_ore_expedition`; old split ids are legacy references only.

## Province System

Province System v1 is a safe foundation layer for biome/province matching and resource category policy. It adds config-backed allow, deny, exclude, and diagnostic primitives without registering new blocks, items, ores, gems, mixins, access transformers, or destructive worldgen hooks.

See `docs/PROVINCE_SYSTEM_V1.md` for scope, config keys, strict exclusions, and follow-up risks.

## Validation Policy

Local Gradle validation, local builds, and local Minecraft or Prism runtime smoke tests are disabled by default for this project workflow. GitHub Actions is the source of truth for automated validation. Runtime Prism testing requires explicit manual approval before launching anything locally.

The consolidated module CI verifies the release jar structure: the runtime jar must include compiled classes under `com/oblixorprime/ioe/` and `META-INF/neoforge.mods.toml`. Release and smoke guidance lives in:

- `ioe_project_packs/immersive_ore_expedition/docs/RELEASE_CHECKLIST.md`
- `ioe_project_packs/immersive_ore_expedition/docs/SMOKE_VALIDATION.md`
- `ioe_project_packs/immersive_ore_expedition/docs/SERVER_CONFIG_EXAMPLES.md`
- `ioe_project_packs/immersive_ore_expedition/docs/RELEASE_NOTES_TEMPLATE.md`

Current v7-v18 worldgen work is mostly scaffold, planning, policy, persistence, release validation, and a default-off runtime placement proof gate. Live client/server smoke evidence must be recorded separately, and the release jar must remain class-bearing rather than metadata-only.

## Build Status

The module statuses below are retained as historical alpha-foundation notes. New automated validation runs in CI from the consolidated build root at `ioe_project_packs/immersive_ore_expedition`.

Verified alpha foundations:

| Module | Version | Build root | Verification | LAB status |
| --- | --- | --- | --- | --- |
| `ioe_core` | `0.1.1-alpha` | `ioe_project_packs/01_ioe_core` | `.\gradlew.bat clean build` | Installed and hash-verified |
| `ioe_expedition_worldgen` | `0.1.1-alpha` | `ioe_project_packs/02_ioe_expedition_worldgen` | `.\gradlew.bat clean build` | Installed and hash-verified |
| `ioe_ieip_prospecting` | `0.1.2-alpha` | `ioe_project_packs/05_ioe_ieip_prospecting` | `.\gradlew.bat clean build` | Installed and hash-verified |
| `ioe_crystal_growth` | `0.1.1-alpha` | `ioe_project_packs/03_ioe_crystal_growth` | `.\gradlew.bat clean build` | Installed and hash-verified |
| `ioe_nether_geodes` | `0.1.1-alpha` | `ioe_project_packs/04_ioe_nether_geodes` | `.\gradlew.bat clean build` | Installed and hash-verified |
| `ioe_retrogen_admin` | `0.1.2-alpha` | `ioe_project_packs/06_ioe_retrogen_admin` | `.\gradlew.bat clean build` | Installed and hash-verified |

The historical module foundations used Gradle 8.8 wrappers and a local LAB mods folder placeholder: `<local-mods-folder>`.

All six module packs now have installed alpha foundations. A Prism LAB client load smoke reached resource reload with all six IOE mods present and no crash/fatal signatures in the checked log window. These are still foundation slices, not a completed gameplay loop: world-entry testing, runtime feature placement, server smoke, and full retrogen mutation are later milestones.
