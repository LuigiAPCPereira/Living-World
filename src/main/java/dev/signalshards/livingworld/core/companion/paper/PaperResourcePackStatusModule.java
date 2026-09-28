package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.ResourcePackSessionStore;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Observa apenas requisições previamente registradas pelo Living World.
 */
public final class PaperResourcePackStatusModule
        implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final ResourcePackSessionStore sessions;
    private boolean active;

    public PaperResourcePackStatusModule(
            Plugin plugin,
            ResourcePackSessionStore sessions
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.sessions = Objects.requireNonNull(sessions, "sessões de resource pack");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
    }

    @Override
    public void disable() {
        active = false;
        HandlerList.unregisterAll(this);
        sessions.clear();
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!active) {
            return;
        }
        sessions.apply(
                event.getPlayer().getUniqueId(),
                event.getID(),
                PaperResourcePackStatusMapper.fromPaper(event.getStatus())
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessions.reset(event.getPlayer().getUniqueId());
    }
}
