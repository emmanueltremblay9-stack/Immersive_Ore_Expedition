# Roadmap — IOE Retrogen & Admin

## Milestone A
- Implement locate/debug commands. `0.1.1-alpha` registers conservative locate/status/start/pause/radius command paths; runtime province and anchor index binding remains pending.

## Milestone B
- Add chunk markers and safe queue. `0.1.1-alpha` includes marker-version checks, duplicate prevention, radius filtering, unexplored-only filtering, and tick batching.

## Milestone C
- Enable admin radius retrogen. `0.1.1-alpha` queues explicit admin-radius requests but does not mutate chunks yet.
- Add reports and no-duplicate tests. `0.1.1-alpha` includes resource diagnostic reports and unit tests for no-duplicate queueing.
