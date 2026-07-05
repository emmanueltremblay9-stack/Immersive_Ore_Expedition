# IOE Worldgen Registration Scaffold

Province System v7 adds a scaffold for future runtime worldgen registration in the consolidated module. The scaffold centralizes:

- stable IOE worldgen feature keys under the `immersive_ore_expedition` namespace;
- a bootstrap entrypoint for future configured feature, placed feature, and biome modifier wiring;
- runtime placement gates that keep placement no-op by default.

This is architecture only. It does not register placed features, configured features, biome modifiers, structure templates, blocks, items, entities, ores, gems, fluids, datapacks, mixins, access transformers, or dependencies.

Runtime behavior remains default-off. Future placement systems can use `IoeWorldgenBootstrap`, `IoeWorldgenRegistration`, `IoeWorldgenFeatureKeys`, and `IoeWorldgenPlacementGates` to plug in safely, but v7 does not place ore chambers, anchors, IE/IP clues, crystal growth sites, Nether geodes, or retrogen mutations.

Existing Province System behavior is unchanged: `worldgen.provinces.runtimeIntegrationEnabled` still defaults to `false`, `worldgen.provinces.resourcePolicyRules` still defaults to `[]`, strict resource exclusions still win, diagnostics remain opt-in, and old split IOE namespaces remain legacy opt-in references only.

## Province System v8 anchor placement planning

v8 adds deterministic expedition anchor placement planning for:

- `tiny_vertical_mine_entrance`
- `collapsed_shaft`
- `miner_camp`
- `buried_survey_marker`
- `basic_mineshaft_connector`

This is still scaffold-only planning. Runtime worldgen remains default-off and no-op unless a caller explicitly supplies enabled placement gates. v8 does not generate live structures, place ore-load chambers, place IE/IP clues, place crystal/geode sites, add retrogen mutation, or introduce generated content.

Anchor planning accepts only known `immersive_ore_expedition` anchor keys, rejects old split IOE namespaces as defaults, and returns safe skipped plans for invalid input or disabled runtime placement. v9 is expected to build on `OreLoadPlan` with ore-load chamber placement planning or placement scaffold work.

## Province System v9 ore-load chamber placement planning

v9 adds deterministic ore-load chamber placement planning from existing `OreLoadPlan` output. It records future chamber center, resource, site quality, anchor type, optional biome/province context, and chamber metadata such as shape, radius, half-size, and approximate volume.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, no live chamber blocks are placed, no configured features or placed features are registered, no biome modifiers are registered, no structures are generated, no IE/IP clues are placed, and no crystal/geode/retrogen mutation is added.

The planner preserves the existing `ResourcePolicyService` path, keeps strict exclusions enforced, rejects unloaded or policy-denied resources safely, and does not carry block-state lists or world references. v10 is expected to handle random ore suppression integration scaffold work.

## Province System v10 random ore suppression integration scaffold

v10 adds a deterministic random ore suppression decision scaffold. It models how future hooks would decide whether a random ore attempt should keep original density, scale density through `OreSuppressionPolicy`, suppress placement, skip unloaded resources, or reject policy-denied and strictly excluded resources.

This is still scaffold-only planning. Runtime worldgen remains default-off and no-op, no live ore generation is intercepted, no ore blocks are removed or placed, no configured features or placed features are registered, no biome modifiers are registered, no mixins or access transformers are added, and no retrogen mutation is added.

Suppression planning preserves the existing `ResourcePolicyService` checks, keeps strict exclusions enforced, records optional biome/province context only as metadata, and does not carry block-state lists, world references, or runtime placement hooks. v11 is expected to handle live biome-to-province worldgen binding scaffold work.

## Province System v11 live biome-to-province binding scaffold

v11 adds a deterministic live biome-to-province context-binding scaffold. It models how future worldgen callers can resolve a live biome id into province metadata and pass that optional context into anchor placement planning, ore-load planning, ore-load chamber planning, and random ore suppression planning.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, province runtime integration remains controlled by the existing `worldgen.provinces.runtimeIntegrationEnabled` gate, no worldgen placement is added, no configured features or placed features are registered, no biome modifiers are registered, no blocks are placed or removed, no ore generation is intercepted, and no retrogen mutation is added.

The binding adapter uses the existing `worldgen.provinces.defaultProvince` and `worldgen.provinces.biomeProvinceBindings` behavior, preserves malformed-binding safety and legacy namespace policy, and stores no world references, block-state lists, or runtime placement hooks. v12 is expected to handle IE/IP surface clue placement planning scaffold work.

## Province System v12 IE/IP surface clue placement planning

v12 adds deterministic IE/IP surface clue placement planning scaffold for Immersive Engineering mineral outcrop clues and Immersive Petroleum seep, pocket-lake, and gas-vent clue metadata.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, no live boulders, seep pockets, lakes, or gas vents are placed, full underground IE/IP deposit rendering remains out of scope, no configured features or placed features are registered, no biome modifiers are registered, no blocks are placed or removed, no ore generation is intercepted, and no retrogen mutation is added.

