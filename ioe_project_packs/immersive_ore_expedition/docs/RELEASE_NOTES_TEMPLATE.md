# IOE Release Notes Template

## Candidate Identity

- Release:
- Status: `NOT_PUBLISHED` / `PUBLISHED`
- Date:
- Source HEAD:
- Source tree:
- Target Minecraft / NeoForge / Java:
- Runtime JAR:

## Summary

- User-visible changes:
- Technical changes:
- Compatibility or migration impact:
- Scope limitations:

## Included Pull Requests / Major Changes

- List the bounded, verified changes represented by this candidate.
- State explicitly when the history summary is non-exhaustive.

## Current Runtime and Config State

- Natural expedition-site generation gate: default `true` / `false`
- Natural worldgen state for this candidate:
- Legacy runtime-placement gate: default `true` / `false`
- Legacy runtime-placement diagnostics: default `true` / `false`
- Legacy runtime-proof-feature gate: default `true` / `false`
- Legacy runtime-proof diagnostics: default `true` / `false`
- Retrogen mutation:
- Production configured/placed feature and biome-modifier notes:

## Hosted Validation

- Required CI status: `NOT_RUN` / `PASS` / `FAIL`
- Workflow and job:
- Run ID / number / attempt:
- Event:
- Exact CI head SHA:
- JUnit:
- Static validators:
- Baseline hosted GameTests:
- Complete pinned-runtime hosted GameTests:
- Build and JAR inspection:
- Additional checks:

## CI Artifact Verification

- Artifact ID:
- Artifact digest:
- Runtime JAR filename:
- Runtime JAR size:
- Runtime JAR SHA-256:
- Compiled class count:
- Compiled `com/oblixorprime/ioe/` classes present: `NOT_RUN` / `PASS` / `FAIL`
- `META-INF/neoforge.mods.toml` present: `NOT_RUN` / `PASS` / `FAIL`
- Embedded JAR count:
- Duplicate entries:

## Manual Client Smoke

- Status: `NOT_PERFORMED` / `PASS` / `FAIL`
- Minecraft / NeoForge / Java:
- JAR filename and SHA-256:
- Fresh `latest.log` path:
- Evidence notes:

## Manual Dedicated-Server Smoke

- Status: `NOT_PERFORMED` / `PASS` / `FAIL`
- Minecraft / NeoForge / Java:
- JAR filename and SHA-256:
- Fresh `latest.log` path:
- Evidence notes:

## Manual Visual Worldgen Smoke

- Status: `NOT_PERFORMED` / `PASS` / `FAIL`
- Seed / dimension / new-chunk procedure:
- Observation notes:
- Evidence path:

## Third-Party Notices and Blockers

- Notice changes:
- Source-license metadata:
- Published Maven metadata:
- Runtime metadata:
- Resolution status: `RESOLVED` / `AMBIGUOUS / UNRESOLVED`
- Owner or legal review: `NOT_PERFORMED` / completed with recorded disposition

## Known Limitations

- Distinguish source inspection, hosted CI, artifact inspection, and manual runtime evidence.
- Do not infer manual gameplay or visual worldgen proof from hosted validation.
- Preserve every unresolved dependency, licensing, runtime, or publication blocker.

## Breaking Changes and Rollback

- Breaking changes:
- Previous known-good release:
- Config rollback notes:
- World/save rollback notes:

## Publication

- Git tag: `NOT_CREATED` / value
- GitHub Release: `NOT_PUBLISHED` / URL
- Third-party distribution: `NOT_PUBLISHED` / URL
- Publication blockers:
