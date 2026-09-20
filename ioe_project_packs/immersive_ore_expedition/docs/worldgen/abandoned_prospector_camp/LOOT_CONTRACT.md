# Abandoned Prospector Camp Loot Contract

Loot is sparse archaeological utility, never a second mineral or industrial reward path.

## Container limits

| Quality | Hard maximum | Implemented state reductions |
|---|---:|---|
| DRY | 0 | All eligible states remain 0. |
| POOR | 1 | `FAILED_PROSPECTION` and `COLLAPSED` use 0. |
| NORMAL | 1 | `EVACUATED` and `COLLAPSED` use 0. |
| RICH | 2 | This pass deliberately uses at most 1; all states are below the hard maximum. |
| MOTHERLODE | 2 | This pass deliberately uses at most 1; all states are below the hard maximum. |

`EVACUATED` RICH/MOTHERLODE camps use the one-roll sparse table. `FAILED_PROSPECTION` never receives a
container. A single retained container also enforces the site-wide maximum of one map and one compass.

## Allowlist

Only these vanilla entries are permitted:

```text
minecraft:torch
minecraft:charcoal
minecraft:bread
minecraft:paper
minecraft:string
minecraft:feather
minecraft:wooden_pickaxe
minecraft:map
minecraft:compass
```

Wooden pickaxes use `minecraft:set_damage` with at most 20% remaining durability. `minecraft:filled_map`,
exploration-map functions, the IOE Expedition Compass, minerals, ores, crystals, industrial components, Domum
items, MineColonies items, and Structurize items are forbidden.

## Navigation-item probabilities

- Empty map: one independent one-roll `random_chance` pool at exactly 8%; NORMAL/RICH/MOTHERLODE only.
- Vanilla compass: one independent one-roll `random_chance` pool at exactly 2%; RICH/MOTHERLODE only.
- POOR and sparse/evacuated tables contain neither item.

Because an abandoned camp retains at most one loot container, effective per-eligible-container and per-site
probabilities are both bounded at 8% for a map and 2% for a compass.
