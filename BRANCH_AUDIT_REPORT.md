# Branch Audit Report

Date: 2026-07-05
Repository: https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition
Default branch: `main`

## Executive Summary

Remote branches inspected: 37, excluding the symbolic `origin/HEAD` ref.

Current state:

Verification summary:

- Total branches distantes: 37
- Branches mergées / zero-ahead: 21
- Branches non mergées avec commits hors `main`: 15
- Branches probablement obsolètes / à investiguer: 3
- Conclusion: Oui, il y a trop de branches non mergées.
- Current audit branch: `codex/feature-v33-runtime-evidence-remediation-tracker`
- Aucune opération de merge, suppression de branche, fermeture de PR, build, Prism ou Minecraft n'a été exécutée.

| Group | Count |
|---|---:|
| `main` / à conserver | 1 |
| Branches non mergées with commits not in `main` | 15 |
| Branches à merger in the active `v22`-`v33` stack | 12 |
| Branches probablement obsolètes / à investiguer | 3 |
| Merged or zero-ahead non-main branches / à supprimer après confirmation | 21 |
| Branches à fermer/abandonner immediately | 0 |
| Closed-without-merge branches | 0 |
| Branches without an associated PR | 1 (`main`) |
| Exact duplicate branch tips | 0 |

Yes: there are too many unmerged active branches. The main risk is not raw branch count; it is the active stack from `v22` through `v33`, plus three older open branches (`#3`, `#4`, `#15`) that are behind `main` and still contain unique commits. That creates:

- confusion de roadmap, because active work is spread across 15 open PR branches;
- duplication de travail, especially where older branches may already be superseded by later docs or CI changes;
- divergence de code/docs, because `#3`, `#4`, and `#15` are 20 to 40 commits behind `main`;
- difficulty knowing which version is canonical, because `main` has `v21` while the open stack reaches `v33`;
- accumulation de branches `vXX`, with 12 consecutive open PR branches from `v22` to `v33`.

## Scope And Evidence

No merges, branch deletes, source edits, builds, PrismLauncher actions, Minecraft launches, or jar installs were performed.

Project/root preflight:

| Item | Value |
|---|---|
| Project root | `C:/Users/Emmanuel Tremblay/AI Depot/Codex Documents/Immersive_Ore_Expedition` |
| Current local branch | `codex/feature-v33-runtime-evidence-remediation-tracker` |
| Default branch | `main` |
| Build system/package manager at current root | None detected at root (`settings.gradle`, `build.gradle`, `gradlew`, `pom.xml`, `package.json` absent) |
| Relevant config found | `AGENTS.md`, `.github/workflows/ci.yml` |
| Requested target output | `BRANCH_AUDIT_REPORT.md` |

CodeGraph was checked first because `.codegraph/` exists. `codegraph status` reported a healthy index with no pending changes and no reindex recommendation. A CodeGraph query for branch/PR metadata was not useful because this is repository metadata, so Git and GitHub CLI were used as source of truth.

Commands used for evidence:

```powershell
git rev-parse --show-toplevel
git status --short --branch
codegraph status
codegraph explore "branch audit repository metadata GitHub pull request branch chain default branch current remote branch status"
gh repo view emmanueltremblay9-stack/Immersive_Ore_Expedition --json nameWithOwner,defaultBranchRef,url,pushedAt,updatedAt
git fetch --all --prune
git for-each-ref refs/remotes/origin --format="..."
gh pr list --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --state all --limit 300 --json number,title,headRefName,baseRefName,state,isDraft,mergedAt,closedAt,url
git rev-list --left-right --count origin/main...origin/<branch>
```

`git fetch --all --prune` exited successfully and pruned one stale local remote-tracking ref: `origin/codex/feature-biome-aware-province-planning-v6`. This did not delete a remote branch.

## Full Branch Table

`Ahead/Behind` means commits unique to the branch versus commits missing from the branch, compared to `origin/main`.

