# Living World — Discovery Architecture

- **Status:** implemented foundation (M14)
- **Requirements:** `PRODUCT.md` (Discovery direction)
- **Architecture:** `DESIGN.md`
- **Roadmap:** `DISCOVERY_ROADMAP.md`
- **Code:** `src/main/java/dev/signalshards/livingworld/features/discovery/`
- **No Blockbench/model/asset/resource-pack coupling.** The visual Waystone system is developed in a separate flow. This module contains no model, texture, animation or resource-pack concept, and no code path may grow one without an explicit task.

## 1. Objective

Discovery is a first-class Living World capability: the durable memory that a player — or the world itself — has *found* something.

It exists so that future capabilities (biomes, landmarks, villages, ruins, natural waystones) can record a discovery without inventing their own notion of "the player saw this for the first time", and without a presentation system having to know which feature produced the event.

Discovery is not a teleport list, not a quest log, and not an achievement tracker. Waystones are *places*; a discovery is the *act of finding one*. Keeping those separate is what allows the same foundation to later hold a biome the player has never visited.

## 2. Responsibilities and boundaries

| Component | Responsibility | Does not own |
| --- | --- | --- |
| `features.discovery.domain` | identity and vocabulary of a discovery: type, id, scope, record | Paper types, persistence, presentation, gameplay effects |
| `features.discovery.application` | the single owner of the "a discovery happened" use case: validate, persist, decide first-time, publish | Bukkit, i18n text, feature-specific knowledge |
| `features.discovery.persistence` | ports for durable per-player and per-world discovery state | Paper `PersistentDataContainer`, encoding details |
| `features.discovery.paper` | PDC adapters, the Paper presentation of a discovery, module lifecycle | discovery rules, gameplay balance |
| `features.discovery.presentation` | the presentation *contract* (`DiscoveryPresentationRequest`) and the pure mapping from a domain event to localized text | Bukkit rendering, Adventure components |
| producing feature (e.g. `features.waystones`) | decides *what* was discovered and supplies the label it owns | discovery storage, discovery events |
| `LivingWorldPlugin` | composition root: builds registry, stores, service, presentation, wiring | any discovery rule |

Hard boundaries, enforced by package direction:

- **Discovery never depends on Waystones** (or on any other feature). `features.discovery` imports nothing from `features.*` other than itself.
- **Waystones depend only on the narrow public surface of Discovery's application layer** (`DiscoveryService`, `DiscoveryRequest`, and the domain value types). Waystones never touch Discovery persistence or presentation.
- **Waystones never depend on the Title System, HUD, or any other presentation surface.** A Waystone activation does not know that a title will be shown.
- **Domain and application code contain no Paper API**, following the same rule the calendar/climate layers follow.
- **No global singleton, no service locator, no generic event bus.** The publisher is a single typed list for one event, owned by the service.

## 3. Package layout

The requested layout was `domain / application / persistence / presentation`. Living World keeps that separation but names the Paper-facing layer `paper`, because every existing feature already places its Bukkit adapters there (`features.calendar.paper`, `features.waystones.paper`, `features.hud.paper`). Introducing a second name for the same layer would be a new convention for no gain.

```text
dev.signalshards.livingworld.features.discovery
├── domain
│   ├── DiscoveryType      open vocabulary: which kind of thing was discovered
│   ├── DiscoveryId        identity of the discovered thing inside its type
│   ├── DiscoveryScope     PERSONAL or WORLD
│   └── DiscoveryRecord    (id, scope, owner) — the durable fact
├── application
│   ├── DiscoveryTypeDefinition  type + scope + i18n label key (the policy)
│   ├── DiscoveryRegistry        the declared types; fail-closed lookup
│   ├── DiscoveryRequest         input of the use case
│   ├── DiscoveryOutcome         result: record + firstDiscovery
│   ├── DiscoveryUnlocked        the event value
│   ├── DiscoveryListener        narrow consumer port (one method)
│   ├── DiscoveryEventPublisher  typed fan-out with failure isolation
│   └── DiscoveryService         the use case owner
├── persistence
│   ├── PlayerDiscoveryStore     port (player PDC semantics)
│   └── WorldDiscoveryStore      port (world PDC semantics)
├── presentation
│   ├── DiscoveryPresentationRequest  the contract a future visual system consumes
│   └── DiscoveryPresentationPolicy   pure event -> localized request mapping
└── paper
    ├── PaperPlayerDiscoveryStore
    ├── PaperWorldDiscoveryStore
    ├── PaperDiscoveryPresentation
    └── PaperDiscoveryModule
```

