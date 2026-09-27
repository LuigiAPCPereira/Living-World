# Engineering DNA

> Universal engineering principles for building software that is correct, understandable, secure, efficient, observable, testable, and easy to evolve.

This document defines the default engineering philosophy for this repository.

It does **not** prescribe one language, framework, folder tree, architecture, process, or tooling stack. Different projects may legitimately look very different.

What should remain consistent is the quality of reasoning.

---

## 1. Core principle

> Observe before assuming.  
> Give every responsibility a clear owner.  
> Keep boundaries narrow.  
> Preserve unknowns as unknowns.  
> Fail safely.  
> Test the behavior that matters.  
> Measure before optimizing.  
> Make only the smallest commitment justified by current evidence.

### Craftsmanship: Monozukuri-inspired software engineering

Treat software development as a careful craft: understand the real need, respect
existing contracts, and make behavior correct, understandable, testable, and
maintainable for the people who use and evolve the product. This is an
engineering adaptation inspired by Monozukuri, not a claim that a manufacturing
tradition maps literally onto software.

Craftsmanship is observable in useful behavior, explicit ownership, failure
handling, tests, and justified simplicity—not merely visual or stylistic polish.
Deliver the smallest **complete** solution that satisfies the approved acceptance
criteria. Do not invoke craftsmanship to prolong investigation, refactor beyond
scope, create speculative abstractions, or invent follow-up features.

### Idiomatic code and naming

Follow the conventions of the actual language, project contracts, formatter,
and consistent local code. Choose names that convey purpose and ownership.
No single naming style (PascalCase, camelCase, snake_case, or another) is a
universal rule. In languages where capitalization has semantics—such as Go
identifier export—preserve those semantics and established public APIs.

For changes to existing code, stay consistent with the affected area unless
its conventions are materially harmful. Broad renaming and formatting are
separate, justified changes; adopting an agent protocol is not permission for
a repository-wide style rewrite.

---

## 2. Scope

This DNA governs general principles of:

- architecture;
- code organization;
- state and ownership;
- concurrency and lifecycle;
- external integrations;
- security and privacy;
- configuration;
- storage and caching;
- performance;
- APIs;
- frontend;
- observability;
- testing;
- tooling;
- documentation;
- evolution and refactoring;
- delivery and definition of done.

It does **not** override:

- explicit product requirements;
- security requirements;
- regulatory constraints;
- observed runtime behavior;
- external contracts;
- project-specific instructions;
- accepted specifications.

---

## 3. Authority order

When guidance conflicts, use approximately this order:

1. security and integrity;
2. explicit product truth and user requirements;
3. observed runtime behavior;
4. accepted contracts and specifications;
5. intentional tests;
6. project architecture and local instructions;
7. this Engineering DNA;
8. specification tooling;
9. analysis tooling;
10. UX tooling;
11. framework conventions;
12. personal preference.

Tools are advisors. They do not redefine reality.

---

# Part I — Truth, evidence, and scope

## 4. Reconstruct reality before changing it

Before meaningful work, inspect the state that actually exists.

When applicable, confirm:

- repository;
- branch;
- HEAD;
- working tree;
- open PRs;
- current tests;
- build;
- runtime configuration;
- generated artifacts;
- active architecture;
- relevant documentation;
- external contracts;
- current deployment behavior.

Do not assume that a branch, SHA, API behavior, document, screenshot, or previous diagnosis is still current.

---

## 5. Evidence, inference, and hypothesis are different

Treat important claims as one of:

### Evidence

Directly supported by:

- code;
- tests;
- runtime;
- observed responses;
- authoritative documentation;
- accepted contract;
- reproducible measurement.

### Inference

A reasoned conclusion derived from evidence.

### Hypothesis

A possibility not yet established.

Never silently promote a hypothesis to fact.

---

## 6. Unknown is a valid result

Do not convert lack of evidence into convenient values.

Avoid unjustified transformations such as:

```text
unknown -> false
unknown -> 0
unknown -> healthy
unknown -> offline
unknown -> unsupported
unknown -> empty
```

Prefer explicit states when relevant:

- unknown;
- unavailable;
- not observed;
- not configured;
- unsupported;
- not applicable.

---

## 7. Smallest sufficient commitment

Build only what current evidence and product requirements justify.

Before adding a capability or abstraction, ask:

