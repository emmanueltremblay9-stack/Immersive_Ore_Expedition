# Runtime Smoke Validation: Immersive Ore Expedition 0.2.0-alpha

Date: 2026-06-29

## Scope

Runtime smoke validation for the consolidated NeoForge IOE jar in the Prism LAB instance.

Current validation policy: no additional local Gradle, Prism, Minecraft, client, server, or world smoke validation should be run on a personal PC by default. Automated validation has moved to GitHub Actions / CI, and any future local Prism runtime testing requires explicit manual approval before launch.

Repository root:

`<project-root>`

Prism instance:

`<prism-instance>`

Active mods tree:

`<local-mods-folder>`

The alternate `.minecraft\mods` tree does not exist for this instance.

## Mods Folder State

Top-level jar count in active mods tree: 40

Active IOE jar count: 1

Active IOE jar:

`immersive_ore_expedition-0.2.0-alpha-neoforge-1.21.1.jar`

SHA-256:

`5B4075F2D16306ED9BD72DA61A743FBEFBAFD98176AC7336D0D81D0C070FEE67`

Top-level old split IOE jar count: 0

Old split IOE active mod ids checked:

- `ioe_core`
- `ioe_expedition_worldgen`
- `ioe_crystal_growth`
- `ioe_nether_geodes`
- `ioe_ieip_prospecting`
- `ioe_retrogen_admin`

Backup folder retained:

`<local-mods-folder>\_ioe_six_module_backup_20260629_011254`

## Launches

Client/main-menu launch:

`prismlauncher.exe --launch "<prism-instance-name>"`

Existing-world launch:

`prismlauncher.exe --launch "<prism-instance-name>" --world "<existing-test-world>"`

Prism launcher path used:

`<local-prismlauncher-executable>`

## Latest Log Findings

Log inspected:

`<prism-instance>\minecraft\logs\latest.log`

Relevant evidence:

- NeoForge loaded: `NeoForge mod loading, version 21.1.233, for MC 1.21.1`
- Consolidated jar discovered: `immersive_ore_expedition-0.2.0-alpha-neoforge-1.21.1.jar`
- Active mod listed: `Immersive Ore Expedition 0.2.0-alpha (immersive_ore_expedition)`
- Resource reload included `mod/immersive_ore_expedition`
- IOE bootstrap lines ran for Core, Worldgen, Crystal Growth, Nether Geodes, IE/IP Prospecting, Retrogen & Admin, and the consolidated entrypoint.
- Consolidated config file was generated at `minecraft\config\immersive_ore_expedition-common.toml`.
- Config sections/options were present for `resourcePolicy`, `worldgen`, `crystalGrowth`, `netherGeodes`, `ieipProspecting`, and `retrogen`.
- Existing world launched: `Started integrated minecraft server version 1.21.1`
- Existing world entered: `SynTekSystems joined the game`
- Shutdown after validation saved overworld, nether, and end chunks.

## Optional Dependencies

Present as external mods:

- `immersiveengineering`: `ImmersiveEngineering-1.21.1-12.4.2-194.jar`
- `immersivepetroleum`: `ImmersivePetroleum-1.21.1-4.4.1-37.jar`

Not present in the active mods folder:

- `ae2`
- `geore`

The absence of AE2 and GeOre did not block loading. IE/IP remained external jars and were not embedded in IOE.

## Retrogen/Admin Hook

The Retrogen/Admin bootstrap line ran:

`Initializing Immersive Ore Expedition: Retrogen & Admin alpha services`

This confirms the consolidated entrypoint executed the bootstrap path that registers `IoeAdminCommands::registerCommands` on `NeoForge.EVENT_BUS`. The command registration event itself does not emit a separate command-name log line in this build.

## Warnings and Errors

No IOE-caused hard failures were found:

- No `ModLoadingException`
- No `NoClassDefFoundError`
- No `NoSuchMethodError`
- No duplicate mod error
- No metadata crash
- No config crash
- No current crash report from the 02:10-02:13 validation run

Observed non-IOE warnings/errors:

- Iris-related mixin warnings for missing Iris classes.
- `modern_companions:textures/item/wand.png.BL` invalid path errors.
- Immersive Petroleum missing sprite/sound warnings.
- Immersive Railroading missing model warnings.
- MineColonies missing texture warnings.
- Existing save warned that the six old IOE split mod ids are missing.

Old IOE missing-mod warnings from existing save:

- `ioe_core (version 0.1.7-alpha -> MISSING)`
- `ioe_crystal_growth (version 0.1.9-alpha -> MISSING)`
- `ioe_expedition_worldgen (version 0.1.6-alpha -> MISSING)`
- `ioe_ieip_prospecting (version 0.1.8-alpha -> MISSING)`
- `ioe_nether_geodes (version 0.1.9-alpha -> MISSING)`
- `ioe_retrogen_admin (version 0.1.10-alpha -> MISSING)`

These warnings are expected save compatibility fallout from replacing six mod ids with one consolidated mod id. They did not prevent the world from opening.

## Smoke Results

Client/main-menu smoke: PASS

Evidence: Minecraft reached the main menu, and a screenshot was captured under ignored build output at:

`ioe_project_packs\immersive_ore_expedition\build\runtime-smoke\client-screen.png`

Existing-world smoke: PASS

Evidence: `New World` opened through Prism CLI, the integrated server started, the player joined, and an in-world screenshot was captured under ignored build output at:

`ioe_project_packs\immersive_ore_expedition\build\runtime-smoke\existing-world-screen.png`

New-world smoke: NOT RUN

Reason: Codex GUI click injection did not reliably interact with the Minecraft window in this desktop context. I did not create a new save by guessing through the UI. The existing-world CLI launch path was used instead.

Current follow-up policy: this gap remains unfilled locally. Build/test/package validation should run in GitHub Actions, and any future local new-world Prism smoke requires explicit manual approval before launching Prism or Minecraft.

## Files Changed

Added:

`docs\RUNTIME_SMOKE_VALIDATION_0.2.0-alpha.md`

No gameplay code was modified.

No source module was deleted or restored.

No old split jars were restored.

## Release-Candidate Readiness

Packaging/runtime-load status: release-candidate ready for the next QA stage.

Reason: the consolidated jar is the only active IOE jar, it loads as `immersive_ore_expedition`, reaches the client main menu, generates/loads its consolidated config, and opens the existing Prism LAB test world without IOE runtime exceptions.

Remaining caveat: a dedicated new-world creation smoke is still unverified because GUI automation was not reliable enough to create a fresh save safely from Codex.
