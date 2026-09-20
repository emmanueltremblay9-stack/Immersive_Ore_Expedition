#!/usr/bin/env python3
"""Static contract validation for the IOE prospector camp and optional Domum integration."""

from __future__ import annotations

import csv
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/oblixorprime/ioe"
RESOURCES = ROOT / "src/main/resources"
DOCS = ROOT / "docs/worldgen/prospector_camp"

QUALITY_CONTRACT = {
    "DRY": (9, 9, 4, 0, 0),
    "POOR": (11, 11, 5, 1, 7),
    "NORMAL": (13, 13, 6, 2, 12),
    "RICH": (15, 15, 7, 2, 18),
    "MOTHERLODE": (17, 15, 8, 2, 24),
}
PLANNED_AIR = {
    "DRY": {"ground": 30, "raised": 32},
    "POOR": {"ground": 38, "raised": 37},
    "NORMAL": {"ground": 56, "raised": 53},
    "RICH": {"ground": 91, "raised": 91},
    "MOTHERLODE": {"ground": 114, "raised": 114},
}
SHELTER_SHELL = {
    "DRY": {"ground": 21, "raised": 15},
    "POOR": {"ground": 31, "raised": 27},
    "NORMAL": {"ground": 45, "raised": 41},
    "RICH": {"ground": 68, "raised": 64},
    "MOTHERLODE": {"ground": 95, "raised": 91},
}
SHELTER_DIMENSIONS = {
    "DRY": (3, 2),
    "POOR": (3, 3),
    "NORMAL": (5, 3),
    "RICH": (6, 4),
    "MOTHERLODE": (7, 5),
}
VISUAL_FAMILIES = {
    "TEMPERATE", "CONIFER", "SNOWY", "WETLAND",
    "TROPICAL", "ARID", "ROCKY", "VOLCANIC",
}
VISUAL_FAMILY_ORDINAL = {
    "SNOWY": 1,
    "VOLCANIC": 2,
    "ARID": 3,
    "WETLAND": 4,
    "TROPICAL": 5,
    "ROCKY": 6,
    "CONIFER": 7,
    "TEMPERATE": 8,
}
QUALITY_ORDINAL = {quality: index for index, quality in enumerate(QUALITY_CONTRACT)}
VEGETATION_COUNT = {
    "TEMPERATE": 2,
    "CONIFER": 2,
    "SNOWY": 2,
    "WETLAND": 2,
    "TROPICAL": 3,
    "ARID": 1,
    "ROCKY": 1,
    "VOLCANIC": 0,
}
BIOME_GEOMETRY_LABEL = {
    "TEMPERATE": "open_baseline",
    "CONIFER": "two_sided_windbreak_and_log_reserve",
    "SNOWY": "rear_windbreak_and_covered_log_cache",
    "WETLAND": "raised_stable_deck",
    "TROPICAL": "raised_ventilated_lattice",
    "ARID": "upper_shade_valance",
    "ROCKY": "paired_vertical_rock_anchors",
    "VOLCANIC": "compact_stone_screen_and_gear_plinth",
}
ALLOWED_STATES = {
    "DRY": {"ABANDONED", "COLLAPSED"},
    "POOR": {"WEATHERED", "ABANDONED"},
    "NORMAL": {"WEATHERED", "ACTIVE_RECENT"},
    "RICH": {"ACTIVE_RECENT", "WEATHERED"},
    "MOTHERLODE": {"ACTIVE_RECENT", "WEATHERED"},
}
COLLISION_GEOMETRY = {
    "DRY": (4, 3, 3, 2, 1, 0, 1, False, False),
    "POOR": (5, 4, 3, 3, 2, 1, 1, False, False),
    "NORMAL": (6, 5, 5, 3, 3, 2, 2, True, False),
    "RICH": (7, 6, 6, 4, 4, 2, 3, True, True),
    "MOTHERLODE": (7, 7, 7, 5, 5, 2, 4, True, True),
}
ROTATIONS = ("NONE", "CLOCKWISE_90", "CLOCKWISE_180", "COUNTERCLOCKWISE_90")
FORBIDDEN_LOOT = {
    "minecraft:coal", "minecraft:raw_iron", "minecraft:raw_copper", "minecraft:raw_gold",
    "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:gold_ingot",
    "minecraft:diamond", "minecraft:emerald", "minecraft:redstone", "minecraft:lapis_lazuli",
}
ALLOWED_LOOT = {
    "minecraft:torch",
    "minecraft:charcoal",
    "minecraft:bread",
    "minecraft:paper",
    "minecraft:string",
    "minecraft:feather",
    "minecraft:wooden_pickaxe",
}
LOOT_TABLES = {
    "POOR": ("prospector_camp_supplies.json", 1, 2),
    "NORMAL": ("prospector_camp_supplies_normal.json", 2, 3),
    "RICH": ("prospector_camp_supplies_rich.json", 2, 4),
    "MOTHERLODE": ("prospector_camp_supplies_motherlode.json", 3, 4),
}


