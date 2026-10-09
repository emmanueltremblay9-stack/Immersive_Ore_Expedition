# AE2 parallel meteorite contract

## Current world-generation rule

AE2 19.2.17 keeps ownership of its normal meteorite and Certus-growth progression. IOE does not replace, relocate, or reproduce AE2 meteorites inside expedition structures.

The IOE overlay at `data/ae2/tags/worldgen/biome/has_meteorites.json` is deliberately empty with `replace: false`. It therefore contributes no biome exclusion and preserves the upstream AE2 `has_meteorites` values. The new-chunk ore guard preserves non-Flawless AE2 budding quartz, quartz buds and clusters, Sky Stone, quartz storage and ordinary Fluix blocks. Native Flawless outside authorized IOE Motherlode nodes is replaced with native Flawed during new-chunk admission only.

Certus remains available through the rare `immersive_ore_expedition:mineral/certus` Immersive Engineering mineral mix. The final Budding design also permits authorized native AE2 Budding ranks in IOE sites and makes every Flawless block Motherlode-exclusive. The approved reconciliation preserves meteorite placement and replaces its newly generated Flawless with Flawed. This policy is explicitly non-retroactive: existing chunks and their Flawless blocks remain unchanged.

AE2 Crystal Science's normal and charged Certus ore placed features remain part of IOE's physical free-ore suppression policy. This does not suppress AE2's meteorite structure or its normal processing, repair, growth, crafting, and automation methods.

## Ownership and proof boundary

- AE2 owns its structures, blocks, items, recipes, machines, growth and repair behavior.
- Immersive Engineering owns mineral discovery, extraction, depletion, and output selection.
- IOE owns the biome allowlist, mineral-mix recipe, reserve transaction, site quality, initial Budding rank, and Motherlode-only Flawless policy.
- No AE2 source or asset is copied into IOE.

Static evidence was inspected from AE2 `19.2.17` (`neoforge/v19.2.17`, source commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`). Runtime proof belongs to the pinned full-runtime GitHub Actions GameTests.

## Approved replacement and site policy

On 2026-10-09 at 04:26 UTC the user approved native Flawless → Flawed for new
chunks only, without retrogen. IOE uses its new-chunk guard; it does not move or
suppress meteorites or replace other native progression blocks. Exact IOE
Motherlode placement authorization protects the site's one selected Flawless.

Certus site hearts use native AE2 ranks and the canonical budgets. Surrounding
blocks and DRY residues use `ae2:quartz_block`. Existing chunks are not scanned
for this policy on reload. Consequently old Flawless may still exist outside
Motherlodes. See [implementation and validation scope](CERTUS_INTEGRATION_BLOCKERS.md).
