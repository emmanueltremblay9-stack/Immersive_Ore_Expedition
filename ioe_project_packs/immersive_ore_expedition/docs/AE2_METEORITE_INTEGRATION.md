# AE2 parallel meteorite contract

## Current world-generation rule

AE2 19.2.17 keeps ownership of its normal meteorite and Certus-growth progression. IOE does not replace, relocate, or reproduce AE2 meteorites inside expedition structures.

The IOE overlay at `data/ae2/tags/worldgen/biome/has_meteorites.json` is deliberately empty with `replace: false`. It therefore contributes no biome exclusion and preserves the upstream AE2 `has_meteorites` values. The new-chunk ore guard also leaves AE2 budding quartz, quartz buds and clusters, Sky Stone, and Fluix blocks untouched.

Certus is integrated separately as the rare `immersive_ore_expedition:mineral/certus` Immersive Engineering mineral mix. The mix can output the verified item `ae2:certus_quartz_crystal`; it never places Certus ore, a budding heart, Sky Stone, a fake meteorite, a geode, or an intermediate matrix in an IOE site.

AE2 Crystal Science's normal and charged Certus ore placed features remain part of IOE's physical free-ore suppression policy. This does not suppress AE2's meteorite structure or its normal processing, repair, growth, crafting, and automation methods.

## Ownership and proof boundary

- AE2 owns its structures, blocks, items, recipes, machines, growth and repair behavior.
- Immersive Engineering owns mineral discovery, extraction, depletion, and output selection.
- IOE owns only the biome allowlist, mineral-mix recipe, reserve tier, and transaction that reserves an abstract IE vein.
- No AE2 source or asset is copied into IOE.

Static evidence was inspected from AE2 `19.2.17` (`neoforge/v19.2.17`, source commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`). Runtime proof belongs to the pinned full-runtime GitHub Actions GameTests.
