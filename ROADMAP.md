# Living World — Roadmap

No delivery dates are committed. Milestones describe dependency order and verified outcomes, not schedule promises.

| Milestone | Verifiable outcome | Dependencies | Tasks | State |
| --- | --- | --- | --- | --- |
| M0 — Foundation | buildable/testable modular Paper plugin with i18n foundation and runtime smoke boot | none | LW-001..LW-003 | validated |
| M1 — World Time | coherent world clock, calendar and seasons core | M0 | LW-010..LW-012 | validated |
| M2 — Environment | climate model and first environmental events integrated with seasons at runtime | M1 | LW-020..LW-024 | validated |
| M3 — World Memory & QoL | first reactive-world and convenience mechanics such as desire lines, double doors and waystones | M0; feature-specific dependencies | LW-030..LW-036 | validated |
| M4 — Player HUD & environmental feedback | surface existing calendar/climate/navigation state through optional boss bars/action bar without coupling presentation into domain logic | M1, M2 | LW-040..LW-044 | validated |
| M5 — Ecology & richer evolution | bounded ecological/environmental responses: growth, ground cover, farmland moisture, fire spread and natural snow/ice persistence react to effective climate without scans | M2, M4 gameplay evidence | LW-050..LW-059 | validated |
| M6 — Stability, profiling & operator controls | harden runtime behavior, measure event hot paths, improve diagnostics/configuration and verify graceful lifecycle before adding broader simulation | M0..M5 | LW-060..LW-062 | validated |
| M7 — Waystone survival UX | make the validated travel system feel intentional in long-running survival without adding hidden economy/balance costs | M3, M6 | LW-070..LW-071 | validated |
| M8 — Waystone visual navigation | add an optional visual travel surface over the same validated Waystone identity/access/travel rules | M3, M7 | LW-080..LW-081 | validated |
| M9 — Seasonal transition feedback | make season boundaries perceptible to players using existing calendar transition events without introducing polling or simulation state | M1, M4 | LW-090..LW-091 | validated |
| M10 — Local climate readability | expose the effective local climate and already-active ecology probabilities so survival players can understand why the world reacts differently by place/season | M2, M5, M9 | LW-100..LW-101 | validated |
| M11 — Weather readability | expose current local weather tendency and announce weather events only when Living World actually applies them, without fake forecasting or new simulation state | M2, M10 | LW-110..LW-112 | in progress |
| M12 — Seasonal ecology & environmental foundation | evolve validated seasonal ecology into a shared thermal/environmental foundation before broader visual/physical simulation | M10, M11 | LW-120..LW-128 | in progress |
| M13 — Waystone navigation clarity | make the existing Waystone network easier to scan by surfacing nearest-first same-world ordering plus world/coordinate/distance context without changing access or travel safety | M8 | LW-130..LW-131 | in progress |
| M14 — Discovery foundation | add durable personal/world discovery memory and prove it through additive Waystone integration without coupling presentation or changing Waystone rules | M3, M13 | LW-140..LW-142 | in progress |

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

## Checkpoint M12.4

M12.3 concluído conforme confirmação do usuário. M12.4 implementa a primeira fatia visual de folhas via movimento limitado por jogador, sem scans periódicos ou mutações de blocos, e teve aceite/runtime smoke concluídos.

## Direção M12.5 — Environmental & Thermal Foundation

Antes de ampliar flora/efeitos, consolidar:

1. arquitetura ambiental e contratos plugin/datapack/resourcepack;
2. `AmbientTemperature` separado de `PlayerThermalState`;
3. feedback térmico (cold breath + sensação progressiva de congelamento);
4. compatibilidade por propriedades/fallback com Terralith + Tectonic, Incendium, Nullscape e namespaces desconhecidos;
5. spike de projeção visual sazonal client-side;
6. inverno físico bounded/lazy (snow/ice/thaw) somente após a fundação;
7. performance gate medido antes de alegar vantagem comparativa.

Detalhes e limites: `ECOLOGY_AND_SEASONS.md`.

## Checkpoints M11/M13/M14

- **M11 Weather readability — in progress:** LW-110 validated including live `/lw climate` tendency; LW-111 is implemented/tested but still lacks direct live applied-weather announcement; LW-112 remains pending.
- **M13 Waystone navigation clarity — in progress:** LW-130 code/gates validated; LW-131 still requires a real menu-order/lore/click smoke. Existing travel success does not substitute for the menu-specific acceptance.
- **M14 Discovery foundation — in progress:** LW-140 foundation validated by automated contracts; LW-141 is integrated with partial live evidence; LW-142 remains incomplete despite the user-confirmed `Descoberto` moment and runtime create/re-interaction/travel/destruction/climate coexistence.
