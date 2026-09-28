package dev.signalshards.livingworld.features.qol.containersort.paper;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class ContainerSortPlanner {
    Optional<ItemStack[]> plan(ItemStack[] source) {
        List<Group> groups = new ArrayList<>();

        for (ItemStack item : source) {
            if (item == null || item.isEmpty()) {
                continue;
            }

            Group existing = findSimilar(groups, item);
            if (existing == null) {
                groups.add(new Group(item.clone(), item.getAmount()));
            } else {
                existing.add(item.getAmount());
            }
        }

        groups.sort(Comparator.comparing(group ->
                group.template().getType().getKey().toString()
        ));

        ItemStack[] result = new ItemStack[source.length];
        int outputSlot = 0;

        for (Group group : groups) {
            int maxStackSize = group.template().getMaxStackSize();
            if (maxStackSize <= 0) {
                return Optional.empty();
            }

            long remaining = group.amount();
            while (remaining > 0) {
                if (outputSlot >= result.length) {
                    return Optional.empty();
                }

                int amount = (int) Math.min(remaining, maxStackSize);
                ItemStack stack = group.template().clone();
                stack.setAmount(amount);
                result[outputSlot++] = stack;
                remaining -= amount;
            }
        }

        return Optional.of(result);
    }

    private Group findSimilar(List<Group> groups, ItemStack candidate) {
        for (Group group : groups) {
            if (group.template().isSimilar(candidate)) {
                return group;
            }
        }
        return null;
    }

    private static final class Group {
        private final ItemStack template;
        private long amount;

        private Group(ItemStack template, long amount) {
            this.template = template;
            this.amount = amount;
        }

        private ItemStack template() {
            return template;
        }

        private long amount() {
            return amount;
        }

        private void add(int additionalAmount) {
            amount += additionalAmount;
        }
    }
}
