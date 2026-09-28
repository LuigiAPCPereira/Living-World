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
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperClimateCoordinatorTest {
    @Test
    void reageUmaVezAoEstadoFinalDoDiaComPlanoLimitado() {
        List<String> weatherCalls = new ArrayList<>();
        AtomicInteger announcements = new AtomicInteger();
        World world = fakeWorld(weatherCalls, announcements);
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
                new PaperWeatherEventAnnouncement(
                        new MessageCatalog(Locale.forLanguageTag("pt-BR")),
                        true
                ),
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
        assertEquals(1, announcements.get());
    }

    private World fakeWorld(
            List<String> weatherCalls,
            AtomicInteger announcements
    ) {
        final World[] holder = new World[1];
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> new Location(holder[0], 0, 64, 0);
                    case "sendMessage" -> {
                        announcements.incrementAndGet();
                        yield null;
                    }
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayers" -> List.of(player);
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
                    default -> defaultValue(method.getReturnType());
                }
        );
        holder[0] = world;
        return world;
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        if (type == double.class) {
            return 0.0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
