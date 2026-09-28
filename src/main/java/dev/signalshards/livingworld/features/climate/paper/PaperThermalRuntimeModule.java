package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.logging.Level;

/**
 * Owner Paper do estado térmico runtime por jogador.
 *
 * <p>Executa somente no mundo climático configurado, em cadence coarse.
 * Resolvers observam o mundo; toda matemática permanece na camada application/domain.</p>
 */
public final class PaperThermalRuntimeModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperThermalRuntimeSettings settings;
    private final PaperThermalEnvironmentProvider environmentProvider;
    private final PlayerThermalRuntimeService runtime;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastUpdateNanos = new HashMap<>();
    private final Set<UUID> trackedPlayers = new HashSet<>();
    private BukkitTask task;
    private boolean active;

    public PaperThermalRuntimeModule(
            Plugin plugin,
            World world,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime
    ) {
        this(
                plugin,
                world,
                settings,
                environmentProvider,
                runtime,
                System::nanoTime
        );
    }

    PaperThermalRuntimeModule(
            Plugin plugin,
            World world,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            LongSupplier clock
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração térmica");
        this.environmentProvider = Objects.requireNonNull(
                environmentProvider,
                "resolver ambiental"
        );
        this.runtime = Objects.requireNonNull(runtime, "runtime térmico");
        this.clock = Objects.requireNonNull(clock, "relógio");
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
        long period = settings.updatePeriodTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::pulse,
                period,
                period
        );
    }

    @Override
    public void disable() {
        active = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(this);
        for (UUID playerId : trackedPlayers) {
            runtime.reset(playerId);
        }
        trackedPlayers.clear();
        lastUpdateNanos.clear();
    }

    void pulse() {
        if (!active) {
            return;
        }
        long now = clock.getAsLong();
        for (Player player : world.getPlayers()) {
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            UUID playerId = player.getUniqueId();
            Long previous = lastUpdateNanos.put(playerId, now);
            if (previous == null || now <= previous) {
                continue;
            }
            try {
                runtime.advance(
                        playerId,
                        environmentProvider.forPlayer(player),
                        Duration.ofNanos(now - previous)
                );
                trackedPlayers.add(playerId);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Falha ao atualizar estado térmico do jogador " + playerId,
                        exception
                );
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        reset(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        reset(event.getPlayer().getUniqueId());
        lastUpdateNanos.put(event.getPlayer().getUniqueId(), clock.getAsLong());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        lastUpdateNanos.remove(event.getPlayer().getUniqueId());
    }

    private void reset(UUID playerId) {
        lastUpdateNanos.remove(playerId);
        trackedPlayers.remove(playerId);
        runtime.reset(playerId);
    }
}
