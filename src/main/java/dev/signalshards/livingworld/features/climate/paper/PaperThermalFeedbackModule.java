package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.application.ThermalFeedbackCoordinator;
import org.bukkit.GameMode;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.logging.Level;

/**
 * Pulse visual barato sobre profiles já observados pelo runtime térmico coarse.
 *
 * <p>Não resolve mundo, não recalcula clima e não altera estado térmico.
 * Cada jogador pode emitir no máximo um breath por pulse.</p>
 */
public final class PaperThermalFeedbackModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperThermalFeedbackSettings settings;
    private final ThermalFeedbackCoordinator feedback;
    private final PaperThermalFeedbackPresenter presenter;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastPulseNanos = new HashMap<>();
    private BukkitTask task;
    private boolean active;

    public PaperThermalFeedbackModule(
            Plugin plugin,
            World world,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter
    ) {
        this(
                plugin,
                world,
                settings,
                feedback,
                presenter,
                System::nanoTime
        );
    }

    PaperThermalFeedbackModule(
            Plugin plugin,
            World world,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter,
            LongSupplier clock
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração de feedback");
        this.feedback = Objects.requireNonNull(feedback, "coordenador de feedback");
        this.presenter = Objects.requireNonNull(presenter, "presenter térmico");
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
        lastPulseNanos.clear();
    }

    void pulse() {
        if (!active) {
            return;
        }
        long now = clock.getAsLong();
        for (Player player : world.getPlayers()) {
            if (!player.isOnline()
                    || player.isDead()
                    || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            UUID playerId = player.getUniqueId();
            if (!feedback.breathEligible(playerId)) {
                lastPulseNanos.remove(playerId);
                continue;
            }
            Long previous = lastPulseNanos.put(playerId, now);
            Duration elapsed = previous == null || now <= previous
                    ? Duration.ZERO
                    : Duration.ofNanos(now - previous);
            try {
                feedback.pulse(playerId, elapsed).ifPresent(decision -> {
                    if (decision.emitBreath()) {
                        presenter.emitBreath(
                                player,
                                decision.profile().breath().intensity()
                        );
                    }
                });
            } catch (RuntimeException exception) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Falha ao apresentar feedback térmico do jogador " + playerId,
                        exception
                );
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }
}
