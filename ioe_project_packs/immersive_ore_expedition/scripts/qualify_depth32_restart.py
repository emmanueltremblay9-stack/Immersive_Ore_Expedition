#!/usr/bin/env python3
"""Two sequential invocations of the SAME existing full-runtime profile; CI fixture only."""
import hashlib,json,os,secrets,subprocess,sys
from pathlib import Path

module=Path(__file__).resolve().parents[1]
root=module/'run/gametest-full'
evidence=module/'build/reports/ioe/depth32-restart'

def require(ok,message):
    if not ok:raise RuntimeError(message)
def digest(p):
    require(p.is_file() and not p.is_symlink(),'Missing/linked evidence: '+str(p))
    h=hashlib.sha256()
    with p.open('rb') as f:
        for b in iter(lambda:f.read(65536),b''):h.update(b)
    return h.hexdigest()
def run(phase):
    command=['gradle','--no-daemon','-Pdomum_version_range=[1.0.234-snapshot]',
             '-Pjade_version_range=[15.10.5,15.10.6)','-PioeIncludeIeRuntime=true','-PioeDepth32Prototype=true',
             '-PioeDepth32RestartPhase='+phase,'runFullRuntimeGameTestServer']
    with (evidence/(phase+'.log')).open('w') as log:
        process=subprocess.Popen(command,cwd=module,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
        for line in process.stdout:print(line,end='',flush=True);log.write(line)
        require(process.wait()==0,'GameTest process failed: '+phase)
def snapshot(world):
    files={};size=0
    for p in sorted(world.rglob('*')):
        require(not p.is_symlink(),'World symlink refused')
        if not p.is_file():continue
        relative=p.relative_to(world)
        require(not {'playerdata','players','stats','advancements'}.intersection(relative.parts),'Player data refused')
        if relative.as_posix()=='session.lock':continue
        size+=p.stat().st_size
        require(len(files)<4096 and size<=2147483648,'Fixture exceeds bounded snapshot')
        files[relative.as_posix()]=digest(p)
    return files

require(os.environ.get('GITHUB_ACTIONS')=='true','Hosted GitHub Actions only')
identity={key:os.environ[env] for key,env in [('run','GITHUB_RUN_ID'),('attempt','GITHUB_RUN_ATTEMPT'),('sha','GITHUB_SHA')]}
root.mkdir(parents=True,exist_ok=True);evidence.mkdir(parents=True,exist_ok=True)
require(not any(p.name in ('level.dat','level.dat_old') or p.suffix=='.mca' for p in root.rglob('*')),'Existing save refused before fresh launch')
# Observe a separate NEW CI fixture first in the same existing profile. Preserve it
# outside the profile after its process exits; never reopen or delete a player save.
stream_report=module/'build/reports/ioe/player-streaming.json'
archive=module/'build/tmp/ioe-player-streaming-world'
require(not stream_report.exists() and not archive.exists(),'Streaming evidence already exists')
command=['gradle','--no-daemon','-Pdomum_version_range=[1.0.234-snapshot]',
             '-Pjade_version_range=[15.10.5,15.10.6)','-PioeIncludeIeRuntime=true','-PioeDepth32Prototype=true',
         '-PioePlayerStreamingQualification=true','runFullRuntimeGameTestServer']
with (evidence/'streaming.log').open('w') as log:
    process=subprocess.Popen(command,cwd=module,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    for line in process.stdout:print(line,end='',flush=True);log.write(line)
    require(process.wait()==0,'Player streaming observation failed')
stream=json.loads(stream_report.read_text())
require(stream['source']==identity['sha'] and Path(stream['world'])==(root/'world').resolve(),'Unknown streaming fixture')
try:os.kill(stream['pid'],0)
except ProcessLookupError:pass
else:raise RuntimeError('Streaming producer still alive')
require((root/'world/level.dat').is_file(),'Streaming world not saved')
archive.parent.mkdir(parents=True,exist_ok=True)
(root/'world').rename(archive)
print('IOE streaming fixture process stopped and its own world archived outside the unchanged profile; activation_qualified='+str(stream['activation_qualified']),flush=True)
session=dict(schema=1,phase='fresh',root=str(root.resolve()),nonce=secrets.token_hex(32),**identity)
session_path=evidence/'session.json'
with session_path.open('x') as f:json.dump(session,f)
run('fresh')
origin_path=root/'.ioe-depth32-origin.json';origin=json.loads(origin_path.read_text())
proof_path=evidence/'expected.json';proof=json.loads(proof_path.read_text())
require(origin['nonce']==proof['nonce']==session['nonce'],'Producer provenance differs')
require(origin['producer_pid']==proof['producer_pid'],'Producer process identity differs')
try:os.kill(origin['producer_pid'],0)
except ProcessLookupError:pass
else:raise RuntimeError('Producer Java process still alive')
world=(root/'world').resolve()
require(Path(proof['world'])==world and (world/'level.dat').is_file(),'Unknown saved world path')
require(len(proof['blocks'])==3377 and len(proof['resources'])==20 and len(proof['markers'])==2,'Incomplete pre-restart evidence')
session.update(phase='sealed',producer_pid=origin['producer_pid'],producer_start=origin['producer_start'],
               origin_sha256=digest(origin_path),proof_sha256=digest(proof_path),world_files=snapshot(world))
require(any(p.endswith('.mca') for p in session['world_files']),'No persisted region files')
tmp=session_path.with_suffix('.tmp');tmp.write_text(json.dumps(session));tmp.replace(session_path)
print('IOE restart boundary: first invocation exited, producer Java PID absent; exact fixture sealed; next invocation uses same runFullRuntimeGameTestServer profile',flush=True)
run('resume')
verified=json.loads((evidence/'verified.json').read_text())
require(verified['producer_pid']==origin['producer_pid'] and verified['consumer_pid']!=verified['producer_pid'],'No distinct verifier process')
require(verified['blocks']==3377 and verified['resources']==20 and verified['markers']==2 and not verified['replay'],'Incomplete restart result')
require(json.loads(session_path.read_text())['phase']=='consumed','Resume permit not consumed')
print('IOE restart qualification complete: two terminated sequential Java invocations, same profile, persisted blocks and ledger verified',flush=True)
