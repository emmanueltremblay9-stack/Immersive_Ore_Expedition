#!/usr/bin/env python3
"""Download the pinned external worldgen runtime into a GitHub Actions run directory."""

from __future__ import annotations

import argparse
import hashlib
import json
import tomllib
import urllib.request
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = ROOT / "ci/runtime-mods.json"
EXPECTED_BIOME_COUNTS = {
    "biomesoplenty": 69,
    "regions_unexplored": 78,
    "biomeswevegone": 55,
}


def sha512(path: Path) -> str:
    digest = hashlib.sha512()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def metadata_mod_ids(path: Path) -> set[str]:
    with zipfile.ZipFile(path) as jar:
        metadata = jar.read("META-INF/neoforge.mods.toml").decode("utf-8")
    parsed = tomllib.loads(metadata)
    return {mod["modId"] for mod in parsed.get("mods", []) if isinstance(mod, dict) and "modId" in mod}


def registered_biomes(path: Path, namespace: str) -> set[str]:
    prefix = f"data/{namespace}/worldgen/biome/"
    with zipfile.ZipFile(path) as jar:
        return {
            f"{namespace}:{entry.removeprefix(prefix).removesuffix('.json')}"
            for entry in jar.namelist()
            if entry.startswith(prefix)
            and entry.endswith(".json")
            and "/" not in entry.removeprefix(prefix)
        }


def ru_removed_biomes(path: Path) -> set[str]:
    with zipfile.ZipFile(path) as jar:
        payload = json.loads(
            jar.read("data/regions_unexplored/tags/worldgen/biome/removed.json").decode("utf-8")
        )
    return {entry for entry in payload.get("values", []) if isinstance(entry, str)}


def stage(manifest_path: Path, destination: Path) -> list[dict[str, object]]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    distribution_manifest = json.loads(
        (ROOT / "docs/biome_mineral_distribution/biome_distribution_manifest.json").read_text(
            encoding="utf-8"
        )
    )
    expected_ru_removed = set(distribution_manifest["ru_removed"])
    destination.mkdir(parents=True, exist_ok=True)
    evidence = []
    for entry in manifest["files"]:
        target = destination / entry["filename"]
        if not target.is_file() or sha512(target) != entry["sha512"]:
            request = urllib.request.Request(entry["url"], headers={"User-Agent": "IOE-GitHub-Actions/1.0"})
            with urllib.request.urlopen(request, timeout=120) as response, target.open("wb") as output:
                while block := response.read(1024 * 1024):
                    output.write(block)
        actual_hash = sha512(target)
        if actual_hash != entry["sha512"]:
            raise SystemExit(f"SHA-512 mismatch for {target.name}: {actual_hash}")
        actual_mod_ids = metadata_mod_ids(target)
        if entry["mod_id"] not in actual_mod_ids:
            raise SystemExit(
                f"Metadata mismatch for {target.name}: expected {entry['mod_id']}, found {sorted(actual_mod_ids)}"
            )
        item_evidence: dict[str, object] = {
            "filename": target.name,
            "mod_id": entry["mod_id"],
            "size": target.stat().st_size,
            "sha512": actual_hash,
        }
        expected_biome_count = EXPECTED_BIOME_COUNTS.get(entry["mod_id"])
        if expected_biome_count is not None:
            actual_biomes = registered_biomes(target, entry["mod_id"])
            if len(actual_biomes) != expected_biome_count:
                raise SystemExit(
                    f"Biome inventory mismatch for {target.name}: expected {expected_biome_count}, "
                    f"found {len(actual_biomes)}"
                )
            item_evidence["registered_biome_count"] = len(actual_biomes)
        if entry["mod_id"] == "regions_unexplored":
            actual_removed = ru_removed_biomes(target)
            if actual_removed != expected_ru_removed:
                raise SystemExit(
                    "Regions Unexplored removed-biome tag differs from the generated compatibility report: "
                    f"actual={sorted(actual_removed)}, expected={sorted(expected_ru_removed)}"
                )
            item_evidence["removed_biome_count"] = len(actual_removed)
        evidence.append(item_evidence)
    report = destination.parent / "runtime-mods-evidence.json"
    report.write_text(json.dumps(evidence, indent=2) + "\n", encoding="utf-8")
    return evidence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--destination", type=Path, default=ROOT / "run/gametest-full/mods")
    args = parser.parse_args()
    evidence = stage(args.manifest.resolve(), args.destination.resolve())
    print(f"Staged and verified {len(evidence)} pinned runtime mods in {args.destination}.")


if __name__ == "__main__":
    main()
