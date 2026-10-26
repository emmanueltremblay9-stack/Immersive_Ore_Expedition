#!/usr/bin/env python3
"""Verify resolved core artifacts, including the owner's exact AE2CS hash."""
import hashlib, json, zipfile, tomllib
from pathlib import Path
cache = Path.home()/'.gradle/caches/modules-2/files-2.1'
expected = {
    'ae2wtlib': ('maven.modrinth/pNabrMMw/IQFaf9vp', '19.2.6', None),
    'ae2': ('org.appliedenergistics/appliedenergistics2/19.2.18', '19.2.18', None),
    'ae2cs': ('maven.modrinth/uJ9afomy/taIhgdOA', '1.2.1', 'fb0d921c68d43e3ff76c9da16b6e3b8ba4944849064fd42aeb27e0f9ed099dde'),
    'geore': ('maven.modrinth/Xw6zG9hl/73hXLxdm', '6.2.4', None),
}
report=[]
for mod, (coordinate, version, expected_hash) in expected.items():
    matches={}
    for jar in (cache/coordinate).rglob('*.jar'):
        with zipfile.ZipFile(jar) as z:
            if 'META-INF/neoforge.mods.toml' not in z.namelist(): continue
            metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
            declared=[m for m in metadata.get('mods',[]) if m['modId']==mod]
            if not declared: continue
            actual_version=declared[0]['version']
            if actual_version=='${file.jarVersion}':
                manifest=z.read('META-INF/MANIFEST.MF').decode()
                actual_version=next(line.split(': ',1)[1] for line in manifest.splitlines() if line.startswith('Implementation-Version: '))
            assert actual_version==version,(mod,actual_version,version)
        digest=hashlib.sha256(jar.read_bytes()).hexdigest()
        if expected_hash: assert digest==expected_hash,(mod,digest)
        matches[digest]=jar
    assert len(matches)==1,(mod,matches)
    digest,jar=next(iter(matches.items()))
    report.append(dict(mod=mod,version=version,sha256=digest,coordinate=coordinate))
p=Path('build/reports/ioe/current-core-runtime.json');p.parent.mkdir(parents=True,exist_ok=True)
p.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
