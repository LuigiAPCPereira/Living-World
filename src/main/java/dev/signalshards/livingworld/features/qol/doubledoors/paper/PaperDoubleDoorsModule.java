package dev.signalshards.livingworld.features.qol.doubledoors.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PaperDoubleDoorsModule implements LivingWorldModule, Listener {
    private static final List<BlockFace> HORIZONTAL_FACES = List.of(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    );

    private final JavaPlugin plugin;
    private final boolean enabled;
    private final DoorPairPolicy pairPolicy = new DoorPairPolicy();
    private final Set<BukkitTask> pendingTasks = new HashSet<>();

    public PaperDoubleDoorsModule(JavaPlugin plugin, boolean enabled) {
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

        for (BukkitTask task : List.copyOf(pendingTasks)) {
            task.cancel();
        }
        pendingTasks.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null
                || clicked.getType() == Material.IRON_DOOR
                || !(clicked.getBlockData() instanceof Door)) {
            return;
        }

        Block bottom = bottomHalf(clicked);
        int x = bottom.getX();
        int y = bottom.getY();
        int z = bottom.getZ();

        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    pendingTasks.remove(holder[0]);
                    synchronizePair(bottom.getWorld().getBlockAt(x, y, z));
                },
                1L
        );
        pendingTasks.add(holder[0]);
    }

    private void synchronizePair(Block sourceBottom) {
        if (sourceBottom.getType() == Material.IRON_DOOR
                || !(sourceBottom.getBlockData() instanceof Door source)
                || source.getHalf() != Bisected.Half.BOTTOM
                || source.isPowered()) {
            return;
        }

        List<Block> candidates = new ArrayList<>(2);
        for (BlockFace face : HORIZONTAL_FACES) {
            Block candidateBlock = sourceBottom.getRelative(face);
            if (candidateBlock.getType() != sourceBottom.getType()
                    || !(candidateBlock.getBlockData() instanceof Door candidate)
                    || candidate.getHalf() != Bisected.Half.BOTTOM) {
                continue;
            }

            if (pairPolicy.isCompatible(
                    sourceBottom.getType(),
                    source,
                    candidateBlock.getType(),
                    candidate
            )) {
                candidates.add(candidateBlock);
            }
        }

        if (candidates.size() != 1) {
            return;
        }

        setOpen(candidates.getFirst(), source.isOpen());
    }

    private Block bottomHalf(Block block) {
        Door door = (Door) block.getBlockData();
        return door.getHalf() == Bisected.Half.TOP
                ? block.getRelative(BlockFace.DOWN)
                : block;
    }

    private void setOpen(Block bottom, boolean open) {
        if (!(bottom.getBlockData() instanceof Door bottomDoor)
                || bottomDoor.isOpen() == open) {
            return;
        }

        bottomDoor.setOpen(open);
        bottom.setBlockData(bottomDoor);

        Block top = bottom.getRelative(BlockFace.UP);
        if (top.getBlockData() instanceof Door topDoor) {
            topDoor.setOpen(open);
            top.setBlockData(topDoor);
        }
    }
}
