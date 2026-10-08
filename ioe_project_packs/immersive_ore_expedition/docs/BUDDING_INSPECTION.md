# Budding inspection and remaining family decisions

## Jade and persistence

Optional integration targets Jade 15.10.6 for NeoForge 1.21.1, Modrinth version `eYz2YBGT`.
The build uses its compile-only API; the full CI runtime pins its published SHA-512.
The API was checked against the actual JAR and the upstream
[1.21 NeoForge API](https://github.com/Snownee/Jade/tree/6701b1eea41143b2e4713ed9c6e7d57e2595a820/src/main/java/snownee/jade/api).
Jade is neither bundled nor required to load IOE.

The locator stores immutable original node positions, index/count, initial ore quantity
and family as an optional `budding_nodes` list in the existing site save. Metadata is
recorded together with the final site, after deposit commit/fallback, inside the existing
compensation boundary. Rejected provisional plans have no node entries. The original
rank is deliberately not stored: Jade reads the current block rank after growth decay.
Lookup is indexed by dimension and exact block position, not proximity to an anchor.

Old version-2 saves without this optional field remain playable and expose no invented
node identity. Malformed node entries are ignored. Player break/replacement removes the
node identity; a hand-placed block shows its rank and family without claiming a generated
site quality. Clearing/replacing a site also updates the node lookup.

Tests cover save/load, old saves, invalid data, dimension separation, replacement/removal,
real production commit and IE fallback metadata. With Jade loaded, a GameTest verifies
provider discovery and the actual server-data and tooltip callbacks for Flawed, Chipped
and Flawless. This is API/runtime coverage, not a client screenshot or visual acceptance.

## Other GeOre families

The pinned GeOre 6.2.2 JAR contains the budding model/texture, growth-stage blockstates
and resource-block blockstate for all thirteen existing IOE GeOre profile names:
aluminum, coal, copper, diamond, emerald, gold, iron, lapis, lead, nickel, redstone,
silver and uranium. All thirteen corresponding IOE datapack profile files exist.
This asset/profile inventory alone is not a claim of functional rank support.

Iron's exhausted target is explicitly `minecraft:iron_block`. For the remaining twelve
families the current family descriptor names `geore:<material>_block`; GeOre tags these
as `geore:storage_blocks/geore_<material>`, separately from ordinary mineral storage tags.
The canonical document names the corresponding mineral storage block but gives only the
Iron concrete target. Choosing ordinary Minecraft/IE storage versus GeOre storage changes
the actual output and the acquisition/depletion loop. That choice must be resolved before
registering the remaining functional families and finalizing their recipes/loot.

DRY residual count likewise remains an explicit 0–5 planner parameter. Its distribution
is unspecified; no worldgen distribution is introduced by the neutral-seed reward work.
