# Living World — Architecture

- **Observed baseline:** Paper plugin, Java 25, Gradle, package `dev.signalshards.livingworld`.
- **Requirements:** `PRODUCT.md`.
- **Current implementation scope:** Foundation v0.1 / TASKLIST M0.
- **Status:** architecture is intentionally small and expected to evolve with real features.

## Ownership map

| Component | Responsibility | Does not own |
| --- | --- | --- |
| `LivingWorldPlugin` | Paper lifecycle entrypoint and composition root | feature rules, persistence details, translation storage internals |
| `core.module` | deterministic lifecycle for Living World modules | feature-specific behavior |
| `core.i18n` | locale selection, message lookup, formatting/fallback | gameplay rules or per-feature state |
| `features.calendar.domain` | deterministic world-time/calendar domain values independent from Paper scheduling | server polling, listener registration or file I/O |
| `features.calendar.persistence` | narrow persistence contract for logical calendar state | Paper storage details or calendar arithmetic |
| `features.calendar.paper` | Paper time observation, sleep/command filtering, scheduler lifecycle and World PDC adapter | calendar arithmetic or climate policy |
| `features.seasons.domain` | derives current season and explicit transitions from calendar dates | duplicate persistence, climate effects or Paper listeners |
| `features.climate.domain` | bounded climate state and deterministic weather tendency from biome profile + season | Paper weather scheduling, biome lookup or environmental side effects |
| `features.climate.application` | bounded/configurable weather-event planning from climate snapshots | direct Paper API calls |
| `features.climate.paper` | samples representative world climate and applies weather-event plans through public Paper APIs | climate policy, calendar or event selection |
| `features.desirelines.domain` | traffic thresholds and wear stages independent from Paper | movement events, chunks or block mutation |
| `features.desirelines.application` | bounded sparse per-chunk traffic ledger | Paper PDC or block types |
| `features.desirelines.paper` | movement observation, block wear and Chunk PDC persistence | global world scanning or unrelated QoL rules |
| `features.qol.doubledoors.paper` | detects an unambiguous adjacent compatible door and mirrors manual open state | redstone automation or arbitrary nearby doors |
| `features.waystones.domain` | stable waystone identity, display name and anchor coordinates | Paper storage, GUI or teleport APIs |
| `features.waystones.application` | registration, activation visibility and travel result contracts | player/world PDC details |
| `features.waystones.paper` | World/Player PDC adapters, safe destination validation and async Paper teleport | economy, GUI or physical activation presentation |
| future `features.*` packages | one gameplay capability and its state/listeners/tasks | unrelated feature internals |
| Paper API | external server framework boundary | Living World domain decisions |

## Initial package direction

```text
dev.signalshards.livingworld
├── LivingWorldPlugin
├── core
│   ├── i18n
│   └── module
└── features
    ├── calendar       (future)
    ├── seasons        (future)
    ├── climate        (future)
    ├── desirelines    (future)
    ├── waystones      (future)
    └── qol            (future)
```

Only create future packages when their task begins.

## Dependency direction

```text
Paper entrypoint / composition root
              |
              v
        core contracts
              ^
              |
        feature modules
              |
              v
          Paper API
```

Features may use narrow core contracts and Paper APIs. A feature must not reach into another feature's implementation package. Shared behavior should move to core only when more than one real owner needs it.

## Module lifecycle

A module has an explicit identity and lifecycle. The manager enables registered modules in deterministic order and disables them in reverse order. If startup fails partway through, already-enabled modules are rolled back before the failure escapes.

A module that owns event registrations, scheduled work, or resources is responsible for cleanup. The manager coordinates lifecycle; it does not become a service locator.

## i18n

The foundation uses Java resource bundles with a root Brazilian Portuguese catalog. Configuration stores a BCP 47 language tag such as `pt-BR` or `en-US`.

The catalog is intentionally narrow: lookup + formatting + deterministic fallback to `pt-BR`. Rich player messaging (Adventure/MiniMessage, placeholders, per-player locale) should be added only when a concrete feature requires it.

