# Project Spec — IOE Retrogen & Admin

## Responsibility
This module adds safe administrative tools and retrogen workflows after the core worldgen is stable.

## Must implement
- Chunk version markers.
- Retrogen modes: off, unexplored chunks only, admin radius, ore pocket only, clue + pocket.
- Commands for locate/status/start/pause/radius.
- Diagnostics for province/resource/site validation.

## Must not implement
- No automatic destructive retrogen by default.
- No full old-chunk structure rebuild unless admin explicitly requests it.
- No repeated generation in already marked chunks.

## Design rule
Retrogen is late-phase and conservative.
