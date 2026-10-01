package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.InMemoryThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.application.ThermalFeedbackCoordinator;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutAssembler;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
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
 * <p>Executa para jogadores online em qualquer dimensão, em cadence coarse.
 * Resolvers observam o mundo atual do jogador; toda matemática permanece na
 * camada application/domain.</p>
 */
public final class PaperThermalRuntimeModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final PaperThermalRuntimeSettings settings;
    private final PaperThermalEnvironmentProvider environmentProvider;
    private final PlayerThermalRuntimeService runtime;
    private final ThermalFeedbackCoordinator feedback;
    private final ThermalRuntimeReadoutStore readouts;
    private final ThermalRuntimeReadoutAssembler readoutAssembler;
    private final LongSupplier clock;
    private final EnvironmentalPerformanceMetrics performanceMetrics;
    private final LongSupplier performanceClock;
    private final Map<UUID, Long> lastUpdateNanos = new HashMap<>();
    private final Set<UUID> trackedPlayers = new HashSet<>();
    private BukkitTask task;
    private boolean active;

    public PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback
    ) {
        this(
                plugin,
                settings,
                environmentProvider,
                runtime,
                feedback,
                new InMemoryThermalRuntimeReadoutStore(),
                System::nanoTime,
                new EnvironmentalPerformanceMetrics(),
                System::nanoTime
        );
    }

    public PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this(
                plugin,
                settings,
                environmentProvider,
                runtime,
                feedback,
                new InMemoryThermalRuntimeReadoutStore(),
                System::nanoTime,
                performanceMetrics,
                System::nanoTime
        );
    }

    public PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback,
            ThermalRuntimeReadoutStore readouts,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this(
                plugin,
                settings,
                environmentProvider,
                runtime,
                feedback,
                readouts,
                System::nanoTime,
                performanceMetrics,
                System::nanoTime
        );
    }

    PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback,
            LongSupplier clock
    ) {
        this(
                plugin,
                settings,
                environmentProvider,
                runtime,
                feedback,
                new InMemoryThermalRuntimeReadoutStore(),
                clock,
                new EnvironmentalPerformanceMetrics(),
                System::nanoTime
        );
    }

    PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback,
            LongSupplier clock,
            EnvironmentalPerformanceMetrics performanceMetrics,
            LongSupplier performanceClock
    ) {
        this(
                plugin,
                settings,
                environmentProvider,
                runtime,
                feedback,
                new InMemoryThermalRuntimeReadoutStore(),
                clock,
                performanceMetrics,
                performanceClock
        );
    }

    PaperThermalRuntimeModule(
            Plugin plugin,
            PaperThermalRuntimeSettings settings,
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalFeedbackCoordinator feedback,
            ThermalRuntimeReadoutStore readouts,
            LongSupplier clock,
            EnvironmentalPerformanceMetrics performanceMetrics,
            LongSupplier performanceClock
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração térmica");
        this.environmentProvider = Objects.requireNonNull(
                environmentProvider,
                "resolver ambiental"
        );
        this.runtime = Objects.requireNonNull(runtime, "runtime térmico");
        this.feedback = Objects.requireNonNull(feedback, "coordenador de feedback");
        this.readouts = Objects.requireNonNull(readouts, "store de readout");
        this.readoutAssembler = new ThermalRuntimeReadoutAssembler();
        this.clock = Objects.requireNonNull(clock, "relógio");
        this.performanceMetrics = Objects.requireNonNull(
                performanceMetrics,
                "métricas de performance"
        );
        this.performanceClock = Objects.requireNonNull(
                performanceClock,
                "relógio de performance"
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
        HandlerList.unregisterAll(this);
        for (UUID playerId : trackedPlayers) {
            runtime.reset(playerId);
        }
        feedback.clear();
        readouts.clear();
        trackedPlayers.clear();
        lastUpdateNanos.clear();
    }

    void pulse() {
        if (!active) {
            return;
        }
        long started = performanceClock.getAsLong();
        try {
            long now = clock.getAsLong();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.isOnline() || player.isDead()) {
                    continue;
                }
                UUID playerId = player.getUniqueId();
                Long previous = lastUpdateNanos.put(playerId, now);
                if (previous == null || now <= previous) {
                    continue;
                }
                try {
                    var environment = environmentProvider.forPlayer(player);
                    var result = runtime.advance(
                            playerId,
                            environment,
                            Duration.ofNanos(now - previous)
                    );
                    var body = new PlayerThermalSnapshot(
                            result.thermalState(),
                            result.wetnessState()
                    );
                    readouts.save(
                            playerId,
                            readoutAssembler.assemble(body, environment)
                    );
                    feedback.observe(
                            playerId,
                            result.thermalState(),
                            environment
                    );
                    trackedPlayers.add(playerId);
                    performanceMetrics.increment(
                            EnvironmentalMetric.THERMAL_PLAYER_UPDATES
                    );
                } catch (RuntimeException exception) {
                    plugin.getLogger().log(
                            Level.WARNING,
                            "Falha ao atualizar estado térmico do jogador " + playerId,
                            exception
                    );
                }
            }
        } finally {
            performanceMetrics.add(
                    EnvironmentalMetric.THERMAL_UPDATE_NANOS,
                    Math.max(
                            0L,
                            performanceClock.getAsLong() - started
                    )
            );
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
        UUID playerId = event.getPlayer().getUniqueId();
        lastUpdateNanos.remove(playerId);
        feedback.reset(playerId);
        readouts.remove(playerId);
    }

    private void reset(UUID playerId) {
        lastUpdateNanos.remove(playerId);
        trackedPlayers.remove(playerId);
        runtime.reset(playerId);
        feedback.reset(playerId);
        readouts.remove(playerId);
    }
}
