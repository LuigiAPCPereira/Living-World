# Living World — Discovery Roadmap

No delivery dates are committed. Milestones describe dependency order and verified outcomes, not schedule promises. See `ROADMAP.md` for the product-wide milestone table; this file covers only the Discovery track.

## Milestone numbering

The original design brief for this track proposed `M10..M15` for discovery. This repository had already used **M10** for *Waystone navigation clarity* (`LW-100`/`LW-101`), and renaming a validated milestone would corrupt the task inventory. The Discovery track therefore starts at **M11** and keeps its own order:

| Brief numbering | Repository milestone |
| --- | --- |
| M10 — discovery foundation | **M11 — Discovery foundation** |
| M11 — Waystone integration | **M11** (same block, see below) |
| M12 — biome titles | **M12 — Biome discovery** |
| M13 — landmarks | **M13 — World landmarks** |
| M14 — natural waystones / villages | **M14 — Collective discovery** |
| M15 — travel scrolls | **M15 — Travel documents** |

The foundation and the Waystone integration shipped in the same block because the integration is the smallest honest proof that the foundation is usable; splitting them would have left a foundation with no producer. Later milestones are deliberately separate.

## M11 — Discovery foundation + Waystone integration (implemented, not runtime-validated)

Outcome: a player can find a Waystone, Living World remembers that they found it, and the moment is rendered by an adapter that any future visual system can replace.

- Domain vocabulary and open type catalog (`DiscoveryType`, `DiscoveryId`, `DiscoveryScope`, `DiscoveryRecord`, `DiscoveryRegistry`).
- Single-owner use case (`DiscoveryService`), typed event fan-out (`DiscoveryEventPublisher` + `DiscoveryListener`).
- Per-player and per-world persistence ports with Paper PDC adapters.
- Presentation contract (`DiscoveryPresentationRequest`) + pure localized mapping + disableable Paper presentation.
- Waystone activation records a personal discovery; no existing Waystone behaviour changes.
- Documented: `DISCOVERY_ARCHITECTURE.md` (with ADR-001..003), `DISCOVERY_UX_SPEC.md`, `WAYSTONE_DISCOVERY_INTEGRATION.md`.

Validation state: automated gates green; **live Paper client smoke not yet run** (`LW-112`).

## M12 — Biome discovery

Outcome: entering a biome for the first time is a personal discovery.

- New `DiscoveryType("biome")`, `DiscoveryScope.PERSONAL`, own id mapping (biome key).
- Event-driven from movement, reusing the existing bounded "block actually changed" pattern from Desire Lines; no biome scan, no per-tick work.
- Reuse the existing title presentation; no new client surface.
- Depends on: M11.
- Open question: which biomes count (natural vs generated, modded vs vanilla). Needs a decision, not an assumption.

## M13 — World landmarks

Outcome: significant places become part of the world's memory.

- New `DiscoveryType("landmark")` with `DiscoveryScope.WORLD`, exercising `WorldDiscoveryStore` for the first time.
- Producing feature stays separate from Discovery; the landmark rule owns detection, Discovery owns recording.
- Open question: how landmarks are identified without hard-coding a structure list.

## M14 — Collective discovery & broadcast

Outcome: the first genuine world-scope discovery, and the decision of what other players are told.

- Replay/broadcast policy for world discoveries, including players who were offline.
- Justifies a read model beyond the discovering player's moment.
- Open questions: replay on join, per-player notification budget, any world-wide gameplay consequence. These are product decisions with social/balance impact, not rendering details.

## M15 — Travel documents

Outcome: an in-world consumable that documents discovered places (scrolls/records), replacing pure menu travel as the high-context option.

- Reads personal discoveries; resolves labels from owning features (ADR-002).
- Must not become a teleport bypass that weakens Waystone identity/access rules.
- Depends on: M11, and realistically M12/M13 for a meaningful catalog.

## Explicitly out of the Discovery track

- Blockbench models, textures, animations, resource packs, and any backend coupling to them.
- Quests, dialogue, achievements-with-rewards, and stat tracking beyond what a future task explicitly promotes.
- Cross-server or cross-world-database discovery state.
