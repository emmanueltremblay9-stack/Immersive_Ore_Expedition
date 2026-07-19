#!/usr/bin/env python3
"""Generate the audited biome -> IE mineral mix distribution and its reports."""

from __future__ import annotations

import csv
import json
from collections import OrderedDict
from decimal import Decimal
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/immersive_ore_expedition"
IE_DATA = ROOT / "src/main/resources/data/immersiveengineering"
BIOME_TAGS = DATA / "tags/worldgen/biome"
PROFILE_DATA = DATA / "immersive_ore_expedition/mine_resource_profile"
RECIPES = DATA / "recipe/mineral"
IE_AQUATIC_RECIPES = IE_DATA / "recipe/mineral"
REPORTS = ROOT / "docs/biome_mineral_distribution"
MODID = "immersive_ore_expedition"


def ids(namespace: str, values: str) -> list[str]:
    return [f"{namespace}:{value}" for value in values.split()]


REGISTERED = OrderedDict(
    minecraft=ids(
        "minecraft",
        """badlands bamboo_jungle basalt_deltas beach birch_forest cherry_grove cold_ocean
        crimson_forest dark_forest deep_cold_ocean deep_dark deep_frozen_ocean deep_lukewarm_ocean
        deep_ocean desert dripstone_caves end_barrens end_highlands end_midlands eroded_badlands
        flower_forest forest frozen_ocean frozen_peaks frozen_river grove ice_spikes jagged_peaks
        jungle lukewarm_ocean lush_caves mangrove_swamp meadow mushroom_fields nether_wastes ocean
        old_growth_birch_forest old_growth_pine_taiga old_growth_spruce_taiga plains river savanna
        savanna_plateau small_end_islands snowy_beach snowy_plains snowy_slopes snowy_taiga
        soul_sand_valley sparse_jungle stony_peaks stony_shore sunflower_plains swamp taiga the_end
        the_void warm_ocean warped_forest windswept_forest windswept_gravelly_hills windswept_hills
        windswept_savanna wooded_badlands""",
    ),
    biomesoplenty=ids(
        "biomesoplenty",
        """aspen_glade auroral_garden bayou bog cold_desert coniferous_forest crag crystalline_chasm
        dead_forest dryland dune_beach end_corruption end_reef end_wilds erupting_inferno field
        fir_clearing floodplain forested_field fungal_jungle glowing_grotto grassland gravel_beach
        highland hot_springs jacaranda_glade jade_cliffs lavender_field lush_desert lush_savanna
        maple_woods marsh mediterranean_forest moor muskeg mystic_grove old_growth_dead_forest
        old_growth_woodland ominous_woods orchard origin_valley overgrown_greens pasture prairie
        pumpkin_patch rainforest redwood_forest rocky_rainforest rocky_shrubland scrubland
        seasonal_forest shrubland snowblossom_grove snowy_coniferous_forest snowy_fir_clearing
        snowy_maple_woods spider_nest tropics tundra undergrowth visceral_heap volcanic_plains volcano
        wasteland wasteland_steppe wetland wintry_origin_valley withered_abyss woodland""",
    ),
    regions_unexplored=ids(
        "regions_unexplored",
        """alpha_grove ancient_delta arid_mountains ashen_woodland autumnal_maple_forest bamboo_forest
        baobab_savanna barley_fields bayou bioshroom_caves blackstone_basin blackwood_taiga boreal_taiga
        chalk_cliffs clover_plains cold_boreal_taiga cold_deciduous_forest cold_river deciduous_forest
        dry_bushland eucalyptus_forest fen flower_fields frozen_pine_taiga frozen_tundra fungal_fen
        glistering_meadow golden_boreal_taiga grassland grassy_beach gravel_beach highland_fields
        hyacinth_deeps icy_heights infernal_holt inferno joshua_desert magnolia_woodland maple_forest
        marsh mauve_hills mountains muddy_river mycotoxic_undergrowth old_growth_bayou
        old_growth_boreal_taiga old_growth_forest old_growth_golden_boreal_taiga orchard outback
        pine_slopes pine_taiga poppy_fields prairie prismachasm pumpkin_fields rainforest redstone_abyss
        redstone_caves redwoods rocky_meadow rocky_reef saguaro_desert scorching_caves shrubland
        silver_birch_forest sparse_rainforest sparse_redwoods spires steppe temperate_grove
        towering_cliffs tropical_river tropics tundra willow_forest windswept_maple_forest
        wisteria_grove""",
    ),
    biomeswevegone=ids(
        "biomeswevegone",
        """allium_shrubland amaranth_grassland araucaria_savanna aspen_boreal atacama_outback
        baobab_savanna basalt_barrera bayou black_forest canadian_shield cika_woods coconino_meadow
        coniferous_forest crag_gardens crimson_tundra cypress_swamplands cypress_wetlands dacite_ridges
        dacite_shore dead_sea ebony_woods enchanted_tangle eroded_borealis firecracker_chaparral
        forgotten_forest fragment_jungle frosted_coniferous_forest frosted_taiga howling_peaks
        ironwood_gour jacaranda_jungle lush_stacks maple_taiga mojave_desert orchard
        overgrowth_woodlands pale_bog prairie pumpkin_valley rainbow_beach red_rock_peaks
        red_rock_valley redwood_thicket rose_fields rugged_badlands sakura_grove shattered_glacier
        sierra_badlands skyris_vale temperate_grove tropical_rainforest weeping_witch_forest
        white_mangrove_marshes windswept_desert zelkova_forest""",
    ),
)

