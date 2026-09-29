package dev.signalshards.livingworld.features.biomes.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.biomes.application.BiomeEntryTracker;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Adapter Paper bounded para M15.
 *
 * <p>Observa apenas movimento que realmente cruza bloco. Teleportes também passam
 * por {@link PlayerMoveEvent}, portanto o destino final é observado sem polling,
 * scheduler ou scan de chunks.</p>
 */
public final class PaperBiomeDiscoveryModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final BiomeEntryTracker tracker;

    public PaperBiomeDiscoveryModule(
            JavaPlugin plugin,
            BiomeEntryTracker tracker
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.tracker = Objects.requireNonNull(tracker, "tracker de biomas");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        tracker.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) {
            return;
        }

        Location destination = event.getTo();
        String biomeKey = destination
                .getBlock()
                .getBiome()
                .getKey()
                .toString();

        tracker.observe(event.getPlayer().getUniqueId(), biomeKey);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        tracker.forget(event.getPlayer().getUniqueId());
    }
}
