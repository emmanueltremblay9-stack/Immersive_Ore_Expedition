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

## Runtime visibility proof

Current IOE worldgen milestones are scaffold/planning-only, so no visible world or JourneyMap changes are expected yet. Anchors, ore-load chambers, IE/IP surface clues, crystal/geode sites, map markers, overlays, and retrogen mutation remain not live until later gated PRs add placement or marker integration.

Use `/ioe status` to verify that the consolidated mod is loaded and that the scaffold/planning systems are reachable. The command reports the active mod id/version when available, runtime worldgen gates, province runtime integration, diagnostics, scaffold readiness, live placement registration flags, and an explicit planning-only explanation. It does not mutate the world, register features, add JourneyMap integration, or require client-only classes.

Validation for this project remains GitHub Actions on the consolidated NeoForge module. Local Gradle, tests, builds, Minecraft, PrismLauncher, smoke tests, and local CI simulation are disabled by default unless explicitly requested.
