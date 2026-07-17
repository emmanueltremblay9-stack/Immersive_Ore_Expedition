# Multi-seed candidate placement model

Classification: `SUPPORTED_INFERENCE`.

This models the four configured rarity filters over 10,000 chunks per seed. It is an upper-envelope
for candidate positions only; biome selection, terrain rejection, collisions, site transactions, Core
Sample visibility and Excavator extraction still require the GitHub-hosted full-runtime GameTests.

| Seed | Candidates / 10k chunks | P50 | P90 | P99 |
|---:|---:|---:|---:|---:|
| 104729 | 346 | 41.98 | 76.22 | 109.07 |
| 130363 | 349 | 39.2 | 77.83 | 111.77 |
| 155921 | 331 | 39.0 | 77.47 | 114.14 |
| 181081 | 365 | 43.08 | 75.6 | 106.23 |
| 206369 | 395 | 38.59 | 71.84 | 103.23 |
| 232003 | 358 | 38.48 | 76.56 | 113.07 |
| 257591 | 382 | 38.01 | 71.59 | 98.31 |
| 283009 | 363 | 37.05 | 76.24 | 111.07 |
| 308411 | 330 | 42.01 | 74.73 | 121.08 |
| 334021 | 411 | 34.83 | 69.46 | 93.34 |

Runtime validation: `NOT_PERFORMED` until GitHub Actions runs.
