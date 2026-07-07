# V23 Worldgen Smoke Result

## Run Identity

- PR/task:
- Smoke date/time:
- Operator:
- Branch/commit being tested:
- Result file created from template:

## Artifact Under Test

- Minecraft version:
- NeoForge version:
- Java version:
- IOE jar filename:
- IOE jar SHA-256:
- Jar source:

## Profile Setup

- World or server name:
- World seed:
- Test biome id: `minecraft:plains`
- V22 datapack path/name:
- Datapack enabled confirmation:
- Config file path:
- Fresh `latest.log` path:

## Static Preflight

| Check | Expected | Observed |
| --- | --- | --- |
| Active shipped smoke tag default | `values` is `[]` | |
| V22 datapack smoke tag | exactly `minecraft:plains` | |
| Active default runtime gates | all `false` | |
| Smoke config runtime gates | v19/v18 proof gates enabled | |
| Province runtime integration | `false` for this profile | |
| Broad biome tags | not used | |
| Active `src/main/resources` changed for this run | no | |

## Exact Smoke Config Values

```toml
worldgen.runtimeProofFeatureEnabled =
worldgen.runtimeProofFeatureDiagnostics =
worldgen.runtimePlacementEnabled =
worldgen.runtimePlacementDiagnostics =
worldgen.provinces.runtimeIntegrationEnabled =
worldgen.provinces.resourcePolicyRules =
```

## Execution Matrix

| Pass | Status | Evidence |
| --- | --- | --- |
| Client world-entry | not run / pass / fail | |
| Dedicated server startup | not run / pass / fail | |
| Controlled placement observation | not run / skipped / attempted / observed | |

## Log And Coordinate Evidence

- Relevant log excerpt:
- Chunk/coordinate checked:
- Biome at checked coordinate:
- Screenshot or operator note:

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
