#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any


MODULE_ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = MODULE_ROOT / "src/main/java/com/oblixorprime/ioe"
RESOURCES_ROOT = MODULE_ROOT / "src/main/resources"
BLOCKS_JAVA = JAVA_ROOT / "budding/IoeBuddingBlocks.java"
RANK_JAVA = JAVA_ROOT / "budding/BuddingRank.java"

MATERIAL_LABELS = {
    "aluminum": ("Aluminum", "d’aluminium"),
    "coal": ("Coal", "de charbon"),
    "copper": ("Copper", "de cuivre"),
    "diamond": ("Diamond", "de diamant"),
    "emerald": ("Emerald", "d’émeraude"),
    "gold": ("Gold", "d’or"),
    "iron": ("Iron", "de fer"),
    "lapis": ("Lapis Lazuli", "de lapis-lazuli"),
    "lead": ("Lead", "de plomb"),
    "nickel": ("Nickel", "de nickel"),
    "redstone": ("Redstone", "de redstone"),
    "silver": ("Silver", "d’argent"),
    "uranium": ("Uranium", "d’uranium"),
}
FRENCH_RANKS = {
    "damaged": "endommagé",
    "chipped": "ébréché",
    "flawed": "imparfait",
    "flawless": "parfait",
}


def read_source_contract() -> tuple[list[str], list[tuple[str, str]]]:
    blocks_source = BLOCKS_JAVA.read_text(encoding="utf-8")
    material_match = re.search(
        r"GEORE_MATERIALS\s*=\s*List\.of\((.*?)\);",
        blocks_source,
        flags=re.DOTALL,
    )
    if material_match is None:
        raise SystemExit(f"Cannot read GEORE_MATERIALS from {BLOCKS_JAVA}")
    materials = re.findall(r'"([a-z0-9_]+)"', material_match.group(1))

    rank_source = RANK_JAVA.read_text(encoding="utf-8")
    enum_match = re.search(
        r"public\s+enum\s+BuddingRank\s*\{(.*?)"
        r"public\s+static\s+final\s+BuddingRank\s+MAXIMUM_RESTORABLE",
        rank_source,
        flags=re.DOTALL,
    )
    if enum_match is None:
        raise SystemExit(f"Cannot read BuddingRank constants from {RANK_JAVA}")
    ranks = re.findall(
        r"^\s*([A-Z]+)\(\"([a-z]+)\"\)[,;]",
        enum_match.group(1),
        flags=re.MULTILINE,
    )

    if not materials or len(materials) != len(set(materials)):
        raise SystemExit(f"Invalid or duplicate GeOre materials in {BLOCKS_JAVA}: {materials}")
    if not ranks or len(ranks) != len(set(ranks)):
        raise SystemExit(f"Invalid or duplicate ranks in {RANK_JAVA}: {ranks}")
    missing_labels = sorted(set(materials) - set(MATERIAL_LABELS))
    if missing_labels:
        raise SystemExit(f"Missing translations for source materials: {missing_labels}")
    return materials, ranks


def dump_json(value: Any) -> str:
    return json.dumps(value, indent=2, ensure_ascii=False) + "\n"


def block_id(material: str, rank_path: str) -> str:
    return f"immersive_ore_expedition:{rank_path}_budding_{material}"


def silk_touch_condition() -> dict[str, Any]:
    return {
        "condition": "minecraft:match_tool",
        "predicate": {
            "predicates": {
                "minecraft:enchantments": [
                    {
                        "enchantments": "minecraft:silk_touch",
                        "levels": {"min": 1},
                    }
                ]
            }
        },
    }


def block_loot(material: str, rank_path: str) -> dict[str, Any]:
    current = block_id(material, rank_path)
    if rank_path == "flawless":
        entry: dict[str, Any] = {
            "type": "minecraft:item",
            "name": block_id(material, "flawed"),
        }
        conditions = [{"condition": "minecraft:survives_explosion"}]
    else:
        fallback = {
            "flawed": block_id(material, "chipped"),
            "chipped": block_id(material, "damaged"),
            "damaged": f"geore:{material}_block",
        }[rank_path]
        entry = {
            "type": "minecraft:alternatives",
            "children": [
                {
                    "type": "minecraft:item",
                    "conditions": [silk_touch_condition()],
                    "name": current,
                },
                {
                    "type": "minecraft:item",
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                    "name": fallback,
                },
            ],
        }
        conditions = []

    pool: dict[str, Any] = {
        "bonus_rolls": 0.0,
        "entries": [entry],
        "rolls": 1.0,
    }
    if conditions:
        pool["conditions"] = conditions
    return {
        "type": "minecraft:block",
        "pools": [pool],
        "random_sequence": f"immersive_ore_expedition:blocks/{rank_path}_budding_{material}",
    }