RU_REMOVED = set(
    ids(
        "regions_unexplored",
        """arid_mountains barley_fields cold_deciduous_forest deciduous_forest frozen_tundra
        golden_boreal_taiga mauve_hills mountains pumpkin_fields redstone_abyss rocky_meadow
        scorching_caves steppe temperate_grove""",
    )
)

RARE_ALLOWLISTS = OrderedDict(
    diamond=[
        "minecraft:frozen_peaks",
        "biomesoplenty:crag",
        "biomesoplenty:glowing_grotto",
        "regions_unexplored:icy_heights",
        "regions_unexplored:spires",
        "biomeswevegone:shattered_glacier",
    ],
    emerald=[
        "minecraft:grove",
        "biomesoplenty:jade_cliffs",
        "regions_unexplored:wisteria_grove",
        "biomeswevegone:crag_gardens",
    ],
    certus=[
        "minecraft:cherry_grove",
        "biomesoplenty:auroral_garden",
        "regions_unexplored:alpha_grove",
        "biomeswevegone:sakura_grove",
        "biomeswevegone:skyris_vale",
    ],
    entroized_fluix=[
        "minecraft:mushroom_fields",
        "biomesoplenty:mystic_grove",
        "regions_unexplored:bioshroom_caves",
        "regions_unexplored:prismachasm",
        "biomeswevegone:enchanted_tangle",
        "biomeswevegone:weeping_witch_forest",
    ],
)

AQUATIC = OrderedDict(
    alluvial_sift=[
        "minecraft:river",
        "minecraft:frozen_river",
        "biomesoplenty:floodplain",
        "regions_unexplored:ancient_delta",
        "regions_unexplored:cold_river",
        "regions_unexplored:muddy_river",
        "regions_unexplored:tropical_river",
    ],
    silt=[
        "minecraft:beach",
        "minecraft:snowy_beach",
        "minecraft:stony_shore",
        "biomesoplenty:dune_beach",
        "biomesoplenty:gravel_beach",
        "regions_unexplored:grassy_beach",
        "regions_unexplored:gravel_beach",
        "regions_unexplored:rocky_reef",
        "biomeswevegone:basalt_barrera",
        "biomeswevegone:dacite_shore",
        "biomeswevegone:rainbow_beach",
    ],
    ancient_seabed=[
        "minecraft:ocean",
        "minecraft:cold_ocean",
        "minecraft:deep_cold_ocean",
        "minecraft:deep_frozen_ocean",
        "minecraft:deep_lukewarm_ocean",
        "minecraft:deep_ocean",
        "minecraft:frozen_ocean",
        "minecraft:lukewarm_ocean",
        "minecraft:warm_ocean",
        "regions_unexplored:hyacinth_deeps",
        "biomeswevegone:dead_sea",
        "biomeswevegone:lush_stacks",
    ],
)

AQUATIC_MIX_IDS = {
    "alluvial_sift": "immersiveengineering:mineral/alluvial_sift",
    "silt": "immersiveengineering:mineral/silt",
    "ancient_seabed": "immersiveengineering:mineral/ancient_seabed",
}