Text intended for players or server administrators is translatable and therefore belongs in the message catalog. Internal developer-facing validation/exception text may remain directly in Java in PT-BR; it is not part of the user-facing localization contract.

## Performance invariants

- event-driven work before polling when practical;
- bounded scheduled work;
- no whole-world recurring scans;
- no blocking I/O on the server thread;
- no claims of optimization without measurement;
- module disable must stop owned background work;
- persistent systems should index only state they own rather than rediscovering the world repeatedly.

## Persistence

Calendar is the first feature with durable state. Its canonical persisted value is only the number of elapsed calendar days. The runtime adapter stores this value in the target World Persistent Data Container (PDC), which keeps the state namespaced with the plugin and avoids bespoke synchronous file I/O on the main thread.

Calendar date is not derived directly from mutable Minecraft wall-clock commands. Administrative time changes must not silently rewrite historical calendar progress. The Paper integration layer will decide which observed world transitions count as a calendar day and persist the resulting logical state.

Runtime observation samples the world's absolute time once per second and derives crossed day boundaries from the observed value rather than assuming the scheduler executed on time. `TimeSkipEvent` is observed at MONITOR priority: NIGHT_SKIP contributes crossed days, while COMMAND and CUSTOM update the observation baseline without advancing the logical calendar. Defensive backward jumps reset the baseline rather than creating negative progress.

In Paper 26.3 the inherited `ClockTimeSkipEvent` reason/amount API is marked experimental. Living World intentionally isolates that dependency inside the Paper calendar adapter because it is the authoritative distinction between sleep, command and plugin time jumps; the domain/application layers contain no Paper experimental types. If Paper changes this surface, only the adapter should need replacement.

Default calendar rules are 12 months per year and 8 days per month (96 days/year). The domain accepts different values so pacing can evolve without changing the date model.

## Seasons

Season state is derived from calendar date instead of persisted independently. The default cycle starts with Primavera and assigns three months to each season in the order Primavera → Verão → Outono → Inverno.

The domain exposes an explicit transition value only when a calendar change crosses a season boundary. Delivery of player messages, weather effects, biome reactions or other consequences belongs to later application/integration layers.

## Climate

Climate is modeled from three inputs: a biome-derived base profile, the current season, and small bounded transient anomalies. The domain deliberately does not depend on Paper biome classes or weather APIs.

The representative Paper sampler reads 3D temperature/humidity values directly from the world API instead of maintaining a table of biome names. With online players it samples at most 32 active player locations; with none online it samples the world spawn. This keeps cost bounded and makes custom worldgen a compatibility input rather than a special-case list.

Numeric-to-band thresholds are Living World policy, not claimed Paper/Minecraft constants. Initial thresholds are explicit in `ClimateProfileThresholds` and covered by tests so they can be tuned later without changing the Paper sampling boundary.

Base profiles use thermal and moisture bands. Seasonal influence is intentionally moderate: Primavera increases moisture, Verão warms and dries, Outono is neutral, and Inverno cools. Transient temperature/moisture shifts are clamped to ±2 bands and storm pressure to 0..2 so repeated events cannot accumulate unbounded climate state.

The output is a `ClimateSnapshot` with effective temperature, moisture and a weather tendency: clear, stable, precipitation or storm. This is policy, not an instruction to mutate the server immediately. Paper-side application, timing and environmental consequences belong to LW-021.

The first environmental-event slice translates non-stable tendencies into bounded weather plans. Defaults are 10 minutes of clear weather, 5 minutes of precipitation and 3 minutes of thunderstorm; no event may exceed 30 minutes. Stable climate deliberately produces no forced event.

`PaperWeatherController` is the narrow adapter that changes world weather and records the action in the server log. It uses only the public world-weather API; it does not scan chunks, blocks or entities. Runtime scheduling/triggering remains a separate composition concern so the adapter cannot accidentally become a background loop.

Runtime climate reaction is driven by logical calendar progress through the narrow `CalendarProgressListener` contract. The calendar owns time observation and persistence; climate only reacts to the resulting progress value. A multi-day advance is collapsed into one evaluation of the final date, so lag or a valid large skip cannot generate a burst of queued weather events.

