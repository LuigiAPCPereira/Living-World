# Project Agent Instructions — Living World

## Protocol and engineering authority

This repository uses the Agent Development Protocol snapshot in `DOCUMENTATION_AND_CONTINUITY.md` and the engineering principles in `ENGINEERING_DNA.md`.

- Protocol version: 2.2
- Snapshot verified in this repository on 2026-09-26.
- `DOCUMENTATION_AND_CONTINUITY.md` SHA-256: `c0f9077fc37f0ca33243a107739153aa2578638923ee1df189fa26582311df78`
- `ENGINEERING_DNA.md` SHA-256: `461427c820cd4ebf746d1f2031fee3a8af374566ae2c31de55549d0e58203672`
- External/Notion publication state is not assumed from this repository snapshot.

Before substantial work, recover `PRODUCT.md`, `DESIGN.md`, `TASKLIST.md`, `ROADMAP.md`, and `PROJECT_STATE.md`, then inspect the real Git/build state.

Recognize the protocol commands `<novo_projeto>`, `<adotar_protocolo>` / `<adaptar_protocolo>`, `<continuar>`, `<sincronizar>`, `<status>`, and `<encerrar>` only when invoked by the user.

## Product

**Living World** is a server-side Paper/Purpur plugin that makes survival worlds feel more alive, reactive, convenient, and evolutionary without replacing world generation.

Primary directions include time/calendar/seasons, climate and environmental events, world memory such as desire lines, waystones, and focused quality-of-life mechanics.

Heavy custom world generation, large structure systems, and recreation of projects such as Terralith/Tectonic are explicitly outside the plugin's responsibility.

## Current scope

The active scope is the project foundation described in `PRODUCT.md` and `TASKLIST.md`.

Current task: **LW-050 — climate-sensitive natural growth policy**.

Do not implement later roadmap modules opportunistically.

## Architecture rules

- Keep `LivingWorldPlugin` as a small composition root.
- Organize code by ownership/feature, not generic drawers such as `utils` or `helpers`.
- Core code may define narrow shared contracts; feature modules depend on those contracts, not on other feature internals.
- Do not add a dependency-injection framework, generic event bus, service locator, or plugin-within-plugin framework without demonstrated need.
- Use Paper public APIs by default. NMS/internal Minecraft APIs require a concrete documented reason.
- Every module that starts listeners, tasks, resources, or state owns their shutdown/cleanup.
- Prefer explicit construction and lifecycle over mutable global singletons.

## Performance invariants

- No unbounded scans of loaded worlds/chunks/entities on the server tick.
- No blocking disk/network I/O on the main server thread.
- Avoid per-tick work unless gameplay requires it; prefer events, bounded batches, and coarse scheduling.
- Performance-sensitive features require measurement/profiling before claims of optimization.
- Do not alter gameplay as a hidden performance optimization.
- Background or scheduled work must have an owner, bounded work, failure handling, and cancellation.

## Internationalization policy

Living World is i18n-ready from the foundation.

- Brazilian Portuguese (`pt-BR`) is the canonical/default language of the project.
- User/admin-facing messages must use the message catalog instead of hard-coded text in Java.
- Message keys are stable contracts and should be namespaced by feature where useful.
- Missing translations must fall back predictably to `pt-BR`; do not silently return unrelated text.
- English may exist as an additional locale, but it is not the canonical source language.
- Human-readable messages written directly in Java (exceptions, validation diagnostics and similar developer-facing text) should also be PT-BR unless an external API/contract requires otherwise.
- Do not build a full translation platform before it is needed.

## Configuration

Configuration should have a clear owner and validated defaults. Do not read raw configuration ad hoc throughout the codebase.

The initial global locale is configured in `config.yml`; feature-specific configuration should live with the owning feature when introduced.

## Testing and gates

Run the relevant subset after each meaningful block and all applicable gates before completion:

```bash
./gradlew test
./gradlew build
```

Use `runServer` for runtime smoke testing when the task requires Paper behavior.

Tests should cover observable behavior, lifecycle, fallback semantics, and real regressions. Do not couple tests to incidental implementation details.

## Git and authorization

Observed initial repository state: local Git repository, branch `master`, unborn HEAD at project initialization.

Current user authorization covers protocol initialization and continued local development. It does **not** authorize merge, deploy, remote repository creation, branch-protection changes, publishing releases, or external paid services.

Preserve unrelated user changes. Inspect the diff before committing. Never commit secrets.

## Definition of done

A task is not complete merely because code compiles. Relevant acceptance criteria, tests/build, documentation/checkpoint state, and runtime validation when applicable must be reconciled before marking it validated.