- Does this problem exist now?
- Is the current solution insufficient?
- Does the abstraction reduce total complexity?
- Is there a real boundary?
- Is there real variation?
- Is there a real second consumer?
- Can this remain simpler for now?

Avoid speculative:

- plugin systems;
- registries;
- event buses;
- generic frameworks;
- multi-provider systems;
- distributed locks;
- generalized extension points;
- deep inheritance hierarchies.

---

## 8. Complexity must pay rent

Every abstraction costs:

- concepts;
- files;
- indirection;
- dependencies;
- runtime states;
- failure modes;
- cognitive load;
- maintenance.

A good abstraction reduces total system complexity.

A bad abstraction only moves complexity elsewhere.

---

# Part II — Ownership and architecture

## 9. One clear owner per responsibility

For every important state or behavior, be able to answer:

- Who creates it?
- Who validates it?
- Who may mutate it?
- Who observes it?
- Who ends its lifecycle?
- Why would this module change?

If multiple modules appear equally responsible, ownership is probably unclear.

---

## 10. Separate concepts that change for different reasons

Default distinctions:

```text
Transport       != Domain
DTO             != Domain Model
Provider        != Use Case
Storage         != Domain
Route           != Feature
Feature         != Infrastructure
Session         != Profile
Identity        != Authorization
Configuration   != Runtime State
Fixture         != Production Data
Telemetry       != Source of Truth
UI              != Business Rule
Tooling         != Runtime
Specification   != Implementation
```

Co-location is fine when ownership is shared.

Mixing independent responsibilities is not.

---

## 11. Dependency direction

Prefer dependencies that point inward:

```text
Entrypoint / Presentation
          |
          v
Application / Use Cases
          |
          v
Domain / Ports
          ^
          |
Adapters
          ^
          |
Infrastructure / Transport
```

Infrastructure implements internal contracts.

Domain should not depend on specific:

- frameworks;
- databases;
- HTTP clients;
- cache technologies;
- filesystems;
- UI systems;
- deployment platforms.

---

## 12. External data crosses explicit boundaries

Preferred flow:

```text
External Input
    |
    v
Parse
    |
    v
Validate
    |
    v
Normalize
    |
    v
Adapter
    |
    v
Domain
```

Avoid:

```text
fetch()
  |
  v
type assertion
  |
  v
UI
```

A type cast is not runtime validation.

---

## 13. Composition root

Non-trivial systems should have a small, explicit place where concrete dependencies are assembled.

Example:

```text
Validated Config
      |
      v
Database
      |
      v
Repository Adapter
      |
      v
Application Service
      |
      v
Entrypoint
```

Do not duplicate wiring across routes, components, handlers, workers, and jobs.

Do not introduce a heavy dependency-injection framework when a small factory or constructor graph is enough.

---

## 14. Interfaces are for real boundaries

Create an interface/port when there is a concrete reason, such as:

- external boundary;
- more than one legitimate implementation;
- test isolation;
- provider independence;
- infrastructure inversion;
- policy separation.

Do not create interfaces by ritual.

Suspicious pattern:

```text
ThingInterface
ThingImpl
ThingFactory
ThingFactoryImpl
```

with no real variation or boundary.

---

## 15. Public module surfaces should be narrow

Large modules may expose a small intentional API while keeping internals private.

Avoid:

- global barrel exports that hide cycles;
- leaking internals for convenience;
- cross-feature imports into implementation details.

---

## 16. Organize by ownership, not by generic drawers

Be cautious with directories such as:

- `utils/`;
- `helpers/`;
- `services/`;
- `common/`;
- `misc/`.

Before placing code there, ask whether it actually belongs to:

- a domain;
- a feature;
- configuration;
- transport;
- presentation;
- persistence;
- tooling.

Generic modules should be genuinely generic.

---

## 17. Refactor by reason to change, not line count

A large file can be correct if it has one clear owner.

Several tiny files can still be wrong if they fragment a single responsibility.

Split when there are:

- different owners;
- different lifecycles;
- different reasons to change;
- mixed layers;
- difficult test seams;
- unnecessary dependencies;
- structurally harmful coupling.

---

## 18. Legacy stays contained

Legacy code should be:

- explicitly identified;
- isolated behind boundaries where possible;
- prevented from spreading;
- removable when compatibility permits.

Do not redesign the entire new architecture around accidental legacy constraints when an adapter can contain them.

---

# Part III — State, lifecycle, and concurrency

## 19. Prefer local state over global state

