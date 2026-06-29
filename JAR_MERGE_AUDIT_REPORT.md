# IOE Jar Merge Audit Report

Date: 2026-06-29

Project root: `<project-root>`

## Decision

Final recommendation: do not blindly merge the jars.

The safe classification is:

- A) Safe to merge into one jar: no.
- B) Safe only by source-level consolidation: possible later, but not safe as an automated packaging-only change in this dirty checkout.
- C) Safe only for non-Minecraft library shading: no eligible non-Minecraft helper library jars were found.
- D) Not safe to merge; keep as runtime/mod/build dependencies: yes for the current jar set.

Reason: the project currently contains six independent IOE NeoForge mods, each with its own hard-coded `@Mod` id and its own `META-INF/neoforge.mods.toml`. A jar-level merge would collide on `META-INF/neoforge.mods.toml`, `META-INF/MANIFEST.MF`, and `pack.mcmeta`; it would also risk dropping five of six mod identities depending on merge order. The remaining jars under the project are Gradle wrapper jars and ModDevGradle/NeoForge/Minecraft artifacts, which must not be embedded into the IOE distributable.

## Root, Build, And Loader

- Real repository root: `<project-root>`
- CodeGraph: no `.codegraph` directory exists; CodeGraph was skipped after status check.
- Build system: six independent Gradle wrapper builds under `ioe_project_packs\01_ioe_core` through `ioe_project_packs\06_ioe_retrogen_admin`.
- Package manager/build tool: Gradle wrapper, ModDevGradle `net.neoforged.moddev` version `2.0.80`.
- Java: OpenJDK 21.0.11.
- Loader: NeoForge.
- Minecraft: 1.21.1.
- NeoForge: 21.1.230.

## Hard Stop Findings

- Multiple NeoForge metadata files would conflict at the same path: `META-INF/neoforge.mods.toml`.
- Six IOE mod ids exist: `ioe_core`, `ioe_expedition_worldgen`, `ioe_crystal_growth`, `ioe_nether_geodes`, `ioe_ieip_prospecting`, `ioe_retrogen_admin`.
- Six hard-coded `@Mod(...)` entrypoints exist, one per module.
- All IOE module metadata currently says `license="All Rights Reserved"` and no IOE jar contains an embedded LICENSE or NOTICE file.
- ModDevGradle produced NeoForge/Minecraft artifacts under each module's `build\moddev\artifacts`; those are loader/platform artifacts and are not safe to embed.
- Optional external Minecraft mods are referenced only as metadata dependencies (`ae2`, `geore`, `immersiveengineering`, `immersivepetroleum`); their jars were not found under this project and should remain runtime/mod dependencies if used.

## Collision Check Across IOE Runtime Jars

Duplicate entries across all six IOE runtime jars:

- `META-INF/MANIFEST.MF`
- `META-INF/neoforge.mods.toml`
- `pack.mcmeta`

No duplicate IOE asset or data namespaces were found; each module uses its own namespace.

## Jar Inventory

Safety labels:

- `source-only`: IOE-owned output that should only be consolidated from source after a mod-id/entrypoint redesign.
- `do-not-merge`: build tool, loader, Minecraft, NeoForge, or otherwise unsafe jar.

