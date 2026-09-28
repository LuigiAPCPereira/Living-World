package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.ClimateProfile;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperClimateSamplerTest {
    private final PaperClimateSampler sampler = new PaperClimateSampler(
            new ClimateProfileClassifier(ClimateProfileThresholds.livingWorldDefaults())
    );

    @Test
    void usaSpawnQuandoNaoHaJogadores() {
        AtomicInteger temperatureCalls = new AtomicInteger();
        World world = fakeWorld(List.of(), temperatureCalls, 0.8, 0.5);

        ClimateProfile profile = sampler.sample(world);

        assertEquals(
                new ClimateProfile(ThermalBand.TEMPERADO, MoistureBand.EQUILIBRADO),
                profile
        );
        assertEquals(1, temperatureCalls.get());
    }

    @Test
    void limitaAmostragemDeJogadores() {
        AtomicInteger temperatureCalls = new AtomicInteger();
        List<Player> players = new ArrayList<>();
        World[] holder = new World[1];

        for (int index = 0; index < 40; index++) {
            int coordinate = index;
            players.add(fakePlayer(() -> new Location(holder[0], coordinate, 64, coordinate)));
        }

        World world = fakeWorld(players, temperatureCalls, 0.8, 0.5);
        holder[0] = world;

        sampler.sample(world);

        assertEquals(PaperClimateSampler.MAX_PLAYER_SAMPLES, temperatureCalls.get());
    }

    @Test
    void worldgenCustomizadoEhClassificadoSoPorTemperaturaEUmidade() {
        AtomicInteger temperatureCalls = new AtomicInteger();
        World world = fakeWorld(
                List.of(),
                temperatureCalls,
                0.15D,
                0.90D
        );

        ClimateProfile profile = sampler.sample(world);

        assertEquals(
                new ClimateProfile(
                        ThermalBand.FRIO,
                        MoistureBand.ENCHARCADO
                ),
                profile
        );
        assertEquals(1, temperatureCalls.get());
    }

    private World fakeWorld(
            List<Player> players,
            AtomicInteger temperatureCalls,
            double temperature,
            double humidity
    ) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayers" -> players;
                    case "getSpawnLocation" -> new Location((World) proxy, 0, 64, 0);
                    case "getTemperature" -> {
                        temperatureCalls.incrementAndGet();
                        yield temperature;
                    }
                    case "getHumidity" -> humidity;
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Player fakePlayer(java.util.function.Supplier<Location> locationSupplier) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> locationSupplier.get();
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
