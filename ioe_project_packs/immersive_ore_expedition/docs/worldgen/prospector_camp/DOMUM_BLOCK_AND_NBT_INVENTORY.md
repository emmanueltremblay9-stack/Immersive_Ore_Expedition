# Domum Ornamentum Block and Material Inventory

## Version evidence

- Target mod ID: `domum_ornamentum`
- Target artifact version: `1.0.231`
- Minecraft release tag: `v1.21.1-1.0.231`
- Audited upstream commit: `af1106ab66fafea7427ed23dec73776a4c8d7748`
- Official Maven metadata: `com.ldtteam:domum-ornamentum:1.0.231`
- API artifact SHA-256: `e5845bb264cbe03f3914c98e514056183fef564bd9f4c683b20602cf74766d92`
- Runtime `main` artifact SHA-256: `04c0c902bdbcbd48e38bee5a323907ae0b7b7db4ff4a3e4da7c45334b65610a1`
- Runtime `main` artifact SHA-512: `483061574a8452ce6b5b25779431540fc4e444123a987633546b290e9beb03ce094d0bdb80f28fb1dbc2317fd91d1dfbde1c4c0619dbf124768744cb8bc6ea35`
- Tagged source license and runtime-mod metadata: `GPL-3.0`
- Published Maven POM license declaration: `LGPL-3.0`

