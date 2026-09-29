package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.status.LivingWorldStatusSnapshot;
import dev.signalshards.livingworld.features.climate.application.ClimateRuleReadout;
import dev.signalshards.livingworld.features.climate.application.LocalClimateReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import dev.signalshards.livingworld.features.waystones.application.WaystoneAccessStore;
import dev.signalshards.livingworld.features.waystones.application.WaystoneRegistry;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWaystoneCommandTest {
    @Test
    void statusAceitaConsoleSemPassarPeloPlayerOnly() {
        AtomicInteger messagesSent = new AtomicInteger();
        CommandSender console = proxy(
                CommandSender.class,
                (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage")) {
                        messagesSent.incrementAndGet();
                        return null;
                    }
                    return defaultValue(proxy, method.getName(), args);
                }
        );
        CommandSourceStack source = proxy(
                CommandSourceStack.class,
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSender" -> console;
                    default -> defaultValue(proxy, method.getName(), args);
                }
        );

        PaperWaystoneCommand command = command();
        command.execute(source, new String[]{"status"});

        assertEquals(11, messagesSent.get());
        assertEquals(List.of("status"), List.copyOf(command.suggest(source, new String[0])));
    }

    @Test
    void thermalMostraDiagnosticoSomenteLeituraAoJogador() {
        AtomicInteger messagesSent = new AtomicInteger();
        Player player = proxy(
                Player.class,
                (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage")) {
                        messagesSent.incrementAndGet();
                        return null;
                    }
                    return defaultValue(proxy, method.getName(), args);
                }
        );
        CommandSourceStack source = proxy(
                CommandSourceStack.class,
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSender" -> player;
                    default -> defaultValue(proxy, method.getName(), args);
                }
        );

        PaperWaystoneCommand command = command();
        command.execute(source, new String[]{"thermal"});

        assertEquals(6, messagesSent.get());
        assertTrue(command.suggest(source, new String[]{""}).contains("thermal"));
    }

    private PaperWaystoneCommand command() {
        WaystoneRegistry registry = new WaystoneRegistry() {
            @Override
            public void register(Waystone waystone) {
            }

            @Override
            public Optional<Waystone> find(WaystoneId id) {
                return Optional.empty();
            }

            @Override
            public Optional<Waystone> findAt(UUID worldId, int x, int y, int z) {
                return Optional.empty();
            }

            @Override
            public List<Waystone> all() {
                return new ArrayList<>();
            }

            @Override
            public boolean remove(WaystoneId id) {
                return false;
            }
        };
        WaystoneAccessStore access = new WaystoneAccessStore() {
            @Override
            public Set<WaystoneId> load(UUID playerId) {
                return Set.of();
            }

            @Override
            public boolean add(UUID playerId, WaystoneId waystoneId) {
                return false;
            }
        };
        WaystoneService service = new WaystoneService(registry, access);
        Server server = proxy(
                Server.class,
                (proxy, method, args) -> defaultValue(proxy, method.getName(), args)
        );
        PaperWaystoneTravelService travel = new PaperWaystoneTravelService(
                server,
                service,
                new PaperSafeWaystoneDestination()
        );
        PaperWaystoneMenu menu = new PaperWaystoneMenu(
                service,
                travel,
                MessageCatalog.fromLanguageTag("pt-BR"),
                org.bukkit.Material.LODESTONE
        );

        return new PaperWaystoneCommand(
                service,
                travel,
                MessageCatalog.fromLanguageTag("pt-BR"),
                () -> new LivingWorldStatusSnapshot(
                        "teste",
                        "world",
                        "Primavera",
                        1,
                        2,
                        3,
                        1,
                        1,
                        2,
                        3,
                        20.0D,
                        4.2D
                ),
                6.0D,
                menu,
                player -> new LocalClimateReadout(
                        Season.OUTONO,
                        23,
                        ThermalBand.QUENTE,
                        MoistureBand.SECO,
                        WeatherTendency.TEMPO_LIMPO,
                        new ClimateRuleReadout(true, 79),
                        new ClimateRuleReadout(true, 89),
                        new ClimateRuleReadout(true, 84),
                        new ClimateRuleReadout(true, 0),
                        new ClimateRuleReadout(true, 100),
                        true,
                        false
                ),
                player -> new ThermalRuntimeReadout(
                        PlayerThermalBand.COMFORTABLE,
                        0.0D,
                        0.0D,
                        20.0D,
                        PlayerActivity.RESTING,
                        0.0D,
                        0.0D,
                        0.0D,
                        DirectThermalExposure.NONE,
                        0.0D,
                        0.0D,
                        0,
                        0,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,
                        new dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile(
                                dev.signalshards.livingworld.features.climate.domain.BreathFeedback.none(),
                                0.0D
                        )
                )
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        );
    }

    private Object defaultValue(Object proxy, String methodName, Object[] args) {
        if (methodName.equals("toString")) {
            return "Fake";
        }
        if (methodName.equals("hashCode")) {
            return System.identityHashCode(proxy);
        }
        if (methodName.equals("equals")) {
            return proxy == args[0];
        }
        return null;
    }
}
