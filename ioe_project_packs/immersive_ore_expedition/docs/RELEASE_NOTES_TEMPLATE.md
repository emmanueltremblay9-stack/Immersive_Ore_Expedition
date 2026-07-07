# IOE Release Notes Template

## Summary

- Release:
- Date:
- Target Minecraft / NeoForge / Java:
- Release jar:

## Included PRs / Features

- TBD

## Validation

- GitHub Actions `CI / Consolidated NeoForge module`:
- CI artifact verification:
- Additional checks:

## CI Artifact Verification

- Runtime jar found:
- Compiled `com/oblixorprime/ioe/` classes present:
- `META-INF/neoforge.mods.toml` present:
- Embedded jars absent:
- Duplicate entries absent:

## Client Smoke

- Status: not run / pass / fail
- Minecraft version:
- NeoForge version:
- Java version:
- Jar filename and SHA-256:
- Log path:
- Evidence notes:

## Server Smoke

- Status: not run / pass / fail
- Minecraft version:
- NeoForge version:
- Java version:
- Jar filename and SHA-256:
- Log path:
- Evidence notes:

## Known Limitations

- Most worldgen systems remain scaffold or planning layers until future live placement PRs explicitly enable them.
- Do not claim visible IOE worldgen placement unless a smoke pass proves it for that release.

## Config Changes

- Defaults changed: yes / no
- Notes:

## Compatibility Notes

- Required Minecraft:
- Required NeoForge:
- Required Java:
- Optional integrations:

## Breaking Changes

- TBD

## Rollback Notes

- Previous known-good release:
- Config rollback notes:
- World/save rollback notes:
