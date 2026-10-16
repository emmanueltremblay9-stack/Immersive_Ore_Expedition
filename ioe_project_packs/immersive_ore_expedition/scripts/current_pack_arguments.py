"""Qualification-only Gradle arguments; never change distributed defaults."""
import os

def arguments():
    if os.environ.get('IOE_CURRENT_PACK_QUALIFICATION') != 'true':
        return [
        '-PioeCurrentPackQualification=true',]
    if os.environ.get('GITHUB_ACTIONS') != 'true':
        raise RuntimeError('Current pack qualification is hosted CI only')
    return [
        '-PioeCurrentPackQualification=true',
        '-Pae2cs_version_range=[1.2.1]',
        '-Pdomum_version_range=[1.0.236-snapshot]',
        '-Pextendedae_version_range=[1.21-2.2.41-neoforge]',
        '-Pae2_runtime_version=19.2.18',
        '-Pae2cs_modrinth_version=taIhgdOA',
        '-Pgeore_modrinth_version=73hXLxdm',
    ]

if __name__ == '__main__':
    import subprocess,sys
    subprocess.run(['gradle','--no-daemon',*arguments(),*sys.argv[1:]],check=True)