def read(path: Path) -> str:
    if not path.is_file():
        raise AssertionError(f"missing {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def neutralize_java(source: str) -> str:
    neutralized = list(source)
    index = 0
    while index < len(source):
        if source.startswith("//", index):
            end = source.find("\n", index + 2)
            end = len(source) if end < 0 else end
        elif source.startswith("/*", index):
            close = source.find("*/", index + 2)
            end = len(source) if close < 0 else close + 2
        elif source.startswith('\"\"\"', index):
            end = index + 3
            while end < len(source):
                if source.startswith('\"\"\"', end):
                    end += 3
                    break
                end += 2 if source[end] == "\\" and end + 1 < len(source) else 1
        elif source[index] in {'\"', "'"}:
            quote = source[index]
            end = index + 1
            while end < len(source):
                if source[end] == "\\" and end + 1 < len(source):
                    end += 2
                elif source[end] == quote:
                    end += 1
                    break
                else:
                    end += 1
        else:
            index += 1
            continue
        for ignored in range(index, end):
            if source[ignored] not in "\r\n":
                neutralized[ignored] = " "
        index = end
    return "".join(neutralized)


def java_method_body(source: str, name: str, signature: str) -> str:
    structural_source = neutralize_java(source)
    expected_signature = re.sub(r"\s+", "", signature)
    candidates: list[int] = []
    for match in re.finditer(rf"\b{re.escape(name)}\s*\(", structural_source):
        opening_parenthesis = structural_source.find("(", match.start())
        depth = 1
        cursor = opening_parenthesis + 1
        while cursor < len(structural_source) and depth:
            depth += (structural_source[cursor] == "(") - (structural_source[cursor] == ")")
            cursor += 1
        if depth or re.sub(r"\s+", "", structural_source[opening_parenthesis + 1:cursor - 1]) != expected_signature:
            continue
        while cursor < len(structural_source) and structural_source[cursor].isspace():
            cursor += 1
        if structural_source.startswith("throws", cursor):
            opening_brace = structural_source.find("{", cursor + len("throws"))
            semicolon = structural_source.find(";", cursor + len("throws"))
            if opening_brace < 0 or 0 <= semicolon < opening_brace:
                continue
            cursor = opening_brace
        if cursor >= 0 and cursor < len(structural_source) and structural_source[cursor] == "{":
            candidates.append(cursor)
    label = f"{name}({signature})"
    if not candidates:
        raise AssertionError(f"missing Java method {label}")
    if len(candidates) != 1:
        raise AssertionError(f"ambiguous Java method {label}: {len(candidates)} matches")
    opening_brace = candidates[0]
    depth = 1
    cursor = opening_brace + 1
    while cursor < len(structural_source) and depth:
        depth += (structural_source[cursor] == "{") - (structural_source[cursor] == "}")
        cursor += 1
    if depth:
        raise AssertionError(f"unterminated Java method body {label}")
    return structural_source[opening_brace + 1:cursor - 1]


def java_test_method_body(source: str, name: str) -> str:
    matches = list(re.finditer(rf"@Test\s+void\s+{re.escape(name)}\s*\(\s*\)\s*\{{", neutralize_java(source)))
    if len(matches) != 1:
        raise AssertionError(f"Java test method {name} must have exactly one preceding @Test: {len(matches)} matches")
    return java_method_body(source, name, "")


def synthetic_planned_bounds(body: str, name: str):
    positions = [
        tuple(map(int, match))
        for match in re.findall(r"newBlockPos\((-?\d+),(-?\d+),(-?\d+)\)", body)
    ]
    if len(positions) != 2:
        raise AssertionError(f"{name} must contain exactly two literal BlockPos values")
    raw = tuple(min(position[axis] for position in positions) for axis in range(3)) + tuple(
        max(position[axis] for position in positions) for axis in range(3)
    )
    return raw, tuple(value - 1 for value in raw[:3]) + tuple(value + 1 for value in raw[3:])


def synthetic_collision_fixture(body: str, name: str):
    boxes = [
        tuple(map(int, match))
        for match in re.findall(
            r"newBoundingBox\((-?\d+),(-?\d+),(-?\d+),(-?\d+),(-?\d+),(-?\d+)\)", body
        )
    ]
    if len(boxes) != 1:
        raise AssertionError(f"{name} must contain exactly one literal BoundingBox")
    raw, margin = synthetic_planned_bounds(body, name)
    return raw, margin, boxes[0]


def boxes_intersect(left, right) -> bool:
    return all(left[axis] <= right[axis + 3] and left[axis + 3] >= right[axis] for axis in range(3))


def load_json(path: Path):
    return json.loads(read(path))


def csv_rows(path: Path) -> list[dict[str, str]]:
    return list(csv.DictReader(read(path).splitlines()))


def biome_geometry_count(quality: str, family: str) -> int:
    width, depth = SHELTER_DIMENSIONS[quality]
    return {
        "TEMPERATE": 0,
        "CONIFER": width + depth - 3,
        "SNOWY": width - 1,
        "WETLAND": 0,
        "TROPICAL": (width - 1) // 2,
        "ARID": width - 2,
        "ROCKY": 2 * min(2, width - 2),
        "VOLCANIC": width - 1,
    }[family]


def collect_named_values(value) -> list[str]:
    found: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            if key == "name" and isinstance(child, str):
                found.append(child)
            found.extend(collect_named_values(child))
    elif isinstance(value, list):
        for child in value:
            found.extend(collect_named_values(child))
    return found


def rotate_column(column: tuple[int, int], rotation: str) -> tuple[int, int]:
    x, z = column
    return {
        "NONE": (x, z),
        "CLOCKWISE_90": (-z, x),
        "CLOCKWISE_180": (-x, -z),
        "COUNTERCLOCKWISE_90": (z, -x),
    }[rotation]


def mix64(value: int) -> int:
    mask = (1 << 64) - 1
    value &= mask
    value = ((value ^ (value >> 30)) * 0xBF58476D1CE4E5B9) & mask
    value = ((value ^ (value >> 27)) * 0x94D049BB133111EB) & mask
    return (value ^ (value >> 31)) & mask


def state_for(quality: str, family: str, seed: int) -> str:
    allowed = {
        "DRY": ("ABANDONED", "COLLAPSED"),
        "POOR": ("WEATHERED", "ABANDONED"),
        "NORMAL": ("WEATHERED", "ACTIVE_RECENT"),
        "RICH": ("ACTIVE_RECENT", "WEATHERED"),
        "MOTHERLODE": ("ACTIVE_RECENT", "WEATHERED"),
    }[quality]
    family_salt = ((VISUAL_FAMILY_ORDINAL[family] + 1) * 0x632BE59BD9B4E019) & ((1 << 64) - 1)
    quality_salt = (QUALITY_ORDINAL[quality] * 0xD1B54A32D192ED03) & ((1 << 64) - 1)
    return allowed[mix64(seed ^ family_salt ^ quality_salt) % len(allowed)]


def field_work_candidates(radius: int) -> list[tuple[int, int]]:
    candidates: list[tuple[int, int]] = []
    for distance in range(1, max(1, radius - 2) + 1):
        for x in range(-distance, distance + 1):
            for candidate in ((x, -distance), (x, distance)):
                if candidate not in candidates:
                    candidates.append(candidate)
        for z in range(-distance + 1, distance):
            for candidate in ((-distance, z), (distance, z)):
                if candidate not in candidates:
                    candidates.append(candidate)
    return candidates


def validate_collision_matrix() -> int:
    cases = 0
    for origin_x in (4, 11):
        for origin_z in (6, 7, 8, 9):
            hatch = {
                (origin_x + dx, origin_z + dz)
                for dx in range(-1, 2)
                for dz in range(-1, 2)
            }
            for global_rotation in ROTATIONS:
                def world_column(local: tuple[int, int]) -> tuple[int, int]:
                    rotated_x, rotated_z = rotate_column(local, global_rotation)
                    return 7 + rotated_x, 7 + rotated_z

                for quality, geometry in COLLISION_GEOMETRY.items():
                    (radius, outcrop_width, shelter_width, shelter_depth, marker_count,
                     container_count, sample_count, work, observe) = geometry
                    claims: dict[tuple[int, int], str] = {}

                    def try_claim(columns: list[tuple[int, int]], role: str) -> bool:
                        proposed = [world_column(column) for column in columns]
                        if len(set(proposed)) != len(proposed):
                            return False
                        if any(column in hatch or column in claims for column in proposed):
                            return False
                        claims.update({column: role for column in proposed})
                        return True

                    path: list[tuple[int, int]] = []
                    cursor_x, cursor_z = origin_x, origin_z
                    while cursor_x != 7:
                        path.append((cursor_x, cursor_z))
                        cursor_x += 1 if cursor_x < 7 else -1
                    while cursor_z != 7:
                        path.append((cursor_x, cursor_z))
                        cursor_z += 1 if cursor_z < 7 else -1
                    path.append((7, 7))
                    open_path = {column for column in path if column not in hatch}
                    claims.update({column: "PATH" for column in open_path})

                    outcrop_start = -radius + 1
                    outcrop_depth = max(2, (outcrop_width + 1) // 2)
                    outcrop = [
                        (x, z)
                        for x in range(outcrop_start, outcrop_start + outcrop_width)
                        for z in range(outcrop_start, outcrop_start + outcrop_depth)
                    ]
                    if not any(try_claim([rotate_column(column, rotation) for column in outcrop], "OUTCROP")
                               for rotation in ROTATIONS):
                        raise AssertionError(f"{quality} {global_rotation} has no collision-free outcrop")

                    end_x = radius
                    start_x = end_x - shelter_width + 1
                    start_z = -radius + 1
                    end_z = min(radius - 1, start_z + shelter_depth - 1)
                    shelter = [
                        (x, z)
                        for x in range(start_x, end_x + 1)
                        for z in range(start_z, end_z + 1)
                    ]
                    if not any(try_claim([rotate_column(column, rotation) for column in shelter], "SHELTER")
                               for rotation in ROTATIONS):
                        raise AssertionError(f"{quality} {global_rotation} has no collision-free shelter")

                    candidates = field_work_candidates(radius)

                    def claim_available(count: int, role: str) -> list[tuple[int, int]]:
                        if count == 0:
                            return []
                        selected: list[tuple[int, int]] = []
                        for candidate in candidates:
                            if try_claim([candidate], role):
                                selected.append(candidate)
                                if len(selected) == count:
                                    return selected
                        raise AssertionError(
                            f"{quality} {global_rotation} cannot satisfy the {role} budget"
                        )

                    claim_available(1, "HEARTH")
                    claim_available(2 if work else 0, "WORKSTATION")

                    container_access: dict[tuple[int, int], tuple[int, int]] = {}
                    for _ in range(container_count):
                        selected_container = None
                        for candidate in candidates:
                            world_candidate = world_column(candidate)
                            if world_candidate in hatch or world_candidate in claims:
                                continue
                            for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                                access = candidate[0] + offset_x, candidate[1] + offset_z
                                if abs(access[0]) > radius or abs(access[1]) > radius:
                                    continue
                                world_access = world_column(access)
                                if world_access in hatch:
                                    continue
                                access_role = claims.get(world_access)
                                if access_role in {"PATH", "ACCESS"}:
                                    claims[world_candidate] = "CONTAINER"
                                    selected_container = world_candidate
                                    container_access[world_candidate] = world_access
                                    break
                                if access_role is None:
                                    claims[world_candidate] = "CONTAINER"
                                    claims[world_access] = "ACCESS"
                                    selected_container = world_candidate
                                    container_access[world_candidate] = world_access
                                    break
                            if selected_container is not None:
                                break
                        if selected_container is None:
                            raise AssertionError(
                                f"{quality} {global_rotation} has no accessible container column"
                            )

                    claim_available(sample_count, "SAMPLE")

                    if observe:
                        platform = [
                            (x, z)
                            for x in range(-1, 2)
                            for z in range(radius - 3, radius)
                        ]
                        if not any(try_claim([rotate_column(column, rotation) for column in platform], "OBSERVATION")
                                   for rotation in ROTATIONS):
                            raise AssertionError(f"{quality} {global_rotation} has no collision-free platform")

                    inset = radius - 1
                    perimeter: list[tuple[int, int]] = []
                    for coordinate in range(-inset, inset + 1, 2):
                        for candidate in (
                            (coordinate, inset),
                            (inset, -coordinate),
                            (-coordinate, -inset),
                            (-inset, coordinate),
                        ):
                            if candidate not in perimeter:
                                perimeter.append(candidate)
                    selected_markers = 0
                    for candidate in perimeter:
                        if try_claim([candidate], "MARKER"):
                            selected_markers += 1
                            if selected_markers == marker_count:
                                break
                    if selected_markers != marker_count:
                        raise AssertionError(f"{quality} {global_rotation} has too few marker columns")

                    route_candidates = ((radius, 0), (0, radius), (-radius, 0), (0, -radius))
                    if not any(try_claim([candidate], "ROUTE") for candidate in route_candidates):
                        raise AssertionError(f"{quality} {global_rotation} has no route marker column")
                    claim_available(3, "VEGETATION")
                    if set(claims) & hatch:
                        raise AssertionError(f"{quality} {global_rotation} intersects the hatch")
                    if any(claims.get(column) != "PATH" for column in open_path):
                        raise AssertionError(f"{quality} {global_rotation} blocks the camp path")
                    if any(claims.get(access) not in {"PATH", "ACCESS"}
                           for access in container_access.values()):
                        raise AssertionError(f"{quality} {global_rotation} blocks container access")
                    cases += 1
    return cases


def validate() -> None:
    failures: list[str] = []

    def require(condition: bool, message: str) -> None:
        if not condition:
            failures.append(message)

    quality_source = read(JAVA / "worldgen/ProspectorCampQualitySpec.java")
    family_source = read(JAVA / "worldgen/ProspectorCampVisualFamily.java")
    context_source = read(JAVA / "worldgen/ProspectorCampContext.java")
    composer_source = read(JAVA / "worldgen/ProspectorCampOutcropComposer.java")
    blueprint_source = read(JAVA / "worldgen/ExpeditionSiteBlueprints.java")
    feature_source = read(JAVA / "worldgen/ExpeditionSiteFeature.java")
    plan_source = read(JAVA / "worldgen/ExpeditionSiteBlockPlan.java")
    placement_source = read(JAVA / "worldgen/IoeExpeditionPlanPlacement.java")
    pending_source = read(JAVA / "worldgen/IoePendingExpeditionSites.java")
    locator_source = read(JAVA / "expeditionlocator/ExpeditionLocatorIndex.java")
    compat_source = read(JAVA / "compat/domum/DomumOrnamentumCompat.java")
    adapter_source = read(JAVA / "compat/domum/DomumOrnamentumAdapter.java")
    game_test_source = read(JAVA / "worldgen/ExpeditionWorldgenGameTests.java")
    config_source = read(JAVA / "config/ImmersiveOreExpeditionConfig.java")
    worldgen_config_source = read(JAVA / "worldgen/IoeWorldgenConfig.java")
    placement_gates_source = read(JAVA / "worldgen/IoeWorldgenPlacementGates.java")
    unit_test_source = read(ROOT / "src/test/java/com/oblixorprime/ioe/worldgen/ProspectorCampOutcropComposerTest.java")
    collision_test_source = read(ROOT / "src/test/java/com/oblixorprime/ioe/worldgen/ExpeditionSiteFeatureCollisionTest.java")
    readme_source = read(DOCS / "README.md")
    domum_inventory_source = read(DOCS / "DOMUM_BLOCK_AND_NBT_INVENTORY.md")

    for source_name, source in {
        "quality": quality_source,
        "family": family_source,
        "context": context_source,
        "composer": composer_source,
        "blueprint": blueprint_source,
        "feature": feature_source,
        "plan": plan_source,
        "placement": placement_source,
        "pending transaction": pending_source,
        "locator index": locator_source,
        "compat": compat_source,
        "adapter": adapter_source,
        "game test": game_test_source,
        "config": config_source,
        "worldgen config": worldgen_config_source,
        "placement gates": placement_gates_source,
        "unit test": unit_test_source,
        "collision test": collision_test_source,
    }.items():
        require(source.count("{") == source.count("}"), f"unbalanced Java braces in {source_name}")

    quality_pattern = re.compile(
        r"case\s+(DRY|POOR|NORMAL|RICH|MOTHERLODE)\s+->\s+new ProspectorCampQualitySpec\(([^)]*)\)"
    )
    parsed_quality = {}
    for quality, raw_values in quality_pattern.findall(quality_source):
        values = tuple(int(value.strip()) for value in raw_values.split(","))
        parsed_quality[quality] = (values[0], values[1], values[2], values[8], values[9])
    require(parsed_quality == QUALITY_CONTRACT, f"quality contract mismatch: {parsed_quality}")

    for family in VISUAL_FAMILIES | {"AQUATIC"}:
        require(re.search(rf"\b{family}\b", family_source) is not None, f"missing visual family {family}")
    priority = ["AQUATIC", "SNOWY", "VOLCANIC", "ARID", "WETLAND", "TROPICAL", "ROCKY", "CONIFER"]
    offsets = [family_source.find(f"return {family};") for family in priority]
    require(all(offset >= 0 for offset in offsets) and offsets == sorted(offsets), "visual-family priority changed")
    require("return TEMPERATE;" in family_source, "temperate fallback is missing")

    require(feature_source.count("qualityRoll.roll(context.random())") == 1,
            "natural sites must perform exactly one quality roll")
    require("siteType == ExpeditionSiteType.MINER_CAMP" in feature_source
            and "? SiteQualityRoll.DEFAULT" in feature_source
            and "new SiteQualityRoll(0, 25, 45, 17, 3)" in feature_source,
            "miner camp must expose DRY through DEFAULT while other active surface ids preserve their weights")
    require(feature_source.count("BiomeMineResourceProfile.resolve(") == 1,
            "feature must retain exactly one mineral-profile resolution path")
    require("if (!quality.isProductive())" in feature_source
            and "IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED" in feature_source,
            "DRY must bypass the IE deposit reservation path")
    require("|| !quality.isProductive()" in feature_source,
            "DRY must bypass the optional petroleum reservation path")
    require(blueprint_source.count("ProspectorCampOutcropComposer.compose(") == 1,
            "prospector camp must have one canonical blueprint insertion")
    require("addMinerCamp(" not in blueprint_source, "legacy miner-camp composer remains reachable")
    require("blockEntityPayloads" in plan_source and "blockEntityPayloads" in placement_source,
            "typed block-entity payloads are not carried through placement")
    require("abortPartialPlacement" in placement_source and "applyPayload" in placement_source,
            "payload failures are not covered by the placement compensation path")
    require("hasMaterialPayload(beamPos)" in composer_source
            and "fieldWorkCandidates(spec.placedRadius())" in composer_source
            and "tryClaimAccessibleLocalColumn" in composer_source
            and "reserveOpenWorldColumn" in composer_source
            and "putWorld(worldColumn.above(), Blocks.AIR.defaultBlockState())" in composer_source
            and "standingY + 2" in composer_source
            and "requiresOpenRaisedHeadroomBay" in composer_source
            and "openRaisedHeadroomBay && x == headroomBayX" in composer_source
            and "surfacePos.above(2)" in composer_source
            and "ComponentRole.PATH" in composer_source
            and "ComponentRole.ACCESS" in composer_source
            and "ComponentRole.OUTCROP" in composer_source
            and '"host-rock outcrop"' in composer_source
            and "reservedSurfaceColumns" in composer_source
            and "tryClaimLocalColumns" in composer_source
            and "Shaft hatch overlaps a reserved camp detail" in composer_source,
            "rotation-safe camp details, vertical circulation, or Domum post preservation regressed")
    require("addBiomeVegetation(builder, spec, palette);" in composer_source
            and "addBiomeShelterGeometry" in composer_source
            and "biomeGeometryBlocks" in composer_source
            and "palette.raisedShelter()" in composer_source
            and "Blocks.MOSS_CARPET" in composer_source
            and "Blocks.FERN" in composer_source
            and "Blocks.DEAD_BUSH" in composer_source
            and "Blocks.STRIPPED_SPRUCE_LOG" in composer_source
            and "Blocks.JUNGLE_FENCE" in composer_source
            and "Blocks.CHISELED_POLISHED_BLACKSTONE" in composer_source
            and "LeavesBlock.PERSISTENT" in composer_source
            and "visualFamilySalt" in context_source,
            "biome-dependent vegetation, geometry, or narrative-state selection regressed")
    require("coversEveryAllowedNarrativeStateForEveryQualityAndVisualFamily" in unit_test_source
            and "ProspectorCampOutcropComposer.compose(ORIGIN, quality, context)" in unit_test_source
            and "Snowy spruce-leaf vegetation must not decay" in unit_test_source,
            "narrative-state composition or persistent snowy-vegetation test coverage regressed")
    require(composer_source.index("addObservationPlatform(builder, spec, quality, palette);")
            < composer_source.index("addSurveyMarkers(builder, spec, quality, palette);"),
            "observation reservation must precede perimeter-marker selection")
    require("materialPayloads == qualitySpec.maxDomumAccents()" in game_test_source
            and "composition.domumBlockEntityCount()" in game_test_source,
            "full-runtime GameTest source lost the exact Domum block-entity budget")
    require("void prospectorCampDomumPayloadPlacement" in game_test_source
            and "IoeExpeditionPlanPlacement.apply(level, plan)" in game_test_source
            and "DomumOrnamentumCompat.matchesMaterialPayload(placedBlockEntity, materials)" in game_test_source
            and "saveWithFullMetadata" in game_test_source
            and "BlockEntity.loadStatic" in game_test_source
            and "Block.getDrops" in game_test_source
            and "applied.rollback(level)" in game_test_source,
            "hosted Domum test source does not apply, read back, serialize, recover, and rotate the actual plan")
    require("void minerCampDryProductionPath" in game_test_source
            and "RandomSource.create(DRY_SEED)" in game_test_source
            and "new ExpeditionSiteFeature(ExpeditionSiteType.MINER_CAMP).place(context)" in game_test_source
            and "IoePendingExpeditionSites.confirmLoadedChunk(level, testChunk)" in game_test_source,
            "DRY GameTest source no longer covers Feature.place through final confirmation")
    require("confirmation.rejectedResourcePositions().isEmpty()" in game_test_source
            and "quality == SiteQuality.DRY" in game_test_source,
            "DRY GameTest source no longer checks resource-free locator confirmation")
    require("new int[]{4, 11}" in unit_test_source
            and "new int[]{6, 7, 8, 9}" in unit_test_source
            and "spec.outcropWidth() * Math.max" in unit_test_source
            and "reservedColumnCount" in unit_test_source
            and "coversEveryAllowedNarrativeStateForEveryQualityAndVisualFamily" in unit_test_source
            and "Loot container lacks an open access column" in unit_test_source
            and "The camp center is not open" in unit_test_source,
            "unit-test source lost the legal-anchor/rotation semantic matrix")
    require("siteType == ExpeditionSiteType.MINER_CAMP ? 1 : 2" in feature_source
            and composer_source.count("palette.ground().defaultBlockState()") >= 8
            and "surfacePos.below()" in composer_source,
            "gentle-slope foundation or reserved hatch support regressed")
    require("Blocks.POWDER_SNOW" in feature_source
            and "FluidTags.LAVA" in feature_source
            and "SURFACE_HAZARD_MARGIN" in feature_source,
            "surface hazard scan no longer rejects powder snow and nearby lava")
    require("runtimePlacementEnabled" not in feature_source
            and "naturalExpeditionSiteGenerationEnabled()" in feature_source
            and "runtimePlacementEnabled()" in placement_gates_source
            and "separate naturalExpeditionSiteGenerationEnabled gate" in config_source
            and "legacy" in readme_source.lower(),
            "legacy proof placement and natural-site generation gates are no longer clearly separated")
    require("MINER_CAMP_MINIMUM_SPACING_BLOCKS = 48" in pending_source
            and "hasPlayableAnchorWithinHorizontalDistance" in pending_source
            and "SiteSkipReason.NEIGHBORING_SITE" in pending_source
            and "hasPlayableAnchorWithinHorizontalDistance" in locator_source
            and "dx * dx + dz * dz < minimumDistanceSquared" in locator_source
            and "void prospectorCampRejectsNearbyConfirmedAnchor" in game_test_source,
            "the final transaction no longer enforces or tests the 48-block horizontal camp spacing invariant")
    place_body = java_method_body(
        feature_source, "place", "FeaturePlaceContext<NoneFeatureConfiguration> context"
    )
    resolve_surface_body = java_method_body(
        feature_source,
        "resolveSurfaceOrigin",
        "WorldGenLevel level, BlockPos requestedOrigin, ExpeditionSiteType siteType, SiteQuality quality",
    )
    natural_ground_body = java_method_body(
        feature_source, "isNaturalProspectorCampGround", "BlockState state"
    )
    expanded_bounds_body = java_method_body(
        feature_source, "expandedPlanBounds", "Collection<BlockPos> plannedPositions"
    )
    intersects_bounds_body = java_method_body(
        feature_source,
        "intersectsStructureBounds",
        "Collection<BlockPos> plannedPositions, Collection<BoundingBox> structureBounds",
    )
    collision_body = java_method_body(
        feature_source,
        "collidesWithStructure",
        "WorldGenLevel level, ExpeditionSiteBlockPlan plan",
    )
    place = re.sub(r"\s+", "", place_body)
    resolve_surface = re.sub(r"\s+", "", resolve_surface_body)
    natural_ground = re.sub(r"\s+", "", natural_ground_body)
    expanded_bounds = re.sub(r"\s+", "", expanded_bounds_body)
    intersects_bounds = re.sub(r"\s+", "", intersects_bounds_body)
    collision = re.sub(r"\s+", "", collision_body)

    require("!isNaturalProspectorCampGround(groundState)" in resolve_surface,
            "resolveSurfaceOrigin no longer rejects artificial prospector-camp ground")
    require("BlockTags.BASE_STONE_OVERWORLD" in natural_ground,
            "isNaturalProspectorCampGround no longer accepts overworld base stone")
    require("getStructureWithPieceAt(" not in neutralize_java(feature_source),
            "ExpeditionSiteFeature restored the obsolete per-piece structure lookup")

    require("for(BlockPospos:plannedPositions)" in expanded_bounds,
            "expandedPlanBounds no longer derives bounds from every planned position")
    require("Math.min(minX,pos.getX())" in expanded_bounds and "Math.max(maxX,pos.getX())" in expanded_bounds,
            "expandedPlanBounds no longer derives both X extrema")
    require("Math.min(minY,pos.getY())" in expanded_bounds and "Math.max(maxY,pos.getY())" in expanded_bounds,
            "expandedPlanBounds no longer derives both Y extrema")
    require("Math.min(minZ,pos.getZ())" in expanded_bounds and "Math.max(maxZ,pos.getZ())" in expanded_bounds,
            "expandedPlanBounds no longer derives both Z extrema")
    require(all(fragment in expanded_bounds for fragment in (
                "minX==Integer.MIN_VALUE?minX:minX-1",
                "minY==Integer.MIN_VALUE?minY:minY-1",
                "minZ==Integer.MIN_VALUE?minZ:minZ-1",
                "maxX==Integer.MAX_VALUE?maxX:maxX+1",
                "maxY==Integer.MAX_VALUE?maxY:maxY+1",
                "maxZ==Integer.MAX_VALUE?maxZ:maxZ+1",
            )), "expandedPlanBounds no longer applies a saturated one-block margin on all six faces")

    require("BoundingBoxcandidateBounds=expandedPlanBounds(plannedPositions)" in intersects_bounds,
            "intersectsStructureBounds no longer builds bounds from planned positions")
    require("structureBounds.stream().anyMatch(candidateBounds::intersects)" in intersects_bounds,
            "intersectsStructureBounds no longer checks candidate intersection")

    require("expandedPlanBounds(plan.blocks().keySet())" in collision,
            "collidesWithStructure no longer derives chunk coverage from plan positions")
    require(all(fragment in collision for fragment in (
                "Math.floorDiv(candidateBounds.minX(),16)",
                "chunkX<=Math.floorDiv(candidateBounds.maxX(),16)",
                "Math.floorDiv(candidateBounds.minZ(),16)",
                "chunkZ<=Math.floorDiv(candidateBounds.maxZ(),16)",
            )), "collidesWithStructure no longer scans every floor-divided covered chunk")
    starts_add = collision.find(
        "starts.addAll(level.getLevel().structureManager().startsForStructure("
    )
    bounds_pipeline = collision.find(
        "List<BoundingBox>structureBounds=starts.stream()"
        ".filter(StructureStart::isValid)"
        ".map(StructureStart::getBoundingBox)"
        ".toList();"
    )
    collision_return = collision.find(
        "returnintersectsStructureBounds(plan.blocks().keySet(),structureBounds);"
    )
    require(starts_add >= 0,
            "collidesWithStructure no longer adds queried starts to the starts set")
    require(bounds_pipeline >= 0,
            "collidesWithStructure no longer derives structureBounds from valid global start bounds")
    require(collision_return >= 0,
            "collidesWithStructure no longer intersects plan positions with structureBounds")
    require(0 <= starts_add < bounds_pipeline < collision_return,
            "collidesWithStructure no longer preserves the starts-to-bounds-to-intersection data chain")
    require(".getPieces(" not in collision,
            "collidesWithStructure reverted from global structure bounds to structure pieces")

    collision_guard_prefix = (
        "if(siteType==ExpeditionSiteType.MINER_CAMP&&"
        "(collidesWithStructure(context.level(),plan)||"
        "fallbackPlans.stream().anyMatch(fallbackPlan->collidesWithStructure("
        "context.level(),fallbackPlan)))){"
    )
    collision_guards = [match.start() for match in re.finditer(re.escape(collision_guard_prefix), place)]
    collision_guard = ""
    collision_guard_start = collision_guards[0] if len(collision_guards) == 1 else -1
    collision_guard_end = -1
    if collision_guard_start >= 0:
        opening_brace = collision_guard_start + len(collision_guard_prefix) - 1
        depth = 1
        cursor = opening_brace + 1
        while cursor < len(place) and depth:
            depth += (place[cursor] == "{") - (place[cursor] == "}")
            cursor += 1
        if not depth:
            collision_guard_end = cursor
            collision_guard = place[collision_guard_start:collision_guard_end]
    require(len(collision_guards) == 1 and collision_guard_end >= 0,
            "place must contain exactly one complete MINER_CAMP primary-or-fallback collision guard")
    require(place.count("collidesWithStructure(") == 2,
            "place must contain exactly two collidesWithStructure calls")
    require("skip(origin,IoeWorldgenRuntimeDiagnostics.SiteSkipReason.SURFACE_UNSUITABLE," in collision_guard
            and "returnfalse;" in collision_guard,
            "place collision guard no longer skips as SURFACE_UNSUITABLE and returns false")

    primary_collision = collision_guard.find("collidesWithStructure(context.level(),plan)")
    fallback_collision = collision_guard.find(
        "fallbackPlans.stream().anyMatch(fallbackPlan->collidesWithStructure(context.level(),fallbackPlan))"
    )
    stage_call = place.find("IoePendingExpeditionSites.stage(")
    apply_call = place.find("IoeExpeditionPlanPlacement.apply(")
    require(primary_collision >= 0 and fallback_collision >= 0,
            "place collision guard no longer checks both primary and fallback plans")
    require(collision_guard_end >= 0 and stage_call >= 0 and collision_guard_end <= stage_call,
            "place no longer checks primary and fallback collisions before staging")
    require(collision_guard_end >= 0 and apply_call >= 0 and collision_guard_end <= apply_call,
            "place no longer checks primary and fallback collisions before direct application")
    require(collision_guard_end >= 0 and "structureOnlyPlan(" not in collision_guard,
            "place recomposes a plan inside the collision guard")

    collision_tests = {
        name: re.sub(r"\s+", "", java_test_method_body(collision_test_source, name))
        for name in (
            "expandsSyntheticPlannedPositionsByExactlyOneBlock",
            "detectsOneBlockOverlap",
            "rejectsBoundaryFaceAdjacency",
            "allowsOneEmptyBlockSeparation",
            "activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure",
            "abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure",
            "collisionHelperDoesNotMutateInputs",
            "rejectsEmptyPlannedPositions",
        )
    }
    expansion_test = collision_tests["expandsSyntheticPlannedPositionsByExactlyOneBlock"]
    require("ExpeditionSiteFeature.expandedPlanBounds(" in expansion_test
            and "assertEquals(List.of(" in expansion_test
            and all(f"bounds.{axis}()" in expansion_test for axis in
                    ("minX", "minY", "minZ", "maxX", "maxY", "maxZ")),
            "expandsSyntheticPlannedPositionsByExactlyOneBlock lost six-face expansion coverage")
    expansion_expected_match = re.search(
        r"assertEquals\(List\.of\(((-?\d+,){5}-?\d+)\),List\.of\(bounds\.minX\(\),bounds\.minY\(\),"
        r"bounds\.minZ\(\),bounds\.maxX\(\),bounds\.maxY\(\),bounds\.maxZ\(\)\)\)",
        expansion_test,
    )
    require(expansion_expected_match is not None,
            "expandsSyntheticPlannedPositionsByExactlyOneBlock lost its literal six-bound expectation")
    if expansion_expected_match is not None:
        _, expansion_margin = synthetic_planned_bounds(
            expansion_test, "expandsSyntheticPlannedPositionsByExactlyOneBlock"
        )
        expansion_expected = tuple(map(int, expansion_expected_match.group(1).split(",")))
        require(expansion_expected == expansion_margin,
                "expandsSyntheticPlannedPositionsByExactlyOneBlock expected bounds no longer equal raw bounds plus one")
    require("assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(" in
            collision_tests["detectsOneBlockOverlap"],
            "detectsOneBlockOverlap no longer proves an overlap is rejected")
    overlap_raw, _, overlap_box = synthetic_collision_fixture(
        collision_tests["detectsOneBlockOverlap"], "detectsOneBlockOverlap"
    )
    require(boxes_intersect(overlap_raw, overlap_box),
            "detectsOneBlockOverlap fixture no longer intersects the raw planned volume")
    overlap_extents = tuple(
        min(overlap_raw[axis + 3], overlap_box[axis + 3])
        - max(overlap_raw[axis], overlap_box[axis]) + 1
        for axis in range(3)
    )
    require(all(extent > 0 for extent in overlap_extents)
            and sum(extent == 1 for extent in overlap_extents) == 1,
            "detectsOneBlockOverlap fixture no longer overlaps by one block on exactly one axis")
    require("assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(" in
            collision_tests["rejectsBoundaryFaceAdjacency"],
            "rejectsBoundaryFaceAdjacency no longer proves face adjacency is rejected")
    adjacency_raw, adjacency_margin, adjacency_box = synthetic_collision_fixture(
        collision_tests["rejectsBoundaryFaceAdjacency"], "rejectsBoundaryFaceAdjacency"
    )
    touching_faces = sum(
        adjacency_box[axis] == adjacency_margin[axis + 3]
        or adjacency_box[axis + 3] == adjacency_margin[axis]
        for axis in range(3)
    )
    require(not boxes_intersect(adjacency_raw, adjacency_box)
            and boxes_intersect(adjacency_margin, adjacency_box)
            and touching_faces == 1,
            "rejectsBoundaryFaceAdjacency fixture no longer touches exactly one expanded-volume face")
    require("assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(" in
            collision_tests["allowsOneEmptyBlockSeparation"],
            "allowsOneEmptyBlockSeparation no longer proves separated bounds are allowed")
    separation_raw, separation_margin, separation_box = synthetic_collision_fixture(
        collision_tests["allowsOneEmptyBlockSeparation"], "allowsOneEmptyBlockSeparation"
    )
    separated_axes = sum(
        (separation_box[axis] == separation_raw[axis + 3] + 2
         or separation_box[axis + 3] == separation_raw[axis] - 2)
        and all(
            separation_box[other] <= separation_raw[other + 3]
            and separation_box[other + 3] >= separation_raw[other]
            for other in range(3) if other != axis
        )
        for axis in range(3)
    )
    require(not boxes_intersect(separation_margin, separation_box) and separated_axes == 1,
            "allowsOneEmptyBlockSeparation fixture no longer leaves exactly one empty block beyond the margin")
    active_test = collision_tests["activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure"]
    require("ProspectorCampOutcropComposer.compose(" in active_test
            and "active.blocks().keySet()" in active_test
            and ".getY()" in active_test and ".getX()" in active_test,
            "activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure lost volume semantics")
    require("assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(positions,List.of(newBoundingBox("
            "high.getX(),high.getY(),high.getZ(),high.getX(),high.getY(),high.getZ()))))" in active_test,
            "activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure lost its exact high-point assertion")
    require("assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(positions,List.of(newBoundingBox("
            "lateral.getX()+2,lateral.getY(),lateral.getZ(),lateral.getX()+2,lateral.getY(),lateral.getZ()))))"
            in active_test,
            "activeComposerVolumeDetectsHighStructureAndAllowsSeparatedLateralStructure lost its exact separated assertion")
    abandoned_test = collision_tests["abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure"]
    require("AbandonedProspectorCampComposer.compose(" in abandoned_test
            and "abandoned.blocks().keySet()" in abandoned_test
            and ".getX()" in abandoned_test and ".getZ()" in abandoned_test,
            "abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure lost volume semantics")
    require("assertTrue(ExpeditionSiteFeature.intersectsStructureBounds(positions,List.of(newBoundingBox("
            "lateral.getX(),lateral.getY(),lateral.getZ(),lateral.getX(),lateral.getY(),lateral.getZ()))))"
            in abandoned_test,
            "abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure lost its exact lateral assertion")
    require("assertFalse(ExpeditionSiteFeature.intersectsStructureBounds(positions,List.of(newBoundingBox("
            "rear.getX(),rear.getY(),rear.getZ()-2,rear.getX(),rear.getY(),rear.getZ()-2))))" in abandoned_test,
            "abandonedComposerVolumeDetectsLateralStructureAndAllowsSeparatedStructure lost its exact separated assertion")
    mutation_test = collision_tests["collisionHelperDoesNotMutateInputs"]
    require("List.copyOf(positions)" in mutation_test and "List.copyOf(bounds)" in mutation_test
            and "ExpeditionSiteFeature.intersectsStructureBounds(positions,bounds)" in mutation_test
            and "assertEquals(positionsBefore,positions)" in mutation_test
            and "assertEquals(boundsBefore,bounds)" in mutation_test,
            "collisionHelperDoesNotMutateInputs no longer verifies both inputs remain unchanged")
    empty_test = collision_tests["rejectsEmptyPlannedPositions"]
    require("assertThrows(IllegalArgumentException.class" in empty_test
            and "ExpeditionSiteFeature.intersectsStructureBounds(List.of(),List.of())" in empty_test,
            "rejectsEmptyPlannedPositions no longer verifies the empty-plan contract")

    require('!ModList.get().isLoaded(MOD_ID)' in compat_source,
            "Domum adapter is not guarded by optional-mod presence")
    require("DomumOrnamentumAdapter.applyMaterialPayload" in compat_source,
            "guarded Domum adapter call is missing")
    require(compat_source.count("DomumOrnamentumAdapter.matchesMaterialPayload") == 2
            and "MaterialTextureData.readFromItemStack" in adapter_source,
            "guarded Domum block-entity or recovered-item readback is missing")
    require("IMateriallyTexturedBlockEntity" in adapter_source
            and "new MaterialTextureData" in adapter_source
            and "updateTextureDataWith" in adapter_source,
            "Domum integration does not use the verified typed material API")
    require("CompoundTag" not in adapter_source and "textureData" not in plan_source,
            "hand-authored Domum NBT entered the placement contract")

    block_names = set(re.findall(r"Blocks\.([A-Z0-9_]+)", composer_source))
    forbidden_blocks = sorted(name for name in block_names if name.endswith("_ORE")
                              or name.startswith("RAW_") or "BUDDING" in name)
    require(not forbidden_blocks, f"free resource blocks in camp composer: {forbidden_blocks}")
    require(composer_source.count('"domum_ornamentum", "post"') == 1
            and composer_source.count('"domum_ornamentum", "framed"') == 1,
            "Domum registry inventory differs from the audited post/framed set")
    for forbidden_mod in ("minecolonies", "structurize"):
        require(forbidden_mod not in composer_source.lower(), f"forbidden dependency in composer: {forbidden_mod}")

    loot_root = RESOURCES / "data/immersive_ore_expedition/loot_table/chests"
    all_loot_items: set[str] = set()
    for quality, (filename, minimum_rolls, maximum_rolls) in LOOT_TABLES.items():
        loot = load_json(loot_root / filename)
        loot_items = set(collect_named_values(loot))
        all_loot_items.update(loot_items)
        require(loot_items <= ALLOWED_LOOT,
                f"{quality} camp loot leaves the utility allowlist: {sorted(loot_items - ALLOWED_LOOT)}")
        require(not (loot_items & FORBIDDEN_LOOT),
                f"{quality} mineral loot entries found: {sorted(loot_items & FORBIDDEN_LOOT)}")
        require(all(item.startswith("minecraft:") for item in loot_items),
                f"{quality} camp loot must remain vanilla utility loot")
        pools = loot.get("pools", [])
        require(len(pools) == 1, f"{quality} loot must retain one bounded pool")
        if len(pools) != 1:
            continue
        rolls = pools[0].get("rolls", {})
        require((rolls.get("min"), rolls.get("max")) == (minimum_rolls, maximum_rolls),
                f"{quality} loot roll budget changed: {rolls}")
        pickaxes = [
            entry for entry in pools[0].get("entries", [])
            if entry.get("name") == "minecraft:wooden_pickaxe"
        ]
        require(len(pickaxes) == 1, f"{quality} loot must expose one worn wooden pickaxe entry")
        if len(pickaxes) == 1:
            damage_functions = [
                function for function in pickaxes[0].get("functions", [])
                if function.get("function") == "minecraft:set_damage"
            ]
            require(len(damage_functions) == 1, f"{quality} wooden pickaxe is not explicitly worn")
            if len(damage_functions) == 1:
                damage = damage_functions[0].get("damage", {})
                require(damage.get("type") == "minecraft:uniform"
                        and 0.0 < float(damage.get("min", 1.0))
                        <= float(damage.get("max", 1.0)) <= 0.3,
                        f"{quality} wooden pickaxe is not strongly worn: {damage}")
    require(all_loot_items == ALLOWED_LOOT,
            f"tiered camp loot inventory drifted: {sorted(all_loot_items ^ ALLOWED_LOOT)}")
    for loot_id in (
        "chests/prospector_camp_supplies",
        "chests/prospector_camp_supplies_normal",
        "chests/prospector_camp_supplies_rich",
        "chests/prospector_camp_supplies_motherlode",
    ):
        require(loot_id in composer_source, f"composer does not route quality loot table {loot_id}")

    volcanic_tag = load_json(
        RESOURCES / "data/immersive_ore_expedition/tags/worldgen/biome/prospector_camp/volcanic.json"
    )
    require(volcanic_tag == {"replace": False, "values": ["#immersive_ore_expedition:ip_lava_volcano"]},
            "volcanic visual tag no longer reuses the audited IOE volcanic cohort")

    build_source = read(ROOT / "build.gradle")
    properties_source = read(ROOT / "gradle.properties")
    metadata_source = read(RESOURCES / "META-INF/neoforge.mods.toml")
    require("https://ldtteam.jfrog.io/ldtteam/modding/" in build_source, "official LDTTeam Maven is missing")
    require('compileOnly "com.ldtteam:domum-ornamentum:${domum_version}:api"' in build_source,
            "Domum API is not compile-only")
    require("ioeIncludeDomumRuntime" in build_source and ":${domum_version}:main" in build_source,
            "Domum runtime is not isolated behind the integration property")
    require('systemProperty "ioe.expectDomumRuntime", "true"' in build_source,
            "full-runtime GameTests can silently skip a missing pinned Domum runtime")
    require("domum_version=1.0.231" in properties_source
            and "domum_version_range=[1.0.231,1.0.232)" in properties_source,
            "Domum version is not exactly pinned")
    require('modId="domum_ornamentum"' in metadata_source
            and 'type="optional"' in metadata_source
            and 'versionRange="${domum_version_range}"' in metadata_source,
            "Domum metadata is not optional and range-pinned")

    runtime_lock = load_json(ROOT / "ci/runtime-mods.json")
    domum_entries = [entry for entry in runtime_lock["files"] if entry.get("mod_id") == "domum_ornamentum"]
    require(len(domum_entries) == 1, "full-runtime lock must contain exactly one Domum artifact")
    if len(domum_entries) == 1:
        entry = domum_entries[0]
        require(entry.get("filename") == "domum-ornamentum-1.0.231-main.jar", "unexpected Domum runtime file")
        require(entry.get("sha512") == "483061574a8452ce6b5b25779431540fc4e444123a987633546b290e9beb03ce094d0bdb80f28fb1dbc2317fd91d1dfbde1c4c0619dbf124768744cb8bc6ea35",
                "Domum runtime SHA-512 differs from authoritative Maven evidence")

    dimension_rows = csv_rows(DOCS / "STRUCTURE_DIMENSION_REPORT.csv")
    require(len(dimension_rows) == len(QUALITY_CONTRACT) * len(VISUAL_FAMILIES),
            "dimension report does not cover the complete quality/family matrix")
    dimension_keys = {(row["quality"], row["visual_family"]) for row in dimension_rows}
    require(dimension_keys == {
        (quality, family) for quality in QUALITY_CONTRACT for family in VISUAL_FAMILIES
    }, "dimension report has missing or duplicate quality/family keys")
    for row in dimension_rows:
        quality = row["quality"]
        family = row["visual_family"]
        expected = QUALITY_CONTRACT[quality]
        actual_contract = tuple(int(row[key]) for key in (
            "target_footprint", "placed_footprint", "max_height",
            "container_count", "domum_material_block_entity_count"
        ))
        require(actual_contract == expected, f"dimension contract drift for {quality}: {actual_contract}")
        require(row["narrative_state"] == state_for(quality, family, 0x51A7E5EED),
                f"static audit state drift for {quality}/{family}")
        require(row["audit_seed"] == "0x51A7E5EED"
                and row["rotation"] == "CLOCKWISE_180"
                and row["measurement_status"] == "STATIC_COMPOSER_MODEL",
                f"dimension evidence lost its measurement boundary for {quality}")
        planned_air = int(row["block_count"]) - int(row["non_air_block_count"])
        shelter_kind = "raised" if family in {"WETLAND", "TROPICAL"} else "ground"
        detail_count = biome_geometry_count(quality, family)
        require(int(row["biome_geometry_block_count"]) == detail_count,
                f"biome shelter-geometry budget drift for {quality}/{family}")
        require(planned_air == PLANNED_AIR[quality][shelter_kind] - detail_count,
                f"explicit path/access air budget drift for {quality}/{family}: {planned_air}")
        require(0 < int(row["block_count"]) <= expected[1] * expected[1] * expected[2],
                f"surface block budget is implausible for {quality}/{family}")
        require(int(row["block_entity_count_vanilla"]) == expected[3] + 1,
                f"vanilla block-entity budget drift for {quality}/{family}")
        require(int(row["block_entity_count_with_domum"])
                == int(row["block_entity_count_vanilla"]) + int(row["domum_material_block_entity_count"]),
                f"block-entity totals do not reconcile for {quality}")
        shell_count = int(row["shelter_shell_block_count"])
        require(shell_count == SHELTER_SHELL[quality][shelter_kind] + detail_count,
                f"nominal shelter-shell denominator drift for {quality}/{family}")
        share_percent = float(row["domum_share_of_shelter_shell_percent"])
        if quality == "DRY":
            require(share_percent == 0.0, "DRY must not report a Domum material share")
        else:
            expected_share = expected[4] * 100.0 / shell_count
            require(abs(share_percent - expected_share) <= 0.01
                    and 20.0 <= share_percent <= 35.0,
                    f"Domum shelter share is outside 20-35% for {quality}/{family}: {share_percent}")
        require(int(row["footprint_x"]) == expected[1] and int(row["footprint_z"]) == expected[1],
                f"placed extent drift for {quality}")
        require(int(row["min_relative_y"]) == -1
                and int(row["max_relative_y"]) < expected[2]
                and int(row["placed_height_span"]) <= expected[2],
                f"height envelope drift for {quality}/{family}")
        require(row["cross_chunk_writes"] == "0", f"cross-chunk write budget changed for {quality}")

    matrix_rows = csv_rows(DOCS / "BIOME_QUALITY_MATRIX.csv")
    expected_matrix_keys = {
        (quality, family, state)
        for quality in QUALITY_CONTRACT
        for family in VISUAL_FAMILIES
        for state in ALLOWED_STATES[quality]
    }
    require(len(matrix_rows) == len(expected_matrix_keys), "biome-quality-state matrix is incomplete")
    keys = {(row["quality"], row["visual_family"], row["structure_state"]) for row in matrix_rows}
    require(keys == expected_matrix_keys, "biome-quality-state matrix has missing or duplicate keys")
    for row in matrix_rows:
        quality = row["quality"]
        family = row["visual_family"]
        state = row["structure_state"]
        coverage_seed = int(row["coverage_seed"], 0)
        require(state_for(quality, family, coverage_seed) == state,
                f"narrative-state coverage seed does not reproduce its row: {row}")
        expected = QUALITY_CONTRACT[quality]
        require(tuple(int(row[key]) for key in (
            "target_footprint", "placed_footprint", "max_height",
            "loot_containers", "max_domum_block_entities"
        )) == expected, f"matrix quality contract drift: {row}")
        require(int(row["vegetation_accents"]) == VEGETATION_COUNT[family],
                f"matrix vegetation contract drift: {row}")
        require(row["raised_shelter"] == ("yes" if family in {"WETLAND", "TROPICAL"} else "no"),
                f"matrix geometry contract drift: {row}")
        require(row["biome_shelter_geometry"] == BIOME_GEOMETRY_LABEL[family],
                f"matrix biome shelter-geometry label drift: {row}")
        require(row["surface_ore_blocks"] == "0" and row["vanilla_fallback"] == "yes",
                f"matrix violates fallback/resource contract: {row}")

    for required_field in (
        "DISPLAY_PURPOSE:",
        "REGISTRY_ID:",
        "BLOCK_ENTITY_TYPE:",
        "REQUIRED_BLOCK_STATE:",
        "REQUIRED_NBT:",
        "PRIMARY_MATERIAL_ID:",
        "SECONDARY_MATERIAL_ID:",
        "ROTATION_BEHAVIOR:",
        "MIRROR_BEHAVIOR:",
        "DROPPED_ITEM_BEHAVIOR:",
        "MISSING_MATERIAL_BEHAVIOR:",
        "SOURCE_EVIDENCE:",
    ):
        require(domum_inventory_source.count(required_field) == 2,
                f"Domum inventory must record {required_field} once per retained component")

    require("mix64" in context_source and "RandomSource" not in context_source,
            "visual variant selection is not an isolated deterministic seed transform")
    require("productiveDomumBudgetsStayWithinTheNominalShelterMaterialShare" in unit_test_source
            and "shelterShellBlockCount" in unit_test_source
            and "biomeShelterDetailBlockCount" in unit_test_source
            and "biomeGeometryBlocks" in unit_test_source
            and "twoBlockHeadroomColumns" in unit_test_source
            and "openRaisedShelterHeadroomBay" in unit_test_source
            and "DRY raised shelter head space is blocked at its designated bay" in unit_test_source
            and "Raised-shelter headroom bay is not explicitly open" in composer_source,
            "unit-test source no longer enforces the productive 20-35% Domum shelter share")

    collision_cases = 0
    try:
        collision_cases = validate_collision_matrix()
    except AssertionError as failure:
        require(False, f"semantic collision matrix failed: {failure}")
    require(collision_cases == 160, f"semantic collision matrix covered {collision_cases} cases instead of 160")

    if failures:
        raise SystemExit("Prospector-camp validation failed:\n- " + "\n- ".join(failures))
    print(
        "Prospector-camp validation passed: "
        "5 qualities x 8 visual families x 2 allowed states, "
        "160 anchor/rotation/quality collision cases, "
        "family-specific shelter geometry, two-block circulation headroom, 48-block anchor spacing, "
        "one deterministic selector, zero surface ores, "
        "four tiered utility loot tables, optional Domum 1.0.231 API/main contract"
    )


if __name__ == "__main__":
    validate()