COMMON_PROFILES = OrderedDict(
    redstone=ids("biomesoplenty", "volcanic_plains volcano") + ids("regions_unexplored", "redstone_caves"),
    gold=ids("minecraft", "badlands eroded_badlands wooded_badlands")
    + ids("regions_unexplored", "outback")
    + ids("biomeswevegone", "red_rock_peaks red_rock_valley rugged_badlands sierra_badlands"),
    uranium=ids(
        "biomesoplenty",
        "wasteland wasteland_steppe dead_forest old_growth_dead_forest fungal_jungle ominous_woods spider_nest",
    )
    + ids("regions_unexplored", "ashen_woodland fungal_fen mycotoxic_undergrowth"),
    silver=ids("minecraft", "snowy_plains snowy_slopes snowy_taiga ice_spikes")
    + ids(
        "biomesoplenty",
        "cold_desert muskeg snowblossom_grove snowy_coniferous_forest snowy_fir_clearing snowy_maple_woods tundra",
    )
    + ids("regions_unexplored", "cold_boreal_taiga frozen_pine_taiga tundra")
    + ids("biomeswevegone", "crimson_tundra eroded_borealis frosted_coniferous_forest frosted_taiga"),
    lapis=ids("minecraft", "swamp mangrove_swamp")
    + ids("biomesoplenty", "bayou bog marsh wetland moor")
    + ids("regions_unexplored", "bayou fen marsh old_growth_bayou")
    + ids("biomeswevegone", "bayou cypress_swamplands cypress_wetlands pale_bog white_mangrove_marshes"),
    copper=ids("minecraft", "jungle bamboo_jungle sparse_jungle")
    + ids("biomesoplenty", "rainforest rocky_rainforest tropics")
    + ids("regions_unexplored", "rainforest sparse_rainforest tropics bamboo_forest")
    + ids("biomeswevegone", "fragment_jungle jacaranda_jungle tropical_rainforest"),
    aluminum=ids("minecraft", "savanna savanna_plateau windswept_savanna")
    + ids("biomesoplenty", "lush_savanna scrubland")
    + ids("regions_unexplored", "baobab_savanna dry_bushland")
    + ids("biomeswevegone", "araucaria_savanna baobab_savanna"),
    coal=ids("minecraft", "desert")
    + ids("biomesoplenty", "dryland lush_desert")
    + ids("regions_unexplored", "joshua_desert saguaro_desert")
    + ids("biomeswevegone", "atacama_outback mojave_desert windswept_desert"),
    nickel=ids("minecraft", "taiga old_growth_pine_taiga old_growth_spruce_taiga")
    + ids("biomesoplenty", "coniferous_forest redwood_forest")
    + ids(
        "regions_unexplored",
        "blackwood_taiga boreal_taiga old_growth_boreal_taiga old_growth_golden_boreal_taiga pine_taiga redwoods sparse_redwoods",
    )
    + ids("biomeswevegone", "aspen_boreal coniferous_forest maple_taiga redwood_thicket"),
    lead=ids("minecraft", "jagged_peaks stony_peaks windswept_gravelly_hills")
    + ids("biomesoplenty", "rocky_shrubland")
    + ids("regions_unexplored", "chalk_cliffs towering_cliffs")
    + ids("biomeswevegone", "howling_peaks dacite_ridges"),
    iron=ids(
        "minecraft",
        "plains sunflower_plains forest flower_forest birch_forest old_growth_birch_forest "
        "windswept_hills windswept_forest meadow",
    )
    + ids("biomesoplenty", "highland grassland")
    + ids("regions_unexplored", "highland_fields grassland")
    + ids("biomeswevegone", "canadian_shield ironwood_gour prairie"),
)

PROFILES = OrderedDict()
PROFILES.update(RARE_ALLOWLISTS)
PROFILES.update(COMMON_PROFILES)


def out_tag(tag: str, chance: float) -> dict:
    return {"chance": chance, "output": {"tag": tag}}


def out_item(item: str, chance: float) -> dict:
    return {"chance": chance, "output": {"id": item}}


