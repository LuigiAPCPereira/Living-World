package dev.signalshards.livingworld.features.qol.autorefill.paper;

import java.util.function.IntPredicate;

final class HotbarAutoRefillPolicy {
    static final int FIRST_HOTBAR_SLOT = 0;
    static final int LAST_HOTBAR_SLOT = 8;
    static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    static final int LAST_MAIN_INVENTORY_SLOT = 35;

    boolean shouldRefill(
            int changedSlot,
            int heldSlot,
            int oldAmount,
            int oldMaxStackSize,
            boolean oldEmpty,
            boolean newEmpty
    ) {
        return changedSlot >= FIRST_HOTBAR_SLOT
                && changedSlot <= LAST_HOTBAR_SLOT
                && changedSlot == heldSlot
                && !oldEmpty
                && oldAmount == 1
                && oldMaxStackSize > 1
                && newEmpty;
    }

    int findReplacementSlot(int storageLength, IntPredicate matchesSlot) {
        int lastSlot = Math.min(
                LAST_MAIN_INVENTORY_SLOT,
                storageLength - 1
        );
        for (int slot = FIRST_MAIN_INVENTORY_SLOT; slot <= lastSlot; slot++) {
            if (matchesSlot.test(slot)) {
                return slot;
            }
        }
        return -1;
    }
}
