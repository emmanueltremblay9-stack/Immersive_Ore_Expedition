# ExtendedAE Entroized Fluix integration

## Current rule

In an Entroized Fluix profile biome, a productive expedition site places one bounded chamber geode using ExtendedAE's registered fully budding Entroized block and AE2 Fluix shell. The formation is part of the same compensated site transaction as the connected mine and finite IE deposit.

Entroized Fluix is integrated through the extremely rare `immersive_ore_expedition:mineral/entroized_fluix` Immersive Engineering mineral mix. Its verified rare output is `extendedae:entro_crystal`; the remaining outputs are compatible host materials. The mix uses the exact six-biome allowlist, low selection weight, high sterile-extraction chance, and limited reserve tiers documented in the biome distribution report.

ExtendedAE retains all normal crafting, machine, seed, budding, degradation, repair, and automation methods. The new-chunk ore guard does not remove ExtendedAE budding, bud, or cluster blocks. The existing `ae2:growth_acceleratable` compatibility overlay exposes the upstream-owned budding block to AE2 acceleration; the connected expedition transaction is its bounded IOE acquisition path.

## Ownership and proof boundary

- ExtendedAE owns `extendedae:entro_crystal`, its blocks, recipes, machines, behavior, code, and assets.
- Immersive Engineering owns mineral extraction and depletion.
- IOE owns the mineral-mix recipe, biome allowlist, reserve tier, and connected site/deposit transaction.
- No ExtendedAE source or asset is embedded in IOE.

The inspected line is ExtendedAE `2.2.33`, source tag `1.21-2.2.33-neoforge`, revision `90005ee29839fb9fa83bbe6544919c722f8b0dc6`, LGPL-3.0. Runtime proof belongs to the pinned full-runtime GitHub Actions GameTests.
