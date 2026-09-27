package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlanner;
import dev.signalshards.livingworld.features.climate.application.WeatherEventSettings;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperClimateCoordinatorTest {
    @Test
    void reageUmaVezAoEstadoFinalDoDiaComPlanoLimitado() {
        List<String> weatherCalls = new ArrayList<>();
        World world = fakeWorld(weatherCalls);
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        PaperClimateCoordinator coordinator = new PaperClimateCoordinator(
                world,
                new PaperClimateSampler(
                        new ClimateProfileClassifier(
                                ClimateProfileThresholds.livingWorldDefaults()
                        )
                ),
                new ClimatePolicy(),
                new WeatherEventPlanner(WeatherEventSettings.defaults()),
                new PaperWeatherController(),
                SeasonCycle.livingWorldDefaults(),
                new MessageCatalog(Locale.forLanguageTag("pt-BR")),
                logger
        );

        coordinator.onProgress(new CalendarProgress(
                3,
                new CalendarDate(1, 3, 6),
                new CalendarDate(1, 4, 1),
                Optional.empty()
        ));

        assertEquals(List.of(
                "setThundering:false",
                "setStorm:false",
                "setClearWeatherDuration:12000"
        ), weatherCalls);
    }

    private World fakeWorld(List<String> weatherCalls) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayers" -> List.of();
                    case "getSpawnLocation" -> new Location((World) proxy, 0, 64, 0);
                    case "getTemperature" -> 0.8;
                    case "getHumidity" -> 0.5;
                    case "getName" -> "mundo-teste";
                    case "setThundering", "setStorm", "setClearWeatherDuration",
                         "setWeatherDuration", "setThunderDuration" -> {
                        weatherCalls.add(method.getName() + ":" + args[0]);
                        yield null;
                    }
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
