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

## Province System v18 runtime placement proof gate

v18 adds the first default-off runtime placement proof gate:

- `worldgen.runtimePlacementEnabled`, default `false`
- `worldgen.runtimePlacementDiagnostics`, default `false`

`IoeWorldgenPlacementGates.fromConfig()` now reads the explicit runtime placement gate instead of hard-coding runtime placement off. With defaults, runtime placement remains no-op.

The new `RuntimeWorldgenPlacementProof` path is intentionally narrow. It validates a known expedition anchor through the existing anchor planner, requires an explicit block `ResourceRef`, evaluates that block through the existing `ResourcePolicyService` and `LoadedResourceScanner`, preserves strict exclusions, refuses block tags instead of substituting an arbitrary block, and only attempts world placement inside the writable generation region and into an empty target when called with an enabled gate. `OreLoadGenerator.generateAnchoredOreLoad` delegates to this proof path with a vanilla `minecraft:amethyst_block` proof resource, still guarded by the same loaded-resource and policy checks.

Diagnostics are emitted only when `worldgen.runtimePlacementDiagnostics` is enabled. No runtime logs are emitted by default.

This is the first opt-in proof slice, not the complete gameplay loop. v18 does not register configured features, placed features, biome modifiers, structure templates, blocks, items, entities, ores, gems, fluids, datapacks, mixins, access transformers, or dependencies. It does not implement IE/IP clues, crystal growth, Nether geodes, Ancient Debris hearts, ore-load chamber placement, random ore suppression hooks, or retrogen mutation. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.

## Province System v19 runtime registration smoke bridge

v19 adds one default-off bridge between NeoForge runtime feature registration and the existing v18 placement proof path. The consolidated module registers a custom feature type under the existing `immersive_ore_expedition:tiny_vertical_mine_entrance` key. It does not register configured features, placed features, biome modifiers, datapack JSON, structure templates, blocks, items, entities, ores, gems, fluids, mixins, access transformers, dependencies, or the complete surface clue to ore-load gameplay loop.

The bridge is guarded separately from the v18 placement proof:

- `worldgen.runtimeProofFeatureEnabled`, default `false`
- `worldgen.runtimeProofFeatureDiagnostics`, default `false`
- `worldgen.runtimePlacementEnabled`, default `false`
- `worldgen.runtimePlacementDiagnostics`, default `false`

With defaults, registered feature invocations are no-op and non-noisy. When a future controlled smoke profile explicitly enables both the v19 bridge gate and the v18 placement gate, the registered feature delegates to `OreLoadGenerator.generateAnchoredOreLoad`, which routes through the v18 runtime placement proof/resource-policy path. Missing, denied, unsupported, or strictly excluded resources are skipped rather than substituted, and writable-region/replaceability checks still apply before any block placement attempt. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.

## Province System v20 configured/placed feature declaration bridge

v20 adds declaration-only datapack resources for the existing v19 proof feature id:

- `data/immersive_ore_expedition/worldgen/configured_feature/tiny_vertical_mine_entrance.json`
- `data/immersive_ore_expedition/worldgen/placed_feature/tiny_vertical_mine_entrance.json`

The configured feature points at the registered custom feature type `immersive_ore_expedition:tiny_vertical_mine_entrance` with an empty `NoneFeatureConfiguration` payload. The placed feature references that configured feature with an empty placement modifier list.

This makes the v19 proof feature addressable as configured/placed worldgen data, but it still does not add a biome modifier, does not attach the placed feature to any biome, and does not create live placement by default. The existing gates remain unchanged and default-off:

- `worldgen.runtimeProofFeatureEnabled`, default `false`
- `worldgen.runtimeProofFeatureDiagnostics`, default `false`
- `worldgen.runtimePlacementEnabled`, default `false`
- `worldgen.runtimePlacementDiagnostics`, default `false`

v20 does not add structures, blocks, items, entities, ores, gems, fluids, mixins, access transformers, dependencies, IE/IP clues, crystal growth, Nether geodes, Ancient Debris hearts, retrogen mutation, or the complete surface clue to ore-load gameplay loop. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.

## Province System v21 default-off biome modifier smoke-tag bridge

v21 adds one datapack biome modifier declaration and one IOE-owned biome tag for a controlled future smoke invocation path:

- `data/immersive_ore_expedition/neoforge/biome_modifier/tiny_vertical_mine_entrance_smoke_bridge.json`
- `data/immersive_ore_expedition/tags/worldgen/biome/worldgen_smoke_test_biomes.json`

