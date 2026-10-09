# Certus cycle: decisions required before runtime wiring

Audit baseline: PR 63, branch `codex/canonical-budding-site-planner`, remote head
`4e1f861d76111489c367757dc858363a17818289`, checked 2026-10-09.
This checkpoint changes documentation only; it does not implement Certus nodes
or claim stable 1.0.0 acceptance.

## Confirmed contract

[Budding final decisions](BUDDING_FINAL_DECISIONS.md) require productive budgets
of 3/4/5/7 nodes, 4/5/6/7 surrounding blocks per node, and initial ranks
Damaged/Chipped/Flawed/Flawed. Motherlode has one 7.77% draw for at most one
Flawless replacing a Flawed. Lower-tier fallback removes that selection.
[AE2 integration](AE2_METEORITE_INTEGRATION.md) preserves native meteorites,
growth, repair and normal processing. AE2CS Flawless crafting must stay disabled.

## Decision register

| Missing decision | Why it blocks completion | Required answer |
| --- | --- | --- |
| Productive Certus surrounding material | Budgets count physical blocks outside hearts; neither contract maps these to a registered block. Crystal item outputs and Sky Stone shells do not establish that mapping. | Exact registered block for the 4/5/6/7 surrounding blocks. This is separate from DRY. |
| Native meteorite Flawless replacement | Exclusivity requires intervention, but preserving meteorites does not select a replacement rank or define migration. | Replacement rank and scope: new generation only or also existing meteorites/chunks. |

No replacement rank, surrounding material or migration is inferred. The pending
DRY proposals remain 0–5 `ae2:quartz_block` for Certus and no physical residues
for Entro; neither is applied. `ae2:fluix_block` is ordinary AE2 Fluix, not
Entroized Fluix. Entro remains an IE mineral output, never a Budding family.

## Verified implementation gaps

- `worldgen/ExpeditionSiteFeature.java` selects Budding plans through
  `BuddingResourceFamily.fromGeOreMaterial`; Certus has no matching family and
  follows the structure-only path with its separate IE reserve.
- `worldgen/Ae2MeteoriteIntegration.java` can resolve Flawed and Sky Stone
  registry entries. That helper is not a canonical multi-node Certus placement
  path or an authorization to use Sky Stone as counted ore.
- `worldgen/BuddingPlanMetadata.java` detects `GeOreBuddingBlock` only.
- `compat/jade/IoeJadePlugin.java` and `IoeBuddingProvider.java` register and
  inspect GeOre blocks only. Generic `BuddingNodeInfo` serialization does not
  prove native Certus metadata capture, persistence or display.

## Implementation milestones after decisions

1. Resolve native AE2 states and the approved surrounding material, rejecting
   missing resources. Feed the canonical budget and one site draw into geometry.
2. Attach exact-position Certus provenance through the existing transactional
   commit, fallback and compensation path; teach Jade to read the live AE2 rank.
3. Apply the approved meteorite replacement scope without suppressing structures,
   buds, clusters, Sky Stone, ordinary Fluix or normal repair/processing.
4. Verify budgets, fallback, no extra Flawless, independent DRY reward, rollback,
   save/reload, native repair/degradation and Jade callbacks in hosted CI.
5. Verify actual client visuals and progression separately before stable release.

The risks are unauthorized material substitution, accidental meteorite/progression
changes, false provenance after replacement, and counting historical tests as new
evidence. Explicit decisions, transactional tests and exact-commit CI address them.
The prior 596 JUnit / 46 GameTests per configuration / 14 green checks belong to
the previous implementation and do not validate this unimplemented Certus cycle.
