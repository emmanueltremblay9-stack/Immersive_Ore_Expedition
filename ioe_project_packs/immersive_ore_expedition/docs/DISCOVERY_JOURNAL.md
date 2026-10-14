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
evidence from trusted server producers. The visible-proximity producer below now
records the first two stages. There is no grant command, terrain write or IE dependency
in the persistence core.

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

## Visible-proximity discovery — bounded production increment

The owner resolved the first trigger in the live voice conversation: “à proximité”,
“non pas un outil”, a private chat notice on discovering an actual nearby clue,
then more information in the manual. This decision was supplied by the parent on
2026-10-10 and authorizes this production connection. The merged handoff does not
set radius/visibility constants; the current mandate permits a conservative default.

`ProximityDiscovery` is registered on server post-ticks. No tool or interaction is
required. Survival/creative living players, excluding spectators, first earn
`EVIDENCE_DISCOVERED`; after that, verified exterior entrance observation can earn
`SITE_LOCATED` as specified below. Resource identification, quality surveying and
expedition completion producers remain unimplemented.

Chosen defaults and limits:
- At most eight player slots processed per server tick, round-robin; each player is
  scanned at most once per 20 ticks. Large populations may wait longer. Session
  scheduling is memory-only and cleared on server stop; journal history is durable.
- The existing locator has a derived nine-chunk lookup, capped at sixteen natural
  anchors per bucket (144 candidates/player scan). Exact identity eligibility is an
  O(1) lookup. Both derived indexes rebuild through the normal SavedData loader.
  No full index/terrain scan, chunk ticket or forced load. Extremely dense imported
  buckets can omit later candidates rather than exceed the cap.
- Eye-to-witness block-center distance at most eight blocks. The player must face
  within sixty degrees of the witness collision-shape center (dot product >= 0.5).
  A server collision/fluid ray must hit that witness first. Intervening solid
  blocks or fluids reject observation; this is not through-wall detection.
- All chunks along the short ray and structural-neighbor reads must already be
  loaded. No night/light-level requirement is introduced. This geometric visibility
  check does not prove an actual rendered pixel or human attention.

Only playable natural connected ANCHOR records qualify, excluding debug/proof,
unregistered and recovered-only sites. Index presence alone cannot grant discovery:
recognizable blocks must survive at the source blueprint's surface offsets:
entrance lantern on oak planks; collapsed-shaft oak beam over stripped-oak support;
survey-marker wall over chiseled stone bricks; camp oak hatch next to planks.
A destroyed, changed or obstructed witness fails closed. Modified/historical layouts
without these witnesses may remain undiscovered. Blocks reconstructed identically
at an already legitimate site are not distinguishable from surviving original blocks;
no player-build ownership claim is made. No generic lantern/hatch outside a confirmed
site is accepted. Reindex-only provenance does not invent natural-placement evidence.

The saved first-stage record is the deduplication authority. The record advances
before a private `sendSystemMessage` notice; repeated ticks, revisits and ordinary
save/reload cannot notify again. Notification text contains no coordinates, resource,
quality or hidden stage. The journal stores only the observed witness location/type
and dimension. Existing later-stage history is never regressed. As with the existing
SavedData contract, an OS crash before the next world save is not durable delivery
proof; no cross-file/transport exactly-once guarantee is claimed.

The localized EN/FR notice links to the personal IOE journal through `/ioejournal`,
a client command registered whenever IOE is present, independent of IE. With IE,
the notice also directs the player to the Engineer's Manual; without IE, it directs
only to the usable standalone journal. This command opens consultation only and
uses the existing private paginated protocol; it never grants evidence. Client-only
registration stays behind the existing distribution gate. No new item or dependency.

Compass visibility, Jade, native progression, Nether/End generation and resource
contracts are unchanged. Manual narrative pages contain nine static entries.

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
is authorized by this implementation. No Nether or End rule changes. The first-stage trigger is described above.

### Proximity-specific qualification

A hosted GameTest exercises the production scan with fake server players and actual
blocks: unknown/debug records, facing, wall, range and destroyed-witness rejection;
private independent first-stage records; repeated scans; real SavedData disk reload;
and the shared IE-independent chat action. Its notice sink captures the exact
production message but does not prove client receipt/click rendering. A second
GameTest checks witness signatures against actual generated blueprints for all four
surface types and five qualities. A JUnit test checks the derived spatial index's
cap, negative chunk boundaries, dimensions, duplicate anchors and clearing. Existing
aggregate suites cover persistence/protocol and both dependency configurations.

Actual player exploration, reconnect, client chat-link behavior/manual layout and
visual qualification remain NOT_PERFORMED. Hosted results must be attached to the
exact final source commit, not inherited from the preceding manual-only build.

## Exterior entrance location — approved second-stage producer

The owner resolved the pending next trigger in the voice conversation: **« À l'entrée
extérieure »**, in answer to entrance versus underground deposit. The parent supplied
this decision and explicitly authorized the existing eight-block visible-proximity
checks for this bounded increment. No new tool, IE or Jade prerequisite is introduced.

Only an existing personal `EVIDENCE_DISCOVERED` record can advance. The same bounded
scheduler, loaded-chunk ray checks, range and facing apply. Each scan records at most
one stage and sends at most one private notice. Stage lookup is O(1), without copying
or scanning the journal. Completed location records are skipped on later scans.

The exterior witness is the top north-facing ladder for tiny entrances/collapsed
shafts, or the actual oak hatch for survey markers/camps (including the camp's
supported origin fallback). In addition to the visible witness, both first shaft
rungs below the surface must survive, face north, and have air in the adjacent shaft.
All reads require already loaded chunks. This confirms the access rather than merely
a decorative surface marker. For camps the clue itself is the entrance hatch, so a
later scan may locate it without requiring the player to move or use it. Closed or
open hatches can qualify when their collision shape is actually visible. DRY uses
the same entrance geometry and does not require mineral blocks or an IE reserve.

Only the observed exterior witness position is saved as `siteLocation`; original clue
and record identity remain unchanged. No chamber center, resource, quality or higher
stage is inferred. EN/FR private notices link to the standalone journal; persistence
of the stage deduplicates notices across ordinary save/reload. Destroyed/changed
access signatures fail closed; identical rebuilding at a confirmed site cannot be
distinguished from the original, as for first-stage clues. Existing natural placement
eligibility and production gates are unchanged.

Hosted coverage adds an actual production-scan GameTest for prerequisite rejection,
DRY progression, gaze/obstruction/range/damaged-access rejection, private independent
players, precise permitted fields and real SavedData disk reload without repeat
notification. Blueprint signature coverage checks all four surface types and five
qualities. Exact-commit CI evidence is required; these tests are not actual player
exploration, reconnect, full process restart or client visual proof.
