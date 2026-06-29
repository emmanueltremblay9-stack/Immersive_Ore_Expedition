# Project Working Rules

- Do not run local Gradle validation, local builds, PrismLauncher, Minecraft, client smoke tests, server smoke tests, or world smoke tests on a personal PC by default. Configure and use GitHub-hosted GitHub Actions / CI for automated validation.
- Do not copy jars into a local Prism `mods` folder or use a self-hosted runner on this PC unless Emmanuel explicitly approves that specific local runtime action.
- For NeoForge module builds, verify the produced runtime jar contains compiled mod classes and `META-INF/neoforge.mods.toml` before installing or calling it launch-ready. Metadata-only jars are invalid even if Gradle reports `build` success.
- If a module uses ModDevGradle and composite builds, keep the `jar` task configured to include `sourceSets.main.output.classesDirs` so downstream modules and Prism installs receive class-bearing jars.