The current transient climate state is stable/immutable. Evolution of anomalies (for example pressure building toward a storm) requires a separate task with explicit persistence/randomness semantics rather than hidden mutation inside the daily coordinator.

## Desire Lines

Desire Lines is event-driven. It observes only player movement events that actually enter a different block and ignores teleports. It does not use the deprecated/client-controlled `Player.isOnGround()`; instead it identifies the block geometrically below the destination feet position and exits immediately unless that surface is grass or dirt.

Traffic is sparse and bounded per chunk. The initial policy tracks at most 512 positions per chunk and saturates each score at the final wear threshold. Default progression is 12 visits for grass → dirt and 24 total visits for dirt → dirt path. These are Living World policy values and are configurable.

Tracked chunk data is persisted into that Chunk's namespaced PDC as compact integer pairs. Dirty ledgers are flushed every 200 ticks, on chunk unload, and on module disable. There is no recurring chunk/world scan: the periodic flush iterates only chunks that have actually accumulated tracked traffic in the current loaded session.

Recovery uses the same sparse ledgers and the logical calendar callback. A position touched since the previous logical-day transition is protected from one day of decay; untouched scores decay by one per day. Large valid day advances decay the additional elapsed days in one bounded pass. Only cached tracked chunks are visited. When a score crosses back below the path threshold, DIRT_PATH becomes DIRT; below the wear threshold, tracked DIRT becomes GRASS_BLOCK.

Natural dirt is not enrolled into Desire Lines merely by walking over it. A new ledger entry starts only on grass; dirt continues accumulating traffic only when that exact position is already owned by Desire Lines. This prevents recovery from converting unrelated natural dirt into grass.

## Double Doors

Double Doors listens only to main-hand right-click block interactions that are not denied for the interacted block. It ignores iron doors and schedules a one-tick reconciliation so the clicked door's actual post-vanilla open state is authoritative.

The pair finder checks only the four horizontal neighbors at the lower half. A candidate must use the same material and facing, the opposite hinge, and neither door may be powered. Synchronization occurs only when exactly one candidate matches, preventing rows/triples of doors from being coupled accidentally. The module updates both halves through modern `org.bukkit.block.data.type.Door` data; it does not use the legacy deprecated material-data API.

## Waystones

Waystone core intentionally separates identity/access/travel from the future physical block, GUI and economy. Each waystone has a stable UUID, bounded display name, owning world UUID and anchor block coordinates.

Loaded worlds are the registry owners. Each waystone is stored under its own namespaced World PDC key with a versioned compact binary payload; listing iterates loaded worlds and only this plugin's prefixed PDC keys, never chunks. Player activation IDs use the online Player PDC as packed UUID longs. OfflinePlayer PDC is deliberately not used for mutation because current Paper exposes it as a read-only view and warns that reads may involve blocking disk I/O.

Travel resolves only an activated waystone. It asynchronously requests/generates the destination chunk before reading destination blocks, then validates the world border, a solid/non-liquid anchor below the player, and passable/non-liquid feet/head spaces. The final teleport uses Paper `teleportAsync(..., PLUGIN)`; no synchronous destination chunk load is required by Living World.

The first player-facing slice uses a configurable physical anchor with `LODESTONE` as the default. Right-clicking an anchor with the main hand deterministically derives its UUID from world+coordinates, registers it on first contact, and activates it for the player. This makes identity stable across restarts without a scan.

The minimal command interface uses Paper's current `BasicCommand` API registered by the JavaPlugin: `/livingworld list` (alias `/lw list`) and `/lw travel <number>`. Numeric selection is intentionally simple and unambiguous for the first slice. GUI, custom naming, crafting recipe, economy and cooldown remain separate policies.

Travel through this physical interface additionally requires the configured anchor material to still exist at the stored coordinate; destroying/replacing the anchor makes the destination unsafe instead of silently teleporting to a stale point.

## Open design questions

- whether later messages need per-player locale in addition to the server default;
- which persistent store best fits waystones/world-memory data once their data model exists;
- whether Folia compatibility becomes a target. It is not assumed in Foundation v0.1.
