# V22 Worldgen Smoke Evidence

## Run Identity

- PR/task: v22 controlled worldgen smoke profile
- Smoke date/time:
- Operator:
- Minecraft version:
- NeoForge version:
- Java version:
- IOE jar filename:
- IOE jar SHA-256:
- Branch/commit being tested:

## World And Profile

- World seed:
- World name:
- Test biome id: `minecraft:plains`
- Datapack path/name:
- Datapack enabled confirmation:
- Config file path:

## Exact Config Values

```toml
worldgen.runtimeProofFeatureEnabled =
worldgen.runtimeProofFeatureDiagnostics =
worldgen.runtimePlacementEnabled =
worldgen.runtimePlacementDiagnostics =
worldgen.provinces.runtimeIntegrationEnabled =
worldgen.provinces.resourcePolicyRules =
```

## Log Evidence

- Fresh `latest.log` path:
- Relevant log excerpt:
- Chunk/coordinate checked:

## Observed Result

Select one:

- not run
- startup failed
- world load failed
- datapack rejected
- config rejected
- feature skipped because gate disabled
- feature skipped because resource policy denied/missing/excluded
- feature skipped because target outside writable region
- feature skipped because target not replaceable
- placement attempted
- placement observed

## Result

- Pass/fail status:
- Crash/fatal/missing dependency/classloading/config parse errors:
- Notes:

## Conclusion

Select one and include the evidence basis:

- No live proof claimed.
- Live proof claimed only if placement evidence exists.
