# Nether geometry comparison — bounded design study, 2026-10-12

## Geometry and scope

The exact 74x74 window (5,476 columns) qualifies the connected lava surface; it is
not the volume carved or filled. Coverage stays 60% (3,286 deep connected columns),
LOWEST at the canonical column, without fallback. The separate floor survey is
15x15 (225 first non-liquid cells), also governing soul-soil quality/debris.
The canonical center is F-16, where F is the minimum floor Y. A 15-cube of allowed
unprotected rock is inspected; only radius-3 interior cells become air, and finite
ore budgets occupy selected cells in the radius-5 shell. Two shore marker blocks
are additional writes. None of these volumes implies a 74x74 excavation.

Temporary source `bd6849ef08c1cdc6ba636dea4b15f748e25f2917` compared four geometry
options in the existing hosted GameTest, using a separate test-only planner copy.
Production planner stayed unchanged. Its 16/15 control was checked against the
canonical planner's status, connected-column count and complete plan equality.

| Study depth / inspected cube | Minimum F for build-height fit (minY=0) | Intended tradeoff |
|---|---:|---|
| 16 / 15 (control) | 23 | Current deep placement, widest solid-rock check |
| 12 / 15 | 19 | Four blocks higher; retain the original rock envelope |
| 8 / 15 | 15 | Eight blocks higher, closer to lake floor; original envelope retained |
| 12 / 13 | 18 | Smaller inspected rock envelope; still three solid cells beyond radius-3 cavity on each axis |

Cavity, mineral shell, floor survey, materials and budgets are unchanged in every
variant. A smaller inspected cube does not shrink the placed mineral formation.
Reducing the floor survey to 11x11 was NOT executed: it would also change the
225-first-floor-cell soul-soil decision. Admitting magma/gravel, reducing the 60%
coverage threshold or choosing another lava surface was likewise NOT executed.
Those are separate locked decisions, not implicit geometry study permissions.

## Paired experiment and exact evidence

Seed 0, Minecraft 1.21.1, NeoForge 21.1.230, baseline runtime. Reused the coordinates
from the existing bounded256 evidence: first 16 height-prefilter failures, eight
floor-start failures, four floor-material failures and four absent-surface failures.
This is a deliberately stratified 32-candidate diagnostic cohort, not an unbiased
frequency estimate. Worlds were freshly engine-generated and unedited. Each candidate
had real new-chunk receipts. Controlled FULL 5x5 loading remains artificial.

All four variants read the same cached, same-server-turn terrain capture. Every real
read, including selection and final state/protection reread, debits the existing
shared tick budget. Cache reuse is not a quota reset. Complete read-set revalidation
passed for all 32; total real reads 870,244, max 55,338 per candidate/tick, below 65,536.
No budget, availability or control-divergence failure occurred. There were 128
variant evaluations, no terrain edits and no placement/ledger mutation. This compares
full planner eligibility, not actual committed variant placements or player loading.

[Push CI 38201536790](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38201536790)
ran the comparison once. [PR CI 38201539982](https://github.com/emmanueltremblay9-stack/Immersive_Ore_Expedition/actions/runs/38201539982)
and full-runtime repetitions explicitly did not run the study. Both CIs succeeded.
`NETHER_GEOMETRY_COMPARISON_EVIDENCE.json` preserves all 128 rows, 32 read-set receipts,
coordinates, source, versions, read/write counts and source-log hash.

| Variant | Eligible plans | Coverage refusal | Floor refusal | No surface | Height | Crust/protection |
|---|---:|---:|---:|---:|---:|---:|
| 16 / 15 | 0/32 | 18 | 9 | 4 | 1 | 0 |
| 12 / 15 | 0/32 | 18 | 9 | 4 | 0 | 1 |
| 8 / 15 | 0/32 | 18 | 9 | 4 | 0 | 1 |
| 12 / 13 | 0/32 | 18 | 9 | 4 | 0 | 1 |

Only region (-2042,-2000), candidate (-522712,-511832), reached the volume check.
It has 4,528 qualifying lake columns. Shallower variants removed its height refusal
but failed rock/protection validation; none gained eligibility. Other candidates
fail before variant geometry is reached. These first-failure totals differ from the
cheap prefilter categories because full planning checks connected coverage first;
that is expected ordering, not a terrain improvement or a new defect.

## Recommendation and required decision

**Do not adopt a production change as a proven fix:** no tested option improved
eligibility. Keep the 15-cube protection envelope; shrinking it has no observed benefit.
If choosing a provisional depth for a future approved redesign, prefer **12 blocks,
15-cube**, as the moderate compromise: four blocks more vertical room, unchanged
cavity/resources and original envelope. This is a design recommendation, not evidence
of better natural frequency. Depth 8 is not empirically better and moves the formation
closer to the lake floor. None is presented as accepted placement.

A functional change first requires explicit adoption of “center F-12, same 15x15 floor
and 15-cube, all other rules retained.” It would remove the fixed F-16 contract, but
would NOT resolve the observed dominant coverage/floor refusals. The more consequential
next design question is whether to retain both 60% coverage in 74x74 and the fully
lava-covered 225-cell floor/material rule. Any proposal to relax either must specify
its relation to soul-soil qualification before another dependent experiment. No new
threshold, material list or fallback is approved or implemented by this study.

The study is closed; no widening or second comparison loop. Instrumentation is
removed and source restored to checkpoint `61f6f1c333b520a40e2694915b98b908d9b35aea`.
Only evidence/documentation changes remain. Production stays disabled; natural
committed acceptance, representative loading and visual qualification remain missing.
No server/profile installation, merge or publication occurred.
