package dev.signalshards.livingworld.features.qol.containersort.paper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

final class ContainerSortPlanner {
    <T> Optional<List<PlannedStack<T>>> plan(
            List<T> source,
            int capacity,
            BiPredicate<T, T> similar,
            Function<T, String> sortKey,
            ToIntFunction<T> amount,
            ToIntFunction<T> maxStackSize
    ) {
        List<Group<T>> groups = new ArrayList<>();

        for (T item : source) {
            Group<T> existing = findSimilar(groups, item, similar);
            if (existing == null) {
                groups.add(new Group<>(
                        item,
                        amount.applyAsInt(item)
                ));
            } else {
                existing.add(amount.applyAsInt(item));
            }
        }

        groups.sort(Comparator.comparing(group ->
                sortKey.apply(group.template())
        ));

        List<PlannedStack<T>> result = new ArrayList<>();
        for (Group<T> group : groups) {
            int max = maxStackSize.applyAsInt(group.template());
            if (max <= 0) {
                return Optional.empty();
            }

            long remaining = group.amount();
            while (remaining > 0) {
                if (result.size() >= capacity) {
                    return Optional.empty();
                }

                int plannedAmount = (int) Math.min(remaining, max);
                result.add(new PlannedStack<>(
                        group.template(),
                        plannedAmount
                ));
                remaining -= plannedAmount;
            }
        }

        return Optional.of(List.copyOf(result));
    }

    private <T> Group<T> findSimilar(
            List<Group<T>> groups,
            T candidate,
            BiPredicate<T, T> similar
    ) {
        for (Group<T> group : groups) {
            if (similar.test(group.template(), candidate)) {
                return group;
            }
        }
        return null;
    }

    record PlannedStack<T>(T template, int amount) {
    }

    private static final class Group<T> {
        private final T template;
        private long amount;

        private Group(T template, long amount) {
            this.template = template;
            this.amount = amount;
        }

        private T template() {
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
