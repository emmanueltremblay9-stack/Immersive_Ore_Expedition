#!/usr/bin/env python3
"""Validate canonical GeOre rank bindings/geometry and recipes; optionally resolve assets from pinned runtime JARs.

This is a resource check, not a Minecraft renderer or client visual acceptance test.
"""
import argparse
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
MOD = 'immersive_ore_expedition'
RANKS = ('flawless', 'flawed', 'chipped', 'damaged')
FAMILIES = ('iron', 'coal', 'copper', 'diamond', 'emerald', 'gold', 'lapis', 'redstone',
            'aluminum', 'lead', 'nickel', 'silver', 'uranium')
IE_FAMILIES = {'aluminum', 'lead', 'nickel', 'silver', 'uranium'}
FACES = {'north', 'south', 'east', 'west', 'up', 'down'}


def validate_data(family, read):
    required = {'ae2', 'geore'} | ({'immersiveengineering'} if family in IE_FAMILIES else set())
    def data(path, extra=()):
        result = json.loads(read(f'data/{MOD}/{path}.json'))
        assert {c['modid'] for c in result['neoforge:conditions']} == required | set(extra), path
        return result
    storage = (f'immersiveengineering:storage_{family}' if family in IE_FAMILIES
               else f'minecraft:{family}_block')
    for rank in RANKS:
        name = f'{rank}_budding_{family}'
        loot = data(f'loot_table/blocks/{name}')
        entries = loot['pools'][0]['entries']
        if rank == 'flawless':
            assert entries[0]['name'] == f'{MOD}:flawed_budding_{family}', name
        else:
            children = entries[0]['children']
            assert children[0]['name'] == f'{MOD}:{name}', name
            assert children[0]['conditions'][0]['predicate']['predicates']['minecraft:enchantments'][0]['enchantments'] == 'minecraft:silk_touch'
            lower = {'flawed': 'chipped', 'chipped': 'damaged'}.get(rank)
            assert children[1]['name'] == (f'{MOD}:{lower}_budding_{family}' if lower else storage), name
        for tag in ('c/tags/block/budding_blocks', 'minecraft/tags/block/mineable/pickaxe'):
            values = json.loads(read(f'data/{tag}.json'))['values']
            assert {'id': f'{MOD}:{name}', 'required': False} in values, name
    for previous, rank in (('damaged', 'chipped'), ('chipped', 'flawed')):
        recipe = data(f'recipe/transform/{rank}_budding_{family}')
        assert recipe['type'] == 'ae2:transform'
        assert recipe['ingredients'] == [{'item': 'ae2:charged_certus_quartz_crystal'},
                                          {'item': f'{MOD}:{previous}_budding_{family}'}]
        assert recipe['result'] == {'count': 1, 'id': f'{MOD}:{rank}_budding_{family}'}
    recipe = data(f'recipe/aggregator/damaged_budding_{family}', ('ae2cs',))
    assert recipe['energy_cost'] == 16000
    assert recipe['input_a'] == {'count': 1, 'item': 'ae2cs:resonating_seed'}
    assert recipe['input_b'] == {'count': 1, 'tag': 'ae2cs:pure_crystal/resonating_crystal'}
    assert recipe['input_c'] == {'count': 1, 'tag': f'c:storage_blocks/{family}'}
    assert recipe['result'] == {'count': 1, 'id': f'{MOD}:damaged_budding_{family}'}


def validate(jars=()):
    archives = [ZipFile(path) for path in jars]
    resolved = set()

    def read(path):
        local = ROOT / path
        if local.is_file():
            return local.read_bytes()
        for archive in archives:
            if path in archive.namelist():
                return archive.read(path)
        raise AssertionError(f'Missing resource: {path}')

    def resource(identifier, kind, suffix):
        namespace, name = (identifier if ':' in identifier else 'minecraft:' + identifier).split(':', 1)
        return f'assets/{namespace}/{kind}/{name}.{suffix}'

    def resolve(identifier, chain=()):
        assert identifier not in chain, f'Model inheritance cycle: {chain + (identifier,)}'
        model = json.loads(read(resource(identifier, 'models', 'json')))
        textures = {}
        if 'parent' in model:
            textures.update(resolve(model['parent'], chain + (identifier,)))
        textures.update(model.get('textures', {}))
        if chain:
            return textures
        for texture in textures.values():
            seen = set()
            while texture.startswith('#'):
                assert texture not in seen, f'Texture alias cycle in {identifier}'
                seen.add(texture)
                texture = textures[texture[1:]]
            path = resource(texture, 'textures', 'png')
            assert read(path).startswith(b'\x89PNG\r\n\x1a\n'), path
            resolved.add(path)
        return textures

    try:
        for family in FAMILIES:
            signatures = set()
            coverage = []
            for rank in RANKS:
                name = f'{rank}_budding_{family}'
                model_id = f'{MOD}:block/{name}'
                state = json.loads(read(f'assets/{MOD}/blockstates/{name}.json'))
                assert state['variants']['']['model'] == model_id, name
                item_id = f'{MOD}:item/{name}'
                assert json.loads(read(resource(item_id, 'models', 'json')))['parent'] == model_id, name
                for language in ('en_us', 'fr_fr'):
                    translations = json.loads(read(f'assets/{MOD}/lang/{language}.json'))
                    assert translations[f'block.{MOD}.{name}'].strip(), name
                model = json.loads(read(resource(model_id, 'models', 'json')))
                assert model['textures']['all'] == f'geore:block/budding_{family}', name
                signatures.add(json.dumps(model, sort_keys=True))
                face_area = dict.fromkeys(FACES, 0)
                for element in model['elements']:
                    lo, hi = element['from'], element['to']
                    assert len(lo) == len(hi) == 3 and all(-16 <= a <= b <= 32 for a, b in zip(lo, hi)), name
                    assert set(element['faces']) <= FACES, name
                    for face, data in element['faces'].items():
                        assert data['texture'][1:] in model['textures'], name
                        assert data.get('cullface', face) == face, name
                        assert all(0 <= value <= 16 for value in data.get('uv', [0, 0, 16, 16])), name
                        if data['texture'] == '#fracture':
                            axis = {'north': 2, 'south': 2, 'west': 0, 'east': 0, 'up': 1, 'down': 1}[face]
                            expected = -0.01 if face in ('north', 'west', 'down') else 16.01
                            assert lo[axis] == hi[axis] == expected, f'Coplanar/hidden wear face: {name}/{face}'
                            dimensions = [hi[i] - lo[i] for i in range(3) if i != axis]
                            face_area[face] += dimensions[0] * dimensions[1]
                assert len(set(face_area.values())) == 1, f'Unequal rank readability across faces: {name}'
                coverage.append(face_area['north'])
                if archives:
                    resolve(item_id)
            assert len(signatures) == 4, 'Ranks must have distinct model data'
            assert coverage == sorted(set(coverage)) and coverage[0] == 0 and coverage[-1] < 64, coverage
            validate_data(family, read)
        print(f'Budding resources: {len(FAMILIES)} families, 52 distinct rank bindings; '
              f'{len(resolved)} texture resources resolved' if archives else
              'Budding resources: 13 families, 52 rank bindings; external JAR resolution not requested')
    finally:
        for archive in archives:
            archive.close()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', action='append', default=[], type=Path)
    validate(parser.parse_args().jar)
