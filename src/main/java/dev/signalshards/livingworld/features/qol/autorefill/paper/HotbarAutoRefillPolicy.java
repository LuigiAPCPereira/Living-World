package dev.signalshards.livingworld.features.qol.autorefill.paper;

import org.bukkit.inventory.ItemStack;

final class HotbarAutoRefillPolicy {
    static final int FIRST_HOTBAR_SLOT = 0;
    static final int LAST_HOTBAR_SLOT = 8;
    static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    static final int LAST_MAIN_INVENTORY_SLOT = 35;

    boolean shouldRefill(
            int changedSlot,
            int heldSlot,
            ItemStack oldItem,
            ItemStack newItem
    ) {
        return changedSlot >= FIRST_HOTBAR_SLOT
                && changedSlot <= LAST_HOTBAR_SLOT
                && changedSlot == heldSlot
                && !oldItem.isEmpty()
                && oldItem.getAmount() == 1
                && oldItem.getMaxStackSize() > 1
                && newItem.isEmpty();
    }

    int findReplacementSlot(ItemStack[] storageContents, ItemStack depletedItem) {
        int lastSlot = Math.min(
                LAST_MAIN_INVENTORY_SLOT,
                storageContents.length - 1
        );
        for (int slot = FIRST_MAIN_INVENTORY_SLOT; slot <= lastSlot; slot++) {
            ItemStack candidate = storageContents[slot];
            if (candidate != null
                    && !candidate.isEmpty()
                    && candidate.isSimilar(depletedItem)) {
                return slot;
            }
        }
        return -1;
    }
}
