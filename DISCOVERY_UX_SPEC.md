# Living World — Discovery UX Specification

- **Status:** implemented foundation (M14)
- **Architecture:** `DISCOVERY_ARCHITECTURE.md`
- **Roadmap:** `DISCOVERY_ROADMAP.md`

This document defines the *experience* of a discovery in Living World. It deliberately does not specify the final animation, the sound, or any Blockbench-driven visual; that system is developed in a separate flow and consumes the contract defined in section 4.

## 1. Philosophy

> A discovery must be memorable, not merely informative.

Living World is a cooperative survival server, not a server hub. A `/warp` list rewards knowing the command; a discovery rewards having actually been there. The three rules that follow from that:

1. **Arrival beats menu.** A discovery happens because the player physically arrived somewhere, not because they opened an interface.
2. **The first time counts.** Re-visiting a place you already found is a pleasant return, not an achievement. Repeat events are silent.
3. **It is memory, not progress.** Nothing is consumed, unlocked or spent. Losing access to a Waystone (destroying its anchor) does not erase the memory that the player found it.

## 2. Personal discovery

**Definition:** the fact belongs to one player.

**Example:** João activates the Obelisco de Khnor. Only João is told about it. Paulo, standing three blocks away, is told nothing.

**Rules:**

- Triggered by the real interaction with the place, never by a command.
- Announced to the discovering player only.
- The player's own memory is durable: it survives logout, death and server restart.
- It grants nothing. It changes no access rule, no travel cost and no permission.

**Waystone case (implemented, M14):** activating a Waystone anchor for the first time. The existing chat feedback (`Waystone criada e ativada: …` / `Waystone ativada: …`) is unchanged, and a discovery moment is added on top of it.

Compatibility rule: Waystone access predates the Discovery feature. If a player already had access but has no corresponding Discovery record, the next physical interaction with that Waystone is treated as their one missing discovery moment. This is a backfill of memory, not a second activation.

## 3. World discovery

**Definition:** the fact belongs to the world and is shared by everyone who plays on it.

**Example:** the first player to find an ancient village makes it part of the world's memory.

**Rules:**

- Recorded once per world, in world-scoped storage, not in any player's data.
- Survives player logout and the discoverer never returning.
- The discovering player is the one who receives the moment at the time it happens.
- Broadcasting the moment to every online player is **not** part of this slice. It is a deliberate follow-up (M17) because "tell everyone" is a real balance and social decision, not a rendering detail.

Both scopes share one model and one event. The scope is declared by the discovery *type*, so a future landmark/village type is world-scoped from its first commit without changing the foundation.

## 4. Future presentation contract

A discovery produces exactly one presentation request, which is a pure value with no Bukkit type in it:

```text
DiscoveryPresentationRequest
    type      DiscoveryType   (waystone, biome, landmark, …)
    scope     DiscoveryScope  (PERSONAL | WORLD)
    title     String          localized
    subtitle  String          localized
```

Current output for a Waystone:

| Field | pt-BR | en-US |
| --- | --- | --- |
| `title` | `Descoberto` | `Discovered` |
| `subtitle` | `Obelisco de Khnor • Waystone` | `Khnor Obelisk • Waystone` |

The request is produced by a pure mapping (`DiscoveryPresentationPolicy`) from the domain event, so the wording is testable without a running server, and a future visual system can consume the same request to build its own richer output — longer titles, a book page, a sound cue, a landmark marker — without Discovery changing.

Design rules for any future presentation:

- One discovery produces at most one moment. No stacking, no queueing.
- The moment must never block gameplay input or delay travel.
- The moment is cosmetic. Disabling presentation (`discovery.presentation.enabled: false`) must leave discovery state exactly as correct.
- Copy is translatable and lives in the message catalog; PT-BR is canonical.

## 5. Relationship with the Title System

There is no Title System in Living World today, and this foundation does not create one. What it does is remove the reason to build one badly:

- A Title System, if it ever exists, is **a** `DiscoveryListener` (or several: one per discovery type). It is not a dependency of Discovery, and Discovery does not know it exists.
- Waystones do not depend on any title/announcement surface. A Waystone activation calls `DiscoveryService`; whether anything is rendered afterwards is a presentation decision made in the composition root.
- The same applies to the existing seasonal transition title (`PaperSeasonTransitionAnnouncement`): it is an independent `CalendarProgressListener`. Discovery does not reuse it and does not extend it, because a season change is world time, not a discovery.
- If several listeners are active, each owns its own client channel or is configured off. Living World will not build a priority/arbitration system until two surfaces genuinely compete for the same channel — exactly the rule already applied to HUD boss bars and the action bar.
- Discovery and the existing seasonal-transition announcement both currently use the Minecraft title channel. If they fire at the same instant, the last title sent replaces the previous one. This is a documented client-channel interaction, not a Discovery persistence/Waystone correctness issue; arbitration remains deferred until runtime evidence justifies it.

## 6. What the player sees today (M14)

| Situation | Feedback |
| --- | --- |
| First activation of a Waystone anchor | existing chat message + short title `Descoberto` / `<nome> • Waystone` |
| Re-clicking an already activated anchor | existing chat message only; no repeated discovery moment |
| First re-click of a legacy activated anchor with no Discovery record | existing "already activated" chat message + one discovery moment; later clicks are silent |
| Renaming a Waystone near its anchor | unchanged; the stored discovery is identity-only and cannot go stale |
| Breaking a registered anchor | unchanged; the memory of the discovery remains, the Waystone itself is removed |

## 7. Open UX questions, deliberately unanswered

- Should a world discovery be replayed to a player who joins later ("o mundo já conhece a vila antiga")? This needs a read model and a notification policy; not decided.
- Should the player have a journal/command to review discoveries? Blocked on ADR-002: reading a discovery cannot render a label from Discovery alone, so a reader must resolve subjects from their owning features. That is a real task, not a menu.
- Should returning to a place you already discovered say anything at all? Currently: nothing.
