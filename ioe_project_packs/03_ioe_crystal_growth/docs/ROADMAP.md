# Roadmap — IOE Crystal Growth

## Milestone A
- Implement generic CrystalGrowthSiteProvider. `0.1.1-alpha` foundation complete.
- Add vanilla amethyst anchored site. `0.1.1-alpha` includes deterministic site plans only; real configured-feature placement remains pending.

## Milestone B
- Add AE2 buried Certus site. `0.1.1-alpha` includes optional AE2-gated site plans from supplied loaded resources only.
- Add sky-stone crust around geode shell. `0.1.1-alpha` requires a supplied loaded sky-stone resource before planning a meteoritic variant.

## Milestone C
- Add optional GeOre provider if GeOre can be controlled safely. `0.1.1-alpha` includes policy-only GeOre planning and no hard dependency.
- Ensure free/random GeOre generation is disabled by pack config or compat hook. `0.1.1-alpha` exposes a policy hook; concrete GeOre API/config mutation remains pending verification.