def block_model(material: str, rank_path: str) -> dict[str, Any]:
    storage = f"geore:block/{material}_block"
    budding = f"geore:block/budding_{material}"
    if rank_path == "damaged":
        return {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": storage},
        }
    if rank_path == "flawless":
        return {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": budding},
        }
    if rank_path == "chipped":
        return {
            "parent": "minecraft:block/cube_bottom_top",
            "textures": {
                "bottom": storage,
                "side": storage,
                "top": budding,
            },
        }
    if rank_path == "flawed":
        return {
            "parent": "minecraft:block/cube_bottom_top",
            "textures": {
                "bottom": storage,
                "side": budding,
                "top": budding,
            },
        }
    raise ValueError(f"Unknown rank path: {rank_path}")


def aggregator_recipe(material: str, rank_path: str) -> dict[str, Any]:
    previous = {
        "damaged": f"geore:{material}_block",
        "chipped": block_id(material, "damaged"),
        "flawed": block_id(material, "chipped"),
    }[rank_path]
    return {
        "type": "ae2cs:crystal_aggregator_recipe_serializer",
        "energy_cost": 16000,
        "input_a": {"count": 1, "item": "ae2cs:resonating_seed"},
        "input_b": {
            "count": 1,
            "tag": "ae2cs:pure_crystal/resonating_crystal",
        },
        "input_c": {"count": 1, "item": previous},
        "result": {"count": 1, "id": block_id(material, rank_path)},
    }


def disabled_recipe(recipe: dict[str, Any]) -> dict[str, Any]:
    return {
        "neoforge:conditions": [{"type": "neoforge:false"}],
        **recipe,
    }


def generated_json() -> dict[Path, Any]:
    materials, ranks = read_source_contract()
    rank_paths = [path for _, path in ranks]
    outputs: dict[Path, Any] = {}
    all_ids: list[str] = []

    for material in materials:
        for rank_path in rank_paths:
            path = f"{rank_path}_budding_{material}"
            identifier = block_id(material, rank_path)
            all_ids.append(identifier)
            outputs[
                RESOURCES_ROOT
                / f"assets/immersive_ore_expedition/blockstates/{path}.json"
            ] = {
                "variants": {
                    "": {"model": f"immersive_ore_expedition:block/{path}"}
                }
            }
            outputs[
                RESOURCES_ROOT
                / f"assets/immersive_ore_expedition/models/block/{path}.json"
            ] = block_model(material, rank_path)
            outputs[
                RESOURCES_ROOT
                / f"assets/immersive_ore_expedition/models/item/{path}.json"
            ] = {"parent": f"immersive_ore_expedition:block/{path}"}
            outputs[
                RESOURCES_ROOT
                / f"data/immersive_ore_expedition/loot_table/blocks/{path}.json"
            ] = block_loot(material, rank_path)

        for rank_path in ("damaged", "chipped", "flawed"):
            outputs[
                RESOURCES_ROOT
                / "data/immersive_ore_expedition/recipe/aggregator"
                / f"{rank_path}_budding_{material}.json"
            ] = aggregator_recipe(material, rank_path)

    block_tag = {"replace": False, "values": all_ids}
    outputs[RESOURCES_ROOT / "data/c/tags/block/budding_blocks.json"] = block_tag
    outputs[RESOURCES_ROOT / "data/c/tags/item/budding_blocks.json"] = block_tag
    outputs[
        RESOURCES_ROOT / "data/c/tags/block/relocation_not_supported.json"
    ] = block_tag
    outputs[
        RESOURCES_ROOT / "data/minecraft/tags/block/mineable/pickaxe.json"
    ] = block_tag
    outputs[
        RESOURCES_ROOT / "data/minecraft/tags/block/crystal_sound_blocks.json"
    ] = block_tag
    outputs[
        RESOURCES_ROOT / "data/geore/tags/block/budding_geore.json"
    ] = block_tag
    outputs[
        RESOURCES_ROOT / "data/geore/tags/item/geore_budding.json"
    ] = block_tag

    outputs[
        RESOURCES_ROOT
        / "data/ae2cs/recipe/aggregator/flawless_budding_quartz.json"
    ] = disabled_recipe(
        {
            "type": "ae2cs:crystal_aggregator_recipe_serializer",
            "energy_cost": 80000,
            "input_a": {
                "count": 6,
                "item": "ae2cs:certus_quartz_seed",
            },
            "input_b": {
                "count": 6,
                "tag": "ae2cs:pure_crystal/resonating_crystal",
            },
            "input_c": {
                "count": 6,
                "item": "ae2:flawed_budding_quartz",
            },
            "result": {
                "count": 1,
                "id": "ae2:flawless_budding_quartz",
            },
        }
    )
    outputs[
        RESOURCES_ROOT / "data/extendedae/recipe/fixer/certus_flawed.json"
    ] = disabled_recipe(
        {
            "type": "extendedae:crystal_fixer",
            "chance": 500,
            "fuel": {
                "ingredient": {
                    "item": "ae2:charged_certus_quartz_crystal"
                }
            },
            "input": {
                "count": 1,
                "id": "ae2:flawed_budding_quartz",
            },
            "output": {
                "count": 1,
                "id": "ae2:flawless_budding_quartz",
            },
        }
    )
    outputs[
        RESOURCES_ROOT / "data/extendedae/recipe/fixer/entro_mostly.json"
    ] = disabled_recipe(
        {
            "type": "extendedae:crystal_fixer",
            "chance": 7000,
            "fuel": {
                "ingredient": {"tag": "c:dusts/ender_pearl"}
            },
            "input": {
                "count": 1,
                "id": "extendedae:entro_budding_mostly",
            },
            "output": {
                "count": 1,
                "id": "extendedae:entro_budding_fully",
            },
        }
    )
    return outputs


