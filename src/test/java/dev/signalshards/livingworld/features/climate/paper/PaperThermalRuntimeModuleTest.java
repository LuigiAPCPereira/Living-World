package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.InMemoryThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.application.ThermalFeedbackCoordinator;
import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperThermalRuntimeModuleTest {
    @Test
    void primeiroPulsoInicializaRelogioESegundoAcumulaEstado() {
        Fixture f = new Fixture(true);
        f.module.enable();

        f.module.pulse();
        assertTrue(f.runtime.snapshot(f.playerId).isEmpty());

        f.now = 1_000_000_000L;
        f.module.pulse();

        var snapshot = f.runtime.snapshot(f.playerId).orElseThrow();
        assertTrue(snapshot.thermalState().thermalLoad() < 0.0D);
        var readout = f.readouts.load(f.playerId).orElseThrow();
        assertEquals(-20.0D, readout.ambientCelsius(), 1.0E-9D);
        assertTrue(f.feedback.profile(f.playerId).isPresent());
        assertEquals(1, f.environmentReads);
        assertEquals(1, f.registrations);
        assertEquals(1, f.schedules);
        assertEquals(
                1L,
                f.performance.snapshot().value(
                        EnvironmentalMetric.THERMAL_PLAYER_UPDATES
                )
        );
    }

    @Test
    void quitEDisableLimpamEstadoECancelamTarefa() {
        Fixture f = new Fixture(true);
        f.module.enable();
        f.module.pulse();
        f.now = 1_000_000_000L;
        f.module.pulse();
        assertTrue(f.runtime.snapshot(f.playerId).isPresent());

        f.module.onQuit(new PlayerQuitEvent(
                f.player,
                (net.kyori.adventure.text.Component) null,
                PlayerQuitEvent.QuitReason.DISCONNECTED
        ));
        assertFalse(f.runtime.snapshot(f.playerId).isPresent());
        assertTrue(f.readouts.load(f.playerId).isEmpty());
        assertTrue(f.feedback.profile(f.playerId).isEmpty());

        f.module.disable();
        assertEquals(1, f.cancellations);
    }

    @Test
    void configuracaoDesabilitadaNaoRegistraNemAgenda() {
        Fixture f = new Fixture(false);

        f.module.enable();
        f.module.pulse();

        assertEquals(0, f.registrations);
        assertEquals(0, f.schedules);
        assertEquals(0, f.environmentReads);
    }

    @Test
    void trocaDeDimensaoPreservaInerciaCorporalEReiniciaSomenteAmostragem() {
        Fixture f = new Fixture(true);
        f.module.enable();
        f.module.pulse();
        f.now = 1_000_000_000L;
        f.module.pulse();
        var before = f.runtime.snapshot(f.playerId).orElseThrow();

        f.module.onChangedWorld(new PlayerChangedWorldEvent(
                f.player,
                f.previousWorld
        ));

        assertEquals(
                before,
                f.runtime.snapshot(f.playerId).orElseThrow()
        );
        assertTrue(f.feedback.profile(f.playerId).isEmpty());

        f.now = 2_000_000_000L;
        f.module.pulse();
        assertEquals(1, f.environmentReads);
        assertEquals(
                before,
                f.runtime.snapshot(f.playerId).orElseThrow()
        );
    }

    private static final class Fixture {
        long now;
        int environmentReads;
        int registrations;
        int schedules;
        int cancellations;
        final UUID playerId = UUID.randomUUID();
        final PlayerThermalRuntimeService runtime = new PlayerThermalRuntimeService();
        final InMemoryThermalRuntimeReadoutStore readouts =
                new InMemoryThermalRuntimeReadoutStore();
        final ThermalFeedbackCoordinator feedback = new ThermalFeedbackCoordinator();
        final EnvironmentalPerformanceMetrics performance =
                new EnvironmentalPerformanceMetrics();
        final Player player = stub(Player.class, (proxy, method, args) -> switch (method.getName()) {
            case "getUniqueId" -> playerId;
            case "isOnline" -> true;
            case "isDead" -> false;
            default -> unexpected(method.getName());
        });
        final World previousWorld = stub(
                World.class,
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> "PreviousWorld";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> unexpected(method.getName());
                }
        );
        final BukkitTask task = stub(BukkitTask.class, (proxy, method, args) -> switch (method.getName()) {
            case "cancel" -> {
                cancellations++;
                yield null;
            }
            default -> unexpected(method.getName());
        });
        final PluginManager pluginManager = stub(PluginManager.class, (proxy, method, args) -> {
            if (method.getName().equals("registerEvents")) {
                registrations++;
                return null;
            }
            return unexpected(method.getName());
        });
        final BukkitScheduler scheduler = stub(BukkitScheduler.class, (proxy, method, args) -> {
            if (method.getName().equals("runTaskTimer")) {
                schedules++;
                return task;
            }
            return unexpected(method.getName());
        });
        final Server server = stub(Server.class, (proxy, method, args) -> switch (method.getName()) {
            case "getPluginManager" -> pluginManager;
            case "getScheduler" -> scheduler;
            case "getOnlinePlayers" -> List.of(player);
            default -> unexpected(method.getName());
        });
        final Plugin plugin = stub(Plugin.class, (proxy, method, args) -> switch (method.getName()) {
            case "getServer" -> server;
            case "getLogger" -> Logger.getLogger("PaperThermalRuntimeModuleTest");
            default -> unexpected(method.getName());
        });
        final PaperThermalEnvironmentProvider environment = ignored -> {
            environmentReads++;
            return new ThermalEnvironmentContext(
                    new AmbientTemperature(-20.0D),
                    WaterExposure.none(),
                    PlayerActivity.RESTING,
                    WindExposure.calm(),
                    ShelterFactor.exposed(),
                    ArmorThermalLoadout.empty(),
                    List.of()
            );
        };
        final PaperThermalRuntimeModule module;

        Fixture(boolean enabled) {
            module = new PaperThermalRuntimeModule(
                    plugin,
                    new PaperThermalRuntimeSettings(enabled, 20L),
                    environment,
                    runtime,
                    feedback,
                    readouts,
                    () -> now,
                    performance,
                    () -> now
            );
        }
    }

    private static Object unexpected(String method) {
        throw new AssertionError("Chamada não esperada: " + method);
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        ));
    }
}
