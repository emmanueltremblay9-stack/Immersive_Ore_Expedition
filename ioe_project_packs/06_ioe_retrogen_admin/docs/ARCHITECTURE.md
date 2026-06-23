# Architecture — Immersive Ore Expedition: Retrogen & Admin

## Module ID
`ioe_retrogen_admin`

## Java package
`com.oblixorprime.ioe.retrogen`

## Dependencies
[
  {
    "modid": "ioe_core",
    "required": true,
    "range": "[0.1.0,)"
  },
  {
    "modid": "ioe_expedition_worldgen",
    "required": false,
    "range": "[0.1.0,)"
  }
]

## Contracts
This module should use IOE Core for shared data models and resource policy wherever possible.

## Data-driven design
Prefer config, tags, and data-driven rules over hardcoded resource IDs.
