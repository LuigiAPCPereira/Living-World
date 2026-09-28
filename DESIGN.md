# Living World — Architecture

- **Observed baseline:** Paper plugin, Java 25, Gradle, package `dev.signalshards.livingworld`.
- **Requirements:** `PRODUCT.md`.
- **Environmental specialization:** `ECOLOGY_AND_SEASONS.md`.
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
| `features.discovery.domain` | discovery identity, scope and durable record vocabulary | Paper, persistence adapters, feature-specific detection |
| `features.discovery.application` | first-discovery use case, type registry and typed discovery event publication | Bukkit, feature-specific rules, client rendering |
| `features.discovery.persistence` / `.paper` | narrow stores plus Player/World PDC adapters and optional Paper presentation | Waystone access/travel state or discovery policy |
| `features.desirelines.domain` | traffic thresholds and wear stages independent from Paper | movement events, chunks or block mutation |
| `features.desirelines.application` | bounded sparse per-chunk traffic ledger | Paper PDC or block types |
| `features.desirelines.paper` | movement observation, block wear and Chunk PDC persistence | global world scanning or unrelated QoL rules |
| `features.qol.doubledoors.paper` | detects an unambiguous adjacent compatible door and mirrors manual open state | redstone automation or arbitrary nearby doors |
| `features.waystones.domain` | stable waystone identity, display name and anchor coordinates | Paper storage, GUI or teleport APIs |
| `features.waystones.application` | registration, activation visibility and travel result contracts | player/world PDC details |
| `features.waystones.paper` | World/Player PDC adapters, safe destination validation and async Paper teleport | economy, GUI or physical activation presentation |
| future `features.hud.paper` | owns player-facing boss bars/action bar lifecycle and renders read-only projections of calendar/climate/navigation state | calendar/climate rules or persistent gameplay state |
| `features.ecology.domain` | pure suitability/acceptance/visual policies for ecological reactions from effective climate and season | Paper events, random source or block mutation |
| `features.ecology.paper` | adapts bounded Paper events/player-local triggers into ecology decisions | world scanning, forced global simulation or duplicated climate rules |
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

Season-transition presentation is a narrow `CalendarProgressListener`. When a progress value carries `SeasonTransition`, the Paper adapter optionally sends one short Adventure title to currently online players using the new season's semantic color and the resulting Living World date as subtitle. It owns no scheduler or persistence and is disabled with `seasons.transition-announcement.enabled`.

## Local climate readout

`/lw climate` is a read-only explanation layer over the existing climate/ecology rules. `PaperLocalClimateReadoutProvider` resolves the player's effective `ClimateSnapshot`, current season and the same apparent-temperature policy used by the HUD. It then evaluates the already-configured crop/tree/grass growth, farmland retention, fire spread and frozen-surface policies with their current strengths/enabled flags. The command displays those actual percentages and state; it does not introduce thresholds, scans, polling, persistence or a second balance model.

Player exposure (water/fire/lava) for apparent temperature is shared between HUD and climate readout through `PaperTemperatureExposureResolver`, preventing presentation drift between the two surfaces.

Weather readability deliberately separates **tendency** from **applied event**. `/lw climate` displays the local `WeatherTendency` already present in its `ClimateSnapshot`; it is not described as a guaranteed forecast because the daily world weather coordinator uses a bounded representative world sample. Separately, `PaperWeatherEventAnnouncement` runs only after `PaperWeatherController.apply(...)` succeeds and emits one short message to players in that world. Stable/no-plan climate remains silent, and announcements can be disabled via `climate.weather-events.player-announcements`.

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

The first player-facing slice uses a configurable physical anchor with `LODESTONE` as the default. Right-clicking an unregistered anchor creates a random persistent UUID and activates it for the player. Existing anchors are found from the loaded-world registry by world+coordinates; this keeps previously persisted waystones compatible without making coordinates the permanent identity.

Breaking a registered anchor immediately removes its registry entry and sends translated feedback to the breaker. Because a replacement anchor receives a new UUID, stale player discovery IDs cannot silently resurrect access to a destroyed waystone. Player lists already derive from currently registered waystones, so destroyed entries disappear immediately from `/lw list`.

Legacy orphan cleanup is chunk-bounded and never forces a chunk load. On module enable, Living World builds an in-memory index from registered waystones and validates only anchors whose chunks are already loaded. Later `ChunkLoadEvent` callbacks inspect only registered anchors indexed for that naturally loaded chunk. Missing anchors are removed from the registry; stale player activation UUIDs remain harmless because listings intersect current registry entries.

