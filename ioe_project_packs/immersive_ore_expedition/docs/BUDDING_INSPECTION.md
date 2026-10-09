# Budding inspection and family activation

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

The bounded administrative locator reindex recognizes all registered IOE GeOre ranks
and native Certus hearts, while preserving recognition of legacy growth blocks. It
still requires the existing shaft/surface mine signature, scans only loaded chunks,
and changes no blocks. Recovery restores an anchor, not original node provenance:
it never reconstructs `budding_nodes` or Jade node indices/budgets from nearby hearts.

Tests cover save/load, old saves, invalid data, dimension separation, replacement/removal,
real production commit and IE fallback metadata. With Jade loaded, a GameTest verifies
provider discovery and the actual server-data and tooltip callbacks for Flawed, Chipped
and Flawless. This is API/runtime coverage, not a client screenshot or visual acceptance.

## Native Certus

The IOE Jade provider handles both IOE GeOre and exact native AE2 Certus rank IDs.
It registers against the common block API and ignores unrelated blocks, including
Entro. `BuddingBlockIdentity` supplies the current rank and family; the native AE2
blocks retain their own behavior. `BuddingPlanMetadata` captures committed Certus
hearts using the same optional save field and exact-position lookup.

Certus displays family AE2, live rank, original site quality, node index/count and
initial surrounding quartz quantity. The Flawless indicator appears only with
proven Motherlode metadata. A native meteorite or manual heart has no invented IOE
site quality. The runtime checks exercise all four native ranks, persisted
metadata and removal of provenance after replacement. These callbacks do not
prove client rendering; graphical Minecraft acceptance remains unproven.
See [Certus decisions and validation scope](CERTUS_INTEGRATION_BLOCKERS.md).

## Other GeOre families

The pinned GeOre 6.2.2 JAR contains the budding model/texture, growth-stage blockstates
and resource-block blockstate for all thirteen existing IOE GeOre profile names:
aluminum, coal, copper, diamond, emerald, gold, iron, lapis, lead, nickel, redstone,
silver and uranium. All thirteen corresponding IOE datapack profile files exist.
This asset/profile inventory alone is not a claim of functional rank support.

The approved exhausted targets are ordinary Minecraft storage for eight families and
IE storage for aluminum, lead, nickel, silver and uranium. All thirteen GeOre families
now share the rank implementation; the five IE families pre-register only with IE present.
Before generation/growth, storage, pocket material and compatible growth blocks must all
exist. No fallback to a different storage material is performed. Jade matches the actual
block's family identity to the stored node, for every available family.

The approved DRY count is uniform 0–5 for the whole pocket. GeOre profiles use their
existing `geore:<material>_block` pocket material, not their exhausted storage target.
See `BUDDING_FAMILY_IMPLEMENTATION.md` for scope, tests and remaining special-profile limits.
