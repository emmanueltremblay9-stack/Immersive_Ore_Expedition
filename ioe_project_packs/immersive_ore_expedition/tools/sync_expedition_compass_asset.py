#!/usr/bin/env python3
"""Synchronize the authored expedition-compass visual pass across all angle frames."""

from __future__ import annotations

import argparse
import base64
import json
import math
import uuid
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
TEXTURE_PATH = (
    MODULE_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "immersive_ore_expedition"
    / "textures"
    / "item"
    / "expedition_compass.png"
)

ANGLE_FRAME_COUNT = 32
GEAR_PHASE_COUNT = 4
ANGLE_PREDICATE = "immersive_ore_expedition:angle"
GEAR_PHASE_PREDICATE = "immersive_ore_expedition:gear_phase"
ANIMATION_GROUPS = (
    "calibration_gear_large",
    "calibration_gear_small",
    "calibration_gear_copper",
)

MATERIALS: dict[str, list[int]] = {
    "steel": [0, 0, 2, 2],
    "shadow": [2, 0, 4, 2],
    "brass": [4, 0, 6, 2],
    "copper": [6, 0, 8, 2],
    "worn": [8, 0, 10, 2],
    "dark_green": [10, 0, 12, 2],
    "locator_green": [12, 0, 14, 2],
    "cyan": [14, 0, 16, 2],
    "glass": [0, 2, 2, 4],
    "parchment": [0, 2, 2, 4],
}

GROUP_RENAMES = {
    "outer_brass_ring": "outer_frame",
    "dark_steel_backplate": "compass_body",
    "inner_dial": "dial_recess",
    "locator_needle": "direction_marker",
    "cyan_crystal_core": "ore_indicator",
    "green_indicator_marks": "cardinal_markers",
    "copper_side_caps": "side_grips",
    "small_bolts": "fasteners",
    "grip_tab": "service_grip",
    "optional_signal_antenna": "expedition_lug",
}

BBMODEL_GEOMETRY_BOUNDS: dict[str, tuple[list[float], list[float]]] = {
    "outer_brass_ring_top_left_bevel": ([2.2, 11.4, 6.85], [4.6, 13.8, 9.45]),
    "outer_brass_ring_top_right_bevel": ([11.4, 11.4, 6.85], [13.8, 13.8, 9.45]),
    "outer_brass_ring_bottom_left_bevel": ([2.2, 2.2, 6.85], [4.6, 4.6, 9.45]),
    "outer_brass_ring_bottom_right_bevel": ([11.4, 2.2, 6.85], [13.8, 4.6, 9.45]),
    "dark_steel_backplate_horizontal": ([3, 5, 6.05], [13, 11, 6.7]),
    "dark_steel_backplate_vertical": ([5, 3, 6.1], [11, 13, 6.65]),
    "minor_marker_north_west": ([4, 11, 9.75], [5, 12, 10.15]),
    "minor_marker_north_east": ([11, 11, 9.75], [12, 12, 10.15]),
    "minor_marker_south_west": ([4, 4, 9.75], [5, 5, 10.15]),
    "minor_marker_south_east": ([11, 4, 9.75], [12, 5, 10.15]),
    "service_fastener_left": ([1.5, 7.5, 9.55], [2.5, 8.5, 10.1]),
    "service_fastener_right": ([13.5, 7.5, 9.55], [14.5, 8.5, 10.1]),
    "top_lug_bridge": ([6.5, 16.4, 7.35], [9.5, 17.3, 9.05]),
}

NEEDLE_NAMES = {
    "locator_needle_forward",
    "locator_needle_tail",
    "locator_needle_cyan_tip",
}

