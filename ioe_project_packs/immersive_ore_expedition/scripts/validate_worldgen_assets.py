#!/usr/bin/env python3
"""Static validation for IOE expedition worldgen resources."""

from __future__ import annotations

import gzip
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/immersive_ore_expedition"
FEATURE_IDS = (
    "tiny_vertical_mine_entrance",
    "collapsed_shaft",
    "miner_camp",
    "buried_survey_marker",
    "basic_mineshaft_connector",
    "ore_load_chamber",
)
NATURAL_FEATURE_TAGS = {
    "tiny_vertical_mine_entrance": "mine_entrance_biomes",
    "collapsed_shaft": "collapsed_shaft_biomes",
    "miner_camp": "miner_camp_biomes",
    "buried_survey_marker": "survey_marker_biomes",
}


def read_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as source:
        return json.load(source)


def validate() -> None:
    failures: list[str] = []
    for feature_id in FEATURE_IDS:
        configured_path = DATA / f"worldgen/configured_feature/{feature_id}.json"
        placed_path = DATA / f"worldgen/placed_feature/{feature_id}.json"
        for path in (configured_path, placed_path):
            if not path.is_file():
                failures.append(f"missing {path.relative_to(ROOT)}")
        if not configured_path.is_file() or not placed_path.is_file():
            continue

        configured = read_json(configured_path)
        expected_id = f"immersive_ore_expedition:{feature_id}"
        if configured != {"type": expected_id, "config": {}}:
            failures.append(f"invalid configured feature {feature_id}: {configured!r}")

        placed = read_json(placed_path)
        if placed.get("feature") != expected_id or not placed.get("placement"):
            failures.append(f"invalid placed feature {feature_id}: {placed!r}")

    for feature_id, biome_tag in NATURAL_FEATURE_TAGS.items():
        modifier_path = DATA / f"neoforge/biome_modifier/{feature_id}.json"
        tag_path = DATA / f"tags/worldgen/biome/{biome_tag}.json"
        if not modifier_path.is_file():
            failures.append(f"missing {modifier_path.relative_to(ROOT)}")
            continue
        if not tag_path.is_file():
            failures.append(f"missing {tag_path.relative_to(ROOT)}")
            continue
        modifier = read_json(modifier_path)
        if modifier.get("features") != f"immersive_ore_expedition:{feature_id}":
            failures.append(f"biome modifier points at wrong feature: {modifier_path.relative_to(ROOT)}")
        if modifier.get("biomes") != f"#immersive_ore_expedition:{biome_tag}":
            failures.append(f"biome modifier points at wrong biome tag: {modifier_path.relative_to(ROOT)}")
        tag = read_json(tag_path)
        if not tag.get("values"):
            failures.append(f"empty biome tag: {tag_path.relative_to(ROOT)}")

    template_path = DATA / "structure/expedition_worldgen_empty.nbt"
    if not template_path.is_file():
        failures.append(f"missing {template_path.relative_to(ROOT)}")
    else:
        with gzip.open(template_path, "rb") as source:
            payload = source.read()
        for required in (b"DataVersion", b"size", b"palette", b"blocks", b"entities"):
            if required not in payload:
                failures.append(f"GameTest template lacks {required.decode('ascii')}")

    if failures:
        raise SystemExit("Worldgen asset validation failed:\n- " + "\n- ".join(failures))
    print(
        "Worldgen asset validation passed: "
        f"{len(FEATURE_IDS)} feature pairs, {len(NATURAL_FEATURE_TAGS)} biome modifiers, 1 GameTest template"
    )


if __name__ == "__main__":
    validate()