Default preference:

```text
local state
    >
explicitly owned shared state
    >
global mutable state
```

Global mutable state requires a strong reason.

Avoid hidden global:

- current user;
- token;
- tenant;
- request context;
- mutable singleton;
- private-user cache without scope.

---

## 20. Every important state has a lifecycle

Define:

- owner;
- lifetime;
- mutation policy;
- concurrency policy;
- cleanup;
- recovery behavior.

---

## 21. Everything that starts must have a way to stop

Applicable to:

- goroutines;
- tasks;
- promises;
- workers;
- timers;
- sockets;
- subscriptions;
- streams;
- background loops;
- file handles;
- processes.

For each, know:

- who starts it;
- who cancels it;
- how it fails;
- when it ends;
- how cleanup occurs.

---

## 22. Timeout and cancellation are part of I/O design

Potentially blocking operations should consider:

- timeout;
- cancellation;
- deadlines;
- retry;
- backpressure;
- idempotency.

Do not make unbounded external calls the default.

---

## 23. Retry is a policy, not a reflex

Before retrying, ask:

- Is the operation idempotent?
- Is the failure transient?
- Is there a hard limit?
- Is there backoff?
- Is there jitter?
- Could retry duplicate a side effect?
- Could retry hide an outage?

Automatic retry deserves extra scrutiny for:

- login;
- payments;
- writes;
- destructive actions;
- non-idempotent operations.

---

## 24. Concurrency should be bounded

Avoid unbounded fan-out.

Consider:

- upstream rate limits;
- memory;
- sockets;
- CPU;
- cancellation;
- queue pressure;
- resource ownership.

Concurrency is not automatically faster or safer.

---

## 25. Locks should be narrow

Avoid holding locks during external I/O when possible.

Prefer clearer ownership over complicated locking.

---

## 26. Shared mutability must be explicit

Consider:

- immutable structures;
- readonly types;
- defensive copies;
- ownership rules;
- synchronization.

Do not assume callers will never mutate shared data.

---

# Part IV — Failure semantics and resilience

## 27. Fail closed at security boundaries

For authorization and trust:

```text
uncertain -> deny
```

not:

```text
uncertain -> allow
```

Apply this to:

- authentication;
- authorization;
- signatures;
- encryption validation;
- session validation;
- ownership checks;
- permissions;
- security configuration.

---

## 28. Minimize failure blast radius

A failure should affect the smallest semantically correct area.

If primary data is valid and optional metadata fails, do not automatically discard everything.

But do not use partial failure to hide critical security failures.

---

## 29. Partial failure belongs to the layer that understands the product

Transport reports what failed.

Application/use cases decide whether a valid partial result exists.

Presentation communicates that result.

Do not spread fallback policy through UI components.

---

## 30. Errors are translated at boundaries

Example:

```text
network timeout
    |
    v
TransportError
    |
    v
CapabilityUnavailable
    |
    v
FeatureState.partial
    |
    v
User-facing message
```

The UI should not receive raw:

- stack traces;
- database errors;
- HTTP implementation details;
- upstream payloads;
- internal exception objects.

Transport should not decide final UX.

---

## 31. Avoid ambiguous sentinel values

Be careful with:

```text
null
false
0
""
[]
```

when they could mean multiple things.

Do not collapse:

- not found;
- unavailable;
- unauthorized;
- malformed;
- timed out;
- empty;

into one value when the distinction matters.

---

## 32. Real bugs get regression tests

Preferred flow:

```text
observed bug
   |
   v
minimal reproduction
   |
   v
failing test
   |
   v
smallest sufficient fix
   |
   v
passing regression test
   |
   v
relevant gates
```

---

# Part V — Security and privacy

## 33. Security is designed, not appended

Consider from the beginning:

- trust boundaries;
- authentication;
- authorization;
- input validation;
- output encoding;
- session isolation;
- origin/CSRF protection;
- rate limiting;
- secret management;
- encryption;
- secure defaults;
- logging hygiene.

---

## 34. Minimize sensitive data

If the product does not need data, prefer not to collect it.

Better:

```text
do not cross the boundary
```

than:

```text
collect and promise not to use
```

Discard unnecessary sensitive fields as early as possible.

---

## 35. Secrets never belong in ordinary artifacts

Never intentionally expose secrets in:

- Git;
- logs;
- URLs;
- client bundles;
- analytics;
- screenshots;
- fixtures;
- architecture reports;
- test snapshots;
- error messages.

