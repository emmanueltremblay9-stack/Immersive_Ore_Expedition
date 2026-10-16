#!/usr/bin/env python3
"""Verify exact TesT play2 JARs and public JVM descriptors before hosted runtime qualification."""
import argparse,hashlib,json,re,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser()
parser.add_argument('--manifest',default='ci/runtime-mods.json')
args=parser.parse_args()
manifest=json.loads((root/args.manifest).read_text())
material='com/ldtteam/domumornamentum/client/model/data/MaterialTextureData'
checks={
 'domum_ornamentum':{
  material.replace('/','.'): [('<init>','(Ljava/util/Map;)V'),('getTexturedComponents','()Ljava/util/Map;'),('readFromItemStack',f'(Lnet/minecraft/world/item/ItemStack;)L{material};')],
  'com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity': [('updateTextureDataWith',f'(L{material};)V'),('getTextureData',f'()L{material};')],
 },
 'jade':{
  'snownee.jade.api.IWailaPlugin':[('register','(Lsnownee/jade/api/IWailaCommonRegistration;)V'),('registerClient','(Lsnownee/jade/api/IWailaClientRegistration;)V')],
  'snownee.jade.api.IWailaCommonRegistration':[('registerBlockDataProvider','(Lsnownee/jade/api/IServerDataProvider;Ljava/lang/Class;)V')],
  'snownee.jade.api.IWailaClientRegistration':[('registerBlockComponent','(Lsnownee/jade/api/IComponentProvider;Ljava/lang/Class;)V')],
  'snownee.jade.api.IServerDataProvider':[('appendServerData','(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/Accessor;)V')],
  'snownee.jade.api.IComponentProvider':[('appendTooltip','(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/Accessor;Lsnownee/jade/api/config/IPluginConfig;)V')],
  'snownee.jade.impl.WailaCommonRegistration':[('instance','()Lsnownee/jade/impl/WailaCommonRegistration;'),('getBlockNBTProviders','(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;')],
 }
}
report=[]
for entry in manifest['files']:
 mod=entry['mod_id']
 if mod not in checks:continue
 jar=root/'run/gametest-full/mods'/entry['filename']
 actual=hashlib.sha256(jar.read_bytes()).hexdigest()
 assert actual==entry['sha256'],f'Exact PC hash differs for {mod}'
 for cls,required in checks[mod].items():
  out=subprocess.check_output(['javap','-public','-s','-classpath',str(jar),cls],text=True)
  for name,descriptor in required:
   identifier=cls if name=='<init>' else name
   assert re.search(r'\b'+re.escape(identifier)+r'\([^\n]*\)[^\n]*;\s+descriptor: '+re.escape(descriptor)+r'(?:\s|$)',out),f'Missing public ABI: {cls}.{name}{descriptor}'
  report.append({'mod':mod,'sha256':actual,'class':cls,'required_descriptors':required})
path=root/'build/reports/ioe/pack-compatibility-api.json';path.parent.mkdir(parents=True,exist_ok=True)
path.write_text(json.dumps(report,indent=2)+'\n')
print(f'Exact PC hashes and public API/ABI descriptors passed for {len(report)} classes; runtime behavior still requires GameTests.')