The biome modifier uses the verified NeoForge `neoforge:add_features` shape, targets only `#immersive_ore_expedition:worldgen_smoke_test_biomes`, references the existing placed feature `immersive_ore_expedition:tiny_vertical_mine_entrance`, and runs at the verified `surface_structures` decoration step. The shipped smoke biome tag has an empty `values` list, so no real vanilla or modded biome receives the placed feature by default.

This is still a default-safe bridge, not live gameplay proof. Runtime invocation remains impossible in default worlds because the tag binds zero biomes, and any future external smoke datapack that adds an explicit biome to the tag must still enable both gates before placement can be attempted:

- `worldgen.runtimeProofFeatureEnabled`, default `false`
- `worldgen.runtimePlacementEnabled`, default `false`

The v19 custom feature and v18 placement proof path still preserve resource-policy validation, loaded-resource checks, strict exclusions, missing-resource skips, writable-region checks, and opt-in diagnostics. v21 does not add real biome binding by default, structures, blocks, items, entities, ores, gems, fluids, mixins, access transformers, dependencies, IE/IP clues, crystal growth, Nether geodes, Ancient Debris hearts, retrogen mutation, or the complete surface clue to ore-load gameplay loop. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.

## Province System v22 controlled external worldgen smoke profile

v22 adds a docs-only controlled smoke profile package at `docs/smoke_profiles/v22_worldgen_smoke_profile/`. The package includes an external datapack template, a config template, and an evidence template for a future manual smoke run.

The datapack template appends exactly one explicit vanilla test biome, `minecraft:plains`, to `immersive_ore_expedition:worldgen_smoke_test_biomes`. It does not use broad biome tags, modded biome ids, or multiple biome ids, and it is not copied into active `src/main/resources`.

The config template explicitly enables both default-off smoke gates for a disposable test world:

- `worldgen.runtimeProofFeatureEnabled = true`
- `worldgen.runtimeProofFeatureDiagnostics = true`
- `worldgen.runtimePlacementEnabled = true`
- `worldgen.runtimePlacementDiagnostics = true`

The active shipped smoke tag still contains zero biome ids, all runtime gates still default to `false`, and province runtime integration remains disabled by default. v22 does not run smoke, does not prove live placement, and does not add blocks, items, entities, ores, gems, fluids, mixins, access transformers, dependencies, generated content, or the complete surface clue to ore-load gameplay loop.

## Province System v23 controlled smoke runbook and result template

v23 adds a docs-only controlled smoke runbook at `docs/smoke_profiles/v23_worldgen_smoke_runbook/`. The runbook consumes the v22 external datapack/config profile and formalizes the preflight checks, client/server execution matrix, result classifications, and evidence fields that must be captured before any live placement proof is claimed.

This remains process documentation only. It does not add or modify active `src/main/resources` data, default config values, biome bindings, configured features, placed features, biome modifiers, structures, blocks, items, entities, ores, gems, fluids, mixins, access transformers, dependencies, generated content, or the complete surface clue to ore-load gameplay loop. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.

## Province System v24 post-smoke evidence gate

v24 adds a docs-only evidence gate at `docs/smoke_profiles/v24_worldgen_smoke_evidence_gate/`. The gate is for maintainer review after a v23 smoke result is filled from an actual run, and it records a go/no-go decision before any later runtime integration or shipped-behavior change is planned.

This remains process documentation only. It does not execute smoke, accept evidence by itself, activate runtime worldgen, change shipped `src/main/resources`, alter config defaults, bind real biomes by default, or implement the complete surface clue to ore-load gameplay loop. If required v23 evidence is missing, the decision remains no-go.

## Province System v25 runtime promotion readiness packet

v25 adds a docs-only runtime promotion readiness packet at `docs/smoke_profiles/v25_worldgen_runtime_promotion_readiness/`. The packet links the v23 smoke result and v24 maintainer decision, then records whether the next step is `DOCS_ONLY_HOLD`, `REPEAT_SMOKE`, `BLOCKED`, or `READY_FOR_RUNTIME_SLICE`.

This remains process documentation only. `READY_FOR_RUNTIME_SLICE` does not activate runtime worldgen, change active `src/main/resources`, alter config defaults, or accept smoke evidence by itself. Any future runtime activation must be a separate small PR with explicit runtime diffs, rollback/disable expectations, and fresh validation.

