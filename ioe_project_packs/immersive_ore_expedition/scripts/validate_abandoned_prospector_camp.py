#!/usr/bin/env python3
"""Static wiring and exhaustive-test declaration checks for Surface Structure 02."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/oblixorprime/ioe"
TEST = ROOT / "src/test/java/com/oblixorprime/ioe/worldgen/AbandonedProspectorCampComposerTest.java"
LOOT = ROOT / "src/main/resources/data/immersive_ore_expedition/loot_table/chests"
DOCS = ROOT / "docs/worldgen/abandoned_prospector_camp"

LOOT_FILES = {
    "POOR": "abandoned_prospector_camp_supplies.json",
    "NORMAL": "abandoned_prospector_camp_supplies_normal.json",
    "RICH": "abandoned_prospector_camp_supplies_rich.json",
    "MOTHERLODE": "abandoned_prospector_camp_supplies_motherlode.json",
    "SPARSE": "abandoned_prospector_camp_supplies_sparse.json",
}
ALLOWED_LOOT = {
    "minecraft:torch",
    "minecraft:charcoal",
    "minecraft:bread",
    "minecraft:paper",
    "minecraft:string",
    "minecraft:feather",
    "minecraft:wooden_pickaxe",
    "minecraft:map",
    "minecraft:compass",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def strip_java_comments(source: str) -> str:
    return re.sub(r"//[^\r\n]*|/\*.*?\*/", "", source, flags=re.DOTALL)


def method_body(source: str, method_name: str) -> str:
    code = strip_java_comments(source)
    declaration = re.search(rf"\b{re.escape(method_name)}\s*\([^)]*\)\s*\{{", code, re.DOTALL)
    require(declaration is not None, f"JUnit method is missing: {method_name}")
    opening = code.find("{", declaration.start())
    depth = 0
    for index in range(opening, len(code)):
        if code[index] == "{":
            depth += 1
        elif code[index] == "}":
            depth -= 1
            if depth == 0:
                return code[opening + 1:index]
    raise AssertionError(f"JUnit method is unterminated: {method_name}")


def hand_authored_supported_pairs(test_source: str) -> tuple[tuple[str, str], ...]:
    code = strip_java_comments(test_source)
    entries = tuple(re.findall(
        r"new\s+SupportedPair\(\s*SiteQuality\.([A-Z][A-Z0-9_]*)\s*,\s*"
        r"AbandonedProspectorCampState\.([A-Z][A-Z0-9_]*)\s*\)",
        code,
    ))
    require(len(entries) == 18, f"JUnit golden must declare exactly 18 supported pairs: {len(entries)}")
    require(len(set(entries)) == len(entries), "JUnit golden contains duplicate supported pairs")
    return entries


def enum_constants(source: str, enum_name: str) -> tuple[str, ...]:
    match = re.search(rf"\benum\s+{re.escape(enum_name)}\s*\{{(?P<body>.*?)\;", source, re.DOTALL)
    require(match is not None, f"enum declaration is missing: {enum_name}")
    constants = tuple(re.findall(r"\b[A-Z][A-Z0-9_]*\b", match.group("body")))
    require(bool(constants), f"enum has no constants: {enum_name}")
    require(len(constants) == len(set(constants)), f"enum constants are duplicated: {enum_name}")
    return constants


def live_eligibility(
    state_source: str,
    qualities: tuple[str, ...],
    states: tuple[str, ...],
) -> dict[str, tuple[str, ...]]:
    eligibility: dict[str, tuple[str, ...]] = {}
    for labels, values in re.findall(
        r"case\s+([A-Z][A-Z0-9_]*(?:\s*,\s*[A-Z][A-Z0-9_]*)*)\s*"
        r"->\s*List\.of\(([^)]*)\)",
        state_source,
    ):
        selected_states = tuple(re.findall(r"\b[A-Z][A-Z0-9_]*\b", values))
        require(bool(selected_states), f"empty eligibility branch: {labels}")
        require(set(selected_states) <= set(states), f"unknown state in eligibility branch: {labels}")
        for quality in re.findall(r"\b[A-Z][A-Z0-9_]*\b", labels):
            require(quality not in eligibility, f"quality has duplicate eligibility: {quality}")
            eligibility[quality] = selected_states
    require(set(eligibility) == set(qualities), "live quality eligibility is incomplete")
    return eligibility


def collect_item_names(value: Any) -> set[str]:
    names: set[str] = set()
    if isinstance(value, dict):
        name = value.get("name")
        if isinstance(name, str) and name.startswith("minecraft:"):
            names.add(name)
        for child in value.values():
            names.update(collect_item_names(child))
    elif isinstance(value, list):
        for child in value:
            names.update(collect_item_names(child))
    return names


def chance_for_item(loot: dict[str, Any], item: str) -> list[float]:
    chances: list[float] = []
    for pool in loot.get("pools", []):
        if item not in collect_item_names(pool):
            continue
        random_chances = [
            condition.get("chance")
            for condition in pool.get("conditions", [])
            if condition.get("condition") == "minecraft:random_chance"
        ]
        require(len(random_chances) == 1, f"{item} must be isolated in one random_chance pool")
        require(pool.get("rolls") == 1, f"{item} pool must roll exactly once")
        chances.append(float(random_chances[0]))
    return chances


def validate_loot() -> None:
    union: set[str] = set()
    for tier, filename in LOOT_FILES.items():
        loot = json.loads(read(LOOT / filename))
        items = collect_item_names(loot)
        union.update(items)
        require(items <= ALLOWED_LOOT, f"{tier} loot leaves allowlist: {sorted(items - ALLOWED_LOOT)}")
        require("minecraft:filled_map" not in items, f"{tier} uses a filled map")

        pickaxes = [
            entry
            for pool in loot.get("pools", [])
            for entry in pool.get("entries", [])
            if entry.get("name") == "minecraft:wooden_pickaxe"
        ]
        require(len(pickaxes) == 1, f"{tier} must contain one worn-pickaxe entry")
        damage_functions = [
            function
            for function in pickaxes[0].get("functions", [])
            if function.get("function") == "minecraft:set_damage"
        ]
        require(len(damage_functions) == 1, f"{tier} pickaxe must have set_damage")
        damage = damage_functions[0]["damage"]
        require(
            0.0 <= float(damage["min"]) <= float(damage["max"]) <= 0.20,
            f"{tier} wooden pickaxe is not capped at 20% remaining durability",
        )

        map_chances = chance_for_item(loot, "minecraft:map")
        compass_chances = chance_for_item(loot, "minecraft:compass")
        if tier in {"NORMAL", "RICH", "MOTHERLODE"}:
            require(map_chances == [0.08], f"{tier} map chance must be exactly 8%")
        else:
            require(not map_chances, f"{tier} cannot contain a map")
        if tier in {"RICH", "MOTHERLODE"}:
            require(compass_chances == [0.02], f"{tier} compass chance must be exactly 2%")
        else:
            require(not compass_chances, f"{tier} cannot contain a compass")
    require(union == ALLOWED_LOOT, f"abandoned loot inventory drifted: {sorted(union ^ ALLOWED_LOOT)}")


def validate_production_wiring(states: tuple[str, ...]) -> None:
    outcrop = read(JAVA / "worldgen/ProspectorCampOutcropComposer.java")
    composer = read(JAVA / "worldgen/AbandonedProspectorCampComposer.java")
    blueprint = read(JAVA / "worldgen/ExpeditionSiteBlueprints.java")
    feature = read(JAVA / "worldgen/ExpeditionSiteFeature.java")
    context = read(JAVA / "worldgen/ProspectorCampContext.java")

    explicit_compose = re.compile(
        r"^[ \t]*static\s+Composition\s+compose\s*\(\s*"
        r"BlockPos\s+shaftOrigin\s*,\s*SiteQuality\s+quality\s*,\s*"
        r"ProspectorCampContext\s+context\s*,\s*Rotation\s+explicitRotation\s*\)\s*\{",
        re.MULTILINE | re.DOTALL,
    )
    public_delegation = (
        r"Objects\.requireNonNull\(\s*shaftOrigin\s*,\s*\"shaftOrigin\"\s*\)\s*;\s*"
        r"Objects\.requireNonNull\(\s*quality\s*,\s*\"quality\"\s*\)\s*;\s*"
        r"Objects\.requireNonNull\(\s*context\s*,\s*\"context\"\s*\)\s*;\s*"
        r"return\s+compose\s*\(\s*shaftOrigin\s*,\s*quality\s*,\s*context\s*,\s*"
        r"context\.rotation\(\)\s*\)\s*;"
    )
    for label, source in (("active", outcrop), ("abandoned", composer)):
        code = strip_java_comments(source)
        require(explicit_compose.search(code) is not None,
                f"{label} composer lacks the package-private trailing-Rotation compose overload")
        require(source.count('Objects.requireNonNull(explicitRotation, "explicitRotation")') == 1,
                f"{label} composer does not validate explicitRotation exactly once")
        require(re.search(public_delegation, method_body(source, "compose"), re.DOTALL) is not None,
                f"{label} public compose does not preserve null order and delegate with context.rotation()")

    require(re.search(
        r"new\s+Builder\s*\(\s*shaftOrigin\s*,\s*campCenter\s*,\s*spec\s*,\s*"
        r"explicitRotation\s*,\s*context\.archetype\(\)\s*\)",
        strip_java_comments(outcrop),
        re.DOTALL,
    ) is not None, "active composer does not flow explicitRotation into Builder")
    require(re.search(
        r"new\s+Builder\s*\([^;]*context\.rotation\(\)",
        strip_java_comments(outcrop),
        re.DOTALL,
    ) is None, "active Builder still reads context.rotation()")
    require(re.search(
        r"ProspectorCampOutcropComposer\.compose\s*\(\s*shaftOrigin\s*,\s*quality\s*,\s*"
        r"context\s*,\s*explicitRotation\s*\)",
        strip_java_comments(composer),
        re.DOTALL,
    ) is not None, "abandoned composer does not forward explicitRotation to the active composer")
    require(composer.count("AbandonedProspectorCampState.select(") == 1 and re.search(
        r"AbandonedProspectorCampState\.select\s*\(\s*context\.siteSeed\(\)\s*,\s*quality\s*\)",
        strip_java_comments(composer),
        re.DOTALL,
    ) is not None, "abandoned state selection no longer uses context.siteSeed() and quality exactly once")
    require("active.rotation()" in composer,
            "abandoned composition no longer preserves the active explicit rotation")

    require("ProspectorCampOutcropComposer.compose(" in composer, "Surface 01 layout kernel is not reused")
    require("normalizeSurfaceOneWear" in composer, "Surface 01 wear normalization is missing")
    require("clearActiveHazards" in composer, "active-hazard clearing is missing")
    require("preserveWalkability" in composer, "walkability preservation is missing")
    for state in states:
        require(
            re.search(rf"case\s+{re.escape(state)}\s*->\s*apply[A-Za-z]+\(", composer) is not None,
            f"abandoned state is not wired in compose: {state}",
        )

    require(
        "prospectorCampContext.archetype() == ProspectorCampArchetype.ABANDONED" in blueprint,
        "canonical abandoned blueprint branch is missing",
    )
    require(blueprint.count("AbandonedProspectorCampComposer.compose(") == 1,
            "abandoned composer insertion is not singular")
    require(blueprint.count("ProspectorCampOutcropComposer.compose(") == 1,
            "active composer insertion is not singular")

    final_quality = feature.index("quality = depositPreparation.resolution().finalQuality();")
    archetype_selection = feature.index("ProspectorCampArchetype.select(planSeed, quality)")
    require(archetype_selection > final_quality, "archetype selection precedes confirmed final quality")
    require("structureOnlyPlan(" in feature and "lowerQuality" in feature,
            "lower-quality fallback plan wiring is missing")
    require("AbandonedProspectorCampState.select(" in composer,
            "abandoned state is not selected from the compose quality")
    require("ProspectorCampArchetype archetype" in context, "camp context lacks archetype identity")

    require("getDeclared" not in composer and "setAccessible" not in composer,
            "production uses reflection for Surface 02")
    for dependency in ("minecolonies", "structurize"):
        require(dependency not in composer.lower(), f"forbidden dependency entered composer: {dependency}")
    require(
        re.search(r"Blocks\.[A-Z0-9_]*(?:ORE|CRYSTAL|BUDDING|AMETHYST)", composer) is None,
        "surface ore/crystal block entered abandoned composer",
    )
    require("Blocks.LAVA.defaultBlockState()" not in composer, "lava placement entered composer")
    require("Blocks.FIRE.defaultBlockState()" not in composer, "fire placement entered composer")
    require("Blocks.SOUL_FIRE.defaultBlockState()" not in composer, "soul-fire placement entered composer")


def validate_exhaustive_junit(
    qualities: tuple[str, ...],
    states: tuple[str, ...],
    eligibility: dict[str, tuple[str, ...]],
    supported_pairs: int,
    unsupported_pairs: int,
) -> None:
    test = read(TEST)
    code = strip_java_comments(test)
    golden = hand_authored_supported_pairs(test)
    live_pairs = {
        (quality, state)
        for quality, eligible_states in eligibility.items()
        for state in eligible_states
    }
    require(set(golden) == live_pairs,
            f"JUnit hand-authored golden differs from live eligibility: {sorted(set(golden) ^ live_pairs)}")

    require("@ParameterizedTest" in code and "@MethodSource" in code,
            "Surface 02 JUnit is not parameterized")
    require("SiteQuality.values()" in code, "JUnit does not derive the live quality domain")
    require("AbandonedProspectorCampState.values()" in code,
            "JUnit does not derive the live state domain")
    require(code.count("Rotation.values()") >= 3, "JUnit does not exercise the live rotation domain")
    require("findSeed(pair.quality(), pair.state(), rotation)" in code,
            "JUnit lacks bounded deterministic seed discovery per supported key")
    require("SEED_SEARCH_LIMIT" in code, "JUnit seed discovery is not bounded")
    require("SUPPORTED_SEEDS = discoverSupportedSeeds()" in code,
            "JUnit does not cache supported-case seeds exactly once")

    require(f"EXPECTED_SUPPORTED_PAIR_COUNT = {supported_pairs}" in code,
            "JUnit supported-pair declaration disagrees with live eligibility")
    require(f"EXPECTED_SUPPORTED_CASE_COUNT = {supported_pairs * 4}" in code,
            "JUnit supported rotation-case declaration disagrees with live eligibility")
    require(f"EXPECTED_UNSUPPORTED_PAIR_COUNT = {unsupported_pairs}" in code,
            "JUnit unsupported-complement declaration disagrees with live eligibility")
    require("EXPECTED_ROTATION_COUNT = 4" in code,
            "JUnit does not declare the Minecraft Rotation.values() cardinality")

    for state in states:
        require(re.search(rf"case\s+{re.escape(state)}\s*->", code) is not None,
                f"JUnit lacks a state-specific observable assertion: {state}")

    supported_body = method_body(test, "supportedCases")
    metamorphic_cases_body = method_body(test, "metamorphicCases")
    find_common_seed_body = method_body(test, "findCommonSeed")
    unsupported_body = method_body(test, "unsupportedCases")
    eligibility_body = method_body(test, "liveEligibilityExactlyMatchesHandAuthoredGolden")
    invariant_body = method_body(
        test,
        "everySupportedStateQualityRotationSatisfiesIndependentOutputInvariants",
    )
    rotation_body = method_body(test, "inverseNormalizedObservableTopologyIsRotationMetamorphic")
    normalization_body = method_body(test, "inverseNormalizedObservation")
    boundary_body = method_body(test, "boundarySides")
    digest_body = method_body(test, "canonicalDigest")
    common_invariant_body = method_body(test, "assertCommonOutputInvariants")
    regression_cases_body = method_body(test, "roleSensitiveReachabilityRegressionCases")
    regression_body = method_body(test, "roleSensitiveTerrainSupportsKnownReachabilityRegressions")
    require("SUPPORTED_PAIRS" in supported_body and "Rotation.values()" in supported_body,
            "supported cases are not golden x live rotations")
    require("eligibleFor" not in supported_body,
            "supported cases still derive their expected matrix from production eligibility")
    require("SUPPORTED_PAIRS.stream()" in metamorphic_cases_body
            and metamorphic_cases_body.count("Arguments.of(") == 1
            and re.search(
                r"Arguments\.of\s*\(\s*pair\.quality\(\)\s*,\s*pair\.state\(\)\s*,\s*"
                r"findCommonSeed\s*\(\s*pair\.quality\(\)\s*,\s*pair\.state\(\)\s*\)\s*\)",
                metamorphic_cases_body,
                re.DOTALL,
            ) is not None
            and "assertEquals(EXPECTED_SUPPORTED_PAIR_COUNT, cases.size())" in metamorphic_cases_body
            and "return cases.stream()" in metamorphic_cases_body,
            "metamorphic cases are not exactly one common-seed argument per supported pair")
    for forbidden in (
        "SupportedKey",
        "SUPPORTED_SEEDS",
        "context.rotation()",
        "findSeed(",
        "findSeedForRotation",
        "Rotation.",
    ):
        require(forbidden not in metamorphic_cases_body,
                f"metamorphic cases retain rotation-dependent lookup: {forbidden}")

    require(re.search(
        r"for\s*\(\s*long\s+seed\s*=\s*0\s*;\s*seed\s*<\s*SEED_SEARCH_LIMIT\s*;\s*seed\+\+\s*\)",
        find_common_seed_body,
    ) is not None, "common-seed lookup is not bounded by SEED_SEARCH_LIMIT")
    require(find_common_seed_body.count("AbandonedProspectorCampState.select(") == 1
            and re.search(
                r"AbandonedProspectorCampState\.select\s*\(\s*seed\s*,\s*quality\s*\)\s*==\s*state",
                find_common_seed_body,
            ) is not None,
            "common-seed lookup does not select solely by seed, quality, and expected state")
    for forbidden in (
        "context.rotation()",
        "ProspectorCampContext",
        "context(",
        "SupportedKey",
        "SUPPORTED_SEEDS",
        "findSeed(",
        "findSeedForRotation",
        "eligibleFor",
    ):
        require(forbidden not in find_common_seed_body,
                f"common-seed lookup contains a forbidden dependency: {forbidden}")

    require(re.search(
        r"@ParameterizedTest(?:\([^)]*\))?\s*"
        r"@MethodSource\(\"metamorphicCases\"\)\s*"
        r"void\s+inverseNormalizedObservableTopologyIsRotationMetamorphic\s*\(\s*"
        r"SiteQuality\s+quality\s*,\s*AbandonedProspectorCampState\s+state\s*,\s*long\s+seed\s*\)",
        code,
        re.DOTALL,
    ) is not None, "metamorphicCases is not attached to the quality/state/common-seed test signature")
    require("assertEquals(state, AbandonedProspectorCampState.select(seed, quality)" in rotation_body,
            "metamorphic test does not assert common-seed state selection")
    require(rotation_body.count("context(seed)") == 1
            and "ProspectorCampContext commonContext = context(seed);" in rotation_body,
            "metamorphic test does not create exactly one common context")
    require("for (Rotation rotation : Rotation.values())" in rotation_body,
            "metamorphic test does not iterate the four live rotations")
    require(re.search(
        r"AbandonedProspectorCampComposer\.compose\s*\(\s*METAMORPHIC_SHAFT_ORIGIN\s*,\s*"
        r"quality\s*,\s*commonContext\s*,\s*rotation\s*\)",
        rotation_body,
        re.DOTALL,
    ) is not None, "metamorphic test does not use the four-argument explicit-rotation seam")
    require("assertEquals(rotation, composition.rotation(), diagnostic)" in rotation_body,
            "metamorphic test does not assert the explicit output rotation")
    for forbidden in ("SUPPORTED_SEEDS", "SupportedKey", "context.rotation()"):
        require(forbidden not in rotation_body,
                f"metamorphic test body retains per-rotation lookup: {forbidden}")
    require("int uniqueObservationCount = new HashSet<>(observations.values()).size();" in rotation_body
            and rotation_body.count("assertEquals(1, uniqueObservationCount") == 1,
            "metamorphic test does not assert exactly one unique canonical observation")
    require("List<Rotation> explicitRotations = new ArrayList<>()" in rotation_body
            and "explicitRotations.add(rotation)" in rotation_body
            and "assertEquals(EXPECTED_ROTATION_COUNT, explicitRotations.size())" in rotation_body
            and "assertEquals(EXPECTED_ROTATION_COUNT, observations.size())" in rotation_body,
            "metamorphic test does not prove all four explicit rotations were observed")
    require(rotation_body.count("System.out.println(") == 1
            and rotation_body.count("METAMORPHIC_CASE_EVIDENCE") == 1,
            "metamorphic test must emit exactly one evidence line per pair")
    for evidence_surface in (
        "COMMON_SEED=",
        "ROTATIONS=",
        "UNIQUE_COUNT=",
        "RESULT=PASS",
    ):
        require(evidence_surface in rotation_body,
                f"metamorphic evidence is missing: {evidence_surface}")

    require("BlockPos campCenter = new BlockPos(" in normalization_body
            and re.search(
                r"relativeTo\s*\(\s*pos\s*,\s*campCenter\s*\)\s*"
                r"\.rotate\s*\(\s*inverse\s*\(\s*composition\.rotation\(\)\s*\)\s*\)",
                normalization_body,
                re.DOTALL,
            ) is not None
            and re.search(
                r"relativeTo\s*\(\s*composition\.shelterEntrance\(\)\s*,\s*campCenter\s*\)\s*"
                r"\.rotate\s*\(\s*inverse\s*\(\s*composition\.rotation\(\)\s*\)\s*\)",
                normalization_body,
                re.DOTALL,
            ) is not None,
            "canonical observation lacks inverse outer normalization around campCenter")
    normalized_roles = set(re.findall(
        r"ProspectorCampOutcropComposer\.ComponentRole\.([A-Z][A-Z0-9_]*)",
        normalization_body,
    ))
    require(normalized_roles == {"SHELTER", "PATH", "ACCESS"},
            f"canonical observation includes noncontractual roles: {sorted(normalized_roles)}")
    require("stableRoleColumns" not in code,
            "stableRoleColumns remains in the metamorphic observation equality")

    require("for (Rotation canonicalRotation : Rotation.values())" in normalization_body
            and "assertEquals(EXPECTED_ROTATION_COUNT, candidates.size())" in normalization_body,
            "canonical observation does not evaluate all four internal rotations")
    require("pos.rotate(canonicalRotation)" in normalization_body
            and "normalizedEntrance.rotate(canonicalRotation)" in normalization_body
            and "offset.rotate(canonicalRotation)" in normalization_body,
            "canonical observation does not rotate shelter, entrance, and access vectors together")
    require(all(surface in normalization_body for surface in (
        "int minimumX =",
        "int minimumY =",
        "int minimumZ =",
        "BlockPos anchor = new BlockPos(minimumX, minimumY, minimumZ)",
    )), "canonical observation lacks the rotated shelter XYZ anchor")
    require(".map(pos -> relativeTo(pos, anchor))" in normalization_body
            and "relativeTo(normalizedEntrance.rotate(canonicalRotation), anchor)" in normalization_body,
            "canonical observation does not translate shelter and entrance by the common anchor")
    require(".map(offset -> offset.rotate(canonicalRotation))" in normalization_body
            and "relativeTo(offset" not in normalization_body,
            "canonical access offsets are not retained as rotated vectors")
    require(normalization_body.count(".sorted(POSITION_ORDER)") >= 4
            and ".comparingInt((BlockPos pos) -> pos.getX())" in code,
            "canonical shelter/access ordering does not use the protected POSITION_ORDER")
    require(all(surface in code for surface in (
        "POSITION_LIST_ORDER",
        "BOUNDARY_LIST_ORDER",
        "NORMALIZED_OBSERVATION_ORDER",
        "POSITION_ORDER.compare(first.get(index), second.get(index))",
        "Integer.compare(first.get(index).ordinal(), second.get(index).ordinal())",
        ".thenComparing(NormalizedObservation::cardinalAccessOffsets, POSITION_LIST_ORDER)",
        ".thenComparing(NormalizedObservation::entranceBoundaryClassification, BOUNDARY_LIST_ORDER)",
    )) and "candidates.stream().min(NORMALIZED_OBSERVATION_ORDER)" in normalization_body,
            "canonical candidates are not selected by the structural lexicographic comparator")
    observation_order_match = re.search(
        r"private\s+static\s+final\s+Comparator<NormalizedObservation>\s+"
        r"NORMALIZED_OBSERVATION_ORDER\s*=\s*(?P<body>.*?);",
        code,
        re.DOTALL,
    )
    require(observation_order_match is not None,
            "NormalizedObservation comparator declaration is missing")
    observation_order_body = observation_order_match.group("body")
    require(re.fullmatch(
        r"\s*Comparator\s*\.comparing\s*\(\s*"
        r"\(\s*NormalizedObservation\s+observation\s*\)\s*->\s*"
        r"observation\.shelterColumns\(\)\s*,\s*POSITION_LIST_ORDER\s*\)\s*"
        r"\.thenComparing\s*\(\s*NormalizedObservation::cardinalAccessOffsets\s*,\s*"
        r"POSITION_LIST_ORDER\s*\)\s*"
        r"\.thenComparing\s*\(\s*NormalizedObservation::entranceBoundaryClassification\s*,\s*"
        r"BOUNDARY_LIST_ORDER\s*\)\s*"
        r"\.thenComparingInt\s*\(\s*NormalizedObservation::shelterPostHeight\s*\)\s*",
        observation_order_body,
        re.DOTALL,
    ) is not None and "shelterEntrance" not in observation_order_body,
            "NormalizedObservation comparator does not match the four-field structural contract")
    require(re.search(
        r"candidates\.add\s*\(\s*new\s+NormalizedObservation\s*\(\s*"
        r"translatedShelter\s*,\s*composition\.qualitySpec\(\)\.postHeight\(\)\s*,\s*"
        r"rotatedAccessOffsets\s*,\s*boundarySides\s*\(\s*translatedEntrance\s*,\s*"
        r"Set\.copyOf\s*\(\s*translatedShelter\s*\)\s*\)\s*\)\s*\)",
        normalization_body,
        re.DOTALL,
    ) is not None, "NormalizedObservation construction does not match the four-field contract")

    boundary_order = tuple(re.findall(
        r"sides\.add\(BoundarySide\.([A-Z][A-Z0-9_]*)\)",
        boundary_body,
    ))
    require("List<BoundarySide> sides = new ArrayList<>()" in boundary_body
            and boundary_order == ("MIN_X", "MAX_X", "MIN_Z", "MAX_Z")
            and "return List.copyOf(sides);" in boundary_body,
            f"boundary classification is not represented in deterministic order: {boundary_order}")
    require("boundarySides(translatedEntrance, Set.copyOf(translatedShelter))" in normalization_body,
            "boundary classification is not recomputed after canonical translation")

    observation_match = re.search(
        r"private\s+record\s+NormalizedObservation\s*\((?P<fields>.*?)\)\s*\{",
        code,
        re.DOTALL,
    )
    require(observation_match is not None, "NormalizedObservation record is missing")
    observation_fields = re.sub(r"\s+", " ", observation_match.group("fields")).strip()
    expected_observation_fields = (
        "List<BlockPos> shelterColumns, int shelterPostHeight, "
        "List<BlockPos> cardinalAccessOffsets, "
        "List<BoundarySide> entranceBoundaryClassification"
    )
    require(observation_fields == expected_observation_fields,
            f"NormalizedObservation contractual fields changed: {observation_fields}")
    require("shelterEntrance" not in observation_fields,
            "NormalizedObservation must not store exact shelter entrance coordinates")
    for forbidden in (
        "shaft",
        "hatch",
        "path",
        "blockentity",
        "payload",
        "loot",
        "decoration",
        "marker",
        "seed",
    ):
        require(forbidden not in observation_fields.lower(),
                f"NormalizedObservation contains a noncontractual field: {forbidden}")
    require("composition.qualitySpec().postHeight()" in normalization_body,
            "canonical observation omits the contractual shelter height signal")
    require("assertFalse(shelter.isEmpty()" in normalization_body
            and "assertTrue(shelter.contains(normalizedEntrance)" in normalization_body
            and "assertFalse(accessOffsets.isEmpty()" in normalization_body
            and "offset.getY() == 0" in normalization_body
            and "Math.abs(offset.getX()) + Math.abs(offset.getZ()) == 1" in normalization_body
            and "assertBoundaryOutwardAccess(" in normalization_body,
            "canonical observation lacks shelter, boundary, or same-Y cardinal access assertions")
    require("SiteQuality.values()" in unsupported_body
            and "AbandonedProspectorCampState.values()" in unsupported_body
            and "SupportedPair" in unsupported_body,
            "unsupported cases are not the enum Cartesian complement of the golden")
    require("assertEquals(expected, AbandonedProspectorCampState.eligibleFor(quality)" in eligibility_body,
            "JUnit does not directly compare live per-quality eligibility with the golden")
    require(re.search(
        r"@ParameterizedTest(?:\([^)]*\))?\s*@MethodSource\(\"supportedCases\"\)\s*"
        r"void\s+everySupportedStateQualityRotationSatisfiesIndependentOutputInvariants",
        code,
        re.DOTALL,
    ) is not None, "supported-case annotations are not attached to the active test method")

    expected_regression_cases = (
        ("NORMAL", "COLLAPSED", "NONE", "21"),
        ("RICH", "RECENTLY_ABANDONED", "NONE", "50"),
        ("RICH", "WEATHERED", "NONE", "25"),
        ("RICH", "COLLAPSED", "NONE", "21"),
        ("MOTHERLODE", "RECENTLY_ABANDONED", "NONE", "51"),
        ("MOTHERLODE", "RECENTLY_ABANDONED", "COUNTERCLOCKWISE_90", "8"),
        ("MOTHERLODE", "COLLAPSED", "NONE", "6"),
    )
    actual_regression_cases = tuple(re.findall(
        r"Arguments\.of\(\s*SiteQuality\.([A-Z][A-Z0-9_]*)\s*,\s*"
        r"AbandonedProspectorCampState\.([A-Z][A-Z0-9_]*)\s*,\s*"
        r"Rotation\.([A-Z][A-Z0-9_]*)\s*,\s*([0-9]+)L\s*\)",
        regression_cases_body,
        re.DOTALL,
    ))
    require(actual_regression_cases == expected_regression_cases,
            f"role-aware regression must contain exactly the seven known tuples: {actual_regression_cases}")
    require(re.search(
        r"@ParameterizedTest\(name\s*=\s*\"\{index\}: \{0\}/\{1\}/\{2\}/seed=\{3\}\"\)\s*"
        r"@MethodSource\(\"roleSensitiveReachabilityRegressionCases\"\)\s*"
        r"void\s+roleSensitiveTerrainSupportsKnownReachabilityRegressions",
        code,
        re.DOTALL,
    ) is not None, "role-aware seven-case annotations are not attached to the dedicated method")
    require("ProspectorCampOutcropComposer.compose(" in regression_body
            and "AbandonedProspectorCampComposer.compose(" in regression_body,
            "role-aware regression does not use the same public composition path")
    require("assertFinalOccupancyReachability(" in regression_body
            and "assertFinalOccupancyReachability(" in common_invariant_body
            and "assertCommonOutputInvariants(first, active, diagnostic)" in invariant_body,
            "dedicated and exhaustive tests do not reuse the shared reachability oracle")
    require("assertAir(composition.blocks(), composition.shelterEntrance(), diagnostic)"
            in common_invariant_body
            and "assertAir(composition.blocks(), composition.shelterEntrance().above(), diagnostic)"
            in common_invariant_body
            and "active.shaftHatchColumns().forEach(hatch ->" in common_invariant_body
            and "assertAir(composition.blocks(), hatch.above(), diagnostic)" in common_invariant_body
            and "assertAir(composition.blocks(), hatch.above(2), diagnostic)" in common_invariant_body,
            "common invariants no longer enforce shelter entrance and shaft hatch clearance")
    require("new BlockPos(" not in regression_cases_body and "new BlockPos(" not in regression_body,
            "role-aware regression contains hard-coded composition coordinates")
    for evidence_surface in (
        "ROLE_AWARE_CASE_EVIDENCE",
        "VEGETATION_COLUMNS_OBSERVED",
        "SAMPLE_COLUMNS_OBSERVED",
        "ROLE_AWARE_COLUMNS_TRAVERSED",
        "ENTRY_CANDIDATE_COUNT",
        "PATH_COUNT",
        "REACHABLE_PATH_COUNT",
        "RESULT=PASS",
    ):
        require(evidence_surface in regression_body,
                f"role-aware regression evidence is missing: {evidence_surface}")

    required_assertion_surfaces = (
        "canonicalDigest(first)",
        "assertCommonOutputInvariants(first, active, diagnostic)",
        "assertStateSpecificObservable(first, active, diagnostic)",
        "assertFinalOccupancyReachability(composition, active.shaftHatchColumns(), diagnostic)",
        "active.shaftHatchColumns()",
        "CampfireBlock.LIT",
        "placedFootprint()",
        "METAMORPHIC_SHAFT_ORIGIN = new BlockPos(4, 90, 4)",
        "inverseNormalizedObservation(composition, METAMORPHIC_SHAFT_ORIGIN)",
        "NormalizedObservation",
        "metamorphicCases",
        "findCommonSeed",
        "POSITION_LIST_ORDER",
        "BOUNDARY_LIST_ORDER",
        "NORMALIZED_OBSERVATION_ORDER",
        "canonicalRotation",
        "shelterColumns",
        "shelterPostHeight",
        "shelterEntrance",
        "cardinalAccessOffsets",
        "accessOffsets",
        "boundarySides",
        "assertBoundaryOutwardAccess",
        "entrancesOnMaximumZ",
        "accessesBeyondMaximumZ",
    )
    for surface in required_assertion_surfaces:
        require(surface in code, f"critical JUnit assertion surface is missing: {surface}")
    require("canonicalDigest(first)" in invariant_body
            and "assertCommonOutputInvariants(first, active, diagnostic)" in invariant_body
            and "assertStateSpecificObservable(first, active, diagnostic)" in invariant_body,
            "supported test method does not execute all critical assertion groups")
    require("assertEquals(first.shelterEntrance(), second.shelterEntrance(), diagnostic)"
            in invariant_body,
            "exact shelter entrance determinism assertion is missing")
    require('.append("entrance:")' in digest_body
            and "position(composition.shelterEntrance())" in digest_body,
            "canonical digest no longer includes the exact shelter entrance")
    require("new HashSet<>(observations.values()).size()" in rotation_body,
            "rotation method does not compare normalized observations across all rotations")

    reachability_body = method_body(test, "assertFinalOccupancyReachability")
    cardinal_body = method_body(test, "cardinallyAdjacent")
    synthetic_air_body = method_body(test, "isSyntheticAir")
    synthetic_passability_body = method_body(test, "isSyntheticPassableColumn")
    require("domainMargin = 1" in reachability_body
            and "for (int x = minimumX; x <= maximumX; x++)" in reachability_body
            and "for (int z = minimumZ; z <= maximumZ; z++)" in reachability_body,
            "reachability oracle lacks an explicit bounded synthetic terrain domain")
    require("state == null || state.isAir()" in synthetic_air_body,
            "reachability oracle does not model absent planned cells as synthetic air")
    require("int supportY = standingY - 1" in reachability_body
            and "Set<BlockPos> syntheticSupport = new HashSet<>()" in reachability_body
            and "syntheticSupport.add(new BlockPos(x, supportY, z))" in reachability_body
            and "for (BlockPos support : syntheticSupport)" in reachability_body
            and "BlockPos column = support.above()" in reachability_body
            and "isSyntheticPassableColumn(composition, column)" in reachability_body,
            "reachability oracle lacks uniform support plus role-aware final occupancy")
    require("isSyntheticAir(composition.blocks().get(column))" not in reachability_body
            and "isSyntheticAir(composition.blocks().get(column.above()))" not in reachability_body,
            "reachability oracle still applies unconditional Y/Y+1 rejection before role dispatch")

    terrain_support_branch = re.search(
        r"if\s*\(\s*role\s*==\s*ProspectorCampOutcropComposer\.ComponentRole\.VEGETATION\s*"
        r"\|\|\s*role\s*==\s*ProspectorCampOutcropComposer\.ComponentRole\.SAMPLE\s*\)\s*"
        r"\{(?P<body>[^{}]*)\}",
        synthetic_passability_body,
        re.DOTALL,
    )
    require(terrain_support_branch is not None,
            "synthetic passability lacks an explicit VEGETATION/SAMPLE support branch")
    terrain_support_body = terrain_support_branch.group("body")
    require("composition.blocks().get(column.above())" in terrain_support_body
            and "composition.blocks().get(column.above(2))" in terrain_support_body
            and terrain_support_body.count("isSyntheticAir(") == 2,
            "VEGETATION/SAMPLE support must require exactly Y+1 and Y+2 synthetic air")
    require("composition.blocks().get(column)" not in terrain_support_body,
            "VEGETATION/SAMPLE support is still rejected solely by non-air at Y")

    flat_terrain_branch = re.search(
        r"if\s*\(\s*role\s*==\s*null\s*"
        r"\|\|\s*role\s*==\s*ProspectorCampOutcropComposer\.ComponentRole\.PATH\s*"
        r"\|\|\s*role\s*==\s*ProspectorCampOutcropComposer\.ComponentRole\.ACCESS\s*\)\s*"
        r"\{(?P<body>[^{}]*)\}",
        synthetic_passability_body,
        re.DOTALL,
    )
    require(flat_terrain_branch is not None,
            "synthetic passability lacks the explicit absent/PATH/ACCESS branch")
    flat_terrain_body = flat_terrain_branch.group("body")
    require("composition.blocks().get(column)" in flat_terrain_body
            and "composition.blocks().get(column.above())" in flat_terrain_body
            and "column.above(2)" not in flat_terrain_body
            and flat_terrain_body.count("isSyntheticAir(") == 2,
            "absent/PATH/ACCESS synthetic body/head semantics changed")
    require("composition.reservedSurfaceColumns().get(column)" in synthetic_passability_body,
            "synthetic passability does not use observable component roles")
    require(synthetic_passability_body.strip().endswith("return false;"),
            "synthetic passability must block every other role without a default allow")
    require("columnsWithRole(" in reachability_body
            and "ProspectorCampOutcropComposer.ComponentRole.ACCESS" in reachability_body
            and "cardinallyAdjacent(pos, composition.shelterEntrance())" in reachability_body,
            "reachability oracle does not derive adjacent ACCESS-only entry candidates")
    require("routeColumns(composition).stream()" not in reachability_body,
            "reachability oracle still derives entry candidates from PATH-or-ACCESS routes")
    require("reachablePathCount > 0" in reachability_body
            and "ComponentRole.PATH" in reachability_body,
            "reachability oracle does not target any reachable PATH")
    require("first.getY() == second.getY()" in cardinal_body
            and "Math.abs(first.getX() - second.getX())"
            " + Math.abs(first.getZ() - second.getZ()) == 1" in cardinal_body,
            "reachability oracle does not preserve same-Y four-cardinal connectivity")
    require("ComponentRole.SHELTER" in reachability_body
            and "shaftHatchColumns" in reachability_body
            and "composition.shaftOrigin()" in reachability_body,
            "reachability oracle does not exclude shelter, shaft, and hatch columns")
    for obsolete in (
        "composition.blocks().keySet().forEach(pos -> candidateColumns.add",
        "BlockPos start = composition.shelterEntrance()",
        "passable.containsAll(accessTargets)",
        "accessTargets.forEach",
        "occupancy.connected(start",
    ):
        require(obsolete not in reachability_body,
                f"obsolete keySet/entrance/all-ACCESS reachability pattern remains: {obsolete}")

    forbidden_test_oracles = (
        "reconstructShelter",
        "selectedCandidate",
        "hasReachableSurfaceOnePath",
        "isReachableSurfaceColumn",
        "campFacingShelterEdge(",
        "ShelterAccessCandidate",
        "accessCandidates()",
        "detourCandidates",
        "mix64",
        "SELECTION_SALT",
        "ArrayDeque",
        "RouteTopologySignature",
        "routeTopologySignature(",
    )
    for forbidden in forbidden_test_oracles:
        require(forbidden not in code, f"duplicated production oracle remains in JUnit: {forbidden}")
    require("getDeclared" not in code and "setAccessible" not in code,
            "JUnit uses reflection into production helpers")


def validate() -> None:
    quality_source = read(JAVA / "core/SiteQuality.java")
    state_source = read(JAVA / "worldgen/AbandonedProspectorCampState.java")
    qualities = enum_constants(quality_source, "SiteQuality")
    states = enum_constants(state_source, "AbandonedProspectorCampState")
    eligibility = live_eligibility(state_source, qualities, states)

    supported_pairs = sum(len(eligible) for eligible in eligibility.values())
    total_pairs = len(qualities) * len(states)
    unsupported_pairs = total_pairs - supported_pairs
    require(supported_pairs == 18, f"live supported-pair count changed: {supported_pairs}")
    require(unsupported_pairs == 7, f"live unsupported-complement count changed: {unsupported_pairs}")

    validate_production_wiring(states)
    validate_exhaustive_junit(qualities, states, eligibility, supported_pairs, unsupported_pairs)
    validate_loot()

    reports = tuple(DOCS / name for name in (
        "README.md",
        "LOOT_CONTRACT.md",
        "TEST_PLAN.md",
        "ABANDONED_STATE_MATRIX.csv",
        "STRUCTURE_DIMENSION_REPORT.csv",
    ))
    reports_present = sum(path.is_file() for path in reports)
    print(
        "PASS: abandoned prospector camp static wiring and exhaustive JUnit declaration; "
        f"qualities={len(qualities)}; states={len(states)}; "
        f"supported_pairs={supported_pairs}; unsupported_pairs={unsupported_pairs}; "
        f"rotation_count=4; supported_rotation_cases={supported_pairs * 4}; "
        f"reports_present_informational={reports_present}/{len(reports)}"
    )
    print(
        "counter_sources: qualities=live SiteQuality enum; states=live AbandonedProspectorCampState enum; "
        "supported_pairs=sum(live eligibleFor branches); unsupported_pairs=quality_count*state_count-supported; "
        "rotation_count=Minecraft Rotation.values() contract asserted by JUnit; reports=existence only"
    )


def main() -> int:
    try:
        validate()
        return 0
    except (AssertionError, FileNotFoundError, KeyError, ValueError, json.JSONDecodeError) as failure:
        print(f"FAIL: {failure}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