Secrets should have:

- source;
- scope;
- owner;
- lifetime;
- rotation path.

---

## 36. Validate before side effects

Where possible:

```text
parse
  |
  v
validate
  |
  v
authorize
  |
  v
side effect
```

Do not publish state, create sessions, open privileged resources, or perform writes before required checks have succeeded.

---

# Part VI — Configuration, persistence, and caching

## 37. Configuration is an executable contract

Centralize:

- environment parsing;
- configuration files;
- defaults;
- validation;
- provider selection;
- feature flags.

Preferred flow:

```text
Raw Config
    |
    v
Parse
    |
    v
Validate
    |
    v
Validated Config
    |
    v
Runtime
```

Avoid reading raw environment values throughout the codebase.

---

## 38. Defaults must be intentional

Distinguish when relevant:

- omitted;
- explicit `false`;
- explicit `0`;
- empty string;
- `null`.

Do not silently reinterpret user intent.

---

## 39. Example configuration should stay trustworthy

Examples should:

- use valid options;
- contain no real secrets;
- match current contracts;
- be tested when practical.

---

## 40. Persistence is an adapter

Domain should not casually depend on:

- PostgreSQL;
- SQLite;
- Redis;
- S3;
- filesystem;
- ORM-specific types.

Use ports where a real boundary exists.

Do not add repository abstractions by dogma.

---

## 41. Cache is not source of truth

Every cache should define:

- key;
- scope;
- TTL;
- invalidation;
- stale semantics;
- failure behavior;
- privacy boundary.

Never confuse private-user cache with global cache.

---

## 42. Prefer append-only when historical integrity matters

If historical truth or auditability is important, consider append-only evidence/events instead of rewriting the past.

Do not adopt event sourcing merely because append-only storage is useful in one area.

---

# Part VII — Performance and efficiency

## 43. Performance starts with architecture

Preferred order:

```text
Correctness
    |
    v
Clear Ownership
    |
    v
Good Data Flow
    |
    v
Adequate Algorithms
    |
    v
Avoid Unnecessary Work
    |
    v
Measure
    |
    v
Profile
    |
    v
Optimize
```

---

## 44. Identify hot paths

Know which paths are:

- latency-sensitive;
- throughput-sensitive;
- memory-sensitive;
- availability-critical.

Keep non-critical work out of hot paths where possible.

Potentially auxiliary work includes:

- admin UI;
- analytics;
- report generation;
- downloads;
- secondary APIs;
- heavy logging;
- optional enrichment.

---

## 45. Fight unnecessary work first

Common problems:

- N+1;
- duplicate fetches;
- repeated parsing;
- redundant serialization;
- unnecessary sequential I/O;
- oversized payloads;
- avoidable hydration;
- global recomputation;
- expensive copies;
- unbounded queries;
- unbounded concurrency.

---

## 46. Measure before micro-optimizing

Use evidence:

- profiling;
- tracing;
- benchmarks;
- metrics;
- flame graphs;
- allocation profiles;
- query plans.

Do not make code obscure to save hypothetical cost.

---

## 47. Benchmarks need purpose

A benchmark should protect or inform something real.

If it becomes a gate, document:

- what it measures;
- why;
- environment assumptions;
- threshold;
- acceptable variance.

---

# Part VIII — Testing and verification

## 48. Test observable behavior

Prefer tests that protect:

- contracts;
- invariants;
- state transitions;
- outputs;
- side effects;
- failure semantics.

Avoid overfitting tests to implementation details without product value.

---

## 49. Use the smallest useful test level

Possible layers:

```text
Pure / Domain
Contract
Adapter
Integration
System / Smoke
End-to-End
```

Not every behavior needs every layer.

---

## 50. External systems should be controllable in tests

Prefer, when appropriate:

- fake servers;
- mock transports;
- synthetic credentials;
- deterministic clocks;
- explicit fixtures.

Use real-environment smoke tests when they prove something mocks cannot.

---

## 51. Time is a dependency

When deterministic behavior matters, avoid uncontrolled time reads everywhere.

A small injectable clock or `now()` function is often enough.

Do not build an enterprise time framework for simple cases.

---

## 52. Gates are project-specific but explicit

Possible gates:

- formatter;
- lint;
- static analysis;
- typecheck;
- tests;
- race checks;
- build;
- smoke tests;
- security checks;
- architecture checks;
- benchmark thresholds;
- generated artifact validation.

