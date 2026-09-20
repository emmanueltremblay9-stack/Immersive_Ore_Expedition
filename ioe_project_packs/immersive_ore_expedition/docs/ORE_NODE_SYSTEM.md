# Ore Node and Budding System

> **Status: `FINAL_DESIGN`, implementation acceptance pending.** This path is retained because other project documents link to it. The former structure-only contract and earlier blueprint budgets are obsolete as gameplay design.

The canonical specification is [`BUDDING_FINAL_DECISIONS.md`](BUDDING_FINAL_DECISIONS.md). It defines:

- natural quality weights `DRY/POOR/NORMAL/RICH/MOTHERLODE = 10/25/45/17/3`;
- node counts `0/3/4/5/7`;
- ore counts outside the Budding hearts `0–5/12/20/30/49`;
- initial ranks `DAMAGED/CHIPPED/FLAWED`, with `FLAWLESS` exclusive to Motherlodes;
- one site-level `7.77%` Flawless roll with an absolute maximum of one;
- a `10%` `DRY` reward of one neutral `ae2cs:resonating_seed`;
- four IOE Budding ranks to be pre-registered for each explicitly supported GeOre material;
- AE2-version-matched degradation, restoration and acceleration behavior;
- Jade output that keeps site quality and block rank separate.

## Implementation boundary

The final design does not by itself prove implementation. Until the acceptance checklist in the canonical specification passes:

- current source, recipes, assets and tests remain the authority for what is implemented;
- no obsolete design budget may be presented as the final gameplay target;
- GitHub Actions is the required automated validation surface;
- client, server, world-generation and recipe-viewer behavior remain unverified without separate runtime evidence.

Immersive Engineering finite deposits may coexist with the node system. Their exact coexistence is not redefined by the Budding decision and remains governed by [`IMMERSIVE_ENGINEERING_RESOURCE_INTEGRATION.md`](IMMERSIVE_ENGINEERING_RESOURCE_INTEGRATION.md).
