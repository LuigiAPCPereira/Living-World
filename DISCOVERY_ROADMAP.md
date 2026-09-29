# Living World — Discovery Roadmap

No delivery dates are committed. Milestones describe dependency order and verified outcomes, not schedule promises. See `ROADMAP.md` for the product-wide milestone table; this file covers only the Discovery track.

## Milestone numbering

The original design brief for this track proposed `M10..M15` for discovery, but the canonical repository already owns **M10** for local climate readability, **M11** for weather readability and **M12** for seasonal ecology/environmental foundation. The imported Waystone navigation refinement is normalized to **M13**, and Discovery starts at **M14**. Historical validated milestones are not renumbered.

| Brief numbering | Repository milestone |
| --- | --- |
| M10 — discovery foundation | **M14 — Discovery foundation** |
| M11 — Waystone integration | **M14** (same block, see below) |
| M12 — biome titles | **M15 — Biome discovery** |
| M13 — landmarks | **M16 — World landmarks** |
| M14 — natural waystones / villages | **M17 — Collective discovery** |
| M15 — travel scrolls | **M18 — Travel documents** |

The foundation and the Waystone integration shipped in the same block because the integration is the smallest honest proof that the foundation is usable; splitting them would have left a foundation with no producer. Later milestones are deliberately separate.

## M14 — Discovery foundation + Waystone integration (implemented, not runtime-validated)

Outcome: a player can find a Waystone, Living World remembers that they found it, and the moment is rendered by an adapter that any future visual system can replace.

- Domain vocabulary and open type catalog (`DiscoveryType`, `DiscoveryId`, `DiscoveryScope`, `DiscoveryRecord`, `DiscoveryRegistry`).
- Single-owner use case (`DiscoveryService`), typed event fan-out (`DiscoveryEventPublisher` + `DiscoveryListener`).
- Per-player and per-world persistence ports with Paper PDC adapters.
- Presentation contract (`DiscoveryPresentationRequest`) + pure localized mapping + disableable Paper presentation.
- Waystone activation records a personal discovery; no existing Waystone behaviour changes.
- Documented: `DISCOVERY_ARCHITECTURE.md` (with ADR-001..003), `DISCOVERY_UX_SPEC.md`, `WAYSTONE_DISCOVERY_INTEGRATION.md`.

Validation state: automated gates green on the integration branch; **live Paper client smoke not yet run** (`LW-142`).

## M15 — Biome discovery

Outcome: entering/encountering a biome for the first time is a personal discovery.

Initial policy decision:

- Every biome with a stable namespaced key exposed by Paper counts, regardless of vanilla/datapack/modded namespace. M15 does not guess whether a biome is "natural enough" and does not maintain a curated allowlist.
- Durable identity is the full namespaced key (for example `minecraft:plains` or `terralith:yellowstone`), so equal path names in different namespaces remain distinct.
- Presentation may humanize the key path as a readable fallback, but that label is event data only and is never persisted. A future localization/catalog policy can improve labels without migrating discovery identity.
- If real servers reveal noisy/internal biome entries, filtering becomes a later evidence-driven policy instead of an assumption in the foundation.

Slices:

- **LW-150 — biome discovery contract:** declare `DiscoveryType("biome")`, personal scope, stable key→`DiscoveryId` mapping and readable fallback label; register the type in the composition root.
- **LW-151 — bounded Paper detector:** observe only real player block movement/teleport events, resolve the biome at the destination block, suppress repeated work while the player remains in the same biome and delegate first-time semantics to `DiscoveryService`. No biome scan and no per-tick polling.
- **LW-152 — runtime smoke:** prove first presentation, silence while remaining/re-entering an already-known biome, a second title for a genuinely new biome, custom namespaced biome handling when available, cleanup on quit/reconnect and clean logs. This smoke may remain deferred under `RUNTIME_VALIDATION_BACKLOG.md` while automated work continues.

- Reuse the existing title presentation; no new client surface.
- Depends on: M14.

## M16 — World landmarks

Outcome: significant places become part of the world's memory.

- New `DiscoveryType("landmark")` with `DiscoveryScope.WORLD`, exercising `WorldDiscoveryStore` for the first time.
- Producing feature stays separate from Discovery; the landmark rule owns detection, Discovery owns recording.
- Open question: how landmarks are identified without hard-coding a structure list.

## M17 — Collective discovery & broadcast

Outcome: the first genuine world-scope discovery, and the decision of what other players are told.

- Replay/broadcast policy for world discoveries, including players who were offline.
- Justifies a read model beyond the discovering player's moment.
- Open questions: replay on join, per-player notification budget, any world-wide gameplay consequence. These are product decisions with social/balance impact, not rendering details.

## M18 — Travel documents

Outcome: an in-world consumable that documents discovered places (scrolls/records), replacing pure menu travel as the high-context option.

- Reads personal discoveries; resolves labels from owning features (ADR-002).
- Must not become a teleport bypass that weakens Waystone identity/access rules.
- Depends on: M14, and realistically M15/M16 for a meaningful catalog.

## Explicitly out of the Discovery track

- Blockbench models, textures, animations, resource packs, and any backend coupling to them.
- Quests, dialogue, achievements-with-rewards, and stat tracking beyond what a future task explicitly promotes.
- Cross-server or cross-world-database discovery state.
