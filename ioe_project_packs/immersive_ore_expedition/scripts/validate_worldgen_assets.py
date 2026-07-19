#!/usr/bin/env python3
"""Static validation for IOE expedition worldgen resources."""

from __future__ import annotations

import gzip
import csv
import json
from decimal import Decimal
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA_ROOT = ROOT / "src/main/resources/data"
DATA = DATA_ROOT / "immersive_ore_expedition"
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
EXPECTED_MINE_PROFILES = frozenset({
    "aluminum", "certus", "coal", "copper", "diamond", "emerald", "entroized_fluix",
    "gold", "iron", "lapis", "lead", "nickel", "redstone", "silver", "uranium",
})
EXPECTED_AQUATIC_MIXES = frozenset({"alluvial_sift", "silt", "ancient_seabed"})
EXPECTED_TIERS = ("mother", "major", "minor", "direct")
EXPECTED_RARE_ALLOWLISTS = {
    "diamond": {
        "minecraft:frozen_peaks", "biomesoplenty:crag", "biomesoplenty:glowing_grotto",
        "regions_unexplored:icy_heights", "regions_unexplored:spires", "biomeswevegone:shattered_glacier",
    },
    "emerald": {
        "minecraft:grove", "biomesoplenty:jade_cliffs", "regions_unexplored:wisteria_grove",
        "biomeswevegone:crag_gardens",
    },
    "certus": {
        "minecraft:cherry_grove", "biomesoplenty:auroral_garden", "regions_unexplored:alpha_grove",
        "biomeswevegone:sakura_grove", "biomeswevegone:skyris_vale",
    },
    "entroized_fluix": {
        "minecraft:mushroom_fields", "biomesoplenty:mystic_grove", "regions_unexplored:bioshroom_caves",
        "regions_unexplored:prismachasm", "biomeswevegone:enchanted_tangle",
        "biomeswevegone:weeping_witch_forest",
    },
}
REPORT_ROOT = ROOT / "docs/biome_mineral_distribution"
AE2CS_CREATE_RECIPE_GUARD = (
    DATA_ROOT
    / "ae2cs/recipe/mechanical_cutting/polished_rose_quartz_from_pure_rose_quartz.json"
)
EXPECTED_AE2CS_CREATE_RECIPE = {
    "neoforge:conditions": [
        {
            "type": "neoforge:mod_loaded",
            "modid": "create",
        }
    ],
    "type": "create:cutting",
    "ingredients": [{"item": "ae2cs:purified_rose_quartz"}],
    "processing_time": 20,
    "results": [{"id": "create:polished_rose_quartz"}],
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
OPTIONAL_EXTERNAL_ORE_PLACED_FEATURES = frozenset(
    {
        "ae2cs:certus_quartz_ore_placed",
        "ae2cs:charged_certus_quartz_ore_placed",
    }
)


def read_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as source:
        return json.load(source)


def validate_mine_profiles(failures: list[str]) -> int:
    tag_files = {path.stem: path for path in MINE_PROFILE_TAGS.glob("*.json") if path.stem != "specialized"}
    definition_files = {path.stem: path for path in MINE_PROFILE_DEFINITIONS.glob("*.json")}
    recipe_root = DATA / "recipe/mineral"
    ioe_recipe_files = {path.stem: path for path in recipe_root.glob("*.json")}
    aquatic_recipe_root = DATA_ROOT / "immersiveengineering/recipe/mineral"
    aquatic_recipe_files = {path.stem: path for path in aquatic_recipe_root.glob("*.json")}
    recipe_files = ioe_recipe_files | aquatic_recipe_files
    direct_biome_owners: dict[str, str] = {}
    if tag_files.keys() != definition_files.keys():
        failures.append(
            "mine profile tag/definition ids differ: "
            f"tags={sorted(tag_files)}, definitions={sorted(definition_files)}"
        )
    if set(definition_files) != EXPECTED_MINE_PROFILES:
        failures.append(
            "built-in mine profile ids differ from the canonical set: "
            f"actual={sorted(definition_files)}, expected={sorted(EXPECTED_MINE_PROFILES)}"
        )
    if set(ioe_recipe_files) != EXPECTED_MINE_PROFILES:
        failures.append(f"IOE mineral mix recipe ids differ from the canonical set: {sorted(ioe_recipe_files)}")
    if set(aquatic_recipe_files) != EXPECTED_AQUATIC_MIXES:
        failures.append(
            f"IE aquatic override recipe ids differ from the canonical set: {sorted(aquatic_recipe_files)}"
        )

    for profile_id in sorted(tag_files.keys() | definition_files.keys()):
        tag_path = tag_files.get(profile_id)
        definition_path = definition_files.get(profile_id)
        if tag_path is None or definition_path is None:
            continue
        tag = read_json(tag_path)
        direct_values: set[str] = set()
        if not isinstance(tag.get("values"), list) or not tag["values"]:
            failures.append(f"empty mine profile biome tag: {tag_path.relative_to(ROOT)}")
        else:
            for entry in tag["values"]:
                biome_id = entry if isinstance(entry, str) else entry.get("id") if isinstance(entry, dict) else None
                if not isinstance(biome_id, str) or biome_id.startswith("#"):
                    continue
                direct_values.add(biome_id)
                previous_owner = direct_biome_owners.setdefault(biome_id, profile_id)
                if previous_owner != profile_id:
                    failures.append(
                        f"biome {biome_id} belongs directly to both {previous_owner} and {profile_id} profiles"
                    )
        if profile_id in EXPECTED_RARE_ALLOWLISTS and direct_values != EXPECTED_RARE_ALLOWLISTS[profile_id]:
            failures.append(f"rare allowlist drift for {profile_id}: {sorted(direct_values)}")

        definition = read_json(definition_path)
        expected_definition_keys = {"biome_tag", "mineral_mix", "resource_name", "survey_radius_chunks", "deposit_tiers"}
        if set(definition) != expected_definition_keys:
            failures.append(f"mine profile {profile_id} has invalid top-level fields")
        expected_tag = f"immersive_ore_expedition:ore_profile/{profile_id}"
        if definition.get("biome_tag") != expected_tag:
            failures.append(f"mine profile {profile_id} points at wrong biome tag")
        expected_mix = f"immersive_ore_expedition:mineral/{profile_id}"
        if definition.get("mineral_mix") != expected_mix:
            failures.append(f"mine profile {profile_id} must select exactly {expected_mix}")
        if definition.get("resource_name") != profile_id:
            failures.append(f"mine profile {profile_id} must use resource_name={profile_id!r}")
        radius = definition.get("survey_radius_chunks")
        if not isinstance(radius, int) or isinstance(radius, bool) or not 1 <= radius <= 16:
            failures.append(f"mine profile {profile_id} has invalid survey radius={radius!r}")
        tiers = definition.get("deposit_tiers")
        if not isinstance(tiers, dict) or tuple(tiers) != EXPECTED_TIERS:
            failures.append(f"mine profile {profile_id} must define ordered tiers {EXPECTED_TIERS}")
            continue
        previous_radius = previous_capacity = 1_000_001
        for tier_name in EXPECTED_TIERS:
            tier = tiers[tier_name]
            if not isinstance(tier, dict) or set(tier) != {"radius_blocks", "capacity"}:
                failures.append(f"mine profile {profile_id}.{tier_name} has invalid tier fields")
                continue
            tier_radius = tier.get("radius_blocks")
            capacity = tier.get("capacity")
            if not isinstance(tier_radius, int) or not 1 <= tier_radius <= 128:
                failures.append(f"mine profile {profile_id}.{tier_name} has invalid radius")
            if not isinstance(capacity, int) or not 1 <= capacity <= 1_000_000:
                failures.append(f"mine profile {profile_id}.{tier_name} has invalid capacity")
            if isinstance(tier_radius, int) and tier_radius > previous_radius:
                failures.append(f"mine profile {profile_id} radius increases during fallback")
            if isinstance(capacity, int) and capacity > previous_capacity:
                failures.append(f"mine profile {profile_id} capacity increases during fallback")
            previous_radius, previous_capacity = tier_radius, capacity

    aquatic_tags = DATA / "tags/worldgen/biome/aquatic"
    for aquatic_id in EXPECTED_AQUATIC_MIXES:
        tag_path = aquatic_tags / f"{aquatic_id}.json"
        if not tag_path.is_file():
            failures.append(f"missing aquatic biome tag {aquatic_id}")
            continue
        for entry in read_json(tag_path).get("values", []):
            biome_id = entry if isinstance(entry, str) else entry.get("id") if isinstance(entry, dict) else None
            if isinstance(biome_id, str):
                previous_owner = direct_biome_owners.setdefault(biome_id, f"aquatic/{aquatic_id}")
                if previous_owner != f"aquatic/{aquatic_id}":
                    failures.append(f"biome {biome_id} overlaps {previous_owner} and aquatic/{aquatic_id}")

    for mix_id, recipe_path in sorted(recipe_files.items()):
        recipe = read_json(recipe_path)
        if recipe.get("type") != "immersiveengineering:mineral_mix":
            failures.append(f"{mix_id} is not an IE mineral_mix recipe")
        conditions = recipe.get("neoforge:conditions", [])
        if {"type": "neoforge:mod_loaded", "modid": "immersiveengineering"} not in conditions:
            failures.append(f"{mix_id} lacks the Immersive Engineering load condition")
        total = sum(Decimal(str(ore.get("chance"))) for ore in recipe.get("ores", []))
        if total != Decimal("1.0"):
            failures.append(f"{mix_id} ore chance sum is {total}, expected exactly 1.0")
        if mix_id == "alluvial_sift":
            diamond_outputs = [
                ore for ore in recipe.get("ores", [])
                if ore.get("output", {}).get("tag") == "c:gems/diamond"
            ]
            if len(diamond_outputs) != 1 or Decimal(str(diamond_outputs[0].get("chance"))) != Decimal("0.2"):
                failures.append("Alluvial Sift must retain exactly 20 percent diamond")
        expected_aquatic_spoils = {
            "alluvial_sift": [("minecraft:gravel", "0.6"), ("minecraft:cobblestone", "0.3"), ("minecraft:coarse_dirt", "0.1")],
            "silt": [("minecraft:gravel", "0.6"), ("minecraft:cobblestone", "0.3"), ("minecraft:coarse_dirt", "0.1")],
            "ancient_seabed": [("minecraft:sandstone", "0.6"), ("minecraft:gravel", "0.3"), ("minecraft:sand", "0.1")],
        }
        if mix_id in expected_aquatic_spoils:
            actual_spoils = [
                (spoil.get("output", {}).get("id"), str(Decimal(str(spoil.get("chance")))))
                for spoil in recipe.get("spoils", [])
            ]
            if actual_spoils != expected_aquatic_spoils[mix_id]:
                failures.append(f"{mix_id} does not preserve the official IE spoils")
        expected_tag = (
            f"immersive_ore_expedition:aquatic/{mix_id}"
            if mix_id in EXPECTED_AQUATIC_MIXES
            else f"immersive_ore_expedition:ore_profile/{mix_id}"
        )
        if recipe.get("biome_predicates") != [["minecraft:is_overworld"], [expected_tag]]:
            failures.append(f"{mix_id} has invalid exclusive biome predicates")

    manifest_path = REPORT_ROOT / "biome_distribution_manifest.json"
    matrix_path = REPORT_ROOT / "biome_profile_matrix.csv"
    mix_matrix_path = REPORT_ROOT / "mineral_mix_matrix.csv"
    registered_path = REPORT_ROOT / "registered_biomes.csv"
    active_path = REPORT_ROOT / "active_biomes.csv"
    removed_report_path = REPORT_ROOT / "ru_removed_biomes.csv"
    aquatic_matrix_path = REPORT_ROOT / "aquatic_deposit_matrix.csv"
    candidate_model_path = REPORT_ROOT / "multi_seed_candidate_model.json"
    required_report_paths = (
        manifest_path,
        matrix_path,
        mix_matrix_path,
        registered_path,
        active_path,
        removed_report_path,
        aquatic_matrix_path,
        candidate_model_path,
    )
    if not all(path.is_file() for path in required_report_paths):
        failures.append("one or more mandatory generated biome-distribution deliverables are missing")
    else:
        manifest = read_json(manifest_path)
        with matrix_path.open("r", encoding="utf-8", newline="") as handle:
            rows = list(csv.DictReader(handle))
        with mix_matrix_path.open("r", encoding="utf-8", newline="") as handle:
            mix_rows = list(csv.DictReader(handle))
        with registered_path.open("r", encoding="utf-8", newline="") as handle:
            registered_rows = list(csv.DictReader(handle))
        with active_path.open("r", encoding="utf-8", newline="") as handle:
            active_rows = list(csv.DictReader(handle))
        with removed_report_path.open("r", encoding="utf-8", newline="") as handle:
            removed_rows = list(csv.DictReader(handle))
        with aquatic_matrix_path.open("r", encoding="utf-8", newline="") as handle:
            aquatic_rows = list(csv.DictReader(handle))
        candidate_model = read_json(candidate_model_path)
        if len(rows) != 252 or len({row["biome"] for row in rows}) != 252:
            failures.append("active biome matrix must contain exactly 252 unique rows")
        expected_registered_namespaces = {
            "minecraft": 64,
            "biomesoplenty": 69,
            "regions_unexplored": 78,
            "biomeswevegone": 55,
        }
        actual_registered_namespaces = {
            namespace: sum(1 for row in registered_rows if row["namespace"] == namespace)
            for namespace in expected_registered_namespaces
        }
        if (
            len(registered_rows) != 266
            or len({row["biome"] for row in registered_rows}) != 266
            or actual_registered_namespaces != expected_registered_namespaces
        ):
            failures.append("registered biome inventory must remain 64 Vanilla / 69 BOP / 78 RU / 55 BWG")
        expected_active_namespaces = {
            "minecraft": 64,
            "biomesoplenty": 69,
            "regions_unexplored": 64,
            "biomeswevegone": 55,
        }
        actual_active_namespaces = {
            namespace: sum(1 for row in active_rows if row["namespace"] == namespace)
            for namespace in expected_active_namespaces
        }
        if (
            len(active_rows) != 252
            or len({row["biome"] for row in active_rows}) != 252
            or actual_active_namespaces != expected_active_namespaces
            or {row["biome"] for row in active_rows} != {row["biome"] for row in rows}
        ):
            failures.append("active biome inventory must match the 252-row outcome matrix exactly")
        if manifest.get("counts", {}).get("ru_registered") != 78 or manifest["counts"].get("ru_removed") != 14 or manifest["counts"].get("ru_active") != 64:
            failures.append("manifest RU inventory must remain 78 registered / 14 removed / 64 active")
        if manifest.get("counts", {}).get("bwg_active") != 55:
            failures.append("manifest BWG inventory must remain 55 active")
        removed = set(manifest.get("ru_removed", []))
        assigned = set(direct_biome_owners)
        if (
            len(removed_rows) != 14
            or {row["biome"] for row in removed_rows} != removed
            or any(row["status"] != "removed" for row in removed_rows)
        ):
            failures.append("separate RU removed report must exactly match all 14 manifest entries")
        if removed & assigned:
            failures.append(f"removed RU biomes are assigned: {sorted(removed & assigned)}")
        if (
            len(aquatic_rows) != 30
            or {row["biome"] for row in aquatic_rows}
            != {row["biome"] for row in rows if row["outcome"] == "aquatic"}
            or any(row["outcome"] != "aquatic" for row in aquatic_rows)
        ):
            failures.append("aquatic deposit matrix must exactly cover the 30 aquatic-only active biomes")
        role_columns = {
            "principal_outputs", "secondary_outputs", "trace_outputs", "host_rock_outputs"
        }
        if len(mix_rows) != 18 or not mix_rows or not role_columns.issubset(mix_rows[0]):
            failures.append("mineral mix matrix must contain 18 rows and explicit output-role columns")
        else:
            for row in mix_rows:
                composition = json.loads(row["composition"])
                role_outputs = [
                    output
                    for column in sorted(role_columns)
                    for output in json.loads(row[column])
                ]
                canonical_composition = sorted(
                    json.dumps(output, sort_keys=True, separators=(",", ":"))
                    for output in composition
                )
                canonical_roles = sorted(
                    json.dumps(output, sort_keys=True, separators=(",", ":"))
                    for output in role_outputs
                )
                if canonical_roles != canonical_composition:
                    failures.append(
                        f"{row['mineral_mix']} output roles do not partition its composition exactly once"
                    )
                if not json.loads(row["principal_outputs"]):
                    failures.append(f"{row['mineral_mix']} has no principal output in the report matrix")
                if row["mineral_mix"] == "immersiveengineering:mineral/alluvial_sift":
                    expected_secondary_diamond = {
                        "chance": 0.2,
                        "output": {"tag": "c:gems/diamond"},
                    }
                    if expected_secondary_diamond not in json.loads(row["secondary_outputs"]):
                        failures.append("Alluvial Sift must report its official 20% diamond as secondary")
        expected_role_keys = EXPECTED_MINE_PROFILES | EXPECTED_AQUATIC_MIXES
        if set(manifest.get("mineral_mix_output_roles", {})) != expected_role_keys:
            failures.append("manifest mineral mix output-role map does not cover all 18 deposits")
        if (
            candidate_model.get("classification") != "SUPPORTED_INFERENCE"
            or len(candidate_model.get("seeds", [])) != 10
        ):
            failures.append("candidate multi-seed model must remain a 10-seed SUPPORTED_INFERENCE")

    feature_source = (ROOT / "src/main/java/com/oblixorprime/ioe/worldgen/ExpeditionSiteFeature.java").read_text(encoding="utf-8")
    required_feature_paths = (
        "previewPlan.chamberCenter()",
        "List.copyOf(fallbackPlans)",
        "planWithEmbeddedResource",
        "structureOnlyPlan",
        "GeOreNodeIntegration.resolve",
        "Ae2MeteoriteIntegration.resolve",
        "ExtendedAeGeodeIntegration.resolve",
    )
    for required in required_feature_paths:
        if required not in feature_source:
            failures.append(f"ExpeditionSiteFeature is missing required connected resource path {required}")

    pending_source = (ROOT / "src/main/java/com/oblixorprime/ioe/worldgen/IoePendingExpeditionSites.java").read_text(
        encoding="utf-8"
    )
    for required in (
        "for (ExpeditionSiteBlockPlan fallbackPlan : fallbackPlans)",
        "depositBackedStructure",
        "DepositReservationFactory",
    ):
        if required not in pending_source:
            failures.append(f"Pending-site transaction is missing required fallback invariant {required}")

    meteorite_overlay = read_json(
        ROOT / "src/main/resources/data/ae2/tags/worldgen/biome/has_meteorites.json"
    )
    if meteorite_overlay != {"replace": False, "values": []}:
        failures.append("AE2 meteorite overlay must preserve the upstream biome tag")

    ore_guard_source = (ROOT / "src/main/java/com/oblixorprime/ioe/worldgen/IoeNewChunkOreGuard.java").read_text(
        encoding="utf-8"
    )
    for forbidden in ("path.equals(\"sky_stone_block\")", "path.startsWith(\"entro_\")"):
        if forbidden in ore_guard_source:
            failures.append(f"New-chunk guard still removes a preserved AE2/ExtendedAE resource: {forbidden}")

    deposit_rule_source = (ROOT / "src/main/java/com/oblixorprime/ioe/worldgen/IoeExcavatorDepositRules.java").read_text(
        encoding="utf-8"
    )
    if "columnContainsSpecializedBiome" not in deposit_rule_source:
        failures.append("Native IE gating does not account for specialized subterranean biomes")

    game_test_source = (ROOT / "src/main/java/com/oblixorprime/ioe/worldgen/ExpeditionWorldgenGameTests.java").read_text(
        encoding="utf-8"
    )
    for required in (
        "ieCoreSampleAndExcavatorUseCommittedIoeDeposit",
        "fullRuntimeBiomeInventoryAndProfileExclusivity",
        "fullRuntimeMultiSeedBiomeFrequency",
        "terraBlenderGeneratorForSeed",
        "terrablender.util.LevelUtils",
        "initializeBiomes",
        "isInitialized",
        "createCoreSample",
        "invokeExcavatorFillBucket",
        "getRandomOre",
        "extendedae\", \"entro_crystal",
        "ieCommitFailuresRunFullChainBeforeLocator",
        "normalAe2MeteoriteBlocksSurviveOreGuard",
    ):
        if required not in game_test_source:
            failures.append(f"Full-runtime GameTest evidence is missing {required}")

    build_source = (ROOT / "build.gradle").read_text(encoding="utf-8")
    if 'systemProperty "ioe.multiSeedReport"' not in build_source:
        failures.append("Full-runtime Gradle run does not configure the multi-seed report path")
    workflow_source = (ROOT.parents[1] / ".github/workflows/ci.yml").read_text(encoding="utf-8")
    for required in (
        "Inspect runtime multi-seed biome frequency",
        "multi-seed-biome-frequency.json",
        "CONFIRMED_RUNTIME",
        "terrablender_initialized_per_seed",
    ):
        if required not in workflow_source:
            failures.append(f"GitHub-hosted runtime evidence gate is missing {required}")
    return len(definition_files)


def validate_ae2cs_create_recipe_guard(failures: list[str]) -> None:
    if not AE2CS_CREATE_RECIPE_GUARD.is_file():
        failures.append(
            f"missing AE2CS/Create recipe guard: {AE2CS_CREATE_RECIPE_GUARD.relative_to(ROOT)}"
        )
        return
    recipe = read_json(AE2CS_CREATE_RECIPE_GUARD)
    if recipe != EXPECTED_AE2CS_CREATE_RECIPE:
        failures.append(
            "AE2CS/Create recipe guard must preserve the upstream recipe and require the create mod"
        )


def validate() -> None:
    failures: list[str] = []
    mine_profile_count = validate_mine_profiles(failures)
    validate_ae2cs_create_recipe_guard(failures)
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
        if tag != {"replace": False, "values": ["#c:is_overworld"]}:
            failures.append(
                f"natural site wrapper must cover the Overworld before chamber-biome filtering: "
                f"{tag_path.relative_to(ROOT)}"
            )

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
        if actual_required_external_ids:
            failures.append(
                "external normal ore placed features must be optional: "
                f"actual={sorted(actual_required_external_ids)}"
            )
        actual_optional_external_ids = {
            feature_id for feature_id in optional_ids if not feature_id.startswith("minecraft:")
        }
        missing_optional_external_ids = sorted(
            OPTIONAL_EXTERNAL_ORE_PLACED_FEATURES - actual_optional_external_ids
        )
        if missing_optional_external_ids:
            failures.append(
                "normal ore tag is missing optional external placed features: "
                f"{missing_optional_external_ids}"
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
        "1 guarded AE2CS/Create recipe, "
        f"{len(MINECRAFT_1_21_1_NORMAL_ORE_PLACED_FEATURES)} vanilla ore/fossil placed features, "
        "1 GameTest template"
    )


if __name__ == "__main__":
    validate()
