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

Active validation path:
- Active module: `ioe_project_packs/immersive_ore_expedition`
- Active mod id: `immersive_ore_expedition`
- Local validation is disabled by default.
- GitHub Actions validates the consolidated module unless Emmanuel explicitly instructs otherwise.
- Legacy split modules are not part of the active validation path.

## Existing Worlds

For strict zero autonomous ore, create a new world with IOE installed before its first chunk is generated. Existing chunks are never stripped automatically: IOE cannot safely distinguish naturally generated ore from player-placed ore, so automatic cleanup could damage builds or stored resources.

Before using any bounded IOE administrator retrogen or locator-reindex operation on an existing world, make a complete backup of the save. Locator reindexing scans only already loaded chunks and does not change world blocks; it does not convert an old world's free ore distribution into IOE mines.

## Data-driven Mine Profiles

Biome-to-resource selection and connected-biome quantity scaling are loaded from the server datapack registry `immersive_ore_expedition:mine_resource_profile`. Built-in entries live under `data/immersive_ore_expedition/immersive_ore_expedition/mine_resource_profile`; each entry selects a biome tag, a resource kind, a connected-chunk survey radius, and exact ore, node, or budding counts for every site quality.

Datapacks may replace the built-in selection and quantity rules. A new `geore` resource name is accepted only when IOE's GeOre and Immersive Engineering integration mappings support that material; the datapack registry does not invent blocks or IE mineral definitions. Crystal profiles only select authorized budding blocks, while AE2, AE2 Crystal Science, or Extended AE continue to own crystal growth and automation.

The consolidated module CI verifies the release jar structure: the runtime jar must include compiled classes under `com/oblixorprime/ioe/` and `META-INF/neoforge.mods.toml`. Release and smoke guidance lives in:

- `ioe_project_packs/immersive_ore_expedition/docs/RELEASE_CHECKLIST.md`
- `ioe_project_packs/immersive_ore_expedition/docs/SMOKE_VALIDATION.md`
- `ioe_project_packs/immersive_ore_expedition/docs/SERVER_CONFIG_EXAMPLES.md`
- `ioe_project_packs/immersive_ore_expedition/docs/RELEASE_NOTES_TEMPLATE.md`

Natural expedition worldgen is now active through the consolidated module's biome modifiers, connected site features, final new-chunk resource guard, and persistent locator. The older v7-v35 scaffold and evidence packets remain historical design records, not descriptions of the current runtime path. Live client/server smoke evidence must still be recorded separately, and the release jar must remain class-bearing rather than metadata-only.

## Build Status

The module statuses below are retained as historical alpha-foundation notes. The legacy Gradle commands in this table are historical/reference evidence only, not current validation instructions. Current validation must use the consolidated module at `ioe_project_packs/immersive_ore_expedition` and GitHub Actions unless Emmanuel explicitly instructs otherwise.

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
