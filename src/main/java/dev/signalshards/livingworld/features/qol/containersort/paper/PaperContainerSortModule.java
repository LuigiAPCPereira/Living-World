package dev.signalshards.livingworld.features.qol.containersort.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.GameMode;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PaperContainerSortModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final boolean enabled;
    private final ContainerSortPolicy policy = new ContainerSortPolicy();
    private final ContainerSortPlanner planner = new ContainerSortPlanner();
    private final Set<BukkitTask> pendingTasks = new HashSet<>();

    public PaperContainerSortModule(JavaPlugin plugin, boolean enabled) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.enabled = enabled;
    }

    @Override
    public void enable() {
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (!enabled) {
            return;
        }

        for (BukkitTask task : Set.copyOf(pendingTasks)) {
            task.cancel();
        }
        pendingTasks.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!enabled || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory target = event.getView().getTopInventory();
        boolean clickedTopInventory = event.getRawSlot() >= 0
                && event.getRawSlot() < target.getSize();
        ItemStack clicked = event.getCurrentItem();

        if (!policy.shouldTrigger(
                isSupportedVanillaStorage(target),
                clickedTopInventory,
                event.getClick() == ClickType.SHIFT_LEFT,
                event.getAction() == InventoryAction.NOTHING,
                clicked == null || clicked.isEmpty(),
                event.getCursor().isEmpty(),
                isExclusiveViewer(target, player),
                isExcludedGameMode(player)
        )) {
            return;
        }

        StorageTargetKey targetKey = StorageTargetKey.from(target);
        if (targetKey == null) {
            return;
        }

        ItemStack[] snapshot = cloneContents(target.getStorageContents());
        scheduleSort(player, targetKey, snapshot);
    }

    private void scheduleSort(
            Player player,
            StorageTargetKey targetKey,
            ItemStack[] snapshot
    ) {
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    trySort(player, targetKey, snapshot);
                }
        );
        pendingTasks.add(holder[0]);
    }

    private void trySort(
            Player player,
            StorageTargetKey targetKey,
            ItemStack[] snapshot
    ) {
        if (!player.isOnline()) {
            return;
        }

        Inventory liveTarget = player.getOpenInventory().getTopInventory();
        if (!isSupportedVanillaStorage(liveTarget)
                || !targetKey.matches(liveTarget)
                || !isExclusiveViewer(liveTarget, player)) {
            return;
        }

        ItemStack[] liveContents = cloneContents(liveTarget.getStorageContents());
        if (!sameContents(snapshot, liveContents)) {
            return;
        }

        List<ItemStack> sortable = new ArrayList<>();
        for (ItemStack item : liveContents) {
            if (item != null && !item.isEmpty()) {
                sortable.add(item.clone());
            }
        }

        Optional<List<ContainerSortPlanner.PlannedStack<ItemStack>>> planned =
                planner.plan(
                        sortable,
                        liveContents.length,
                        ItemStack::isSimilar,
                        item -> item.getType().getKey().toString(),
                        ItemStack::getAmount,
                        ItemStack::getMaxStackSize
                );
        if (planned.isEmpty()) {
            return;
        }

        ItemStack[] sorted = materialize(
                liveContents.length,
                planned.get()
        );
        if (sameContents(liveContents, sorted)) {
            return;
        }

        liveTarget.setStorageContents(sorted);
        player.updateInventory();
    }

    private ItemStack[] materialize(
            int capacity,
            List<ContainerSortPlanner.PlannedStack<ItemStack>> planned
    ) {
        ItemStack[] result = new ItemStack[capacity];
        for (int slot = 0; slot < planned.size(); slot++) {
            var stack = planned.get(slot);
            ItemStack item = stack.template().clone();
            item.setAmount(stack.amount());
            result[slot] = item;
        }
        return result;
    }

    private boolean isSupportedVanillaStorage(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        return switch (inventory.getType()) {
            case CHEST -> holder instanceof Chest || holder instanceof DoubleChest;
            case BARREL -> holder instanceof Barrel;
            case SHULKER_BOX -> holder instanceof ShulkerBox;
            default -> false;
        };
    }

    private boolean isExclusiveViewer(Inventory inventory, Player player) {
        return inventory.getViewers().size() == 1
                && inventory.getViewers().contains(player);
    }

    private boolean isExcludedGameMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR;
    }

    private ItemStack[] cloneContents(ItemStack[] source) {
        ItemStack[] clone = new ItemStack[source.length];
        for (int slot = 0; slot < source.length; slot++) {
            ItemStack item = source[slot];
            if (item != null && !item.isEmpty()) {
                clone[slot] = item.clone();
            }
        }
        return clone;
    }

    private boolean sameContents(ItemStack[] left, ItemStack[] right) {
        if (left.length != right.length) {
            return false;
        }

        for (int slot = 0; slot < left.length; slot++) {
            if (!sameStack(left[slot], right[slot])) {
                return false;
            }
        }
        return true;
    }

    private boolean sameStack(ItemStack left, ItemStack right) {
        boolean leftEmpty = left == null || left.isEmpty();
        boolean rightEmpty = right == null || right.isEmpty();

        if (leftEmpty || rightEmpty) {
            return leftEmpty == rightEmpty;
        }
        return left.equals(right);
    }

    private record StorageTargetKey(
            InventoryType type,
            List<BlockKey> blocks
    ) {
        private static StorageTargetKey from(Inventory inventory) {
            List<BlockKey> blocks = new ArrayList<>(2);
            addHolder(inventory.getHolder(), blocks);
            if (blocks.isEmpty()) {
                return null;
            }

            blocks.sort(Comparator.naturalOrder());
            return new StorageTargetKey(
                    inventory.getType(),
                    List.copyOf(blocks)
            );
        }

        private boolean matches(Inventory inventory) {
            return equals(from(inventory));
        }

        private static void addHolder(
                InventoryHolder holder,
                List<BlockKey> blocks
        ) {
            if (holder instanceof DoubleChest doubleChest) {
                addHolder(doubleChest.getLeftSide(), blocks);
                addHolder(doubleChest.getRightSide(), blocks);
                return;
            }

            if (holder instanceof BlockInventoryHolder blockHolder) {
                blocks.add(BlockKey.from(blockHolder.getBlock()));
            }
        }
    }

    private record BlockKey(
            UUID worldId,
            int x,
            int y,
            int z
    ) implements Comparable<BlockKey> {
        private static BlockKey from(Block block) {
            return new BlockKey(
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
        }

        @Override
        public int compareTo(BlockKey other) {
            int worldComparison = worldId.compareTo(other.worldId);
            if (worldComparison != 0) {
                return worldComparison;
            }

            int xComparison = Integer.compare(x, other.x);
            if (xComparison != 0) {
                return xComparison;
            }

            int yComparison = Integer.compare(y, other.y);
            if (yComparison != 0) {
                return yComparison;
            }

            return Integer.compare(z, other.z);
        }
    }
}
