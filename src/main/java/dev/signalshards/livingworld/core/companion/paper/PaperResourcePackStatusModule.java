package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.ResourcePackSessionStore;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
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
    private final EnvironmentalPerformanceMetrics performanceMetrics;
    private boolean active;

    public PaperResourcePackStatusModule(
            Plugin plugin,
            ResourcePackSessionStore sessions
    ) {
        this(
                plugin,
                sessions,
                new EnvironmentalPerformanceMetrics()
        );
    }

    public PaperResourcePackStatusModule(
            Plugin plugin,
            ResourcePackSessionStore sessions,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.sessions = Objects.requireNonNull(sessions, "sessões de resource pack");
        this.performanceMetrics = Objects.requireNonNull(
                performanceMetrics,
                "métricas de performance"
        );
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
        boolean ownedRequest = sessions
                .session(event.getPlayer().getUniqueId())
                .filter(session -> session.requestId().equals(event.getID()))
                .isPresent();
        sessions.apply(
                event.getPlayer().getUniqueId(),
                event.getID(),
                PaperResourcePackStatusMapper.fromPaper(event.getStatus())
        );
        if (ownedRequest) {
            performanceMetrics.increment(
                    EnvironmentalMetric.RESOURCE_PACK_STATUS_EVENTS
            );
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessions.reset(event.getPlayer().getUniqueId());
    }
}
