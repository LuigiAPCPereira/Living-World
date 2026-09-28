package dev.signalshards.livingworld.features.qol.depositmatching.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PaperDepositMatchingModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final boolean enabled;
    private final DepositMatchingPolicy policy = new DepositMatchingPolicy();
    private final Set<BukkitTask> pendingTasks = new HashSet<>();

    public PaperDepositMatchingModule(JavaPlugin plugin, boolean enabled) {
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!enabled || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory target = event.getView().getTopInventory();
        boolean clickedTopInventory = event.getRawSlot() >= 0
                && event.getRawSlot() < target.getSize();
        boolean excludedGameMode = player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR;
        ItemStack clicked = event.getCurrentItem();

        if (!policy.shouldTrigger(
                isSupportedVanillaStorage(target),
                clickedTopInventory,
                event.getClick() == ClickType.SHIFT_RIGHT,
                clicked == null || clicked.isEmpty(),
                event.getCursor().isEmpty(),
                excludedGameMode
        )) {
            return;
        }

        StorageTargetKey targetKey = StorageTargetKey.from(target);
        if (targetKey == null) {
            return;
        }

        List<ItemStack> templates = snapshotTemplates(target);
        if (templates.isEmpty()) {
            return;
        }

        scheduleDeposit(player, targetKey, templates);
    }

    private void scheduleDeposit(
            Player player,
            StorageTargetKey targetKey,
            List<ItemStack> templates
    ) {
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    tryDeposit(player, targetKey, templates);
                }
        );
        pendingTasks.add(holder[0]);
    }

    private void tryDeposit(
            Player player,
            StorageTargetKey targetKey,
            List<ItemStack> templates
    ) {
        if (!player.isOnline()) {
            return;
        }

        Inventory liveTarget = player.getOpenInventory().getTopInventory();
        if (!isSupportedVanillaStorage(liveTarget)
                || !targetKey.matches(liveTarget)) {
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] storageContents = playerInventory.getStorageContents();

        List<Integer> sourceSlots = policy.matchingSourceSlots(
                storageContents.length,
                slot -> {
                    ItemStack source = storageContents[slot];
                    return source != null
                            && !source.isEmpty()
                            && matchesAny(templates, source);
                }
        );

        for (int sourceSlot : sourceSlots) {
            ItemStack source = playerInventory.getItem(sourceSlot);
            if (source == null
                    || source.isEmpty()
                    || !matchesAny(templates, source)
                    || !containsSimilar(liveTarget, source)) {
                continue;
            }

            ItemStack attemptedTransfer = source.clone();
            Map<Integer, ItemStack> leftovers = liveTarget.addItem(attemptedTransfer);
            int remainingAmount = leftovers.values()
                    .stream()
                    .mapToInt(ItemStack::getAmount)
                    .sum();
            int transferredAmount = source.getAmount() - remainingAmount;
            if (transferredAmount <= 0) {
                continue;
            }

            if (remainingAmount == 0) {
                playerInventory.setItem(sourceSlot, null);
            } else {
                ItemStack remainder = source.clone();
                remainder.setAmount(remainingAmount);
                playerInventory.setItem(sourceSlot, remainder);
            }
        }
    }

    private List<ItemStack> snapshotTemplates(Inventory target) {
        List<ItemStack> templates = new ArrayList<>();
        for (ItemStack item : target.getStorageContents()) {
            if (item != null && !item.isEmpty()) {
                templates.add(item.clone());
            }
        }
        return List.copyOf(templates);
    }

    private boolean matchesAny(List<ItemStack> templates, ItemStack candidate) {
        for (ItemStack template : templates) {
            if (template.isSimilar(candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSimilar(Inventory target, ItemStack candidate) {
        for (ItemStack item : target.getStorageContents()) {
            if (item != null && !item.isEmpty() && item.isSimilar(candidate)) {
                return true;
            }
        }
        return false;
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

    private record StorageTargetKey(
            InventoryType type,
            UUID worldId,
            double x,
            double y,
            double z
    ) {
        private static StorageTargetKey from(Inventory inventory) {
            Location location = inventory.getLocation();
            if (location == null || location.getWorld() == null) {
                return null;
            }
            return new StorageTargetKey(
                    inventory.getType(),
                    location.getWorld().getUID(),
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );
        }

        private boolean matches(Inventory inventory) {
            return equals(from(inventory));
        }
    }
}
