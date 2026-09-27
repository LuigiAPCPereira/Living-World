# Living World — Task Inventory

- **Scope:** Foundation v0.1 and staged product roadmap
- **Requirements:** `PRODUCT.md`
- **Architecture:** `DESIGN.md`
- **Roadmap:** `ROADMAP.md`
- **Task states:** pending → in progress → implemented not validated → validated → integrated (when required)

| ID | Milestone | Result / task | State | Depends on | Acceptance | Validation evidence | Branch/PR |
| --- | --- | --- | --- | --- | --- | --- | --- |
| LW-001 | M0 | Initialize repository protocol/product/task/architecture continuity | validated | none | Recoverable product scope, instructions, task inventory, roadmap and checkpoint exist | files created and reopened; repository snapshot protocol v2.2 | local master |
| LW-002 | M0 | Implement minimal module lifecycle + i18n foundation | validated | LW-001 | deterministic enable/disable + rollback; PT-BR canonical/fallback; additional locale support; tests | `./gradlew test build` successful after PT-BR canonicalization | local master |
| LW-003 | M0 | Smoke boot Living World on a real Paper dev server | validated | LW-002 | plugin enables/disables cleanly via run-paper; no startup errors | Paper 26.3 build 49: plugin loaded, logged "Living World ativado.", reached Done and disabled cleanly on stop | local master |
| LW-010 | M1 | Define and implement world clock domain | validated | M0 | deterministic time model separated from Paper scheduling | IntelliJ rebuild + `./gradlew test build` successful; boundary/negative tick tests included | local master |
| LW-011 | M1 | Implement calendar (days/months/years) | validated | LW-010 | calendar progression and persistence rules specified/tested | IntelliJ rebuild + clean Gradle gates; default 12×8 calendar and World PDC persistence tests | local master |
| LW-012 | M1 | Implement seasons core | validated | LW-011 | season state derives from calendar and emits explicit transitions | IntelliJ rebuild + `./gradlew test build` successful; season mapping and transition boundary tests | local master |
| LW-020 | M2 | Define climate model and weather policy | validated | LW-012 | biome/season-aware bounded climate state with clear gameplay impact | IntelliJ rebuild + `./gradlew test build` successful; bounded anomaly and biome/season policy tests | local master |
| LW-021 | M2 | Implement first environmental events | validated | LW-020 | events are bounded, configurable and observable | clean `./gradlew test build --rerun-tasks` successful; planner/config/Paper adapter covered by tests and compile validation | local master |
| LW-022 | M2 | Wire calendar/season runtime source and persistence | implemented not validated | LW-021 | logical calendar samples real world time without scheduler drift; sleep may advance days; command/plugin jumps do not; state persists via World PDC | clean unit/build gates pass; runtime startup confirmed; direct console /time validation blocked by Paper 26.3 build 49 console-dispatch NPE | local master |
| LW-023 | M2 | Implement representative climate sampling for active world | validated | LW-020 | bounded sampling uses Paper temperature/humidity APIs without biome-name tables or world scans | clean Gradle gates; classifier + spawn fallback + 32-player cap tests | local master |
| LW-024 | M2 | Connect daily logical-calendar progress to climate/weather reaction | validated | LW-021, LW-023 | at most one representative climate evaluation/weather plan per logical advance; climate failure cannot roll back persisted calendar progress | clean Gradle gates; end-to-end coordinator test with Paper API proxy | local master |
| LW-030 | M3 | Implement desire lines / path wear | implemented not validated | M0 | repeated traffic can evolve paths without whole-world scans | clean IntelliJ/Gradle gates; policy/ledger/Chunk PDC tests pass; live-player movement smoke still pending | local master |
| LW-033 | M3 | Add Desire Lines recovery/regrowth policy | pending | LW-030, calendar | unused worn terrain can recover without world scans by reusing tracked state and logical-day cadence | not validated | local master |
| LW-031 | M3 | Implement double-door QoL | implemented not validated | M0 | paired compatible doors act together without surprising unrelated blocks | clean IntelliJ/Gradle gates; pair-policy tests pass; live-player click smoke still pending | local master |
| LW-032 | M3 | Define and implement waystone core | pending | M0 | activation/travel lifecycle and safe destination rules specified/tested | not validated | TBD |

## Scope discipline

Future ideas remain future until promoted into the active scope. Do not start a later task merely because it is easy while the active milestone has unfinished dependencies.

## Current task

**LW-032 — define and implement waystone core.**