# Field Survey Compass Mk III.  The repeated octagonal proportions are
# deliberate: frame, backplate, bezel, and dial all share the same stepped
# silhouette instead of competing square/cross shapes.
MKIII_ELEMENT_SPECS: dict[str, dict[str, Any]] = {
    "outer_brass_ring_top": {"from": [5, 12.8, 6.8], "to": [11, 14, 9.15], "side": "steel", "up": "worn"},
    "outer_brass_ring_bottom": {"from": [5, 2, 6.8], "to": [11, 3.2, 9.15], "side": "steel", "up": "worn"},
    "outer_brass_ring_left": {"from": [2, 5, 6.8], "to": [3.2, 11, 9.15], "side": "steel", "up": "worn"},
    "outer_brass_ring_right": {"from": [12.8, 5, 6.8], "to": [14, 11, 9.15], "side": "steel", "up": "worn"},
    "outer_brass_ring_top_left_bevel": {
        "from": [3, 11.2, 6.85], "to": [5, 12.2, 9.2], "side": "brass", "up": "worn",
        "rotation": {"origin": [4, 11.7, 8], "axis": "z", "angle": -45.0, "rescale": True},
    },
    "outer_brass_ring_top_right_bevel": {
        "from": [11, 11.2, 6.85], "to": [13, 12.2, 9.2], "side": "brass", "up": "worn",
        "rotation": {"origin": [12, 11.7, 8], "axis": "z", "angle": 45.0, "rescale": True},
    },
    "outer_brass_ring_bottom_left_bevel": {
        "from": [3, 3.8, 6.85], "to": [5, 4.8, 9.2], "side": "brass", "up": "worn",
        "rotation": {"origin": [4, 4.3, 8], "axis": "z", "angle": 45.0, "rescale": True},
    },
    "outer_brass_ring_bottom_right_bevel": {
        "from": [11, 3.8, 6.85], "to": [13, 4.8, 9.2], "side": "brass", "up": "worn",
        "rotation": {"origin": [12, 4.3, 8], "axis": "z", "angle": -45.0, "rescale": True},
    },
    "dark_steel_backplate_core": {"from": [3.4, 3.4, 6], "to": [12.6, 12.6, 6.55], "side": "shadow", "up": "steel"},
    "dark_steel_backplate_horizontal": {"from": [2.8, 5, 6.08], "to": [13.2, 11, 6.62], "side": "steel", "up": "worn"},
    "dark_steel_backplate_vertical": {"from": [5, 2.8, 6.12], "to": [11, 13.2, 6.58], "side": "steel", "up": "shadow"},
    "dial_recess_horizontal": {"from": [3.8, 5.7, 9.48], "to": [12.2, 10.3, 9.72], "side": "glass", "up": "glass"},
    "dial_recess_vertical": {"from": [5.7, 3.8, 9.49], "to": [10.3, 12.2, 9.73], "side": "glass", "up": "glass"},
    "dial_face_plate": {"from": [4.6, 4.6, 9.5], "to": [11.4, 11.4, 9.74], "side": "glass", "up": "glass"},
    "locator_needle_forward": {"from": [7.6, 7.9, 10.12], "to": [8.4, 11.55, 10.4], "side": "locator_green", "up": "cyan", "shade": False, "needle_origin": [8, 8, 10.25]},
    "locator_needle_tail": {"from": [7.7, 4.7, 10.08], "to": [8.3, 8.15, 10.36], "side": "dark_green", "up": "locator_green", "shade": False, "needle_origin": [8, 8, 10.22]},
    "locator_needle_cyan_tip": {"from": [7.15, 11.2, 10.42], "to": [8.85, 12.35, 10.72], "side": "cyan", "up": "cyan", "shade": False, "needle_origin": [8, 8, 10.45]},
    "ore_indicator_hub": {"from": [7.25, 7.25, 10.76], "to": [8.75, 8.75, 11.08], "side": "steel", "up": "worn"},
    "ore_indicator_glint": {"from": [7.5, 7.6, 11.1], "to": [8.5, 8.4, 11.34], "side": "cyan", "up": "cyan", "shade": False},
    "green_indicator_north": {"from": [7.4, 12.15, 10.05], "to": [8.6, 12.55, 10.25], "side": "cyan", "up": "cyan", "shade": False},
    "green_indicator_south": {"from": [7.4, 3.45, 10.05], "to": [8.6, 3.85, 10.25], "side": "worn", "up": "brass"},
    "green_indicator_west": {"from": [3.45, 7.4, 10.05], "to": [3.85, 8.6, 10.25], "side": "worn", "up": "brass"},
    "green_indicator_east": {"from": [12.15, 7.4, 10.05], "to": [12.55, 8.6, 10.25], "side": "worn", "up": "brass"},
    "minor_marker_north_west": {"from": [4.1, 10.9, 9.83], "to": [5.1, 11.7, 10.05], "side": "brass", "up": "worn"},
    "minor_marker_north_east": {"from": [10.9, 10.9, 9.83], "to": [11.9, 11.7, 10.05], "side": "brass", "up": "worn"},
    "minor_marker_south_west": {"from": [4.1, 4.3, 9.83], "to": [5.1, 5.1, 10.05], "side": "brass", "up": "worn"},
    "minor_marker_south_east": {"from": [10.9, 4.3, 9.83], "to": [11.9, 5.1, 10.05], "side": "brass", "up": "worn"},
    "copper_side_cap_left": {"from": [0.9, 6.7, 7], "to": [2, 9.3, 9.25], "side": "copper", "up": "worn"},
    "copper_side_cap_right": {"from": [14, 6.7, 7], "to": [15.1, 9.3, 9.25], "side": "copper", "up": "worn"},
    "copper_top_cap": {"from": [6.8, 13.9, 7], "to": [9.2, 15.1, 9.25], "side": "copper", "up": "worn"},
    "small_bolt_top_left": {"from": [3.5, 11.6, 10.08], "to": [4.3, 12.6, 10.4], "side": "steel", "up": "worn"},
    "small_bolt_top_right": {"from": [11.7, 11.6, 10.08], "to": [12.5, 12.6, 10.4], "side": "steel", "up": "worn"},
    "small_bolt_bottom_left": {"from": [3.5, 3.4, 10.08], "to": [4.3, 4.4, 10.4], "side": "steel", "up": "worn"},
    "small_bolt_bottom_right": {"from": [11.7, 3.4, 10.08], "to": [12.5, 4.4, 10.4], "side": "steel", "up": "worn"},
    "service_fastener_left": {"from": [1.3, 7.45, 9.58], "to": [2.3, 8.55, 10.02], "side": "shadow", "up": "worn"},
    "service_fastener_right": {"from": [13.7, 7.45, 9.58], "to": [14.7, 8.55, 10.02], "side": "shadow", "up": "worn"},
    "grip_tab_base": {"from": [6.4, 0.2, 6.85], "to": [9.6, 1.65, 9.2], "side": "steel", "up": "worn"},
    "grip_tab_rivet": {"from": [7.4, 0.55, 9.22], "to": [8.6, 1.25, 9.62], "side": "brass", "up": "worn"},
    "top_lug_left_mount": {"from": [6.6, 15.45, 7.15], "to": [7.5, 16.9, 9.15], "side": "copper", "up": "worn"},
    "top_lug_bridge": {"from": [7.45, 16.35, 7.3], "to": [8.55, 17.2, 9], "side": "steel", "up": "worn"},
    "top_lug_right_mount": {"from": [8.5, 15.45, 7.15], "to": [9.4, 16.9, 9.15], "side": "copper", "up": "worn"},
    "calibration_gear_large_core": {"from": [8.25, 5.65, 9.78], "to": [10.15, 7.55, 9.98], "side": "brass", "up": "brass", "rotation": {"origin": [9.2, 6.6, 9.88], "axis": "z", "angle": 45.0, "rescale": False}},
    "calibration_gear_large_tooth_north": {"from": [8.85, 7.35, 9.79], "to": [9.55, 8.35, 10], "side": "brass", "up": "brass"},
    "calibration_gear_large_tooth_south": {"from": [8.85, 4.85, 9.8], "to": [9.55, 5.85, 10.01], "side": "brass", "up": "brass"},
    "calibration_gear_large_tooth_west": {"from": [7.65, 6.25, 9.81], "to": [8.65, 6.95, 10.02], "side": "brass", "up": "brass"},
    "calibration_gear_large_tooth_east": {"from": [9.95, 6.25, 9.82], "to": [10.95, 6.95, 10.03], "side": "brass", "up": "brass"},
    "calibration_gear_small_core": {"from": [4.65, 8.4, 9.78], "to": [6.25, 10, 9.98], "side": "steel", "up": "steel", "rotation": {"origin": [5.45, 9.2, 9.88], "axis": "z", "angle": 45.0, "rescale": False}},
    "calibration_gear_small_tooth_north": {"from": [4.95, 9.8, 9.79], "to": [5.95, 10.35, 10], "side": "steel", "up": "steel"},
    "calibration_gear_small_tooth_south": {"from": [4.95, 8.05, 9.8], "to": [5.95, 8.6, 10.01], "side": "steel", "up": "steel"},
    "calibration_gear_small_tooth_west": {"from": [4.3, 8.7, 9.81], "to": [4.95, 9.7, 10.02], "side": "steel", "up": "steel"},
    "calibration_gear_small_tooth_east": {"from": [6.05, 8.7, 9.82], "to": [6.6, 9.7, 10.03], "side": "steel", "up": "steel"},
    "calibration_gear_copper_core": {"from": [5.05, 5.75, 9.78], "to": [6.75, 7.45, 9.98], "side": "copper", "up": "copper", "rotation": {"origin": [5.9, 6.6, 9.88], "axis": "z", "angle": 45.0, "rescale": False}},
    "calibration_gear_copper_tooth_north": {"from": [5.4, 7.25, 9.79], "to": [6.4, 7.75, 10], "side": "copper", "up": "copper"},
    "calibration_gear_copper_tooth_south": {"from": [5.4, 5.45, 9.8], "to": [6.4, 5.95, 10.01], "side": "copper", "up": "copper"},
    "calibration_gear_copper_tooth_west": {"from": [4.75, 6.1, 9.81], "to": [5.25, 7.1, 10.02], "side": "copper", "up": "copper"},
    "calibration_gear_copper_tooth_east": {"from": [6.5, 6.1, 9.82], "to": [7.05, 7.1, 10.03], "side": "copper", "up": "copper"},
    "dial_bezel_top": {"from": [5.6, 12.15, 9.73], "to": [10.4, 12.55, 10.02], "side": "brass", "up": "brass"},
    "dial_bezel_bottom": {"from": [5.6, 3.45, 9.73], "to": [10.4, 3.85, 10.02], "side": "brass", "up": "brass"},
    "dial_bezel_left": {"from": [3.45, 5.6, 9.73], "to": [3.85, 10.4, 10.02], "side": "brass", "up": "brass"},
    "dial_bezel_right": {"from": [12.15, 5.6, 9.73], "to": [12.55, 10.4, 10.02], "side": "brass", "up": "brass"},
    "dial_bezel_north_west": {"from": [3.75, 10.75, 9.73], "to": [5.45, 11.15, 10.02], "side": "brass", "up": "brass", "rotation": {"origin": [4.6, 10.95, 9.88], "axis": "z", "angle": 45.0, "rescale": True}},
    "dial_bezel_north_east": {"from": [10.55, 10.75, 9.73], "to": [12.25, 11.15, 10.02], "side": "brass", "up": "brass", "rotation": {"origin": [11.4, 10.95, 9.88], "axis": "z", "angle": -45.0, "rescale": True}},
    "dial_bezel_south_west": {"from": [3.75, 4.85, 9.73], "to": [5.45, 5.25, 10.02], "side": "brass", "up": "brass", "rotation": {"origin": [4.6, 5.05, 9.88], "axis": "z", "angle": -45.0, "rescale": True}},
    "dial_bezel_south_east": {"from": [10.55, 4.85, 9.73], "to": [12.25, 5.25, 10.02], "side": "brass", "up": "brass", "rotation": {"origin": [11.4, 5.05, 9.88], "axis": "z", "angle": 45.0, "rescale": True}},
}