| Jar | Size | Type | Main mod id | Metadata | License files / license | Contents | Safe to merge | Reason |
| --- | ---: | --- | --- | --- | --- | --- | --- | --- |
| `<project-root>\ioe_project_packs\01_ioe_core\build\libs\ioe_core-0.1.7-alpha.jar` | 24644 | IOE runtime artifact | `ioe_core` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 16 classes; assets `ioe_core`; data `ioe_core`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\build\libs\ioe_expedition_worldgen-0.1.7-alpha.jar` | 19131 | IOE runtime artifact | `ioe_expedition_worldgen` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 10 classes; assets `ioe_expedition_worldgen`; data `ioe_expedition_worldgen`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\build\libs\ioe_crystal_growth-0.1.9-alpha.jar` | 21409 | IOE runtime artifact | `ioe_crystal_growth` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 11 classes; assets `ioe_crystal_growth`; data `ioe_crystal_growth`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. Optional dependencies should remain external: `ae2`, `geore`. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\build\libs\ioe_nether_geodes-0.1.10-alpha.jar` | 16194 | IOE runtime artifact | `ioe_nether_geodes` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 7 classes; assets `ioe_nether_geodes`; data `ioe_nether_geodes`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\build\libs\ioe_ieip_prospecting-0.1.9-alpha.jar` | 22304 | IOE runtime artifact | `ioe_ieip_prospecting` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 11 classes; assets `ioe_ieip_prospecting`; data `ioe_ieip_prospecting`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. Optional dependencies should remain external: `immersiveengineering`, `immersivepetroleum`. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\build\libs\ioe_retrogen_admin-0.1.10-alpha.jar` | 32073 | IOE runtime artifact | `ioe_retrogen_admin` | `META-INF/neoforge.mods.toml` | no license file; metadata `All Rights Reserved` | 18 classes; assets `ioe_retrogen_admin`; data `ioe_retrogen_admin`; no mixins/AT/services | source-only | IOE-owned, but direct merge collides on metadata and pack files. |
| `<project-root>\ioe_project_packs\01_ioe_core\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\build\moddev\artifacts\neoforge-21.1.230-client-extra-aka-minecraft-resources.jar` | 9361844 | Minecraft resources artifact | none | none | none detected | 0 classes; assets/data `.mcassetsroot`, `minecraft` | do-not-merge | Minecraft/NeoForge development artifact. |
| `<project-root>\ioe_project_packs\01_ioe_core\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\build\moddev\artifacts\neoforge-21.1.230.jar` | 23526234 | NeoForge artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\01_ioe_core\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\build\moddev\artifacts\neoforge-21.1.230-merged.jar` | 32407287 | NeoForge merged artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 9766 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform component. |
| `<project-root>\ioe_project_packs\01_ioe_core\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\build\moddev\artifacts\neoforge-21.1.230-sources.jar` | 9390394 | NeoForge sources artifact | `neoforge` | `META-INF/neoforge.mods.toml` | metadata `LGPL v2.1`; no license file detected | 0 classes; assets/data `c`, `minecraft`, `neoforge`; mixin `neoforge.mixins.json`; AT `META-INF/accesstransformer.cfg`; services present | do-not-merge | Loader/platform source artifact. |
| `<project-root>\ioe_project_packs\01_ioe_core\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |
| `<project-root>\ioe_project_packs\02_ioe_expedition_worldgen\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |
| `<project-root>\ioe_project_packs\03_ioe_crystal_growth\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |
| `<project-root>\ioe_project_packs\04_ioe_nether_geodes\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |
| `<project-root>\ioe_project_packs\05_ioe_ieip_prospecting\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |
| `<project-root>\ioe_project_packs\06_ioe_retrogen_admin\gradle\wrapper\gradle-wrapper.jar` | 43453 | Gradle wrapper tool | none | none | `META-INF/LICENSE` | 31 classes; no assets/data/mod metadata | do-not-merge | Build tool jar, not a mod/library dependency to embed. |

## Build And Validation

Commands run:

```powershell
java -version
.\gradlew.bat clean build test processResources jar --stacktrace --console=plain
```

The Gradle command was run once in each implementation module:

- `ioe_project_packs\01_ioe_core`: exit 0, `BUILD SUCCESSFUL`.
- `ioe_project_packs\02_ioe_expedition_worldgen`: exit 0, `BUILD SUCCESSFUL`.
- `ioe_project_packs\03_ioe_crystal_growth`: exit 0, `BUILD SUCCESSFUL`.
- `ioe_project_packs\04_ioe_nether_geodes`: exit 0, `BUILD SUCCESSFUL`.
- `ioe_project_packs\05_ioe_ieip_prospecting`: exit 0, `BUILD SUCCESSFUL`.
- `ioe_project_packs\06_ioe_retrogen_admin`: exit 0, `BUILD SUCCESSFUL`.

Installed Prism LAB target:

`<local-mods-folder>`

The rebuilt split-module jars were installed to Prism LAB after validation. Old matching IOE jars were removed only after matching exact IOE mod metadata. The target now contains exactly six IOE jars, one for each IOE mod id.

| Mod id | Installed jar | SHA-256 |
| --- | --- | --- |
| `ioe_core` | `<local-mods-folder>\ioe_core-0.1.7-alpha.jar` | `E538CA4DDD73DC51210AE07BD26ECD0EBEDE84C52FC05E7CDC51546E85B74856` |
| `ioe_expedition_worldgen` | `<local-mods-folder>\ioe_expedition_worldgen-0.1.7-alpha.jar` | `5852A16DE6EA9834FC21C91FA26FBFF36510306F5A7B8C2EA2E40D069FABF9F6` |
| `ioe_crystal_growth` | `<local-mods-folder>\ioe_crystal_growth-0.1.9-alpha.jar` | `615047B0ECAE9EE2B03C8CBF8F2DC396F769FF8B0A442B901AEC6D2E51883FC6` |
| `ioe_nether_geodes` | `<local-mods-folder>\ioe_nether_geodes-0.1.10-alpha.jar` | `65904871B4E5EB5C2EA63FB446C589AE80E031FCA6B5A8A8B04D02C3B1580273` |
| `ioe_ieip_prospecting` | `<local-mods-folder>\ioe_ieip_prospecting-0.1.9-alpha.jar` | `4BEB1DA7FA37BB32464B50B1F3A6E548BAD83B20003EE07E1CD9793EE50B24BD` |
| `ioe_retrogen_admin` | `<local-mods-folder>\ioe_retrogen_admin-0.1.10-alpha.jar` | `5704EAB3806B0C846FB18FA693711D5AC3627E2B603749871B9F6E43CFCDE8A7` |

Every installed split jar was read back and verified to contain:

- exactly one `META-INF/neoforge.mods.toml`;
- the expected IOE mod id and version;
- compiled mod classes;
- its own asset and data namespace;
- no Fabric/Quilt metadata;
- no mixin configs;
- no access transformers;
- no service files.

## Files Modified

- Added this report: `<project-root>\JAR_MERGE_AUDIT_REPORT.md`
- Rebuilt generated `build\` outputs through Gradle.
- Replaced six IOE jars in the Prism LAB mods directory with rebuilt, hash-verified split-module jars.

No production source files, Gradle build files, metadata files, or dependency declarations were changed by this audit.

## Remaining Risks

- No merged single IOE artifact was produced because direct jar merging is unsafe and source-level consolidation requires an intentional mod-id/entrypoint redesign.
- No client/server smoke test was run because the packaging strategy did not change from the existing split-module stack.
- The current repo worktree had many pre-existing dirty source/config/test changes before this audit; they were preserved.
- IOE publication licensing remains undecided: module metadata and `LICENSE_POLICY.md` currently say `All Rights Reserved`.
