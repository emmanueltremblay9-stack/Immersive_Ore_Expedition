#!/usr/bin/env python3
"""Validate the expedition compass direction and gear-animation assets."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any


MODULE_ROOT = Path(__file__).resolve().parents[1]
MODEL_ROOT = (
    MODULE_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "immersive_ore_expedition"
    / "models"
    / "item"
)
BBMODEL_PATH = MODULE_ROOT / "src" / "main" / "blockbench" / "expedition_compass.bbmodel"
REPORT_PATH = MODULE_ROOT / "build" / "asset-validation" / "expedition_compass_validation.json"

ANGLE_PREDICATE = "immersive_ore_expedition:angle"
GEAR_PREDICATE = "immersive_ore_expedition:gear_phase"
MODEL_PREFIX = "immersive_ore_expedition:item/expedition_compass_"
ANGLE_COUNT = 32
GEAR_PHASE_COUNT = 4
ELEMENT_COUNT = 64
GEAR_GROUPS = {
    "calibration_gear_large": ([9.2, 6.6, 10.34], 90.0),
    "calibration_gear_small": ([5.45, 9.2, 10.42], 90.0),
    "calibration_gear_copper": ([5.9, 6.6, 10.44], -90.0),
}
NEEDLE_NAMES = {
    "locator_needle_forward",
    "locator_needle_tail",
    "locator_needle_cyan_tip",
}
GEAR_NAMES = {
    f"{group_name}_{part}"
    for group_name in GEAR_GROUPS
    for part in ("core", "tooth_north", "tooth_east", "tooth_south", "tooth_west")
}


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def model_name(angle: int, gear_phase: int) -> str:
    suffix = "" if gear_phase == 0 else f"_g{gear_phase:02d}"
    return f"expedition_compass_{angle:02d}{suffix}.json"


def model_reference(angle: int, gear_phase: int) -> str:
    return MODEL_PREFIX + model_name(angle, gear_phase).removesuffix(".json").removeprefix(
        "expedition_compass_"
    )


def expected_model_paths() -> list[Path]:
    return [
        MODEL_ROOT / model_name(angle, gear_phase)
        for gear_phase in range(GEAR_PHASE_COUNT)
        for angle in range(ANGLE_COUNT)
    ]


def expected_overrides() -> list[dict[str, Any]]:
    overrides: list[dict[str, Any]] = []
    for gear_phase in range(GEAR_PHASE_COUNT):
        for angle in range(ANGLE_COUNT):
            angle_threshold = 0.0 if angle == 0 else (angle - 0.5) / ANGLE_COUNT
            overrides.append(
                {
                    "predicate": {
                        ANGLE_PREDICATE: angle_threshold,
                        GEAR_PREDICATE: gear_phase / GEAR_PHASE_COUNT,
                    },
                    "model": model_reference(angle, gear_phase),
                }
            )
        overrides.append(
            {
                "predicate": {
                    ANGLE_PREDICATE: (ANGLE_COUNT - 0.5) / ANGLE_COUNT,
                    GEAR_PREDICATE: gear_phase / GEAR_PHASE_COUNT,
                },
                "model": model_reference(0, gear_phase),
            }
        )
    return overrides


def element_map(model: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {element["name"]: element for element in model.get("elements", [])}


def geometry_signature(elements: dict[str, dict[str, Any]], names: set[str]) -> str:
    geometry = {
        name: {
            key: elements[name].get(key)
            for key in ("from", "to", "rotation")
            if key in elements[name]
        }
        for name in sorted(names)
    }
    return json.dumps(geometry, sort_keys=True, separators=(",", ":"))


def resolve_override(
    overrides: list[dict[str, Any]], angle_value: float, gear_value: float
) -> str | None:
    selected: str | None = None
    properties = {ANGLE_PREDICATE: angle_value, GEAR_PREDICATE: gear_value}
    for override in overrides:
        if all(properties.get(key, 0.0) >= float(value) for key, value in override["predicate"].items()):
            selected = override["model"]
    return selected


def validate() -> tuple[list[str], dict[str, Any]]:
    failures: list[str] = []
    expected_paths = expected_model_paths()
    actual_paths = sorted(
        path
        for path in MODEL_ROOT.glob("expedition_compass*.json")
        if path.name != "expedition_compass.json"
    )
    if actual_paths != sorted(expected_paths):
        missing = sorted(path.name for path in set(expected_paths) - set(actual_paths))
        extra = sorted(path.name for path in set(actual_paths) - set(expected_paths))
        failures.append(f"Runtime model matrix mismatch; missing={missing}, extra={extra}")

    base_path = MODEL_ROOT / "expedition_compass.json"
    if not base_path.is_file():
        failures.append("Missing base expedition_compass.json")
        return failures, {"status": "FAIL", "failures": failures}

    base = load_json(base_path)
    overrides = base.get("overrides", [])
    if overrides != expected_overrides():
        failures.append("Base model overrides differ from the exact 32x4 predicate matrix")

    parsed_models: dict[tuple[int, int], dict[str, Any]] = {}
    expected_names: set[str] | None = None
    for gear_phase in range(GEAR_PHASE_COUNT):
        for angle in range(ANGLE_COUNT):
            path = MODEL_ROOT / model_name(angle, gear_phase)
            if not path.is_file():
                continue
            model = load_json(path)
            elements = element_map(model)
            if len(elements) != ELEMENT_COUNT:
                failures.append(f"{path.name} contains {len(elements)} named elements, expected {ELEMENT_COUNT}")
            if "overrides" in model:
                failures.append(f"{path.name} unexpectedly contains overrides")
            if expected_names is None:
                expected_names = set(elements)
            elif set(elements) != expected_names:
                failures.append(f"{path.name} element-name set differs from phase zero")
            parsed_models[(angle, gear_phase)] = model

    if len(parsed_models) == ANGLE_COUNT * GEAR_PHASE_COUNT and expected_names is not None:
        fixed_names = expected_names - NEEDLE_NAMES - GEAR_NAMES
        fixed_signature = geometry_signature(element_map(parsed_models[(0, 0)]), fixed_names)
        for gear_phase in range(GEAR_PHASE_COUNT):
            expected_gear = geometry_signature(
                element_map(parsed_models[(0, gear_phase)]),
                GEAR_NAMES,
            )
            for angle in range(ANGLE_COUNT):
                elements = element_map(parsed_models[(angle, gear_phase)])
                if geometry_signature(elements, GEAR_NAMES) != expected_gear:
                    failures.append(
                        f"Gear phase {gear_phase} changes with compass angle {angle}"
                    )
                if geometry_signature(elements, fixed_names) != fixed_signature:
                    failures.append(
                        f"Fixed compass geometry changes at angle={angle}, gear={gear_phase}"
                    )
        for angle in range(ANGLE_COUNT):
            expected_needle = geometry_signature(
                element_map(parsed_models[(angle, 0)]),
                NEEDLE_NAMES,
            )
            for gear_phase in range(GEAR_PHASE_COUNT):
                if geometry_signature(
                    element_map(parsed_models[(angle, gear_phase)]),
                    NEEDLE_NAMES,
                ) != expected_needle:
                    failures.append(
                        f"Compass needle angle {angle} changes with gear phase {gear_phase}"
                    )
        gear_signatures = {
            geometry_signature(element_map(parsed_models[(0, gear_phase)]), GEAR_NAMES)
            for gear_phase in range(GEAR_PHASE_COUNT)
        }
        if len(gear_signatures) != GEAR_PHASE_COUNT:
            failures.append(
                f"Expected {GEAR_PHASE_COUNT} distinct gear geometries, found {len(gear_signatures)}"
            )

    for gear_phase in range(GEAR_PHASE_COUNT):
        for angle in range(ANGLE_COUNT):
            expected = model_reference(angle, gear_phase)
            actual = resolve_override(
                overrides,
                angle / ANGLE_COUNT,
                gear_phase / GEAR_PHASE_COUNT,
            )
            if actual != expected:
                failures.append(
                    f"Predicate resolver selected {actual} for angle={angle}, gear={gear_phase}; "
                    f"expected {expected}"
                )

    if not BBMODEL_PATH.is_file():
        failures.append("Missing Blockbench source")
        bbmodel_report: dict[str, Any] = {}
    else:
        bbmodel = load_json(BBMODEL_PATH)
        groups = {group["name"]: group for group in bbmodel.get("groups", [])}
        animations = bbmodel.get("animations", [])
        if bbmodel.get("meta", {}).get("model_format") != "java_block_sequence":
            failures.append("Unexpected Blockbench model format")
        if len(bbmodel.get("elements", [])) != ELEMENT_COUNT:
            failures.append("Blockbench source does not contain 64 elements")
        if len(groups) != 14:
            failures.append(f"Blockbench source contains {len(groups)} groups, expected 14")
        for name, (origin, _) in GEAR_GROUPS.items():
            if groups.get(name, {}).get("origin") != origin:
                failures.append(f"{name} pivot differs from {origin}")
        if len(animations) != 1:
            failures.append(f"Blockbench source contains {len(animations)} animations, expected 1")
        else:
            animation = animations[0]
            if animation.get("name") != "animation.expedition_compass.gear_drive":
                failures.append("Unexpected Blockbench gear animation name")
            if animation.get("loop") != "loop" or float(animation.get("length", 0)) != 0.4:
                failures.append("Blockbench gear animation must loop every 0.4 seconds")
            animators = animation.get("animators", {})
            for name, (_, expected_z) in GEAR_GROUPS.items():
                group = groups.get(name, {})
                keyframes = animators.get(group.get("uuid"), {}).get("keyframes", [])
                rotations = [
                    float(keyframe["data_points"][0]["z"])
                    for keyframe in keyframes
                    if keyframe.get("channel") == "rotation"
                ]
                if rotations != [0.0, expected_z]:
                    failures.append(f"{name} animation rotations are {rotations}, expected [0, {expected_z}]")
        bbmodel_report = {
            "elements": len(bbmodel.get("elements", [])),
            "groups": len(groups),
            "animations": len(animations),
            "gear_pivots": {
                name: groups.get(name, {}).get("origin") for name in GEAR_GROUPS
            },
        }

    report = {
        "status": "PASS" if not failures else "FAIL",
        "model_count": len(actual_paths),
        "override_count": len(overrides),
        "angle_count": ANGLE_COUNT,
        "gear_phase_count": GEAR_PHASE_COUNT,
        "bbmodel": bbmodel_report,
        "failures": failures,
    }
    return failures, report


def main() -> int:
    failures, report = validate()
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    REPORT_PATH.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8", newline="\n")
    if failures:
        print(f"FAIL: {len(failures)} expedition compass asset validation error(s)")
        for failure in failures:
            print(f"- {failure}")
        print(f"Report: {REPORT_PATH}")
        return 1
    print("PASS: expedition compass direction and gear-animation assets")
    print(f"Runtime models: {report['model_count']}")
    print(f"Predicate overrides: {report['override_count']}")
    print(f"Blockbench animations: {report['bbmodel']['animations']}")
    print(f"Report: {REPORT_PATH}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
