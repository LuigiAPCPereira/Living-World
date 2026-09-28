package dev.signalshards.livingworld.features.qol.depositmatching.paper;

import com.destroystokyo.paper.loottable.LootableBlockInventory;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lockable;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
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
    private final PaperDepositMatchingPresentation presentation;
    private final Set<BukkitTask> pendingTasks = new HashSet<>();
    private final Map<UUID, SuppressedOffhandInteraction> suppressedOffhand =
            new HashMap<>();

    public PaperDepositMatchingModule(JavaPlugin plugin, boolean enabled) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.enabled = enabled;
        this.presentation = new PaperDepositMatchingPresentation(plugin);
    }

    @Override
    public void enable() {
        if (!enabled) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        presentation.enable();
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
        suppressedOffhand.clear();
        presentation.disable();
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
        boolean excludedGameMode = isExcludedGameMode(player);
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

        StorageTargetKey targetKey = StorageTargetKey.fromInventory(target);
        if (targetKey == null) {
            return;
        }

        List<ItemStack> templates = snapshotTemplates(target);
        if (templates.isEmpty()) {
            return;
        }

        scheduleDeposit(
                player,
                targetKey,
                templates,
                DepositTrigger.OPEN_GUI
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClosedStorageInteract(PlayerInteractEvent event) {
        if (!enabled
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null) {
            return;
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            suppressPairedOffhandInteraction(event);
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        BlockState state = clickedBlock.getState();
        Inventory target = supportedBlockInventory(state);
        boolean supportedStorage = target != null;

        if (target == null || !policy.shouldTriggerClosedStorage(
                supportedStorage,
                true,
                player.isSneaking(),
                player.getInventory().getItemInMainHand().isEmpty(),
                event.useInteractedBlock() != Event.Result.DENY,
                isClosedTargetAccessible(target),
                isExcludedGameMode(player)
        )) {
            return;
        }

        StorageTargetKey targetKey = StorageTargetKey.fromBlock(
                clickedBlock,
                target.getType()
        );
        if (targetKey == null) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        suppressedOffhand.put(
                player.getUniqueId(),
                new SuppressedOffhandInteraction(
                        BlockInteractionKey.from(clickedBlock),
                        plugin.getServer().getCurrentTick() + 1
                )
        );

        List<ItemStack> templates = snapshotTemplates(target);
        if (templates.isEmpty()) {
            return;
        }

        scheduleDeposit(
                player,
                targetKey,
                templates,
                DepositTrigger.CLOSED_BLOCK
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        suppressedOffhand.remove(event.getPlayer().getUniqueId());
    }

    private void suppressPairedOffhandInteraction(PlayerInteractEvent event) {
        SuppressedOffhandInteraction suppression =
                suppressedOffhand.get(event.getPlayer().getUniqueId());
        if (suppression == null) {
            return;
        }

        int currentTick = plugin.getServer().getCurrentTick();
        if (currentTick > suppression.expiresAtTick()) {
            suppressedOffhand.remove(event.getPlayer().getUniqueId());
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null
                || !suppression.block().equals(BlockInteractionKey.from(clickedBlock))) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }

    private void scheduleDeposit(
            Player player,
            StorageTargetKey targetKey,
            List<ItemStack> templates,
            DepositTrigger trigger
    ) {
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    tryDeposit(player, targetKey, templates, trigger);
                }
        );
        pendingTasks.add(holder[0]);
    }

    private void tryDeposit(
            Player player,
            StorageTargetKey targetKey,
            List<ItemStack> templates,
            DepositTrigger trigger
    ) {
        if (!player.isOnline()) {
            return;
        }

        Inventory liveTarget = switch (trigger) {
            case OPEN_GUI -> resolveOpenTarget(player, targetKey);
            case CLOSED_BLOCK -> resolveClosedTarget(targetKey);
        };
        if (liveTarget == null) {
            return;
        }

        List<ItemStack> transferred = transferMatching(
                player,
                liveTarget,
                templates
        );
        if (trigger == DepositTrigger.CLOSED_BLOCK
                && !transferred.isEmpty()) {
            Location targetCenter = targetKey.center(plugin.getServer());
            if (targetCenter != null) {
                presentation.show(
                        player,
                        liveTarget,
                        targetCenter,
                        transferred
                );
            }
        }
    }

    private Inventory resolveOpenTarget(
            Player player,
            StorageTargetKey targetKey
    ) {
        Inventory liveTarget = player.getOpenInventory().getTopInventory();
        if (!isSupportedVanillaStorage(liveTarget)
                || !targetKey.matches(liveTarget)) {
            return null;
        }
        return liveTarget;
    }

    private Inventory resolveClosedTarget(StorageTargetKey targetKey) {
        Inventory target = targetKey.resolve(plugin.getServer());
        if (target == null || !isClosedTargetAccessible(target)) {
            return null;
        }
        return target;
    }

    private List<ItemStack> transferMatching(
            Player player,
            Inventory liveTarget,
            List<ItemStack> templates
    ) {
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

        List<ItemStack> transferred = new ArrayList<>();
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

            ItemStack moved = source.clone();
            moved.setAmount(transferredAmount);
            transferred.add(moved);

            if (remainingAmount == 0) {
                playerInventory.setItem(sourceSlot, null);
            } else {
                ItemStack remainder = source.clone();
                remainder.setAmount(remainingAmount);
                playerInventory.setItem(sourceSlot, remainder);
            }
        }
        return List.copyOf(transferred);
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

    private boolean isExcludedGameMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR;
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

    private Inventory supportedBlockInventory(BlockState state) {
        if (state instanceof Chest chest) {
            return chest.getInventory();
        }
        if (state instanceof Barrel barrel) {
            return barrel.getInventory();
        }
        if (state instanceof ShulkerBox shulkerBox) {
            return shulkerBox.getInventory();
        }
        return null;
    }

    private boolean isClosedTargetAccessible(Inventory target) {
        if (!target.getViewers().isEmpty()) {
            return false;
        }

        List<InventoryHolder> holders = physicalHolders(target);
        if (holders.isEmpty()) {
            return false;
        }

        for (InventoryHolder holder : holders) {
            if (holder instanceof Lockable lockable && lockable.isLocked()) {
                return false;
            }
            if (holder instanceof LootableBlockInventory lootable
                    && lootable.hasLootTable()) {
                return false;
            }
            if (holder instanceof Chest chest && chest.isBlocked()) {
                return false;
            }
            if (holder instanceof ShulkerBox shulkerBox
                    && !hasShulkerOpeningSpace(shulkerBox)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasShulkerOpeningSpace(ShulkerBox shulkerBox) {
        if (!(shulkerBox.getBlock().getBlockData() instanceof Directional directional)) {
            return false;
        }
        return shulkerBox.getBlock()
                .getRelative(directional.getFacing())
                .isPassable();
    }

    private List<InventoryHolder> physicalHolders(Inventory target) {
        InventoryHolder holder = target.getHolder();
        if (holder instanceof DoubleChest doubleChest) {
            List<InventoryHolder> holders = new ArrayList<>(2);
            if (doubleChest.getLeftSide() != null) {
                holders.add(doubleChest.getLeftSide());
            }
            if (doubleChest.getRightSide() != null) {
                holders.add(doubleChest.getRightSide());
            }
            return List.copyOf(holders);
        }
        return holder == null ? List.of() : List.of(holder);
    }

    private enum DepositTrigger {
        OPEN_GUI,
        CLOSED_BLOCK
    }

    private record SuppressedOffhandInteraction(
            BlockInteractionKey block,
            int expiresAtTick
    ) {
    }

    private record BlockInteractionKey(
            UUID worldId,
            int x,
            int y,
            int z
    ) {
        private static BlockInteractionKey from(Block block) {
            return new BlockInteractionKey(
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
        }
    }

    private record StorageTargetKey(
            InventoryType type,
            UUID worldId,
            int x,
            int y,
            int z
    ) {
        private static StorageTargetKey fromInventory(Inventory inventory) {
            Block block = representativeBlock(inventory.getHolder());
            if (block == null) {
                return null;
            }
            return fromBlock(block, inventory.getType());
        }

        private static StorageTargetKey fromBlock(
                Block block,
                InventoryType type
        ) {
            return new StorageTargetKey(
                    type,
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
        }

        private boolean matches(Inventory inventory) {
            return equals(fromInventory(inventory));
        }

        private Inventory resolve(Server server) {
            Block block = resolveBlock(server);
            if (block == null) {
                return null;
            }

            BlockState state = block.getState();
            Inventory inventory;
            if (state instanceof Chest chest) {
                inventory = chest.getInventory();
            } else if (state instanceof Barrel barrel) {
                inventory = barrel.getInventory();
            } else if (state instanceof ShulkerBox shulkerBox) {
                inventory = shulkerBox.getInventory();
            } else {
                return null;
            }
            return inventory.getType() == type ? inventory : null;
        }

        private Location center(Server server) {
            Block block = resolveBlock(server);
            return block == null
                    ? null
                    : block.getLocation().add(0.5, 0.65, 0.5);
        }

        private Block resolveBlock(Server server) {
            var world = server.getWorld(worldId);
            return world == null ? null : world.getBlockAt(x, y, z);
        }

        private static Block representativeBlock(InventoryHolder holder) {
            if (holder instanceof BlockInventoryHolder blockHolder) {
                return blockHolder.getBlock();
            }
            if (holder instanceof DoubleChest doubleChest) {
                Block left = representativeBlock(doubleChest.getLeftSide());
                return left != null
                        ? left
                        : representativeBlock(doubleChest.getRightSide());
            }
            return null;
        }
    }
}
