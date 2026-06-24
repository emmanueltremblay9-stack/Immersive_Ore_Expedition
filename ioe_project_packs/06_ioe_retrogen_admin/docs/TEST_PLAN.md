# Test Plan — Immersive Ore Expedition: Retrogen & Admin

## Static checks
- Validate mod ID, package name, and config keys.
- Confirm optional dependency gates.
- Confirm excluded resources are absent.
- `0.1.1-alpha` adds unit coverage for mode parsing, config fallback behavior, marker version semantics, resource diagnostics, and safe queue filtering.

## Runtime smoke checks
- Launch dev client/server.
- Confirm mod loads.
- Confirm config file is generated or read.
- Confirm missing optional mods do not crash.
- Pending: live client/server smoke has not been run for this module.

## Gameplay checks
Use module-specific debug commands or logs to prove generation rules.

## Regression checks
- No duplicate generation.
- No fake resource fallback.
- No branch-mining bypass introduced by this module.
- `0.1.1-alpha` proves duplicate chunk queueing is skipped by marker/queue checks before runtime retrogen is allowed to mutate chunks.