## Province System v26 runtime slice implementation packet

v26 adds a docs-only runtime slice implementation packet at `docs/smoke_profiles/v26_worldgen_runtime_slice_packet/`. The packet translates a completed v23/v24/v25 evidence chain into a reviewable outline for the smallest later separate runtime PR, including candidate active resource/source surfaces, explicit out-of-scope items, required validation, and rollback/disable expectations.

This remains process documentation only. It does not activate runtime worldgen, change active `src/main/resources`, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or accept smoke evidence by itself. Runtime worldgen remains disabled in v26, and any future runtime/resource change must regenerate validation and smoke evidence in its own PR.

## Province System v27 runtime resource inventory snapshot

v27 adds a docs-only active runtime resource inventory snapshot at `docs/smoke_profiles/v27_worldgen_runtime_resource_inventory/`. The inventory records read-only observations of the active shipped resource pack metadata, NeoForge mod metadata template, IOE smoke biome tag, biome modifier smoke bridge, configured feature declaration, placed feature declaration, and related config gate defaults.

This remains process documentation only. It does not activate runtime worldgen, authorize a runtime PR, change active `src/main/resources`, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or accept smoke evidence by itself. Runtime worldgen remains disabled in v27, and any future runtime/resource change must recheck the inventory after rebase, keep a disable path, and regenerate validation and smoke evidence in its own PR.

## Province System v28 runtime PR preflight manifest

v28 adds a docs-only runtime PR preflight manifest at `docs/smoke_profiles/v28_worldgen_runtime_pr_preflight/`. The manifest turns the v23 smoke result, v24 evidence gate, v25 readiness packet, v26 runtime slice packet, and v27 runtime resource inventory into a preflight checklist for a later separate runtime PR.

This remains process documentation only. It does not activate runtime worldgen, authorize a runtime PR, change active `src/main/resources`, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or accept smoke evidence by itself. Runtime worldgen remains disabled in v28, and any future runtime/resource change must fill the preflight manifest after rebase, keep a disable path, and regenerate validation and smoke evidence in its own PR.

## Province System v29 runtime traceability matrix

v29 adds a docs-only runtime traceability matrix at `docs/smoke_profiles/v29_worldgen_runtime_traceability_matrix/`. The matrix maps candidate future runtime paths to the required v23 smoke result, v24 evidence gate, v25 readiness packet, v26 runtime slice packet, v27 runtime resource inventory, and v28 runtime PR preflight manifest references.

This remains process documentation only. It does not activate runtime worldgen, authorize a runtime PR, change active `src/main/resources`, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or accept smoke evidence by itself. Runtime worldgen remains disabled in v29, and any future runtime/resource change must refresh the matrix after rebase, keep a disable path, and regenerate validation and smoke evidence in its own PR.

## Province System v30 runtime evidence packet

v30 adds a docs-only runtime evidence packet at `docs/smoke_profiles/v30_runtime_evidence_packet/`. The packet translates the v29 traceability matrix into a future smoke evidence capture form covering run identity, jar/build provenance, config gate proof, active datapack/worldgen resource proof, runtime log expectations, world load proof, chunk/biome sampling, artifact references, and final `PASS`, `FAIL`, or `INCONCLUSIVE` criteria.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or accept smoke evidence by itself. Runtime worldgen remains disabled in v30, and any future real smoke run must capture fresh evidence from the tested branch, commit, PR, jar/build provenance, client/server/world profile, logs, and coordinates.

## Province System v31 runtime evidence review checklist

v31 adds a docs-only runtime evidence review checklist at `docs/smoke_profiles/v31_runtime_evidence_review_checklist/`. The checklist reviews a filled v30 evidence packet after a future real smoke run, separates absent, incomplete, contradictory, and sufficient proof, and records whether the evidence packet is `ACCEPTED`, `REJECTED`, `NEEDS_MORE_EVIDENCE`, or `OUT_OF_SCOPE`.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or invent smoke evidence. Runtime worldgen remains disabled in v31, and any accepted review still applies only to the reviewed evidence packet scope unless a later separate runtime PR is explicitly authorized and validated.

## Province System v32 runtime evidence decision record

