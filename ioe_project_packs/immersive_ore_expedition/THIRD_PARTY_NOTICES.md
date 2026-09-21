# Third-party notices

## GeOre

- Project: GeOre
- Authors: ShyNieke and Mrbysco
- Source: https://github.com/Mrbysco/GeOre
- Distribution: https://www.curseforge.com/minecraft/mc-mods/geore
- Integrated line: Minecraft 1.21.1, GeOre 6.2.x
- Source revision inspected: `307174eaf237e82236b8534d59c554031000ba05`
- License: MIT

Immersive Ore Expedition uses GeOre as a required, separately distributed runtime dependency. GeOre owns and supplies its code, registered resources, models, textures, sounds, language resources, recipes, loot tables, and geode world generation. No GeOre source file or asset is copied into the IOE jar by this integration. Any IOE Budding rank variant is an original IOE implementation for a validated GeOre material and must preserve this provenance boundary.

The following MIT notice is preserved for attribution and for any future distribution that includes a copy or substantial portion of GeOre:

```text
MIT License

Copyright (c) 2021 Mrbysco

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Applied Energistics 2

- Project: Applied Energistics 2
- Source: https://github.com/AppliedEnergistics/Applied-Energistics-2
- Distribution: https://appliedenergistics.org/download
- Integrated line: Minecraft 1.21.1, AE2 19.2.17
- Source tag inspected: `neoforge/v19.2.17`
- Source revision inspected: `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`
- Upstream licensing: LGPL-3.0 for code, with separately identified MIT and CC BY-NC-SA material upstream

Immersive Ore Expedition treats AE2 as a separately distributed required runtime dependency. IOE references `ae2:certus_quartz_crystal` as a rare Immersive Engineering mineral-mix output and extends the public `ae2:growth_acceleratable` block tag for compatibility; it does not copy AE2 Java source, models, textures, sounds, recipes, loot tables, language files, or world-generation data. The empty IOE `ae2:has_meteorites` overlay uses `replace: false`, preserving AE2's upstream meteorite biome values and normal meteorite placement.

## AE2 Crystal Science

- Project: AE2 Crystal Science
- Source: https://github.com/ExtremelyFrozen/AE2-Crystal-Science
- Distribution: https://www.curseforge.com/minecraft/mc-mods/ae2-crystal-science
- Integrated line: Minecraft 1.21.1, AE2 Crystal Science 1.1.12
- Mod id: `ae2cs`
- Modrinth project/version: `uJ9afomy` / `PP2uuQ6t`
- Source tag inspected: `v1.1.12-1.21.1-neoforge`
- Source revision inspected: `9b67c6407080fee43cbac95ada80c13c2d1b64ef`
- Upstream licensing: LGPL-3.0-or-later for code; CC BY-NC-SA 3.0 for assets
- LGPL license text: https://github.com/ExtremelyFrozen/AE2-Crystal-Science/blob/v1.1.12-1.21.1-neoforge/LICENSES/LGPL-3.0.txt
- Embedded LGPL license copy: `src/main/resources/META-INF/licenses/immersive_ore_expedition/AE2CS-LGPL-3.0.txt`

AE2 Crystal Science is a separately distributed required runtime dependency and remains the owner of its purified crystals, seeds, growth chamber, pulverizer, aggregator, pattern providers, wireless automation, recipes, code, and assets. IOE relies on the installed mod's public gameplay systems.

IOE includes one LGPL-3.0-or-later compatibility override at `src/main/resources/data/ae2cs/recipe/mechanical_cutting/polished_rose_quartz_from_pure_rose_quartz.json`, adapted from the same path in AE2 Crystal Science 1.1.12. The IOE source copy adds only the NeoForge `create` mod-loaded condition: when Create is installed, AE2CS retains the original cutting recipe and values; when Create is absent, NeoForge ignores the recipe before resolving the unavailable `create:cutting` serializer. No AE2CS Java source, model, texture, sound, language file, loot table, or other recipe is copied into IOE.

## GeOre: Additions

- Project: GeOre: Additions
- Author: Aidenboss434_OFFICIAL
- Distribution: https://www.curseforge.com/minecraft/mc-mods/geore-additions
- Restriction target inspected: Minecraft 1.21.1, GeOre: Additions 4.0
- CurseForge project ID: `1297257`
- CurseForge file ID inspected: `7794628`
- License: All Rights Reserved

IOE applies an original event-level restriction when the separately installed add-on is detected: its tools cannot recover recognized budding blocks through player break events, and ordinary entity placement of blocks registered under its namespace is canceled. This is a policy boundary, not an embedded or adapted upstream implementation. No GeOre: Additions code, asset, recipe, data file, or generated resource is copied, adapted, decompiled, or redistributed by IOE. Runtime behavior remains unverified until an official artifact is available in an approved CI environment.

## ExtendedAE

- Project: ExtendedAE
- Author: GlodBlock
- Source: https://github.com/GlodBlock/ExtendedAE
- Distribution: https://www.curseforge.com/minecraft/mc-mods/ex-pattern-provider
- Integrated line: Minecraft 1.21.1, ExtendedAE 2.2.x
- Source tag inspected: `1.21-2.2.33-neoforge`
- Source revision inspected: `90005ee29839fb9fa83bbe6544919c722f8b0dc6`
- License: GNU LGPL version 3

IOE references `extendedae:entro_crystal` as a rare Immersive Engineering mineral-mix output. It creates no ExtendedAE geode or budding-heart formation. No ExtendedAE Java source, model, texture, sound, recipe, language file, loot table, or generated resource is copied into IOE.

## Immersive Engineering

- Project: Immersive Engineering
- Author: BluSunrize and contributors
- Source: https://github.com/BluSunrize/ImmersiveEngineering
- Distribution: https://www.curseforge.com/minecraft/mc-mods/immersive-engineering
- Integrated line: Minecraft 1.21.1
- Source branch inspected: `1.21.1`
- Source revision inspected: `75a27f03e4243544243567e8d5c38d336f4f10f4`
- License: Blu's License of Common Sense

IOE references public registry and placed-feature identifiers to prevent free ore-block generation and to coordinate its original prospecting plans. No Immersive Engineering code, API class, asset, manual page, data file, or generated resource is copied into IOE. Immersive Engineering remains a separately distributed optional dependency and retains ownership of its machines, mineral-deposit system, recipes, blocks, items, assets, and behavior.

## Immersive Petroleum

- Project: Immersive Petroleum
- Author: TwistedGate and contributors
- Source: https://github.com/TwistedGate/ImmersivePetroleum
- Distribution: https://modrinth.com/mod/immersivepetroleum
- Integrated line: Minecraft 1.21.1, Immersive Petroleum 4.4.1-37 through 4.5.0-39
- Source branch inspected: `1.21.1`
- Source revision inspected: `09dfa613627d2f0114dc06de37f208aa1684bb89`
- Modrinth project/versions inspected: `MOw5TN6u` / `CV6UXQBi` (4.4.1-37), `1nu6ZI52` (4.5.0-39)
- License: All Rights Reserved

IOE compiles against the separately distributed Immersive Petroleum artifact and uses its native reservoir API objects and persistence entrypoint. IOE only controls spatial admission: free reservoir scans and unauthorized registrations are blocked, while admitted desert coal sites register oil reservoirs, volcanic sites register lava reservoirs, and beach, shore, river or ocean sites register aquifers transactionally. Immersive Petroleum remains the owner of reservoir recipes, fluids, SavedData format, surveying, pressure, depletion, wells, Pumpjack extraction, code and assets. No Immersive Petroleum source file, recipe, model, texture, sound, language file or other asset is copied into the IOE jar.

## Domum Ornamentum

- Project: Domum Ornamentum
- Mod id: `domum_ornamentum`
- Version inspected: `1.0.231`
- Source tag inspected: `v1.21.1-1.0.231`
- Source revision inspected: `af1106ab66fafea7427ed23dec73776a4c8d7748`
- Maven coordinate: `com.ldtteam:domum-ornamentum:1.0.231`
- API artifact SHA-256: `e5845bb264cbe03f3914c98e514056183fef564bd9f4c683b20602cf74766d92`
- Runtime artifact SHA-256: `04c0c902bdbcbd48e38bee5a323907ae0b7b7db4ff4a3e4da7c45334b65610a1`
- Runtime artifact SHA-512: `483061574a8452ce6b5b25779431540fc4e444123a987633546b290e9beb03ce094d0bdb80f28fb1dbc2317fd91d1dfbde1c4c0619dbf124768744cb8bc6ea35`
- Tagged source and runtime metadata: GPL-3.0
- Published Maven POM metadata: LGPL-3.0
- `DOMUM_FACTUAL_METADATA_STATUS: CONFIRMED_MIXED_METADATA`
- `DOMUM_OWNER_DISTRIBUTION_DISPOSITION: PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION`
- `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`
- Legal review: `NOT_PERFORMED`
- Owner disposition record: `docs/DOMUM_OWNER_DISTRIBUTION_DISPOSITION_0.2.50-alpha.md`

IOE uses Domum Ornamentum through optional compile/runtime integration for prospector-camp materialized architectural blocks when present. Domum Ornamentum remains separately distributed. No Domum Ornamentum Java source, model, texture, sound, recipe, loot table, language file, or other asset is copied into IOE. The qualified IOE release JAR contains zero embedded JARs.

The tagged source and runtime metadata identify GPL-3.0, while the published Maven POM identifies LGPL-3.0. This is recorded as `CONFIRMED_MIXED_METADATA`. For IOE `0.2.50-alpha`, the owner elected `PROCEED_WITH_SEPARATE_OPTIONAL_DOMUM_INTEGRATION` as a release-risk and distribution decision. The decision does not select GPL or LGPL as legally controlling and is not legal approval; `DOMUM_LEGAL_COMPATIBILITY: UNDETERMINED`. The optional, separately distributed integration, zero embedded Domum JARs, and no copied Domum source or assets remain conditions of the disposition. Any change to those boundaries reopens the owner/legal gate. CI retrieval or testing is not redistribution approval.
