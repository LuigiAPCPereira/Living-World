# Living World — Container Sort QoL

- **Milestone:** M21
- **Implementation task:** LW-210
- **Runtime validation task:** LW-211
- **Branch:** `qol/container-sort`
- **Status:** implementation candidate present; automated gate and real-player runtime validation still required.
- **Product framing:** explicit organization of one already-open storage inventory. This is not automatic sorting, nearby-container search, item classification, storage networking or player-inventory management.

## Player interaction

Container Sort is intentionally explicit.

Trigger:

1. open a supported chest, double chest, barrel or placed shulker box;
2. keep the cursor empty;
3. Shift + left-click an **empty slot in the top storage inventory**.

The complete trigger additionally requires Paper to report the click as `InventoryAction.NOTHING`, so Living World only observes a vanilla no-op rather than replacing a meaningful inventory action.

The click is not cancelled. Actual storage mutation is deferred to the next server tick, following Paper guidance for inventory-content changes originating from `InventoryClickEvent`.

### Intentional full-container limitation

The first slice requires an empty target slot as the gesture surface. A completely full container therefore has no Container Sort gesture.

Living World deliberately accepts that limitation rather than stealing Shift + left-click from an occupied stack, where vanilla has meaningful transfer behavior.

## Supported targets

Initial supported targets are physical vanilla storage blocks:

- chest;
- double chest;
- barrel;
- placed shulker box.

The adapter checks both inventory type and physical vanilla holder identity.

Out of scope:

- hopper;
- dispenser/dropper;
- furnace/smoker/blast furnace;
- brewing stand;
- crafter and other machines;
- Ender Chest;
- entity storage;
- player inventory;
- plugin-created chest GUIs;
- nearby-container discovery;
- storage networks.

Machines are intentionally excluded because their slot positions can carry gameplay/input/output meaning that a generic sorter should not rewrite.

## Exclusive-viewer and concurrency rule

Container Sort only triggers when the initiating player is the target inventory's sole viewer.

At trigger time Living World snapshots the target storage contents and records the physical target identity. The next-tick operation proceeds only if:

- the player is still online;
- the same physical supported storage is still open;
- the initiating player is still the sole viewer;
- every storage slot still equals the captured snapshot, including amount and item metadata/components.

If another player opens the storage, any item changes, or the player switches/closes the target before execution, sorting aborts without mutation.

This makes the operation fail closed instead of trying to merge concurrent inventory intent.

## Sorting semantics

Container Sort has two deterministic stages.

### 1. Exact stack consolidation

Non-empty stacks are grouped only when Paper `ItemStack.isSimilar` reports them equivalent.

Amount is therefore ignored for grouping, while item type and metadata/components remain part of identity.

Consequences:

- two ordinary Stone stacks may consolidate;
- a named Stone and ordinary Stone remain separate variants;
- differently enchanted/component-bearing items are never merged unless Paper itself considers them similar.

Each exact variant uses its own `ItemStack.getMaxStackSize()`, including item-component stack-size overrides exposed by Paper.

If normalization would require more storage slots than the inventory physically has, the planner fails closed and the live inventory remains unchanged.

### 2. Stable material ordering

After exact variants are grouped, groups are sorted lexically by the material namespaced key, such as `minecraft:dirt` before `minecraft:stone`.

Variants of the **same material** intentionally receive no hidden metadata ranking. Java's stable group ordering preserves the order in which those variants first appeared in the original storage.

This produces a predictable coarse organization without Living World inventing semantic categories such as blocks/tools/food/value/rarity.

Generated stacks fill storage slots from the beginning; unused slots remain empty at the end.

## Player inventory boundary

Container Sort never reads from or writes to the player's:

- main inventory;
- hotbar;
- offhand;
- armor;
- cursor.

Only the already-open target storage is reorganized.

## Game mode, lifecycle and performance

Creative and Spectator are excluded.

The module is event-driven:

- no polling;
- no persistence;
- no world/container scans;
- one gesture inspects one already-open storage inventory;
- chest/barrel/shulker storage sizes bound the work naturally.

The sort planner is runtime-independent. The Paper adapter supplies only the concrete stack semantics it owns: exact similarity, material key, amount and max stack size.

Any next-tick task is owned by the module and cancelled on disable; the listener is unregistered on disable.

Configuration:

```yaml
qol:
  container-sort:
    enabled: true
```

When disabled, no Container Sort listener is registered.

## Relationship to other storage QoL

Container Sort is independent from Deposit Matching.

A shared generic storage framework is intentionally **not** introduced yet. Two related features are insufficient evidence that another abstraction would reduce complexity. If future Restock Matching creates proven shared ownership/behavior, that later task may justify extracting a small common storage boundary.

Restock Matching is an accepted future QoL direction but is not part of M21.

Waystone Favorites and Recent are unrelated navigation state and remain owned by the Waystone system, not generic QoL.

## Acceptance — LW-210 automated slice

LW-210 may be called automated-validated only when the exact candidate revision:

- compiles against the project's Paper 26.3 dependency with Java 25;
- passes `./gradlew clean test build` in GitHub Actions;
- has pure regression coverage for trigger rules, exact-variant grouping, stable same-material variant order, consolidation/max-stack behavior, amount conservation and capacity fail-closed behavior;
- passes scope/engineering review for supported targets, exclusive-viewer safety, snapshot revalidation, player-inventory isolation and bounded lifecycle.

Automated validation does not prove the client-facing inventory behavior on a real Paper session.

## Acceptance — LW-211 runtime slice

LW-211 remains pending until the scenarios in `RUNTIME_VALIDATION_BACKLOG.md` are exercised with a real player/Paper server.

Pending LW-211 does not block a separate independently authorized feature under the project's deferred-runtime policy.
