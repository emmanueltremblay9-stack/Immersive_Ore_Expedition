# Project Spec — IOE Crystal Growth

## Responsibility
This module unifies vanilla amethyst, AE2 Certus, and optional GeOre resource growth under one gameplay category: Crystal Growth Sites.

## Must implement
- Crystal Growth Site abstraction.
- Structure-anchored amethyst growth chambers.
- Optional AE2 buried Certus / meteorite sites.
- Optional GeOre adapter that disables or avoids free/random geode placement and places sites only through IOE anchors.
- Meteoritic Geode variant: AE2 sky-stone crust around amethyst/GeOre/Certus core.

## Must not implement
- No fake Fluix ore.
- No random crystal caves everywhere.
- No resource type that fails ResourcePolicy.
- No hard dependency on GeOre unless user later changes scope.

## Important nuance
GeOre may not be present in the uploaded pack list. Keep it optional.
