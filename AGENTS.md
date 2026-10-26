# Project Working Rules

- Do not run local Gradle validation, local builds, PrismLauncher, Minecraft, client smoke tests, server smoke tests, or world smoke tests on a personal PC by default. Configure and use GitHub-hosted GitHub Actions / CI for automated validation.
- Do not copy jars into a local Prism `mods` folder or use a self-hosted runner on this PC unless Emmanuel explicitly approves that specific local runtime action.
- For NeoForge module builds, verify the produced runtime jar contains compiled mod classes and `META-INF/neoforge.mods.toml` before installing or calling it launch-ready. Metadata-only jars are invalid even if Gradle reports `build` success.
- If a module uses ModDevGradle and composite builds, keep the `jar` task configured to include `sourceSets.main.output.classesDirs` so downstream modules and Prism installs receive class-bearing jars.

## Verified Git state across interfaces

- Owner-approved workflow (2026-10-10): use the current technical state checkpoint in
  `ioe_project_packs/immersive_ore_expedition/docs/PATH_TO_3_0.md`. Governance remains
  owned by `Immersive_Ore_Expedition_GPT`; do not create competing state frameworks.
- Before mutation, read that checkpoint and the applicable contracts, then verify
  the live remote branch/PR head, local branch/HEAD, tracked and untracked work,
  and the identity/hash of any artifact being used. Preserve local work and reconcile
  drift before proceeding; a saved interface transcript is not current Git evidence.
- At session close, update the existing checkpoint when material state changes,
  persist authorized work and relevant evidence, and verify the actual remote receipt
  by commit/ref and exact file readback. Report unsaved or unverified results honestly.
- Keep proposed decisions, implemented behavior, exact-source test results and owner
  acceptance separate. A successful CI run is not visual/client or release acceptance.
- Record the tested source commit separately from the state-document commit. Resolve
  the latter from Git history and the remote receipt; never claim a document contains
  its own final commit hash. Documentation-only successors do not relabel an earlier
  runtime JAR or transfer its CI qualification to a different source commit.
