# PR63 regression and resource validation

This checkpoint extends `6d84b8401851c47f7f1f1e2aa4ece2401996b1a5`.
It is development evidence, not approval of IOE 1.0.0 or a client playthrough.
Historical checkpoint: storage outputs and DRY count were pending when this regression
fix was made. The user subsequently approved ordinary Minecraft/IE storage and uniform
0–5 DRY counts on 2026-10-09; see `BUDDING_FAMILY_IMPLEMENTATION.md` for that implementation.

## Demonstrated rollback defect and correction

Removing the transaction's chest used vanilla container removal, which unpacked and
scattered its loot. New GameTests failed for both an unopened DRY chest and one whose
loot was already generated. A retry could therefore leave rewards from failed sites.

Compensation now clears the loot table and inventory of the plan's own loot container
before restoring its prior block. It still checks that the current state matches the
planned target. Placement already rejects pre-existing block entities; this does not
clear pre-existing player containers. Accepted transactions remain untouched.

`DrySiteRewardGameTests` checks unopened rollback, generated loot with two placement/
rollback attempts and repeated compensation, plus accepted loot through container NBT
reload before first extraction and after extraction. The last case verifies exactly
one `ae2cs:resonating_seed`, then zero; accepted rollback must preserve the container.
These tests run synchronously on the server; they do not simulate a player extracting
items from an uncommitted transaction in a modified asynchronous placement pipeline.

## Jade persistence proof

The existing Jade runtime check now saves the real overworld locator data file, waits
for NeoForge's asynchronous I/O worker, reads it back through `DimensionDataStorage`,
deserializes it with the production factory, and replaces the cached SavedData object.
It verifies index contents before invoking the registered Jade server-data and tooltip
providers for live Flawed, Chipped and Flawless ranks with Motherlode site metadata.
The same disk round trip occurs after removing the node, verifying that the tooltip
cannot regain the removed metadata. The optional runtime is Jade 15.10.6.

This is a real server file round trip and provider callback test. It is not a server
process restart, chunk reload, client packet transport test or observed Jade overlay.
The DRY container reload proof uses block-entity NBT serialization in the running world;
it does not claim a full world restart either.

## Rank resource specification

Minecraft 1.21.1 block models keep a full cube and the installed GeOre Iron texture.
Original model geometry adds dark, pixel-aligned fractures using vanilla black concrete;
no third-party image is copied or edited. Wear is cumulative on all six faces:

| Rank | Fracture rectangles per face | Covered pixel area per face |
| --- | ---: | ---: |
| Flawless | 0 | 0 / 256 |
| Flawed | 4 | 9 / 256 |
| Chipped | 8 | 18 / 256 |
| Damaged | 12 | 26 / 256 |

The central Iron motif stays unobstructed. Planes sit 0.01 model units outside the
base cube to avoid exact coplanarity, retain face culling and use no special renderer.
Collision, growth, loot, recipes, inventory bindings and translated names are unchanged.
This distinction uses shape/contrast rather than color alone. Readability, flicker at
viewing distances and compatibility with shader/resource packs need client observation.

Files changed for resources:

- `src/main/resources/assets/immersive_ore_expedition/models/block/{flawless,flawed,chipped,damaged}_budding_iron.json`
- `scripts/validate_budding_models.py`
- `scripts/validate_worldgen_assets.py` (invokes the model validator in the existing CI step)
- `docs/IRON_BUDDING_IMPLEMENTATION.md` and this validation record

Implementation order: reproduce compensation failure; fix only transaction-owned loot;
exercise Jade disk reload; add cumulative rank geometry; resolve resource references;
run JUnit, server GameTests, JAR inspection, then the existing exact-head CI suite.

## Reproducible checks and current client limit

Run `python3 scripts/validate_worldgen_assets.py` for the existing resource suite.
For external references, run `python3 scripts/validate_budding_models.py --jar <Minecraft-1.21.1-client.jar>
--jar <GeOre-6.2.2.jar>`. This checks four blockstate/item/model bindings, English/French
names, geometry/UV/culling, distinct cumulative wear and parent/texture references
against actual dependency archives. It does not bake models or render Minecraft.

Run `test runGameTestServer jar` with Java 21, Gradle 8.8 and optional Jade runtime
(`-PioeIncludeJadeRuntime=true`). The cloud cache requires the existing temporary Jade
artifact substitution script for offline runs; repository dependency pins are unchanged.
The existing full-runtime CI additionally exercises IE/IP and the pinned integration pack.
Baseline tests conditionally succeed when optional integrations are absent; baseline
success alone does not prove those integrations.

The cloud session has neither `DISPLAY` nor `WAYLAND_DISPLAY`, and no `Xvfb` or
`xvfb-run`. No graphical client was launched and no screenshot is claimed.
Minimum remaining means: a Minecraft 1.21.1/NeoForge 21.1.230 graphical OpenGL client
(or a provisioned virtual display with compatible rendering and capture), the exact
candidate JAR and pinned dependencies, and an authorized test world. No personal PC,
Prism installation or environment/security change was performed.

Client acceptance procedure: place all four Iron ranks side by side, inspect all faces
and inventory icons at near/far distances and in ordinary lighting; inspect a natural
committed node with Jade; save/exit/reopen and compare rank, quality, node count and
initial ore; break/replace it and verify site metadata disappears. Exercise seed-to-
Damaged acquisition, charged-Certus restoration up to Flawed, growth/degradation,
Flawless loot, and DRY extraction after reopening. Record candidate hash, dependency
versions and screenshots/results. Existing server differential, loot, recipe and
restoration GameTests cover parts of this progression, not this complete player path.