Passing the build alone is not a complete definition of done.

---

# Part IX — Observability

## 53. Observability answers operational questions

Logs should help answer:

- What happened?
- Where?
- Which capability?
- How long did it take?
- Which error class?
- Was fallback used?
- Was the operation partial?

---

## 54. Do not log the same error everywhere

Log at boundaries where context is added.

Prefer structured logs when suitable.

Avoid duplicate noise across every layer.

---

## 55. Metrics need a question

Do not instrument because "professional systems have metrics."

Each metric should answer something useful.

Examples:

- latency;
- error rate;
- queue depth;
- cache hit rate;
- memory pressure;
- active sessions.

---

# Part X — APIs and integrations

## 56. APIs should expose stable concepts

Contracts should be:

- explicit;
- coherent;
- minimal;
- deterministic;
- versionable when needed;
- predictable in failure.

Do not expose infrastructure details without need.

---

## 57. Backward compatibility is a requirement, not a religion

Before preserving old behavior, ask:

- Is there a real consumer?
- Is the contract public?
- Is migration possible?
- Does compatibility preserve an accidental mistake?

Sometimes fixing a design is better than immortalizing it.

---

## 58. External integrations require evidence

Do not assume one deployment, provider, version, institution, or implementation behaves like another.

For unknown external systems:

1. discover;
2. observe;
3. record supported contract;
4. distinguish unknowns;
5. implement only proven behavior.

---

## 59. Raw external payloads stay at the boundary

External DTOs should not leak casually into:

- domain;
- UI;
- storage schemas;
- public APIs.

Normalize into internal concepts.

---

# Part XI — Frontend principles

## 60. Product truth comes before visual polish

Preferred flow:

```text
Problem
  |
  v
Information
  |
  v
States
  |
  v
Interaction
  |
  v
Composition
  |
  v
Visual Expression
  |
  v
Components
  |
  v
Code
```

UI must not invent domain facts to make a screen look complete.

---

## 61. Frontend is not owner of backend semantics

UI consumes contracts.

It should not assume internal:

- lifecycle;
- persistence;
- transport;
- storage;
- provider behavior;
- hidden state.

---

## 62. States are part of the product contract

Consider, when applicable:

- loading;
- success;
- empty;
- partial;
- unavailable;
- unauthorized;
- error.

Do not collapse them for convenience.

---

## 63. Loading must preserve truth

Avoid:

- fake production data;
- fake metrics;
- oversized spinners;
- unnecessary full-screen blocking.

Prefer:

- structural skeletons;
- stable layout;
- subtle motion;
- reduced-motion support.

---

## 64. Accessibility is a requirement

When applicable, cover:

- semantic markup;
- keyboard;
- focus visibility;
- labels;
- contrast;
- touch target size;
- zoom;
- screen-reader semantics;
- reduced motion.

---

## 65. Responsive design may change composition

Mobile is not simply compressed desktop.

Preserve:

- information priority;
- readability;
- interaction ergonomics;
- touch reach;
- density.

---

## 66. Avoid visual cargo cults

A visual container should have semantic reason.

Avoid turning every block into:

```text
rounded card + border + shadow
```

by habit.

---

# Part XII — Tooling

## 67. Tooling is separate from runtime

Development tools should not contaminate production runtime unless there is a clear reason.

Examples:

- Graphify;
- OpenSpec;
- Impeccable;
- profilers;
- linters;
- coverage tools;
- generators;
- static analyzers.

---

## 68. Tools are advisors, not authorities

> A tool should reduce uncertainty or work.  
> If it only adds ceremony, do not use it.

---

# Part XIII — Graphify

## 69. Graphify is the architectural X-ray

Use Graphify when the codebase is large enough for structural analysis to be valuable.

Especially useful for:

- structural refactors;
- migrations;
- god-node discovery;
- dependency paths;
- feature-boundary analysis;
- before/after architecture comparison.

---

## 70. Graphify workflow

For meaningful architecture changes:

```text
Graphify Before
    |
    v
Inspect Nodes / Paths / Communities
    |
    v
Form Hypotheses
    |
    v
Confirm in Code
    |
    v
Implement
    |
    v
Graphify After
    |
    v
Compare
```

---

## 71. Useful Graphify questions

Examples:

