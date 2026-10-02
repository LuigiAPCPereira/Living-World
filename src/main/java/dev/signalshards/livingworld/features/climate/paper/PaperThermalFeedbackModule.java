package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import dev.signalshards.livingworld.features.climate.application.ThermalFeedbackCoordinator;
import org.bukkit.GameMode;
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
    private final PaperThermalFeedbackSettings settings;
    private final ThermalFeedbackCoordinator feedback;
    private final PaperThermalFeedbackPresenter presenter;
    private final PaperFrostFeedbackPresenter frostPresenter;
    private final LongSupplier clock;
    private final EnvironmentalPerformanceMetrics performanceMetrics;
    private final Map<UUID, Long> lastPulseNanos = new HashMap<>();
    private BukkitTask task;
    private boolean active;

    public PaperThermalFeedbackModule(
            Plugin plugin,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter,
            PaperFrostFeedbackPresenter frostPresenter
    ) {
        this(
                plugin,
                settings,
                feedback,
                presenter,
                frostPresenter,
                System::nanoTime,
                new EnvironmentalPerformanceMetrics()
        );
    }

    public PaperThermalFeedbackModule(
            Plugin plugin,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter,
            PaperFrostFeedbackPresenter frostPresenter,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this(
                plugin,
                settings,
                feedback,
                presenter,
                frostPresenter,
                System::nanoTime,
                performanceMetrics
        );
    }

    PaperThermalFeedbackModule(
            Plugin plugin,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter,
            PaperFrostFeedbackPresenter frostPresenter,
            LongSupplier clock
    ) {
        this(
                plugin,
                settings,
                feedback,
                presenter,
                frostPresenter,
                clock,
                new EnvironmentalPerformanceMetrics()
        );
    }

    PaperThermalFeedbackModule(
            Plugin plugin,
            PaperThermalFeedbackSettings settings,
            ThermalFeedbackCoordinator feedback,
            PaperThermalFeedbackPresenter presenter,
            PaperFrostFeedbackPresenter frostPresenter,
            LongSupplier clock,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração de feedback");
        this.feedback = Objects.requireNonNull(feedback, "coordenador de feedback");
        this.presenter = Objects.requireNonNull(presenter, "presenter térmico");
        this.frostPresenter = Objects.requireNonNull(
                frostPresenter,
                "presenter de frost"
        );
        this.clock = Objects.requireNonNull(clock, "relógio");
        this.performanceMetrics = Objects.requireNonNull(
                performanceMetrics,
                "métricas de performance"
        );
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
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearFrostSafely(player);
        }
        HandlerList.unregisterAll(this);
        lastPulseNanos.clear();
    }

    void pulse() {
        if (!active) {
            return;
        }
        long now = clock.getAsLong();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }
            if (player.isDead()
                    || player.getGameMode() == GameMode.SPECTATOR) {
                clearFrostSafely(player);
                lastPulseNanos.remove(player.getUniqueId());
                continue;
            }
            UUID playerId = player.getUniqueId();
            var profile = feedback.profile(playerId);
            if (profile.isEmpty()) {
                clearFrostSafely(player);
                lastPulseNanos.remove(playerId);
                continue;
            }
            boolean breathActive = settings.coldBreathEnabled()
                    && profile.orElseThrow().breath().enabled();
            boolean frostActive = settings.frostEnabled()
                    && profile.orElseThrow().frostEnabled();
            if (!breathActive && !frostActive) {
                clearFrostSafely(player);
                lastPulseNanos.remove(playerId);
                continue;
            }
            Long previous = lastPulseNanos.put(playerId, now);
            Duration elapsed = previous == null || now <= previous
                    ? Duration.ZERO
                    : Duration.ofNanos(now - previous);
            try {
                feedback.pulse(playerId, elapsed).ifPresent(decision -> {
                    if (settings.coldBreathEnabled() && decision.emitBreath()) {
                        presenter.emitBreath(
                                player,
                                decision.profile().breath().intensity()
                        );
                        performanceMetrics.increment(
                                EnvironmentalMetric.BREATH_PRESENTATIONS
                        );
                    }
                    if (settings.frostEnabled()
                            && decision.profile().frostEnabled()) {
                        frostPresenter.presentFrost(
                                player,
                                decision.profile().frostIntensity()
                        );
                        performanceMetrics.increment(
                                EnvironmentalMetric.FROST_PRESENTATIONS
                        );
                    } else {
                        clearFrostSafely(player);
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
        clearFrostSafely(event.getPlayer());
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        clearFrostSafely(event.getPlayer());
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        clearFrostSafely(event.getPlayer());
        lastPulseNanos.remove(event.getPlayer().getUniqueId());
    }

    private void clearFrostSafely(Player player) {
        try {
            frostPresenter.clearFrost(player);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Falha ao limpar frost visual do jogador "
                            + player.getUniqueId(),
                    exception
            );
        }
    }
}
