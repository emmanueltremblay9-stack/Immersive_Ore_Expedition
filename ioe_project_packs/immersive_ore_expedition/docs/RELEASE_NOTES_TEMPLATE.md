# IOE Release Notes Template

## Release Identity

- Release:
- Status: `NOT_PUBLISHED`
- Target Minecraft / NeoForge / Java:

## Release Qualification Provenance

These tracked release notes describe stable release content and are not the authoritative byte-level provenance record. After the candidate is frozen, a separate final external qualification/publication record must bind the exact source HEAD and tree; the exact CI workflow run, attempt, event, checkout SHA, and checkout tree; the exact artifact ID and digest; the runtime JAR filename, size, SHA-256, and inspection results; and the manual client, dedicated-server, and visual worldgen results and evidence produced from those exact artifact bytes.

Any repository write after final candidate selection creates a new candidate that requires qualification to be repeated and rebound. Publication requires the complete external binding and every applicable release gate; these tracked notes retain `NOT_PUBLISHED` as their stable prepublication status. Record actual publication status only in the final external qualification/publication record after successful destination readback.

## Summary

- User-visible changes:
- Technical changes:
- Compatibility or migration impact:
- Scope limitations:

## Included Pull Requests / Major Changes

- List the bounded, verified changes represented by this version.
- State explicitly when the history summary is non-exhaustive.

## Current Runtime and Config State

- Natural expedition-site generation gate: default `true` / `false`
- Natural worldgen state for this version:
- Legacy runtime-placement gate: default `true` / `false`
- Legacy runtime-placement diagnostics: default `true` / `false`
- Legacy runtime-proof-feature gate: default `true` / `false`
- Legacy runtime-proof diagnostics: default `true` / `false`
- Retrogen mutation:
- Production configured/placed feature and biome-modifier notes:

## Third-Party Notices and Blockers

- Notice changes:
- Source-license metadata:
- Published Maven metadata:
- Runtime metadata:
- Resolution constraints:
- Owner disposition and legal-review limitations:

## Known Limitations

- Distinguish source inspection, hosted CI, artifact inspection, and manual runtime evidence.
- Do not infer manual gameplay or visual worldgen proof from hosted validation.
- Preserve every unresolved dependency, licensing, runtime, or publication blocker.

## Breaking Changes and Rollback

- Breaking changes:
- Previous known-good release:
- Config rollback notes:
- World/save rollback notes:

## Publication Requirements

- Require the complete final external qualification/publication record for the exact frozen source and artifact bytes.
- Require all applicable manual smoke evidence from the exact runtime JAR bound by that record.
- Require every dependency, notice, compatibility, legal, and publication gate to retain its actual status.
- Do not create a tag, GitHub Release, or third-party distribution without separate publication authorization.
