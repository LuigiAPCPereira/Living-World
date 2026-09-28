package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PaperWaystoneMenu implements Listener {
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final MessageCatalog messages;
    private final Material iconMaterial;

    public PaperWaystoneMenu(
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            MessageCatalog messages,
            Material iconMaterial
    ) {
        this.waystones = Objects.requireNonNull(
                waystones,
                "serviço de waystones"
        );
        this.travel = Objects.requireNonNull(
                travel,
                "viagem de waystones"
        );
        this.messages = Objects.requireNonNull(
                messages,
                "catálogo de mensagens"
        );
        this.iconMaterial = Objects.requireNonNull(
                iconMaterial,
                "ícone da waystone"
        );
    }

    public void open(Player player) {
        Objects.requireNonNull(player, "jogador");
        List<Waystone> activated = waystones.activatedWaystones(
                player.getUniqueId()
        );
        if (activated.isEmpty()) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.list-empty"
            ));
            return;
        }
        if (!WaystoneMenuLayout.canDisplay(activated.size())) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.menu-too-many",
                    activated.size(),
                    WaystoneMenuLayout.MAX_ENTRIES
            ));
            return;
        }

        var playerLocation = player.getLocation();
        UUID currentWorldId = player.getWorld().getUID();
        List<Waystone> destinations = WaystoneMenuLayout.orderDestinations(
                currentWorldId,
                playerLocation.getX(),
                playerLocation.getY(),
                playerLocation.getZ(),
                activated
        );

        int size = WaystoneMenuLayout.inventorySize(destinations.size());
        WaystoneMenuHolder holder = new WaystoneMenuHolder();
        Inventory inventory = Bukkit.createInventory(
                holder,
                size,
                Component.text(
                        messages.text("waystone.menu-title"),
                        NamedTextColor.DARK_AQUA
                )
        );
        holder.bind(inventory);

        for (int slot = 0; slot < destinations.size(); slot++) {
            Waystone waystone = destinations.get(slot);
            holder.bind(slot, waystone.id());
            inventory.setItem(
                    slot,
                    menuItem(
                            currentWorldId,
                            playerLocation.getX(),
                            playerLocation.getY(),
                            playerLocation.getZ(),
                            waystone
                    )
            );
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof WaystoneMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() != top
                || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        WaystoneId id = holder.idAt(event.getRawSlot());
        if (id == null) {
            return;
        }
        Waystone target = waystones.findActivated(
                player.getUniqueId(),
                id
        ).orElse(null);
        player.closeInventory();
        if (target == null) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.menu-stale"
            ));
            return;
        }

        PaperWaystoneTravelFeedback.sendStart(
                player,
                messages,
                target.name()
        );
        travel.travel(player, target.id()).thenAccept(result ->
                PaperWaystoneTravelFeedback.sendResult(
                        player,
                        messages,
                        target.name(),
                        result
                )
        );
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
                instanceof WaystoneMenuHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack menuItem(
            UUID currentWorldId,
            double playerX,
            double playerY,
            double playerZ,
            Waystone waystone
    ) {
        ItemStack item = new ItemStack(iconMaterial);
        var meta = item.getItemMeta();
        meta.displayName(Component.text(
                waystone.name(),
                NamedTextColor.GOLD
        ));

        List<Component> lore = new ArrayList<>();
        World targetWorld = Bukkit.getWorld(waystone.worldId());
        String worldName = targetWorld == null
                ? messages.text("waystone.menu-world-unavailable")
                : targetWorld.getName();
        lore.add(Component.text(
                messages.text("waystone.menu-world", worldName),
                NamedTextColor.DARK_GRAY
        ));
        lore.add(Component.text(
                messages.text(
                        "waystone.menu-location",
                        waystone.x(),
                        waystone.y(),
                        waystone.z()
                ),
                NamedTextColor.GRAY
        ));
        if (waystone.worldId().equals(currentWorldId)) {
            lore.add(Component.text(
                    messages.text(
                            "waystone.menu-distance",
                            WaystoneMenuLayout.distanceBlocks(
                                    playerX,
                                    playerY,
                                    playerZ,
                                    waystone
                            )
                    ),
                    NamedTextColor.AQUA
            ));
        } else {
            lore.add(Component.text(
                    messages.text("waystone.menu-other-world"),
                    NamedTextColor.LIGHT_PURPLE
            ));
        }
        lore.add(Component.text(
                messages.text("waystone.menu-click"),
                NamedTextColor.GREEN
        ));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static final class WaystoneMenuHolder implements InventoryHolder {
        private final Map<Integer, WaystoneId> idsBySlot = new HashMap<>();
        private Inventory inventory;

        private void bind(Inventory inventory) {
            this.inventory = Objects.requireNonNull(inventory, "inventário");
        }

        private void bind(int slot, WaystoneId id) {
            idsBySlot.put(slot, Objects.requireNonNull(id, "waystone"));
        }

        private WaystoneId idAt(int slot) {
            return idsBySlot.get(slot);
        }

        @Override
        @org.jetbrains.annotations.NotNull
        public Inventory getInventory() {
            if (inventory == null) {
                throw new IllegalStateException(
                        "O inventário do menu ainda não foi vinculado"
                );
            }
            return inventory;
        }
    }
}