MIXES = OrderedDict(
    diamond=dict(weight=4, fail_chance=0.35, ores=[out_tag("c:gems/diamond", 0.12), out_item("minecraft:calcite", 0.44), out_item("minecraft:smooth_basalt", 0.44)]),
    emerald=dict(weight=5, fail_chance=0.30, ores=[out_tag("c:gems/emerald", 0.15), out_item("minecraft:prismarine", 0.45), out_item("minecraft:stone", 0.40)]),
    certus=dict(weight=4, fail_chance=0.35, ores=[out_item("ae2:certus_quartz_crystal", 0.20), out_item("minecraft:calcite", 0.40), out_item("minecraft:smooth_basalt", 0.40)], mods=["ae2"]),
    entroized_fluix=dict(weight=2, fail_chance=0.45, ores=[out_item("extendedae:entro_crystal", 0.08), out_item("minecraft:amethyst_shard", 0.46), out_item("minecraft:deepslate", 0.46)], mods=["extendedae"]),
    redstone=dict(weight=8, fail_chance=0.15, ores=[out_item("minecraft:redstone", 0.65), out_tag("c:dusts/sulfur", 0.20), out_item("minecraft:obsidian", 0.15)]),
    gold=dict(weight=12, fail_chance=0.10, ores=[out_tag("c:ores/gold", 0.60), out_tag("c:ores/copper", 0.40)]),
    uranium=dict(weight=8, fail_chance=0.15, ores=[out_tag("c:ores/uranium", 0.60), out_tag("c:ores/lead", 0.30), out_tag("c:dusts/sulfur", 0.10)]),
    silver=dict(weight=9, fail_chance=0.15, ores=[out_tag("c:ores/silver", 0.70), out_tag("c:ores/lead", 0.20), out_item("minecraft:calcite", 0.10)]),
    lapis=dict(weight=10, fail_chance=0.15, ores=[out_tag("c:ores/lapis", 0.75), out_tag("c:ores/gold", 0.15), out_tag("c:dusts/sulfur", 0.10)]),
    copper=dict(weight=20, fail_chance=0.10, ores=[out_tag("c:ores/copper", 0.70), out_tag("c:ores/iron", 0.20), out_tag("c:dusts/sulfur", 0.10)]),
    aluminum=dict(weight=20, fail_chance=0.05, ores=[out_tag("c:ores/aluminum", 0.60), out_tag("c:ores/iron", 0.30), out_tag("c:ores/nickel", 0.10)]),
    coal=dict(weight=25, fail_chance=0.05, ores=[out_tag("c:ores/coal", 0.80), out_tag("c:dusts/sulfur", 0.20)]),
    nickel=dict(weight=10, fail_chance=0.15, ores=[out_tag("c:ores/nickel", 0.65), out_tag("c:ores/iron", 0.25), out_tag("c:dusts/sulfur", 0.10)]),
    lead=dict(weight=15, fail_chance=0.10, ores=[out_tag("c:ores/lead", 0.65), out_tag("c:ores/silver", 0.25), out_tag("c:dusts/sulfur", 0.10)]),
    iron=dict(weight=30, fail_chance=0.05, ores=[out_tag("c:ores/iron", 0.80), out_item("minecraft:calcite", 0.20)]),
    alluvial_sift=dict(weight=15, fail_chance=0.20, ores=[out_tag("c:gems/diamond", 0.20), out_item("minecraft:clay", 0.40), out_item("minecraft:sand", 0.40)]),
    silt=dict(weight=25, fail_chance=0.20, ores=[out_item("minecraft:clay", 0.60), out_item("minecraft:sand", 0.40)]),
    ancient_seabed=dict(weight=15, fail_chance=0.05, ores=[out_item("minecraft:calcite", 0.65), out_item("minecraft:dripstone_block", 0.30), out_item("minecraft:bone_block", 0.05)]),
)

# Semantic geology roles for the final deposit -> principal/secondary/trace matrix.
# The role is intentionally independent from numeric chance: a rare target resource can be
# the deposit's principal resource while lower-value host rock occupies most of the output.
MIX_ROLE_INDEXES = {
    "diamond": {"principal": [0], "secondary": [], "trace": [], "host_rock": [1, 2]},
    "emerald": {"principal": [0], "secondary": [], "trace": [], "host_rock": [1, 2]},
    "certus": {"principal": [0], "secondary": [], "trace": [], "host_rock": [1, 2]},
    "entroized_fluix": {"principal": [0], "secondary": [1], "trace": [], "host_rock": [2]},
    "redstone": {"principal": [0], "secondary": [1], "trace": [], "host_rock": [2]},
    "gold": {"principal": [0], "secondary": [1], "trace": [], "host_rock": []},
    "uranium": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "silver": {"principal": [0], "secondary": [1], "trace": [], "host_rock": [2]},
    "lapis": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "copper": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "aluminum": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "coal": {"principal": [0], "secondary": [1], "trace": [], "host_rock": []},
    "nickel": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "lead": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
    "iron": {"principal": [0], "secondary": [], "trace": [], "host_rock": [1]},
    "alluvial_sift": {"principal": [1], "secondary": [0], "trace": [], "host_rock": [2]},
    "silt": {"principal": [0], "secondary": [1], "trace": [], "host_rock": []},
    "ancient_seabed": {"principal": [0], "secondary": [1], "trace": [2], "host_rock": []},
}

