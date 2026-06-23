# Test Plan — Immersive Ore Expedition: Retrogen & Admin

## Static checks
- Validate mod ID, package name, and config keys.
- Confirm optional dependency gates.
- Confirm excluded resources are absent.

## Runtime smoke checks
- Launch dev client/server.
- Confirm mod loads.
- Confirm config file is generated or read.
- Confirm missing optional mods do not crash.

## Gameplay checks
Use module-specific debug commands or logs to prove generation rules.

## Regression checks
- No duplicate generation.
- No fake resource fallback.
- No branch-mining bypass introduced by this module.
