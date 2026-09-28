# Living World — Runtime Validation Backlog

## Purpose

This document is the durable queue for validations that require a real Paper server, a connected player/client, visual confirmation, home-machine access or another environment that is not always available during implementation.

Its goal is to preserve validation debt explicitly without forcing unrelated Living World work to stop.

This document does **not** replace `TASKLIST.md` as the canonical task/state inventory. It records the runtime scenarios still owed and the evidence required to close them.

## Operating policy

1. **Automated gates remain mandatory for implementation work.**
   - New code must compile.
   - Relevant unit/regression tests must exist where practical.
   - The branch must pass the project's automated build gate, currently `./gradlew clean test build`, through GitHub Actions when the workflow is available on that branch.
   - Scope/architecture review remains required before claiming an implementation slice is ready for runtime validation.

2. **Runtime-only validation may be deferred when the required environment is unavailable.**
   - A pending real-player/client/server smoke does not block starting a separate, independent QoL slice.
   - The deferred smoke must be added here with task ID, branch/HEAD when relevant, exact scenario and expected evidence.
   - The implementation must remain described truthfully as automated-validated/runtime-pending until the smoke is completed.

3. **Deferral never means implicit success.**
   - Do not convert a pending runtime item to validated because CI is green.
   - Do not infer visual/gameplay correctness from unit tests alone.
   - When runtime evidence is finally collected, record the date, relevant branch/commit, server/client version where useful, result and any follow-up defect.

4. **Unrelated work may continue.**
   - A pending runtime smoke for one module does not block development of another module when their behavior and ownership are independent.
   - If a later change touches the same code path or invalidates the assumptions of a pending smoke, update that smoke before continuing.

5. **Release/merge decisions stay evidence-aware.**
   - Runtime-pending work may exist on development branches while other slices progress.
   - Before making a release claim that includes a runtime-pending feature, execute the relevant scenarios or record an explicit owner decision accepting the remaining risk.
   - Never describe a feature as fully runtime-validated while its entry here is still open.

## Evidence format

For each completed runtime item, record:

- **Date**
- **Task / feature**
- **Branch + commit SHA** tested
- **Paper/Purpur version**
- **Client version/mod context**, only when it matters
- **Scenario result**
- **Relevant logs/screenshots/video/console observations**, when useful
- **Cleanup/restoration performed**, if the test changed world state
- **Follow-up issue/task**, if the result exposed a defect

---

## Pending runtime validations

### LW-131 — Waystone navigation-menu smoke

**Current state:** pending.

**Goal:** prove that the M13 menu/readability changes work for a real player without changing access or travel semantics.

**Prepare**

- At least three discovered Waystones.
- Prefer two in the player's current world at clearly different distances and one in another world.
- Player must have access to every Waystone used by the scenario.

**Scenarios**

- Open `/lw menu`.
- Confirm current-world destinations appear before cross-world destinations.
- Confirm current-world destinations are ordered nearest-first.
- Confirm each destination exposes the target world and coordinates.
- Confirm same-world destinations expose a rounded distance.
- Click a destination and confirm the existing travel flow still works.
- Confirm travel safety/access behavior is unchanged from the pre-M13 system.
- Reopen the menu after moving materially closer to another Waystone and confirm ordering reflects the new player position.

**Evidence to record**

- Visible menu ordering/lore.
- Successful travel to the selected stable destination.
- Any console warning/error.

---

### LW-142 — Discovery + Waystone integration smoke

**Current state:** partially exercised; first physical Waystone interaction already displayed `Descoberto`. The remaining behaviors below are still pending.

**Goal:** prove one-time Discovery presentation and durable discovery memory without regressing existing Waystone or climate behavior.

**Scenarios**

- Interact physically with a previously undiscovered Waystone and confirm one discovery title.
- Repeat the physical interaction and confirm no second discovery title.
- Exercise a legacy/already-activated Waystone that needs backfill and confirm it records once.
- Repeat the legacy/backfill interaction and confirm idempotency/silence.
- Rename a Waystone and confirm discovery identity/history remains coherent.
- Travel using a Waystone and confirm travel behavior remains unchanged.
- Run `/lw climate` in the same session and confirm the command/presentation coexists normally with Discovery.
- Destroy/remove the Waystone anchor and confirm the player's durable discovery memory is not deleted.
- Recreate or otherwise inspect the relevant Waystone flow after destruction and confirm no unexpected duplicate/discovery reset is introduced.