GEAR_NAMES = tuple(name for name in MKIII_ELEMENT_SPECS if name.startswith("calibration_gear_"))
DIAL_BEZEL_NAMES = tuple(name for name in MKIII_ELEMENT_SPECS if name.startswith("dial_bezel_"))
GEAR_ASSEMBLIES: dict[str, dict[str, Any]] = {
    "calibration_gear_large_": {"center": [9.2, 6.6], "direction": 1.0},
    "calibration_gear_small_": {"center": [5.45, 9.2], "direction": 1.0},
    "calibration_gear_copper_": {"center": [5.9, 6.6], "direction": -1.0},
}

GROUP_MEMBERS: dict[str, tuple[str, ...]] = {
    "outer_frame": tuple(list(MKIII_ELEMENT_SPECS)[0:8]),
    "compass_body": tuple(list(MKIII_ELEMENT_SPECS)[8:11]),
    "dial_recess": (
        "dial_recess_horizontal", "dial_recess_vertical", "dial_face_plate",
        *DIAL_BEZEL_NAMES, *GEAR_NAMES,
    ),
    "direction_marker": (
        "locator_needle_forward", "locator_needle_tail", "locator_needle_cyan_tip",
    ),
    "ore_indicator": ("ore_indicator_hub", "ore_indicator_glint"),
    "cardinal_markers": (
        "green_indicator_north", "green_indicator_south", "green_indicator_west", "green_indicator_east",
        "minor_marker_north_west", "minor_marker_north_east", "minor_marker_south_west", "minor_marker_south_east",
    ),
    "side_grips": ("copper_side_cap_left", "copper_side_cap_right", "copper_top_cap"),
    "fasteners": (
        "small_bolt_top_left", "small_bolt_top_right", "small_bolt_bottom_left", "small_bolt_bottom_right",
        "service_fastener_left", "service_fastener_right",
    ),
    "service_grip": ("grip_tab_base", "grip_tab_rivet"),
    "expedition_lug": ("top_lug_left_mount", "top_lug_bridge", "top_lug_right_mount"),
}


