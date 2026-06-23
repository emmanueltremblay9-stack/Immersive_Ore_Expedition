# Immersive Ore Expedition — Multi-Mod Production Split

## Goal
Split Immersive Ore Expedition into smaller independent NeoForge 1.21.1 projects so each can be developed, tested, and shipped separately.

## Recommended development order
1. `ioe_core`
2. `ioe_expedition_worldgen`
3. `ioe_ieip_prospecting`
4. `ioe_crystal_growth`
5. `ioe_nether_geodes`
6. `ioe_retrogen_admin`

## Dependency map
```text
ioe_core
├── ioe_expedition_worldgen
├── ioe_crystal_growth
├── ioe_nether_geodes
├── ioe_ieip_prospecting
└── ioe_retrogen_admin
    └── optional link to ioe_expedition_worldgen
```

## Scope correction
This split intentionally does not integrate Immersive Petro-Machinery, MineColonies, or CIVITAS.

## Production strategy
- Keep each module compileable alone where possible.
- Use optional dependencies for AE2, GeOre, IE, and IP.
- Keep all shared data contracts in IOE Core.
- Build the core gameplay before compat/retrogen.