**Evidence to record**

- First title shown.
- Repeat interaction silent.
- Backfill once-only behavior.
- Rename/travel result.
- `/lw climate` result.
- Discovery persistence after anchor destruction.
- Server log cleanliness.

---

### LW-191 — Hotbar Auto-Refill real-player smoke

**Branch implementation:** `qol/hotbar-auto-refill`.

**Automated status:** LW-190 passed GitHub Actions `./gradlew clean test build`; this runtime matrix remains pending.

**Goal:** prove that the QoL removes repetitive restocking while preserving deliberate inventory actions and Vanilla+ item semantics.

#### A. Basic block refill

**Prepare**

- Selected hotbar slot contains exactly 1 Stone.
- Main-inventory slot 9..35 contains another Stone stack.
- No other intervention during the final placement.

**Action**

- Place the final Stone.

**Expected**

- After vanilla consumes the final item, one matching Stone stack from the main inventory moves into the same hotbar slot.
- Total Stone count changes only by the one block vanilla consumed.
- No duplicate item is created.

#### B. Main-inventory source only

**Prepare**

- Selected slot contains 1 Stone.
- Another Stone stack exists only in a different hotbar slot.

**Expected**

- No auto-refill.

Repeat with a matching stack only in offhand.

**Expected**

- No auto-refill.

#### C. First eligible main-inventory source

**Prepare**

- Selected slot contains 1 Stone.
- Two matching Stone stacks exist in different slots inside 9..35.

**Expected**

- The first matching main-inventory slot is moved into the depleted hotbar slot.
- The second source remains unchanged.

#### D. Metadata-aware matching

**Prepare**

- Selected slot contains a stackable item with metadata that differs from another same-material item in main inventory. A custom name is sufficient for the smoke if convenient.

**Expected**

- The different-metadata item is not used as replacement.
- An exact `ItemStack.isSimilar` match, if also present, may refill normally.

#### E. Food consumption

**Prepare**

- Selected slot contains exactly 1 stackable food item.
- An exact matching stack exists in main inventory.

**Action**

- Consume the final food item normally.

**Expected**

- Matching stack refills the same selected slot after vanilla consumption settles.
- Hunger/saturation behavior remains vanilla.

#### F. Projectile/throwable consumption

Use a naturally stackable throwable such as Snowball or Egg.

**Prepare**

- Selected slot contains exactly 1 item.
- Matching source stack exists in main inventory.

**Action**

- Throw/use the final item.

**Expected**

- Matching stack refills the depleted slot.
- Exactly one projectile/throwable is consumed by vanilla.

#### G. Vanilla replacement item wins

Use an interaction where vanilla leaves another item in the slot instead of emptying it, such as consuming the final Honey Bottle and receiving a Glass Bottle.

**Expected**

- Auto-refill does not overwrite the vanilla replacement item.

#### H. Non-stackable item exclusion

**Prepare**

- Selected slot contains a max-stack-size-1 item such as a Diamond Pickaxe.
- A same-type replacement exists in main inventory.

**Action**

- Remove/break/otherwise exhaust the selected item through normal gameplay where practical.

**Expected**

- No auto-refill.
- Tool replacement remains outside M19.

Also spot-check another max-stack-size-1 item if convenient.

#### I. Manual drop intent

**Prepare**

- Selected slot contains exactly 1 stackable item.
- Matching source exists in main inventory.

**Action**

- Deliberately drop the selected item.

**Expected**

- No auto-refill for that intentional drop interaction.

#### J. Manual inventory move intent

**Prepare**

- Selected slot contains exactly 1 stackable item.
- Matching source exists in main inventory.

**Action**

- Open inventory and deliberately click/drag/move the selected item out of the slot.

**Expected**

- No auto-refill fights the player's manual inventory action.

#### K. Main/offhand swap intent

**Prepare**

- Selected slot contains a candidate stackable item with a matching main-inventory source.

**Action**

- Perform the normal main-hand/offhand swap.

**Expected**

- No unintended auto-refill caused by the swap interaction.

