# Immersive Ore Expedition 0.2.50-alpha

## Release Identity

- Release: `0.2.50-alpha`
- Status: `NOT_PUBLISHED`
- Minecraft: `1.21.1`
- NeoForge: `21.1.230`
- Java: 21

These tracked release notes describe the content of this unpublished alpha candidate, not a qualified stable 1.0.0 release. They are not a publication record and do not imply that a tag, GitHub Release, CurseForge file, Modrinth version, or other distribution exists.

## Release Qualification Provenance

These tracked notes are not the authoritative byte-level provenance record. After the candidate is frozen, a separate final external qualification/publication record must bind the exact source HEAD and tree; the exact CI workflow run, attempt, event, checkout SHA, and checkout tree; the exact artifact ID and digest; the runtime JAR filename, size, SHA-256, and inspection results; and the manual client, dedicated-server, and visual worldgen results and evidence produced from those exact artifact bytes.

Any repository write after final candidate selection creates a new candidate that requires qualification to be repeated and rebound. These notes make no manual-smoke pass or publication claim. Publication requires the complete external binding, every applicable release gate, and separate publication authorization.

### Historical Automated Checkpoint

Commit `854e5c2a2bf6224869ce9413a5781e3622249da5` passed the [push workflow](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/37884681940) and [pull-request workflow](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/37884685289): 14 successful checks across both runs, 598 JUnit tests with no failures, errors, or skips in the inspected push report, and 51 GameTests in each of the baseline and full pinned-runtime configurations in each run. This is historical evidence for that commit only, not qualification of subsequent commits or their artifact bytes. The separately recorded artifact provenance for that candidate must remain unchanged; a later candidate requires its own exact-commit CI and artifact binding.

Manual client, dedicated-server, and visual worldgen validation remain `NOT_PERFORMED`. `PUBLICATION_READY: BLOCKED_MANUAL_SMOKE`.

## Non-Exhaustive Major Changes Since 0.2.0-alpha

The following is a bounded summary of major reviewed changes since `v0.2.0-alpha`, not a complete changelog:

- PR #45 corrected compass scrolling and expedition-site placement behavior.
- PR #46 added connected expedition-site generation, and PR #47 routed ore and crystal generation through expedition sites.
- PR #48 reworked biome mineral distribution, while PR #49 added new-chunk sanitation controls.
- PR #50 extended compass targeting and branding; PR #51 addressed shaft accessibility.
- PR #52 expanded mine generation and village relationships.
- PR #53 added the Immersive Petroleum 4.5 compatibility path.
- PR #54 added installer manifests, and PR #55 finalized the Budding design documentation.
- PR #56 added the Abandoned Prospector Camp Surface 01/02 implementation and its qualification surface: deterministic active and abandoned camp composers, state/context/quality handling, collision and placement validation, optional Domum Ornamentum materialized architecture, tiered loot behavior, and focused validators and tests.
- The final PR #56 DRY/profile GameTest remediation preserved the prospector shaft contract and injected the qualified DRY camp profile into the hosted GameTest path.

These descriptions are based on merged history and reviewed source. They do not substitute for manual client, server, or visual gameplay evidence.

## Additional Changes in Draft PR #63