```text
Which modules are highly coupled?
Where are cycles?
Which files mix unrelated responsibilities?
What imports infrastructure into domain?
Which feature boundaries leak?
Where are environment dependencies read?
What is the dependency path from entrypoint to storage?
Which modules became more central after this change?
Which UI modules import transport?
Which nodes act as god modules?
```

---

## 72. Graphify does not decide architecture

A high-degree node is not automatically bad.

Legitimate central nodes may include:

- composition roots;
- domain kernels;
- routing registries;
- stable public abstractions.

Graphify identifies where to investigate.

Code, behavior, contracts, and tests decide.

---

## 73. Graphify artifacts

When useful and safe, version portable outputs such as:

```text
graphify-out/graph.html
graphify-out/graph.json
graphify-out/GRAPH_REPORT.md
```

Do not version:

- machine-local interpreter paths;
- caches;
- temp data;
- secrets;
- sensitive source-derived artifacts.

Use `.graphifyignore` to avoid analyzing:

- dependencies;
- builds;
- generated noise;
- secrets;
- Graphify's own output.

---

# Part XIV — OpenSpec

## 74. Use OpenSpec for meaningful contract changes

OpenSpec is useful for:

- new capabilities;
- public behavior changes;
- protocol changes;
- migrations;
- large architecture transitions;
- ambiguous multi-module work.

Do not require a spec for every small fix.

---

## 75. Specification should reduce ambiguity

Preferred sequence:

```text
Problem
  |
  v
Expected Behavior
  |
  v
Constraints
  |
  v
Failure Semantics
  |
  v
Acceptance Criteria
  |
  v
Implementation
```

If a specification would not affect a decision, implementation, or validation, it may not be worth creating.

---

# Part XV — Impeccable and frontend refinement

## 76. Impeccable is a frontend critic

Use Impeccable for:

- critique;
- hierarchy;
- composition;
- spacing;
- responsive behavior;
- accessibility;
- polish;
- interaction refinement;
- anti-slop.

It does not define:

- product truth;
- factual data;
- security;
- backend contracts;
- domain semantics.

---

## 77. Frontend refinement order

Preferred sequence:

```text
Product truth
    |
    v
Information architecture
    |
    v
States
    |
    v
Interaction
    |
    v
Initial implementation
    |
    v
Impeccable critique
    |
    v
Refinement
```

---

# Part XVI — Fixtures, demo data, and generated evidence

## 78. Fixtures must look like fixtures

Prefer explicit locations:

```text
fixtures/
tests/fixtures/
demo/
samples/
```

Do not let production silently fall back to fixtures.

---

## 79. Demo data must not masquerade as reality

Do not present fake:

- health;
- uptime;
- activity;
- metrics;
- user data;
- institutional data;
- timestamps;

as live information.

---

## 80. Generated evidence can be versioned when useful

Examples:

- architecture graphs;
- schemas;
- benchmark reports;
- API docs;
- coverage reports;
- profiles.

Version when they are:

- portable;
- useful;
- intentional;
- free of secrets;
- part of the project's long-term understanding.

---

# Part XVII — Documentation and evolution

## 81. Documentation preserves hard-to-recover knowledge

Good documentation explains:

- invariants;
- contracts;
- security boundaries;
- architecture boundaries;
- operational constraints;
- non-obvious decisions;
- why something exists.

Avoid documentation that merely narrates every implementation step.

---

## 82. Code explains how; documentation should often explain why

Not an absolute rule, but a strong default.

---

## 83. Do not create documents by ritual

Do not create ADRs, reports, investigations, or status files merely because a process says they should exist.

Create them when they preserve useful, non-trivial knowledge.

---

# Part XVIII — Dependencies and builds

## 84. Dependencies need justification

Before adding a dependency, ask:

- What problem does it solve?
- Why is the current stack insufficient?
- What maintenance does it add?
- What runtime cost?
- What security surface?
- Is it actively maintained?
- Can it remain isolated?

Avoid both dependency-phobia and dependency-sprawl.

---

## 85. Upgrades need a reason

Good reasons include:

- security;
- support;
- compatibility;
- required capability;
- performance;
- maintenance.

"Newer exists" is not sufficient by itself.

---

## 86. Builds should be reproducible

Avoid accidental dependence on:

- developer-specific state;
- undeclared local files;
- implicit caches;
- machine-specific configuration.

---

## 87. CI should reproduce local gates

Prefer:

```text
local command == CI command
```

where practical.

