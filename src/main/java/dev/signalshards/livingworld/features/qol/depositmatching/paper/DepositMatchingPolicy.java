package dev.signalshards.livingworld.features.qol.depositmatching.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

final class DepositMatchingPolicy {
    static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    static final int LAST_MAIN_INVENTORY_SLOT = 35;

    boolean shouldTrigger(
            boolean supportedStorage,
            boolean clickedTopInventory,
            boolean shiftRightClick,
            boolean clickedSlotEmpty,
            boolean cursorEmpty,
            boolean excludedGameMode
    ) {
        return supportedStorage
                && clickedTopInventory
                && shiftRightClick
                && clickedSlotEmpty
                && cursorEmpty
                && !excludedGameMode;
    }

    List<Integer> matchingSourceSlots(
            int storageLength,
            IntPredicate matchesPreexistingContainerItem
    ) {
        int lastSlot = Math.min(
                LAST_MAIN_INVENTORY_SLOT,
                storageLength - 1
        );
        if (lastSlot < FIRST_MAIN_INVENTORY_SLOT) {
            return List.of();
        }

        List<Integer> matches = new ArrayList<>();
        for (int slot = FIRST_MAIN_INVENTORY_SLOT; slot <= lastSlot; slot++) {
            if (matchesPreexistingContainerItem.test(slot)) {
                matches.add(slot);
            }
        }
        return List.copyOf(matches);
    }
}
