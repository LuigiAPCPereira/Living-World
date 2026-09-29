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

### LW-163 — World landmark Discovery real-player smoke

**Branch:** `discovery/m16-world-landmarks`.

**Current state:** pending; runtime validation may be deferred after automated LW-160..LW-162 gates are green.

**Goal:** prove that declaratively tagged generated structures become durable world-scope discoveries without radius searches, coordinate-based identity or duplicate presentation.

**Prepare**

- Provide a datapack/server data setup where `#livingworld:landmarks` contains at least one generated structure key.
- Prefer two distinct generated instances of an eligible structure; a custom/datapack namespace is useful when available.

**Scenarios**

- Enter the bounding box of an undiscovered eligible generated structure and confirm exactly one Discovery presentation.
- Continue moving inside the same structure and confirm no duplicate presentation.
- Leave and re-enter the same instance and confirm it remains known/silent.
- Enter a second eligible generated instance and confirm it becomes a distinct world discovery.
- Disconnect/reconnect and revisit both instances; confirm identity remains durable.
- Restart the server, revisit a known instance and confirm the same structure UUID is reused rather than creating a new discovery.
- Remove/omit `#livingworld:landmarks` in a controlled setup and confirm the feature becomes safely inert rather than failing plugin startup.
- When a non-`minecraft` structure is available, include it in the tag and confirm namespace-agnostic behavior.
- Confirm server logs stay clean and no nearby/unloaded chunks are force-loaded by the feature.

**Evidence to record**

- Branch + exact commit tested.
- Paper/Purpur and client versions.
- Datapack/tag contents used by the scenario.
- First-instance presentation, repeated-entry silence and second-instance presentation.
- Evidence that restart/reconnect preserved the same generated-structure identity.
- Missing-tag behavior.
- Relevant logs and any setup/cleanup performed.

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

## Completed runtime validations

Move entries here only after the exact runtime evidence exists. Preserve the original task ID and add the evidence format fields above.

None added by this document.
