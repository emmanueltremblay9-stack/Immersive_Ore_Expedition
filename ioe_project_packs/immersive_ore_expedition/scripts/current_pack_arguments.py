"""Current CI runtime coordinates; admission uses distributed defaults."""
import os

def arguments():
    if os.environ.get('IOE_CURRENT_PACK_QUALIFICATION') != 'true':
        return [
        '-PioeCurrentPackQualification=true',]
    if os.environ.get('GITHUB_ACTIONS') != 'true':
        raise RuntimeError('Current pack qualification is hosted CI only')
    return [
        '-PioeCurrentPackQualification=true',
        '-Pae2_runtime_version=19.2.18',
        '-Pae2cs_modrinth_version=taIhgdOA',
        '-Pgeore_modrinth_version=73hXLxdm',
    ]

if __name__ == '__main__':
    import subprocess,sys
    subprocess.run(['gradle','--no-daemon',*arguments(),*sys.argv[1:]],check=True)
