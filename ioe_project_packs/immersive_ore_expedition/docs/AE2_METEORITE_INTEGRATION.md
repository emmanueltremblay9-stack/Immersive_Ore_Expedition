# AE2 parallel meteorite contract

## Current world-generation rule

AE2 19.2.17 keeps ownership of its normal meteorite and Certus-growth progression. IOE does not replace, relocate, or reproduce AE2 meteorites inside expedition structures.

The IOE overlay at `data/ae2/tags/worldgen/biome/has_meteorites.json` is deliberately empty with `replace: false`. It therefore contributes no biome exclusion and preserves the upstream AE2 `has_meteorites` values. The new-chunk ore guard also leaves AE2 budding quartz, quartz buds and clusters, Sky Stone, and Fluix blocks untouched.

Certus is integrated as the rare `immersive_ore_expedition:mineral/certus` Immersive Engineering mineral mix and as one bounded expedition-chamber formation made from AE2's registered flawed budding quartz and Sky Stone blocks. IOE does not reproduce the upstream meteorite structure or copy AE2 assets.

AE2 Crystal Science's normal and charged Certus ore placed features remain part of IOE's physical free-ore suppression policy. This does not suppress AE2's meteorite structure or its normal processing, repair, growth, crafting, and automation methods.

## Ownership and proof boundary

- AE2 owns its structures, blocks, items, recipes, machines, growth and repair behavior.
- Immersive Engineering owns mineral discovery, extraction, depletion, and output selection.
- IOE owns the biome allowlist, mineral-mix recipe, reserve tier, and transaction that places the bounded chamber formation alongside the abstract IE vein.
- No AE2 source or asset is copied into IOE.

Static evidence was inspected from AE2 `19.2.17` (`neoforge/v19.2.17`, source commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`). Runtime proof belongs to the pinned full-runtime GitHub Actions GameTests.