The following changes are implemented on the candidate branch in [draft PR #63](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/63), which remains unmerged. They are distinct from the merged-history summary above. The canonical contracts are documented in [Budding final decisions](BUDDING_FINAL_DECISIONS.md), [family implementation](BUDDING_FAMILY_IMPLEMENTATION.md), and [Certus integration decisions](CERTUS_INTEGRATION_BLOCKERS.md).

### GeOre and Native Certus Sites

- IOE provides four distinct Budding rank blocks for each of 13 GeOre families: aluminum, coal, copper, diamond, emerald, gold, iron, lapis, lead, nickel, redstone, silver, and uranium (52 blocks total). The five IE-material families require Immersive Engineering; missing dependencies do not trigger substitute materials. Existing Iron identifiers remain compatible.
- Native AE2 Certus hearts use the same canonical site planner and budgets as GeOre. Certus preserves AE2's native growth, repair, and loot behavior, with `ae2:quartz_block` surrounding each productive heart.

| Quality | Hearts | Surrounding blocks per heart | Total excluding hearts | Initial rank |
| --- | ---: | ---: | ---: | --- |
| POOR | 3 | 4 | 12 | Damaged |
| NORMAL | 4 | 5 | 20 | Chipped |
| RICH | 5 | 6 | 30 | Flawed |
| MOTHERLODE | 7 | 7 | 49 | Flawed; one site-wide 7.77% chance to replace one with Flawless |

Quality-roll weights are DRY/POOR/NORMAL/RICH/MOTHERLODE = 10/25/45/17/3; they are not generation probabilities per chunk. Placement and IE reserve registration are transactional. A fallback to a lower quality recomputes its budget and discards any Flawless selection.

### DRY Pockets and Progression

- DRY sites have no Budding hearts. GeOre pockets draw a uniform 0–5 physical `geore:<material>_block` residues for the whole pocket. Certus draws a uniform 0–5 `ae2:quartz_block` residues. Entro is the explicit exception with no physical residues.
- All three profiles retain an independent 10% chance of the neutral `ae2cs:resonating_seed`, including when the residue count is zero. These are the decisions approved on 2026-10-09 at 04:26 UTC.
- Entro remains reserved for IE extraction of `extendedae:entro_crystal`; there is no Budding Entro family. Ordinary `ae2:fluix_block` is neither Entro nor its DRY residue.
- GeOre follows the loaded AE2 growth/degradation behavior: Flawless does not degrade; Flawed → Chipped → Damaged eventually becomes the corresponding vanilla or IE storage block. That exhausted storage is distinct from the GeOre DRY pocket material. Restoration is capped at Flawed, and the AE2 Crystal Science Flawless crafting recipe is disabled.

### Flawless and AE2 Meteorites: Non-Retroactive Migration

New Flawless hearts are restricted to the authorized Motherlode lottery, with at most one per site and no extra eighth heart. In new chunks, unauthorized native `ae2:flawless_budding_quartz` becomes native `ae2:flawed_budding_quartz`; exact authorized IOE hearts are protected. AE2 still generates its meteorites, and quartz, other native ranks, buds/clusters, Sky Stone, and ordinary Fluix are preserved. See [AE2 meteorite integration](AE2_METEORITE_INTEGRATION.md).

This rule does not mutate already-generated chunks and does not enable retrogen. Pre-existing Flawless blocks can remain outside Motherlode sites, so the candidate does not claim global removal of historical Flawless blocks.

### Jade and Saved Site Identity

Optional Jade inspection shows the current rank from the live block and committed IOE provenance: site quality, node index/count, initial surrounding-ore budget, family, and location. Native Certus is identified as AE2. Metadata follows the final successful placement/fallback and is rolled back with failed placement. Player break/replacement removes the node identity; manually placed hearts and ordinary meteorite hearts do not acquire invented IOE site provenance.

Node records persist through the optional `budding_nodes` field in the existing version-2 save format. Old saves without that field remain readable and do not receive invented historical identities. Jade is separately distributed and is not embedded in IOE. See [Budding inspection](BUDDING_INSPECTION.md) for the displayed fields and limitations. Automated checks do not establish the appearance or usability of the overlay in a Minecraft client.

### Consolidation Scope and Deferred Nether Generation

On 2026-10-09 the owner explicitly selected consolidation of the existing approved
scope with Nether generation deferred. This supersedes the undecided scope in the
initial 3.0 roadmap. Version remains `0.2.50-alpha`, status `NOT_PUBLISHED`; 3.0 is
an objective, not a released version. See [current roadmap](PATH_TO_3_0.md).

Nether 74×74 planning, bounded reads, complete read-set revalidation and diagnostics
remain in the source for future work. Production placement remains disabled:
`NetherPlacementRuntime.commit` returns `BACKEND_UNVERIFIED` and the direct
`SubLavaGeodeGenerator.generateBelowLake` path returns `false`. Planning options
are not generation switches. Nether generation is excluded from this consolidation;
no Nether generation, atomic publication or external-claim support is delivered.

Camp structure collision checks now consult only loaded chunks or the current
WorldGenRegion dependency cache, retaining complete structure bounds and the
existing one-block margin. Work is capped at 64 footprint chunks and 256 metadata
entries per plan; primary and fallback plans are checked before placement.
Unavailable metadata or an exhausted budget conservatively refuses the camp.
This protects against native structure metadata, not player builds or third-party claims.

The owner subsequently resumed Nether implementation and approved best-effort
compensation (2026-10-09 19:14:50 UTC), with permanent partial sites permitted only
under the amended failure contract. An explicit prepared-placement backend now
uses real world SavedData and consumes existing first-generation capabilities.
Incomplete rollback is persisted, reserves spacing and cannot be retried after
reload. Automatic generation remains disabled; this is not delivered natural
Nether generation or a relaxation of size, budgets, protection or no-old-chunk rules.

PR62 remains a separate, unmerged draft containing three restoration-rank tests.
It is not included by this scope decision and requires its own disposition.

## Current Worldgen and Config State

- `worldgen.global.naturalExpeditionSiteGenerationEnabled`: default `true`.
- Current Overworld natural expedition-site worldgen: active when the current gate is enabled. Direct source inspection supports the production configured-feature, placed-feature, biome-modifier, and `ExpeditionSiteFeature` path.
- Production wrapper tags use `#c:is_overworld` and remain subject to IOE's runtime eligibility and placement controls. Historical controlled-smoke tags are not the current production binding.
- `worldgen.runtimePlacementEnabled`: default `false`.
- `worldgen.runtimePlacementDiagnostics`: default `false`.
- `worldgen.runtimeProofFeatureEnabled`: default `false`.
- `worldgen.runtimeProofFeatureDiagnostics`: default `false`.
- Retrogen mutation: default-off and administrator-controlled.
- No-fake-resources and unsupported-material protections remain release gates.

PR #56 did not change these defaults. The active natural-worldgen gate and the four disabled legacy proof controls are distinct mechanisms.

## Third-Party Notice Status

Domum Ornamentum remains an optional, separately distributed integration used by the prospector-camp materialized architecture. It must remain unembedded, with no Domum source or assets copied into IOE. The final external qualification/publication record must include artifact inspection that confirms those constraints for the exact runtime JAR.

Domum Ornamentum `1.0.231` has conflicting license metadata: the inspected source tag and runtime metadata identify GPL-3.0, while the published Maven POM identifies LGPL-3.0. `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`. `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`. `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`. The owner disposition is recorded in `docs/DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md` and is not legal approval. CI compatibility testing is not a legal conclusion or redistribution authorization.

## Publication Requirements and Limitations

- Successful applicable hosted qualification and runtime-JAR inspection are required for the exact frozen candidate.
- Manual client world-entry, dedicated-server, and visual worldgen smoke evidence are required from the exact runtime JAR bound by the final external qualification/publication record.
- Hosted tests and artifact inspection do not prove a manual launch, server lifecycle, newly generated world observation, visual composition, or player-facing gameplay result.
- No tag, GitHub Release, or third-party distribution may be created until every release gate has its actual evidence and publication is separately authorized.
- Status remains `NOT_PUBLISHED`.

## Compatibility and Rollback

- Required runtime: Minecraft `1.21.1`, NeoForge `21.1.230`, Java 21.
- Optional integrations remain separately distributed and subject to their own compatible versions and notices.
- Previous published release: `v0.2.0-alpha`.
- Back up worlds and configs before upgrading or downgrading. Existing chunks are not retroactively converted by the Certus meteorite rule; legacy saves without node metadata remain readable, without reconstructed provenance.
- Config and save rollback must be evaluated before downgrading; this candidate does not claim downgrade compatibility.
