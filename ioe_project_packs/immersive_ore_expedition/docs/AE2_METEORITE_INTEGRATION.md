# AE2 parallel meteorite contract

## Current world-generation rule

AE2 19.2.17 keeps ownership of its normal meteorite and Certus-growth progression. IOE does not replace, relocate, or reproduce AE2 meteorites inside expedition structures.

The IOE overlay at `data/ae2/tags/worldgen/biome/has_meteorites.json` is deliberately empty with `replace: false`. It therefore contributes no biome exclusion and preserves the upstream AE2 `has_meteorites` values. The new-chunk ore guard also leaves AE2 budding quartz, quartz buds and clusters, Sky Stone, and Fluix blocks untouched.

Certus remains available through the rare `immersive_ore_expedition:mineral/certus` Immersive Engineering mineral mix. The final Budding design also permits authorized native AE2 Budding ranks in IOE sites and makes every Flawless block Motherlode-exclusive. Reconciling that exclusivity with preserved upstream meteorites requires explicit implementation and hosted/runtime verification; this document does not claim it is already enforced.

AE2 Crystal Science's normal and charged Certus ore placed features remain part of IOE's physical free-ore suppression policy. This does not suppress AE2's meteorite structure or its normal processing, repair, growth, crafting, and automation methods.

## Ownership and proof boundary

- AE2 owns its structures, blocks, items, recipes, machines, growth and repair behavior.
- Immersive Engineering owns mineral discovery, extraction, depletion, and output selection.
- IOE owns the biome allowlist, mineral-mix recipe, reserve transaction, site quality, initial Budding rank, and Motherlode-only Flawless policy.
- No AE2 source or asset is copied into IOE.

Static evidence was inspected from AE2 `19.2.17` (`neoforge/v19.2.17`, source commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`). Runtime proof belongs to the pinned full-runtime GitHub Actions GameTests.

## Unresolved replacement policy

`BUDDING_FINAL_DECISIONS.md` specifies Motherlode-only Flawless but does not
specify what replaces native meteorite Flawless or whether existing chunks must
be migrated. This contract preserves meteorites and native progression, and
does not itself authorize selecting a replacement rank. The productive Certus
node's surrounding resource is also unspecified. See
[the decision record](CERTUS_INTEGRATION_BLOCKERS.md) before implementing either
policy. No replacement or retrogen is introduced by this documentation update.
