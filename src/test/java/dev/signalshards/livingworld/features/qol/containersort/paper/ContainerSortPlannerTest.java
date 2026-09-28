package dev.signalshards.livingworld.features.qol.containersort.paper;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerSortPlannerTest {
    private final ContainerSortPlanner planner = new ContainerSortPlanner();

    @Test
    void consolidatesExactVariantsAndSortsByMaterialKey() {
        List<TestStack> source = List.of(
                new TestStack("minecraft:stone", "normal", 32, 64),
                new TestStack("minecraft:dirt", "normal", 5, 64),
                new TestStack("minecraft:stone", "normal", 40, 64)
        );

        List<ContainerSortPlanner.PlannedStack<TestStack>> result =
                plan(source, 5).orElseThrow();

        assertEquals("minecraft:dirt", result.get(0).template().materialKey());
        assertEquals(5, result.get(0).amount());
        assertEquals("minecraft:stone", result.get(1).template().materialKey());
        assertEquals(64, result.get(1).amount());
        assertEquals("minecraft:stone", result.get(2).template().materialKey());
        assertEquals(8, result.get(2).amount());
    }

    @Test
    void keepsSameMaterialMetadataVariantsSeparateAndStable() {
        List<TestStack> source = List.of(
                new TestStack("minecraft:stone", "named-b", 2, 64),
                new TestStack("minecraft:dirt", "normal", 1, 64),
                new TestStack("minecraft:stone", "normal", 4, 64),
                new TestStack("minecraft:stone", "named-b", 3, 64)
        );

        List<ContainerSortPlanner.PlannedStack<TestStack>> result =
                plan(source, 5).orElseThrow();

        assertEquals("minecraft:dirt", result.get(0).template().materialKey());
        assertEquals("named-b", result.get(1).template().variant());
        assertEquals(5, result.get(1).amount());
        assertEquals("normal", result.get(2).template().variant());
        assertEquals(4, result.get(2).amount());
    }

    @Test
    void respectsPerVariantMaximumStackSize() {
        List<TestStack> source = List.of(
                new TestStack("minecraft:diamond_sword", "normal", 1, 1),
                new TestStack("minecraft:diamond_sword", "normal", 1, 1)
        );

        List<ContainerSortPlanner.PlannedStack<TestStack>> result =
                plan(source, 3).orElseThrow();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).amount());
        assertEquals(1, result.get(1).amount());
    }

    @Test
    void preservesTotalAmounts() {
        List<TestStack> source = List.of(
                new TestStack("minecraft:oak_log", "normal", 17, 64),
                new TestStack("minecraft:cobblestone", "normal", 64, 64),
                new TestStack("minecraft:oak_log", "normal", 64, 64),
                new TestStack("minecraft:cobblestone", "normal", 7, 64)
        );

        List<ContainerSortPlanner.PlannedStack<TestStack>> result =
                plan(source, 6).orElseThrow();

        assertEquals(81, total(result, "minecraft:oak_log"));
        assertEquals(71, total(result, "minecraft:cobblestone"));
    }

    @Test
    void failsClosedWhenNormalizingWouldNeedMoreSlotsThanAvailable() {
        List<TestStack> source = List.of(
                new TestStack("minecraft:stone", "normal", 128, 64)
        );

        Optional<List<ContainerSortPlanner.PlannedStack<TestStack>>> result =
                plan(source, 1);

        assertTrue(result.isEmpty());
    }

    private Optional<List<ContainerSortPlanner.PlannedStack<TestStack>>> plan(
            List<TestStack> source,
            int capacity
    ) {
        return planner.plan(
                source,
                capacity,
                (left, right) -> left.materialKey().equals(right.materialKey())
                        && left.variant().equals(right.variant()),
                TestStack::materialKey,
                TestStack::amount,
                TestStack::maxStackSize
        );
    }

    private int total(
            List<ContainerSortPlanner.PlannedStack<TestStack>> stacks,
            String materialKey
    ) {
        return stacks.stream()
                .filter(stack ->
                        stack.template().materialKey().equals(materialKey)
                )
                .mapToInt(ContainerSortPlanner.PlannedStack::amount)
                .sum();
    }

    private record TestStack(
            String materialKey,
            String variant,
            int amount,
            int maxStackSize
    ) {
    }
}