def material_faces(side: str, *, up: str | None = None, down: str = "shadow") -> dict[str, Any]:
    up = up or side
    result: dict[str, Any] = {}
    for face in ("north", "east", "south", "west"):
        result[face] = {"uv": MATERIALS[side].copy(), "texture": f"#{side}"}
    result["up"] = {"uv": MATERIALS[up].copy(), "texture": f"#{up}"}
    result["down"] = {"uv": MATERIALS[down].copy(), "texture": f"#{down}"}
    return result


def find_element(elements: list[dict[str, Any]], *names: str) -> dict[str, Any]:
    matches = [element for element in elements if element.get("name") in names]
    if len(matches) != 1:
        raise ValueError(f"Expected one element named from {names}, found {len(matches)}")
    return matches[0]


def rewrite_element(
    elements: list[dict[str, Any]],
    old_name: str,
    new_name: str,
    *,
    from_: list[float] | None = None,
    to: list[float] | None = None,
    side: str | None = None,
    up: str | None = None,
    remove_rotation: bool = False,
    shade: bool = True,
) -> None:
    element = find_element(elements, old_name, new_name)
    element["name"] = new_name
    if from_ is not None:
        element["from"] = from_
    if to is not None:
        element["to"] = to
    if remove_rotation:
        element.pop("rotation", None)
    if side is not None:
        element["faces"] = material_faces(side, up=up)
    if shade:
        element.pop("shade", None)
    else:
        element["shade"] = False


def apply_mkiii_design(elements: list[dict[str, Any]]) -> None:
    actual_names = {str(element.get("name")) for element in elements}
    expected_names = set(MKIII_ELEMENT_SPECS)
    for missing_name in MKIII_ELEMENT_SPECS:
        if missing_name not in actual_names:
            elements.append({"name": missing_name, "from": [0, 0, 0], "to": [1, 1, 1], "faces": {}})
    actual_names = {str(element.get("name")) for element in elements}
    if actual_names != expected_names:
        missing = sorted(expected_names - actual_names)
        extra = sorted(actual_names - expected_names)
        raise ValueError(f"Mk III element-set mismatch; missing={missing}, extra={extra}")

    for name, spec in MKIII_ELEMENT_SPECS.items():
        element = find_element(elements, name)
        element["from"] = spec["from"].copy()
        element["to"] = spec["to"].copy()
        element["faces"] = material_faces(spec["side"], up=spec.get("up"))
        if spec.get("shade", True):
            element.pop("shade", None)
        else:
            element["shade"] = False

        if name in NEEDLE_NAMES:
            rotation = element.setdefault(
                "rotation",
                {"origin": [8, 8, 10.3], "axis": "z", "angle": 0.0, "rescale": True},
            )
            rotation["origin"] = spec["needle_origin"].copy()
            rotation["axis"] = "z"
            rotation["rescale"] = True
        elif "rotation" in spec:
            element["rotation"] = dict(spec["rotation"])
            element["rotation"]["origin"] = spec["rotation"]["origin"].copy()
        else:
            element.pop("rotation", None)


def apply_heading_geometry(elements: list[dict[str, Any]], heading: float) -> None:
    """Represent a 22.5-degree heading using cardinal boxes plus a legal residual rotation."""
    cardinal = (round(heading / 90.0) * 90.0) % 360.0
    residual = ((heading - cardinal + 180.0) % 360.0) - 180.0
    if residual not in {-45.0, -22.5, 0.0, 22.5, 45.0}:
        raise ValueError(f"Heading {heading} cannot be represented by Minecraft element rotations")

    radians = math.radians(cardinal)
    cos_value = round(math.cos(radians))
    sin_value = round(math.sin(radians))
    for name in NEEDLE_NAMES:
        element = find_element(elements, name)
        spec = MKIII_ELEMENT_SPECS[name]
        corners = []
        for x in (spec["from"][0], spec["to"][0]):
            for y in (spec["from"][1], spec["to"][1]):
                dx = x - 8.0
                dy = y - 8.0
                corners.append(
                    (
                        8.0 + dx * cos_value + dy * sin_value,
                        8.0 - dx * sin_value + dy * cos_value,
                    )
                )
        element["from"] = [
            round(min(point[0] for point in corners), 4),
            round(min(point[1] for point in corners), 4),
            spec["from"][2],
        ]
        element["to"] = [
            round(max(point[0] for point in corners), 4),
            round(max(point[1] for point in corners), 4),
            spec["to"][2],
        ]
        rotation = element["rotation"]
        rotation["origin"] = spec["needle_origin"].copy()
        rotation["angle"] = -residual