Do not hide essential build logic only in CI.

---

# Part XIX — Red-team engineering

## 88. Try to falsify the design before declaring success

Ask:

- How would this boundary be bypassed?
- What happens without network?
- What happens with malformed data?
- What happens twice?
- What happens very slowly?
- What happens concurrently?
- What happens empty?
- What happens partially valid?
- What happens without permission?
- What happens with missing config?
- What happens after restart?
- What happens during shutdown?
- What happens with clock skew?

---

## 89. Architectural red-team

Try to falsify:

```text
Domain does not depend on infrastructure.
UI does not receive raw external DTOs.
Auth does not depend on product features.
Fixtures do not enter production.
Config is not scattered.
No unsafe global mutable state appeared.
No accidental god module was created.
Partial failure has the right blast radius.
Sensitive data stays inside the right boundaries.
```

Fix or explicitly document justified exceptions.

---

# Part XX — Progressive maturity

## 90. Engineering rigor scales with project maturity

### Level 0 — Experiment

Focus on:

- idea validation;
- minimal code;
- critical correctness;
- minimal ceremony.

### Level 1 — Project

Add:

- clear organization;
- meaningful tests;
- validated configuration;
- basic CI;
- reasonable boundaries.

### Level 2 — Product

Add:

- observability;
- security review;
- explicit failure semantics;
- smoke tests;
- architecture discipline;
- Graphify for structural changes;
- frontend state coverage;
- operational documentation where needed.

### Level 3 — Critical System

Consider:

- threat modeling;
- load tests;
- race tests;
- fault injection;
- recovery testing;
- SLOs;
- runbooks;
- benchmark gates;
- stricter security review.

Do not force Level 3 ceremony onto a prototype.

---

# Part XXI — Definition of Done

## 91. General definition of done

A meaningful change is not done merely because code exists.

Usually require:

- implementation complete;
- relevant tests;
- applicable gates green;
- failure semantics understood;
- security considered;
- no leaked secrets;
- relevant dead code removed;
- documentation updated if a contract changed;
- runtime behavior understood.

---

## 92. Frontend addition

Frontend work additionally considers:

- loading;
- empty;
- partial;
- unavailable/error;
- responsive;
- accessibility;
- visual inspection.

---

## 93. Architecture addition

Architecture-heavy work additionally considers:

- dependency inspection;
- Graphify before/after when useful;
- boundary review;
- god-node review.

---

## 94. Contract addition

Contract-heavy work may additionally require:

- OpenSpec;
- migration notes;
- compatibility analysis.

---

# Part XXII — Delivery

## 95. Significant delivery should distinguish

### Implemented

What changed.

### Validated

What evidence confirms it.

### Not validated

What has not been exercised.

### Unknown

What remains unknown.

### Follow-up

The next real blocker or slice.

Do not call uncertain behavior "done."

---

# Part XXIII — Anti-patterns

## 96. Patterns requiring strong justification

- global mutable singleton;
- god object;
- `ManagerServiceFactoryRegistry` without necessity;
- empty `catch`;
- silent fallback;
- infinite retry;
- task without owner;
- external I/O without timeout;
- scattered configuration;
- raw transport payload in UI;
- domain importing framework;
- secrets in logs;
- fixture in production;
- optimization without measurement;
- interface without a real boundary;
- generic abstraction for one case;
- ignored N+1;
- unbounded concurrency;
- fake telemetry;
- fake live data;
- documentation by ritual;
- tooling driving product semantics.

---

# Part XXIV — Checklists

## 97. New feature checklist

Before:

- What real problem are we solving?
- What evidence supports the behavior?
- Who owns it?
- What is the boundary?
- What is the contract?
- What remains unknown?
- What can fail?
- What data is sensitive?
- Is it on a hot path?
- Does it need OpenSpec?
- Does Graphify add value?

During:

- Is transport separated from domain?
- Does application own policy?
- Is partial failure correct?
- Did hidden global state appear?
- Is config centralized?
- Do tests protect behavior?
- Did we add unnecessary dependencies?

After:

- Tests pass?
- Build passes?
- Security invariants remain?
- Performance regressed?
- Graphify shows a new god node?
- Dead code remains?
- Docs need update?
- What is still unknown?

---

## 98. Bug checklist

- reproduce;
- identify boundary;
- identify owner;
- confirm root cause;
- add regression test;
- implement smallest sufficient fix;
- run related tests;
- run gates;
- check blast radius;
- update docs only if contract changed.