def expected_languages() -> dict[Path, dict[str, str]]:
    materials, ranks = read_source_contract()
    paths = {
        "en": RESOURCES_ROOT / "assets/immersive_ore_expedition/lang/en_us.json",
        "fr": RESOURCES_ROOT / "assets/immersive_ore_expedition/lang/fr_fr.json",
    }
    languages = {
        language: json.loads(path.read_text(encoding="utf-8"))
        for language, path in paths.items()
    }
    for material in materials:
        english_material, french_material = MATERIAL_LABELS[material]
        for rank_name, rank_path in ranks:
            key = (
                "block.immersive_ore_expedition."
                f"{rank_path}_budding_{material}"
            )
            languages["en"][key] = (
                f"{rank_name.title()} Budding {english_material}"
            )
            languages["fr"][key] = (
                f"Bloc bourgeonnant {french_material} "
                f"{FRENCH_RANKS[rank_path]}"
            )
    return {
        paths["en"]: languages["en"],
        paths["fr"]: languages["fr"],
    }


def expected_outputs() -> dict[Path, str]:
    outputs = {
        path: dump_json(value)
        for path, value in generated_json().items()
    }
    outputs.update(
        {
            path: dump_json(value)
            for path, value in expected_languages().items()
        }
    )
    return outputs


def sync(write: bool) -> list[str]:
    failures: list[str] = []
    for path, expected in expected_outputs().items():
        if write:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(expected, encoding="utf-8", newline="\n")
            continue
        if not path.is_file():
            failures.append(f"missing generated file: {path.relative_to(MODULE_ROOT)}")
        elif path.read_text(encoding="utf-8") != expected:
            failures.append(f"stale generated file: {path.relative_to(MODULE_ROOT)}")
    return failures


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Synchronize source-linked IOE Budding rank resources."
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--write", action="store_true", help="write generated resources")
    mode.add_argument("--check", action="store_true", help="check generated resources")
    args = parser.parse_args()

    failures = sync(write=args.write)
    if failures:
        print("Budding rank resource synchronization failed:")
        for failure in failures:
            print(f"- {failure}")
        return 1

    materials, ranks = read_source_contract()
    action = "wrote" if args.write else "verified"
    print(
        f"Budding rank resources {action}: "
        f"{len(materials)} materials x {len(ranks)} ranks"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
