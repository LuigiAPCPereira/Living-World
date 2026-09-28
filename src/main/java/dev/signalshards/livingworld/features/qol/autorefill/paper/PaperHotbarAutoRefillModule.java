package dev.signalshards.livingworld.features.qol.autorefill.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PaperHotbarAutoRefillModule implements LivingWorldModule, Listener {
    private static final int INTENT_SUPPRESSION_TICKS = 2;

    private final JavaPlugin plugin;
    private final boolean enabled;
    private final HotbarAutoRefillPolicy policy = new HotbarAutoRefillPolicy();
    private final Set<BukkitTask> pendingTasks = new HashSet<>();
    private final Map<UUID, Integer> suppressedUntilTick = new HashMap<>();

    public PaperHotbarAutoRefillModule(JavaPlugin plugin, boolean enabled) {
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
        suppressedUntilTick.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        if (!enabled) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (!policy.shouldRefill(
                event.getSlot(),
                inventory.getHeldItemSlot(),
                event.getOldItemStack(),
                event.getNewItemStack()
        )) {
            return;
        }

        scheduleRefill(
                player,
                event.getSlot(),
                event.getOldItemStack().clone()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        suppressIntentionalChange(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        suppressIntentionalChange(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            suppressIntentionalChange(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            suppressIntentionalChange(player);
        }
    }

    private void scheduleRefill(Player player, int targetSlot, ItemStack depletedItem) {
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    tryRefill(player, targetSlot, depletedItem);
                },
                1L
        );
        pendingTasks.add(holder[0]);
    }

    private void tryRefill(Player player, int targetSlot, ItemStack depletedItem) {
        if (!player.isOnline() || isIntentionalChangeSuppressed(player.getUniqueId())) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack currentTarget = inventory.getItem(targetSlot);
        if (currentTarget != null && !currentTarget.isEmpty()) {
            return;
        }

        int sourceSlot = policy.findReplacementSlot(
                inventory.getStorageContents(),
                depletedItem
        );
        if (sourceSlot < 0) {
            return;
        }

        ItemStack source = inventory.getItem(sourceSlot);
        if (source == null || source.isEmpty() || !source.isSimilar(depletedItem)) {
            return;
        }

        ItemStack replacement = source.clone();
        inventory.setItem(sourceSlot, null);
        inventory.setItem(targetSlot, replacement);
    }

    private void suppressIntentionalChange(Player player) {
        suppressedUntilTick.put(
                player.getUniqueId(),
                plugin.getServer().getCurrentTick() + INTENT_SUPPRESSION_TICKS
        );
    }

    private boolean isIntentionalChangeSuppressed(UUID playerId) {
        Integer untilTick = suppressedUntilTick.get(playerId);
        if (untilTick == null) {
            return false;
        }

        if (plugin.getServer().getCurrentTick() <= untilTick) {
            return true;
        }

        suppressedUntilTick.remove(playerId);
        return false;
    }
}
