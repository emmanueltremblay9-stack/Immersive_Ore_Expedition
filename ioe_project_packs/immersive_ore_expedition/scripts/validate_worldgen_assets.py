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
NORMAL_ORE_TAG = DATA / "tags/worldgen/placed_feature/normal_ore_generation.json"
MINE_PROFILE_TAGS = DATA / "tags/worldgen/biome/ore_profile"
MINE_PROFILE_DEFINITIONS = DATA / "immersive_ore_expedition/mine_resource_profile"
SITE_QUALITIES = frozenset({"dry", "poor", "normal", "rich", "motherlode"})
RESOURCE_KINDS = frozenset({"geore", "ae2_certus", "extendedae_fluix"})
EXPECTED_MINE_PROFILES = {
    "aluminum": "geore",
    "certus": "ae2_certus",
    "coal": "geore",
    "copper": "geore",
    "diamond": "geore",
    "emerald": "geore",
    "entroized_fluix": "extendedae_fluix",
    "gold": "geore",
    "iron": "geore",
    "lapis": "geore",
    "lead": "geore",
    "nickel": "geore",
    "redstone": "geore",
    "silver": "geore",
    "uranium": "geore",
}
# Versioned allowlist from data/minecraft/worldgen/placed_feature in the Minecraft 1.21.1 client JAR.
MINECRAFT_1_21_1_NORMAL_ORE_PLACED_FEATURES = frozenset(
    {
        "minecraft:ore_coal_upper",
        "minecraft:ore_coal_lower",
        "minecraft:ore_iron_upper",
        "minecraft:ore_iron_middle",
        "minecraft:ore_iron_small",
        "minecraft:ore_copper",
        "minecraft:ore_copper_large",
        "minecraft:ore_gold",
        "minecraft:ore_gold_lower",
        "minecraft:ore_gold_extra",
        "minecraft:ore_redstone",
        "minecraft:ore_redstone_lower",
        "minecraft:ore_lapis",
        "minecraft:ore_lapis_buried",
        "minecraft:ore_diamond",
        "minecraft:ore_diamond_medium",
        "minecraft:ore_diamond_large",
        "minecraft:ore_diamond_buried",
        "minecraft:ore_emerald",
        "minecraft:ore_quartz_nether",
        "minecraft:ore_gold_nether",
        "minecraft:ore_ancient_debris_large",
        "minecraft:ore_debris_small",
        "minecraft:fossil_upper",
        "minecraft:fossil_lower",
    }
)
REQUIRED_EXTERNAL_ORE_PLACED_FEATURES = frozenset(
    {
        "ae2cs:certus_quartz_ore_placed",
        "ae2cs:charged_certus_quartz_ore_placed",
    }
)


def read_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as source:
        return json.load(source)


def is_codec_int(value: object, minimum: int, maximum: int) -> bool:
    return isinstance(value, int) and not isinstance(value, bool) and minimum <= value <= maximum


def validate_count_rule(rule: object, label: str, failures: list[str]) -> bool:
    if not isinstance(rule, dict) or not is_codec_int(rule.get("base"), 0, 4096):
        failures.append(f"count rule {label}.base must be an integer from 0 through 4096")
        return False
    unexpected = set(rule) - {"base", "bonus"}
    if unexpected:
        failures.append(f"unknown count rule keys {label}: {sorted(unexpected)}")
    bonus = rule.get("bonus")
    if bonus is None:
        return rule["base"] == 0
    if not isinstance(bonus, dict):
        failures.append(f"invalid bonus rule {label}: {bonus!r}")
        return False
    required = {"first_chunk", "chunks_per_bonus"}
    if not required.issubset(bonus):
        failures.append(f"bonus rule lacks required keys {label}: {bonus!r}")
        return False
    unexpected_bonus = set(bonus) - {"first_chunk", "chunks_per_bonus", "max_bonus"}
    if unexpected_bonus:
        failures.append(f"unknown bonus rule keys {label}: {sorted(unexpected_bonus)}")
    for key in ("first_chunk", "chunks_per_bonus", "max_bonus"):
        if key in bonus and not is_codec_int(bonus[key], 1, 4096):
            failures.append(f"bonus rule {label}.{key} must be an integer from 1 through 4096")
    return False


