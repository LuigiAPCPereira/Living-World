# Living World — Deposit Matching QoL

- **Milestone:** M20
- **Implementation task:** LW-200
- **Runtime validation task:** LW-201
- **Branch:** `qol/deposit-matching`
- **Status:** implementation in progress; automated and runtime evidence are tracked separately.
- **Product framing:** Vanilla+ convenience for already-organized storage. This is not sorting, classification, storage networking or automation.

## Player interaction

Deposit Matching is intentionally explicit and only exists while the player has one supported vanilla storage inventory open.

Trigger:

1. open a supported chest, double chest, barrel or shulker box;
2. keep the cursor empty;
3. Shift + right-click an **empty slot in the container's top inventory**.

The clicked empty slot acts only as the gesture surface. The operation is not limited to that one slot.

The first slice deliberately requires an empty target slot for the gesture. A completely occupied container therefore has no Deposit Matching gesture even when one of its existing stacks still has capacity. This preserves normal Shift + right-click behavior on occupied slots instead of stealing a vanilla inventory action.

## Matching rule

At trigger time, Living World snapshots the non-empty storage contents already present in the opened container.

A player item is eligible only when one of those initial template stacks is `ItemStack.isSimilar` to it. Stack amount is ignored; item type and item data/components must match according to Paper/Bukkit semantics.

Consequences:

- cobblestone deposits only if matching cobblestone already exists in the container;
- an unrelated item is never introduced merely because there is free space;
- differently named, enchanted or otherwise different-component items do not match a normal stack unless Paper considers them similar;
- items added by Deposit Matching during the same operation do not create new eligibility categories.

Before each transfer, Living World rechecks that an equivalent item still exists in the live container. If another action removed the category before the deferred transfer executes, that source item remains with the player.

## Source inventory boundary

Only player main-inventory storage slots **9..35** are sources.

Living World does not pull from:

- hotbar slots 0..8;
- offhand;
- armor slots;
- cursor;
- another container.

This keeps the player's deliberate combat/building loadout untouched.

## Supported targets

Initial supported targets are real vanilla block storage:

- chest;
- double chest;
- barrel;
- shulker box.

The adapter checks both inventory type and the vanilla block holder. This intentionally excludes plugin-created CHEST-type GUIs that happen to mimic a chest inventory.

Out of scope for M20:

- hoppers;
- dispensers/droppers;
- furnaces/smokers/blast furnaces;
- brewing stands;
- crafters and other machines;
- entity inventories;
- Ender Chest;
- plugin GUIs;
- storage networks;
- nearby-container scanning;
- automatic sorting or category assignment.

Ender Chest is intentionally deferred because it is player-owned remote storage rather than a physical shared storage block; adding it should be an explicit product choice rather than an accidental consequence of supporting generic storage inventories.

## Transfer semantics

The click is observed at `MONITOR` with cancelled events ignored. Living World does not cancel or rewrite the vanilla click; the chosen empty-slot/empty-cursor gesture is already a no-op for vanilla inventory movement.

The actual inventory mutation is scheduled for the next server tick, following Paper guidance for inventory changes originating from `InventoryClickEvent`.

Before mutation:

- player must still be online;
- the same supported physical target must still be open, identified by inventory type plus world/location rather than wrapper-object identity;
- the source stack must still exist and still match an initial template;
- the live target must still contain a similar item.

For each eligible source stack, the target uses normal inventory insertion semantics through `Inventory.addItem`.

This allows:

- filling compatible partial stacks;
- using empty storage slots;
- partial transfer when capacity runs out.

Any amount the container cannot accept remains in the original player slot. The operation never drops overflow on the ground and never creates or destroys items intentionally.

## Game mode and lifecycle

Creative and Spectator are excluded.

The feature has no persistence and no polling. One explicit gesture inspects:

- one already-open storage inventory;
- at most 27 main-inventory slots.

Any next-tick task created by the feature belongs to the module and is cancelled during module disable.

Configuration:

```yaml
qol:
  deposit-matching:
    enabled: true
```

When disabled, the listener is not registered and vanilla behavior remains authoritative.

## Acceptance — LW-200 automated slice

LW-200 is automated-validated only when the exact candidate revision:

- compiles against the project's Paper 26.3 dependency with Java 25;
- passes `./gradlew clean test build` in GitHub Actions;
- has regression coverage for the trigger predicate and main-inventory slot boundary;
- preserves the supported-target and Vanilla+ boundaries above during code review.

Automated validation does not prove client-facing inventory behavior.

## Acceptance — LW-201 runtime slice

LW-201 remains pending until the scenarios recorded in `RUNTIME_VALIDATION_BACKLOG.md` are exercised on a real Paper server/player session.

Pending LW-201 does not block an independent future QoL slice under the project's deferred-runtime policy.
