# Discovery journal — server persistence and optional IE consultation

Scope source: [governance PR5](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition_GPT/pull/5),
handoff `IOE-EXPEDITION-DISCOVERY-MANUAL-001`, reconstruction commit
`cd48b5129eae1881db2c8e0677455d58baea164d`. The user approved implementation and
subsequently resumed bounded technical cycles. Governance PR5 was merged on
2026-10-10 at 00:39:30 UTC as `2257fc3db7b296aa055bb1548955767da0446c7f`,
integrating the handoff into governance main. This does not authorize technical PR
merges or modify technical FINAL_DESIGN contracts.

## Server persistence core

The consolidated mod registers a server-start listener that opens world SavedData
`immersive_ore_expedition_discoveries`. `DiscoveryJournalService` is the production
server-thread API for reading the current player's immutable journal and accepting
evidence from future trusted server producers. There is no command granting
discoveries, chunk scan, terrain write or IE dependency in this core.

Records are keyed by player UUID and dimension plus exact site anchor position.
The anchor is the existing locator site position, not a generated ID derived from
quality, resource, source, node list, depletion or reindex metadata. Anchor/province
index aliases at the same physical origin are not separate discoveries. The current
single-site-per-anchor architecture makes this identity sufficient; if later designs
permit co-located independent sites or relocate anchors, define an explicit migration
before enabling them. Existing journal rows are never pruned against the live index.

Player views have a random persisted record UUID and only earned fields. They never
contain the internal anchor key, province, original quality roll, Budding nodes or
ore budgets. At Evidence Discovered, only clue type/dimension/location are exposed.
Site location, resource and quality become available respectively at Site Located,
Resource Identified and Site Surveyed. Expedition Documented adds no unobserved fact.

The storage transition requires the next stage, preserving previous fields and
rejecting repeats, regressions and skipped stages. A future producer with evidence
for several stages can submit each justified step; it must not fabricate prerequisites.
The boolean changed result is a deduplication signal, not a notification implementation.
Repeated evidence does not dirty SavedData. Save/load preserves progress and IDs.
Malformed rows are skipped, first valid duplicate wins, and future-stage fields in
an earlier-stage row are ignored. Unknown format versions retain their raw data,
log a warning and refuse progression or player projections rather than allowing
SavedData fallback to overwrite them; forward migration is not provided in this slice.

## Trust boundary and actual connection

`recordVerifiedEvidence` accepts only the player's current dimension and a playable
natural connected ANCHOR registered at the exact key. Debug/proof, unregistered,
failed and recovered-only targets cannot newly earn evidence via this entry point.
This is placement eligibility, **not proof of observation**. The caller must validate
the actual clue, observation or survey on the server before passing stage-specific
facts. The service does not copy hidden fields from the global locator.

No gameplay evidence producer is connected in this increment. Neither opening the
Compass nor requesting a Jade tooltip automatically grants a discovery. The real
connection in this slice is server lifecycle plus persistent storage/service; tests
exercise that production service with controlled evidence, not natural discovery.
Normal play does not yet populate it; no discovery notifications are implemented.
The optional IE consultation below can display the saved records or an empty state.

## Next bounded increment and decisions

Define the first authoritative discovery event using actual placed-clue evidence.
The approved scope does not yet specify a proximity radius, interaction requirement,
observation validation, valid resource probe, quality survey action or documentation
completion action. Resolve only the trigger needed for that next increment before
wiring it; do not turn locator availability into player knowledge. If live evidence
is lost on reindex, new progression fails closed while the historical record remains.

Default Compass visibility and Jade behavior are unchanged. Narrative IE manual
pages and stage notifications remain separate work. IE is optional;
no native recipe or progression is gated. End parameters and the pending Nether
resource-transition decision remain untouched.

## Validation boundaries

Unit coverage checks all five transitions, NBT roundtrips, player/dimension isolation,
identity stability, duplicate/invalid rows and withheld fields. Hosted GameTest uses
the registered service, real world SavedData disk save/read and fake server players,
checks ineligible targets, hidden fields, duplicate signals and unchanged locator,
then verifies history remains after removing live site-index evidence. It runs in
baseline and complete pinned runtime configurations, including IE absence in baseline.
This is not an actual reconnect, process restart, natural discovery or visual/manual
qualification. CI results must be reported for the exact resulting commit.

## Optional Engineer's Manual consultation increment

On clients with pinned IE 12.4.2-194 present, the load-complete callback adds one
IOE-owned `immersive_ore_expedition:journal` entry under the additive Field Records
category. It uses `ManualHelper.getManual()`, the manual tree and `ManualEntryBuilder`,
with an original functional description and a button opening the IOE journal screen.
It does not replace native entries, recipes, advancements or progression. Registration
checks entry identity to avoid duplicates; supplier-based text follows the current
language on manual reload. English and French functional labels are provided.

The journal screen lists all five approved stages, an empty/loading state, and one
personal record per page, with previous/next/refresh/back controls and scrolling for
small viewports. Only earned fields are rendered; exact registry IDs identify clue
types/resources. This is a functional consultation interface, not the narrative
Field Notes chapter. The original RP source was supplied by the parent during this
cycle and is reserved for a later scoped reconciliation; it is not imported here.

The common network registration has no client or IE linkage. A distribution-gated
reflection bridge loads the generic client; a separate `ModList` check admits the
IE-specific registration only when IE is loaded. Dedicated-server and IE-absent
paths never load the manual adapter. No dependency/version change was needed.

### Private, bounded protocol

- `journal/request` carries only a correlation token and nonnegative page offset.
  The handler obtains `ServerPlayer` exclusively from the connection. There is no
  requested player UUID, coordinate, dimension or discovery-stage write operation.
- `journal/page` returns only that player's count and at most one `DiscoveryView`.
  Oversized offsets clamp to the last personal record; empty journals return offset
  zero. An in-memory per-player row index is rebuilt on load, so lookup does not
  scan/copy the global locator or all journal records per request.
- Identifiers use length-limited UTF encoding (256 characters); conditional stage
  encoding omits unearned location/resource/quality even if incidental view fields
  are populated. No internal anchor, province, nodes or budgets are transmitted.
- Only `PacketDistributor.sendToPlayer` replies to the originating player. The
  Compass snapshot and global site index are not used for consultation.
- The client accepts only its outstanding request token, rejects stale/duplicate
  responses, refreshes the active screen, and clears cached data on close/logout.
  The sequence does not reset on logout, preventing an old response from matching
  a subsequent request. No discovery is granted by opening or refreshing the UI.

### Qualification limits

Codec/unit tests cover page isolation, reload, extreme offsets, invalid bounds,
bounded strings, stage-specific fields and cache/logout ordering. The server GameTest
exercises the same response builder for two players, verifies clamping/privacy and
that consultation changes no discovery. Baseline runtime covers IE absence. Compiled
bytecode checks cover common/generic-client isolation and client registration wiring;
they do not execute the actual client/manual lifecycle or prove visual behavior.

Actual manual opening, language/resource reload, button layout, scrolling and
rendering remain `NOT_PERFORMED` in a Minecraft client. No local launch or installation
is authorized by this implementation. No trigger, Nether or End rule changes.