def apply_synchronized_gear_geometry(elements: list[dict[str, Any]], phase: float) -> None:
    """Phase-lock the three visible gears to the independent runtime gear phase.

    The large brass and small steel gears rotate clockwise together. The
    meshing copper idler counter-rotates by the same amount. Cardinal motion is
    baked into each cube's bounds so the remaining element rotation always
    uses one of Minecraft's legal 22.5-degree increments.
    """
    for prefix, assembly in GEAR_ASSEMBLIES.items():
        center_x, center_y = assembly["center"]
        gear_phase = (phase * float(assembly["direction"])) % 360.0
        cardinal = (round(gear_phase / 90.0) * 90.0) % 360.0
        residual = ((gear_phase - cardinal + 180.0) % 360.0) - 180.0
        if residual not in {-45.0, -22.5, 0.0, 22.5, 45.0}:
            raise ValueError(f"Gear phase {gear_phase} cannot be represented by Minecraft element rotations")

        radians = math.radians(cardinal)
        cos_value = round(math.cos(radians))
        sin_value = round(math.sin(radians))
        for name in (candidate for candidate in GEAR_NAMES if candidate.startswith(prefix)):
            element = find_element(elements, name)
            spec = MKIII_ELEMENT_SPECS[name]
            corners = []
            for x in (spec["from"][0], spec["to"][0]):
                for y in (spec["from"][1], spec["to"][1]):
                    dx = x - center_x
                    dy = y - center_y
                    corners.append(
                        (
                            center_x + dx * cos_value + dy * sin_value,
                            center_y - dx * sin_value + dy * cos_value,
                        )
                    )
            element["from"] = [
                round(min(point[0] for point in corners), 4),
                round(min(point[1] for point in corners), 4),
                spec["from"][2],
            ]
            element["to"] = [
                round(max(point[0] for point in corners), 4),
                round(max(point[1] for point in corners), 4),
                spec["to"][2],
            ]

            base_angle = float(spec.get("rotation", {}).get("angle", 0.0))
            angle = base_angle - residual
            if base_angle:
                # A square core is identical after a 90-degree turn. Folding
                # its angle keeps the representation inside Minecraft's safe
                # rotation range without changing its rendered shape.
                while angle > 45.0:
                    angle -= 90.0
                while angle < -45.0:
                    angle += 90.0
            if math.isclose(angle, 0.0, abs_tol=1e-9):
                element.pop("rotation", None)
            else:
                element["rotation"] = {
                    "origin": [
                        center_x,
                        center_y,
                        round((float(spec["from"][2]) + float(spec["to"][2])) / 2.0, 4),
                    ],
                    "axis": "z",
                    "angle": angle,
                    "rescale": False,
                }


def synchronize_group_children(groups: list[dict[str, Any]], elements: list[dict[str, Any]]) -> None:
    index_by_name = {element["name"]: index for index, element in enumerate(elements)}
    groups_by_name = {group["name"]: group for group in groups}
    if set(groups_by_name) != {"root", *GROUP_MEMBERS}:
        raise ValueError(f"Unexpected runtime groups: {sorted(groups_by_name)}")
    groups_by_name["root"]["children"] = list(range(len(elements)))
    for group_name, member_names in GROUP_MEMBERS.items():
        groups_by_name[group_name]["children"] = [index_by_name[name] for name in member_names]


