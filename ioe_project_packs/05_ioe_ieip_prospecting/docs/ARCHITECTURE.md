# Architecture — Immersive Ore Expedition: IE/IP Prospecting

## Module ID
`ioe_ieip_prospecting`

## Java package
`com.oblixorprime.ioe.ieip`

## Dependencies
[
  {
    "modid": "ioe_core",
    "required": true,
    "range": "[0.1.0,)"
  },
  {
    "modid": "immersiveengineering",
    "required": false
  },
  {
    "modid": "immersivepetroleum",
    "required": false
  }
]

## Contracts
This module should use IOE Core for shared data models and resource policy wherever possible.

## Data-driven design
Prefer config, tags, and data-driven rules over hardcoded resource IDs.