DEFAULT_SPOILS = [
    out_item("minecraft:gravel", 0.50),
    out_item("minecraft:cobblestone", 0.30),
    out_item("minecraft:cobbled_deepslate", 0.20),
]
AQUATIC_SPOILS = {
    "alluvial_sift": [
        out_item("minecraft:gravel", 0.60),
        out_item("minecraft:cobblestone", 0.30),
        out_item("minecraft:coarse_dirt", 0.10),
    ],
    "silt": [
        out_item("minecraft:gravel", 0.60),
        out_item("minecraft:cobblestone", 0.30),
        out_item("minecraft:coarse_dirt", 0.10),
    ],
    "ancient_seabed": [
        out_item("minecraft:sandstone", 0.60),
        out_item("minecraft:gravel", 0.30),
        out_item("minecraft:sand", 0.10),
    ],
}
COMMON_TIERS = {
    "mother": {"radius_blocks": 48, "capacity": 16384},
    "major": {"radius_blocks": 32, "capacity": 8192},
    "minor": {"radius_blocks": 20, "capacity": 4096},
    "direct": {"radius_blocks": 12, "capacity": 2048},
}
DENSE_FAMILY_TIERS = {
    "mother": {"radius_blocks": 44, "capacity": 12288},
    "major": {"radius_blocks": 30, "capacity": 6144},
    "minor": {"radius_blocks": 18, "capacity": 3072},
    "direct": {"radius_blocks": 10, "capacity": 1536},
}
RARE_TIERS = {
    "mother": {"radius_blocks": 36, "capacity": 4096},
    "major": {"radius_blocks": 24, "capacity": 2048},
    "minor": {"radius_blocks": 16, "capacity": 1024},
    "direct": {"radius_blocks": 10, "capacity": 512},
}
ENTRO_TIERS = {
    "mother": {"radius_blocks": 28, "capacity": 2048},
    "major": {"radius_blocks": 20, "capacity": 1024},
    "minor": {"radius_blocks": 14, "capacity": 512},
    "direct": {"radius_blocks": 8, "capacity": 256},
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def tag_values(values: list[str]) -> list[object]:
    return [value if value.startswith("minecraft:") else {"id": value, "required": False} for value in values]


def active_biomes() -> list[str]:
    return sorted(value for values in REGISTERED.values() for value in values if value not in RU_REMOVED)


def mineral_mix_id(name: str) -> str:
    return AQUATIC_MIX_IDS.get(name, f"{MODID}:mineral/{name}")


def profile_tiers(name: str) -> dict[str, dict[str, int]]:
    if name == "entroized_fluix":
        return ENTRO_TIERS
    if name in RARE_ALLOWLISTS:
        return RARE_TIERS
    if name == "iron":
        return COMMON_TIERS
    if len(PROFILES[name]) > 12:
        return DENSE_FAMILY_TIERS
    return COMMON_TIERS


def validate_model() -> None:
    active = set(active_biomes())
    if (
        len(REGISTERED["regions_unexplored"]),
        len(RU_REMOVED),
        len(active & set(REGISTERED["regions_unexplored"])),
    ) != (78, 14, 64):
        raise ValueError("RU inventory must remain 78 registered / 14 removed / 64 active")
    if len(REGISTERED["biomeswevegone"]) != 55:
        raise ValueError("BWG inventory must remain 55 active biomes")
    owners: dict[str, str] = {}
    for group, values in list(AQUATIC.items()) + list(PROFILES.items()):
        if len(values) != len(set(values)):
            raise ValueError(f"Duplicate biome inside {group}")
        for biome in values:
            if biome not in active:
                raise ValueError(f"{group} references missing or removed biome {biome}")
            if biome in owners:
                raise ValueError(f"Biome {biome} belongs to both {owners[biome]} and {group}")
            owners[biome] = group
    for profile, exact in RARE_ALLOWLISTS.items():
        if PROFILES[profile] != exact:
            raise ValueError(f"Rare allowlist drift for {profile}")
    for name, mix in MIXES.items():
        total = sum(Decimal(str(ore["chance"])) for ore in mix["ores"])
        if total != Decimal("1.0"):
            raise ValueError(f"Mineral mix {name} chance sum is {total}, expected exactly 1.0")
        roles = MIX_ROLE_INDEXES.get(name)
        if roles is None or set(roles) != {"principal", "secondary", "trace", "host_rock"}:
            raise ValueError(f"Mineral mix {name} is missing its complete semantic output-role map")
        if not roles["principal"]:
            raise ValueError(f"Mineral mix {name} has no principal output")
        assigned_indexes = [index for indexes in roles.values() for index in indexes]
        if sorted(assigned_indexes) != list(range(len(mix["ores"]))):
            raise ValueError(f"Mineral mix {name} output roles do not partition every output exactly once")


def generate_tags() -> None:
    for profile, values in PROFILES.items():
        write_json(BIOME_TAGS / "ore_profile" / f"{profile}.json", {"replace": True, "values": tag_values(values)})
    write_json(
        BIOME_TAGS / "ore_profile/specialized.json",
        {"replace": True, "values": [f"#{MODID}:ore_profile/{profile}" for profile in PROFILES]},
    )
    write_json(
        BIOME_TAGS / "expedition_site_biomes.json",
        {"replace": True, "values": [f"#{MODID}:ore_profile/{profile}" for profile in PROFILES]},
    )
    for name, values in AQUATIC.items():
        write_json(BIOME_TAGS / "aquatic" / f"{name}.json", {"replace": True, "values": tag_values(values)})
    write_json(BIOME_TAGS / "ip_oil_coal.json", {"replace": True, "values": tag_values(PROFILES["coal"])})
    write_json(BIOME_TAGS / "ip_lava_volcano.json", {"replace": True, "values": tag_values(PROFILES["redstone"])})
    write_json(
        BIOME_TAGS / "ip_aquifer_water.json",
        {"replace": True, "values": tag_values([value for values in AQUATIC.values() for value in values])},
    )


def generate_profiles() -> None:
    for profile in PROFILES:
        tiers = profile_tiers(profile)
        write_json(
            PROFILE_DATA / f"{profile}.json",
            {
                "biome_tag": f"{MODID}:ore_profile/{profile}",
                "mineral_mix": f"{MODID}:mineral/{profile}",
                "resource_name": profile,
                "survey_radius_chunks": 8,
                "deposit_tiers": tiers,
            },
        )


def generate_recipes() -> None:
    for name, mix in MIXES.items():
        tag = f"{MODID}:aquatic/{name}" if name in AQUATIC else f"{MODID}:ore_profile/{name}"
        conditions = [{"type": "neoforge:mod_loaded", "modid": "immersiveengineering"}]
        conditions.extend({"type": "neoforge:mod_loaded", "modid": mod} for mod in mix.get("mods", []))
        recipe_root = IE_AQUATIC_RECIPES if name in AQUATIC else RECIPES
        write_json(
            recipe_root / f"{name}.json",
            {
                "neoforge:conditions": conditions,
                "type": "immersiveengineering:mineral_mix",
                "biome_predicates": [["minecraft:is_overworld"], [tag]],
                "fail_chance": mix["fail_chance"],
                "ores": mix["ores"],
                "spoils": AQUATIC_SPOILS.get(name, DEFAULT_SPOILS),
                "weight": mix["weight"],
            },
        )


def outcome_rows() -> list[dict[str, object]]:
    owner = {biome: ("aquatic", name) for name, values in AQUATIC.items() for biome in values}
    owner.update({biome: ("specialized", name) for name, values in PROFILES.items() for biome in values})
    oil = set(PROFILES["coal"])
    lava = set(PROFILES["redstone"])
    aquifer = {biome for values in AQUATIC.values() for biome in values}
    rows = []
    for biome in active_biomes():
        outcome, profile = owner.get(biome, ("generic_ie", ""))
        petroleum = "aquifer" if biome in aquifer else "lava" if biome in lava else "oil" if biome in oil else ""
        rows.append(
            {
                "biome": biome,
                "namespace": biome.split(":", 1)[0],
                "outcome": outcome,
                "profile": profile,
                "mineral_mix": mineral_mix_id(profile) if profile else "immersiveengineering:generic_by_recipe",
                "priority": "aquatic" if outcome == "aquatic" else "rare_allowlist" if profile in RARE_ALLOWLISTS else "curated_profile" if profile else "generic",
                "petroleum": petroleum,
            }
        )
    return rows


def write_csv(path: Path, fieldnames: list[str], rows: list[dict[str, object]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def generate_reports() -> None:
    registered_rows = []
    for namespace, values in REGISTERED.items():
        for biome in sorted(values):
            registered_rows.append({"namespace": namespace, "biome": biome, "status": "removed" if biome in RU_REMOVED else "active"})
    write_csv(REPORTS / "registered_biomes.csv", ["namespace", "biome", "status"], registered_rows)
    write_csv(
        REPORTS / "active_biomes.csv",
        ["namespace", "biome"],
        [{"namespace": biome.split(":", 1)[0], "biome": biome} for biome in active_biomes()],
    )
    write_csv(
        REPORTS / "ru_removed_biomes.csv",
        ["biome", "status"],
        [{"biome": biome, "status": "removed"} for biome in sorted(RU_REMOVED)],
    )
    rows = outcome_rows()
    write_csv(REPORTS / "biome_profile_matrix.csv", list(rows[0]), rows)
    aquatic_rows = [row for row in rows if row["outcome"] == "aquatic"]
    write_csv(REPORTS / "aquatic_deposit_matrix.csv", list(aquatic_rows[0]), aquatic_rows)
    mix_rows = []
    for name, mix in MIXES.items():
        tiers = profile_tiers(name) if name in PROFILES else {}
        role_outputs = {
            role: [mix["ores"][index] for index in indexes]
            for role, indexes in MIX_ROLE_INDEXES[name].items()
        }
        mix_rows.append(
            {
                "mineral_mix": mineral_mix_id(name),
                "profile_biome_count": len(PROFILES.get(name, AQUATIC.get(name, []))),
                "weight": mix["weight"],
                "fail_chance": mix["fail_chance"],
                "ore_chance_sum": str(sum(Decimal(str(ore["chance"])) for ore in mix["ores"])),
                "principal_outputs": json.dumps(role_outputs["principal"], separators=(",", ":"), ensure_ascii=False),
                "secondary_outputs": json.dumps(role_outputs["secondary"], separators=(",", ":"), ensure_ascii=False),
                "trace_outputs": json.dumps(role_outputs["trace"], separators=(",", ":"), ensure_ascii=False),
                "host_rock_outputs": json.dumps(role_outputs["host_rock"], separators=(",", ":"), ensure_ascii=False),
                "composition": json.dumps(mix["ores"], separators=(",", ":"), ensure_ascii=False),
                "mother_radius_blocks": tiers.get("mother", {}).get("radius_blocks", ""),
                "mother_capacity": tiers.get("mother", {}).get("capacity", ""),
                "major_radius_blocks": tiers.get("major", {}).get("radius_blocks", ""),
                "major_capacity": tiers.get("major", {}).get("capacity", ""),
                "minor_radius_blocks": tiers.get("minor", {}).get("radius_blocks", ""),
                "minor_capacity": tiers.get("minor", {}).get("capacity", ""),
                "direct_radius_blocks": tiers.get("direct", {}).get("radius_blocks", ""),
                "direct_capacity": tiers.get("direct", {}).get("capacity", ""),
            }
        )
    write_csv(REPORTS / "mineral_mix_matrix.csv", list(mix_rows[0]), mix_rows)
    manifest = {
        "minecraft_version": "1.21.1",
        "loader": "NeoForge 21.1.230",
        "source_versions": {
            "biomesoplenty": "21.1.0.13",
            "regions_unexplored": "0.6.2",
            "biomeswevegone": "2.6.0",
            "immersiveengineering": "12.4.2-194",
            "immersivepetroleum": "4.5.0-39",
            "ae2": "19.2.17",
            "extendedae": "1.21-2.2.33-neoforge",
        },
        "counts": {
            "registered": sum(len(values) for values in REGISTERED.values()),
            "active": len(active_biomes()),
            "ru_registered": len(REGISTERED["regions_unexplored"]),
            "ru_removed": len(RU_REMOVED),
            "ru_active": len(set(REGISTERED["regions_unexplored"]) - RU_REMOVED),
            "bwg_active": len(REGISTERED["biomeswevegone"]),
            "specialized": sum(len(values) for values in PROFILES.values()),
            "aquatic": sum(len(values) for values in AQUATIC.values()),
            "generic_ie": sum(1 for row in rows if row["outcome"] == "generic_ie"),
        },
        "rare_allowlists": RARE_ALLOWLISTS,
        "ru_removed": sorted(RU_REMOVED),
        "deposit_tiers": {profile: profile_tiers(profile) for profile in PROFILES},
        "mineral_mix_output_roles": {
            name: {
                role: [MIXES[name]["ores"][index] for index in indexes]
                for role, indexes in roles.items()
            }
            for name, roles in MIX_ROLE_INDEXES.items()
        },
    }
    write_json(REPORTS / "biome_distribution_manifest.json", manifest)
    tier_table_lines = [
        "| Profile | Mother radius/capacity | Major radius/capacity | Minor radius/capacity | Direct radius/capacity |",
        "| --- | ---: | ---: | ---: | ---: |",
    ]
    for profile in PROFILES:
        tiers = profile_tiers(profile)
        tier_table_lines.append(
            f"| {profile} "
            f"| {tiers['mother']['radius_blocks']} / {tiers['mother']['capacity']} "
            f"| {tiers['major']['radius_blocks']} / {tiers['major']['capacity']} "
            f"| {tiers['minor']['radius_blocks']} / {tiers['minor']['capacity']} "
            f"| {tiers['direct']['radius_blocks']} / {tiers['direct']['capacity']} |"
        )
    tier_table = "\n".join(tier_table_lines)
    report = f"""# Biome -> mineral deposit distribution

Generated deterministically by `scripts/generate_biome_mineral_distribution.py`.

## Generated deliverables

- Complete registered-biome inventory: `registered_biomes.csv`.
- Active-biome inventory: `active_biomes.csv`.
- Separate RU removed inventory: `ru_removed_biomes.csv`.
- Active biome -> exclusive outcome/profile matrix: `biome_profile_matrix.csv`.
- Aquatic biome -> preserved IE deposit matrix: `aquatic_deposit_matrix.csv`.
- Deposit composition, semantic principal/secondary/trace/host-rock roles, exact chances, weight, fail chance, tier capacities and active profile counts: `mineral_mix_matrix.csv`.
- Machine-readable counts, rare allowlists, role maps and tier capacities: `biome_distribution_manifest.json`.
- Candidate multi-seed rarity model: `multi_seed_candidate_model.json` and `MULTI_SEED_CANDIDATE_MODEL.md`.
- Confirmed runtime multi-seed evidence after hosted GameTests: `build/reports/ioe/multi-seed-biome-frequency.json`.

## Inventory

- Registered biomes: {manifest['counts']['registered']}
- Active biomes: {manifest['counts']['active']}
- Regions Unexplored: 78 registered, 14 removed, 64 active
- Oh The Biomes We've Gone: 55 active
- Specialized IOE profiles: {manifest['counts']['specialized']} biomes
- Aquatic-only IE deposits: {manifest['counts']['aquatic']} biomes
- Generic IE outcome: {manifest['counts']['generic_ie']} biomes

## Runtime model

- Each specialized biome owns exactly one `immersiveengineering:mineral_mix` recipe.
- `mineral_mix_matrix.csv` explicitly classifies every output as principal, secondary, trace or host rock. These are geological semantic roles, not a sort by chance; rare target resources can remain principal while host rock occupies most output rolls.
- Mother, Major, Minor and Direct share the same composition; only radius and capacity change.
- Aquatic biomes allow exactly one preserved native IE aquatic mix under an exact overridden biome predicate.
- Alluvial Sift preserves its official 20% diamond output and reports it as a secondary resource, never as the biome's principal IOE profile.
- Unassigned active biomes retain generic Immersive Engineering recipe selection.
- Productive expedition chambers contain one bounded biome-profile formation: a GeOre node, a Certus budding chamber, or an Entroized Fluix geode. The finite IE mineral deposit remains the bulk extraction path.
- Immersive Petroleum oil, lava and aquifer tags are disjoint and evaluated independently.

## Reserve capacities

Radius is measured in blocks and capacity is the configured initial Excavator reserve.

{tier_table}

## Curated priority exceptions

- `regions_unexplored:towering_cliffs` is Lead despite its conifer tags.
- `biomeswevegone:howling_peaks` is Lead despite its snowy/conifer tags.
- `biomeswevegone:basalt_barrera` is Silt because aquatic priority outranks basaltic Redstone.
- Redstone therefore has three compatible specialized biomes; it is intentionally not padded with an incompatible fourth.
- High-confidence snowy, swamp, jungle, taiga and redwood tags are retained even when a family exceeds 12 biome ids.
- Dense Silver, Lapis, Copper and Nickel families use lower weight, higher fail chance and/or reduced tier reserves instead of arbitrary biome exclusion.

## Evidence status

- Static generation and exclusivity: generated; validate with `scripts/validate_worldgen_assets.py`.
- Local Gradle/build/Minecraft/Prism execution: NOT_PERFORMED by project policy.
- Candidate rarity envelope: `SUPPORTED_INFERENCE` in `MULTI_SEED_CANDIDATE_MODEL.md`.
- Real multi-seed biome frequency: implemented by the full-runtime GameTest using a fresh normal Overworld `BiomeSource` whose TerraBlender state is independently initialized for each seed, a matching seed-specific `RandomState`, 6,400 chunk columns per seed and three altitudes; output is `build/reports/ioe/multi-seed-biome-frequency.json`.
- Core Sample, Excavator, fallback and real multi-seed runtime: NOT_PERFORMED until GitHub-hosted CI runs.
"""
    (REPORTS / "BIOME_MINERAL_DISTRIBUTION_REPORT.md").write_text(report, encoding="utf-8")


def main() -> None:
    validate_model()
    generate_tags()
    generate_profiles()
    generate_recipes()
    generate_reports()
    print(
        f"Generated {len(PROFILES)} profiles, {len(MIXES)} mineral mixes and "
        f"{len(active_biomes())} active-biome matrix rows."
    )


if __name__ == "__main__":
    main()