| Branch | Probable base | Last commit | Date | Author | Ahead/Behind | PR | PR status | Probable status | Category | Subject |
|---|---|---|---|---|---:|---|---|---|---|---|
| `codex/consolidated-ioe-ci` | `main` | `234e008` | 2026-06-29 | emmanueltremblay9-stack | +0/-43 | [#1](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/1) | merged | mergée | à supprimer après confirmation | ci: add GitHub-hosted validation for consolidated IOE jar |
| `codex/debug-runtime-visible-proof-layer` | `main` | `a755893` | 2026-07-04 | emmanueltremblay9-stack | +1/-20 | [#15](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/15) | open | obsolète / à vérifier | à investiguer | chore: add IOE runtime visibility proof command |
| `codex/feature-crystal-ae2-site-placement-v13` | `main` | `5c40af0` | 2026-07-04 | emmanueltremblay9-stack | +0/-17 | [#17](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/17) | merged | mergée | à supprimer après confirmation | feat: add crystal and AE2 site placement planning |
| `codex/feature-expedition-anchor-placement-v8` | `main` | `9eb49b0` | 2026-07-02 | emmanueltremblay9-stack | +0/-27 | [#11](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/11) | merged | mergée | à supprimer après confirmation | feat: add expedition anchor placement planning |
| `codex/feature-ie-ip-surface-clue-placement-v12` | `main` | `95c2e21` | 2026-07-04 | emmanueltremblay9-stack | +0/-19 | [#16](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/16) | merged | mergée | à supprimer après confirmation | feat: add IE IP surface clue placement planning |
| `codex/feature-live-biome-province-worldgen-binding-v11` | `main` | `2b2dc2b` | 2026-07-03 | emmanueltremblay9-stack | +0/-21 | [#14](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/14) | merged | mergée | à supprimer après confirmation | feat: add live biome province binding scaffold |
| `codex/feature-meteoritic-ae2-geodes-v14` | `main` | `3488d22` | 2026-07-04 | emmanueltremblay9-stack | +0/-15 | [#18](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/18) | merged | mergée | à supprimer après confirmation | feat: add meteoritic AE2 geode planning |
| `codex/feature-nether-sub-lava-geode-planning-v15` | `main` | `7758cf6` | 2026-07-05 | emmanueltremblay9-stack | +0/-13 | [#19](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/19) | merged | mergée | à supprimer après confirmation | feat: add Nether sub-lava geode planning |
| `codex/feature-ore-load-chamber-placement-planning-v9` | `main` | `6afe9fb` | 2026-07-02 | emmanueltremblay9-stack | +0/-25 | [#12](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/12) | merged | mergée | à supprimer après confirmation | feat: add ore-load chamber placement planning |
| `codex/feature-persistent-conservative-retrogen-v16` | `main` | `835a989` | 2026-07-05 | emmanueltremblay9-stack | +0/-11 | [#20](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/20) | merged | mergée | à supprimer après confirmation | feat: add persistent conservative retrogen scaffold |
| `codex/feature-province-bindings-v3` | `main` | `7690062` | 2026-06-29 | emmanueltremblay9-stack | +0/-37 | [#6](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/6) | merged | mergée | à supprimer après confirmation | feat: add configurable province bindings |
| `codex/feature-province-resource-policies-v4` | `main` | `2335f94` | 2026-06-29 | emmanueltremblay9-stack | +0/-35 | [#7](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/7) | merged | mergée | à supprimer après confirmation | feat: add province resource policy rules |
| `codex/feature-province-resource-selectors-v5` | `main` | `b42e064` | 2026-06-29 | emmanueltremblay9-stack | +0/-33 | [#8](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/8) | merged | mergée | à supprimer après confirmation | feat: add province resource selectors |
| `codex/feature-province-runtime-integration-v2` | `main` | `bdadb87` | 2026-06-29 | emmanueltremblay9-stack | +0/-39 | [#5](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/5) | merged | mergée | à supprimer après confirmation | feat: add opt-in province runtime integration |
| `codex/feature-province-system-v1` | `main` | `eeeefd8` | 2026-06-29 | emmanueltremblay9-stack | +0/-41 | [#2](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/2) | merged | mergée | à supprimer après confirmation | feat: add province system v1 foundation |
| `codex/feature-random-ore-suppression-integration-v10` | `main` | `01cd5a2` | 2026-07-03 | emmanueltremblay9-stack | +0/-23 | [#13](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/13) | merged | mergée | à supprimer après confirmation | feat: add random ore suppression integration scaffold |
| `codex/feature-release-hardening-smoke-validation-v17` | `main` | `6796728` | 2026-07-05 | emmanueltremblay9-stack | +0/-9 | [#21](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/21) | merged | mergée | à supprimer après confirmation | chore: add release hardening and smoke validation checks |
| `codex/feature-v18-default-off-worldgen-placement-proof` | `main` | `206a58c` | 2026-07-05 | emmanueltremblay9-stack | +0/-7 | [#22](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/22) | merged | mergée | à supprimer après confirmation | feat: add default-off runtime placement proof gate |
| `codex/feature-v19-default-off-worldgen-registration-smoke-bridge` | `main` | `6e4fbd9` | 2026-07-05 | emmanueltremblay9-stack | +0/-5 | [#23](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/23) | merged | mergée | à supprimer après confirmation | feat: add default-off v19 worldgen registration smoke bridge |
| `codex/feature-v20-default-off-configured-placed-feature-bridge` | `main` | `88988fc` | 2026-07-05 | emmanueltremblay9-stack | +0/-3 | [#24](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/24) | merged | mergée | à supprimer après confirmation | feat: add default-off v20 configured placed feature bridge |
| `codex/feature-v21-default-off-biome-modifier-smoke-tag-bridge` | `main` | `bf2953e` | 2026-07-05 | emmanueltremblay9-stack | +0/-1 | [#25](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/25) | merged | mergée | à supprimer après confirmation | feat: add default-off v21 biome modifier smoke tag bridge |
| `codex/feature-v22-controlled-worldgen-smoke-profile` | `main` | `bafc403` | 2026-07-05 | emmanueltremblay9-stack | +1/-0 | [#26](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/26) | open | active / non mergée | à merger | docs: add v22 controlled worldgen smoke profile package |
| `codex/feature-v23-controlled-smoke-runbook` | `codex/feature-v22-controlled-worldgen-smoke-profile` | `d7aa3c8` | 2026-07-05 | emmanueltremblay9-stack | +2/-0 | [#27](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/27) | open | active / non mergée | à merger | docs: add v23 controlled smoke runbook |
| `codex/feature-v24-smoke-evidence-gate` | `codex/feature-v23-controlled-smoke-runbook` | `1b6879a` | 2026-07-05 | emmanueltremblay9-stack | +3/-0 | [#28](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/28) | open | active / non mergée | à merger | docs: add v24 smoke evidence gate |
| `codex/feature-v25-runtime-promotion-readiness` | `codex/feature-v24-smoke-evidence-gate` | `477b2f6` | 2026-07-05 | emmanueltremblay9-stack | +4/-0 | [#29](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/29) | open | active / non mergée | à merger | docs: add v25 runtime promotion readiness |
| `codex/feature-v26-runtime-slice-implementation-packet` | `codex/feature-v25-runtime-promotion-readiness` | `cd128c9` | 2026-07-05 | emmanueltremblay9-stack | +5/-0 | [#30](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/30) | open | active / non mergée | à merger | docs: add v26 runtime slice implementation packet |
| `codex/feature-v27-runtime-resource-inventory` | `codex/feature-v26-runtime-slice-implementation-packet` | `9812a90` | 2026-07-05 | emmanueltremblay9-stack | +6/-0 | [#31](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/31) | open | active / non mergée | à merger | docs: add v27 runtime resource inventory |
| `codex/feature-v28-runtime-pr-preflight-manifest` | `codex/feature-v27-runtime-resource-inventory` | `f4fe29e` | 2026-07-05 | emmanueltremblay9-stack | +7/-0 | [#32](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/32) | open | active / non mergée | à merger | docs: add v28 runtime PR preflight manifest |
| `codex/feature-v29-runtime-traceability-matrix` | `codex/feature-v28-runtime-pr-preflight-manifest` | `9c8deda` | 2026-07-05 | emmanueltremblay9-stack | +8/-0 | [#33](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/33) | open | active / non mergée | à merger | docs: add v29 runtime traceability matrix |
| `codex/feature-v30-runtime-evidence-packet` | `codex/feature-v29-runtime-traceability-matrix` | `ef372fc` | 2026-07-05 | emmanueltremblay9-stack | +9/-0 | [#34](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/34) | open | active / non mergée | à merger | docs: add v30 runtime evidence packet |
| `codex/feature-v31-runtime-evidence-review-checklist` | `codex/feature-v30-runtime-evidence-packet` | `45741b2` | 2026-07-05 | emmanueltremblay9-stack | +10/-0 | [#35](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/35) | open | active / non mergée | à merger | docs: add v31 runtime evidence review checklist |
| `codex/feature-v32-runtime-evidence-decision-record` | `codex/feature-v31-runtime-evidence-review-checklist` | `844b676` | 2026-07-05 | emmanueltremblay9-stack | +11/-0 | [#36](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/36) | open | active / non mergée | à merger | docs: add v32 runtime evidence decision record |
| `codex/feature-v33-runtime-evidence-remediation-tracker` | `codex/feature-v32-runtime-evidence-decision-record` | `91621e6` | 2026-07-05 | emmanueltremblay9-stack | +12/-0 | [#37](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/37) | open | active / non mergée | à merger | docs: add v33 runtime evidence remediation tracker |
| `codex/feature-worldgen-registration-scaffold-v7` | `main` | `20ec8a9` | 2026-07-02 | emmanueltremblay9-stack | +0/-29 | [#10](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/10) | merged | mergée | à supprimer après confirmation | feat: add worldgen registration scaffold |
| `codex/hygiene-post-province-v1` | `main` | `a9b832a` | 2026-06-29 | emmanueltremblay9-stack | +1/-40 | [#4](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/4) | open | obsolète / à vérifier | à investiguer | docs: clarify active validation path after province v1 |
| `codex/legacy-split-module-hardening` | `main` | `bf01f7d` | 2026-06-29 | emmanueltremblay9-stack | +2/-40 | [#3](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/pull/3) | open | obsolète / à vérifier | à investiguer | ci: validate legacy split modules |
| `main` | `(default)` | `765db7b` | 2026-07-05 | Oblixor Prime | +0/-0 | none | none found | active | à conserver | Merge pull request #25 from emmanueltremblay9-stack/codex/feature-v21-default-off-biome-modifier-smoke-tag-bridge |

## Recommendations

À conserver:

- `main`.
- The `v22` to `v33` branches only while their stacked PRs remain under active review.

À merger, in dependency order:

1. `#26` / `codex/feature-v22-controlled-worldgen-smoke-profile` into `main`.
2. `#27` / `codex/feature-v23-controlled-smoke-runbook`, after `#26` is merged or retargeted.
3. `#28` / `codex/feature-v24-smoke-evidence-gate`, after `#27`.
4. `#29` / `codex/feature-v25-runtime-promotion-readiness`, after `#28`.
5. `#30` / `codex/feature-v26-runtime-slice-implementation-packet`, after `#29`.
6. `#31` / `codex/feature-v27-runtime-resource-inventory`, after `#30`.
7. `#32` / `codex/feature-v28-runtime-pr-preflight-manifest`, after `#31`.
8. `#33` / `codex/feature-v29-runtime-traceability-matrix`, after `#32`.
9. `#34` / `codex/feature-v30-runtime-evidence-packet`, after `#33`.
10. `#35` / `codex/feature-v31-runtime-evidence-review-checklist`, after `#34`.
11. `#36` / `codex/feature-v32-runtime-evidence-decision-record`, after `#35`.
12. `#37` / `codex/feature-v33-runtime-evidence-remediation-tracker`, after `#36`.

À investiguer before merge or close:

- `#3` / `codex/legacy-split-module-hardening`: open, +2 unique commits, -40 behind `main`. It is likely stale and should be rebased/revalidated before any merge.
- `#4` / `codex/hygiene-post-province-v1`: open, +1 unique commit, -40 behind `main`. It may be superseded by later documentation and should be reviewed for relevance.
- `#15` / `codex/debug-runtime-visible-proof-layer`: open, +1 unique commit, -20 behind `main`. It may still be useful, but it should be retested against current `main`.

À fermer/abandonner immediately:

- None confirmed. The likely candidates are `#3`, `#4`, and `#15`, but only after their unique commits are checked against current `main`.

À supprimer après confirmation:

- `codex/consolidated-ioe-ci`
- `codex/feature-province-system-v1`
- `codex/feature-province-runtime-integration-v2`
- `codex/feature-province-bindings-v3`
- `codex/feature-province-resource-policies-v4`
- `codex/feature-province-resource-selectors-v5`
- `codex/feature-worldgen-registration-scaffold-v7`
- `codex/feature-expedition-anchor-placement-v8`
- `codex/feature-ore-load-chamber-placement-planning-v9`
- `codex/feature-random-ore-suppression-integration-v10`
- `codex/feature-live-biome-province-worldgen-binding-v11`
- `codex/feature-ie-ip-surface-clue-placement-v12`
- `codex/feature-crystal-ae2-site-placement-v13`
- `codex/feature-meteoritic-ae2-geodes-v14`
- `codex/feature-nether-sub-lava-geode-planning-v15`
- `codex/feature-persistent-conservative-retrogen-v16`
- `codex/feature-release-hardening-smoke-validation-v17`
- `codex/feature-v18-default-off-worldgen-placement-proof`
- `codex/feature-v19-default-off-worldgen-registration-smoke-bridge`
- `codex/feature-v20-default-off-configured-placed-feature-bridge`
- `codex/feature-v21-default-off-biome-modifier-smoke-tag-bridge`

No branch was classified as an exact duplicate by identical tip SHA. The merged branches above are redundant from a history/PR-state perspective, not duplicate-tip branches.

## Cleanup Order

1. Freeze new branch creation until the active PR stack is reduced.
2. Confirm `main` is green in GitHub Actions.
3. Process the stacked PRs from `#26` through `#37`, lowest to highest, updating downstream PR bases after each merge.
4. Separately decide `#3`, `#4`, and `#15`: rebase/revalidate and merge only if still needed; otherwise close them as superseded.
5. After confirmation, delete the 21 merged branch refs listed above.
6. Re-run this audit command set and confirm only `main` plus intentional active work remain.

## Proposed Commands Only

These commands are proposed for a future cleanup pass. They were not executed during this audit.

Inspect open PRs:

```powershell
gh pr view 3 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --web
gh pr view 4 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --web
gh pr view 15 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --web
gh pr view 26 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --web
```

Process the active stack, one PR at a time after checks pass:

```powershell
gh pr checks 26 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition
gh pr merge 26 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --merge
gh pr edit 27 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --base main
```

Repeat that pattern for PRs `27` through `37`: check, merge, retarget the next PR to the new base.

Close superseded open PRs only after review:

```powershell
gh pr close 3 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --comment "Superseded by later mainline work."
gh pr close 4 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --comment "Superseded by later mainline work."
gh pr close 15 --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --comment "Superseded or will be recreated against current main."
```

Delete merged branch refs only after confirmation:

```powershell
$mergedBranches = @(
  'codex/consolidated-ioe-ci',
  'codex/feature-province-system-v1',
  'codex/feature-province-runtime-integration-v2',
  'codex/feature-province-bindings-v3',
  'codex/feature-province-resource-policies-v4',
  'codex/feature-province-resource-selectors-v5',
  'codex/feature-worldgen-registration-scaffold-v7',
  'codex/feature-expedition-anchor-placement-v8',
  'codex/feature-ore-load-chamber-placement-planning-v9',
  'codex/feature-random-ore-suppression-integration-v10',
  'codex/feature-live-biome-province-worldgen-binding-v11',
  'codex/feature-ie-ip-surface-clue-placement-v12',
  'codex/feature-crystal-ae2-site-placement-v13',
  'codex/feature-meteoritic-ae2-geodes-v14',
  'codex/feature-nether-sub-lava-geode-planning-v15',
  'codex/feature-persistent-conservative-retrogen-v16',
  'codex/feature-release-hardening-smoke-validation-v17',
  'codex/feature-v18-default-off-worldgen-placement-proof',
  'codex/feature-v19-default-off-worldgen-registration-smoke-bridge',
  'codex/feature-v20-default-off-configured-placed-feature-bridge',
  'codex/feature-v21-default-off-biome-modifier-smoke-tag-bridge'
)

foreach ($branch in $mergedBranches) {
  git push origin --delete $branch
}
```

Verify afterwards:

```powershell
git fetch --all --prune
git branch -r --no-merged origin/main
gh pr list --repo emmanueltremblay9-stack/Immersive_Ore_Expedition --state open
```

## Risks And Assumptions

- Classifications are based on Git commit graph state and GitHub PR state as queried on 2026-07-05.
- "Merged" means the branch has no commits ahead of `origin/main` or has a merged PR; it does not mean the remote branch ref has already been deleted.
- "Duplicate" was checked by exact branch-tip SHA only. Semantic duplicate content was not exhaustively diffed.
- No build or runtime validation was run, in line with the project rule to prefer GitHub-hosted CI and avoid local Gradle/Minecraft/Prism validation by default.
- The active stacked PRs may need retargeting after each merge; merging them blindly out of order can create confusing diffs or dropped context.

## Final Conclusion

Oui, il y a trop de branches non mergées. Factually, 15 non-main branches still contain commits absent from `main`: 12 belong to the consecutive `v22`-`v33` stack and 3 older open PR branches are probably obsolete / à investiguer. The safe path is to reduce the active stack from the bottom (`#26`) upward, make an explicit decision on the three older stale open PRs (`#3`, `#4`, `#15`), and then delete the 21 already-merged remote branches only after confirmation.
