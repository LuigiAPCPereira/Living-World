# Living World — Waystone ↔ Discovery Integration

- **Status:** implemented (M11)
- **Architecture:** `DISCOVERY_ARCHITECTURE.md`
- **UX:** `DISCOVERY_UX_SPEC.md`

## 1. Principle

Waystones are *places*. Discovery is the *act of finding one*. Waystones therefore do not gain a discovery mode, a discovery flag, or a discovery-aware travel path. They gain exactly one call, in the place where the real interaction already happens.

Nothing about activation, access, travel, rename, destruction or cleanup changes.

## 2. Contracts involved

| Type | Layer | Role |
| --- | --- | --- |
| `WaystoneService` | `waystones.application` | unchanged: register / activate / travel eligibility |
| `WaystoneDiscovery` | `waystones.application` | the Waystone feature's own discovery declaration: type, scope, label key, id mapping |
| `DiscoveryService` | `discovery.application` | the use case owner: validate, persist, decide first-time, publish |
| `DiscoveryListener` | `discovery.application` | the only way anything reacts to a discovery |
| `PaperDiscoveryPresentation` | `discovery.paper` | renders the moment; disableable |

`WaystoneDiscovery` lives in the Waystone feature on purpose. The `"waystone"` discovery type, its personal scope, its label message key and the mapping `WaystoneId -> DiscoveryId` are Waystone-owned facts. If they lived inside Discovery, the foundation would start enumerating the features that use it.

## 3. Flow

```text
PlayerInteractEvent (main hand, anchor material)
        |
        v
PaperWaystoneModule.onAnchorInteract
        |
        +-- existing: find or create Waystone, register, rebuild anchor index
        |
        +-- existing: WaystoneService.activate(player, waystone)
        |        -> chat feedback (created-and-activated / activated / already-activated)
        |
        +-- new (only when activation is genuinely new):
                 DiscoveryService.discover(
                     type      = WaystoneDiscovery.TYPE,
                     id        = WaystoneDiscovery.idFor(waystone.id()),
                     owner     = player UUID,
                     discoveredBy = player UUID,
                     label     = waystone.name()
                 )
                        |
                        v
                 PaperPlayerDiscoveryStore.add(player, record)
                        |
                  first time? -> DiscoveryEventPublisher
                        |
                        v
                 PaperDiscoveryPresentation -> Adventure title
```

The call sits *after* the activation has already been decided and persisted, so:

- A failure in the discovery path cannot un-activate a Waystone.
- A player with a broken discovery presentation still gets a fully working Waystone.
- The Waystone module depends on Discovery's public application surface only; it never sees a store, a publisher, or a presentation class.

## 4. Behaviour matrix

| Event | Waystone behaviour | Discovery behaviour |
| --- | --- | --- |
| First interaction, unregistered anchor | creates + activates, green chat message | records `waystone/<uuid>` in that player's PDC, publishes, shows the moment |
| First interaction, existing Waystone never activated by this player | activates, green chat message | records + publishes + moment |
| Interaction with a Waystone this player already activated | yellow "already activated" message | no store write, no publish, no moment |
| `/lw rename` near the anchor | renames, keeps UUID and access | untouched — the stored discovery holds no name (ADR-002) |
| `/lw rename` far from the anchor | rejected | untouched |
| Breaking a registered anchor | removed from registry + index, red chat message | untouched; the memory of having found it remains |
| Recreating an anchor at the same coordinates | new UUID, new Waystone | new discovery id; the old record is inert history, exactly like a stale activation id |
| Travel via menu/command | unchanged | untouched; travel is not a discovery |

## 5. Events emitted by Waystones

None. Waystones emit no Bukkit event of their own and gain none. `DiscoveryUnlocked` is published by `DiscoveryEventPublisher` inside Discovery, not by the Waystone module, so the Waystone feature never learns what happens downstream.

## 6. Future extensions

Each of these is additive and requires no change to the current code path:

- **Travel as a discovery.** Travelling to a waystone a player has never visited would call the same `discover(...)` with the same type and id. It is deliberately *not* done now: a player who builds a waystone and immediately travels away already activated it, so travel cannot produce a first-time discovery in the current model. Any such rule must be a product decision, not an implementation detail.
- **Natural waystones / villages / landmarks (M14).** A new feature declares its own `DiscoveryTypeDefinition` with `DiscoveryScope.WORLD` and calls the same service. Waystones are not modified.
- **A discovery journal or `/lw discoveries`.** Implemented as a `DiscoveryListener`-independent read of `DiscoveryService.discoveries(owner, scope)` plus a lookup in the owning feature for labels (ADR-002). It would be a new command surface, not a change to this integration.
- **Title/biome rewards.** A listener that reacts to `DiscoveryUnlocked` by type. Waystones neither know nor care.
- **Landmark markers or minimap hints.** A listener that renders `DiscoveryPresentationRequest`. Still no coupling from Waystones.

## 7. What is explicitly not done here

- No discovery state on the Waystone record; the Waystone binary payload is untouched (v1 remains v1, no migration).
- No `OfflinePlayer` PDC access; discoveries are recorded for online players only, matching the existing activation store.
- No removal of a discovery when a Waystone is destroyed. Deleting history because a block was broken would contradict the "memory, not progress" principle, and stale ids are already inert.
- No Blockbench, model, animation, texture or resource-pack coupling of any kind.