def transform_model(
    model: dict[str, Any],
    angle_frame_index: int,
    gear_phase_index: int,
) -> dict[str, Any]:
    elements = model.get("elements")
    groups = model.get("groups")
    display = model.get("display")
    if not isinstance(elements, list) or not isinstance(groups, list) or not isinstance(display, dict):
        raise ValueError("Compass model is missing elements, groups, or display data")

    model.setdefault("textures", {})["glass"] = (
        "immersive_ore_expedition:item/expedition_compass"
    )

    for name in (
        "outer_brass_ring_top_left_bevel",
        "outer_brass_ring_top_right_bevel",
        "outer_brass_ring_bottom_left_bevel",
        "outer_brass_ring_bottom_right_bevel",
        "dark_steel_backplate_horizontal",
        "dark_steel_backplate_vertical",
    ):
        from_, to = BBMODEL_GEOMETRY_BOUNDS[name]
        rewrite_element(elements, name, name, from_=from_, to=to)

    rewrite_element(
        elements,
        "inner_dial_aged_plate",
        "dial_recess_horizontal",
        from_=[3.5, 5, 9.25],
        to=[12.5, 11, 9.55],
        side="parchment",
    )
    rewrite_element(
        elements,
        "inner_dial_lower_shadow",
        "dial_recess_vertical",
        from_=[5, 3.5, 9.2],
        to=[11, 12.5, 9.5],
        side="parchment",
    )
    rewrite_element(
        elements,
        "inner_dial_worn_highlight",
        "dial_face_plate",
        from_=[4.5, 4.5, 9.56],
        to=[11.5, 11.5, 9.78],
        side="parchment",
    )

    rewrite_element(elements, "cyan_crystal_core", "ore_indicator_hub", shade=False)
    rewrite_element(elements, "cyan_crystal_highlight", "ore_indicator_glint", shade=False)

    for direction in ("north_west", "north_east", "south_west", "south_east"):
        marker_name = f"minor_marker_{direction}"
        from_, to = BBMODEL_GEOMETRY_BOUNDS[marker_name]
        rewrite_element(
            elements,
            f"green_indicator_{direction}",
            marker_name,
            from_=from_,
            to=to,
            side="worn",
        )

    rewrite_element(
        elements,
        "small_bolt_left_mid",
        "service_fastener_left",
        from_=BBMODEL_GEOMETRY_BOUNDS["service_fastener_left"][0],
        to=BBMODEL_GEOMETRY_BOUNDS["service_fastener_left"][1],
        side="steel",
        up="worn",
    )
    rewrite_element(
        elements,
        "small_bolt_right_mid",
        "service_fastener_right",
        from_=BBMODEL_GEOMETRY_BOUNDS["service_fastener_right"][0],
        to=BBMODEL_GEOMETRY_BOUNDS["service_fastener_right"][1],
        side="steel",
        up="worn",
    )

    rewrite_element(
        elements,
        "optional_signal_antenna_hinge",
        "top_lug_left_mount",
        from_=[6.4, 15.1, 7.2],
        to=[7.3, 16.8, 9.2],
        side="copper",
        up="worn",
        remove_rotation=True,
    )

    rewrite_element(
        elements,
        "optional_signal_antenna_prong",
        "top_lug_bridge",
        from_=BBMODEL_GEOMETRY_BOUNDS["top_lug_bridge"][0],
        to=BBMODEL_GEOMETRY_BOUNDS["top_lug_bridge"][1],
        side="steel",
        up="worn",
        remove_rotation=True,
    )
    rewrite_element(
        elements,
        "optional_signal_antenna_green_tip",
        "top_lug_right_mount",
        from_=[8.7, 15.1, 7.2],
        to=[9.6, 16.8, 9.2],
        side="copper",
        up="worn",
        remove_rotation=True,
    )

    seen_groups: set[str] = set()
    for group in groups:
        name = group.get("name")
        if name in GROUP_RENAMES:
            group["name"] = GROUP_RENAMES[name]
        seen_groups.add(str(group.get("name")))
        if group.get("name") == "expedition_lug":
            group["origin"] = [8, 16, 8.2]

    apply_mkiii_design(elements)
    heading = (((angle_frame_index + 1) // 2) * 22.5) % 360.0
    gear_phase = (gear_phase_index % GEAR_PHASE_COUNT) * 22.5
    apply_heading_geometry(elements, heading)
    apply_synchronized_gear_geometry(elements, gear_phase)
    synchronize_group_children(groups, elements)

    expected_groups = {"root", *GROUP_RENAMES.values()}
    if seen_groups != expected_groups:
        raise ValueError(f"Unexpected group set: {sorted(seen_groups)}")

    display["gui"] = {
        "rotation": [0, 180, 0],
        "translation": [0, -0.7, 0],
        "scale": [0.86, 0.86, 0.86],
    }
    display["firstperson_righthand"] = {
        "rotation": [18, -38, 0],
        "translation": [-4.5, 5, 0],
        "scale": [0.52, 0.52, 0.52],
    }
    display["firstperson_lefthand"] = {
        "rotation": [18, 38, 0],
        "translation": [-4.5, 5, 0],
        "scale": [0.52, 0.52, 0.52],
    }
    return model


def synchronize_bbmodel_geometry(model: dict[str, Any], runtime_model: dict[str, Any]) -> dict[str, Any]:
    elements = model.get("elements")
    runtime_elements = runtime_model.get("elements")
    if not isinstance(elements, list) or not isinstance(runtime_elements, list):
        raise ValueError("Blockbench source is missing elements")
    elements_by_name = {element.get("name"): element for element in elements}
    runtime_by_name = {element.get("name"): element for element in runtime_elements}
    for index, runtime in enumerate(runtime_elements):
        name = runtime["name"]
        if name not in elements_by_name:
            element_uuid = str(uuid.uuid5(uuid.NAMESPACE_URL, f"immersive-ore-expedition:blockbench:{name}"))
            authored = {
                "name": name,
                "box_uv": False,
                "render_order": "default",
                "rescale": False,
                "locked": False,
                "shade": True,
                "light_emission": 0,
                "export": True,
                "scope": 0,
                "allow_mirror_modeling": True,
                "from": runtime["from"].copy(),
                "to": runtime["to"].copy(),
                "autouv": 0,
                "color": index % 10,
                "origin": [0, 0, 0],
                "faces": {},
                "type": "cube",
                "uuid": element_uuid,
            }
            elements.append(authored)
            elements_by_name[name] = authored
    if set(elements_by_name) != set(runtime_by_name):
        raise ValueError("Blockbench/runtime element names differ during synchronization")

    textures = model.get("textures")
    if not isinstance(textures, list) or len(textures) != 1:
        raise ValueError("Blockbench source must contain exactly one embedded texture")
    textures[0]["source"] = (
        "data:image/png;base64," + base64.b64encode(TEXTURE_PATH.read_bytes()).decode("ascii")
    )

    for name, runtime in runtime_by_name.items():
        element = elements_by_name[name]
        element["from"] = runtime["from"].copy()
        element["to"] = runtime["to"].copy()
        element["shade"] = bool(runtime.get("shade", True))
        for face_name, runtime_face in runtime.get("faces", {}).items():
            authored_face = element.setdefault("faces", {}).setdefault(face_name, {})
            authored_face["uv"] = runtime_face["uv"].copy()
            authored_face["texture"] = 0

        rotation = runtime.get("rotation")
        if isinstance(rotation, dict):
            element["origin"] = rotation["origin"].copy()
            element["rescale"] = bool(rotation.get("rescale", False))
            angle = float(rotation.get("angle", 0.0))
            if abs(angle) > 1e-9:
                axis = rotation.get("axis")
                element["rotation"] = [
                    angle if axis == "x" else 0,
                    angle if axis == "y" else 0,
                    angle if axis == "z" else 0,
                ]
            else:
                element.pop("rotation", None)
        else:
            element["origin"] = [0, 0, 0]
            element["rescale"] = False
            element.pop("rotation", None)

    model["display"] = json.loads(json.dumps(runtime_model["display"]))
    runtime_groups = {group["name"]: group for group in runtime_model["groups"]}
    for group in model.get("groups", []):
        group_name = group.get("name")
        runtime_group = runtime_groups.get(group_name)
        if runtime_group is None:
            if group_name in ANIMATION_GROUPS:
                continue
            raise ValueError(f"Runtime model is missing Blockbench group {group_name}")
        group["origin"] = runtime_group["origin"].copy()

    groups_by_name = {group["name"]: group for group in model.get("groups", [])}
    element_uuids = [element["uuid"] for element in elements]
    root_node = model.get("outliner", [None])[0]
    if not isinstance(root_node, dict):
        raise ValueError("Blockbench source is missing its outliner root")
    child_nodes_by_uuid = {
        child.get("uuid"): child
        for child in root_node.get("children", [])
        if isinstance(child, dict)
    }
    gear_element_uuids = {
        elements_by_name[name]["uuid"]
        for name in GEAR_NAMES
    }
    for group_name, runtime_group in runtime_groups.items():
        if group_name == "root":
            continue
        group = groups_by_name[group_name]
        node = child_nodes_by_uuid.get(group["uuid"])
        if node is None:
            raise ValueError(f"Blockbench outliner is missing group {group_name}")
        runtime_children = [element_uuids[index] for index in runtime_group["children"]]
        if group_name == "dial_recess":
            animation_group_names_by_uuid = {
                groups_by_name[name]["uuid"]: name
                for name in ANIMATION_GROUPS
            }
            animation_nodes = {
                animation_group_names_by_uuid.get(child.get("uuid")): child
                for child in node.get("children", [])
                if isinstance(child, dict)
                and child.get("uuid") in animation_group_names_by_uuid
            }
            if set(animation_nodes) != set(ANIMATION_GROUPS):
                raise ValueError("Blockbench source is missing animated gear groups")
            node["children"] = [
                child_uuid
                for child_uuid in runtime_children
                if child_uuid not in gear_element_uuids
            ] + [animation_nodes[name] for name in ANIMATION_GROUPS]
        else:
            node["children"] = runtime_children
    return model


def dump_compact_base(model: dict[str, Any]) -> str:
    allowed = {"credit", "gui_light", "textures", "elements", "display", "groups", "overrides"}
    if set(model) != allowed:
        raise ValueError(f"Unexpected base-model keys: {sorted(model)}")

    lines = ["{"]
    lines.append(f'  "credit": {json.dumps(model["credit"], ensure_ascii=False)},')
    lines.append(f'  "gui_light": {json.dumps(model["gui_light"])},')
    lines.append('  "textures": {')
    texture_items = list(model["textures"].items())
    for index, (name, value) in enumerate(texture_items):
        comma = "," if index + 1 < len(texture_items) else ""
        lines.append(f'    {json.dumps(name)}: {json.dumps(value)}{comma}')
    lines.append("  },")

    lines.append('  "elements": [')
    for element_index, element in enumerate(model["elements"]):
        unknown = set(element) - {"name", "from", "to", "rotation", "shade", "faces"}
        if unknown:
            raise ValueError(f"Unexpected element keys for {element.get('name')}: {sorted(unknown)}")
        lines.append("    {")
        lines.append(f'      "name": {json.dumps(element["name"])},')
        lines.append(f'      "from": {json.dumps(element["from"])},')
        lines.append(f'      "to": {json.dumps(element["to"])},')
        if "rotation" in element:
            lines.append(f'      "rotation": {json.dumps(element["rotation"])},')
        if "shade" in element:
            lines.append(f'      "shade": {json.dumps(element["shade"])},')
        lines.append('      "faces": {')
        face_items = list(element["faces"].items())
        for face_index, (face, value) in enumerate(face_items):
            comma = "," if face_index + 1 < len(face_items) else ""
            lines.append(f'        {json.dumps(face)}: {json.dumps(value)}{comma}')
        lines.append("      }")
        comma = "," if element_index + 1 < len(model["elements"]) else ""
        lines.append(f"    }}{comma}")
    lines.append("  ],")

    lines.append('  "display": {')
    display_items = list(model["display"].items())
    for display_index, (slot, transform) in enumerate(display_items):
        lines.append(f'    {json.dumps(slot)}: {{')
        transform_items = list(transform.items())
        for transform_index, (name, value) in enumerate(transform_items):
            comma = "," if transform_index + 1 < len(transform_items) else ""
            lines.append(f'      {json.dumps(name)}: {json.dumps(value)}{comma}')
        comma = "," if display_index + 1 < len(display_items) else ""
        lines.append(f"    }}{comma}")
    lines.append("  },")

    lines.append('  "groups": [')
    for group_index, group in enumerate(model["groups"]):
        lines.append("    {")
        lines.append(f'      "name": {json.dumps(group["name"])},')
        lines.append(f'      "origin": {json.dumps(group["origin"])},')
        lines.append(f'      "children": {json.dumps(group["children"])}')
        comma = "," if group_index + 1 < len(model["groups"]) else ""
        lines.append(f"    }}{comma}")
    lines.append("  ],")

    lines.append('  "overrides": [')
    for index, override in enumerate(model["overrides"]):
        predicate = override["predicate"]
        if list(predicate) != [ANGLE_PREDICATE, GEAR_PHASE_PREDICATE]:
            raise ValueError(f"Unexpected override predicate: {predicate}")
        angle_value = predicate[ANGLE_PREDICATE]
        gear_value = predicate[GEAR_PHASE_PREDICATE]
        model_ref = override["model"]
        comma = "," if index + 1 < len(model["overrides"]) else ""
        lines.append(
            f'    {{"predicate": {{{json.dumps(ANGLE_PREDICATE)}: {angle_value:.6f}, '
            f'{json.dumps(GEAR_PHASE_PREDICATE)}: {gear_value:.6f}}}, '
            f'"model": {json.dumps(model_ref)}}}{comma}'
        )
    lines.append("  ]")
    lines.append("}")
    return "\n".join(lines) + "\n"


def angle_threshold(override_index: int) -> float:
    if override_index == 0:
        return 0.0
    return (override_index - 0.5) / ANGLE_FRAME_COUNT


def angle_frame_for_override(override_index: int) -> int:
    if override_index in {0, ANGLE_FRAME_COUNT}:
        return 0
    return override_index


def frame_name(angle_frame_index: int, gear_phase_index: int) -> str:
    base = f"expedition_compass_{angle_frame_index:02d}"
    if gear_phase_index == 0:
        return base
    return f"{base}_g{gear_phase_index:02d}"


def build_overrides() -> list[dict[str, Any]]:
    overrides: list[dict[str, Any]] = []
    for gear_phase_index in range(GEAR_PHASE_COUNT):
        gear_threshold = gear_phase_index / GEAR_PHASE_COUNT
        for angle_override_index in range(ANGLE_FRAME_COUNT + 1):
            angle_frame_index = angle_frame_for_override(angle_override_index)
            overrides.append(
                {
                    "predicate": {
                        ANGLE_PREDICATE: angle_threshold(angle_override_index),
                        GEAR_PHASE_PREDICATE: gear_threshold,
                    },
                    "model": (
                        "immersive_ore_expedition:item/"
                        + frame_name(angle_frame_index, gear_phase_index)
                    ),
                }
            )
    return overrides


def model_paths() -> list[Path]:
    base = MODEL_ROOT / "expedition_compass.json"
    frames = [
        MODEL_ROOT / f"{frame_name(angle_frame_index, gear_phase_index)}.json"
        for gear_phase_index in range(GEAR_PHASE_COUNT)
        for angle_frame_index in range(ANGLE_FRAME_COUNT)
    ]
    return [base, *frames]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="Write synchronized resources")
    args = parser.parse_args()

    changed: list[Path] = []
    transformed_models: dict[str, dict[str, Any]] = {}
    base_path = MODEL_ROOT / "expedition_compass.json"
    base_original = base_path.read_text(encoding="utf-8")
    template = json.loads(base_original)

    base_model = transform_model(
        json.loads(json.dumps(template)),
        angle_frame_index=0,
        gear_phase_index=0,
    )
    base_model["overrides"] = build_overrides()
    base_rendered = dump_compact_base(base_model)
    transformed_models[base_path.name] = base_model
    if base_rendered != base_original:
        changed.append(base_path)
        if args.write:
            base_path.write_text(base_rendered, encoding="utf-8", newline="\n")

    for gear_phase_index in range(GEAR_PHASE_COUNT):
        for angle_frame_index in range(ANGLE_FRAME_COUNT):
            path = MODEL_ROOT / f"{frame_name(angle_frame_index, gear_phase_index)}.json"
            model = transform_model(
                json.loads(json.dumps(template)),
                angle_frame_index,
                gear_phase_index,
            )
            model.pop("overrides", None)
            transformed_models[path.name] = model
            rendered = json.dumps(model, indent=2, ensure_ascii=False) + "\n"
            original = path.read_text(encoding="utf-8") if path.is_file() else ""
            if rendered == original:
                continue
            changed.append(path)
            if args.write:
                path.write_text(rendered, encoding="utf-8", newline="\n")

    bbmodel_original = BBMODEL_PATH.read_text(encoding="utf-8")
    bbmodel = synchronize_bbmodel_geometry(
        json.loads(bbmodel_original),
        transformed_models["expedition_compass_00.json"],
    )
    bbmodel_rendered = json.dumps(bbmodel, ensure_ascii=False, separators=(",", ":"))
    if bbmodel_rendered != bbmodel_original:
        changed.append(BBMODEL_PATH)
        if args.write:
            BBMODEL_PATH.write_text(bbmodel_rendered, encoding="utf-8", newline="\n")

    if changed and not args.write:
        print(f"FAIL: {len(changed)} compass asset file(s) need synchronization")
        for path in changed:
            print(path.relative_to(MODULE_ROOT))
        return 1

    verb = "Updated" if changed else "Verified"
    print(f"{verb} {len(model_paths())} compass model resources and Blockbench source")
    if changed:
        for path in changed:
            print(path.relative_to(MODULE_ROOT))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