The planner preserves optional IE/IP mod safety, uses only existing resource references, carries optional biome/province context as metadata, and keeps strict exclusions enforced before future placement hooks can use a clue plan. v13 is expected to handle crystal and AE2 site placement planning scaffold work.

## Province System v13 crystal and AE2 site placement planning

v13 adds deterministic crystal and AE2 site placement planning scaffold for vanilla amethyst growth sites, AE2 Certus growth sites, and optional GeOre growth site metadata.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, no live amethyst, AE2 Certus, or GeOre sites are placed, no fake Fluix ore is generated, no configured features or placed features are registered, no biome modifiers are registered, no blocks are placed or removed, no ore generation is intercepted, and no retrogen mutation is added.

The planner preserves optional AE2 and GeOre mod safety, uses only existing resource references, carries optional biome/province context as metadata, and keeps strict exclusions enforced before future placement hooks can use a crystal site plan. v14 is expected to handle meteoritic AE2 geode planning scaffold work.

## Province System v14 meteoritic AE2 geode planning

v14 adds deterministic meteoritic AE2 geode placement planning scaffold for buried AE2-style Certus geode sites. Plans can record supplied existing-resource references for a Certus primary resource, Sky Stone crust, optional middle layer, optional crystal core, buried-depth metadata, layer radii, rarity/density metadata, and optional biome/province/anchor context.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, no live Sky Stone crust, Certus core, geode layers, or meteorite blocks are placed, no fake Fluix ore is generated, no configured features or placed features are registered, no biome modifiers are registered, no blocks are placed or removed, no ore generation is intercepted, and no retrogen mutation is added.

The planner preserves optional AE2 safety, uses only supplied existing AE2 resource references, rejects unloaded, policy-denied, strictly excluded, malformed, or fake Fluix resources safely, and stores no world references, block-state lists, or runtime placement hooks. v15 is expected to handle Nether sub-lava geode planning scaffold work.

## Province System v15 Nether sub-lava geode planning

v15 adds deterministic Nether sub-lava geode placement planning scaffold for future quartz geode sites anchored below explicit giant lava-lake samples. Plans can record supplied existing-resource references for Nether Quartz and optional Ancient Debris heart metadata, Nether dimension/depth metadata, lava-lake anchor metadata, shell/core radii, and optional biome/province/anchor context.

This remains scaffold-only planning. Runtime worldgen remains default-off and no-op, no live quartz geodes or Ancient Debris hearts are placed, no configured features or placed features are registered, no biome modifiers are registered, no blocks are placed or removed, no ore generation is intercepted, and no retrogen mutation is added.

The planner preserves vanilla resource safety, uses only supplied existing resource references, rejects non-Nether dimensions, invalid lava anchors, invalid depths, unloaded, policy-denied, or strictly excluded resources safely, and stores no world references, block-state lists, or runtime placement hooks. v16 is expected to handle persistent conservative retrogen scaffold work.

## Province System v16 persistent conservative retrogen scaffold

v16 adds persistent conservative retrogen scaffold for queue, marker, pause, resume, failed, skipped, processed, and status snapshot state. The scaffold is designed so future storage can persist and resume admin-controlled retrogen work while retaining processed chunk markers that prevent duplicate retrogen attempts.

This remains persistence/admin-safety scaffold only. Retrogen remains opt-in and admin-controlled, runtime worldgen remains default-off and no-op, no chunks are mutated, no blocks are placed or removed, no ore-load chambers, anchors, IE/IP clues, crystal sites, AE2 geodes, Nether geodes, or Ancient Debris hearts are generated, no configured features or placed features are registered, no biome modifiers are registered, no ore generation is intercepted, and no retrogen resources are generated.

The in-memory scaffold store supports deterministic queue, pause, resume, processed, skipped, failed, and status snapshot semantics for tests and future persistence wiring without storing world references, chunk references, block-state lists, or runtime placement hooks. v17 is expected to handle release hardening and smoke validation docs/checks.

## Province System v17 release hardening and smoke validation

v17 adds release hardening and smoke validation documentation/checks for the consolidated module. CI now verifies that the produced runtime jar contains `META-INF/neoforge.mods.toml` and compiled IOE classes under `com/oblixorprime/ioe/`, helping catch empty, metadata-only, or resource-only jar artifacts before release.

Manual client and dedicated server smoke procedures are documented for release evidence capture. Smoke validation remains a manual activity unless explicitly run outside the default Codex workflow, and release status must not claim full gameplay completion or smoke success without captured evidence.

This remains release/process hardening only. Runtime worldgen remains default-off and no-op, no live placement is enabled, no chunks are mutated, no blocks are placed or removed, no configured features or placed features are registered, no biome modifiers are registered, no config defaults change, and no generated content is added.

Validation for this project remains GitHub Actions on the consolidated NeoForge module. Local Gradle, tests, builds, Minecraft, PrismLauncher, smoke tests, and local CI simulation are disabled by default unless explicitly requested.
