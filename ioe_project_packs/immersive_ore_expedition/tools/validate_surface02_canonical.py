#!/usr/bin/env python3
"""Source-linked gate for the passive canonical Surface 02 implementation."""

from __future__ import annotations

import json
from pathlib import Path
import sys


MODULE_ROOT = Path(__file__).resolve().parents[1]
REPOSITORY_ROOT = MODULE_ROOT.parents[1]
MAIN_PACKAGE = MODULE_ROOT / "src/main/java/com/oblixorprime/ioe/surface02"
TEST_FILE = MODULE_ROOT / "src/test/java/com/oblixorprime/ioe/surface02/Surface02ComposerTest.java"
DOC_FILE = MODULE_ROOT / "docs/surface02/README.md"
CI_FILE = REPOSITORY_ROOT / ".github/workflows/ci.yml"
RUNTIME_GAMETEST_FILE = (
    MODULE_ROOT
    / "src/main/java/com/oblixorprime/ioe/worldgen/Surface02BuddingGameTests.java"
)

EXPECTED_MAIN_FILES = {
    "Surface02BiomeFamily.java",
    "Surface02Composer.java",
    "Surface02Plan.java",
    "Surface02ResourceResolver.java",
    "Surface02TerrainContext.java",
}


def require_text(path: Path, errors: list[str]) -> str:
    if not path.is_file():
        errors.append(f"missing required file: {path.relative_to(REPOSITORY_ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def require_tokens(label: str, text: str, tokens: list[str], errors: list[str]) -> None:
    for token in tokens:
        if token not in text:
            errors.append(f"{label}: missing source-linked token {token!r}")


def reject_tokens(label: str, text: str, tokens: list[str], errors: list[str]) -> None:
    for token in tokens:
        if token in text:
            errors.append(f"{label}: forbidden source-linked token {token!r}")


def main() -> int:
    errors: list[str] = []
    actual_main_files = {path.name for path in MAIN_PACKAGE.glob("*.java")} if MAIN_PACKAGE.is_dir() else set()
    if actual_main_files != EXPECTED_MAIN_FILES:
        errors.append(
            "Surface 02 production file set mismatch: "
            f"expected={sorted(EXPECTED_MAIN_FILES)} actual={sorted(actual_main_files)}"
        )

    sources = {
        path.name: require_text(path, errors)
        for path in sorted(MAIN_PACKAGE.glob("*.java"))
    }
    composer = sources.get("Surface02Composer.java", "")
    plan = sources.get("Surface02Plan.java", "")
    resource = sources.get("Surface02ResourceResolver.java", "")
    biome = sources.get("Surface02BiomeFamily.java", "")
    terrain = sources.get("Surface02TerrainContext.java", "")
    test = require_text(TEST_FILE, errors)
    documentation = require_text(DOC_FILE, errors)
    ci = require_text(CI_FILE, errors)

    require_tokens(
        "composer",
        composer,
        [
            "SUPPORTED",
            "INELIGIBLE_QUALITY",
            "UNSUPPORTED_RESOURCE_FAMILY",
            "INVALID_TERRAIN",
            "case DRY -> 0",
            "case POOR -> 3",
            "case NORMAL -> 4",
            "quality == SiteQuality.POOR ? 4",
            "quality == SiteQuality.NORMAL ? 5",
            "Surface02Plan.SlotId.SLOT_4",
            "Surface02ResourceResolver.resolve(requestedFamily, rank)",
            "Surface02Plan.Layer.BIOME_FACADE",
            "Surface02Plan.Layer.ACCESS",
            "Surface02Plan.Layer.FOUR_SLOT_CORE",
            "Surface02Plan.Layer.NARRATIVE_DECORATION",
            "AGING_SALT",
            "FACADE_SALT",
            "NARRATIVE_SALT",
        ],
        errors,
    )
    require_tokens(
        "plan",
        plan,
        [
            "PLANNING_ONLY = true",
            "DEFAULT_OFF = true",
            "WIDTH = 15",
            "LENGTH = 15",
            "MIN_DEPTH = 8",
            "MAX_DEPTH = 15",
            "SLOT_1",
            "SLOT_2",
            "SLOT_3",
            "SLOT_4",
            "toExpeditionSiteBlockPlan",
            "BASIC_MINESHAFT_CONNECTOR",
            "ORE_LOAD_CHAMBER",
            "single-chunk envelope",
            "expectedActiveCount",
            "expectedGrowthEnvelope",
            "plannedBuddingPositions",
            "Surface 02 active-slot identity is canonical and fixed",
            "canonicalNodePos",
            "canonicalApproachPos",
            "canonicalMineralPositions",
            "reachableWalkableCells",
            "Every Surface 02 quality requires its canonical entrance",
            "Inactive Surface 02 slots require the canonical depleted face",
            "The Surface 02 core must own every mineral block",
            "observed and canonical biome families must match",
            "obstruction cells must be declared exactly",
            "validateResolvedState",
            "state.isAir()",
        ],
        errors,
    )
    require_tokens(
        "resource resolver",
        resource,
        [
            "BuddingRankResolver.resolve(requestedFamily, requestedRank)",
            'rank.path() + "_budding_quartz"',
            "resolution.storageBlockId()",
            "RANK_UNSUPPORTED",
            "resource.orElseThrow().family() != requestedFamily",
            "resource.orElseThrow().rank() != requestedRank",
            "ExtendedAE cannot be a supported Surface 02 resource",
        ],
        errors,
    )
    require_tokens(
        "biome mapping",
        biome,
        [
            "CONIFER(Canonical.TEMPERATE)",
            "TROPICAL(Canonical.WET)",
            "VOLCANIC(Canonical.RELIEF)",
            "ROCKY(Canonical.RELIEF)",
            "MOUNTAIN(Canonical.RELIEF)",
            "AQUATIC(null)",
            "SHORELINE(null)",
        ],
        errors,
    )
    require_tokens(
        "terrain",
        terrain,
        [
            "admitsCanonicalEnvelope",
            "singleAnchorChunk",
            "stableSurface",
            "clearEntrance",
            "!aquatic",
            "!shoreline",
        ],
        errors,
    )

    production_text = "\n".join(sources.values())
    reject_tokens(
        "Surface 02 production package",
        production_text,
        [
            "ExpeditionSiteBlueprints",
            "ProspectorCampOutcropComposer",
            "Surface01",
            "RandomSource",
            "java.util.Random",
            "directLower(",
            "IoeSiteQualityFallbackResolver",
            "LootTable",
            "ChestBlock",
            "BarrelBlock",
            "ConfiguredFeature",
            "PlacedFeature",
            "BiomeModifier",
            "rarity_filter",
        ],
        errors,
    )
    if "LOOT_CONTAINER" in plan:
        errors.append("plan: loot-container cell kind must not exist")

    require_tokens(
        "Java tests",
        test,
        [
            "assertEquals(120, cases)",
            "assertEquals(26, cases)",
            "extendedAeUsesTheRealFoundationRefusalWithoutFallback",
            "dryExtendedAeIsSterileWithoutClaimingRankSupport",
            "richAndMotherlodeAreTypedIneligibleWithoutFallback",
            "aquaticShorelineAndNonFittingEnvelopesAreTypedInvalidTerrain",
            "slotsAndCoreAreInvariantAcrossBiomeAndDecorativeSeed",
            "reachablePaths",
            "publicPlanAndResourceBoundariesRejectContradictoryCanonicalData",
            "passiveAdapterValidationRejectsMismatchedUnsafeStateSemantics",
            "assertThrows",
        ],
        errors,
    )
    require_tokens(
        "documentation",
        documentation,
        [
            "PLANNING_ONLY",
            "DEFAULT_OFF",
            "23 x 27",
            "15 x 15",
            "single-anchor-chunk",
            "DRY",
            "POOR",
            "NORMAL",
            "ExtendedAE",
            "Surface 01",
            "No loot",
            "AGING_SALT",
            "NOT_PERFORMED",
        ],
        errors,
    )
    require_tokens(
        "CI",
        ci,
        ["python3 -B tools/validate_surface02_canonical.py"],
        errors,
    )

    active_references: list[str] = []
    main_java_root = MODULE_ROOT / "src/main/java"
    for path in main_java_root.rglob("*.java"):
        if MAIN_PACKAGE in path.parents:
            continue
        text = path.read_text(encoding="utf-8")
        if "Surface02" in text or "surface02" in text:
            if path == RUNTIME_GAMETEST_FILE:
                require_tokens(
                    "runtime GameTest isolation",
                    text,
                    [
                        "@GameTestHolder(ImmersiveOreExpeditionMod.MODID)",
                        "Surface02Composer.resolveAndCompose(",
                        "IoeExpeditionPlanPlacement.apply(",
                        "assertPlanningOnlyRegistries(",
                    ],
                    errors,
                )
                continue
            active_references.append(str(path.relative_to(MODULE_ROOT)).replace("\\", "/"))
    if active_references:
        errors.append(f"active production code references passive Surface 02: {active_references}")

    payload = {
        "validator": "surface02-canonical-source-linked",
        "status": "PASS" if not errors else "FAIL",
        "planning_only": True,
        "default_off": True,
        "production_files": sorted(actual_main_files),
        "active_production_references": active_references,
        "errors": errors,
    }
    print(json.dumps(payload, indent=2, sort_keys=True))
    return 0 if not errors else 1


if __name__ == "__main__":
    sys.exit(main())
