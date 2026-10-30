#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
import os
import re
import subprocess
import sys
import zipfile
from pathlib import Path
from typing import Any

import sync_budding_rank_foundation as sync


MODULE_ROOT = Path(__file__).resolve().parents[1]
REPOSITORY_ROOT = MODULE_ROOT.parents[1]
RESOURCES_ROOT = MODULE_ROOT / "src/main/resources"
JAVA_ROOT = MODULE_ROOT / "src/main/java/com/oblixorprime/ioe"


def load_json(path: Path, failures: list[str]) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exception:
        failures.append(f"invalid JSON {path.relative_to(MODULE_ROOT)}: {exception}")
        return {}


def find_runtime_jar(
    cache_root: Path,
    filename: str,
    label: str,
    failures: list[str],
) -> Path | None:
    candidates = sorted(cache_root.rglob(filename)) if cache_root.is_dir() else []
    if len(candidates) != 1:
        failures.append(
            f"expected one pinned {label} JAR under {cache_root}, found {candidates}"
        )
        return None
    return candidates[0]


def run_javap(
    jar_path: Path,
    class_name: str,
    options: list[str],
    failures: list[str],
) -> str:
    result = subprocess.run(
        ["javap", "-classpath", str(jar_path), *options, class_name],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        failures.append(
            f"javap failed for {class_name} in {jar_path} "
            f"with exit {result.returncode}: {result.stderr.strip()}"
        )
        return ""
    return result.stdout


def javap_method(output: str, method_name: str) -> str:
    match = re.search(
        rf"(?m)^  public [^\n]*\b{re.escape(method_name)}\([^\n]*\);?\r?\n"
        rf"(.*?)(?=^\s{{2}}(?:public|private|protected|static) |\Z)",
        output,
        flags=re.DOTALL | re.MULTILINE,
    )
    return match.group(0) if match else ""


def javap_constant(output: str, name: str) -> int | None:
    match = re.search(
        rf"(?m)^\s*public static final int {re.escape(name)} = (\d+);$",
        output,
    )
    return int(match.group(1)) if match else None


def ordered_symbols(text: str, symbols: tuple[str, ...]) -> bool:
    position = 0
    for symbol in symbols:
        found = text.find(symbol, position)
        if found < 0:
            return False
        position = found + len(symbol)
    return True


def inspect_native_bytecode(failures: list[str]) -> dict[str, int]:
    ae2_root = (
        Path.home()
        / ".gradle/caches/modules-2/files-2.1/org.appliedenergistics/"
        "appliedenergistics2/19.2.17"
    )
    geore_root = (
        Path.home()
        / ".gradle/caches/modules-2/files-2.1/maven.modrinth/Xw6zG9hl/OV3DOSul"
    )
    ae2_jar = find_runtime_jar(
        ae2_root,
        "appliedenergistics2-19.2.17.jar",
        "AE2 19.2.17",
        failures,
    )
    geore_jar = find_runtime_jar(
        geore_root,
        "*.jar",
        "GeOre 6.2.2",
        failures,
    )
    if ae2_jar is None or geore_jar is None:
        return {}

    ae2_class = "appeng.decorative.solid.BuddingCertusQuartzBlock"
    ae2_constants = run_javap(
        ae2_jar, ae2_class, ["-constants", "-p"], failures
    )
    ae2_bytecode = run_javap(ae2_jar, ae2_class, ["-c", "-p"], failures)
    ae2_random_tick = javap_method(ae2_bytecode, "randomTick")
    growth_chance = javap_constant(ae2_constants, "GROWTH_CHANCE")
    decay_chance = javap_constant(ae2_constants, "DECAY_CHANCE")
    if growth_chance is None or decay_chance is None:
        failures.append("cannot derive AE2 growth/decay constants from pinned bytecode")
    if not ordered_symbols(
        ae2_random_tick,
        (
            "SMALL_QUARTZ_BUD",
            "MEDIUM_QUARTZ_BUD",
            "LARGE_QUARTZ_BUD",
            "QUARTZ_CLUSTER",
        ),
    ):
        failures.append("AE2 bytecode no longer proves the four-stage growth sequence")
    if not ordered_symbols(
        ae2_random_tick,
        (
            "FLAWLESS_BUDDING_QUARTZ",
            "FLAWED_BUDDING_QUARTZ",
            "CHIPPED_BUDDING_QUARTZ",
            "CHIPPED_BUDDING_QUARTZ",
            "DAMAGED_BUDDING_QUARTZ",
            "DAMAGED_BUDDING_QUARTZ",
            "QUARTZ_BLOCK",
        ),
    ):
        failures.append("AE2 bytecode no longer proves the canonical degradation chain")

    geore_class = "com.shynieke.geore.block.BuddingGeoreBlock"
    geore_bytecode = run_javap(
        geore_jar, geore_class, ["-c", "-p"], failures
    )
    geore_random_tick = javap_method(geore_bytecode, "randomTick")
    geore_growth_match = re.search(
        r"iconst_(\d+).*?RandomSource\.nextInt",
        geore_random_tick,
        flags=re.DOTALL,
    )
    geore_growth_chance = (
        int(geore_growth_match.group(1)) if geore_growth_match else None
    )
    if geore_growth_chance is None:
        failures.append("cannot derive GeOre growth chance from pinned bytecode")
    for supplier in (
        "smallSupplier",
        "mediumSupplier",
        "largeSupplier",
        "clusterSupplier",
    ):
        if supplier not in geore_random_tick:
            failures.append(f"GeOre bytecode is missing growth supplier: {supplier}")

    if (
        growth_chance is not None
        and geore_growth_chance is not None
        and growth_chance != geore_growth_chance
    ):
        failures.append(
            "pinned AE2 and GeOre growth chances differ: "
            f"AE2={growth_chance}, GeOre={geore_growth_chance}"
        )
    return {
        "growth_chance": growth_chance or -1,
        "decay_chance": decay_chance or -1,
    }


def java_method_body(source: str, signature_pattern: str) -> str:
    signature = re.search(signature_pattern, source, flags=re.DOTALL)
    if signature is None:
        return ""
    start = source.find("{", signature.end())
    if start < 0:
        return ""
    depth = 0
    for index in range(start, len(source)):
        character = source[index]
        if character == "{":
            depth += 1
        elif character == "}":
            depth -= 1
            if depth == 0:
                return source[start + 1:index]
    return ""


def parse_optional_rank_switch(method_body: str) -> dict[str, str]:
    return dict(
        re.findall(
            r"case\s+([A-Z]+)\s*->\s*Optional\.of\(([A-Z]+)\)",
            method_body,
        )
    )


def validate_source_contract(
    failures: list[str],
    native_behavior: dict[str, int],
) -> tuple[list[str], list[tuple[str, str]]]:
    materials, ranks = sync.read_source_contract()
    worldgen_source = (
        JAVA_ROOT / "worldgen/GeOreNodeIntegration.java"
    ).read_text(encoding="utf-8")
    allowlist_match = re.search(
        r"SUPPORTED_MATERIALS\s*=\s*Set\.of\((.*?)\);",
        worldgen_source,
        flags=re.DOTALL,
    )
    if allowlist_match is None:
        failures.append("cannot read GeOreNodeIntegration.SUPPORTED_MATERIALS")
    else:
        allowlist = re.findall(r'"([a-z0-9_]+)"', allowlist_match.group(1))
        if materials != allowlist:
            failures.append(
                "budding registration materials differ from the IOE GeOre allowlist: "
                f"registrations={materials}, allowlist={allowlist}"
            )

    rank_source = (JAVA_ROOT / "budding/BuddingRank.java").read_text(
        encoding="utf-8"
    )
    degraded_body = java_method_body(
        rank_source,
        r"public\s+Optional<BuddingRank>\s+degradedRank\s*\(\s*\)",
    )
    restored_body = java_method_body(
        rank_source,
        r"public\s+Optional<BuddingRank>\s+restoredRank\s*\(\s*\)",
    )
    degraded_map = parse_optional_rank_switch(degraded_body)
    restored_map = parse_optional_rank_switch(restored_body)
    if degraded_map != {"FLAWED": "CHIPPED", "CHIPPED": "DAMAGED"}:
        failures.append(f"actual degradation switch is invalid: {degraded_map}")
    if restored_map != {"DAMAGED": "CHIPPED", "CHIPPED": "FLAWED"}:
        failures.append(f"actual restoration switch is invalid: {restored_map}")
    maximum_match = re.search(
        r"MAXIMUM_RESTORABLE\s*=\s*([A-Z]+)", rank_source
    )
    if maximum_match is None or maximum_match.group(1) != "FLAWED":
        failures.append("actual maximum restorable rank is not FLAWED")
    degrade_capability_body = java_method_body(
        rank_source,
        r"public\s+boolean\s+canGrowthDegrade\s*\(\s*\)",
    )
    if not re.fullmatch(
        r"\s*return\s+this\s*!=\s*FLAWLESS\s*;\s*",
        degrade_capability_body,
    ):
        failures.append(
            "actual canGrowthDegrade method does not make only FLAWLESS immutable"
        )

    block_source = (
        JAVA_ROOT / "budding/GeOreRankedBuddingBlock.java"
    ).read_text(encoding="utf-8")
    local_growth_match = re.search(
        r"GROWTH_CHANCE\s*=\s*(\d+)", block_source
    )
    local_decay_match = re.search(r"DECAY_CHANCE\s*=\s*(\d+)", block_source)
    local_growth = int(local_growth_match.group(1)) if local_growth_match else None
    local_decay = int(local_decay_match.group(1)) if local_decay_match else None
    if local_growth != native_behavior.get("growth_chance"):
        failures.append(
            "functional GeOre growth chance differs from pinned AE2/GeOre bytecode: "
            f"local={local_growth}, native={native_behavior.get('growth_chance')}"
        )
    if local_decay != native_behavior.get("decay_chance"):
        failures.append(
            "functional GeOre decay chance differs from pinned AE2 bytecode: "
            f"local={local_decay}, native={native_behavior.get('decay_chance')}"
        )

    random_tick_body = java_method_body(
        block_source,
        r"public\s+void\s+randomTick\s*\([^)]*RandomSource\s+randomSource\s*\)",
    )
    executable_requirements = (
        "randomSource.nextInt(GROWTH_CHANCE)",
        'requireGeOreBlock("small_" + material + "_bud")',
        'requireGeOreBlock("medium_" + material + "_bud")',
        'requireGeOreBlock("large_" + material + "_bud")',
        'requireGeOreBlock(material + "_cluster")',
        "AmethystClusterBlock.WATERLOGGED",
        "rank.canGrowthDegrade()",
        "randomSource.nextInt(DECAY_CHANCE)",
        "rank.degradedRank()",
        'material + "_block"',
    )
    for requirement in executable_requirements:
        if requirement not in random_tick_body:
            failures.append(
                f"actual GeOre randomTick method is missing behavior: {requirement}"
            )
    piston_body = java_method_body(
        block_source,
        r"public\s+PushReaction\s+getPistonPushReaction\s*\([^)]*\)",
    )
    if "return PushReaction.DESTROY;" not in piston_body:
        failures.append("actual GeOre block does not use AE2 piston destruction behavior")

    resolver_source = (
        JAVA_ROOT / "budding/BuddingRankResolver.java"
    ).read_text(encoding="utf-8")
    resolve_body = java_method_body(
        resolver_source,
        r"public\s+static\s+BuddingRankResolution\s+resolve\s*\([^)]*\)",
    )
    for fragment in (
        "RANK_UNSUPPORTED",
        "fully -> mostly -> half -> hardly",
        "ae2:quartz_block",
    ):
        if fragment not in resolver_source:
            failures.append(f"resolver is missing evidence-bearing fragment: {fragment}")
    for fragment in (
        "BuddingResourceFamily.Kind.EXTENDED_AE",
        "BuddingRankResolution.unsupported",
        "BuiltInRegistries.BLOCK.getOptional",
    ):
        if fragment not in resolve_body:
            failures.append(f"actual resolver method is missing: {fragment}")
    return materials, ranks


def validate_generated_resources(
    materials: list[str],
    ranks: list[tuple[str, str]],
    failures: list[str],
) -> None:
    failures.extend(sync.sync(write=False))
    expected = sync.expected_outputs()
    expected_paths = set(expected)
    expected_block_ids = {
        sync.block_id(material, rank_path)
        for material in materials
        for _, rank_path in ranks
    }

    for path in RESOURCES_ROOT.rglob("*.json"):
        load_json(path, failures)

    tag_paths = (
        "data/c/tags/block/budding_blocks.json",
        "data/c/tags/item/budding_blocks.json",
        "data/c/tags/block/relocation_not_supported.json",
        "data/minecraft/tags/block/mineable/pickaxe.json",
        "data/minecraft/tags/block/crystal_sound_blocks.json",
        "data/geore/tags/block/budding_geore.json",
        "data/geore/tags/item/geore_budding.json",
    )
    for relative in tag_paths:
        data = load_json(RESOURCES_ROOT / relative, failures)
        if data.get("replace") is not False:
            failures.append(f"{relative} must merge with replace=false")
        if set(data.get("values", [])) != expected_block_ids:
            failures.append(f"{relative} does not contain exactly the registered rank IDs")

    generated_patterns = (
        "assets/immersive_ore_expedition/blockstates/*_budding_*.json",
        "assets/immersive_ore_expedition/models/block/*_budding_*.json",
        "assets/immersive_ore_expedition/models/item/*_budding_*.json",
        "data/immersive_ore_expedition/loot_table/blocks/*_budding_*.json",
        "data/immersive_ore_expedition/recipe/aggregator/*_budding_*.json",
    )
    for pattern in generated_patterns:
        actual = set(RESOURCES_ROOT.glob(pattern))
        permitted = {path for path in expected_paths if path.match(pattern)}
        extras = sorted(actual - permitted)
        if extras:
            failures.append(
                f"orphan generated resources for {pattern}: "
                f"{[str(path.relative_to(MODULE_ROOT)) for path in extras]}"
            )

    local_budding_textures = sorted(
        (
            RESOURCES_ROOT
            / "assets/immersive_ore_expedition/textures/block"
        ).glob("*budding*.png")
    )
    if local_budding_textures:
        failures.append(
            "rank models must reference GeOre MIT textures without copied PNGs: "
            f"{local_budding_textures}"
        )


def false_condition(data: dict[str, Any]) -> bool:
    return any(
        isinstance(condition, dict) and condition.get("type") == "neoforge:false"
        for condition in data.get("neoforge:conditions", [])
    )


def validate_flawless_exclusivity(
    materials: list[str],
    failures: list[str],
) -> None:
    disabled_paths = (
        RESOURCES_ROOT
        / "data/ae2cs/recipe/aggregator/flawless_budding_quartz.json",
        RESOURCES_ROOT / "data/extendedae/recipe/fixer/certus_flawed.json",
        RESOURCES_ROOT / "data/extendedae/recipe/fixer/entro_mostly.json",
    )
    for path in disabled_paths:
        data = load_json(path, failures)
        if not false_condition(data):
            failures.append(
                f"top-rank dependency recipe is not disabled: {path.relative_to(MODULE_ROOT)}"
            )

    forbidden_outputs = {
        "ae2:flawless_budding_quartz",
        "extendedae:entro_budding_fully",
        *(sync.block_id(material, "flawless") for material in materials),
    }
    for path in RESOURCES_ROOT.rglob("*.json"):
        if "/recipe/" not in path.as_posix():
            continue
        data = load_json(path, failures)
        if false_condition(data):
            continue
        produced_ids: set[str] = set()
        for key in ("result", "output"):
            result = data.get(key)
            if isinstance(result, dict):
                identifier = result.get("id") or result.get("item")
                if isinstance(identifier, str):
                    produced_ids.add(identifier)
            elif isinstance(result, str):
                produced_ids.add(result)
        forbidden = sorted(produced_ids & forbidden_outputs)
        if forbidden:
            failures.append(
                f"active recipe produces a top Budding rank "
                f"{path.relative_to(MODULE_ROOT)}: {forbidden}"
            )


def validate_geore_dependency(
    materials: list[str],
    failures: list[str],
) -> None:
    cache_root = (
        Path.home()
        / ".gradle/caches/modules-2/files-2.1/maven.modrinth/Xw6zG9hl/OV3DOSul"
    )
    candidates = sorted(cache_root.rglob("*.jar")) if cache_root.is_dir() else []
    if len(candidates) != 1:
        failures.append(
            f"expected one pinned GeOre 6.2.2 JAR under {cache_root}, found {candidates}"
        )
        return

    with zipfile.ZipFile(candidates[0]) as jar:
        entries = set(jar.namelist())
    for material in materials:
        required = {
            f"assets/geore/textures/block/{material}_block.png",
            f"assets/geore/textures/block/budding_{material}.png",
            f"assets/geore/blockstates/budding_{material}.json",
            f"assets/geore/blockstates/small_{material}_bud.json",
            f"assets/geore/blockstates/medium_{material}_bud.json",
            f"assets/geore/blockstates/large_{material}_bud.json",
            f"assets/geore/blockstates/{material}_cluster.json",
            f"data/geore/loot_table/blocks/{material}_cluster.json",
        }
        missing = sorted(required - entries)
        if missing:
            failures.append(f"GeOre dependency lacks {material} resources: {missing}")


def validate_ae2cs_dependency(failures: list[str]) -> None:
    cache_root = (
        Path.home()
        / ".gradle/caches/modules-2/files-2.1/maven.modrinth/uJ9afomy/PP2uuQ6t"
    )
    candidates = sorted(cache_root.rglob("*.jar")) if cache_root.is_dir() else []
    if len(candidates) != 1:
        failures.append(
            f"expected one pinned AE2CS 1.1.12 JAR under {cache_root}, found {candidates}"
        )
        return

    required_entries = {
        "io/github/lounode/ae2cs/common/recipe/crystal_aggregator/"
        "CrystalAggregatorRecipeSerializer.class",
        "assets/ae2cs/models/item/resonating_seed.json",
        "data/ae2cs/recipe/craft/shapeless/resonating_seed.json",
        "data/ae2cs/tags/item/pure_crystal/resonating_crystal.json",
        "data/ae2cs/recipe/aggregator/damaged_budding_quartz.json",
        "data/ae2cs/recipe/aggregator/flawless_budding_quartz.json",
    }
    with zipfile.ZipFile(candidates[0]) as jar:
        entries = set(jar.namelist())
        missing = sorted(required_entries - entries)
        if missing:
            failures.append(f"AE2CS dependency lacks required recipe resources: {missing}")
            return

        resonating_seed = json.loads(
            jar.read(
                "data/ae2cs/recipe/craft/shapeless/resonating_seed.json"
            ).decode("utf-8")
        )
        resonating_tag = json.loads(
            jar.read(
                "data/ae2cs/tags/item/pure_crystal/resonating_crystal.json"
            ).decode("utf-8")
        )
        damaged_recipe = json.loads(
            jar.read(
                "data/ae2cs/recipe/aggregator/damaged_budding_quartz.json"
            ).decode("utf-8")
        )
        flawless_recipe = json.loads(
            jar.read(
                "data/ae2cs/recipe/aggregator/flawless_budding_quartz.json"
            ).decode("utf-8")
        )

    if resonating_seed.get("result", {}).get("id") != "ae2cs:resonating_seed":
        failures.append("AE2CS does not define the required resonating_seed result")
    if "ae2cs:purified_resonating_crystal" not in resonating_tag.get("values", []):
        failures.append(
            "AE2CS resonating crystal tag lacks purified_resonating_crystal"
        )
    if (
        damaged_recipe.get("type")
        != "ae2cs:crystal_aggregator_recipe_serializer"
        or damaged_recipe.get("energy_cost") != 16000
    ):
        failures.append(
            "AE2CS native Damaged recipe no longer proves the 16000-energy "
            "crystal aggregator schema"
        )
    if (
        flawless_recipe.get("result", {}).get("id")
        != "ae2:flawless_budding_quartz"
    ):
        failures.append(
            "AE2CS Flawless producer no longer matches its disabled override"
        )


def validate_extendedae_dependency(failures: list[str]) -> None:
    manifest = json.loads(
        (MODULE_ROOT / "ci/runtime-mods.json").read_text(encoding="utf-8")
    )
    matches = [
        entry
        for entry in manifest.get("files", [])
        if entry.get("mod_id") == "extendedae"
    ]
    if len(matches) != 1:
        failures.append(
            f"runtime manifest must contain one ExtendedAE pin, found {matches}"
        )
        return
    pin = matches[0]

    configured_path = os.environ.get("IOE_EXTENDEDAE_JAR")
    jar_path = (
        Path(configured_path)
        if configured_path
        else MODULE_ROOT / "run/gametest-full/mods" / pin["filename"]
    )
    if not jar_path.is_file():
        failures.append(
            "pinned ExtendedAE JAR is unavailable; stage CI runtime mods or set "
            f"IOE_EXTENDEDAE_JAR: {jar_path}"
        )
        return

    actual_sha512 = hashlib.sha512(jar_path.read_bytes()).hexdigest()
    if actual_sha512.lower() != pin["sha512"].lower():
        failures.append(
            f"ExtendedAE SHA-512 differs from ci/runtime-mods.json: {actual_sha512}"
        )
        return

    required_entries = {
        "com/glodblock/github/extendedae/common/blocks/BlockBuddingEntro.class",
        "assets/extendedae/blockstates/entro_budding_hardly.json",
        "assets/extendedae/blockstates/entro_budding_half.json",
        "assets/extendedae/blockstates/entro_budding_mostly.json",
        "assets/extendedae/blockstates/entro_budding_fully.json",
        "assets/extendedae/blockstates/entro_cluster_small.json",
        "assets/extendedae/blockstates/entro_cluster_medium.json",
        "assets/extendedae/blockstates/entro_cluster_large.json",
        "assets/extendedae/blockstates/entro_cluster.json",
        "data/extendedae/recipe/fixer/certus_flawed.json",
        "data/extendedae/recipe/fixer/entro_mostly.json",
    }
    with zipfile.ZipFile(jar_path) as jar:
        entries = set(jar.namelist())
        missing = sorted(required_entries - entries)
        if missing:
            failures.append(f"ExtendedAE dependency lacks ranked resources: {missing}")
            return
        certus_recipe = json.loads(
            jar.read(
                "data/extendedae/recipe/fixer/certus_flawed.json"
            ).decode("utf-8")
        )
        entro_recipe = json.loads(
            jar.read(
                "data/extendedae/recipe/fixer/entro_mostly.json"
            ).decode("utf-8")
        )

    class_name = "com.glodblock.github.extendedae.common.blocks.BlockBuddingEntro"
    constants = run_javap(jar_path, class_name, ["-constants", "-p"], failures)
    bytecode = run_javap(jar_path, class_name, ["-c", "-p"], failures)
    growth_chance = javap_constant(constants, "GROWTH_CHANCE")
    decay_chance = javap_constant(constants, "DECAY_CHANCE")
    if growth_chance != 3 or decay_chance != 10:
        failures.append(
            "ExtendedAE pinned bytecode no longer has the audited 1/3 growth "
            f"and 1/10 decay chances: growth={growth_chance}, decay={decay_chance}"
        )

    random_tick = javap_method(bytecode, "randomTick")
    degradation = javap_method(bytecode, "degradeBudding")
    if "degradeBudding" not in random_tick:
        failures.append(
            "ExtendedAE randomTick bytecode no longer invokes rank degradation"
        )
    degradation_chain = (
        "FULLY_ENTROIZED_FLUIX_BUDDING",
        "MOSTLY_ENTROIZED_FLUIX_BUDDING",
        "MOSTLY_ENTROIZED_FLUIX_BUDDING",
        "HALF_ENTROIZED_FLUIX_BUDDING",
        "HALF_ENTROIZED_FLUIX_BUDDING",
        "HARDLY_ENTROIZED_FLUIX_BUDDING",
        "QUARTZ_BLOCK",
    )
    if not ordered_symbols(degradation, degradation_chain):
        failures.append(
            "ExtendedAE bytecode no longer proves "
            "fully -> mostly -> half -> hardly -> ae2:quartz_block"
        )
    if certus_recipe.get("output", {}).get("id") != "ae2:flawless_budding_quartz":
        failures.append("ExtendedAE certus fixer no longer matches its disabled override")
    if entro_recipe.get("output", {}).get("id") != "extendedae:entro_budding_fully":
        failures.append("ExtendedAE Entro fixer no longer matches its disabled override")


def validate_no_worldgen_changes(failures: list[str]) -> None:
    status_result = subprocess.run(
        ["git", "status", "--porcelain=v1", "--untracked-files=all"],
        cwd=REPOSITORY_ROOT,
        check=False,
        capture_output=True,
        text=True,
    )
    if status_result.returncode != 0:
        failures.append(
            f"git status failed with exit {status_result.returncode}: "
            f"{status_result.stderr}"
        )
        return

    forbidden_fragments = (
        "/worldgen/",
        "/structure/",
        "/structures/",
        "/neoforge/biome_modifier/",
    )
    allowed_runtime_harness = {
        "ioe_project_packs/immersive_ore_expedition/src/main/java/"
        "com/oblixorprime/ioe/worldgen/Surface02BuddingGameTests.java",
        "ioe_project_packs/immersive_ore_expedition/src/visualQa/java/"
        "com/oblixorprime/ioe/worldgen/Surface02VisualQaCommands.java",
    }
    changed_paths = {
        line[3:].replace("\\", "/")
        for line in status_result.stdout.splitlines()
        if len(line) >= 4
    }

    base_ref = os.environ.get("IOE_SCOPE_BASE_REF", "").strip()
    fallback_base_ref = os.environ.get(
        "IOE_SCOPE_FALLBACK_BASE_REF", ""
    ).strip()
    if base_ref and set(base_ref) == {"0"}:
        base_ref = ""
    if not base_ref:
        base_ref = fallback_base_ref
    if not base_ref and os.environ.get("GITHUB_ACTIONS") == "true":
        failures.append(
            "hosted CI did not provide IOE_SCOPE_BASE_REF or "
            "IOE_SCOPE_FALLBACK_BASE_REF; refusing an incomplete scope check"
        )
        return
    if base_ref:
        diff_result = subprocess.run(
            [
                "git",
                "diff",
                "--no-renames",
                "--name-only",
                "--diff-filter=ACDMRTUXB",
                f"{base_ref}...HEAD",
            ],
            cwd=REPOSITORY_ROOT,
            check=False,
            capture_output=True,
            text=True,
        )
        if diff_result.returncode != 0:
            failures.append(
                f"git diff from scope base {base_ref} failed with exit "
                f"{diff_result.returncode}: {diff_result.stderr.strip()}"
            )
            return
        changed_paths.update(
            path.replace("\\", "/")
            for path in diff_result.stdout.splitlines()
            if path
        )

    forbidden = sorted(
        path for path in changed_paths
        if path not in allowed_runtime_harness
        if any(fragment in f"/{path}" for fragment in forbidden_fragments)
    )
    if forbidden:
        failures.append(f"worldgen or structure files changed outside scope: {forbidden}")


def main() -> int:
    failures: list[str] = []
    native_behavior = inspect_native_bytecode(failures)
    materials, ranks = validate_source_contract(failures, native_behavior)
    validate_generated_resources(materials, ranks, failures)
    validate_flawless_exclusivity(materials, failures)
    validate_geore_dependency(materials, failures)
    validate_ae2cs_dependency(failures)
    validate_extendedae_dependency(failures)
    validate_no_worldgen_changes(failures)

    if failures:
        print("Budding rank foundation validation failed:")
        for failure in failures:
            print(f"- {failure}")
        return 1

    print("Budding rank foundation validation passed.")
    print(f"GeOre materials: {len(materials)}")
    print(f"Canonical ranks: {len(ranks)}")
    print(f"Registered GeOre rank blocks: {len(materials) * len(ranks)}")
    print(f"Restoration recipes: {len(materials) * 3}")
    print("Certus: native AE2 mapping")
    print("ExtendedAE: RANK_UNSUPPORTED (native fully rank degrades)")
    print("Active top-rank data recipes: 0")
    return 0


if __name__ == "__main__":
    sys.exit(main())