#### L. Target changed before refill executes

**Action**

- Exhaust a one-item stack and immediately cause another legitimate item to occupy that slot before the deferred refill can apply, if reproducible.

**Expected**

- Living World preserves the newly occupied slot and does nothing.

#### M. Creative and Spectator

**Expected**

- Auto-refill does not operate in Creative or Spectator.

#### N. Configuration off

Set:

```yaml
qol:
  hotbar-auto-refill:
    enabled: false
```

Restart/reload using the project's normal safe procedure.

**Expected**

- Exhausting a normal selected stack behaves exactly like vanilla; no replacement is moved automatically.

#### O. Log/lifecycle sanity

During the smoke:

- join, use the feature, manipulate inventory manually, disconnect and reconnect;
- stop the server normally after testing.

**Expected**

- No Living World errors/warnings related to auto-refill.
- No task/lifecycle exception on disconnect or shutdown.

---

### LW-201 — Deposit Matching real-player smoke

**Branch implementation:** `qol/deposit-matching`.

**Automated status:** tracked separately as LW-200; this matrix is runtime-only and stays pending until a real Paper player session exercises it.

**Goal:** prove that Deposit Matching speeds up returns to an already-organized base without sorting for the player, touching deliberate loadout slots, duplicating items or hijacking unrelated inventory interactions.

#### A. Basic matching deposit

**Prepare**

- Open a normal chest with at least one Cobblestone stack already inside and at least one empty chest slot.
- Put Cobblestone and one unrelated item in player main-inventory slots 9..35.
- Keep hotbar layout unchanged and cursor empty.

**Action**

- Shift + right-click an empty slot in the chest's top inventory.

**Expected**

- Main-inventory Cobblestone moves into the chest.
- The unrelated item stays with the player.
- Total Cobblestone count is conserved.
- No hotbar item moves.

#### B. Exact metadata/component matching

**Prepare**

- Put a normal item in the target storage.
- In the player's main inventory, hold both an exact match and another same-material item with different metadata/components, such as a custom name where convenient.

**Expected**

- Only the exact `ItemStack.isSimilar` match is eligible.
- Different metadata is preserved in the player inventory.

#### C. Partial-capacity conservation

**Prepare**

- Leave only limited capacity for one already-present matching item type.
- Give the player more of that exact item than the target can accept.

**Expected**

- Only the amount actually accepted by the container moves.
- Remainder stays in the same player source slot.
- Container + player total is unchanged.

#### D. Main-inventory-only boundary

**Prepare**

- Put a matching stack in hotbar and another matching stack in slots 9..35.

**Expected**

- Main-inventory source deposits.
- Hotbar source stays untouched.

Repeat with a matching offhand item.

**Expected**

- Offhand stays untouched.

#### E. Supported vanilla storage

Repeat the basic scenario for:

- single chest;
- double chest;
- barrel;
- placed shulker box.

**Expected**

- Same matching behavior in each supported target.

#### F. Unsupported storage stays vanilla

Try the gesture in representative unsupported inventories where practical, such as a hopper, dispenser/dropper or furnace-like machine.

**Expected**

- Deposit Matching does not run.
- Vanilla interaction behavior remains authoritative.

If another plugin with a chest-style custom GUI is available during future testing, verify it is not treated as a physical vanilla chest.

#### G. Occupied-slot Shift + right-click stays vanilla

**Prepare**

- Open a supported storage with matching player items.
- Shift + right-click an **occupied** container slot.

**Expected**

- Deposit Matching does not trigger.
- The ordinary vanilla shift-click result for that occupied slot remains authoritative.

#### H. Non-empty cursor does not trigger

**Prepare**

- Pick up an item onto the cursor.
- Shift + right-click an empty top-inventory slot.

**Expected**

- Deposit Matching does not run.

#### I. Empty container does not classify player inventory

**Prepare**

- Open an empty supported container.

**Action**

- Use the Deposit Matching gesture.

**Expected**

- Nothing from the player inventory is moved.
- Living World does not invent a first category or automatically organize the empty storage.

#### J. Fully occupied container limitation

**Prepare**

- Fill every target slot, but leave spare room inside at least one existing stack.

**Expected**

