# Living World — Deposit Matching QoL

- **Milestone:** M20
- **Implementation task:** LW-200
- **Runtime validation task:** LW-201
- **Branch:** `qol/deposit-matching`
- **Status:** LW-200 automated-validated through the closed-container gesture and bounded Paper presentation; LW-201 runtime validation pending.
- **Product framing:** Vanilla+ convenience for already-organized storage. This is not sorting, classification, storage networking or automation.

## Player interactions

Deposit Matching has two explicit gestures over the same matching/transfer rules.

### Open-storage gesture

1. open a supported chest, double chest, barrel or shulker box;
2. keep the cursor empty;
3. Shift + right-click an **empty slot in the container's top inventory**.

The clicked empty slot acts only as the gesture surface. The operation is not limited to that slot.

A completely occupied container has no open-GUI Deposit Matching gesture even when an existing stack still has capacity. This preserves normal Shift + right-click behavior on occupied slots instead of stealing a vanilla inventory action.

### Closed-storage gesture

1. look at a supported physical chest, double chest, barrel or shulker box;
2. sneak;
3. keep the **main hand empty**;
4. right-click the block.

When the complete gesture is accepted, Living World denies only that block/item interaction so the GUI stays closed, then performs Deposit Matching on the next server tick.

The paired offhand interaction for the same click/block is suppressed for at most one additional server tick so an offhand item cannot reopen/use the storage after the main-hand gesture. The offhand itself is never a Deposit Matching source.

If the target is unsafe or ambiguous, Living World does not take ownership of the interaction and vanilla/other-plugin behavior remains authoritative.

## Closed-target safety

Closed-storage Deposit Matching is fail-closed.

The gesture is rejected when:

- another plugin has already denied use of the interacted block;
- the player is in Creative or Spectator;
- the storage is currently viewed by another player;
- any physical half of the target is locked;
- any physical half still has an active loot table;
- a chest half reports itself blocked;
- a shulker box does not have passable space in its opening direction.

The access checks are repeated on the deferred transfer tick. If the block changed, became inaccessible, gained viewers or no longer resolves to the expected storage type, nothing moves.

This slice intentionally does not bypass protection-plugin decisions, locked storage, unopened loot containers or obvious vanilla physical opening constraints.

## Matching rule

At trigger time, Living World snapshots the non-empty storage contents already present in the target.

A player item is eligible only when one of those initial template stacks is `ItemStack.isSimilar` to it. Stack amount is ignored; item type and item data/components must match according to Paper/Bukkit semantics.

Consequences:

- cobblestone deposits only if matching cobblestone already exists in the container;
- an unrelated item is never introduced merely because there is free space;
- differently named, enchanted or otherwise different-component items do not match a normal stack unless Paper considers them similar;
- items added by Deposit Matching during the same operation do not create new eligibility categories.

Before each transfer, Living World rechecks that an equivalent item still exists in the live target. If another action removed the category before transfer, that source item remains with the player.

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

The adapter checks both inventory type and the physical vanilla holder. This intentionally excludes plugin-created CHEST-type GUIs that only mimic a chest inventory.

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

Ender Chest remains an explicit future product decision because it is player-owned remote storage rather than ordinary physical shared storage.

## Transfer semantics

Both gestures defer inventory mutation to the next server tick.

For the open-GUI gesture, the same supported physical inventory must still be open.

For the closed-block gesture, the same physical block storage must still resolve and pass the closed-target safety checks.

For each eligible source stack, the target uses normal inventory insertion semantics through `Inventory.addItem`. This allows:

- filling compatible partial stacks;
- using empty storage slots;
- partial transfer when capacity runs out.

Any amount the container cannot accept remains in the original player slot. The operation never drops overflow on the ground and never intentionally creates or destroys items.

## Vanilla Paper presentation

Successful **closed-block** transfers may produce short optional feedback. Presentation does not participate in matching or transfer decisions.

The initial Paper-only presentation:

- uses the real transferred ItemStacks as visual representatives;
- deduplicates equal categories and spawns at most **3 ItemDisplay entities**;
- moves them from near the player toward the clicked storage over **6 ticks**;
- removes every display after **9 ticks**;
- marks display entities non-persistent and owns their cleanup on module disable;
- plays one quiet vanilla item-pickup sound near arrival;
- visually opens supported `Lidded` storage only when the target has no viewers and no lid is already forced/open.

A forced lid is owned by the presentation layer. It normally closes after 10 ticks. If another player opens the container during that interval, Living World waits until the inventory has no viewers before releasing/closing its forced lid. All pending presentation tasks, displays and forced lids are cleaned on disable.

The presentation uses public Paper/Bukkit APIs only. No NMS, packets or client mod are required.

A future optional resource pack may enrich models/trails/sounds, but it is not part of M20 and must reuse the same presentation input rather than duplicate gameplay rules.

## Game mode, performance and lifecycle

Creative and Spectator are excluded.

The feature has no persistence and no polling. One explicit gesture inspects:

- one target storage inventory;
- at most 27 main-inventory slots;
- at most two physical holders for a double chest;
- for a closed shulker, one adjacent opening-space block.

The visual layer creates at most 3 short-lived display entities per successful closed operation.

Any next-tick transfer task and every presentation task/entity/lid belongs to the module and is cleaned during disable.

Configuration:

```yaml
qol:
  deposit-matching:
    enabled: true
    presentation:
      enabled: true
```

When `deposit-matching.enabled` is false, neither gesture is registered. When only `presentation.enabled` is false, deposits still work but no Deposit Matching visual presentation is created.

## Acceptance — LW-200 automated slice

LW-200 is automated-validated only when the exact candidate revision:

- compiles against the project's Paper 26.3 dependency with Java 25;
- passes `./gradlew clean test build` in GitHub Actions;
- has regression coverage for both trigger predicates and the main-inventory slot boundary;
- preserves closed-target fail-closed rules during code review;
- keeps presentation bounded and lifecycle-owned;
- preserves the supported-target and Vanilla+ boundaries above.

Automated validation does not prove client-facing lid/display animation or real inventory interaction behavior.

## Acceptance — LW-201 runtime slice

LW-201 remains pending until the scenarios recorded in `RUNTIME_VALIDATION_BACKLOG.md` are exercised on a real Paper server/player session, including the closed gesture and presentation.

Pending LW-201 does not block an independent future QoL slice under the project's deferred-runtime policy.
