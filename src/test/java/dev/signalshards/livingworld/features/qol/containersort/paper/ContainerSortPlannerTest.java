package dev.signalshards.livingworld.features.qol.containersort.paper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerSortPlannerTest {
    private final ContainerSortPlanner planner = new ContainerSortPlanner();

    @Test
    void consolidatesSimilarStacksAndSortsByMaterialKey() {
        ItemStack[] source = new ItemStack[] {
                new ItemStack(Material.STONE, 32),
                null,
                new ItemStack(Material.DIRT, 5),
                new ItemStack(Material.STONE, 40),
                null
        };

        ItemStack[] result = planner.plan(source).orElseThrow();

        assertEquals(Material.DIRT, result[0].getType());
        assertEquals(5, result[0].getAmount());
        assertEquals(Material.STONE, result[1].getType());
        assertEquals(64, result[1].getAmount());
        assertEquals(Material.STONE, result[2].getType());
        assertEquals(8, result[2].getAmount());
        assertTrue(result[3] == null && result[4] == null);
    }

    @Test
    void respectsRealMaxStackSizeForNonStackableItems() {
        ItemStack[] source = new ItemStack[] {
                new ItemStack(Material.DIAMOND_SWORD, 1),
                new ItemStack(Material.DIAMOND_SWORD, 1),
                null
        };

        ItemStack[] result = planner.plan(source).orElseThrow();

        assertEquals(Material.DIAMOND_SWORD, result[0].getType());
        assertEquals(1, result[0].getAmount());
        assertEquals(Material.DIAMOND_SWORD, result[1].getType());
        assertEquals(1, result[1].getAmount());
    }

    @Test
    void preservesTotalAmounts() {
        ItemStack[] source = new ItemStack[] {
                new ItemStack(Material.OAK_LOG, 17),
                new ItemStack(Material.COBBLESTONE, 64),
                new ItemStack(Material.OAK_LOG, 64),
                new ItemStack(Material.COBBLESTONE, 7),
                null,
                null
        };

        ItemStack[] result = planner.plan(source).orElseThrow();

        assertEquals(81, total(result, Material.OAK_LOG));
        assertEquals(71, total(result, Material.COBBLESTONE));
    }

    @Test
    void failsClosedWhenNormalizingWouldNeedMoreSlotsThanAvailable() {
        ItemStack[] source = new ItemStack[] {
                new ItemStack(Material.STONE, 128)
        };

        Optional<ItemStack[]> result = planner.plan(source);

        assertTrue(result.isEmpty());
    }

    private int total(ItemStack[] stacks, Material material) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }
}
