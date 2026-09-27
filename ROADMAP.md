# Living World — Roadmap

No delivery dates are committed. Milestones describe dependency order and verified outcomes, not schedule promises.

| Milestone | Verifiable outcome | Dependencies | Tasks | State |
| --- | --- | --- | --- | --- |
| M0 — Foundation | buildable/testable modular Paper plugin with i18n foundation and runtime smoke boot | none | LW-001..LW-003 | validated |
| M1 — World Time | coherent world clock, calendar and seasons core | M0 | LW-010..LW-012 | validated |
| M2 — Environment | climate model and first environmental events integrated with seasons at runtime | M1 | LW-020..LW-024 | implemented; runtime validation partial |
| M3 — World Memory & QoL | first reactive-world and convenience mechanics such as desire lines, double doors and waystones | M0; feature-specific dependencies | LW-030..LW-032 | in progress |
| M4 — Ecology & richer evolution | only after profiling/gameplay evidence justifies deeper ecological simulation | prior foundations | not inventoried yet | future |

## Sequencing principles

- Foundation precedes broad gameplay development.
- Calendar/season state should become a shared source of truth before climate depends on it.
- Independent QoL/world-memory features may evolve after M0 without forcing climate/calendar coupling.
- Features that change balance must expose that impact rather than hiding it under "QoL" or "optimization."
- External worldgen remains a compatibility surface, not a dependency we recreate.

## Known risks

- Paper 26.3 API is currently resolved from an alpha build; version churn must be reconciled before release claims.
- Environmental systems can become tick-heavy if implemented as global scans.
- Persistence requirements are not yet known and should not be guessed.
- Visual seasonal effects may eventually need optional resource-pack support; not part of Foundation v0.1.