The command interface uses Paper's current `BasicCommand` API registered by the JavaPlugin: `/livingworld list` (alias `/lw list`), `/lw travel <number>`, `/lw rename <number> <name>` and operator `/lw status`. Numeric selection remains intentionally simple. Rename rewrites the existing version-1 payload under the same Waystone UUID, so player activation IDs and identity remain stable; no persistence migration is required.

Rename is intentionally local rather than remote: the player must have activated the Waystone and be within the configured default 6-block radius of its anchor. Names are global/shared for the Waystone. This avoids introducing an ownership schema solely for naming while preventing arbitrary remote renames.

The first visual Waystone menu is a thin Paper presentation adapter. `/lw menu` creates a titled inventory with one anchor-material icon per activated Waystone, maps each slot directly to its stable `WaystoneId`, cancels click/drag mutations, revalidates activation when clicked, closes the inventory, and delegates to the existing `PaperWaystoneTravelService`. It never reimplements travel safety or access policy.

For navigation clarity, the menu derives a presentation-only ordering from the player's current position: destinations in the current world come first by 3D distance to the anchor center, then cross-world destinations follow alphabetically. Item lore exposes target world and coordinates; same-world entries include rounded distance while cross-world entries explicitly avoid pretending that Euclidean distance across dimensions is meaningful. The application service keeps its stable alphabetical activated-waystone listing for command numbering, so visual ordering does not redefine domain/access state.

The menu caps at 54 destinations in the first slice. More than 54 produces explicit fallback guidance to `/lw list`; destinations are never silently truncated. Pagination is deferred until real usage demonstrates the need.

Travel through this physical interface additionally requires the configured anchor material to still exist at the stored coordinate; destroying/replacing the anchor makes the destination unsafe instead of silently teleporting to a stale point.

## Discovery

Discovery is an independent world-memory capability under `features.discovery`. Its domain/application layers contain no Paper API and do not depend on Waystones. Producers depend only on the narrow Discovery application contracts.

The first integration is additive to physical Waystone interaction: after the existing activation path, `PaperWaystoneModule` asks `DiscoveryService` to record the Waystone identity for the player. The call is intentionally independent from whether activation was newly granted, so players with legacy Waystone access receive a one-time memory backfill on their next physical interaction. Repeated interactions are idempotent.

Discovery stores identity only, with versioned PDC payloads. Waystone rename therefore cannot stale a Discovery record, Waystone destruction does not erase historical memory, and travel is not itself a discovery. Detailed ownership, persistence, ADRs and UX are in `DISCOVERY_ARCHITECTURE.md`, `DISCOVERY_UX_SPEC.md` and `WAYSTONE_DISCOVERY_INTEGRATION.md`.

## Future Player HUD

HUD must be an adapter/read-model layer, not a new source of truth. Calendar boss bars read the logical calendar/season state; navigation reads player position/yaw; temperature reads a dedicated apparent-temperature policy derived from the existing climate inputs.

The first HUD layout uses exactly one persistent boss bar for calendar/season. Navigation, coordinates and temperature share one composed action bar instead of creating additional boss bars. Each segment can be disabled independently.

HUD color is semantic rather than decorative noise. The calendar title colors the season while the boss-bar fill also changes by season; action-bar heading is aqua, coordinates are neutral gray, separators are dark gray, and temperature changes color by degree band. Player-facing Waystone messages use the same Adventure component approach with consistent success/info/warning/error tones instead of legacy chat color codes.

Paper exposes Adventure natively, so HUD implementation should prefer `Component`, `Audience.showBossBar/hideBossBar` and `Audience.sendActionBar` rather than legacy string/Bungee APIs. A single HUD module should own visibility/update lifecycle for these channels to prevent calendar, climate and navigation features from independently overwriting one another.

Boss bars are persistent mutable UI objects and should be created per player/surface, updated only when their rendered state changes, and hidden on disable/player quit. Action bar is a shared transient channel, so temperature must be configurable and refreshed at a bounded cadence; future integrations with other plugins may require a suppression/priority policy rather than unconditional writes every tick.

Numeric degrees require a named policy such as `ApparentTemperaturePolicy`. The policy may use coordinate temperature/humidity, season and bounded climate anomalies, but its output is Living World gameplay temperature rather than a claim that Paper's biome-temperature scalar maps canonically to Celsius.

Initial apparent-temperature policy is intentionally explicit and simple: Paper temperature 0.8 maps to 15 °C before seasonal adjustment; one Paper temperature unit maps to 20 °C; Primavera/Verão/Outono/Inverno apply +2/+6/0/-6 °C; final output is clamped to -40..55 °C. These are Living World gameplay constants, covered by tests and expected to be tuned from playtest evidence.