`presentation` is Paper-free on purpose: it is the seam a future visual/Blockbench-driven system will consume without importing Bukkit.

## 4. Domain model

```text
DiscoveryType     "waystone"                open value, validated, lowercase
DiscoveryId       "<waystone uuid string>"  identity inside the type
DiscoveryScope    PERSONAL | WORLD
DiscoveryRecord   (DiscoveryId, DiscoveryScope, owner UUID)
DiscoveryRequest  (type, id, owner, discoveredBy, label)
```

Invariants:

- A discovery is identified by `(type, id)`. Identity is never coordinates and never a display name.
- `owner` is the player UUID for `PERSONAL` and the world UUID for `WORLD`.
- `PERSONAL` requires `owner == discoveredBy`; a personal discovery has exactly one discoverer. Violations fail closed at construction.
- A record stores **identity only**. It never stores a copy of the subject's name, so renaming a Waystone cannot leave a stale duplicate behind (see ADR-002).
- The *label* is supplied per call by the feature that owns the name, and is carried on the event for the presentation that happens at that moment. It is not persisted.

## 5. Flow

```text
Player right-clicks an anchor
        |
        v
PaperWaystoneModule  (existing activation path, unchanged)
        |
        v
WaystoneService.activate(...)          existing access rules, unchanged
        |
        v
DiscoveryService.discover(request)     new, additive
        |
        +--> DiscoveryRegistry.require(type)   fail closed on undeclared type
        +--> PlayerDiscoveryStore | WorldDiscoveryStore   (routed by declared scope)
        +--> firstDiscovery == false ? stop : publish
                |
                v
        DiscoveryEventPublisher --> DiscoveryListener
                |
                v
        PaperDiscoveryPresentation --> Adventure title
```

The event is the only way out of Discovery. A future Title System, quest log, statistics module or landmark module subscribes to the same `DiscoveryListener` port and needs no change to Discovery.

## 6. Events

`DiscoveryUnlocked(DiscoveryRecord record, UUID discoveredBy, String label)` is published **only on the first** unlock of a given `(type, id)` for its owner. Repeat activations are idempotent and silent — a player re-clicking a Waystone they already activated must not be re-congratulated.

`discoveredBy` exists because `PERSONAL` and `WORLD` need different audiences. For a personal discovery the audience is the player; for a world discovery the owner is the world, and the actor is the player who found it. Broadcasting a world discovery to all online players is deliberately **not** implemented yet (see `DISCOVERY_ROADMAP.md`, M17).

Listener failures are isolated: a broken presentation cannot roll back a discovery that is already durable, and cannot prevent other listeners from running. This mirrors the existing calendar-progress rule (a climate failure cannot roll back persisted calendar progress).

## 7. Persistence

Discovery state is split by ownership, not by convenience:

| Scope | Owner of the fact | Store | Paper container |
| --- | --- | --- | --- |
| `PERSONAL` | the player | `PlayerDiscoveryStore` | online `Player` PDC |
| `WORLD` | the world | `WorldDiscoveryStore` | `World` PDC |

Encoding uses one namespaced `STRING_ARRAY` per container (`discovery_learned`). The current payload starts with the explicit marker `@livingworld-discovery:v1`, followed by `(type, id)` pairs. The marker cannot collide with a valid `DiscoveryType`, and pairs avoid delimiter ambiguity when an id contains `:`.

Ordering is discovery order: the persisted array preserves insertion order, and load returns a `LinkedHashSet`-backed set. The original foundation briefly wrote headerless `(type, id)` pairs; the V1 decoder accepts that legacy shape and the next mutation rewrites it with the explicit marker. No timestamp is stored because nothing consumes age or ordering-by-date yet. If timestamp/history metadata becomes justified, it requires a new schema version rather than silently changing the V1 tuple width.

Mutation rules (deliberately asymmetric, and covered by tests):

- `add` on a container that is not available (player offline, world unloaded) **fails closed** with `IllegalStateException` — a discovery must never be silently dropped.
- `load` on an unavailable player **fails closed** for the same reason.
- `load` on an unloaded world returns an empty set: a world PDC is only reachable through a loaded world object. This is the one tolerant read, and it can only be reached by a caller that explicitly asks about an unloaded world.

