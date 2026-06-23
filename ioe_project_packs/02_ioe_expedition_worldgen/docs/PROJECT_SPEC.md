# Project Spec — IOE Worldgen

## Responsibility
This module owns the main anti-branch-mining gameplay loop.

## Must implement
- Random ore suppression hooks/config strategy.
- Biome province rules.
- Surface clue structures: tiny vertical mine entrance, collapsed shaft, miner camp, survey marker.
- Underground mineshaft anchors and ore-load chambers.
- Site quality roll: dry, poor, normal, rich, motherlode.
- Ore loads must be near/connected to expedition anchors.

## Must not implement
- No GeOre-specific code.
- No AE2-specific code.
- No IE/IP-specific clue logic.
- No retrogen beyond hooks for future retrogen module.

## Design rule
The player should find resources by following clues and mine networks, not by branch-mining at a universal Y-level.