## Ecology

The first ecological integration hooks only vanilla natural growth. Paper's `BlockGrowEvent` is cancellable and is fired for natural block growth, so Living World can reduce growth success without polling or creating an independent plant scheduler. The Paper adapter will ignore non-`Ageable` growth in the first slice and will not handle explicit fertilization events.

For each relevant event, the adapter samples temperature/humidity only at that block coordinate, classifies the local base profile, applies the existing season-aware `ClimatePolicy`, then asks a pure growth-suitability policy for an acceptance probability. A small injected random source decides whether to cancel the event. This keeps tests deterministic and confines randomness to the adapter boundary.

The initial ecology contract only slows growth in unsuitable conditions; suitability never exceeds 1.0, so Living World does not grow crops faster than vanilla. This keeps the first balance change bounded and reversible.

Natural sapling-to-tree attempts reuse the same suitability policy through Paper's cancellable `StructureGrowEvent`. The adapter handles only events whose origin block is a `Sapling`, and explicitly bypasses `isFromBonemeal()` events. Huge mushrooms and other organic structures therefore remain vanilla in this slice.

Crop and tree growth use separate ecological strengths. Initial defaults are 0.65 for Ageable crops and 0.35 for natural sapling-to-tree attempts. This tuning came directly from live gameplay: applying crop-strength penalties to already-sparse tree attempts made hot-biome saplings feel excessively slow.

Local effective climate calculation is shared through `PaperLocalClimateResolver`: one block-coordinate temperature/humidity sample, the current logical season, and stable bounded climate state. Ecology consumers must use this resolver instead of duplicating climate classification rules.

Frozen-surface ecology reacts only to vanilla block-condition events. Natural WATER→ICE and AIR/SNOW→SNOW `BlockFormEvent` transitions are allowed only when effective temperature is FRIO/CONGELANTE. `EntityBlockFormEvent` and `BlockSpreadEvent` are explicitly ignored so Frost Walker, snow golems and unrelated formation/spread mechanics are untouched. `BlockFadeEvent` for ICE/SNOW is cancelled while effective climate remains FRIO/CONGELANTE and otherwise left to vanilla.

`BlockFadeEvent` does not carry creation provenance, so cold-climate preservation applies to any ICE/SNOW block that vanilla attempts to fade, including player-placed frozen surfaces. This limitation is explicit; Living World does not add global tracking merely to recover provenance.

Ground-cover ecology listens to `BlockSpreadEvent` but accepts only the exact vanilla shape source=GRASS_BLOCK, destination-current=DIRT, destination-new=GRASS_BLOCK. That narrow predicate deliberately excludes fire, fungi, vines, bamboo and other spread mechanics. Accepted grass spread reuses the existing climate suitability policy with an independent default strength of 0.50; Living World never forces or accelerates spread.

Farmland moisture ecology listens only to cancellable `MoistureChangeEvent` transitions whose current/new block data are both `Farmland` and whose moisture value decreases. Effective cold/humid climate can cancel a bounded fraction of those vanilla drying steps through a pure retention policy (default strength 0.70). Moisture increases always remain vanilla, and hot/dry climate receives no artificial extra drying.

Fire ecology listens only to cancellable `BlockIgniteEvent` events whose cause is exactly `SPREAD`. The target block's effective climate feeds a pure fire-spread suitability policy (default strength 0.65). Hot/dry conditions can preserve the vanilla acceptance chance while cold/wet conditions reject some spread attempts. FLINT_AND_STEEL, LAVA, LIGHTNING, FIREBALL, ARROW, EXPLOSION and other non-SPREAD causes are untouched.

## Runtime diagnostics

Performance work follows measurement rather than speculation. The first M6 spark baseline held 20 TPS with 10-second tick durations of 2.8/4.4/5.9/13.7 ms (min/median/p95/max) and one-minute values of 2.8/4.3/6.2/24.1 ms under a short elevated-random-tick profile. This does not justify hot-path optimization by itself.

`/lw status` is a read-only diagnostic surface available to both players and console. A narrow core status snapshot is assembled in the composition root from existing module read models; modules do not depend on one another. It reports plugin/world/calendar state, online/HUD counts, Desire Lines cache size, registered Waystones and Paper TPS/MSPT. Living World intentionally does not expose plugin hot-reload; config changes require restart because server/plugin reload is unsafe and deprecated in Paper.