Growth is proportional to the number of distinct things a player has discovered, the same policy already used for Waystone activations. A hard cap is deferred until real usage evidence requires one.

## 8. Extensibility

Adding a discovery type is three steps and touches no existing type:

1. Declare `DiscoveryTypeDefinition` (type, scope, label message key) in the composition root.
2. Give the producing feature an `id` mapping from its own identity to `DiscoveryId`.
3. Call `DiscoveryService.discover(...)` from the feature's real interaction path.

Example, biomes (future, M15):

```java
public static final DiscoveryTypeDefinition BIOME = new DiscoveryTypeDefinition(
        new DiscoveryType("biome"),
        DiscoveryScope.PERSONAL,
        "discovery.type.biome"
);
// id = the biome key, e.g. "minecraft:plains"
```

A landmark or village (future, M16) uses the same shape with `DiscoveryScope.WORLD` and therefore automatically routes to `WorldDiscoveryStore`.

Adding a new *presentation* (title, action bar, book, future visual system) means adding a `DiscoveryListener`. It does not change domain, application or persistence.

## 9. Decisions (ADR)

### ADR-001 — Personal and world discoveries are different capabilities, not a flag

**Status:** accepted.

**Context:** the request explicitly required supporting both "the player found it" and "the world has it" from the start, without deciding arbitrarily.

**Decision:** scope is part of the discovery record, is declared by the *type* rather than chosen by the caller, and determines the persistence owner (Player PDC vs World PDC). Only `PERSONAL` is produced today (Waystones).

**Consequences:** a type cannot be both personal and world in this slice. A player cannot personally "discover" a village, and a biome cannot become a shared world fact, without a deliberate design change. This is the safe failure direction: the cheaper mistake is to add a second type later rather than to migrate persisted records later. Both stores are implemented and tested now so the split is a verified contract, not a promise.

### ADR-002 — Discovery stores identity, never a copy of the subject's data

**Status:** accepted.

**Context:** Waystones are renameable (`/lw rename`). A discovery record containing a display name would go stale on rename and would duplicate state the Waystone registry already owns.

**Decision:** `DiscoveryRecord` holds `(id, scope, owner)` only. The label travels on the event, captured at unlock time by the feature that owns the name.

**Consequences:** reading discoveries back later cannot render a label from Discovery alone; a reader must resolve the subject from its owning feature. That is the correct direction of dependency, and it is why no read command is shipped in this slice.

### ADR-003 — The type registry is a policy catalog, not a service locator

**Status:** accepted.

**Context:** the request named a `DiscoveryRegistry`. A registry of "everything" would be a disguised service locator.

**Decision:** the registry is an immutable catalog of `DiscoveryTypeDefinition` — type, scope, i18n label key — and is the single owner of the scope policy. Undeclared types fail closed.

**Consequences:** the vocabulary of discoveries is open without editing the core, and the scope of a type cannot be contradicted by a caller. An enum of `BIOME/WAYSTONE/LANDMARK` was rejected: it hard-codes today's vocabulary into the core and would need a core edit for every future type.

### ADR-004 — Persisted discovery payloads are explicitly versioned

**Status:** accepted.

**Context:** Discovery is world/player memory expected to outlive individual plugin builds. The first implementation encoded only repeated `(type, id)` pairs. That shape is sufficient for V1, but changing tuple width later (for example, adding a timestamp) would be ambiguous without a schema discriminator.

**Decision:** every newly written payload begins with an impossible-to-collide schema marker, currently `@livingworld-discovery:v1`. Headerless even-length payloads are accepted as legacy V1 for compatibility. Unknown explicit versions fail closed instead of being guessed.

**Consequences:** future persistence evolution gets an intentional migration boundary. V1 stays minimal and stores no speculative metadata; adding fields later means introducing V2 plus a documented decoder/migration path.

## 10. Invariants and non-goals

- No world/chunk/entity scans; discovery is event-driven from feature interactions.
- No blocking I/O: PDC only, online players only, and no `OfflinePlayer` PDC reads (Paper warns those may block).
- Discovery never changes travel, access, rename or destruction rules of any feature.
- Not in this slice, by design: read commands, quest/journal UI, statistics, rewards, broadcasting world discoveries, biomes, landmarks, villages, ruins, and any model/animation/resource-pack asset.