The source evidence is the tagged [LDTTeam Domum-Ornamentum repository](https://github.com/ldtteam/Domum-Ornamentum/tree/v1.21.1-1.0.231)
and its [official Maven repository](https://ldtteam.jfrog.io/ldtteam/modding/com/ldtteam/domum-ornamentum/1.0.231/).
IOE copies no Domum source or assets in this pass. The exact release's tagged source and embedded mod metadata say
GPL-3.0, while its published Maven POM says LGPL-3.0. The discrepancy is `AMBIGUOUS / UNRESOLVED`, with
`DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`; neither GPL nor LGPL is claimed as legally controlling.
The owner distribution decision `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`
is recorded in `../../DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md`. Legal review remains `NOT_PERFORMED`,
and `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`. This permits only optional, separately distributed, unembedded
compile/runtime linkage with no copied Domum source or assets; any boundary change reopens review. CI retrieval for
validation is not treated here as redistribution approval.

## Retained components

### Post

```text
DISPLAY_PURPOSE: temporary vertical shelter support
REGISTRY_ID: domum_ornamentum:post
BLOCK_ENTITY_TYPE: domum_ornamentum:materially_retexturable
REQUIRED_BLOCK_STATE: default post state transformed through Minecraft BlockState.rotate; IOE uses it vertically
REQUIRED_NBT: no hand-authored NBT; the typed API owns the textureData codec payload
PRIMARY_MATERIAL_ID: minecraft:block/oak_planks -> the selected biome-family plank block
SECONDARY_MATERIAL_ID: NOT_APPLICABLE; the audited post exposes only the primary textured component used by IOE
ROTATION_BEHAVIOR: AbstractPostBlock.rotate transforms FACING; IOE applies the same deterministic quarter-turn to state and position
MIRROR_BEHAVIOR: no dedicated post mirror override was found in the audited source; IOE never requests mirroring
DROPPED_ITEM_BEHAVIOR: generated post loot copies domum_ornamentum:texture_data and type to the dropped item
MISSING_MATERIAL_BEHAVIOR: the adapter rejects an unresolved Registry ID and compensated placement rolls the plan back; no random or vanilla material is substituted
SOURCE_EVIDENCE: tag v1.21.1-1.0.231 commit af1106ab66fafea7427ed23dec73776a4c8d7748; ModBlocks, PostBlock, AbstractPostBlock, MateriallyTexturedBlockEntity, and generated loot_table/blocks/post.json
```

### Framed timber

```text
DISPLAY_PURPOSE: simple shelter infill and visible timber-frame accent
REGISTRY_ID: domum_ornamentum:framed
BLOCK_ENTITY_TYPE: domum_ornamentum:materially_retexturable
REQUIRED_BLOCK_STATE: default framed state transformed through Minecraft BlockState.rotate
REQUIRED_NBT: no hand-authored NBT; the typed API owns the textureData codec payload
PRIMARY_MATERIAL_ID: minecraft:block/oak_planks -> the selected biome-family plank block
SECONDARY_MATERIAL_ID: minecraft:block/dark_oak_planks -> the selected biome-family host-rock block
ROTATION_BEHAVIOR: TimberFrameBlock.rotate transforms FACING; IOE applies the same deterministic quarter-turn to state and position
MIRROR_BEHAVIOR: TimberFrameBlock.mirror transforms FACING, but IOE's programmatic camp path does not request mirrors
DROPPED_ITEM_BEHAVIOR: generated framed loot copies domum_ornamentum:texture_data to the dropped item
MISSING_MATERIAL_BEHAVIOR: the adapter rejects an unresolved Registry ID and compensated placement rolls the plan back; no random or vanilla material is substituted
SOURCE_EVIDENCE: tag v1.21.1-1.0.231 commit af1106ab66fafea7427ed23dec73776a4c8d7748; ModBlocks, TimberFrameBlock, MateriallyTexturedBlockEntity, and generated loot_table/blocks/framed.json
```

No dynamic timber frame, shingle, panel, pillar, stair, slab, Architect's Cutter, decorative barrel, door, trapdoor,
MineColonies asset, or Structurize template is used in this pass.

## Payload and persistence strategy

IOE does not hand-author Domum SNBT. `ExpeditionBlockEntityPayload` carries a typed map of component registry ID to
material-block registry ID. After the existing compensated placement writes the block state, the guarded adapter:

1. obtains the newly created block entity;
2. requires `IMateriallyTexturedBlockEntity`;
3. resolves every material through `BuiltInRegistries.BLOCK`;
4. builds `MaterialTextureData`;
5. calls `updateTextureDataWith`;
6. reads the material map back immediately;
7. fails the whole placement and triggers rollback if any step disagrees.

Upstream `MateriallyTexturedBlockEntity.saveAdditional` serializes that API-owned data under `textureData` through
`MaterialTextureData.CODEC`; `loadAdditional` decodes through the same codec. Empty material data is deliberately
never submitted because upstream replaces it with random material choices, which would violate IOE determinism.

## Rotation and fallback

Layout positions and block states use the same one of four deterministic Minecraft rotations. Domum's
`TimberFrameBlock.rotate` transforms its `FACING` property; the material payload is component-keyed and independent
of direction. `post` is used as a vertical support. The hosted test sources cover the complete 5x8 quality/family
planning matrix. The full-runtime test then applies all four rotations through `IoeExpeditionPlanPlacement`, reads
back every material BE, performs an NBT serialization round trip, and verifies material-bearing recovered items.
Mirroring is not a supported operation in the current programmatic camp composer, so no mirror branch is claimed.

When Domum is absent or either retained Registry ID is unavailable, the composer selects only vanilla block states
and emits no material payload. The optional adapter is not invoked.

## Block-entity and progression budget

| Quality | Maximum Domum material BEs | Loot containers |
|---|---:|---:|
| DRY | 0 | 0 |
| POOR | 7 | 1 |
| NORMAL | 12 | 2 |
| RICH | 18 | 2 |
| MOTHERLODE | 24 | 2 |

Domum accents are limited to shelter supports, front framing, and selected intact roof cells. Host rock, paths, work
surfaces, hearths, markers, shaft hatches, observation decks, and loot remain vanilla. The material-share denominator
is the nominal architectural shelter shell: one floor/deck layer, unique post cells, the front beam excluding post
overlap, one roof layer, and the family-specific windbreak, shade, lattice, anchor, or gear-plinth geometry. The DRY
raised variant subtracts its intentional one-cell beam/roof opening from this denominator; productive qualities are
unchanged.
Geological rock, vegetation, paths, explicit air, and below-deck foundation are excluded because they are not
architectural shell materials.

| Quality | Lowest family share | Highest family share |
|---|---:|---:|
| POOR | conifer 7/34 = 20.59% | wetland 7/27 = 25.93% |
| NORMAL | conifer 12/50 = 24.00% | wetland 12/41 = 29.27% |
| RICH | conifer 18/75 = 24.00% | wetland 18/64 = 28.13% |
| MOTHERLODE | conifer 24/104 = 23.08% | wetland 24/91 = 26.37% |

This satisfies the prompt's provisional 20-35% Domum architectural target while retaining a maximum of 24 material
block entities (27 total BEs including the hearth/containers) in the largest camp. The complete static report records
both the shell denominator and percentage for every quality/family row.

## Evidence status

- Registry IDs, component IDs, API methods, codec path, save key, rotation methods, mirror method for `framed`, and
  generated loot component-copy rules: `CONFIRMED` by exact tagged source inspection.
- Maven API/runtime artifacts and hashes: `CONFIRMED` by official repository readback.
- Conflicting GPL-3.0/LGPL-3.0 upstream license declarations: `AMBIGUOUS / UNRESOLVED`; factual status: `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`.
- Owner distribution disposition: `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`, recorded in `../../DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md`; legal review: `NOT_PERFORMED`; `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`; neither GPL nor LGPL is claimed as legally controlling.
- IOE typed payload and immediate material readback path: `CONFIRMED` by source inspection and static validator.
- Compilation against the API artifact: `NOT_PERFORMED` locally by repository policy.
- Four applied rotations, immediate material readback, NBT round trip, and recovered-item material checks: test source
  `CONFIRMED`; execution `NOT_PERFORMED`.
- Actual world save/reload material persistence: `NOT_PERFORMED`.
- Client rendering and missing-texture check: `NOT_PERFORMED`.
- Survival execution of the source-defined drops and progression impact in runtime: `NOT_PERFORMED`.