Module shutdown is best-effort across every enabled module. `ModuleManager.disableAll()` continues in reverse order after individual failures, returns the first failure with later failures suppressed, and clears its enabled set. The plugin boundary catches/logs that aggregate during `onDisable` so a cleanup failure is visible without aborting the remainder of Bukkit's plugin shutdown sequence.

Player apparent temperature is biome/coordinate temperature plus season and a bounded direct-exposure modifier. The first microclimate slice uses only Paper's player-local state: lava adds +20 °C, burning adds +8 °C and water subtracts 4 °C, with priority lava → water → fire → neutral so contradictory states are not stacked. The result still respects the HUD clamp of -40..55 °C.

Nearby heat-source influence such as merely standing beside lava or campfires remains deferred. Supporting that would require a cached/bounded sampling design; the HUD must not scan an area around every player on each refresh.

## Open design questions

- whether later messages need per-player locale in addition to the server default;
- which persistent store best fits waystones/world-memory data once their data model exists;
- whether Folia compatibility becomes a target. It is not assumed in Foundation v0.1.

## M12.4 — Folhas sazonais

`PaperSeasonalLeavesModule` observa movimento posicional normal em MONITOR, ignora cancelamentos/teleportes/rotação e limita tentativas a uma por jogador a cada 5 segundos (relógio monotônico). Não agenda tarefas. Consulta no máximo nove posições fixas de copa, até seis blocos acima do jogador, somente em chunks carregados e dentro da altura válida. A primeira folha encerra a busca, mesmo se a chance rejeitar o efeito. Quit e disable limpam o estado transitório. Apenas o jogador do evento recebe até três partículas; não há alteração de blocos.

`SeasonalLeafVisualPolicy` decide o efeito e usa `NaturalGrowthSuitabilityPolicy` com categoria TREE, força 1 e o modificador sazonal existente. Chance visual = 0,35 × min(1, chance ecológica); esse fator visual não altera configurações/regras de crescimento. Primavera usa CHERRY_LEAVES; outono PALE_OAK_LEAVES; inverno SNOWFLAKE apenas em FRIO/CONGELANTE e fora de ARIDO/SECO; verão não emite. Folhas decorativas também podem participar; detecção de árvores naturais não faz parte desta fatia.

## M12.5 — Environmental & Thermal Foundation (direção aceita)

A próxima evolução não adiciona efeitos de forma isolada. Ela introduz uma fundação ambiental explícita:

```text
Calendar -> Season -> ClimateSnapshot -> Environmental State
                                     |-> AmbientTemperature
                                     |-> PlayerThermalState
                                     |-> Ecology policies
                                     |-> Visual Projection
                                     +-> Physical Ecology
```

`AmbientTemperature` descreve o ambiente e pode governar neve/gelo/ecologia. `PlayerThermalState` descreve a exposição corporal e pode considerar umidade, abrigo, atividade, isolamento e fontes térmicas. A migração deve preservar o contrato atual da temperatura aparente do HUD até haver uma tarefa específica e testes.

Primeiro slice implementado de LW-122: `AmbientTemperaturePolicy` agora possui a calibração base Paper + season; `ApparentTemperaturePolicy` consome `AmbientTemperature` e só então aplica a exposição legada do jogador (`WATER`, `FIRE`, `LAVA`) e o clamp de HUD. `PaperLocalClimateResolver` expõe `ambientTemperatureAt(Block)`. Portanto água/fogo/lava não alteram mais conceitualmente a temperatura ambiental, mesmo que o HUD preserve os resultados históricos durante a migração.

O modelo térmico segue um princípio Vanilla+: condições ambientais definem o cenário; microclima/exposição definem a taxa de troca; equipamento/isolamento/wetness/atividade modulam essa taxa; inércia ao longo do tempo produz o estado corporal. Armadura não soma graus diretamente. Água/submersão, vento, abrigo, atividade, fontes como torch/campfire/lava e estados vanilla como powder snow entram como contribuições ao mesmo modelo, não como sistemas paralelos. Teleporte muda o ambiente imediatamente sem zerar a inércia corporal; respawn limpa estado corporal transitório. Detalhes/casos estão em `ECOLOGY_AND_SEASONS.md`.

Segundo slice de LW-122: `PlayerThermalState` é um índice corporal interno normalizado em `[-1, +1]`, independente de Celsius; `ThermalExchangeRate` fornece a taxa líquida por segundo; `PlayerThermalPolicy` integra essa taxa usando tempo decorrido e classifica o resultado com `PlayerThermalThresholds` em bandas de `EXTREME_COLD` a `EXTREME_HEAT`. O integrador não conhece Paper nem a origem da taxa, permitindo que água, vento, atividade, isolamento e fontes térmicas sejam compostos depois sem duplicar a inércia corporal.

