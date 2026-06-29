# CI Validation Policy

Local Gradle tests, local builds, and local Minecraft or Prism runtime smoke tests are disabled by default for this project workflow.

GitHub Actions is the source of truth for automated validation. The CI workflow runs the consolidated NeoForge module checks on GitHub-hosted runners with Java 21, including `clean`, `test`, `processResources`, `jar`, `build`, and final jar inspection.

Runtime Prism or Minecraft validation on a personal PC requires explicit manual approval before launching the client, server, PrismLauncher, or a world.
