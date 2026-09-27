package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class PaperWaystoneModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final PaperWaystoneSettings settings;
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final MessageCatalog messages;

    public PaperWaystoneModule(
            JavaPlugin plugin,
            PaperWaystoneSettings settings,
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            MessageCatalog messages
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração de waystones");
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.travel = Objects.requireNonNull(travel, "viagem de waystones");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.registerCommand(
                "livingworld",
                "Comandos do Living World",
                List.of("lw"),
                new PaperWaystoneCommand(waystones, travel, messages)
        );
    }

    @Override
    public void disable() {
        if (settings.enabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnchorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != settings.anchorMaterial()) {
            return;
        }

        WaystoneId id = WaystoneId.fromAnchor(
                block.getWorld().getUID(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
        Waystone waystone = waystones.find(id).orElse(null);
        boolean created = false;
        if (waystone == null) {
            waystone = new Waystone(
                    id,
                    defaultName(block),
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
            waystones.register(waystone);
            created = true;
        }

        boolean activated = waystones.activate(event.getPlayer().getUniqueId(), id);
        if (created) {
            event.getPlayer().sendPlainMessage(
                    messages.text("waystone.created-and-activated", waystone.name())
            );
        } else if (activated) {
            event.getPlayer().sendPlainMessage(
                    messages.text("waystone.activated", waystone.name())
            );
        } else {
            event.getPlayer().sendPlainMessage(
                    messages.text("waystone.already-activated", waystone.name())
            );
        }
    }

    private String defaultName(Block block) {
        return block.getWorld().getName()
                + " (" + block.getX() + ", " + block.getZ() + ")";
    }
}