def validate_mine_profiles(failures: list[str]) -> int:
    tag_files = {path.stem: path for path in MINE_PROFILE_TAGS.glob("*.json")}
    definition_files = {path.stem: path for path in MINE_PROFILE_DEFINITIONS.glob("*.json")}
    direct_biome_owners: dict[str, str] = {}
    if tag_files.keys() != definition_files.keys():
        failures.append(
            "mine profile tag/definition ids differ: "
            f"tags={sorted(tag_files)}, definitions={sorted(definition_files)}"
        )
    if set(definition_files) != set(EXPECTED_MINE_PROFILES):
        failures.append(
            "built-in mine profile ids differ from the canonical set: "
            f"actual={sorted(definition_files)}, expected={sorted(EXPECTED_MINE_PROFILES)}"
        )

    for profile_id in sorted(tag_files.keys() | definition_files.keys()):
        tag_path = tag_files.get(profile_id)
        definition_path = definition_files.get(profile_id)
        if tag_path is None or definition_path is None:
            continue
        tag = read_json(tag_path)
        if not isinstance(tag.get("values"), list) or not tag["values"]:
            failures.append(f"empty mine profile biome tag: {tag_path.relative_to(ROOT)}")
        else:
            for entry in tag["values"]:
                biome_id = entry if isinstance(entry, str) else entry.get("id") if isinstance(entry, dict) else None
                if not isinstance(biome_id, str) or biome_id.startswith("#"):
                    continue
                previous_owner = direct_biome_owners.setdefault(biome_id, profile_id)
                if previous_owner != profile_id:
                    failures.append(
                        f"biome {biome_id} belongs directly to both {previous_owner} and {profile_id} profiles"
                    )

        definition = read_json(definition_path)
        expected_definition_keys = {
            "biome_tag", "resource_kind", "resource_name", "survey_radius_chunks", "quality_counts"
        }
        if set(definition) != expected_definition_keys:
            failures.append(f"mine profile {profile_id} has invalid top-level fields")
        expected_tag = f"immersive_ore_expedition:ore_profile/{profile_id}"
        if definition.get("biome_tag") != expected_tag:
            failures.append(f"mine profile {profile_id} points at wrong biome tag")
        kind = definition.get("resource_kind")
        if kind not in RESOURCE_KINDS:
            failures.append(f"mine profile {profile_id} has invalid resource_kind={kind!r}")
        elif kind != EXPECTED_MINE_PROFILES.get(profile_id):
            failures.append(
                f"mine profile {profile_id} must use resource_kind={EXPECTED_MINE_PROFILES.get(profile_id)!r}"
            )
        if definition.get("resource_name") != profile_id:
            failures.append(f"mine profile {profile_id} must use resource_name={profile_id!r}")
        radius = definition.get("survey_radius_chunks")
        if not is_codec_int(radius, 1, 16):
            failures.append(f"mine profile {profile_id} has invalid survey radius={radius!r}")

        quality_counts = definition.get("quality_counts")
        if not isinstance(quality_counts, dict) or set(quality_counts) != SITE_QUALITIES:
            failures.append(
                f"mine profile {profile_id} must define exactly {sorted(SITE_QUALITIES)}"
            )
            continue
        for quality in sorted(SITE_QUALITIES):
            counts = quality_counts[quality]
            if not isinstance(counts, dict) or set(counts) != {
                "ore_budget", "node_count", "special_budding_count"
            }:
                failures.append(f"mine profile {profile_id}.{quality} has invalid count fields")
                continue
            zero_rules = {
                name: validate_count_rule(rule, f"{profile_id}.{quality}.{name}", failures)
                for name, rule in counts.items()
            }
            if quality == "dry" and not all(zero_rules.values()):
                failures.append(f"mine profile {profile_id}.dry counts must all be zero")
            elif quality != "dry" and kind == "geore":
                if counts["ore_budget"].get("base", 0) <= 0 or counts["node_count"].get("base", 0) <= 0:
                    failures.append(f"productive GeOre profile {profile_id}.{quality} needs ore and nodes")
                if not zero_rules["special_budding_count"]:
                    failures.append(f"productive GeOre profile {profile_id}.{quality} cannot use special budding")
            elif quality != "dry" and kind in RESOURCE_KINDS - {"geore"}:
                if not zero_rules["ore_budget"] or not zero_rules["node_count"]:
                    failures.append(f"crystal profile {profile_id}.{quality} cannot use GeOre counts")
                if counts["special_budding_count"].get("base", 0) <= 0:
                    failures.append(f"crystal profile {profile_id}.{quality} needs budding blocks")
    return len(definition_files)


