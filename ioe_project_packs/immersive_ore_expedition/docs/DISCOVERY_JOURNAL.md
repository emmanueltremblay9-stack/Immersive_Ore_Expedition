# Discovery journal — first server persistence increment

Scope source: [governance PR5](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition_GPT/pull/5),
handoff `IOE-EXPEDITION-DISCOVERY-MANUAL-001`, reconstruction commit
`cd48b5129eae1881db2c8e0677455d58baea164d`. The user approved implementation and
subsequently resumed this bounded technical cycle. The governance PR is unmerged;
this reference does not claim canonical governance integration or modify FINAL_DESIGN.

## Runtime provided

The consolidated mod registers a server-start listener that opens world SavedData
`immersive_ore_expedition_discoveries`. `DiscoveryJournalService` is the production
server-thread API for reading the current player's immutable journal and accepting
evidence from future trusted server producers. It has no client packet receiver,
command granting discoveries, chunk scan, terrain write or optional IE dependency.

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
There is no journal UI or notification yet, and normal play does not populate it.

## Next bounded increment and decisions

Define the first authoritative discovery event using actual placed-clue evidence.
The approved scope does not yet specify a proximity radius, interaction requirement,
observation validation, valid resource probe, quality survey action or documentation
completion action. Resolve only the trigger needed for that next increment before
wiring it; do not turn locator availability into player knowledge. If live evidence
is lost on reindex, new progression fails closed while the historical record remains.

Default Compass visibility and Jade behavior are unchanged. IE manual pages, client
rendering/localization and stage notifications remain separate work. IE is optional;
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
