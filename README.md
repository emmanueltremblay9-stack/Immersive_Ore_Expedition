# Immersive Ore Expedition

Immersive Ore Expedition is a split NeoForge 1.21.1 / Java 21 mod ecosystem focused on replacing random branch mining with structure- and province-anchored resource expeditions.

This repository currently contains the Codex implementation packs from `Immersive_Ore_Expedition_All_Modules_Bundle.zip`. The packs are production skeletons and planning documents, not completed runtime mods.

## Repository Layout

| Path | Purpose |
| --- | --- |
| `ioe_project_packs/00_master_planning_pack` | Shared plan, scope decisions, module order, resource policy, and workspace scripts. |
| `ioe_project_packs/01_ioe_core` | Shared API, resource policy, config primitives, registry scanning, and province abstractions. |
| `ioe_project_packs/02_ioe_expedition_worldgen` | Surface clues, mineshaft anchors, biome provinces, and structure-linked ore loads. |
| `ioe_project_packs/03_ioe_crystal_growth` | Amethyst, AE2 Certus, and optional GeOre crystal-growth sites. |
| `ioe_project_packs/04_ioe_nether_geodes` | Sub-lava Nether geodes and rare ancient debris hearts. |
| `ioe_project_packs/05_ioe_ieip_prospecting` | Immersive Engineering outcrop clues and Immersive Petroleum seep clues. |
| `ioe_project_packs/06_ioe_retrogen_admin` | Conservative retrogen, chunk markers, diagnostics, and admin commands. |

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

## Build Status

The bundle does not include Gradle wrapper files. Each module contains a Gradle skeleton, but build verification requires adding or restoring an approved wrapper/template for the target NeoForge 1.21.1 project before producing runtime jars.
