package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.application.ThermalFeedbackCoordinator;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.bukkit.Server;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
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

class PaperThermalFeedbackModuleTest {
    @Test
    void profileFrioEmiteDepoisDaCadenciaSemNovoProbeAmbiental() {
        Fixture f = new Fixture(true);
        f.feedback.observe(
                f.playerId,
                PlayerThermalState.neutral(),
                coldEnvironment()
        );
        f.module.enable();

        f.module.pulse();
        assertEquals(0, f.emissions);

        f.now = 5_000_000_000L;
        f.module.pulse();

        assertEquals(1, f.emissions);
        assertEquals(1, f.registrations);
        assertEquals(1, f.schedules);
    }

    @Test
    void atrasoLongoAindaEmiteNoMaximoUmaVezPorPulse() {
        Fixture f = new Fixture(true);
        f.feedback.observe(
                f.playerId,
                PlayerThermalState.neutral(),
                coldEnvironment()
        );
        f.module.enable();
        f.module.pulse();

        f.now = 300_000_000_000L;
        f.module.pulse();

        assertEquals(1, f.emissions);
    }

    @Test
    void configuracaoDesabilitadaNaoAgendaNemEmite() {
        Fixture f = new Fixture(false);
        f.feedback.observe(
                f.playerId,
                PlayerThermalState.neutral(),
                coldEnvironment()
        );

        f.module.enable();
        f.module.pulse();

        assertEquals(0, f.registrations);
        assertEquals(0, f.schedules);
        assertEquals(0, f.emissions);
    }

    @Test
    void spectatorNaoEmiteMesmoComProfileFrio() {
        Fixture f = new Fixture(true, GameMode.SPECTATOR);
        f.feedback.observe(
                f.playerId,
                PlayerThermalState.neutral(),
                coldEnvironment()
        );
        f.module.enable();
        f.module.pulse();
        f.now = 10_000_000_000L;
        f.module.pulse();

        assertEquals(0, f.emissions);
    }

    @Test
    void profileQuenteNaoEntraNaCadenciaVisual() {
        Fixture f = new Fixture(true);
        f.feedback.observe(
                f.playerId,
                PlayerThermalState.neutral(),
                new ThermalEnvironmentContext(
                        new AmbientTemperature(20.0D),
                        WaterExposure.none(),
                        PlayerActivity.RESTING,
                        WindExposure.calm(),
                        ShelterFactor.exposed(),
                        ArmorThermalLoadout.empty(),
                        List.of()
                )
        );
        f.module.enable();
        f.module.pulse();
        f.now = 30_000_000_000L;
        f.module.pulse();

        assertEquals(0, f.emissions);
    }

    @Test
    void disableCancelaTarefaELimpaRelogioLocal() {
        Fixture f = new Fixture(true);
        f.module.enable();

        f.module.disable();

        assertEquals(1, f.cancellations);
    }

    private static ThermalEnvironmentContext coldEnvironment() {
        return new ThermalEnvironmentContext(
                new AmbientTemperature(-15.0D),
                WaterExposure.none(),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                List.of()
        );
    }

    private static final class Fixture {
        long now;
        int emissions;
        int registrations;
        int schedules;
        int cancellations;
        final UUID playerId = UUID.randomUUID();
        final ThermalFeedbackCoordinator feedback = new ThermalFeedbackCoordinator();
        final GameMode gameMode;
        final Player player;
        final World world;
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
            default -> unexpected(method.getName());
        });
        final Plugin plugin = stub(Plugin.class, (proxy, method, args) -> switch (method.getName()) {
            case "getServer" -> server;
            case "getLogger" -> Logger.getLogger("PaperThermalFeedbackModuleTest");
            default -> unexpected(method.getName());
        });
        final PaperThermalFeedbackModule module;

        Fixture(boolean enabled) {
            this(enabled, GameMode.SURVIVAL);
        }

        Fixture(boolean enabled, GameMode gameMode) {
            this.gameMode = gameMode;
            player = stub(Player.class, (proxy, method, args) -> switch (method.getName()) {
                case "getUniqueId" -> playerId;
                case "isOnline" -> true;
                case "isDead" -> false;
                case "getGameMode" -> this.gameMode;
                default -> unexpected(method.getName());
            });
            world = stub(World.class, (proxy, method, args) -> switch (method.getName()) {
                case "getPlayers" -> List.of(player);
                default -> unexpected(method.getName());
            });
            module = new PaperThermalFeedbackModule(
                    plugin,
                    world,
                    new PaperThermalFeedbackSettings(enabled, 10L),
                    feedback,
                    (ignored, intensity) -> emissions++,
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
