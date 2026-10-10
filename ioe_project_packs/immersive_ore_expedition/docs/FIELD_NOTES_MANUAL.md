# Field Notes — fundamentals and Overworld surface evidence

## Delivered pages

One additive IOE category, `immersive_ore_expedition:field_notes`, contains:

1. `field_notes/expedition_principles`: observation, separate location/resource/quality
   questions and independence of native progression from reading the guide.
2. `field_notes/site_quality`: DRY through MOTHERLODE, site allocation versus resource
   frequency, and individual Budding rank versus site quality.
3. `field_notes/formations_and_reserves`: world blocks/growth formations versus finite
   abstract IE mineral reserves, Core Sample discovery and Excavator extraction.

Each has an original English quotation from the supplied RP source, with a French
translation, followed by factual explanations reconciled with current IOE contracts.
No other narrative, equipment, resource, clue or gameplay rule is introduced.

## Source and reconciliation

Original source: [RP message 38b3463c](https://chatgpt.com/c/6ac97266-1b14-83e8-80f8-f750625811ab?messageId=38b3463c-2408-45ee-8f96-6119024c833e),
full text supplied by the parent thread on 2026-10-10. Quotations are taken respectively
from “The Engineer's Principle”, “The Five Geological Classifications” and “Final Field
Directive”. English quotations are preserved verbatim; French quotations are translations,
not claims that the original text was French. The initial source marked its mechanics
as proposed; current scoped user authorization and current technical contracts govern
what is shipped here.

Factual sources: `BUDDING_FINAL_DECISIONS.md`,
`IMMERSIVE_ENGINEERING_RESOURCE_INTEGRATION.md`, `DISCOVERY_JOURNAL.md` and the current
implementation. DRY residues are resource-specific; not every resource has Budding
nodes. Site quality is not final world frequency. Abstract IE reserves are not rendered
ore volumes. Reading these static entries neither awards discoveries nor gates recipes.

No site coordinates, original rolls, personal survey results or runtime index are read
or embedded. No promise of active Nether geodes, End caverns or an automatic journal
discovery trigger is made. Their pending decisions remain outside this slice.

## Additive registration and localization

Pinned IE 1.21.1-12.4.2-194 `ManualInstance.loadAutoEntries()` reads
`assets/immersiveengineering/manual/autoload.json` separately from every client resource
pack; it does not select only the highest-priority file. IOE contributes a manifest
containing only its own category and entry IDs. Upstream categories and entries are not
copied, replaced or removed. Registration is conditional on IE's client manual existing;
without IE these resources are inert. No code/dependency/network change is required.

All entry JSON and text files live under `assets/immersive_ore_expedition/manual/`.
Each JSON is an empty object (plain text, no recipe, advancement gate or special element).
The localized text has title and subtitle as its first two lines, then the body using
IE's supported `<br>` formatting. `en_us` and `fr_fr` are provided; IE falls back to
`en_us` for other languages. Category names use the normal language JSON keys.
The category's ordered entry list is the navigation; there are no unresolved hyperlink
targets, duplicate programmatic entries or changes to the Journal/Compass.

## Evidence and limits

Static verification checks manifest namespace/order, entry JSON, both localized texts,
title/subtitle/body structure, supported markup and category translations. Existing
automated suites and artifact inspection qualify the exact resulting commit; their
results must be reported separately, not inherited from the previous Journal build.
Actual IE client registration, page breaks, typography, resource reload and language
switch remain NOT_PERFORMED. Static resource validation does not replace visual proof.

## Overworld surface-evidence increment

A separate IOE `surface_evidence` category adds four EN/FR entries. English quotations
come verbatim from source Chapter II entries 1–4; French quotations are translations.
The miner-camp quotation originated in the abandoned-camp entry; the factual page
describes both supported active/recent and abandoned camp presentations without
claiming every camp contains survey notes or valuable supplies.

| Included page | Active source and concrete details |
|---|---|
| `tiny_vertical_mine_entrance` | `ExpeditionSiteBlueprints.addMineEntrance`: stone ring, oak posts/planks, raised lantern |
| `collapsed_shaft` | `addCollapsedShaft`: stone ring, gravel/tuff, stripped-oak support, planks and mossy cobblestone |
| `miner_camp` | `addSurfaceClue` dispatches to `ProspectorCampOutcropComposer` or `AbandonedProspectorCampComposer`; campfire, worktables/containers according to layout, host-rock outcrop; archetype selected in `ExpeditionSiteFeature` |
| `buried_survey_marker` | `addSurveyMarker`: partly buried mossy border, chiseled stone bricks, wall/lantern, stone-brick shaft cover with oak trapdoor |

These are the four `ExpeditionSiteType.naturalSurfaceSites()`, not merely catalog
entries. Each has a configured feature, placed feature and live `neoforge:add_features`
modifier at `surface_structures`, through a biome tag containing `#c:is_overworld`.
Natural generation and their structure toggles default on. Actual placement still
requires compatible terrain, one valid resource profile, available resource dependencies
and confirmed transaction/deposit preparation. Productive sites require IE. New-chunk
admission/placement constraints still apply. This source path proves supported generation,
not a fresh manual observation or a guarantee that every attempted site appears.

The pages describe vanilla/common recognition features; they do not promise optional
Domum-specific decoration when Domum is absent. Server configs/datapacks can change
availability. No rarity numbers, geometry offsets, hidden quality correlations, resource
mapping, site coordinates or private journal data are exposed by these pages.

### Explicit exclusions from this section

- **Standalone mineral outcrop:** not one of the four registered natural surface
  types. The camp's host-rock outcrop is described only as part of its composition,
  not as guaranteed extractable ore or a separately generated deposit.
- **Reservoir seep/pocket lake/vent:** `IpReservoirSeepFeature.placeSeepClue` skips
  direct placement, and the `SurfaceCluePlacementPlanner` path is planning scaffolding.
  Abstract IP reservoir integration does not prove a generated surface seep. No IP-
  dependent page is added and IP absence is not bypassed.
- **Aquatic/shoreline camps:** the current natural camp path rejects that visual
  family; no underwater or shoreline camp page is advertised.
- **Dynamic collapses, special excavation puzzles, alternate hidden tunnels and
  survey-note rewards:** source RP possibilities are not promoted to implemented
  mechanics or guaranteed loot.
- **Connector/chamber:** implemented underground components, not independent surface
  clues; reserved for the next bounded section rather than advertised here as surface
  types. Nether/End and unavailable optional integration content remain excluded.

No worldgen, resource profile, recipe, dependency, Journal/Compass code or pending
discovery-trigger decision changes. Registration uses the same additive IE manifest
with a new IOE category; all four JSON bodies and eight texts stay in the IOE namespace.
Static verification checks the live feature chain and localized entry references.
Actual client rendering/registration remains NOT_PERFORMED.

Next essential approved content: connected underground access and ore-load chambers,
using source Chapter III entries 5–6 and reconciling their current geometry/resource
semantics. The full Field Notes chapter set, atlas, Nether/End sections and discovery
triggers remain incomplete.
