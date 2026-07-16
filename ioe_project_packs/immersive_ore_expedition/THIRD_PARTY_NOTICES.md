# Third-party notices

## GeOre

- Project: GeOre
- Authors: ShyNieke and Mrbysco
- Source: https://github.com/Mrbysco/GeOre
- Distribution: https://www.curseforge.com/minecraft/mc-mods/geore
- Integrated line: Minecraft 1.21.1, GeOre 6.2.x
- Source revision inspected: `307174eaf237e82236b8534d59c554031000ba05`
- License: MIT

Immersive Ore Expedition uses GeOre as a required, separately distributed runtime dependency. GeOre owns and supplies its code, block behavior, models, textures, sounds, language resources, recipes, loot tables, and geode world generation. No GeOre source file or asset is copied into the IOE jar by this integration. IOE selects GeOre blocks through Minecraft registry identifiers and places them inside IOE-controlled ore nodes.

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

Immersive Ore Expedition treats AE2 as a separately distributed required runtime dependency. IOE resolves the public registry entries `ae2:flawed_budding_quartz` and `ae2:sky_stone_block`, relies on AE2's native `ae2:charged_certus_quartz_crystal` water transformations, and extends the public `ae2:growth_acceleratable` block tag; it does not copy AE2 Java source, models, textures, sounds, recipes, loot tables, language files, or world-generation data. The empty `ae2:has_meteorites` biome-tag override and the replacement mine placement are original IOE data and code.

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

IOE resolves `extendedae:entro_budding_fully` and the separately owned AE2 block `ae2:fluix_block` through Minecraft registries. No ExtendedAE Java source, model, texture, sound, recipe, language file, loot table, or generated resource is copied into IOE.

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
- Integrated line: Minecraft 1.21.1, Immersive Petroleum 4.4.1-37
- Source branch inspected: `1.21.1`
- Source revision inspected: `09dfa613627d2f0114dc06de37f208aa1684bb89`
- Modrinth project/version inspected: `MOw5TN6u` / `CV6UXQBi`
- License: All Rights Reserved

IOE compiles against the separately distributed Immersive Petroleum artifact and uses its native reservoir API objects and persistence entrypoint. IOE only controls spatial admission: free reservoir scans and unauthorized registrations are blocked, while admitted desert coal sites register native IP oil reservoirs transactionally. Immersive Petroleum remains the owner of reservoir recipes, fluids, SavedData format, surveying, pressure, depletion, wells, Pumpjack extraction, code and assets. No Immersive Petroleum source file, recipe, model, texture, sound, language file or other asset is copied into the IOE jar.
