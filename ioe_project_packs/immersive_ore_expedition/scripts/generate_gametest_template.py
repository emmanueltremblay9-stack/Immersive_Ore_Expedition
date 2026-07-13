#!/usr/bin/env python3
"""Generate the deterministic empty structure used by the IOE GameTests."""

from __future__ import annotations

import gzip
import io
import struct
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = (
    ROOT
    / "src/main/resources/data/immersive_ore_expedition/structure/expedition_worldgen_empty.nbt"
)


def name(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def named_int(tag_name: str, value: int) -> bytes:
    return b"\x03" + name(tag_name) + struct.pack(">i", value)


def named_string(tag_name: str, value: str) -> bytes:
    encoded = value.encode("utf-8")
    return b"\x08" + name(tag_name) + struct.pack(">H", len(encoded)) + encoded


def named_int_list(tag_name: str, values: tuple[int, ...]) -> bytes:
    return (
        b"\x09"
        + name(tag_name)
        + b"\x03"
        + struct.pack(">i", len(values))
        + b"".join(struct.pack(">i", value) for value in values)
    )


def named_empty_compound_list(tag_name: str) -> bytes:
    return b"\x09" + name(tag_name) + b"\x0a" + struct.pack(">i", 0)


def build_structure_nbt() -> bytes:
    palette_entry = named_string("Name", "minecraft:air") + b"\x00"
    palette = b"\x09" + name("palette") + b"\x0a" + struct.pack(">i", 1) + palette_entry
    root_payload = b"".join(
        (
            named_int("DataVersion", 3955),
            named_int_list("size", (32, 48, 32)),
            named_empty_compound_list("blocks"),
            palette,
            named_empty_compound_list("entities"),
            b"\x00",
        )
    )
    return b"\x0a\x00\x00" + root_payload


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    buffer = io.BytesIO()
    with gzip.GzipFile(fileobj=buffer, mode="wb", mtime=0) as archive:
        archive.write(build_structure_nbt())
    OUTPUT.write_bytes(buffer.getvalue())
    print(f"Generated {OUTPUT.relative_to(ROOT)} ({OUTPUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
