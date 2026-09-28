package dev.signalshards.livingworld.features.qol.depositmatching.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class PaperDepositMatchingPresentation implements LivingWorldModule, Listener {
    private static final int MAX_ITEM_DISPLAYS = 3;
    private static final long MOVE_DELAY_TICKS = 1L;
    private static final int MOVE_DURATION_TICKS = 6;
    private static final long ARRIVAL_SOUND_TICKS = 7L;
    private static final long DISPLAY_LIFETIME_TICKS = 9L;
    private static final long LID_DURATION_TICKS = 10L;

    private final JavaPlugin plugin;
    private final Set<BukkitTask> pendingTasks = new HashSet<>();
    private final Set<ItemDisplay> displays = new HashSet<>();
    private final Map<StorageVisualKey, ForcedLid> forcedLids = new HashMap<>();
    private int lidGeneration;

    PaperDepositMatchingPresentation(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        for (BukkitTask task : Set.copyOf(pendingTasks)) {
            task.cancel();
        }
        pendingTasks.clear();

        for (ItemDisplay display : Set.copyOf(displays)) {
            if (display.isValid()) {
                display.remove();
            }
        }
        displays.clear();

        for (ForcedLid forced : List.copyOf(forcedLids.values())) {
            closeOwnedLids(forced);
        }
        forcedLids.clear();
        HandlerList.unregisterAll(this);
    }

    void show(
            Player player,
            Inventory target,
            Location targetCenter,
            List<ItemStack> transferred
    ) {
        if (transferred.isEmpty()) {
            return;
        }

        animateLid(target);
        animateItems(player, targetCenter, transferred);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        StorageVisualKey key = StorageVisualKey.from(event.getInventory());
        ForcedLid forced = forcedLids.get(key);
        if (forced == null) {
            return;
        }

        runLater(1L, () -> tryCloseLid(key, forced.generation()));
    }

    private void animateLid(Inventory target) {
        if (!target.getViewers().isEmpty()) {
            return;
        }

        List<Lidded> lids = liddedStates(target);
        if (lids.isEmpty() || lids.stream().anyMatch(Lidded::isOpen)) {
            return;
        }

        StorageVisualKey key = StorageVisualKey.from(target);
        int generation = ++lidGeneration;
        ForcedLid previous = forcedLids.put(
                key,
                new ForcedLid(target, lids, generation)
        );

        if (previous == null) {
            for (Lidded lid : lids) {
                lid.open();
            }
        }

        runLater(
                LID_DURATION_TICKS,
                () -> tryCloseLid(key, generation)
        );
    }

    private void tryCloseLid(StorageVisualKey key, int generation) {
        ForcedLid forced = forcedLids.get(key);
        if (forced == null || forced.generation() != generation) {
            return;
        }
        if (!forced.inventory().getViewers().isEmpty()) {
            return;
        }

        closeOwnedLids(forced);
        forcedLids.remove(key);
    }

    private void closeOwnedLids(ForcedLid forced) {
        for (Lidded lid : forced.lids()) {
            if (lid instanceof BlockState state
                    && state.getBlock().getType() != state.getType()) {
                continue;
            }
            if (lid.isOpen()) {
                lid.close();
            }
        }
    }

    private void animateItems(
            Player player,
            Location targetCenter,
            List<ItemStack> transferred
    ) {
        List<ItemStack> representatives = representatives(transferred);
        if (representatives.isEmpty()) {
            return;
        }

        Location start = player.getLocation().clone().add(0.0, 1.25, 0.0);
        Vector look = player.getEyeLocation().getDirection().clone().normalize();
        Vector right = new Vector(-look.getZ(), 0.0, look.getX());
        if (right.lengthSquared() > 0.0) {
            right.normalize();
        }
        start.add(look.multiply(0.28)).add(right.multiply(0.20));

        for (int index = 0; index < representatives.size(); index++) {
            ItemStack visual = representatives.get(index).clone();
            visual.setAmount(1);

            Location displayStart = start.clone().add(
                    0.0,
                    index * 0.10,
                    0.0
            );
            ItemDisplay display = displayStart.getWorld().spawn(
                    displayStart,
                    ItemDisplay.class,
                    entity -> {
                        entity.setItemStack(visual);
                        entity.setBillboard(Display.Billboard.CENTER);
                        entity.setTeleportDuration(MOVE_DURATION_TICKS);
                        entity.setPersistent(false);
                        entity.setInvulnerable(true);
                    }
            );
            displays.add(display);

            double offset = (index - 1) * 0.10;
            Location destination = targetCenter.clone().add(
                    offset,
                    0.15 + index * 0.04,
                    -offset
            );
            runLater(MOVE_DELAY_TICKS, () -> {
                if (display.isValid()) {
                    display.teleport(destination);
                }
            });
            runLater(DISPLAY_LIFETIME_TICKS, () -> {
                displays.remove(display);
                if (display.isValid()) {
                    display.remove();
                }
            });
        }

        runLater(ARRIVAL_SOUND_TICKS, () -> {
            if (targetCenter.getWorld() != null) {
                targetCenter.getWorld().playSound(
                        targetCenter,
                        Sound.ENTITY_ITEM_PICKUP,
                        0.45f,
                        1.25f
                );
            }
        });
    }

    private List<ItemStack> representatives(List<ItemStack> transferred) {
        List<ItemStack> representatives = new ArrayList<>();
        for (ItemStack candidate : transferred) {
            boolean alreadyRepresented = representatives.stream()
                    .anyMatch(existing -> existing.isSimilar(candidate));
            if (alreadyRepresented) {
                continue;
            }

            representatives.add(candidate.clone());
            if (representatives.size() == MAX_ITEM_DISPLAYS) {
                break;
            }
        }
        return List.copyOf(representatives);
    }

    private List<Lidded> liddedStates(Inventory target) {
        InventoryHolder holder = target.getHolder();
        if (holder instanceof Lidded lidded) {
            return List.of(lidded);
        }
        if (!(holder instanceof DoubleChest doubleChest)) {
            return List.of();
        }

        List<Lidded> lids = new ArrayList<>(2);
        addLidded(lids, doubleChest.getLeftSide());
        addLidded(lids, doubleChest.getRightSide());
        return List.copyOf(lids);
    }

    private void addLidded(List<Lidded> lids, InventoryHolder holder) {
        if (holder instanceof Lidded lidded) {
            lids.add(lidded);
        }
    }

    private void runLater(long delayTicks, Runnable action) {
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    action.run();
                },
                delayTicks
        );
        pendingTasks.add(holder[0]);
    }

    private record ForcedLid(
            Inventory inventory,
            List<Lidded> lids,
            int generation
    ) {
    }

    private record StorageVisualKey(
            String world,
            double x,
            double y,
            double z
    ) {
        private static StorageVisualKey from(Inventory inventory) {
            Location location = inventory.getLocation();
            if (location == null || location.getWorld() == null) {
                return new StorageVisualKey("", 0.0, 0.0, 0.0);
            }
            return new StorageVisualKey(
                    location.getWorld().getUID().toString(),
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );
        }
    }
}