- There is no empty-slot Deposit Matching gesture in M20.
- Shift + right-clicking an occupied stack remains vanilla.
- This is an intentional first-slice limitation, not an implicit sorter behavior.

#### K. Target removed or switched before deferred execution

If reproducible with a second player or rapid interaction:

- trigger Deposit Matching and close/switch the container before the deferred mutation;
- optionally have another player remove the last target template category before execution.

**Expected**

- Living World does not transfer into a different/closed target.
- A source category whose live target match disappeared remains with the player.
- No item duplication/loss occurs.

#### L. Creative and Spectator exclusion

**Expected**

- Deposit Matching does not operate in Creative or Spectator.

#### M. Configuration off

Set:

```yaml
qol:
  deposit-matching:
    enabled: false
```

Restart using the project's normal safe procedure.

**Expected**

- The gesture produces only vanilla inventory behavior; no bulk matching transfer occurs.

#### N. Closed-container basic gesture

**Prepare**

- Use Survival or Adventure.
- Put at least one matching category in a supported physical storage.
- Keep the player's main hand empty.
- Keep one exact matching stack in slots 9..35.

**Action**

- Sneak and right-click the closed storage block.

**Expected**

- The GUI does not open.
- Matching main-inventory items deposit using the same rules as the open-GUI path.
- Hotbar/offhand/armor remain untouched.
- The paired offhand interaction does not reopen/use the storage.

#### O. Closed-container access safety

Exercise representative blocked/unsafe states where practical:

- locked chest/barrel/shulker;
- chest blocked by vanilla obstruction;
- shulker with a non-passable block in its opening direction;
- storage currently being viewed by another player;
- naturally generated loot storage before its loot table has been consumed, when a safe fixture is available.

**Expected**

- Living World does not take ownership of unsafe/denied storage.
- No inventory transfer occurs.
- Vanilla/protection-plugin behavior remains authoritative.
- No loot generation/access rule is bypassed.

#### P. Protection-plugin denial

If the runtime environment includes a protection plugin, deny interaction with one supported storage and repeat the closed gesture.

**Expected**

- Deposit Matching does not bypass the denied block interaction.

If no protection plugin is installed, record this scenario as not exercised rather than inferring compatibility.

#### Q. Closed-container visual presentation

With `qol.deposit-matching.presentation.enabled: true`:

- perform a successful closed deposit with one category;
- repeat with at least three distinct eligible categories;
- if convenient, repeat with more than three eligible categories.

**Expected**

- A short visual reaction occurs only after a real transfer.
- The container lid reacts when Living World can safely own that animation.
- Real transferred item types are visually represented.
- At most three ItemDisplay representatives appear even when more categories move.
- Displays travel briefly toward the clicked storage and disappear automatically.
- One quiet vanilla pickup-style sound accompanies arrival.
- No ItemDisplay remains after the animation window.

#### R. Viewer/lid ownership

Use two players if available.

**Action**

- Player A triggers a closed deposit that starts the lid animation.
- Player B opens the same storage before Living World's planned visual close.

**Expected**

- Living World does not force-close the lid while the inventory still has viewers.
- After the final viewer closes, the temporary forced-lid state is released.
- The chest/barrel/shulker does not remain visually stuck open.

#### S. Presentation disabled

Set:

```yaml
qol:
  deposit-matching:
    enabled: true
    presentation:
      enabled: false
```

Restart using the normal safe procedure and repeat a successful closed deposit.

**Expected**

- Deposit Matching still transfers items.
- No Deposit Matching ItemDisplay/lid presentation is created.

#### T. Log/lifecycle sanity

During the smoke:

- use both Deposit Matching gestures repeatedly across supported storages;
- disconnect/reconnect;
- stop the server normally, including after at least one visual closed deposit.

**Expected**

- No Living World error/warning related to Deposit Matching.
- No pending-task, orphan ItemDisplay, forced-lid or shutdown exception.

**Evidence to record**

- Before/after item counts for at least basic and partial-capacity scenarios.
- Supported target types tested.
- Exact-metadata negative case.
- Hotbar/offhand preservation.
- Any console warnings/errors.
- Branch + exact commit SHA and Paper build tested.

---

## Completed runtime validations

Move entries here only after the exact runtime evidence exists. Preserve the original task ID and add the evidence format fields above.

None added by this document.
