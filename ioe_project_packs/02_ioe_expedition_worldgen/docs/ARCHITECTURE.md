# Architecture — Immersive Ore Expedition: Worldgen

## Module ID
`ioe_expedition_worldgen`

## Java package
`com.oblixorprime.ioe.worldgen`

## Dependencies
[
  {
    "modid": "ioe_core",
    "required": true,
    "range": "[0.1.0,)"
  }
]

## Contracts
This module should use IOE Core for shared data models and resource policy wherever possible.

## Data-driven design
Prefer config, tags, and data-driven rules over hardcoded resource IDs.