---

## 99. Refactor checklist

- Is observable behavior supposed to remain?
- Is baseline green?
- Graphify before?
- Which boundary is wrong?
- Which coupling should decrease?
- Which imports should disappear?
- Does the new design reduce total complexity?
- Do tests protect behavior?
- Graphify after?
- Did a new god node appear?

---

## 100. Performance checklist

- Which metric is bad?
- Is there evidence?
- Is it a real hot path?
- Is the algorithm adequate?
- Is work duplicated?
- Is there N+1?
- Is I/O unnecessarily serial?
- Would cache actually help?
- Is concurrency bounded?
- Is there before/after measurement?
- Does optimization complexity justify the gain?

---

## 101. Security checklist

- Trust boundaries known?
- Inputs validated?
- Authentication and authorization distinct?
- Fail closed?
- Secrets absent from logs?
- Sensitive data minimized?
- Sessions isolated?
- Retries safe?
- Rate limits where needed?
- Standard authenticated cryptography?
- Logout/revocation semantics accurate?
- Errors avoid leaking internals?

---

## 102. External integration checklist

- Was the real contract observed?
- Are we extrapolating from another provider?
- Is runtime data validated?
- Timeout?
- Cancellation?
- Retry semantics?
- Rate limits?
- Unknown fields?
- Optional fields?
- Privacy boundary?
- Raw payload contained?
- Adapter normalizes into internal domain?
- Tests use synthetic fixtures?
- Is a real smoke test necessary?

---

# Part XXV — Repository integration

## 103. Minimal project start

Start small:

```text
README.md
ENGINEERING_DNA.md
AGENTS.md
src/
tests/
```

Add only when justified:

```text
docs/
openspec/
.graphifyignore
graphify-out/
scripts/
```

Do not create empty architecture for hypothetical future needs.

---

## 104. AGENTS integration

Project instructions should explicitly inherit this DNA unless a local rule overrides it for a documented project-specific reason.

For multi-session development, use `DOCUMENTATION_AND_CONTINUITY.md` as the operational protocol when that document is accessible. This DNA governs engineering principles, not conversational commands or permission grants.

Recommended concept:

```text
Read ENGINEERING_DNA.md before architectural changes.

This repository inherits the Engineering DNA unless AGENTS.md explicitly
overrides a rule for project-specific reasons.

Use Graphify for structural analysis when it adds value.

Use OpenSpec for meaningful behavioral or contract changes.

For frontend changes, read FRONTEND_DNA.md and use Impeccable as a critique
and refinement tool when appropriate.

Product truth, security constraints, accepted contracts, and observed runtime
behavior override tooling suggestions.
```

---

# 105. Final decision questions

When a decision is difficult, ask:

```text
What do we know?
What are we inferring?
What is still unknown?

Who owns this responsibility?
Which boundary protects it?
What is the smallest contract needed?

How does it fail safely?
What is the smallest correct blast radius?

How will we prove it works?
How will we know if it became slower?
How will we know if it became more coupled?

Does this complexity really need to exist now?
```

If these answers are clear, the implementation is probably aligned with the Engineering DNA.

---

# 106. Compact DNA

The shortest version:

1. Observe before assuming.
2. Evidence outranks hypothesis.
3. Preserve unknown as unknown.
4. One owner per responsibility.
5. Separate concepts that change for different reasons.
6. Dependencies point inward.
7. Keep boundaries narrow.
8. Make the smallest sufficient commitment.
9. Fail closed where trust is involved.
10. Minimize failure blast radius.
11. Test observable behavior.
12. Give real bugs regression tests.
13. Treat configuration as a contract.
14. Minimize sensitive data.
15. Make global state exceptional.
16. Give every lifecycle an owner and end.
17. Make timeout/cancellation part of I/O design.
18. Keep hot paths lean.
19. Measure before optimizing.
20. Keep fixtures separate from production truth.
21. Use tooling as evidence, not authority.
22. Use Graphify to make architecture visible.
23. Use OpenSpec to reduce meaningful ambiguity.
24. Use Impeccable to refine frontend without redefining product truth.
25. Document hard-to-recover knowledge.
26. Make complexity justify its cost.
27. Do not call work complete without validation.
28. Design today so the next change is easier without building tomorrow in advance.
29. Practice craftsmanship: idiomatic code, verified quality, and timely completion within scope.