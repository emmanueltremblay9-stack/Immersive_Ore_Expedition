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

Validation for this project remains GitHub Actions on the consolidated NeoForge module. Local Gradle, tests, builds, Minecraft, PrismLauncher, smoke tests, and local CI simulation are disabled by default unless explicitly requested.