Terceiro slice de LW-122: wetness também é estado temporal próprio. `WetnessState` guarda `[0,1]`, `WetnessRate` define a variação líquida por segundo e `WetnessPolicy` integra/satura. Molhamento não altera `AmbientTemperature`; ele será um dos fatores que modificam a troca térmica corporal.

Quarto slice de LW-122: `WaterExposure` modela fração submersa + profundidade, enquanto `WaterExposurePolicy` produz `WetnessRate` e `WaterThermalTransferFactor`. A fração submersa controla contato; profundidade adiciona apenas um bônus bounded de transferência. O fator térmico é sem sinal e não gera `ThermalExchangeRate` diretamente enquanto `WaterTemperature` não existir, evitando codificar a hipótese incorreta de que toda água sempre resfria.

Quinto slice de LW-122: `WaterTemperature` separa a temperatura da água da temperatura do ar. `WaterTemperaturePolicy` usa somente `AmbientTemperature` + profundidade e parâmetros bounded: superfície acompanha parcialmente o ambiente; água profunda tende a uma referência estável; temperatura de água líquida é clampada ao intervalo configurado. Não existe cache/persistência por corpo d'água neste estágio e nenhum nome de bioma é necessário.

Sexto slice de LW-122: `WaterThermalExchangePolicy` converte `WaterTemperature` + `WaterThermalTransferFactor` + `PlayerThermalState` em `ThermalExchangeRate`. O cálculo usa um equilíbrio-alvo normalizado e converge em direção a ele com resposta/limite configuráveis. Assim a direção da troca depende da relação entre água e estado corporal, não de um modificador fixo como `water = -4 °C`.

Sétimo slice de LW-122: `AirThermalExchangePolicy` aplica a mesma ideia ao ar. `AmbientTemperature` define o equilíbrio-alvo, `WetnessState` amplifica a velocidade de troca e `AirThermalTransferFactor` deixa espaço explícito para vento/abrigo posteriores. `ThermalExchangeRate.plus(...)` permite compor contribuições independentes (ar, água, atividade, fontes locais) sem introduzir um gerenciador global.

Oitavo slice de LW-122: `WindExposure` e `ShelterFactor` alimentam `AirTransferPolicy`, que produz um `AirThermalTransferFactor` bounded. O domínio só conhece intensidades normalizadas; detectar céu aberto, vento, altitude ou geometria de abrigo fica para adapters Paper bounded/cached. Nenhum algoritmo tenta classificar semanticamente uma construção como “casa”.

Nono slice de LW-122: `PlayerActivity` + `ActivityThermalPolicy` adicionam calor metabólico pequeno e composável por `ThermalExchangeRate`. A atividade não altera ambiente, water temperature nem vento. `GLIDING` representa apenas esforço corporal baixo; eventual convecção de Elytra entra por `WindExposure`.

Plugin, datapack e resourcepack seguem ownership unidirecional: o plugin controla comportamento; datapack fornece dados Minecraft-native declarativos; resourcepack fornece apresentação. Nenhum deles mantém season/climate paralelo. Schemas/manifests devem permitir verificar compatibilidade sem exigir versões textuais idênticas.

Compatibilidade ambiental deve funcionar por propriedades observáveis/namespaced fallback e não por uma tabela obrigatória de nomes. O target de smoke é Terralith + Tectonic (Overworld), Incendium (Nether) e Nullscape (End). Perfis específicos refinam precisão; namespace desconhecido não quebra a simulação.

Feedback térmico deve progredir de frio perceptível a congelamento severo, incluindo cold breath e frost. Uso de freeze ticks vanilla é um spike: apresentação de frost e dano térmico permanecem conceitos separados para evitar dano acidental causado apenas por feedback visual.

Visual sazonal amplo pode ser projetado ao cliente; estado com colisão/gameplay, como gelo caminhável e snow layers físicas, deve existir no servidor. Biomas auxiliares sazonais/packets são hipótese técnica a validar antes de compromisso com NMS/registry machinery.

Performance permanece bounded: nenhum scan recorrente global; trabalho por jogador/região possui budget; mudanças são preferencialmente event/delta-driven com cache/invalidação; acesso Bukkit permanece thread-safe; async é reservado a trabalho puro quando medição justificar.

O desenho completo, benchmarks de referência, transições sazonais e critérios de M12.5 estão em `ECOLOGY_AND_SEASONS.md`.
