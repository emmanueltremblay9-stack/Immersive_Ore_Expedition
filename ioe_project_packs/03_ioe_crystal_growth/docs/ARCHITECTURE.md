# Architecture — Immersive Ore Expedition: Crystal Growth

## Module ID
`ioe_crystal_growth`

## Java package
`com.oblixorprime.ioe.crystalgrowth`

## Dependencies
[
  {
    "modid": "ioe_core",
    "required": true,
    "range": "[0.1.0,)"
  },
  {
    "modid": "ae2",
    "required": false
  },
  {
    "modid": "geore",
    "required": false
  }
]

## Contracts
This module should use IOE Core for shared data models and resource policy wherever possible.

## Data-driven design
Prefer config, tags, and data-driven rules over hardcoded resource IDs.
