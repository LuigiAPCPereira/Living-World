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

Outcome: entering a biome for the first time is a personal discovery.

- New `DiscoveryType("biome")`, `DiscoveryScope.PERSONAL`, own id mapping (biome key).
- Event-driven from movement, reusing the existing bounded "block actually changed" pattern from Desire Lines; no biome scan, no per-tick work.
- Reuse the existing title presentation; no new client surface.
- Depends on: M14.
- Open question: which biomes count (natural vs generated, modded vs vanilla). Needs a decision, not an assumption.

## M16 — World landmarks

Outcome: significant generated places become part of the world's memory.

Initial policy decision:

- Landmark eligibility is declarative through the Paper/Minecraft structure-registry tag `#livingworld:landmarks`. M16 does not hard-code vanilla structure names and accepts entries from any namespace supplied by the server/datapacks.
- If `#livingworld:landmarks` is absent or empty, the feature is safely inert. The plugin checks `Registry#hasTag` before resolving it.
- A placed landmark instance receives one plugin-owned UUID in the `GeneratedStructure` PDC on first eligible encounter. Paper exposes that PDC from the underlying `StructureStart` and persists it in structure NBT, so Discovery identity does not depend on coordinates or visible names.
- The Discovery owner is the world UUID because `landmark` uses `DiscoveryScope.WORLD`. Durable `DiscoveryId` is the instance UUID; the structure registry key is only non-persistent label/context.
- Detection is bounded and event-driven: on real block movement/teleport, inspect only the destination chunk's `Chunk#getStructures()`, filter structures by `#livingworld:landmarks`, and accept only generated structures whose bounding box contains the destination position.
- M16 never calls `locateNearestStructure`, never performs a radius search, never scans unloaded chunks and never force-loads chunks.

Slices:

- **LW-160 — landmark discovery contract:** declare `DiscoveryType("landmark")`, WORLD scope, UUID→`DiscoveryId` mapping and readable structure-key fallback label; document `#livingworld:landmarks` as the eligibility contract.
- **LW-161 — Paper landmark identity/resolver:** resolve the structure tag dynamically, treat missing/empty tag as no-op, read-or-create one UUID in eligible `GeneratedStructure` PDC and expose a small resolved-landmark value without leaking Paper into Discovery core.
- **LW-162 — bounded landmark-entry detector:** observe only real block movement/teleport, inspect the destination chunk only, bounding-box test eligible generated structures, suppress repeated work while the player remains inside the same instance set and delegate first-world-discovery semantics to `DiscoveryService`.
- **LW-163 — runtime smoke:** prove first world-scope landmark discovery, silence while remaining/re-entering the same landmark, distinct discovery for another eligible instance, persistence across restart/reconnect, safe behavior with missing tag and clean logs. This smoke may remain deferred under `RUNTIME_VALIDATION_BACKLOG.md` while independent automated work continues.

- Producing feature stays separate from Discovery; the landmark rule owns detection/identity, Discovery owns recording/event semantics.
- Depends on: M14.

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
