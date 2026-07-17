#!/usr/bin/env python3
"""Model the configured site rarity envelope over ten deterministic 10k-chunk grids.

This is a configuration-level model, not a substitute for biome, terrain, GameTest, Core Sample or
Excavator runtime evidence. The report labels that distinction explicitly.
"""

from __future__ import annotations

import hashlib
import json
import math
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PLACED = ROOT / "src/main/resources/data/immersive_ore_expedition/worldgen/placed_feature"
REPORTS = ROOT / "docs/biome_mineral_distribution"
SEEDS = (104729, 130363, 155921, 181081, 206369, 232003, 257591, 283009, 308411, 334021)
GRID_SIDE = 100


def rarity_chances() -> dict[str, int]:
    chances = {}
    for path in sorted(PLACED.glob("*.json")):
        payload = json.loads(path.read_text(encoding="utf-8"))
        for placement in payload.get("placement", []):
            if placement.get("type") == "minecraft:rarity_filter":
                chances[path.stem] = int(placement["chance"])
    return chances


def digest_int(*parts: object) -> int:
    payload = ":".join(str(part) for part in parts).encode("ascii")
    return int.from_bytes(hashlib.sha256(payload).digest()[:8], "big")


def percentile(sorted_values: list[float], fraction: float) -> float:
    if not sorted_values:
        return 0.0
    index = min(len(sorted_values) - 1, math.ceil(fraction * len(sorted_values)) - 1)
    return sorted_values[max(0, index)]


def nearest_distances(points: list[tuple[int, int]]) -> list[float]:
    distances = []
    for index, (x, z) in enumerate(points):
        nearest = min(
            (math.hypot(x - other_x, z - other_z) for other_index, (other_x, other_z) in enumerate(points) if other_index != index),
            default=0.0,
        )
        distances.append(nearest)
    return sorted(distances)


def simulate(seed: int, chances: dict[str, int]) -> dict[str, object]:
    points = []
    per_feature = {feature: 0 for feature in chances}
    for chunk_x in range(GRID_SIDE):
        for chunk_z in range(GRID_SIDE):
            for feature, chance in chances.items():
                if digest_int(seed, chunk_x, chunk_z, feature, "rarity") % chance:
                    continue
                local_x = digest_int(seed, chunk_x, chunk_z, feature, "x") % 16
                local_z = digest_int(seed, chunk_x, chunk_z, feature, "z") % 16
                points.append((chunk_x * 16 + local_x, chunk_z * 16 + local_z))
                per_feature[feature] += 1
    distances = nearest_distances(points)
    return {
        "seed": seed,
        "candidate_sites": len(points),
        "candidate_sites_per_10000_chunks": len(points),
        "nearest_distance_blocks": {
            "p50": round(percentile(distances, 0.50), 2),
            "p90": round(percentile(distances, 0.90), 2),
            "p99": round(percentile(distances, 0.99), 2),
        },
        "per_feature": per_feature,
    }


def main() -> None:
    chances = rarity_chances()
    if chances != {
        "buried_survey_marker": 96,
        "collapsed_shaft": 128,
        "miner_camp": 128,
        "tiny_vertical_mine_entrance": 96,
    }:
        raise SystemExit(f"Unexpected natural-site rarity configuration: {chances}")
    seed_results = [simulate(seed, chances) for seed in SEEDS]
    output = {
        "classification": "SUPPORTED_INFERENCE",
        "scope": "rarity-filter candidate envelope before biome, terrain, collision and transaction gates",
        "grid_chunks_per_seed": GRID_SIDE * GRID_SIDE,
        "seeds": seed_results,
        "runtime_validation": "NOT_PERFORMED",
    }
    REPORTS.mkdir(parents=True, exist_ok=True)
    (REPORTS / "multi_seed_candidate_model.json").write_text(
        json.dumps(output, indent=2) + "\n",
        encoding="utf-8",
    )
    lines = [
        "# Multi-seed candidate placement model",
        "",
        "Classification: `SUPPORTED_INFERENCE`.",
        "",
        "This models the four configured rarity filters over 10,000 chunks per seed. It is an upper-envelope",
        "for candidate positions only; biome selection, terrain rejection, collisions, site transactions, Core",
        "Sample visibility and Excavator extraction still require the GitHub-hosted full-runtime GameTests.",
        "",
        "| Seed | Candidates / 10k chunks | P50 | P90 | P99 |",
        "|---:|---:|---:|---:|---:|",
    ]
    for result in seed_results:
        distance = result["nearest_distance_blocks"]
        lines.append(
            f"| {result['seed']} | {result['candidate_sites']} | {distance['p50']} | {distance['p90']} | {distance['p99']} |"
        )
    lines.extend(["", "Runtime validation: `NOT_PERFORMED` until GitHub Actions runs.", ""])
    (REPORTS / "MULTI_SEED_CANDIDATE_MODEL.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"Modeled {len(SEEDS)} seeds x {GRID_SIDE * GRID_SIDE} chunks with {len(chances)} rarity filters.")


if __name__ == "__main__":
    main()