v32 adds a docs-only runtime evidence decision record at `docs/smoke_profiles/v32_runtime_evidence_decision_record/`. The record turns a completed v31 review into a traceable final decision, preserves the v29 -> v30 -> v31 -> v32 audit trail, records reason codes, tracks gaps, and names required remediation before an evidence packet can be accepted.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, replace v29, v30, or v31, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or invent smoke evidence. Runtime worldgen remains disabled in v32, and any accepted decision still applies only to the reviewed evidence record scope unless a later separate runtime PR is explicitly authorized and validated.

## Province System v33 runtime evidence remediation tracker

v33 adds a docs-only runtime evidence remediation tracker at `docs/smoke_profiles/v33_runtime_evidence_remediation_tracker/`. The tracker turns a completed v32 decision record into follow-up actions that preserve the v29 -> v30 -> v31 -> v32 -> v33 audit trail, carry v32 reason codes forward, track missing or contradictory proof, and define closure gates before any remediation can affect a later decision.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, replace v29, v30, v31, or v32, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or invent smoke evidence. Runtime worldgen remains disabled in v33, and any closed remediation action still applies only to the tracked evidence scope unless a later separate runtime PR is explicitly authorized and validated.

## Province System v34 runtime evidence remediation closure record

v34 adds a docs-only runtime evidence remediation closure record at `docs/smoke_profiles/v34_runtime_evidence_remediation_closure_record/`. The record turns v33 remediation actions into reviewed closure decisions that preserve the v29 -> v30 -> v31 -> v32 -> v33 -> v34 audit trail, document closure reason codes, track reopened actions, and keep deferred or out-of-scope work visible.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, replace v29, v30, v31, v32, or v33, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or invent smoke evidence. Runtime worldgen remains disabled in v34, and any verified closure still applies only to the reviewed evidence scope unless a later separate runtime PR is explicitly authorized and validated.

## Province System v35 runtime evidence final sign-off handoff

v35 adds a docs-only runtime evidence final sign-off handoff at `docs/smoke_profiles/v35_runtime_evidence_final_signoff_handoff/`. The handoff turns v34 remediation closure decisions into a final review transmission packet that preserves the v29 -> v30 -> v31 -> v32 -> v33 -> v34 -> v35 audit trail, records readiness states, tracks remaining blockers, and separates merge-review readiness from executed runtime proof.

This remains process documentation only. It does not run smoke, activate runtime worldgen, authorize a runtime PR, authorize merge by itself, replace v29, v30, v31, v32, v33, or v34, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, or invent smoke evidence. Runtime worldgen remains disabled in v35, and any final review handoff still applies only to the reviewed evidence scope unless a later separate runtime PR or merge decision is explicitly authorized and validated.

## Province System v36 ore-load chamber block candidate planning

v36 adds source-only block candidate planning for ore-load chambers. The new planner consumes an already allowed `OreLoadChamberPlacementPlan` and produces deterministic concrete block targets inside the planned chamber envelope. It refuses skipped chamber plans and refuses `BLOCK_TAG` resources instead of substituting an arbitrary block.

v36 also adds conservative replacement rules for future world writers. Those rules reject air, fluids, bedrock, barriers, portal blocks, command/structure-critical blocks, and block-entity states before any future placement path can treat a target as replaceable.

This remains runtime-prep planning only. It does not call `setBlock`, activate runtime worldgen, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, add blocks/items/entities/ores/gems/resources, or complete the surface clue to anchored ore-load gameplay loop. Runtime worldgen remains default-off and manual client/server/world smoke evidence is still required before claiming live placement.

## Province System v37 default-off ore-load chamber block placement applier proof

v37 adds a narrow default-off/manual applier proof layer for ore-load chamber block placement plans. The applier consumes a precomputed v36 `OreLoadChamberBlockPlacementPlan`, checks each candidate against conservative replacement rules, then reports deterministic placed and skipped counters for a caller-supplied target.

The only new world mutation boundary is isolated in the applier's explicit `WorldGenLevel` adapter. No planner calls `setBlock`, no biome modifier or resource JSON invokes the applier, and no automatic placement path is registered by this slice. Source-level tests use a small target seam so replacement and result behavior can be verified without vanilla bootstrap-heavy block initialization.

This remains runtime proof plumbing only. It does not enable runtime worldgen by default, change active `src/main/resources`, change active JSON, alter config defaults, bind real biomes by default, modify legacy split-module source trees, add blocks/items/entities/ores/gems/resources, add mixins, add access transformers, add dependencies, or complete the surface clue to anchored ore-load gameplay loop. Manual client/server/world smoke evidence is still required before claiming live placement or gameplay proof.