def validate() -> None:
    failures: list[str] = []
    mine_profile_count = validate_mine_profiles(failures)
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

    if not NORMAL_ORE_TAG.is_file():
        failures.append(f"missing {NORMAL_ORE_TAG.relative_to(ROOT)}")
    else:
        normal_ore_tag = read_json(NORMAL_ORE_TAG)
        if normal_ore_tag.get("replace") is not False:
            failures.append("normal ore placed-feature tag must merge with replace=false")

        values = normal_ore_tag.get("values")
        if not isinstance(values, list):
            failures.append("normal ore placed-feature tag must contain a values list")
            values = []

        required_ids: list[str] = []
        optional_ids: list[str] = []
        for index, value in enumerate(values):
            if isinstance(value, str):
                required_ids.append(value)
                continue
            if not isinstance(value, dict) or not isinstance(value.get("id"), str):
                failures.append(f"invalid normal ore tag entry at index {index}: {value!r}")
                continue
            if value.get("required") is not False:
                failures.append(
                    f"external optional placed feature must declare required=false: {value!r}"
                )
            optional_ids.append(value["id"])

        all_ids = required_ids + optional_ids
        duplicates = sorted({feature_id for feature_id in all_ids if all_ids.count(feature_id) > 1})
        if duplicates:
            failures.append(f"duplicate normal ore placed-feature ids: {duplicates}")

        actual_vanilla_ids = {
            feature_id for feature_id in required_ids if feature_id.startswith("minecraft:")
        }
        missing_vanilla_ids = sorted(
            MINECRAFT_1_21_1_NORMAL_ORE_PLACED_FEATURES - actual_vanilla_ids
        )
        unknown_vanilla_ids = sorted(
            actual_vanilla_ids - MINECRAFT_1_21_1_NORMAL_ORE_PLACED_FEATURES
        )
        if missing_vanilla_ids:
            failures.append(
                f"normal ore tag is missing Minecraft 1.21.1 placed features: {missing_vanilla_ids}"
            )
        if unknown_vanilla_ids:
            failures.append(
                f"normal ore tag contains unknown Minecraft 1.21.1 placed features: {unknown_vanilla_ids}"
            )

        actual_required_external_ids = {
            feature_id for feature_id in required_ids if not feature_id.startswith("minecraft:")
        }
        if actual_required_external_ids != REQUIRED_EXTERNAL_ORE_PLACED_FEATURES:
            failures.append(
                "required external normal ore placed features changed: "
                f"expected={sorted(REQUIRED_EXTERNAL_ORE_PLACED_FEATURES)}, "
                f"actual={sorted(actual_required_external_ids)}"
            )

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
        f"{len(FEATURE_IDS)} feature pairs, {len(NATURAL_FEATURE_TAGS)} biome modifiers, "
        f"{mine_profile_count} datapack mine profiles, "
        f"{len(MINECRAFT_1_21_1_NORMAL_ORE_PLACED_FEATURES)} vanilla ore/fossil placed features, "
        "1 GameTest template"
    )


if __name__ == "__main__":
    validate()
