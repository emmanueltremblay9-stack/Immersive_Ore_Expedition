# Resource Policy

## Goal
Prevent speculative resources and keep Immersive Ore Expedition tied to the actual loaded modpack.

## Rules
- Every ore/resource must resolve from a loaded registry entry or accepted tag.
- If a config references a missing tag/block/fluid, skip it and log a clear warning.
- Do not register new ore blocks in these modules.
- Do not generate GeOre variants for resources that do not exist in the pack.
- Do not generate fake Fluix ore.
- Do not include removed resources: Apatite, Tin, Forestry Copper, Platinum, Osmium, Tungsten, Black Quartz, Uraninite, Monazite.

## Runtime validation
Codex must implement or preserve validation that checks:
- block registry presence
- fluid registry presence for IP clues
- tag resolution
- optional mod loaded checks
- config entry safety
