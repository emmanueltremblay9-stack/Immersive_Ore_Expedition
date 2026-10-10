# Field Notes — static fundamentals slice

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

Next suggested section: supported Overworld surface evidence, reconciled page by page
against actual implemented structures before importing more of the RP source. The full
Field Notes chapter set, resource atlas, Nether/End sections and discovery triggers are
not completed by this slice.
