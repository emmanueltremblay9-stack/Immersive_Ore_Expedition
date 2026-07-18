# Expedition Compass Blockbench Source

`expedition_compass.bbmodel` is the canonical Blockbench inspection and UV
authoring surface for the expedition compass. The hardcoded element
specifications in `tools/sync_expedition_compass_asset.py` are the generation
authority for runtime geometry and the 32 direction × 4 gear-phase matrix.
Transcribe intentional
Blockbench geometry edits into those specifications before running `--write`;
otherwise synchronization will replace the edited geometry in the `.bbmodel`.

Authoring contract:

- Blockbench format: Java Block/Item (imported through Java Block Sequencer)
- Texture resolution: 16×16
- Front/dial axis: +Z
- Up axis: +Y
- Root pivot: `[8, 8, 8]`
- Direction-marker pivot: `[8, 8, 10.1]`
- Gear pivots: large `[9.2, 6.6, 10.34]`, small `[5.45, 9.2, 10.42]`,
  copper `[5.9, 6.6, 10.44]`
- Authoring animation: `animation.expedition_compass.gear_drive`, 0.4 seconds,
  looping
- Runtime texture: `../resources/assets/immersive_ore_expedition/textures/item/expedition_compass.png`
- Runtime models: `../resources/assets/immersive_ore_expedition/models/item/expedition_compass.json`
  plus 128 combined direction/gear-phase models

The runtime keeps the 32 phase-zero direction filenames for compatibility and
adds three `_g01` through `_g03` variants for every direction. The
`immersive_ore_expedition:angle` predicate controls only the locator needle.
The independent `immersive_ore_expedition:gear_phase` predicate advances the
large brass and small steel gears together while the copper idler
counter-rotates. The Blockbench timeline is the authoring preview; Minecraft
uses the generated whole-model frame matrix. After an intentional visual edit,
run:

```powershell
python tools/sync_expedition_compass_asset.py --write
python tools/sync_expedition_compass_asset.py
python tools/validate_expedition_compass_asset.py
```

Do not replace either namespaced predicate, add a custom renderer, or change
compass gameplay from this asset workflow.
